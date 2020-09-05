# gturtle.py
# AP, TK
# Version 2.75, July 18, 2018

from ch.aplu.turtle import LineRenderer, Main, MouseHitListener, MouseHitXListener, MyProperties, NoMain,\
Pen, Playground, SharedConstants, StatusBar, TPrintable, Turtle, DotTurtle, TurtleArea, \
TurtleContainer, TurtleFactory, TurtleKeyAdapter, TurtlePane, TurtlePaneBeanInfo, \
TurtleProgram, TurtleRenderer, TurtleRunner, X11Color, BitmapTurtleFactory, Options, GBitmap, TurtleHitListener

import ch.aplu.turtle.TurtleFrame as JavaTurtleFrame
from ch.aplu.packagedoc import HtmlPane
from ch.aplu.simulationbar import *
import sys
import time
from ch.aplu.util import MessageDialog, MessagePane, QuitPane, ModelessOptionPane, Monitor
from java.awt import Color, Dimension, Cursor, Point, Font
import java.awt.geom.Point2D.Double
from javax.swing import JColorChooser, SwingUtilities
from java.awt import BasicStroke
from sys import exit
import thread, threading
from enum import enum
from java.awt.event import KeyEvent
from java.awt.event import KeyListener
from entrydialog import *
from gvideo import *

__framePosition = None
__moveToCenter = False

class TurtleFrame(JavaTurtleFrame):
   def __init__(self, *args, **kwargs):
      if len(args) == 0:
         JavaTurtleFrame.__init__(self, **kwargs)
      elif type(args[0]) == str:
         JavaTurtleFrame.__init__(self, **kwargs)
         self.setTitle(args[0])
      else:
         raise ValueError("Illegal parameter(s) in TurtleFrame()")

   def enableRepaint(self, enable):
      turtles = self.getPlayground().getTurtles()
      for turtle in turtles:
         turtle.enableRepaint(enable)

   def clear(self, *args):
      if len(args) == 0:
         self.getPlayground().clear()
      else:
         self.getPlayground().clear(args[0])

   def clean(self, *args):
      if len(args) == 0:
         self.getPlayground().clean()
      else:
         self.getPlayground().clean(args[0])

   def delay(self, t):
      time.sleep(t / 1000)


class KeyThread(threading.Thread):
   def __init__(self, keyCode):
      threading.Thread.__init__(self)
      self.keyCode = keyCode

   def run(self):
      try:
         __keyHit(self.keyCode)
      except:
         pass

class MyKeyListener(KeyListener):
   def keyPressed(self, e):
      global __event
      __event = e
      t = KeyThread(e.getKeyCode())
      t.start()

   def keyReleased(self, e):
      pass

   def keyTyped(self, e):
      pass


class MyKeyListenerX(KeyListener):
   def keyPressed(self, e):
      global __keyCode, __event
      __event = e
      __keyCode = e.getKeyCode()
      __keyEvent.set()

   def keyReleased(self, e):
      pass

   def keyTyped(self, e):
      pass

def __keyHandler():
    while __isKeyHandlerRunning:
#        print "key handler sleeping"
        __keyEvent.wait()
#        print "key hander woke up"
        try:
           __keyHitX(__keyCode)
        except:
           pass
        __keyEvent.clear()


# -------------- Mouse and keyboard callbacks ---------------
__turtleHit = None
__mouseHit = None
__mouseHitX = None
__mouseClicked = None
__mouseEntered = None
__mouseExited = None
__mousePressed = None
__mouseReleased = None
__mouseDragged = None
__mouseMoved = None
__mouseSingleClicked = None
__mouseDoubleClicked = None

__keyHit = None
__keyHitX = None
__keyPressed = None
__closeClicked = None

__event = None

def __callTurtleHit(t, x, y):
   global __event
   if __turtleHit != None:
#      __event = "LEFT"
      __turtleHit(x, y)

def __callMouseHit(x, y):
   global __event
   if __mouseHit != None:
#      __event = "LEFT"
      __mouseHit(x, y)

def __callMouseHitX(x, y):
   global __event
   if __mouseHitX != None:
#      __event = "LEFT"
      __mouseHitX(x, y)

def __callMouseClicked(e):
   global __event
   if __mouseClicked != None:
      __event = e
      if __mouseClicked.func_code.co_argcount == 1:
            __mouseClicked(e)
      if __mouseClicked.func_code.co_argcount == 2:
          x = toTurtleX(e.getX())
          y = toTurtleY(e.getY())
          __mouseClicked(x, y)

def __callMouseEntered(e):
   global __event
   if __mouseEntered != None:
      __event = e
      if __mouseEntered.func_code.co_argcount == 1:
            __mouseEntered(e)
      if __mouseEntered.func_code.co_argcount == 2:
          x = toTurtleX(e.getX())
          y = toTurtleY(e.getY())
          __mouseEntered(x, y)

def __callMouseExited(e):
   global __event
   if __mouseExited != None:
      __event = e
      if __mouseExited.func_code.co_argcount == 1:
            __mouseExited(e)
      if __mouseExited.func_code.co_argcount == 2:
          x = toTurtleX(e.getX())
          y = toTurtleY(e.getY())
          __mouseExited(x, y)

def __callMousePressed(e):
   global __event
   if __mousePressed != None:
      __event = e
      if __mousePressed.func_code.co_argcount == 1:
            __mousePressed(e)
      if __mousePressed.func_code.co_argcount == 2:
          x = toTurtleX(e.getX())
          y = toTurtleY(e.getY())
          __mousePressed(x, y)

def __callMouseReleased(e):
   global __event
   if __mouseReleased != None:
      __event = e
      if __mouseReleased.func_code.co_argcount == 1:
            __mouseReleased(e)
      if __mouseReleased.func_code.co_argcount == 2:
          x = toTurtleX(e.getX())
          y = toTurtleY(e.getY())
          __mouseReleased(x, y)

def __callMouseDragged(e):
   global __event
   if __mouseDragged != None:
      __event = e
      if __mouseDragged.func_code.co_argcount == 1:
            __mouseDragged(e)
      if __mouseDragged.func_code.co_argcount == 2:
          x = toTurtleX(e.getX())
          y = toTurtleY(e.getY())
          __mouseDragged(x, y)

def __callMouseMoved(e):
   global __event
   if __mouseMoved != None:
      __event = e
      if __mouseMoved.func_code.co_argcount == 1:
            __mouseMoved(e)
      if __mouseMoved.func_code.co_argcount == 2:
          x = toTurtleX(e.getX())
          y = toTurtleY(e.getY())
          __mouseMoved(x, y)

def __callMouseSingleClicked(e):
   global __event
   if __mouseSingleClicked != None:
      __event = e
      if __mouseSingleClicked.func_code.co_argcount == 1:
            __mouseSingleClicked(e)
      if __mouseSingleClicked.func_code.co_argcount == 2:
          x = toTurtleX(e.getX())
          y = toTurtleY(e.getY())
          __mouseSingleClicked(x, y)

def __callMouseDoubleClicked(e):
   global __event
   if __mouseDoubleClicked != None:
      __event = e
      if __mouseDoubleClicked.func_code.co_argcount == 1:
            __mouseDoubleClicked(e)
      if __mouseDoubleClicked.func_code.co_argcount == 2:
          x = toTurtleX(e.getX())
          y = toTurtleY(e.getY())
          __mouseDoubleClicked(x, y)

def __callKeyHit(e):
   global __event
   if __keyHit != None:
      __event = e
      t = KeyThread(e.getKeyCode())
      t.start()

def __callKeyHitX(e):
   global __keyCode
   global __event
   if __keyHitX != None:
      __event = e
      __keyCode = e.getKeyCode()
      __keyEvent.set()

def __callKeyPressed(e):
   global __event
   if __keyPressed != None:
      __event = e
      __keyPressed(e.getKeyCode())

def __callCloseClicked():
   if __closeClicked != None:
      __closeClicked()
   else: 
      dispose()
      wakeUp()

def onTurtleHit(f):
   global __turtleHit
   __turtleHit = f
   return f

def onMouseHit(f):
   global __mouseHit
   __mouseHit = f
   return f

def onMouseHitX(f):
   global __mouseHitX
   __mouseHitX = f
   return f

def onMouseClicked(f):
   global __mouseClicked
   __mouseClicked = f
   return f

def onMouseEntered(f):
   global __mouseEntered
   __mouseEntered = f
   return f

def onMouseExited(f):
   global __mouseExited
   __mouseExited = f
   return f

def onMousePressed(f):
   global __mousePressed
   __mousePressed = f
   return f

def onMouseReleased(f):
   global __mouseReleased
   __mouseReleased = f
   return f

def onMouseDragged(f):
   global __mouseDragged
   __mouseDragged = f
   return f

def onMouseMoved(f):
   global __mouseMoved
   __mouseMoved = f
   return f

def onMouseSingleClicked(f):
   global __mouseSingleClicked
   __mouseSingleClicked = f
   return f

def onMouseDoubleClicked(f):
   global __mouseDoubleClicked
   __mouseDoubleClicked = f
   return f

def onKeyHit(f):
   isPlayground = True
   try:
      __g
   except NameError:
      isPlayground = False

   if isPlayground:  # makeTurtle already called
      addKeyListener(MyKeyListener())

   global __keyHit
   __keyHit = f
   return f


def onKeyHitX(f):
   isPlayground = True
   try:
      __g
   except NameError:
      isPlayground = False

   if isPlayground:  # makeTurtle already called
      __startKeyThread()
      addKeyListener(MyKeyListenerX())

   global __keyHitX
   __keyHitX = f
   return f

def onKeyPressed(f):
   global __keyPressed
   __keyPressed = f
   return f

def onCloseClicked(f):
   global __closeClicked
   __closeClicked = f
   return f


# -------------- End of callbacks ---------------


class MyPrintable(TPrintable):
   def __init__(self, fun):
      self.fun = fun
   def draw(self):
      self.fun()

class WindowNotInitialized(Exception): pass
class FunctionDeprecated(Exception): pass
class TurtleOutOfWindow(Exception): pass

__sb = None

def isPlaygroundValid():
   try:
      __g
   except NameError:
      raise WindowNotInitialized("Use \"makeTurtle()\" to create the turtle's playground before calling turtle methods.")

def do(fun, args):
   _args = [None] * len(args)
   for i in range(len(args)):
      if type(args[i]) == list:
         _args[i] = java.awt.geom.Point2D.Double(args[i][0], args[i][1])
      elif type(args[i]) == tuple:
         _args[i] = java.awt.geom.Point2D.Double(args[i][0], args[i][1])
      elif type(args[i]) == complex:
         _args[i] = java.awt.geom.Point2D.Double(args[i].real, args[i].imag)
      else:
         _args[i] =  args[i]

   y = None
   if len(args) == 0:
      y = fun()
   elif len(args) == 1:
      y = fun(_args[0])
   elif len(args) == 2:
      y = fun(_args[0], _args[1])
   elif len(args) == 3:
      y = fun(_args[0], _args[1], _args[2])
   elif len(args) == 4:
      y = fun(_args[0], _args[1], _args[2], _args[3])
   elif len(args) == 5:
      y = fun(_args[0], _args[1], _args[2], _args[3], _args[4])
   elif len(args) == 6:
      y = fun(_args[0], _args[1], _args[2], _args[3], _args[4], _args[5])
   else:
      raise ValueError("Illegal number of arguments")
   if y != None:
      return y

hideFromDebugView()

def makeTurtle(*args, **kwargs):
   global _turtleFrame, __g
   global __turtleHit, __mouseHit, __mouseHitX, __mouseClicked, __mouseEntered
   global __mouseExited, __mousePressed, __mouseReleased
   global __mouseDragged, __mouseMoved, __keyHit, __keyHitX 
   global __mouseSingleClicked, __mouseDoubleClicked, __keyPressed, __closeClicked
   __loc =  TurtleFrame.getLastScreenLocation()
   lastFrame = TurtleFrame.getLastFrame()
   if lastFrame != None:
      lastFrame.dispose()
   _turtleFrame = TurtleFrame()
   argDict = {"turtleHit"     : __callTurtleHit,
              "mouseHit"      : __callMouseHit, 
              "mouseHitX"     : __callMouseHitX,
              "mouseClicked"  : __callMouseClicked,
              "mouseEntered"  : __callMouseEntered,
              "mouseExited"   : __callMouseExited,
              "mousePressed"  : __callMousePressed,
              "mouseReleased" : __callMouseReleased,
              "mouseDragged"  : __callMouseDragged,
              "mouseMoved"    : __callMouseMoved,
              "notifyClick"   : __callMouseSingleClicked,
              "notifyDoubleClick"   : __callMouseDoubleClicked,
              "keyPressed"    : __callKeyPressed,
              "notifyExit"    : __callCloseClicked}
   for key in kwargs:
      if key == "turtleHit":
         __turtleHit = kwargs[key]
      elif key == "mouseHit":
         __mouseHit = kwargs[key]
      elif key == "mouseHitX":
         __mouseHitX = kwargs[key]
      elif key == "mouseClicked":
         __mouseClicked = kwargs[key]
      elif key == "mouseEntered":
         __mouseEntered = kwargs[key]
      elif key == "mouseExited":
         __mouseExited = kwargs[key]
      elif key == "mousePressed":
         __mousePressed = kwargs[key]
      elif key == "mouseReleased":
         __mouseReleased = kwargs[key]
      elif key == "mouseDragged":
         __mouseDragged = kwargs[key]
      elif key == "mouseMoved":
         __mouseMoved = kwargs[key]
      elif key == "mouseSingleClicked":
         __mouseSingleClicked = kwargs[key]
      elif key == "mouseDoubleClicked":
         __mouseDoubleClicked = kwargs[key]
      elif key == "keyHit":
         __keyHit = kwargs[key]
         argDict["keyPressed"] = __callKeyHit
      elif key == "keyHitX":
         __keyHitX = kwargs[key]
         argDict["keyPressed"] = __callKeyHitX
      elif key == "keyPressed":
         __keyPressed = kwargs[key]
      elif key == "closeClicked":
         __closeClicked = kwargs[key]

   if __keyHit != None:
      argDict["keyPressed"] = __callKeyHit
   if __keyHitX != None:
      argDict["keyPressed"] = __callKeyHitX
      __startKeyThread()

   if len(args) == 0: 
      __g = Turtle(_turtleFrame, **argDict)
   elif len(args) == 1:
      __g = Turtle(_turtleFrame, args[0], **argDict)
   elif len(args) == 2:
      __g = DotTurtle(_turtleFrame, args[0], args[1], **argDict)
   else:
      raise ValueError("Illegal number of arguments")
   if __loc != None:
      _turtleFrame.setScreenLocation(__loc)

   if getTigerJythonFlag("gturtle.hideOnStart"):
      __g.ht()

   if __moveToCenter: # deferred
      setFramePositionCenter()
   elif __framePosition != None: # deferred
      setFramePosition(__framePosition[0], __framePosition[1])


   return __g
   
def createTurtle(*args):
   raise FunctionDeprecated("'createTurtle' is deprecated. Use 'makeTurtle' instead.")

def _getX():
   isPlaygroundValid()
   return round(__g._getX(), 4)
    
def _getY():
   isPlaygroundValid()  
   return round(__g._getY(), 4)

def addMouseListener(listener):
   isPlaygroundValid()  
   _turtleFrame.addMouseListener(listener)

def addTurtleHitListener(listener):
   isPlaygroundValid()  
   __g.addTurtleHitListener(listener)

def addMouseHitListener(listener):
   isPlaygroundValid()  
   _turtleFrame.addMouseHitListener(listener)

def addMouseHitXListener(listener):
   isPlaygroundValid()  
   _turtleFrame.addMouseHitXListener(listener)

def addMouseMotionListener(listener):
   isPlaygroundValid()  
   _turtleFrame.addMouseMotionListener(listener)

def addMouseWheelListener(listener):
   isPlaygroundValid()  
   _turtleFrame.addMouseWheelListener(listener)

def addKeyListener(listener):
   isPlaygroundValid()  
   _turtleFrame.addKeyListener(listener)

def addStatusBar(n):
   isPlaygroundValid()
   __g.addStatusBar(n)
   
def back(distance):
   isPlaygroundValid()
   __g.back(distance)
   return __g

def backward(distance):
    return back(distance)

def beep():
   isPlaygroundValid()
   __g.beep()
   return __g

def bk(distance):
   isPlaygroundValid()
   __g.bk(distance)
   return __g

def clean(*args):
   isPlaygroundValid()
   do(__g.clean, args)
   return __g

def clear(*args):
   isPlaygroundValid()
   do(__g.clear, args)
   return __g

def clip():
   isPlaygroundValid()
   __g.clip()
   return __g

def clone():
   isPlaygroundValid()
   return __g.clone()

def cs():
   isPlaygroundValid()
   return __g.cs()

def delay(t):
# not sleep, because of name confusion with Python time.sleep()
   time.sleep(t / 1000)

def direction(*args):
   isPlaygroundValid()
   return do(__g.direction, args)

def dispose():
   isPlaygroundValid()
   __g.dispose()

def distance(*args):
   isPlaygroundValid()
   return round(do(__g.distance, args), 4)

def dot(diameter):
   isPlaygroundValid()
   __g.dot(diameter)
   return __g

def drawBkImage(imagePath):
   isPlaygroundValid()
   __g.drawBkImage(imagePath)
   return __g

def enableRepaint(b):
   isPlaygroundValid()
   __g.enableRepaint(b)

def fd(distance):
   isPlaygroundValid()
   __g.fd(distance)
   return __g

def fill(*args):
   isPlaygroundValid()
   if len(args) == 1 and not (type(args[0]) is list or type(args[0]) is tuple or type(args[0]) is complex):
      raise ValueError("Illegal parameter in fill(). (Use setFillColor(c) to set the fill color.)")
   do(__g.fill, args)
   return __g

def fillOff():
   isPlaygroundValid()
   __g.fillOff()
   return __g

def fillPath():
   isPlaygroundValid()
   __g.fillPath()
   return __g

def fillToHorizontal(y):
   isPlaygroundValid()
   __g.fillToHorizontal(y)
   return __g

def fillToPoint(*args):
   isPlaygroundValid()
   return do(__g.fillToPoint, args)

def fillToVertical(x):
   isPlaygroundValid()
   __g.fillToVertical(x)
   return __g

def forward(distance):
   isPlaygroundValid()
   __g.forward(distance)
   return __g

def getAvailableFontFamilies():
   isPlaygroundValid()
   return __g.getAvailableFontFamilies()

def getColor():
   isPlaygroundValid()
   return __g.getColor()

def getColorStr():
   isPlaygroundValid()
   return __g.getColorStr()

def getEnvironment():
   isPlaygroundValid()
   return __g.getEnvironment()

def getFillColor():
   isPlaygroundValid()
   return __g.getFillColor()

def getFillColorStr():
   isPlaygroundValid()
   return __g.getFillColorStr()

def getFont():
   isPlaygroundValid()
   return __g.getFont()

def getFrame():
   isPlaygroundValid()
   return __g.getFrame()

def getKey():
   isPlaygroundValid()
   key = _turtleFrame.getKeyInt()
   if key > 255:
      return ""
   else:    
      return str(chr(key))

def getKeyCode():
   isPlaygroundValid()
   return _turtleFrame.getKeyCode()

def getKeyCodeWait(*args):
   isPlaygroundValid()
   if len(args) == 0:
      return _turtleFrame.getKeyCodeWait()
   if len(args) == 1:
      if args[0]:
          code = _turtleFrame.getKeyCodeWait()
          if isDisposed():
             exit()
          return code
      else:
          return _turtleFrame.getKeyCodeWait()

def _getKeyWait():
   key = _turtleFrame.getKeyWaitInt()
   if key > 255:
      return ""
   else:    
      return str(chr(key))

def getKeyWait(*args):
   isPlaygroundValid()
   if len(args) == 0:
      return _getKeyWait()
   if len(args) == 1:
      if args[0]:
          key = _getKeyWait()
          if isDisposed():
             exit()
          return key
      else:
          return _getKeyWait()

def getModifiers():
   isPlaygroundValid()
   return _turtleFrame.getModifiers()

def getModifiersText():
   isPlaygroundValid()
   return _turtleFrame.getModifiersText()

def getPen():
   isPlaygroundValid()
   return __g.getPen()

def getPenColor():
   isPlaygroundValid()
   return __g.getPenColor()

def getPenColorStr():
   isPlaygroundValid()
   return __g.getPenColorStr()

def getPenWidth():
   isPlaygroundValid()
   return __g.getPenWidth()

def getPixelColor():
   isPlaygroundValid()
   c = __g.getPixelColor()
   return c

def getPixelColorAhead(distance):
   isPlaygroundValid()
   c = __g.getPixelColorAhead(distance)
   return c

def getPixelColorStr():
   isPlaygroundValid()
   c = __g.getPixelColorStr()
   if c == None:
      raise TurtleOutOfWindow("Turtle Out Of Window")
   return c

def getPixelColorAheadStr(distance):
   isPlaygroundValid()
   c = __g.getPixelColorAheadStr(distance)
   if c == None:
      raise TurtleOutOfWindow("Ahead Position Out Of Window")
   return c

def getPlayground():
   isPlaygroundValid()
   return __g.getPlayground()

def getPos():
   isPlaygroundValid()
   return [getX(), getY()]

def getPropLocation():
   isPlaygroundValid()
   return __g.getPropLocation()

def getSpeed():
   isPlaygroundValid()
   return __g.getSpeed()

def getTextHeight():
   isPlaygroundValid()
   return __g.getTextHeight()

def getTextAscent():
   isPlaygroundValid()
   return __g.getTextAscent()

def getTextDescent():
   isPlaygroundValid()
   return __g.getTextDescent()

def getTextWidth(text):
   isPlaygroundValid()
   return __g.getTextWidth(text)
   
def getTurtle():
   isPlaygroundValid()
   return __g

def getTurtleFactory():
   isPlaygroundValid()
   return __g.getTurtleFactory()

def getX():
   isPlaygroundValid()
   return round(__g.getX(), 4)

def getY():
   isPlaygroundValid()
   return round(__g.getY(), 4)

def heading(*args):
   isPlaygroundValid()
   return do(__g.heading, args)

def hideTurtle():
   isPlaygroundValid()
   __g.hideTurtle()
   return __g

def home():
   isPlaygroundValid()
   __g.home()
   return __g

def ht():
   isPlaygroundValid()
   __g.ht()
   return __g

def isClip():
   isPlaygroundValid()
   return __g.isClip()

def isDisposed():
   isPlaygroundValid()
   return _turtleFrame.isDisposed()

def isHidden():
   isPlaygroundValid()
   return __g.isHidden()

def isMirror():
   isPlaygroundValid()
   return __g.isMirror()

def isPenUp():
   isPlaygroundValid()
   return __g.isPenUp()

def isWrap():
   isPlaygroundValid()
   return __g.isWrap()

def label(*args, **kwargs):
   isPlaygroundValid()
   text = " ".join([str(arg) for arg in args])
   adj = 'l'
   if 'adjust' in kwargs.keys():
      adj = kwargs['adjust']
   __g.label(text, adj)
   return __g

def left(degrees):
   isPlaygroundValid()
   __g.left(degrees)
   return __g

def leftArc(radius, angle):
   isPlaygroundValid()
   __g.leftArc(radius, angle)
   return __g

def leftCircle(radius):
   isPlaygroundValid()
   __g.leftCircle(radius)
   return __g

def lt(degrees):
   isPlaygroundValid()
   __g.lt(degrees)
   return __g

def moveTo(*args):
   isPlaygroundValid()
   # Changed by TK
   do (__g.moveTo, args)
   return __g
   
lineTo = moveTo

def openDot(diameter):
   isPlaygroundValid()
   __g.openDot(diameter)
   return __g

def pd():
   isPlaygroundValid()
   __g.pd()
   return __g

def pe():
   isPlaygroundValid()
   __g.pe()
   return __g

def penDown():
   isPlaygroundValid()
   __g.penDown()
   return __g

def penErase():
   isPlaygroundValid()
   __g.penErase()
   return __g

def penUp():
   isPlaygroundValid()
   __g.penUp()
   return __g

def penWidth(*args):
   isPlaygroundValid()
   return do(__g.penWidth, args)

def printScreen(*args):
   isPlaygroundValid()
   return do(__g.print, args)

def pu():
   isPlaygroundValid()
   __g.pu()
   return __g

def pushState():
   isPlaygroundValid()
   __g.pushState()
   return __g

def popState():
   isPlaygroundValid()
   __g.popState()
   return __g

def repaint():
   isPlaygroundValid()
   __g.repaint()

def right(degrees):
   isPlaygroundValid()
   __g.right(degrees)
   return __g

def rightArc(radius, angle):
   isPlaygroundValid()
   __g.rightArc(radius, angle)
   return __g

def rightCircle(radius):
   isPlaygroundValid()
   __g.rightCircle(radius)
   return __g

def rt(degrees):
   isPlaygroundValid()
   __g.rt(degrees)
   return __g

def savePlayground(*args):
   isPlaygroundValid()
   if len(args) == 0:
       __g.savePlayground()
   elif len(args) == 2:
       return __g.savePlayground(args[0], args[1])
   else:
       raise ValueError("Illegal number of arguments")

def setColor(*args):
   isPlaygroundValid()
   __g.setColor(_toColor(*args))
   return __g

def setFillColor(*args):
   isPlaygroundValid()
   __g.setFillColor(_toColor(*args))
   return __g

def setFont(*args):
   isPlaygroundValid()
   if len(args) == 1:
      if type(*args) == java.awt.Font:
          __g.setFont(*args)
      else:
         font = __g.getFont()
         __g.setFont(args[0], font.getStyle(), font.getSize())    
   elif len(args) == 2:
      font = __g.getFont()
      __g.setFont(args[0], args[1], font.getSize())    
   elif len(args) == 3:
      __g.setFont(args[0], args[1], args[2])   
   return __g

def setFontSize(size):
   isPlaygroundValid()
   __g.setFontSize(size)
   return __g

def setH(degrees):
   isPlaygroundValid()
   __g.setH(degrees)
   return __g

def setHeading(degrees):
   isPlaygroundValid()
   __g.setHeading(degrees)
   return __g

def setPenWidth(*args):
   isPlaygroundValid()
   return do(__g.setPenWidth, args)

def setRandomHeading():
   isPlaygroundValid()
   __g.setRandomHeading()
   return __g

def setLineWidth(lineWidth):
   isPlaygroundValid()
   __g.setLineWidth(lineWidth)
   return __g

def setMirror(enable):
   isPlaygroundValid()
   __g.setMirror(enable)
   return __g

def setPenColor(*args):
   isPlaygroundValid()
   __g.setPenColor(_toColor(*args))
   return __g

def setPos(*args):
   isPlaygroundValid()
   do (__g.setPos, args)
   return __g

def setRandomPos(w, h):
   isPlaygroundValid()
   __g.setRandomPos(w, h)
   return __g

def setScreenPos(*args):
   isPlaygroundValid()
   return do(__g.setScreenPos, args)

def setScreenX(x):
   isPlaygroundValid()
   __g.setScreenX(x)
   return __g

def setScreenY(y):
   isPlaygroundValid()
   __g.setScreenY(y)
   return __g

def setStatusText(*args):
   isPlaygroundValid()
   do(__g.setStatusText, args)

def setX(x):
   isPlaygroundValid()
   __g.setX(x)
   return __g

def setY(y):
   isPlaygroundValid()
   __g.setY(y)
   return __g

def showStatusBar(show):
   isPlaygroundValid()
   __g.showStatusBar(show)

def setEndCap(style):
   isPlaygroundValid()
   if style != None:
      if lower(style).strip() in ['square', u'square']:
         __g.getPen().setEndCap(BasicStroke.CAP_SQUARE)
      elif lower(style).strip() in ['round', u'round']:
         __g.getPen().setEndCap(BasicStroke.CAP_ROUND)
      elif lower(style).strip() in ['clip', u'clip']:
         __g.getPen().setEndCap(BasicStroke.CAP_BUTT)

def setTitle(text):
   isPlaygroundValid()
   __g.setTitle(text)

def showTurtle():
   isPlaygroundValid()
   __g.showTurtle()
   return __g

def sound(*args):
   isPlaygroundValid()
   return do(__g.sound, args)

def speed(speed):
   isPlaygroundValid()
   __g.speed(speed)
   return __g

def spray(*args):
   isPlaygroundValid()
   return do(__g.spray, args)

def st():
   isPlaygroundValid()
   __g.st()
   return __g

def stampTurtle(*args):
   isPlaygroundValid()
   return do(__g.stampTurtle, args)

def startPath():
   isPlaygroundValid()
   __g.startPath()
   return __g

def toBottom():
   isPlaygroundValid()
   __g.toBottom()
   return __g

def toTop():
   isPlaygroundValid()
   __g.toTop()
   return __g

def toTurtlePos(*args):
   isPlaygroundValid()
   pt = do(__g.toTurtlePos, args)
   return [pt.x, pt.y]

def toTurtleX(x):
   isPlaygroundValid()
   return __g.toTurtleX(x)

def toTurtleY(y):
   isPlaygroundValid()
   return __g.toTurtleY(y)

def towards(*args):
   isPlaygroundValid()
   return do(__g.towards, args)

def version():
   isPlaygroundValid()
   return __g.version()

def viewingMoveTo(x, y):
   isPlaygroundValid()
   return __g.viewingMoveTo(x, y)

def viewingSetPos(x, y):
   isPlaygroundValid()
   return __g.viewingSetPos(x, y)

def wrap():
   isPlaygroundValid()
   __g.wrap()
   return __g

def toString():
   isPlaygroundValid()
   return __g.toString()

def setCursor(type):
   isPlaygroundValid()
   __g.setCursor(type)
   return __g

def setCustomCursor(*args):
   isPlaygroundValid()
   if len(args) == 1:
      dim = __g.setCustomCursor(args[0])
   elif len(args) == 2:
      dim = __g.setCustomCursor(args[0], Point(args[1][0], args[1][1]))
      if dim != None:
         return [dim.height, dim.width]
      else:   
         return None
   else:
      raise ValueError("Illegal number of arguments")




# ------ Non Turtle class method calls ---------------------------------


# Clears the turtle's playground. All traces and text are erased, 
# but the turtles remain (visible) at their positions.
# Moves the global turtle back "home", i.e. to coordinates (0, 0), 
# heading north. Other turtle properties are not modified.
def clearScreen():
   clean()
   return home()

def cs():
   return clearScreen()

def getRandomX11Color():
   return X11Color.getRandomColorStr()

def askColor(title, defaultColor):
   if isinstance(defaultColor, str):
      return JColorChooser.showDialog(None, title, X11Color.toColor(defaultColor))
   else:
      return JColorChooser.showDialog(None, title, defaultColor)

def printerPlot(*args):
   isPlaygroundValid()
   if len(args) == 1:
      mp = MyPrintable(args[0])
      __g.print(mp, 0.81)  # scale adapted for 600x600 playground
   elif len(args) == 2:
      mp = MyPrintable(args[0])
      __g.print(mp, 0.81 * args[1])
   else:
      raise ValueError("Illegal number of arguments")

def drawImage(img, *args):
   isPlaygroundValid()
   if len(args) == 0:
      __g.drawImage(img)
   else:
      if type(args[0]) == bool and type(args[1]) == bool:
         __g.drawImage(img, args[0], args[1]) # mirror image
      else:
         __g.getPlayground().drawImage(img, args[0], args[1])
   
def getPlaygroundWidth():
   try:
      __g
   except NameError:   # makeTurtle() not yet called
      optionSize = getTigerJythonFlag("gturtle.playground.size")
      if optionSize == None: # Standard
         dim = TurtleFrame.getPlaygroundSize()
         if dim != None:
            return dim.width
         else:
            return -1
      return optionSize[0]
   return __g.getPlayground().getSize().width

def getPlaygroundHeight():
   try:
      __g
   except NameError:  # makeTurtle() not yet called
      optionSize = getTigerJythonFlag("gturtle.playground.size")
      if optionSize == None: # Standard
         dim = TurtleFrame.getPlaygroundSize()
         if dim != None:
            return dim.height
         else:
            return -1
      return optionSize[1]
   return __g.getPlayground().getSize().height

def getPlaygroundBufferHeight():
   isPlaygroundValid()
   return __g.getPlayground().getBufferSize().height

def getPlaygroundBufferWidth():
   isPlaygroundValid()
   return __g.getPlayground().getBufferSize().width

def isLeftMouseButton(*args):
   if len(args) == 1:
      return SwingUtilities.isLeftMouseButton(args[0])
   if __event == None and _turtleFrame.getMouseHitButton() == 1:
      return True
   if __event != None and SwingUtilities.isLeftMouseButton(__event):
      return True
   return False

def isRightMouseButton(*args):
   if len(args) == 1:
      return SwingUtilities.isRightMouseButton(args[0])
   if __event == None and  _turtleFrame.getMouseHitButton() == 3:
      return True
   if __event != None and SwingUtilities.isRightMouseButton(__event):
      return True
   return False

def getKeyModifiers():
   if __event == None:
      return None
   return __event.getModifiersEx()

def __stopKeyThread():
    global __keyCode, __isKeyHandlerRunning
    __keyCode = KeyEvent.VK_UNDEFINED
    __isKeyHandlerRunning = False
    __keyEvent.set()

def __startKeyThread():
    global __isKeyHandlerRunning, __keyEvent 
    __isKeyHandlerRunning = True
    __keyEvent = threading.Event()
    t = threading.Thread(name = "key_handler", 
                     target = __keyHandler,
                     args  = ())
    t.start()

def getImage(path):
    isPlaygroundValid()
    rc = __g.getImage(path)
    if rc == None:
        raise ValueError("Image " + str(path) + " not found.")
    return rc 

def putSleep():
    Monitor.putSleep()

def wakeUp():
    Monitor.wakeUp()

def polyCircumference(n, radius):
    from math import pi, sin
    if n > 2:
        return 2 * radius * n * sin(pi / n)
    else:
        return 0
            
def _toColor(*args):
    if len(args) == 1:
        if type(args[0]) in [str, unicode, java.awt.Color]:
            return args[0]
        elif type(args[0]) is int: # logo colors
            return makeColor("vga", -args[0])
    elif len(args) == 3: # RGB
        return makeColor(args[0]/255, args[1]/255, args[2]/255)
    else:
        raise ValueError("Illegal number of arguments")

def showSimulationBar(*args, **kwargs):
    global __sb
    if __sb != None:
        hideSimulationBar()
    if len(args) == 0:
        __sb = SimulationBar(**kwargs)        
    elif len(args) == 1:
        __sb = SimulationBar(args[0], **kwargs)        
    elif len(args) == 2:
        __sb =  SimulationBar(args[0], args[1], **kwargs)        
    elif len(args) == 3:
        __sb = SimulationBar(args[0], args[1], args[2], **kwargs)        
    else:
        raise ValueError("Illegal number of arguments")
    return __sb

def hideSimulationBar():
    global __sb
    if __sb != None:
        __sb.dispose()
        __sb = None

def transparent(color, alpha = 0.5):
    if type(color) is int:
        color = makeColor("vga", -color)
    return makeColor(color, alpha)

def setFramePosition(x, y):
    global __framePosition
    try:
        _turtleFrame.setScreenLocation(Point(x, y))
    except:
        __framePosition = (x, y)  # deferred

def setFramePositionCenter():
    global __moveToCenter
    try:
        screenWidth, screenHeight = getScreenSize()
        frameWidth = _turtleFrame.getWidth()
        frameHeight = _turtleFrame.getHeight()
        setFramePosition((screenWidth - frameWidth) // 2, (screenHeight - frameHeight) // 2) 
    except:
        __moveToCenter = True # deferred

def getScreenSize():
    from java.awt import GraphicsEnvironment
    gd = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice()
    width = gd.getDisplayMode().getWidth()
    height = gd.getDisplayMode().getHeight()
    return width, height

def setPlaygroundSize(width, height):
    Options.setPlaygroundSize(width, height)

# --------- Global wrapper for GBitmap static methods -------
def save(img, filename, type):
   abspath = os.path.abspath(filename)
   return GBitmap.save(img, abspath, type)

def scale(image, factor, angle):
   return GBitmap.scale(image, factor, angle)

def getImage(filename):
   return GBitmap.getImage(filename)

def readImage(data):
   return GBitmap.readImage(data)

def paste(bm, bmReplace, x, y):
   return GBitmap.paste(bm, bmReplace, x, y)

def crop(bm, x1, y1, x2, y2):
   return GBitmap.crop(bm, x1, y1, x2, y2)

def floodFill(bm, x, y, oldColor, newColor):
   return GBitmap.floodFill(bm, Point(x, y), oldColor, newColor)

def imageToString(bm, type):
    data = GBitmap.getByteArray(bm, type)
    return data.tostring()

def saveData(data, filename):
    file = open(filename, "wb")
    file.write(data)
    file.close()

def getBitmap():
    isGPanelValid()
    bm = p.getWindow().getBufferedImage()
    w = bm.getWidth()
    h = bm.getHeight()
    bm1 = GBitmap(w, h)
    for x in range(w):
        for y in range(h):
            color = Color(bm.getRGB(x, y), True)
            bm1.setPixelColor(x, y, color)
    return bm1

_makeColor = makeColor
def makeColor(*args):
    if len(args) == 3 and type(args[0]) == int and type(args[1]) == int and type(args[2]) == int:
        return Color(args[0], args[1], args[2])
    return _makeColor(*args)

