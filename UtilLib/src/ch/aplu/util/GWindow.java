// GWindow.java

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
import java.awt.image.*;
import java.awt.event.*;
import java.awt.font.FontRenderContext;
import java.awt.font.LineMetrics;
import java.awt.font.TextAttribute;
import java.awt.font.TextLayout;
import javax.swing.*;
import java.awt.geom.*;
import java.text.AttributedString;

/*
 * Window coordinates:  (wx, wy)
 * User coordinates: (px, py) (screen pixels)
 * Transformation: px = px( wx ), py = py( wy )
 * User range: 0..xPix, 0..yPix (0,0) upper left corner, x to right, y down
 * Window range: xmin..xmax, ymin..ymax (0,0) lower left corner, x to right, y up
 * px = a*wx + b
 * py = c*wy + d
 * with a = xPix / ( xmax - xmin )
 * b = xPix * xmin / ( xmin - xmax )
 * c = yPix / ( ymin - ymax )
 * d = yPix * ymax / ( ymax - ymin )
 * Inverse:
 * wx = (px - b) /a
 * wy = (py - d) /c
 *
 * When zooming the offscreen buffer's coordinate system remains the same. The
 * affine transform scale factor is applied for rendering. Therefore the
 * transformation methods must be distinguished (toUser, _toUser,
 * toWindow, _toWindow)
 *
 */

/*
 * Includes methods with package access to be used by class GPanel (derived from JPanel).
 * Each GPanel constructs its own GWindow.
 * GPanel's paintComponent paints the offscreen buffer using
 * an AffineTransform. So the coordinate transformation from window coordinates
 * to user coordinates is kept unchanged (GPanel sets doAjust = false)
 *
 * There is a exception from this scheme when construction a GPanel embedded in an other JPanel
 * with new GPanel(GPanel.EMBEDDED).
 * In this case the component must report it's current size the first time the GPanel is shown.
 */
/**
 * Simple screen window with an event handler to exit the application when
 * clicking the close button in the title bar.<br>
 * The exit handler will call the dispose method for the graphic context
 * in order to release system resources automatically.
 *
 * Defaut background is white, default drawing color is black.
 */
public class GWindow extends JFrame
{
  private class MyComponentAdapter implements ComponentListener
  {
    public void componentHidden(ComponentEvent e)
    {
      adjust();
      //    System.out.println("componentHidden event from "
      //    + e.getComponent().getClass().getName());
    }

    public void componentMoved(ComponentEvent e)
    {
      if (_doAdjust)
        adjust();
      if (_statusDialog != null)
      {
        _statusDialog.getDialog().setLocation(getLocation().x,
          getLocation().y + getHeight());
      }
      if (isVisible())
      {
        _screenLocation = getLocationOnScreen();
        _lastScreenLocation = new Point(_screenLocation);
      }

      //   System.out.println("componentMoved event from "
      //     + e.getComponent().getClass().getName());
    }

    public void componentResized(ComponentEvent e)
    {
      if (_doAdjust)
        adjust();
      if (_statusDialog != null)
      {
        _statusDialog.resize(new Point(getLocation().x,
          getLocation().y + getHeight()), getWidth());
      }
      //    System.out.println("componentResized event from "
      //    + e.getComponent().getClass().getName());
    }

    public void componentShown(ComponentEvent e)
    {
      adjust();
      //  System.out.println("componentShown event from "
      //    + e.getComponent().getClass().getName());
    }
  }
  private static final long serialVersionUID = 1772883994005116227L;
  private final int DEFAULT_WIDTH = 500;
  private final int DEFAULT_HEIGHT = 500;
  private final int DEFAULT_ULX = 50;
  private final int DEFAULT_ULY = 50;
  private final int DEFAULT_XMIN = 0;
  private final int DEFAULT_XMAX = 1;
  private final int DEFAULT_YMIN = 0;
  private final int DEFAULT_YMAX = 1;
  private final Color DEFAULT_BGCOLOR = Color.white;
  private final String DEFAULT_TITLE = "Graphics Window";
  private String _title;
  private GPane _myPane;
  private JPanel _contentPane;
  private Component _panel = null;
  private BufferedImage _bi = null;
  private Graphics2D _offG2D;
  private Graphics2D _screenG2D;  // Different while printing
  private boolean _doRepaint = true;
  private boolean _doAdjust = true;
  private double _a, _b, _c, _d;
  private int _width = DEFAULT_WIDTH;
  private int _height = DEFAULT_HEIGHT;
  private int _ulx = DEFAULT_ULX;
  private int _uly = DEFAULT_ULY;
  private Color _bgColor = DEFAULT_BGCOLOR;
  private double _xCurrent;
  private double _yCurrent;
  private Color _color = Color.black;
  private int _lineWidth = 1;
  private boolean _isFullscreen = false;
  private boolean _isUndecorated = false;
  private int _leftInset;
  private int _rightInset;
  private int _topInset;
  private int _bottomInset;
  private int _xPix = DEFAULT_WIDTH;
  private int _yPix = DEFAULT_HEIGHT;
  private double _xmin = DEFAULT_XMIN;
  private double _xmax = DEFAULT_XMAX;
  private double _ymin = DEFAULT_YMIN;
  private double _ymax = DEFAULT_YMAX;
  private BufferedImage _saveImage;
  private Graphics2D _saveGraphics;
  private int _reduceSize = 0;
  private int _emptyBorder = 0;
  private int _panelMode = GPanel.STANDARD;
  private boolean _doNothingOnClose = false;
  private ExitListener _exitListener = null;
  private ModelessOptionPane _statusDialog = null;
  private GPanel.ClosingMode _closingMode;
  protected boolean _isDisposed = false;
  private Point _screenLocation = new Point();
  private static Point _lastScreenLocation = null;

  /**
   * Construct a GWindow with default title and size.
   * of the embedded component and show the window at default screen positition.
   * Creates an offscreen buffer (instance of BufferedImage) to be
   * used for animations.
   * Default size of client window: 500 x 500 pixels.
   * Default position upper left corner: 50,50.
   */
  public GWindow()
  {
    _title = DEFAULT_TITLE;
    init();
  }

  /**
   * Same as GWindow() with given title.
   */
  public GWindow(String title)
  {
    _title = title;
    init();
  }

  /**
   * Same as GWindow() with given title and size of client window.
   */
  public GWindow(String title, Size size)
  {
    _title = title;
    _width = size.getWidth();
    _height = size.getHeight();
    init();
  }

  /**
   * Same as GWindow() with given title, position and size of client window.
   * If size is a Fullscreen object reference, the window size will be adapted to
   * the current screen dimensions and the size of the client area is accordingly
   * adapted and may be asked with getWidth(), getHeight().
   */
  public GWindow(String title, Position position, Size size)
  {
    if (size instanceof Fullscreen)
    {
      _isFullscreen = true;
    }
    _title = title;
    _width = size.getWidth();
    _height = size.getHeight();
    _ulx = position.getUlx();
    _uly = position.getUly();
    init();
  }

  /**
   * Same as GWindow(), but titlebar may be omited.
   * If size is a Fullscreen object reference, the window size will be adapted to
   * the current screen dimensions and the size of the client area is accordingly
   * adapted and may be asked with getWidth(), getHeight().
   */
  public GWindow(Position position, Size size, boolean noTitlebar)
  {
    if (size instanceof Fullscreen)
      _isFullscreen = true;
    setUndecorated(noTitlebar);
    _isUndecorated = noTitlebar;
    _width = size.getWidth();
    _height = size.getHeight();
    _ulx = position.getUlx();
    _uly = position.getUly();
    init();
  }

  protected GWindow(GPane myPane, String title, Position position, Size size,
    boolean noTitlebar, GPanel.ClosingMode closingMode)
  {
    _myPane = myPane;
    _closingMode = closingMode;
    if (title == null)
      _title = "";
    else
      _title = title;
    if (size instanceof Fullscreen)
      _isFullscreen = true;
    setUndecorated(noTitlebar);
    _isUndecorated = noTitlebar;
    _width = size.getWidth();
    _height = size.getHeight();
    _ulx = position.getUlx();
    _uly = position.getUly();
    init();

  }

  private void init()
  {
    if (_isFullscreen)
    {
      // Create dummy frame in order to determine insets
      JFrame dummyFrame = new JFrame();
      dummyFrame.setUndecorated(_isUndecorated);
      dummyFrame.pack();
      _leftInset = dummyFrame.getInsets().left;
      _rightInset = dummyFrame.getInsets().right;
      _topInset = dummyFrame.getInsets().top;
      _bottomInset = dummyFrame.getInsets().bottom;
      dummyFrame.dispose(); // Release resources

      // Determine client area
      _width = getScreenWidth() - _leftInset - _rightInset;
      _height = getScreenHeight() - _topInset - _bottomInset;
    }

    enableEvents(AWTEvent.WINDOW_EVENT_MASK);
    try
    {
      addComponentListener(new MyComponentAdapter());
      _contentPane = (JPanel)getContentPane();
      _contentPane.setLayout(new BorderLayout());
      super.setTitle(_title);
      if (_isFullscreen)
        _screenLocation = new Point(0, 0);
      else
         _screenLocation = new Point(_ulx, _uly);
      setLocation(_screenLocation);
      _lastScreenLocation = new Point(_screenLocation);
      setBackground(_bgColor);
      _bi = new BufferedImage(_width, _height, BufferedImage.TYPE_INT_RGB);
      _offG2D = _bi.createGraphics();
      _screenG2D = _offG2D;
    }
    catch (Exception e)
    {
      e.printStackTrace();
    }
  }

  /**
   * Register an ExitListener to get a notification when the close button is clicked.
   * (In this case, this is the only action performed when clicking the close button.)
   */
  public void addExitListener(ExitListener exitListener)
  {
    _exitListener = exitListener;
  }

  /**
   * Return a Position ref with specified upperleft x and y coordinates.
   * May be used in constructor to avoid the keyword <code>new</code>
   */
  public static Position position(int ulx, int uly)
  {
    Position p = new Position(ulx, uly);
    return p;
  }

  /**
   * Return a Size ref with specified width and height.
   * May be used in constructor to avoid the keyword new
   */
  public static Size size(int width, int height)
  {
    Size s = new Size(width, height);
    return s;
  }

  /**
   * Get the width of the screen (in pixels).
   */
  public static int getScreenWidth()
  {
    Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
    return (int)screenSize.width;
  }

  /**
   * Get the height of the screen (in pixels).
   */
  public static int getScreenHeight()
  {
    Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
    return (int)screenSize.height;
  }

  /**
   * Set the background color of the window and offscreen buffer
   * and show the window. All drawings are erased.
   * Return the previous color.
   */
  public Color setBgColor(Color color)
  {
    Color oldColor = _bgColor;
    _bgColor = color;
    _offG2D.setPaint(_bgColor);
    _offG2D.fill(new Rectangle.Double(0, 0, _width, _height));
    _offG2D.setPaint(Color.black);
    if (_panel != null)
      _panel.setBackground(_bgColor);
    setBackground(_bgColor);
    validate();
    return oldColor;
  }

  /**
   * Return the current background color.
   */
  public Color getBgColor()
  {
    return _bgColor;
  }

  /**
   * Set the position of the window (position of uppler left corner in device coordinates).
   */
  public void setWinPosition(Position position)
  {
    _ulx = position.getUlx();
    _uly = position.getUly();
    setLocation(new Point(_ulx, _uly));
  }

  /**
   * Set the position of the window (x-y-coordinates of upper left corner in device coordinates).
   */
  public void setWinPosition(int ulx, int uly)
  {
    setWinPosition(new Position(ulx, uly));
  }

  /**
   * Set the size of the embedded component (in device coordinates).
   */
  public void setWinSize(Size size)
  {
    _width = size.getWidth();
    _height = size.getHeight();
    setSize(_width, _height);
    if (_offG2D != null)
      _offG2D.dispose();
    _bi = new BufferedImage(_width, _height, BufferedImage.TYPE_INT_RGB);
    _offG2D = _bi.createGraphics();

    // Draw the background
    _offG2D.setPaint(_bgColor);
    _offG2D.fill(new Rectangle.Double(0, 0, _width, _height));
    _offG2D.setPaint(Color.black);
    adjust();
  }

  /**
   * Set the size of the embedded component (in device coordinates)
   *  to given width and heigth.
   */
  public void setWinSize(int width, int height)
  {
    setWinSize(new Size(width, height));
  }

  /**
   * Set window to screen center with given size of the embedded component.
   */
  public void setWinCenter(Size size)
  {
    setWinSize(new Size(size.getWidth(), size.getHeight()));
    _ulx = (int)((getScreenWidth() - _width) / 2.0);
    _uly = (int)((getScreenHeight() - _height) / 2.0);
    setWinPosition(new Position(_ulx, _uly));
  }

  /**
   * Set window to screen center.
   */
  public void setWinCenter()
  {
    _ulx = (int)((getScreenWidth() - _width) / 2.0);
    _uly = (int)((getScreenHeight() - _height) / 2.0);
    setWinPosition(new Position(_ulx, _uly));
  }

  /**
   * Add the component to the frame's contentPane
   * and show the window with default background color.
   */
  public void showComponent(JComponent component)
  {
    showComponent(component, _bgColor, true);
  }

  /**
   * Same as showComponent(JComponent component)
   * but set visibilty to the given value.
   * (Visibility may be changed by setVisible())
   */
  public void showComponent(JComponent component, boolean visible)
  {
    showComponent(component, _bgColor, visible);
  }

  /**
   * Add a component to the frame's contentPane
   * and show the window with the given background color.
   */
  public void showComponent(JComponent component, Color bgColor)
  {
    showComponent(component, bgColor, true);
  }

  /**
   * Same as showComponent(JComponent component, Color bgColor)
   * but set visibilty to the given value.
   * (Visibility may be changed by setVisible())
   */
  public void showComponent(JComponent component, Color bgColor, boolean visible)
  {
    _panel = component;
    _xCurrent = 0;
    _yCurrent = 0;
    component.setPreferredSize(new Dimension(_width, _height));
    component.setBackground(bgColor);
    _contentPane.setBackground(bgColor);
    _contentPane.add(component, BorderLayout.CENTER);
    pack();    // Adapt size of JFrame to size of JPanel

    if (!_isFullscreen)
    {
      _leftInset = getInsets().left;
      _rightInset = getInsets().right;
      _topInset = getInsets().top;
      _bottomInset = getInsets().bottom;
    } // else already determined with dummy frame

    // Draw the background
    _offG2D.setPaint(_bgColor);
    _offG2D.fill(new Rectangle.Double(0, 0, _width, _height));
    _offG2D.setPaint(Color.black);

    setVisible(visible);

  }

  /**
   * Return a reference to the offscreen buffer
   * created by GWindow's constructor.
   */
  public BufferedImage getBufferedImage()
  {
    return _bi;
  }

  /**
   * Return the Graphics2D context of the offscreen buffer
   * created by GWindow's constructor.
   */
  public Graphics2D getOffG2D()
  {
    return _offG2D;
  }

  /**
   * Set the Graphics2D context of the offscreen buffer.
   */
  public void setOffG2D(Graphics2D g2D)
  {
    _offG2D = g2D;
  }

  /**
   * Return the width of the client area in user coordinates.
   */
  public int getCurrentWidth()
  {
    Dimension d = _panel.getSize();
    return d.width;
  }

  /**
   * Return the height of the client area in user coordinates.
   */
  public int getCurrentHeight()
  {
    Dimension d = _panel.getSize();
    return d.height;
  }

  /**
   * Return the left inset of the window (border width) in pixels.
   */
  public int getLeftInset()
  {
    return _leftInset;
  }

  /**
   * Return the right inset of the window (border width) in pixels.
   */
  public int getRightInset()
  {
    return _rightInset;
  }

  /**
   * Return the top inset of the window (title bar + border) in pixels.
   */
  public int topLeftInset()
  {
    return _topInset;
  }

  /**
   * Return the bottom inset of the window (border width) in pixels.
   */
  public int getBottomInset()
  {
    return _bottomInset;
  }

  /**
   * Return the current magnification factor of horizontal window size.
   */
  public double getScaleX()
  {
    return (double)getCurrentWidth() / _width;
  }

  /**
   * Return the current magnification factor of vertical window size.
   */
  public double getScaleY()
  {
    return (double)getCurrentHeight() / _height;
  }

  /**
   * Return the width of the window in user coordinates
   * when it was created.
   */
  public int getPopupWidth()
  {
    return _width;
  }

  /**
   * Return the height of the window in user coordinates
   * when it was created.
   */
  public int getPopupHeight()
  {
    return _height;
  }

  /**
   * Perform scaling of the offscreen buffer to fit the image to
   * the current window dimension.
   * Return a reference to the new affine tranformation.
   */
  public AffineTransform getAffineScale()
  {
    AffineTransform atx = new AffineTransform();
    atx = _screenG2D.getTransform();
    atx.scale(getScaleX(), getScaleY());
    return atx;
  }

  /**
   * Set the title in the window's title bar.
   */
  public void setTitle(String title)
  {
    _title = title;
    super.setTitle(title);
  }

  // Without scaling
  // Internal use
  int _toUserX(double windowX)
  {
    return (int)Math.rint(_a * windowX + _b);
  }

  /**
   * Convert window coordinate to user coordinate (horizontal).
   */
  int toUserX(double windowX)
  {
    int xPix = _xPix - _reduceSize;
    double a = xPix * getScaleX() / (_xmax - _xmin);
    double b = xPix * getScaleX() * _xmin / (_xmin - _xmax) + _emptyBorder;
    return (int)Math.rint(a * windowX + b);
  }

  // Without scaling
  // Internal use
  int _toUserY(double windowY)
  {
    return (int)Math.rint(_c * windowY + _d);
  }

  /**
   * Convert window coordinate to user coordinate (vertical).
   */
  public int toUserY(double windowY)
  {
    int yPix = _yPix - _reduceSize;
    double c = yPix * getScaleY() / (_ymin - _ymax);
    double d = yPix * getScaleY() * _ymax / (_ymax - _ymin) + _emptyBorder;
    return (int)Math.rint(c * windowY + d);
  }

  // Without scaling
  // Internal use
  int _toUserWidth(double windowWidth)
  {
    return (int)Math.rint(Math.abs(_a * windowWidth));
  }

  /**
   * Convert window coordinates increment to user coordinates increment (horizontal).
   * Increment is always positive.
   */
  public int toUserWidth(double windowWidth)
  {
    int xPix = _xPix - _reduceSize;
    double a = xPix * getScaleX() / (_xmax - _xmin);
    return (int)Math.rint(Math.abs(a * windowWidth));
  }

  // Without scaling
  // Internal use
  int _toUserHeight(double windowHeight)
  {
    return (int)Math.rint(Math.abs(_c * windowHeight));
  }

  /**
   * Convert window coordinates increment to user coordinates increment (vertical).
   * Increment is always positive.
   */
  public int toUserHeight(double windowHeight)
  {
    int yPix = _yPix - _reduceSize;
    double c = yPix * getScaleY() / (_ymin - _ymax);
    return (int)Math.rint(Math.abs(c * windowHeight));
  }

  /**
   * Convert user coordinates to window coordinates (horizontal).
   */
  public double toWindowX(int userX)
  {
    int xPix = _xPix - _reduceSize;
    double a = xPix * getScaleX() / (_xmax - _xmin);
    double b = xPix * getScaleX() * _xmin / (_xmin - _xmax) + _emptyBorder;
    return (userX - b) / a;
  }

  /**
   * Convert user coordinates to window coordinates (vertical).
   */
  public double toWindowY(int userY)
  {
    int yPix = _yPix - _reduceSize;
    double c = yPix * getScaleY() / (_ymin - _ymax);
    double d = yPix * getScaleY() * _ymax / (_ymax - _ymin) + _emptyBorder;
    return (userY - d) / c;
  }

  /**
   * Convert user coordinates increment to window coordinates increment (horizontal).
   * Increment is always positive.
   */
  public double toWindowWidth(int userWidth)
  {
    int xPix = _xPix - _reduceSize;
    double a = xPix * getScaleX() / (_xmax - _xmin);
    return Math.abs(userWidth / a);
  }

  /**
   * Convert user coordinates increment to window coordinates increment (vertical).
   * Increment is always positive.
   */
  public double toWindowHeight(int userHeight)
  {
    int yPix = _yPix - _reduceSize;
    double c = yPix * getScaleY() / (_ymin - _ymax);
    return Math.abs(userHeight / c);
  }

  /**
   * Set window coordinate system
   * left_x, right_x, bottom_y, top_y
   */
  public void setWindow(double xmin, double xmax, double ymin, double ymax)
  {
    _xmin = xmin;
    _xmax = xmax;
    _ymin = ymin;
    _ymax = ymax;
    adjust();
  }

  /**
   * Convert the given shape from window coordinates to user coordinates.
   * Return a reference to a new tranformed shape.
   */
  public Shape toUser(Shape shape)
  {
    AffineTransform t = toUserTransform();
    return t.createTransformedShape(shape);
  }

  /**
   * Create an affine transform to convert a shape
   * from window coordinates to user coordinates and return it.
   */
  public AffineTransform toUserTransform()
  {
    double xScale = (getCurrentWidth() - 1) / (_xmax - _xmin);
    double yScale = (getCurrentHeight() - 1) / (_ymax - _ymin);
    AffineTransform t = new AffineTransform();
    t.scale(xScale, yScale);
    t.translate(-_xmin, _ymax);
    t.scale(1, -1);
    return t;
  }

  /**
   * Return version information.
   */
  public String getVersion()
  {
    return SharedConstants.VERSION;
  }

  /**
   * Return copywrite information.
   */
  public String getAbout()
  {
    return SharedConstants.ABOUT;
  }

  /**
   * Actualize the conversion between window und user coordinates
   * according to the current component size.
   */
  public void adjust()
  {
    Dimension d = new Dimension(_width, _height);
    _xPix = d.width - 1;    // width: nb of pixels, coordinates 0 .. nb-1
    _yPix = d.height - 1;
    adjustParams();
  }

  void adjustParams()
  {
    int xPix = _xPix - _reduceSize;
    int yPix = _yPix - _reduceSize;
    _a = xPix / (_xmax - _xmin);
    _b = xPix * _xmin / (_xmin - _xmax) + _emptyBorder;
    _c = yPix / (_ymin - _ymax);
    _d = yPix * _ymax / (_ymax - _ymin) + _emptyBorder;
  }

  int setXPix(int xPix)
  {
    int xPixOld = _xPix;
    _xPix = xPix;
    return xPixOld;
  }

  int setYPix(int yPix)
  {
    int yPixOld = _yPix;
    _yPix = yPix;
    return yPixOld;
  }

  protected void processWindowEvent(WindowEvent e)
  {
//    System.out.println( e.toString() );
    if (_doNothingOnClose)
      return;

    if (e.getID() == WindowEvent.WINDOW_CLOSING)
    {
      if (_exitListener != null)
      {
        _exitListener.notifyExit();
        return;
      }
      if (_panelMode != GPanel.STANDARD
        && (_panel instanceof GPane))
      {
        ((GPane)_panel).dispose();
        return;
      }

      if (_closingMode == GPanel.ClosingMode.NothingOnClose)
        return;

      if (_closingMode == GPanel.ClosingMode.AskOnClose)
      {
        if (JOptionPane.showConfirmDialog(this,
          "Terminating program. Are you sure?",
          "Please confirm",
          JOptionPane.YES_NO_OPTION) == JOptionPane.NO_OPTION)
          return;
      }

      if (_closingMode == GPanel.ClosingMode.ReleaseOnClose
        || _closingMode == GPanel.ClosingMode.DisposeOnClose)
        _myPane.dispose();

      if (_closingMode == GPanel.ClosingMode.TerminateOnClose
        || _closingMode == GPanel.ClosingMode.AskOnClose)
        System.exit(0);
    }
    else
      super.processWindowEvent(e);
  }

  /**
   * Delay execution for the given amount of time ( in ms ).
   */
  static public void delay(int time)
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
   * Clear the graphics offline buffer (without calling repaint).
   */
  public void clearBuf()
  {
    _offG2D.clearRect(0, 0, _width, _height);
    Paint paint = _offG2D.getPaint();
    _offG2D.setPaint(_bgColor);
    _offG2D.fill(new Rectangle.Double(0, 0, _width, _height));
    _offG2D.setPaint(paint);
  }

  /**
   * Clear the screen graphics (without calling repaint).
   */
  public void clear()
  {
    _panel.getGraphics().clearRect(0, 0, _width, _height);
  }

  /**
   * Load the given GIF or JPEG image and set the current
   * image's width and height. Return true if successful and
   * when loading is complete.
   * If not successful, width and height is set to 0.
   */
  /*
   public boolean loadImage(Image image)
   {
   MediaTracker mt = new MediaTracker(_panel);
   mt.addImage(image, 1);
   try
   {
   mt.waitForAll();
   }
   catch (Exception e)
   {
   System.out.println("Exception while loading image.");
   }

   if (image == null)
   {
   _imageWidth = 0;
   _imageHeight = 0;
   _image = null;
   return false;
   }
   _imageWidth = image.getWidth(_panel);
   _imageHeight = image.getHeight(_panel);

   if (_imageWidth == -1 || _imageHeight == -1)
   {
   _imageWidth = 0;
   _imageHeight = 0;
   _image = null;
   return false;
   }
   _image = image;
   return true;
   }
   */
  /**
   * Return the width (horizontal size) of the GIF or JPEG image from the
   * given path (in device coordinates).
   * Return 0, if image is invalid.
   */
  /*
   public int getImageWidth(String imagePath)
   {
   Image image = Toolkit.getDefaultToolkit().getImage(imagePath);
   return getImageWidth(image);
   }

   /**
   * Return the width (horizontal size) of the GIF or JPEG image from the
   * given URL (in device coordinates).
   * Return 0, if image is invalid.
   */
  /*
   public int getImageWidth(URL imageUrl)
   {
   Image image = Toolkit.getDefaultToolkit().getImage(imageUrl);
   return getImageWidth(image);
   }
   */
  /**
   * Return the width (horizontal size) of the given GIF or JPEG image (in device coordinates).
   * Return 0, if image is invalid.
   */
  /*
   public int getImageWidth(Image image)
   {
   MediaTracker mt = new MediaTracker(_panel);
   mt.addImage(image, 1);
   try
   {
   mt.waitForAll();
   }
   catch (Exception e)
   {
   System.out.println("Exception while loading image.");
   }

   if (image == null || image.getWidth(_panel) <= 0)
   return 0;
   else
   return image.getWidth(_panel);
   }
   */
  /**
   * Return the height (vertical size) of the GIF or JPEG image from the
   * given path (in device coordinates).
   * Return 0, if image is invalid.
   */
  /*
   public int getImageHeight(String imagePath)
   {
   Image image = Toolkit.getDefaultToolkit().getImage(imagePath);
   return getImageHeight(image);
   }
   */
  /**
   * Return the height (vertical size) of the GIF or JPEG image from the
   * given URL (in device coordinates).
   * Return 0, if image is invalid.
   */
  /*
   public int getImageHeight(URL imageUrl)
   {
   Image image = Toolkit.getDefaultToolkit().getImage(imageUrl);
   return getImageHeight(image);
   }
   */
  /**
   * Return the height (vertical size) of the given GIF or JPEG image (in device coordinates).
   * Return 0, if image is invalid.
   */
  /*
   public int getImageHeight(Image image)
   {
   MediaTracker mt = new MediaTracker(_panel);
   mt.addImage(image, 1);
   try
   {
   mt.waitForAll();
   }
   catch (Exception e)
   {
   System.out.println("Exception while loading image.");
   }

   if (image == null || image.getHeight(_panel) <= 0)
   return 0;
   else
   return image.getHeight(_panel);
   }
   */
  /**
   * Return the width (horizontal size) of the last loaded GIF or JPEG image
   * (in device coordinates).
   * Return 0, if image is invalid.
   */
  /*
   public int getImageWidth()
   {
   return _imageWidth;
   }
   * */
  /**
   * Return the height (vertical size) of the last loaded GIF or JPEG image
   * (in device coordinates).
   * Return 0, if image is invalid.
   */
  /*
   public int getImageHeight()
   {
   return _imageHeight;
   }
   */
  /**
   * Return the property names of available system properties.
   * Return null, if no properties are available on the current platform.
   */
  public static String[] getDesktopProperties()
  {
    return (String[])Toolkit.getDefaultToolkit().
      getDesktopProperty("win.propNames");
  }

  /**
   * Disable/Enable the title bar's closing button.
   */
  public void disableClose(boolean b)
  {
    _doNothingOnClose = b;
  }

  /**
   * Copy the offscreen buffer to a store buffer.
   */
  public void storeGraphics()
  {
    _saveImage = new BufferedImage(_width, _height, BufferedImage.TYPE_INT_RGB);
    _saveGraphics = _saveImage.createGraphics();
    _saveGraphics.drawImage(_bi, null, 0, 0);
  }

  /**
   * Copy the store buffer to the offscreen buffer.
   */
  public void recallGraphics()
  {
    if (_saveGraphics != null)
      _offG2D.drawImage(_saveImage, null, 0, 0);
  }

  /**
   * Clear store buffer by uniformly painting it with given color.
   */
  public void clearStore(Color color)
  {
    _saveGraphics.setPaint(color);
    _saveGraphics.fill(new Rectangle.Double(0, 0, _width, _height));
  }

  /**
   * Transform offscreen buffer with given affine transformation.
   */
  public void transformGraphics(AffineTransform at)
  {
    // Create new backup buffer
    BufferedImage bi = new BufferedImage(_width, _height, BufferedImage.TYPE_INT_RGB);
    Graphics2D g2D = bi.createGraphics();

    // Clear buffer with background color
    g2D.clearRect(0, 0, _width, _height);
    Paint paint = _offG2D.getPaint();
    g2D.setPaint(_bgColor);
    g2D.fill(new Rectangle.Double(0, 0, _width, _height));
    g2D.setPaint(paint);

    // Copy transformed offscreen buffer in backup buffer
    g2D.drawImage(_bi, at, null);

    // Copy backup buffer to offscreen buffer
    _offG2D.drawImage(bi, null, null);

    // Dispose new buffer
    g2D.dispose();

    if (_doRepaint)
      _panel.repaint();
  }

  /**
   * Return the color of the screen pixel at given location.
   */
  public Color getPixelColor(double x, double y)
  {
    return new Color(_bi.getRGB(_toUserX(x), _toUserY(y)));
  }

  /**
   * Return true, if the window was disposed or released.
   */
  public boolean isDisposed()
  {
    return _isDisposed;
  }

  // --------------- Package access only ------------------------
  void setRepaint(boolean doRepaint)
  {
    _doRepaint = doRepaint;
  }

  boolean getRepaint()
  {
    return _doRepaint;
  }

  void setAdjust(boolean doAdjust)
  {
    _doAdjust = doAdjust;
  }

  void repaintArea(Shape shape)
  {
    if (_doRepaint)
    {

      Rectangle r = shape.getBounds();
      AffineTransform at = getAffineScale();
      int ulx = (int)(r.x * at.getScaleX());
      int uly = (int)(r.y * at.getScaleY());
      int width = (int)(r.width * at.getScaleX());
      int height = (int)(r.height * at.getScaleY());
      _panel.repaint(ulx - 1 - _lineWidth, uly - 1 - _lineWidth,
        width + 2 + 2 * _lineWidth, height + 2 + 2 * _lineWidth);
    }
  }

  void line(double x1, double y1, double x2, double y2)
  {
    BasicStroke stroke = new BasicStroke(_lineWidth);
    _offG2D.setStroke(stroke);
    _offG2D.setPaint(_color);
    int xStart = _toUserX(x1);
    int yStart = _toUserY(y1);
    int xEnd = _toUserX(x2);
    int yEnd = _toUserY(y2);
    Line2D line = new Line2D.Double(xStart, yStart, xEnd, yEnd);
    _offG2D.draw(line);
    _xCurrent = x2;
    _yCurrent = y2;

    repaintArea(line);
  }

  void draw(double x, double y)
  {
    line(_xCurrent, _yCurrent, x, y);
  }

  void pos(double x, double y)    // Cannot use move (used in java.awt)
  {
    _xCurrent = x;
    _yCurrent = y;
  }

  double getPosX()
  {
    return _xCurrent;
  }

  double getPosY()
  {
    return _yCurrent;
  }

  void setColor(Color color)
  {
    _color = color;
  }

  Color getColor()
  {
    return _color;
  }

  void setLineWidth(int lineWidth)
  {
    _lineWidth = lineWidth;
  }

  int getLineWidth()
  {
    return _lineWidth;
  }

  void drawCircle(double radius, boolean fill)
  {
    BasicStroke stroke = new BasicStroke(_lineWidth);
    _offG2D.setStroke(stroke);
    _offG2D.setPaint(_color);

    // adapt to horizontal to draw circle and not ellipse
    int rx = _toUserWidth(radius);
    int ulx = _toUserX(_xCurrent) - rx;
    int uly = _toUserY(_yCurrent) - rx;

    Ellipse2D.Double ellipse = new Ellipse2D.Double(ulx, uly, 2 * rx, 2 * rx);
    if (fill)
      _offG2D.fill(ellipse);
    else
      _offG2D.draw(ellipse);

    repaintArea(ellipse);
  }

  void drawEllipse(double a, double b, boolean fill)
  {
    BasicStroke stroke = new BasicStroke(_lineWidth);
    _offG2D.setStroke(stroke);
    _offG2D.setPaint(_color);

    // adapt to horizontal to draw circle and not ellipse
    int rx = _toUserWidth(a);
    int ry = _toUserWidth(b);
    int ulx = _toUserX(_xCurrent) - rx;
    int uly = _toUserY(_yCurrent) - ry;

    Ellipse2D.Double ellipse = new Ellipse2D.Double(ulx, uly, 2 * rx, 2 * ry);
    if (fill)
      _offG2D.fill(ellipse);
    else
      _offG2D.draw(ellipse);

    repaintArea(ellipse);
  }

  void drawRectangle(double width, double height, boolean fill)
  {
    BasicStroke stroke = new BasicStroke(_lineWidth);
    _offG2D.setStroke(stroke);
    _offG2D.setPaint(_color);

    int ulx;
    if (_xmin < _xmax)
      ulx = _toUserX(_xCurrent - width / 2);
    else
      ulx = _toUserX(_xCurrent + width / 2);
    int uly;
    if (_ymin < _ymax)
      uly = _toUserY(_yCurrent + height / 2);
    else
      uly = _toUserY(_yCurrent - height / 2);
    int w = _toUserWidth(width);
    int h = _toUserHeight(height);

    Rectangle.Double rectangle = new Rectangle.Double(ulx, uly, w, h);
    if (fill)
      _offG2D.fill(rectangle);
    else
      _offG2D.draw(rectangle);

    repaintArea(rectangle);
  }

  void drawArc(double radius, double startAngle, double extendAngle,
    boolean fill)
  {
    BasicStroke stroke = new BasicStroke(_lineWidth);
    _offG2D.setStroke(stroke);
    _offG2D.setPaint(_color);

    int ulx;
    if (_xmin < _xmax)
      ulx = _toUserX(_xCurrent - radius);
    else
      ulx = _toUserX(_xCurrent + radius);
    int uly;
    if (_ymin < _ymax)
      uly = _toUserY(_yCurrent + radius);
    else
      uly = _toUserY(_yCurrent - radius);

    int w = _toUserWidth(radius);
    int h = _toUserHeight(radius);
    Arc2D.Double arc
      = new Arc2D.Double(ulx, uly, 2 * w, 2 * h, startAngle, extendAngle,
        Arc2D.OPEN);
    if (fill)
      _offG2D.fill(arc);
    else
      _offG2D.draw(arc);

    repaintArea(arc);
  }

  void drawPolygon(double[] x, double[] y, int nbPoints, boolean fill)
  {
    BasicStroke stroke = new BasicStroke(_lineWidth);
    _offG2D.setStroke(stroke);
    _offG2D.setPaint(_color);

    int[] xUser = new int[nbPoints];
    int[] yUser = new int[nbPoints];
    for (int i = 0; i < nbPoints; i++)
    {
      xUser[i] = _toUserX(x[i]);
      yUser[i] = _toUserY(y[i]);
    }

    Polygon polygon = new Polygon(xUser, yUser, nbPoints);
    if (fill)
      _offG2D.fill(polygon);
    else
      _offG2D.draw(polygon);

    repaintArea(polygon);
  }

  void drawPolygon(Point2D.Double[] corner, boolean fill)
  {
    int nbCorner = corner.length;
    double[] x = new double[nbCorner];
    double[] y = new double[nbCorner];
    for (int i = 0; i < nbCorner; i++)
    {
      x[i] = corner[i].x;
      y[i] = corner[i].y;
    }
    drawPolygon(x, y, nbCorner, fill);
  }

  void drawGeneralPath(GeneralPath gp, boolean fill)
  {
    BasicStroke stroke = new BasicStroke(_lineWidth);
    _offG2D.setStroke(stroke);
    _offG2D.setPaint(_color);

    Shape sh = toUser(gp);
    if (fill)
      _offG2D.fill(sh);
    else
      _offG2D.draw(sh);

    repaintArea(sh);
  }

  void drawQuadraticBezier(Point2D.Double pt1,
    Point2D.Double ptc, Point2D.Double pt2)

  {
    BasicStroke stroke = new BasicStroke(_lineWidth);
    _offG2D.setStroke(stroke);
    _offG2D.setPaint(_color);

    QuadCurve2D.Double quadCurve;
    quadCurve
      = new QuadCurve2D.Double(
        _toUserX(pt1.x), _toUserY(pt1.y),
        _toUserX(ptc.x), _toUserY(ptc.y),
        _toUserX(pt2.x), _toUserY(pt2.y));
    _offG2D.draw(quadCurve);

    repaintArea(quadCurve);
  }

  void drawCubicBezier(Point2D.Double pt1, Point2D.Double ptc1,
    Point2D.Double ptc2, Point2D.Double pt2)
  {
    BasicStroke stroke = new BasicStroke(_lineWidth);
    _offG2D.setStroke(stroke);
    _offG2D.setPaint(_color);

    CubicCurve2D.Double cubicCurve;
    cubicCurve = new CubicCurve2D.Double(
      _toUserX(pt1.x), _toUserY(pt1.y),
      _toUserX(ptc1.x), _toUserY(ptc1.y),
      _toUserX(ptc2.x), _toUserY(ptc2.y),
      _toUserX(pt2.x), _toUserY(pt2.y));
    _offG2D.draw(cubicCurve);

    repaintArea(cubicCurve);
  }

  void drawString(String s)
  {
    _offG2D.setPaint(_color);
    _offG2D.drawString(s, _toUserX(_xCurrent), _toUserY(_yCurrent));

    if (_doRepaint)
      _panel.repaint();
  }

  void drawString(double x, double y, String s)
  {
    _offG2D.setPaint(_color);
    _offG2D.drawString(s, _toUserX(x), _toUserY(y));

    if (_doRepaint)
      _panel.repaint();
  }

  void drawString(double x, double y, String text, Font font,
    Color textColor, Color bgColor)
  {
    AttributedString as = new AttributedString(text);
    as.addAttribute(TextAttribute.FONT, font);
    as.addAttribute(TextAttribute.FOREGROUND, textColor);
    //  GradientPaint gp = new GradientPaint(0, 0, Color.black, 100, 100,
    //               bgColor, true);
    as.addAttribute(TextAttribute.BACKGROUND, bgColor);
    //  as.addAttribute(TextAttribute.BACKGROUND, gp); 
    _offG2D.drawString(as.getIterator(), _toUserX(x), _toUserY(y));

    if (_doRepaint)
      _panel.repaint();
  }

  static BufferedImage createNodeImage(String text, int size, int borderSize,
    Color borderColor, Color textColor, Color bgColor, Font font)
  {
    BufferedImage bi = // Dummy bi to get font and line metrics
      new BufferedImage(1, 1, Transparency.TRANSLUCENT);
    Graphics2D g = bi.createGraphics();
    FontRenderContext frc = g.getFontRenderContext();
    LineMetrics lm = font.getLineMetrics(text, frc);
    FontMetrics fm = g.getFontMetrics(font);
    int textHeight = (int)Math.ceil(lm.getHeight());
    int textWidth = 0;
    TextLayout textLayout = null;
    if (text != null && text.length() != 0)
    {
      textWidth = fm.charsWidth(text.toCharArray(), 0, text.length());
      textLayout = new TextLayout(text, font, frc);
    }
    else
      textWidth = 1;

    g.dispose();
//    System.out.println("textWidth, textHeight: " + textWidth + "," + textHeight);
    
    bi = new BufferedImage(size, size, Transparency.TRANSLUCENT);
    Graphics2D g2D = bi.createGraphics();
    // Transparent background of entire area
    g2D.setColor(
      new Color(bgColor.getRed(), bgColor.getGreen(), bgColor.getBlue(), 0));
    g2D.fillRect(0, 0, size, size);
    g2D.setColor(bgColor);
    int inset = 0;
    if (borderSize > 0)
      inset = borderSize - 1;
    g2D.fillOval(inset, inset, size - 2 * inset, size - 2 * inset);
    if (borderSize > 0)
    {  
      BasicStroke stroke = new BasicStroke(borderSize);
      g2D.setStroke(stroke);
      g2D.setColor(borderColor);
      inset = borderSize - 1;
      g2D.drawOval(inset, inset, size - 2 * inset, size - 2 * inset);
    }

    if (textLayout != null)
    {
      g2D.setColor(textColor);
      textLayout.draw(g2D, 
        size / 2 - textWidth / 2, 
        size / 2 + (int) (0.3 * textHeight));
    }
    return bi;
  }

  void drawPoint(double x, double y)
  // Point in Graphics2D with lineWidth 1 are not shown, we paint it in Graphics
  {
    int xPoint = _toUserX(x);
    int yPoint = _toUserY(y);
    if (_lineWidth > 1)
    {
      BasicStroke stroke = new BasicStroke(_lineWidth);
      _offG2D.setStroke(stroke);
      _offG2D.setPaint(_color);
      Line2D line = new Line2D.Double(xPoint, yPoint, xPoint, yPoint);
      _offG2D.draw(line);
    }
    else
    {
      Graphics g = (Graphics)_offG2D;
      g.setColor(_color);
      g.drawLine(xPoint, yPoint, xPoint, yPoint);
    }

    // Must map point to actual screen coordinates
    AffineTransform aft = getAffineScale();
    int xP = (int)(xPoint * aft.getScaleX());
    int yP = (int)(yPoint * aft.getScaleY());
    if (_doRepaint)
      _panel.repaint(xP - 1 - _lineWidth, yP - 1 - _lineWidth,
        2 + 2 * _lineWidth, 2 + 2 * _lineWidth);

    _xCurrent = x;
    _yCurrent = y;
  }

  /**
   * Draw the given BufferedImage into offscreen buffer at
   * (x, y) coordinates of upper left corner.
   * If lowerLeft = true,  coordinates of lower left corner.
   */
  public void drawImage(BufferedImage bi, int x, int y, boolean lowerLeft)
  {
    int imageHeight = bi.getHeight();
    AffineTransform at = new AffineTransform();
    if (lowerLeft)
      at.translate(x, y - imageHeight + 1);
    else
      at.translate(x, y);
    _offG2D.drawImage(bi, at, _panel);
  }

  boolean showImage(String imagePath, double x, double y)
  {
    BufferedImage bi = GPanel.getImage(imagePath);
    if (bi == null)
      return false;
    drawImage(bi, _toUserX(x), _toUserY(y), true);
    if (_doRepaint)
      _panel.repaint();
    return true;
  }

  void fill(Point pt, Color color, Color replacement)
  {
    BufferedImage bi = GBitmap.floodFill(_bi, pt, color, replacement);
    drawImage(bi, 0, 0, false);
    if (_doRepaint)
      _panel.repaint();
  }

  boolean showImage(BufferedImage bi, double x, double y)
  {
    drawImage(bi, _toUserX(x), _toUserY(y), true);
    if (_doRepaint)
      _panel.repaint();
    return true;
  }

  void shrink()
  // Reduce size of GPanel drawing area, because
  // border lines are not shown in equal thinkness
  {
    _reduceSize = 2;
    _emptyBorder = 1;
  }

  void setMode(int mode)
  {
    _panelMode = mode;
  }

  void setStatusDialog(ModelessOptionPane statusDialog)
  {
    this._statusDialog = statusDialog;
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
}
