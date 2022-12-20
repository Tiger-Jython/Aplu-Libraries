# callibot.py
# JA
# Version 1.1, June 30, 2021

from javax.swing import *
from java.awt import *
from java.lang import RuntimeException
import sys

from ch.aplu.robotsim import LegoRobot

del print

if LegoRobot.getGameGrid() != None:
    LegoRobot.getGameGrid().dispose()

try:
    from ch.aplu.callibotsim import *
except:
    print("Restart TigerJython when changing the robot.")
    sys.exit()


if Callibot.getGameGrid() != None:
    Callibot.getGameGrid().dispose()

RobotContext().init()


# ----------------- private functions and classes -------
def _makeRobot():
    global _robot
    if _robot == None:
        _robot = Callibot()
    else:
        if _robot.getGameGrid().isDisposed():
            raise RuntimeException("Java frame disposed") 

def _makeGear():
    global _gear
    if _gear == None:
        _gear = Gear()

class _MyMotor:
    def __init__(self, id):
        self._id = id

    def rotate(self, rotationSpeed):
        self._createMotors()
        if rotationSpeed > 0:
            _motors[self._id].setSpeed(abs(rotationSpeed))
            _motors[self._id].forward()
        elif rotationSpeed < 0:
            _motors[self._id].setSpeed(abs(rotationSpeed))
            _motors[self._id].backward()
        else:   
            _motors[self._id].stop()

    def _createMotors(self):
        if _motors[self._id] == None:
            _makeRobot()
            _motors[0] = Motor(0)
            _motors[1] = Motor(1)

class _MyInfraredSensor:
    def __init__(self, id):
        self._id = id
    
    def read_digital(self):
        if _irSensors[self._id] == None:
            _makeRobot()
            #_irSensors[self._id] = InfraredSensor(self._id + 3)
            _irSensors[self._id] = InfraredSensor(self._id)
        delay(10)
        return _irSensors[self._id].getValue()
    
    def getValue(self):
        if _irSensors[self._id] == None:
            _makeRobot()
            _irSensors[self._id] = InfraredSensor(self._id)
        delay(10)
        return _irSensors[self._id].getValue()

class _MyLed:
    def __init__(self, id):
        self._id = id
    
    def write_digital(self, v):
        if _leds[self._id] == None:
            _makeRobot()
            _leds[self._id] = Led(self._id)
        if v == 1:
	    _leds[self._id].setColor(255, 50, 0)
        else:
            _leds[self._id].setColor(Color.black)

class _MyButton:
    def __init__(self, id):
        self._id = id

    def was_pressed(self):
        _makeRobot()
        if self._id == BrickButton.ID_UP:
            rc = _robot.isUpHit()
        elif self._id == BrickButton.ID_DOWN:
            rc = _robot.isDownHit()
        if self._id == BrickButton.ID_LEFT:
            rc = _robot.isLeftit()
        elif self._id == BrickButton.ID_RIGHT:
            rc = _robot.isRightHit()
        if self._id == BrickButton.ID_ENTER:
            rc = _robot.isEnterHit()
        elif self._id == BrickButton.ID_ESCAPE:
            rc = _robot.isEscapeHit()
        else:
            rc = False
        return rc
        
# ------------------- public functions ----------------
def getRobot():
    _makeRobot()
    return _robot

def getDistance():
    global _ultrasonicSensor
    _makeRobot()
    if _ultrasonicSensor == None:
        _ultrasonicSensor = UltrasonicSensor()    
    delay(10)
    return _ultrasonicSensor.getDistance()

def setBeamAreaColor(color):
    global _ultrasonicSensor
    _makeRobot()
    if _ultrasonicSensor == None:
        _ultrasonicSensor = UltrasonicSensor()    
    _ultrasonicSensor.setBeamAreaColor(color)

def setProximityCircleColor(color):
    global _ultrasonicSensor
    _makeRobot()
    if _ultrasonicSensor == None:
        _ultrasonicSensor = UltrasonicSensor()    
    _ultrasonicSensor.setProximityCircleColor(color)

def setMeshTriangleColor(color):
    global _ultrasonicSensor
    _makeRobot()
    if _ultrasonicSensor == None:
        _ultrasonicSensor = UltrasonicSensor()    
    _ultrasonicSensor.setMeshTriangleColor(color)

def eraseBeamArea():
    global _ultrasonicSensor
    _makeRobot()
    if _ultrasonicSensor == None:
        _ultrasonicSensor = UltrasonicSensor()    
    _ultrasonicSensor.eraseBeamArea()

def reset():
    _makeRobot()
    _robot.reset()

def forward():
    _makeRobot()
    _makeGear()
    _gear.forward()
    
def backward():
    _makeRobot()
    _makeGear()
    _gear.backward()
    
def left():
    _makeRobot()
    _makeGear()
    _gear.left()
    
def right():
    _makeRobot()
    _makeGear()
    _gear.right()
    
def leftArc(r):
    _makeRobot()
    _makeGear()
    _gear.leftArc(r)

def rightArc(r):
    _makeRobot()
    _makeGear()
    _gear.rightArc(r)

def stop():
    _makeRobot()
    _makeGear()
    _gear.stop()

def isMoving():
    _makeRobot()
    _makeGear()
    return _gear.isMoving()

def setSpeed(speed):
    _makeRobot()
    _makeGear()
    _gear.setSpeed(speed)
    
def exit():
    _makeRobot()
    _robot.exit()
    
def delay(t):
    Tools.delay(t)   

sleep = delay

def playTone(frequency, duration):
    _makeRobot()
    _robot.playTone(frequency, duration)

def setLED(on):
    _makeRobot()
    ledLeft.write_digital(on)
    ledRight.write_digital(on)
	
def setLEDLeft(on):
    _makeRobot()
    ledLeft.write_digital(on)

def setLEDRight(on):
    _makeRobot()
    ledRight.write_digital(on)	
	
def irLeftValue():
    return irLeft.read_digital()
		
def irRightValue():
    return irRight.read_digital()

def tsValue():
    return ts.getValue()

def tsLeftValue():
    return tsLeft.getValue()
        
def tsRightValue():
    return tsRight.getValue()        		

def setAlarm(on):
    _makeRobot()
    _robot.setAlarm(on)

# ------------------- private variables ----------------
_robot = None
_gear = None
_motors = [None] * 2
_irSensors = [None] * 5
_leds = [None] * 2
_ultrasonicSensor = None

# ------------------- public variables ----------------
motL = _MyMotor(0)
motR = _MyMotor(1)
ts = _MyInfraredSensor(0)
tsLeft = _MyInfraredSensor(1)
tsRight = _MyInfraredSensor(2)
irLeft = _MyInfraredSensor(3)
irRight = _MyInfraredSensor(4)
ledLeft = _MyLed(0)
ledRight = _MyLed(1)
button_up = _MyButton(BrickButton.ID_UP)
button_down = _MyButton(BrickButton.ID_DOWN)
button_left = _MyButton(BrickButton.ID_LEFT)
button_right = _MyButton(BrickButton.ID_RIGHT)
button_enter = _MyButton(BrickButton.ID_ENTER)
button_escape = _MyButton(BrickButton.ID_ESCAPE)

