import os
import seaborn as sns
import numpy as np
import pandas as pd
import matplotlib.pyplot as plt

plt.rc("font", size=4)
files = os.listdir(os.getcwd())
fig = plt.figure(dpi=150)
fig.suptitle("CRUSP Bandiwdth Measurement Results (Mbps)")
fig.subplots_adjust(hspace=.6, wspace=.6)
count = 1
for filename in files:
    if ".txt" not in filename and ".py" not in filename:  
        rates_list, packets_list = [], []
        with open(filename, 'r') as f:
            for line in f.readlines():
                [rate, packets] = line.split(", ")
                rate = rate.split(": ")[1]
                packets = packets.split(": ")[1]
                packets = packets[:packets.index("/")]
                rates_list.append(float(rate))
                packets_list.append(int(packets))
            #data = np.array([rates_list, packets_list]).T
            rates = np.array(rates_list)
            rates_avg = round(np.average(rates), 2)
            ax = fig.add_subplot(3, 4, count)
            count += 1
            ax.set_title(filename)
            sns.lineplot(x=np.arange(len(rates_list)), y=rates)
            plt.axhline(rates_avg, linestyle="--")
            plt.text(21, rates_avg, rates_avg)
            print(f"{filename} average bandwidth: {sum(rates_list)/len(rates_list)} Mbps")
            print(f"{filename} average packets received: {sum(packets_list)/len(packets_list)}/930\n")
    
plt.show()   
