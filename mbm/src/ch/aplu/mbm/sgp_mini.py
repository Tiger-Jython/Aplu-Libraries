#sgp_mini.py
#SGP30 CO2-Sensor, Version 17.9.21/Patrik Anold
from calliope_mini import i2c
from calliope_mini import sleep

raw = [0, 0]
buf = [0,0,0,0,0,0]

# Initialize the IAQ algorithm
i2c.write(0x58, bytearray([0x20,0x03]))
sleep(1500) # wait for initialize

def getValues(a=0x58):
    # IAQ measure, command must be split! (wait for write)
    i2c.write(0x58, bytearray([0x20,0x08]))
    sleep(600)

    # IAQ Read (see NOTE)
    block = i2c.read(0x58, 6)
    buf = [0,0,0,0,0,0]
    for i in range(0,6):
            buf[i]=block[i]
    for i in range(0,2):
            raw[i]= buf[i*3]
            raw[i]<<=8
            raw[i]|=buf[i*3+1]
    return (raw[0], raw[1])



   
   
 
