// GGLine.java

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
import java.awt.geom.*;

/**
 * Class representing a line segment. Start and endpoint are given by a GGVector.
 *
 */
public class GGLine
{
  private GGVector startVector;
  private GGVector endVector;

  /**
   * Creates a new GGLine from given GGLine.
   * @param line the line from where the values of start and end point are copied
   */
  public GGLine(GGLine line)
  {
    this.startVector = line.getStartVector();
    this.endVector = line.getEndVector();
  }

  /**
   * Creates a new GGLine from given start and end point.
   * @param startVector the vector to the start of the line
   * @param endVector the vector to the end of the line
   */
  public GGLine(GGVector startVector, GGVector endVector)
  {
    this.startVector = startVector.clone();
    this.endVector = endVector.clone();
  }

  /**
   * Creates a new GGLine from given start and end point vertexes.
   * @param vertexes an array of the start and end point vertex
   */
  public GGLine(GGVector[] vertexes)
  {
    this.startVector = vertexes[0].clone();
    this.endVector = vertexes[1].clone();
  }

  /**
   * Returns the start vector.
   * @return a clone of the start point
   */
  public GGVector getStartVector()
  {
    return startVector.clone();
  }

  /**
   * Returns the end vector.
   * @return a clone of the end point
   */
  public GGVector getEndVector()
  {
    return endVector.clone();
  }

  /**
   * Returns the start point (GGVector components casted to int).
   * @return the start point
   */
  public Point getStartPoint()
  {
    return new Point((int)startVector.x, (int)startVector.y);
  }

  /**
   * Returns the end point (GGVector components casted to int).
   * @return the end point
   */
  public Point getEndPoint()
  {
    return new Point((int)endVector.x, (int)endVector.y);
  }

  /**
   * Returns the vertexes of the line (startVector, endVector).
   * @return a clone of the vertexes
   */
  public GGVector[] getVertexes()
  {
    GGVector[] tmp = new GGVector[2];
    tmp[0] = startVector.clone();
    tmp[1] = endVector.clone();
    return tmp;
  }

  /**
   * Performs a translation by the given vector.
   * @param v the translation vector
   */
  public void translate(GGVector v)
  {
    startVector = startVector.add(v);
    endVector = endVector.add(v);
  }

  /**
   * Returns a new GGLine with same start and end vector.
   * @return a clone of the original line
   */
  public GGLine clone()
  {
    return new GGLine(this);
  }

  /**
   * Returns a string that enumerates start and end vector.
   * @return a string representation of the line
   */
  public String toString()
  {
    return "line segment from " + startVector + " to " + endVector;
  }

  /**
   * Returns true if the current line segment intersects with the given line segment.
   * @param line the line to be checked for intersection with the current line
   * @return true, if the two lines intersects; otherwise false
   */
  public boolean isIntersecting(GGLine line)
  {
    GGVector start = line.getStartVector();
    GGVector end = line.getEndVector();

    double xmin1 = Math.min(startVector.x, endVector.x);
    double xmax1 = Math.max(startVector.x, endVector.x);
    double xmin2 = Math.min(start.x, end.x);
    double xmax2 = Math.max(start.x, end.x);

    if (xmin1 > xmax2 || xmin2 > xmax1)
      return false;

    // Assume the line equations a1*x + b1*y = c1, a2*x + b2*y = c2
    double a1 = endVector.y - startVector.y;
    double b1 = startVector.x - endVector.x;
    double c1 = a1 * startVector.x + b1 * startVector.y;

    double a2 = end.y - start.y;
    double b2 = start.x - end.x;
    double c2 = a2 * start.x + b2 * start.y;

    // Intersection
    double x, y;
    double det = a1 * b2 - a2 * b1;
    if (det == 0)  // parallel
    {
      if (a1 * start.x + b1 * start.y == c1)  // colinear
      {
        if (xmin1 >= xmin2 && xmin1 <= xmax2)
          return true;
        if (xmax1 >= xmin2 && xmax1 <= xmax2)
          return true;
        if (xmin2 >= xmin1 && xmin2 <= xmax1)
          return true;
        if (xmin2 >= xmin1 && xmin2 <= xmax1)
          return true;
      }
      else
        return false;
    }
    else  // not parallel
    {
      x = (b2 * c1 - b1 * c2) / det;
      y = (a1 * c2 - a2 * c1) / det;
      if (x >= xmin1 && x <= xmax1 && x >= xmin2 && x <= xmax2)
        return true;
    }
    return false;
  }

  /**
   * Returns true if the given point is part of the line segment.
   * @param v the vector to be checked if its end-point lays on line segment
   * @param error the error interval for the floating point comparism
   * @return true, if the point is part of the line segment
   */
  public boolean isIntersecting(GGVector v, double error)
  {
    // Assume the line equations a*x + b*y = c
    double a = endVector.y - startVector.y;  
    double b = startVector.x - endVector.x;
    double c = a * startVector.x + b * startVector.y;
    if (Math.abs((a * v.x + b * v.y - c)) < error)  // Point is on line, is it on line segment?
    {
      double xmin = Math.min(startVector.x, endVector.x);
      double xmax = Math.max(startVector.x, endVector.x);
      if (v.x >= xmin && v.x <= xmax)
        return true;
    }
    return false;
  }

  /**
   * Returns true if the current line intersects with the given circle.
   * @param circle the circle to be checked for intersection with the current line
   * @return true, if the two figures intersects; otherwise false
   */
  public boolean isIntersecting(GGCircle circle)
  {
    // Translate start point to CS origin and end point is on x-axis
    GGVector end = endVector.sub(startVector);
    double direction = end.getDirection();
    end.rotate(-direction);

    GGVector center = circle.getCenter().sub(startVector);
    center.rotate(-direction);

    if (center.x > 0 && center.x < end.x)  // Circle along the line
    {
      if (Math.abs(center.y) <= circle.getRadius())
        return true;
      else
        return false;
    }

    if (center.x <= 0) // Circle near start of segment (CS zero)
    {
      if (center.magnitude2() <= circle.radius * circle.radius)
        return true;
      else
        return false;
    }

    if (center.x >= end.x)  // Circle near end of segment
    {
      if (center.sub(end).magnitude2() <= circle.radius * circle.radius)
        return true;
      else
        return false;
    }
    return false;
  }

   /**
   * Returns true, if at least one point of the line segment (casted to int values)
   * points to a non-transparant pixel of the given image.
   * @param imageCenter the vector to the center of the image
   * @param imageDirection the direction of the image (angle of edge ulx --> uly
   * with respect to the positive x-direction (0..2*pi))
   * @param image the buffered image
   * @param isRotatable if true, the imageDirection is considered; otherwise
   * imageDirection = 0 is assumed
   * @return true, if the line segment intersects with non-transparent pixels 
   * of the image
   */
  public boolean isIntersecting(GGVector imageCenter, double imageDirection,
    BufferedImage image, boolean isRotatable)
  {
    GGVector lineVector = endVector.sub(startVector);
    int lineLength = (int)(lineVector.magnitude() + 0.5);
    lineVector = lineVector.mult(1.0 / lineLength);  // Unit vector
    for (int i = 0; i <= lineLength; i++)
    {
      GGVector v = startVector.add(lineVector.mult(i));
      if (v.isIntersecting(imageCenter, imageDirection, image, isRotatable))
        return true;
    }
    return false;
  }
  
  /**
   * Returns the point on the line through the point pt1 and the point
   * pt2 that is in distance ratio times the length from pt1 to pt2 from
   * pt1. For ratio < 0 the point is in the opposite direction.
   * @param pt1 the start point of the line section
   * @param pt2 the end point of the line section
   * @param ratio the distance ratio (any negative or positive double value)
   * @return the dividing point (rounded to int) 
   */
  public static Point getDividingPoint(Point pt1, Point pt2, double ratio)
  {
    GGVector v1 = new GGVector(pt1.x, pt1.y);
    GGVector v2 = new GGVector(pt2.x, pt2.y);
    GGVector dv = v2.sub(v1);
    GGVector v = v1.add(dv.mult(ratio));
    return new Point((int)(v.x + 0.5), (int)(v.y + 0.5));
  }
}
