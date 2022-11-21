use log::*;
use std::time::Duration;
use std::thread;
use std::net::{UdpSocket, SocketAddr, ToSocketAddrs, TcpStream};
use measurement_shared_rust::crusp_error::CruspError;
use measurement_shared_rust::token::Token;
use measurement_shared_rust::ports::Ports;
use measurement_shared_rust::crusp::{collect_udp, punch_udp_hole, convert_raw_packets, get_next_power_of_two, crusp_send, HTTP_REQUEST_TIMEOUT_IN_MILLIS, TCP_REQUEST_TIMEOUT_IN_MILLIS, TCP_BUFFER_SIZE};
use measurement_shared_rust::results::*;
use reqwest;
use reqwest::StatusCode;
use net2::{UdpSocketExt, TcpStreamExt};
use std::sync::Arc;
use std::io::Read;
use minreq; //i added

/**************************************************************************************************
CRUSP Downlink
**************************************************************************************************/
/// returns ports open by the measurment service
pub fn init_measurement_request(token: &Token, measurement_uri: String) -> Result<Ports, CruspError> {
    let client = reqwest::Client::builder()
        .timeout(Duration::from_millis(HTTP_REQUEST_TIMEOUT_IN_MILLIS))
        .build()?;

    info!("\n\n{}\n", measurement_uri.as_str());
    //info!("\n\n{:?}\n", token);
    // let mut response = client
    //     .post(measurement_uri.as_str())
    //     .json("{}")
    //     .send()?;
    //let mut response = reqwest::get("http://127.0.0.1:8099")?;
    let response = minreq::post(measurement_uri.as_str()).with_json(token)?.send()?;
    info!("{}", response.status_code);
    if response.status_code == 200 {
        info!("Measurement POST request sucessful");
        Ok( response.json()? )
    } else {
        Err(CruspError::new_communication(String::from("POST Request ") + response.status_code.to_string().as_str()))
    }
}

/// Measures the downlink of a connection using CRUSP
///
/// # Arguments
///
/// * `token` - The CRUSP token containing the CRUSP settings
///
/// * `host` - Address of measurement host
///
/// * `port` - UDP port open for measurement
///
/// * `path` - The path adhered to host and port representing the measurement service
///
pub fn measure_downlink(token: Token, host: String, port: u32, path: String) -> Result<MeasurementResult, CruspError> {
    // Uplink request to measurement service
    let measurement_uri_start = format!("http://{}:{}/{}", host, port, path);
    println!("\n broke here! \n");
    let ports = init_measurement_request(&token, measurement_uri_start)?; //
    println!("\n broke here!! \n");
    let socket_uri = format!("{}:{}", host, ports.udp_port); //no http since a UDP-connection is needed
    let (udp_socket, recv_addr_udp) = init_udp_communication_to_server(socket_uri)?;

    let optimal_buffer_size_udp = calculate_optimal_buffer_size(&token);
    &udp_socket.set_recv_buffer_size(optimal_buffer_size_udp)?;

    // socket needs to be cloned for collector and punch_udp_hole
    let udp_socket_arc: Arc<UdpSocket> = Arc::new(udp_socket);
    let socket_clone = udp_socket_arc.clone();

    // spawn thread to collect UDP-packets
    let collector = thread::spawn( move|| {
        return collect_udp(socket_clone, token);
    });

    punch_udp_hole(udp_socket_arc, recv_addr_udp)?;

    let raw_packets = collector // join collect-thread and current thread together again
        .join()
        .map_err(|_err| CruspError::new_error(String::from("Error when joining threads")))??;

    let sequence_coll = convert_raw_packets(raw_packets, recv_addr_udp, token)?;

    return Ok(sequence_coll.calculate_naiv_result(token));
}

/// Initializes UDP communication from client to measurement service.
/// by opening a socket connection to the provided URI
fn init_udp_communication_to_server(socket_uri: String) -> Result<(UdpSocket, SocketAddr), CruspError> {
    let recv_addr_udp =  match socket_uri.to_socket_addrs()?.next() {
        Some(addr) => {
            info!("Established UDP connection to measurement-service");
            addr
        },
        None => {
            return Err(CruspError::new_input(String::from("Couldn't parse host")))
        }
    };

    let local_addr = SocketAddr::from(([0, 0, 0, 0], 0));
    let udp_socket = UdpSocket::bind(local_addr)?;

    return Ok((udp_socket, recv_addr_udp));
}

fn calculate_optimal_buffer_size(token: &Token) -> usize{
    //To avoid dropping of UDP packets on the socket, set the buffer-size of the UDP-socket high enough.
    let total_expected_volume = token.repeats as u32 * (token.volume as u32 * 1_000);
    return get_next_power_of_two(total_expected_volume) as usize;
}

/**************************************************************************************************
CRUSP Uplink
**************************************************************************************************/

/// Measures the downlink of a connection using CRUSP
///
/// # Arguments
///
/// * `token` - The CRUSP token containing the CRUSP settings
///
/// * `host` - Address of measurement host
///
/// * `port` - UDP port open for measurement
///
/// * `path` - The path adhered to host and port representing the measurement service
///
pub fn measure_uplink(token: Token, host: String, port: u32, path: String) -> Result<MeasurementResult, CruspError> {
    // 1. uplink request to measurement service
    let measurement_uri_start = format!("http://{}:{}/{}", host, port, path);
    let ports = init_measurement_request(&token, measurement_uri_start)?; //

    // 2. connect to TCP socket of measurement service
    let tcp_socket_uri: String = format!("{}:{}", host, ports.tcp_port.unwrap()); //no http since a TCP-connection is needed
    let mut tcp_stream = init_tcp_communication_to_server(tcp_socket_uri)?;

    // 3. bind local UDP socket and init UDP connection to measurement service
    let udp_socket_uri: String = format!("{}:{}", host, ports.udp_port); //no http since a UDP-connection is needed
    let (udp_socket, recv_addr_udp) = init_udp_communication_to_server(udp_socket_uri)?;

    // 4. set optimal udp send buffer
    set_optimal_udp_socket_send_buffer(&udp_socket, &token)?;

    // 5. send UDP punch hole
    punch_udp_hole2(&udp_socket, recv_addr_udp)?;

    // 6. wait for punch hole confirmation
    wait_for_hole_punch_confirmation(&mut tcp_stream)?; // BLOCKING, max. duration: TCP_REQUEST_TIMEOUT_IN_MILLIS

    // set timeout to zero after confirmation since sending of UDP packets takes longer
    let _x = tcp_stream.set_read_timeout_ms(Some(0));

    // 7. send CRUSP UDP packets
    crusp_send(&udp_socket, recv_addr_udp, &token)?;

    // set timeout back again to TCP_REQUEST_TIMEOUT_IN_MILLIS
    let _x = tcp_stream.set_read_timeout_ms(Some(TCP_REQUEST_TIMEOUT_IN_MILLIS as u32));

    // 8. get result from measurement service
    let result = get_measurement_result_from_server(&mut tcp_stream)?;

    return Ok(result);
    // tcp_stream closes automatically when variable is out of scope
}

/// Sends optimal send buffer for udp socket
/// Crucial to avoid delays when sending
fn set_optimal_udp_socket_send_buffer(udp_socket: &UdpSocket, token: &Token) -> Result<(), CruspError>{
    let optimal_buffer_size = calculate_optimal_buffer_size(&token);
    let current_buffer_size = udp_socket.send_buffer_size()?;
    if current_buffer_size < optimal_buffer_size {
        udp_socket.set_send_buffer_size(optimal_buffer_size)?;
    }

    return Ok(());
}

/// differs from punch_udp_hole in taking a &UdpSocket as first argument, not a Arc<UdpSocket>
fn punch_udp_hole2(udp_socket: &UdpSocket, recv_addr_udp: SocketAddr) -> Result<(), CruspError> {
    let message = String::from("punch hole");
    info!("punch UDP hole...");
    udp_socket.send_to(message.as_bytes(), recv_addr_udp)?;
    return Ok(());
}

fn wait_for_hole_punch_confirmation(tcp_stream: &mut TcpStream) -> Result<(), CruspError> {
    let _x = tcp_stream.read(&mut [0; 128])?;

    return Ok(());
}

fn get_measurement_result_from_server(tcp_stream: &mut TcpStream) -> Result<MeasurementResult, CruspError> {
    tcp_stream.set_recv_buffer_size(TCP_BUFFER_SIZE)?;

    let mut read_buffer = String::new();
    let bytes_read_size = tcp_stream.read_to_string(&mut read_buffer);
    info!("Received {} bytes via TCP from server", bytes_read_size.unwrap());
    info!("Buffer: {}", &read_buffer);
    let result: Result<MeasurementResult, CruspError> = serde_json::from_str(&read_buffer)?;

    info!("Result: {:?}", &result);

    return Ok(result?);
}

/// Initializes TCP communication from client to measurement service.
/// by opening a TCP Stream for sending and a TCP Listener for receiving data
fn init_tcp_communication_to_server(socket_uri: String) -> Result<TcpStream, CruspError> {
    let recv_addr_tcp =  match socket_uri.to_socket_addrs()?.next() {
        Some(addr) => {
            info!("Established UDP connection to measurement-service");
            addr
        },
        None => {
            return Err(CruspError::new_input(String::from("Couldn't parse host")))
        }
    };

    let tcp_stream = TcpStream::connect_timeout(&recv_addr_tcp, Duration::from_millis(TCP_REQUEST_TIMEOUT_IN_MILLIS))?;
    let _x = tcp_stream.set_read_timeout_ms(Some(TCP_REQUEST_TIMEOUT_IN_MILLIS as u32));
    return Ok(tcp_stream);
}

/**************************************************************************************************
Unit tests
**************************************************************************************************/
#[cfg(test)]
mod tests {
    use super::*;

    #[test]
    fn test_get_next_power_of_two_should_return_8() {
        assert_eq!(8, get_next_power_of_two(5));
    }

    #[test]
    fn test_get_next_power_of_two_should_return_2() {
        assert_eq!(2, get_next_power_of_two(0));
    }
}

/*
Optimizations:

    //Set timestamp in socket for receiving packet, Supported in Unix only
    use std::os::unix::io::AsRawFd;
    use libc;
    use std::mem;

    let udp_fd = udp_socket.as_raw_fd();
    unsafe {
        let optval: libc::c_int = 1; //true
        let ret = libc::setsockopt(
            udp_fd,
            libc::SOL_SOCKET,
            libc::SO_TIMESTAMPNS,
            &optval as *const _ as *const libc::c_void,
            mem::size_of_val(&optval) as libc::socklen_t,
        );
        if ret != 0 {
            return Err(CruspError::new_error(String::from("Error setting SO_TIMESTAMPNS")));
        }
    }

    // Then read out the timestamp
*/
