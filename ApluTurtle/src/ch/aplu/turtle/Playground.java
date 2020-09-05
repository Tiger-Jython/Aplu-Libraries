// Playground.java

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

import java.util.*;
import javax.swing.*;
import java.awt.*;
import java.awt.print.*;
import java.awt.geom.*;
import java.awt.image.*;
import java.awt.font.*;
import java.io.*;
import javax.imageio.ImageIO;
import java.util.Random;

/**
 * A Playground is the turtle's home, i.e. where the turtle moves and draws traces.
 *
 * The<code>Playground</code> is responsible for interpreting angle and position of the
 * <code>Turtle</code> and for choosing the correct turtle image and putting it on the right
 * spot of the Playground. This means: e.g. whenever you wish to switch the x- and y-axis, you
 * should do it in this class, and not in the <code>Turtle</code> class.
 * <i>Remarks:</i>
 * <ul>
 * <li>The <code>Playground</code> needs a Window (e.g. <code>Frame</code>, <code>Applet</code>)
 * to be displayed. One possible solution for this problem is already given with the <code>TurtleFrame</code>
 * class, which implements the <code>TurtleContainer</code> interface. Applets are not yet supported.</li>
 * </ul>
 */
public class Playground
  extends JPanel implements Printable
{
  private final int defaultSize = 400;  // nbHorzPix = 401, nbVertPix = 401, 
  // coordinates -200..200
  // Holds the <code>Turtle</code>s of this Playground. 
  private Vector turtles;
  // Holds the offscreen buffer and graphics context
  // where Turtle traces are drawn.
  private BufferedImage traceBuffer = null;
  protected Graphics2D traceG2D = null;
  private Dimension pgBufferSize;
  private Dimension pgSize;
  protected boolean isBean = false;
  protected Color beanBkColor = DEFAULT_BACKGROUND_COLOR;
  // Holds the offscreen buffer and graphics context
  // of the Turtles images.
  private BufferedImage turtleBuffer = null;
  protected Graphics2D turtleG2D = null;
  // Flag to tell whether we have at least one Turtle shown.
  private boolean isTurtleVisible = false;
  // Flag to tell whether we use automatic repainting
  protected boolean isRepaintEnabled = true;
  // The default background color.
  protected static Color DEFAULT_BACKGROUND_COLOR = Color.white;
  private double printerScale = 1;  // Default printer scaling
  private TPrintable traceCanvas;   // Store ref to user class
  private Graphics2D printerG2D = null;
  private boolean isPrintScreen = false; // Indicate we are printing the playground
  private double printerScaleFactor = 1.1;  // Magnification factor for printer

  /**
   * Creates a Playground with standard size and default background color.
   * The number of pixels is (401 x 401) and determines the
   * span of turtle coordinates (-200..200)
   */
  public Playground()
  {
    init(new Dimension(defaultSize, defaultSize), DEFAULT_BACKGROUND_COLOR);
  }

  protected Playground(boolean isBean)
  {
    this.isBean = isBean;
    init(new Dimension(defaultSize, defaultSize), beanBkColor);
  }

  /**
   * Creates a Playground  with standard size and given background color.
   * The span of turtle coordinates is -200..200
   * and the window size in pixels is  401 x 401.
   */
  public Playground(Color bkColor)
  {
    init(new Dimension(defaultSize, defaultSize), bkColor);
  }

  /**
   * Creates a new Playground with given size.
   * size.width and size.height should be even positive integers.
   * The size determines the
   * span of turtle coordinates (-width/2 .. width/2, -height/2 .. height/2) and
   * and the window size in pixels is (m+1) x (n+1).
   */
  public Playground(Dimension size)
  {
    Dimension dim = new Dimension(size.width, size.height);
    init(dim, DEFAULT_BACKGROUND_COLOR);
  }

  /**
   * Creates a new Playground with given size and background color.
   * size.width and size.height should be even positive integers.
   * The size determines the
   * span of turtle coordinates (-width/2 .. width/2, -height/2 .. height/2) and
   * and the window size in pixels is (m+1) x (n+1).
   */
  public Playground(Dimension size, Color bkColor)
  {
    Dimension dim = new Dimension(size.width, size.height);
    init(dim, bkColor);
  }

  /**
   * Returns the size (defines coordinate span) of the playground. 
   * If the playground size is m x n, the coordinate span in -m/2..m/2, -n/2..n/2
   * and the window size in pixels is (m+1) x (n+1).
   */
  public Dimension getSize()
  {
    return pgSize;
  }

  /**
   * Returns the size of the graphics buffer that holds the 
   * drawing area
   * (number of pixels in horizontal direction and vertical direction). 
   * If the playground size is m x n, the coordinate span in -m/2..m/2, -n/2..n/2
   * and the buffer size is (m+1) x (n+1).
   */
  public Dimension getBufferSize()
  {
    return pgBufferSize;
  }

  /**
   * Initializes everything,
   * e.g. creates a new vector (which holds the
   * <code>Turtle</code>s), the offscreen buffers, and sets the size and
   * background color.
   * @see #DEFAULT_BACKGROUND_COLOR
   */
  protected void init(Dimension pgSize, Color bkColor)
  {
    // Playground size m x n > nbPixels (m + 1) x (n + 1)
    this.pgSize = pgSize;
    pgBufferSize = new Dimension(pgSize.width + 1, pgSize.height + 1);
    if (isBean)
    {
      turtles = new Vector();
      traceBuffer = new BufferedImage(
        pgBufferSize.width,
        pgBufferSize.height,
        BufferedImage.TYPE_INT_ARGB);
      traceG2D = traceBuffer.createGraphics();
      traceG2D.setColor(bkColor);
      traceG2D.fillRect(0, 0, pgBufferSize.width, pgBufferSize.height);
      traceG2D.setBackground(bkColor);
      setBackground(bkColor);
      setPreferredSize(pgBufferSize);
      // We don't need it, because we have our own double buffering
      // We don't see any difference in speed on PC or Mac
      // but maybe we avoid some memory waste
      setDoubleBuffered(false);
    }
    else
    {
      turtles = new Vector();
      traceBuffer = new BufferedImage(
        pgBufferSize.width,
        pgBufferSize.height,
        BufferedImage.TYPE_INT_ARGB);
      traceG2D = traceBuffer.createGraphics();
      traceG2D.setColor(bkColor);
      traceG2D.fillRect(0, 0, pgBufferSize.width, pgBufferSize.height);
      traceG2D.setBackground(bkColor);

      turtleBuffer = new BufferedImage(
        pgBufferSize.width,
        pgBufferSize.height,
        BufferedImage.TYPE_INT_ARGB);
      turtleG2D = turtleBuffer.createGraphics();
      turtleG2D.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
        RenderingHints.VALUE_ANTIALIAS_ON);
      setBackground(bkColor);
      setPreferredSize(pgBufferSize);
      // We don't need it, because we have our own double buffering
      // We don't see any difference in speed on PC or Mac
      // but maybe we avoid some memory waste
      setDoubleBuffered(false);
    }
  }

  /** 
   * Adds a new <code>Turtle</code> to the Playground.
   */
  public void add(Turtle turtle)
  {
    turtles.add(turtle);
    toTop(turtle);
  }

  /** 
   * Removes a <code>Turtle</code> from the Playground.
   */
  public void remove(Turtle turtle)
  {
    turtles.remove(turtle);
  }

  /** 
   * Tells current number of <code>Turtle</code>s in this Playground.
   */
  public int countTurtles()
  {
    return turtles.size();
  }

  /** 
   * Returns the <code>Turtle</code> at index <code>index</code>.
   */
  public Turtle getTurtle(int index)
  {
    return (Turtle)turtles.elementAt(index);
  }

  /** 
   * Moves the given <code>Turtle</code> above all the others, then
   * paints all turtles.
   * @see #toTop
   */
  public void paintTurtles(Turtle turtle)
  {
    toTop(turtle);
    paintTurtles();
  }

  /** 
   * Paints all turtles (calling paintComponent())
   */
  public void paintTurtles()
  {
    isTurtleVisible = false;
    for (int i = 0; i < countTurtles(); i++)
    {
      Turtle aTurtle = getTurtle(i);
      if (!aTurtle.isHidden())
      {
        paintTurtle(aTurtle);
      }
    }

    // This is the main repaint call, when the turtle is
    // moving (even when all turtles are hidden).
    // Strange behaviour an slow Mac machines (pre J2SE 1.4 version):
    // It happens that some turtle images are not completely redrawn.
    // This is probably due to an improper handling of fast multiple repaint requests.
    // Workaround: we wait a small amount of time (and give the thread away)
    // (No visible slow down on new machines.)
    if (printerG2D == null && isRepaintEnabled)
      repaint();

    if (isTurtleVisible)
    {
      try
      {
        Thread.currentThread().sleep(10);
      }
      catch (Exception e)
      {
      }
    }
  }

  /** 
   * Paints the given <code>Turtle</code>.
   */
  public void paintTurtle(Turtle turtle)
  {
    if (turtleBuffer == null)
    {
      turtleBuffer = new BufferedImage(getWidth(),
        getHeight(),
        BufferedImage.TYPE_INT_ARGB);
      turtleG2D = turtleBuffer.createGraphics();
    }
    Graphics2D turtleGraphics = getTurtleG2D();
    TurtleRenderer renderer = turtle.getTurtleRenderer();
    if (renderer != null)  // May happen in mult-threading apps
    {
      turtle.getTurtleRenderer().paint(turtle._getX(),
        turtle._getY(),
        turtleGraphics);
    }
    isTurtleVisible = true;
  }

  /** 
   * Shows a clone of the given turtle. If color != null and
   * the turtle has no custom image, the clone takes the given color.
   */
  protected void stampTurtle(Turtle turtle, Color color)
  {
    Turtle t = (Turtle)turtle.clone();
    if (color != null && turtle.turtleImg == null)
      t.setColor(color);
    isTurtleVisible = true;
    if (printerG2D == null && isRepaintEnabled)
      repaint();
  }

  protected void spray(int density, double spread, int size, Turtle turtle)
  {
    Color oldColor = traceG2D.getColor();
    traceG2D.setColor(turtle.getPenColor());
    float oldLineWidth = turtle.getPen().getLineWidth();
    turtle.getPen().setLineWidth(1);
    traceG2D.setStroke(turtle.getPen().getStroke());
    Point2D.Double pt = toScreenCoords(turtle.getPos());
    int ix = (int)Math.round(pt.x);
    int iy = (int)Math.round(pt.y);

    Random rnd = new Random();
    if (size == 1)
    {
      Graphics g = (Graphics)traceG2D;
      for (int i = 0; i < density; i++)
      {
        int rx = ix + (int)(spread * rnd.nextGaussian());
        int ry = iy + (int)(spread * rnd.nextGaussian());
        g.drawLine(rx, ry, rx, ry);
        if (printerG2D != null)
        {
          g = (Graphics)printerG2D;
          g.drawLine(rx, ry, rx, ry);
        }
      }
    }
    else
    {
      for (int i = 0; i < density; i++)
      {
        int rx = ix - size / 2 + (int)(spread * rnd.nextGaussian());
        int ry = iy - size / 2 + (int)(spread * rnd.nextGaussian());
        traceG2D.fillOval(rx, ry, size, size);
        if (printerG2D != null)
          printerG2D.fillOval(rx, ry, size, size);
      }
    }
    traceG2D.setColor(oldColor);
    turtle.getPen().setLineWidth(oldLineWidth);
    traceG2D.setStroke(turtle.getPen().getStroke());
    if (printerG2D == null && isRepaintEnabled)
      repaint();;
  }

  /** 
   * Draws a circle at the position of the given turtle using the given
   * diameter.
   */
  protected void dot(double diameter, boolean fill, Turtle turtle)
  {
    int d = (int)Math.round(diameter);
    Color oldColor = traceG2D.getColor();
    traceG2D.setColor(turtle.getPenColor());
    float oldLineWidth = turtle.getPen().getLineWidth();
    turtle.getPen().setLineWidth(1);
    traceG2D.setStroke(turtle.getPen().getStroke());
    Point2D.Double pt = toScreenCoords(turtle.getPos());

    if (d > 1)
    {
      int ix = (int)Math.round(pt.x) - d / 2;
      int iy = (int)Math.round(pt.y) - d / 2;
      if (fill)
        traceG2D.fillOval(ix, iy, d, d);
      else
        traceG2D.drawOval(ix, iy, d, d);

      if (printerG2D != null)
      {
        if (fill)
          printerG2D.fillOval(ix, iy, d, d);
        else
          printerG2D.drawOval(ix, iy, d, d);
      }
    }
    else // d = 1 -> draw one pixel, must use Graphics, not Graphics2D
    {
      int ix = (int)Math.round(pt.x);
      int iy = (int)Math.round(pt.y);
      Graphics g = (Graphics)traceG2D;
      g.drawLine(ix, iy, ix, iy);
      if (printerG2D != null)
      {
        g = (Graphics)printerG2D;
        g.drawLine(ix, iy, ix, iy);
      }
    }

    traceG2D.setColor(oldColor);
    turtle.getPen().setLineWidth(oldLineWidth);
    traceG2D.setStroke(turtle.getPen().getStroke());
    if (printerG2D == null && isRepaintEnabled)
      repaint();
  }

  /**
   * Draws the given image into background. x,y is the center of the image
   * in turtle coordinates.
   * Retrieves the image either from the jar resource, from local drive or
   * from a internet server
   * From the given filename the image file is searched in the following order:<br>
   * - if application is packed into a jar archive, relative to the root of the jar archive<br>
   * - relative to the directory &lt;userhome&gt;/gamegrid/<br>
   * - relative or absolute to current application directory<br>
   * - if filename starts with http://, from the given URL<br>
   * - add prefix _ and search relative to the root of the jar archive
   * @return true, if successful: otherwise false
   */
  public boolean drawImage(String imagePath, double x, double y)
  {
    BufferedImage bi = Turtle.getImage(imagePath);
    if (bi == null)
      return false;
    if (traceBuffer == null)
    {
      traceBuffer = new BufferedImage(getWidth(),
        getHeight(),
        BufferedImage.TYPE_INT_ARGB);
      traceG2D = traceBuffer.createGraphics();
    }
    Point2D.Double pt = toScreenCoords(x, y);
    int ix = (int)Math.round(pt.x);
    int iy = (int)Math.round(pt.y);
    int imageWidth = bi.getWidth();
    int imageHeight = bi.getHeight();
    AffineTransform at = new AffineTransform();
    at.translate(ix - imageWidth / 2, iy - imageHeight / 2);
    traceG2D.drawImage(bi, at, null);
    if (printerG2D != null)
      printerG2D.drawImage(bi, at, null);
    if (printerG2D == null && isRepaintEnabled)
      repaint();
    return true;
  }

  /**
   * Draws the given buffered image into background. x,y is the center of the image
   * in turtle coordinates.
   * @return true, if successful: otherwise false
   */
  public boolean drawImage(BufferedImage bi, double x, double y)
  {
    if (bi == null)
      return false;
    if (traceBuffer == null)
    {
      traceBuffer = new BufferedImage(getWidth(),
        getHeight(),
        BufferedImage.TYPE_INT_ARGB);
      traceG2D = traceBuffer.createGraphics();
    }
    Point2D.Double pt = toScreenCoords(x, y);
    int ix = (int)Math.round(pt.x);
    int iy = (int)Math.round(pt.y);
    int imageWidth = bi.getWidth();
    int imageHeight = bi.getHeight();
    AffineTransform at = new AffineTransform();
    at.translate(ix - imageWidth / 2, iy - imageHeight / 2);
    traceG2D.drawImage(bi, at, null);
    if (printerG2D != null)
      printerG2D.drawImage(bi, at, null);
    if (printerG2D == null && isRepaintEnabled)
      repaint();
    return true;
  }

  /**
   * Draw an arc with center at the current graph position
   * and given radius in window coordinates.
   * Start angle and extend angle in degrees (zero to north, positive counterclockwise).
   * The graph position is unchanged.
   */
  protected void arc(double radius, double startAngle,
    double extendAngle, boolean fill, Turtle turtle, int type)
  {
    Color oldColor = traceG2D.getColor();
    traceG2D.setColor(turtle.getPenColor());
    float oldLineWidth = turtle.getPen().getLineWidth();
    turtle.getPen().setLineWidth(1);
    traceG2D.setStroke(turtle.getPen().getStroke());
    int r = (int)Math.round(radius);
    Point2D.Double pt = toScreenCoords(turtle.getPos());
    int ix = (int)Math.round(pt.x) - r;
    int iy = (int)Math.round(pt.y) - r;

    if (traceBuffer == null)
    {
      traceBuffer = new BufferedImage(getWidth(),
        getHeight(),
        BufferedImage.TYPE_INT_ARGB);
      traceG2D = traceBuffer.createGraphics();
    }

    Arc2D.Double arc
      = new Arc2D.Double(ix, iy, 2 * r, 2 * r, 90 - startAngle, -extendAngle, type);
    if (fill)
      traceG2D.fill(arc);
    else
      traceG2D.draw(arc);
    if (printerG2D != null)
      if (fill)
        printerG2D.fill(arc);
      else
        printerG2D.draw(arc);

    traceG2D.setColor(oldColor);
    turtle.getPen().setLineWidth(oldLineWidth);
    traceG2D.setStroke(turtle.getPen().getStroke());
    if (printerG2D == null && isRepaintEnabled)
      repaint();
  }

  /** 
   * Draws a line from the point <code>(x0, y0)</code> to <code>(x1, y1)</code>
   * with the color and width of the given <code>Pen</code>.
   */
  public void drawLine(double x0, double y0, double x1, double y1, Pen pen)
  {
    Point2D.Double pt1 = toScreenCoords(x0, y0);
    Point2D.Double pt2 = toScreenCoords(x1, y1);
    lineTo(pt1.x, pt1.y, pt2.x, pt2.y, pen);
    if (printerG2D == null && isRepaintEnabled)
      repaint();
  }

  /** 
   * Draws a line from the point <code>(x0, y0)</code> to <code>(x1, y1)</code>
   * with the color and width of the given <code>Pen</code>.
   */
  protected void lineTo(double x0, double y0, double x1, double y1, Pen pen)
  {
    int ix0 = (int)Math.round(x0);
    int iy0 = (int)Math.round(y0);
    int ix1 = (int)Math.round(x1);
    int iy1 = (int)Math.round(y1);
    Color color = pen.getColor();

    if (traceBuffer == null)
    {
      traceBuffer = new BufferedImage(getWidth(),
        getHeight(),
        BufferedImage.TYPE_INT_ARGB);
      traceG2D = traceBuffer.createGraphics();
    }
    traceG2D.setColor(color);
    traceG2D.setStroke(pen.getStroke());
    traceG2D.drawLine(ix0, iy0, ix1, iy1);
    if (printerG2D != null)
    {
      printerG2D.drawLine(ix0, iy0, ix1, iy1);
    }
  }

  // A class for convenience.
  protected class Point extends java.awt.Point
  {
    Point(int x, int y)
    {
      super(x, y);
    }

    Point()
    {
      super();
    }

    Point(Point p)
    {
      super(p.x, p.y);
    }

    // Get a new Point with coordinates (this.x+p.x, this.y+p.y).
    protected Point add(Point p)
    {
      return new Point(this.x + p.x, this.y + p.y);
    }

    // Translate by the amounts dx = p.x, dy = p.y. 
    protected void translate(Point p)
    {
      translate(p.x, p.y);
    }

    public String toString()
    {
      return "(" + x + "," + y + ")";
    }
  }

  /** 
   * Fills a region with the <code>Turtle</code>s fill color (flood fill).
   * The region is defined by the <code>Turtle</code>s actual position and
   * is bounded by any other color than the pixel color at the turtle position.
   * If this pixel color is the same as the fill color, nothing is done. If the current 
   * turtle position is outside the playground, nothing happens.
   */
  public void fill(Turtle t)
  {
    if (!t.isInPlayground())
      return;

    final Point[] diff =
    {
      new Point(0, -1),
      new Point(-1, 0),
      new Point(1, 0),
      new Point(0, 1)
    };
    final int N = 0;
    final int W = 1;
    final int E = 2;
    final int S = 3;

    int fillColor = t.getPen().getFillColor().getRGB();
    Vector list = new Vector();
    Point2D.Double p1 = toScreenCoords(t.getPos());
    int _startX = (int)Math.round(p1.getX());
    int _startY = (int)Math.round(p1.getY());
    int bgColor;

    int startX = _startX;
    int startY = _startY;

    bgColor = traceBuffer.getRGB(startX, startY);

    // If current background color same as fill color, vary 1 pixel to neighbor points
    if (bgColor == fillColor)
    {
      int n = 0;
      while (n < 8)
      {
        if (n == 0)
        {
          startX = _startX;
          startY = _startY - 1;
        }
        if (n == 1)
        {
          startX = _startX + 1;
          startY = _startY - 1;
        }
        if (n == 2)
        {
          startX = _startX + 1;
          startY = _startY;
        }
        if (n == 3)
        {
          startX = _startX + 1;
          startY = _startY + 1;
        }
        if (n == 4)
        {
          startX = _startX;
          startY = _startY + 1;
        }
        if (n == 5)
        {
          startX = _startX - 1;
          startY = _startY + 1;
        }
        if (n == 6)
        {
          startX = _startX - 1;
          startY = _startY;
        }
        if (n == 7)
        {
          startX = _startX - 1;
          startY = _startY - 1;
        }
        bgColor = traceBuffer.getRGB(startX, startY);
        if (bgColor != fillColor)
          break;
        n++;
      }
      if (n == 8)
        return;
    }

    traceBuffer.setRGB(startX, startY, fillColor);
    Point p = new Point(startX, startY);
    list.addElement(new Point(startX, startY));
    int d = N;
    int back;
    while (list.size() > 0)
    {
      while (d <= S)
      { // forward
        Point tmp = p.add(diff[d]);
        try
        {
          if (traceBuffer.getRGB(tmp.x, tmp.y) == bgColor)
          {
            p.translate(diff[d]);
            traceBuffer.setRGB(p.x, p.y, fillColor);
            if (printerG2D != null)
            {
              printerG2D.setColor(t.getPen().getFillColor());
//                 printerG2D.drawLine(p.x,p.y, p.x, p.y);
              BasicStroke stroke = new BasicStroke(2);
              printerG2D.setStroke(stroke);
              Line2D line = new Line2D.Double(p.x, p.y, p.x, p.y);
              printerG2D.draw(line);
            }
            list.addElement(new Integer(d));
            d = N;
          }
          else
          {
            d++;
          }
        }
        catch (ArrayIndexOutOfBoundsException e)
        {
          d++;
        }
      }
      Object obj = list.remove(list.size() - 1);
      try
      {
        d = ((Integer)obj).intValue(); // last element
        back = S - d;
        p.translate(diff[back]);
      }
      catch (ClassCastException e)
      {
        // the first (zeroest) element in list is the start-point
        // just do nothing with it
      }
    }
//    traceG2D.drawLine(0, 0, 0, 0); // Workaround because on Mac the trace buffer is not drawn without this
    // Removed because a visible point will be drawn at (0,0). Test on Mac is OK (June 2016).
    if (printerG2D == null && isRepaintEnabled)
      repaint();
  }

  /**
   * Clears the playground with given color. All traces are erased and
   * all turtles are hidden, but remain where they are.
   */
  public void clear(Color color)
  {
    setBkColor(color);
    isTurtleVisible = true;
    for (int i = 0; i < turtles.size(); i++)
      ((Turtle)(turtles.get(i))).hideTurtle();
    if (printerG2D == null && isRepaintEnabled)
      repaint();
  }

  /**
   * Same as clear(Color color) with the current background color.
   */
  public void clear()
  {
    clear(getBackground());
  }

  /** 
   * Cleans the traces using the given color.
   * All turtles stay how and where they are, only lines, text and stamps will be removed.
   */
  public void clean(Color color)
  {
    setBkColor(color);
    isTurtleVisible = true;
    if (printerG2D == null && isRepaintEnabled)
      repaint();
  }

  /**
   * Same as clean(Color color) with the current background color.
   */
  public void clean()
  {
    clean(getBackground());
  }

  /**
   * Sets the background color of the playground. All turtle traces are erase,
   * but the turtles remains where they are.
   */
  public void setBkColor(Color color)
  {
    traceG2D.setColor(color);
    traceG2D.fillRect(0, 0, getWidth(), getHeight());
    traceG2D.setBackground(color);
  }

  /**
   * Draws the given image into the background (trace buffer). The image
   * is loaded with Turtle.getImage(). If the image can't be loaded,
   * nothing happens.
   * @param imagePath the file name or url
   */
  public void setBkImage(String imagePath)
  {
    traceG2D.drawImage(Turtle.getImage(imagePath), 0, 0, null);
  }

  /** 
   * Paints the playground.
   */
  public void paintComponent()
  {
    paintComponent(getGraphics());
  }

  /** 
   * Draws the trace and turtle buffers.
   */
  public void paintComponent(Graphics g)
  {
    super.paintComponent(g);
    Graphics2D g2D = (Graphics2D)g;
    if (isBean)
    {
      int width = getSize().width;
      int height = getSize().height;
      traceG2D.setColor(beanBkColor);
      traceG2D.fillRect(0, 0, width, height);
      setBackground(beanBkColor);
//       setPreferredSize(getSize());

      if (Math.max(Math.max(beanBkColor.getRed(), beanBkColor.getGreen()),
        beanBkColor.getBlue()) < 128)
        traceG2D.setColor(Color.white);
      else
        traceG2D.setColor(Color.black);

      traceG2D.drawString("TurtlePane", 10, 10);
      traceG2D.drawString("Size: (" + width + ", " + height + ")", 10, 20);
      traceG2D.drawString("BackgroundColor:", 10, 30);
      traceG2D.drawString("[" + beanBkColor.getRed()
        + "," + beanBkColor.getGreen()
        + "," + beanBkColor.getBlue() + "]",
        10, 40);
    }

    g2D.drawImage(traceBuffer, 0, 0, this);
    if (isTurtleVisible)
      g2D.drawImage(turtleBuffer, 0, 0, this);
  }

  /** 
   * Remove all turtles from the turtle buffer.
   */
  public void clearTurtles()
  {
    for (int i = 0; i < countTurtles(); i++)
    {
      Turtle turtle = getTurtle(i);
      clearTurtle(turtle);
    }
  }

  /** 
   * Removes the given turtle from the turtle buffer.
   * Override this method if you have added a new behaviour (like
   * wrap or clip) to the turtle.
   */
  public void clearTurtle(Turtle turtle)
  {
    if (turtle != null)
    {
      if (!turtle.isHidden())
      {
        if (turtle.isClip())
        {
          clearClipTurtle(turtle);
        }
        else if (turtle.isWrap())
        {
          clearWrapTurtle(turtle);
        }
      }
    }
  }

  //This method is called when the given <code>Turtle</code> is in wrap mode.
  protected void clearWrapTurtle(Turtle turtle)
  {
    clearWrapTurtle(turtle, turtleBuffer);
  }

  // Here the actual clearing of a <code>Turtle</code> in wrap mode from the
  // given image is performed.
  protected void clearWrapTurtle(Turtle turtle, Image im)
  {
    Rectangle bounds = getBounds(turtle);
    int pWidth = getWidth();
    int pHeight = getHeight();
    int x = bounds.x;
    int y = bounds.y;
    while (x > pWidth)
    {
      x -= pWidth;
    }
    while (x < 0)
    {
      x += pWidth;
    }
    while (y > pHeight)
    {
      y -= pHeight;
    }
    while (y < 0)
    {
      y += pHeight;
    }
    x = x % pWidth;
    y = y % pHeight;
    toAlphaNull(im, new Rectangle(x, y, bounds.width, bounds.height)); // OK
    boolean right = (x + bounds.width > getWidth());
    boolean bottom = (y + bounds.height > getHeight());
    if (right)
    {
      toAlphaNull(im, new Rectangle(x - pWidth, y, bounds.width, bounds.height));
    }
    if (bottom)
    {
      toAlphaNull(im, new Rectangle(x, y - pHeight, bounds.width, bounds.height));
    }
    if (right && bottom)
    {
      toAlphaNull(im, new Rectangle(x - pWidth, y - pHeight, bounds.width, bounds.height));
    }
  }

  // Copies and translates a given Rectangle.
  private Rectangle copyAndTranslate(Rectangle rect, int dx, int dy)
  {
    return new Rectangle(rect.x + dx, rect.y + dy,
      rect.width, rect.height);
  }

  // This method is called when the given <code>Turtle</code> is in clip mode.
  protected void clearClipTurtle(Turtle turtle)
  {
    clearClipTurtle(turtle, turtleBuffer);
  }

  // Here the actual clearing of a <code>Turtle</code> in clip mode from the
  protected void clearClipTurtle(Turtle turtle, Image im)
  {
    Rectangle bounds = getBounds(turtle);
    toAlphaNull(im, bounds);
  }

  // Sets the alpha channel of all pixels in the given image
  // in the given Rectangle to zero (i.e. totally transparent).
  // This method is used byte the clearXXXTurtle methods.
  private void toAlphaNull(Image im, Rectangle rect)
  {
    Rectangle rim = new Rectangle(0, 0,
      im.getWidth(this),
      im.getHeight(this));
    Rectangle r = new Rectangle();
    if (rect.intersects(rim))
    {
      r = rect.intersection(rim);
    }
    float[] alphachannel = new float[r.width * r.height];
    ((BufferedImage)im).getAlphaRaster().setPixels(r.x,
      r.y,
      r.width,
      r.height,
      alphachannel);
  }

  /** 
   * Puts a Turtle above all others.
   */
  public Turtle toTop(Turtle turtle)
  {
    if (turtles.removeElement(turtle))
    {
      turtles.add(turtle);
    }
    return turtle;
  }

  /** 
   * Puts a Turtle below all others.
   */
  public Turtle toBottom(Turtle turtle)
  {
    if (turtles.removeElement(turtle))
    {
      turtles.add(0, turtle);
    }
    return turtle;
  }

  /** 
   * Calculates the screen coordinates of the given point.
   */
  public Point2D.Double toScreenCoords(Point2D.Double p)
  {
    return internalToScreenCoords(p.x, p.y);
  }

  /** 
   * Calculates the screen coordinates of the given point coordinates.
   */
  public Point2D.Double toScreenCoords(double x, double y)
  {
    return internalToScreenCoords(x, y);
  }

  protected Point2D.Double internalToScreenCoords(double x, double y)
  {
    // reflect at x-axis, then translate to center of Playground
    // pixel coordinates coorespond to turtle coordinates, only translation needed
    double newX = getWidth() / 2 + x;
    double newY = getHeight() / 2 - y;
    return new Point2D.Double(newX, newY);
  }

  /** 
   * Calculates the turtle coordinates of the given screen coordinates.
   */
  public Point2D.Double toTurtleCoords(double x, double y)
  {
    // pixel coordinates coorespond to turtle coordinates, only translation needed
    double newX = x - getWidth() / 2;
    double newY = getHeight() / 2 - y;
    return new Point2D.Double(newX, newY);
  }

  /** 
   * Calculates the turtle coordinates of the given screen point.
   */
  public Point2D.Double toTurtleCoords(Point2D.Double p)
  {
    return toTurtleCoords(p.x, p.y);
  }

  /** 
   * Calculates the screen angle.
   * @param radians The angle in radians.
   */
  double toScreenAngle(double radians)
  {
    double sa = radians;
    if (sa < Math.PI / 2)
    {
      sa += 2 * Math.PI;
    }
    sa -= Math.PI / 2;
    if (sa != 0)
    {
      sa = Math.PI * 2 - sa;
    }
    return sa;
  }

  // Calculates the bounds of the <code>Turtle</code>s picture on the screen.
  protected Rectangle getBounds(Turtle turtle)
  {
    Rectangle bounds = turtle.getBounds();
    Point2D.Double tmp = toScreenCoords(new Point2D.Double(bounds.getX(), bounds.getY()));
    bounds.setRect(tmp.x - 2, tmp.y - 2, bounds.width + 4, bounds.height + 4);
    return bounds;
  }

  /** 
   * Returns the graphics context of the turtle buffer.
   */
  public Graphics2D getTurtleG2D()
  {
    return turtleG2D;
  }

  /** 
   * Returns the image of the turtle buffer.
   */
  public BufferedImage getTurtleBuffer()
  {
    return turtleBuffer;
  }

  /**  
   * Returns the graphics context of the trace buffer.
   */
  public Graphics2D getTraceG2D()
  {
    return traceG2D;
  }

  /**  
   * Returns the graphics context of the printer.
   */
  public Graphics2D getPrinterG2D()
  {
    return printerG2D;
  }

  /** 
   * Returns the image of the trace buffer.
   */
  public BufferedImage getTraceBuffer()
  {
    return traceBuffer;
  }

  /** 
   Same as label(String text, Turtle t, char align) with align = 'l'.
   */
  public void label(String text, Turtle t)
  {
    label(text, t, 'l');
  }  
    
  /** 
   * Draws the <code>text</code> at the current position of the Turtle <code>t</code>.
   * Drawing a text at some coordinates <code>(x,y)</code> we mean that the bottom left corner of
   * the text will be at these coordinates.
   * Font and colour are specified by the Turtle's Pen.
   * Text alignment: align = 'l' left, 'c': center, 'r': right 
   */
  public void label(String text, Turtle t, char align)
  {
    int textWidth = Pen.getTextInfo(text, t.getFont()).width;
    Point2D.Double sc = toScreenCoords(t.getPos());
    int x = (int)Math.round(sc.x);
    int y = (int)Math.round(sc.y);
    FontRenderContext frc = traceG2D.getFontRenderContext();
    Font f = t.getFont();
    TextLayout tl = new TextLayout(text, f, frc);
    traceG2D.setColor(t.getPen().getColor());
    if (Character.toLowerCase(align) == 'r')
      x = x - textWidth;
    else if (Character.toLowerCase(align) == 'c')
      x = x - textWidth / 2;
    tl.draw(traceG2D, x, y);
    if (printerG2D != null)
    {
      printerG2D.setColor(t.getPen().getColor());
      tl.draw(printerG2D, x, y);
    }

    if (printerG2D == null && isRepaintEnabled)
      repaint();
  }

  /**  
   * Sets antialiasing on or off for the turtle trace buffer
   * This may result in an better trace quality.
   */
  public void setAntiAliasing(boolean on)
  {
    if (on)
      traceG2D.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
        RenderingHints.VALUE_ANTIALIAS_ON);
    else
      traceG2D.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
        RenderingHints.VALUE_ANTIALIAS_OFF);
  }

  /**
   * Sets the given TPrintable (implementing draw()),
   * open a printer dialog and start printing with given scale.
   * Return false, if printer dialog is aborted,
   * otherwise return true.<br>
   * If tp ==  null, the current playground is printed.
   */
  protected boolean print(TPrintable tp, double scale)
  {
    if (tp == null)
      isPrintScreen = true;
    else
      isPrintScreen = false;
    printerScale = scale;
    PrinterJob pj = PrinterJob.getPrinterJob();
    pj.setPrintable(this);
    traceCanvas = tp;
    if (pj.printDialog())
    {
      try
      {
        pj.print();
      }
      catch (PrinterException ex)
      {
        System.out.println(ex);
      }
      return true;
    }
    else
      return false;
  }

  /**
   * For internal use only. Implementation of Printable.
   * (Callback method called by printing system.)
   */
  public int print(Graphics g, PageFormat pf, int pageIndex)
  {
    if (pageIndex != 0)
      return NO_SUCH_PAGE;
    Graphics2D g2D = (Graphics2D)g;
    double xZero = pf.getImageableX();
    double yZero = pf.getImageableY();

    printerG2D = g2D;  // Indicate also, we are printing now

    // Needed for fill operations: the trace canvas must be empty in order to
    // perform the fill algoritm (searching for outline of figure)
    if (!isPrintScreen)
      clean();

    g2D.scale(printerScaleFactor * printerScale, printerScaleFactor * printerScale);
    g2D.translate(xZero / printerScale, yZero / printerScale);

    if (isPrintScreen)
    {
      print(g);
    }
    else  // Printing the traceCanvas
    {
      // Hide all turtles
      boolean[] turtleState = new boolean[countTurtles()];
      for (int i = 0; i < countTurtles(); i++)
      {
        Turtle aTurtle = getTurtle(i);
        turtleState[i] = aTurtle.isHidden();
        aTurtle.ht();
      }
      traceCanvas.draw();

      // Restore old context
      for (int i = 0; i < countTurtles(); i++)
      {
        Turtle aTurtle = getTurtle(i);
        if (!turtleState[i])
          aTurtle.st();
      }
    }

    printerG2D = null;
    return PAGE_EXISTS;
  }

  /** 
   * Returns the color of the pixel at the current turtle position.
   * Returns null, if the turtle is outside the playground.
   */
  public Color getPixelColor(Turtle t)
  {
    Point2D.Double p1 = toScreenCoords(t.getPos());
    int x = (int)Math.round(p1.getX());
    int y = (int)Math.round(p1.getY());
    int c = 0;
    try
    {
      c = traceBuffer.getRGB(x, y);
    }
    catch (ArrayIndexOutOfBoundsException ex)  // Turtle out of playground
    {
      return null;
    }
    return new Color(c);
  }

  /**
   * Enables/disables automatic screen rendering.
   */
  public void enableRepaint(boolean b)
  {
    isRepaintEnabled = b;
  }

  protected void areaFill(Turtle t, Point2D.Double ptStart, Point2D.Double ptEnd)
  {
    Polygon p = new Polygon();
    Point2D.Double p0;
    switch (t.fillMode)
    {
      case FILL_POINT:
        p0 = toScreenCoords(t.xFillAnchor, t.yFillAnchor);
        p.addPoint((int)Math.round(p0.x), (int)Math.round(p0.y));
        p.addPoint((int)Math.round(ptStart.x), (int)Math.round(ptStart.y));
        p.addPoint((int)Math.round(ptEnd.x), (int)Math.round(ptEnd.y));
        traceG2D.fillPolygon(p);
        break;

      case FILL_HORZ:
        p0 = toScreenCoords(0, t.yFillLine);
        int yLine = (int)Math.round(p0.y);
        p.addPoint((int)Math.round(ptStart.x), (int)Math.round(ptStart.y));
        p.addPoint((int)Math.round(ptEnd.x), (int)Math.round(ptEnd.y));
        p.addPoint((int)Math.round(ptEnd.x), yLine);
        p.addPoint((int)Math.round(ptStart.x), yLine);
        traceG2D.fillPolygon(p);
        break;

      case FILL_VERT:
        p0 = toScreenCoords(t.xFillLine, 0);
        int xLine = (int)Math.round(p0.x);
        p.addPoint((int)Math.round(ptStart.x), (int)Math.round(ptStart.y));
        p.addPoint((int)Math.round(ptEnd.x), (int)Math.round(ptEnd.y));
        p.addPoint(xLine, (int)Math.round(ptEnd.y));
        p.addPoint(xLine, (int)Math.round(ptStart.y));
        traceG2D.fillPolygon(p);
        break;
    }
  }

  protected void fillPath(GeneralPath gp, Turtle turtle)
  {
    Color oldColor = traceG2D.getColor();
    traceG2D.setColor(turtle.getFillColor());
    float oldLineWidth = turtle.getPen().getLineWidth();
    turtle.getPen().setLineWidth(1);
    traceG2D.setStroke(turtle.getPen().getStroke());
    gp.closePath();
    traceG2D.fill(gp);
    traceG2D.setColor(oldColor);
    traceG2D.draw(gp);  // Draw outline again
    turtle.getPen().setLineWidth(oldLineWidth);
    traceG2D.setStroke(turtle.getPen().getStroke());
    if (printerG2D == null && isRepaintEnabled)
      repaint();
  }

  /**
   * Saves the playground (turtles and traces) in an image file.
   * @param fileName the image file path
   * @param formatName the image format (supported values: "PNG", "GIF")
   * @return true, if the operation is successful; otherwise false
   */
  public boolean save(String fileName, String formatName)
  {
    if (!(formatName.toLowerCase().equals("png") || formatName.toLowerCase().equals("gif")))
      return false;
    int w = traceBuffer.getWidth();
    int h = traceBuffer.getHeight();
    BufferedImage combined = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);

    // paint both images, preserving the alpha channels
    Graphics g = combined.getGraphics();
    g.drawImage(traceBuffer, 0, 0, null);
    g.drawImage(turtleBuffer, 0, 0, null);

    try
    {
      File f = new File(fileName);
      ImageIO.write(combined, formatName, f);
    }
    catch (IOException ex)
    {
      return false;
    }
    return true;
  }
}
