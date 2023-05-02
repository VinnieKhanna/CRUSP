import json
import subprocess
import numpy as np
import signal

DEBUG=1
VERBOSE=1

assert(VERBOSE if DEBUG else True) # can only use verbose with debug enabled

### Load our transformed/processed data as maps
# maps 3-tuple of spatial stream, modulation, and coding to 3-tuple of HT, VHT, and HE index
with open("mcs_index_map.json", "r") as f:
    mcs_index_map = json.load(f)

# maps bandwidth (as string) to list of lists, each list being a 6-tuple 
with open("possible_mcs_map.json", "r") as f:
    possible_mcs_map = json.load(f)

### Actually take the measurements using network_probe.bat
proc = subprocess.Popen("network_probe.bat", stdout=subprocess.PIPE, universal_newlines=True, bufsize=1)
# Make sure to explicitly kill on ctrl+c
signal.signal(signal.SIGINT, lambda _, __: proc.kill())
measurements = []
measure = {}

### Stream/read the output line by line and look up possible MCS values
try:
    if not DEBUG:
        print("Receive MCS (HT, VHT, HE) \t Transmit MCS (HT, VHT, HE)".expandtabs(75))
        print("----------- \t ------------".expandtabs(75))
    for line in iter(proc.stdout.readline,''):
        if DEBUG:
            print(line.rstrip())
        k, v = tuple([x.strip() for x in line.split(":")])
        measure[k] = v
        if len(measure) == 8: # corresponds to one full netsh call
            if DEBUG:
                print()
            if 'Receive rate (Mbps)' in measure:
                key = str(round(float(measure['Receive rate (Mbps)']), 1))
                if key not in possible_mcs_map:
                    if DEBUG:
                        print("Exact Receive BW match not found in MCS table, try checking manually for decimals")
                    else:
                        print(" - \t\t\t", end=" ")
                else:
                    entries = possible_mcs_map[key]
                    if DEBUG:
                        if VERBOSE:
                            print(f"Possible Receive MCS for {key} Mbps:")
                            keys = ["Spatial Streams", "Modulation", "Coding", "Data Width", "Guard Interval"]
                            possible_indices = [mcs_index_map[f"({float(entry[0])}, '{entry[1]}', '{entry[2]}')"] for entry in entries]
                            for i, entry in enumerate(entries):
                                print(f"{'HT ' + str(possible_indices[i][0]) if measure['Radio type'] == '802.11n' else 'VHT ' + str(possible_indices[i][1]) if measure['Radio type'] == '802.11ac' else str(possible_indices[i])}", end="")
                                t_entry = entry[:3] + entry[4:]
                                print(f" – {', '.join(f'{k}: {v}' for k,v in dict(zip(keys, t_entry)).items())}")
                        else:
                            print(f"Possible Receive MCS:\n{key} Mbps -> {entries[0] if len(entries) == 1 else np.array(entries)}")
                            
                    else:
                        # this looks bad but it's just for pretty printing
                        mcs_vals = [tuple(['-' if np.isnan(idx) else int(idx) for idx in mcs_index_map[f"({float(entry[0])}, '{entry[1]}', '{entry[2]}')"]]) for entry in entries]
                        string_tuples = [f"({', '.join([str(el) for el in tup])})" for tup in mcs_vals]
                        string_rep = ', '.join([f"({', '.join([str(el) for el in tup])})" for tup in mcs_vals])
                        print(string_rep)
                        # print(*[f"({', '.join([str(el) for el in tup])})" for tup in mcs_vals], sep=", ", end="\r")
            else:
                print("No receive rate reported")
            
            if DEBUG:
                print()
            if 'Transmit rate (Mbps)' in measure:
                key = str(round(float(measure['Transmit rate (Mbps)']), 1))
                if key not in possible_mcs_map:
                    print("Exact Transmit BW match not found in MCS table, try checking manually for decimals")
                else:
                    entries = possible_mcs_map[key]
                    if DEBUG:
                        if VERBOSE:
                            print(f"Possible Transmit MCS for {key} Mbps:")
                            keys = ["Spatial Streams", "Modulation", "Coding", "Data Width", "Guard Interval"]
                            possible_indices = [mcs_index_map[f"({float(entry[0])}, '{entry[1]}', '{entry[2]}')"] for entry in entries]
                            for i, entry in enumerate(entries):
                                print(f"{'HT ' + str(possible_indices[i][0]) if measure['Radio type'] == '802.11n' else 'VHT ' + str(possible_indices[i][1]) if measure['Radio type'] == '802.11ac' else str(possible_indices[i])}", end="")
                                t_entry = entry[:3] + entry[4:]
                                print(f" – {', '.join(f'{k}: {v}' for k,v in dict(zip(keys, t_entry)).items())}")
                        else:
                            print(f"Possible Transmit MCS:\n{key} Mbps -> {entries[0] if len(entries) == 1 else np.array(entries)}")
                    else:
                        mcs_vals = [tuple(['-' if np.isnan(idx) else int(idx) for idx in mcs_index_map[f"({float(entry[0])}, '{entry[1]}', '{entry[2]}')"]]) for entry in entries]
                        print("\t".expandtabs(75), end="")
                        print(*[f"({', '.join([str(el) for el in tup])})" for tup in mcs_vals], sep=", ")

            else:
                print("No transmit rate reported")
            
            if DEBUG:
                print()
                print("----------------------------------------------")
            measurements.append(measure)
            measure = {}
except Exception as e:
    proc.kill()
    raise e

   