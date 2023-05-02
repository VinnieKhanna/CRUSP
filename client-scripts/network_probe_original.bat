@echo off

netsh wlan show interfaces | findstr "Name Radio  Band Channel Receive Transmit Signal Profile" > probe-%1.txt

for /l %%x in (1, 1, 10) do (
    timeout /t 3 
    echo -------------------------------------- >> probe-%1.txt
    netsh wlan show interfaces | findstr "Name Radio  Band Channel Receive Transmit Signal Profile" >> probe-%1.txt
)

type probe-%1.txt