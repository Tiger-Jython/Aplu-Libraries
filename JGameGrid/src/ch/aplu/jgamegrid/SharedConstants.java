// SharedConstants.java

/*
This software is part of the JGameGrid package.
It is Open Source Free Software, so you may
- run the code for any purpose
- study how the code works and adapt it to your needs
- integrate all or parts of the code in your own programs
- redistribute copies of the code
- improve the code and release your improvements to the public
However the use of the code is entirely your responsibility.

Author: Aegidius Pluess, www.aplu.ch
*/

/* History:
 * V1.00 - Jan 2010: - First official release, all basic features implemented
 * V1.01 - Jan 2010: - First official release, simulation loop improved
 * V1.02 - Jan 2010: - Remove all synchronized, cloning of vectors instead
 * V1.03 - Jan 2010: - All methods of GameGrid transfered to GGPane and
                       renamed to GameGrid, GGPane removed
 * V1.04 - Feb 2010: - setVisible() changed to show()/hide()
 * V1.05 - Mar 2010: - Changed: GGPaneBeanInfo to GameGridBeanInfo
                     - Minor modifications to JavaDoc
                     - Number of rotated sprites is user selectable, default 60
 * V1.06 - Apr 2010  - Added: GGJythonListener in order to use constructor registration of action() callback
                     - All Listener classes extend from java.util.EventListener now
 * V1.07 - Apr 2010  - Removed: addMouseListener() with default mask, due to
                       a performance penalty and missing events
 * V1.08 - May 2010  - Fixed: bug with Actor.isInGrid()
 * V1.09 - May 2010  - Added: Actor.getCurrentImage(), Actor.getPixelColor()
 * V1.10 - May 2010  - Added: Sprite or background images loaded from URL (server)
                     - Added: GGExceptionHandler to show uncaught exceptions in
                       a modal dialog box
                     - Modified: duplicating adding actors will cause error
                     - Modified: Invocating actor's method before adding it
                       to game grid will cause error or work
 * V1.11 - May 2010  - Fixed: but with Actor.turn() for negative arguments
 * V1.12 - May 2010  - Added: Default constructor Actor() with no sprite image
 * V1.13 - Jun 2010  - Modified: GameGrid.stopGameThread() called by another thread
                     - Added: GameGrid.getOneActor()
 * V1.14 - Jun 2010  - Modified: GameGrid.getActors()/getActorsAt() with class
                       literal: Actor is considered to have the type of its
                       superclasses too (including Object.class)
 * V1.15 - Jun 2010  - Removed static integers: Location.AHEAD, FULL_CIRCLE,
                       HALF_CIRCLE, HALF_LEFT, HALF_RIGHT, LEFT, RIGHT
                     - Added: Actor.getIntDirection()
 * V1.16 - Jun 2010  - More applications included
 * V1.17 - Jul 2010  - Added: classes GGButton, GGToggleButton, GGCheckButton,
                       GGRadioButton, GGRadioButtonGroup and correspondent
                       event listeners
                     - Added: class TextActor to show text dynamically
                     - Added: Actor constructors to create sprite images dynamically
                       from BufferedImage
                     - Fixed: Actor.turn() removed 'array out of bound' error
                     - Fixed: GGBackground getColor() returns Color.black if
                       point or location is outside game grid
                     - Modified: Location.getAdjacenLocation() also define for
                       negative distance (search in oposite direction)
                     - Modified: Order of calling: first collide notification and
                       then actor's act()
                     - Fixed: Actor constructor with multiple sprites and filename
                       without extension no more fails
 * V1.18 - Jul 2010  - Modified: Button event queue synchronized now, to prevent
                       race conditions
                     - GGCheckButton, GGRadioButton mouse active area restricted
                       to square/circle
 * V1.19 - Jul 2010  - Added: Actor.getPixelLocation()
                     - Added: interface GGButtonOverListener,
                     - Added: GGButton.addButtonOverListener()
                     - Added: GGToggleButton.addButtonOverListener()
                     - Added: GGButton.setHotspotArea()
                     - Added: GGToggleButton.setHostspotArea()
 * V1.20 - Jul 2010  - Modified: simulation loop increased precision
                     - Added: GGButton, default buttons with reverse colors
                     - Modified: All buttons: isInsideButton() public now
                     - Modified: GGButtonBase public now
                     - Added: GGButtonBase.setEnabled()
                     - Added: GGButtonBase: isInsideAnyButton()
                     - Modified: Actor.getWidth(), Actor.getHeight() now
                       returns size of orginally loaded image
                     - Modified/Added: GGBackground.fillCell() two versions:
                       the boundary lines are and are not considered to be
                       part of the cell
 * V1.21 - Aug 2010  - Added GameGrid.setLocation()
                     - Added Actor.rotate(), Actor.getRotatedPosition()
                     - Modified back to prior V1.17: Order of calling:
                       first actor's act() and the collide notification
                     - Added line to image collision
 * V1.22 - Aug 2010  - Fixed bug in window size, canvas fits frame now
 * V1.23 - Aug 2010  - Minor revision of documentation and applications/examples
 * V1.24 - Sep 2010  - Access to scene lists synchronized now, methods of
                       class GameGrid are thread-safe now
                     - Added class ch.aplu.util.SoundRecorder,
                     - Added class ch.aplu.util.SoundSampleListener
 * V1.25 - Sep 2010  - Critical Actor methods synchronized to make them thread-safe
 * V1.26 - Sep 2010  - All public methods of class Actor are synchronized
                     - GameGrid.refresh() synchronized (added refreshInternal())
 * V1.27 - Sep 2010  - All public methods of class GameGrid are synchronized
 * V1.28 - Oct 2010  - Added GameGrid.setUndecorated()
 * V1.29 - Oct 2010  - Modified GGBackground.fillRectangle() public now
                     - Added class GGWindowStateListener to report the
                       change of window position and iconification
                     - Modified GameGrid.setLocation() to GameGrid.setPosition()
                     - Added GameGrid.getPosition()
 * V1.30 - Oct 2010  - Removed synchronization of some public GameGrid methods
                       (may cause race conditions)
                     - Added GGWindowStateListener.activated(), deactivated()
                     - Added class GGWindowStateAdapater
                     - Added GameGrid.isUndecorated()
 * V1.31 - Oct 2010  - Modified some invokeAndWait() to invokeLater() due to
                       blocking problems
 * V1.32 - Oct 2010  - Removed GameGrid.setUndecorated() and moved feature to
                       GameGrid constructor (due to exception on certain machines)
 * V1.33 - Oct 2010  - Fixed non-caught exception when the Java exception handler is
                       modified (if using non-default GameGrid constructor for applets)
 * V1.34 - Nov 2010  - Added in class ch.aplu.util.ModelessOptionPane: provision
                       for integrating a button
                     - Added in class ch.aplu.util.Monitor: putSleep()/wakeUp()
                     - Added in package ch.aplu.util: class LoResAlarmTimer,
                       class LoResTimer
                     - Fixed GameGrid.getPosition() now returns correct value
 * V1.35 - Nov 2010  - Added GameGrid.activate()
 * V1.36 - Nov 2010  - Modifications to NxtSim package
 * V1.37 - Dec 2010  - Fixed bug in GameGrid.getPosition() when not run from EDT
                     - Added GameGrid.addStatusBar(), GameGrid.setStatusText(),
                       GameGrid.showStatusBar()
 * V1.38 - Dec 2010  - Status bar with black 1 pixel border
 * V1.39 - Dec 2010  - Status bar shows and hides the same as the game grid window
 * V1.40 - Dec 2010  - Added GameGrid.setStatusText(text, font, color)
 * V1.41 - Jan 2011  - Documentation revised.
 * V1.42 - Jan 2011  - Fixed bug in collision detection point-image
                     - Added GGMouse.getEventType()
                     - Added mouse-actor touch events:
                       class GGMouseTouchListener
                       Actor.addMouseTouchListener(),
                       Actor.setMouseTouchCircle()
                       Actor.setMouseTouchImage()
                       Actor.setMouseTouchRectangle()
                       Actor.setMouseTouchEnabled()
                     - Added Actor.setPixelLocation()
 * V1.43 - Jan 2011  - Added GameGrid.getMouseLocation()
 * V1.44 - Mar 2011  - Release resources when actor is garbage collected
                     - Added Actor.getNbSprites()
                     - Added Actor.getScaledImage()
                     - Added GameGrid.getPaintOrderList()
                     - Added Location.getDistanceTo()
                     - Added GameGrid.getDiagonalLocations()
                     - Allow scene modifications in touched area callbacks
                     - Added Gamegrid.shiftSceneOrder(), GameGrid.setSceneOrder().
                       GameGrid.reverseSceneOrder()
                     - Added GameGrid.addHand(), GameGrid.addHands()
                     - Added Actor.isRemoved()
                     - Added error exit info when actor's methods are called
                       while actor is not part of game grid
                     - Added GameGrid.setActorOnBottom(), Actor.setOnBottom()
                     - Added GameGrid.setColor(int r, int g, int b)
                     - Added GameGrid.setDoubleClickDelay(), GameGrid.getDoubleClickDelay()
                     - Added classes ToolBar, ToolBarListener, ToolBarAdapter,
                             ToolbarItem, ToolBarItemStack, ToolBarSeparator
 * V1.45 - Mar 2011  - Removed GGButtonBase system defined buttons (for efficiency reasons)
                     - Fixed null pointer exceptions because GGButtons and
                       touched actors interfered
                     - Added class GGButtonAdapter
                     - Defer adding status bar until the window is visible
                       because under Linux JFrame.getLocation() returns wrong values
                       until the window is shown
 * V1.46 - Mar 2011  - Changes to JCardGame
 * V1.47 - Mar 2011  - Modified GameGrid.addActor() now brings a removed actor back
                       by setting the sprite id to zero (no more hidden)
                     - Modified GGButtonBase.hide(), show() may now be
                       called before button is added to the game grid
 * V1.48 - Mar 2011  - All methods of status bar run in EDT now
 *                   - Added GGButtonBase.setRefreshEnabled()
 *                   - Added TextActor.getTextWidth(), getTextHeight()
 *                   - Implementation of GGButton of all kinds revisted, due
 *                     to conflicts with GGTouchListener (callbacks now run
 *                     in MouseEventThread)
 * V1.49 - Apr 2011  - Changes to JCardGame
 * V1.50 - Apr 2011  - Fatal errors now show a stack trace
 * V1.51 - Apr 2011  - Added GameGrid.isVisible()
 *                   - Changes to JCardGame
 * V1.52 - Apr 2011  - Modified GamgeGrid.isVisible() to isShown() because
 *                     of override conflict
 * V1.53 - Apr 2011  - Changes to JCardGame
 * V1.54 - Apr 2011  - Changes to JCardGame and ch.aplu.util.Console
 * V1.55 - May 2011  - Modified: Location.equals() now overrides Object.equals()
 * V1.56 - May 2011  - Modified: TextActor allows text of zero length now
 *                   - Fixed: Concurrency problems in setPaintOrder()
 * V1.57 - May 2011  - Recompilation of applications in distribution
 * V1.58 - Jun 2011  - GameGrid.monitor public now to synchronized concurrent
 *                     access of scene list
 *                   - Fixed: Nullpointer error in class Actor when checked for
 *                     actor on top and actor was removed
 * V1.59 - Jun 2011  - Modified: Part of simulation loop in class Gamegrid 
 *                     synchronized now
 *                   - Modified: GameGrid.addActor() no longer runs in EDT
 *                     (if Actor.act() calls addActor(), it runs in simulation
 *                      thread now that provides better synchronization)    
 *                   - Modified: GGNavigationListener and GGResetListener callbacks
 *                     return boolean now to indicate that the event is consumed
 *                   - GameGrid.setStatusText() now waits for deferred status bar
 * V2.00 - Jun 2011  - Modified: 'Run' button text changes even when GGNaviationListener
 *                     or GGResetListener callbacks consume event
 * V2.01 - Jun 2011  - Fixed: Mouse release events were not reported by the 
 *                     GGMouseTouchListener when the mouse was pressed 
 *                     pressed inside the touch area and dragged outside
 *                   - Modified: GGMouseTouchListener: Mouse drag events only 
 *                     reported if mouse was pressed inside touch area 
 *                   - Fixed: Faulty GameGrid.getTouchedActors() and as consequence
 *                     incorrect GGMouseTouchListener (for actors with 
 *                     non-zero location offset only)
 * V2.02 - Jun 2011  - Fixed: GameGrid.reset() now resets pixel location
 *                   - Added: class GGPanel with user definable double coordinate system
 *                   - Added: GameGrid.getLineLocations()
 * V2.03 - Aug 2011  - Modified: Actor.getX(), .getY() now reports error if
 *                     actor is (no more) part of the gamegrid
 *                     Added interface GGKeyRepeatListener, to report
 *                     repeating key events with given period without start delay
 *                     Added GameGrid.addKeyRepeatListener()
 *                     Added classes GGInputInt, GGInputLong, GGInputDouble, 
 *                     GGInputString for simple user input
 * V2.04 - Aug 2011  - Fixed: GGKeyRepeatListener now works when more than one
 *                     key is pressed at the same time
 *                   - Modified SoundPlayer, SoundPlayerExt constructors: 
 *                     InputStream replaced by URL
 * V2.05 - Sep 2011  - Added: GGRadioButtonGroup.getSelectedButtonId()
 *                   - Added: Actor.displace(), Actor.getDisplacePosition()
 *                     Actor.setDisplacePosition()
 * V2.06 - Oct 2011  - Modified: GameGrid.refresh() now calls act() directly.
 *                     Rendering now using synchronized contentsRestored(), 
 *                     contentsLost() loop
 * V2.08 - Jan 2012  - Modified: class GGKinect moved to application examples
 * V2.09 - Jan 2012  - Fixed: several drawing methods in GGPanel public now
 * V2.10 - Mar 2012  - Fixed: GameGrid.toLocation() for negative coordinates
 *                     now returns locations outside grid
 *                   - Added: class GGTextField 
 *                   - Added: class GGBitmap
 *                   - Added GameGrid.playSound(), GameGrid.playLoop() 
 *                     without object now
 * V2.11 - Mar 2012  - Added: class GGProperties
 *                   - Modified: GGTextField with enableRefresh parameter now
 * V2.12 - Apr 2012  - Modified: GameGrid.checkActorCollision(), checkTileCollision()
 *                     thread-safe now
 *                   - Added: GGTextField.getTextWidth(), getTextHeight()
 * V2.13 - Apr 2012  - Modifications to JCardGame
 * V2.14 - May 2012  - Fixed GGBackground.setLineWidth() now works correctly
 * V2.15 - May 2012  - Fixed bug after removeAllActors() actors remained hidden
 *                   - Fixed: GGBackground fill methods now draw outline correctly
 *                   - Added: GGBitmap Graphics2D drawing methods
 *                   - Added: class GGProgressBar
 * V2.16 - May 2012  - Added: GGTextField.setLocationOffset()
 *                   - Modified: In class GGProgressBar internal TextActor 
 *                     subclass now put on top of paint order
 *                   - Modified: In class GGTextField internal TextActor 
 *                     subclass now
 * V2.17 - Jul 2012  - Added GGProgressBar.setValue(double value, boolean doRefresh)
 *                   - Added GGProgressBar.setFont()
 *                   - Added GGProgressBar.remove()
 *                   - Modified: methods in GGTextField, GGProgressBar synchronized now
 *                   - Fixed: GGProgressBar.setValue() not flickering anymore
 *                   - Added: GGBackground.getBackgroundImage()
 *                   - Added: GameGrid.getImage()
 *                   - Added: GGBitmap.getSupportedImageFormats()
 *                   - Added: GGBitmap.writeImage()
 *                   - Added: class GGPath
 *                   - Modified: Hand.transfer() synchronized now
 *                   - Fixed: MouseTouchListener now reports drag events also
 *                     when press event is not registered
 *                   - Modified: synchronized GameGrid.getActor(), getActorAt(),
 *                     getNbActors()
 * V2.18 - Aug 2012  - Modified: Actor.isVisible() now returns false, if the actor
 *                     is not part of the game grid
 *                   - Added: final static Color GameGrid.WHITE = Color.white etc.
 *                   - Added: GGBitmap.floodFill()
 * V2.19 - Aug 2012  - Changes to JCardGame only
 * V2.20 - Sep 2012  - Modified: Semi-transparency support for all images
 *                   - Added: GGBitmap.setTransparency() to change transparency dynamically
 *                   - Removed: GGTextField.setRefreshEnabled()
 *                   - Modified: Refresh disabled while updating GGTextField
 * V2.21 - Sep 2012  - Fixed: GGBackground.clear() now uses line width 1 even
 *                     when line width has been set to another value
 *                   - Fixed: Exception caught in act() when sceen list is
 *                     outdated
 * V2.22 - Sep 2012  - Modified: GameGrid.removeAllActors(), removeActors(), removeActorsAt()
 *                     synchronized now
 *                   - Modified: Removing actor now maintains global gameGrid ref
 * V2.23 - Oct 2012: - Added ch.aplu.util.Console.show(), Console.hide()   
 *                   - Modified: GameGrid.getActors(), getActorsAt() now return 
 *                     list sorted by paint order
 *                   - Modified GameGrid.getActors(), getActorsAt(), 
 *                     getOneActor(), getOneActorAt synchronized now
 *                   - Modified GameGrid.getOneActor(), getOneActorAt() now 
 *                     return actor at top in paint order
 * V2.24 - Oct 2012: - Added static color GameGrid.TRANSPARENT
 *                   - Fixed: GGBitmap.writeImage() now ignores alpha component
 *                   - Added: GGPanel.drawText()  
 * V2.25 - Oct 2012  - Modified: GameGrid.isTileColliding() returns if
 *                     TileMap not yet created
 *                   - Modified: in class GGTextField do not temporarily inhibit
 *                     refresh() in setText() any more
 *                   - Fixed: Rotatable actor creation with same sprite image
 *                     as existing non-rotatable actor now works
 * V2.26 - Dec 2012  - Fixed: GGPath.createDirectoryTree(File f) now returns
 *                     false, if f is an existing file (not a directory) 
 * V2.27 - Jan 2013  - Modified: GGLine.isIntersecting(GGVector v) now checks with
 *                     a floating point error interval
 * V2.28 - Feb 2013  - Modified: Actor.setActorCollisionEnabled(false) now inhibits check
 *                     of collisions (performance boost only)
 *                   - Added: Actor.setTileCollisionEnabled() 
 *                   - Added: Location.hashCode() in accordance with equals()
 *                   - Added: Complex.hashCode() in accordance with equals()
 * V2.29 - Mar 2013  - Fixed: StatusBar now displays lower border
 *                   - Fixed: GGPath does not show debug messages anymore
 *                   - Fixed: Error in background color initialization
 *                   - Added: class X11Color
 *                   - Added: enum GameGrid.ClosingMode, GameGrid.setClosingMode()
 *                   - Added: GameGrid.isDisposed()
 *                   - Removed: interface GGExceptinHandler
 *                   - Removed: GameGrid.addExceptionHandler
 *                   - Modified: GameGrid.getRandomEmptyLocation() improved algorithm
 *                   - Added: GameGrid.dispose()
 *                   - Modified GameGrid.addMouseListener() now called in EDT
 * V2.30 - Apr 2013  - New distribution because of JCardGame and applications update
 * V2.31 - Apr 2013  - New distribution because of update of util packages
 * V2.32 - May 2013  - Added: Class Main and annotation NoMain
 *                   - Modified: userhome now takes Windows environment variable %USERPROFILE%
 * V2.33 - May 2013  - Removed: System.out in GameGrid.dispose()
 * V2.34 - Jul 2013  - Added search for sprite and background images in _ prefixed path in jar
 *                   - Added ctor Actor(BufferedImage bi)
 *                   - Added ctor GameGrid(int nbHorzCells, int nbVertCells)
 *                   - Added GGPanel.image()
 * V2.35 - Jul 2013  - Fixed: Wait for pause state when doPause() or 
 *                     reset is called 
 * V2.36 - Dec 2013  - Modified: class X11Color modified and moved to package ch.aplu.util
 *                     Updates in package ch.aplu.util
 *                   - Fixed: loading images contained in _ prefixed directory of JAR now works
 *                   - Added: GGBackground.drawImage() with imagePath 
 *                   - Added: Actor.setLocationOffset(x, y)
 *                   - Modified: Actor.reset() is now called before the act cycling starts
 * V2.37 - Jul 2014  - Modified: Documentation
 *                   - Added: GameGrid.getKeyWait(), getKeyCodeWait()
 *                   - Modified: GameGrid.removeActor(actor) returns false, if actor = null
 *                   - Added: GGLine.getDividingPoint()
 *                   - Modified: GGBackground, GameGrid: alpha channel ignored in background color
 *                   - Added: class PackageInfo
 * V2.38 - Aug 2014  - Updated: RobotSim
 * V2.39 - Aug 2014  - Fixed: GGPath.pack() now clears static fileList
 * V2.40 - Nov 2014  - Modified: GGBitmap image read from absolute path now using getCanonicalPath()   
 * V2.41 - Dec 2014  - Fixed: doPause() called when reset is pressed before invoking reset callback
 * V2.42 - Jan 2015  - Modified: Tools.delay() in RobotSim based on HiResAlarmTimer
 * V2.43 - Oct 2015  - Added: RaspiSim
 *                   - Fixed: Erratic behavior in robot simulation movement
 *                   - Modified: Tools.delay() based on simulation period
 * V2.44 - Oct 2015  - Added: Property UseSystemLookAndFeel in properties file 
 *                     (default: yes, as before; TigerJython request)
 * V2.45 - Jan 2016  - Added: Actor.addCollisionActors() with Actor[]
 *                   - Added: Actor.addCollisionTiles() with Location[]
 * V2.46 - Jan 2016  - Modified: GameGrid.setStatusText() now takes the last
 *                     call in deferred mode
 */

package ch.aplu.jgamegrid;  

interface SharedConstants
{
  int DEBUG_LEVEL_OFF = 0;
  int DEBUG_LEVEL_LOW = 1;
  int DEBUG_LEVEL_MEDIUM = 2;
  int DEBUG_LEVEL_HIGH = 3;

  int DEBUG = DEBUG_LEVEL_OFF;
  
  boolean propertyVerbose = false;

  String ABOUT =
    "2003-2016 Aegidius Pluess\n" +
    "OpenSource Free Software\n" +
    "http://www.aplu.ch\n" +
    "All rights reserved";
  String VERSION = "2.46 - Jan 2016";
}
