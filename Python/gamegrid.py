# gamegrid.py
# AP
# Version 1.14, Feb 1, 2016

from ch.aplu.jgamegrid import *
from java.awt import Color, Point
from java.util import ArrayList
from ch.aplu.util import Monitor
from javax.swing import JColorChooser
from java.awt.event import KeyEvent
from java.awt import Font
from java.awt.geom.Point2D import Double
import math
from sys import exit
from enum import enum
from gvideo import *

g = None
__myCounter = 0

class WindowNotInitialized(Exception): pass
class FunctionDeprecated(Exception): pass

def isGameGridValid():
   if g == None or (g.isDisposed() and __myCounter != getProgramCounter()):
      raise WindowNotInitialized("Use \"makeGameGrid()\" to create the graphics window before calling GameGrid methods.")

def do(fun, args):
   y = None
   y = None
   if len(args) == 0:
      y = fun()
   elif len(args) == 1:
      y = fun(args[0])
   elif len(args) == 2:
      y = fun(args[0], args[1])
   elif len(args) == 3:
      y = fun(args[0], args[1], args[2])
   elif len(args) == 4:
      y = fun(args[0], args[1], args[2], args[3])
   elif len(args) == 5:
      y = fun(args[0], args[1], args[2], args[3], args[4])
   elif len(args) == 6:
      y = fun(args[0], args[1], args[2], args[3], args[4], args[5])
   elif len(args) == 7:
      y = fun(_args[0], args[1], args[2], args[3], args[4], args[5], args[6])
   elif len(args) == 8:
      y = fun(_args[0], args[1], args[2], args[3], args[4], args[5], args[6], args[7])
   else:
      raise ValueError("Illegal number of arguments")
   if y != None:
      return y

class MyActListener(GGActListener):
   def __init__(self, myAct):
      self.myAct = myAct
   def act(self):
      self.myAct()

class MyNavigationListener(GGNavigationListener):
   def __init__(self, kwargs):
      self.onReset = None
      self.onPause = None
      self.onPeriodChange = None
      self.onStart = None
      self.onStep = None
      for key, value in kwargs.items():
         if key == "resetted":
            self.onReset = value
         if key == "paused":
             self.onPause = value
         if key == "periodChanged":
             self.onPeriodChange = value
         if key == "started":
             self.onStarted = value
         if key == "stepped":
             self.onStep = value

   def resetted(self):
      if self.onReset != None:
         self.onReset()

   def paused(self):
      if self.onPause != None:
         self.onPause()

   def periodChanged(self, period):
      if self.onPeriodChange != None:
         self.onPeriodChange(period)

   def started(self):
      if self.onStart != None:
         self.onStart()

   def stepped(self):
      if self.onStep != None:
         self.onStep()

hideFromDebugView()

def makeGameGrid(*args, **kwargs):
   global __myCounter
   __myCounter = getProgramCounter()
   global g
   if g != None:
      g.dispose()
   if g == None or g.isDisposed():
      if len(args) == 0:
         g = GameGrid(**kwargs)
      elif len(args) == 1:
         g = GameGrid(args[0], **kwargs)
      elif len(args) == 2:
         g = GameGrid(args[0], args[1], **kwargs)
      elif len(args) == 3:
         g = GameGrid(args[0], args[1], args[2], **kwargs)
      elif len(args) == 4:
         g = GameGrid(args[0], args[1], args[2], args[3], **kwargs)
      elif len(args) == 5:
         g = GameGrid(args[0], args[1], args[2], args[3], args[4], **kwargs)
      elif len(args) == 6:
         g = GameGrid(args[0], args[1], args[2], args[3], args[4], args[5], **kwargs)
      elif len(args) == 7:
         g = GameGrid(args[0], args[1], args[2], args[3], args[4], args[5], args[6], **kwargs)
      elif len(args) == 8:
         g = GameGrid(args[0], args[1], args[2], args[3], args[4], args[5], args[6], args[7], **kwargs)
      else:
         raise ValueError("Illegal number of arguments")
   return g

def registerAct(myAct):
   g.addActListener(MyActListener(myAct))

def registerNavigation(**kwargs):
   g.addNavigationListener(MyNavigationListener(kwargs))

def actAll():
   isGameGridValid()
   g.actAll()

def activate():
   isGameGridValid()
   g.activate()

def addActListener(listener):
   isGameGridValid()
   g.addActListener(listener)

def addActor(*args):
   isGameGridValid()
   do(g.addActor, args)

def addActorNoRefresh(*args):
   isGameGridValid()
   do(g.addActorNoRefresh, args)

def addExitListener(listener):
   isGameGridValid()
   g.addExitListener(listener)

def addKeyListener(listener):
   isGameGridValid()
   g.addKeyListener(listener)

def addKeyRepeatListener(listener):
   isGameGridValid()
   g.addKeyRepeatListener(listener)

def addMouseListener(listener, mouseEventMask):
   isGameGridValid()
   g.addMouseListener(listener, mouseEventMask)

def addNavigationListener(listener):
   isGameGridValid()
   g.addNavigationListener(listener)

def addResetListener(listener):
   isGameGridValid()
   g.addResetListener(listener)

def addStatusBar(height):
   isGameGridValid()
   g.addStatusBar(height)

def addWindowStateListener(listener):
   isGameGridValid()
   g.addWindowStateListener(listener)

def createTileMap(nbHorzTiles, nbVertTiles, tileWidth, tileHeight):
   isGameGridValid()
   return g.createTileMap(nbHorzTiles, nbVertTiles, tileWidth, tileHeight)
    
def delay(time):
    GameGrid.delay(time)

def dispose():
   isGameGridValid()
   g.dispose()

def doPause():
   isGameGridValid()
   g.doPause()

def doReset():
   isGameGridValid()
   g.doReset()

def doRun():
   isGameGridValid()
   g.doRun()

def doStep():
   isGameGridValid()
   g.doStep()

def getActors(*args):
   isGameGridValid()
   return toList(do(g.getActors, args))

def getActorsAt(*args):
   isGameGridValid()
   return toList(do(g.getActorsAt, args))

def getAreaSize():
   isGameGridValid()
   return g.getAreaSize()

def getBg():
   isGameGridValid()
   return g.getBg()

def getBgColor():
   isGameGridValid()
   return g.getBgColor()

def getBgImagePath():
   isGameGridValid()
   return g.getBgImagePath()

def getBgImagePos():
   isGameGridValid()
   return g.getBgImagePos()
   
def getBgImagePosX():
   isGameGridValid()
   return g.getBgImagePosX()

def getBgImagePosY():
   isGameGridValid()
   return g.getBgImagePosY()

def getCellSize():
   isGameGridValid()
   return g.getCellSize()

def getClosingMode():
   return GameGrid.getClosingMode()

def getDiagonalLocations(location, up):
   isGameGridValid()
   return toList(g.getDiagonalLocations(location, up))

def getDoubleClickDelay():
   isGameGridValid()
   return g.getDoubleClickDelay()

def getEmptyLocations():
   isGameGridValid()
   return toList(g.getEmptyLocations())

def getFrame():
   isGameGridValid()
   return g.getFrame()

def getGridColor():
   isGameGridValid()
   return g.getGridColor()

def getImage():
   isGameGridValid()
   return g.getImage()

def getKeyChar():
   isGameGridValid()
   return g.getKeyChar()

def getKeyCharWait(*args):
   isGameGridValid()
   if len(args) == 0:
      return g.getKeyCharWait()
   if len(args) == 1:
      if args[0]:
          code = g.getKeyCharWait()
          if isDisposed():
             exit()
          return code
      else:
          return g.getKeyCharWait()

def getKeyCode():
   isGameGridValid()
   return g.getKeyCode()

def getKeyCodeWait(*args):
   isGameGridValid()
   if len(args) == 0:
      return g.getKeyCodeWait()
   if len(args) == 1:
      if args[0]:
          code = g.getKeyCodeWait()
          if isDisposed():
             exit()
          return code
      else:
          return g.getKeyCodeWait()

def getKeyModifiers():
   isGameGridValid()
   return g.getKeyModifiers()

def getKeyModifiersText():
   isGameGridValid()
   return g.getKeyModifiersText()

def getLineLocations(loc1, loc2, interjacent):
   isGameGridValid()
   return toList(g.getLineLocations(loc1, loc2, interjacent))

def getMouseLocation():
   isGameGridValid()
   return g.getMouseLocation()

def getNbCycles():
   isGameGridValid()
   return g.getNbCycles()

def getNbHorzCells():
   isGameGridValid()
   return g.NbHorzCells()

def getNbVertCells():
   isGameGridValid()
   return g.NbVertCells()

def getNbHorzPixels():
   isGameGridValid()
   return g.NbHorzPixels()

def getNbVertPixels():
   isGameGridValid()
   return g.NbVertPixels()

def getNumberOfActors(*args):
   isGameGridValid()
   return do(g.getNumberOfActors, args)

def getNumberOfActorsAt(*args):
   isGameGridValid()
   return do(g.NumberOfActorsAt, args)

def getOccupiedLocations():
   isGameGridValid()
   return toList(g.getOccupiedLocations())

def getOneActor(clazz):
   isGameGridValid()
   return g.getOneActor(clazz)

def getOneActorAt(*args):
   isGameGridValid()
   return do(g.getOneActorAt, args)

def getPaintOrderList(*args):
   isGameGridValid()
   return toList(do(g.getPaintOrderList, args))

def getPanel(*args):
   isGameGridValid()
   return do(g.getPanel, args)

def getPgHeight():
   isGameGridValid()
   return g.getPgHeight()

def getPgWidth():
   isGameGridValid()
   return g.getPgWidth()

def getPosition():
   isGameGridValid()
   return g.getPosition()

def getPressedKeyCodes():
   isGameGridValid()
   return toList(g.getPressedKeyCodes())

def getRandomDirection():
   isGameGridValid()
   return g.getRandomDirection()

def getRandomEmptyLocation():
   isGameGridValid()
   return g.getRandomEmptyLocation()

def getRandomLocation():
   isGameGridValid()
   return g.getRandomLocation()

def getSimulationPeriod():
   isGameGridValid()
   return g.getSimulationPeriod()

def getStackTrace(s):
   isGameGridValid()
   return g.getStackTrace(s)

def getTileMap():
   isGameGridValid()
   return g.getTileMap()

def getTouchedActors(clazz):
   isGameGridValid()
   return toList(g.getTouchedActors(clazz))

def getVersion():
   return GameGrid.getVersion()

def hide():
   isGameGridValid()
   g.hide()

def isActorColliding(a1, a2):
   isGameGridValid()
   return g.isActorColliding(a1, a2)
   
def isAtBorder(location):
   isGameGridValid()
   return g.isAtBorder(location)

def isDisposed():
   return GameGrid.isDisposed()

def isEmpty(location):
   isGameGridValid()
   return g.isEmpty(location)

def isInGrid(location):
   isGameGridValid()
   return g.isInGrid(location)

def isKeyPressed(keyCode):
   isGameGridValid()
   return g.isKeyPressed(keyCode)

def isPaused():
   isGameGridValid()
   return g.isPaused()

def isRunning():
   isGameGridValid()
   return g.isRunning()

def isShown():
   isGameGridValid()
   return g.isShown()

def isTileColliding(a, location):
   isGameGridValid()
   return g.isTileColliding(a, location)

def isUndecorated():
   isGameGridValid()
   return g.isUndecorated()

def kbhit():
   isGameGridValid()
   return g.kbhit()

def paint(g):
   isGameGridValid()
   return g.paint(g)

def playLoop(*args):
   isGameGridValid()
   return do(g.playLoop, args)

def playLoopExt(*args):
   isGameGridValid()
   return do(g.playLoopExt, args)

def playSound(*args):
   isGameGridValid()
   return do(g.playSound, args)

def playSoundExt(*args):
   isGameGridValid()
   return do(g.playSoundExt, args)

def refresh():
   isGameGridValid()
   g.refresh()

def removeActor(actor):
   isGameGridValid()
   return g.removeActor(actor)

def removeActors(clazz):
   isGameGridValid()
   return g.removeActors(clazz)

def removeActorsAt(*args):
   isGameGridValid()
   return do(g.removeActorsAt, args)

def removeAllActors():
   isGameGridValid()
   return g.removeAllActors()

def removeKeyListener(listener):
   isGameGridValid()
   g.removeKeyListener(listener)

def removeKeyRepeatListener(listener):
   isGameGridValid()
   g.removeKeyRepeatListener(listener)

def removeMouseListener(listener):
   isGameGridValid()
   return g.removeMouseListener(listener)

def reset():
   isGameGridValid()
   g.reset()

def reverseSceneOrder(actors):
   isGameGridValid()
   return g.reverseSceneOrder(actors)

def setActEnabled(enable):
   isGameGridValid()
   g.setActEnabled(enable)

def setActOrder(classes):
   isGameGridValid()
   g.setActOrder(classes)

def setActorOnBottom(actor):
   isGameGridValid()
   g.setActorOnBottom(actor)

def setActorOnTop(actor):
   isGameGridValid()
   g.setActorOnTop(actor)

def setBgColor(*args):
   isGameGridValid()
   do(g.setBgColor, args)

def setBgImagePath(bgImagePath):
   isGameGridValid()
   g.setBgImagePath(bgImagePath)

def setBgImagePos(point):
   isGameGridValid()
   g.setBgImagePos(point)

def setBgImagePosX(x):
   isGameGridValid()
   g.setBgImagePosX(x)

def setBgImagePosY(y):
   isGameGridValid()
   g.setBgImagePosY(y)

def setCellSize(cellSize):
   isGameGridValid()
   g.setCellSize(cellSize)

def setClosingMode(closingMode):
   GameGrid.setClosingMode(closingMode)

def setDoubleClickDelay(delay):
   isGameGridValid()
   g.setDoubleClickDelay(delay)
   
def setGridColor(color):
   isGameGridValid()
   g.setGridColor(color)

def setKeyRepeatPeriod(keyRepeatPeriod):
   isGameGridValid()
   return g.setKeyRepeatPeriod(keyRepeatPeriod)

def setMouseEnabled(enabled):
   isGameGridValid()
   g.setMouseEnabled(enabled)

def setNbHorzCells(nbHorzCells):
   isGameGridValid()
   g.setNbHorzCells(nbHorzCells)

def setNbVertCells(nbVertCells):
   isGameGridValid()
   g.setNbVertCells(nbVertCells)

def setPaintOrder(classes):
   isGameGridValid()
   g.setPaintOrder(classes)

def setPosition(ulx, uly):
   isGameGridValid()
   g.setPosition(ulx, uly)

def setSceneOrder(actors):
   isGameGridValid()
   g.setSceneOrder(actors)

def setSimulationPeriod(millisec):
   isGameGridValid()
   g.setSimulationPeriod(millisec)

def setStatusText(*args):
   isGameGridValid()
   do(g.setStatusText, args)

def setTitle(text):
   isGameGridValid()
   g.setTitle(text)

def shiftSceneOrder(actors, forward):
   isGameGridValid()
   return g.shiftSceneOrder(actors, forward)
   
def show():
   isGameGridValid()
   g.show()

def showStatusBar(show):
   isGameGridValid()
   g.showStatusBar(show)

def stopGameThread():
   isGameGridValid()
   g.stopGameThread()
   
def toLocation(*args):
   isGameGridValid()
   return do(g.toLocation, args)

def toLocationInGrid(*args):
   isGameGridValid()
   return do(g.toLocationInGrid, args)

def toPoint(location):
   isGameGridValid()
   return g.toPoint(location)

# ----------------- End of GameGrid methods -------------------

def getRandomX11Color():
   return X11Color.getRandomColorStr()

def putSleep():
   Monitor.putSleep()

def wakeUp():
   Monitor.wakeUp()

def toList(arrayList):
   if not isinstance(arrayList, ArrayList):
      return None
   mylist = []
   for i in range(arrayList.size()):
      mylist.append(arrayList.get(i))
   return mylist


def toArrayList(mylist):
   if not isinstance(mylist, list):
      return None
   arraylist = ArrayList()
   for i in range(len(mylist)):
      arraylist.add(mylist[i])
   return arraylist


def askColor(title, defaultColor):
   if isinstance(defaultColor, str):
      return JColorChooser.showDialog(None, title, X11Color.toColor(defaultColor))
   else:
      return JColorChooser.showDialog(None, title, defaultColor)

def getDividingPoint(pt1, pt2, ratio):
   return GGLine.getDividingPoint(pt1, pt2, ratio)

def getMarkerPoint(pt1, pt2, d):
    r = math.sqrt((pt1.x - pt2.x)* (pt1.x - pt2.x) + (pt1.y - pt2.y)* (pt1.y - pt2.y))
    return getDividingPoint(pt1, pt2, d / r)

# Must restart TigerJython to take effect 
def setNbRotSprites(n):
   isGameGridValid()
   GameGrid.nbRotSprites = n
   