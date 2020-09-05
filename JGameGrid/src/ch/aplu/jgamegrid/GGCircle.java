// GGCircle.java

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

package ch.aplu.jgamegrid;

import java.awt.*;
import java.awt.image.BufferedImage;

/**
 * Class representing a circle in the x-y-plane with a center vector and
 * a radius (all doubles).
 */
public class GGCircle
{
  /**
   * The public vector to the center of the circle.
   */
  public GGVector center;

  /**
   * The public radius of the circle.
   */
  public double radius;

  /**
   * Constructs a circle with given center and radius.
   * @param center the vector to the center
   * @param radius the radius of the circle
   */
  public GGCircle(GGVector center, double radius)
  {
    this.center = center.clone();
    this.radius = radius;
  }

  /**
   * Constructs a circle at (0, 0) with given radius.
   * @param radius the radius of the circle
   */
  public GGCircle(double radius)
  {
    center = new GGVector(0, 0);
    this.radius = radius;
  }

  /**
   * Constructs a circle at (0, 0) with radius = 0.
   */
  public GGCircle()
  {
    center = new GGVector(0, 0);
    radius = 0;
  }

  /**
   * Constructs a new circle with the same center and radius as the given circle.
   * @param circle the circle from where center and radius are copied.
   */
  public GGCircle(GGCircle circle)
  {
    center = circle.center.clone();
    radius = circle.radius;
  }

  /**
   * Returns the radius of the circle.
   * @return the radius of the circle
   */
  public double getRadius()
  {
    return radius;
  }

  /**
   * Returns a copy of the center of the circle.
   * @return a clone of the center of the circle
   */
  public GGVector getCenter()
  {
    return center.clone();
  }


  /**
   * Returns a new circle with the same center and radius as the current circle.
   */
  public GGCircle clone()
  {
    return new GGCircle(this);
  }

  /**
   * Performs a translation by the given vector.
   * @param v the translation vector
   */
  public void translate(GGVector v)
  {
    center = center.add(v);
  }

 /**
   * Returns true if the current circle intersects with the given circle.
   * @param circle the circle to be checked for intersection with the current circle
   * @return true, if the two circles intersects; otherwise false
   */
  public boolean isIntersecting(GGCircle circle)
  {
    double dSquare = (radius + circle.radius) * (radius + circle.radius);
    return (center.sub(circle.center).magnitude2() <= dSquare);
  }

  /**
   * Returns true if the current circle intersects with the given rectangle.
   * @param rect the rectangle to be checked for intersection with the current circle
   * @return true, if the two figures intersects; otherwise false
   */
  public boolean isIntersecting(GGRectangle rect)
  {
    return isIntersecting(rect, center, radius, true);
  }

  /**
   * Returns true if the current circle intersects with the given java.awt.Rectangle.
   * @param rect the rectangle to be checked for intersection with the current circle
   * @return true, if the two figures intersects; otherwise false
   */
  public boolean isIntersecting(Rectangle rect)
  {
    GGRectangle r = new GGRectangle(rect);
    return isIntersecting(r, center, radius, false);
  }

  private boolean isIntersecting(GGRectangle rectangle, GGVector center, double r, boolean isRotated)
  {
    // Transform to coordinate system CS with zero in center of rectangle
    // and axis parallel to the edges
    GGVector v = rectangle.getCenter();
    double theta = rectangle.getDirection();

    // Transform circle
    GGVector c = center.add(v.invert());
    if (isRotated)
      c.rotate(-theta);

    // Transform rectangle
    GGRectangle rect = rectangle.clone();
    rect.translate(v.invert());
    if (isRotated)
      rect.rotate(-theta);

    // --------- The following is done in the coordinate system CS --------
    // Check if the circle is far away
    double circumradius = rect.getCircumradius();
    if (c.magnitude() > circumradius + r)
      return false;

    Rectangle re = rect.getAWTRectangle();  // Possible, because rect is axis parallel
    double a = re.width / 2;
    double b = re.height / 2;
    // Check if center is in rectangle
    if (c.x >= -a && c.x <= a && c.y >= -b && c.y <= b)
      return true;

    // in strips left, right, top, bottom
    if (c.y >= -b && c.y <= b) // left or right
    {
      if (c.x >= a && c.x <= a + r) // right
        return true;
      if (c.x >= -(a + r) && c.x <= -a) // left
        return true;
    }

    if (c.x >= -a && c.x <= a) // top or bottom
    {
      if (c.y >= b && c.y <= b + r) // top
        return true;
      if (c.y >= -(b + r) && c.y <= -b) // bottom
        return true;
    }

    // in sectors
    if (c.x >= a && c.x <= a + r) // upper right or lower right
    {
      if (c.y >= b && c.y <= b + r) // upper right
      {
        GGVector ur = new GGVector(re.x + re.width, re.y);
        double d = c.sub(ur).magnitude2();
        if (d <= r * r)
          return true;
      }
      if (c.y >= -(b + r) && c.y <= -b) // lower right
      {
        GGVector lr = new GGVector(re.x + re.width, re.y + re.height);
        double d = c.sub(lr).magnitude2();
        if (d <= r * r)
          return true;
      }
    }

    if (c.x >= -(a + r) && c.x <= -a) // upper left or lower left
    {
      if (c.y >= b && c.y <= b + r) // upper left
      {
        GGVector ul = new GGVector(re.x, re.y);
        double d = c.sub(ul).magnitude2();
        if (d <= r * r)
          return true;
      }
      if (c.y >= -(b + r) && c.y <= -b) // lower left
      {
        GGVector ll = new GGVector(re.x, re.y + re.height);
        double d = c.sub(ll).magnitude2();
        if (d <= r * r)
          return true;
      }
    }
    return false;
  }

 /**
   * Returns true if the given points is part of the circle area.
   * @param vector the vector to be checked for intersection
   * @return true, if the vector (end point) lays inside or on the border of the circle; otherwise false
   */
  public boolean isIntersecting(GGVector vector)
  {
    return isIntersecting(vector,new Point());
  }

  protected boolean isIntersecting(GGVector vector, Point pt)
  {
    GGVector v = center.sub(vector);
    if (v.magnitude2() <= radius * radius)
    {
      pt.x = -(int)v.x;
      pt.y = -(int)v.y;
      return true;
    }
    return false;
  }

/**
   * Returns true, if area of the current circle intersects the
   * non-transparant area of the given image.
   * @param imageCenter the vector to the center of the image
   * @param imageDirection the direction of the image (angle of edge ulx --> uly
   * with respect to the positive x-direction (0..2*pi))
   * @param image the buffered image
   * @param isRotatable if true, the imageDirection is considered; otherwise
   * imageDirection = 0 is assumed
   */
  public boolean isIntersecting(GGVector imageCenter, double imageDirection,
    BufferedImage image, boolean isRotatable)
  {
    // If no intersection with circumcircle-> no collsion
    int width = image.getWidth();
    int height = image.getHeight();
    double a = width / 2.0;
    double b = height / 2.0;
    GGCircle circumcircle = new GGCircle(imageCenter, Math.sqrt(a * a + b * b));
    if (!isIntersecting(circumcircle))
      return false;

    // Transform to coordinate system CS in image center and axis parallel
    GGVector vCenter = center.clone();
    vCenter = vCenter.sub(imageCenter);
    if (isRotatable)
      vCenter.rotate(-imageDirection);
    // CS in upper left vertex
    GGVector ul = new GGVector(-a, -b);
    vCenter = vCenter.sub(ul);

    // Consider each pixel of the image: if non-transparent and within circle
    // we have a collision
    for (int i = 0; i < width; i++)
    {
      for (int k = 0; k < height; k++)
      {
        int color_sRGB = image.getRGB(i, k);
        int alpha = color_sRGB >>> 24;
        if (alpha != 0)  // non-transparent
        {
          GGVector pix = new GGVector(i, k);
          if (pix.sub(vCenter).magnitude2() <= radius * radius)
            return true;
        }
      }
    }
    return false;
  }

  /**
   * Returns a string that enumerates center and radius.
   * @return a string representation of the circle
   */
  public String toString()
  {
    return "Center: " + center.toString() + " Radius: " + radius;
  }

  /**
   * Returns true, if the current circle is identical to the given circle.
   * @param circle the cirle to compare
   * @return true, if both circles are identical
   */
  public boolean isEqual(GGCircle circle)
  {
    if (circle.getRadius() == radius && circle.getCenter().isEqual(center))
      return true;
    return false;
  }

}
