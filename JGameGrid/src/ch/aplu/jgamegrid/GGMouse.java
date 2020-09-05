// GGMouse.java

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

/**
 * Singleton class used to report mouse events.
 * For rButton events are triggered by mouse button 2 or 3.
 * Be aware that for some mouse events the reported mouse coordinates
 * are not strictly restricted to the display area. Therefore GameGrid.toLocation()
 * may report cell indices outside the visible grid. Use GameGrid.isInGrid() to test
 * if they are valid grid locations or use GameGrid.toLocationsInGrid()
 * in order to get only valid grid locations.
 */
public class GGMouse
{
  private static GGMouse mouse;
  private GGMouseListener listener;
  private int x;
  private int y;
  private int evt = idle;

  /**
   * No mouse event occured.
   */
  public static final int idle = 0;

  /**
   * Left mouse button down.
   */
  public static final int lPress = 1;

  /**
   * Left mouse button up.
   */
  public static final int lRelease = 2;

  /**
   * Left mouse button down and up in a quick sequence.<br>
   * Press, release, click are also generated in this order.
   */
  public static final int lClick = 4;

  /**
   * Left mouse button down, up, down, up in a quick sequence.<br>
   * Press, release, press, release, double-click is generated in this order.<br>
   */
  public static final int lDClick = 8;

  /**
   * Left mouse button down and move.
   * Because many events are generated, use it with precaution.
   * getX(), getY() may report points outside the display area.
   * No move events generated.
   */
  public static final int lDrag = 16;

  /**
   * Right mouse button down.
   */
  public static final int rPress = 32;

  /**
   * Right mouse button up.
   */
  public static final int rRelease = 64;

  /**
   * Right mouse button down and up in a quick sequence.<br>
   * Press, release, click are also generated in this order.
   */
  public static final int rClick = 128;

/**
   * Right mouse button down, up, down, up in a quick sequence.<br>
   * Press, release, press, release, double-click is generated in this order.<br>
   */
    public static final int rDClick = 256;

  /**
   * Right mouse button down and move.
   * Because many events are generated, use it with precaution.
   * getX(), getY() may report points outside the display area.
   * No move events generated.
   */
  public static final int rDrag = 512;

  /**
   * Mouse cursor enters the window.
   * getX(), getY() reports a point inside the display area but not necessarily
   * at the border.
   */
  public static final int enter = 1024;

  /**
   * Mouse cursor leaves the window.
   * getX(), getY() may report a point outside the display area.
   */
  public static final int leave = 2048;

  /**
   * Mouse cursor is moved inside the window.
   * Because many events are generated, use it with precaution.
   * getX(), getY() may report points outside the display area.
   */
  public static final int move = 4096;

  // private ctor
  private GGMouse()
  {
    this.listener = null;
    this.evt = idle;
    this.x = 0;
    this.y = 0;
  }

  /**
   * Creates a GGMouse instance with default values for the instance variables.
   */
  public static GGMouse create()
  {
    mouse = new GGMouse();
    return mouse;
  }

  /**
   * Creates a GGMouse instance with given values for the instance variables.
   * @param listener the listener instances that created the event
   * @param evt the event type (one of the predefined constants)
   * @param x the x-coordinate where the event occurred
   * @param y the y-coordinate where the event occurred
   * @return a GGMouse instance reference
   */
  public static GGMouse create(GGMouseListener listener, int evt, int x, int y)
  {
    mouse = new GGMouse();
    mouse.setValues(listener, evt, x, y);
    return mouse;
  }

  protected void setValues(GGMouseListener listener, int evt, int x, int y)
  {
    this.listener = listener;
    this.evt = evt;
    this.x = x;
    this.y = y;
  }

  /**
   * Retrieves the GGMouseListener reference that created the event.
   * @return a reference to the GGMouseListener that created the event
   */
  public GGMouseListener getSource()
  {
    return listener;
  }

  /**
   * Retrieves the current event type as integer.
   * @return one of the predefined constants
   */
  public int getEvent()
  {
    return evt;
  }

  /**
   * Retrieves the current x-coordinate of the mouse cursor with respect to the upper left corner
   * of the visible area in pixels. Be aware that for some events the value corresponds to a point outside the
   * visible area.
   * @return x-coordinate (pixel index) where the event occured
   */
  public int getX()
  {
    return x;
  }

  /**
   * Returns the event type as string.
   * @return the event type
   */
  public String getEventType()
  {
    String info = "";
    if ((evt & idle) != 0)
      info += "idle;";
    if ((evt & lPress) != 0)
      info += "lPress;";
    if ((evt & lRelease) != 0)
      info += "lRelease;";
    if ((evt & lClick) != 0)
      info += "lClick;";
    if ((evt & lDClick) != 0)
      info += "lDClick;";
    if ((evt & lDrag) != 0)
      info += "lDrag;";
    if ((evt & rPress) != 0)
      info += "rPress;";
    if ((evt & rRelease) != 0)
      info += "rRelease;";
    if ((evt & rClick) != 0)
      info += "rClick;";
    if ((evt & rDClick) != 0)
      info += "rDClick;";
    if ((evt & rDrag) != 0)
      info += "rDrag;";
    if ((evt & enter) != 0)
      info += "enter;";
    if ((evt & leave) != 0)
      info += "leave;";
    if ((evt & move) != 0)
      info += "move;";
    if (info.length() > 0)
      info = info.substring(0, info.length() - 1);  // Remove trailing ;
    return info;
  }

  /**
   * Retrieves the current y-coordinate of the mouse cursor with respect to the upper left corner
   * of the visible area in pixels. Be aware that for some events the value corresponds to a point outside the
   * visible area.
   * @return y-coordinate (pixel index) where the event occured
   */
  public int getY()
  {
    return y;
  }
}
