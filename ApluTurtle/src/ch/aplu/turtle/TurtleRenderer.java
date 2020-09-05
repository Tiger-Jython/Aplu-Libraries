// TurtleRenderer.java

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

import ch.aplu.turtle.*;
import java.awt.image.*;
import java.awt.*;
import java.awt.geom.*;
import java.util.*;

/** 
 * This class is responsible for creating and selecting the correct turtle picture.
 */
public class TurtleRenderer implements ImageObserver
{
  /** Holds the current image */
  private Image currentImage;
  /** Holds all images */
  private Vector images;
  /** Tells how many pictures are needed*/
  private int resolution;
  /** A reference to the <code>Turtle</code> */
  private Turtle turtle;
  /** Holds the current Angle */
  private double currentAngle;
  private final int turtleSize = 29;

  /**
   * Creates a TurtleRenderer instance for the given turtle.
   */
  public TurtleRenderer(Turtle turtle)
  {
    this.currentImage = null;
    this.images = new Vector();
    this.turtle = turtle;
    currentAngle = 0;
  }

  /** 
   * As an image stays unchanged, there's no need to ever update it. So this method returns always false.
   * For further information cf. <code>java.awt.image.ImageObserver</code>.
   * @see java.awt.image.ImageObserver#imageUpdate(java.awt.Image, int, int, int, int, int)
   */
  public boolean imageUpdate(Image img, int infoflags,
    int x, int y, int width, int height)
  {
    return false;
  }

  /** 
   * Returns the current image.
   */
  public Image currentImage()
  {
    return this.currentImage;
  }

  /** 
   * Tells whether the image has changed.
   */
  public boolean imageChanged(double angle)
  {
    return (this.currentImage != getImage(angle));
  }

  // Sets the current image to the specified one. 
  private void setCurrentImage(Image image)
  {
    currentImage = image;
  }

  // Returns the current image 
  private Image getImage(double angle)
  {
    while (angle < 0)
    {
      angle += 2 * Math.PI;
    }
    while (angle >= 2 * Math.PI)
    {
      angle -= 2 * Math.PI;
    }
    double res = 2 * Math.PI / (double)this.resolution;
    int index = (int)(angle / res);
    return image(index);
  }

  /** 
   * Sets the current image to the one corresponding to the angle <code>angle</code>.
   */
  public void setAngle(double angle)
  {
    currentAngle = angle;
    setCurrentImage(getImage(angle));
  }

  // return the current angle.
  protected double getAngle()
  {
    return currentAngle;
  }

  /** 
   * Creates the images. There are <code>resolution</code> images (i.e. two subsequent
   * images contain an angle of 2&pi;/<resolution> or 360/resolution degrees).
   */
  public void init(TurtleFactory factory, int resolution)
  {
    this.resolution = resolution;
    Integer res = new Integer(resolution);
    double incRes = Math.PI * 2 / res.doubleValue();
    double angle = 0;
    images = new Vector();
    for (int i = 0; i < resolution; i++)
    {
      Image img = factory.turtleImage(turtle.getColor(),
        turtle.getPlayground().toScreenAngle(angle),
        turtleSize, turtleSize);
      if (i == 0)
        turtle.setTurtleImage(img);
      images.add(img);
      angle += incRes;
    }
    setCurrentImage(getImage(currentAngle));
  }

  // Tells how many images this <code>TurtleRenderer</code> holds 
  private int countImages()
  {
    return this.images.size();
  }

  // Gets the image at <code>index</code>
  private Image image(int index)
  {
    return (Image)this.images.elementAt(index);
  }

  /** 
   * This method is responsible for painting the turtle onto the
   * playground at (<code>x, y</code>).
   */
  public final void paint(double x, double y)
  {
    internalPaint(x, y, turtle.getPlayground().getGraphics());
  }

  /** 
   * This method is responsible for painting the turtle onto the
   * playground at <code>p</code>.
   */
  public final void paint(Point2D.Double p)
  {
    internalPaint(p.x, p.y, turtle.getPlayground().getGraphics());
  }

  /** 
   * This method is responsible for painting the <code>Turtle</code>
   * at (<code>x, y</code>).<br>
   * The Graphics argument tells where to paint.
   */
  public final void paint(double x, double y, Graphics g)
  {
    internalPaint(x, y, g);
  }

  /** 
   * This method is responsible for painting the <code>Turtle</code>
   * at <code>p</code>.<br>
   * The Graphics argument tells where to paint.
   */
  public void paint(Point2D.Double p, Graphics g)
  {
    internalPaint(p.x, p.y, g);
  }

  protected void internalPaint(double x, double y, Graphics g)
  {
    if (turtle.isClip())
    {
      Point2D.Double p =
        calcTopLeftCorner(turtle.getPlayground().toScreenCoords(x, y));
      clipPaint((int)p.x, (int)p.y, (Graphics2D)g);
    }
    else if (turtle.isWrap())
    {
      Point2D.Double p =
        calcTopLeftCorner(turtle.getPlayground().toScreenCoords(x, y));
      wrapPaint((int)p.x, (int)p.y, (Graphics2D)g);
    }
  }

  // Defines how to paint in clip mode and do it
  protected void clipPaint(int x, int y, Graphics2D g2D)
  {
    g2D.drawImage(currentImage, x, y, this);
  }

  /// Defines how to paint in wrap mode and do it
  protected void wrapPaint(int x, int y, Graphics2D g2D)
  {
    int pWidth = turtle.getPlayground().getWidth();
    int pHeight = turtle.getPlayground().getHeight();
    int paintX = x;
    while (paintX > pWidth)
    {
      paintX -= pWidth;
    }
    while (paintX < 0)
    {
      paintX += pWidth;
    }
    int paintY = y;
    while (paintY > pHeight)
    {
      paintY -= pHeight;
    }
    while (paintY < 0)
    {
      paintY += pHeight;
    }
    g2D.drawImage(currentImage, paintX, paintY, this);
    int nWidth = currentImage.getWidth(this);
    int nHeight = currentImage.getHeight(this);
    boolean right = (paintX + nWidth > pWidth);
    boolean bottom = (paintY + nHeight > pHeight);
    if (right)
    {
      g2D.drawImage(currentImage,
        paintX - pWidth,
        paintY,
        this);
    }
    if (bottom)
    {
      g2D.drawImage(currentImage,
        paintX,
        paintY - pHeight,
        this);
    }
    if (right && bottom)
    {
      g2D.drawImage(currentImage,
        paintX - pWidth,
        paintY - pHeight,
        this);
    }
  }

  // Computes the x-coordinate of the top left corner of the Turtle image
  // (it depends on the specified x-coordinate and the image width).
  protected int calcTopLeftCornerX(double x)
  {
    int intX = (int)x;
    int nWidth;
    if (currentImage == null)
    {
      setCurrentImage(new TurtleFactory().turtleImage(turtle.getColor(), getAngle(), turtleSize, turtleSize));
    }
    nWidth = this.currentImage.getWidth(this);
    // the center of the turtle lies on the turtle's location:
    intX -= nWidth / 2;
    return intX; // top left corner of the Turtle's image
  }

  // Computes the y-coordinate of the top left corner of the Turtle image
  // (it depends on the specified y-coordinate and the image height).
  protected int calcTopLeftCornerY(double y)
  {
    int intY = (int)y;
    if (currentImage == null)
    {
      setCurrentImage(new TurtleFactory().turtleImage(turtle.getColor(), getAngle(), turtleSize, turtleSize));
    }
    int nHeight = currentImage.getHeight(this);
    // the center of the turtle lies on the turtle's location:
    intY -= nHeight / 2;

    return intY; // top left corner of the Turtle's image
  }

  // Computes the top left corner of the Turtle image
  // (dependent on the specified x- and y-coordinate and the image
  protected Point2D.Double calcTopLeftCorner(double x, double y)
  {
    if (currentImage == null)
    {
      setCurrentImage(new TurtleFactory().turtleImage(turtle.getColor(), getAngle(), turtleSize, turtleSize));
    }
    int w = currentImage.getWidth(this);
    int h = currentImage.getHeight(this);
    return new Point2D.Double(x - w / 2, y - h / 2);
  }

  // Computes the top left corner of the Turtle image
  // (dependent on the specified point <code>p</code> and the image
  // width and height.
  protected Point2D.Double calcTopLeftCorner(Point2D.Double p)
  {
    return calcTopLeftCorner(p.x, p.y);
  }

}
