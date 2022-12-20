# callibot.py
# Version 1.3, 17-Sept-2021 /JA
# new: Touchsensors

import gc
from calliope_mini import i2c, sleep

_axe = 0.06

def w(d1, d2, s1, s2):
    try:
        i2c.write(0x20, bytearray([0x00, d1, s1]))
        i2c.write(0x20, bytearray([0x02, d2, s2]))
    except:
        print("Please switch on Robot!")
        while True:
            pass
            
def setSpeed(speed):
    global _v
    _v = speed + 40
    #_v = int(speed * 1.1) + 35     

def forward():
    w(0, 0, _v, _v)

def backward():
    w(1, 1, _v, _v)
    
def stop():
    w(0, 0, 0, 0)
        
def right():
    v = int(_v * 1.1)
    w(0 if _v > 0 else 1, 1 if _v > 0 else 0, v , v)   

def left():
    v = int(_v * 1.1)  
    w(1 if _v > 0 else 0, 0 if _v > 0 else 1, v, v)

def rightArc(r):  
    v = abs(_v) 
    if r < _axe:
        v1 = 0
    else:            
        f = (r - _axe) / (r + _axe) * (1 - v * v / 200000)             
        v1 = int(f * v)
    if _v > 0:
        w(0, 0, v, v1)
    else:
        w(1, 1, v1, v)

def leftArc(r):  
    v = abs(_v) 
    if r < _axe:
        v1 = 0
    else:
        f = (r - _axe) / (r + _axe) * (1 - v * v / 200000)             
        v1 = int(f * v)
    if _v > 0:
        w(0, 0, v1, v)
    else:
        w(1, 1, v, v1)

def setLEDLeft(on):
    try:
        i2c.write(0x21, bytearray([0,0])) 
    except:
        print("Please switch on Robot!")
        while True:
            pass         
    if on == 1:
        i2c.write(0x21, bytearray([0,0x01])) 
    else:
        i2c.write(0x21, bytearray([0,0]))   
    
def setLEDRight(on):
    try:
        i2c.write(0x21, bytearray([0,0])) 
    except:
        print("Please switch on Robot!")
        while True:
            pass 
    if on == 1:
        i2c.write(0x21, bytearray([0,0x02])) 
    else:
        i2c.write(0x21, bytearray([0,0]))   

def setLED(on):
    try:
        i2c.write(0x21, bytearray([0,0])) 
    except:
        print("Please switch on Robot!")
        while True:
            pass 
    if on == 1:
        i2c.write(0x21, bytearray([0,0x03])) 
    else:
        i2c.write(0x21, bytearray([0,0]))   
     
def irLeftValue():
    try:
        buffer = i2c.read(0x21,1)    
        if (buffer[0] == 130 or buffer[0] == 131):
            return 1
        else:
            return 0   
    except:
        print("Please switch on Robot!")
        while True:
            pass 

def irRightValue():
    try:
        buffer = i2c.read(0x21,1)    
        if (buffer[0] == 129 or buffer[0] == 131):
            return 1
        else:
            return 0   
    except:
        print("Please switch on Robot!")
        while True:
            pass 
                                
def getDistance():
    try:
        buffer = i2c.read(0x21,3)
        dist = (256 * buffer[1] + buffer[2])/10
        return dist
    except:
        print("Please switch on Robot!")
        while True:
            pass  
      
def tsValue():
    try:
        buffer = i2c.read(0x21,1)          
        if (buffer[0] == 0x8C or buffer[0] == 0x8F):
            return 1
        else:
            return 0  
    except:
        print("Please switch on Robot!")
        while True:
            pass    

def tsLeftValue():
    try:
        buffer = i2c.read(0x21,1)          
        if (buffer[0] == 0x88 or buffer[0] == 0x8B):
            return 1
        else:
            return 0  
    except:
        print("Please switch on Robot!")
        while True:
            pass 
        
def tsRightValue():
    try:
        buffer = i2c.read(0x21,1)          
        if (buffer[0] == 0x84 or buffer[0] == 0x87):
            return 1
        else:
            return 0  
    except:
        print("Please switch on Robot!")
        while True:
            pass                         
                        
exit = stop
delay = sleep
_v = 90 # entspricht default Speed 50



