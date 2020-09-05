// LineRenderer.java

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
import java.awt.geom.*;

/** 
 * This class is responsible for drawing the turtle's lines.
 */
public class LineRenderer
{
  private Point2D.Double point;
  private Turtle turtle;

  LineRenderer(Turtle turtle)
  {
    this.point = new Point2D.Double();
    this.turtle = turtle;
  }

  /** 
   * Initialisation with coordinates <code>x</code> and
   <code>y</code>.
   */
  public void init(double x, double y)
  {
    this.point.setLocation(x, y);
  }

  /** 
   * Same as init(double x, double y), but with a Point2D.Double
   * argument for convenience
   */
  public void init(Point2D.Double p)
  {
    this.init(p.x, p.y);
  }

  /** 
   * Gets the current x-coordinate in screen coordinates.*/
  private double getX()
  {
    return (toScreenCoords(point)).getX();
  }

  /** 
   * Gets the current x-coordinate in screen coordinates.*/
  private double getY()
  {
    return toScreenCoords(point).getY();
  }

  /** 
   * Calls the clipLineTo and wrapLineTo methods, according
   * to the turtle's edge behavior.
   * Only overwrite this method when working with another
   * (one that you have defined) edge behaviour. <br>
   * If you mean to change the manner of drawing lines, do this in
   * the methods clipLineTo() and wrapLineTo().
   * @see #clipLineTo
   * @see #wrapLineTo
   */
  protected void internalLineTo(double x, double y)
  {
    Point2D.Double screenPos = toScreenCoords(x, y);
    if (turtle.isClip())
    {
      clipLineTo(screenPos.getX(), screenPos.getY());
    }
    if (turtle.isWrap())
    {
      wrapLineTo(screenPos.getX(), screenPos.getY());
    }
    init(x, y);
  }

  /** 
   * Calls the internalLineTo(x,y), which does the actual painting.
   */
  public void lineTo(double x, double y)
  {
    internalLineTo(x, y);
  }

  /** 
   * Calls the internalLineTo(x,y), which does the actual painting.
   * This method works the same way as lineTo(double x, double y), but
   * is added for convenience.
   */
  public void lineTo(Point2D.Double p)
  {
    internalLineTo(p.getX(), p.getY());
  }

  /** 
   * Does the actual painting for clip mode.
   *
   * It works already with ScreenCoords!
   * For further comments cf. internalLineTo(double x, double y).
   * @see #internalLineTo
   */
  protected void clipLineTo(double x, double y)
  {
    turtle.getPlayground().lineTo(getX(), getY(), x, y, turtle.getPen());
    turtle.getPlayground().areaFill(turtle,
      new Point2D.Double(getX(), getY()),
      new Point2D.Double(x, y));
  }

  /** 
   * Does the actual painting for wrap mode.
   * It works already with ScreenCoords!
   * For further comments cf. internalLineTo(double x, double y).
   * @see #internalLineTo
   */
  protected void wrapLineTo(double x, double y)
  {
    double dx = getX() - x;
    double dy = getY() - y;
    Point2D.Double start = new Point2D.Double(x, y);
    Point2D.Double end = new Point2D.Double(start.x + dx, start.y + dy);

    intoPanel(start, end);
    Point2D.Double tmp;
    while ((tmp = calcIntersection(start.x, start.y, end.x, end.y)) != null)
    {
      turtle.getPlayground().lineTo(start.x, start.y, tmp.getX(), tmp.getY(),
        turtle.getPen());
      turtle.getPlayground().areaFill(turtle,
        new Point2D.Double(start.x, start.y),
        new Point2D.Double(tmp.getX(), tmp.getY()));
      start = tmp;
      intoPanel(start, end);
      dx = end.x - start.x;
      dy = end.y - start.y;
    }

    turtle.getPlayground().lineTo(start.x, start.y, end.x, end.y, turtle.getPen());
    turtle.getPlayground().areaFill(turtle,
      new Point2D.Double(start.x, start.y),
      new Point2D.Double(end.x, end.y));
  }

  // Makes the coordinates fit into the Panel.
  private void intoPanel(Point2D.Double start, Point2D.Double end)
  {
    int pWidth = turtle.getPlayground().getWidth();
    int pHeight = turtle.getPlayground().getHeight();
    while (start.x < 0)
    {
      start.x += pWidth;
      end.x += pWidth;
    }
    while (start.x > pWidth)
    {
      start.x -= pWidth;
      end.x -= pWidth;
    }
    if (start.x == 0 && end.x < start.x)
    {
      start.x += pWidth;
      end.x += pWidth;
    }
    if (start.x == pWidth && end.x > start.x)
    {
      start.x -= pWidth;
      end.x -= pWidth;
    }
    while (start.y < 0)
    {
      start.y += pHeight;
      end.y += pHeight;
    }
    while (start.y > pHeight)
    {
      start.y -= pHeight;
      end.y -= pHeight;
    }
    if (start.y == 0 && end.y < start.y)
    {
      start.y += pHeight;
      end.y += pHeight;
    }
    if (start.y == pHeight && end.y > start.y)
    {
      start.y -= pHeight;
      end.y -= pHeight;
    }

  }

  // Intersection line with playground-edges
  //(startX / startY) MUST lie in the playground!
  private Point2D.Double calcIntersection(double startX, double startY, double endX, double endY)
  {
    double dx = endX - startX;
    double dy = endY - startY;
    double W = turtle.getPlayground().getWidth();
    double H = turtle.getPlayground().getHeight();
    if (endX < 0)
    {
      if ((dy / dx <= startY / startX) && (dy / dx >= -(H - startY) / startX))
      { // links
        return new Point2D.Double(0, startY - startX * dy / dx);
      }
    }
    else if (endX > W)
    {
      if ((dy / dx >= -startY / (W - startX)) && (dy / dx <= (H - startY) / (W - startX)))
      {// rechts
        return new Point2D.Double(W, startY + (W - startX) * dy / dx);
      }
    }
    if (endY < 0)
    { // oben
      return new Point2D.Double(startX - startY * dx / dy, 0);
    }
    else if (endY > H)
    { // unten
      return new Point2D.Double(startX + (H - startY) * dx / dy, H);
    }
    else
    {
      return null; // Endpoint lies in the window
    }
  }

  // Calculates the screen coordinates of the turtle's actual
  // position according to the interpretation of the playground.
  private Point2D.Double toScreenCoords(double x, double y)
  {
    return turtle.getPlayground().toScreenCoords(x, y);
  }

  // Calculates the screen coordinates of the turtle's actual
  // position according to the interpretation of the playground.
  private Point2D.Double toScreenCoords(Point2D.Double p)
  {
    return turtle.getPlayground().toScreenCoords(p.x, p.y);
  }

}
