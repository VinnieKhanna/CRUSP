#![feature(proc_macro_hygiene, decl_macro)]

#[macro_use] extern crate rocket;
extern crate env_logger;

use rocket_contrib::json::{Json/*, JsonValue*/};
use rocket::response::status::BadRequest;
mod measurement; // mod X means: let there be a module X, defined either here, in braces, or in a separate file named X.rs or X/mod.rs.
use crate::measurement::crusp_server::{init_communication_downlink, start_downlink_measurement_process, init_communication_uplink, start_uplink_measurement_process};
use crate::measurement::helpers::{get_public_port, get_local_ip_address, validate_token};
use std::net::SocketAddr;
use std::env;
use measurement_shared_rust::token::Token;
use measurement_shared_rust::ports::Ports;
use log::{warn, debug, info};
use eureka_client::{BaseConfig, EurekaClient, PortData};

/// Receives downlink measurement requests for from the UE
/// # Arguments
///
/// * `token`- A measurement token that holds information about the measurement
///
/// # Return
///
/// * `Json<Ports>` if successfull
/// * `BadRequest<String>` on error
#[post("/downlink", format = "application/json", data = "<token>")]
fn downlink_request(_addr: SocketAddr, token: Json<Token>) -> Result<Json<Ports>, BadRequest<String>> {
    info!("Received downlink measurement request");
    let token: Token = token.into_inner();

    info!("Validate following settings: repeats={}, volume={}, packet-size={}, rate={}, sleep={}, timeout={}", token.repeats, token.volume, token.packet_size, token.rate, token.sleep, token.timeout);
    if let Err(e) = validate_token(&token) {
        warn!("Invalid token");
        return Err(BadRequest(Some(String::from("Invalid token - ") + &e.msg)));
    } else {
        debug!("Received settings validation for repeats={}, volume={}, packet-size={}, rate={}, sleep={}, timeout={}", token.repeats, token.volume, token.packet_size, token.rate, token.sleep, token.timeout);
    }

    debug!("init communication");
    let udp_socket = match init_communication_downlink(&token) {
        Ok(socket) => socket,
        Err(e) => return Err( BadRequest( Some(e.msg.to_string()))),
    };

    let global_port = match get_public_port(udp_socket.local_addr().unwrap().port()) {
        Ok(port) => port,
        Err(e) => return Err (BadRequest( Some(e.msg.to_string()))),
    };
    let ports = Ports{udp_port: global_port, tcp_port: None};

    debug!("start measurement process");
    start_downlink_measurement_process(udp_socket, token);

    info!("Waiting for punch hole...");
    Ok(Json(ports))
}

/// Receives uplink measurement requests from the UE
/// # Arguments
///
/// * `token`- A measurement token that holds information about the measurement
///
/// # Return
///
/// * `Json<Ports>` if successfull
/// * `BadRequest<String>` on error
#[post("/uplink", format = "application/json", data = "<token>")]
fn uplink_request(_addr: SocketAddr, token: Json<Token>) -> Result<Json<Ports>, BadRequest<String>> {
    info!("Received uplink measurement request");
    let token: Token = token.into_inner();

    info!("Validate following settings: repeats={}, volume={}, packet-size={}, rate={}, sleep={}, timeout={}", token.repeats, token.volume, token.packet_size, token.rate, token.sleep, token.timeout);
    if let Err(e) = validate_token(&token) {
        warn!("Invalid token");
        return Err(BadRequest(Some(String::from("Invalid token - ") + &e.msg)));
    } else {
        debug!("Received settings validation for repeats={}, volume={}, packet-size={}, rate={}, sleep={}, timeout={}", token.repeats, token.volume, token.packet_size, token.rate, token.sleep, token.timeout);
    }

    debug!("init communication");
    let (udp_socket, tcp_listener) = match init_communication_uplink() {
        Ok(endpoints) => endpoints,
        Err(e) => return Err( BadRequest( Some(e.msg.to_string()))),
    };

    // get exposed ports so that they are reachable from outside of the system
    let global_port_udp = match get_public_port(udp_socket.local_addr().unwrap().port()) {
        Ok(port) => port,
        Err(e) => return Err (BadRequest( Some(e.msg.to_string()))),
    };
    let global_port_tcp = match get_public_port(tcp_listener.local_addr().unwrap().port()) {
        Ok(port) => port,
        Err(e) => return Err (BadRequest( Some(e.msg.to_string()))),
    };
    let ports = Ports{udp_port: global_port_udp, tcp_port: Some(global_port_tcp)};

    debug!("start uplink measurement process");
    start_uplink_measurement_process(udp_socket, tcp_listener, token);

    info!("Waiting for punch hole...");
    Ok(Json(ports))
}


pub fn init_eureka(
    server_host: String,
    server_port: u16,
    instance_name: String,
    instance_port: u16,
) -> EurekaClient {
    let ip_address = get_local_ip_address();

    let mut config = BaseConfig::default();
    config.eureka.host = server_host;
    config.eureka.port = server_port;
    config.instance.ip_addr = ip_address.clone();
    config.instance.port = Some(PortData::new(instance_port, true));
    config.instance.host_name = ip_address.clone(); // e.g. e19a654d85f0
    config.instance.app =  instance_name.clone().to_uppercase(); // e.g. REVERSE-PORT-SERVICE
    config.instance.vip_address = instance_name.clone(); // e.g. reverse-port-service
    config.instance.secure_vip_address = instance_name.clone(); // e.g. reverse-port-service
    //config.instance.id = rand_string + ":" + &instance_name.clone() + ":" + &instance_port.to_string();

    let eureka = EurekaClient::new(config);
    eureka.start();

    eureka
}

fn main() {
    env_logger::init();
    rocket::ignite()
        .mount("/measurement/", routes![downlink_request])
        .mount("/measurement/", routes![uplink_request])
        // .manage( init_eureka(
        //     env::var("DISCOVERY_SERVICE_NAME").unwrap_or_else(|_| String::from("localhost")),
        //     env::var("DISCOVERY_SERVICE_PORT").unwrap_or_else(|_| String::from("")).parse().unwrap_or_else(|_| 8001),
        //     env::var("INSTANCE_NAME").unwrap_or_else(|_| String::from("measurement-service")),
        //     env::var("INSTANCE_PORT").unwrap_or_else(|_| String::from("")).parse().unwrap_or_else(|_| 8099))) // see Rocket.toml for port
        .launch();
}
