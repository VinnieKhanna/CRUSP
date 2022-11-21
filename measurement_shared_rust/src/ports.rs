use serde::{Serialize, Deserialize};

#[derive(Serialize, Deserialize)]
pub struct Ports {
    pub udp_port: u16,
    pub tcp_port: Option<u16>,
}