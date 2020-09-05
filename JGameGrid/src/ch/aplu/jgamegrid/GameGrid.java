// GameGrid.java

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
/* Data structure for storing the actors when added:
 * Every actor's class reference has an entry in a Vector<Class> called classList. The
 * actors references of the same class are contained in an Vector<Actor> called scene. All
 * scene references contained in a Vector<<Vector<Actor>> called sceneList.
 * The calling sequence for act() and rendering for the class bundles is given by
 * the lists of integers actOrder and paintOrder: they contain the priority sequence of indices of
 * the entries in the classList. New class indices are appended at the end of the lists.
 * The act() methods of each actor are called bundled by class in the
 * reverse order of the actOrder (later entries are called first). The rendering is done
 * in the order of the paintOrder (later entries are called last and will appear on the
 * top of other images).
 * All actors in the same class will act in the reverse order of
 * their entry in the scene (later entries first) and will be rendered in the order
 * of the scene (later entries on top of other)
 */
package ch.aplu.jgamegrid;

import ch.aplu.util.*;
import java.util.*;
import java.awt.event.*;
import java.awt.image.BufferStrategy;
import javax.swing.*;
import java.awt.*;
import java.io.*;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.image.BufferedImage;
import java.net.URL;
import javax.imageio.ImageIO;

/**
 * Class to create a container where the actors live in. It is a two dimensional
 * grid of cells. The size of cells can be specified (in pixel units = distance
 * between two adjacent pixels) at GrameGrid's creation time,
 * and is constant after creation. Simple scenarios may use large cells that
 * entirely contain the representations of objects in a single cell.
 * More elaborate scenarios may use smaller cells (down to a single pixel unit)
 * to achieve fine-grained placement and smoother animation.
 * The background of the game grid can be decorated with drawings or a background
 * image.<br><br>
 *
 * The default constructor (parameterless) is used to create a canvas that can
 * be embedded in your own frame window or applet. You may use a GUI builder
 * because GameGrid is a Java bean. All other constructors embed the canvas
 * in a simple frame window with or without a navigation bar containing 3 buttons
 * (Run, Step, Reset) and a slider to select the simulation period.<br><br>
 *
 * GameGrid is derived from Canvas (a heavy weight component based on AWT) 
 * in order to use hardware based rendering enhancement (page flipping). 
 * In principle do not mix Swing and AWT components. For example you should 
 * not add a GameGrid in a JScrollPane (use the AWT container ScrollPane instead).
 * (A GameGrid may be added to the content pane of a JFrame without harm.)<br><br> 
 * 
 * The cellSize is given in pixel units, e.g. the distance between adjacent pixels.
 * The size in pixel units of the usable playground is width = nbHorzCells * cellSize horizontally and
 * height = nbVertCells * cellSize vertically. Thus the playground contains nbHorzPixels = width + 1 pixels horizontally and
 * nbVertPixels = height + 1 pixels vertically, with pixel indexes 0 <= i <= width (inclusive),
 * 0 <= k <= height (inclusive) respectively.<br><br>
 *
 * For pixel accurate positioning be aware that an image that has the size m x n in pixel units
 * contains m+1 pixels horizontally and n+1 pixels vertically.
 * So the background image must have nbVertPixels horizontally and
 * nbVerticalPixels vertically to fit exactly into the playground.
 * (E.g.  GameGrid(60, 50, 10) will need a background image with 601 x 501 pixels.) <br><br>
 *
 * To increase performance the automatic repainting of the graphics
 * canvas is disabled. Repainting is done in every simulation cycle, when
 * the window is moved or by calling refresh(). setWindowListener()
 * registers a window listener that calls refresh() when the window is activated.
 *
 * The x- and y-coordinates for positioning actors are actually the cell index ranging from
 * 0 <= x < nbHorzCells (exclusive) horizontally and 0 <= y < nbVertCells (exclusive) vertically.
 * There image is automatically centered in the cell as accurate as possible.<br><br>
 *
 *  The class design is inspired by the great Greenfoot programming environment
 * (see www.greenfoot.org) and by an article about game programming in Java
 * at www.cokeandcode.com/tutorials with thanks to the authors. The code
 * is entirely rewritten and in my responsability.<br><br>
 *
 * A extended sound library is included that supports MP3.<br><br>
 * 
 * Using a sound converter integrated in the native DLL soundtouch.dll
 * (source from www.surina.net/soundtouch)
 * you may change pitch and tempo independently.
 * For the compilation you need  the package ch.aplu.jaw and the
 * native DLL soundtouch.dll must be found in the system path. It only  works
 * on Windows machines.<br><br>
 *
 * If a non-default constructor of GameGrid is used, all uncaught exceptions
 * are handled by a GGExceptionHandler that shows the
 * stack trace in a modal dialog box and terminates the application. This is
 * useful for GUI-based applications where no console window is visible.
 * To change this behavior, register a null GGExceptionHandler,
 * override getStackTrace() or register your own GGExceptionHandler.
 * For applets the Java standard exception handler is not modified because
 * this would cause a security exception.<br><br>
 * 
 * Key strokes may be reported by a GGKeyListener or by polling the keyboard using
 * isKeyPressed() or kbhit(). Be aware that the game grid window must have the
 * focus so that the keys are active. You may deny to give the focus to other
 * components (e.g. to a text area or a second game grid in a GUI application)
 * by calling setFocusable(false).<br><br>
 *
 * Methods that access/modify the internal data structure
 * are made thread-safe. This does not mean that ALL methods are thread-safe.<br><br>
 *
 * Some library defaults of the GameGrid window can be modified by using a 
 * Java properties file gamegrid.properties. For more details
 * consult gamegrid.properties file found in the distribution.<br><br>
 * 
 * When the close button of the GameGrid window title bar is hit, System.exit(0)
 * is executed that terminates the JVM, but you can modifiy this behavior
 * by registering your own implementation of the ExitListener interface or by using
 * the key ClosingMode in gamegrid.properties.<br><br>
 * 
 * All Swing methods are executed by the Event Dispatch Thread (EDT), so
 * all methods may be executed directly from any thread.
 * 
 */
public class GameGrid extends Canvas implements GGActListener
{
  /**
   * Modes to determine what happens when the title bar close button is hit.
   */
  public static enum ClosingMode
  {
    /**
     * Terminates application by calling System.exit(0) (default).
     */
    TerminateOnClose,
    /**
     * Shows confirmation dialog before terminating application by 
     * calling System.exit(0).
     */
    AskOnClose,
    /**
     * Stops game thread, hides window and releases system resources.
     */
    DisposeOnClose,
    /**
     * Disables close button action.
     */
    NothingOnClose
  }

  // --------------- Inner class MyUncaughtExceptionHandler ----
  private class MyUncaughtExceptionHandler
    implements Thread.UncaughtExceptionHandler
  {
    public void uncaughtException(Thread t, Throwable e)
    {
      ByteArrayOutputStream baos = new ByteArrayOutputStream();
      PrintStream ps = new PrintStream(baos);
      e.printStackTrace(ps);
      String content = "";
      try
      {
        content = baos.toString("ISO-8859-1");
      }
      catch (UnsupportedEncodingException ex)
      {
      }
      createMessageDialog(null, "JGameGrid Fatal Error", content);
    }
  }
  // --------------- Inner class MyTimerActionListener ---------

  private class MyTimerActionListener implements ActionListener
  {
    public void actionPerformed(ActionEvent e)
    {
      doubleClickTimer.stop();
      if (mouseAdapter != null)
        mouseAdapter.notifyClick(lastMouseEvent);
    }
  }

  // --------------- Inner class KeyRepeatHandler -------------
  private class KeyRepeatHandler extends Thread implements GGKeyListener
  {
    /* 
     Concept:
     - There is only one KeyRepeatHandler created when the first
     GGKeyRepeatListener is registered
     - The internal thread is started looping with given keyRepeatPeriod
     - Each registered GGKeyRepeatListener is put into a vector keyRepeatListeners
     - When a key is pressed, its keyCode is put into a vector pressedKeyCodes
     (when the key is repeated, nothing happens). 
     - When the key is released, its keyCode is removed from pressedKeyCodes
     - The thread loop sends ALL keyCodes of pressed keys to ALL registerd
     GGKeyRepeatListeners (invokes callback keyRepeated(int keyCode))
     */
    private Vector<Integer> pressedKeyCodes = new Vector<Integer>();
    private boolean isRepeatThreadRunning = false;

    public ArrayList<Integer> getPressedKeyCodes()
    {
      ArrayList<Integer> list = new ArrayList<Integer>();
      for (Integer code : pressedKeyCodes)
        list.add(code);
      return list;
    }

    public void startThread()
    {
      if (!isRepeatThreadRunning)
      {
        isRepeatThreadRunning = true;
        start();
      }
    }

    public void stopThread()
    {
      isRepeatThreadRunning = false;
      interrupt();
    }

    public boolean keyPressed(KeyEvent evt)
    {
      Integer code = evt.getKeyCode();
      if (!pressedKeyCodes.contains(code))
      {
        pressedKeyCodes.add(evt.getKeyCode());
        interrupt();  // to ensure at least one event callback when a key 
        // is hit and released shortly after
//        System.out.println("added code: " + evt.getKeyCode());
      }
      return false;  // Not consumed
    }

    public boolean keyReleased(KeyEvent evt)
    {
      Integer code = evt.getKeyCode();
      pressedKeyCodes.remove(code);
      //     System.out.println("removed code: " + evt.getKeyCode());
      return false;  // Not consumed
    }

    public void run()
    {
      while (isRepeatThreadRunning)
      {
        EventQueue.invokeLater(new Runnable()
        {
          public void run()
          {
            for (GGKeyRepeatListener listener : keyRepeatListeners)
            {
              for (Integer v : pressedKeyCodes)
              {
                listener.keyRepeated(v);
              }
            }
          }
        });

        try
        {
          Thread.sleep(keyRepeatPeriod);
        }
        catch (InterruptedException ex)
        {
        }
      }
    }
  }

  // --------------- Inner class MyMouseAdapter ----------------
  private class MyMouseAdapter implements MouseListener, MouseMotionListener
  {
    public void mousePressed(MouseEvent evt)
    {
      int modifiers = evt.getModifiers();
      if ((modifiers & InputEvent.BUTTON1_MASK) != 0)
        notifyMouseEvent(evt, GGMouse.lPress);
      if ((modifiers & InputEvent.BUTTON2_MASK) != 0
        || (modifiers & InputEvent.BUTTON3_MASK) != 0)
        notifyMouseEvent(evt, GGMouse.rPress);
    }

    public void mouseReleased(MouseEvent evt)
    {
      int modifiers = evt.getModifiers();
      if ((modifiers & InputEvent.BUTTON1_MASK) != 0)
        notifyMouseEvent(evt, GGMouse.lRelease);
      if ((modifiers & InputEvent.BUTTON2_MASK) != 0
        || (modifiers & InputEvent.BUTTON3_MASK) != 0)
        notifyMouseEvent(evt, GGMouse.rRelease);
    }

    public void mouseDragged(MouseEvent evt)
    {
      int modifiers = evt.getModifiers();
      if ((modifiers & InputEvent.BUTTON1_MASK) != 0)
        notifyMouseEvent(evt, GGMouse.lDrag);
      if ((modifiers & InputEvent.BUTTON2_MASK) != 0
        || (modifiers & InputEvent.BUTTON3_MASK) != 0)
        notifyMouseEvent(evt, GGMouse.rDrag);
    }

    public void mouseClicked(MouseEvent evt)
    {
      if (evt.getClickCount() > 2)
        return;

      if (doubleClickTime == 0)
      {
        if (evt.getClickCount() == 1)
          notifyClick(evt);
        if (evt.getClickCount() == 2)
          notifyDoubleClick(evt);
      }
      else
      {
        lastMouseEvent = evt;
        if (doubleClickTimer.isRunning())
        {
          doubleClickTimer.stop();
          notifyDoubleClick(evt);
        }
        else
          doubleClickTimer.restart();
      }
    }

    public void mouseEntered(MouseEvent evt)
    {
      notifyMouseEvent(evt, GGMouse.enter);
    }

    public void mouseExited(MouseEvent evt)
    {
      notifyMouseEvent(evt, GGMouse.leave);
    }

    public void mouseMoved(MouseEvent evt)
    {
      notifyMouseEvent(evt, GGMouse.move);
    }

    private void notifyClick(MouseEvent evt)
    {
      int modifiers = evt.getModifiers();
      if ((modifiers & InputEvent.BUTTON1_MASK) != 0)
        notifyMouseEvent(evt, GGMouse.lClick);
      if ((modifiers & InputEvent.BUTTON2_MASK) != 0
        || (modifiers & InputEvent.BUTTON3_MASK) != 0)
        notifyMouseEvent(evt, GGMouse.rClick);
    }

    private void notifyDoubleClick(MouseEvent evt)
    {
      int modifiers = evt.getModifiers();
      if ((modifiers & InputEvent.BUTTON1_MASK) != 0)
        notifyMouseEvent(evt, GGMouse.lDClick);
      if ((modifiers & InputEvent.BUTTON2_MASK) != 0
        || (modifiers & InputEvent.BUTTON3_MASK) != 0)
        notifyMouseEvent(evt, GGMouse.rDClick);
    }

    private void notifyMouseEvent(MouseEvent evt, int mask)
    {
      if (!isMouseEnabled)
        return;
      // Clone vector to prevent conflicts when callback modifies mouseListeners
      Vector<GGMouseListener> tmp;
      Vector<Integer> tmp1;
      synchronized (monitor)
      {
        tmp = new Vector<GGMouseListener>(mouseListeners);
        tmp1 = new Vector<Integer>(mouseEventMasks);
      }
      for (int i = 0; i < tmp.size(); i++)
      {
        GGMouseListener listener = tmp.get(i);
        int mouseEventMask = tmp1.get(i);
        if ((mouseEventMask & mask) != 0)
        {
          GGMouse mouse = GGMouse.create(listener, mask, evt.getX(), evt.getY());
          if (listener.mouseEvent(mouse))
            return;
        }
      }
    }
  }

// --------------- Inner class MyKeyAdapter ----------------
  private class MyKeyAdapter implements KeyListener
  {
    public void keyPressed(KeyEvent evt)
    {
      keyCode = evt.getKeyCode();
      keyChar = evt.getKeyChar();
      keyModifiers = evt.getModifiers();
      keyModifiersText = KeyEvent.getKeyModifiersText(keyModifiers);
      gotKey = true;
      keyCodePressed = evt.getKeyCode();
      // Clone vector to prevent conflicts when callback modifies keyListeners
      Vector<GGKeyListener> tmp = new Vector<GGKeyListener>(keyListeners);
      for (GGKeyListener kal : tmp)
      {
        if (kal.keyPressed(evt))
          return;
      }
    }

    public void keyReleased(KeyEvent evt)
    {
      keyCodePressed = Integer.MIN_VALUE;
      // Clone vector to prevent conflicts when callback modifies keyListeners
      Vector<GGKeyListener> tmp = new Vector<GGKeyListener>(keyListeners);
      for (GGKeyListener kal : tmp)
      {
        if (kal.keyReleased(evt))
          return;
      }

    }

    public void keyTyped(KeyEvent evt)
    {
    }
  }

// --------------- Inner class GameThread ------------------
  private class GameThread extends Thread
  {
    public void run()
    {
      while (isGameThreadRunning)
      {
        long startTime;
        long offset = 1000000;  // Increases precision of period
        if (isRunning)
        {
          startTime = System.nanoTime();
          if (isActEnabled)
            actAll();
          if (simulationPeriodNanos > 0)
          {
            while (System.nanoTime() - startTime < (simulationPeriodNanos - offset) && !isSingleStep)
              delay(1);
          }
          Thread.yield();
          if (isSingleStep)
          {
            isRunning = false;
            isSingleStep = false;
          }
        }
        else  // Paused
        {
          isPaused = true;
          GameGrid.delay(10);
        }
      }
    }
  }
// --------------- End of inner classes --------------------
//
  /**
   * ClosingMode TerminateOnClose.
   */
  public static final ClosingMode TerminateOnClose = ClosingMode.TerminateOnClose;
  /**
   * ClosingMode AskOnClose.
   */
  public static final ClosingMode AskOnClose = ClosingMode.AskOnClose;
  /**
   * ClosingMode DisposeOnClose.
   */
  public static final ClosingMode DisposeOnClose = ClosingMode.DisposeOnClose;
  /**
   * ClosingMode NothingOnClose.
   */
  public static final ClosingMode NothingOnClose = ClosingMode.NothingOnClose;
  /**
   * Short for transparent color Color(0, 0, 0, 0) (black with alpha = 0).
   */
  public static final Color TRANSPARENT = new Color(0, 0, 0, 0);
  /**
   * Short for Color.BLACK.
   */
  public static final Color BLACK = Color.BLACK;
  /**
   * Short for Color.BLUE.
   */
  public static final Color BLUE = Color.BLUE;
  /**
   * Short for Color.CYAN.
   */
  public static final Color CYAN = Color.CYAN;
  /**
   * Short for Color.DKGRAY.
   */
  public static final Color DARKGRAY = Color.darkGray;
  /**
   * Short for Color.GRAY.
   */
  public static final Color GRAY = Color.GRAY;
  /**
   * Short for Color.GREEN.
   */
  public static final Color GREEN = Color.GREEN;
  /**
   * Short for Color.LTGRAY.
   */
  public static final Color LIGHTGRAY = Color.lightGray;
  /**
   * Short for Color.MAGENTY.
   */
  public static final Color MAGENTA = Color.MAGENTA;
  /**
   * Short for Color.RED.
   */
  public static final Color PINK = Color.PINK;
  /**
   * Short for Color.RED.
   */
  public static final Color RED = Color.RED;
  /**
   * Short for Color.WHITE.
   */
  public static final Color WHITE = Color.WHITE;
  /**
   * Short for Color.YELLOW.
   */
  public static final Color YELLOW = Color.YELLOW;
  /**
   * Color of the grid.
   * Default: Color.red
   * Property for bean support.
   */
  public Color gridColor = Color.red;
  //
  /**
   * Number of horizonal cells.
   * Default: 10
   * Property for bean support.
   */
  public int nbHorzCells = 10;
  //
  /**
   * Number of vertical cells.
   * Default: 10
   * Property for bean support.
   */
  public int nbVertCells = 10;
  //
  /**
   * Size of cells in pixels (square).
   * Default: 20
   * Property for bean support.
   */
  public int cellSize = 20;
  //
  /**
   * Path of the background image.
   * Default: null
   * Property for bean support.
   */
  public String bgImagePath = null;
  //
  /**
   * ULX of the background image with respect to playground.
   * Default: 0
   * Property for bean support.
   */
  public int bgImagePosX = 0;
  //
  /**
   * ULY of the background image with respect to playground.
   * Default: 0
   * Property for bean support.
   */
  public int bgImagePosY = 0;
  //
  /**
   * The color of the background.
   * Default: black
   * Property for bean support.
   */
  public Color bgColor = Color.black;
  //
  /**
   * Simulation period (in ms).
   * Default: 200
   * Property for bean support.
   */
  public int simulationPeriod = 200; // ms
  //
  /**
   * Number of rotated sprite images.
   * Default: 60
   * Property for bean support.
   */
  public static int nbRotSprites = 60;
  //
  /** Object used for synchronizing access of methods accessing
   * the sceen list.
   */
  public final static Object monitor = new Object();
  private static final long serialVersionUID = 24362462L;
  private boolean isRefreshEnabled = true;
  private long simulationPeriodNanos = simulationPeriod * 1000000;
  private Vector<Vector<Actor>> sceneList = new Vector<Vector<Actor>>();
  private Vector<Class> classList = new Vector<Class>();
  private Vector<Integer> actOrder = new Vector<Integer>();
  private Vector<Integer> paintOrder = new Vector<Integer>();
  private GGPanel gPanel;
  private volatile boolean isGameThreadRunning = true;
  private volatile boolean isRunning = false;
  private volatile boolean isPaused = false;
  private volatile boolean isSingleStep = false;
  private volatile boolean isActEnabled = true;
  private volatile boolean gotKey = false;
  private BufferStrategy strategy;
  private GameThread gameThread;
  private GGTileMap tileMap = null;
  private int pgWidth; // width of playground in pixel units
  private int pgHeight;  // width of playground in pixel units
  private int nbHorzPix; // number of horizontal pixels involved
  private int nbVertPix; // number of vertical pixels involved
  private char keyChar;
  private int keyCode;
  private int keyModifiers;
  private String keyModifiersText;
  private Vector<GGKeyListener> keyListeners = new Vector<GGKeyListener>();
  private Vector<GGMouseListener> mouseListeners = new Vector<GGMouseListener>();
  private ArrayList<Integer> mouseEventMasks = new ArrayList<Integer>();
  private boolean isMouseListenerAdded = false;
  private boolean isMouseMotionListenerAdded = false;
  private boolean isMouseEnabled;
  private Vector<GGActListener> actListeners = new Vector<GGActListener>();
  private int keyCodePressed = Integer.MIN_VALUE;
  private int nbCycles = 0;
  private static final int SLIDER_MIN_VALUE = 0;
  private static final int SLIDER_MAX_VALUE = 3000;
  private JFrame myFrame = null;
  private JSlider speedSlider;
  private JButton stepBtn = new JButton("Step");
  private JButton runBtn = new JButton("Run");
  private JButton resetBtn = new JButton("Reset");
  private GGNavigationListener navigationListener = null;
  private ArrayList<GGResetListener> resetListeners
    = new ArrayList<GGResetListener>();
  private boolean reportSliderChange = true;
  private GGExitListener exitListener = null;
  private GGWindowStateListener windowStateListener = null;
  private ModelessOptionPane statusDialog = null;
  private int statusHeight = 0;
  private boolean isStatusBarVisible = false;
  private int doubleClickTime = 0;
  private javax.swing.Timer doubleClickTimer;
  private MouseEvent lastMouseEvent;
  private MyMouseAdapter mouseAdapter;
  private int statusBarHeight;
  private boolean deferStatusBar = false;
  private KeyRepeatHandler keyRepeatHandler = new KeyRepeatHandler();
  private Vector<GGKeyRepeatListener> keyRepeatListeners = new Vector<GGKeyRepeatListener>();
  private int keyRepeatPeriod = 20;  // Default 20 ms
  private Graphics2D snapShotG2D = null;
  private String frameTitle = null;
  private Point framePos = null;
  private Color frameBgColor = null;
  private static boolean isFatalError = false;
  private static boolean isFailMessage = false;
  private static boolean isDisposed = false;
  private static ClosingMode frameClosingMode = ClosingMode.TerminateOnClose;
  private boolean useSystemLookAndFeel = true;
  private Thread statusThread = null;
  private String statusText;
  private Font statusFont;
  private Color statusColor;

  /**
   * Constructs the game playground with 10 by 10 cells 
   * (20 pixels wide). No grid is drawn (gridColor = null) and
   * no surrounding frame window is created. Only to be used for a
   * embedded playground in a user defined frame window or an applet.
   */
  public GameGrid()
  {
    adjustDimensions();
    setPreferredSize(new Dimension(nbHorzPix, nbVertPix));
    addKeyListener(new MyKeyAdapter());
    addKeyListener(keyRepeatHandler);
    setFocusable(true);  // Needed to get the key events
    setGridColor(null);
    //   setGridColor(Color.red);
    gameThread = new GameThread();
    gameThread.setPriority(Thread.MAX_PRIORITY);
    gameThread.start();
    addActListener(this);
  }

  /**
   * Constructs a game window including a playground of 10 by 10 cells (60 pixels wide)
   * with possibly a navigation bar and a visible red grid but no background image.
   * @param isNavigation if true, a navigation bar is shown
   */
  public GameGrid(boolean isNavigation)
  {
    this(10, 10, 60, Color.red, null, isNavigation, false);
  }

  /**
   * Constructs a game window including a playground with 
   * a navigation bar and no visible grid
   * and no background image.
   * The cellSize is given in pixel units, e.g. the distance between adjacent pixels.
   * @param nbHorzCells the number of horizontal cells
   * @param nbVertCells the number of vertical cells
   * @param cellSize the side length (in pixelUnits) of the cell
   */
  public GameGrid(int nbHorzCells, int nbVertCells, int cellSize)
  {
    this(nbHorzCells, nbVertCells, cellSize, null, null, true, false);
  }

  /**
   * Constructs a game window with one pixel cell size 
   * including a playground with no navigation bar, no visible grid
   * and no background image.
   * @param nbHorzCells the number of horizontal cells
   * @param nbVertCells the number of vertical cells
   */
  public GameGrid(int nbHorzCells, int nbVertCells)
  {
    this(nbHorzCells, nbVertCells, 1, null, null, false, false);
  }

  /**
   * Constructs a game window including a playground with no visible grid
   * and no background image, but possibly a navigation bar.
   * The cellSize is given in pixel units, e.g. the distance between adjacent pixels.
   * @param nbHorzCells the number of horizontal cells
   * @param nbVertCells the number of vertical cells
   * @param cellSize the side length (in pixel units) of the cell
   * @param isNavigation if true, a navigation bar is shown
   */
  public GameGrid(int nbHorzCells, int nbVertCells, int cellSize, boolean isNavigation)
  {
    this(nbHorzCells, nbVertCells, cellSize, null, null, isNavigation, false);
  }

  /**
   * Constructs a game window including a playground with a navigation bar
   * and a possibly a background image, but no visible grid.
   * The cellSize is given in pixel units, e.g. the distance between adjacent pixels.
   * @param nbHorzCells the number of horizontal cells
   * @param nbVertCells the number of vertical cells
   * @param cellSize the side length (in pixel units) of the cell
   * @param bgImagePath the path or URL to a background image (if null, no background image)
   */
  public GameGrid(int nbHorzCells, int nbVertCells, int cellSize,
    String bgImagePath)
  {
    this(nbHorzCells, nbVertCells, cellSize, null, bgImagePath, true, false);
  }

  /**
   * Constructs a game window including a playground with a navigation bar
   * and a possibly a visible grid, but no background image.
   * The cellSize is given in pixel units, e.g. the distance between adjacent pixels.
   * @param nbHorzCells the number of horizontal cells
   * @param nbVertCells the number of vertical cells
   * @param cellSize the side length (in pixel units) of the cell
   * @param gridColor the color of the grid (if null, no grid is shown)
   */
  public GameGrid(int nbHorzCells, int nbVertCells, int cellSize,
    Color gridColor)
  {
    this(nbHorzCells, nbVertCells, cellSize, gridColor, null, true, false);
  }

  /**
   * Constructs a game window including a playground with a navigation bar,
   * possibly a visible grid and possibly a background image.
   * The cellSize is given in pixel units, e.g. the distance between adjacent pixels.
   * @param nbHorzCells the number of horizontal cells
   * @param nbVertCells the number of vertical cells
   * @param cellSize the side length (in pixel units) of the cell
   * @param gridColor the color of the grid (if null, no grid is shown)
   * @param bgImagePath the path or URL to a background image (if null, no background image)
   */
  public GameGrid(int nbHorzCells, int nbVertCells, int cellSize,
    Color gridColor, String bgImagePath)
  {
    this(nbHorzCells, nbVertCells, cellSize, gridColor, bgImagePath, true, false);
  }

  /**
   * Constructs a game window including playground with possibly a navigation bar,
   * possibly a visible grid and no background image.
   * The cellSize is given in pixel units, e.g. the distance between adjacent pixels.
   * @param nbHorzCells the number of horizontal cells
   * @param nbVertCells the number of vertical cells
   * @param cellSize the side length (in pixel units) of the cell
   * @param gridColor the color of the grid (if null, no grid is shown)
   * @param isNavigation if true, a navigation bar is shown
   */
  public GameGrid(int nbHorzCells, int nbVertCells, int cellSize,
    Color gridColor, boolean isNavigation)
  {
    this(nbHorzCells, nbVertCells, cellSize, gridColor, null, isNavigation, false);
  }

  /**
   * Constructs a game window including a playground with possibly a navigation bar,
   * possibly a visible grid and possibly a background image.
   * The cellSize is given in pixel units, e.g. the distance between adjacent pixels.<br>
   * From the given background image path the image file is searched in the following order:<br>
   * - if application is packed into a jar archive, relative to the root of the jar archive<br>
   * - relative to the directory &lt;userhome&gt;/gamegrid/<br>
   * - relative or absolute to current application directory<br>
   * - if filename starts with http://, from the given URL<br>
   * - imagePath prefixed with _ and searched relative to the root of the jar archive (used for sprite library distribution)
   * @param nbHorzCells the number of horizontal cells
   * @param nbVertCells the number of vertical cells
   * @param cellSize the side length (in pixel units) of the cell
   * @param gridColor the color of the grid (if null, no grid is shown)
   * @param bgImagePath the path to a background image (if null, no background image)
   * @param isNavigation if true, a navigation bar is shown
   */
  public GameGrid(int nbHorzCells, int nbVertCells, int cellSize,
    Color gridColor, String bgImagePath, boolean isNavigation)
  {
    this(nbHorzCells, nbVertCells, cellSize, gridColor, bgImagePath,
      isNavigation, 60, false);
  }

  /**
   * Constructs a game window including a playground with possibly a
   * navigation bar, possibly a visible grid, possibly a background image
   * and possibly no decoration.
   * The cellSize is given in pixel units, e.g. the distance between adjacent pixels.
   * The last parameter is the number of rotated sprite images (default: 60).
   * See the Actor class for more information.<br>
   * From the given background image path the image file is searched in the following order:<br>
   * - if application is packed into a jar archive, relative to the root of the jar archive<br>
   * - relative to the directory &lt;userhome&gt;/gamegrid/<br>
   * - relative or absolute to current application directory<br>
   * - if filename starts with http://, from the given URL<br>
   * - imagePath prefixed with _ and searched relative to the root of the jar archive (used for sprite library distribution)
   * @param nbHorzCells the number of horizontal cells
   * @param nbVertCells the number of vertical cells
   * @param cellSize the side length (in pixel units) of the cell
   * @param gridColor the color of the grid (if null, no grid is shown)
   * @param bgImagePath the path to a background image (if null, no background image)
   * @param isNavigation if true, a navigation bar is shown
   * @param undecorated if true, the window has no title bar and no borders
   */
  public GameGrid(int nbHorzCells, int nbVertCells, int cellSize,
    Color gridColor, String bgImagePath, boolean isNavigation,
    boolean undecorated)
  {
    this(nbHorzCells, nbVertCells, cellSize, gridColor, bgImagePath,
      isNavigation, 60, undecorated);
  }

  /**
   * Constructs a game window including a playground with possibly a
   * navigation bar, possibly a visible grid, and possibly a background image.
   * The cellSize is given in pixel units, e.g. the distance between adjacent pixels.
   * The last parameter is the number of rotated sprite images (default: 60).
   * See the Actor class for more information.<br>
   * From the given background image path the image file is searched in the following order:<br>
   * - if application is packed into a jar archive, relative to the root of the jar archive<br>
   * - relative to the directory &lt;userhome&gt;/gamegrid/<br>
   * - relative or absolute to current application directory<br>
   * - if filename starts with http://, from the given URL<br>
   * - imagePath prefixed with _ and searched relative to the root of the jar archive (used for sprite library distribution)
   * @param nbHorzCells the number of horizontal cells
   * @param nbVertCells the number of vertical cells
   * @param cellSize the side length (in pixel units) of the cell
   * @param gridColor the color of the grid (if null, no grid is shown)
   * @param bgImagePath the path to a background image (if null, no background image)
   * @param isNavigation if true, a navigation bar is shown
   * @param nbRotSprites the number of preloaded rotated sprite images for
   * all actors (default: 60). Be aware that if you change this parameter, your actors
   * must be created AFTER the GameGrid constructor terminates, because the
   * actor's creation process uses this parameter
   */
  public GameGrid(int nbHorzCells, int nbVertCells, int cellSize,
    Color gridColor, String bgImagePath, boolean isNavigation, int nbRotSprites)
  {
    this(nbHorzCells, nbVertCells, cellSize, gridColor, bgImagePath,
      isNavigation, nbRotSprites, false);
  }

  /**
   * Constructs a game window including a playground with possibly a
   * navigation bar, possibly a visible grid, possibly a background image and
   * and possibly no decoration. (This is the most general constructor.)
   * The cellSize is given in pixel units, e.g. the distance between adjacent pixels.
   * The last parameter is the number of rotated sprite images (default: 60).
   * See the Actor class for more information.<br>
   * From the given background image path the image file is searched in the following order:<br>
   * - if application is packed into a jar archive, relative to the root of the jar archive<br>
   * - relative to the directory &lt;userhome&gt;/gamegrid/<br>
   * - relative or absolute to current application directory<br>
   * - if filename starts with http://, from the given URL<br>
   * - imagePath prefixed with _ and searched relative to the root of the jar archive (used for sprite library distribution)
   * @param nbHorzCells the number of horizontal cells
   * @param nbVertCells the number of vertical cells
   * @param cellSize the side length (in pixel units) of the cell
   * @param gridColor the color of the grid (if null, no grid is shown)
   * @param bgImagePath the path to a background image (if null, no background image)
   * @param isNavigation if true, a navigation bar is shown
   * @param nbRotSprites the number of preloaded rotated sprite images for
   * all actors (default: 60). Be aware that if you change this parameter, your actors
   * must be created AFTER the GameGrid constructor terminates, because the
   * actor's creation process uses this parameter
   * @param undecorated if true, the window has no title bar and no borders
   * @see ch.aplu.jgamegrid.Actor
   */
  public GameGrid(final int nbHorzCells, final int nbVertCells,
    final int cellSize, final Color gridColor, final String bgImagePath,
    final boolean isNavigation, final int nbRotSprites, final boolean undecorated)
  {
    try
    {
      Thread.setDefaultUncaughtExceptionHandler(new MyUncaughtExceptionHandler());
    }
    catch (SecurityException ex)
    {
    }

    if (SwingUtilities.isEventDispatchThread())
      initFrame(nbHorzCells, nbVertCells, cellSize, gridColor, bgImagePath,
        isNavigation, nbRotSprites, undecorated);
    else
    {
      try
      {
        EventQueue.invokeAndWait(new Runnable()
        {
          public void run()
          {
            initFrame(nbHorzCells, nbVertCells, cellSize, gridColor,
              bgImagePath, isNavigation, nbRotSprites, undecorated);
          }
        });
      }
      catch (Exception ex)
      {
      }
    }
  }

  private void initFrame(int nbHorzCells, int nbVertCells, int cellSize, Color gridColor,
    String bgImagePath, boolean isNavigation, int nbRotSprites, boolean undecorated)
  {
    isFatalError = false;
    isFailMessage = false;
    isDisposed = false;

    MyProperties props = new MyProperties(SharedConstants.propertyVerbose);
    if (props.search())
    {
      frameTitle = props.getStringValue("FrameTitle");
      if (frameTitle != null)
        frameTitle = frameTitle.trim();

      frameClosingMode = readClosingMode(props);

      useSystemLookAndFeel = getSystemLookAndFeel(props);

      int[] values = props.getIntArray("FramePosition", 2);
      if (values != null && values[0] >= 0 && values[1] >= 0)
        framePos = new Point(values[0], values[1]);

      frameBgColor = getPropColor(props, "BackgroundColor");
      if (frameBgColor != null)
        bgColor = frameBgColor;
    }
    else
      System.out.println("search failed");

    if (useSystemLookAndFeel)
    {
      try
      {
        UIManager.setLookAndFeel(
          UIManager.getSystemLookAndFeelClassName());

      }
      catch (Exception ex)
      {
        System.err.println("Couldn't use the system " + "look and feel: " + ex);
        System.exit(0);
      }
    }

    this.nbRotSprites = nbRotSprites;
    myFrame = new JFrame();
    if (frameTitle == null)
      myFrame.setTitle("JGameGrid (V"
        + SharedConstants.VERSION.substring(0,
          SharedConstants.VERSION.indexOf(" "))
        + ")");
    else
      myFrame.setTitle(frameTitle);
    myFrame.setUndecorated(true);  // Workaround for setResizable() bug 
    myFrame.setResizable(false);
    myFrame.setUndecorated(false);
    myFrame.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
    myFrame.setIconImage(loadImage("gifs/gglogo.gif"));

    init(nbHorzCells, nbVertCells, cellSize, gridColor, bgImagePath);

    JPanel contentPane = (JPanel)myFrame.getContentPane();
    if (isNavigation)
      contentPane.setPreferredSize(new Dimension(
        Math.max(450, getNbHorzPix()), getNbVertPix() + 40));
    else
      contentPane.setPreferredSize(new Dimension(getNbHorzPix(), getNbVertPix()));

    addActListener(this);
    addKeyListener(new MyKeyAdapter());
    addKeyListener(keyRepeatHandler);
    setFocusable(true);  // Needed to get the key events
    gameThread = new GameThread();
    gameThread.setPriority(Thread.MAX_PRIORITY);
    gameThread.start();

    speedSlider = new JSlider(SLIDER_MIN_VALUE, SLIDER_MAX_VALUE,
      (int)(434.2317 * Math.log((double)getSimulationPeriod()) + 0.5));

    speedSlider.setPreferredSize(new Dimension(100,
      speedSlider.getPreferredSize().height));
    speedSlider.setMaximumSize(speedSlider.getPreferredSize());
    speedSlider.setInverted(true);
    speedSlider.setPaintLabels(false);

    JPanel dlgPanel = new JPanel();
    dlgPanel.setPreferredSize(new Dimension(100, 40));
    stepBtn.setPreferredSize(new Dimension(70, 25));
    runBtn.setPreferredSize(new Dimension(70, 25));
    resetBtn.setPreferredSize(new Dimension(70, 25));
    dlgPanel.add(stepBtn);
    dlgPanel.add(runBtn);
    dlgPanel.add(resetBtn);
    dlgPanel.add(new JLabel("     "));
    dlgPanel.add(new JLabel("Slow"));
    dlgPanel.add(speedSlider);
    dlgPanel.add(new JLabel("Fast"));

    if (isNavigation)
    {
      contentPane.add(this, BorderLayout.NORTH);
      contentPane.add(dlgPanel, BorderLayout.SOUTH);
    }
    else
    {
      contentPane.add(this, BorderLayout.CENTER);
    }

    if (undecorated)
      myFrame.setUndecorated(true);
    myFrame.pack();
    Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
    Dimension size = myFrame.getSize();
    int y = (screenSize.height - size.height) / 2;
    int x = (screenSize.width - size.width) / 2;
    if (framePos == null)
      myFrame.setLocation(x, y);
    else
      myFrame.setLocation(framePos.x, framePos.y);

    runBtn.addActionListener(new ActionListener()
    {
      public void actionPerformed(ActionEvent evt)
      {
        boolean rc = false;
        if (runBtn.getText().equals("Pause"))
        {
          if (navigationListener != null)
            rc = navigationListener.paused();
          doPause(rc);
        }
        else
        {
          if (navigationListener != null)
            rc = navigationListener.started();
          doRun(rc);
        }
        requestFocus();  // Needed to get the key events
      }
    });

    resetBtn.addActionListener(new ActionListener()
    {
      public void actionPerformed(ActionEvent evt)
      {
        if (isRunning)
          doPause();  // pause before the navigation callbacks are called
        boolean rc = false;
        if (navigationListener != null)
          rc = navigationListener.resetted();
        if (!rc)  // Not consumed
        {
          for (GGResetListener listener : resetListeners)
          {
            rc = listener.resetted();
            if (rc)  // Consumed
              break;
          }
        }
        doReset(rc);
        requestFocus();  // Needed to get the key events
      }
    });

    stepBtn.addActionListener(new ActionListener()
    {
      public void actionPerformed(ActionEvent evt)
      {
        boolean rc = false;
        if (navigationListener != null)
          rc = navigationListener.stepped();
        doStep(rc);
        requestFocus();  // Needed to get the key events
      }
    });

    speedSlider.addChangeListener(new ChangeListener()
    {
      public void stateChanged(ChangeEvent evt)
      {
        if (!reportSliderChange)
          reportSliderChange = true;
        else
        {
          boolean rc = false;
          int value = ((JSlider)evt.getSource()).getValue();
          // SLIDER_MIN_VALUE -> 0, SLIDER_MAX_VALUE -> 1000
          int time = (int)(Math.exp(value / 434.2317) - 0.5);
          if (navigationListener != null)
            rc = navigationListener.periodChanged(time);
          if (!rc)
            setSimulationPeriod(time);
          requestFocus();  // Needed to get the key events
        }
      }
    });
    addWindowListeners(myFrame);
  }

  private void addWindowListeners(JFrame frame)
  {
    frame.addWindowListener(new WindowAdapter()
    {
      public void windowClosing(WindowEvent evt)
      {
        if (exitListener != null)
        {
          boolean rc = exitListener.notifyExit();
          if (!rc)
            return;
          else
          {
            stopGameThread();
            System.exit(0);
          }
        }

        switch (frameClosingMode)
        {
          case TerminateOnClose:
            stopGameThread();
            System.exit(0);
            break;
          case AskOnClose:
            if (JOptionPane.showConfirmDialog(myFrame,
              "Terminating program. Are you sure?",
              "Please confirm",
              JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION)
            {
              stopGameThread();
              System.exit(0);
            }
            break;
          case DisposeOnClose:
            dispose();
            break;
          case NothingOnClose:
            break;
        }
      }

      public void windowIconified(WindowEvent evt)
      {
        if (windowStateListener != null)
          windowStateListener.windowIconified();
      }

      public void windowDeiconified(WindowEvent evt)
      {
        if (windowStateListener != null)
          windowStateListener.windowDeiconified();
      }

      public void windowActivated(WindowEvent evt)
      {
        if (windowStateListener != null)
          windowStateListener.windowActivated();
      }

      public void windowDeactivated(WindowEvent evt)
      {
        if (windowStateListener != null)
          windowStateListener.windowDeactivated();
      }
    });

    frame.addComponentListener(new ComponentAdapter()
    {
      public void componentMoved(ComponentEvent evt)
      {
        if (myFrame.isVisible())
        {
          int nx = evt.getComponent().getX();
          int ny = evt.getComponent().getY();
          if (statusDialog != null)
          {
            statusDialog.getDialog().setLocation(getPosition().x,
              getPosition().y + getFrame().getHeight());
          }

          if (windowStateListener != null)
          {
            windowStateListener.windowMoved(nx, ny);
          }
        }
      }
    });

    if (gPanel != null)
      gPanel.dispose();
    gPanel = new GGPanel(this);
  }

  private void init(int nbHorzCells, int nbVertCells, int cellSize,
    Color gridColor, String bgImagePath)
  {
    this.nbHorzCells = nbHorzCells;
    this.nbVertCells = nbVertCells;
    this.cellSize = cellSize;
    this.gridColor = gridColor;
    this.bgImagePath = bgImagePath;
    adjustDimensions();
    setPreferredSize(new Dimension(nbHorzPix, nbVertPix));
  }

  /**
   * Returns to path to the background image file.
   * @return Path to image file
   */
  public String getBgImagePath()
  {
    return bgImagePath;
  }

  /**
   * Sets the path to the background image file.
   * @param bgImagePath the path to the image file; if null, no background
   * image is assumed
   */
  public void setBgImagePath(String bgImagePath)
  {
    this.bgImagePath = bgImagePath;
    if (gPanel != null)
      gPanel.dispose();
    gPanel = new GGPanel(this);
  }

  /**
   * Returns the x-coordinate of the upper left vertex of the background image
   * with respect to the playground pixel coordinates.
   * @return the ULX of the background image
   */
  public int getBgImagePosX()
  {
    return bgImagePosX;
  }

  /**
   * Sets the x-coordinate of the upper left vertex of the background image
   * with respect to the playground pixel coordinates.
   * The background is redrawn by calling GGBackground.clear().
   * @param x the ULX of the background image
   */
  public void setBgImagePosX(int x)
  {
    bgImagePosX = x;
    gPanel.clear();
  }

  /**
   * Returns the y-coordinate of the upper left vertex of the background image
   * with respect to the playground pixel coordinates.
   * @return the ULY of the background image
   */
  public int getBgImagePosY()
  {
    return bgImagePosY;
  }

  /**
   * Sets the y-coordinate of the upper left vertex of the background image
   * with respect to the playground pixel coordinates.
   * The background is redrawn by calling GGBackground.clear().
   * @param y the ULY of the background image
   */
  public void setBgImagePosY(int y)
  {
    bgImagePosY = y;
    gPanel.clear();
  }

  /**
   * Returns the x-y-coordinates of the upper left vertex of the background image
   * with respect to the playground pixel coordinates.
   * @return the ULX/ULY of the background image
   */
  public Point getBgImagePos()
  {
    return new Point(bgImagePosX, bgImagePosY);
  }

  /**
   * Sets the x-y-coordinate of the upper left vertex of the background image
   * with respect to the playground pixel coordinates.
   * The background is redrawn by calling GGBackground.clear().
   * @param point the ULX/ULY of the background image
   */
  public void setBgImagePos(Point point)
  {
    bgImagePosX = point.x;
    bgImagePosY = point.y;
    gPanel.clear();
  }

  /**
   * Returns the color of the background.
   * @return  the background color
   */
  public Color getBgColor()
  {
    return bgColor;
  }

  /**
   * Sets the color of the background.
   * The background is automatically redrawn using the given color.
   * @param color the background color. Alpha channel ignored
   */
  public void setBgColor(Color color)
  {
    bgColor = new Color(color.getRed(), color.getGreen(), color.getBlue());
    gPanel.clear();
  }

  /**
   * Sets the color of the background.
   * The background is automatically redrawn using the given color.
   * @param r the red value of the color RGB
   * @param g the green value of the color RGB
   * @param b the blue value of the color RGB
   */
  public void setBgColor(int r, int g, int b)
  {
    bgColor = new Color(r, g, b);
    gPanel.clear();
  }

  /**
   * Returns the horizontal number of cells.
   * @return the number of cells in horizontal direction
   */
  public int getNbHorzCells()
  {
    return nbHorzCells;
  }

  /**
   * Sets the horizontal number of cells.
   * @param nbHorzCells the number of cells in horizontal direction
   */
  public void setNbHorzCells(int nbHorzCells)
  {
    this.nbHorzCells = nbHorzCells;
    adjustDimensions();
    setPreferredSize(new Dimension(nbHorzPix, nbVertPix));
    if (gPanel != null)
      gPanel.dispose();
    gPanel = new GGPanel(this);
  }

  /**
   * Returns the vertical number of cells.
   * @return the number of cells in horizontal direction
   */
  public int getNbVertCells()
  {
    return nbVertCells;
  }

  /**
   * Sets the vertical number of cells.
   * @param nbVertCells the number of cells in vertical direction
   */
  public void setNbVertCells(int nbVertCells)
  {
    this.nbVertCells = nbVertCells;
    adjustDimensions();
    setPreferredSize(new Dimension(nbHorzPix, nbVertPix));
    if (gPanel != null)
      gPanel.dispose();
    gPanel = new GGPanel(this);
  }

  /**
   * Returns the color of the grid.
   * @return the grid color; null, if no grid is defined
   */
  public Color getGridColor()
  {
    return gridColor;
  }

  /**
   * Sets the color of the grid and reconstructs the background.
   * @param color the grid color; null, if no grid is desired
   */
  public void setGridColor(Color color)
  {
    gridColor = color;
    if (gPanel != null)
      gPanel.dispose();
    gPanel = new GGPanel(this);
  }

  /**
   * Returns the horizontal number of pixels of the playground.
   * @return the number of pixels of the playground in horizontal direction.
   */
  public int getNbHorzPix()
  {
    return nbHorzPix;
  }

  /**
   * Returns the vertical number of pixels of the playground.
   * @return the number of pixels of the playground in vertical direction.
   */
  public int getNbVertPix()
  {
    return nbVertPix;
  }

  /**
   * Returns the width (horizontal size) of the playground in pixel units.
   * The number of pixels in the horizontal direction is width + 1.
   * @return the horizontal size of the playground in pixel units.
   */
  public int getPgWidth()
  {
    return pgWidth;
  }

  /**
   * Returns the height (vertical size) of the playground in pixel units.
   * The number of pixels in the vertical direction is height + 1.
   * @return the vertical size of the playground in pixel units.
   */
  public int getPgHeight()
  {
    return pgHeight;
  }

  /**
   * Returns the size of a cell (in pixels).
   * @return the size of a cell
   */
  public int getCellSize()
  {
    return cellSize;
  }

  /**
   * Sets the size of a cell. Cells are squares.
   * @param cellSize the length of the cell side in pixel units.
   */
  public void setCellSize(int cellSize)
  {
    this.cellSize = cellSize;
    adjustDimensions();
    setPreferredSize(new Dimension(nbHorzPix, nbVertPix));
    if (gPanel != null)
      gPanel.dispose();
    gPanel = new GGPanel(this);
  }

  private void adjustDimensions()
  {
    pgWidth = nbHorzCells * cellSize;  // in pixel units
    pgHeight = nbVertCells * cellSize;
    nbHorzPix = pgWidth + 1;
    nbVertPix = pgHeight + 1;
  }

  /**
   * Returns a reference to the background of the game grid.
   * @return a reference to the GGBackground object
   */
  public GGBackground getBg()
  {
    return (GGBackground)gPanel;
  }

  /**
   * Returns a reference to the GGPanel with current settings.
   * @return a reference to the GGPanel object
   */
  public GGPanel getPanel()
  {
    return gPanel;
  }

  /**
   * Sets the coordinate system in GGPanel to given range and
   * returns a reference to the GGPanel of the game grid.
   * @return a reference to the GGPanel object
   */
  public GGPanel getPanel(double xmin, double xmax, double ymin, double ymax)
  {
    gPanel.window(xmin, xmax, ymin, ymax);
    return gPanel;
  }

  /**
   * Same as addActor(Actor actor, Location location), but the game grid window
   * is not refreshed automatically. This can be useful to avoid flickering
   * when adding many actors at once.
   * @param actor the actor to be added at the end of the scene list
   * @param location the location of the actor (cell indices, value copy)
   */
  public void addActorNoRefresh(final Actor actor, Location location)
  {
    addActor(actor, location, 0, false);
  }

  /**
   * Same as addActor(Actor actor, Location location, double direction),
   * but the game grid window is not refreshed automatically.<br><br>
   * 
   * Keep in mind that when using addActor(), the game grid is 
   * refreshed automatically, so that the action becomes immediately visible. 
   * The rendering of the game grid takes some time and it may be preferable to use 
   * addActorNoRefresh() if you add  many actors at the same time and to 
   * avoid flickering. After that you may render the game grid manually by 
   * calling refresh(). (The game grid is also refreshed automatically in 
   * every simulation cycle.)
   * @param actor the actor to be added at the end of the scene list
   * @param location the location of the actor (cell indices, value copy)
   * @param direction the direction (clockwise in degrees, 0 to east)
   */
  public void addActorNoRefresh(final Actor actor, Location location, double direction)
  {
    addActor(actor, location, direction, false);
  }

  /**
   * Adds a new actor at given starting position with given moving direction
   * to the scene. If the actor is already part of the scene, it must
   * be removed from the scene before adding it again, because actors
   * added to the scene should not be duplicated. The starting location
   * and direction are saved and may be retrieved later.
   * The actor reset notification is called in order an initialisation
   * may be done.<br><br>
   * 
   * More than one actor may be added at the same location and the location may be
   * outside the visible game grid to hide the actor when added to the scene.<br><br>
   * The image center is centered at the given location. For even image pixel
   * width or height, the center is half pixel width to the left or resp. to the top.
   * The default act and paint order is given by the order the actor are added, but
   * all actors of the same class are bundled together. Actors added last will
   * act and be painted first, but this default order may be modified.
   * See setActOrder() and setPaintOrder() for more information.<br><br>
   * 
   * After adding the actor to the scene, the game grid is refreshed automatically,
   * so that the action becomes visible. The rendering of the game grid takes some 
   * time and it may be preferable to use addActorNoRefresh() if you add 
   * many actors at the same time and to avoid flickering. After that you may
   * render the game grid manually by calling refresh(). 
   * (The game grid is also refreshed automatically in every simulation cycle.)
   * @param actor the actor to be added at the end of the scene list
   * @param location the location of the actor (cell indices, value copy)
   * @param direction the direction (clockwise in degrees, 0 to east)
   */
  public void addActor(Actor actor, Location location, double direction)
  {
    addActor(actor, location, direction, true);
  }

  /**
   * Same as addActor(Actor actor, Location location, double direction) with
   * direction = 0 (to east).
   * @param actor the actor to be added at the end of the scene list
   * @param location the location of the actor (cell indices, value copy)
   */
  public void addActor(final Actor actor, Location location)
  {
    addActor(actor, location, 0, true);
  }

  protected void addActor(final Actor actor, Location location,
    double direction, final boolean doRefresh)
  {
    // Actors should not be duplicated
    synchronized (monitor)
    {
      for (Actor a : getActors())
      {
        if (a == actor)
          fail("Error in GameGrid.addActor()."
            + "\nActor already added to game grid."
            + "\n(Actors should not be duplicated. Remove it first.)"
            + "\nApplication will terminate.");
      }
      actor.setGameGrid(this);
      actor.setX(location.x);
      actor.setY(location.y);
      actor.setDirection(direction);
      actor.initStart();
      actor.reset();  // Must call reset before act() cycling is performed

      // Scene handling: try to find actor's class from the classList
      Class actorClass = actor.getClass();
      int classIndex = -1;
      for (int i = 0; i < sceneList.size(); i++)
      {
        if (classList.get(i) == actorClass)  // found
        {
          classIndex = i;
          break;
        }
      }
      if (classIndex == -1) // If not found, the scene is created, the actor inserted and the scene
      {                     // added to the sceneList. The class is added to the classList. The
        // class index is added (at the end) to the actOrder and paintOrder.
        Vector<Actor> scene = new Vector<Actor>();
        scene.add(actor);
        sceneList.add(scene);
        classList.add(actorClass);
        actOrder.add(actOrder.size());
        paintOrder.add(paintOrder.size());
      }
      else // the class already exists, all we do is add the actor to the scene
      {
        sceneList.get(classIndex).add(actor);
      }
    }

    if (doRefresh)
      refresh();
  }

  /**
   * Same as addActor(Actor actor, Location location, double direction)
   * with compass direction.
   * @param actor the actor to be added at the end of the scene list
   * @param location the location of the actor (cell indices)
   * @param compassDir the compass direction
   */
  public void addActor(final Actor actor, Location location,
    Location.CompassDirection compassDir)
  {
    addActor(actor, location, compassDir.getDirection());
  }

  private void act(boolean doAct)
  {
    if (doAct)
    {
      nbCycles++;

      // First call all act() of registered GGActListeners
      synchronized (actListeners)
      {
        for (GGActListener listener : actListeners)
          listener.act();
      }

      // Use temporary to avoid concurrency problems
      Vector<Vector<Actor>> sList;
      Vector<Integer> aOrder;
      synchronized (monitor)
      {
        sList = new Vector<Vector<Actor>>(sceneList);
        aOrder = new Vector<Integer>(actOrder);
      }
      // Act order determined by the permutation of indices in actOrder
      // act() is called in the reverse order (later entries first)
      for (int i = aOrder.size() - 1; i >= 0; i--)  // act last class first
      {
        int sceneIndex = aOrder.get(i);
        Vector<Actor> actors = sList.get(sceneIndex);
        for (int k = actors.size() - 1; k >= 0; k--)  // act last actor first
        {
          Actor a;
          try
          {
            a = actors.get(k);
          }
          catch (Exception ex)
          // Happend on rare occations, because the scene list is modified
          // by another thread. Synchronizing with synchronized(monitor) will¨¨
          // cause deadlocks
          {
//            System.out.println("Exception type 1 in act()");
            return;
          }
          if (a.isActEnabled())
          {
            a.decreaseStepCount();
            if (a.getStepCount() == 0)
            {
              a.decreaseActorSimCount();  // Used for rearming collision detection
              if (tileMap != null)
                a.decreaseTileSimCount();   // ditto
              a.act();
              a.nbCycles++;
              if (a.isActorCollisionEnabled())
              {
                ArrayList<Actor> collisionActors = a.getCollisionActors();
                if (!collisionActors.isEmpty())
                  checkActorCollision(a, collisionActors);
              }
              if (a.isTileCollisionEnabled())
              {
                ArrayList<Location> collisionTiles = a.getCollisionTiles();
                if (tileMap != null && !collisionTiles.isEmpty())
                  checkTileCollision(a, collisionTiles);
              }
              a.initStepCount();
            }
          }
        }
      }
    }

    // --------------------- Rendering ------------------
    synchronized (this)
    {
      try
      {
        do
        {
          do
          {
            Graphics2D g2D = (Graphics2D)strategy.getDrawGraphics();

            if (gPanel != null)
            {
              g2D.drawImage(gPanel.getBackgroundImage(), 0, 0, null);
              if (snapShotG2D != null)
                snapShotG2D.drawImage(gPanel.getBackgroundImage(), 0, 0, null);
            }
            if (tileMap != null)
            {
              tileMap.draw(g2D);
              if (snapShotG2D != null)
                tileMap.draw(snapShotG2D);
            }
            g2D.setClip(0, 0, nbHorzPix, nbVertPix);
            if (snapShotG2D != null)
              snapShotG2D.setClip(0, 0, nbHorzPix, nbVertPix);

            Vector<Vector<Actor>> sList;
            Vector<Integer> pOrder;
            // Use temporary to avoid concurrency problems
            synchronized (monitor)
            {
              sList = new Vector<Vector<Actor>>(sceneList);
              pOrder = new Vector<Integer>(paintOrder);
            }

            // Paint order determined by the permutation of indices in actOrder
            // painting is done in the order (later entries last, image will be on top)
            for (int i = 0; i < pOrder.size(); i++) // Draw last class last
            {
              int sceneIndex = pOrder.get(i);
              Vector<Actor> scene = sList.get(sceneIndex);
              for (int k = 0; k < scene.size(); k++) // Draw last actor last (on top)
              {
                Actor a;
                try
                {
                  a = scene.get(k);
                  int id = a.getIdVisible();
                  if (id >= 0)
                  {
                    a.draw(g2D, id);
                    if (snapShotG2D != null)
                      a.draw(snapShotG2D, id);
                  }
                }
                catch (Exception ex)
                // Happend on rare occations, because the scene list is modified
                // by another thread. Synchronizing with synchronized(monitor) will¨¨
                // cause deadlocks
                {
                  //               System.out.println("Exception type 2 in act()");
                  g2D.dispose();
                  return;
                }
              }
            }
            g2D.dispose();
          }
          while (strategy.contentsRestored());
          strategy.show();
        }
        while (strategy.contentsLost());
      }
      catch (Exception ex)
      {
      }
    }
  }

  /**
   * Returns the BufferedImage of the current GameGrid window including
   * the background and all actors.
   * @return the current GameGrid window
   */
  public synchronized BufferedImage getImage()
  {
    boolean oldActEnabled = isActEnabled;
    isActEnabled = false;
    BufferedImage bi
      = new BufferedImage(getNbHorzPix(), getNbVertPix(),
        BufferedImage.TYPE_INT_ARGB);
    snapShotG2D = bi.createGraphics();
    act(false);
    snapShotG2D.dispose();
    snapShotG2D = null;
    isActEnabled = oldActEnabled;
    return bi;
  }

  protected void setRefreshEnable(boolean enable)
  {
    isRefreshEnabled = enable;
  }

  /**
   * Refreshs the current game situation (repaint background, tiles, actors).
   * Refresh is automatically done in each simulation cycle. Manual refresh should
   * be used with care, because it may interfere with the automatic refresh causing
   * flickering.
   */
  public void refresh()
  {
    if (!isRefreshEnabled)
      return;
    if (SharedConstants.DEBUG != SharedConstants.DEBUG_LEVEL_OFF)
      System.out.println("calling refresh()");
    if (strategy == null)
    {
      if (SharedConstants.DEBUG != SharedConstants.DEBUG_LEVEL_OFF)
        System.out.println("must create BufferStrategy");
      createBufferStrategy(2);
      strategy = getBufferStrategy();
    }
    act(false);  // May cause a reentrance of act() if refresh() is done in
    // actor's act()
  }

  /**
   * Invokes all actor's act() methods in the order of the scene
   * and draws the new game situation.
   * Corresponds to the next simulation act.
   */
  public void actAll()
  {
    act(true);
  }

  /**
   * Set the act order of objects in the scene.
   * Act order is specified by class: objects of one class will always act
   * before objects of some other class. The default act order is
   * determined by the order the actor's classes are added to the game grid:
   * last entry will act first. The act order of objects of the same class (called
   * a scene)is given by the default act order: last entry will act first. This order
   * within a class can be changed with setActorOnTop(), 
   * setSceneOrder(), shiftSceneOrder(), reverseSceneOrder().<br>
   * Objects of classes listed first in the specified parameter list will act
   * before any objects of classes listed later.<br><br>
   * Objects of classes not listed will act after all objects whose classes have been specified.
   * @param classes the classes in desired act order
   */
  public void setActOrder(Class... classes)
  {
    if (SharedConstants.DEBUG != SharedConstants.DEBUG_LEVEL_OFF)
      System.out.println("actOrder at entry: " + actOrder);
    permutate(actOrder, classes);
    if (SharedConstants.DEBUG != SharedConstants.DEBUG_LEVEL_OFF)
      System.out.println("actOrder at exit: " + actOrder);
  }

  /**
   * Sets the paint order of objects in the game grid.
   * Paint order is specified by class: objects of one class will always be
   * painted on top of objects of some other class. The default paint order is
   * determined by the order the actor's classes are added to the game grid:
   * last entry will be on top of first. The paint order of objects of the 
   * same class (called a scene) is given by the default paint order:
   * last entry will be on top. This order can be changed with setActorOnTop(),
   * setSceneOrder(), shiftSceneOrder, reverseSceneOrder().<br>
   * Objects of classes listed first in the parameter list will appear on top
   * of all objects of classes listed later.<br><br>
   * Objects of classes not listed will appear below the objects whose classes 
   * have been specified.<br><br>
   * <b>At least one actor of the specified class has to be part of the scene,
   * unless the paint order of this class is not modified.</b>
   * @param classes the classes in desired paint order
   */
  public void setPaintOrder(Class... classes)
  {
    if (SharedConstants.DEBUG != SharedConstants.DEBUG_LEVEL_OFF)
      System.out.println("paintOrder at entry: " + paintOrder);
    permutate(paintOrder, classes);
    if (SharedConstants.DEBUG != SharedConstants.DEBUG_LEVEL_OFF)
      System.out.println("paintOrder at exit: " + paintOrder);
  }

  private void permutate(Vector<Integer> list, Class... classes)
  // Priority is higest at the end of the list
  {
    for (int k = classes.length - 1; k >= 0; k--)
    {
      // Search sceneIndex of this class
      int sceneIndex = -1;
      synchronized (monitor)
      {
        for (int i = 0; i < classList.size(); i++)
        {
          if (classList.get(i) == classes[k])
          {
            sceneIndex = i;
            break;
          }
        }
        if (sceneIndex != -1) // found
        {
          Vector<Integer> tmp = new Vector<Integer>();
          list.remove(new Integer(sceneIndex));  // Remove the priority index
          for (Integer i : list)    // Copy rest in temp list
            tmp.add(i);
          tmp.add(sceneIndex);      // Add priority index at end
          list.clear();             // Clear list
          for (Integer i : tmp)     // Copy back from tmp
            list.add(i);
        }
      }
    }
  }

  /**
   * Delays execution for the given amount of time.
   * @param time the delay time (in ms)
   */
  public static void delay(long time)
  {
    try
    {
      Thread.sleep(time);
    }
    catch (InterruptedException ex)
    {
    }
  }

  private static void delay(long millis, int nanos)
  {
    try
    {
      Thread.sleep(millis, nanos);
    }
    catch (InterruptedException ex)
    {
    }
  }

  /**
   * Starts the simulation cycling. Same as if the 'Run' button is pressed.
   */
  public void doRun()
  {
    doRun(false);
  }

  private void doRun(boolean changeLabelOnly)
  {
    if (!changeLabelOnly)
    {
      if (isRunning)
        return;
      isPaused = false;
      isSingleStep = false;
      isRunning = true;
    }
    if (myFrame == null)
      return;
    EventQueue.invokeLater(new Runnable()
    {
      public void run()
      {
        runBtn.setText("Pause");
      }
    });
  }

  /**
   * Pauses the simulation cycling. Same as if the 'Pause' button is pressed.
   */
  public void doPause()
  {
    doPause(false);
  }

  private void doPause(boolean changeLabelOnly)
  {
    if (!changeLabelOnly)
    {
      if (!isRunning)
        return;
      isRunning = false;
      int nb = 0;
      while (!isPaused && nb < 10)  // Must wait until paused, otherwise
      // may conflict with user code run in reset()
      // Security to avoid hang: break after 10 attempts
      {
        delay(100);
        nb++;
      }

    }
    if (myFrame == null)
      return;
    EventQueue.invokeLater(new Runnable()
    {
      public void run()
      {
        runBtn.setText("Run");
      }
    });
  }

  /**
   * Runs the the simulation loop once. Same as if the 'Step' button is pressed.
   */
  public void doStep()
  {
    doStep(false);
  }

  private void doStep(boolean changeLabelOnly)
  {
    if (myFrame != null)
    {
      EventQueue.invokeLater(new Runnable()
      {
        public void run()
        {
          runBtn.setText("Run");
        }
      });
    }
    if (!changeLabelOnly)
    {
      doPause();
      isSingleStep = true;
      isRunning = true;
    }
    requestFocus();  // Needed to get key events
  }

  /**
   * If still running, calls doPause() and restores actors
   * to their initializing state. The initializing state includes:
   * starting location, starting direction,
   * starting sprite id (set before adding actor to game grid),
   * no horizontal or vertical mirroring,
   * collision enabled, but collision type not changed.
   * All actor's and the GameGrid's reset() methods are called. Then
   * the game grid is refreshed.<br><br>
   * Same as if the 'Reset' button is pressed.
   * This method is not called automatically when the game grid is created.
   */
  public void doReset()
  {
    doReset(false);
  }

  private void doReset(boolean changeLabelOnly)
  {
    if (myFrame != null)
    {
      if (runBtn.getText().equals("Pause"))
      {
        EventQueue.invokeLater(new Runnable()
        {
          public void run()
          {
            runBtn.setText("Run");
          }
        });
      }
    }
    if (!changeLabelOnly)
    {
      if (isRunning)
      {
        doPause();
      }
      for (Integer i : actOrder)
      {
        Vector<Actor> tmp;
        synchronized (monitor)
        {
          Vector<Actor> scene = sceneList.get(i);
          // ActionListner.reset() may need access to the scene, we use
          // a copy to avoid concurrency exceptions
          tmp = new Vector<Actor>(scene);
        }
        for (Actor actor : tmp)
        {
          actor.setLocation(actor.getLocationStart());
          Point cellCenter = toPoint(actor.getLocationStart());
          actor.setPixelLocation(cellCenter);

          actor.setDirection(actor.getDirectionStart());
          actor.show(actor.getIdVisibleStart());
          actor.setHorzMirror(false);
          actor.setVertMirror(false);
          actor.setActorCollisionEnabled(true);
          actor.nbCycles = 0;
          nbCycles = 0;
          actor.reset();  // Announce actors we are resetting
        }
      }
      reset();  // Announce GameGrid we are resetting
      refresh();
    }
  }

  /**
   * Returns the JFrame reference of the surrounding window.
   * @return the JFrame reference; null, if no JFrame is used.
   */
  public JFrame getFrame()
  {
    return myFrame;
  }

  protected static void fail(String message)
  {
    if (isFailMessage)  // Avoid multiple invocation
      return;
    isFailMessage = true;
    JOptionPane.showMessageDialog(null, message + getStackTrace(),
      "JGameGrid Fatal Error", JOptionPane.ERROR_MESSAGE);

    MyProperties props = new MyProperties(false); // Need to get props here
    // because initFrame() may not yet been called 
    if (props.search())
    {
      ClosingMode myFrameClosingMode = readClosingMode(props);
      if (myFrameClosingMode == ClosingMode.TerminateOnClose
        || myFrameClosingMode == ClosingMode.AskOnClose)
        System.exit(0);
    }
    else
    {
      if (frameClosingMode == ClosingMode.TerminateOnClose
        || frameClosingMode == ClosingMode.AskOnClose)
        System.exit(0);
    }
  }

  private static String getStackTrace()
  {
    String s = "\n\nStack Trace:\n";
    int n = 0;
    for (StackTraceElement trace : new Throwable().getStackTrace())
    {
      n++;
      if (n > 2)  // Skip first two entries
        s += trace + "\n";
    }
    return s;
  }

  /**
   * Returns the current simulation period.
   * @return the simulation period (in ms;  0, if slider knob is set to minimum period)
   */
  public int getSimulationPeriod()
  {
    return simulationPeriod;
  }

  /**
   * Sets the period of the simulation loop.
   * If there is too much to do in one period, the period may be
   * exceeded. The default simulaton period is 200 ms. If the navigation
   * bar is shown, the slider knob is set to the new value.
   * @param millisec the period of the simulation loop (in milliseconds)
   */
  public void setSimulationPeriod(int millisec)
  {
    if (millisec < 0)
      millisec = 0;
    simulationPeriod = millisec;
    simulationPeriodNanos = simulationPeriod * 1000000L;
    if (myFrame != null)
    {
      int v = (int)(434.2317 * Math.log((double)millisec + 0.9));
      reportSliderChange = false;  // Disable change
      speedSlider.setValue(v < 0 ? 0 : v);
    }
  }

  /**
   * Returns true, if a key was press since the last call to getKeyChar() or getKeyCode().
   * The key is not removed from the one-key buffer. If a key is continously 
   * held down, after an initial delay, successive kbhit() = true are reported
   * @return true, if a key was pressed and not yet consumed
   */
  public boolean kbhit()
  {
    delay(1);
    return gotKey;
  }

  /**
   * Returns the character of the last key pressed and removes it from the 
   * one-key buffer. If a second key is pressed while another key is held down
   * the method returns the character of the second key.
   * @return the charactor corresponding to the last key pressed, 
   * KeyEvent.CHAR_UNDEFINED if no key was pressed
   */
  public char getKeyChar()
  {
    if (gotKey)
    {
      gotKey = false;
      return keyChar;
    }
    else
      return KeyEvent.CHAR_UNDEFINED;
  }

  /**
   * Waits until a key is pressed and then returns the character of the last key pressed
   * and removes it from the one-key buffer. If a second key is pressed while another key is held down
   * the method returns the character of the second key. The wait loop yields with
   * delay(10). If the GameGrid is disposed, quits the wait loop and  
   * returns KeyEvent.CHAR_UNDEFINED.
   * @return the charactor corresponding to the last key pressed, 
   * KeyEvent.CHAR_UNDEFINED if no key was pressed
   */
  public int getKeyCharWait()
  {
    int ch = 0;
    while ((ch = getKeyChar()) == KeyEvent.CHAR_UNDEFINED && !isDisposed())
      delay(10);
    if (isDisposed())
      return KeyEvent.CHAR_UNDEFINED;
    return ch;
  }

  /**
   * Returns the key code of the last key pressed and removes it from the 
   * one-key buffer. If a second key is pressed while another key is held down
   * the method returns the key code of the second key.
   * @return the key code corresponding to the last key pressed, 
   * KeyEvent.CHAR_UNDEFINED if no key was pressed
   */
  public int getKeyCode()
  {
    if (gotKey)
    {
      gotKey = false;
      return keyCode;
    }
    else
      return KeyEvent.CHAR_UNDEFINED;
  }

  /**
   * Waits until a key is pressed and then eeturns the key code of the last key pressed and removes it from the 
   * one-key buffer. If a second key is pressed while another key is held down
   * the method returns the key code of the second key. The wait loop yields with
   * delay(10). If the GameGrid is disposed, quits the wait loop and 
   * returns KeyEvent.CHAR_UNDEFINED.
   * @return the key code corresponding to the last key pressed, 
   * KeyEvent.CHAR_UNDEFINED if no key was pressed
   */
  public int getKeyCodeWait()
  {
    int code = 0;
    while ((code = getKeyCode()) == KeyEvent.CHAR_UNDEFINED && !isDisposed())
      delay(10);
    if (isDisposed())
      return KeyEvent.CHAR_UNDEFINED;
    return code;
  }

  /**
   * Returns the key modifier of the last key pressed.
   * The key is not removed from the one-key buffer.
   * @return the key modifier
   */
  public int getKeyModifiers()
  {
    return keyModifiers;
  }

  /**
   * Returns the key modifier as text of the last key pressed.
   * The key is not removed from the one-key buffer.
   * @return the key modifier as string
   */
  public String getKeyModifiersText()
  {
    return keyModifiersText;
  }

  /**
   * Returns true if the key with the given key code is currently pressed.
   * If more than one key is pressed at the same time, the method returns
   * true for each of their key code.
   * @param keyCode the code of the key you are interested in (a constant
   * defined in java.awt.event.KeyCode).
   * @return true, if the key is currently pressed; otherwise false
   */
  public boolean isKeyPressed(int keyCode)
  {
    ArrayList<Integer> keyCodesPressed = getPressedKeyCodes();
    return keyCodesPressed.contains(keyCode);
  }

  /**
   * Adds a GGKeyListener to get events when a key is pressed. More than
   * one KeyListener may be registered. They are called in the order they
   * are added. Be aware that if the notification method keyPressed() 
   * returns true, the key is "consumed" and no following KeyListener
   * will be notified.
   * @param listener the GGKeyListener to register
   */
  public void addKeyListener(GGKeyListener listener)
  {
    keyListeners.add(listener);
  }

  /**
   * Removes a previously registered GGKeyListener.
   * @param listener the GGKeyListener to remove
   */
  public void removeKeyListener(GGKeyListener listener)
  {
    keyListeners.remove(listener);
  }

  /**
   * Returns true, if the given cell location is within the grid.
   * @return true, if the cell coordinates are within the grid; otherwise false
   */
  public boolean isInGrid(Location location)
  {
    boolean isHorz = (location.x >= 0 && location.x < nbHorzCells);
    boolean isVert = (location.y >= 0 && location.y < nbVertCells);
    return isHorz && isVert;
  }

  /**
   * Returns true, if the given cell location is at the grid border.
   * Be aware that in one pixel games, the near border location is only 1 pixel from the border.
   * @return true, if the cell coordinates are at the grid border
   */
  public boolean isAtBorder(Location location)
  {
    boolean isHorz = (location.x == 0 || location.x == nbHorzCells - 1);
    boolean isVert = (location.y == 0 || location.y == nbVertCells - 1);
    return isHorz || isVert;
  }

  /**
   * Returns a list of all locations occupied by actors. Hidden actors are
   * considered to be present.
   * @return an ArrayList of all occupied locations
   */
  public ArrayList<Location> getOccupiedLocations()
  {
    ArrayList<Location> locations = new ArrayList<Location>();

    for (int x = 0; x < nbHorzCells; x++)
    {
      for (int y = 0; y < nbVertCells; y++)
      {
        Location location = new Location(x, y);
        if (!getActorsAt(location).isEmpty())
          locations.add(location);
      }
    }
    return locations;
  }

  /**
   * Returns a list of all locations not occupied by actors.
   * Hidden actors are considered to be present.
   * @return an ArrayList of all occupied locations
   */
  public ArrayList<Location> getEmptyLocations()
  {
    ArrayList<Location> locations = new ArrayList<Location>();

    for (int x = 0; x < nbHorzCells; x++)
    {
      for (int y = 0; y < nbVertCells; y++)
      {
        Location location = new Location(x, y);
        if (getActorsAt(location).isEmpty())
          locations.add(location);
      }
    }
    return locations;
  }

  /**
   * Returns all actors that are part of the game grid. The returned
   * list is ordered in the current paint order: subsequent actors in the list 
   * are painted on top of preceding actors.
   * Hidden actors are considered to be present.
   * @return an ArrayList that contains all actors
   */
  public ArrayList<Actor> getActors()
  {
    ArrayList<Actor> list = new ArrayList<Actor>();
    synchronized (monitor)
    {
      for (int i = 0; i < paintOrder.size(); i++) // Last class is drawn last
      {
        int sceneIndex = paintOrder.get(i);
        Vector<Actor> scene = sceneList.get(sceneIndex);
        for (int k = 0; k < scene.size(); k++) // Last actor is drawn last (on top)
        {
          Actor a;
          a = scene.get(k);
          list.add(a);
        }
      }
      return list;
    }
  }

  /**
   * Returns all actors of given type at given location that are part of
   * the game grid. The returned list is ordered in the current paint order: 
   * subsequent actors in the list are painted on top of preceding actors.
   * The list is empty if the location is unoccupied.
   * The actor is considered to have the type of its superclasses too.
   * Hidden actors are considered to be present.
   * @param location the location of the cell
   * @param clazz class type of the actors to be returned (e.g. Fish.class),
   * if clazz is null, all actors are returned
   * @return an ArrayList of actors
   */
  public ArrayList<Actor> getActorsAt(Location location, Class clazz)
  {
    if (clazz == null)
      return getActorsAt(location);

    synchronized (monitor)
    {
      ArrayList<Actor> list = new ArrayList<Actor>();
      ArrayList<Actor> actors = getActorsAt(location);
      for (Actor a : actors)
      {
        Class c = a.getClass();
        while (c != null)
        {
          if (c == clazz)
          {
            list.add(a);
            break;
          }
          c = c.getSuperclass(); // null, if c is Object.class
        }
      }
      return list;
    }
  }

  /**
   * Returns all actors at the given location that are part of the game grid.
   * The returned list is ordered in the current paint order: 
   * subsequent actors in the list are painted on top of preceding actors.
   * The list is empty if the location is unoccupied.
   * Hidden actors are considered to be present.
   * @return an ArrayList of all actors at the given location
   */
  public ArrayList<Actor> getActorsAt(Location location)
  {
    synchronized (monitor)
    {
      ArrayList<Actor> list = new ArrayList<Actor>();
      for (Actor a : getActors())
      {
        if (a.getLocation().equals(location))
          list.add(a);
      }
      return list;
    }
  }

  /**
   * Returns all actors of the specified class that are part of the game grid. The actor is considered to
   * have the type of its superclasses too. The returned
   * list is ordered in the current paint order: subsequent actors in the list are
   * painted on top of preceding actors
   * Hidden actors are considered to be present.
   * @param clazz the class of the actors to look for, if null all actors are returned
   * @return an ArrayList that contains actors of the given class
   */
  public ArrayList<Actor> getActors(Class clazz)
  {
    if (clazz == null)
      return getActors();

    synchronized (monitor)
    {
      ArrayList<Actor> list = new ArrayList<Actor>();
      for (Actor a : getActors())
      {
        Class c = a.getClass();
        while (c != null)
        {
          if (c == clazz)
          {
            list.add(a);
            break;
          }
          c = c.getSuperclass(); // null, if c is Object.class
        }
      }
      return list;
    }
  }

  /**
   * Returns the actor of the specified class at the specified location
   * that is part of the game grid and is on top in the paint order.
   * The actor is considered to have the type of its superclasses too.
   * Hidden actors are considered to be present.
   * @param location the location of the cell
   * @param clazz the class of the actors to look for, if null all actors are considered
   * @return the most visible actor or null, if no actor is found
   */
  public Actor getOneActorAt(Location location, Class clazz)
  {
    synchronized (monitor)
    {
      ArrayList<Actor> list = getActorsAt(location, clazz);
      if (list.isEmpty())
        return null;
      return list.get(list.size() - 1);
    }
  }

  /**
   * Returns the actor of the specified class
   * that is part of the game grid and is on top in the paint order.
   * The actor is considered to have the type of its superclasses too.
   * Hidden actors are considered to be present.
   * @param clazz the class of the actors to look for, if null all actors are considered
   * @return the most visible actor or null, if no actor is found
   */
  public Actor getOneActor(Class clazz)
  {
    synchronized (monitor)
    {
      ArrayList<Actor> list = getActors(clazz);
      if (list.isEmpty())
        return null;
      return list.get(list.size() - 1);
    }
  }

  /**
   * Returns the actor at the specified location
   * that is part of the game grid and is on top in the paint order.
   * Hidden actors are considered to be present.
   * @param location the location of the cell
   * @return the most visible actor or null, if no actor is found
   */
  public Actor getOneActorAt(Location location)
  {
    return getOneActorAt(location, null);
  }

  /**
   * Returns total number of actors in the scene.
   * Hidden actors are considered to be present.
   * @return the total number of actors
   */
  public int getNumberOfActors()
  {
    return getActors().size();
  }

  /**
   * Returns number of actors at specified location.
   * Hidden actors are considered to be present.
   * @return the number of actors
   */
  public int getNumberOfActorsAt(Location location)
  {
    return getActorsAt(location).size();
  }

  /**
   * Returns number of actors of specified class at specified location.
   * The actor is considered to have the type of its superclasses too.
   * Hidden actors are considered to be present.
   * @return the number of actors
   */
  public int getNumberOfActorsAt(Location location, Class clazz)
  {
    return getActorsAt(location, clazz).size();
  }

  /**
   * Returns number of actors of specified class.
   * The actor is considered to have the type of its superclasses too.
   * Hidden actors are considered to be present.
   * @return the number of actors
   */
  public int getNumberOfActors(Class clazz)
  {
    return getActors(clazz).size();
  }

  /**
   * Returns true, if there is no actor at specified location.
   * Hidden actors are considered to be present.
   * @return true, if number of actors of all classes at specified location is zero
   */
  public boolean isEmpty(Location location)
  {
    return (getNumberOfActorsAt(location) == 0) ? true : false;
  }

  /**
   * Removes the given actor from the scene, so that act() is not called any more.
   * The visiblity is turned off (spriteId = -1) and a registered mouse,
   * mouse touch or key listener is removed.<br><br>
   * <b>Be aware that clicking the reset button or calling reset() will not bring
   * the actor to life.</b><br>
   *
   * If you want to reuse the removed actor, you may add it again
   * to the game grid (the sprite with id = 0 is shown). Any mouse, mouse touch
   * or key listener must be registered again. Instead of removing the actor,
   * call hide() to make him invisible and show() to display him again.<br><br>
   *
   * The buffered image resources will not be released until the actor
   * reference runs out of scope. Because actors use a lot of heap space,
   * you should be careful to remove actors and deassign their reference
   * when they are no more used (e.g. moves out of the visible game grid).
   * @param actor the actor to be removed
   * @return true, if the specified actor is found and removed
   */
  public boolean removeActor(Actor actor)
  {
    if (actor == null || actor.isRemoved())
      return false;
    synchronized (monitor)
    {
      if (actor instanceof GGKeyListener)
        removeKeyListener((GGKeyListener)actor);
      if (actor.getMouseListener() != null)
        removeMouseListener((actor.getMouseListener()));

      boolean rc;
      int sceneIndex = -1;
      actor.setRemoved();

      // Search the scene index
      for (int i = 0; i < classList.size(); i++)
      {
        if (classList.get(i) == actor.getClass())
        {
          sceneIndex = i;
          break;
        }
      }
      if (sceneIndex == -1)  // class not found
        return false;

      rc = sceneList.get(sceneIndex).remove(actor);
      if (sceneList.isEmpty())
      {
        sceneList.remove(sceneIndex);
        classList.remove(sceneIndex);
        actOrder.remove(sceneIndex);
        paintOrder.remove(sceneIndex);
      }
      return rc;
    }
  }

  /**
   * Removes all actors from the scene, so that act() is not called any more.
   * The visiblity is turned off and any registered mouse, mouse touch or key
   * listeners are disabled. Be aware that clicking the reset button or
   * calling reset() will not bring the actors to life.
   * @return the number of removed actors
   */
  public int removeAllActors()
  {
    synchronized (monitor)
    {
      ArrayList<Actor> list = getActors();
      int nb = 0;
      for (Actor a : list)
        if (removeActor(a))
          nb++;
      return nb;
    }
  }

  /**
   * Removes all actors from the specified class, so that act() is not called any more.
   * The visiblity is turned off and any registered  mouse, mouse touch or
   * key listeners are disabled.
   * Be aware that clicking the reset button or calling reset() will not bring
   * the actor to life.
   * @param clazz class of the actors to be removed, if null all actors are removed
   * @return the number of removed actors
   */
  public int removeActors(Class clazz)
  {
    if (clazz == null)
      return removeAllActors();

    synchronized (monitor)
    {
      ArrayList<Actor> list = getActors(clazz);
      int nb = 0;
      for (Actor a : list)
        if (removeActor(a))
          nb++;
      return nb;
    }
  }

  /**
   * Removes all actors from the specified class at the specified location,
   * so that act() is not called any more. The visiblity is turned off
   * and any registered  mouse, mouse touch or key listeners are disabled.
   * Be aware that clicking the reset button or calling reset() will not bring
   * the actors to life.
   * @param location the location of the cell
   * @param clazz class of the actors to be removed, if null all actors are removed
   * @return the number of removed actors
   */
  public int removeActorsAt(Location location, Class clazz)
  {
    synchronized (monitor)
    {
      ArrayList<Actor> list = getActorsAt(location, clazz);
      int nb = 0;
      for (Actor a : list)
        if (removeActor(a))
          nb++;
      return nb;
    }
  }

  /**
   * Removes all actors at the specified location, so that act() is not called any more.
   * The visiblity is turned off and any registered  mouse, mouse touch or
   * key listeners are disabled.
   * Be aware that clicking the reset button or calling reset() will not bring
   * the actors to life.
   * @param location the location of the cell
   * @return the number of removed actors
   */
  public int removeActorsAt(Location location)
  {
    return removeActorsAt(location, null);
  }

  /**
   * Returns a random location within the game grid.
   * @return a random location
   */
  public Location getRandomLocation()
  {
    int x = (int)(nbHorzCells * Math.random());
    int y = (int)(nbVertCells * Math.random());
    return new Location(x, y);
  }

  /**
   * Returns a random direction 0..360 degrees.
   * @return a random direction
   */
  public double getRandomDirection()
  {
    return 360 * Math.random();
  }

  /**
   * Returns an empty random location within the game grid.
   * An empty location is where are no actors.
   * Be aware that this may take some time in a big game grid that is
   * densely populated.
   * @return an empty random location or null, if there is no 
   * empty location
   */
  public Location getRandomEmptyLocation()
  {
    synchronized (monitor)
    {
      Location location = getRandomLocation();

      int x = location.x;
      int y = location.y;
      Location loc;

      for (int i = 0; i < nbHorzCells; i++)
      {
        for (int k = 0; k < nbVertCells; k++)
        {
          loc = getTorusLocation(new Location(x + i, y + k));
          if (getActorsAt(loc).isEmpty())
            return loc;
        }
      }
    }
    return null;
  }

  private Location getTorusLocation(Location loc)
  {
    return new Location(loc.x % nbHorzCells, loc.y % nbVertCells);
  }

  /**
   * Returns true, if the game is running.
   * @return true, if the game runs; false, if not yet started or paused
   */
  public boolean isRunning()
  {
    return isRunning;
  }

  /**
   * Returns true, if the game is paused
   * @return true, if the game was started and then paused; false, if not yet started or running
   */
  public boolean isPaused()
  {
    return isPaused;
  }

  private void checkActorCollision(Actor actor, ArrayList<Actor> collisionActors)
  {
    ArrayList<Actor> tmp;
    // Use copy to avoid concurrency
    synchronized (actor)
    {
      tmp = new ArrayList<Actor>(collisionActors);
    }
    if (actor.isActorCollisionRearmed())
      for (Actor a : tmp)
        if (isActorColliding(actor, a))
        {
          actor.notifyActorCollision(a);
          if (SharedConstants.DEBUG != SharedConstants.DEBUG_LEVEL_OFF)
            System.out.println("collision detected");
        }
  }

  /**
   * Checks if the two given actors are colliding.
   * @param a1 the first actor
   * @param a2 the second actor
   * @return true, if the two actors are currently colliding; otherwise false
   */
  public boolean isActorColliding(Actor a1, Actor a2)
  {
    int id1 = a1.getIdVisible();
    int id2 = a2.getIdVisible();
//    System.out.println("id1: " + id1);
//    System.out.println("id2: " + id2);
    if (id1 == -1 || id2 == -1)  // Not both visible
      return false;
    GGRectangle rect1 = a1.getCurrentCollisionRectangle(id1);
    GGRectangle rect2 = a2.getCurrentCollisionRectangle(id2);
    GGCircle circle1 = a1.getCurrentCollisionCircle(id1);
    GGCircle circle2 = a2.getCurrentCollisionCircle(id2);
    GGLine line1 = a1.getCurrentCollisionLine(id1);
    GGLine line2 = a2.getCurrentCollisionLine(id2);
    GGVector spot1 = a1.getCurrentCollisionSpot(id1);
    GGVector spot2 = a2.getCurrentCollisionSpot(id2);
    CollisionType type1 = a1.getCurrentCollisionType(id1);
    CollisionType type2 = a2.getCurrentCollisionType(id2);

    if (SharedConstants.DEBUG != SharedConstants.DEBUG_LEVEL_OFF)
    {
      System.out.println("check collision between " + a1 + " id: " + id1 + " type: " + type1 + " and");
      System.out.println(a2 + " id: " + id2 + " type: " + type2);
    }

    if (type1 == CollisionType.CIRCLE && type2 == CollisionType.CIRCLE)
    {
      return circle1.isIntersecting(circle2);
    }

    if (type1 == CollisionType.CIRCLE && type2 == CollisionType.RECTANGLE)
    {
      if (a2.isRotatable())
        return circle1.isIntersecting(rect2);
      else
      {
        Rectangle r2 = rect2.getAWTRectangle();
        return circle1.isIntersecting(r2);
      }
    }

    if (type1 == CollisionType.RECTANGLE && type2 == CollisionType.CIRCLE)
    {
      if (a1.isRotatable())
        return circle2.isIntersecting(rect1);
      else
      {
        Rectangle r1 = rect1.getAWTRectangle();
        return circle2.isIntersecting(r1);
      }
    }

    if (type1 == CollisionType.RECTANGLE && type2 == CollisionType.RECTANGLE)
    {
      if (a1.isRotatable() || a2.isRotatable())  // use advanced collision detection (SAT)
      {
        // For better efficiency we check first if the rectangles are far away
        double circumradius1 = rect1.getCircumradius();
        double circumradius2 = rect2.getCircumradius();
        GGVector v1 = new GGVector((toPoint(a1.getLocation())).x, (toPoint(a1.getLocation())).y);
        GGVector v2 = new GGVector((toPoint(a2.getLocation())).x, (toPoint(a2.getLocation())).y);
        double distance = v1.sub(v2).magnitude();
        if (distance > circumradius1 + circumradius2)
          return false;
        else
          return rect1.isIntersecting(rect2);
      }
      else // Use standard collision detection with java.awt.Rectangle
      {
        Rectangle r1 = rect1.getAWTRectangle();
        Rectangle r2 = rect2.getAWTRectangle();
        return r1.intersects(r2);
      }
    }

    if (type1 == CollisionType.RECTANGLE && type2 == CollisionType.SPOT)
    {
      return rect1.isIntersecting(spot2, a1.isRotatable());
    }

    if (type1 == CollisionType.SPOT && type2 == CollisionType.RECTANGLE)
    {
      return rect2.isIntersecting(spot1, a2.isRotatable());
    }

    if (type1 == CollisionType.CIRCLE && type2 == CollisionType.SPOT)
    {
      return circle1.isIntersecting(spot2);
    }

    if (type1 == CollisionType.SPOT && type2 == CollisionType.CIRCLE)
    {
      return circle2.isIntersecting(spot1);
    }

    if (type1 == CollisionType.SPOT && type2 == CollisionType.SPOT)
    {
      GGCircle c1 = new GGCircle(spot1, 1);
      GGCircle c2 = new GGCircle(spot2, 1);
      return c1.isIntersecting(c2);
    }

    if (type1 == CollisionType.SPOT && type2 == CollisionType.IMAGE)
    {
      return spot1.isIntersecting(
        a2.getCurrentImageCenter(id2), a2.getCurrentImageDirection(id2), a2.getCurrentImage(id2), a2.isRotatable());
    }

    if (type1 == CollisionType.IMAGE && type2 == CollisionType.SPOT)
    {
      return spot2.isIntersecting(
        a1.getCurrentImageCenter(id1), a1.getCurrentImageDirection(id1), a1.getCurrentImage(id1), a1.isRotatable());
    }

    if (type1 == CollisionType.CIRCLE && type2 == CollisionType.IMAGE)
    {
      return circle1.isIntersecting(
        a2.getCurrentImageCenter(id2), a2.getCurrentImageDirection(id2), a2.getCurrentImage(id2), a2.isRotatable());
    }

    if (type1 == CollisionType.IMAGE && type2 == CollisionType.CIRCLE)
    {
      return circle2.isIntersecting(
        a1.getCurrentImageCenter(id1), a1.getCurrentImageDirection(id1), a1.getCurrentImage(id1), a1.isRotatable());
    }

    if (type1 == CollisionType.RECTANGLE && type2 == CollisionType.LINE)
    {
      return rect1.isIntersecting(line2);
    }

    if (type1 == CollisionType.LINE && type2 == CollisionType.RECTANGLE)
    {
      return rect2.isIntersecting(line1);
    }

    if (type1 == CollisionType.CIRCLE && type2 == CollisionType.LINE)
    {
      return line2.isIntersecting(circle1);
    }

    if (type1 == CollisionType.LINE && type2 == CollisionType.CIRCLE)
    {
      return line1.isIntersecting(circle2);
    }

    if (type1 == CollisionType.LINE && type2 == CollisionType.LINE)
    {
      return line1.isIntersecting(line2);
    }

    if (type1 == CollisionType.LINE && type2 == CollisionType.SPOT)
    {
      return line1.isIntersecting(spot2, 1E-6);
    }

    if (type1 == CollisionType.SPOT && type2 == CollisionType.LINE)
    {
      return line2.isIntersecting(spot1, 1E-6);
    }

    if (type1 == CollisionType.LINE && type2 == CollisionType.IMAGE)
    {
      return line1.isIntersecting(a2.getCurrentImageCenter(id2), a2.getCurrentImageDirection(id2), a2.getCurrentImage(id2), a2.isRotatable());
    }

    if (type1 == CollisionType.IMAGE && type2 == CollisionType.LINE)
    {
      return line2.isIntersecting(a1.getCurrentImageCenter(id1), a1.getCurrentImageDirection(id1), a1.getCurrentImage(id1), a1.isRotatable());
    }
    return false;
  }

  private void checkTileCollision(Actor actor, ArrayList<Location> collisionTiles)
  {
    ArrayList<Location> tmp;
    // Use copy to avoid concurrency
    synchronized (actor)
    {
      tmp = new ArrayList<Location>(collisionTiles);
    }

    if (actor.isTileCollisionRearmed())
      for (Location loc : tmp)
        if (isTileColliding(actor, loc))
        {
          actor.notifyTileCollision(loc);
          if (SharedConstants.DEBUG != SharedConstants.DEBUG_LEVEL_OFF)
            System.out.println("collision detected");
        }
  }

  /**
   * Checks if the given actor and the tile at the given tile location are colliding.
   * @param a the actor to check for collision
   * @param location the tile location
   * @return true, if the actor and the tile are currently colliding; false, if
   * no collision or tile map not created
   */
  public boolean isTileColliding(Actor a, Location location)
  {
    if (tileMap == null)
      return false;

    int id = a.getIdVisible();
    if (id == -1)  // Not visible
      return false;

    if (!(tileMap.isTileCollisionEnabled(location)))
      return false;

    GGRectangle rect1 = a.getCurrentCollisionRectangle(id);
    GGRectangle rect2 = tileMap.getCurrentCollisionRectangle(location);
    GGCircle circle1 = a.getCurrentCollisionCircle(id);
    GGCircle circle2 = tileMap.getCurrentCollisionCircle(location);
    GGLine line1 = a.getCurrentCollisionLine(id);
    GGLine line2 = tileMap.getCurrentCollisionLine(location);
    GGVector spot1 = a.getCurrentCollisionSpot(id);
    GGVector spot2 = tileMap.getCurrentCollisionSpot(location);
    CollisionType type1 = a.getCurrentCollisionType(id);
    CollisionType type2 = tileMap.getCurrentCollisionType(location);

    if (SharedConstants.DEBUG != SharedConstants.DEBUG_LEVEL_OFF)
    {
      System.out.println("check collision between " + a + " id: " + id + " type: " + type1);
      System.out.println(" and tile at location" + location + " type: " + type2);
    }

    if (type1 == CollisionType.CIRCLE && type2 == CollisionType.CIRCLE)
    {
      return circle1.isIntersecting(circle2);
    }

    if (type1 == CollisionType.CIRCLE && type2 == CollisionType.RECTANGLE)
    {
      Rectangle r2 = rect2.getAWTRectangle();
      return circle1.isIntersecting(r2);
    }

    if (type1 == CollisionType.RECTANGLE && type2 == CollisionType.CIRCLE)
    {
      if (a.isRotatable())
        return circle2.isIntersecting(rect1);
      else
      {
        Rectangle r1 = rect1.getAWTRectangle();
        return circle2.isIntersecting(r1);
      }
    }

    if (type1 == CollisionType.RECTANGLE && type2 == CollisionType.RECTANGLE)
    {
      if (a.isRotatable())  // use advanced collision detection (SAT)
      {
        // For better efficiency we check first if the rectangles are far away
        double circumradius1 = rect1.getCircumradius();
        double circumradius2 = rect2.getCircumradius();
        GGVector v1 = new GGVector((toPoint(a.getLocation())).x, (toPoint(a.getLocation())).y);
        GGVector v2 = new GGVector(tileMap.getCenter(location));
        double distance = v1.sub(v2).magnitude();
        if (distance > circumradius1 + circumradius2)
          return false;
        else
          return rect1.isIntersecting(rect2);
      }
      else // Use standard collision detection with java.awt.Rectangle
      {
        Rectangle r1 = rect1.getAWTRectangle();
        Rectangle r2 = rect2.getAWTRectangle();
        return r1.intersects(r2);
      }
    }

    if (type1 == CollisionType.RECTANGLE && type2 == CollisionType.SPOT)
    {
      return rect1.isIntersecting(spot2, a.isRotatable());
    }

    if (type1 == CollisionType.SPOT && type2 == CollisionType.RECTANGLE)
    {
      return rect2.isIntersecting(spot1, false);
    }

    if (type1 == CollisionType.CIRCLE && type2 == CollisionType.SPOT)
    {
      return circle1.isIntersecting(spot2);
    }

    if (type1 == CollisionType.SPOT && type2 == CollisionType.CIRCLE)
    {
      return circle2.isIntersecting(spot1);
    }

    if (type1 == CollisionType.SPOT && type2 == CollisionType.SPOT)
    {
      GGCircle c1 = new GGCircle(spot1, 1);
      GGCircle c2 = new GGCircle(spot2, 1);
      return c1.isIntersecting(c2);
    }

    if (type1 == CollisionType.RECTANGLE && type2 == CollisionType.LINE)
    {
      return rect1.isIntersecting(line2);
    }

    if (type1 == CollisionType.LINE && type2 == CollisionType.RECTANGLE)
    {
      return rect2.isIntersecting(line1);
    }

    if (type1 == CollisionType.CIRCLE && type2 == CollisionType.LINE)
    {
      return line2.isIntersecting(circle1);
    }

    if (type1 == CollisionType.LINE && type2 == CollisionType.CIRCLE)
    {
      return line1.isIntersecting(circle2);
    }

    if (type1 == CollisionType.LINE && type2 == CollisionType.LINE)
    {
      return line1.isIntersecting(line2);
    }

    if (type1 == CollisionType.SPOT && type2 == CollisionType.LINE)
    {
      return line2.isIntersecting(spot1, 1E-6);
    }

    if (type1 == CollisionType.LINE && type2 == CollisionType.SPOT)
    {
      return line1.isIntersecting(spot2, 1E-6);
    }
    return false;
  }

  /**
   * Play a distributed sound sample with maximum volume using the default sound device.
   * Deprecated:  Use playSound(GGSound sound) instead.<br><br>
   * {@link ch.aplu.jgamegrid.GameGrid#playSound(GGSound sound)}
   * @param sound the named sound sample from the GGSound enumeration
   * @param obj object whose class loader is used to load the resource
   */
  public SoundPlayer playSound(Object obj, GGSound sound)
  {
    SoundPlayer player = new SoundPlayer(obj, sound.getPath());
    player.setVolume(1000);
    player.play();
    return player;
  }

  /**
   * Plays a distributed sound sample with maximum volume using the default sound device.
   * Specify the sound indices defined as class constants.
   * The methods returns immediately. More than one sound may be played at the same time.
   * The return value may be used for calling methods of the SoundPlayer class, e.g. setVolume().<br>
   * To start playing immediately, play a dummy sound before.
   * @param sound the named sound sample from the GGSound enumeration
   */
  public SoundPlayer playSound(GGSound sound)
  {
    URL url = Thread.currentThread().getContextClassLoader().
      getResource(sound.getPath());
    //    URL url = ClassLoader.getSystemResource(sound.getPath()); // not found with Webstart
    SoundPlayer player = new SoundPlayer(url);
    player.setVolume(1000);
    player.play();
    return player;
  }

  /**
   * Play continously a distributed sound sample with maximum volume using the default sound device.
   * Deprecated:  Use playLoop(GGSound sound) instead.<br><br>
   * {@link ch.aplu.jgamegrid.GameGrid#playLoop(GGSound sound)}
   * @param sound the named sound sample from the GGSound enumeration
   * @param obj object whose class loader is used to load the resource
   */
  public SoundPlayer playLoop(Object obj, GGSound sound)
  {
    SoundPlayer player = new SoundPlayer(obj, sound.getPath());
    player.setVolume(1000);
    player.playLoop();
    return player;
  }

  /**
   * Play continously a distributed sound sample with maximum volume using the default sound device.
   * Specify the sound indices defined as class constants.
   * The methods returns immediately. More than one sound may be played at the same time.
   * The return value may be used for calling methods of the SoundPlayer class, e.g. stop().<br>
   * To start playing immediately, play a dummy sound before.
   * @param sound the named sound sample from the GGSound enumeration
   */
  public SoundPlayer playLoop(GGSound sound)
  {
    URL url = Thread.currentThread().getContextClassLoader().
      getResource(sound.getPath());
    //   URL url = ClassLoader.getSystemResource(sound.getPath());  // not found with Webstart
    SoundPlayer player = new SoundPlayer(url);
    player.setVolume(1000);
    player.playLoop();
    return player;
  }

  private SoundPlayer getSoundPlayer(Object obj, String audioPathname, boolean isExt)
  {
    String userHome
      = (System.getProperty("user.home").toLowerCase().contains("%userprofile%"))
      ? ((System.getenv("USERPROFILE") == null) ? System.getProperty("java.io.tmpdir")
      : System.getenv("USERPROFILE")) : System.getProperty("user.home");
    String FS = System.getProperty("file.separator");
    String userPath = userHome + FS + "gamegrid" + FS + audioPathname;
    File audioFile = new File(userPath);

    InputStream is = null;
    if (obj != null)
      is = obj.getClass().getResourceAsStream(audioPathname);
    SoundPlayer player = null;
    try
    {
      if (isExt)
      {
        if (is != null)
          player = new SoundPlayerExt(obj, audioPathname);
        else
        {
          if (audioFile.exists())
            player = new SoundPlayerExt(audioFile);
          else
            player = new SoundPlayerExt(new File(audioPathname));
        }
      }
      else
      {
        if (is != null)
          player = new SoundPlayer(obj, audioPathname);
        else
        {
          if (audioFile.exists())
            player = new SoundPlayer(audioFile);
          else
            player = new SoundPlayer(new File(audioPathname));
        }
      }
    }
    catch (IllegalArgumentException ex)
    {
      fail("Error while loading sound resource\n" + audioPathname + "\nApplication will terminate.");
    }
    return player;
  }

  /**
   * Play a sound sample from a JAR resource of the specified object
   * with maximum volume using the default sound device.
   * The methods returns immediately. More than one sound may be played at the same time.
   * The return value may be used for calling methods of the SoundPlayer class, e.g. setVolume().
   * Only WAV files are supported. For MP3 use playSoundExt().<br>
   * To start playing immediately, play a dummy sound before.<br>
   * From the given audioPathname the sound file is searched in the following order:<br>
   * - if application is packed into a jar archive, relative to the root of the jar archive<br>
   * - relative to the directory &lt;userhome&gt;/gamegrid/<br>
   * - relative or absolute to current application directory<br>
   * @param obj object whose class loader is used to load the resource
   * @param audioPathname path to the sound file
   */
  public SoundPlayer playSound(Object obj, String audioPathname)
  {
    SoundPlayer player = getSoundPlayer(obj, audioPathname, false);
    player.setVolume(1000);
    player.play();
    return player;
  }

  /**
   * Play a sound sample from given audio file with maximum volume using the default sound device
   * using the current class loader to load the resource.
   * The methods returns immediately. More than one sound may be played at the same time.
   * The return value may be used for calling methods of the SoundPlayer class, e.g. setVolume().
   * Only WAV files are supported. For MP3 use playSoundExt().<br>
   * To start playing immediately, play a dummy sound before.<br>
   * From the given audioPathname the sound file is searched in the following order:<br>
   * - relative to the directory &lt;userhome&gt;/gamegrid/<br>
   * - relative or absolute to current application directory<br>
   * @param audioPathname path to the sound file
   */
  public SoundPlayer playSound(String audioPathname)
  {
    return playSound(null, audioPathname);
  }

  /**
   * Play continuously a sound sample from a JAR resource of the specified object with maximum volume using the default sound device.
   * The methods returns immediately. More than one sound may be played at the same time.
   * The return value may be used for calling methods of the SoundPlayer class, e.g. stop().
   * Only WAV files are supported. For MP3 use playLoopExt().<br>
   * To start playing immediately, play a dummy sound before.<br>
   * If the audio file is not found in the JAR resource, it is searched in the following order:<br>
   * - relative to the directory &lt;userhome&gt;/gamegrid/<br>
   * - relative or absolute to current application directory<br>
   * @param obj object whose class loader is used to load the resource
   * @param audioPathname path to the sound file
   */
  public SoundPlayer playLoop(Object obj, String audioPathname)
  {
    SoundPlayer player = getSoundPlayer(obj, audioPathname, false);
    player.setVolume(1000);
    player.playLoop();
    return player;
  }

  /**
   * Play continuously a sound sample with maximum volume using the default sound device
   * using the current class loader to load the resource.
   * The methods returns immediately. More than one sound may be played at the same time.
   * The return value may be used for calling methods of the SoundPlayer class, e.g. stop().
   * From the given audioPathname the sound file is searched in the following order:<br>
   * To start playing immediately, play a dummy sound before.<br>
   * - if application is packed into a jar archive, relative to the root of the jar archive<br>
   * - relative to the directory &lt;userhome&gt;/gamegrid/<br>
   * - relative or absolute to current application directory<br>
   * @param audioPathname path to the sound file
   */
  public SoundPlayer playLoop(String audioPathname)
  {
    return playLoop(null, audioPathname);
  }

  /**
   * Same as playSound(obj, audioPathname) but supports MP3.
   * The following JARs (or later versions) must be included in the project:
   * - jl1.0.1.jar<br>
   * - mp3spi1.9.4.jar<br>
   * - tritonius_share.jar<br>
   * Download from www.javazoom.net or www.sourceforge.net.<br>
   * Download of redistribution from www.aplu.ch/mp3support.
   */
  public SoundPlayerExt playSoundExt(Object obj, String audioPathname)
  {
    SoundPlayerExt player = (SoundPlayerExt)getSoundPlayer(obj, audioPathname, true);
    player.setVolume(1000);
    player.play();
    return player;
  }

  /**
   * Same as playSound(audioPathname) but supports MP3.
   * The following JARs (or later versions) must be included in the project:
   * - jl1.0.1.jar<br>
   * - mp3spi1.9.4.jar<br>
   * - tritonius_share.jar<br>
   * Download from www.javazoom.net or www.sourceforge.net.<br>
   * Download of redistribution from www.aplu.ch/mp3support.
   */
  public SoundPlayerExt playSoundExt(String audioPathname)
  {
    return playSoundExt(null, audioPathname);
  }

  /**
   * Same as playLoop(obj, audioPathname) but supports MP3.
   * The following JARs (or later versions) must be included in the project:
   * - jl1.0.1.jar<br>
   * - mp3spi1.9.4.jar<br>
   * - tritonius_share.jar<br>
   * Download from www.javazoom.net or www.sourceforge.net.<br>
   * Download of redistribution from www.aplu.ch/mp3support.
   */
  public SoundPlayerExt playLoopExt(Object obj, String audioPathname)
  {
    SoundPlayerExt player = (SoundPlayerExt)getSoundPlayer(obj, audioPathname, true);
    player.setVolume(1000);
    player.playLoop();
    return player;
  }

  /**
   * Same as playLoop(audioPathname) but supports MP3.
   * The following JARs (or later versions) must be included in the project:
   * - jl1.0.1.jar<br>
   * - mp3spi1.9.4.jar<br>
   * - tritonius_share.jar<br>
   * Download from www.javazoom.net or www.sourceforge.net.<br>
   * Download of redistribution from www.aplu.ch/mp3support.
   */
  public SoundPlayerExt playLoopExt(String audioPathname)
  {
    return playLoopExt(null, audioPathname);
  }

  /**
   * Removes the given mouse listener from the list of registered
   * mouse listeners.
   * @param listener the listener to remove
   * @return true, if the listener is successfully removed; false, if the
   * listener is not part of the mouse listener list
   */
  public boolean removeMouseListener(GGMouseListener listener)
  {
    synchronized (monitor)
    {
      int index = mouseListeners.indexOf(listener);
      if (index == -1)
        return false;
      mouseListeners.remove(index);
      mouseEventMasks.remove(index);
    }
    return true;
  }

  /**
   * Adds a GGMouseListener to get notifications from mouse events. More than
   * one GGMouseListener may be registered. They are called in the order they
   * are added. Be aware that if the notification method mouseEvent() returns true,
   * the event is "consumed" and no following GGMouseistener will be notified.
   * Only the events defined as OR-combination in the specified mask
   * are notified. If the mouse was disabled, it is enabled now.
   * @param listener the GGMouseListener to register
   * @param mouseEventMask an OR-combinaton of constants defined in class GGMouse
   */
  public void addMouseListener(GGMouseListener listener, int mouseEventMask)
  {
    synchronized (monitor)
    {
      mouseListeners.add(listener);
      mouseEventMasks.add(mouseEventMask);
      mouseAdapter = new MyMouseAdapter();
      if (!isMouseListenerAdded && (mouseEventMask & GGMouse.lPress) != 0
        | (mouseEventMask & GGMouse.rPress) != 0
        | (mouseEventMask & GGMouse.lRelease) != 0
        | (mouseEventMask & GGMouse.rRelease) != 0
        | (mouseEventMask & GGMouse.lClick) != 0
        | (mouseEventMask & GGMouse.rClick) != 0
        | (mouseEventMask & GGMouse.lDClick) != 0
        | (mouseEventMask & GGMouse.rDClick) != 0)
      {
        isMouseListenerAdded = true;
        EventQueue.invokeLater(new Runnable()
        {
          public void run()
          {
            addMouseListener(mouseAdapter);
          }
        });
        isMouseEnabled = true;
      }
      if (!isMouseMotionListenerAdded && (mouseEventMask & GGMouse.lDrag) != 0
        | (mouseEventMask & GGMouse.rDrag) != 0
        | (mouseEventMask & GGMouse.enter) != 0
        | (mouseEventMask & GGMouse.leave) != 0
        | (mouseEventMask & GGMouse.move) != 0)
      {
        EventQueue.invokeLater(new Runnable()
        {
          public void run()
          {
            if (!isMouseListenerAdded)
            {
              isMouseListenerAdded = true;
              addMouseListener(mouseAdapter);   // Necessary for enter/leave
            }
            isMouseMotionListenerAdded = true;
            addMouseMotionListener(mouseAdapter);
            isMouseEnabled = true;
          }
        });
      }
    }
  }

  /**
   * Enable/disable all mouse event callbacks.
   * @param enabled if true, the registered callbacks are enabled; otherwise disabled
   */
  public void setMouseEnabled(boolean enabled)
  {
    isMouseEnabled = enabled;
  }

  /**
   * Returns the x-y-coordinates of the center of the cell with given
   * location (cell indices).
   * @param location the indices of the cell
   * @return the x-y-coordinates (as point) of the cell's center.
   */
  public Point toPoint(Location location)
  {
    int x = cellSize / 2 + location.x * cellSize;
    int y = cellSize / 2 + location.y * cellSize;
    return new Point(x, y);
  }

  /**
   * Returns the location (cell indices) of the cell where the given point
   * resides. If the point is outside the grid, returns the location with
   * cell indices outside the valid range.
   * @param pt a point of pixels coordinates
   * @return the location of the cell (cell indices) where the point resides
   */
  public Location toLocation(Point pt)
  {
    return toLocation(pt.x, pt.y);
  }

  /**
   * Returns the location (cell indices) of the cell where the point
   * with given coordinates resides.
   * If the point is outside the grid, returns the location with
   * cell indices outside the valid range.
   * @param x x-coordinate (in pixels)
   * @param y y-coordinate (in pixels)
   * @return the location of the cell (cell indices) where the point resides
   */
  public Location toLocation(int x, int y)
  {
    int xCell, yCell;
    if (x < 0)
      xCell = ((x + 1) / cellSize) - 1;
    else
      xCell = x / cellSize;
    if (y < 0)
      yCell = ((y + 1) / cellSize) - 1;
    else
      yCell = y / cellSize;
    return new Location(xCell, yCell);
  }

  /**
   * Returns the location (cell indices) of the cell where the given point
   * resides. If the point is outside the grid, returns the closest location within the grid.
   * @param pt a point of pixels coordinates
   * @return the location of the cell (cell indices) where the point resides
   */
  public Location toLocationInGrid(Point pt)
  {
    return toLocationInGrid(pt.x, pt.y);
  }

  /**
   * Returns the location (cell indices) of the cell where the point
   * with given coordinates resides.
   * Despite in a strict sense the pixels of the right and bottom border belongs to cells outside
   * the visible cells, the location of the cooresponding visible border cell is returned.
   * If the point is outside the grid, the closest location within the grid is returned.
   * @param x x-coordinate (in pixels)
   * @param y y-coordinate (in pixels)
   * @return the location of the cell (cell indices) where the point resides
   */
  public Location toLocationInGrid(int x, int y)
  {
    if (x < 0)
      x = 0;
    if (x > pgWidth - 1)
      x = pgWidth - 1;
    if (y < 0)
      y = 0;
    if (y > pgHeight - 1)
      y = pgHeight - 1;
    return toLocation(x, y);
  }

  /**
   * Puts the given actor at last place in the corresponding scene list,
   * in order to act first and to be drawn on top of other actors
   * <b>of the same class</b>.
   * If you want to change the paint order of actors in different actor classes,
   * use GameGrid.setPaintOrder().
   * @see ch.aplu.jgamegrid.GameGrid#setPaintOrder(Class... classes)
   * @param actor the actor to put on top
   */
  public void setActorOnTop(Actor actor)
  {
    synchronized (monitor)
    {
      Vector<Actor> scene = getScene(actor.getClass());
      if (scene == null)
        return;

      int index = scene.indexOf(actor);
      scene.add(actor);
      scene.remove(index);
    }
  }

  /**
   * Puts the given actor at first place in the corresponding scene list,
   * in order to act last and to be drawn on the bottom of other actors
   * <b> of the same class</b>.
   * If you want to change the paint order of actors in different actor classes,
   * use GameGrid.setPaintOrder().
   * @see ch.aplu.jgamegrid.GameGrid#setPaintOrder(Class... classes)
   * @param actor the actor to put on the bottom
   */
  public void setActorOnBottom(Actor actor)
  {
    synchronized (monitor)
    {
      Vector<Actor> scene = getScene(actor.getClass());
      if (scene == null)
        return;

      int index = scene.indexOf(actor);
      scene.add(0, actor);
      scene.remove(index);
    }
  }

  /**
   * Shifts (rolls) the actors of the given actor list  in the corresponding
   * scene list forward or backward. When shifting forward, the last element
   * is reinserted at the beginning, when shifting backward, the first element
   * is reinserted at the end. The scene list determines the act and paint
   * order (act and paint order from first to last element. Last element
   * is shown on top.) <b>All actors must have the same type</b> (determined
   * by the first element of the list).
   * @param actors a list of actors to shift
   * @param forward if true, shifts to the right (forward); otherwise
   * shifts to the left (backward)
   * @return the actor put at top; null, if none of the actors is part of the
   * scene list
   */
  public Actor shiftSceneOrder(ArrayList<Actor> actors, boolean forward)
  {
    if (actors.isEmpty())
      return null;
    synchronized (monitor)
    {
      ArrayList<Actor> tmp = new ArrayList<Actor>();
      ArrayList<Integer> tmpIndexes = new ArrayList<Integer>();
      Vector<Actor> scene = getScene(actors.get(0).getClass());
      if (scene == null)
        return null;

      // Copy all actors concerned in tmp list and their indices in tmpIndexes
      for (int i = 0; i < scene.size(); i++)
      {
        Actor actor = scene.get(i);
        if (actors.contains(actor))
        {
          tmp.add(actor);
          tmpIndexes.add(i);
        }
      }
      int size = tmp.size();

      // Roll list tmp (but not tmpIndexes)
      if (forward)
      {
        Actor last = tmp.get(size - 1);
        for (int i = size - 2; i >= 0; i--)
          tmp.set(i + 1, tmp.get(i));
        tmp.set(0, last);
      }
      else
      {
        Actor first = tmp.get(0);
        for (int i = 1; i < size; i++)
          tmp.set(i - 1, tmp.get(i));
        tmp.set(size - 1, first);
      }

      // Copy list back in scene list
      for (int i = 0; i < size; i++)
        scene.set(tmpIndexes.get(i), tmp.get(i));

      return tmp.get(size - 1);
    }
  }

  /**
   * Reverses the order of the given actor list in the corresponding
   * scene list. The scene list determines the act and paint
   * order (act and paint order from first to last element. Last element
   * is shown on top.) <b>All actors must have the same type</b> (determined
   * by the first element of the list).
   * @param actors a list of actors to shift
   * @return the actor put at top; null, if none of the actors is part of the
   * scene list
   */
  public Actor reverseSceneOrder(ArrayList<Actor> actors)
  {
    if (actors.isEmpty())
      return null;
    synchronized (monitor)
    {
      ArrayList<Actor> tmp = new ArrayList<Actor>();
      ArrayList<Integer> tmpIndexes = new ArrayList<Integer>();
      Vector<Actor> scene = getScene(actors.get(0).getClass());
      if (scene == null)
        return null;

      // Copy all actors concerned in tmp list and their indices in tmpIndexes
      for (int i = 0; i < scene.size(); i++)
      {
        Actor actor = scene.get(i);
        if (actors.contains(actor))
        {
          tmp.add(actor);
          tmpIndexes.add(i);
        }
      }
      int size = tmp.size();
      ArrayList<Actor> tmp1 = new ArrayList<Actor>(size);

      // Reverse list
      for (int i = size - 1; i >= 0; i--)
        tmp1.add(tmp.get(i));

      // Copy list back in scene list
      for (int i = 0; i < size; i++)
        scene.set(tmpIndexes.get(i), tmp1.get(i));

      return tmp1.get(size - 1);
    }
  }

  /**
   * Sets the act and paint order of the actors in the given list by
   * rearraging their scene list to the order in the given list. (Actors will act
   * and painted in the order of their scene list. Actors painted later will
   * appear on top of actors painted before.)
   * All actors must have the same type (determined by the first element of the list).
   * Duplicated actors in the given list are ignored.
   * To change the act or paint order of the actors class, use setActOrder()
   * or setPaintOrder().
   * @param actors a list of actors to rearrange
   */
  public void setSceneOrder(ArrayList<Actor> actors)
  {
    if (actors.isEmpty())
      return;

    synchronized (monitor)
    {
      ArrayList<Actor> tmp = new ArrayList<Actor>();
      ArrayList<Integer> tmpIndexes = new ArrayList<Integer>();
      Vector<Actor> scene = getScene(actors.get(0).getClass());
      if (scene == null)
        return;

      // Copy all the indices of all actors concerned
      for (int i = 0; i < scene.size(); i++)
      {
        Actor actor = scene.get(i);
        if (actors.contains(actor) && !tmp.contains(actor))
        {
          tmp.add(actor);
          tmpIndexes.add(i);
        }
      }

      // Copy actor list back in scene list, 
      // only if index still valid (actor not remove by another thread meanwhile)
      try
      {
        for (int i = 0; i < actors.size(); i++)
          scene.set(tmpIndexes.get(i), actors.get(i));
      }
      catch (IndexOutOfBoundsException ex)
      {
      }
    }
  }

  /**
   * Returns a list with actor references of the given class in the order they 
   * are painted. (Actors painted later will appear on top of actors painted before.)
   * @param clazz the class of actors included in the list; if null, all actors are included
   * @return a list of actor references
   */
  public ArrayList<Actor> getPaintOrderList(Class clazz)
  {
    ArrayList<Actor> actors = new ArrayList<Actor>();
    Actor actor;
    synchronized (monitor)
    {
      for (int i = 0; i < paintOrder.size(); i++)
      {
        int sceneIndex = paintOrder.get(i);
        {
          Vector<Actor> tmp = new Vector<Actor>(sceneList.get(sceneIndex));
          for (int k = 0; k < tmp.size(); k++)
          {
            actor = tmp.get(k);
            if (clazz == null || actor.getClass() == clazz)
              actors.add(tmp.get(k));
          }
        }
      }
    }
    return actors;
  }

  /**
   * Returns a list with actor references in the order they are painted.
   * (Actors painted later will appear on top of actors painted before.)
   * @return a list of actor references
   */
  public ArrayList<Actor> getPaintOrderList()
  {
    return getPaintOrderList(null);
  }

  /**
   * Returns a list with actor references of all actors of the given class whose
   * touched area (of type IMAGE, RECTANGLE or CIRCLE) intersects with the current mouse
   * cursor location.
   * @param clazz the class of actors checked for a touch. If null, all
   * actors are checked.
   * @return a list of actor references. (Empty, if no actors are touched)
   */
  public ArrayList<Actor> getTouchedActors(Class clazz)
  {
    Point pt = getMousePosition();
    ArrayList<Actor> touchedActors = new ArrayList<Actor>();
    if (pt == null)  // ouside window
      return touchedActors;  // Empty list
    ArrayList<Actor> actors = getActors(clazz);
    for (Actor actor : actors)
    {
      GGVector v = new GGVector(pt);
      v = v.sub(new GGVector(actor.getLocationOffset()));
      int idVisible = actor.getIdVisible();
      if (idVisible == -1)
        continue;
      boolean intersects = false;
      switch (actor.getCurrentInteractionType(idVisible))
      {
        case IMAGE:
          intersects = v.isIntersecting(actor.getCurrentImageCenter(idVisible),
            actor.getCurrentImageDirection(idVisible),
            actor.getCurrentImage(idVisible),
            actor.isRotatable());
          break;
        case RECTANGLE:
          intersects = actor.getCurrentInteractionRectangle(idVisible).
            isIntersecting(v, actor.isRotatable());
          break;
        case CIRCLE:
          intersects = actor.getCurrentInteractionCircle(idVisible).
            isIntersecting(v);
          break;
      }
      if (intersects)
        touchedActors.add(actor);
    }
    return touchedActors;
  }

  protected Actor getMouseTouchTop(Class clazz)
  {
    Point pt = getMousePosition();
    if (pt == null)  // ouside window
      return null;
    GGVector v = new GGVector(pt);
    ArrayList<Actor> actors = getActors(clazz);
    ArrayList<Actor> touchedActors = new ArrayList<Actor>();
    for (Actor actor : actors)
    {
      v = v.sub(new GGVector(actor.getLocationOffset()));
      int idVisible = actor.getIdVisible();
      if (idVisible == -1)
        continue;
      boolean intersects = false;
      switch (actor.getCurrentInteractionType(idVisible))
      {
        case IMAGE:
          intersects = v.isIntersecting(actor.getCurrentImageCenter(idVisible),
            actor.getCurrentImageDirection(idVisible),
            actor.getCurrentImage(idVisible),
            actor.isRotatable());
          break;
        case RECTANGLE:
          intersects = actor.getCurrentInteractionRectangle(idVisible).
            isIntersecting(v, actor.isRotatable());
          break;
        case CIRCLE:
          intersects = actor.getCurrentInteractionCircle(idVisible).
            isIntersecting(v);
          break;
      }
      if (intersects)
        touchedActors.add(actor);
    }
    int maxIndex = -1;
    Actor topActor = null;
    for (Actor touchedActor : touchedActors)
    {
      if (touchedActor.getMouseTouchListener() != null)
      {
        int index = getPaintOrderList(clazz).indexOf(touchedActor);
        if (index > maxIndex)
        {
          maxIndex = index;
          topActor = touchedActor;
        }
      }
    }
    return topActor;
  }

  // Get the scene for the given class, only called in synchronized block
  private Vector<Actor> getScene(Class clazz)
  {
    synchronized (monitor)
    {
      int sceneIndex = -1;
      // Use copy to avoid concurrency
      Vector<Class> tmp = new Vector<Class>(classList);

      // Search the scene index
      for (int i = 0; i < tmp.size(); i++)
      {
        if (tmp.get(i) == clazz)
        {
          sceneIndex = i;
          break;
        }
      }
      if (sceneIndex == -1)  // class not found
        return null;
      return sceneList.get(sceneIndex);
    }
  }

  /**
   * Stops the game thread to avoid any CPU consumption. The game thread
   * cannot be restarted without creating new GameGrid instance.<br><br>
   * 
   * Normally doPause() is used to stop a simulation, but then
   * some minor CPU time is still consumed. Clicking the close button
   * in the GameGrid title bar stops the game thread automatically.
   */
  public void stopGameThread()
  {
    //   System.out.print("Trying to stop game thread...");
    isGameThreadRunning = false;
    // If called by the thread itself (e.g. from a key callback), it may hang.
    // Better call it from another thread
    new Thread()
    {
      public void run()
      {
        try
        {
          gameThread.join(5000);
          //         System.out.println("Successful");
        }
        catch (InterruptedException ex)
        {
        }
//        if (gameThread.isAlive())
//          System.out.println("Failed");
      }
    }.start();
  }

  /**
   * For internal use only (overrides Canvas.paint() to get
   * paint notifications).
   */
  public void paint(Graphics g)
  {
    if (myFrame == null)
      g.drawString("GameGrid", 10, 10);  //  Show bean designer info

    if (strategy == null)
    {
      createBufferStrategy(2);
      strategy = getBufferStrategy();
    }
    super.paint(g);
    refresh();
  }

  /**
   * Registers the given GGActListener to get act() events.
   * More than one listener may be registered.
   * @param listener the GGActListener to register
   */
  public void addActListener(GGActListener listener)
  {
    synchronized (actListeners)
    {
      actListeners.add(listener);
    }
  }

  /**
   * Creates/Replaces a TileMap with the specified number of horizontal and vertical
   * tiles. All tiles are invisible (images set to null).<br><br>
   * Be aware that tileWidth and tileHeight are given in number of pixels,
   * but the size of the tiles in pixel units is (tileWidth-1) x (tileHeight-1).
   * If the tile map already exists, it is replaced by the new one. To remove
   * a tile map, replace it with a tile map with nbHorzTiles = 0 and nbVertTiles = 0.
   * @param nbHorzTiles the number of horizontal tiles
   * @param nbVertTiles the number of vertical tiles
   * @param tileWidth the width of each tile (in pixels)
   * @param tileHeight the height of each tile (in pixels)
   */
  public GGTileMap createTileMap(int nbHorzTiles, int nbVertTiles, int tileWidth, int tileHeight)
  {
    if (tileMap == null)
      tileMap = new GGTileMap(this);
    tileMap.init(nbHorzTiles, nbVertTiles, tileWidth, tileHeight);
    return tileMap;
  }

  /**
   * Returns the reference to the GGTileMap.
   * @return a reference to the GGTileMap; null, if it is not yet created
   */
  public GGTileMap getTileMap()
  {
    return tileMap;
  }

  /**
   * Returns the number of simulation cycles since last reset.
   * @return the number of simulation cycles
   */
  public int getNbCycles()
  {
    return nbCycles;
  }

  /**
   * Empty method called in every simulation cycle after all actor act() calls.
   * Override to get your own notification.
   */
  public void act()
  {
  }

  /**
   * Empty method called when the reset button is hit or doReset() is called.
   * Override to get your own notification.
   */
  public void reset()
  {
  }

  /**
   * Shows the game grid after initialisation or when hided.
   * Only valid if a GameGrid frame window was created. When
   * show() returns, there is no garantee that the window is already
   * shown, because it is made visible by the Event Dispatch Thread (EDT).
   * An eventually attached status bar becomes visible too.
   */
  public void show()
  {
    if (myFrame == null)
      return;
    EventQueue.invokeLater(new Runnable()
    {
      public void run()
      {
        myFrame.setVisible(true);
        if (deferStatusBar)
        {
          addStatusBarInternal(statusBarHeight);
          deferStatusBar = false;
        }
        if (statusDialog != null)
          statusDialog.setVisible(isStatusBarVisible);
      }
    });

  }

  /**
   * Hides the game grid, but does not destroy it.
   * When hide() returns, there is no garantee that the window is already
   * hidden, because is is made invisible by the Even Dispatch Thread (EDT).
   * An eventually attached status bar is hidden too.
   */
  public void hide()
  {
    EventQueue.invokeLater(new Runnable()
    {
      public void run()
      {
        myFrame.setVisible(false);
      }
    });
  }

  /**
   * Set the title in the window's title bar. The JFrame.setTitle()
   * method is requested to be executed by the Event Dispatch Thread, so
   * after setTitle() returns, there is no garantee that the window
   * title is already set.
   * @param text the text to display
   */
  public void setTitle(final String text)
  {
    EventQueue.invokeLater(new Runnable()
    {
      public void run()
      {
        myFrame.setTitle(text);
      }
    });
  }

  /**
   * Register a navigation listener to get notifications when the navigation
   * panel is used. Only valid if a GameGrid frame window was created.
   * @param listener the GGNavigationListener to register
   */
  public void addNavigationListener(GGNavigationListener listener)
  {
    if (myFrame == null)
      return;
    navigationListener = listener;
  }

  /**
   * Registers the given GGResetListener to get events when the
   * reset button is hit. Only valid if a GameGrid frame window was created.
   * More than one listener may be registered.
   * @param listener the GGActListener to register
   */
  public void addResetListener(GGResetListener listener)
  {
    if (myFrame == null)
      return;
    resetListeners.add(listener);
  }

  /**
   * Registers the given GGExitListener to get an event when the title bar
   * close button is hit. The closing behavior selected by setClosingMode()
   * is ignored.
   * @param listener the GGActListener to register
   */
  public void addExitListener(GGExitListener listener)
  {
    if (myFrame == null)
      return;
    exitListener = listener;
  }

  /**
   * Returns current version information.
   * @return a string with the current version of the JGameGrid library
   */
  public static String getVersion()
  {
    return SharedConstants.VERSION;
  }

  private BufferedImage loadImage(String imagePath)
  {
    BufferedImage image = null;
    URL url = getClass().getClassLoader().getResource(imagePath);

    if (url != null)  // Image found in jar
    {
      try
      {
        image = ImageIO.read(url);
      }
      catch (IOException e)
      {
        // Read error
      }
    }
    return image;
  }

  /**
   * Implementation of the GGExceptionHandler's event method called when
   * an uncaught exception is thrown. The stack trace is shown in a
   * JOptionPane. Override to implement your own exception
   * handling.
   * @param s the stack trace information
   */
  public void getStackTrace(String s)
  {
    if (isFatalError)  // Avoid multiple invocation
      return;
    isFatalError = true;
    JOptionPane.showMessageDialog(null, s,
      "Fatal Error", JOptionPane.ERROR_MESSAGE);
    if (frameClosingMode == ClosingMode.TerminateOnClose
      || frameClosingMode == ClosingMode.AskOnClose)
      System.exit(0);
  }

  /**
   * Move the location of the game grid window to the given screen position.
   * If there is no frame, nothing happens. The JFrame.setLocation()
   * call is requested to be executed by the Event Dispatch Thread, so
   * after setPosition() returns, there is no garantee that the window
   * is already at the requested position.
   * @param ulx the upper left vertex x-coordinate of the window in pixels
   * @param uly the upper left vertex y-coordinate of the window in pixels
   */
  public void setPosition(final int ulx, final int uly)
  {
    if (myFrame == null)
      return;
    EventQueue.invokeLater(new Runnable()
    {
      public void run()
      {
        myFrame.setLocation(ulx, uly);
      }
    });
  }

  /**
   * Returns the the location in screen coordiantes of game grid window.
   * @return the upper left vertex of the window in pixels; null, if
   * there is no frame
   */
  public Point getPosition()
  {
    if (myFrame == null)
      return null;
    final Point loc = new Point();
    if (EventQueue.isDispatchThread())
    {
      loc.setLocation(myFrame.getLocation());
    }
    else
    {
      try
      {
        // Must wait until done to return result (do not use invokeLater)
        EventQueue.invokeAndWait(new Runnable()
        {
          public void run()
          {
            loc.setLocation(myFrame.getLocation());
          }
        });
      }
      catch (Exception ex)
      {
      }
    }
    return loc;
  }

  /**
   * Returns true, if the window is undecorated (no title bar and no borders).
   * @return true, if undecorated; otherwise false.
   */
  public boolean isUndecorated()
  {
    return myFrame.isUndecorated();
  }

  /**
   * Register a GGWindowStateListener that will report change of window
   * location and iconification. Useful to snap another game grid window
   * (button bar, etc). If there is no frame, nothing happens.
   * @param listener the GGFrameStateListener to register
   */
  public void addWindowStateListener(GGWindowStateListener listener)
  {
    if (myFrame == null)
      return;
    windowStateListener = listener;
  }

  /**
   * Brings the game grid window to the front and request the focus. If there
   * is no frame, nothing happens.
   */
  public void activate()
  {
    if (myFrame == null)
      return;
    myFrame.toFront();
    myFrame.requestFocus();
  }

  /**
   * Adds a status window attached at the bottom of the game grid window
   * (below the navigation bar, if available).
   * The window has no title bar, but a one pixel black border and the same
   * width as the game grid. If there is no frame window, nothing happens.
   * The status window becomes visible, even if the game grid window is not shown.
   * If a status window was added before, it is replaced by the new one.
   * @param height the height in pixels of the status window
   */
  public void addStatusBar(int height)
  {
    if (myFrame != null)
    {
      if (!myFrame.isVisible())
      // if the JFrame is not visible, getLocation() returns wrong values under
      // Linux (ubuntu). We defer adding the status bar until the JFrame is
      // shown
      {
        statusBarHeight = height;
        deferStatusBar = true;
      }
      else
        addStatusBarInternal(height);
    }
  }

  private void addStatusBarInternal(final int height)
  {
    if (SwingUtilities.isEventDispatchThread())
      addBar(height + 1);
    else
    {
      try
      {
        EventQueue.invokeAndWait(new Runnable()
        {
          public void run()
          {
            addBar(height + 1);
          }
        });
      }
      catch (Exception ex)
      {
      }
    }
  }

  private void addBar(int height)
  {
    if (statusDialog != null)
      statusDialog.dispose();
    isStatusBarVisible = true;
    statusHeight = height;
    myFrame.setLocation(myFrame.getLocation().x,
      myFrame.getLocation().y - height / 2);
    statusDialog = new ModelessOptionPane(myFrame, getPosition().x,
      getPosition().y + myFrame.getHeight(),
      new Dimension(myFrame.getWidth(), height), false);
  }

  /**
   * Replaces the text in the status bar by the given text using the default
   * font and text color of JOptionPane. The text is
   * left-justified, vertically-centered and may be multi-line (lines
   * separated by newline character).
   * If there is no status bar or no frame window, nothing happens.
   * @param text the new text to show in the status bar
   */
  public void setStatusText(String text)
  {
    setStatusText(text, null, null);
  }

  /**
   * Replaces the text in the status bar by the given text using the given
   * font and text color. The text is left-justified,
   * vertically-centered and may be multi-line (lines
   * separated by newline character).
   * If there is no status bar or no frame window, nothing happens.
   * @param text the new text to show in the status bar
   * @param font the text font
   * @param color the text color
   */
  public void setStatusText(final String text, final Font font,
    final Color color)
  {
    statusText = text;
    statusFont = font;
    statusColor = color;
    if (myFrame == null)
      return;

    if (deferStatusBar)  // Status bar deferred->wait until visible
    {
      if (statusThread == null)  // a second call will only modify statusText
      {
        statusThread = new Thread()
        {
          public void run()
          {
            while (statusDialog == null)  // Wait until visible
              delay(1);
            EventQueue.invokeLater(new Runnable()
            {
              public void run()
              {
                if (statusFont != null)
                {
                  Font defaultFont = (Font)UIManager.get("messageFont");
                  Color defaultColor = (Color)UIManager.get("messageForground");
                  UIManager.put("OptionPane.messageFont", statusFont);
                  UIManager.put("OptionPane.messageForeground", statusColor);
                  statusDialog.setText(statusText);
                  UIManager.put("OptionPane.messageFont", defaultFont);
                  UIManager.put("OptionPane.messageForeground", defaultColor);
                }
                else
                  statusDialog.setText(statusText);
              }
            });
            statusThread = null;
          }
        };
        statusThread.start();
      }
    }
    else
    {
      if (statusDialog != null)  // We have a status bar
      {
        EventQueue.invokeLater(new Runnable()
        {
          public void run()
          {
            if (statusFont != null)
            {
              Font defaultFont = (Font)UIManager.get("messageFont");
              Color defaultColor = (Color)UIManager.get("messageForground");
              UIManager.put("OptionPane.messageFont", statusFont);
              UIManager.put("OptionPane.messageForeground", statusColor);
              statusDialog.setText(text);
              UIManager.put("OptionPane.messageFont", defaultFont);
              UIManager.put("OptionPane.messageForeground", defaultColor);
            }
            else
              statusDialog.setText(text);
          }
        });
      }
    }
  }

  /**
   * Shows or hides the status bar. If there is no status bar or no frame window,
   * nothing happens.
   * @param show if true, a hidden status bar is shown even if the game grid
   * window is hidden; if false, the status bar is hidden and not shown until
   * showStatusBar(true) is called.
   */
  public void showStatusBar(boolean show)
  {
    if (myFrame == null || statusDialog == null)
      return;
    isStatusBarVisible = show;
    statusDialog.setVisible(show);
  }

  /**
   * Returns the size of the occupied area (the frame window
   * including navigation and status bar, if available).
   * @return the size of the occupied area in pixels; width = 0 and
   * height = 0, if there is no frame window
   */
  public Dimension getAreaSize()
  {
    if (myFrame == null)
      return new Dimension(0, 0);
    if (statusDialog == null)
      return requestSize();
    Dimension dim = requestSize();
    return new Dimension(dim.width, dim.height + statusHeight);
  }

  private Dimension requestSize()
  {
    if (myFrame == null)
      return null;
    final Dimension dim = new Dimension();
    if (EventQueue.isDispatchThread())
    {
      dim.setSize(myFrame.getSize());
    }
    else
    {
      try
      {
        // Must wait until done to return result (do not use invokeLater)
        EventQueue.invokeAndWait(new Runnable()
        {
          public void run()
          {
            dim.setSize(myFrame.getSize());
          }
        });
      }
      catch (Exception ex)
      {
      }
    }
    return dim;
  }

  /**
   * Returns the cell location of the cell under the mouse pointer or null, if
   * the game grid is not visible or obscured by another window. To return the pixel
   * coordinates of the mouse pointer use GameGrid.getMousePosition().
   * @return the cell location currently under the mouse pointer; null if no cell is
   * under the pointer
   */
  public Location getMouseLocation()
  {
    Point mousePosition = getMousePosition();
    if (mousePosition == null)
      return null;
    return new Location(toLocationInGrid(mousePosition));
  }

  /**
   * Returns a list of all grid locations that are on the diagonal line
   * through the given location.
   * @param location the location where the diagonal line is fixed
   * @param up if true, the increasing diagonal line (y = x + b) is used; otherwise
   * the decreasing diagonal line (y = -x + b) is used
   * @return an array list containing the diagonal locations within the game grid (sorted by
   * increasing x-coordinates)
   */
  public ArrayList<Location> getDiagonalLocations(Location location, boolean up)
  {
    int sign = up ? 1 : -1;
    ArrayList<Location> diagLocs = new ArrayList<Location>();
    for (int i = 0; i < nbHorzCells + 1; i++)
    {
      Location loc = new Location(i, sign * i + (location.y - sign * location.x));
      if (loc.x >= 0 && loc.x < nbHorzCells && loc.y >= 0 && loc.y < nbVertCells)
        diagLocs.add(loc);
    }
    return diagLocs;
  }

  /** 
   * Returns a list of cell grid locations whose cell centers are exactly on the
   * the line through the centers of loc1 and loc1. If interjacent is true, only
   * locations between loc1 and loc2 are included. If the locations are the same
   * and interjacent is false, the only location is included.
   * @param loc1 the location of the first cell
   * @param loc2 the location of the second cell
   * @param interjacent if true, only location between loc1 and loc2 are 
   * included
   * @return a list of cell whose centers are exactly (close within 10E-6) 
   * on the line through the two cell centers 
   */
  public ArrayList<Location> getLineLocations(Location loc1, Location loc2,
    boolean interjacent)
  {
    ArrayList<Location> list = new ArrayList<Location>();

    if (loc1.x == loc2.x) // Special case: vertical
    {
      if (interjacent)
      {
        if (loc1.equals(loc2))
          return list; // Empty
        for (int y = Math.min(loc1.y, loc2.y) + 1; y < Math.max(loc1.y, loc2.y); y++)
        {
          Location loc = new Location(loc1.x, y);
          if (isInGrid(loc))
            list.add(new Location(loc1.x, y));
        }
      }
      else
      {
        if (loc1.equals(loc2))
        {
          list.add(loc1);
          return list; // 1 location
        }
        for (int y = 0; y < nbVertCells; y++)
          list.add(new Location(loc1.x, y));
      }
      return list;
    }
    if (loc1.x > loc2.x)   // Exchange
    {
      Location tmp = loc1.clone();
      loc1 = loc2;
      loc2 = tmp;
    }

    if (interjacent)
    {
      for (int x = loc1.x + 1; x < loc2.x; x++)
      {
        double inc = (double)(loc2.y - loc1.y) / (loc2.x - loc1.x);
        double y = loc1.y + (x - loc1.x) * inc;
        final double epsilon = 10E-6;
        if ((y - (int)y) < epsilon)
        {
          Location loc = new Location((int)x, (int)y);
          if (isInGrid(loc))
            list.add(new Location((int)x, (int)y));
        }
      }
    }
    else
    {
      for (int x = 0; x < nbHorzCells; x++)
      {
        double inc = (double)(loc2.y - loc1.y) / (loc2.x - loc1.x);
        double y = loc1.y + (x - loc1.x) * inc;
        final double epsilon = 10E-6;
        if ((y - (int)y) < epsilon)
        {
          Location loc = new Location((int)x, (int)y);
          if (isInGrid(loc))
            list.add(new Location((int)x, (int)y));
        }
      }
    }
    return list;
  }

  /**
   * Enables/disables the simulation cycle momentarily. The navigation buttons
   * are not modified.
   * @param enable if true, a running simulation cycle is interrupted; otherwise
   * a running simulation cycles is reenabled
   */
  public void setActEnabled(boolean enable)
  {
    isActEnabled = enable;
  }

  /**
   * Sets the time delay the system uses to distinct click and double-click
   * mouse events. If set to 0, this delay is ignored and both events are
   * generated. If delay is less than zero, the value from the desktop property
   * "awt.multiClickInterval" is used (normally 500 ms). Most systems ignores
   * delays less than 200 ms (treated as 0). Keep in mind that when you
   * perform a single click, the click event is only reported after the
   * double-click time. If the double-click is not registered by the mouse
   * event mask, the delay is ignored.
   *
   * @param delay the double click delay time in milliseconds used when a
   * double-click event is registered
   */
  public void setDoubleClickDelay(int delay)
  {
    if (delay == 0)
      doubleClickTime = 0;
    else
    {
      if (delay < 0)
        doubleClickTime = (Integer)Toolkit.getDefaultToolkit().
          getDesktopProperty("awt.multiClickInterval");
      else
        doubleClickTime = delay;

      doubleClickTimer = new javax.swing.Timer(doubleClickTime,
        new MyTimerActionListener());
    }
  }

  /**
   * Returns the current double-click delay.
   * @return the double-click delay (in milliseconds). 0 if double-click delay
   * is disabled
   */
  public int getDoubleClickDelay()
  {
    return doubleClickTime;
  }

  /**
   * Returns the state if visibilty of the game grid window.
   * Because show()/hide() delegates turning on/off the visibility to the
   * Event Dispatch Thread (EDT) and return immediatetly, this method
   * can be used the check if the action is done.
   * @return true, if the window is visible; otherwise false
   */
  public boolean isShown()
  {
    return myFrame.isVisible();
  }

  /**
   * Adds a GGKeyRepeatListener to get repeating events with default period (20 ms)
   * when a key is pressed until the key is released. More than one 
   * GGKeyRepeatListener may be registered. They are called in the order they
   * are added. 
   * @param listener the GGKeyRepeatListener to register
   */
  public void addKeyRepeatListener(GGKeyRepeatListener listener)
  {
    keyRepeatListeners.add(listener);
    keyRepeatHandler.startThread();
  }

  /**
   * Removes a previously registered GGKeyRepeatListener.
   * @param listener the GGKeyRepeatListener to remove
   */
  public void removeKeyRepeatListener(GGKeyRepeatListener listener)
  {
    keyRepeatListeners.remove(listener);
    if (keyRepeatListeners.isEmpty())
      keyRepeatHandler.stopThread();
  }

  /**
   * Sets the time between two successive key repeat events.
   * @param keyRepeatPeriod the new key repeat period (in ms)
   * @return the current key repeat period
   */
  public int setKeyRepeatPeriod(int keyRepeatPeriod)
  {
    int period = this.keyRepeatPeriod;
    this.keyRepeatPeriod = keyRepeatPeriod;
    return period;
  }

  /**
   * Returns a list of the key codes of all keys currently pressed
   * @return a list of key codes of keys currently pressed 
   */
  public ArrayList<Integer> getPressedKeyCodes()
  {
    return keyRepeatHandler.getPressedKeyCodes();
  }

  /**
   * Sets the closing mode that determines what happens when
   * the title bar close button is hit. If a GGExitListener is
   * registered, the closing behavior is determined by 
   * the notifyExit() callback. The given value overwrites the 
   * value read from the gamegrid.properties file.
   * @param closingMode one of the items in enum GameGrid.ClosingMode
   */
  public static void setClosingMode(ClosingMode closingMode)
  {
    frameClosingMode = closingMode;
  }

  /** 
   * Returns the closing mode read from the gamegrid.properties file.
   * @return one of the items in enum ClosingMode
   */
  public static ClosingMode getClosingMode()
  {
    return frameClosingMode;
  }

  /** 
   * Returns true if closing mode DisposeOnClose is selected 
   * and the close button was hit.
   * @return true, if the GameGrid window was disposed or released; otherwise false
   */
  public static boolean isDisposed()
  {
    return isDisposed;
  }

  private void createMessageDialog(final Component parent, final String title,
    final String message)
  {

    if (EventQueue.isDispatchThread())
      createMessageDialogInternal(parent, title, message);
    else
    {
      try
      {
        EventQueue.invokeAndWait(new Runnable()
        {
          public void run()
          {
            createMessageDialogInternal(parent, title, message);
          }
        });
      }
      catch (Exception ex)
      {
      }
    }
  }

  private void createMessageDialogInternal(Component parent, String title, String message)
  {
    ImageIcon icon = null;
    Object[] options =
    {
    };
    JOptionPane pane = new JOptionPane(message,
      JOptionPane.DEFAULT_OPTION,
      JOptionPane.INFORMATION_MESSAGE,
      icon,
      options,
      null);
    JDialog msgDialog = pane.createDialog(parent, title);
    msgDialog.setModal(false);
    msgDialog.setVisible(true);
  }

  private static ClosingMode readClosingMode(MyProperties props)
  {
    String value = props.getStringValue("FrameClosingMode");
    if (value != null)  // Entry found
    {
      value = value.trim();
      if (value.equals("TerminateOnClose"))
        return ClosingMode.TerminateOnClose;
      if (value.equals("AskOnClose"))
        return ClosingMode.AskOnClose;
      if (value.equals("DisposeOnClose"))
        return ClosingMode.DisposeOnClose;
      if (value.equals("NothingOnClose"))
        return ClosingMode.NothingOnClose;
      return ClosingMode.TerminateOnClose;  // Entry not valid
    }
    return ClosingMode.TerminateOnClose;  // Entry not valid
  }

  private static boolean getSystemLookAndFeel(MyProperties props)
  {
    String value = props.getStringValue("UseSystemLookAndFeel");
    if (value != null)
    {
      value = value.trim().toLowerCase();
      if (value.equals("yes"))
        return true;
      else
        return false;
    }
    return true;  // true, if  not found
  }

  private Color getPropColor(MyProperties props, String entry)
  // null if not valid
  {
    int[] values = props.getIntArray(entry, 3);
    if (values == null)
      return null;
    for (int i = 0; i < 3; i++)
    {
      if (values[i] < 0 || values[i] > 255)
        return null;
    }
    return new Color(values[0], values[1], values[2]);
  }

  /** 
   * Stops the game thread, hides the game window, disposes the frame
   * and calls Monitor.wakeUp().
   */
  public void dispose()
  {
    isActEnabled = false;
    doPause();
    stopGameThread();
    hide();
    isDisposed = true;
    myFrame.dispose();
    ToolBarStack.initInstances();  // Needed because class is not reloaded on next run (Jython)
    nbRotSprites = 60;  // ditto
    Monitor.wakeUp();
  }
}
