# cprover.py
                                             
from calliope_mini import pin28, pin29, pin30

def forward():
    pin28.write_analog(v)
    pin29.write_digital(1)
    pin30.write_digital(1)

def left():
    pin28.write_analog(v)
    pin29.write_digital(0)
    pin30.write_digital(1)

def right():
    pin28.write_analog(v)
    pin29.write_digital(1)
    pin30.write_digital(0)

def stop():
    pin28.write_digital(0)

def move():
    left()

def rewind():    
    right()

def setSpeed(speed):
    global v
    v = int(speed / 100 * 1023)

setSpeed(100)
