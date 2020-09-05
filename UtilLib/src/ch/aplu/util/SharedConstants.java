// SharedConstants.java

/*
 This software is part of the JEX (Java Exemplarisch) Utility Library.
 It is Open Source Free Software, so you may
 - run the code for any purpose
 - study how the code works and adapt it to your needs
 - integrate all or parts of the code in your own programs
 - redistribute copies of the code
 - improve the code and release your improvements to the public
 However the use of the code is entirely your responsibility.
 */

/* History:
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
 V2.16 - April 2010:
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
 - InputStream replaced by URL because of crash under Java 1.7
 V2.26 - January 2013:
 - Added Console.show(), Console.hide()  
 - Compiled with Java Version 1.6
 V2.27 - February 2013:
 - Unchanged, modifications in ch.aplu.turtle
 V2.28 - February 2013:
 - Unchanged, modifications in ch.aplu.turtle
 V2.29 - February 2013:
 - Added Complex.hashCode() in accordances with Complex.equals()
 V2.30 - March 2013:
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
 V2.38 - July 2013:
  - Fixed: GPane, GPanel.rectangle(), arc() now draws correctly when
           coordinate axis are reversed
  - Added: GPane, GPanel.setEnableResize()
 V2.39 - July 2013:
  - Added: SoundPlayer, SoundPlayerExt ctors taking AudioInputStream
  - Added: SoundPlayer, SoundPlayerExt ctors taking ByteArrayInputStream
  - Added: SoundRecorder.writeWavFile() overloaded versions, returns boolean now
 V2.41 - Aug 2013
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
 V2.42 - Sep 2013
  - Modified: X11Color, restored original color name definitions
 V2.43 - Nov 2013
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
 V3.09 - Jun 2016
  - Added: Console.dispose()
 V3.10 - Jul 2016
  - Modified: X11Color.toColorStr() now returns hex value "#rrggbb, 
    if the given color is not part of the implemented X11 colors
 V3.11 - Sep 2016
  - Modified: HTMLPane, now loads local image files with relative path
 V3.12 - Dec 2016
  - Added: Console support for backspace character (deletes last character)
  - Modified Console: no beep when illegal char is entered
 V3.13 - Jan 2017
  - Added: GPane.getInstanceCount(), GPanel.getInstanceCount()
 V3.14 - Jan 2017
  - Modified: HtmlPane default title changed to "HTML Pane"
  - Added: HtmlPane.dispose(), disposeAll()
 V3.15 - Feb 2017
  - Added: Console.initStatics()
  - Added: Gpanel, GPane.getLineWidth()
 V3.16 - Feb 2017
  - Added: Gpanel, GPane.drawNode()
 V3.17 - Mar 2017
  - Modified: IntEntry, DoubleEntry, LongEntry: setValue(null) empties field
  - Modified: EntryItems: if setEnable(false) the mouse callbacks are also disabled
 V3.17 - Mar 2017
  - Added: GWindow.setScreenLocation(), getScreenLocation(), getLastScreenLocation()
 */
package ch.aplu.util;

interface SharedConstants
{
  int DEBUG_LEVEL_OFF = 0;
  int DEBUG_LEVEL_LOW = 1;
  int DEBUG_LEVEL_MEDIUM = 2;
  int DEBUG_LEVEL_HIGH = 3;
  int DEBUG = DEBUG_LEVEL_OFF;
  String ABOUT =
    "2003-2017 Aegidius Pluess\n"
    + "OpenSource Free Software\n"
    + "http://www.aplu.ch\n"
    + "All rights reserved";
  String VERSION = "3.18 - Mar 2017";
}
