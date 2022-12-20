// Size.java

package ch.aplu.packagedoc;

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
