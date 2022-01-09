import re 
import matplotlib.pyplot as plt
import numpy as np
import seaborn as sns

plt.rcParams['font.family']='serif'

a_soldiers = []
b_soldiers = []
a_miners = []
b_miners = []

#raw_text_file = input("ENTER FILE PATH: ")
raw_text_file = r'C:\Users\Research\Documents\GitHub\DROPTABLE2022\random\match_output_dump.txt'
with open(raw_text_file, 'r') as f: 
    for line in f: 
        if '[B:SOLDIER' in line: 
            line = line.split(']')[0]
            line = line.split('#')[1].split('@')[0]
            b_soldiers.append(int(line))
        elif '[B:MINER' in line: 
            line = line.split(']')[0]
            line = line.split('#')[1].split('@')[0]
            b_miners.append(int(line))
        elif '[A:SOLDIER' in line: 
            line = line.split(']')[0]
            line = line.split('#')[1].split('@')[0]
            a_soldiers.append(int(line)) 
        elif '[A:MINER' in line: 
            line = line.split(']')[0]
            line = line.split('#')[1].split('@')[0]
            a_miners.append(int(line))

a_miners = np.array(list(set(a_miners)))
a_miners = a_miners%10

b_miners = np.array(list(set(b_miners)))
b_miners = b_miners%10

a_soldiers = np.array(list(set(a_soldiers)))
a_soldiers = a_soldiers%10

b_soldiers = np.array(list(set(b_soldiers)))
b_soldiers = b_soldiers%10


sns.displot([a_miners, b_miners, a_soldiers, b_soldiers], label=['a', 'b', 'c', 'd'], kde='true', legend='false')

plt.xlabel("Last Digit")

plt.show()