use crate::token::Token;
use crate::crusp_error::CruspError;
use byteorder::{WriteBytesExt, NetworkEndian};

pub struct CruspDatagram {
    pub data_to_send: Vec<u8>,
}

pub struct CruspRepeat {
    pub datagrams: Vec<CruspDatagram>,
}

pub struct CruspPlan {
    pub repeats: Vec<CruspRepeat>,
    pub packets_per_repeat: u16,
    pub inter_packet_sleep_ns: u64,
}

/// Precalculates all data and timings needed for the measurement
impl CruspPlan {
    pub fn new(settings: &Token) -> Result<Self, CruspError> {
        // volume in kByte * 1000 / packetSize in Byte
        let num_packets = calculate_num_of_packets(settings.volume, settings.packet_size);

        // packets * packet_size in byte * 8
        let new_bit_volume = num_packets as u32 * settings.packet_size as u32 * 8;

        // measurement_time_sec = volume in bits / (rate in Mbit/s * 10^6) = seconds
        // measurement_time_ns = measurement_time_sec / 10^9
        let inter_packet_sleep_ns = ((new_bit_volume as f32 * 1_000.0 / (settings.rate as f32)).round() as f32 / (num_packets-1) as f32).floor() as u64;

        let data_to_send : Vec<u8> = (0..settings.packet_size)
            .map( |_| rand::random() )
            .collect();

        let mut repeats: Vec<CruspRepeat> = Vec::with_capacity(settings.repeats as usize);

        for repeat_number in 1..=settings.repeats {

            let mut datagrams: Vec<CruspDatagram> = Vec::with_capacity(num_packets as usize);

            for packet_number in 1..=num_packets {
                let mut payload = data_to_send.clone();

                let mut writer = Vec::new();
                writer.write_u16::<NetworkEndian>(repeat_number)?;
                writer.write_u16::<NetworkEndian>(packet_number)?;
                payload.splice(0..writer.len(), writer);

                let crusp_datagram = CruspDatagram {
                    data_to_send: payload,
                };

                datagrams.push(crusp_datagram);
            }

            repeats.push( CruspRepeat {
                datagrams,
            } );
        }

        let crusp_plan = CruspPlan {
            repeats,
            packets_per_repeat: num_packets,
            inter_packet_sleep_ns,
        };

        return Ok(crusp_plan);

    }
}

pub fn calculate_num_of_packets(volume: u32, packet_size: u32) -> u16{
    return (volume as f32 * 1_000 as f32 /packet_size as f32).ceil() as u16;
}