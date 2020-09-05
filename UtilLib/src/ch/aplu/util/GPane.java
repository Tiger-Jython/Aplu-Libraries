// GPane.java

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
package ch.aplu.util;

import java.awt.*;
import java.awt.event.*;
import java.awt.geom.*;
import java.awt.image.*;
import java.awt.print.*;
import javax.swing.*;
import java.util.ArrayList;

/**
 * Simple graphics window similar to GPanel to be used as component in a
 * top-level window (JFrame, JDialog, Browswer window, etc.) using a
 * coordinate system with x-axis from left to right, y-axis from bottom to top
 * (called window coordinates, default range 0..1, 0..1).
 * The drawing methods perform all drawings in an offscreen buffer
 * and automatically repaint it on the screen, so the graphics is shown step-by-step.
 * <br><br>
 *
 * A current graph position is used, that remembers the end position of the last
 * drawing process and where new drawings start.<br><br>
 *
 * Because its offscreen buffer will be created at the moment when the GPane
 * is displayed, all graphics drawing operations will be blocked until then.
 * You should check with <code>isReady()</code>) before invoking drawing methods.
 * (A GPanelNotReadyException is thrown after a blocking timeout of 30 sec)<br><br>
 *
 * GPane is derived from JPanel and can only be used by adding an instance to another
 * top-level window. Unlike GPanel where the constructors and all public methods run in the
 * Event Dispatch Thread, with GPane it is your responsiblity to invoke
 * Swing methods in the EDT.
 * @see ch.aplu.util.GPanel
 */
public class GPane extends JPanel implements FocusListener, Printable
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

// ---------------------------- Inner class MyKeyAdapter ---------------
  private class MyKeyAdapter implements KeyListener
  {
    public void keyPressed(KeyEvent evt)
    {
      synchronized (monitor)
      {
        if (_keyListener != null)
          _keyListener.keyPressed(evt);
        _keyCode = evt.getKeyCode();
        _keyChar = evt.getKeyChar();
        _modifiers = evt.getModifiers();
        _modifiersText = KeyEvent.getKeyModifiersText(_modifiers);
        _gotKey = true;
        if (_mustNotify)
          doNotify();
      }
    }

    public void keyReleased(KeyEvent evt)
    {
      if (_keyListener != null)
        _keyListener.keyReleased(evt);
    }

    public void keyTyped(KeyEvent evt)
    {
      if (_keyListener != null)
        _keyListener.keyTyped(evt);
    }
  }
// ---------------------------- End of inner class ---------------------
  /**
   * Property for bean support.
   */
  public Color backgroundColor = Color.white;
  /**
   * Property for bean support.
   */
  public Color penColor = Color.black;
  /**
   * Property for bean support.
   */
  public double xmin = 0;
  /**
   * Property for bean support.
   */
  public double xmax = 1;
  /**
   * Property for bean support.
   */
  public double ymin = 0;
  /**
   * Property for bean support.
   */
  public double ymax = 1;
  /**
   * Property for bean support.
   */
  private final static boolean propertyVerbose = false;
  public boolean enableFocus = false;
  private int _wWidth;
  private int _wHeight;
  // will produce usable coordinates 0..500 (0 and 500 inclusive = 501 pixels)
  // must increase 2 pixels because 0,0 is on components border
  private static int _previousWidth;  // Width of previous window (for multiple GPanels)
  private static int _previousUlx;  // ulx of previous window (for multiple GPanels)
  private static int _previousUly;  // uly of previous window (for multiple GPanels)
  private GWindow _wnd = null;
  private String _title = "GPanel";
  private static GPane _previousPanel;
  private static int _instanceCount = 0;
  private static ArrayList<GPane> _instances = new ArrayList();
  private GPrintable _userPanel;
  private double _scale;
  private boolean _isPrinting = false;
  private boolean _printScreen = false;
  private JDialog _msgDialog;
  // Modes
  private int _panelMode = GPanel.STANDARD;
  private boolean _isFullscreen = false;
  private volatile boolean _isReady = false;
  private boolean _isFirst = true;
  private Thread waitThread = null;
  private boolean _gotKey = false;
  private boolean _mustNotify = false;
  private char _keyChar;
  private int _keyCode;
  private int _modifiers;
  private String _modifiersText;
  private Object monitor = new Object();
  private String _panelTitle = null;
  private Point _panelPos = null;
  private Dimension _panelSize = null;
  protected GPanel.ClosingMode _closingMode = null;
  private double[] _panelRange = null;
  private boolean _isUndecorated = false;
  private boolean _isSizeRequested;
  private boolean _isTitleRequested;
  private KeyListener _keyListener = null;
  private MouseDoubleClickListener mouseDoubleClickListener = null;
  private int doubleClickTime = 0;
  private javax.swing.Timer doubleClickTimer;
  private MouseEvent lastMouseEvent;

  /**
   * Create a GPane to be used as embedded graphics component with default size 100x100 pixels.
   * Parameterless bean constructor.
   */
  public GPane()
  {
    this(new Size(100, 100), true);
  }

  /**
   * Create a GPane to be used as embedded graphics component with given size.
   */
  public GPane(Size size)
  {
    this(size, true);
  }

  /**
   * Create a GPane with given size. If embedded = true, an embedded graphics
   * component is created; if false, the graphics window has its own frame (like
   * a GPanel).
   */
  public GPane(Size size, boolean embedded)
  {
    _wWidth = size.getWidth() + GPanel.gap;  // add 2: see comment on top of file
    _wHeight = size.getHeight() + GPanel.gap;
    setFocusable(enableFocus);
    _panelMode = GPanel.EMBEDDED;
    init(_title, null, 0, 1, 0, 1, false);
  }

  protected GPane(int panelMode, Size size, String title, JMenuBar menuBar,
    double xmin, double xmax, double ymin, double ymax,
    boolean visible, boolean isSizeRequested, boolean isTitleRequested)
  {
    _panelMode = panelMode;

    _isSizeRequested = isSizeRequested;
    _isTitleRequested = isTitleRequested;

    if (size instanceof Fullscreen)
      _isFullscreen = true;
    else
    {
      _wWidth = size.getWidth();
      _wHeight = size.getHeight();
    }

    if (_panelMode == GPanel.STANDARD)
      init(title, menuBar, xmin, xmax, ymin, ymax, visible);
    if (_panelMode == GPanel.APPLETFRAME)
      init(_title, null, 0, 1, 0, 1, true);
    if (_panelMode == GPanel.NOTITLEBAR)
      init(_title, null, 0, 1, 0, 1, false);
  }

  /**
   * Set window coordinates.
   * left_x, right_x, bottom_y, top_y
   */
  public void window(double xmin, double xmax, double ymin, double ymax)
  {
    _wnd.setWindow(xmin, xmax, ymin, ymax);
  }

  protected void init(String title, JMenuBar menuBar, double xmin, double xmax,
    double ymin, double ymax, boolean visible)
  {
    MyProperties props = new MyProperties(propertyVerbose);
    if (props.search())
    {
      if (!_isTitleRequested)
      {
        _panelTitle = props.getStringValue("GPanelTitle");
        if (_panelTitle != null)
          _panelTitle = _panelTitle.trim();
      }

      String decoration = props.getStringValue("GPanelDecoration");
      if (decoration != null)
      {
        decoration = decoration.trim();
        _isUndecorated = decoration.toLowerCase().equals("no");
      }

      _closingMode = getClosingMode(props);

      int[] values = props.getIntArray("GPanelPosition", 2);
      if (values != null && values[0] >= 0 && values[1] >= 0)
        _panelPos = new Point(values[0], values[1]);

      values = props.getIntArray("GPanelSize", 2);
      if (values != null && values[0] > 0 && values[1] > 0)
        _panelSize = new Dimension(values[0], values[1]);

      _panelRange = props.getDoubleArray("GPanelRange", 4);
    }

    if (GPanelOptions.getPanelTitle() != null)
      _panelTitle = GPanelOptions.getPanelTitle();
    if (GPanelOptions.getPanelPosition() != null)
      _panelPos = GPanelOptions.getPanelPosition();
    if (GPanelOptions.getPanelSize() != null)
      _panelSize = GPanelOptions.getPanelSize();
    if (GPanelOptions.getPanelRange() != null)
      _panelRange = GPanelOptions.getPanelRange();

    if (_panelSize != null && !_isSizeRequested)
    {
      _wWidth = _panelSize.width + GPanel.gap + 1;
      _wHeight = _panelSize.height + GPanel.gap + 1;
    }

    if (_panelRange != null)
    {
      xmin = _panelRange[0];
      xmax = _panelRange[1];
      ymin = _panelRange[2];
      ymax = _panelRange[3];
    }

    // Calculate position if more than one window is shown
    int screenWidth = GWindow.getScreenWidth();
    int screenHeight = GWindow.getScreenHeight();
    int xBorder = screenWidth / 10;
    int yBorder = screenHeight / 10;
    int xStep = (screenWidth - 2 * xBorder - _wWidth) / 10;
    int yStep = (screenHeight - 2 * yBorder - _wHeight) / 10;

    int ulx;
    int uly;
    if (_panelPos != null)
    {
      ulx = _panelPos.x;
      uly = _panelPos.y;
    }
    else
    {
      if (_instanceCount % 2 == 0)
      {
        ulx = xBorder + ((_instanceCount / 2) % 10) * xStep;
        uly = yBorder + ((_instanceCount / 2) % 10) * yStep;
      }
      else
      {
        ulx = _previousUlx + _previousWidth + 2 * _previousPanel._wnd.getInsets().left;
        uly = _previousUly;
      }
    }

    // Create GWindow
    if (_panelMode == GPanel.NOTITLEBAR)
    {
      if (_isFullscreen)
      {
        _wnd = new GWindow(this, null, new Position(0, 0),
          new Fullscreen(), true, _closingMode);
        _wWidth = _wnd.getWidth();
        _wHeight = _wnd.getHeight();
      }
      else
        _wnd = new GWindow(this, null, new Position(ulx, uly),
          new Size(_wWidth, _wHeight), true, _closingMode);
    }
    else // Normal window
    {
      if (_panelTitle != null)
        title = _panelTitle;
      if (_isFullscreen)
      {
        _wnd = new GWindow(this, title, new Position(0, 0),
          new Fullscreen(), _isUndecorated, _closingMode);
        _wWidth = _wnd.getWidth();
        _wHeight = _wnd.getHeight();
      }
      else
      {
        _wnd
          = new GWindow(this, title, new Position(ulx, uly),
            new Size(_wWidth, _wHeight), _isUndecorated, _closingMode);
      }
    }

    _wnd.setAdjust(false);  // Do not adjust window coordinates when zooming
    _wnd.shrink();
    _wnd.setWindow(xmin, xmax, ymin, ymax);
    if (menuBar != null)
      _wnd.setJMenuBar(menuBar);

    _wnd.setMode(_panelMode);

    setFocusable(true);  // Needed to get the keyevents
    addFocusListener(this);
    addKeyListener(new MyKeyAdapter());
    _wnd.showComponent(this, visible);
    requestFocus();

    _instanceCount++;
    _instances.add(this);

    _previousPanel = this;
    _previousWidth = _wWidth;
    _previousUlx = ulx;
    _previousUly = uly;
  }

  /**
   * Select if GPane's window is visible or not.
   * (You may draw into an invisible GPane and show it
   * afterwards.)
   */
  public void visible(boolean isVisible)
  {
    _wnd.setVisible(isVisible);
  }

  /**
   * Return a reference to the GWindow used by the GPane.
   * Can be used to access the surrounding JFrame
   * because GWindow is derived from JFrame.
   */
  public GWindow getWindow()
  {
    return _wnd;
  }

  /**
   * Override JPanel's repaint() to inhibit explicit repainting
   * while printing.
   */
  public void repaint()
  {
    if (!_isPrinting)
      super.repaint();
  }

  /**
   * Override JPanel's repaint(int x, int y, int width, int height)
   * to inhibit explicit repainting while printing.
   */
  public void repaint(int x, int y, int width, int height)
  {
    if (!_isPrinting)
      super.repaint(x, y, width, height);
  }

  /**
   * Test if GPane is fully initialized.
   * Only used for GPane.EMBEDDED.<br>
   * (If GPane is embedded in an other component, any graphics
   * drawing operation must be postponed until the parent component is shown
   * because the GPane's size is only known at this time.)<br>
   * Because yield() is called, it can be used in a narrow loop.
   *
   */
  public boolean isReady()
  {
    Thread.currentThread().yield();
    return _isReady;
  }

  private boolean waitForReady()
  // Return true to say: we are already disposed
  {
    if (_wnd._isDisposed)
      return true;
    if (_panelMode == GPanel.EMBEDDED)
    {
      int count = 30;  // Timeout in sec
      while (count > 0 && !_isReady)
      {
        Console.delay(1000);
        count--;
      }
      if (count == 0)
        throw new GPanelNotReadyException(
          "Timeout when waiting for embedded GPane to be shown");
    }
    return false;
  }

  /**
   * Print the graphics context to an attached printer with
   * the given magnification scale factor.
   * scale = 1 will print on standard A4 format paper.
   *
   * The given gp must implement the GPrintable interface,
   * e.g. the single method void draw(), where all the
   * drawing into the GPane must occur.
   *
   * A standard printer dialog is shown before printing is
   * started.<br>
   *
   * Return false, if printing is canceled in this dialog,
   * return true, when print data is sent to the printer spooler.
   *
   *(On some platforms the Java Virtual Machine crashes when
   * changing the printer in this dialog. Use the default printer)
   * <br>
   * Example:<br>
   * <code><br>
   import ch.aplu.util.*;<br>
   <br>
   public class PrintEx extends GPane implements GPrintable<br>
   {<br>
   &nbsp;&nbsp;public PrintEx()<br>
   &nbsp;&nbsp;{<br>
   &nbsp;&nbsp;&nbsp;&nbsp;draw();       // Draw on screen<br>
   &nbsp;&nbsp;&nbsp;&nbsp;print(this);  // Draw on printer<br>
   &nbsp;&nbsp;}<br>
   <br>
   &nbsp;&nbsp;public void draw()<br>
   &nbsp;&nbsp;{<br>
   &nbsp;&nbsp;&nbsp;&nbsp;move(0, 0);<br>
   &nbsp;&nbsp;&nbsp;&nbsp;draw(1, 0);<br>
   &nbsp;&nbsp;&nbsp;&nbsp;draw(1, 1);<br>
   &nbsp;&nbsp;&nbsp;&nbsp;draw(0, 1);<br>
   &nbsp;&nbsp;&nbsp;&nbsp;draw(0, 0);<br>
   <br>
   &nbsp;&nbsp;&nbsp;&nbsp;line(0, 1, 1, 0);<br>
   &nbsp;&nbsp;&nbsp;&nbsp;line(0, 0, 1, 1);<br>
   &nbsp;&nbsp;}<br>
   <br>
   &nbsp;&nbsp;public static void main(String[] args)<br>
   &nbsp;&nbsp;{<br>
   &nbsp;&nbsp;&nbsp;&nbsp;new PrintEx();<br>
   &nbsp;&nbsp;}<br>
   }<br>
   */
  public boolean print(GPrintable gp, double scale)
  {
    return internalPrint(gp, scale);
  }

  /**
   * Same as print(GPrintable gp, double scale)
   * with scale = 1.
   */
  public boolean print(GPrintable gp)
  {
    return print(gp, 1);
  }

  /**
   * Print the current screen view to an attached printer
   * with the given magnification scale factor.
   * A standard printer dialog is shown before printing is
   * started.<br>
   * Return false, if printing is canceled in this dialog,
   * return true, when print data is sent to the printer spooler.
   *
   */
  public boolean printScreen(double scale)
  {
    return internalPrint(null, scale);
  }

  /**
   * Same printScreen(double scale) with scale = 1.
   */
  public boolean printScreen()
  {
    return printScreen(1);
  }

  private boolean internalPrint(GPrintable gp, double scale)
  {
    MessageDialog msg = new MessageDialog(this, "Printing in progress. Please wait...");

    _scale = scale;
    PrinterJob pj = PrinterJob.getPrinterJob();
    pj.setPrintable(this);
    _userPanel = gp;
    if (gp == null)
      _printScreen = true;
    else
      _printScreen = false;
    if (pj.printDialog())
    {
      try
      {
        msg.show();
        pj.print();
        msg.close();
      }
      catch (PrinterException ex)
      {
        System.out.println("Exception in GPane.internalPrint()\n" + ex);
      }
      return true;
    }
    else
      return false;
  }

  /**
   * Set the background color. All drawing are erased. Ignores the alpha channel.
   * Return the previous color.
   */
  public Color bgColor(Color color)
  {
    Color c = _wnd.setBgColor(
      new Color(color.getRed(), color.getGreen(), color.getBlue()));
    if (_wnd.getRepaint())
      repaint();
    return c;
  }

  /**
   *  Return the current background color.
   */
  public Color getBgColor()
  {
    return _wnd.getBgColor();
  }

  /**
   * Set the title in the window's title bar.
   */
  public void title(String title)
  {
    _wnd.setTitle(title);
    if (_wnd.getRepaint())
      repaint();
  }

  /** 
   * Adds the specified MouseDoubleClickListener get notifications for
   * mouse double-click events and calls setDoubleClickDelay(-1).
   * See setDoubleClickDelay() to get information about 
   * how notifyClick() and notifyDoubleClick() work together.
   * @param listener the registered MouseDoubleClickListener
   */
  public void addMouseDoubleClickListener(MouseDoubleClickListener listener)
  {
    setDoubleClickDelay(-1);
    this.mouseDoubleClickListener = listener;
    addMouseListener(new MyMouseAdapter());
  }

  /**
   * Return user coordinate x of given window coordinate x.
   */
  public int toUserX(double windowX)
  {
    return _wnd.toUserX(windowX);
  }

  /**
   * Return user coordinate y of given window coordinate y.
   */
  public int toUserY(double windowY)
  {
    return _wnd.toUserY(windowY);
  }

  /**
   * Return reference to point in user coordinate of given point in window coordinate.
   */
  public Point toUser(Point2D.Double windowPt)
  {
    return new Point(_wnd.toUserX(windowPt.x), _wnd.toUserY(windowPt.y));
  }

  /**
   * Return reference to point in user coordinate of given window coordinates.
   */
  public Point toUser(double windowX, double windowY)
  {
    return new Point(_wnd.toUserX(windowX), _wnd.toUserY(windowY));
  }

  /**
   * Return user coordinates increment x of given window coordinates increment x.
   * Increment is always positive.
   */
  public int toUserWidth(double windowWidth)
  {
    return _wnd.toUserWidth(windowWidth);
  }

  /**
   * Return user coordinates increment y of given window coordinates increment y.
   * Increment is always positive.
   */
  public int toUserHeight(double windowHeight)
  {
    return _wnd.toUserHeight(windowHeight);
  }

  /**
   * Return window coordinate x of given user coordinate x.
   */
  public double toWindowX(int userX)
  {
    return _wnd.toWindowX(userX);
  }

  /**
   * Return window coordinate y of given user coordinate y.
   */
  public double toWindowY(int userY)
  {
    return _wnd.toWindowY(userY);
  }

  /**
   * Return reference to point in window coordinates of given point in user coordinates.
   */
  public Point2D.Double toWindow(Point userPt)
  {
    return new Point2D.Double(_wnd.toWindowX(userPt.x), _wnd.toWindowY(userPt.y));
  }

  /**
   * Return reference to point in window coordinates of given user coordinates.
   */
  public Point2D.Double toWindow(int userX, int userY)
  {
    return new Point2D.Double(_wnd.toWindowX(userX), _wnd.toWindowY(userY));
  }

  /**
   * Return window coordinates increment x of given user coordinates increment x.
   * Increment is always positive.
   */
  public double toWindowWidth(int userWidth)
  {
    return _wnd.toWindowWidth(userWidth);
  }

  /**
   * Return window coordinates increment y of given user coordinates increment y.
   * Increment is always positive.
   */
  public double toWindowHeight(int userHeight)
  {
    return _wnd.toWindowHeight(userHeight);
  }

  /**
   * Set the current line width in pixels.
   */
  public void lineWidth(int width)
  {
    if (width > 0)
      _wnd.setLineWidth(width);
  }

  /**
   * Return current line width in pixels.
   */
  public int getLineWidth()
  {
    return _wnd.getLineWidth();
  }

  /**
   * Same as setColor() (deprecated).
   */
  public Color color(Color color)
  {
    return setColor(color);
  }

  /**
   * Set the given new color and return the previous color
   */
  public Color setColor(Color color)
  {
    Color oldColor = _wnd.getColor();
    _wnd.setColor(color);
    return oldColor;
  }

  protected Color getColor()
  {
    return _wnd.getColor();
  }

  /**
   * Draws a line with given window coordinates
   * and set the graph position to the endpoint.
   */
  public void line(double x1, double y1, double x2, double y2)
  {
    if (waitForReady())
      return;
    _wnd.line(x1, y1, x2, y2);
  }

  /**
   * Draws a line with given points (in window coordinates)
   * and set the graph position to the endpoint.
   */
  public void line(Point2D.Double pt1, Point2D.Double pt2)
  {
    line(pt1.x, pt1.y, pt2.x, pt2.y);
  }

  /**
   * Draw a line from current graph position to given window coordinates and
   * set the graph position to the endpoint.
   */
  public void draw(double x, double y)
  {
    if (waitForReady())
      return;;
    _wnd.draw(x, y);
  }

  /**
   * Draw a line from current graph position to given point (in window coordinates)
   * and set the graph position to the endpoint.
   */
  public void draw(Point2D.Double pt)
  {
    draw(pt.x, pt.y);
  }

  /**
   * Set the current graph position to given window coordinates
   * (without drawing anything).
   */
  public void move(double x, double y)
  {
    _wnd.pos(x, y);
  }

  /**
   * Same as move(double x, double y).
   * ( Override java.component.move(int x, int y) )
   */
  public void move(int x, int y)
  {
    _wnd.pos((double)x, (double)y);
  }

  /**
   * Same as move(double x, double y).
   */
  public void pos(double x, double y)
  {
    _wnd.pos(x, y);
  }

  /**
   * Set the current graph position to given point (in window coordinates)
   * (without drawing anything).
   */
  public void move(Point2D.Double pt)
  {
    pos(pt);
  }

  /**
   * Same as move(Point2D.Double pt).
   */
  public void pos(Point2D.Double pt)
  {
    pos(pt.x, pt.y);
  }

  /**
   * Return window coordinate x of current graph position.
   */
  public double getPosX()
  {
    return _wnd.getPosX();
  }

  /**
   * Return window coordinate y of current graph position.
   */
  public double getPosY()
  {
    return _wnd.getPosY();
  }

  /**
   * Return reference to point of current graph position (in window coordinates).
   */
  public Point2D.Double getPos()
  {
    return new Point2D.Double(_wnd.getPosX(), _wnd.getPosY());
  }

  /**
   * Clear the graphics window (fully paint with background color)
   * (and the offscreen buffer used by the window).
   * Set the current graph position to (0, 0).<br>
   * If enableRepaint(false) only clear the offscreen buffer.
   */
  public void clear()
  {
    if (waitForReady())
      return;
    move(0, 0);
    _wnd.clearBuf();
    if (_wnd.getRepaint())
      repaint();
  }

  /**
   * Same as clear() but let the current graph position unchanged.
   */
  public void erase()
  {
    if (waitForReady())
      return;
    _wnd.clearBuf();
    if (_wnd.getRepaint())
      repaint();
  }

  /**
   * Draw a circle with center at the current graph position
   * and given radius in horizontal window coordinates.
   * The graph position is unchanged.
   */
  public void circle(double radius)
  {
    if (waitForReady())
      return;
    _wnd.drawCircle(radius, false);
  }

  /**
   * Draw a filled circle with center at the current graph position
   * and given radius in window coordinates using the current color.
   * The graph position is unchanged.
   */
  public void fillCircle(double radius)
  {
    if (waitForReady())
      return;
    _wnd.drawCircle(radius, true);
  }

  /**
   * Draw an ellipse with center at the current graph position
   * and given horizontal and vertical axes.
   * The graph position is unchanged.
   */
  public void ellipse(double a, double b)
  {
    if (waitForReady())
      return;
    _wnd.drawEllipse(a, b, false);
  }

  /**
   * Draw a filled ellipse with center at the current graph position
   * and given horizontal and vertical axes.
   * The graph position is unchanged.
   */
  public void fillEllipse(double a, double b)
  {
    if (waitForReady())
      return;
    _wnd.drawEllipse(a, b, true);
  }

  /**
   * Draw a rectangle with center at the current graph position
   * and given width and height in window coordinates.
   * The graph position is unchanged.
   */
  public void rectangle(double width, double height)
  {
    if (waitForReady())
      return;
    _wnd.drawRectangle(width, height, false);
  }

  /**
   * Draw a rectangle with given opposite corners
   * in window coordinates.
   * The graph position is unchanged.
   */
  public void rectangle(double x1, double y1, double x2, double y2)
  {
    rectangle(x1, y1, x2, y2, false);
  }

  /**
   * Draw a rectangle with given opposite corner points
   * in window coordinates.
   * The graph position is unchanged.
   */
  public void rectangle(Point2D.Double pt1, Point2D.Double pt2)
  {
    rectangle(pt1.x, pt1.y, pt2.x, pt2.y, false);
  }

  private void rectangle(double x1, double y1, double x2, double y2, boolean fill)
  {
    if (waitForReady())
      return;
    double xold = getPosX();
    double yold = getPosY();
    double centerX = (x1 + x2) / 2;
    double centerY = (y1 + y2) / 2;
    double w = Math.abs(x2 - x1);
    double h = Math.abs(y2 - y1);
    move(centerX, centerY);
    _wnd.drawRectangle(w, h, fill);
    move(xold, yold);
  }

  /**
   * Draw a filled rectangle with center at the current graph position
   * and given width and height in window coordinates using the current color.
   * The graph position is unchanged.
   */
  public void fillRectangle(double width, double height)
  {
    if (waitForReady())
      return;
    _wnd.drawRectangle(width, height, true);
  }

  /**
   * Draw a filled rectangle with given opposite corners
   * in window coordinates using the current color.
   * The graph position is unchanged.
   */
  public void fillRectangle(double x1, double y1, double x2, double y2)
  {
    rectangle(x1, y1, x2, y2, true);
  }

  /**
   * Draw a filled rectangle with given opposite corner points
   * in window coordinates using the current color.
   * The graph position is unchanged.
   */
  public void fillRectangle(Point2D.Double pt1, Point2D.Double pt2)
  {
    rectangle(pt1.x, pt1.y, pt2.x, pt2.y, true);
  }

  /**
   * Draw an arc with center at the current graph position
   * and given radius in window coordinates.
   * Start angle and extend angle in degrees (zero to east, positive counterclockwise).
   * The graph position is unchanged.
   */
  public void arc(double radius, double startAngle, double extendAngle)
  {
    if (waitForReady())
      return;
    _wnd.drawArc(radius, startAngle, extendAngle, false);
  }

  /**
   * Draw a filled arc with center at the current graph position
   * and given radius in window coordinates.
   * Start angle and extend angle in degrees (zero to east, positive counterclockwise).
   * The graph position is unchanged.
   */
  public void fillArc(double radius, int startAngle, int extendAngle)
  {
    if (waitForReady())
      return;
    _wnd.drawArc(radius, startAngle, extendAngle, true);
  }

  /**
   * Draw a polygon with given corner coordinates in window coordinates.
   * (Both arrays must be of equal size.)
   * The graph position is unchanged.
   */
  public void polygon(double[] x, double[] y)
  {
    if (waitForReady())
      return;
    _wnd.drawPolygon(x, y, x.length, false);
  }

  /**
   * Draw a polygon with given corner points in window coordinates.
   * The graph position is unchanged.
   */
  public void polygon(Point2D.Double[] corner)
  {
    if (waitForReady())
      return;
    _wnd.drawPolygon(corner, false);
  }

  /**
   * Draw a filled polygon with given corner coordinates in window coordinates
   * using the current color.
   * (Both arrays must be of equal size.)
   * The graph position is unchanged.
   */
  public void fillPolygon(double[] x, double[] y)
  {
    if (waitForReady())
      return;
    _wnd.drawPolygon(x, y, x.length, true);
  }

  /**
   * Draw a filled polygon with given corner points in window coordinates
   * using the current color.
   * The graph position is unchanged.
   */
  public void fillPolygon(Point2D.Double[] corner)
  {
    if (waitForReady())
      return;
    _wnd.drawPolygon(corner, true);
  }

  /**
   * Draw a quadratic bezier curve.
   * A quadratic bezier curve is a Bezier curve of degree 2 and 
   * is defined by 3 points: pt1, pt2, ptc (control point).
   * The curve first endpoint is pt1, the second endpoint is pt2. 
   * The line ptc-pt1 is the tangent of the curve in point pt1. 
   * The line ptc-pt2 is the tangent of the curve in point pt2.
   * The graph position is unchanged.
   */
  public void quadraticBezier(Point2D.Double pt1, Point2D.Double ptc,
    Point2D.Double pt2)
  {
    if (waitForReady())
      return;
    _wnd.drawQuadraticBezier(pt1, ptc, pt2);
  }

  /**
   * Draw a quadratic bezier curve.
   * A quadratic bezier curve is a Bezier curve of degree 2 and 
   * is defined by 3 points: pt1, pt2, ptc (control point).
   * The curve first endpoint is pt1, the second endpoint is pt2. 
   * The line ptc-pt1 is the tangent of the curve in point pt1. 
   * The line ptc-pt2 is the tangent of the curve in point pt2.
   * The graph position is unchanged.
   */
  public void quadraticBezier(
    double x1, double y1,
    double xc, double yc,
    double x2, double y2)
  {
    quadraticBezier(
      new Point2D.Double(x1, y1),
      new Point2D.Double(xc, yc),
      new Point2D.Double(x2, y2));
  }

  /**
   * Draw a cubic bezier curve.
   * A cubic bezier curve is a Bezier curve of degree 3 and 
   * is defined by 4 points: pt1, pt2, and the control points ptc1, ptc2.
   * The curve first endpoint is pt1, the second endpoint is pt2. 
   * The line ptc1-pt1 is the tangent of the curve in point pt1. 
   * The line ptc2-pt2 is the tangent of the curve in point pt2.
   * The graph position is unchanged.
   */
  public void cubicBezier(Point2D.Double pt1, Point2D.Double ptc1,
    Point2D.Double ptc2, Point2D.Double pt2)
  {
    if (waitForReady())
      return;
    _wnd.drawCubicBezier(pt1, ptc1, ptc2, pt2);
  }

  /**
   * Draw a cubic bezier curve.
   * A cubic bezier curve is a Bezier curve of degree 3 and 
   * is defined by 4 points: pt1, pt2, and the control points ptc1, ptc2.
   * The curve first endpoint is pt1, the second endpoint is pt2. 
   * The line ptc1-pt1 is the tangent of the curve in point pt1. 
   * The line ptc2-pt2 is the tangent of the curve in point pt2.
   * The graph position is unchanged.
   */
  public void cubicBezier(
    double x1, double y1,
    double xc1, double yc1,
    double xc2, double yc2,
    double x2, double y2)
  {
    cubicBezier(
      new Point2D.Double(x1, y1),
      new Point2D.Double(xc1, yc1),
      new Point2D.Double(xc2, yc2),
      new Point2D.Double(x2, y2));
  }

  /**
   * Draw a triangle with given corner coordinates in window coordinates.
   * The graph position is unchanged.
   */
  public void triangle(double x1, double y1, double x2, double y2, double x3,
    double y3)
  {
    double[] x = new double[3];
    double[] y = new double[3];
    x[0] = x1;
    x[1] = x2;
    x[2] = x3;
    y[0] = y1;
    y[1] = y2;
    y[2] = y3;

    if (waitForReady())
      return;
    _wnd.drawPolygon(x, y, 3, false);
  }

  /**
   * Draw a triangle with given corners in window coordinates.
   * The graph position is unchanged.
   */
  public void triangle(Point2D.Double pt1, Point2D.Double pt2, Point2D.Double pt3)
  {
    triangle(pt1.x, pt1.y, pt2.x, pt2.y, pt3.x, pt3.y);
  }

  /**
   * Draw a filled triangle with given corner coordinates in window coordinates
   * using the current color.
   * The graph position is unchanged.
   */
  public void fillTriangle(double x1, double y1, double x2, double y2,
    double x3, double y3)
  {
    double[] x = new double[3];
    double[] y = new double[3];
    x[0] = x1;
    x[1] = x2;
    x[2] = x3;
    y[0] = y1;
    y[1] = y2;
    y[2] = y3;

    if (waitForReady())
      return;
    _wnd.drawPolygon(x, y, 3, true);
  }

  /**
   * Draw a filled triangle with given corners in window coordinates
   * using the current color.
   * The graph position is unchanged.
   */
  public void fillTriangle(Point2D.Double pt1, Point2D.Double pt2, Point2D.Double pt3)
  {
    fillTriangle(pt1.x, pt1.y, pt2.x, pt2.y, pt3.x, pt3.y);
  }

  /**
   * Draw a figure defined by the given GeneralPath (using window coordinates).
   * The graph position is unchanged.
   */
  public void generalPath(GeneralPath gp)
  {
    if (waitForReady())
      return;
    _wnd.drawGeneralPath(gp, false);
  }

  /**
   * Fill a figure defined by the given GeneralPath (using window coordinates)
   * using the current color.
   * The graph position is unchanged.
   */
  public void fillGeneralPath(GeneralPath gp)
  {
    if (waitForReady())
      return;
    _wnd.drawGeneralPath(gp, true);
  }

  /**
   * Draw a string at the current graph position.
   * The graph position is unchanged.
   */
  public void text(String str)
  {
    if (waitForReady())
      return;
    _wnd.drawString(str);
  }

  /**
   * Draw a char at the current graph position.
   * The graph position is unchanged.
   */
  public void text(char c)
  {
    String str = "" + c;
    if (waitForReady())
      return;
    _wnd.drawString(str);
  }

  /**
   * Draw a string at the given x-y position.
   * The graph position is unchanged.
   */
  public void text(double x, double y, String str)
  {
    if (waitForReady())
      return;
    _wnd.drawString(x, y, str);
  }

  /**
   * Draw a string at the given position.
   * The graph position is unchanged.
   */
  public void text(Point2D.Double pt, String str)
  {
    text(pt.x, pt.y, str);
  }

  /**
   * Draw a char at the given position.
   * The graph position is unchanged.
   */
  public void text(double x, double y, char c)
  {
    String str = "" + c;
    if (waitForReady())
      return;
    _wnd.drawString(x, y, str);
  }

  /**
   * Draw a char at the given position.
   * The graph position is unchanged.
   */
  public void text(Point2D.Double pt, char c)
  {
    text(pt.x, pt.y, c);
  }

  /**
   * Draw a string at the given position with extended text attributes.
   * The graph position is unchanged.
   */
  public void text(double x, double y, String str, Font font,
    Color textColor, Color bgColor)
  {
    if (waitForReady())
      return;

    _wnd.drawString(x, y, str, font, textColor, bgColor);
  }

  /**
   * Draw a string at the given position with extended text attributes.
   * The graph position is unchanged.
   */
  public void text(Point2D.Double pt, String str, Font font,
    Color textColor, Color bgColor)
  {
    text(pt.x, pt.y, str, font, textColor, bgColor);
  }

  /**
   * Select the given font for all following text operations.
   */
  public void font(Font font)
  {
    _wnd.getOffG2D().setFont(font);
  }

  /**
   * Draw a single point at the given window coordinates.
   * The graph position is set to the given point.
   */
  public void point(double x, double y)
  {
    if (waitForReady())
      return;
    _wnd.drawPoint(x, y);
  }

  /**
   * Draw a single point at the given pt (in window coordinates).
   * The graph position is set to the given point.
   */
  public void point(Point2D.Double pt)
  {
    if (waitForReady())
      return;
    _wnd.drawPoint(pt.x, pt.y);
  }

  /**
   * Change the color of the bounded region with the given internal point 
   * to the replacement color using the flood fill algorithm.
   */
  public void fill(Point2D.Double pt, Color color, Color replacement)
  {
    Point point = new Point(toUserX(pt.x), toUserY(pt.y));
    _wnd.fill(point, color, replacement);
  }

  /**
   * Change the color of the bounded region with the given internal point 
   * to the replacement color using the flood fill algorithm.
   */
  public void fill(double x, double y, Color color, Color replacement)
  {
    fill(new Point2D.Double(x, y), color, replacement);
  }

  /**
   * Show the GIF image from given file path at x, y window coordinates
   * (specifies lowerleft corner of image).<br><br>
   * From the given filename the image file is searched in the following order:<br>
   * - if application is packed into a jar archive, relative to the root of the jar archive<br>
   * - relative or absolute to current application directory<br>
   * - if filename starts with http://, from the given URL<br>
   * - if imagePath prefixed with _ relative to the root of the jar archive<br>
   * Return true if successful.
   * If enableRepaint(false), the image is drawn in offscreen buffer only.
   */
  public boolean image(String imagePath, double x, double y)
  {
    if (waitForReady())
      return false;
    return _wnd.showImage(imagePath, x, y);
  }

  /**
   * Show the GIF image from given file path at given point(in window coordinates)
   * (specifies lowerleft corner of image).<br><br>
   * From the given filename the image file is searched in the following order:<br>
   * - if application is packed into a jar archive, relative to the root of the jar archive<br>
   * - relative or absolute to current application directory<br>
   * - if filename starts with http://, from the given URL<br>
   * - if imagePath prefixed with _ relative to the root of the jar archive<br>
   * Return true if successful.
   * If enableRepaint(false), the image is drawn in offscreen buffer only.
   */
  public boolean image(String imagePath, Point2D.Double pt)
  {
    if (waitForReady())
      return false;
    return _wnd.showImage(imagePath, pt.x, pt.y);
  }

  /**
   * Show the GIF image from given BufferedImage at x, y window coordinates
   * (specifies lowerleft corner of image).
   * Return true if successful.
   * If enableRepaint(false), the image is drawn in offscreen buffer only.
   */
  public boolean image(BufferedImage bi, double x, double y)
  {
    if (waitForReady())
      return false;
    return _wnd.showImage(bi, x, y);
  }

  /**
   * Show the GIF image from given BufferedImage at given point(in window coordinates)
   * (specifies lowerleft corner of image).
   * Return true if successful.
   * If enableRepaint(false), the image is drawn in offscreen buffer only.
   */
  public boolean image(BufferedImage bi, Point2D.Double pt)
  {
    if (waitForReady())
      return false;
    return _wnd.showImage(bi, pt.x, pt.y);
  }

  /**
   * Return the width (horizontal size) of the GIF image from the given path (in window coordinates).
   * Return 0, if GIF image is invalid.
   */
  public double imageWidth(String imagePath)
  {
    BufferedImage bi = GPanel.getImage(imagePath);
    if (bi == null)
      return 0;
    return _wnd.toWindowWidth(bi.getWidth());
  }

  /**
   * Return the height (vertical size) of the GIF image from the given path (in window coordinates).
   * Return 0, if GIF image is invalid.
   */
  public double imageHeight(String imagePath)
  {
    BufferedImage bi = GPanel.getImage(imagePath);
    if (bi == null)
      return 0;
    return _wnd.toWindowHeight(bi.getHeight());
  }

  /**
   * Apply the given AffineTransform to offscreen buffer.
   */
  public void applyTransform(AffineTransform at)
  {
    if (waitForReady())
      return;
    _wnd.transformGraphics(at);
  }

  /**
   * Enable or disable the automatic repaint in graphics methods.
   * Return the current state of repainting.<br>
   * When automatic repaint is disabled, the method repaint() must be called to show
   * the image on the screen.<br>
   * Useful to avoid flickering while animating an image. If repainting is disabled
   * clear() will only erase the offscreen buffer and not the screen. repaint() should
   * be called after drawing the new situation in order to copy the offscreen buffer to the screen.<br>
   * While printing, repainting is disabled.
   */
  public boolean enableRepaint(boolean doRepaint)
  {
    if (_isPrinting)
      return false;
    boolean _isRepaint = _wnd.getRepaint();
    _wnd.setRepaint(doRepaint);
    return _isRepaint;
  }

  protected boolean kbhit()
  {
    Console.delay(1);
    return _gotKey;
  }

  protected char getKey()
  {
    synchronized (monitor)
    {
      if (_gotKey)
      {
        _gotKey = false;
        return _keyChar;
      }
      else
        return KeyEvent.CHAR_UNDEFINED;
    }
  }

  protected int getKeyCode()
  {
    synchronized (monitor)
    {
      if (_gotKey)
      {
        _gotKey = false;
        return _keyCode;
      }
      else
        return KeyEvent.CHAR_UNDEFINED;
      // Better should return KeyEvent.VK_UNDEFINED, kept because of old releases
    }
  }

  protected char getKeyWait()
  {
    waitThread = Thread.currentThread();
    _mustNotify = true;
    synchronized (this)
    {
      try
      {
        wait();
      }
      catch (InterruptedException ex)
      {
        _mustNotify = false;
        return KeyEvent.CHAR_UNDEFINED;
      }
    }
    _mustNotify = false;
    return getKey();
  }

  protected int getKeyCodeWait()
  {
    waitThread = Thread.currentThread();
    _mustNotify = true;
    synchronized (this)
    {
      try
      {
        wait();
      }
      catch (InterruptedException ex)
      {
        _mustNotify = false;
        return KeyEvent.VK_UNDEFINED;
      }
    }
    _mustNotify = false;
    return getKeyCode();
  }

  protected int getModifiers()
  {
    return _modifiers;
  }

  protected String getModifiersText()
  {
    return _modifiersText;
  }

  protected void setKeyListener(KeyListener listener)
  {
    _keyListener = listener;
  }

  private void doNotify()
  {
    synchronized (this)
    {
      notify();
    }
  }

  /**
   * Set the paint mode of the graphics context to overwrite
   * with current color.
   */
  public void setPaintMode()
  {
    if (waitForReady())
      return;
    _wnd.getOffG2D().setPaintMode();
  }

  /**
   * Sets the paint mode to alternate between the current color and the given color.
   * This specifies that logical pixel operations are performed in the XOR mode,
   * which alternates pixels between the current color and a specified XOR color.
   * When drawing operations are performed, pixels which are the current color
   * are changed to the specified color, and vice versa.
   * Pixels that are of colors other than those two colors are changed in an
   * unpredictable but reversible manner; if the same figure is drawn twice,
   * then all pixels are restored to their original values.
   */
  public void setXORMode(Color c)
  {
    if (waitForReady())
      return;
    _wnd.getOffG2D().setXORMode(c);
  }

  /**
   * Return the Graphics2D context of the offscreen buffer
   */
  public Graphics2D getOffG2D()
  {
    if (waitForReady())
      return null;
    return _wnd.getOffG2D();
  }

  /**
   * Release all used system resources (offscreen buffer, graphics context)
   * and unblock getKeyWait() and getKeyCodeWait() and waiting Monitor.putSleep().
   * After calling, don't use the GPane instance anymore.
   */
  public void dispose()
  {
    _instances.remove(this);
    _dispose();
  }

  private void _dispose()
  {
    if (_instanceCount == 0)
      return;
    if (waitForReady())
      return;
    _wnd.setVisible(false);
    _wnd.getOffG2D().dispose();
    _instanceCount -= 1;
    _wnd._isDisposed = true;
    Monitor.wakeUp();   // Take out of waiting thread
    if (waitThread != null)
    {
      waitThread.interrupt();  // Take out if hangs in getKeyWait(), getKeyCodeWait()
      waitThread = null;
    }
  }

  /**
   * Create a store buffer and copy current graphics to it.
   * @see #recallGraphics
   * @see #clearStore
   */
  public void storeGraphics()
  {
    _wnd.storeGraphics();
  }

  /**
   * Copy graphics from store buffer to offscreen buffer
   * and render it on the screen window (if enableRepaint(true)).
   * @see #storeGraphics
   * @see #clearStore
   */
  public void recallGraphics()
  {
    _wnd.recallGraphics();
    if (_wnd.getRepaint())
      repaint();
  }

  /**
   * Clear store buffer by uniformly painting it with with given color.
   * @see #storeGraphics
   * @see #recallGraphics
   */
  public void clearStore(Color color)
  {
    _wnd.clearStore(color);
  }

  /**
   * Return version information
   */
  public String getVersion()
  {
    return SharedConstants.VERSION;
  }

  /**
   * Return copywrite information
   */
  public String getAbout()
  {
    return SharedConstants.ABOUT;
  }

  /**
   * Essentially for internal use only.
   * You may override this method to get your own notification, when the focus is gained.
   */
  public void focusGained(FocusEvent evt)
  {
  }

  /**
   * Essentially for internal use only.
   * You may override this method to get your own notification, when the focus is lost.
   * (Do not override for mode = APPLETFRAME).
   */
  public void focusLost(FocusEvent evt)
  {
    if (_panelMode == GPanel.APPLETFRAME && _isFirst)
    {
      _wnd.toFront();
      _isFirst = false;
    }
  }

  /**
   * Set the size of the entire window (including title bar and borders)
   * in device coordinates.<br><br>
   * The window coordinates are unchanged.<br><br>
   * Due to rescaling some graphics resolution is lost. To avoid this, use size
   * parameters when instantiating the GPanel.
   */
  public void windowSize(final int width, final int height)
  {
    if (EventQueue.isDispatchThread())
    {
      _wnd.setWinSize(width, height);
    }
    else
    {
      try
      {
        EventQueue.invokeAndWait(new Runnable()
        {
          public void run()
          {
            _wnd.setWinSize(width, height);
          }
        });
      }
      catch (Exception ex)
      {
      }
    }
  }

  /**
   * For internal use only.
   */
  public void paintComponent(Graphics g)
  {
    super.paintComponent(g);
    if (!_isReady)  // When called the first time
    {
      if (_panelMode == GPanel.EMBEDDED)
      {
        _wnd.setWinSize(getSize().width, getSize().height);
        requestFocus(); // In order to get key events
      }
      _isReady = true;
    }

    BufferedImage bi = _wnd.getBufferedImage();
    Graphics2D g2D = (Graphics2D)g;
    g2D.drawImage(bi, _wnd.getAffineScale(), this);
  }

  /**
   * For internal use only.
   */
  public int print(Graphics g, PageFormat pf, int pageIndex)
  {
    if (pageIndex != 0)
      return NO_SUCH_PAGE;
    _isPrinting = true;
    boolean isRepaintEnabled = enableRepaint(false);
    Graphics2D g2D = (Graphics2D)g;
    double printerWidth = pf.getImageableWidth();
    double printerHeight = pf.getImageableHeight();
    double printerSize
      = printerWidth > printerHeight ? printerWidth : printerHeight;
    // The 600 depends on the JPanel default size
    double scalex = 600 / printerSize * _scale;
    double scaley = scalex;

    double xZero = pf.getImageableX();
    double yZero = pf.getImageableY();

    Graphics2D g2DOld = _wnd.getOffG2D();
    _wnd.setOffG2D(g2D);

    g2D.translate(xZero, yZero);
    g2D.scale(scalex, scaley);

    if (_printScreen)
      print(g);
    else
      _userPanel.draw();

    // Restore old context
    _isPrinting = false;
    enableRepaint(isRepaintEnabled);
    _wnd.setOffG2D(g2DOld);

    return PAGE_EXISTS;
  }

  /**
   * Return the color of the pixel at given window coordinates.
   */
  public Color getPixelColor(double x, double y)
  {
    return _wnd.getPixelColor(x, y);
  }

  /**
   * Return the color of the pixel at given point (in window coordinates).
   */
  public Color getPixelColor(Point2D.Double pt)
  {
    return getPixelColor(pt.x, pt.y);
  }

  /**
   * Set the color of the background.
   */
  public void setBackgroundColor(Color value)
  {
    backgroundColor = value;
    bgColor(value);
  }

  /**
   * Return the color of the background
   */
  public Color getBackgroundColor()
  {
    return backgroundColor;
  }

  /**
   * Set the color of the pen.
   */
  public void setPenColor(Color value)
  {
    penColor = value;
    color(value);
  }

  /**
   * Return the color of the pen.
   */
  public Color getPenColor()
  {
    return penColor;
  }

  /**
   * Set the xmin range value.
   */
  public void setXmin(double value)
  {
    xmin = value;
    window(xmin, xmax, ymin, ymax);
  }

  /**
   * Return the ymin range value.
   */
  public double getXmin()
  {
    return xmin;
  }

  /**
   * Set the xmax range value.
   */
  public void setXmax(double value)
  {
    xmax = value;
    window(xmin, xmax, ymin, ymax);
  }

  /**
   * Return the xmax range value.
   */
  public double getXmax()
  {
    return xmax;
  }

  /**
   * Set the ymin range value.
   */
  public void setYmin(double value)
  {
    ymin = value;
    window(xmin, xmax, ymin, ymax);
  }

  /**
   * Return the ymin range value.
   */
  public double getYmin()
  {
    return ymin;
  }

  /**
   * Set the xmax range value.
   */
  public void setYmax(double value)
  {
    ymax = value;
    window(xmin, xmax, ymin, ymax);
  }

  /**
   * Return the ymax range value.
   */
  public double getYmax()
  {
    return ymax;
  }

  /**
   * Enable/disable the focus.
   */
  public void setEnableFocus(boolean enable)
  {
    enableFocus = enable;
    setFocusable(enable);
  }

  /**
   * Return true, if the focus is enabled; otherwise false.
   */
  public boolean getEnableFocus()
  {
    return enableFocus;
  }

  /**
   * Enable/disable resizing.
   */
  public void setEnableResize(boolean enable)
  {
    _wnd.setResizable(false);
  }

  /**
   * Delay execution for the given amount of time ( in ms ).
   */
  public static void delay(int time)
  {
    try
    {
      Thread.currentThread().sleep(time);
    }
    catch (Exception e)
    {
    }
  }

  private static GPanel.ClosingMode getClosingMode(MyProperties props)
  {
    String value = props.getStringValue("GPanelClosingMode");
    if (value != null)  // Entry found
    {
      value = value.trim();
      if (value.equals("TerminateOnClose"))
        return GPanel.ClosingMode.TerminateOnClose;
      if (value.equals("ClearOnClose"))
        return GPanel.ClosingMode.ClearOnClose;
      if (value.equals("AskOnClose"))
        return GPanel.ClosingMode.AskOnClose;
      if (value.equals("DisposeOnClose"))
        return GPanel.ClosingMode.DisposeOnClose;
      if (value.equals("ReleaseOnClose"))
        return GPanel.ClosingMode.ReleaseOnClose;
      if (value.equals("NothingOnClose"))
        return GPanel.ClosingMode.NothingOnClose;
      return GPanel.ClosingMode.TerminateOnClose;  // Entry not valid
    }
    return GPanel.ClosingMode.TerminateOnClose;  // Entry not valid
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
   * Returns the current double-click delay.
   * @return the double-click delay (in milliseconds). 0 if double-click delay
   * is disabled
   */
  public int getDoubleClickDelay()
  {
    return doubleClickTime;
  }

  /**
   * Returns the number of GPane instances.
   * @return current number of GPane windows
   */
  public static int getInstanceCount()
  {
    return _instanceCount;
  }

  /**
   * Release all used system resources (offscreen buffer, graphics context)
   * and unblock getKeyWait() and getKeyCodeWait() and waiting Monitor.putSleep()
   * of all currently openend GPane windows.
   */
  public static void disposeAll()
  {
    for (GPane pane : _instances)
      pane._dispose();
    _instances.clear();
  }

  /**
   * Creates an image that shows a filled circle with given text 
   * center-aligned and draws it at given position. It can be used 
   * as a graphics element for network or graph pictures.
   * @param xCenter the x-coordinate of the center in window coordinates
   * @param yCenter the y-coordinate of the center in window coordinates
   * @param text the text to show
   * @param size the diameter of the circle
   * @param borderSize the width of the border line (in pixels)
   * @param borderColor the color of the border
   * @param textColor the color of the text
   * @param bgColor the color of the background
   * @param font the text font
   * @return the buffered image of the node image
   */
  public BufferedImage drawNode(double xCenter, double yCenter,
    String text, int size, int borderSize,
    Color borderColor, Color textColor,
    Color bgColor, Font font)
  {
    double w = toWindowWidth(size);
    double h = toWindowHeight(size);
    BufferedImage bi
      = GWindow.createNodeImage(text, size, borderSize, borderColor,
        textColor, bgColor, font);
    image(bi, xCenter - w / 2, yCenter - h / 2);
    return bi;
  }
  
}
