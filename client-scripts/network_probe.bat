@echo off

netsh wlan show interfaces | findstr "Name Radio  Band Channel Receive Transmit Signal Profile" | findstr /v "Connection mode"

for /l %%x in (1, 1, 9) do (
    ping -n 3 127.0.0.1 >NUL
    netsh wlan show interfaces | findstr "Name Radio  Band Channel Receive Transmit Signal Profile" | findstr /v "Connection mode" 
)
