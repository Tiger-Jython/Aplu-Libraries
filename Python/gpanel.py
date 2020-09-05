# gpanel.py
# AP, TK
# Version 2.35, Feb 28, 2018

from ch.aplu.util import GPanel, GPrintable, Size, Monitor, X11Color, GWindow
from ch.aplu.util import GBitmap, MessageDialog, MessagePane, QuitPane
from ch.aplu.util import ModelessOptionPane, Fullscreen
from ch.aplu.simulationbar import *
from java.awt import Color, Font, Point
from java.awt.geom.Point2D import Double
from javax.swing import JColorChooser
from javax.swing import SwingUtilities
from sys import exit
import os
import math
from enum import enum
import threading
from entrydialog import *
from gvideo import *
from math import atan2
from operator import itemgetter

isTigerJython = True

def keep():
    pass


# -------------- Mouse and keyboard callbacks ---------------
__mouseClicked = None
__mouseEntered = None
__mouseExited = None
__mousePressed = None
__mouseReleased = None
__mouseDragged = None
__mouseMoved = None
__mouseSingleClicked = None
__mouseDoubleClicked = None
__keyPressed = None
__closeClicked = None

NO_KEY_PRESSED = 65535

def __callMouseClicked(e):
   global __event
   if __mouseClicked != None:
      __event = e
      if __mouseClicked.func_code.co_argcount == 1:
            __mouseClicked(e)
      if __mouseClicked.func_code.co_argcount == 2:
          x = toWindowX(e.getX())
          y = toWindowY(e.getY())
          __mouseClicked(x, y)

def __callMouseEntered(e):
   global __event
   if __mouseEntered != None:
      __event = e
      if __mouseEntered.func_code.co_argcount == 1:
            __mouseEntered(e)
      if __mouseEntered.func_code.co_argcount == 2:
          x = toWindowX(e.getX())
          y = toWindowY(e.getY())
          __mouseEntered(x, y)

def __callMouseExited(e):
   global __event
   if __mouseExited != None:
      __event = e
      if __mouseExited.func_code.co_argcount == 1:
            __mouseExited(e)
      if __mouseExited.func_code.co_argcount == 2:
          x = toWindowX(e.getX())
          y = toWindowY(e.getY())
          __mouseExited(x, y)

def __callMousePressed(e):
   global __event
   if __mousePressed != None:
      __event = e
      if __mousePressed.func_code.co_argcount == 1:
            __mousePressed(e)
      if __mousePressed.func_code.co_argcount == 2:
          x = toWindowX(e.getX())
          y = toWindowY(e.getY())
          __mousePressed(x, y)

def __callMouseReleased(e):
   global __event
   if __mouseReleased != None:
      __event = e
      if __mouseReleased.func_code.co_argcount == 1:
            __mouseReleased(e)
      if __mouseReleased.func_code.co_argcount == 2:
          x = toWindowX(e.getX())
          y = toWindowY(e.getY())
          __mouseReleased(x, y)

def __callMouseDragged(e):
   global __event
   if __mouseDragged != None:
      __event = e
      if __mouseDragged.func_code.co_argcount == 1:
            __mouseDragged(e)
      if __mouseDragged.func_code.co_argcount == 2:
          x = toWindowX(e.getX())
          y = toWindowY(e.getY())
          __mouseDragged(x, y)

def __callMouseMoved(e):
   global __event
   if __mouseMoved != None:
      __event = e
      if __mouseMoved.func_code.co_argcount == 1:
            __mouseMoved(e)
      if __mouseMoved.func_code.co_argcount == 2:
          x = toWindowX(e.getX())
          y = toWindowY(e.getY())
          __mouseMoved(x, y)

def __callMouseSingleClicked(e):
   global __event
   if __mouseSingleClicked != None:
      __event = e
      if __mouseSingleClicked.func_code.co_argcount == 1:
            __mouseSingleClicked(e)
      if __mouseSingleClicked.func_code.co_argcount == 2:
          x = toWindowX(e.getX())
          y = toWindowY(e.getY())
          __mouseSingleClicked(x, y)

def __callMouseDoubleClicked(e):
   global __event
   if __mouseDoubleClicked != None:
      __event = e
      if __mouseDoubleClicked.func_code.co_argcount == 1:
            __mouseDoubleClicked(e)
      if __mouseDoubleClicked.func_code.co_argcount == 2:
          x = toWindowX(e.getX())
          y = toWindowY(e.getY())
          __mouseDoubleClicked(x, y)

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

def onKeyPressed(f):
   global __keyPressed
   __keyPressed = f
   return f

def onCloseClicked(f):
   global __closeClicked
   __closeClicked = f
   return f

# -------------- End of callbacks ---------------


__event = None
p = None
__myCounter = 0
__sb = None

class MyPrintable(GPrintable):
   def __init__(self, fun):
      self.fun = fun
   def draw(self):
      self.fun()

class WindowNotInitialized(Exception): pass
class FunctionDeprecated(Exception): pass

def isGPanelValid():
   if p == None or (p.getPane().getWindow().isDisposed() and __myCounter != getProgramCounter()):
      raise WindowNotInitialized("Use \"makeGPanel()\" to create the graphics window before calling GPanel methods.")

def do(fun, args):
   _args = [None] * len(args)
   for i in range(len(args)):
      if type(args[i]) == list:
         _args[i] = Double(args[i][0], args[i][1])
      elif type(args[i]) == tuple:
         _args[i] = Double(args[i][0], args[i][1])
      elif type(args[i]) == complex:
         _args[i] = Double(args[i].real, args[i].imag)
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
   elif len(args) == 7:
      y = fun(_args[0], _args[1], _args[2], _args[3], _args[4], _args[5], _args[6])
   elif len(args) == 8:
      y = fun(_args[0], _args[1], _args[2], _args[3], _args[4], _args[5], _args[6], _args[7])
   else:
      raise ValueError("Illegal number of arguments")
   if y != None:
      return y

def _dummy(e):
   pass

hideFromDebugView()


def makeGPanel(*args, **kwargs):
   global __myCounter
   global __mouseClicked, __mouseEntered
   global __mouseExited, __mousePressed, __mouseReleased
   global __mouseDragged, __mouseMoved, __keyPressed, __closeClicked
   global __mouseSingleClicked, __mouseDoubleClicked
   global p

   __myCounter = getProgramCounter()
   if p != None:
      p.dispose()

   if not getTigerJythonFlag("gpanel.multiwindow"):
      __loc =  GWindow.getLastScreenLocation()
      GPanel.disposeAll()
    

   argDict = {"mouseClicked"  : __callMouseClicked,
              "mouseEntered"  : __callMouseEntered,
              "mouseExited"   : __callMouseExited,
              "mousePressed"  : __callMousePressed,
              "mouseReleased" : __callMouseReleased,
              "mouseDragged"  : __callMouseDragged,
              "mouseMoved"    : __callMouseMoved,
              "keyPressed"    : __callKeyPressed,
              "notifyClick"   : __callMouseSingleClicked,
              "notifyDoubleClick"   : __callMouseDoubleClicked,
              "notifyExit"    : __callCloseClicked}
   for key in kwargs:
      if key == "mouseClicked":
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
      elif key == "keyPressed":
         __keyPressed = kwargs[key]
      elif key == "closeClicked":
         __closeClicked = kwargs[key]

   if p == None or p.getPane().getWindow().isDisposed():
      if len(args) == 0:
         p = GPanel(**argDict)
      elif len(args) == 1:
         p = GPanel(args[0], **argDict)
      elif len(args) == 2:
         p = GPanel(args[0], args[1], **argDict)
      elif len(args) == 3:
         p = GPanel(args[0], args[1], args[2], **argDict)
      elif len(args) == 4:
         p = GPanel(args[0], args[1], args[2], args[3], **argDict)
      elif len(args) == 5:
         p = GPanel(args[0], args[1], args[2], args[3], args[4], **argDict)
      else:
         raise ValueError("Illegal number of arguments")
   p.setClosingMode(GPanel.ClosingMode.ReleaseOnClose)
   if not getTigerJythonFlag("gpanel.multiwindow"):
      if __loc != None:
         p.getWindow().setScreenLocation(__loc)
   return p
   
def createGPanel(*args):
   raise FunctionDeprecated("'createGPanel' is deprecated. Use 'makeGPanel' instead.")

def addComponent(comp):
   isGPanelValid()
   p.addComponent(comp)

def addExitListener(exitListener):
   isGPanelValid()
   p.addExitListener(exitListener)

def addMouseListener(listener):
   isGPanelValid()
   p.addMouseListener(listener)

def addMouseMotionListener(listener):
   isGPanelValid()
   p.addMouseMotionListener(listener)

def addMouseWheelListener(listener):
   isGPanelValid()
   p.addMouseWheelListener(listener)

def addStatusBar(height):
   isGPanelValid()
   p.addStatusBar(height)

def applyTransform(at):
   isGPanelValid()
   p.applyTransform(at)

def arc(radius, startAngle, extendAngle):
   isGPanelValid()
   p.arc(radius, startAngle, extendAngle)

def bgColor(*args):
   isGPanelValid()
   return p.bgColor(_toColor(*args))

def circle(*args):
   isGPanelValid()
   if len(args) == 1:
      p.circle(args[0])
   elif len(args) == 2:
      move(args[0])
      p.circle(args[1])
   elif len(args) == 3:
      move(args[0], args[1])
      p.circle(args[2])

def clear():
   isGPanelValid()
   p.clear()

def clearStore(*args):
   isGPanelValid()
   p.clearStore(_toColor(*args))

def crop(img, x1, y1, x2, y2):
   isGPanelValid()
   return p.crop(img, x1, y1, x2, y2)

def cubicBezier(*args):
   isGPanelValid()
   do(p.cubicBezier, args)

def delay(time):
   isGPanelValid()
   p._delay(int(time))

def disableClose(b):
   isGPanelValid()
   p.disableClose(b)

def dispose():
   isGPanelValid()
   p.dispose()

def draw(*args):
   isGPanelValid()
   do(p.draw, args)

def rlineto(*args):
    px, py = getPos()
    if len(args) == 1:
        arg = args[0]
        if type(arg) is tuple:
            x, y = arg
        elif type(arg) is complex:
            x, y = arg.real, arg.imag
        else:
            raise TypeError("rlineto(): tuple needed")
    elif len(args) == 2:
        x, y = args
        if type(x) not in [int, float] or type(y) not in [int, float]:
            raise TypeError("rlineto(): coordinates must be numbers")
    else:
        raise TypeError("rlineto(): too many arguments")
    lineto(x + px, y + py)

def ellipse(a, b):
   isGPanelValid()
   p.ellipse(a, b)

def enableRepaint(doRepaint):
   isGPanelValid()
   p.enableRepaint(doRepaint)

def erase():
   isGPanelValid()
   p.erase()

def fill(*args):
   isGPanelValid()
   do(p.fill, args)

def fillArc(radius, startAngle, extendAngle):
   isGPanelValid()
   p.fillArc(radius, startAngle, extendAngle)

def fillCircle(*args):
   isGPanelValid()
   if len(args) == 1:
      p.fillCircle(args[0])
   elif len(args) == 2:
      move(args[0])
      p.fillCircle(args[1])
   elif len(args) == 3:
      move(args[0], args[1])
      p.fillCircle(args[2])

def fillEllipse(a, b):
   isGPanelValid()
   p.fillEllipse(a, b)

def fillGeneralPath(gp):
   isGPanelValid()
   p.fillGeneralPath(gp)

def fillPolygon(*args):
   if len(args) == 1:
      isGPanelValid()
      if type(args[0][0]) == list or type(args[0][0]) == tuple:
         vertexes = []
         for point in args[0]:
            vertexes.append(Double(point[0], point[1]))
         p.fillPolygon(vertexes)
      elif type(args[0][0]) == complex:
         vertexes = []
         for point in args[0]:
            vertexes.append(Double(point.real, point.imag))
         p.fillPolygon(vertexes)
      else:
         fillPolygon(args[0])
   elif len(args) == 2:
      isGPanelValid()
      p.fillPolygon(args[0], args[1])
   else:
      raise ValueError("Illegal number of arguments")

def fillRectangle(*args):
   isGPanelValid()
   do(p.fillRectangle, args)

def fillTriangle(*args):
   isGPanelValid()
   if len(args) == 1:  # passed list with 3 points
      do(p.fillTriangle, args[0])
   else:
      do(p.fillTriangle, args)

def floodFill(img, pt, oldColor, newColor):
   isGPanelValid()
   return p.floodFill(img, pt, oldColor, newColor)

def font(myFont):
   isGPanelValid()
   p.font(myFont)

def generalPath(gp):
   isGPanelValid()
   p.generalPath(gp)

def getAbout():
   isGPanelValid()
   return p.getAbout()

def getBgColor():
   isGPanelValid()
   return p.getBgColor()

def getBgColorStr():
   isGPanelValid()
   return p.getBgColorStr()

def getByteArray(img, imageFormat):
   isGPanelValid()
   return p.getByteArray(img, imageFormat)

def getDividingPoint(*args):
   isGPanelValid()
   pt = do(p.getDividingPoint, args)
   return [pt.x, pt.y]

def getLineWidth():
   isGPanelValid()
   return p.getLineWidth()

def getKey():
   isGPanelValid()
   key = p.getKeyInt()
   if key > 255:
      return ""
   else:    
      return str(chr(key))

def getKeyCode():
   isGPanelValid()
   return p.getKeyCode()

def getKeyCodeWait(*args):
   isGPanelValid()
   if len(args) == 0:
      return p.getKeyCodeWait()
   if len(args) == 1:
      if args[0]:
          code = p.getKeyCodeWait()
          if isDisposed():
             exit()
          return code
      else:
          return p.getKeyCodeWait()

def _getKeyWait():
   key = p.getKeyWaitInt()
   if key > 255:
      return ""
   else:    
      return str(chr(key))

def getKeyWait(*args):
   isGPanelValid()
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
   isGPanelValid()
   return p.getModifiers()

def getModifiersText():
   isGPanelValid()
   return p.getModifiersText()

def getPixelColor(*args):
   isGPanelValid()
   return do(p.getPixelColor, args)

def getPixelColorStr(*args):
   isGPanelValid()
   return do(p.getPixelColorStr, args)

def getPos():
   isGPanelValid()
   return [p.getPosX(), p.getPosY()]

def getPosX():
   isGPanelValid()
   return p.getPosX()

def getPosY():
   isGPanelValid()
   return p.getPosY()

def getSupportedImageFormats():
   isGPanelValid()
   return p.getSupportedImageFormats()

def getVersion():
   isGPanelValid()
   return p.getVersion()

def getWindow():
   isGPanelValid()
   return p.getWindow()

def image(*args):
   isGPanelValid()
   return do(p.image, args)

def imageHeight(*args):
   isGPanelValid()
   return do(p.imageHeight, args)

def imageWidth(*args):
   isGPanelValid()
   return do(p.imageWidth, args)

def isDisposed():
   isGPanelValid()
   return p.isDisposed()

def isReady():
   isGPanelValid()
   return p.isReady()

def kbhit():
   isGPanelValid()
   return p.kbhit()

def line(*args):
   isGPanelValid()
   if len(args) == 1 and type(args[0]) in (list, tuple):
       for i in range(len(args[0])):
           if i == 0:
               pos(args[0][i])
           else:
               draw(args[0][i])
   else:
       do(p.line, args)

lineto = draw  # synonym
lineTo = draw  # synonym

def lineWidth(width):
   isGPanelValid()
   p.lineWidth(width)

def move(*args):
   isGPanelValid()
   do(p.move, args)

def paste(original, replacement, xstart, ystart):
   isGPanelValid()
   return p.paste(original, replacement, xstart, ystart)

def point(*args):
   isGPanelValid()
   do(p.point, args)

def printerPlot(*args):
   isGPanelValid()
   if len(args) == 1:
      mp = MyPrintable(args[0])
      p.print(mp)
   elif len(args) == 2:
      mp = MyPrintable(args[0])
      p.print(mp, args[1])
   else:
      raise ValueError("Illegal number of arguments")

def polygon(*args):
   if len(args) == 1:
      isGPanelValid()
      if type(args[0][0]) == list or type(args[0][0]) == tuple:
         vertexes = []
         for point in args[0]:
            vertexes.append(Double(point[0], point[1]))
         p.polygon(vertexes)
      elif type(args[0][0]) == complex:
         vertexes = []
         for point in args[0]:
            vertexes.append(Double(point.real, point.imag))
         p.polygon(vertexes)
      else:
         polygon(args[0])
   elif len(args) == 2:
      isGPanelValid()
      p.polygon(args[0], args[1])
   else:
      raise ValueError("Illegal number of arguments")

def pos(*args):
   isGPanelValid()
   do(p.pos, args)

def printPrinter(*args):
   isGPanelValid()
   return do(p.print, args)

def printScreen(*args):
   isGPanelValid()
   return do(p.printScreen, args)

def quadraticBezier(*args):
   isGPanelValid()
   do(p.quadraticBezier, args)

def recallGraphics():
   isGPanelValid()
   p.recallGraphics()

def rectangle(*args):
   isGPanelValid()
   do(p.rectangle, args)

def repaint():
   isGPanelValid()
   p.repaint()

def resizable(b):
   isGPanelValid()
   p.resizable(b)

def setColor(*args):
   isGPanelValid()
   return p.setColor(_toColor(*args))

def setFocusable(focusable):
   isGPanelValid()
   p.setFocusable(focusable)

def setPaintMode():
   isGPanelValid()
   p.setPaintMode()

def setStatusText(*args):
   isGPanelValid()
   do(p.setStatusText, args)

def setTransparency(img, factor):
   isGPanelValid()
   return p.setTransparency(img, factor)
 
def setXORMode(c):
   isGPanelValid()
   p.setXORMode(c)

def showStatusBar(show):
   isGPanelValid()
   p.showStatusBar(show)

def storeGraphics():
   isGPanelValid()
   p.storeGraphics()

def text(*args):
   isGPanelValid()
   do(p.text, args)

def title(title):
   isGPanelValid()
   p.title(title)

def toUser(windowX, windowY):
   isGPanelValid()
   return p.toUser(windowX, windowY)

def toUserHeight(windowHeight):
   isGPanelValid()
   return p.toUserHeight(windowHeight)

def toUserWidth(windowWidth):
   isGPanelValid()
   return p.toUserWidth(windowWidth)

def toUserX(windowX):
   isGPanelValid()
   return p.toUserX(windowX)

def toUserY(windowY):
   isGPanelValid()
   return p.toUserY(windowY)

def toWindow(userX, userY):
   isGPanelValid()
   return p.toWindow(userX, userY)

def toWindowHeight(userHeight):
   isGPanelValid()
   return p.toWindowHeight(userHeight)

def toWindowWidth(userWidth):
   isGPanelValid()
   return p.toWindowWidth(userWidth)

def toWindowX(userX):
   isGPanelValid()
   return p.toWindowX(userX)

def toWindowY(userY):
   isGPanelValid()
   return p.toWindowY(userY)

def triangle(*args):
   isGPanelValid()
   if len(args) == 1:  # passed list with 3 points
      do(p.triangle, args[0])
   else:
      do(p.triangle, args) 

def validate():
   isGPanelValid()
   p.validate()

def visible(isVisible):
   isGPanelValid()
   p.visible(isVisible)

def window(xmin, xmax, ymin, ymax):
   isGPanelValid()
   p.window(xmin, xmax, ymin, ymax)

def windowCenter():
   isGPanelValid()
   p.windowCenter()

def windowPosition(ulx, uly):
   isGPanelValid()
   p.windowPosition(ulx, uly)

def windowSize(width, height):
   isGPanelValid()
   p.windowSize(width, height)


# ------------------------ end of GPanel methods -----------

def getRandomX11Color():
   return X11Color.getRandomColorStr()

def putSleep():
   Monitor.putSleep()

def wakeUp():
   Monitor.wakeUp()

def askColor(title, defaultColor):
   if isinstance(defaultColor, str):
      return JColorChooser.showDialog(None, title, X11Color.toColor(defaultColor))
   else:
      return JColorChooser.showDialog(None, title, defaultColor)


def drawGrid(*args):
   isGPanelValid()
   if len(args) == 2:
      __drawGrid(p, 0, args[0], 0, args[1], 10, 10, None)
   if len(args) == 3:
      __drawGrid(p, 0, args[0], 0, args[1], 10, 10, args[2])
   elif len(args) == 4:
      __drawGrid(p, args[0], args[1], args[2], args[3], 10, 10, None)
   elif len(args) == 5:
      __drawGrid(p, args[0], args[1], args[2], args[3], 10, 10, args[4])
   elif len(args) == 6:
      __drawGrid(p, args[0], args[1], args[2], args[3], args[4], args[5], None)
   elif len(args) == 7:
      __drawGrid(p, args[0], args[1], args[2], args[3], args[4], args[5], args[6])
   else:
      raise ValueError("Illegal number of arguments")

def drawPanelGrid(panel, *args):
   if len(args) == 2:
      __drawGrid(panel, 0, args[0], 0, args[1], 10, 10, None)
   if len(args) == 3:
      __drawGrid(panel, 0, args[0], 0, args[1], 10, 10, args[2])
   elif len(args) == 4:
      __drawGrid(panel, args[0], args[1], args[2], args[3], 10, 10, None)
   elif len(args) == 5:
      __drawGrid(panel, args[0], args[1], args[2], args[3], 10, 10, args[4])
   elif len(args) == 6:
      __drawGrid(panel, args[0], args[1], args[2], args[3], args[4], args[5], None)
   elif len(args) == 7:
      __drawGrid(panel, args[0], args[1], args[2], args[3], args[4], args[5], args[6])
   else:
      raise ValueError("Illegal number of arguments")

def __drawGrid(pa, xmin, xmax, ymin, ymax, xticks, yticks, color):
   # Save current cursor and color
   xPos = pa.getPosX()
   yPos = pa.getPosY()
   if color != None:
       oldColor = pa.setColor(color)
   # Horizontal
   for i in range(yticks + 1):
      y = ymin + (ymax - ymin) / yticks * i
      pa.line(xmin, y, xmax, y)
      if isinstance(ymin, float) or isinstance(ymax, float):
         pa.text(xmin - 0.09 * (xmax - xmin), y, str(y))
      else:
         pa.text(xmin - 0.09 * (xmax - xmin), y, str(int(y)))
   # Vertical
   for k in range(xticks + 1):
      x = xmin + (xmax - xmin) / xticks * k
      pa.line(x, ymin, x, ymax)
      if isinstance(xmin, float) or isinstance(xmax, float):
         pa.text(x, ymin - 0.05 * (ymax - ymin), str(x))
      else:
         pa.text(x, ymin - 0.05 * (ymax - ymin), str(int(x)))
   # Restore cursor and color
   pa.move(xPos, yPos)
   if color != None:
       pa.setColor(oldColor)

def isLeftMouseButton(*args):
   isGPanelValid()
   if len(args) == 1:
      return SwingUtilities.isLeftMouseButton(args[0])
   if __event != None and SwingUtilities.isLeftMouseButton(__event):
      return True
   return False

def isRightMouseButton(*args):
   isGPanelValid()
   if len(args) == 1:
      return SwingUtilities.isRightMouseButton(args[0])
   if __event != None and SwingUtilities.isRightMouseButton(__event):
      return True
   return False

def getKeyModifiers():
   isGPanelValid()
   if __event == None:
      return None
   return __event.getModifiersEx()

#def put_sleep():
#    global __evt
#    __evt = threading.Event()    
#    __evt.clear()
#    __evt.wait()

#def wake_up():
#    try:
#        __evt   
#    except NameError:
#        return
#    __evt.set()
            
def linfit(X, Y):
# Code from: http://code.activestate.com
    def mean(Xs):
        return sum(Xs) / len(Xs)
    m_X = mean(X)
    m_Y = mean(Y)

    def std(Xs, m):
        normalizer = len(Xs) - 1
        return math.sqrt(sum((pow(x - m, 2) for x in Xs)) / normalizer)

    def pearson_r(Xs, Ys):
        sum_xy = 0
        sum_sq_v_x = 0
        sum_sq_v_y = 0

        for (x, y) in zip(Xs, Ys):
            var_x = x - m_X
            var_y = y - m_Y
            sum_xy += var_x * var_y
            sum_sq_v_x += pow(var_x, 2)
            sum_sq_v_y += pow(var_y, 2)
        return sum_xy / math.sqrt(sum_sq_v_x * sum_sq_v_y)

    r = pearson_r(X, Y)
    b = r * (std(Y, m_Y) / std(X, m_X))
    A = m_Y - b * m_X
    return b, A

def getScreenWidth():
   return Fullscreen().getWidth()

def getScreenHeight():
   return Fullscreen().getHeight()


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

def _toColor(*args):
    import java.awt.Color
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

def arrow(*args):
    if len(args) == 5:
        _arrow([args[0], args[1]], [args[2], args[3]], args[4])
    elif len(args) == 4:
        _arrow([args[0], args[1]], [args[2], args[3]])
    elif len(args) == 3:
        if type(args[0]) == complex:
            _arrow([args[0].real, args[0].imag], [args[1].real, args[1].imag], args[2])
        else:
            _arrow(args[0], args[1], args[2])
    elif len(args) == 2:
        if type(args[0]) == complex:
            _arrow([args[0].real, args[0].imag], [args[1].real, args[1].imag])
        else:
            _arrow(args[0], args[1])

def _arrow(pt0, pt1, size = 10):
    s = toWindowWidth(size)
    phi = math.atan2(pt1[1] - pt0[1], pt1[0] - pt0[0])
    h = math.sqrt(3) / 2 * s
    tri = [[-h, s/2], [-h, -s/2], [0, 0]]
    tri = _rotTri(tri, phi)
    tri = _transTri(tri, pt1)
    fillTriangle(tri)
    d = math.sqrt((pt0[0] - pt1[0]) * (pt0[0] - pt1[0]) + (pt0[1] - pt1[1]) * (pt0[1] - pt1[1]))
    a = getDividingPoint(pt0, pt1, (d - h) / d)
    line(pt0, a)

def doubleArrow(*args):
    if len(args) == 5:
        _doubleArrow([args[0], args[1]], [args[2], args[3]], args[4])
    elif len(args) == 4:
        _doubleArrow([args[0], args[1]], [args[2], args[3]])
    elif len(args) == 3:
        if type(args[0]) == complex:
            _doubleArrow([args[0].real, args[0].imag], [args[1].real, args[1].imag], args[2])
        else:
            _doubleArrow(args[0], args[1], args[2])
    elif len(args) == 2:
        if type(args[0]) == complex:
            _doubleArrow([args[0].real, args[0].imag], [args[1].real, args[1].imag])
        else:
            _doubleArrow(args[0], args[1])

def _doubleArrow(pt0, pt1, size = 10):
    s = toWindowWidth(size)
    phi = math.atan2(pt1[1] - pt0[1], pt1[0] - pt0[0])
    h = math.sqrt(3) / 2 * s
    tri = [[-h, s/2], [-h, -s/2], [0, 0]]
    tri1 = _rotTri(tri, phi)
    tri1 = _transTri(tri1, pt1)
    fillTriangle(tri1)
    tri2 = _rotTri(tri, math.pi + phi)
    tri2 = _transTri(tri2, pt0)
    fillTriangle(tri2)
    d = math.sqrt((pt0[0] - pt1[0]) * (pt0[0] - pt1[0]) + (pt0[1] - pt1[1]) * (pt0[1] - pt1[1]))
    a = getDividingPoint(pt0, pt1, (d - h) / d)
    b = getDividingPoint(pt0, pt1, h / d)
    line(a, b)

def _rotTri(tri, phi):
   pt0 = [math.cos(phi) * tri[0][0] - math.sin(phi) * tri[0][1], math.sin(phi) * tri[0][0] + math.cos(phi) * tri[0][1]]
   pt1 = [math.cos(phi) * tri[1][0] - math.sin(phi) * tri[1][1], math.sin(phi) * tri[1][0] + math.cos(phi) * tri[1][1]]
   pt2 = [math.cos(phi) * tri[2][0] - math.sin(phi) * tri[2][1], math.sin(phi) * tri[2][0] + math.cos(phi) * tri[2][1]]
   return [pt0, pt1, pt2]

def _transTri(tri, pt):
    pt0 = [tri[0][0] + pt[0], tri[0][1] + pt[1]]
    pt1 = [tri[1][0] + pt[0], tri[1][1] + pt[1]]
    pt2 = [tri[2][0] + pt[0], tri[2][1] + pt[1]]
    return [pt0, pt1, pt2]

class Node():
    def __init__(self, center, text, 
            size = 50, 
            borderSize = 1, 
            borderColor = Color.black, 
            textColor = Color.black, 
            bgColor = Color.white, 
            font = Font("Courier", Font.PLAIN, 14)):
        self.center = center
        self.text = text
        self.size = size
        self.borderSize =  borderSize
        self.borderColor = _toColor(borderColor)
        if type(self.borderColor) in [str, unicode]:
            self.borderColor = GPanel.toColor(self.borderColor)
        self.textColor = _toColor(textColor)
        if type(self.textColor) in [str, unicode]:
            self.textColor = GPanel.toColor(self.textColor)
        self.bgColor = _toColor(bgColor)
        if type(self.bgColor) in [str, unicode]:
            self.bgColor = GPanel.toColor(self.bgColor)
        self.font = font
        p.drawNode(self.center[0], self.center[1], 
                   self.text, self.size, self.borderSize, 
                   self.borderColor, self.textColor, 
                   self.bgColor, self.font)

    def getCenter(self):
        return self.center

    def getText(self):
        return self.text

    def getSize(self):
        return self.size

    def getBorderSize(self):
        return self.borderSize

    def getBorderColor(self):
        return self.borderColor

    def getTextColor(self):
        return self.textColor
    
    def getBgColor(self):
        return self.bgColor
    
    def getFont(self):
        return self.font


class EdgeBase():
    def __init__(self, node1, node2, lineWidth, color):
        pt1 = node1.getCenter()
        pt2 = node2.getCenter()
        size1 = node1.getSize()
        size2 = node2.getSize()
        d = math.sqrt((pt1[0] - pt2[0]) * (pt1[0] - pt2[0]) + (pt1[1] - pt2[1]) * (pt1[1] - pt2[1]))
        self.p1 = getDividingPoint(pt1, pt2, toWindowWidth(size1 // 2) / d)
        self.p2 = getDividingPoint(pt1, pt2, (d - toWindowWidth(size2 // 2)) / d)

class Edge(EdgeBase):
    def __init__(self, node1, node2, lWidth = 1, color = Color.black):
        EdgeBase.__init__(self, node1, node2, lWidth, color)
        oldLWidth = getLineWidth()
        lineWidth(lWidth)
        oldColor = setColor(_toColor(color))
        line(self.p1, self.p2)                
        lineWidth(oldLWidth)
        setColor(oldColor)

class DirectedEdge(EdgeBase):
    def __init__(self, node1, node2, lWidth = 1, color = Color.black, headSize = 10):
        EdgeBase.__init__(self, node1, node2, lWidth, color)
        oldLWidth = getLineWidth()
        lineWidth(lWidth)
        oldColor = setColor(_toColor(color))
        arrow(self.p1, self.p2, headSize) 
        lineWidth(oldLWidth)
        setColor(oldColor)

class DoubleDirectedEdge(EdgeBase):
    def __init__(self, node1, node2, lWidth = 1, color = Color.black, headSize = 10):
        EdgeBase.__init__(self, node1, node2, lWidth, color)
        oldLWidth = getLineWidth()
        lineWidth(lWidth)
        oldColor = setColor(_toColor(color))
        doubleArrow(self.p1, self.p2, headSize) 
        lineWidth(oldLWidth)
        setColor(oldColor)

_makeColor = makeColor
def makeColor(*args):
    if len(args) == 3 and type(args[0]) == int and type(args[1]) == int and type(args[2]) == int:
        return Color(args[0], args[1], args[2])
    return _makeColor(*args)

def _getBorder(poly, left):
    x = poly[0][0]
    data = []
    for pt in poly:
        if left and pt[0] < x or not left and pt[0] > x:
            x = pt[0]
            data = []
        if pt[0] == x:
            data.append(pt)
    data.sort(key = itemgetter(1), reverse = not left)
    return data

def _getNext(poly, path, current, isUpper):
    max = -10  # less than -pi/2 
    same = None
    for pt in poly:
        if isUpper and pt not in path and pt[0] > current[0] \
          or  not isUpper and pt not in path and pt[0] < current[0]:
            dx = pt[0] - current[0]
            dy = pt[1] - current[1]
            if isUpper:
                angle = atan2(dy, dx)
            else:    
                angle = atan2(-dx, dy)
            if angle > max:
                max = angle
                same = []
            if angle == max:
                 same.append(pt)
    if same == None:
        print "Not a real polygon"
        raise Exception("Not a real polygon")
    x = same[0][0]
    next = same[0]
    for pt in same:
        if isUpper and pt[0] < x: # take most left
            x = pt[0]
            next = pt
        elif not isUpper and pt[0] > x: # take most right
            x = pt[0]
            next = pt
    return next

def _getUpperPath(poly, left, right):
    path = left[:]
    current = left[-1]
    next = _getNext(poly, path, current, True)
    while next != right[0]:
        path.append(next)
        next = _getNext(poly, path, next, True)
    path.append(right[0])
    return path

def _getLowerPath(poly, left, right):
    path = right[:]
    current = right[-1]
    next = _getNext(poly, path, current, False)
    while next != left[0]:
        path.append(next)
        next = _getNext(poly, path, next, False)
    path.append(left[0])
    return path

def getHull(poly):
    left = _getBorder(poly, True)
    right = _getBorder(poly, False)
    upperPath = _getUpperPath(poly, left, right)
    lowerPath = _getLowerPath(poly, left, right)
    li = []
    for pt in upperPath[:-1]:
        li.append(pt)
    for pt in lowerPath[:-1]:
        li.append(pt)
    return upperPath, lowerPath, li

