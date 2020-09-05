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


/* History;
 * V1.36 - Sep 2004: First official release
 * V1.37 - Nov 2004: Unchanged, modifications in ch.aplu.util
 * V1.38 - Dec 2004: Unchanged, modifications in ch.aplu.util
 * V1.39 - Jan 2005: Unchanged, modifications in ch.aplu.util
 * V1.40 - Mar 2005: Add background color, TurtleKeyAdapter, TurtleArea
 * V1.41 - May 2005: User defined turtle shape, minor changes in doc and code style
 * V1.42 - Dec 2005: Unchanged, modifications in ch.aplu.util
 * V1.43 - Feb 2006: Bug removed: Turtle.turtleFrame was not initialized in all ctors of class Turtle V1.44 - Mar 2007: Bug removed: stampTurtle did not work properly in wrap mode
 * V1.45 - Aug 2007: TurtleKeyAdapter: use wait/notify to reduce CPU time (from 100% to 2%)
 * V1.46 - Aug 2007: synchronize(playground) for forward and rotate animation,
 *                   new method bong() using StreamingPlayer
 * V1.47 - Sept 2007: Unchanged, modifications in ch.aplu.util
 * V1.48 - Sept 2007: Unchanged, modifications in ch.aplu.util
 * V1.49 - Oct 2007: Unchanged, modifications in ch.aplu.util
 * V1.50 - Oct 2007: Unchanged, modifications in ch.aplu.util
 * V1.51 - Nov 2007: Fixed: correct position of label, when wrapping is on
 *                   Fixed: getPos() returns now the wrapped coordinates
 *                   Added: _getPos() returns unwrapped coordinates
 * V1.52 - Nov 2007: Added bean classes in order to use turtles with a Gui builder
 * V1.53 - Nov 2007: Added TurtlePane visual information when used in Gui builder design mode
 * V1.54 - Nov 2007: Minor changes to documentation
 * V1.55 - Dec 2007: Added property enableFocus to GPane, default: setFocusable(false)
 * V1.56 - Mar 2008: Unchanged, modifications in ch.aplu.util
 * V1.57 - Mar 2008: Unchanged, modifications in ch.aplu.util
 * V1.58 - Mar 2008: Modification to fill() (fill(x, y)):
 *                   region is defined with pixel color at current position
 *                   as opposed to background color
 * V1.59 - Oct 2008: Added ctors TurtleFrame with window position (ulx, uly)
 *                   Added Turtle.getPixelColor()
 * V2.00 - Nov 2008: Unchanged, modifications in ch.aplu.util
 *                   J2SE V1.4 no longer supported
 * V2.01 - Jan 2009: Unchanged, modifications in ch.aplu.util
 * V2.02 - Feb 2009  Turtle constructors run in EDT now
 *
 * V2.03 - Feb 2009  Unchanged, modifications in ch.aplu.util
 * V2.04 - Feb 2009  Unchanged, modifications in ch.aplu.util
 * V2.05 - Feb 2009  Unchanged, modifications in ch.aplu.util
 * V2.06 - Mar 2009  Unchanged, modifications in ch.aplu.util
 * V2.07 - Mar 2009  All except print methods synchronized, so Turtle package is
 *                   now thread-safe
 * V2.08 - Apr 2009  Unchanged, modifications in ch.aplu.util
 * V2.09 - Jun 2009  Unchanged, modifications in ch.aplu.util
 * V2.10 - Jun 2009  Unchanged, modifications in ch.aplu.util
 * V2.11 - Oct 2009  Unchanged, modifications in ch.aplu.util
 * V2.12 - Oct 2009  Unchanged, modifications in ch.aplu.util
 * V2.13 - Oct 2009  Unchanged, modifications in ch.aplu.util
 * V2.14 - Dec 2009  getX(), getY(), getPos() now reports correct values when
 *                   using a custom playground size and wrapping is on
 * V2.15 - Apr 2010 Unchanged, modifications in ch.aplu.util
 * V2.16 - Apr 2010 Fixed: wrong direction after leftCircle()/rightCircle()
 * V2.17 - Apr 2010 Unchanged, modifications in ch.aplu.util
 * V2.18 - Sep 2010 Unchanged, modifications in ch.aplu.util
 * V2.19 - Oct 2010 Unchanged, modifications in ch.aplu.util
 * V2.20 - Nov 2010 Unchanged, modifications in ch.aplu.util
 * V2.21 - Dec 2010 Unchanged, modifications in ch.aplu.util
 * V2.22 - Dec 2010 Added addStatusBar(), setStatusText(), showStatusBar()
 *                  in classes Turtle and TurtleFrame
 * V2.23 - Dec 2010 Added setStatusText(text, font, color)
 *                  in classes Turtle and TurtleFrame
 * V2.24 - Apr 2011 Unchanged, modifications in ch.aplu.util
 * V2.25 - Aug 2011 Unchanged, modifications in ch.aplu.util
 * V2.26 - Jan 2013 Added Turtle.fillToPoint(), Turtle.fillToHorizontal(),
 *                  Turtle.fillToVertical(), Turtle.fillOff()
 *                  Major revision of documentation
 * V2.27 - Feb 2013 Added Turtle.clean(Color color), Turtle.clear(Color color)
 * V2.28 - Feb 2013 Unchanged, modifications in ch.aplu.util
 * V2.29 - Feb 2013 Unchanged, modifications in ch.aplu.util
 * V2.30 - Mar 2013 Added Turtle ctor taking color as string
 *                  Added Turtle.setColor() taking string
 *                  Added Turtle.setPen() taking string
 *                  Added Turtle.setFillColor() taking string
 *                  Added Turtle.clean() taking string
 *                  Added Turtle.clear() taking string
 *                  Added Turtle.toColor()
 *                  Added turtlegraphics.properties for defining defaults
 *                  Added TurtleFrame.addMouseListener()
 *                  Fixed: 1 pixel error in playground size 
 *                  Fixed: Wrong frame size after setResizable(false)
 *                  This is a ugly bug in Java on Windows machines
 *                  Workaround: setDecorated(false) before setResizable(false)
 * V2.31 - Mar 2013 Added: Check in every public method of class Turtle
 *                  if the frame is disposed and throw RuntimeException if this
 *                  is the case
 *                  Added: Turtle.dot(), Turtle.openDot()
 * V2.32 - Mar 2013 Added: X11 color names
 * V2.33 - Mar 2013 Added: Closing option RELEASE_ON_CLOSE
 * V2.34 - Mar 2013 Added: Turtle.dispose(), Turtle.isDisposed(), TurtleFrame.isDisposed()
 * V2.35 - Apr 2013 Unchanged, modifications in ch.aplu.util
 * V2.36 - May 2013 Added: Class Main and annotation NoMain
 * V2.37 - Jul 2013 Added: all methods returning java.awt.Color now return X11 color strings too
 * V2.38 - Jul 2013 Unchanged, modifications in ch.aplu.util
 * V2.39 - Jul 2013 Unchanged, modifications in ch.aplu.util
 * V2.40 - Aug 2013 Added: Turtle.fillToPoint() with current turtle coordinates
 * V2.41 - Sep 2013 Fixed: Hanging which Turtle.fill() called twice (same color)
 * V2.42 - Oct 2013 Added: Playground.drawLine(), Playground.drawImage()
 * V2.43 - Oct 2013 Added: class MouseTouchListener, TurtleFrame.addMouseTouchListener()
 * V2.44 - Nov 2013 Added: Key event support in class TurtleFrame
 * V2.45 - Nov 2013 Renamed: class MouseTouchListener to MouseHitListener
 *                  Added: class MouseHitXListener (ignoring consecutive callbacks)
 *                         and TurtleFrame.addMouseHitXListener
 * V2.46 - Nov 2013 Removed: Turtle.bong()
 * V2.47 - Nov 2013 Added in class Turte and TurtleFrame: setCursor()/setCustomCursor()
 * V2.48 - Nov 2013 Added: Custom Turtle image
 * V2.49 - Dec 2013 Fixed: loading images contained in _ prefixed directory of JAR now works
 *                  Added: Colors olive and teal in class X11Color
 * V2.50 - Dec 2013 Added: Turtle.startPath(), fillPath() to fill a polygone path
 *                  Added: Turtle.pushState(), popState() to save/restore the turtle state    
 *                  Added: Turtle.viewingSetPos(), viewingMoveTo() versions of setPos(), moveTo()
 *                         with translated/rotated coordinate system in turtle's viewing direction
 * V2.51 - Dec 2013 Added: Turtle.setRandomHeading()
 *                  Fixed: Turtle.stampTurtle() now takes custom image
 *                  Added: Turtle.stampTurtle(color)
 * V2.52 - Jan 2014 Modified: Turtle.getPixelColor()/getPixelColorStr() now
 *                         returns null, of Turtle is outside playground
 * V2.53 - Jan 2014 Added: Turtle.leftArc(), rightArc()
 *                  Modified: Implementation of Turtle.leftCircle(), rightCircle()
 * V2.54 - Jan 2014 Added: forward() now returns immediately, if speed approx. 0
 * V2.55 - Jan 2014 Fixed: Nullpointer when starting applets
 * V2.56 - Jan 2014 Added: Turtle.towards(otherTurtle)
 * V2.57 - Mar 2014 Added: Class Options to set library options on-the-fly
 * V2.58 - Apr 2013 Added: TurtleFrame.setClosingMode()
 * V2.59 - Apr 2013 Fixed: Turtle.startPath(), fillPath() now thread-safe
 * V3.00 - Jul 2014 Added: class PackageInfo
 * V3.01 - Jul 2014 Modified: Clear distinction between 
 *                    playground size (determines coordinate system span) and 
 *                    playground buffer size (determines number of pixels)
 *                    Playground ctors now take new definition of playground size 
 *                  Added: Playground.getBufferSize()
 * V3.02 - Jul 2014 Added: class GBitmap, Playground.drawImage(GBitmap bi, x, y)
 * V3.03 - Jul 2014 Fixed: fill() with turtle outside playground returns immediately
 *                  Added: Turtle.isInPlayground()
 * V3.04 - Aug 2014 Fixed: Turtle.enableRepaint() now works
 * V3.05 - Aug 2014 Unchanged, modifications in ch.aplu.util
 * V3.06 - Aug 2014 Unchanged, modifications in ch.aplu.util
 * V3.07 - Aug 2014 Added: Turtle.sound() to play a single tone
 * V3.08 - Nov 2014 Added: TurtleFrame.getLastFrame() to close it in Jython
 * V3.09 - Nov 2014 Added: Turtle.toString()
 * V3.10 - Nov 2014 Modified: Turtle image read from absolute path now using getCanonicalPath()   
 * V3.11 - Dec 2014 Unchanged, modifications in ch.aplu.util
 * V3.12 - Dec 2014 Added: Support for mouse double-clicks
 * V3.13 - Jan 2015 Fixed: Null-pointer in setDoubleClickDelay() on Mac OS
 * V3.14 - Jan 2015 Added: Class ExitListener and addExitListener() in Turtle, TurtleFrame
 * V3.15 - Feb 2015 Added: Methods to get font info in class Pen and Turtle
 *                  Fixed: fillPath() now redraws outline 
 * V3.16 - Feb 2015 Added: Class TurtleHitListener to get events when the turtle is hit
 * V3.17 - Feb 2015 Modified: compiled with Java 1.6 instead of 1.7 for downward compatibility
 * V3.18 - Apr 2015 Fixed: Turtle.sound() now sets the volume correctly
 * V3.19 - May 2015 Modified: Turtle.hideTurtle() now sets speed(-1) for more than one turtle
 * V3.20 - Jul 2015 Fixed: Turtle.clear() now no longer calls repaint()
 *                  when enableRepaint(false) is done
 * V3.21 - Jul 2015 Added: Turtle.drawImage()
 * V3.22 - Sep 2015 Unchanged, modifications in ch.aplu.util
 * V3.23 - Sep 2015 Added: Turtle.setPenWidth(), getPenWidth()
 * V3.24 - Oct 2015 Added: Turtle.drawImage(imagePath)
 * V3.25 - Oct 2015 Added: Property UseSystemLookAndFeel in properties file 
 *                  (default: yes, as before; TigerJython request)
 * V3.26 - Jan 2016 Modified: setResizable() did not work, because of the
 *                  ugly workaround to use setUndecorated(). Calling it before
 *                  setVisible() works, but sporadically the frame size is too
 *                  big on slow computers. New workaround using insets and setSize().
 *                  Modified: Frame position for multiple frame instances
 * V3.27 - Feb 2016 Added: Turtle, Playground, TurtleFrame.drawBkImage()
 * V3.28 - Jun 2016 Fixed: Turtle.fill() (floodfill) draws point at (0,0)
 * V3.29 - Jul 2016 Modified: mouse hit event also generated by right mouse button
 *                  Added: TurtleFrame.getMouseHitButton() 
 *                  Modified: X11Color.toColorStr() now returns hex value "#rrggbb, 
 *                  if the given color is not part of the implemented X11 colors
 * V3.30 - Jul 2016 Added: Playground.save(), Turtle.savePlayground()
 * V3.31 - Aug 2016 Added: Turtle.direction(), 
                    Added: Turtle.distance(Turtle t)
 * V3.32 - Oct 2016 Added: Turtle.fill(Point2D.Double pt)
 * V3.33 - Dec 2016 Fixed: Turtle.dot(1) now draw a single pixel
                    Added: Turtle.spread()
 * V3.34 - Mar 2017 Added: TurtleFrame.setScreenLocation(), getScreenLocation(), getLastScreenLocation()
 */
package ch.aplu.turtle;

interface SharedConstants
{
  int DEBUG_LEVEL_OFF = 0;
  
  int DEBUG_LEVEL_LOW = 1;
  int DEBUG_LEVEL_MEDIUM = 2;
  int DEBUG_LEVEL_HIGH = 3;
  int DEBUG = DEBUG_LEVEL_OFF;
  String ABOUT =
    "2003-2016 Regula Hoefer-Isenegger, Aegidius Pluess\n" +
    "OpenSource Free Software\n" +
    "http://www.aplu.ch\n" +
    "All rights reserved";
  String VERSION = "3.34 - Dec 2017";
}
