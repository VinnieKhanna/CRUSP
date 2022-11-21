use log::{info, error};
use env_logger;
use clap::{Arg, App};
use csv;
use std::fs::{File, OpenOptions};
use measurement_shared_rust::crusp_error::CruspError;
use measurement_shared_rust::token::Token;
use measurement_shared_rust::results::MeasurementResult;
use measurement_client_rust::crusp_client::{measure_downlink, measure_uplink};

fn main() {
    println!("sample log");
    env_logger::init();

    let (token, host, port, downlink, debug, write) = match init_settings() {
        Ok(settings) => settings,
        Err(err) => {
            error!("{}", err.msg);
            return;
        }
    };

    println!("passed arg checking");

    let result = if downlink {
        info!("Value for path: measurement/downlink");
        measure_downlink(token, host, port, "measurement/downlink".to_string())
    } else {
        measure_uplink(token, host, port, "measurement/uplink".to_string())
    };

    match result {
        Ok(res) => {
            print_result(&res, debug);

            if let Some(filename) = write {
                write_result_to_csv(&res, filename);
            }

        },
        Err(e) => info!("Error: {}", e.msg),
    }

    //Socket is closed automatically when going out of scope
}

fn write_result_to_csv(result: &MeasurementResult, filename: String) {

    let (file, is_new) = match OpenOptions::new()
                                            .write(true)
                                            .append(true)
                                            .open(&filename) {
        Ok(file) => (file, false),
        Err(_err) => {
            let file = File::create(&filename).expect("Couldn't create file");
            (file, true)
        }
    };

    let mut wtr = csv::Writer::from_writer(file);
    if is_new { // write header
        wtr.write_record(&["Repeat", "Packet", "Received Bytes", "Delta Time in ns"]).expect("Couldn't write record");
    } 

    for sequence in &result.sequences {
        for packet_details in &sequence.packet_details_vec {
            wtr.write_record(&[packet_details.repeat_nr.to_string(),
                packet_details.packet_nr.to_string(),
                packet_details.recv_bytes_amount.to_string(),
                packet_details.delta_to_start_ns.to_string()])
            .expect("Couldn't write record");
        }
    }

    wtr.flush().expect("Couldn't write file");
}

fn print_result(result: &MeasurementResult, debug: bool) {
    let mut i = 0;
    for sequence in &result.sequences {
        i += 1;

        if debug {
            println!("-------------------------------------------------------------------------------");
            println!("---------------------------- Details to Sequence {} ----------------------------",i);
            println!("-------------------------------------------------------------------------------");

            for packet_details in &sequence.packet_details_vec {
                println!("Repeat: {}, Packet: {}, Received: {} bytes, Start-Delta: {} ms", packet_details.repeat_nr, packet_details.packet_nr, packet_details.recv_bytes_amount, packet_details.delta_to_start_ns as f64 / 1_000_000.0);
            }
        }

        if sequence.naive_rate > 0.0 {
            println!("Naive Rate Sequence: {}, Received packets: {}/{}", sequence.naive_rate, &sequence.packet_details_vec.len(), sequence.expected_packets);
        } else {
            println!("Received insufficient packets: {}/{}", &sequence.packet_details_vec.len(), sequence.expected_packets);
        }
    }

    println!("Total Naive Rate: {} Mbps ", result.available_bandwidth);
    println!("Total received Packets: {} ", result.num_received_packets);
}

fn init_settings() -> Result<(Token, String, u32, bool, bool, Option<String>), CruspError> {
    let matches = App::new("ABE Client Rust")
        .version("0.2")
        .author("Wolfgang H. <wolfgang.e389.hofer@tuwien.ac.at>")
        .about("Simple Measurement Client for ABE Tool")
        .arg(Arg::with_name("sleep_repeats")
            .short("e")
            .long("sleep")
            .value_name("SLEEP")
            .help("Sets the sleep between repeats in milliseconds, default: 200ms")
            .takes_value(true)
            .validator(|input| match input.parse::<u16>() {
                Ok(_x) => Ok(()),
                Err(e) => Err(e.to_string()), }))
        .arg(Arg::with_name("volume")
            .short("v")
            .long("volume")
            .value_name("VOLUME")
            .help("Sets the total data volume in kB of the test, default: 930 kB")
            .takes_value(true)
            .validator(|input| match input.parse::<u16>() {
                Ok(_x) => Ok(()),
                Err(e) => Err(e.to_string()), }))
        .arg(Arg::with_name("repeats")
            .short("r")
            .long("repeats")
            .value_name("REPEATS")
            .help("Sets the number of repeats/sequences per measurement, default: 1")
            .takes_value(true)
            .validator(|input| match input.parse::<u16>() {
                Ok(_x) => Ok(()),
                Err(e) => Err(e.to_string()), }))
        .arg(Arg::with_name("dest")
            .short("h")
            .long("host")
            .value_name("HOST")
            .help("Sets the hostname of the destination, default: hossman.nt.tuwien.ac.at")
            .takes_value(true))
        .arg(Arg::with_name("port")
            .short("p")
            .long("port")
            .value_name("PORT")
            .help("Sets the port of the server, default port: 8090")
            .takes_value(true)
            .validator(|input| {
                match input.parse::<u16>() {
                    Ok(x) if x > 0 && x < 65535 => Ok(()),
                    Ok(_) => Err(String::from("Port number invalid")),
                    Err(e) => Err(e.to_string()),
                }
            }))
        .arg(Arg::with_name("rate")
            .short("z")
            .long("rate")
            .value_name("RATE")
            .help("Sets the UDP rate in Mbit/s, default rate is 150 Mbit/s")
            .takes_value(true)
            .validator(|input| {
                match input.parse::<f32>() {
                    Ok(rate) if rate > 0.0 => Ok(()),
                    Ok(_) => Err(String::from("Rate too low")),
                    Err(e) => Err(e.to_string()),
                }
            }))
        .arg(Arg::with_name("size")
            .short("s")
            .long("size")
            .value_name("SIZE")
            .help("Packet-size in Byte, default size is 1000 Byte")
            .takes_value(true)
            .validator(|input| {
                match input.parse::<u16>() {
                    Ok(size) if size > 0 => Ok(()),
                    Ok(_) => Err(String::from("Size too small")),
                    Err(e) => Err(e.to_string()),
                }
            }))
        .arg(Arg::with_name("timeout")
            .short("t")
            .long("timeout")
            .value_name("TIMEOUT")
            .help("Maximum inter-packet timeout in ms before aborting measurement, default timeout is 100 ms")
            .takes_value(true)
            .validator(|input| {
                match input.parse::<u16>() {
                    Ok(size) if size > 0 => Ok(()),
                    Ok(_) => Err(String::from("Timeout too small")),
                    Err(e) => Err(e.to_string()),
                }
            }))
        .arg(Arg::with_name("write")
            .short("w")
            .long("write")
            .multiple(true)
            .help("write results in csv-format to file"))
        .arg(Arg::with_name("uplink")
            .short("u")
            .long("uplink")
            .multiple(true)
            .help("performs uplink measurement, default: downlink"))
        .arg(Arg::with_name("debug")
            .short("d")
            .long("debug")
            .multiple(true)
            .help("print debug output"))
        .arg(Arg::with_name("file")
            .short("f")
            .long("file")
            .value_name("FILENAME")
            .help("writes result to specified FILENAME, default is 'results.csv'")
            .takes_value(true))
        .get_matches();


    // Calling .unwrap() is safe here because "dest" is required
    let arg_host = matches.value_of("dest").unwrap_or("hossman.nt.tuwien.ac.at");
    info!("Value for dest: {}", arg_host);

    // Calling .unwrap() is safe here because "port" is required
    let arg_port = matches.value_of("port").unwrap_or("8090");
    info!("Value for port: {}", arg_port);

    // Gets a value for dest if supplied by user, or defaults to "default.dest"
    let arg_rate = matches.value_of("rate").unwrap_or("150");
    info!("Value for rate: {}", arg_rate);

    let arg_size = matches.value_of("size").unwrap_or("1000");
    info!("Value for packet-size: {}", arg_size);

    let arg_repeats = matches.value_of("repeats").unwrap_or("1");
    info!("Value for repeats: {}", arg_repeats);

    let arg_volume = matches.value_of("volume").unwrap_or("930");
    info!("Value for volume: {}", arg_volume);

    let arg_timeout = matches.value_of("timeout").unwrap_or("100");
    info!("Value for timeout: {}", arg_timeout);

    let arg_file = matches.value_of("file").unwrap_or("results.csv");
    info!("Value for file: {}", arg_file);

    let mut write = false;
    if matches.occurrences_of("write") > 0 {
        write = true;
    }
    info!("Value for write: {}", write);

    let mut debug = false;
    if matches.occurrences_of("debug") > 0 {
        debug = true;
    }
    info!("Value for debug: {}", debug);

    let mut downlink = true;
    if matches.occurrences_of("debug") > 0 {
        downlink = false;
    }
    info!("Value for downlink: {}", downlink);

    let arg_sleep_repeats = matches.value_of("sleep_repeats").unwrap_or("200");
    info!("Value for sleep between repeats: {} ms", arg_sleep_repeats);


    let port: u32 = arg_port.parse::<u32>().unwrap();
    let rate = arg_rate.parse::<f32>().unwrap();
    let packet_size = arg_size.parse::<u16>().unwrap();
    let num_repeats = arg_repeats.parse::<u16>().unwrap();
    let volume = arg_volume.parse::<u16>().unwrap();
    let sleep = arg_sleep_repeats.parse::<u64>().unwrap();
    let timeout = arg_timeout.parse::<u16>().unwrap();

    let token = Token::new(
        num_repeats,
        sleep,
        volume as u32,
        rate,
        packet_size as u32,
        timeout);

    if write {
        return Ok((token, arg_host.to_string(), port, downlink, debug, Some(String::from(arg_file))));
    } else {
        return Ok((token, arg_host.to_string(), port, downlink, debug, None));
    }
}