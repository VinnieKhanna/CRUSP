use log::{info};
use crate::crusp_error::CruspError;
use crate::token::Token;
use serde::{Serialize, Deserialize};

#[derive(Serialize, Deserialize, Debug)]
pub struct ReceivedPacketDetails {
    pub delta_to_start_ns: u32, // max delta of 4.29 sec due to max value of type u32
    pub recv_bytes_amount: usize,
    pub repeat_nr: u16,
    pub packet_nr: u16,
}

#[derive(Serialize, Deserialize, Debug)]
pub struct MeasurementResult {
    pub start_time: u128,
    pub num_received_packets: u16,
    pub available_bandwidth: f32,
    pub sequences: Vec<SequenceDetails>,
}

#[derive(Serialize, Deserialize, Debug)]
pub struct SequenceDetails {
    pub packet_details_vec: Vec<ReceivedPacketDetails>,
    pub expected_packets: u16,
    pub naive_rate: f32,
    pub seq_start_time: u128,
}

#[derive(Serialize, Deserialize, Debug)]
pub struct SequenceCollection {
    pub sequences: Vec<SequenceDetails>,
    pub start_time: u128,
}

impl MeasurementResult {
    pub fn new(start_time: u128, num_received_packets: u16, available_bandwidth: f32, sequences: Vec<SequenceDetails>) -> Self {
        MeasurementResult{
            start_time,
            num_received_packets,
            available_bandwidth,
            sequences,
        }
    }
}

impl SequenceCollection {
    pub fn new (sequence_size: u16, num_sequences: u16) -> Self {
        let mut collection = SequenceCollection{
            sequences: Vec::new(),
            start_time: 0,
        };

        for _i in 0..num_sequences {
            let sequence_details = SequenceDetails::new(sequence_size);
            collection.sequences.push(sequence_details);
        }
        return collection;
    }

    pub fn add(&mut self, new_packet: ReceivedPacketDetails) -> Result<(), CruspError> {
        self.sequences
            .get_mut((new_packet.repeat_nr-1) as usize)
            .ok_or(CruspError::new_error(String::from("Wrong tracking number in UDP Packet")))?
            .packet_details_vec.push(new_packet);

        return Ok(());
    }

    pub fn calculate_naiv_result(mut self, settings: Token) -> MeasurementResult {
        let mut total_received_packets = 0;
        let mut total_rate_sum: (u16, f32) = (0, 0.0); //first element is the number of sequences, second element is the sum of rates

        for i in 0..settings.repeats {
            let seq_details =  self.sequences.get(i as usize);

            if seq_details.is_none() || seq_details.unwrap().packet_details_vec.len() < 2 {
                info!("received insufficient packets for sequence {}", i+1)
            } else {
                let seq_details = seq_details.unwrap();

                let max: Option<&ReceivedPacketDetails>  = seq_details.packet_details_vec
                    .get((seq_details.packet_details_vec.len()-1) as usize);

                let received_packets = seq_details.packet_details_vec.len();
                total_received_packets += received_packets;

                let sequence_length = seq_details.packet_details_vec.len() as f32;

                info!("Sequence_length: {}, Packet size: {}, Delta: {} ms", received_packets, settings.packet_size, max.unwrap().delta_to_start_ns as f64 /1_000_000.0);

                // estimate: (number or received packets  * packet size) in bits / time delta between last and first time stamp in usec
                let naive_rate: f32 = ((sequence_length as f64 * settings.packet_size as f64 * 8.0) / max.unwrap().delta_to_start_ns as f64 * 1e3) as f32;
                info!("Rate for sequence {}: {} Mbit/s", i+1, naive_rate);

                if naive_rate > 0.0 {
                    let (x,y) = total_rate_sum;
                    total_rate_sum = (x+1, y+naive_rate);

                    self.sequences.get_mut(i as usize).unwrap().naive_rate = naive_rate;
                }
            }
        }

        let total_rate_fn = |rate| -> f32 {
            let (seq, sum) = rate;

            if seq > 0 {
                return sum / (seq as f32);
            } else {
                return 0.0;
            }
        };

        return MeasurementResult::new(self.start_time, total_received_packets as u16, total_rate_fn(total_rate_sum), self.sequences);
    }
}

impl SequenceDetails {
    pub fn new(expected_packets: u16) -> Self {
        SequenceDetails {
            packet_details_vec: Vec::with_capacity(expected_packets as usize),
            expected_packets,
            naive_rate: 0.0,
            seq_start_time: 0,
        }
    }
}