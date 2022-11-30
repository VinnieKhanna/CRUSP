use duct_sh::sh;
use measurement_shared_rust::crusp_error::CruspError;
use log::*;
use reqwest;
use reqwest::StatusCode;
use measurement_shared_rust::token::Token;

use minreq;
use rocket::request;

const REVERSE_PORT_BASE_ADDRESS :&str = "http://reverse-port-service:8010/v1/reverse-port";
const SETTINGS_BASE_ADDRESS :&str ="http://settings-service:8020/v1/verify";

pub fn get_public_port(local_port: u16) -> Result<u16, CruspError> {
    let local_ip_address: String = get_local_ip_address();
    info!("local ip: {}", local_ip_address);
    let reqwest_string: String = format!("{}/{}/{}", REVERSE_PORT_BASE_ADDRESS, local_ip_address, local_port);
    let res_string: String = reqwest::get(reqwest_string.as_str())?.text()?;
    let public_port: u16 = res_string.parse::<u16>()?;
    info!("Received public port for reverse-port request");
    return Ok(public_port);
}

pub fn get_local_ip_address() -> String  {
    match sh("ip addr show eth0 | grep 'inet' | awk '{print $2}' | cut -f1 -d'/'").read() {
        Ok(ip) if ip != "Device \"eth0\" does not exist." && ip != "" => {
            info!("Found local IP Address of container");
            ip
        },
        _ => { // If "Device not found" message, also do this 
            error!("Could not query for local ip address of container");
            "localhost".to_string()
        }
    }
}

pub fn validate_token(token: &Token) -> Result<bool, CruspError> {
    let reqwest_string: String = format!("{}", SETTINGS_BASE_ADDRESS);

        info!("{}\n\n", reqwest_string);

    // let client = reqwest::Client::new();
    // let mut response = client.post(reqwest_string.as_str())
    //     .json(&token)
    //     .send()?;

    //     info!("\n\ndoes this break?\n\n");
    let response = minreq::post(reqwest_string.as_str()).with_json(token)?.send()?;

    if response.status_code == 200 {
        if response.as_str()?.parse::<bool>()? {
            return Ok(true);
        } else {
            return Err(CruspError::new_input(String::from("Validation of settings failed ")));
        }
    } else if response.status_code == 400 { //Validation failed
        Err(CruspError::new_input(String::from("POST Request ") + response.status_code.to_string().as_str()))
    } else if response.status_code == 404 {
        Err(CruspError::new_not_found(String::from("POST Request ") + response.status_code.to_string().as_str()))
    } else {
        Err(CruspError::new_communication(String::from("POST Request ") + response.status_code.to_string().as_str()))
    }
}