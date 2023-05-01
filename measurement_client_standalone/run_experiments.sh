# First, make sure we have a very clear iperf3 ground truth bandwidth
# Use UDP since CRUSP also does
iperf3 -c 192.168.0.205 -u | grep -e "sender" -e "receiver" > output/mcs/iperf_$1

# Second, run a large sequence of CRUSP tests
# Downlink
for i in {1..200}
do
	cargo run -- -h 192.168.0.205 -p 8099 | grep -i "naive rate sequence" >> output/mcs/downlink_$1
done

# Uplink
for i in {1..100}
do
	cargo run -- --uplink -h 192.168.0.205 -p 8099 | grep -i "naive rate sequence" >> output/mcs/uplink_$1
done
