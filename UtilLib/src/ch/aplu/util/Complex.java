// Complex.java

/*
 This software is part of the Gidlet framework.
 It is Open Source Free Software, so you may
 - run the code for any purpose
 - study how the code works and adapt it to your needs
 - integrate all or parts of the code in your own programs
 - redistribute copies of the code
 - improve the code and release your improvements to the public
 However the use of the code is entirely your responsibility.
 */
package ch.aplu.util;

/**
 * This is a complex number class with the essential features
 * needed for computations. The real and imaginary
 * parts can be accessed directly for fast operation. (See
 * ComplexImm for an immutable version of a complex number
 * class.)
 *
 * Several of the common complex number operations are provided
 * as static methods. New instances of Complex are returned
 * by these methods.
 *
 * Code from book: Lindsey, Tolliver, Lindblad,
 * "JavaTech, an Introduction to Scientific and Technical Computing with Java"
 * with some minor modifications.
 **/
public class Complex
{
  // Properties
  public double real;
  public double img;

  /** Constructor that initializes the values. **/
  public Complex(double r, double i)
  {
    real = r;
    img = i;
  }

  /** Get method for real part. **/
  public double getReal()
  {
    return real;
  }

  /** Get method for imaginary part. **/
  public double getImg()
  {
    return img;
  }

  /** Define a complex add method. **/
  public void add(Complex cvalue)
  {
    real = real + cvalue.real;
    img = img + cvalue.img;
  }

  /** Define a complex subtract method. **/
  public void subtract(Complex cvalue)
  {
    real = real - cvalue.real;
    img = img - cvalue.img;
  }

  /**
   * Define a static add method that creates a
   * a new Complex object with the sum.
   **/
  public static Complex add(Complex cvalue1, Complex cvalue2)
  {
    double r = cvalue1.real + cvalue2.real;
    double i = cvalue1.img + cvalue2.img;
    return new Complex(r, i);
  }

  /** Define a static subtract method that creates a
   * a new Complex object equal to
   *
   *  cvalue1 - cvalue2.
   **/
  public static Complex subtract(Complex cvalue1, Complex cvalue2)
  {
    double r = cvalue1.real - cvalue2.real;
    double i = cvalue1.img - cvalue2.img;
    return new Complex(r, i);
  }

  /**
   * Check for the equality of this object with that of the argument.
   **/
  public boolean equals(Object obj)
  {
    if (obj == null)
      return false;
    if (obj == this)
      return true;
    if (obj.getClass() != Complex.class)
      return false;

    Complex c = (Complex)obj;
    return ((real == c.real)
      && (img == c.img));
  }

  /** 
   * Returns a hash code value for the object. 
   * In accordance with the general contract for hashCode().
   */
  public int hashCode()
  {
    Double tempReal = new Double(real);
    Double tempImaginary = new Double(img);
    int realHashCode = tempReal.hashCode();
    int imaginaryHashCode = tempImaginary.hashCode();
    return realHashCode ^ imaginaryHashCode;
  }

  /**
   * Provide the magnitude of the complex value.
   **/
  public double modulus()
  {
    return Math.sqrt(real * real + img * img);
  }

  /**
   * Multiply this complex object by the complex argument.
   **/
  public void multiply(Complex cvalue)
  {
    double r2 = real * cvalue.real - img * cvalue.img;
    double i2 = real * cvalue.img + img * cvalue.real;
    real = r2;
    img = i2;
  } // multiply

  /** Define a static multiply method that creates a
   * a new Complex object with the product.
   **/
  public static Complex multiply(Complex cvalue1, Complex cvalue2)
  {
    double r2 = cvalue1.real * cvalue2.real
      - cvalue1.img * cvalue2.img;
    double i2 = cvalue1.img * cvalue2.real
      + cvalue1.real * cvalue2.img;
    return new Complex(r2, i2);
  } // multiply

  /**
   * Divide this complex object by the complex argument.
   **/
  public void divide(Complex cvalue)
  {
    double denom = cvalue.real * cvalue.real
      + cvalue.img * cvalue.img;

    double r = real * cvalue.real
      + img * cvalue.img;

    double i = img * cvalue.real
      - real * cvalue.img;

    real = r / denom;
    img = i / denom;
  } // divide

  /** Define a static divide method that creates a
   * a new Complex object with the result of
   *
   *  cvalue1/cvalue2.
   **/
  public static Complex divide(Complex cvalue1, Complex cvalue2)
  {
    double denom = cvalue2.real * cvalue2.real
      + cvalue2.img * cvalue2.img;

    double r = cvalue1.real * cvalue2.real
      + cvalue1.img * cvalue2.img;

    double i = cvalue1.img * cvalue2.real
      - cvalue1.real * cvalue2.img;

    return new Complex(r / denom, i / denom);
  } // divide

  /** Return a string representation of the complex value. **/
  public String toString()
  {
    String img_sign = (img < 0) ? " - " : " + ";
    return (real + img_sign + img + "i");
  }

} // Complex