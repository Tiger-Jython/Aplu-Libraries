# mbrobot_plusV2.py
# Version 1.0 (27.7.2023)
 
from microbit import i2c,pin0,pin1,pin2,pin13,pin14,pin15,sleep
import machine
import gc
import music

_v = 50
_axe = 0.082

# According to https://github.com/lancaster-university/microbit-dal/blob/master/source/drivers/MicroBitPin.cpp#L245:
# a duty cycle of 500us per 20ms corresponds to setting the servo to 0 degrees
# a duty cycle of 2500us per 20ms corresponds to setting the servo to 180 degrees
# in the range from 0 - 1023, that is
#     for 0 degrees: 0.5/20 * 1024 = 25.6
#     for 180 degrees: 2.5/20 * 1024 = 128
# but the following default values seem to work better :)

_min_angle_val = 25
_max_angle_val = 131
    
def w(d1, s1, d2, s2):
    try:
        i2c.write(0x10, bytearray([0x00,d1, s1, d2, s2]))      
    except:
        print("Please switch on mbRobot!")
        
def setSpeed(speed):
    global _v
    if speed < 20 and speed != 0:
        _v = speed + 5
    else:
        _v = speed 
  
def stop():
    w(0, 0, 0, 0) 
    
def resetSpeed():
    _v = 50          

def forward():
    w(0, _v, 0, _v)

def backward():
    w(1, _v, 1, _v)          
            
def left():
    m = 1.825 -0.0175 * _v
    w(1, int(_v * m), 0, int(_v * m))
    
def right():
    m = 1.825 -0.0175 * _v
    w(0, int(_v * m), 1, int(_v * m))
    
def rightArc(r):
    v = abs(_v)
    if r < _axe:
        v1 = 0
    else:
        f = (r - _axe) / (r + _axe) * (1 - v * v / 200000)             
        v1 = int(f * v)
    if _v > 0:
        w(0, v, 0, v1)
    else:
        w(1, v1, 1, v)

def leftArc(r):
    v = abs(_v)
    if r < _axe:
        v1 = 0
    else:
        f = (r - _axe) / (r + _axe) * (1 - v * v / 200000)        
        v1 = int(f * v)
    if _v > 0:
        w(0, v1, 0, v)
    else:
        w(1, v, 1, v1)    

def getDistance():
    pin13.write_digital(1)
    pin13.write_digital(0)
    p = machine.time_pulse_us(pin14, 1, 50000)
    cm = int(p / 58.2 - 0.5)
    return cm if cm > 0 else 255 

class Motor:
    def __init__(self, id):
        self._id = 2 * id
        
    def _w(self, d, s):
        try:
            i2c.write(0x10, bytearray([self._id, d, s]))
        except:
            print("Please switch on mbRobot!")
            while True:
                pass               

    def rotate(self, s):
        p = abs(s) 
        if s > 0:
            self._w(0, p)    
        elif s < 0:
            self._w(1, p) 
        else:   
            self._w(0, 0)
       
class LEDState:
    OFF = 0
    RED = 1
    GREEN = 2
    YELLOW = 3
    BLUE = 4
    PINK = 5
    CYAN = 6
    WHITE = 7

def setLED(state, stateR=None):
    stateR = stateR or state
    i2c.write(0x10, bytearray([0x0B, state, stateR]))
        
def setLEDLeft(state):
    i2c.write(0x10, bytearray([0x0B, state])) 
    
def setLEDRight(state):
    i2c.write(0x10, bytearray([0x0C, state])) 
    
def setAlarm(on):
    if on:
        music.play(_m, wait = False, loop = True)    
    else:
        music.stop() 
        
def beep():
    music.pitch(2000, 200, wait = False)   

def setServo(s, angle):
    if s == "P0":
        pin = pin0
    elif s == "P1":
        pin = pin1
    elif s == "P2":
        pin = pin2
    else:
        print("Unknown Servo Port. Must be P0, P1 or P2")
        return

    if angle < 0 or angle > 180:
        print("Invalid angle. Must be between 0 and 180")
        return
    
    frac = angle / 180.0
    val = _min_angle_val + (_max_angle_val - _min_angle_val) * frac
    
    pin.set_analog_period(20)
    pin.write_analog(int(val))

def setMinAngleVal(a):
    global _min_angle_val
    _min_angle_val = a

def setMaxAngleVal(a):
    global _max_angle_val
    _max_angle_val = a    

def ir_read_values_as_byte():
    i2c.write(0x10, bytearray([0x1D]))
    buf = i2c.read(0x10, 1)
    return ~buf[0]

class IR:
    R2 = 0
    R1 = 1
    M = 2
    L1 = 3
    L2 = 4
    masks = [0x01,0x02,0x04,0x08,0x10]
   
class IRSensor:
    def __init__(self, index):
        self.index = index
        
    def read_digital(self):
        byte = ir_read_values_as_byte()
        return (byte & IR.masks[self.index]) >> self.index

irLeft = IRSensor(IR.L1)
irRight = IRSensor(IR.R1)
irL1 = IRSensor(IR.L1)
irR1 = IRSensor(IR.R1)
irL2 = IRSensor(IR.L2)
irR2 = IRSensor(IR.R2)
irM = IRSensor(IR.M)
pin2.set_pull(pin2.NO_PULL)
motL = Motor(0)
motR = Motor(1)
delay = sleep
_m = ['c6:1', 'r', 'c6,1', 'r', 'r', 'r']

