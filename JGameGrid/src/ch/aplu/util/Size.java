// Size.java

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

import java.awt.*;

/**
 * The size of a window.
 */
public class Size
{
  protected int _width;
  protected int _height;

  /**
   * Construct a Size instance.
   * @param width    width of window
   * @param height   height of window
   */
  public Size(int width, int height)
  {
    _width = width;
    _height = height;
  }

  /**
   * Return width.
   */
  public int getWidth()
  {
    return _width;
  }

  /**
   * Return height.
   */
  public int getHeight()
  {
    return _height;
  }

}
