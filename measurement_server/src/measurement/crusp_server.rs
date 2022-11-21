use log::*;
use std::net::{UdpSocket, SocketAddr, TcpListener, TcpStream};
use std::{thread, io};
use measurement_shared_rust::crusp_error::CruspError;
use measurement_shared_rust::token::Token;
use measurement_shared_rust::crusp::{waiting_for_udp_hole_punch, get_next_power_of_two, crusp_send, collect_udp, convert_raw_packets, TCP_BUFFER_SIZE};
use std::env;
use net2::{UdpSocketExt, TcpStreamExt};
use std::io::{Write, Read};
use std::sync::Arc;
use measurement_shared_rust::results::MeasurementResult;
use std::time::{SystemTime};

const UDP_PORT_LOWER_BOUND: u16 = 56700;
const UDP_PORT_UPPER_BOUND: u16 = 56800;
const TCP_PORT_LOWER_BOUND: u16 = 56900;
const TCP_PORT_UPPER_BOUND: u16 = 57000;
// Thread::sleep(0) itself takes around 3_000 ns
// send_udp_packet itself takes around 6_000 ns
const TCP_REQUEST_TIMEOUT_IN_MILLIS: u64 = 500; // 2,0 sec

/**************************************************************************************************
CRUSP Downlink
**************************************************************************************************/

pub fn init_communication_downlink(settings: &Token) -> Result<UdpSocket, CruspError> {
    let udp_lower_bound = env::var("UDP_PORT_LOWER_BOUND")
        .map(|value| value.parse().unwrap_or(UDP_PORT_LOWER_BOUND))
        .unwrap_or(UDP_PORT_LOWER_BOUND);

    let udp_upper_bound = env::var("UDP_PORT_UPPER_BOUND")
        .map(|value| value.parse().unwrap_or(UDP_PORT_UPPER_BOUND))
        .unwrap_or(UDP_PORT_UPPER_BOUND);

    let udp_socket = get_available_udp_socket(udp_lower_bound, udp_upper_bound)?;
    info!("Bound UDP Socket on port: {}", udp_socket.local_addr().unwrap().port());

    let total_expected_volume = settings.repeats as u32 * (settings.volume as u32 * 1_000);
    let new_buffer_size = get_next_power_of_two(total_expected_volume) as usize;
    let current_buffer_size = udp_socket.send_buffer_size()?;
    if current_buffer_size < new_buffer_size {
        udp_socket.set_send_buffer_size(new_buffer_size)?;
    }

    return Ok(udp_socket);
}

fn get_available_udp_socket(lower_bound: u16, upper_bound: u16) -> Result<UdpSocket, CruspError> {
    for port in lower_bound..upper_bound {
        let addr = SocketAddr::from(([0, 0, 0, 0], port));
        match UdpSocket::bind(addr) {
            Ok(port) => return Ok(port),
            Err(_err) => (),
        }
    }

    return Err(CruspError::new_communication(String::from("Couldn't find available port")));
}

pub fn start_downlink_measurement_process(mut udp_socket: UdpSocket, settings: Token) {
    thread::spawn(move || {
        let client_addr = match waiting_for_udp_hole_punch(&mut udp_socket) {
            Ok(addr) => addr,
            Err(err) => {
                error!("Waiting for UDP hole punch failed; {}", err.msg);
                return; // abort downlink measurement process
            }
        };

        if let Err(err) = crusp_send(&udp_socket, client_addr, &settings) {
            error!("Crusp send failed; {}", err.msg);
        }
    });
}

/**************************************************************************************************
CRUSP Uplink
**************************************************************************************************/

pub fn init_communication_uplink() -> Result<(UdpSocket, TcpListener), CruspError> {
    // 1. get lower bound for UDP port number
    let udp_lower_bound = env::var("UDP_PORT_LOWER_BOUND")
        .map(|value| value.parse().unwrap_or(UDP_PORT_LOWER_BOUND))
        .unwrap_or(UDP_PORT_LOWER_BOUND);

    // 2. get upper bound for UDP port number
    let udp_upper_bound = env::var("UDP_PORT_UPPER_BOUND")
        .map(|value| value.parse().unwrap_or(UDP_PORT_UPPER_BOUND))
        .unwrap_or(UDP_PORT_UPPER_BOUND);

    let tcp_lower_bound = env::var("TCP_PORT_LOWER_BOUND")
        .map(|value| value.parse().unwrap_or(TCP_PORT_LOWER_BOUND))
        .unwrap_or(TCP_PORT_LOWER_BOUND);

    let tcp_upper_bound = env::var("TCP_PORT_UPPER_BOUND")
        .map(|value| value.parse().unwrap_or(TCP_PORT_UPPER_BOUND))
        .unwrap_or(TCP_PORT_UPPER_BOUND);

    // 3. find free UDP port and bind it to local address
    let udp_socket = get_available_udp_socket(udp_lower_bound, udp_upper_bound)?;
    info!("Bound UDP Socket on port: {}", udp_socket.local_addr().unwrap().port());

    // 4. init a TCP listener at a free port at the local address
    let tcp_listener = get_available_tcp_listener(tcp_lower_bound, tcp_upper_bound)?;
    info!("Bound TCP Listener on port: {}", tcp_listener.local_addr().unwrap().port());

    return Ok((udp_socket, tcp_listener));
}

fn get_available_tcp_listener(lower_bound: u16, upper_bound: u16) -> Result<TcpListener, CruspError> {
    for port in lower_bound..upper_bound {
        let addr = SocketAddr::from(([0, 0, 0, 0], port));
        match TcpListener::bind(addr) {
            Ok(port) => return Ok(port),
            Err(_err) => (),
        }
    }

    return Err(CruspError::new_communication(String::from("Couldn't find available port")));
}

pub fn start_uplink_measurement_process(udp_socket: UdpSocket, tcp_listener: TcpListener, token: Token) {
    thread::spawn(move || {
        // 5. waiting for TCP connect from UE; BLOCKING
        let mut tcp_stream = match wait_for_tcp_connect(tcp_listener) {
            Ok(stream) => stream,
            Err(err) => {
                error!("Failed waiting for TCP connect; {}", err.msg);
                return;
            }
        };

        let mut crusp_result = uplink_measurmeent_process(&tcp_stream, udp_socket, token);

        // 11. wait for TCP result request from UE, BLOCKING
        let _x = tcp_stream.set_read_timeout_ms(Some(TCP_REQUEST_TIMEOUT_IN_MILLIS as u32));
        if let Err(err) =  waiting_for_results_request(&tcp_stream) {
            error!("Failed waiting for TCP result request; {}", err.msg);
            crusp_result = Err(CruspError::new_communication(String::from("Failed waiting for TCP result request")));
        }

        // 12. send sequence_collection back to UE
        let serialized_string = match serde_json::to_string_pretty(&crusp_result) {
            Ok(serialized) => serialized,
            Err(err) => {
                error!("Failed serializing result; {}", err.to_string());
                String::from("")
            },
        };

        let _x = tcp_stream.set_send_buffer_size(TCP_BUFFER_SIZE);
        let bytes_written = tcp_stream.write(serialized_string.as_bytes());
        info!("Wrote result as {} bytes via TCP to client", bytes_written.unwrap());

        // tcp_stream closes automatically when variable is out of scope
    });
}

fn wait_for_tcp_connect(tcp_listener: TcpListener) -> Result<TcpStream, CruspError> {
    let _x = tcp_listener.set_nonblocking(true);

    let mut loop_active = true;
    let start = SystemTime::now();
    let mut tcp_stream_option: Option<TcpStream> = None;

    while loop_active { // loop is active as long the elapsed time is not greater than TCP_REQUEST_TIMEOUT_IN_MILLIS
        tcp_stream_option = match tcp_listener.accept() {
            Ok((stream, _socket_addr)) =>  {
                loop_active = false;
                Some(stream)
            },
            Err(err) => {
                if err.kind() == io::ErrorKind::WouldBlock {
                    debug!("Active waiting for TCP accept");
                    None
                } else {
                    loop_active = match start.elapsed() {
                        Ok(elapsed) => (elapsed.as_millis() as u64) < TCP_REQUEST_TIMEOUT_IN_MILLIS,
                        Err(_err) => {
                            return Err(CruspError::new_communication(String::from("timing in TCP accept failed")));
                        }
                    };
                    None
                }
            },
        };
    }

    return Ok(tcp_stream_option.unwrap());
}

fn uplink_measurmeent_process(tcp_stream: &TcpStream, mut udp_socket: UdpSocket, token: Token) -> Result<MeasurementResult, CruspError> {
    // 6. waiting for UDP punch hole; BLOCKING
    let client_addr = waiting_for_udp_hole_punch(&mut udp_socket)?;

    // 7. start collecting UDP packages; uses separate Thread
    // before punch hole confirmation to give the collector-thread enough time to start
    let udp_socket_arc: Arc<UdpSocket> = Arc::new(udp_socket);
    let collector = thread::spawn( move|| {
        return collect_udp(udp_socket_arc, token);
    });

    // 8. send TCP punch hole confirmation
    send_hole_punch_confirmation(&tcp_stream)?;

    // 9. Join threads together to get access to the collected packets
    let raw_packets = collector
        .join()
        .map_err(|_err| CruspError::new_error(String::from("Error when joining threads")))??;

    // 10. convert raw packages to sequence_collection
    let sequence_coll = convert_raw_packets(raw_packets, client_addr, token)?;
    let result = sequence_coll.calculate_naiv_result(token);

    return Ok(result);
}

fn send_hole_punch_confirmation(mut tcp_stream: &TcpStream) -> Result<(), CruspError> {
    let _bytes_written = tcp_stream.write(&[1]);

    return Ok(());
}

fn waiting_for_results_request(mut tcp_stream: &TcpStream) -> Result<(), CruspError> {
    let _bytes_read = tcp_stream.read(&mut [0; 128]);

    return Ok(());
}