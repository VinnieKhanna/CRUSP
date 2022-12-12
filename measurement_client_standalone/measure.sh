for i in {1..20}
do
RUST_LOG=debug cargo run -- -h 192.168.0.205 -p 8099 | grep -i "naive rate sequence" >> output/downlink_$1

RUST_LOG=debug cargo run -- --uplink -h 192.168.0.205 -p 8099 | grep -i "naive rate sequence" >> output/uplink_$1

done

