# Repo Details
This repo is divided into 
- `client-scripts/`, which contains scripts I wrote for client probing of the network **on a Windows machine**. If not on Windows, you are free to still use the JSON files and/or `map_mcs_table.py`, but if you are on Linux just prefer to use `iw`.
- `CRUSP/`, which houses the main codebase cloned from https://gitlab.com/gitlabwolf/infrastructure-for-mobile. The code has been minimally adapted (extracted Dockerfile dependencies, replaced HTTP request library, disconnected auxillary services) such that the Rust binaries can be run locally with `cargo` as detailed in the next section.
    - `CRUSP/measurement_server` contains the code for spawning the CRUSP server
    - `CRUSP/measurement_client_standalone` contains the code for spawning a CRUSP client.
    - All other subdirectories either support/link with these two directories, such as `measurement_client_rust` and `measurement_shared_rust`, or are non-essential auxillary services, such as `settings-service` and `database-service`.
    - If you would like to try the full CRUSP experience, clone directly from https://gitlab.com/gitlabwolf/infrastructure-for-mobile and follow the READMEs to set up the Android application.

<br/>

# Running CRUSP
First, to get CRUSP running, spawn the server using `cargo`. You may need to install `cargo` and run `cargo update` before this step.
```
## In root dir ##

cd CRUSP/measurement_server
RUST_LOG=DEBUG cargo run
```
Will spawn a CRUSP server running at `localhost:8099` that you can test with local CRUSP clients.
To expose the server to your LAN/WLAN, instead run
`cargo run --release`, which exposes the CRUSP server at your local IP and port 8099. This IP is specified in `CRUSP/measurement_server/Rocket.toml`. For example, mine looks like:
```
[development]
address = "localhost"
port = 8099
log = "normal"
#workers = [number of cpus * 2]
#keep_alive = 5
#secret_key = [randomly generated at launch]
#limits = { forms = 32768 }

[production]
address = "192.168.1.156"
port = 8099
log = "normal" # normal, debug or cricitcal
workers = 16 # [number of cpus * 2] - hossman has 8 cores
keep_alive = 0 # indicating the minimum amount of time an idle connection has to be kept opened (in seconds), no multiple access is needed
# secret_key = [randomly generated at launch]
limits = { forms = 32768 } # maximum amount of data rocket will accept for a givewn data type
```
Simply change the `address` field under the `production` block with your local IP, which you can find using `ifconfig` on Linux.

With the server running, you can test a client connection:
```
## In root dir ##

cd CRUSP/measurement_client_standalone
RUST_LOG=DEBUG cargo run -- -h 192.168.1.156 -p 8099
```
You can configure host with the `-h` flag and port with the `-p` flag. You can also toggle downlink vs uplink tests with the `-u` flag. For a full list of options, check `CRUSP/measurement_client_standalone/src/main.rs`.

<br/>

# Running Experiments
You are free to create your own scripts to perform batches of experiments, or you can re-use mine. I provide the following:
- `CRUSP/measurement_client_standalone/measure.sh` 
- `CRUSP/measurement_client_standalone/run_experiments.sh`
    - These two spawn CRUSP clients to connect to an already running CRUSP server and dump `grep`'d output of measured/estimated ABW and packets lost. Examine each script to see which suits your use case. `run_experiments.sh` also includes an iperf dump. The former dumps output in `./output/`, the latter in `./output/mcs/`
    - The one and only cmd line argument to each script is the name of the output file to dump results in.
- `CRUSP/measurement_client_standalone/output`
    - Contains raw output of experiment results
    - `analysis.py` has two methods that aggregate/summarize (and in the case of distance-based, plot) the data in the `output` folder. Feel free to use it or adapt it.
    - `CRUSP Workbook.xlsx` is just a working place - see the report in the root of this repo for results discussion.
  