# simrobot.py
# AP
# Version 1.18, Aug 13, 2019

from javax.swing import *
from java.awt import *
from enum import enum
from java.lang import RuntimeException
import sys

from ch.aplu.mbrobotsim import MbRobot

if MbRobot.getGameGrid() != None:
    MbRobot.getGameGrid().dispose()

try:
    from ch.aplu.robotsim import *
except:
    print "Restart TigerJython when changing the robot."
    sys.exit()

if LegoRobot.getGameGrid() != None:
    LegoRobot.getGameGrid().dispose()

RobotContext().init()

# ----------------- private functions and classes -------
def _makeRobot():
    global _robot
    if _robot == None:
        _robot = LegoRobot()
    else:
        if _robot.getGameGrid().isDisposed():
            raise RuntimeException("Java frame disposed") 

def _makeGear():
    global _gear
    if _gear == None:
        _gear = Gear()
        _robot.addPart(_gear)

class _MyUltrasonicSensor:
    def __init__(self, id):
        self._id = id
    
    def getDistance(self):
        if _ultrasonicSensors[self._id] == None:
            _makeRobot()
            _ultrasonicSensors[self._id] = UltrasonicSensor(_sensorPorts[self._id])
            _robot.addPart(_ultrasonicSensors[self._id])
        delay(10)
        return _ultrasonicSensors[self._id].getDistance()

    def setBeamAreaColor(self, color):
        if _ultrasonicSensors[self._id] == None:
            _makeRobot()
            _ultrasonicSensors[self._id] = UltrasonicSensor(_sensorPorts[self._id])
            _robot.addPart(_ultrasonicSensors[self._id])
        _ultrasonicSensors[self._id].setBeamAreaColor(color)
    
    def setProximityCircleColor(self, color):
        if _ultrasonicSensors[self._id] == None:
            _makeRobot()
            _ultrasonicSensors[self._id] = UltrasonicSensor(_sensorPorts[self._id])
            _robot.addPart(_ultrasonicSensors[self._id])
        _ultrasonicSensors[self._id].setProximityCircleColor(color)
    
    def setMeshTriangleColor(self, color):
        if _ultrasonicSensors[self._id] == None:
            _makeRobot()
            _ultrasonicSensors[self._id] = UltrasonicSensor(_sensorPorts[self._id])
            _robot.addPart(_ultrasonicSensors[self._id])
        _ultrasonicSensors[self._id].setMeshTriangleColor(color)
    
    def eraseBeamArea(self):
        if _ultrasonicSensors[self._id] == None:
            _makeRobot()
            _ultrasonicSensors[self._id] = UltrasonicSensor(_sensorPorts[self._id])
            _robot.addPart(_ultrasonicSensors[self._id])
        _ultrasonicSensors[self._id].eraseBeamArea()

class _MyLightSensor:
    def __init__(self, id):
        self._id = id
    
    def getValue(self):
        if _lightSensors[self._id] == None:
            _makeRobot()
            _lightSensors[self._id] = LightSensor(_sensorPorts[self._id])
            _robot.addPart(_lightSensors[self._id])
        delay(10)
        return _lightSensors[self._id].getValue()

class _MyTouchSensor:
    def __init__(self, id):
        self._id = id
    
    def isPressed(self):
        if _touchSensors[self._id] == None:
            _makeRobot()
            _touchSensors[self._id] = TouchSensor(_sensorPorts[self._id])
            _robot.addPart(_touchSensors[self._id])
        delay(10)
        return _touchSensors[self._id].isPressed()

class _MyButton:
    def __init__(self, id):
        self._id = id

    def was_pressed(self):
        _makeRobot()
        if self._id == "ENTER":
            return _robot.isEnterHit()
        elif self._id == "ESCAPE":
            return _robot.isEscapeHit()
        elif self._id == "LEFT":
            return _robot.isLeftHit()
        elif self._id == "RIGHT":
            return _robot.isRightHit()
        elif self._id == "UP":
            return _robot.isUpHit()
        elif self._id == "DOWN":
            return _robot.isDownHit()

class _MyMotor:
    def __init__(self, id):
        self._id = id
    
    def _setup(self):
        if _motors[self._id] == None:
            _makeRobot()
            _motors[0] = Motor(_motorPorts[0])
            _robot.addPart(_motors[0])
            _motors[1] = Motor(_motorPorts[1])
            _robot.addPart(_motors[1])

    def _setupSingle(self):
        id = self._id
        if _motors[id] == None:
            _makeRobot()
	    _motors[id] = Motor(_motorPorts[id])
            _robot.addPart(_motors[id])

    def rotate(self, rotationalSpeed):
        self._setup()
        if rotationalSpeed > 0:
            _motors[self._id].setSpeed(abs(rotationalSpeed))
            _motors[self._id].forward()
        elif rotationalSpeed < 0:
            _motors[self._id].setSpeed(abs(rotationalSpeed))
            _motors[self._id].backward()
        else:
            _motors[self._id].stop()
        
    def getMotorCount(self):
        self._setup()
        return _motors[self._id].getMotorCount()
    
    def resetMotorCount(self):
        self._setup()
        _motors[self._id].resetMotorCount()
       
    def rotateTo(self, *args):
        self._setupSingle()
        _motors[self._id].rotateTo(*args)
        
    def continueTo(self, *args):
        self._setup()
        _motors[self._id].continueTo(*args)
    
    def continueRelativeTo(self, *args):
        self._setup()
        _motors[self._id].continueRelativeTo(*args)
        
    def isMoving(self):
        self._setup()
        return _motors[self._id].isMoving()
    
    def setAcceleration(self, acc):
        self._setup()
        _motors[self._id].setAcceleration(acc)

# ------------------- public functions ----------------
def getRobot():
    _makeRobot()
    return _robot

def getDistance():
    return us1.getDistance()

def setBeamAreaColor(color):
    us1.setBeamAreaColor(color)
    
def setProximityCircleColor(color):
    us1.setProximityCircleColor(color)
   
def setMeshTriangleColor(color):
    us1.setMeshTriangleColor(color)
    
def eraseBeamArea():
    us1.eraseBeamArea()

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

def moveTo(count, blocking = True):
    _makeRobot()
    _makeGear()
    _gear.moveTo(count, blocking)

def turnTo(count, blocking = True):
    _makeRobot()
    _makeGear()
    _gear.turnTo(count, blocking)
    
def exit():
    _makeRobot()
    _robot.exit()
    
def delay(t):
    Tools.delay(t)   

sleep = delay

def isEscapeHit():
    _makeRobot()
    return _robot.isEscapeHit()

def isDownHit():
    _makeRobot()
    return _robot.isDownHit()

def isUpHit():
    _makeRobot()
    return _robot.isUpHit()

def isLeftHit():
    _makeRobot()
    return _robot.isLeftHit()

def isRightHit():
    _makeRobot()
    return _robot.isRightHit()

def isEnterHit():
    _makeRobot()
    return _robot.isEnterHit()

def playTone(frequency, duration):
    _makeRobot()
    return _robot.playTone(frequency, duration)

def setLED(pattern):
    _makeRobot()
    return _robot.setLED(pattern)

def reset():
    _makeRobot()
    _robot.reset()
    


# ------------------- public variables ----------------
motA = _MyMotor(0)
motB = _MyMotor(1)
motL = motA
motR = motB

us1 = _MyUltrasonicSensor(0)
us2 = _MyUltrasonicSensor(1)
us3 = _MyUltrasonicSensor(2)
us4 = _MyUltrasonicSensor(3)

ls1 = _MyLightSensor(0)
ls2 = _MyLightSensor(1)
ls3 = _MyLightSensor(2)
ls4 = _MyLightSensor(3)

ts1 = _MyTouchSensor(0)
ts2 = _MyTouchSensor(1)
ts3 = _MyTouchSensor(2)
ts4 = _MyTouchSensor(3)

button_enter = _MyButton("ENTER")
button_escape = _MyButton("ESCAPE")
button_left = _MyButton("LEFT")
button_right = _MyButton("RIGHT")
button_up = _MyButton("UP")
button_down = _MyButton("DOWN")


# ------------------- private variables ----------------
_robot = None 
_gear = None
_ultrasonicSensors = [None] * 4
_lightSensors = [None] * 4
_touchSensors = [None] * 4
_motors = [None] * 4
_motorPorts = [MotorPort.A, MotorPort.B]
_sensorPorts = [SensorPort.S1, SensorPort.S2, SensorPort.S3, SensorPort.S4]

