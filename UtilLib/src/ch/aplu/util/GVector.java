// GVector.java

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

import java.awt.Point;
import java.awt.geom.*;
import java.awt.image.BufferedImage;

/**
 * Class representing a two-dimensional vector with double coordinates (x, y).
 * The following situation is assumed:
 * <code><pre>
 *
 *     (0, 0) o------------------>x-axis
 *            | .  / direction
 *            |  .
 *            |   .vector
 *            |    .
 *            |     .
 *            |      *
 *            v
 *          y-axis
 *
 * </pre></code>
 * The direction of the vector is defined as the angle (0..2*pi) of the
 * vector direction clockwise with reference to the x-coordinate direction.
 */
public class GVector
{
  /**
   * The public x-coordinate.
   */
  public double x;
  /**
   * The public y-coordinate.
   */
  public double y;

  /**
   * Constructs a zero vector (with coordinates (0, 0)).
   */
  public GVector()
  {
    this(0, 0);
  }

  /**
   * Constructs a vector from integer coordinates using jawa.awt.Point.
   * @param pt the point that becomes a vector
   */
  public GVector(Point pt)
  {
    this(pt.x, pt.y);
  }

  /**
   * Constructs a vector from given integer x-y-coordinates.
   * @param x the x-coordinate of the vector
   * @param y the y-coordinate of the vector
   */
  public GVector(int x, int y)
  {
    this((double)x, (double)y);
  }

  /**
   * Constructs a vector from given float x-y-coordinates.
   * @param x the x-coordinate of the vector
   * @param y the y-coordinate of the vector
   */
  public GVector(float x, float y)
  {
    this((double)x, (double)y);
  }

  /**
   * Constructs a vector from given double x-y-coordinates.
   * @param x the x-coordinate of the vector
   * @param y the y-coordinate of the vector
   */
  public GVector(double x, double y)
  {
    this.x = x;
    this.y = y;
  }

  /**
   * Returns the magnitude (length) of the vector.
   * @return the magnitude
   */
  public double magnitude()
  {
    return Math.sqrt(x * x + y * y);
  }

  /**
   * Returns the square of the magnitude, without squareroot calculation.
   * This is useful when performance is an issue.
   * @return the square of the magnitude
   */
  public double magnitude2()
  {
    return x * x + y * y;
  }

  /**
   * Modifies the vector to unit magnitude.
   */
  public void normalize()
  {
    double magnitude = magnitude();
    x = x / magnitude;
    y = y / magnitude;
  }

  /**
   * Returns a new vector with magnitude 1 in the direction of the given vector.
   * @return a unit vector with same direction
   */
  public GVector getNormalized()
  {
    double magnitude = magnitude();
    return new GVector(x / magnitude, y / magnitude);
  }

  /**
   * Returns the direction of the vector (range 0..2*pi)
   * @return the direction of the vector (in radian, zero to east, clockwise)
   */
  public double getDirection()
  {
    double theta = Math.atan2(y, x);
    if (theta >= 0)
      return theta;
    else
      return 2 * Math.PI + theta;
  }

  /**
   * Rotates the vector by the specified angle.
   * @param angle the rotation angle (in radian, clockwise)
   */
  public void rotate(double angle)
  {
    double xnew, ynew;
    xnew = Math.cos(angle) * x - Math.sin(angle) * y;
    ynew = Math.sin(angle) * x + Math.cos(angle) * y;
    x = xnew;
    y = ynew;
  }

  /**
   * Returns the scalar product (dot product) of the current vector with the given vector.
   * @param v the vector to take for the dot product
   * @return the dot product
   */
  public double dot(GVector v)
  {
    return x * v.x + y * v.y;
  }

  /**
   * Returns the distance between the current vector and the given vector.
   * @param v the vector to take for the distance measurement
   * @return the distance (magnitude of the vector difference)
   */
  public double distanceTo(GVector v)
  {
    return Math.sqrt(Math.pow(v.x - x, 2) + Math.pow(v.y - y, 2));
  }

  /**
   * Returns the coordinates as a java.awt.Point by casting coordinates to integers.
   * @return the point with rounded vector coordinates
   */
  Point point()
  {
    return new Point((int)x, (int)y);
  }

  /**
   * Returns the coordinates as java.awt.geom.Point2D.Double.
   * @return the Point2D.Double with the vector coordinates
   */
  Point2D.Double pointD()
  {
    return new Point2D.Double(x, y);
  }

  /**
   * Returns a new vector that is the vector sum of the current vector and the given vector.
   * Be aware that the current vector is not modified.
   * @param v the vector to be added to the current vector
   * @return the sum of the two vectors
   */
  public GVector add(GVector v)
  {
    return new GVector(x + v.x, y + v.y);
  }

  /**
   * Returns a new vector with inverted coordinates.
   * Be aware that the current vector is not modified.
   * @return the inverted vector
   */
  public GVector invert()
  {
    return new GVector(-x, -y);
  }

  /**
   * Returns a new vector that is the vector difference of the current vector and the given vector.
   * Be aware that the current vector is not modified.
   * @param v the vector to be substracted from the current vector
   * @return the difference of the two vectors
   */
  public GVector sub(GVector v)
  {
    return new GVector(x - v.x, y - v.y);
  }

  /**
   * Returns a new vector that is the product by a scalar of the current vector and the given integer.
   * Be aware that the current vector is not modified.
   * @param b the integer scale factor
   * @return the vector that is scaled with the given integer
   */
  public GVector mult(int b)
  {
    return new GVector(x * b, y * b);
  }

  /**
   * Returns a new vector that is the product by a scalar of the current vector and the given float.
   * Be aware that the current vector is not modified.
   * @param b the float scale factor
   * @return the vector that is scaled with the given float
   */
  public GVector mult(float b)
  {
    return new GVector(x * b, y * b);
  }

  /**
   * Returns a new vector that is the product by a scalar of the current vector and the given double.
   * Be aware that the current vector is not modified.
   * @param b the double scale factor
   * @return the vector that is scaled with the given double
   */
  public GVector mult(double b)
  {
    return new GVector(x * b, y * b);
  }

  /**
   * Returns a new vector with the same coordinates as the current vector.
   * @return a clone of the current vector
   */
  public GVector clone()
  {
    return new GVector(x, y);
  }

  /**
   * Returns a string with the x-y-coordinates in the format (x, y).
   * @return a string representation of the vector
   */
  public String toString()
  {
    return "(" + x + ", " + y + ")";
  }

  /**
   * Returns true, if the current vector (casted to int values) points to a
   * non-transparant pixel of the given image.
   * @param imageCenter the vector to the center of the image
   * @param imageDirection the direction of the image (angle of edge ulx --> uly
   * with respect to the positive x-direction (0..2*pi))
   * @param isRotatable if true, the imageDirection is considered; otherwise
   * imageDirection = 0 is assumed
   * @return true, if the vector points to non-transparent pixels of the image
   */
  public boolean isIntersecting(GVector imageCenter, double imageDirection,
    BufferedImage image, boolean isRotatable)
  {
    return isIntersecting(imageCenter, imageDirection, image, isRotatable, new Point());
  }

  protected boolean isIntersecting(GVector imageCenter, double imageDirection,
    BufferedImage image, boolean isRotatable, Point pt)
    // imageDirection in degrees
  {
    // Transform to coordinate system CS in image center
    GVector v = clone();
    v = v.sub(imageCenter);

    // If distance outside circumcircle-> no collision
    int width = image.getWidth();
    int height = image.getHeight();
    double a = width / 2.0;
    double b = height / 2.0;
    if (v.magnitude2() > (a * a + b * b))
      return false;

    // CS axis parallel
    if (isRotatable)
      v.rotate(-Math.toRadians(imageDirection));
    // CS in upper left vertex
    GVector ul = new GVector(-a, -b);
    v = v.sub(ul);

    // Use integer coordinates
    int i = (int)v.x;
    int k = (int)v.y;
    // Check if point is on a non-transparent pixel
    if (i >= 0 && i < width && k >= 0 && k < height)  // point inside image
    {
      int color_sRGB = image.getRGB(i, k);
      int alpha = color_sRGB >>> 24;
      if (alpha > 0)  // non-transparent
      {
        pt.x = -width / 2 + i;
        pt.y = -height / 2 + k;
        return true;
      }
    }
    return false;
  }

  /**
   * Returns true, if the current vector is identical to the given vector.
   * @param v the vector to compare
   * @return true, if both vectors are identical
   */
  public boolean isEqual(GVector v)
  {
    if (x == v.x && y == v.y)
      return true;
    return false;
  }
}
