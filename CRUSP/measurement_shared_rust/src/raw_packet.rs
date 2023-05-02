use std::net::SocketAddr;
use std::time::SystemTime;

pub const BUFFER_SIZE: usize = 65536; //bytes, 2^16 = 65536 Bytes =  1 MB = 1048576 Bytes

pub struct RawPacket {
    pub recv_buffer: [u8; BUFFER_SIZE],
    pub num_received_bytes: usize,
    pub timestamp: SystemTime,
    pub send_addr: SocketAddr,
}