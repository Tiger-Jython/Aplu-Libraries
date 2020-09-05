// Position.java

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

/**
 * Position of a window.
 */
public class Position
{
  private int _ulx;
  private int _uly;

  /**
   * Construct a Position instance with given coordinates.
   * @param ulx   upper left x-coordinate of window
   * @param uly   upper left y-coordinate of window
   */
  public Position(int ulx, int uly)
  {
    _ulx = ulx;
    _uly = uly;
  }

  /**
   * Return x-coordinate.
   */
  public int getUlx()
  {
    return _ulx;
  }

  /**
   * Return y-coordinate.
   */
  public int getUly()
  {
    return _uly;
  }

}
