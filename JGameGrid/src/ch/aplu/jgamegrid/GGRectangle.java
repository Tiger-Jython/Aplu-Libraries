    // GGRectangle.java

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
import java.awt.geom.*;

/**
 * Class representing a rotatable rectangle. The following terms for
 * a rectangle with direction = 0 are used:
 * <code><pre>
(ulx, uly)            (urx, ury)
vertex[0]             vertex[1]
o--------------------->o    ==========>> direction    ---------->x
^      edges[0]        |                              |
|                      |                              |
|  e                e  |                              |
|  d                d  |                              v
|  g                g  |                              y
|  e                e  | height
|  s                s  |
| [3}              [1] |
|                      |
|      edges[2]        v
o<---------------------o
vertex[3]   width     vertex[2]
(llx, lly)            (lrx, lry)
 </pre></code>
 * The direction of the rectangle is defined as the angle (0..2*pi) of the
 * vector direction of edges[0] clockwise with reference to the x-coordinate direction.
 * When defining the rectangle instance by the 4 vertexes, it is assumed that the shape is
 * a rectangle.
 */
public class GGRectangle
{
  private GGVector[] vertexes = new GGVector[4];
  private GGVector[] edges = new GGVector[4];

  /**
   * Creates a new GGRectangle from given GGRectangle.
   * @param rect the rectangle from where the values of vertexes and edges are copied
   */
  public GGRectangle(GGRectangle rect)
  {
    GGVector[] vertexes = rect.getVertexes();
    for (int i = 0; i < 4; i++)
      this.vertexes[i] = vertexes[i].clone();
    GGVector[] edges = rect.getEdges();
    for (int i = 0; i < 4; i++)
      this.edges[i] = edges[i].clone();
  }

  /**
   * Creates a new GGRectangle from given vertexes. The edges are recalculated.
   * @param vertexes the 4 vertexes from where the values of new vertexes are copied
   */
  public GGRectangle(GGVector[] vertexes)
  {
    for (int i = 0; i < 4; i++)
      this.vertexes = vertexes.clone();
    buildEdges();
  }

  /**
   * Creates a new GGRectangle from given 8 x-y-coordinates. The edges are calculated.
   * @param x0 coordinate of lower left x  (llx)
   * @param y0 coordinate of lower left y  (lly)
   * @param x1 coordinate of lower right x (lrx)
   * @param y1 coordinate of lower right y (lry)
   * @param x2 coordinate of upper right x (urx)
   * @param y2 coordinate of upper right y (ury)
   * @param x3 coordinate of upper left x  (ulx)
   * @param y3 coordinate of upper left y  (uly)
   */
  public GGRectangle(double x0, double y0, double x1, double y1, double x2, double y2, double x3, double y3)
  {
    for (int i = 0; i < 4; i++)
      vertexes[i] = new GGVector();
    vertexes[0].x = x0;
    vertexes[0].y = y0;
    vertexes[1].x = x1;
    vertexes[1].y = y1;
    vertexes[2].x = x2;
    vertexes[2].y = y2;
    vertexes[3].x = x3;
    vertexes[3].y = y3;
    buildEdges();
  }

  /**
   * Creates a new GGRectangle from given 4 vertex points. The edges are calculated.
   * @param pt0 coordinates of lower left vertex
   * @param pt1 coordinates of lower right vertex
   * @param pt2 coordinates of upper right vertex
   * @param pt3 coordinates of upper right vertex
   */
  public GGRectangle(Point2D.Double pt0, Point2D.Double pt1, Point2D.Double pt2, Point2D.Double pt3)
  {
    for (int i = 0; i < 4; i++)
      vertexes[i] = new GGVector();
    vertexes[0].x = pt0.x;
    vertexes[0].y = pt0.y;
    vertexes[1].x = pt1.x;
    vertexes[1].y = pt1.y;
    vertexes[2].x = pt2.x;
    vertexes[2].y = pt2.y;
    vertexes[3].x = pt3.x;
    vertexes[3].y = pt3.y;
    buildEdges();
  }

  /**
   * Creates a new GGRectangle from given center, direction, width and height. The edges are calculated.
   * @param center vector to the center of the rectangle
   * @param direction direction of edges[0] (in arc, 0..2*pi, positive clockwise)
   * @param width the length of edges[0] and edges[2]
   * @param height the length of edges[1] and edges[3]
   */
  public GGRectangle(GGVector center, double direction, double width, double height)
  {
    for (int i = 0; i < 4; i++)
      vertexes[i] = new GGVector();
    GGVector e1 = new GGVector(Math.cos(direction), Math.sin(direction));
    GGVector e2 = new GGVector(Math.cos(Math.PI / 2 + direction), Math.sin(Math.PI / 2 + direction));
    vertexes[0] = center.add(e1.mult(-width / 2).add(e2.mult(-height / 2)));
    vertexes[1] = center.add(e1.mult(width / 2).add(e2.mult(-height / 2)));
    vertexes[2] = center.add(e1.mult(width / 2).add(e2.mult(height / 2)));
    vertexes[3] = center.add(e1.mult(-width / 2).add(e2.mult(height / 2)));
    buildEdges();
  }

  /** Creates a new GGRectangle from given java.awt.Rectangle.
   * @param rect the rectangle from where the values of vertexes are taken
   */
  public GGRectangle(Rectangle rect)
  {
    vertexes[0] = new GGVector(rect.x, rect.y);
    vertexes[1] = new GGVector(rect.x + rect.width, rect.y);
    vertexes[2] = new GGVector(rect.x + rect.width , rect.y + rect.height);
    vertexes[3] = new GGVector(rect.x, rect.y + rect.height);
    buildEdges();
  }

  private void buildEdges()
  {
    for (int i = 0; i < 3; i++)
      edges[i] = vertexes[i + 1].sub(vertexes[i]);
    edges[3] = vertexes[0].sub(vertexes[3]);
  }

  /**
   * Rotates with rotation center at (0, 0).
   * @param angle
   */
  public void rotate(double angle)
  {
    for (int i = 0; i < 4; i++)
      vertexes[i].rotate(angle);
    buildEdges();
  }

  /**
   * Rotates with given rotation center.
   * @param rotationCenter the center of the rotation
   * @param angle the angle (in radian, clockwise)
   */
  public void rotate(GGVector rotationCenter, double angle)
  {
    GGVector center = getCenter();
    translate(center.invert());
    for (int i = 0; i < 4; i++)
      vertexes[i].rotate(angle);
    translate(center);
    buildEdges();
  }

  /**
   * Translates by the given vector.
   * @param v the translatin vector
   */
  public void translate(GGVector v)
  {
    for (int i = 0; i < 4; i++)
    {
      vertexes[i].x += v.x;
      vertexes[i].y += v.y;
    }
    buildEdges();
  }

  /**
   * Returns a GGVector array with 4 GGVectors whoses values are copies of
   * the original vertexes.
   * @return a GGVector array of size 4
   */
  public GGVector[] getVertexes()
  {
    GGVector[] tmp = new GGVector[4];
    for (int i = 0; i < 4; i++)
      tmp[i] = vertexes[i].clone();
    return tmp;
  }

  /**
   * Returns a GGVector array with 4 GGVectors whoses values are copies of
   * the original edges.
   * @return a GGVector array of size 4
   */
  public GGVector[] getEdges()
  {
    GGVector[] tmp = new GGVector[4];
    for (int i = 0; i < 4; i++)
      tmp[i] = edges[i].clone();
    return tmp;
  }

  /**
   * Returns a vector that points to the center of the rectangle.
   * @return a GGVector that points to the center
   */
  public GGVector getCenter()
  {
    return (vertexes[2].add(vertexes[0])).mult(0.5);
  }

  /**
   * Returns the width of the rectangle.
   * @return the width (length of edges[0])
   */
  public double getWidth()
  {
    return edges[0].magnitude();
  }

  /**
   * Returns the height of the rectangle.
   * @return the height (length of edges[1])
   */
  public double getHeight()
  {
    return edges[1].magnitude();
  }

  /**
   * Direction of edges[0], zero to west, clockwise 0..2*pi
   * @return the direction (arc 0.. 2*pi)
   */
  public double getDirection()
  {
    return edges[0].getDirection();
  }

  /**
   * Returns the circumradius of the rectangle.
   * @return the circumradius, e.g. the radius of the circle through the vertexes
   */
  public double getCircumradius()
  {
    GGVector diagonal = vertexes[0].sub(vertexes[2]);
    return 0.5 * diagonal.magnitude();
  }

  /**
   * Returns a new rectangle with same vertices and egdes as the original.
   * @return a clone of the original rectangle
   */
  public GGRectangle clone()
  {
    GGRectangle tmp = new GGRectangle(this);
    return tmp;
  }

  /**
   * Returns a string that enumerates vertexes, edges, center, width, height and direction.
   * @return a string representation of the rectangle
   */
  public String toString()
  {
    StringBuffer sb = new StringBuffer();
    sb.append("Vertexes: ");
    for (int i = 0; i < 4; i++)
      sb.append(getVertexes()[i] + " ");
    sb.append("\nEdges: ");
    for (int i = 0; i < 4; i++)
      sb.append(getEdges()[i] + " ");
    GGVector center = getCenter();
    sb.append("\nCenter: " + center);
    double width = getWidth();
    sb.append("\nWidth: " + width);
    double height = getHeight();
    sb.append("\nHeight: " + height);
    double direction = getDirection();
    sb.append("\nDirection: " + direction);
    return sb.toString();
  }

  /**
   * Returns true if the current rectangle intersects with the given rectangle.
   * @param rect the rectangle to be checked for intersection with the current rectangle
   * @return true, if the two rectangles intersects; otherwise false
   */
  public boolean isIntersecting(GGRectangle rect)
  {
    // 4 axis for the projections:
    // they must be parallel to the polygons edges, for two rectangles this
    // gives 8 axis, but two are parallel, so only one of them is needed.
    // We select two perpendicular edges for each rectangle
    GGVector[] axis = new GGVector[4];
    axis[0] = edges[0];
    axis[1] = edges[1];
    axis[2] = rect.getEdges()[0];
    axis[3] = rect.getEdges()[1];

    // Algorithm (Separating Axis Theorem, SAT)
    // On each axis:
    // Consider the projection points of all 4 vertexes of the first rectangle.
    // They span a first region on this axis. Consider the second region
    // for the second rectangle. Check if these two regions overlaps.
    //
    // As soon as you find an axis, where the regions do not overlap, the rectangles
    // do not intersect, e.g if the regions overlap on every axis, the rectangles
    // intersects

    for (int i = 0; i < 4; i++)
    {
      double magnitudeSquare = axis[i].x * axis[i].x + axis[i].y * axis[i].y;
      double min1, max1, min2, max2;

      GGVector[] projections = new GGVector[4];
      double[] positions = new double[4];
      for (int k = 0; k < 4; k++)
      {
        // For current rectangle, projection vector for 4 vertexes on axis
        projections[k] = axis[i].mult((vertexes[k].dot(axis[i])) / magnitudeSquare);
        // Trick: This is a measure where the projection points are
        positions[k] = projections[k].dot(axis[i]);
      }

      // Determine the min and max for rectangle 1
      min1 = positions[0];
      max1 = positions[0];
      for (int k = 1; k < 4; k++)
      {
        if (positions[k] > max1)
          max1 = positions[k];
        if (positions[k] < min1)
          min1 = positions[k];
      }

      // Same for given rectangle
      for (int k = 0; k < 4; k++)
      {
        projections[k] = axis[i].mult((rect.getVertexes()[k].dot(axis[i])) / magnitudeSquare);
        positions[k] = projections[k].dot(axis[i]);
      }
      // Determine the min and max for rectangle 2
      min2 = positions[0];
      max2 = positions[0];
      for (int k = 1; k < 4; k++)
      {
        if (positions[k] > max2)
          max2 = positions[k];
        if (positions[k] < min2)
          min2 = positions[k];
      }

      // Check for overlap of projections
      // all 4 axis must have an overlap to have an intersection
      if (min2 > max1 || min1 > max2) // true if no overlap
        return false;
    }
    return true;
  }

  /**
   * Returns true if the current rectangle intersects with the given line segment.
   * @param line the line segment to be checked for intersection with the current rectangle
   * @return true, if the line and the rectangle intersects; otherwise false
   */
  public boolean isIntersecting(GGLine line)
  {
    // See comments for SAT above
    GGVector[] axis = new GGVector[4];
    axis[0] = edges[0];
    axis[1] = edges[1];
    axis[2] = line.getEndVector().sub(line.getStartVector());
    axis[3] = new GGVector(-axis[2].y, axis[2].x);  // Perpendicular to line

    for (int i = 0; i < 4; i++)
    {
      double magnitudeSquare = axis[i].x * axis[i].x + axis[i].y * axis[i].y;
      double min1, max1, min2, max2;

      GGVector[] projections = new GGVector[4];
      double[] positions = new double[4];
      for (int k = 0; k < 4; k++)
      {
        projections[k] = axis[i].mult((vertexes[k].dot(axis[i])) / magnitudeSquare);
        positions[k] = projections[k].dot(axis[i]);
      }

      // Determine the min and max for rectangle
      min1 = positions[0];
      max1 = positions[0];
      for (int k = 1; k < 4; k++)
      {
        if (positions[k] > max1)
          max1 = positions[k];
        if (positions[k] < min1)
          min1 = positions[k];
      }

      // Same for given line
      for (int k = 0; k < 2; k++)
      {
        projections[k] = axis[i].mult((line.getVertexes()[k].dot(axis[i])) / magnitudeSquare);
        positions[k] = projections[k].dot(axis[i]);
      }
      min2 = positions[0];
      max2 = positions[0];
      if (positions[1] > max2)
        max2 = positions[1];
      if (positions[1] < min2)
        min2 = positions[1];

      // Check for overlap of projections
      if (min2 > max1 || min1 > max2)
        return false;
    }
    return true;

  }

  /**
   * Returns true if the current rectangle intersects with the given circle.
   * @param circle the circle to be checked for intersection with the current rectangle
   * @return true, if the two figures intersects; otherwise false
   */
  public boolean isIntersecting(GGCircle circle)
  {
    return circle.isIntersecting(this);
  }

  /**
   * Returns true if the given points is part of the rectangle area.
   * @param vector the vector to be checked for intersection
   * @param isRotatable if false, the axis-parallel rectangle is assumed; if true,
   * the rectangle may be rotated
   * @return true, if the vector (end point) lays inside or on the border of the rectangle; otherwise false
   */
  public boolean isIntersecting(GGVector vector, boolean isRotatable)
  {
    return (isIntersecting(vector, isRotatable, new Point()));
  }

  protected boolean isIntersecting(GGVector vector, boolean isRotatable, Point pix)
  {
    // Transform to coordinate system CS with zero in center of rectangle
    // and axis parallel to the edges
    GGVector v = getCenter();
    double theta = getDirection();

    // Transform given vector
    GGVector vTrans = vector.add(v.invert());
    if (isRotatable)
      vTrans.rotate(-theta);

    // Transform rectangle
    GGRectangle rectTrans = clone();
    rectTrans.translate(v.invert());
    if (isRotatable)
      rectTrans.rotate(-theta);

    double a = rectTrans.getWidth() / 2;
    double b = rectTrans.getHeight() / 2;
    if (vTrans.x >= -a && vTrans.x <= a &&
        vTrans.y >= -b && vTrans.y <= b)
    {
      pix.x = (int)vTrans.x;
      pix.y = (int)vTrans.y;
      return true;
    }
    return false;
  }

  /**
   * Returns true, if the current rectangle is identical to the given rectangle.
   * @param rect the rectangle to compare
   * @return true, if both rectangles are identical
   */
  public boolean isEqual(GGRectangle rect)
  {
    GGVector[] vertexes = new GGVector[4];
    vertexes = rect.getVertexes();
    boolean same = true;
    for (int i = 0; i < 4; i++)
    {
      if (!vertexes[i].isEqual(this.vertexes[i]))
        return false;
    }
    return same;
  }
  

  // Makes only sense, if the GGRectangle has direction 0, 90, 180, 270 degrees
  protected Rectangle getAWTRectangle()
  {
    int minx = (int)Math.min(
      Math.min(vertexes[0].x, vertexes[1].x),
      Math.min(vertexes[2].x, vertexes[3].x));
    int miny = (int)Math.min(
      Math.min(vertexes[0].y, vertexes[1].y),
      Math.min(vertexes[2].y, vertexes[3].y));
    int maxx = (int)Math.max(
      Math.max(vertexes[0].x, vertexes[1].x),
      Math.max(vertexes[2].x, vertexes[3].x));
    int maxy = (int)Math.max(
      Math.max(vertexes[0].y, vertexes[1].y),
      Math.max(vertexes[2].y, vertexes[3].y));
    return new Rectangle(minx, miny, maxx-minx, maxy-miny);
  }

}
