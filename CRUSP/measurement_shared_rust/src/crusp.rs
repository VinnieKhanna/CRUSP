/*
Problem without TCP:
- UDP-collect doesn't know exactly, when measurement starts. Therefore the timeout could be reached
    just by time needed on server. Solution: define big timeout (2s) for first packet, then change it to small timeout
*/
use log::*;
use std::sync::Arc;
use std::net::{UdpSocket, SocketAddr};
use crate::crusp_error::CruspError;
use std::time::{Duration, SystemTime, UNIX_EPOCH};
use crate::token::Token;
use crate::raw_packet::{RawPacket, BUFFER_SIZE};
use crate::results::{SequenceCollection, ReceivedPacketDetails};
use std::io::Cursor;
use byteorder::{ReadBytesExt, NetworkEndian};
use std::thread;
use crate::crusp_plan::{CruspPlan, calculate_num_of_packets};

const UDP_TIMEOUT_INIT: u64 = 1000; //timeout for starting measurement in ms
const PUNCH_HOLE_TIMEOUT: u64 = 1000; //read-timeout for punch-hole in milliseconds
const INIT_SLEEP_CORRECTION: u64 = 10_000; //in ns
pub const TCP_BUFFER_SIZE: usize = 167776216; // 16 MB
pub const HTTP_REQUEST_TIMEOUT_IN_MILLIS: u64 = 2000; // 2 sec
pub const TCP_REQUEST_TIMEOUT_IN_MILLIS: u64 = 2000; // 2,0 sec

/**************************************************************************************************
Punch hole
**************************************************************************************************/

pub fn punch_udp_hole(udp_socket: Arc<UdpSocket>, recv_addr_udp: SocketAddr) -> Result<(), CruspError> {
    let message = String::from("punch hole");
    info!("punch UDP hole...");
    udp_socket.send_to(message.as_bytes(), recv_addr_udp)?;
    return Ok(());
}

/*
Precondition: communication channels are initialized and working
Waits PUNCH_HOLE_TIMEOUT for UDP packet from the client.
*/
pub fn waiting_for_udp_hole_punch(udp_socket: &mut UdpSocket) -> Result<SocketAddr, CruspError>{
    let mut buffer = [0; 10];

    udp_socket.set_read_timeout(Some(Duration::from_millis(PUNCH_HOLE_TIMEOUT)))?;

    let (_received, addr) = udp_socket.recv_from(&mut buffer)?;
    info!("received hole punch from {}", addr.to_string());

    return Ok(addr);
}

/**************************************************************************************************
CRUSP send
**************************************************************************************************/

pub fn crusp_send(udp_socket: &UdpSocket, receiver_addr: SocketAddr, settings: &Token) -> Result<(), CruspError> {
    info!("crusp: Start sending packets...");
    let plan = CruspPlan::new(settings)?;

    let sleep_duration = Duration::from_millis(settings.sleep);
    let mut start_ts = SystemTime::now();
    let mut measured_duration = plan.inter_packet_sleep_ns - INIT_SLEEP_CORRECTION; //init duration a little bit too short
    let mut next_sleep_time = (plan.inter_packet_sleep_ns - INIT_SLEEP_CORRECTION) as i64;

    let sleep_barrier: i64 = (measure_sleep() as f64 * 1.5).ceil() as i64;

    for repeat_number in 0..plan.repeats.len() {
        for packet_number in 0..plan.packets_per_repeat {
            send_udp_packet(&plan.repeats
                .get(repeat_number as usize)
                .unwrap()
                .datagrams
                .get(packet_number as usize)
                .unwrap()
                .data_to_send, &udp_socket, receiver_addr)?;

            if packet_number > 0 || repeat_number == 0 {
                // Ignore changes in sleep-time for first packets in following repeats
                // since sleep-time after a sequence manipulates measured-time

                // 1. Get the difference between the measured duration and the supposed duration:
                //    If positive then sleep was too long, otherwise sleep was to short
                //    At first run, the sleep is too short for INIT_SLEEP_CORRECTION ns
                let diff = measured_duration as i64 - plan.inter_packet_sleep_ns as i64;
                // 2. If the measured_duration is higher than the supposed duration
                //    Then subtract the difference from the next_sleep_time
                //    So that the next_sleep_time is shorter and therefore the new duration is shorter

                // subtracting half of difference smooths down to the supposed sleep time and
                // avoids jumping around from too slow and too fast
                next_sleep_time -= (0.5 * diff as f64).round() as i64;
            }

            // Thread::sleep(0) itself takes around 3_000 ns
            if next_sleep_time > sleep_barrier {
                thread::sleep(Duration::from_nanos(next_sleep_time as u64));
            } else if next_sleep_time > 0 {
                let bound = next_sleep_time as u128;
                let mut loop_ts = SystemTime::now();
                while match loop_ts.duration_since(start_ts) {
                    Ok(result) => result.as_nanos() < bound,
                    Err(_err) => true,
                } {
                    loop_ts = SystemTime::now();
                }
            }

            let end_ts = SystemTime::now();

            if packet_number > 0 || repeat_number == 0 {
                measured_duration = end_ts.duration_since(start_ts).unwrap().as_nanos() as u64;
            }
            start_ts = end_ts;
        }

        // if this is too short we will have collision on the bottleneck
        thread::sleep(sleep_duration);
        info!("crusp: Sequence {}: sent {} packets", repeat_number+1, plan.packets_per_repeat);
    }

    info!("crusp: finished measurement sending");
    return Ok(());
}


fn send_udp_packet(data_to_send: &Vec<u8>, udp_socket: &UdpSocket, client_addr: SocketAddr) -> Result<(), CruspError>{
    return udp_socket
        .send_to(data_to_send, client_addr)
        .map(|_x| () )
        .map_err(|err| CruspError::new_communication(String::from("Error when sending UDP Packet: ") + err.to_string().as_str()));
}

fn measure_sleep() -> i64 {
    let start_ts = SystemTime::now();
    thread::sleep(Duration::from_nanos(1));
    let end_ts = SystemTime::now();

    return match end_ts.duration_since(start_ts) {
        Ok(duration) => duration.as_nanos(),
        Err(_error) => 2000
    } as i64;
}

/**************************************************************************************************
CRUSP receive
**************************************************************************************************/

pub fn collect_udp(udp_socket: Arc<UdpSocket>, settings: Token) -> Result<Vec<RawPacket>, CruspError> {
    let mut raw_packets = Vec::new();

    for seq_nr in 1 ..= settings.repeats {

        //wait UDP_TIMEOUT for first response from server
        udp_socket.set_read_timeout(Some(Duration::from_millis(UDP_TIMEOUT_INIT)))?;

        let num_packets: u16 = calculate_num_of_packets(settings.volume, settings.packet_size);
        collect_udp_for_sequence(&mut raw_packets, udp_socket.clone(), settings.timeout, seq_nr, num_packets)?;
    }

    return Ok(raw_packets);
}

fn collect_udp_for_sequence(raw_packets: &mut Vec<RawPacket>, udp_socket: Arc<UdpSocket>, timeout: u16, seq_nr: u16, expected_num_packets: u16) -> Result<(), CruspError> {
    let mut recv_first_packet = false;
    for _i in 0..expected_num_packets {
        let mut recv_buffer = [0; BUFFER_SIZE]; //max 1 MB = 1048576 bytes
        //If a message is too long to fit in the supplied buffer, excess bytes may be discarded.

        let (num_received_bytes, send_addr) = match udp_socket.recv_from(&mut recv_buffer) {
            Ok((bytes, addr)) => (bytes, addr),
            Err(_err) => { //This error should happen when not all packets are received
                if !recv_first_packet && seq_nr == 1 { // Error should NOT happen in first sequence on the first packet
                    return Err(CruspError::new_communication(String::from("Didn't get UDP-Packets")));
                } else {
                    return Ok(());
                }
            }
        };
        let recv_time = SystemTime::now();

        if !recv_first_packet { // change time-out to much lower duration after first received packet
            udp_socket.set_read_timeout(Some(Duration::from_millis(timeout as u64)))?;
            recv_first_packet = true;
        }

        raw_packets.push(RawPacket{
            recv_buffer,
            num_received_bytes,
            timestamp: recv_time,
            send_addr,
        });
    }
    return Ok(());
}

pub fn get_next_power_of_two(number: u32) -> u32{
    let mut next = 2;

    if number == 0 {
        return 2;
    }

    loop {
        if next > number {
            return next;
        } else {
            next *= 2;
        }
    }
}

pub fn convert_raw_packets(raw_packets: Vec<RawPacket>,recv_addr_udp: SocketAddr, settings: Token) -> Result<SequenceCollection, CruspError>{
    let packets_per_seq = (settings.volume as f32 * 1_000 as f32 / settings.packet_size as f32).ceil() as u16;

    let mut sequence_collection = SequenceCollection::new(packets_per_seq, settings.repeats);

    let mut start_time : u128 = 0;

    for raw_packet in raw_packets.iter() {
        if recv_addr_udp != raw_packet.send_addr // invalid sender, dump packet
            || raw_packet.num_received_bytes <= 4 { // received bytes too low
            continue;
        }

        let duration_since_epoch_as_nanos = match raw_packet.timestamp.duration_since(UNIX_EPOCH) {
            Ok(duration) => duration.as_nanos(),
            Err(_err) => Duration::new(0, 0).as_nanos(), // if duration is negative, set time to 0
        };

        if start_time == 0 || duration_since_epoch_as_nanos < start_time  {
            start_time = duration_since_epoch_as_nanos;
        }

        let mut rdr = Cursor::new(raw_packet.recv_buffer.to_vec());

        let repeat_nr = rdr.read_u16::<NetworkEndian>()?;
        let packet_nr = rdr.read_u16::<NetworkEndian>()?;

        if repeat_nr > settings.repeats {
            continue;
        }


        if sequence_collection.sequences.get((repeat_nr-1) as usize).unwrap().seq_start_time == 0 {
            sequence_collection.sequences.get_mut((repeat_nr-1) as usize).unwrap().seq_start_time = duration_since_epoch_as_nanos;
        }

        let mut delta_to_start_ns = duration_since_epoch_as_nanos - sequence_collection.sequences
            .get((repeat_nr-1) as usize)
            .unwrap()
            .seq_start_time;

        if delta_to_start_ns > (2u64.pow(32)-1) as u128 {
            delta_to_start_ns = 0;
        }

        let new_packet = ReceivedPacketDetails {
            delta_to_start_ns: delta_to_start_ns as u32,
            recv_bytes_amount: raw_packet.num_received_bytes,
            repeat_nr,
            packet_nr,
        };

        sequence_collection.add(new_packet)?;
    }

    sequence_collection.start_time = start_time;

    return Ok(sequence_collection);
}