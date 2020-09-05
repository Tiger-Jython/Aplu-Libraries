Readme.txt for the ch.aplu.util and ch.aplu.turtle package distribution
Version 3.27, Feb 17, 2016
==================================================================

See http://www.aplu.ch/home/apluhomex.jsp?site=65
for most recent information.


History of package ch.aplu.util:
-------------------------------

V1.36 - Sep 2004:
  - First official release
V1.37 - Nov 2004:
  - Add focus listener in GPanel and request focus
    when using APPLETFRAME to show the GPanel window
    in front of browser window
V1.38 - Dez 2004:
  - Add Console.setTitle()
V1.39 - Jan 2005:
  - Add Console.printf() in V5 library, add init() in setTitle()
V1.40 - March 2005:
  - No changes (modifications in ch.aplu.turtle)
V1.41 - May 2005:
  - Autowrapping in Console
V1.42 - Dec 2005:
  - class LptPort, ExitListener in Console
V1.43 - Feb 2006:
  - No changes (modifications in ch.aplu.turtle)
V1.44 - March 2007:
  - Remove deprecated methods from GPanel
  - Added methods in GPanel: windowSize(int width, int height)
                             windowCenter()
                             several constructors to set the size of the client area
  - new class Fullscreen in order to create a GPanel on full screen
  - Minor changes in GWindow in order to support full screen windows
  - Minor updates in Javadoc documentation
  - Classes added in package
     - MessagePane
     - QuitPane
     - Cleanable (interface)
  - class Console: Call Swing methods from Event Dispatch Thread only
V1.45 - Aug 2007:
  - Repaired printf() in ch.aplu.util.Console (for J2SE V5up)
  - Added class Monitor
  - Modified Console use wait/notify to reduce CPU time (from 100% to 2%)
V1.46 - Aug 2007:
  - Added classes SoundPlayer, SoundPlayerExt and interface SoundPlayerListener
  - GPanel.color() now returns the previous color
V1.47 - Sept 2007:
  - Correction of transformation methods toWindow, toUser, when GPanel is zoomed
V1.48 - Sept 2007:
  - Remove bug with getKeyWait()
  - Add storeGraphics(), recallGraphics(), clearStore();
V1.49 - Oct 2007:
  - Added: interface SoundConverter for easy integration of a sound converter
V1.50 - Oct 2007:
  - Modified implementation of MessagePane, added MessagePane.title()
V1.51- Nov 2007:
   - No changes (modifications in ch.aplu.turtle)
V1.52 - Nov 2007:
   - Added bean support for GPanel in order use it in a Gui builder
V1.53 - Nov 2007:
   - No changes (modifications in ch.aplu.turtle)
V1.54 - Nov 2007:
   - Added method JRunner, GPane creates GPanel 100x100
   - class Monitor modified: notifyAll() instead of notify()
V1.55 - Dec 2007:
   - Added more methods to JRunner
   - Added property enableFocus to GPane, default: setFocusable(false)
V1.56 - March 2008:
   - ModelessOptionDialog: changed icon resource to url, added: showTitle()
V1.57 - March 2008:
   - JRunner.run() allows invoking void method, JRunner doc revisted
V1.58 - March 2008:
   - No changes (modifications in ch.aplu.turtle)
V1.59 - Oct 2008:
   - Changed code in MessageDialog in order to avoid depreciation message
   - Added Thread.currentThread().sleep(1) in QuitPane.quit()
   - Added Gpanel.getPixelColor(x, y) to return color of pixel at (x, y)
V2.00 - Nov 2008:
   - Fixed bug in callback notification of AlarmTimer
   - J2SE V1.4 no longer supported
V2.01 - Jan 2009:
   - GPanel no longer changes the current look-and-feel
V2.02 - Feb 2009:
   - QuitPane, Console, GPanel, MessageDialog, MessagePane, ModelessOptionPane
     constructors and InputDialog methods run in EDT now
V2.03 - Feb 2009:
   - Added ModelessOptionPane.dispose()
V2.04 - Feb 2009:
   - Added ModelessOptionPane.setVisible(), ModelessOptionPane.getDialog()
   - Added QuitPane.setVisible(), QuitPane.getDialog()
   - Added MessageDialog.getDialog()
   - Added MessagePane.setVisible(),  MessagePane.getDialog()
V2.05 - Feb 2009:
   - Fixed bug in QuitPane: listener cleanable did not work any more
   - Added QuitPane.halt() to block the current thread until Quit or Close is hit.
   - Fixed bug in BaseAlarmTimer: callback timeElapsed() was not triggered
     at elapsed time after calling resume()
V2.06 - March 2009:
   - Added default constructor to QuitPane and QuitPane.addQuitNotifier()
V2.07 - March 2009:
   - Added GPanel.delay()
V2.08 - April 2009:
   - Added GPanel.addMouseMotionListener, GPanel.addMouseWheelListener
V2.09 - June 2009:
   - Added GPane.delay()
V2.10 - June 2009:
   - Added support for multiple sound devices in SoundPlayer, SoundPlayerEx
   - Added class SoundRecorder
V2.11 - October 2009:
   - Removed closing stream in init() of SoundPlayer (could not play from jar)
V2.12 - October 2009:
   - Added playLoop() in SoundPlayer
V2.13 - October 2009:
   - Added PCM 8 bit unsigned wav support in SoundPlayer/SoundPlayerExt
V2.14 - Dec 2009:
   - No changes (modifications in ch.aplu.turtle)
V2.15 - April 2010:
   - ExitListener now extends java.util.EventListener (for Jython)
   - TimerListener now extends java.util.EventListener (for Jython)
   - SoundPlayerListener now extends java.util.EventListener (for Jython)
2.16 - April 2010:
   - No changes (modifications in ch.aplu.turtle)
V2.17 - April 2010:
   - Added class FunctionPlayer to play and save user defined audio functions
V2.18 - September 2010:
   - Modified SoundRecorder: close audio stream in stopCapture(), 
   - Add class SoundSampleListener to get notification at each recorded sample
V2.19 - October 2010:
   - Fixed bug in GPanel.lineWidth(), GPane.lineWidth() when lineWidth was
     set back to 1 after it was set to >1
V2.20 - November 2010:
   - Added class ModelessOptionPane: provision for integrating a button
   - Added class Monitor: putSleep()/wakeUp() using an external object lock
   - Modified class Console: putSleep()/wakeUp() with internal object
V2.21 - December 2010:
   - Added ModelessOptionPane constructor for undecorated window of given size
   - Added ModelessOptionPane.isVisible(), .requestFocus(), .toFront()
V2.22 - December 2010:
   - Added ModelessOptionPane undecorated window with 1 pixel black border
   - Added addStatusBar(), setStatusText(), showStatusBar() in class GPanel
V2.23 - December 2010:
   - Added setStatusText(text, font, color) in class GPanel
V2.24 - April 2011:
   - Removed Console.getVersion(), Console.getAbout()
V2.25 - August 2011:
   - Modified SoundPlayer, SoundPlayerExt constructors: 
     InputStream replaced by URL because of crash under Java 1.7
V2.26 - January 2013:
   - Added Console.show(), Console.hide()  
   - Compiled with Java Version 1.6
V2.27 
   - Febrary 2013 Unchanged, modifications in ch.aplu.turtle
V2.28 
   - Febrary 2013 Unchanged, modifications in ch.aplu.turtle
V2.29 
   - Febrary 2013 Added Complex.hashCode() in accordances with Complex.equals()
V2.30 
   - March 2013:
   - Unchanged, modifications in ch.aplu.turtle
V2.31 - March 2013:
   - Unchanged, modifications in ch.aplu.turtle
V2.32 - March 2013:
   - Unchanged, modifications in ch.aplu.turtle
V2.33 - March 2013:
   - Added class FilePath
   - Added aplu_util.properties to change default library options
   - Added GPanel with options when the title bar close button is hit
   - Added GPanel all graphics methods throw RunTimeException,
     if GPanel is closed (for ReleaseOnClose option only)
   - Added X11 color names for all GPanel methods with color parameter
V2.34 - March 2013:
   - Added GWindow.isDisposed()
   - Added GPane.setColor(), GPanel.setColor(), GPanel.addComponent()
V2.35 - April 2013:
   - Added ModelessOptionDialog.addExitListener()
   - Added QuitPane.quit(boolean exit)
   - Remove QuitPane.addCleanable() (replaced by addExitListener)
   - Added ModelessOptionPane.addExitListener()
   - Added MessagePane.addExitListener()
V2.36 - May 2013:
   - Added: Class Main and annotation NoMain
V2.37 - July 2013:
   - Added: class GImage with some useful image functions
   - Modified: Image loading in GWindow, GPane, GPanel: 
             - Removed: concept of last loaded image
             - more than one image may be displayed
             - loading from JAR supported
             - BuffereImage load supported
   - Added: GPane, GPanel.ellipse() and fillEllipse()
2.38 - July 2013:
   - Fixed: GPane, GPanel.rectangle(), arc() now draws correctly when
            coordinate axis are reversed
   - Added: GPane, GPanel.setEnableResize()
V2.39 - July 2013:
   - Added: SoundPlayer, SoundPlayerExt ctors taking AudioInputStream
   - Added: SoundPlayer, SoundPlayerExt ctors taking ByteArrayInputStream
   - Added: SoundRecorder.writeWavFile() overloaded versions, returns boolean now
V2.40 - Aug 2013: 
   - Unchanged, modifications in ch.aplu.turtle
V2.41 - Aug 2013:
  - Fixed: GPanel size adaption with properties file now works
  - Added: Property GPanelDecoration
  - Added: SoundRecorder.capture(), SoundRecorder.getCapturedBytes()
  - Added: SoundPlayer ctor with int[], SoundRecorder.writeWavFile() with int[]
  - Added: SoundPlayer.getWavRaw(), getWavMono(), getWavStereo(), getWavInfo()
  - Added: SoundPlayer.blockingPlay() 
  - Added: GPane and GPanel quadratic and cubic bezier curve
  - Added: GPane and GPanel fill()
  - Added. GPanel.getDividingPoint()
  - Added: class GVector
  - Added: GPane, GPanel.text() with extended attributes
 V2.42 - Sep 2013:
  - Modified: X11Color, restored original color name definitions
 V2.43 - Nov 2013:
  - Fixed: Title taken from Ctor, even if defined as property
  - Fixed: InputDialog now displays user defined title, all methods are static now
 V2.44 - Dec 2013
  - Fixed: loading images contained in _ prefixed directory of JAR now works
  - Added: Colors olive and teal in class X11Color
 V2.45 - Dec 2013
  - SoundPlayer now searches sound file in JAR automatically
 V2.46 - Feb 2014
  - Console initialized before being redisplayed
 V2.47 - June 2014
  - Added: GPanel.setClosingMode()
  - Added: Console.setClosingMode()
  - Fixed: GPanel.bgColor() ignores alpha channel
 V2.48 - July 2014:
  - Added: class PackageInfo
 V2.49 - Aug 2014:
  - Added: additional methods in HtmlPane to load files from JAR
 V2.50 - Aug 2014:
  - Fixed: Image tag load now works in HtmlPane for all kind of situations
 V2.51 - Aug 2014:
  - Fixed: FilePath.pack() now clears static fileList
  - Added: FilePath.copyTree() with extension filter
V2.52 - Sep 2014:
  - Added: SoundPlayer.setWavMono(), setWavStereo(), setWavRaw(), setWavInfo() with url
  - Added: SoundPlayer.playSine()
V2.53 - Nov 2014:
  - Added: class GPanelOptions to set GPanel options on-the-fly
V2.54 - Nov 2014:
  - Modified: GBitmap image read from absolute path now using getCanonicalPath()   
V2.55 - Dec 2014:
  - Added: GPanel.addKeyListener()
V2.56 - Jan 2015:
  - Fixed: SoundPlayer.playSine() static now
V2.57 - Jan 2015
  - Added: Support for mouse double-clicks in GPane and GPanel
V2.58 - Apr 2015
  - Added: class EntryDialog and items
V2.59 - Apr 2015
  - Modified: Implementation of EntryDialog no longer derived from JFrame
V3.00 - Apr 2015
  - Recompiled with Java 1.6 for backward compatibility
V3.01 - June 2015
  - Fixed: closing DataLine in SoundPlayer.playSine() now to avoid overflow on
       small systems
V3.02 - June 2015
  - Fixed: SoundPlayer.play() releases resources now
V3.03 - Aug 2015
  - Added: GPanel.getKeyInt(), GPanel.getKeyWaitInt()
V3.04 - Aug 2015
  - Added: GBitmap.getImage(), readImage()
V3.05 - Sep 2015
  - Fixed: Properties for HtmlPane inserted in aplu_util.properties
V3.06 - Jan 2016
  - Added: setEditable() for all EntryPane text fields
V3.07 - Feb 2016
  - Modified: EntryPane with increased size
V3.08 - Feb 2016
  - SoundPlayer(String audioPathname,...) now converts to absolute path
  - same for SoundPlayerEx()

History of package ch.aplu.turtle:
---------------------------------

V1.36 - Sep 2004 First official release
V1.37 - Nov 2004 Unchanged, modifications in ch.aplu.util
V1.38 - Dec 2004 Unchanged, modifications in ch.aplu.util
V1.39 - Jan 2005 Unchanged, modifications in ch.aplu.util
V1.40 - Mar 2005 Add background color, TurtleKeyAdapter, TurtleArea
V1.41 - May 2005 User defined turtle shape, minor changes in doc and code style
V1.42 - Dec 2005 Unchanged, modifications in ch.aplu.util
V1.43 - Feb 2006 Bug removed: Turtle.turtleFrame was not initialized in all ctors of class Turtle
V1.44 - Mar 2007 Bug removed: stampTurtle did not work properly in wrap mode
V1.45 - Aug 2007 TurtleKeyAdapter: use wait/notify to reduce CPU time (from 100% to 2%)
V1.46 - Aug 2007 synchronize(playground) for forward and rotate animation,
                 new method bong() using StreamingPlayer
V1.47 - Sep 2007 Unchanged, modifications in ch.aplu.util
V1.48 - Sep 2007 Unchanged, modifications in ch.aplu.util
V1.49 - Oct 2007 Unchanged, modifications in ch.aplu.util
V1.50 - Oct 2007 Unchanged, modifications in ch.aplu.util
V1.51 - Nov 2007 Fixed: correct position of label, when wrapping is on
                 Fixed: getPos() returns now the wrapped coordinates
                 Added: _getPos() returns unwrapped coordinates
V1.52 - Nov 2007 Added bean classes in order to use turtles with a Gui builder
V1.53 - Nov 2007 Added TurtlePane visual information when used in Gui builder design mode
V1.54 - Nov 2007 Minor changes to documentation
V1.55 - Dec 2007 Added property enableFocus to GPane, default: setFocusable(false)
V1.56 - Mar 2008 Unchanged, modifications in ch.aplu.util
V1.57 - Mar 2008 Unchanged, modifications in ch.aplu.util
V1.58 - Mar 2008 Modification to fill() (fill(x, y)):
                 region is defined with pixel color at current position
                 as opposed to background color
V1.59 - Oct 2008 Added ctors TurtleFrame with window position (ulx, uly)
                 Added Turtle.getPixelColor()
V2.00 - Nov 2008 Unchanged, modifications in ch.aplu.util
                 J2SE V1.4 no longer supported
V2.01 - Jan 2009 Unchanged, modifications in ch.aplu.util
V2.02 - Feb 2009 Turtle constructors run in EDT now
V2.03 - Feb 2009 Unchanged, modifications in ch.aplu.util
V2.04 - Feb 2009 Unchanged, modifications in ch.aplu.util
V2.05 - Feb 2009 Unchanged, modifications in ch.aplu.util
V2.06 - Mar 2009 Unchanged, modifications in ch.aplu.util
V2.07 - Mar 2009 All except print methods synchronized, so Turtle package is
                 now thread-safe
V2.08 - Apr 2009 Unchanged, modifications in ch.aplu.util
V2.09 - Jun 2009 Unchanged, modifications in ch.aplu.util
V2.10 - Jun 2009 Unchanged, modifications in ch.aplu.util
V2.11 - Oct 2009 Unchanged, modifications in ch.aplu.util
V2.12 - Oct 2009 Unchanged, modifications in ch.aplu.util
V2.13 - Oct 2009 Unchanged, modifications in ch.aplu.util
V2.14 - Dec 2009 getX(), getY(), getPos() now reports correct values when
                 using a custom playground size and wrapping is on
V2.15 - Apr 2010 Unchanged, modifications in ch.aplu.util
V2.16 - Apr 2010 Fixed: wrong direction after leftCircle()/rightCircle()
V2.17 - Apr 2010 Unchanged, modifications in ch.aplu.util
V2.18 - Sep 2010 Unchanged, modifications in ch.aplu.util
V2.19 - Oct 2010 Unchanged, modifications in ch.aplu.util
V2.20 - Nov 2010 Unchanged, modifications in ch.aplu.util
V2.21 - Dec 2010 Unchanged, modifications in ch.aplu.util
V2.22 - Dec 2010 Added addStatusBar(), setStatusText(), showStatusBar()
                 in classes Turtle and TurtleFrame
V2.23 - Dec 2010 Added setStatusText(text, font, color)
                 in classes Turtle and TurtleFrame
V2.24 - Apr 2010 Unchanged, modifications in ch.aplu.util
V2.25 - Aug 2011 Unchanged, modifications in ch.aplu.util
V2.26 - Jan 2013 Added Turtle.fillToPoint(), Turtle.fillToHorizontal(),
                 Turtle.fillToVertical(), Turtle.fillOff()
                 Major revision of documentation
                 Compiled with Java Version 1.6
V2.27 - Feb 2013 Added Turtle.clean(Color color), Turtle.clear(Color color)
V2.28 - Feb 2013 Null-pointer error while rendering in multithreaded apps
V2.29 - Feb 2013 Added TurtleFrame.playgroundPressed()
V2.30 - Mar 2013 Added Turtle ctor taking color as string
                 Added Turtle.setColor() taking string
                 Added Turtle.setPen() taking string
                 Added Turtle.setFillColor() taking string
                 Added Turtle.clean() taking string
                 Added Turtle.clear() taking string
                 Added Turtle.toColor()
                 Added turtlegraphics.properties for defining defaults
                 Added TurtleFrame.addMouseListener()
                 Fixed: 1 pixel error in playground size 
                 Fixed: Wrong frame size after setResizable(false)
                 This is a ugly bug in Java on Windows machines
                 Workaround: setDecorated(false) before setResizable(false)
V2.31 - Mar 2013 Added: Check in every public method of class Turtle
                 if the frame is disposed and throw RuntimeException if this
                 is the case
                 Added: Turtle.dot(), Turtle.openDot()
V2.32 - Mar 2013 Added: X11 color names
V2.33 - Mar 2013 Added: Closing option RELEASE_ON_CLOSE
V2.34 - Mar 2013 Added: Turtle.dispose(), Turtle.isDisposed(), TurtleFrame.isDisposed()
V2.35 - Apr 2013 Unchanged, modifications in ch.aplu.util
V2.36 - May 2013 Added: Class Main and annotation NoMain
V2.37 - Jul 2013 Added: all methods returning java.awt.Color now return X11 color strings too
V2.38 - Jul 2013 Unchanged, modifications in ch.aplu.util
V2.39 - Jul 2013 Unchanged, modifications in ch.aplu.util
V2.40 - Aug 2013 Added: Turtle.fillToPoint() with current turtle coordinates
V2.41 - Sep 2013 Fixed: Hanging which Turtle.fill() called twice (same color)
V2.42 - Oct 2013 Added: Playground.drawLine(), Playground.drawImage()
V2.43 - Oct 2013 Added: class MouseTouchListener, TurtleFrame.addMouseTouchListener()
V2.44 - Nov 2013 Added: Key event support in class TurtleFrame
V2.45 - Nov 2013 Renamed: class MouseTouchListener to MouseHitListener
                 Added: class MouseHitXListener (ignoring consecutive callbacks)
                        and TurtleFrame.addMouseHitXListener
V2.46 - Nov 2013 Removed: Turtle.bong()
V2.47 - Nov 2013 Added in class Turte and TurtleFrame: setCursor()/setCustomCursor()
V2.48 - Nov 2013 Added: Custom Turtle image
V2.49 - Dec 2013 Fixed: loading images contained in _ prefixed directory of JAR now works
                 Added: Colors olive and teal in class X11Color
V2.50 - Dec 2013 Added: Turtle.startPath(), fillPath() to fill a polygone path
                 Added: Turtle.pushState(), popState() to save/restore the turtle state    
                 Added: Turtle.viewingSetPos(), viewingMoveTo() versions of setPos(), moveTo()
                        with translated/rotated coordinate system in turtle's viewing direction
V2.51 - Dec 2013 Added: Turtle.setRandomHeading()
                 Fixed: Turtle.stampTurtle() now takes custom image
                 Added: Turtle.stampTurtle(color)
V2.52 - Jan 2014 Modified: Turtle.getPixelColor()/getPixelColorStr() now
                           returns null, of Turtle is outside playground
V2.53 - Jan 2014 Added: Turtle.arc(), openArc(), pie(), openPie()
V2.54 - Jan 2014 Added: forward() now returns immediately, if speed approx. 0
V2.55 - Jan 2014 Fixed: Nullpointer when starting applets
V2.56 - Jan 2014 Added: Turtle.towards(otherTurtle)
V2.57 - Mar 2014 Added: Class Options to set library options on-the-fly
V2.58 - Apr 2013 Added: TurtleFrame.setClosingMode()
V2.59 - Apr 2013 Fixed: Turtle.startPath(), fillPath() now thread-safe
V3.00 - Jul 2014 Added: class PackageInfo
V3.01 - Jul 2014 Modified: Clear distinction between 
                    playground size (determines coordinate system span) and 
                    playground buffer size (determines number of pixels)
                    Playground ctors now take new definition of playground size 
                 Added: Playground.getBufferSize()
V3.02 - Jul 2014 Added: class GBitmap, Playground.drawImage(GBitmap bi, x, y)
V3.03 - Jul 2014 Fixed: fill() with turtle outside playground returns immediately
                 Added: Turtle.isInPlayground()
V3.04 - Aug 2014 Fixed: Turtle.enableRepaint() now works
V3.05 - Aug 2014 Unchanged, modifications in ch.aplu.util
V3.06 - Aug 2014 Unchanged, modifications in ch.aplu.util
V3.07 - Aug 2014 Added: Turtle.sound() to play a single tone
V3.08 - Nov 2014 Added: TurtleFrame.getLastFrame() to close it in Jython
V3.09 - Nov 2014 Added: Turtle.toString()
V3.10 - Nov 2014 Modified: Turtle image read from absolute path now using getCanonicalPath()   
V3.11 - Dec 2014 Unchanged, modifications in ch.aplu.util
V3.12 - Dec 2014 Added: Support for mouse double-clicks
V3.13 - Jan 2015 Fixed: Null-pointer in setDoubleClickDelay() on Mac OS
V3.14 - Jan 2015 Added: Class ExitListener and addExitListener() in Turtle, TurtleFrame
V3.15 - Feb 2015 Added: Methods to get font info in class Pen and Turtle
                 Fixed: fillPath() now redraws outline 
V3.16 - Feb 2015 Added: Class TurtleHitListener to get events when the turtle is hit
V3.17 - Feb 2015 Modified: compiled with Java 6 instead of Java 7 for downward compatibility
V3.18 - Apr 2015 Unchanged, modifications in ch.aplu.util
V3.19 - Jun 2015 Unchanged, modifications in ch.aplu.util
V3.20 - Jul 2015 Fixed: Turtle.clear() now no longer calls repaint()
                    when enableRepaint(false) is done
V3.21 - Jul 2015 Added: Turtle.drawImage() 
V3.22 - Jun 2015 Unchanged, modifications in ch.aplu.util
V3.23 - Sep 2015 Added: Turtle.setPenWidth(), getPenWidth()
V3.24 - Oct 2015 Added: Turtle.drawImage(imagePath)
V3.25 - Oct 2015 Added: Property UseSystemLookAndFeel in properties file 
                 (default: yes, as before; TigerJython request)
V3.26 - Jan 2016 Modified: setResizable() did not work, because of the
                 ugly workaround to use setUndecorated(). Calling it before
                 setVisible() works, but sporadically the frame size is too
                 big on slow computers. New workaround using insets and setSize().
                 Modified: Frame position for multiple frame instances

V3-27 - Feb 2016 Added: Turtle, Playground, TurtleFrame.drawBkImage()

For any help or suggestions send an e-mail to support@aplu.ch or post an article
to the forum at http://www.aplu.ch/forum.

Enjoy!

