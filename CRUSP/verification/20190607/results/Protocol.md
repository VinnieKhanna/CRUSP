# Test runs on old Nemo phone - Samsung (Android 5.0.1)

At first I performed a reference measurement on the Nemo-Phone (With Android 5.0.1) with iperf3.

```bash
./iperf3 -c squid2.nt.tuwien.ac.at -p 1001 -t 60 -R
```

The result was `67.3 MBit/sec` for sender and `66.9 MBit/sec` for receiver.

In the next step I performed measurements with the rust standalone tool on the same phone.
```bash
./measurement_client_standalone --host hossman.nt.tuwien.ac.at --port 8000 -w
```

## Testrun with default settings
### Settings
- Packet size: 6200 Byte
- Total size: 930 KB
- Speed: 150 MBit/s
- Repeats: 3

### Results: 
`66.466 MBit/s`, `64.257 MBit/s`, `66.826 MBit/s`

### Files
- Measurement: 6200.csv
- Nemo: 19Jun07_165021.zip

## Testrun with 1200 Bytes packet-size
### Settings
- Packet size: **1200** Byte
- Total size: 930 KB
- Speed: 150 MBit/s
- Repeats: 3

### Results
`64.172 MBit/s`, `63.330 MBit/s`, `54.770 MBit/s`

### Files
- Measurement: 1200.csv
- Nemo: 19Jun07_165652.zip

## Testrun with 48 KB & 6200 Bytes
### Settings
- Packet size: **6200** Byte
- Total size: **48** KB
- Speed: 150 MBit/s
- Repeats: 3

### Results
more flactuating

### Files
- Measurement: 48kb.csv
- Nemo: 19Jun07_170603.zip

## Testrun with 48 KB & 1200 Bytes
### Settings
- Packet size: **1200** Byte
- Total size: **48** KB
- Speed: 150 MBit/s
- Repeats: 3

### Results
more flactuating

### Files
- Measurement: 48kb120.csv
- Nemo: 19Jun07_170639.zip

# Test runs on new Nemo phone - Sony (Android 8.0)

At first I performed a reference measurement on the Nemo-Phone with iperf3.

```bash
./iperf3 -c squid2.nt.tuwien.ac.at -p 1001 -t 60 -R
```

The result was again around `67,5 MBit/s`

## Testrun with default settings


In the next step I performed measurements with the installed app on the Sony-phone.
Used settings: Crusp 930 with 1 repeat, 930KB, 6200 Byte packet size and 150 MBit/s rate.

Overview of test-results can be seen in `measurement-results.csv`.
- First 8 tests are from the default-settings with `67.43 MBit/s` to `69.74 MBit/s`
- The next 6 tests are with 233 KB size and results of with `65.04 MBit/s` to `73.31 MBit/s`
- The next 9 tests are with 117 KB size and results range from `67.19 MBit/s` to `82.87 MBit/s`
- The last 3 are with 58 KB size and results range from `74.17 MBit/s` to `85.67 MBit/s`

The detailed packet-arrival times can be seen in `received_packet_details_for_measurement_1975.csv`
