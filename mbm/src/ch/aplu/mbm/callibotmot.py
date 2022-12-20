# callibotmot.py
# Version 1.0, Apr 24, 2021 / AR

import gc
from calliope_mini import i2c, sleep
import machine

class Motor:
    def __init__(self, id):
        self._id = id

    def _w(self, d, s):
        if self._id == 0:
            _self = 0x00
        else:
            _self = 0x02    
        try:           
            i2c.write(0x20, bytearray([_self, d, s]))
        except:
            print("Please switch on mbRobot!")
            while True:
                pass
    
    def rotate(self, s):
        v = abs(s)
        v = v + 50 
        if s > 0:           
            self._w(0, v)    
        elif s < 0:           
            self._w(1, v) 
        else:   
            self._w(0, 0)    

 
delay = sleep
def setLED(on):
    if on == 1:
        i2c.write(0x21, bytearray([0,0x03])) 
    else:
        i2c.write(0x21, bytearray([0,0]))   

motL = Motor(0)
motR = Motor(2)



