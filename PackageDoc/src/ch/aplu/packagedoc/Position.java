// Position.java

package ch.aplu.packagedoc;

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
