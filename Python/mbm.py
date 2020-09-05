# mbm.py
# Programming interface to the MicroBit manager
# Aug. 2017, AP

from ch.aplu.mbm import MBM
from java.io import File
import os
import time

def showFileList():
    MBM(["-b"])

def openTerminal():
    MBM(["-t"])

def enableDataCapture(enable):
    while isTerminalDisposed():
        time.sleep(1)
    MBM.enableDataCapture(enable)

def getDataLines():
    time.sleep(0.001)
    arrayList = MBM.getDataLines()
    mylist = []
    for i in range(arrayList.size()):
        mylist.append(arrayList.get(i))
    return mylist

def extract(filename):
    MBM.setDestinationDir(os.getcwd())
    MBM(["-e " + filename])
    time.sleep(1)  # wait until extraction is underway
    while MBM.getExtractionResult() == 1:
        time.sleep(0.1)
    rc = True if MBM.getExtractionResult() == 0 else False
    return rc
    
def copy(filename):
    currentDir = os.getcwd()
    fs = File.separator
    MBM(["-c " + currentDir + fs + filename])
        
def run(filename):
    currentDir = os.getcwd()
    fs = File.separator
    MBM(["-r " + currentDir + fs + filename])

def runMain():
    MBM(["-x"])

def flash(*args):
    if len(args) == 0:
        MBM(["-f"])
    else:
        currentDir = os.getcwd()
        fs = File.separator
        MBM(["-f", "-w " + currentDir + fs + args[0]])

def isTerminalDisposed():
    return MBM.isDisposed()