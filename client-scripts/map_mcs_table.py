import pandas as pd
import numpy as np
from collections import defaultdict
import json

c_arrays = [
    ["OFDM (Prior 11ax)"] * 8 + ["OFDM (802.11ax)"] * 12,
    "20MHz	20MHz	40MHz	40MHz	80MHz	80MHz	160MHz	160MHz	20MHz	20MHz	20MHz	40MHz	40MHz	40MHz	80MHz	80MHz	80MHz	160MHz	160MHz	160MHz".split("\t"),
    "0.8µs GI	0.4µs GI	0.8µs GI	0.4µs GI	0.8µs GI	0.4µs GI	0.8µs GI	0.4µs GI	0.8µs GI	1.6µs GI	3.2µs GI	0.8µs GI	1.6µs GI	3.2µs GI	0.8µs GI	1.6µs GI	3.2µs GI	0.8µs GI	1.6µs GI	3.2µs GI".replace("µ", "u").split("\t")
]
c_tuples = list(zip(*c_arrays))
multi_columns = pd.MultiIndex.from_tuples(c_tuples, names=["Radio Type", "Data Width", "Guard Interval"])

r_arrays = [
    "1	1	1	1	1	1	1	1	1	1	1	1	2	2	2	2	2	2	2	2	2	2	2	2".split("\t"),
    "BPSK	QPSK	QPSK	16-QAM	16-QAM	64-QAM	64-QAM	64-QAM	256-QAM	256-QAM	1024-QAM	1024-QAM	BPSK	QPSK	QPSK	16-QAM	16-QAM	64-QAM	64-QAM	64-QAM	256-QAM	256-QAM	1024-QAM	1024-QAM".split("\t"),
    "1/2	 1/2	 3/4	 1/2	 3/4	 2/3	 3/4	 5/6	 3/4	 5/6	 3/4	 5/6	 1/2	 1/2	 3/4	 1/2	 3/4	 2/3	 3/4	 5/6	 3/4	 5/6	 3/4	 5/6".split("\t ")
]

r_tuples = list(zip(*r_arrays))
multi_rows = pd.MultiIndex.from_tuples(r_tuples, names=["Spatial Streams", "Modulation", "Coding"])

# Actual sequential data:
rows = """6.5	7.2	13.5	15	29.3	32.5	58.5	65	8.6	8.1	7.3	17.2	16.3	14.6	36	34	30.6	72.1	68.1	61.3
13	14.4	27	30	58.5	65	117	130	17.2	16.3	14.6	34.4	32.5	29.3	72.1	68.1	61.3	144.1	136.1	122.5
19.5	21.7	40.5	45	87.8	97.5	175.5	195	25.8	24.4	21.9	51.6	48.8	43.9	108.1	102.1	91.9	216.2	204.2	183.8
26	28.9	54	60	117	130	234	260	34.4	32.5	29.3	68.8	65	58.5	144.1	136.1	122.5	288.2	272.2	245
39	43.3	81	90	175.5	195	351	390	51.6	48.8	43.9	103.2	97.5	87.8	216.2	204.2	183.8	432.4	408.3	367.5
52	57.8	108	120	234	260	468	520	68.8	65	58.5	137.6	130	117	288.2	272.2	245	576.5	544.4	490
58.5	65	121.5	135	263.3	292.5	526.5	585	77.4	73.1	65.8	154.9	146.3	131.6	324.3	306.3	275.6	648.5	612.5	551.3
65	72.2	135	150	292.5	325	585	650	86	81.3	73.1	172.1	162.5	146.3	360.3	340.3	306.3	720.6	680.6	612.5
78	86.7	162	180	351	390	702	780	103.2	97.5	87.8	206.5	195	175.5	432.4	408.3	367.5	864.7	816.7	735
N/A	N/A	180	200	390	433.3	780	866.7	114.7	108.3	97.5	229.4	216.7	195	480.4	453.7	408.3	960.8	907.4	816.7
								129	121.9	109.7	258.1	243.8	219.4	540.4	510.4	459.4	1080.9	1020.8	918.8
								143.4	135.4	121.9	286.8	270.8	243.8	600.5	567.1	510.4	1201	1134.3	1020.8
13	14.4	27	30	58.5	65	117	130	17.2	16.3	14.6	34.4	32.5	29.3	72.1	68.1	61.3	144.1	136.1	122.5
26	28.9	54	60	117	130	234	260	34.4	32.5	29.3	68.8	65	58.5	144.1	136.1	122.5	288.2	272.2	245
39	43.3	81	90	175.5	195	351	390	51.6	48.8	43.9	103.2	97.5	87.8	216.2	204.2	183.8	432.4	408.3	367.5
52	57.8	108	120	234	260	468	520	68.8	65	58.5	137.6	130	117	288.2	272.2	245	576.5	544.4	490
78	86.7	162	180	351	390	702	780	103.2	97.5	87.8	206.5	195	175.5	432.4	408.3	367.5	864.7	816.7	735
104	115.6	216	240	468	520	936	1040	137.6	130	117	275.3	260	234	576.5	544.4	490	1152.9	1088.9	980
117	130	243	270	526.5	585	1053	1170	154.9	146.3	131.6	309.7	292.5	263.3	648.5	612.5	551.3	1297.1	1225	1102.5
130	144.4	270	300	585	650	1170	1300	172.1	162.5	146.3	344.1	325	292.5	720.6	680.6	612.5	1441.2	1361.1	1225
156	173.3	324	360	702	780	1404	1560	206.5	195	175.5	412.9	390	351	864.7	816.7	735	1729.4	1633.3	1470
N/A	N/A	360	400	780	866.7	1560	1733.3	229.4	216.7	195	458.8	433.3	390	960.8	907.4	816.7	1921.6	1814.8	1633.3
								258.1	243.8	219.4	516.2	487.5	438.8	1080.9	1020.8	918.8	2161.8	2041.7	1837.5
								286.8	270.8	243.8	573.5	541.7	487.5	1201	1134.3	1020.8	2402	2268.5	2041.7
""".strip().split("\n")
data = []
for row in rows:
    data += row.split("\t")

# print(data)

df = pd.DataFrame(np.array(data).reshape((len(multi_rows),len(multi_columns))), 
             index=multi_rows,
             columns=multi_columns)

possible_mcs_map = defaultdict(list) # map will hold key of bandwidth (in mbps) -> value of list of possible mcs indices/configurations

for row_index, row in df.iterrows():
    for col_index, col in row.items():
        mcs_config = tuple([*row_index, *col_index])
        # print(f"{mcs_config}: {col}")
        try:
            col = float(col)
            if col != 0:
                possible_mcs_map[col].append(mcs_config)
        except:
            pass
            
# for k,v in possible_mcs_map.items():
#     print(k,v)

with open('possible_mcs_map.json', 'w') as f:
    json.dump(possible_mcs_map, f)

# Now create map of MCS -> Indices (VHT, HT, HE) i.e. (802.11n, 802.11ac, 802.11ax)
mcs_df = pd.read_csv("mcs_sheet.csv", skiprows=2, index_col=[3, 4, 5], usecols=[0, 1, 2, 3, 4, 5])
mcs_df.dropna(how="all", inplace=True)
mcs_map = {}
for row in mcs_df.itertuples():
    mcs_map[str((row.Index[0], row.Index[1], row.Index[2].strip()))] = (row.HT, row.VHT, row.HE)

# for k,v in mcs_map.items():
#     print(k,v)

with open('mcs_index_map.json', 'w') as f:
    json.dump(mcs_map, f)

