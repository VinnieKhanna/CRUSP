import os
import seaborn as sns
import numpy as np
import pandas as pd
import matplotlib.pyplot as plt

plt.rc("font", size=4)
# files = os.listdir(os.getcwd())
files = os.listdir("./distance-based")
fig = plt.figure(dpi=150)
fig.suptitle("CRUSP Bandiwdth Measurement Results (Mbps)")
fig.subplots_adjust(hspace=.6, wspace=.6)
count = 1
for filename in files:
    if "2-6-23" in filename:  
        rates_list, packets_list = [], []
        with open(f"./distance-based/{filename}", 'r') as f:
            for line in f.readlines():
                [rate, packets] = line.split(", ")
                rate = rate.split(": ")[1]
                packets = packets.split(": ")[1]
                packets = packets[:packets.index("/")]
                rates_list.append(float(rate))
                packets_list.append(int(packets))
            rates = np.array(rates_list)
            rates_avg = round(np.average(rates), 2)
            ax = fig.add_subplot(2, 4, count)
            count += 1
            ax.set_title("".join(filename.split("2-6-23-")))
            sns.lineplot(x=np.arange(len(rates_list)), y=rates)
            plt.axhline(rates_avg, linestyle="--")
            plt.text(21, rates_avg, rates_avg)
            print(f"{filename} average bandwidth: {sum(rates_list)/len(rates_list)} Mbps")
            print(f"{filename} average packets received: {sum(packets_list)/len(packets_list)}/930\n")
    
plt.show()   
            #data = np.array([rates_list, packets_list]).T
