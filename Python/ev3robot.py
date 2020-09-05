# ev3robot.py
# AP
# Version 1.6, March 21, 2017

import os, sys

from ch.aplu.ev3 import *
from javax.swing import *
from java.awt import *
from enum import enum
from jarray import array


import ch.aplu.ev3.ArduinoLink as JavaArduinoLink

# redefine ArduinoLink
class ArduinoLink(JavaArduinoLink):
    def __init__(self, port):
        JavaArduinoLink.__init__(self, port)

    def getReply(self, request, reply):
        if (not isinstance(reply, list)) or len(reply) < 16:
              raise ValueError("Illegal reply param in ArduinoLink.getReply()")

        data = [0] * 16
        dataAry = array(data, 'i')  # must use jarray.array to get back values
        JavaArduinoLink.getReply(self, request, dataAry)
        dataList = dataAry.tolist()
        for i in range(16):
            reply[i] = dataList[i]


    def getReplyString(self, request):
        reply = [0] * 16
        self.getReply(request, reply)
        s = ""
        for i in range(16):  
           if reply[i] > 0 and reply[1] < 256:
                s += unichr(reply[i])
           if reply[i] == 0: # terminate at C null terminator
               break
        return s 


ConnectPanel.setDefaultIPAddress(getTigerJythonFlag("aplu.device.ip"))
           
ColorSensor.initColorCubes()


