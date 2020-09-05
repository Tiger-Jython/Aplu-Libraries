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

package ch.aplu.turtle;

import java.awt.*;
import javax.swing.*;
import java.awt.event.*;
import java.awt.geom.Point2D;
import java.util.ArrayList;

/** 
 * This class is used for the turtle application. 
 * It contains the <code>Playground</code> where the 
 * <code>Turtle</code>s lives.<br><br>
 * 
 * Default properties of the frame can
 * be changed from the library defaults by using a 
 * Java properties file turtlegraphics.properties. For more details,
 * consult turtlegraphics.properties found in the distribution.<br><br>
 */
public class TurtleFrame extends JFrame
  implements TurtleContainer, FocusListener
{
  // ------------------ Inner class MyMouseAdapter --------------------
  private class MyMouseAdapter extends MouseAdapter
  {
    public void mouseClicked(MouseEvent evt)
    {
      if (evt.getClickCount() > 2)
        return;

      if (doubleClickTime == 0)
      {
        if (evt.getClickCount() == 1 && mouseDoubleClickListener != null)
          mouseDoubleClickListener.notifyClick(evt);
        if (evt.getClickCount() == 2 && mouseDoubleClickListener != null)
          mouseDoubleClickListener.notifyDoubleClick(evt);
      }
      else
      {
        lastMouseEvent = evt;
        if (doubleClickTimer.isRunning())
        {
          doubleClickTimer.stop();
          if (mouseDoubleClickListener != null)
            mouseDoubleClickListener.notifyDoubleClick(evt);
        }
        else
          doubleClickTimer.restart();
      }
    }
  }

  // ------------------ Inner class MyTimerActionListener --------------------
  private class MyTimerActionListener implements ActionListener
  {
    public void actionPerformed(ActionEvent e)
    {
      doubleClickTimer.stop();
      if (mouseDoubleClickListener != null)
        mouseDoubleClickListener.notifyClick(lastMouseEvent);
    }
  }

  // ------------------ Inner class MyMouseDoubleClickListener ---------------
  private class MyMouseDoubleClickListener implements MouseDoubleClickListener
  {
    public void notifyClick(MouseEvent evt)
    {
      if (mouseDoubleClickListener != null)
        mouseDoubleClickListener.notifyClick(evt);
    }

    public void notifyDoubleClick(MouseEvent evt)
    {
      if (mouseDoubleClickListener != null)
        mouseDoubleClickListener.notifyDoubleClick(evt);
    }
  }

  // ---------------------------- Inner class MyMouseHitAdapter ---------------
  private class MyMouseHitAdapter extends MouseAdapter
  {
    public void mousePressed(MouseEvent evt)
    {
      Thread.UncaughtExceptionHandler h = new Thread.UncaughtExceptionHandler()
      {
        public void uncaughtException(Thread th, Throwable ex)
        {
          System.out.println("Exception in mouseHit(): " + ex);
        }
      };

      if (mouseHitListeners.isEmpty())
        return;

      for (final MouseHitListener listener : mouseHitListeners)
      {
        mouseHitButton = evt.getButton();

        Point point = evt.getPoint();
        final Point2D.Double pt
          = playground.toTurtleCoords((double)point.x, (double)point.y);
        Thread t = new Thread()
        {
          public void run()
          {
            listener.mouseHit(pt.x, pt.y);
          }
        };
        threadList.add(t.getName());
        t.setUncaughtExceptionHandler(h);
        t.start();
      }
    }
  }

  // ---------------------------- Inner class MyMouseHitXAdapter ---------------
  private class MyMouseHitXAdapter extends MouseAdapter
  {
    private boolean inCallback = false;

    public void mousePressed(MouseEvent evt)
    {
      if (inCallback)
        return;

      Thread.UncaughtExceptionHandler h = new Thread.UncaughtExceptionHandler()
      {
        public void uncaughtException(Thread th, Throwable ex)
        {
          System.out.println("Exception in mouseHitX(): " + ex);
        }
      };

      if (mouseHitXListener != null)
      {
        mouseHitButton = evt.getButton();

        Point point = evt.getPoint();
        final Point2D.Double pt
          = playground.toTurtleCoords((double)point.x, (double)point.y);
        Thread t = new Thread()
        {
          public void run()
          {
            inCallback = true;
            mouseHitXListener.mouseHitX(pt.x, pt.y);
            inCallback = false;
          }
        };
        threadList.add(t.getName());
        t.setUncaughtExceptionHandler(h);
        t.start();
      }
    }
  }

  // ---------------------------- Inner class MyKeyAdapter ---------------
  private class MyKeyAdapter implements KeyListener
  {
    public void keyPressed(KeyEvent evt)
    {
      synchronized (monitor)
      {
        keyCode = evt.getKeyCode();
        keyChar = evt.getKeyChar();
        modifiers = evt.getModifiers();
        modifiersText = KeyEvent.getKeyModifiersText(modifiers);
        gotKey = true;
        if (mustNotify)
          doNotify();
      }
    }

    public void keyReleased(KeyEvent evt)
    {
    }

    public void keyTyped(KeyEvent evt)
    {
    }
  }
// ---------------------------- End of inner classes ---------------------
  private Playground playground;
  private String DEFAULT_TITLE = "Java Turtle Playground";
  protected int mode = Turtle.STANDARDFRAME;
  private boolean isAppletFirst = true;
  static StatusBar statusBar = null;
  protected static int instanceCount = 0;
  protected static int leftBorderSize = 0;
  protected static TurtleFrame firstFrame = null;
  protected static final JMenuBar nullMenuBar = null;
  protected static boolean isDisposed = false;
  private MouseDoubleClickListener mouseDoubleClickListener = null;
  protected ArrayList<String> threadList = new ArrayList<String>();
  private char keyChar;
  private int keyCode;
  private int modifiers;
  private String modifiersText;
  private boolean gotKey = false;
  private boolean mustNotify = false;
  private Object monitor = new Object();
  private Thread waitThread = null;
  private static TurtleFrame myLastFrame = null;
  private int doubleClickTime = 0;
  private javax.swing.Timer doubleClickTimer;
  private MouseEvent lastMouseEvent;
  private ExitListener exitListener = null;
  private ArrayList<MouseHitListener> mouseHitListeners
    = new ArrayList<MouseHitListener>();
  private MouseHitXListener mouseHitXListener = null;
  private boolean useSystemLookAndFeel = true;
  private int mouseHitButton = 0;
  private Point _screenLocation = new Point();
  private static Point _lastScreenLocation = null;

  /** 
   * Creates a window with default title.
   * If turtlegraphics.properties is found, defaults are specified there.
   */
  public TurtleFrame()
  {
    MyProperties props = new MyProperties(Turtle.propertyVerbose);
    boolean isLoaded = props.search();

    String title = null;
    if (Options.getFrameTitle() != null)
      title = Options.getFrameTitle();
    else if (isLoaded)
      title = getFrameTitle(props);

    if (Options.getFrameMode() != -1)
      mode = Options.getFrameMode();
    else if (isLoaded)
      mode = getFrameMode(props);

    useSystemLookAndFeel = getSystemLookAndFeel(props);

    Point pos = new Point(-1, -1);
    int[] values;
    if (Options.getFramePosition() != null)
      pos = Options.getFramePosition();
    else if (isLoaded)
    {
      values = props.getIntArray("FramePosition", 2);
      if (values != null && values[0] >= 0 && values[1] >= 0)
        pos = new Point(values[0], values[1]);
    }

    Dimension dim = null;
    if (Options.getPlaygroundSize() != null)
      dim = Options.getPlaygroundSize();
    else if (isLoaded)
    {
      values = props.getIntArray("PlaygroundSize", 2);
      if (values != null && values[0] > 0 && values[1] > 0)
        dim = new Dimension(values[0], values[1]);
    }

    Color bkColor = null;
    if (Options.getBackgroundColor() != null)
      bkColor = Options.getBackgroundColor();
    else if (isLoaded)
      bkColor = getPropColor(props, "BackgroundColor");

    if (dim != null)
    {
      if (bkColor != null)
        init(title == null ? DEFAULT_TITLE : title, nullMenuBar, pos.x, pos.y,
          new Playground(dim, bkColor));
      else
        init(title == null ? DEFAULT_TITLE : title, nullMenuBar, pos.x, pos.y,
          new Playground(dim));
    }
    else
    {
      if (bkColor != null)
        init(title == null ? DEFAULT_TITLE : title, nullMenuBar, pos.x, pos.y,
          new Playground(bkColor));
      else
        init(title == null ? DEFAULT_TITLE : title, nullMenuBar, pos.x, pos.y,
          new Playground());
    }
  }

  /** 
   * Creates a window with given title.
   * If turtlegraphics.properties is found, all entries are ignored.
   */
  public TurtleFrame(String title)
  {
    init(title, nullMenuBar, -1, -1, new Playground());
  }

  /** 
   * Creates a window with default title and given JMenuBar.
   * If turtlegraphics.properties is found, all entries are ignored.
   */
  public TurtleFrame(JMenuBar menuBar)
  {
    init(DEFAULT_TITLE, menuBar, -1, -1, new Playground());
  }

  protected TurtleFrame(JMenuBar menuBar, MyProperties props)
  {
    boolean isLoaded = props.search();

    String title = null;
    if (Options.getFrameTitle() != null)
      title = Options.getFrameTitle();
    else if (isLoaded)
      title = getFrameTitle(props);

    if (Options.getFrameMode() != -1)
      mode = Options.getFrameMode();
    else if (isLoaded)
      mode = getFrameMode(props);

    Point pos = new Point(-1, -1);
    int[] values;
    if (Options.getFramePosition() != null)
      pos = Options.getFramePosition();
    else if (isLoaded)
    {
      values = props.getIntArray("FramePosition", 2);
      if (values != null && values[0] >= 0 && values[1] >= 0)
        pos = new Point(values[0], values[1]);
    }

    Dimension dim = null;
    if (Options.getPlaygroundSize() != null)
      dim = Options.getPlaygroundSize();
    else if (isLoaded)
    {
      values = props.getIntArray("PlaygroundSize", 2);
      if (values != null && values[0] > 0 && values[1] > 0)
        dim = new Dimension(values[0], values[1]);
    }

    Color bkColor = null;
    if (Options.getBackgroundColor() != null)
      bkColor = Options.getBackgroundColor();
    else if (isLoaded)
      bkColor = getPropColor(props, "BackgroundColor");

    if (dim != null)
    {
      if (bkColor != null)
        init(title == null ? DEFAULT_TITLE : title, menuBar, pos.x, pos.y,
          new Playground(dim, bkColor));
      else
        init(title == null ? DEFAULT_TITLE : title, menuBar, pos.x, pos.y,
          new Playground(dim));
    }
    else
    {
      if (bkColor != null)
        init(title == null ? DEFAULT_TITLE : title, menuBar, pos.x, pos.y,
          new Playground(bkColor));
      else
        init(title == null ? DEFAULT_TITLE : title, menuBar, pos.x, pos.y,
          new Playground());
    }
  }

  protected static String getFrameTitle(MyProperties props)
  {
    String value = props.getStringValue("FrameTitle");
    if (value != null)
      value = value.trim();
    return value;  // null, if not found
  }

  protected static boolean getSystemLookAndFeel(MyProperties props)
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

  protected static int getFrameMode(MyProperties props)
  {
    String value = props.getStringValue("ClosingMode");
    if (value != null)  // Entry found
    {
      value = value.trim();
      if (value.equals("TerminateOnClose"))
        return Turtle.STANDARDFRAME;
      if (value.equals("ClearOnClose"))
        return Turtle.CLEAR_ON_CLOSE;
      if (value.equals("AskOnClose"))
        return Turtle.ASK_ON_CLOSE;
      if (value.equals("DisposeOnClose"))
        return Turtle.DISPOSE_ON_CLOSE;
      if (value.equals("ReleaseOnClose"))
        return Turtle.RELEASE_ON_CLOSE;
      if (value.equals("NothingOnClose"))
        return Turtle.NOTHING_ON_CLOSE;
      return Turtle.STANDARDFRAME;  // Entry not valid
    }

    return Turtle.STANDARDFRAME;
  }

  protected static Color getPropColor(MyProperties props, String entry)
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
   * Creates a window with given title and JMenuBar.
   * If turtlegraphics.properties is found, all entries are ignored.
   */
  public TurtleFrame(String title, JMenuBar menuBar)
  {
    init(title, menuBar, -1, -1, new Playground());
  }

  /** 
   * Creates a window with default title and give background color.
   * If turtlegraphics.properties is found, all entries are ignored.
   */
  public TurtleFrame(Color bkColor)
  {
    init(DEFAULT_TITLE, nullMenuBar, -1, -1, new Playground(bkColor));
  }

  /** 
   * Creates a window with given title and background color.
   * If turtlegraphics.properties is found, all entries are ignored.
   */
  public TurtleFrame(String title, Color bkColor)
  {
    init(title, nullMenuBar, -1, -1, new Playground(bkColor));
  }

  /** 
   * Creates a window with given title, JMenuBar and background color.
   * If turtlegraphics.properties is found, all entries are ignored.
   */
  public TurtleFrame(String title, JMenuBar menuBar, Color bkColor)
  {
    init(title, menuBar, -1, -1, new Playground(bkColor));
  }

  /** 
   * Creates a window with given title, playground width and height.
   * The playground size is width x height and determines the
   * range of turtle coordinates: -width/2 .. width/2, -height/2..height/2.
   * with number of pixels: (width+1) x (height+1)
   * If turtlegraphics.properties is found, all entries are ignored.
   */
  public TurtleFrame(String title, int width, int height)
  {
    Dimension size = new Dimension(width, height);
    init(title, nullMenuBar, -1, -1, new Playground(size));
  }

  /** 
   * Creates a window with given title, playground width, height and background color.
   * The playground size is width x height and determines the
   * range of turtle coordinates: -width/2 .. width/2, -height/2..height/2.
   * with number of pixels: (width+1) x (height+1)
   * If turtlegraphics.properties is found, all entries are ignored.
   * with and height determines the turtle coordinate system
   */
  public TurtleFrame(String title, int width, int height, Color bkColor)
  {
    Dimension size = new Dimension(width, height);
    init(title, nullMenuBar, -1, -1, new Playground(size, bkColor));
  }

  /** 
   * Creates a window with given title, JMenuBar, playground width and height.
   * The playground size is width x height and determines the
   * range of turtle coordinates: -width/2 .. width/2, -height/2..height/2.
   * with number of pixels: (width+1) x (height+1)
   * If turtlegraphics.properties is found, all entries are ignored.
   */
  public TurtleFrame(String title, JMenuBar menuBar, int width, int height)
  {
    Dimension size = new Dimension(width, height);
    init(title, menuBar, -1, -1, new Playground(size));
  }

  /** 
   * Creates a window with given title, JMenuBar, playground width, height 
   * and background color.
   * The playground size is width x height and determines the
   * range of turtle coordinates: -width/2 .. width/2, -height/2..height/2.
   * with number of pixels: (width+1) x (height+1)
   * If turtlegraphics.properties is found, all entries are ignored.
   */
  public TurtleFrame(String title, JMenuBar menuBar, int width, int height,
    Color bkColor)
  {
    Dimension size = new Dimension(width, height);
    init(title, menuBar, -1, -1, new Playground(size, bkColor));
  }

  /** 
   * Creates a window with given r, g and title.
   * If turtlegraphics.properties is found, all entries are ignored.
   */
  public TurtleFrame(int ulx, int uly, String title)
  {
    init(title, nullMenuBar, ulx, uly, new Playground());
  }

  /** 
   * Creates a window with given r, g, title, playground width and height.
   * The playground size is width x height and determines the
   * range of turtle coordinates: -width/2 .. width/2, -height/2..height/2.
   * with number of pixels: (width+1) x (height+1)
   * If turtlegraphics.properties is found, all entries are ignored.
   */
  public TurtleFrame(int ulx, int uly, String title, int width, int height)
  {
    Dimension size = new Dimension(width, height);
    init(title, nullMenuBar, ulx, uly, new Playground(size));
  }

  /** 
   * Creates a window with given r, g, title, playground width, height 
   * and background color.
   * The playground size is width x height and determines the
   * range of turtle coordinates: -width/2 .. width/2, -height/2..height/2.
   * with number of pixels: (width+1) x (height+1)
   * If turtlegraphics.properties is found, all entries are ignored.
   */
  public TurtleFrame(int ulx, int uly, String title, int width, int height,
    Color bkColor)
  {
    Dimension size = new Dimension(width, height);
    init(title, nullMenuBar, ulx, uly, new Playground(size, bkColor));
  }

  /** 
   * Creates a window with given r, g, title, JMenuBar, playground width, 
   * height and background color.
   * The playground size is width x height and determines the
   * range of turtle coordinates: -width/2 .. width/2, -height/2..height/2.
   * with number of pixels: (width+1) x (height+1)
   * If turtlegraphics.properties is found, all entries are ignored.
   */
  public TurtleFrame(int ulx, int uly, String title, JMenuBar menuBar,
    int width, int height, Color bkColor)
  {
    Dimension size = new Dimension(width, height);
    init(title, menuBar, ulx, uly, new Playground(size, bkColor));
  }

  /** 
   * Creates a window with given mode.
   * Used for an applet with the standalone window (mode: Turtle.APPLETFRAME).
   * (Instance count for multiple windows is disabled.)
   * If turtlegraphics.properties is found, all entries are ignored.
   */
  public TurtleFrame(int mode)
  {
    this.mode = mode;
    init(DEFAULT_TITLE, nullMenuBar, -1, -1, new Playground());
  }

  protected TurtleFrame(int mode, MyProperties props)
  {
    this(nullMenuBar, props);
    this.mode = mode;
  }

  /** 
   * Creates a window with given mode, title, width, height and background color.
   * Used for an applet with a standalone window (mode: Turtle.APPLETFRAME).
   * (Instance count for multiple windows is disabled.)
   */
  public TurtleFrame(int mode, String title, int width, int height, Color bkColor)
  {
    this.mode = mode;
    Dimension size = new Dimension(width, height);
    init(title, nullMenuBar, -1, -1, new Playground(size, bkColor));
  }

  /** 
   * Creates a window with given mode, r, g, title, JMenuBar, 
   * width, height and background color.
   */
  public TurtleFrame(int mode, int ulx, int uly, String title,
    JMenuBar menuBar, int width, int height, Color bkColor)
  {
    this.mode = mode;
    Dimension size = new Dimension(width, height);
    init(title, menuBar, ulx, uly, new Playground(size, bkColor));
  }

  private void init(final String title, final JMenuBar menuBar, final int ulx,
    final int uly, final Playground playground)
  {
    myLastFrame = this;
    isDisposed = false;
    if (EventQueue.isDispatchThread())
    {
      internalInit(title, menuBar, ulx, uly, playground);
    }
    else
    {
      try
      {
        EventQueue.invokeAndWait(new Runnable()
        {
          public void run()
          {
            internalInit(title, menuBar, ulx, uly, playground);
          }
        });
      }
      catch (Exception ex)
      {
      }
    }
  }

  private void internalInit(String title, JMenuBar menuBar, int ulx, int uly,
    final Playground playground)
  {
    this.playground = playground;

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

    playground.setFocusable(true);
    playground.addKeyListener(new MyKeyAdapter());

    addComponentListener(new ComponentAdapter()
    {
      public void componentMoved(ComponentEvent evt)
      {
        if (isVisible())
        {
          _screenLocation = getLocationOnScreen();
          _lastScreenLocation = new Point(_screenLocation);
          if (statusBar != null)
          {
            statusBar.getDialog().setLocation(getLocation().x,
              getLocation().y + getHeight());
          }
        }
      }
    });

    addFocusListener(this);

    String s;
    if (instanceCount > 0)
    {
      s = title + "   # " + (instanceCount + 1);
      if (instanceCount == 1)
      {
        firstFrame.setTitle(title + "   # 1");
      }
    }
    else
      s = title;
    setTitle(s);
    if (ulx != -1)
    {
      setLocation(ulx, uly);
      _lastScreenLocation = new Point(ulx, uly);
    }
    else
      setLocation();
    
    if (mode == Turtle.STANDARDFRAME)
      setDefaultCloseOperation(EXIT_ON_CLOSE);
    if (mode == Turtle.DISPOSE_ON_CLOSE)
      setDefaultCloseOperation(DISPOSE_ON_CLOSE);
    if (mode == Turtle.RELEASE_ON_CLOSE)
      setDefaultCloseOperation(DISPOSE_ON_CLOSE);
    if (menuBar != nullMenuBar)
      this.setJMenuBar(menuBar);

    if (instanceCount == 0)
    {
      leftBorderSize = this.getInsets().left;
      firstFrame = this;
    }
    if (mode == Turtle.STANDARDFRAME)
      instanceCount++;

    Dimension dim = playground.getPreferredSize();
    JPanel contentPane = (JPanel)getContentPane();
    contentPane.add(playground);
    setResizable(false);
    pack();
    Insets insets = getInsets();
    setSize(dim.width + insets.left + insets.right,
      dim.height + insets.top + insets.bottom);
    setVisible(true);
  }

  /**
   * Returns the last frame of all initialized frames.
   * @return a reference to the last TurtleFrame
   */
  public static TurtleFrame getLastFrame()
  {
    return myLastFrame;
  }

  protected void processWindowEvent(WindowEvent e)
  {
    //   System.out.println(e.toString());

    if (e.getID() == WindowEvent.WINDOW_CLOSING)
    {
      if (exitListener != null)
      {
        exitListener.notifyExit();
        return;
      }

      if (mode == Turtle.ASK_ON_CLOSE)
      {
        if (JOptionPane.showConfirmDialog(this,
          "Terminating program. Are you sure?",
          "Please confirm",
          JOptionPane.YES_NO_OPTION) == JOptionPane.NO_OPTION)
          return;
        else
          System.exit(0);
      }
      if (mode == Turtle.CLEAR_ON_CLOSE)
      {
        playground.clear();
        return;
      }
      if (mode == Turtle.DISPOSE_ON_CLOSE || mode == Turtle.RELEASE_ON_CLOSE)
      {
        dispose();
        return;
      }
      if (mode == Turtle.NOTHING_ON_CLOSE)
        return;
    }
    super.processWindowEvent(e);
  }

  protected void setLocation()
  {
    Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
    Dimension appSize = getPreferredSize();
    int xBorder = screenSize.width / 10 + (instanceCount / 2) * 10;
    int yBorder = screenSize.height / 10 + (instanceCount / 2) * 10;
    int xStep = (8 * screenSize.width / 10 - appSize.width) / 9;
    int yStep = (8 * screenSize.height / 10 - appSize.width) / 9;
    int locX = xBorder + (instanceCount % 5) * xStep;
    int locY = yBorder + (instanceCount % 5) * yStep;
    setLocation(locX, locY);
    _lastScreenLocation = new Point(locX, locY);
  }

  /** 
   * Returns the playground of this TurtleFrame.
   */
  public Playground getPlayground()
  {
    return playground;
  }

  /** 
   * For standalone applets we must put the applet window in front of the browser.
   */
  public void focusLost(FocusEvent evt)
  {
    if (mode == Turtle.APPLETFRAME && isAppletFirst)
    {
      toFront();
      isAppletFirst = false;
    }
  }

  /** 
   * Empty implementation of a FocusListener method.
   *
   */
  public void focusGained(FocusEvent evt)
  {
  }

  /**
   * Adds the status window attached at the bottom of the game grid window.
   * The dialog has no decoration, the same width as the turtle frame and
   * the given height in pixels.
   */
  public void addStatusBar(int height)
  {
    statusBar = new StatusBar(this,
      new Point(getLocation().x, getLocation().y + getHeight()),
      new Dimension(getWidth(), height + 1));
  }

  /**
   * Replaces the text in the status bar by the given text using the current
   * JOptionPane font and color. The text is left-justified,
   * vertical-centered and may be multi-line.
   * If there is no status bar, nothing happens.
   */
  public void setStatusText(String text)
  {
    check();
    if (statusBar != null)
      statusBar.setText(text);
  }

  /**
   * Replaces the text in the status bar by the given text using the given font
   * and text color. The text is left-justified, vertical-centered and may be multi-line.
   * If there is no status bar, nothing happens.
   */
  public void setStatusText(String text, Font font, Color color)
  {
    check();
    if (statusBar != null)
    {
      Font defaultFont = (Font)UIManager.get("messageFont");
      Color defaultColor = (Color)UIManager.get("messageForground");
      UIManager.put("OptionPane.messageFont", font);
      UIManager.put("OptionPane.messageForeground", color);

      statusBar.setText(text);

      UIManager.put("OptionPane.messageFont", defaultFont);
      UIManager.put("OptionPane.messageForeground", defaultColor);
    }
  }

  /**
   * Shows or hides the status bar. If there is no status bar,
   * nothing happens.
   */
  public void showStatusBar(boolean show)
  {
    check();
    if (statusBar == null)
      return;
    statusBar.setVisible(show);
  }

  /** 
   * Adds the specified MouseListener to receive mouse events.
   * The mouse listener is not affected when a mouse double-click listener
   * is registered. When a double-click happens, the click event is reported twice,
   * but you may use the notifyClick() callback of the MouseDoubleClickListener
   * to get only one click event. See TurtleFrame.setDoubleClickDelay() 
   * to get information about how notifyClick() and notifyDoubleClick() work together.
 
   * @param listener the registered MouseListener
   */
  public void addMouseListener(MouseListener listener)
  {
    check();
    playground.addMouseListener(listener);
  }

  /** 
   * Adds the specified MouseMotionListener to receive mouse events.
   * @param listener the registered MouseMotionListener
   */
  public void addMouseMotionListener(MouseMotionListener listener)
  {
    check();
    playground.addMouseMotionListener(listener);
  }

  /** 
   * Adds the specified MouseDoubleClickListener to receive mouse events
   * and calls setDoubleClickDelay(-1).
   * See TurtleFrame.setDoubleClickDelay() to get information about 
   * how notifyClick() and notifyDoubleClick() work together.
   * @param listener the registered MouseDoubleClickListener
   */
  public void addMouseDoubleClickListener(MouseDoubleClickListener listener)
  {
    check();
    setDoubleClickDelay(-1);
    this.mouseDoubleClickListener = listener;
    addMouseListener(new MyMouseAdapter());
  }

  /** 
   * Adds the specified KeyListener to receive key events.
   * @param listener the registered KeyListener
   */
  public void addKeyListener(KeyListener listener)
  {
    check();
    playground.addKeyListener(listener);
  }

  /** 
   * Adds the specified MouseWheelListener to receive mouse events.
   * @param listener the registered MouseWheelListener
   */
  public void addMouseWheelListener(MouseWheelListener listener)
  {
    check();
    playground.addMouseWheelListener(listener);
  }

  /**
   * Adds the specified ExitListener to get a notification when the close button is clicked.
   * After registering other closing modes are disabled.
   * To terminate the application, System.exit() may be called.
   */
  public void addExitListener(ExitListener exitListener)
  {
    this.exitListener = exitListener;
  }

  /**
   * Release all used system resources. 
   * After calling, don't use the TurtleFrame instance anymore.
   */
  public void dispose()
  {
    setVisible(false);
    playground.traceG2D.dispose();
    playground.turtleG2D.dispose();
    if (waitThread != null)
      waitThread.interrupt();  // Take out if hangs in getKeyWait(), getKeyCodeWait()
    isDisposed = true;
  }

  /**
   * Checks if the turtle frame was disposed or released.
   */
  public static boolean isDisposed()
  {
    return isDisposed;
  }

  /**
   * Registers a MouseHitListener to report mouse press events
   * each in a separate thread.
   */
  public void addMouseHitListener(MouseHitListener listener)
  {
    check();
    if (mouseHitListeners.isEmpty())
      playground.addMouseListener(new MyMouseHitAdapter());
    mouseHitListeners.add(listener);
  }

  /**
   * Registers a MouseHitXListener to report mouse press events
   * each in a separate thread.
   */
  public void addMouseHitXListener(MouseHitXListener listener)
  {
    check();
    playground.addMouseListener(new MyMouseHitXAdapter());
    mouseHitXListener = listener;
  }

  private void delay(int time)
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
   * Returns true, if a key was typed
   * since the last call to getChar() or getCharWait().
   * The one-character buffer is not changed.
   * Put the current thread to sleep for 1 ms, to improve
   * response time when used in a loop.
   */
  public boolean kbhit()
  {
    check();
    delay(1);
    return gotKey;
  }

  /**
   * Returns the unicode character associated with last key typed.
   * The one-character buffer is cleared.
   * Return KeyEvent.CHAR_UNDEFINED if the buffer is empty.
   */
  public char getKey()
  {
    check();
    synchronized (monitor)
    {
      if (gotKey)
      {
        gotKey = false;
        return keyChar;
      }
      else
        return KeyEvent.CHAR_UNDEFINED;
    }
  }

  /**
   * Returns the key code associated with last key pressed.
   * The one-character buffer is cleared.
   * Return KeyEvent.VK_UNDEFINED if the buffer is empty.
   */
  public int getKeyCode()
  {
    check();
    synchronized (monitor)
    {
      if (gotKey)
      {
        gotKey = false;
        return keyCode;
      }
      else
        return KeyEvent.VK_UNDEFINED;
    }
  }

  /**
   * Waits until a key is typed and
   * returns the unicode character associated with it.
   * Unblocked and returning KeyEvent.CHAR_UNDEFINED by calling dispose().
   */
  public char getKeyWait()
  {
    check();
    waitThread = Thread.currentThread();
    mustNotify = true;
    synchronized (this)
    {
      try
      {
        wait();
      }
      catch (InterruptedException ex)
      {
        mustNotify = false;
        return KeyEvent.CHAR_UNDEFINED;
      }
    }
    mustNotify = false;
    return getKey();
  }

  /**
   * Waits until a key is typed and
   * returns the key code associated with last key pressed.
   * Unblocked and returning KeyEvent.VK_UNDEFINED by calling dispose().
   */
  public int getKeyCodeWait()
  {
    check();
    waitThread = Thread.currentThread();
    mustNotify = true;
    synchronized (this)
    {
      try
      {
        wait();
      }
      catch (InterruptedException ex)
      {
        mustNotify = false;
        return KeyEvent.VK_UNDEFINED;
      }
    }
    mustNotify = false;
    return getKeyCode();
  }

  /**
   *  Returns the modifiers associated with last key pressed.
   */
  public int getModifiers()
  {
    check();
    return modifiers;
  }

  /**
   *  Returns the modifiers associated with last key pressed.
   */
  public String getModifiersText()
  {
    check();
    return modifiersText;
  }

  /**
   * Sets the mouse cursor image.
   * @param cursorType one of the constants in class java.awt.Cursor 
   */
  public void setCursor(int cursorType)
  {
    check();
    playground.setCursor(new Cursor(cursorType));
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
    Toolkit toolkit = Toolkit.getDefaultToolkit();
//    Image image = toolkit.getImage(cursorImage); 
    Image image = Turtle.getImage(cursorImage);
    if (image == null)
      return null;
    Dimension dim = toolkit.getBestCursorSize(0, 0);
    if (hotSpot == null)
      hotSpot = new Point(dim.width / 2, dim.height / 2);
    Cursor cursor = toolkit.createCustomCursor(image, hotSpot, "Custom");
    playground.setCursor(cursor);
    return dim;
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
    return setCustomCursor(cursorImage, null);
  }

  private void doNotify()
  {
    synchronized (this)
    {
      notify();
    }
  }

  /** 
   * Determines what happens when the title bar close button is hit. Values:<br>
   * Turtle.TERMINATE_ON_CLOSE -> Terminating and shutting down JRE 
   * by System.exit(0)<br>
   * Turtle.CLEAR_ON_CLOSE -> Remove all turtle images and traces, 
   * but turtles remain where they are<br>
   * Turtle.ASK_ON_CLOSE -> Show confirmation dialog 
   * asking for termination<br>
   * Turtle.DISPOSE_ON_CLOSE -> Closes the graphics window, but 
   * does not shutdown JRE<br>
   * Turtle.RELEASE_ON_CLOSE -> Like DISPOSE_ON_CLOSE, but throws runtime 
   * exception when turtle graphics methods are called<br>
   * Turtle.NOTHING_ON_CLOSE -> Do nothing<br>
   * Default value: Turtle.TERMINATE_ON_CLOSE
   */
  public void setClosingMode(int mode)
  {
    this.mode = mode;
  }

  /**
   * Sets the time delay the system uses to distinct click and double-click
   * mouse events. If set to 0, this delay is ignored and both events are
   * generated. If delay is less than zero, the value from the desktop property
   * "awt.multiClickInterval" is used (normally 500 ms). (If the desktop 
   * property "awt.multiClickInterval" is not available (e.g. under some 
   * versions of Mac OS), the delay is then set to 500.)<br> 
   *
   * Most systems ignores delays less than 200 ms (treated as 0).<br>
   *
   * Keep in mind that when you perform a single click, the click event is only reported after the
   * double-click time. If the double-click is not registered by the mouse
   * event mask, the delay is ignored.<br>
   *
   * @param delay the double-click delay time in milliseconds used when a
   * double-click event is registered
   */
  public void setDoubleClickDelay(int delay)
  {
    if (delay == 0)
      doubleClickTime = 0;
    else
    {
      if (delay < 0)
      {
        Toolkit toolkit = Toolkit.getDefaultToolkit();
        Object obj = toolkit.getDesktopProperty("awt.multiClickInterval");
        if (obj == null)
          doubleClickTime = 500;
        else
          doubleClickTime = (Integer)obj;
      }
      else
        doubleClickTime = delay;

      doubleClickTimer = new javax.swing.Timer(doubleClickTime,
        new MyTimerActionListener());
    }
  }

  /**
   * Draws the given image into the background (trace buffer). The image
   * is loaded with Turtle.getImage(). If the image can't be loaded,
   * nothing happens.
   * @param imagePath the file name or url
   */
  public void drawBkImage(String filename)
  {
    check();
    getPlayground().setBkImage(filename);
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
   Returns the number of the last mouse button from a registered 
   mouseHit or mouseHitX event.
   @return the id number of the last button hit (0: no button hit until now, 1: left button,
   2: middle button, 3: right button)
   */
  public int getMouseHitButton()
  {
    return mouseHitButton;
  }
  
   /**
   * Sets the location of the window on the screen and returns the old location.
   */
  public Point setScreenLocation(Point location)
  {
    Point oldLocation = new Point(_screenLocation);
    setLocation(location);
    _screenLocation = location;
    _lastScreenLocation = new Point(location);
    return oldLocation;
  }  

  /**
   * Returns the current location of the window on the screen.
   */
  public Point getScreenLocation()
  {
    return new Point(_screenLocation);
  }  

  /**
   * Returns the location of the last window 
   * (null if not yet visible).
   */
  public static Point getLastScreenLocation()
  {
    if (_lastScreenLocation == null)
      return null;
    return new Point(_lastScreenLocation);
  }  

  private void check()
  {
    if (mode == Turtle.RELEASE_ON_CLOSE && isDisposed)
      throw new RuntimeException("Java frame disposed");
  }
}
