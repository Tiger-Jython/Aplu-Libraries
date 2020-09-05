// Vector3D.java

/*
 This software is part of the EV3JLib library.
 It is Open Source Free Software, so you may
 - run the code for any purpose
 - study how the code works and adapt it to your needs
 - integrate all or parts of the code in your own programs
 - redistribute copies of the code
 - improve the code and release your improvements to the public
 However the use of the code is entirely your responsibility.
 */
package ch.aplu.ev3;

/**
 * Class representing a three-dimensional vector with double coordinates (x, y, z).
 */
public class Vector3D
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
   * The public z-coordinate.
   */
  public double z;

  /**
   * Constructs a zero vector (with coordinates (0, 0, 0)).
   */
  public Vector3D()
  {
    this(0, 0, 0);
  }

  /**
   * Constructs a vector from given integer x-y-coordinates.
   * @param x the x-coordinate of the vector
   * @param y the y-coordinate of the vector
   * @param z the z-coordinate of the vector
   */
  public Vector3D(int x, int y, int z)
  {
    this((double)x, (double)y, (double)z);
  }

  /**
   * Constructs a vector from given float x-y-coordinates.
   * @param x the x-coordinate of the vector
   * @param y the y-coordinate of the vector
   * @param z the z-coordinate of the vector
   */
  public Vector3D(float x, float y, float z)
  {
    this((double)x, (double)y, (double)z);
  }

  /**
   * Constructs a vector from given double x-y-coordinates.
   * @param x the x-coordinate of the vector
   * @param y the y-coordinate of the vector
   * @param z the z-coordinate of the vector
   */
  public Vector3D(double x, double y, double z)
  {
    this.x = x;
    this.y = y;
    this.z = z;
  }

  /**
   * Returns the magnitude (length) of the vector.
   * @return the magnitude
   */
  public double magnitude()
  {
    return Math.sqrt(x * x + y * y + z * z);
  }

  /**
   * Returns the square of the magnitude, without squareroot calculation.
   * This is useful when performance is an issue.
   * @return the square of the magnitude
   */
  public double magnitude2()
  {
    return x * x + y * y + z * z;
  }

  /**
   * Modifies the vector to unit magnitude.
   */
  public void normalize()
  {
    double magnitude = magnitude();
    x = x / magnitude;
    y = y / magnitude;
    z = z / magnitude;
  }

  /**
   * Returns a new vector with magnitude 1 in the direction of the given vector.
   * @return a unit vector with same direction
   */
  public Vector3D getNormalized()
  {
    double magnitude = magnitude();
    return new Vector3D(x / magnitude, y / magnitude, z / magnitude);
  }

  /**
   * Returns the phi direction of the vector (range 0..2*pi). 
   * (Euler angle of projection in x-y-plane).
   * @return the direction of the vector (in radian, zero to x-axis, clockwise)
   */
  public double getPhi()
  {
    double phi = Math.atan2(y, x);
    if (phi >= 0)
      return phi;
    else
      return 2 * Math.PI + phi;
  }
  
    /**
   * Returns the phi direction of the vector (range 0..2*pi). 
   * (Euler angle elevation with respect to x-y-plane).
   * @return the elevation of the vector (in radian, positive in z-direction)
   */
  public double getTheta()
  {
    double a = Math.sqrt(x  * x  + y * y);
    double theta = Math.atan2(z, a);
    if (theta >= 0)
      return theta;
    else
      return 2 * Math.PI + theta;
  }

  /**
   * Returns the scalar product (dot product) of the current vector with the given vector.
   * @param v the vector to take for the dot product
   * @return the dot product
   */
  public double dot(Vector3D v)
  {
    return x * v.x + y * v.y + z * v.z;
  }

  /**
   * Returns the distance between the current vector and the given vector.
   * @param v the vector to take for the distance measurement
   * @return the distance (magnitude of the vector difference)
   */
  public double distanceTo(Vector3D v)
  {
    return Math.sqrt(Math.pow(v.x - x, 2) + Math.pow(v.y - y, 2) 
      + Math.pow(v.z - z, 2));
  }

  /**
   * Returns a new vector that is the vector sum of the current vector and the given vector.
   * Be aware that the current vector is not modified.
   * @param v the vector to be added to the current vector
   * @return the sum of the two vectors
   */
  public Vector3D add(Vector3D v)
  {
    return new Vector3D(x + v.x, y + v.y, z + v.z);
  }

  /**
   * Returns a new vector with inverted coordinates.
   * Be aware that the current vector is not modified.
   * @return the inverted vector
   */
  public Vector3D invert()
  {
    return new Vector3D(-x, -y, -z);
  }

  /**
   * Returns a new vector that is the vector difference of the current vector and the given vector.
   * Be aware that the current vector is not modified.
   * @param v the vector to be substracted from the current vector
   * @return the difference of the two vectors
   */
  public Vector3D sub(Vector3D v)
  {
    return new Vector3D(x - v.x, y - v.y, z - v.z);
  }

  /**
   * Returns a new vector that is the product by a scalar of the current vector and the given integer.
   * Be aware that the current vector is not modified.
   * @param b the integer scale factor
   * @return the vector that is scaled with the given integer
   */
  public Vector3D mult(int b)
  {
    return new Vector3D(x * b, y * b, z * b);
  }

  /**
   * Returns a new vector that is the product by a scalar of the current vector and the given float.
   * Be aware that the current vector is not modified.
   * @param b the float scale factor
   * @return the vector that is scaled with the given float
   */
  public Vector3D mult(float b)
  {
    return new Vector3D(x * b, y * b, z * b);
  }

  /**
   * Returns a new vector that is the product by a scalar of the current vector and the given double.
   * Be aware that the current vector is not modified.
   * @param b the double scale factor
   * @return the vector that is scaled with the given double
   */
  public Vector3D mult(double b)
  {
    return new Vector3D(x * b, y * b, z * b);
  }

  /**
   * Returns a new vector with the same coordinates as the current vector.
   * @return a clone of the current vector
   */
  public Vector3D clone()
  {
    return new Vector3D(x, y, z);
  }

  /**
   * Returns a string with the x-y-coordinates in the format (x,y,z).
   * @return a string representation of the vector
   */
  public String toString()
  {
    return "(" + x + "," + y + "," + z + ")";
  }

  /**
   * Returns true, if the current vector is identical to the given vector.
   * @param v the vector to compare
   * @return true, if both vectors are identical
   */
  public boolean isEqual(Vector3D v)
  {
    if (x == v.x && y == v.y && z == v.z)
      return true;
    return false;
  }
}
