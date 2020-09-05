// Turtle.java

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

/*
 * The Turtle library was originally developed by Regula Hoefer-Isenegger.
 * Important modifications and bug fixes by Aegidius Pluess (www.aplu.ch).
 * Improved leftCircle()/rightCircle() algorithm by Beat Trachsler.
 */
package ch.aplu.turtle;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.text.DecimalFormat;
import java.util.ArrayList;
import javax.imageio.ImageIO;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;

/**
 * The core class for turtles.
 * Default turtle coordinate system: doubles -200.0..+200.0 in both directions 
 * (zero at center; x to left, y upward). Turtle coordinates are rounded to
 * to 400 x 400 pixel coordinates.<br>
 * Unless special constructors are used, new turtles are shown in a new window.
 * <br>
 * Defaults when a turtle is created:<br>
 * Coordinates: (0, 0) (center of window)<br>
 * Heading: north<br>
 * Speed: 200 (coordinates per seconds)<br><br>
 * 
 * Default properties of the turtle and it's playground can
 * be changed from the library defaults by using a 
 * Java properties file turtlegraphics.properties. For more details
 * consult turtlegraphics.properties file found in the distribution.<br><br>
 * 
 * When the close button of the console window title bar is hit, System.exit(0)
 * is executed that terminates the JVM, but you can modifiy this behavior by using
 * the key ConsoleClosingMode in turtlegraphics.properties.
 * If the value RELEASE_ON_CLOSE is selected, every public Turtle drawing method checks
 * if the frame is closed and  throws a RuntimeException if this is the case.<br><br>
 * 
 *  
 * <code><pre>
 ================ UML diagram ===========================================
 -----------              --------------------------       ------------
 |JPanel     |            |interface                 |     |JFrame      |
 |-----------|            |TurtleContainer           |     |----------- |
 |           |            |--------------------------|     |            |
 |           |            |Playground getPlayground()|     |            |
 -----------              --------------------------       ------------
 ^                               ^                           ^
 | is-a                          |                           |
 |                               |                           |
 -----------                     |                           |
 |class      |                   |                           | is-a
 |Playground |                   |implements                 |
 |-----------|                   |                           |
 |           |                   |                           |
 |           |                   |              -------------
 -----------                     |             |
 ^ o                   --------------------------------
 | |                  |class                           |
 | |     has-a        |TurtleFrame                     |
 | |    (creates)     |------------------------------- |
 |  ----------------- |TurtleFrame(...new Playground())|
 | is-a               |Playground playground           |
 |                     --------------------------------
 ----------                            o
 |class     |                          | has-a (creates)
 |TurtlePane|                          |
 |----------|                          |
 |          |             --------------------------------
 |          |            |class                           |
 ----------             |Turtle                          |
 |------------------------------- |
 |TurtleFrame turtleFrame         |
 |                                |
 --------------------------------

 ========================================================================
 </pre></code>
 */
//
//
public class Turtle implements Cloneable
{
  // ------------- Inner class TurtleState
  private class TurtleState
  {
    private double myX = 0;
    private double myY = 0;
    private Color myColor = defaultTurtleColor;
    private double myHeading = 0;

    public TurtleState(double x, double y, Color color, double heading)
    {
      myX = x;
      myY = y;
      myColor = color;
      myHeading = heading;
    }

    public double getX()
    {
      return myX;
    }

    public double getY()
    {
      return myY;
    }

    public Color getColor()
    {
      return myColor;
    }

    public double getHeading()
    {
      return myHeading;
    }
  }

  // ------------- Inner class MyMouseHitListener
  private class MyMouseHitListener implements MouseHitListener
  {
    private Turtle turtle;
    private TurtleHitListener turtleHitListener = null;

    MyMouseHitListener(Turtle t, TurtleHitListener listener)
    {
      this.turtle = t;
      this.turtleHitListener = listener;
    }

    private boolean isInside(double x, double y, double r)
    {
      double scale = 0.3;
      double h = scale * getTurtleImage().getHeight();
      double w = scale * getTurtleImage().getWidth();
      double d = h * h + w * w;
      double x0 = getX();
      double y0 = getY();
      return (x - x0) * (x - x0) + (y - y0) * (y - y0) < d;
    }

    public void mouseHit(double x, double y)
    {
      if (turtleHitListener == null)
        return;
      if (isInside(x, y, 10))
        turtleHitListener.turtleHit(turtle, x, y);
    }
  }

// ---------------------------------------------
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
   * Short for Color.GRAY.
   */
  public static final Color DKGRAY = Color.GRAY;
  /**
   * Short for Color.GRAY.
   */
  public static final Color GRAY = Color.GRAY;
  /**
   * Short for Color.GREEN.
   */
  public static final Color GREEN = Color.GREEN;
  /**
   * Short for Color.LIGHT_GRAY.
   */
  public static final Color LTGRAY = Color.LIGHT_GRAY;
  /**
   * Short for Color.MAGENTA.
   */
  public static final Color MAGENTA = Color.MAGENTA;
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

  protected enum FillMode
  {
    FILL_OFF,
    FILL_HORZ,
    FILL_VERT,
    FILL_POINT
  }
  protected static final boolean propertyVerbose = false;
  protected FillMode fillMode = FillMode.FILL_OFF;
  protected double xFillAnchor, yFillAnchor;
  protected double xFillLine;
  protected double yFillLine;
  private double angle;
  private Point2D.Double position;
  private Playground playground;
  private JMenuBar menuBar;
  private int framesPerSecond;
  private double speed;           // Pixel/sec
  private double angleSpeed;      // Radian/sec
  private TurtleRenderer turtleRenderer;
  private int angleResolution;
  private LineRenderer lineRenderer;
  private TurtleFactory turtleFactory;
  private boolean isPenUp;
  private boolean isTurtleShown;
  private boolean initialVisibility = true;
  private Pen pen;
  private Color color;
  private int edgeBehaviour;
  private TurtleFrame turtleFrame;
  private static JMenuBar nullMenuBar = null;
  private boolean isTurtleArea = false;
  protected final static int CLIP = 0;
  protected final static int WRAP = 1;
  protected static int defaultEdgeBehavior = CLIP;
  protected static double defaultSpeed = 200;
  protected static int defaultAngleResolution = 72;
  protected static int defaultFramesPerSecond = 10;
  protected static Color defaultTurtleColor = Color.cyan;
  protected static Color defaultPenColor = Color.blue;
  private static boolean isExceptionThrown;
  protected BufferedImage turtleImg = null;
  private ArrayList<TurtleState> stateStack = new ArrayList<TurtleState>();
  protected GeneralPath gp = null;
  private Image turtleImage = null;  // Image with no rotation

  //
  /** 
   * Mode attribute for normal turtle applications (default, value: 0).
   * When the title bar close button is hit, System.exit() is called
   * to terminate the application.
   */
  public static int STANDARDFRAME = 0;
  /** 
   * Mode attribute for turtle applets in a standalone window
   * (value: 1).
   */
  public static int APPLETFRAME = 1;
  /** 
   * Mode attribute for a turtle frame that calls Turtle.clear() when
   * the close button is hit (value: 2).
   */
  public static int CLEAR_ON_CLOSE = 2;
  /** 
   * Mode attribute for a turtle frame that opens a confirm dialog when
   * the close button is hit (value: 3). If accepted, System.exit() is called
   * to terminate the application.
   */
  public static int ASK_ON_CLOSE = 3;
  /** 
   * Mode attribute for a turtle frame that closes the turtle frame 
   * and releases grapics resources (value: 4), but does not call
   * System.exit().
   */
  public static int DISPOSE_ON_CLOSE = 4;
  /** 
   * Mode attribute for a turtle frame that closes the turtle frame 
   * by disposing all resources (value: 5), but does not call 
   * System.exit(). Every public turtle method
   * throws a runtime exception if called after the frame is closed.
   */
  public static int RELEASE_ON_CLOSE = 5;
  /** 
   * Mode attribute for a turtle frame that does nothing when
   * the close button is hit (value: -1).
   */
  public static int NOTHING_ON_CLOSE = -1;

  // ============ Start of ctors ===========================
  /** 
   * Creates a new turtle in its own new window.
   * If turtlegraphics.properties is found, defaults are specified there.
   */
  public Turtle()
  {
    this(nullMenuBar);
  }

  /** 
   * Creates a new turtle in its own new window
   * with the given menu.
   * If turtlegraphics.properties is found, defaults are specified there.
   * @param menuBar a reference to the JMenuBar instance
   */
  Turtle(JMenuBar menuBar)
  {
    createTurtle(menuBar);
  }

  private void createTurtle(JMenuBar menuBar)
  {
    MyProperties props = new MyProperties(propertyVerbose);
    turtleFrame = new TurtleFrame(menuBar, props);
    setDefaults(props);
    init(turtleFrame, defaultTurtleColor);
    getPlayground().paintTurtles();
  }

  /** 
   * Creates a new turtle with specified visibility
   * in its own new window.
   * @param  show if true, the turtle is visible when created; otherwise it remains
   * hidden until showTurtle() or st() is called
   */
  public Turtle(boolean show)
  {
    this(nullMenuBar, show);
  }

  /** 
   * Creates a new turtle with specified visibility
   * in its own new window with the given menu.
   * If turtlegraphics.properties is found, defaults are specified there.
   * @param menuBar a reference to the JMenuBar instance
   * @param  show if true, the turtle is visible when created; otherwise it remains
   * hidden until showTurtle() or st() is called
   */
  Turtle(JMenuBar menuBar, boolean show)
  {
    createTurtle(menuBar, show);
  }

  private void createTurtle(JMenuBar menuBar, boolean show)
  {
    initialVisibility = show;
    MyProperties props = new MyProperties(propertyVerbose);
    turtleFrame = new TurtleFrame(menuBar, props);
    setDefaults(props);
    init(turtleFrame, defaultTurtleColor);
    getPlayground().paintTurtles();
  }

  /** 
   * Creates a new turtle with specified color in its own new window.
   * If turtlegraphics.properties is found, defaults are specified there,
   * but the turtle color is specified here.
   * @param color the turtle color (default: cyan)
   */
  public Turtle(Color color)
  {
    this(nullMenuBar, color);
  }

  /** 
   * Creates a new turtle with specified color as string in its own new window.
   * The X11 color names are supported.
   * If turtlegraphics.properties is found, defaults are specified there,
   * but the turtle color is specified here.
   * @param colorStr the turtle color (default: cyan); if the
   * string is not one of the predefined values, the given string is 
   * interpreted as filename for a custom turtle shape. If the image file 
   * can't be loaded the default turtle color is used
   */
  public Turtle(String colorStr)
  {
    if (toColor(colorStr) != null)
      createTurtle(nullMenuBar, toColor(colorStr));
    else
    {
      turtleImg = getImage(colorStr);
      createTurtle(nullMenuBar, defaultTurtleColor);
    }
  }

  /** Creates a new turtle with specified color in its own new window
   * with the given menu.
   * If turtlegraphics.properties is found, defaults are specified there,
   * but the turtle color is specified here.
   * @param menuBar a reference to the JMenuBar instance
   * @param color the turtle color (default: cyan)
   */
  public Turtle(JMenuBar menuBar, Color color)
  {
    createTurtle(menuBar, color);
  }

  private void createTurtle(JMenuBar menuBar, Color color)
  {
    MyProperties props = new MyProperties(propertyVerbose);
    turtleFrame = new TurtleFrame(menuBar, props);
    setDefaults(props);
    init(turtleFrame, color);
    getPlayground().paintTurtles();
  }

  /** 
   * Creates a new turtle in the given TurtleContainer.
   * If turtlegraphics.properties is found, defaults are specified there.
   * @param turtleContainer a reference to the TurtleContainer where the
   * turtle lives
   */
  public Turtle(TurtleContainer turtleContainer)
  {
    createTurtle(turtleContainer);
  }

  private void createTurtle(TurtleContainer turtleContainer)
  {
    if (turtleContainer instanceof TurtleArea)
      isTurtleArea = true;
    else
      turtleFrame = (TurtleFrame)turtleContainer;  // turtleFrame not initialized for embedded applets
    MyProperties props = new MyProperties(propertyVerbose);
    setDefaults(props);
    init(turtleContainer, defaultTurtleColor);
    getPlayground().paintTurtles();
  }

  /** 
   * Creates a new turtle in the given TurtlePane.
   * If turtlegraphics.properties is found, defaults are specified there.
   * @param turtlePane a reference to the TurtlePane where the turtle lives.
   */
  public Turtle(TurtlePane turtlePane)
  {
    createTurtle(turtlePane);
  }

  private void createTurtle(TurtlePane turtlePane)
  {
    playground = turtlePane;

    if (playground.turtleG2D != null)  // TurtlePane already initialized, put
    // new turtle in same playground
    {
      MyProperties props = new MyProperties(propertyVerbose);
      setDefaults(props);
      init(getPlayground(), defaultTurtleColor);
      getPlayground().paintTurtles();
    }
    else
    {
      playground.isBean = false; // inhibit special section in Playground.paintComponent()

      // deferred init:
      if (playground.traceG2D != null)
        playground.traceG2D.dispose();
      Dimension dim = turtlePane.getSize();
      Color bkColor = turtlePane.getBackgroundColor();
      getPlayground().init(dim, bkColor);

      MyProperties props = new MyProperties(propertyVerbose);
      setDefaults(props);
      init(turtlePane.getPlayground(), defaultTurtleColor);
      getPlayground().paintTurtles();
    }
  }

  /** 
   * Creates a new turtle with specified visibility
   * in the given TurtleContainer.
   * If turtlegraphics.properties is found, defaults are specified there.
   * @param turtleContainer a reference to the TurtleContainer where the
   * turtle lives
   * @param  show if true, the turtle is visible when created; otherwise it remains
   * hidden until showTurtle() or st() is called
   */
  public Turtle(TurtleContainer turtleContainer, boolean show)
  {
    createTurtle(turtleContainer, show);
  }

  private void createTurtle(TurtleContainer turtleContainer, boolean show)
  {
    if (turtleContainer instanceof TurtleArea)
      isTurtleArea = true;
    else
      turtleFrame = (TurtleFrame)turtleContainer;  // ditto
    initialVisibility = show;
    MyProperties props = new MyProperties(propertyVerbose);
    setDefaults(props);
    init(turtleContainer, defaultTurtleColor);
    getPlayground().paintTurtles();
  }

  /** 
   * Creates a new turtle with specified color
   * in the given TurtleContainer.
   * If turtlegraphics.properties is found, defaults are specified there,
   * but the turtle color is specified here.
   * @param turtleContainer a reference to the TurtleContainer where the
   * turtle lives
   * @param color the turtle color (default: cyan)
   */
  public Turtle(TurtleContainer turtleContainer, Color color)
  {
    createTurtle(turtleContainer, color);
  }

  private void createTurtle(TurtleContainer turtleContainer, Color color)
  {
    if (turtleContainer instanceof TurtleArea)
      isTurtleArea = true;
    else
      turtleFrame = (TurtleFrame)turtleContainer;  // ditto
    MyProperties props = new MyProperties(propertyVerbose);
    setDefaults(props);
    init(turtleContainer, color);
    getPlayground().paintTurtles();
  }

  /** 
   * Creates a new turtle with specified color as a string
   * in the given TurtleContainer.
   * The X11 color names are supported.
   * If turtlegraphics.properties is found, defaults are specified there,
   * but the turtle color is specified here.
   * @param turtleContainer a reference to the TurtleContainer where the
   * turtle lives
   * @param colorStr the turtle color (default: cyan); if the
   * string is not one of the predefined values, the given string is 
   * interpreted as filename for a custom turtle shape. If the image file 
   * can't be loaded the default turtle color is used
   */
  public Turtle(TurtleContainer turtleContainer, String colorStr)
  {
    if (toColor(colorStr) != null)
      createTurtle(turtleContainer, toColor(colorStr));
    else
    {
      turtleImg = getImage(colorStr);
      createTurtle(turtleContainer, defaultTurtleColor);
    }
  }

  /** 
   * Creates a new turtle in the same TurtleContainer (window) as 
   * the given turtle.
   * If turtlegraphics.properties is found, defaults for the
   * turtle are specified there.
   * @param otherTurtle the turtle from where to take the window
   */
  public Turtle(Turtle otherTurtle)
  {
    createTurtle(otherTurtle);
  }

  private void createTurtle(Turtle otherTurtle)
  {
    MyProperties props = new MyProperties(propertyVerbose);
    turtleFrame = otherTurtle.getFrame();
    setDefaults(props);
    init(otherTurtle.getPlayground(), defaultTurtleColor);
    getPlayground().paintTurtles();
  }

  /** 
   * Creates a new turtle in the same
   * TurtleContainer (window) as
   * the given turtle and specifies its visibility.
   * If turtlegraphics.properties is found, defaults for the
   * turtle are specified there.
   * @param otherTurtle the turtle from where to take the window
   * @param  show if true, the turtle is visible when created; otherwise it remains
   * hidden until showTurtle() or st() is called
   */
  public Turtle(Turtle otherTurtle, boolean show)
  {
    createTurtle(otherTurtle, show);
  }

  private void createTurtle(Turtle otherTurtle, boolean show)
  {
    MyProperties props = new MyProperties(propertyVerbose);
    initialVisibility = show;
    turtleFrame = otherTurtle.getFrame();
    setDefaults(props);
    init(otherTurtle.getPlayground(), defaultTurtleColor);
    getPlayground().paintTurtles();
  }

  /** 
   * Creates a new turtle with the specified color in the same 
   * TurtleContainer (window) as the given turtle.
   * If turtlegraphics.properties is found, defaults for the turtle 
   * are specified there, but TurtleColor key is ignored.
   * @param otherTurtle the turtle from where to take the window
   * @param color the turtle color (default: cyan)
   */
  public Turtle(Turtle otherTurtle, Color color)
  {
    createTurtle(otherTurtle, color);
  }

  private void createTurtle(Turtle otherTurtle, Color color)
  {
    MyProperties props = new MyProperties(propertyVerbose);
    turtleFrame = otherTurtle.getFrame();
    setDefaults(props);
    init(otherTurtle.getPlayground(), color);
    getPlayground().paintTurtles();
  }

  // Used for clone with turtle image 
  private Turtle(Turtle otherTurtle, BufferedImage turtleImg, boolean show)
  {
    this.turtleImg = turtleImg;
    createTurtle(otherTurtle, show);
  }

  /** 
   * Creates a new turtle with specified color as string 
   * in the same TurtleContainer (window) as the given turtle.
   * The X11 color names are supported.
   * If turtlegraphics.properties is found, defaults are specified there,
   * but TurtleColor key is ignored. 
   * @param otherTurtle the turtle from where to take the window
   * @param colorStr the turtle color (default: cyan); if the
   * string is not one of the predefined values, the given string is 
   * interpreted as filename for a custom turtle shape. If the image file 
   * can't be loaded the default turtle color is used
   */
  public Turtle(Turtle otherTurtle, String colorStr)
  {
    if (toColor(colorStr) != null)
      createTurtle(otherTurtle, toColor(colorStr));
    else
    {
      turtleImg = getImage(colorStr);
      createTurtle(otherTurtle, defaultTurtleColor);
    }
  }

  private void init(TurtleContainer turtleContainer, Color color)
  {
    init(turtleContainer.getPlayground(), color);
  }

  /** 
   * Creates a new turtle with a special mode.
   * Use Turtle.APPLETFRAME for a standalone applet window.
   * Instance count for multiple windows is disabled.
   * If more features are needed, use TurtleFrame(int mode, ...).
   * If turtlegraphics.properties is found, defaults for the turtle 
   * are specified there, but ClosingMode key is ignored.
   * @param the mode flag, one of the constants defined in class Turtle
   */
  Turtle(int mode)
  {
    createTurtle(mode);
  }

  private void createTurtle(int mode)
  {
    MyProperties props = new MyProperties(propertyVerbose);
    turtleFrame = new TurtleFrame(mode, props);
    setDefaults(props);
    init(turtleFrame, defaultTurtleColor);
    getPlayground().paintTurtles();
  }
  // ============ End of ctors =============================

  /** 
   * Converts from screen coordinates to turtle coordinates.
   * @param p the point in pixel coordinates (default 0..400)
   * @return the turtle coordinates
   */
  public Point2D.Double toTurtlePos(Point p)
  {
    return playground.toTurtleCoords((double)p.x, (double)p.y);
  }

  /** 
   * Converts from screen coordinates to turtle coordinates.
   * @param x the x-coordinate in pixel (default 0..400)
   * @param y the y-coordinate in pixel (default 0..400)
   * @return the turtle coordinates
   */
  public Point2D.Double toTurtlePos(int x, int y)
  {
    return playground.toTurtleCoords((double)x, (double)y);
  }

  /** 
   * Converts from screen coordinates to turtle coordinates.
   * @param x the x-coordinate in pixels
   * @return the turtle x-coordinate
   */
  public double toTurtleX(int x)
  {
    return (playground.toTurtleCoords((double)x, 0)).x;
  }

  /** 
   * Converts from screen coordinates to turtle coordinates.
   * @param y the y-coordinate in pixels
   * @return the turtle y-coordinate
   */
  public double toTurtleY(int y)
  {
    return (playground.toTurtleCoords(0, (double)y)).y;
  }

  /** 
   * Returns the turtle's TurtleFrame (derivated from JFrame).
   * @return a reference to the TurtleFrame where the turtle lives
   */
  public TurtleFrame getFrame()
  {
    return turtleFrame;
  }

  /** 
   * Adds the specified MouseListener to receive mouse events.
   * @param listener the registered MouseListener
   */
  public void addMouseListener(MouseListener listener)
  {
    playground.addMouseListener(listener);
  }

  /** 
   * Adds the specified MouseMotionListener to receive mouse motion events.
   * @param listener the registered MouseMotionListener
   */
  public void addMouseMotionListener(MouseMotionListener listener)
  {
    playground.addMouseMotionListener(listener);
  }

  /** 
   * Adds the specified MouseDoubleClickListener to receive mouse events.
   * See TurtleFrame.setDoubleClickDelay() to get information about 
   * how notifyClick() and notifyDoubleClick() work together.
   * @param listener the registered MouseDoubleClickListener
   */
  public void addMouseDoubleClickListener(MouseDoubleClickListener listener)
  {
    turtleFrame.addMouseDoubleClickListener(listener);
  }

  /** 
   * Adds the specified mouse-hit listener to receive mouse events.
   * @param listener the registered MouseHitListener
   */
  public void addMouseHitListener(MouseHitListener listener)
  {
    turtleFrame.addMouseHitListener(listener);
  }

  /** 
   * Adds the specified turtle-hit listener to receive mouse events when
   * the mouse cursor is inside the turtle image.
   * @param listener the registered TurtleHitListener
   */
  public void addTurtleHitListener(TurtleHitListener listener)
  {
    addMouseHitListener(new MyMouseHitListener(this, listener));
  }

  /** 
   * Adds the specified MouseHitXListener to receive mouse events.
   * @param listener the registered MouseHitXListener
   */
  public void addMouseHitXListener(MouseHitXListener listener)
  {
    turtleFrame.addMouseHitXListener(listener);
  }

  /** 
   * Adds the specified KeyListener to receive key events.
   * (Not allowed for embedded applets.)
   * @param listener the registered KeyListener
   */
  public void addKeyListener(KeyListener listener)
  {
    if (turtleFrame == null)
      throw new RuntimeException("KeyListener not allowed for embedded applets");
    turtleFrame.addKeyListener(listener);
  }

  /** 
   * Adds the specified WindowListener to receive window events.
   * (Not allowed for embedded applets.)
   * @param listener the registered WindowListener
   */
  public void addWindowListener(WindowListener listener)
  {
    if (turtleFrame == null)
      throw new RuntimeException("WindowListener not allowed for embedded applets");
    turtleFrame.addWindowListener(listener);
  }

  /** 
   * Adds the specified WindowFocusListener to receive window focus events.
   * (Not allowed for embedded applets.)
   * @param listener the registered WindowFocusListener
   */
  public void addWindowFocusListener(WindowFocusListener listener)
  {
    if (turtleFrame == null)
      throw new RuntimeException("WindowFocusListener not allowed for embedded applets");
    turtleFrame.addWindowFocusListener(listener);
  }

  /** 
   * Adds the specified ComponentListener to receive component events.
   * (Not allowed for embedded applets.)
   * @param listener the registered ComponentListener
   */
  public void addComponentListener(ComponentListener listener)
  {
    if (turtleFrame == null)
      throw new RuntimeException("ComponentListener not allowed for embedded applets");
    turtleFrame.addComponentListener(listener);
  }

  /** 
   * Adds the specified FocusListener to receive focus events.
   * (Not allowed for embedded applets.)
   * @param listener the registered FocusListener
   */
  public void addFocusListener(FocusListener listener)
  {
    if (turtleFrame == null)
      throw new RuntimeException("FocusListener not allowed for embedded applets");
    turtleFrame.addFocusListener(listener);
  }

  /**
   * Adds the specifiec ExitListener to get a notification when the close button is clicked.
   * After registering other closing modes are disabled.
   * To terminate the application, System.exit() may be called.
   */
  public void addExitListener(ExitListener listener)
  {
    if (turtleFrame == null)
      throw new RuntimeException("ExitListener not allowed for embedded applets");
    turtleFrame.addExitListener(listener);
  }

  /**
   * Emits a 'beep'. Fails if no standard speaker available. 
   * @return the Turtle reference to allow chaining
   */
  public Turtle beep()
  {
    check();
    Toolkit.getDefaultToolkit().beep();
    return this;
  }

  private void init(Playground playground, Color color)
  {
    isExceptionThrown = false;
    angle = 0;
    position = new Point2D.Double(0, 0);
    setPlayground(playground);
    framesPerSecond = defaultFramesPerSecond;
    if (initialVisibility)
      speed(defaultSpeed);
    else
      speed(-1);
    if (isTurtleArea)
      initialVisibility = false;
    isTurtleShown = initialVisibility;
    setAngleResolution(defaultAngleResolution);
    angleSpeed = getSpeed() * Math.PI * 2 / defaultSpeed;
    pen = new Pen(defaultPenColor);
    if (turtleImg == null)
      turtleFactory = createTurtleFactory();
    else
      turtleFactory
        = createBitmapTurtleFactory(turtleImg);
    internalSetColor(color);
    lineRenderer = createLineRenderer();
    getTurtleRenderer().setAngle(getAngle());
  }

  /** 
   * Sets the angle resolution when animating the turtle rotation.
   * The parameter specifies the number of images for a 360 degrees
   * rotation (default 72, e.g. 5 degrees increment).
   * @param resolution the new angle resolution (default: 72)
   * @return the Turtle reference to allow chaining
   */
  public Turtle setAngleResolution(int resolution)
  {
    check();
    synchronized (playground)
    {
      angleResolution = resolution;
      return this;
    }
  }

  /** 
   * Returns the turtle's TurtleFactory.
   * @return the TurtleFactory reference
   */
  public TurtleFactory getTurtleFactory()
  {
    return turtleFactory;
  }

  protected LineRenderer createLineRenderer()
  {
    return new LineRenderer(this);
  }

  protected TurtleRenderer createTurtleRenderer()
  {
    return new TurtleRenderer(this);
  }

  protected TurtleFactory createTurtleFactory()
  {
    return new TurtleFactory();
  }

  protected TurtleFactory createBitmapTurtleFactory(BufferedImage turtleImg)
  {
    return new BitmapTurtleFactory(turtleImg);
  }

  protected int getAngleResolution()
  {
    return angleResolution;
  }

  protected TurtleRenderer getTurtleRenderer()
  {
    return turtleRenderer;
  }

  private LineRenderer getLineRenderer()
  {
    return lineRenderer;
  }

  /** 
   * Returns the turtle's Playground.
   * @return the Playground reference
   */
  public Playground getPlayground()
  {
    return playground;
  }

  private void setPlayground(Playground playground)
  {
    Playground pg = getPlayground();
    // 	if (playground!=null) {
    // 	    System.out.println("arg: "+playground.hashCode());
    // 	}
    if (pg != null)
    {
      // 	    System.out.println("old: "+pg.hashCode());
      pg.clearTurtle(this);
      pg.remove(this);
      pg.paintTurtles();
    }
    this.playground = playground;
    // 	if (this.playground!=null) {
    // 	    System.out.println("new: "+this.playground.hashCode());
    // 	}
    playground.add(this);
    playground.paintTurtles(this);
  }

  private void setMenuBar(JMenuBar menuBar)
  {
    this.menuBar = menuBar;
  }

  private JMenuBar getMenuBar()
  {
    return menuBar;
  }

  private double getAngleSpeed()
  {
    return angleSpeed;
  }

  private void setAngleSpeed(double newAngleSpeed)
  {
    this.angleSpeed = newAngleSpeed;
  }

  private double getAngle()
  {
    return angle; // in radians
  }

  /** 
   * Returns the current turtle animation speed.
   * @return the turtle speed (turtle coordinates per seconds; -1, for
   * no animation)
   */
  public double getSpeed()
  {
    check();
    return speed;
  }

  /** 
   * Returns the current unbounded turtle's x-coordinate.
   * @return the x-coordinate (not bounded to the playground,
   * even when wrapping is on)
   */
  public double _getX()
  {
    check();
    synchronized (playground)
    {
      return position.getX();
    }
  }

  /** 
   * Returns the current unbounded turtle's y-coordinate.
   * @return the y-coordinate (not bounded to the playground,
   * even when wrapping is on)
   */
  public double _getY()
  {
    check();
    synchronized (playground)
    {
      return position.getY();
    }
  }

  /** 
   * Returns the current playground bounded turtle's x-coordinate.
   * If the turtle is outside the playground and wrapping is on, the coordinate
   * is mapped to the playground.
   * @return the x-coordinate of the visible turtle
   */
  public double getX()
  {
    check();
    synchronized (playground)
    {
      double xPos = _getX();
      if (isWrap())
      {
        int xmax = (playground.getSize().width - 2) / 2;
        if (_getX() > xmax)
          xPos = (_getX() + xmax) % (2 * xmax) - xmax;
        if (_getX() < -xmax)
          xPos = (_getX() - xmax) % (2 * xmax) + xmax;
      }
      return xPos;
    }
  }

  /**
   * Returns the current playground bounded turtle's y-coordinate.
   * If the turtle is outside the playground and wrapping is on, the coordinate
   * is mapped to the playground.
   * @return the y-coordinate of the visible turtle
   */
  public double getY()
  {
    check();
    synchronized (playground)
    {
      double yPos = _getY();
      if (isWrap())
      {
        int ymax = (playground.getSize().height - 2) / 2;
        if (_getY() > ymax)
          yPos = (_getY() + ymax) % (2 * ymax) - ymax;
        if (_getY() < -ymax)
          yPos = (_getY() - ymax) % (2 * ymax) + ymax;
      }
      return yPos;
    }
  }

  /** 
   * Returns the current unbounded turtle position.
   * @return the turtle coordinates (not bounded to the playground, 
   * even when wrapping is on)
   */
  public Point2D.Double _getPos()
  {
    check();
    return position;
  }

  /**
   * Returns the current playground bounded turtle position.
   * If the turtle is outside the playground and wrapping is on, the position
   * is mapped to the playground.
   * @return the turtle coordinates of the visible turtle
   */
  public Point2D.Double getPos()
  {
    check();
    return new Point2D.Double(getX(), getY());
  }

  /**  
   * Puts the turtle to a new random position in a rectangle area with
   * given width and height (center in the middle of the playground)
   * without drawing a trace.
   * @param width the width of the rectangular area
   * @param height the height of the rectangular 
   * @return the Turtle reference to allow chaining
   */
  public Turtle setRandomPos(double width, double height)
  {
    check();
    double x = -width / 2 + width * Math.random();
    double y = -height / 2 + height * Math.random();
    return setPos(x, y);
  }

  /**  
   * Puts the turtle to a new position without drawing a trace.
   * @param x the x-coordinate of the new position
   * @param y the y-coordinate of the new position
   * @return the Turtle reference to allow chaining
   */
  public Turtle setPos(double x, double y)
  {
    check();
    synchronized (playground)
    {
      getPlayground().clearTurtle(this);
      internalSetPos(x, y);
      getPlayground().paintTurtles();
      addPositionToPath();
      return this;
    }
  }

  /**  
   * Puts the turtle to a new position without drawing a trace.
   * @param pt the turtle coordinates of the new position
   * @return the Turtle reference to allow chaining
   */
  public Turtle setPos(Point2D.Double pt)
  {
    return setPos(pt.x, pt.y);
  }

  /** 
   * Puts the Turtle to a new screen position without drawing a trace.
   * @param pt the screen coordinates of the new position
   * (coordinate system with origin at upper left
   * vertex of playground, same coordinate increments as turtle coordinates)
   * @return the Turtle reference to allow chaining
   */
  public Turtle setScreenPos(Point pt)
  {
    check();
    return setPos(toTurtlePos(pt));
  }

  /** 
   * Puts the turtle to a new position with the given 
   * screen x-coordinates without drawing a trace.
   * (Coordinate system with origin at upper left
   * vertex of playground, same coordinate increments as turtle coordinates.)
   * @return the Turtle reference to allow chaining
   */
  public Turtle setScreenX(int x)
  {
    check();
    synchronized (playground)
    {
      return setX(toTurtleX(x));
    }
  }

  /** 
   * Puts the turtle to a new position with the given 
   * screen y-coordinates without drawing a trace.
   * (Coordinate system with origin at upper left
   * vertex of playground, same coordinate increments as turtle coordinates.)

   * @return the Turtle reference to allow chaining
   */
  public Turtle setScreenY(int y)
  {
    check();
    synchronized (playground)
    {
      return setY(toTurtleY(y));
    }
  }

  /** 
   * Puts the turtle to a new position with the given x-coordinates 
   * without drawing a trace.
   * @return the Turtle reference to allow chaining
   */
  public Turtle setX(double x)
  {
    check();
    synchronized (playground)
    {
      getPlayground().clearTurtle(this);
      internalSetX(x);
      getPlayground().paintTurtles(this);
      addPositionToPath();
      return this;
    }
  }

  /** 
   * Puts the turtle to a new position with the given y-coordinates 
   * without drawing a trace.
   * @return the Turtle reference to allow chaining
   */
  public Turtle setY(double y)
  {
    check();
    synchronized (playground)
    {
      getPlayground().clearTurtle(this);
      internalSetY(y);
      getPlayground().paintTurtles(this);
      addPositionToPath();
      return this;
    }
  }

  protected void internalSetX(double x)
  {
    synchronized (playground)
    {
      position.setLocation(x, _getY());
    }
  }

  protected void internalSetY(double y)
  {
    position.setLocation(_getX(), y);
  }

  protected void internalSetPos(double x, double y)
  {
    position.setLocation(x, y);
  }

  /** 
   * Hides the Turtle.
   * Hiding the Turtle speeds up the graphics enormously.
   * @return the Turtle reference to allow chaining
   */
  public Turtle ht()
  {
    check();
    synchronized (playground)
    {
      internalHide();
      speed(-1);
      return this;
    }
  }

  /** 
   * Hide the Turtle.
   * Same as ht().
   * @return the Turtle reference to allow chaining
   */
  public Turtle hideTurtle()
  {
    return ht();
  }

  protected void internalHide()
  {
    getPlayground().clearTurtle(this);
    isTurtleShown = false;
    if (getPlayground().getPrinterG2D() == null && getPlayground().isRepaintEnabled)
      getPlayground().repaint();
  }

  /** 
   * Makes a hidden turtle visible.
   * @return the Turtle reference to allow chaining
   */
  public Turtle st()
  {
    check();
    synchronized (playground)
    {
      if (getPlayground().getPrinterG2D() != null)
        return this;  // Don't show the turtle during printing
      getPlayground().paintTurtle(this);
      isTurtleShown = true;
      if (getPlayground().isRepaintEnabled)
        getPlayground().repaint();
      return this;
    }
  }

  /** 
   * Same as st().
   * @return the Turtle reference to allow chaining
   */
  public Turtle showTurtle()
  {
    return st();
  }

  /** 
   * Reports if the turtle is hidden or shown.
   * @return true if the turtle is hidden; otherwise false
   */
  public boolean isHidden()
  {
    check();
    return !isTurtleShown;
  }

  private int getFramesPerSecond()
  {
    return this.framesPerSecond;
  }

  private void setAngle(double radians)
  {
    this.angle = radians;
  }

  /** 
   * Sets the turtle's heading (zero to north, clockwise positive).
   * @param degrees the compass direction (in degrees)
   * @return the Turtle reference to allow chaining
   */
  public Turtle setH(double degrees)
  {
    return setHeading(degrees);
  }

  /** 
   * Same as setH().
   * @return the Turtle reference to allow chaining
   */
  public Turtle setHeading(double degrees)
  {
    check();
    synchronized (playground)
    {
      setAngle(Math.toRadians(degrees));
      getTurtleRenderer().setAngle(Math.toRadians(degrees));
      getPlayground().clearTurtle(this);
      getPlayground().paintTurtles(this);
      return this;
    }
  }

  /** 
   * Sets the turtle's heading to a random value between 0 and 360 degrees.
   * @return the Turtle reference to allow chaining
   */
  public Turtle setRandomHeading()
  {
    return setHeading(360 * Math.random());
  }

  /** 
   * Returns the turtle's heading.
   * @return the compass direction (degrees, zero to north, clockwise positive)
   */
  public double heading()
  {
    check();
    synchronized (playground)
    {
      return Math.toDegrees(getAngle());
    }
  }

  /** 
   * Sets the turtle's heading (zero to north, clockwise positive).
   * @param degrees the compass direction (in degrees)
   * @return the old (previous) value
   */
  public double heading(double degrees)
  {
    check();
    synchronized (playground)
    {
      double tmp = Math.toDegrees(getAngle());
      setHeading(degrees);
      return tmp;
    }
  }

  /** 
   * Sets the turtle's speed.
   * @param speed the speed in turtle coordinates (pixels per second 
   * up to a certain limit depending on the hardware). Zero causes
   * forward() to return immediatetly, -1 if for no animation.
   * @return the turtle reference reference to allow chaining.
   */
  public Turtle speed(double speed)
  {
    check();
    this.speed = speed;
    return this;
  }

  private void internalRotate(double angle)
  {
    // angle in radians
    if (!isTurtleShown)
    {
      synchronized (playground)
      {
        setAngle(getAngle() + angle);
        if (getTurtleRenderer().imageChanged(getAngle()))
        {
          getTurtleRenderer().setAngle(getAngle());
        }
      }
      return;
    }
    if (angle != 0)
    {
      int iterations = getAngleIterations(angle);

      double sign = angle / Math.abs(angle);
      double increment = sign * getAngleSpeed() / (double)getFramesPerSecond();
      double startAngle = getAngle();

      for (int index = 0; index < iterations; index++)
      {
        long timeStamp = System.currentTimeMillis();

        synchronized (playground)
        {
          getPlayground().clearTurtle(this);

          if (index < iterations - 1)
          {
            setAngle(getAngle() + increment);
          }
          else
          {
            setAngle(startAngle + angle);
          }

          if (getTurtleRenderer().imageChanged(getAngle()))
          {
            getTurtleRenderer().setAngle(getAngle());
            getPlayground().paintTurtles(this);
          }
        }

        long newTimeStamp = System.currentTimeMillis();
        Double secs = new Double(1000. / getFramesPerSecond());
        long requiredTime = secs.longValue() - newTimeStamp + timeStamp;

        if (requiredTime > 0)
        {
          try
          {
            Thread.sleep(requiredTime);
          }
          catch (InterruptedException e)
          {
          }
        }
      }
    }
    getPlayground().paintTurtles(this);
  }

  private void internalMove(double length)
  {
    if (speed < 0.1 && speed > -0.1)  // speed approx 0
      return;

    if (getSpeed() > 0)
    {
      if (length != 0)
      {
        int iterations = getPathIterations(length);
        // an angle of 0 means: facing NORTH
        double startX = _getX();
        double startY = _getY();
        getLineRenderer().init(startX, startY);
        double dx = length * Math.sin(getAngle());
        double dy = length * Math.cos(getAngle());
        double incrementX = dx / iterations;
        double incrementY = dy / iterations;
        for (int index = 0; index < iterations; index++)
        {
          long timeStamp = System.currentTimeMillis();
          int nX = (int)_getX();
          int nY = (int)_getY();

          synchronized (playground)
          {
            getPlayground().clearTurtle(this);

            if (index < iterations - 1)
            {
              internalSetX(_getX() + incrementX);
              internalSetY(_getY() + incrementY);
            }
            else
            { // last step: Calc the "exact" value
              internalSetX(startX + dx);
              internalSetY(startY + dy);
            }
            if (nX != (int)_getX()
              || nY != -(int)_getY()
              || index == iterations - 1)
            {
              if (!isPenUp())
                getLineRenderer().lineTo(_getX(), _getY());
              getPlayground().paintTurtles(this);
            }
          }
          Double frames = new Double(1000.0 / getFramesPerSecond());
          long newTimeStamp = System.currentTimeMillis();
          long requiredTime = frames.longValue() - newTimeStamp + timeStamp;
          if (requiredTime > 0)
          {
            try
            {
              Thread.sleep(requiredTime);
            }
            catch (InterruptedException e)
            {
            }
          }
        }
      }
    }
    else
    { // Speed < 0, i.e. no animation
      synchronized (playground)
      {
        double startX = _getX();
        double startY = _getY();

        getLineRenderer().init(startX, startY);
        double dx = length * Math.sin(getAngle());
        double dy = length * Math.cos(getAngle());
        getPlayground().clearTurtle(this);
        internalSetX(startX + dx);
        internalSetY(startY + dy);
        if (!isPenUp())
          getLineRenderer().lineTo(_getX(), _getY());
        getPlayground().paintTurtles(this);
      }
    }
  }

  /** 
   * Turns the turtle to the left.
   * @param degrees the rotation angle (in degrees)
   * @return the Turtle reference to allow chaining
   */
  public Turtle lt(double degrees)
  {
    return left(degrees);
  }

  /**	
   * Same as lt()
   * @return the Turtle reference to allow chaining
   */
  public Turtle left(double degrees)
  {
    check();
    internalRotate(-Math.toRadians(degrees));
    return this;
  }

  /** 
   * Turns the turtle to the right.
   * @param degrees the rotation angle (in degrees)
   * @return the Turtle reference to allow chaining
   */
  public Turtle rt(double degrees)
  {
    return right(degrees);
  }

  /** 
   * Same as rt().
   * @param degrees the rotation angle (in degrees)
   * @return the Turtle reference to allow chaining
   */
  public Turtle right(double degrees)
  {
    check();
    internalRotate(Math.toRadians(degrees));
    return this;
  }

  /** 
   * Moves the turtle forwards. 
   * @param distance the moving distance in turtle coordinates. Negative
   * values move the turtle backwards.
   * @return the Turtle reference to allow chaining
   */
  public Turtle fd(double distance)
  {
    return forward(distance);
  }

  /** 
   * Same as fd().
   * @param distance the moving distance in turtle coordinates. Negative
   * values move the turtle backwards.
   * @return the Turtle reference to allow chaining
   */
  public Turtle forward(double distance)
  {
    check();
    internalMove(distance);
    addPositionToPath();
    return this;
  }

  /** 
   * Moves the turtle backwards. 
   * @param distance the moving distance in turtle coordinates. Negative
   * values move the turtle forwards.
   * @return the Turtle reference to allow chaining
   */
  public Turtle bk(double distance)
  {
    return back(distance);
  }

  /** 
   * Same as bk().
   * @param distance the moving distance in turtle coordinates. Negative
   * values move the turtle forwards.
   * @return the Turtle reference to allow chaining
   */
  public Turtle back(double distance)
  {
    check();
    internalMove(-distance);
    addPositionToPath();
    return this;
  }

  /**
   * Returns the distance between the current turtle position and an other turtle.
   * The turtle positions are bounded to the playground.
   * @param otherTurtle the other turtle to sight
   * @return distance between turtle and the other turtle (in turtle coordinates)
   */
  public double distance(Turtle otherTurtle)
  {
    return distance(otherTurtle.getX(), otherTurtle.getY());
  }

  /**
   * Returns the distance between the current turtle position and the
   * given position,
   * The turtle position is bounded to the playground.
   * @param x the x-coordinate of the target position
   * @param y the y-coordinate of the target position
   * @return distance between turtle and target (in turtle coordinates)
   */
  public double distance(double x, double y)
  {
    check();
    synchronized (playground)
    {
      return getPos().distance(x, y);
    }
  }

  /**
   * Returns the distance between the current turtle position and the
   * given position,
   * The turtle position is bounded to the playground.
   * @param pt the coordinate of the target position
   * @return distance between turtle and target (in turtle coordinates)
   */
  public double distance(Point2D.Double pt)
  {
    check();
    synchronized (playground)
    {
      return getPos().distance(pt);
    }
  }

  // Calculates the number of iterations when animating left 
  // or right (rotation).
  private int getAngleIterations(double dAngle)
  {
    if (getAngleSpeed() < 0)
    {
      return 1;
    }
    if (getAngleSpeed() == 0)
    {
      setAngleSpeed(1);
    }
    double dAbsAngle = Math.abs(dAngle);
    Double dValue = new Double(Math.ceil(dAbsAngle / getAngleSpeed() * getFramesPerSecond()));
    return dValue.intValue();
  }

  // Calculates the number of iterations when animating forwards or backwards.
  private int getPathIterations(double length)
  {
    if (speed < 0)
      return 1;

    if (speed == 0)
      speed(1);

    double dAbsLength = Math.abs(length);
    Double dValue = new Double(Math.ceil(dAbsLength / getSpeed() * getFramesPerSecond()));
    return dValue.intValue();
  }

  /**	
   * Lifts the turtle's pen up, so no trace is drawn.
   * @return the Turtle reference to allow chaining
   */
  public Turtle pu()
  {
    return this.penUp();
  }

  /**	
   * Same as pu().
   * @return the Turtle reference to allow chaining
   */
  public Turtle penUp()
  {
    check();
    synchronized (playground)
    {
      this.isPenUp = true;
      return this;
    }
  }

  /**	
   * Lowers the turtle's pen, so the trace is drawn.
   * @return the Turtle reference to allow chaining
   */
  public Turtle pd()
  {
    return this.penDown();
  }

  /**	
   * Same as pd().
   * @return the Turtle reference to allow chaining
   */
  public Turtle penDown()
  {
    check();
    synchronized (playground)
    {
      this.isPenUp = false;
      return this;
    }
  }

  /** 
   * Reports if the pen is up.
   * @return true if the pen is up; otherwise false
   */
  public boolean isPenUp()
  {
    check();
    return this.isPenUp;
  }

  // Return the bounds of this Turtle. This is required
  // by the methods that (return-)paint the turtles.
  protected Rectangle getBounds()
  {
    Rectangle rect = new Rectangle();

    Image img = getTurtleRenderer().currentImage();
    int nWidth = img.getWidth(getTurtleRenderer());
    int nHeight = img.getHeight(getTurtleRenderer());
    double x = (_getX() < 0) ? Math.floor(_getX()) : Math.ceil(_getX());
    double y = (_getY() < 0) ? Math.floor(_getY()) : Math.ceil(_getY());
    rect.setBounds((int)x - nWidth / 2, (int)y + nHeight / 2, nWidth, nHeight);
    return rect;
  }

  /** 
   * Return the turtle's Pen reference.
   * @return a reference to the Pen instance
   */
  public Pen getPen()
  {
    return pen;
  }

  /** 
   * Sets the line width of the traces.
   * (In wrap mode, larger lines may not be cut cleanly at the window border.)
   * @param width the line width in pixels
   * @return the Turtle reference to allow chaining
   */
  public Turtle penWidth(int width)
  {
    check();
    synchronized (playground)
    {
      return setLineWidth(width);
    }
  }

  /** 
   * Sets the line width of the traces.
   * (In wrap mode, larger lines may not be cut cleanly at the window border.)
   * @param width the line width in pixels
   * @return the Turtle reference to allow chaining
   */
  public Turtle setPenWidth(int width)
  {
    return penWidth(width);
  }

  /** 
   * Returns the current pen width.
   * @return the pen width in pixels
   */
  public int penWidth()
  {
    check();
    return (int)this.getPen().getLineWidth();
  }

  /** 
   * Returns the current pen width.
   * @return the pen width in pixels
   */
  public int getPenWidth()
  {
    return penWidth();
  }

  /** 
   * Sets the line width of the traces.
   * (In wrap mode, larger lines may not be cut cleanly at the window border.)
   * @param lineWidth the line width in turtle coordinates (pixels)
   * @return the Turtle reference to allow chaining
   */
  public Turtle setLineWidth(double lineWidth)
  {
    check();
    synchronized (playground)
    {
      getPen().setLineWidth((float)lineWidth);
      return this;
    }
  }

  /** 
   * Sets the turtle's color.
   * @param color the new turtle color. If a custom turtle image is used,
   * the method does nothing
   * @return the Turtle reference to allow chaining
   */
  public Turtle setColor(Color color)
  {
    check();
    if (turtleImg != null)
      return this;
    synchronized (playground)
    {
      internalSetColor(color);
      getPlayground().paintTurtles();
      return this;
    }
  }

  /** 
   * Sets the turtle's color as string.
   * The X11 color names are supported.
   * @param colorStr the turtle's color (default: cyan); if the
   * string is not one of the predefined values or a custom turtle
   * image is used, the color is not modified; if a custom turtle image
   * is used, the method does nothing
   * @return the Turtle reference to allow chaining
   */
  public Turtle setColor(String colorStr)
  {
    check();
    if (turtleImg != null)
      return this;
    Color color = toColor(colorStr);
    if (color != null)
      return setColor(color);
    return this;
  }

  /**
   * Returns the Color reference for the given color as string.
   * The X11 color names are supported.
   * @return the Color reference or null, if the given color is not
   * one of the predefined values
   */
  public static Color toColor(String colorStr)
  {
    return X11Color.toColor(colorStr);
  }

  private void internalSetColor(Color color)
  {
    this.color = color;
    if (getTurtleRenderer() == null)
    {
      turtleRenderer = createTurtleRenderer();
      getTurtleRenderer().init(getTurtleFactory(), getAngleResolution());
    }
    else
    {
      getTurtleRenderer().init(new TurtleFactory(),
        this.angleResolution);
    }
  }

  /** 
   * Sets the fill color for fill(). 
   * The fill color for fillToPoint(), fillToHorizontal(), fillToVertical()
   * is not affected.
   * @param color the new fill color (default: Color.blue).
   * @return the Turtle reference to allow chaining
   */
  public Turtle setFillColor(Color color)
  {
    check();
    synchronized (playground)
    {
      getPen().setFillColor(color);
      return this;
    }
  }

  /** 
   * Sets the fill color for fill() using the color as string. 
   * The fill color for fillToPoint(), fillToHorizontal(), fillToVertical()
   * is not affected.
   * The X11 color names are supported.
   * @param colorStr the fill color (default: blue); if the
   * string is not one of the predefined values, the color is not modified
   * @return the Turtle reference to allow chaining
   */
  public Turtle setFillColor(String colorStr)
  {
    check();
    Color color = toColor(colorStr);
    if (color != null)
      return setFillColor(color);
    return this;
  }

  /** 
   * Returns the turtle's current fill color.
   * @return the fill color
   */
  public Color getFillColor()
  {
    check();
    return getPen().getFillColor();
  }

  /** 
   * Returns the X11 color string of the turtle's current fill color.
   * @return the fill color string (lowercase); empty if not an X11 color 
   */
  public String getFillColorStr()
  {
    check();
    return X11Color.toColorStr(getFillColor());
  }

  /** 
   * Returns the turtle's current color.
   * @return the turtle color
   */
  public Color getColor()
  {
    check();
    return color;
  }

  /** 
   * Returns the X11 color string of the turtle's current color.
   * @return the turtle color string (lowercase); empty if not an X11 color
   */
  public String getColorStr()
  {
    check();
    return X11Color.toColorStr(getColor());
  }

  /**
   * Sets the turtle's pen color.
   * @param color the new pen color
   * @return the Turtle reference to allow chaining
   */
  public Turtle setPenColor(Color color)
  {
    check();
    synchronized (playground)
    {
      getPen().setColor(color);
      return this;
    }
  }

  /**
   * Sets the turtle's pen color as string.
   * The X11 color names are supported.
   * @param colorStr the pen color (default: blue); if the
   * string is not one of the predefined values, the color is not modified
   * @return the Turtle reference to allow chaining
   */
  public Turtle setPenColor(String colorStr)
  {
    check();
    Color color = toColor(colorStr);
    if (color != null)
      return setPenColor(color);
    return this;
  }

  /** 
   * Returns the turtle's pen color.
   * @return the pen color
   */
  public Color getPenColor()
  {
    check();
    return getPen().getColor();
  }

  /** 
   * Returns the X11 color string of the turtle's current pen color.
   * @return the turtle pen color string (lowercase); empty if not an X11 color
   */
  public String getPenColorStr()
  {
    return X11Color.toColorStr(getPenColor());
  }

  /**
   * Moves the turtle back "home", i.e. to coordinates (0, 0), 
   * heading north. Other turtle properties are not modified.
   * @return the Turtle reference to allow chaining
   */
  public Turtle home()
  {
    check();
    // first : clean the Turtle!
    synchronized (playground)
    {
      getPlayground().clearTurtle(this);
      position = new Point2D.Double(0, 0);
      setHeading(0);

      return this;
    }
  }

  /**	
   * Sets the pen color to the background color, so existing traces
   * are erased.
   * @return the Turtle reference to allow chaining
   */
  public Turtle pe()
  {
    return penErase();
  }

  /**	
   * Same as pe().
   * @return the Turtle reference to allow chaining
   */
  public Turtle penErase()
  {
    check();
    synchronized (playground)
    {
      this.internalPenErase();
      return this;
    }
  }

  protected void internalPenErase()
  {
    this.setPenColor(getPlayground().getBackground());
  }

  /**	
   * Shows a turtle image (a clone) at the current position/heading.
   * @return the Turtle reference to allow chaining
   */
  public Turtle stampTurtle()
  {
    check();
    synchronized (playground)
    {
      getPlayground().stampTurtle(this, null);
      return this;
    }
  }

  /**	
   * Shows a turtle image (a clone) with given color at the current position/heading.
   * If the turtle has a custom image, the clone's image is the same and
   * the given color is not used.
   * @return the Turtle reference to allow chaining
   */
  public Turtle stampTurtle(Color color)
  {
    check();
    synchronized (playground)
    {
      getPlayground().stampTurtle(this, color);
      return this;
    }
  }

  /**	
   * Shows a turtle image (a clone) with given color as string at the current position/heading.
   * If the turtle has a custom image, the clone's image is the same and
   * the given color is not used.
   * @return the Turtle reference to allow chaining
   */
  public Turtle stampTurtle(String colorStr)
  {
    return stampTurtle(toColor(colorStr));
  }

  /**
   * Returns the direction (heading) to a given other turtle.
   * @param otherTurtle the other turtle to sight
   * @return the angle from the current turtle position
   to the given target point (in degrees, clockwise positive measured from north)
   */
  public double towards(Turtle otherTurtle)
  {
    check();
    synchronized (playground)
    {
      double dx = otherTurtle.getX() - getX();
      double dy = otherTurtle.getY() - getY();
      double result = Math.toDegrees(Math.atan2(dx, dy));
      return (result < 0) ? result + 360 : result;
    }
  }

  /**
   * Returns the direction (heading) to a given position.
   * @param x the x-coordinate of the target
   * @param y the x-coordinate of the target
   * @return the angle from the current turtle position
   to the given target point (in degrees, clockwise positive measured from north)
   */
  public double towards(double x, double y)
  {
    check();
    synchronized (playground)
    {
      double dx = x - getX();
      double dy = y - getY();
      double result = Math.toDegrees(Math.atan2(dx, dy));
      return (result < 0) ? result + 360 : result;
    }
  }

  /**
   * Returns the direction (heading) to a given position.
   * @param pt the coordinates of the target
   * @return the angle from the current turtle position
   to the given target point (in degrees, clockwise positive measured from north)
   */
  public double towards(Point2D.Double pt)
  {
    return towards(pt.getX(), pt.getY());
  }

  /**
   * Returns the relative direction to a given other turtle.
   * @param otherTurtle the other turtle to sight
   * @return the angle the turtle must turn to point to the target point 
   (in degrees, clockwise positive)
   */
  public double direction(Turtle otherTurtle)
  {
    return towards(otherTurtle) - heading();
  }

  /**
   * Returns the relative direction to a given position.
   * @param x the x-coordinate of the target
   * @param y the x-coordinate of the target
   * @return the angle the turtle must turn to point to the target point 
   (in degrees, clockwise positive)
   */
  public double direction(double x, double y)
  {
    return towards(x, y) - heading();
  }

  /**
   * Returns the direction to a given position.
   * @param pt the coordinates of the target
   * @return the angle (the turtle must turn to point to the target point 
   (in degrees, clockwise positive)
   */
  public double direction(Point2D.Double pt)
  {
    return towards(pt) - heading();
  }

  // Puts the turtle to top (i.e. above any other turtle).
  protected void internalToTop()
  {
    this.getPlayground().toTop(this);
  }

  // Puts the Turtle to the bottom (i.e. under any other turtle).
  protected void internalToBottom()
  {
    this.getPlayground().toBottom(this);
  }

  /** 
   * Puts the turtle to the bottom, so other turtles 
   * will be drawn above.
   * @return the Turtle reference to allow chaining
   */
  public Turtle toBottom()
  {
    check();
    synchronized (playground)
    {
      internalToBottom();
      getPlayground().paintTurtles();
      return this;
    }
  }

  /** 
   * Puts the turtle to the top, so other turtles 
   * will be drawn below.
   * @return the Turtle reference to allow chaining
   */
  public Turtle toTop()
  {
    check();
    synchronized (playground)
    {
      this.getPlayground().paintTurtles(this);
      return this;
    }
  }

  protected int getEdgeBehaviour()
  {
    return edgeBehaviour;
  }

  protected void setEdgeBehaviour(int edgeBehaviour)
  {
    synchronized (playground)
    {
      this.edgeBehaviour = edgeBehaviour;
    }
  }

  /** 
   * Sets the turtle to clip mode.
   * In clip mode the turtle may move outside the window.
   * @return the Turtle reference to allow chaining
   */
  public Turtle clip()
  {
    check();
    synchronized (playground)
    {
      setEdgeBehaviour(CLIP);
      return this;
    }
  }

  /**
   * Sets the turtle to wrap mode.
   * In wrap mode the turtle remains visible in the window, i.e. 
   * when it leaves the window on one side, it reappears on the opposite side
   * (torus symmetry). 
   * @return the Turtle reference to allow chaining
   */
  public Turtle wrap()
  {
    check();
    synchronized (playground)
    {
      setEdgeBehaviour(WRAP);
      return this;
    }
  }

  /** 
   * Reports if the turtle is in clip mode.
   * @return true if in clip mode; otherwise false
   */
  public boolean isClip()
  {
    check();
    synchronized (playground)
    {
      return (getEdgeBehaviour() == CLIP);
    }
  }

  /** 
   * Requests if the turtle is in wrap mode.
   * @return true if in wrap mode; otherwise false
   */
  public boolean isWrap()
  {
    check();
    synchronized (playground)
    {
      return (getEdgeBehaviour() == WRAP);
    }
  }

  /** 
   * Fills a closed region (flood fill) with the current fill color 
   * (default: Color.blue).
   * A region is bounded by lines of any color different than the pixel 
   * color at the current turtle position and by the border of the window. 
   * If this pixel color is the same as the fill color, nothing is done, unless
   * one of the 8 neighbor pixels has a different color.
   * @return the Turtle reference to allow chaining
   */
  public Turtle fill()
  {
    check();
    synchronized (playground)
    {
      getPlayground().fill(this);
      return this;
    }
  }

  /** 
   * Fills a closed region (flood fill) with the current fill color 
   * (default: Color.blue) as if the turtle where at the given coordinates.
   * A region is bounded by lines of any color different to the pixel 
   * color at the given coordinates and by the border of the window. 
   * If this pixel color is the same as the fill color, nothing is done, unless
   * one of the 8 neighbor pixels has a different color.
   * @param x the x-coordinate of the inner point
   * @param y the y-coordinate of the inner point
   * @return the Turtle reference to allow chaining
   */
  public Turtle fill(double x, double y)
  {
    check();
    synchronized (playground)
    {
      double oldX = getX();
      double oldY = getY();
      boolean hidden = isHidden();
      ht();
      setPos(x, y);
      getPlayground().fill(this);
      setPos(oldX, oldY);
      if (!hidden)
        st();
      return this;
    }
  }

  /**
   * Fills a closed region (flood fill) with the current fill color
   * (default: Color.blue) as if the turtle where at the given coordinates.
   * A region is bounded by lines of any color different to the pixel
   * color at the given coordinates and by the border of the window.
   * If this pixel color is the same as the fill color, nothing is done, unless
   * one of the 8 neighbor pixels has a different color.
   * @param pt coordinates of the inner point
   * @return the Turtle reference to allow chaining
   */
  public Turtle fill(Point2D.Double pt)
  {
    return fill(pt.x, pt.y);
  }

  /** 
   * Draw the specified text at the current turtle position using
   * the current text font (default SansSerif, Font.PLAIN, 24).
   * The text is left aligned, e.g. it starts at the turtle position.
   * @return the Turtle reference to allow chaining
   */
  public Turtle label(String text)
  {
    return label(text, 'l');
  }
  
  /** 
   * Draw the specified text at the current turtle position using
   * the current text font (default SansSerif, Font.PLAIN, 24).
   * Text alignment: align = 'l': left, 'c': center, 'r': right
   * @return the Turtle reference to allow chaining
   */
  public Turtle label(String text, char align)
  {
    check();
    synchronized (playground)
    {
      if (text == null || text.length() == 0)
        return this;
      getPlayground().label(text, this, align);
      return this;
    }
  }

  /** Sets the current text font (default: 
   * Font("SansSerif", Font.PLAIN, 24).
   * @param font the new text font.
   * @return the Turtle reference to allow chaining
   */
  public Turtle setFont(Font font)
  {
    check();
    synchronized (playground)
    {
      getPen().setFont(font);
      return this;
    }
  }

  /** 
   * Sets the current text font (default: 
   * Font("SansSerif", Font.PLAIN, 24).
   * If you want to know what fonts are available on your system,
   * call getAvailableFontFamilies().
   * See java.awt.Font for more information about fontName, style and size.
   * @param fontName the name of the font
   * @param style the font style id
   * @param size the font size
   * @return the Turtle reference to allow chaining
   */
  public Turtle setFont(String fontName, int style, int size)
  {
    check();
    synchronized (playground)
    {
      getPen().setFont(new Font(fontName, style, size));
      return this;
    }
  }

  /** 
   * Sets the font size.
   * @param size the font size
   * @return the Turtle reference to allow chaining
   */
  public Turtle setFontSize(int size)
  {
    check();
    synchronized (playground)
    {
      getPen().setFontSize(size);
      return this;
    }
  }

  /** 
   * Sets the font style.
   * @param style the font style id
   * @return the Turtle reference to allow chaining
   */
  public Turtle setFontStyle(int style)
  {
    check();
    synchronized (playground)
    {
      getPen().setFontStyle(style);
      return this;
    }
  }

  /** 
   * Provides information about all font families 
   * currently available on your system.
   * Each font name is a string packed into a array of strings.
   */
  public static String[] getAvailableFontFamilies()
  {
    return Pen.getAvailableFontFamilies();
  }

  /** 
   * Returns the current font.
   * @return the text font
   */
  public Font getFont()
  {
    return getPen().getFont();
  }

  /**
   * Returns width of text with current font (in pixels).
   */
  public int getTextWidth(String text)
  {
    return getPen().getTextWidth(text);
  }

  /**
   * Returns height of text with current font (in pixels).
   */
  public int getTextHeight()
  {
    return getPen().getTextHeight();
  }

  /**
   * Returns ascender height of text with current font (in pixels).
   */
  public int getTextAscent()
  {
    return getPen().getTextAscent();
  }

  /**
   * Returns descender height of text with current font (in pixels).
   */
  public int getTextDescent()
  {
    return getPen().getTextDescent();
  }

  /** 
   * Creates a partial clone of the turtle.
   * The playground, color or image, position and heading are the same.
   * Other properties are those of a new turtle.
   * @return the cloned object reference as an Object type, because
   * it overrides Object.clone(). You may cast it to a Turtle
   */
  public Object clone()
  {
    synchronized (playground)
    {
      Turtle t;

      if (turtleImg == null)
      {
        t = new Turtle(this, false);
        t.setColor(getColor());
      }
      else
        t = new Turtle(this, turtleImg, false);
      t.setPos(getX(), getY());
      t.heading(heading());
      t.showTurtle();
      return t;
    }
  }

  /**  
   * Sets anti-aliasing (for the turtle trace buffer) on or off.
   * This may result in an better image quality, especially
   * for filling operations (platform dependant).
   * @return the Turtle reference to allow chaining
   */
  public Turtle antiAliasing(boolean on)
  {
    synchronized (playground)
    {
      getPlayground().setAntiAliasing(on);
      return this;
    }
  }

  /**
   * Prints the graphics context to an attached printer with
   * the given magnification scale factor.
   * scale = 1 will print on standard A4 format paper.<br>
   *
   * The given tp must implement the GPrintable interface,
   * e.g. the single method void draw(), where all the
   * drawing into the GPanel must occur.
   *
   * Be aware the turtle(s) state (position, direction, etc.)
   * must be reinitialized, because draw() is called several
   * times by the printing system.
   *
   * A standard printer dialog is shown before printing is
   * started. Only turtle traces are printed.
   * 
   * <br>
   * Example:<br>
   * 
   import ch.aplu.turtle.*;<br>
   <br>
   public class PrintTest implements TPrintable<br>
   {<br>
   private Turtle t = new Turtle();<br>
   <br>
   public PrintTest()<br>
   {<br>
   draw();           // Draw on screen<br>
   t.print(this);    // Draw on printer<br>
   }<br>
   <br>
   public void draw()<br>
   {<br>
   t.home();   // Needed for initialization<br>
   for (int i = 0; i < 5; i++)<br>
   t.fd(20).rt(90).fd(20).lt(90);<br>
   }<br>
   <br>
   public static void main(String[] args)<br>
   {<br>
   new PrintTest();<br>
   }<br>
   }<br>
   * 
   * @param tp a TPrintable reference
   * @param scale the scaling factor
   */
  public boolean print(TPrintable tp, double scale)
  {
    return getPlayground().print(tp, scale);
  }

  /**
   * Same as print(tp, scale) with scale = 1.
   * @param tp a TPrintable reference
   */
  public boolean print(TPrintable tp)
  {
    return print(tp, 1);
  }

  /**
   * Print the turtle's current playground with given scale.
   * @param scale the scaling factor
   */
  public boolean printScreen(double scale)
  {
    return print(null, scale);
  }

  /**
   * Same as printScreen(scale) with scale = 1.
   */
  public boolean printScreen()
  {
    return printScreen(1);
  }

  /**
   * Clears the turtle's playground. All turtle images, traces and text are erased,
   * and the turtles remain (hidden) at their positions.
   * @return the Turtle reference to allow chaining
   */
  public Turtle clear()
  {
    check();
    synchronized (playground)
    {
      getPlayground().clear();
      return this;
    }
  }

  /**
   * Clears the turtle's playground but painting it with the given color.
   * All traces and text are erased, and the turtles remain (hidden) 
   * at their positions.
   * @return the Turtle reference to allow chaining
   */
  public Turtle clear(Color color)
  {
    check();
    synchronized (playground)
    {
      getPlayground().clear(color);
      return this;
    }
  }

  /**
   * Clears the turtle's playground but painting it with the given color.
   * All traces and text are erased, and the turtles remain (hidden) 
   * at their positions.
   * The X11 color names are supported.
   * @param colorStr the background color (default: white); if the
   * string is not one of the predefined values, the current background color is used
   * @return the Turtle reference to allow chaining
   */
  public Turtle clear(String colorStr)
  {
    check();
    Color color = toColor(colorStr);
    if (color != null)
      return clear(color);
    return clear();
  }

  /**
   * Clears the turtle's playground. All traces and text are erased, 
   * but the turtles remain (visible) at their positions.
   * @return the Turtle reference to allow chaining
   */
  public Turtle clean()
  {
    check();
    synchronized (playground)
    {
      getPlayground().clean();
      return this;
    }
  }

  /**
   * Clears the turtle's playground but painting it with the given color.
   * All traces and text are erased, but the turtles remain (visible) 
   * at their positions.
   * @return the Turtle reference to allow chaining
   */
  public Turtle clean(Color color)
  {
    check();
    synchronized (playground)
    {
      getPlayground().clean(color);
      return this;
    }
  }

  /**
   * Clears the turtle's playground but painting it with the given color.
   * All traces and text are erased, but the turtles remain (visible) 
   * at their positions.
   * The X11 color names are supported.
   * @param colorStr the background color (default: white); if the
   * string is not one of the predefined values, the current background color is used
   * @return the Turtle reference to allow chaining
   */
  public Turtle clean(String colorStr)
  {
    check();
    Color color = toColor(colorStr);
    if (color != null)
      return clean(color);
    return clean();
  }

  /**
   * Draws the given image into the background (trace buffer). The image
   * is loaded with Turtle.getImage(). If the image can't be loaded,
   * nothing happens.
   * @param imagePath the file name or url
   * @return the Turtle reference to allow chaining
   */
  public Turtle drawBkImage(String imagePath)
  {
    check();
    getPlayground().setBkImage(imagePath);
    return this;
  }

  /**
   * Non-static version of sleep().
   * Checks if Turtle frame is disposed and throws RuntimeException. 
   */
  public void _sleep(int time)
  {
    check();
    try
    {
      Thread.currentThread().sleep(time);
    }
    catch (Exception e)
    {
    }
  }

  /**
   * Delays execution for the given amount of time (in ms).
   */
  public static void sleep(int time)
  {
    try
    {
      Thread.currentThread().sleep(time);
    }
    catch (Exception e)
    {
    }
  }

  /** 
   * Sets the title of turtle's playground
   * @param text the title text
   */
  public void setTitle(final String text)
  {
    check();
    if (EventQueue.isDispatchThread())
      turtleFrame.setTitle(text);
    else
    {
      try
      {
        EventQueue.invokeAndWait(new Runnable()
        {
          public void run()
          {
            turtleFrame.setTitle(text);
          }
        });
      }
      catch (Exception ex)
      {
      }
    }
  }

  /**
   * Returns libarary version information.
   * @return version information
   */
  public static String version()
  {
    return SharedConstants.VERSION;
  }

  /**
   * Returns properties file location.
   * @return properties information
   */
  public static String getPropLocation()
  {
    MyProperties props = new MyProperties();
    props.search();
    return props.getLocation();
  }

  /**
   * Returns environment information (JRE and OS).
   * @return environment information
   */
  public static String getEnvironment()
  {
    return "JRE version " + System.getProperty("java.version") + " running on "
      + System.getProperty("os.name");
  }

  /**
   * Enables/disables automatic repainting (default: enabled).
   * Disabling automatic repainting and hiding the
   * turtle speeds up the graphics enormously.
   * @param b if true, automatic repaining is performed; otherwise repainting
   * must be performed by calling repaint()
   */
  public void enableRepaint(boolean b)
  {
    getPlayground().enableRepaint(b);
  }

  /**
   * Performs manual repainting when automatic repainting is
   * disabled.
   */
  public void repaint()
  {
    check();
    getPlayground().repaint();
  }

  /**
   * Returns the color of the pixel at the current turtle position.
   * @return the color of the pixel at the turtle position or null, if the
   * turtle is outside the playground
   */
  public Color getPixelColor()
  {
    check();
    synchronized (playground)
    {
      return getPlayground().getPixelColor(this);
    }
  }

  /**
   * Returns the color of the pixel at the current turtle position as X11 string.
   * @return the X11 color (lowercase) of the pixel at the turtle position; empty if not an X11 color;
   * null, if the turtle is outside the playground
   */
  public String getPixelColorStr()
  {
    return X11Color.toColorStr(getPixelColor());
  }

  /**
   * Adds a status window attached at the bottom of the game grid window.
   * The dialog has no decoration, the same width as the turtle frame and
   * the given height.
   * @param height the height of the status bar in pixels
   */
  public void addStatusBar(int height)
  {
    if (turtleFrame != null)
      turtleFrame.addStatusBar(height);
  }

  /**
   * Replaces the text in the status bar by the given text using 
   * the current JOptionPane font and color. 
   * The text is left-justified, vertical-centered and may be multi-line.
   * If there is no status bar, nothing happens.
   * @param text the text to display
   */
  public void setStatusText(String text)
  {
    check();
    if (turtleFrame != null)
      turtleFrame.setStatusText(text);
  }

  /**
   * Replaces the text in the status bar by the given text 
   * using the given font and text color. 
   * The text is left-justified, vertical-centered and may be multi-line.
   * If there is no status bar, nothing happens.
   * @param text the text to display
   * @param font the text font
   * @param color the text color
   */
  public void setStatusText(String text, Font font, Color color)
  {
    check();
    if (turtleFrame != null)
      turtleFrame.setStatusText(text, font, color);
  }

  /**
   * Shows/hides the status bar. If there is no status bar,
   * nothing happens.
   * @param show if true, the status bar is visible; otherwise invisible
   */
  public void showStatusBar(boolean show)
  {
    if (turtleFrame != null)
      turtleFrame.setVisible(show);
  }

  /**  
   * Attachs a imaginary rubber band between the turtle and the given 
   * anchor point. When the turtle now moves, the area  
   * covered by the rubber band is filled with the current pen color. 
   * Filling is on until fillOff() is called.
   * @param x the x-coordinate of the anchor point
   * @param y the y-coordinate of the anchor point
   * @return the Turtle reference to allow chaining
   */
  public Turtle fillToPoint(double x, double y)
  {
    check();
    synchronized (playground)
    {
      fillMode = FillMode.FILL_POINT;
      xFillAnchor = x;
      yFillAnchor = y;
      return this;
    }
  }

  /**  
   * Same as fillToPoint(x, y) using the current turtle coordinates.
   * @return the Turtle reference to allow chaining
   */
  public Turtle fillToPoint()
  {
    return fillToPoint(getX(), getY());
  }

  /**
   * Draws a imaginary horizonatal line at given y-coordinate. 
   * When the turtle now moves, the trapezoidal area 
   * covered by this line and the turtle position is filled 
   * with the current pen color. Filling is on until fillOff() is called.  
   * @param y the y-coordinate of the horizontal line
   * @return the Turtle reference to allow chaining
   */
  public Turtle fillToHorizontal(double y)
  {
    check();
    synchronized (playground)
    {
      fillMode = FillMode.FILL_HORZ;
      yFillLine = y;
      return this;
    }
  }

  /**
   * Draws a imaginary vertical line at given x-coordinate. 
   * When the turtle now moves, the trapezoidal area 
   * covered by this line and the turtle position is filled 
   * with the current pen color. Filling is on until fillOff() is called.  
   * @param x the x-coordinate of the vertical line
   * @return the Turtle reference to allow chaining
   */
  public Turtle fillToVertical(double x)
  {
    check();
    synchronized (playground)
    {
      fillMode = FillMode.FILL_VERT;
      xFillLine = x;
      return this;
    }
  }

  /**
   * Stops the filling started by calling fillToPoint(), 
   * fillToHorizontal() or fillToVertical(). 
   * @return the Turtle reference to allow chaining
   */
  public Turtle fillOff()
  {
    check();
    synchronized (playground)
    {
      fillMode = FillMode.FILL_OFF;
      return this;
    }
  }

  private void setDefaults(MyProperties props)
  {
    boolean isLoaded = props.search();
    if (Options.getShapeColor() != null)
      defaultTurtleColor = Options.getShapeColor();
    else if (isLoaded)
    {
      Color turtleColor = TurtleFrame.getPropColor(props, "TurtleColor");
      if (turtleColor != null)
        defaultTurtleColor = turtleColor;
    }

    if (Options.getTraceColor() != null)
      defaultPenColor = Options.getTraceColor();
    else if (isLoaded)
    {
      Color penColor = TurtleFrame.getPropColor(props, "PenColor");
      if (penColor != null)
        defaultPenColor = penColor;
    }

    Integer value = null;
    if (Options.getTurtleSpeed() != -1)
      defaultSpeed = Options.getTurtleSpeed();
    else if (isLoaded)
    {
      value = props.getIntValue("TurtleSpeed");
      if (value != null)  // Entry found
        defaultSpeed = value;
    }

    if (Options.getAngleResolution() != -1)
      defaultAngleResolution = Options.getAngleResolution();
    else if (isLoaded)
    {
      value = props.getIntValue("AngleResolution");
      if (value != null)  // Entry found
        defaultAngleResolution = value;
    }

    if (Options.getFramesPerSecond() != -1)
      defaultFramesPerSecond = Options.getFramesPerSecond();
    else if (isLoaded)
    {
      value = props.getIntValue("FramesPerSecond");
      if (value != null)  // Entry found
        defaultFramesPerSecond = value;
    }

    if (Options.getEdgeBehavior() != -1)
      defaultEdgeBehavior = Options.getEdgeBehavior();
    else if (isLoaded)
    {
      String edge = props.getStringValue("EdgeBehavior");
      if (edge != null)  // Entry found
      {
        if (edge.trim().toLowerCase().equals("clip"))
          defaultEdgeBehavior = CLIP;
        if (edge.trim().toLowerCase().equals("wrap"))
          defaultEdgeBehavior = WRAP;
      }
    }
  }
  
  /**
   * Same as spray(density, extend, size) with size = 1.
   * @param density the number of points
   * @param spread the mean variation in turtle coordinates
   * @return the Turtle reference to allow chaining
   */
  public Turtle spray(int density, double spread)
  {
    return spray(density, spread, 1);
  }
  
  /**
   * Draws a random scatter plot at turtle position with current pen color.
   * @param density the number of points
   * @param spread the mean variation in turtle coordinates
   * @param size the size (diameter) in pixels of the points
   * @return the Turtle reference to allow chaining
   */
  public Turtle spray(int density, double spread, int size)
  {
    check();
    synchronized (playground)
    {
      getPlayground().spray(density, spread, size, this);
      return this;
    }
  }

  /**
   * Draws a dot (filled circle) at the current turtle position using the
   * current pen color. Any turtle traces under the circle are painted anew.
   * @param diameter the diameter of the circle
   * @return the Turtle reference to allow chaining
   */
  public Turtle dot(double diameter)
  {
    check();
    synchronized (playground)
    {
      getPlayground().dot(diameter, true, this);
      return this;
    }
  }

  /**
   * Draws an open dot (unfilled circle) with line width 1 at the current turtle position using the
   * current pen color. 
   * @param diameter the diameter of the circle
   * @return the Turtle reference to allow chaining
   */
  public Turtle openDot(double diameter)
  {
    check();
    synchronized (playground)
    {
      getPlayground().dot(diameter, false, this);
      return this;
    }
  }

  private void check()
  {
    if (turtleFrame != null && turtleFrame.mode == RELEASE_ON_CLOSE
      && TurtleFrame.isDisposed
      && !isExceptionThrown)
    {
      isExceptionThrown = true;
      if (!turtleFrame.threadList.contains(Thread.currentThread().getName()))
        throw new RuntimeException("Java frame disposed");
    }
  }

  /**
   * Moves the turtle to the given position. 
   * First the turtle turns to the new heading, 
   * then it moves forward.
   * @param x the new turtle's x coordinate
   * @param y the new turtle's y coordinate
   * @return the turtle reference to allow chaining
   */
  public Turtle moveTo(double x, double y)
  {
    synchronized (playground)
    {
      setHeading(towards(x, y));
      forward(distance(x, y));
      // For better accuracy 
      setX(x);
      setY(y);
      return this;
    }
  }

  /**
   * Moves the turtle to the given position. 
   * First the turtle turns to the new heading, 
   * then it moves forward.
   * @param pt the turtle coordinates of the new position
   * @return the turtle reference to allow chaining
   */
  public Turtle moveTo(Point2D.Double pt)
  {
    return moveTo(pt.x, pt.y);
  }

  /**
   * Checks if the turtle stays within the playground.
   * @return true, if the turtle position is in the playground boundary
   */
  public boolean isInPlayground()
  {
    Dimension dim = playground.getSize();
    int xmin = -dim.width / 2;
    int xmax = dim.width / 2;
    int ymin = -dim.height / 2;
    int ymax = dim.height / 2;
    return (getX() >= xmin && getX() <= xmax
      && getY() >= ymin && getY() <= ymax);
  }

  /**
   * Sets the mouse cursor image.
   * @param cursorType one of the constants in class java.awt.Cursor 
   * @return the turtle reference to allow chaining
   */
  public Turtle setCursor(int cursorType)
  {
    check();
    turtleFrame.setCursor(new Cursor(cursorType));
    return this;
  }

  /**
   * Sets the mouse cursor image to a custom icon. Normally the icon image
   * should have size 32x32 pixels with a transparent background. 
   * The method returns the current size of the cursor. In order to avoid image
   * transformation that reduces the image quality, the cursor image should be
   * set to this size.
   * From the given filename the image file is searched in the following order:<br>
   * - if application is packed into a jar archive, relative to the root of the jar archive<br>
   * - relative or absolute to current application directory<br>
   * - if filename starts with http://, from the given URL<br>
   * - if imagePath prefixed with _ relative to the root of the jar archive<br>
   * If the image can't be loaded, the cursor is unchanged
   * @param cursorImage the path to the image file
   * @param hotSpot the image point that reports the mouse
   * location (origin in upper left corner). Must be inside the custom image.
   * @return the size of the cursor the system uses currently; null if the image
   * can't be loaded
   */
  public Dimension setCustomCursor(String cursorImage, Point hotSpot)
  {
    check();
    return turtleFrame.setCustomCursor(cursorImage, hotSpot);
  }

  /**
   * Same as setCustomCursor(cursorImage, hotSpot) with
   * hotSpot in center of image.
   * @param cursorImage the path to the image file
   * @return the size of the cursor the system uses currently; null if the image
   * can't be loaded
   */
  public Dimension setCustomCursor(String cursorImage)
  {
    check();
    return turtleFrame.setCustomCursor(cursorImage);
  }

  /**
   * Checks if the turtle frame was disposed or released.
   */
  public static boolean isDisposed()
  {
    return TurtleFrame.isDisposed;
  }

  /**
   * Disposes the TurtleFrame.
   */
  public void dispose()
  {
    turtleFrame.dispose();
  }

  /**
   * Retrieves the image either from the jar resource, from local drive or
   * from a internet server. <br><br>
   * From the given imagePath the image file is searched in the following order:<br>
   * - if application is packed into a jar archive, relative to the root of the jar archive<br>
   * - relative to current application directory or absolute<br>
   * - if imagePath starts with http://, from the given URL<br>
   * - add prefix _ and search relative to the root of the jar archive
   * @param imagePath the file name or url
   * @return the buffered image or null, if the image search fails
   */
  public static synchronized GBitmap getImage(String imagePath)
  {
    BufferedImage image = null;

    // First try to load from jar resource
    // URL url = ClassLoader.getSystemResource(imagePath);  // Does not work with Webstart
    URL url = Thread.currentThread().getContextClassLoader().
      getResource(imagePath);

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

    // Failed, try to load from given path
    if (image == null)
    {
      try
      {
        String abs = new File(imagePath).getCanonicalPath();
        image = ImageIO.read(new File(abs));
      }
      catch (IOException e)
      {
      }
    }

    // Failed, try to load from server
    if (image == null && imagePath.indexOf("http://") != -1)
    {
      try
      {
        image = ImageIO.read(new URL(imagePath));
      }
      catch (IOException e)
      {
        // Read error
      }
    }

    if (image == null)
    {
      // Last: Try to load from _ prefixed subdirectory of jar resource
      url = Thread.currentThread().getContextClassLoader().
        getResource("_" + imagePath);
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
    }
    if (image == null)
      return null;

    GBitmap dest = new GBitmap(image.getWidth(), image.getHeight(),
      BufferedImage.TYPE_INT_ARGB);
    Graphics2D g2D = dest.createGraphics();
    g2D.drawImage(image, 0, 0, null);
    g2D.dispose();
    return dest;
  }

  /**
   * Draws the given image into background with the image center at 
   * the current turtle position and rotated to the current turtle 
   * viewing direction.
   * @return the turtle reference to allow chaining
   */
  public Turtle drawImage(BufferedImage bi)
  {
    playground.drawImage(GBitmap.scale(bi, 1, heading()), getX(), getY());
    return this;
  }

  /**
   * Retrieves the image either from the jar resource, from local drive or
   * from a internet server. <br><br>
   * From the given imagePath the image file is searched in the following order:<br>
   * - if application is packed into a jar archive, relative to the root of the jar archive<br>
   * - relative to current application directory or absolute<br>
   * - if imagePath starts with http://, from the given URL<br>
   * - add prefix _ and search relative to the root of the jar archive<br>
   * Draws the given image into background with the image center at 
   * the current turtle position and rotated to the current turtle 
   * viewing direction. If the image cannot be loaded, nothing happens.<br>
   * This method is a combination of getImage() and drawImage(BufferedImage bi).
   * @return the turtle reference to allow chaining
   */
  public Turtle drawImage(String imagePath)
  {
    GBitmap img = getImage(imagePath);
    if (img != null)
      playground.drawImage(GBitmap.scale(img, 1, heading()), getX(), getY());
    return this;
  }

  /**
   * Starts recording the path vertexes where the turtle moves. 
   * The path is used to draw a filled region with fillPath().<br>
   * If wrapping is enabled, the path vertexes correspond to the non-wrapped turtle
   * positions.
   * @return the turtle reference to allow chaining
   */
  public Turtle startPath()
  {
    check();
    synchronized (playground)
    {
      gp = new GeneralPath();
      Point2D.Double pt = playground.toScreenCoords(getPos());
      gp.moveTo(pt.x, pt.y);
      return this;
    }
  }

  /**
   * Closes the path (line from current position to where
   * the turtle path starts) and fills the polygon with
   * the current fill color. If no path was started or the path is
   * already closed, nothing happens.
   * @return the turtle reference to allow chaining
   */
  public Turtle fillPath()
  {
    check();
    synchronized (playground)
    {
      if (gp == null)
        return this;
      getPlayground().fillPath(gp, this);
      gp = null;
      return this;
    }
  }

  private void addPositionToPath()
  {
    if (gp != null)
    {
      Point2D.Double pt = getPlayground().toScreenCoords(_getPos());
      gp.lineTo(pt.x, pt.y);
    }
  }

  /**
   * Saves the current turtle state in a turtle state buffer. 
   * The buffer is stack oriented, e.g. it is a FIFO-buffer (First In-First Out).<br>
   * The following turtle properties are saved:<br>
   * - current position<br>
   * - current color<br>
   * - current heading<br><br>
   * 
   * The states can be restored by calling popState().
   * @return the turtle reference to allow chaining
   */
  public Turtle pushState()
  {
    check();
    synchronized (playground)
    {
      TurtleState state = new TurtleState(getX(), getY(), getColor(), heading());
      stateStack.add(state);
      return this;
    }
  }

  /**
   * Retrieves the last state from the turtle state buffer and 
   * sets the turtle properties accordingly. The state is then removed from
   * the state buffer.<br>
   * If the turtle state buffer is empty, nothing happens.
   * @return the turtle reference to allow chaining
   */
  public Turtle popState()
  {
    check();
    if (stateStack.isEmpty())
      return this;
    synchronized (playground)
    {
      int lastIndex = stateStack.size() - 1;
      TurtleState state = stateStack.get(lastIndex);
      setX(state.getX());
      setY(state.getY());
      setColor(state.getColor());
      heading(state.getHeading());
      stateStack.remove(lastIndex);
      return this;
    }
  }

  /**
   * Empties the state buffer. The current turtle state remains unchanged.
   * @return the turtle reference to allow chaining
   */
  public Turtle clearStates()
  {
    stateStack.clear();
    return this;
  }

  /**
   * Puts the turtle to a new relative position without drawing a trace.
   * For the given x, y coordinates a coordinate system relative to the current
   * position and heading (viewing direction) is used (zero at current
   * position, y-axis in viewing direction, x-axis perpendicular to the right).
   * @param x the relative x coordinate
   * @param y the relative y coordinate
   * @return the turtle reference to allow chaining
   */
  public Turtle viewingSetPos(double x, double y)
  {
    synchronized (playground)
    {
      double alpha = Math.toRadians(heading());
      double xnew = (Math.cos(alpha) * x + Math.sin(alpha) * y) + getX();
      double ynew = (-Math.sin(alpha) * x + Math.cos(alpha) * y) + getY();
      setPos(xnew, ynew);
      return this;
    }
  }

  /**
   * Moves the turtle to the new relative position. 
   * First the turtle turns to the new heading, then it moves forward.
   * For the given x, y coordinates a coordinate system relative to the current
   * position and heading (viewing direction) is used (zero at current
   * position, y-axis in viewing direction, x-axis perpendicular to the right).
   * @param x the relative x coordinate
   * @param y the relative y coordinate
   * @return the turtle reference to allow chaining
   */
  public Turtle viewingMoveTo(double x, double y)
  {
    synchronized (playground)
    {

      double alpha = Math.toRadians(heading());
      double xnew = (Math.cos(alpha) * x + Math.sin(alpha) * y) + getX();
      double ynew = (-Math.sin(alpha) * x + Math.cos(alpha) * y) + getY();
      moveTo(xnew, ynew);
      return this;
    }
  }

  /** 
   * Moves the turtle on a right-oriented circle from the 
   * current position with turtle's heading tangent direction 
   * and given radius.<br>
   * The movement is animated unless the turtle is hidden or its speed is -1.
   * The turtle ends its movement with the starting position and location.
   * All filling modes are supported.
   * @param radius the radius of the circle.If negative, nothing happens
   * @return the Turtle reference to allow chaining
   */
  public Turtle rightCircle(double radius)
  {
    if (speed > -0.1 && speed < 0.1)
      return this;
    synchronized (playground)
    {
      double x = getX();
      double y = getY();
      double dir = heading();
      rightArc(radius, 360);
      setPos(x, y);
      heading(dir);
      return this;
    }
  }

  /**
   * Moves the turtle on a right-oriented arc from the 
   * current position with turtle's heading tangent direction, 
   * given radius and given sector angle.
   * The movement is animated unless the turtle is hidden or its speed is -1. 
   * All filling modes are supported.
   * @param radius the radius of the arc. If negative, nothing happens.
   * @param angle the sector angle in degrees (if negative, performs leftArc())
   * @return the turtle reference to allow chaining
   */
  public Turtle rightArc(double radius, double angle)
  {
    if (speed > -0.1 && speed < 0.1)
      return this;
    if (radius <= 0 || angle == 0)
      return this;
    synchronized (playground)
    {
      if (angle > 0)
        arc(radius, angle, false);
      else
        arc(radius, -angle, true);
      return this;
    }
  }

  /** 
   * Moves the turtle on a left-oriented circle from the 
   * current position with turtle's heading tangent direction 
   * and given radius.
   * The movement is animated unless the turtle is hidden or its speed is -1. 
   * The turtle ends its movement with the starting position and location.
   * All filling modes are supported.
   * @param radius the radius of the circle.If negative, nothing happens
   * @return the Turtle reference to allow chaining
   */
  public Turtle leftCircle(double radius)
  {
    if (speed > -0.1 && speed < 0.1)
      return this;
    synchronized (playground)
    {
      double x = getX();
      double y = getY();
      double dir = heading();
      leftArc(radius, 360);
      setPos(x, y);
      heading(dir);
      return this;
    }
  }

  /**
   * Moves the turtle on a left-oriented arc from the 
   * current position with turtle's heading tangent direction, 
   * given radius and given sector angle.
   * The movement is animated unless the turtle is hidden or its speed is -1. 
   * All filling modes are supported.
   * @param radius the radius of the arc. If negative, nothing happens.
   * @param angle the sector angle in degrees (if negative, performs rightArc())
   * @return the turtle reference to allow chaining
   */
  public Turtle leftArc(double radius, double angle)
  {
    if (speed > -0.1 && speed < 0.1)
      return this;
    if (radius <= 0 || angle == 0)
      return this;
    synchronized (playground)
    {
      if (angle > 0)
        arc(radius, angle, true);
      else
        arc(radius, -angle, false);
      return this;
    }
  }

  private void arc(double radius, double angle, boolean left)
  {
    double delta = heading();
    double x0, y0;
    if (left)
    {
      x0 = getX() - radius * Math.cos(Math.toRadians(delta));
      y0 = getY() + radius * Math.sin(Math.toRadians(delta));
    }
    else
    {
      x0 = getX() + radius * Math.cos(Math.toRadians(delta));
      y0 = getY() - radius * Math.sin(Math.toRadians(delta));
    }

    double dalpha = 360.0 / 100;
    int n = (int)(angle / dalpha);
    dalpha = angle / n;
    double xOld = getX();
    double yOld = getY();
    lineRenderer.init(xOld, yOld);
    double dt = 1000 * radius * Math.toRadians(dalpha) / speed; // speed never 0
    boolean isShown = isTurtleShown;
    if (speed <= -0.1)  // not animated-> hide turtle during movement
      ht();
    for (int i = 1; i <= n; i++)
    {
      long startTime = System.currentTimeMillis();
      double xi, yi;
      if (left)
      {
        xi = x0 + radius * Math.cos(Math.toRadians(i * dalpha - delta));
        yi = y0 + radius * Math.sin(Math.toRadians(i * dalpha - delta));
      }
      else
      {
        xi = x0 - radius * Math.cos(Math.toRadians(i * dalpha + delta));
        yi = y0 + radius * Math.sin(Math.toRadians(i * dalpha + delta));
      }
      setPos(xi, yi);
      if (!isPenUp)
        lineRenderer.lineTo(xi, yi);
      xOld = xi;
      yOld = yi;

      if (speed >= 0.1)  // animated
      {
        isTurtleShown = false;  // to speed up the rotation
        if (left)
          left(dalpha);
        else
          right(dalpha);
        isTurtleShown = isShown;
        while (System.currentTimeMillis() - startTime < dt)
          sleep(1);
      }
    }
    if (speed <= -0.1)  // not animated
    {
      if (left)
        left(angle);
      else
        right(angle);
      if (isShown)
        st();
    }
  }

  /**
   * Returns a string with turtle information in format
   * Turtle(Position=(x, y), Heading=h) unless the turtle frame is disposed. 
   * All values are rounded to two decimal places.
   * @return turtle information
   */
  public String toString()
  {
    if (isDisposed())
      return "Turtle(frame disposed)";
    String formattedX = new DecimalFormat("#.##").format(getX());
    if (formattedX.equalsIgnoreCase("-0"))
      formattedX = "0";
    String formattedY = new DecimalFormat("#.##").format(getY());
    if (formattedY.equalsIgnoreCase("-0"))
      formattedY = "0";
    String formattedHeading = new DecimalFormat("#.##").format(heading());
    if (formattedY.equalsIgnoreCase("-0"))
      formattedY = "0";

    return "Turtle(Position=("
      + formattedX
      + "," + formattedY
      + "), Heading=" + formattedHeading + ")";
  }

  /**
   * Plays a sine tone with given frequency and duration and maximum volume.
   * The method blocks until the tone is finished.
   * @param frequency the frequency of the tone (in Hertz) (double rounded to int)
   * @param duration the duration of the tone (in Millisec) (double rounded to int)
   */
  public void sound(double frequency, double duration)
  {
    sound(100, frequency, duration, true);
  }

  /**
   * Plays a sine tone with given frequency and duration.
   * @param volume the sound volume (0..100) (double rounded to int)
   * @param frequency the frequency of the tone (in Hertz) (double rounded to int)
   * @param duration the duration of the tone (in Millisec) (double rounded to int)
   * @param blocking if true, the methods blocks until the tone is finished
   */
  public void sound(double volume, double frequency, double duration, boolean blocking)
  {
    AudioFormat audioFormat = getAudioFormat();
    ByteArrayOutputStream data = new ByteArrayOutputStream();
    float sampleRate = audioFormat.getSampleRate();
    int nbFrames = (int)(duration / 1000 * sampleRate);
    double t = 0;
    double durationSec = duration / 1000;
    double dt = 1.0 / sampleRate;
    int amplitude = 100;
    double tAttack;
    double tRelease;
    if (duration > 100)
    {
      tAttack = 0.05;
      tRelease = 0.05;
    }
    else
    {
      tAttack = durationSec / 2;
      tRelease = durationSec / 2;
    }
    for (int i = 0; i < nbFrames; i++)
    {
      int a;
      if (t < tAttack)
        a = (int)(t / tAttack * amplitude);  // Attack
      else if (t > durationSec - tRelease)  // Release
        a = (int)((durationSec - t) / tRelease * amplitude);
      else  // Substain
        a = amplitude;

      byte soundData = (byte)(a * Math.sin(2 * Math.PI * frequency * t));
      data.write(soundData);
      t += dt;
    }
    try
    {
      data.close();
    }
    catch (IOException ex)
    {
    }

    InputStream is
      = new ByteArrayInputStream(data.toByteArray());
    AudioInputStream audioInputStream
      = new AudioInputStream(is, audioFormat,
        data.size() / audioFormat.getFrameSize());
    try
    {
      SoundPlayer player = new SoundPlayer(audioInputStream);
      player.setVolume(Math.min(990, (int)(9.9 * volume)));
      if (blocking)
      {
        player.addSoundPlayerListener(
          new SoundPlayerListener()
          {
            public void notifySoundPlayerStateChange(int reason, int mixerIndex)
            {
              switch (reason)
              {
                case 0:
//                System.out.println("Notify: start playing");
                  break;
                case 1:
//                System.out.println("Notify: resume playing");
                  break;
                case 2:
//                System.out.println("Notify: pause playing");
                  break;
                case 3:
//                System.out.println("Notify: stop playing");
                  break;
                case 4:
//                System.out.println("Notify: end of resource");
                  sleep(500);
                  Monitor.wakeUp();
                  break;
              }
            }
          }
        );
      }
      player.play();
      if (blocking)
        Monitor.putSleep();
    }
    catch (Exception ex)
    {
      ex.printStackTrace();
    }
  }

  private AudioFormat getAudioFormat()
  {
    // 8000,11025,16000,22050,44100
    float sampleRate = 22050.0F;
//   float sampleRate = 44100.0F;

    // 8,16
    int sampleSizeInBits = 8;

    // 1,2
    int channels = 1;

    // true,false
    boolean signed = true;

    // true,false
    boolean bigEndian = false;

    return new AudioFormat(sampleRate, sampleSizeInBits,
      channels, signed, bigEndian);
  }

  protected void setTurtleImage(Image img)
  {
    turtleImage = img;
  }

  /**
   * Returns the turtle's image (not rotated).
   * @return the BufferedImage of the turtle
   */
  public BufferedImage getTurtleImage()
  {
    return (BufferedImage)turtleImage;
  }

  /**
   * Saves the playground (turtles and traces) in an image file.
   * @param fileName the image file path
   * @param formatName the image format (supported values: "PNG", "GIF")
   * @return true, if the operation is successful; otherwise false
   */
  public boolean savePlayground(String fileName, String formatName)
  {
    return playground.save(fileName, formatName);
  }
}
