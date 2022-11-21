use serde::{Serialize, Deserialize};

/*
    Used for sending and receiving CruspSettings through the HTTP-Interface
*/
#[derive(Serialize, Deserialize, Copy, Clone)]
#[serde(rename_all = "camelCase")] // for communication with json
pub struct Token {
    pub repeats: u16,
    pub sleep: u64, //sleep between repeats in milliseconds (ms)
    pub volume: u32, // data-volume in kilo-byte (kB)
    pub rate: f32, // sending rate in Megabit per second (Mb/s)
    pub packet_size: u32, // packet-size in bytes
    pub timeout: u16, // timeout in ms between arriving packets before aborting measurement
}

impl Token {
    pub fn new(repeats: u16,
               sleep: u64,
               volume: u32,
               rate: f32,
               packet_size: u32,
               timeout: u16) -> Self {
        Token {
            repeats,
            sleep,
            volume,
            rate,
            packet_size,
            timeout,
        }
    }
}
