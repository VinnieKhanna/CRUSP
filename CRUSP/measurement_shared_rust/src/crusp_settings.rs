use std::time::Duration;

#[derive(Clone, Copy)]
pub struct CruspSettings {
    pub uid: u16,
    pub num_repeats: u16,
    pub volume: u32,      // data-volume in kilo-byte (kB)
    pub rate: f32,        // data rate in Megabit per second (Mb/s)
    pub packet_size: u32, // size of one packet in byte
    pub sleep: Duration,
    pub timeout: u16,     // timeout in ms between arriving packets before aborting measurement
    pub standard: bool // will be a standard setting or a customized setting used
}
