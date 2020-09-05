// RobotContext.java

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
 * Dummy class to make EV3JLibA source compatible with RobotSim. 
 * All methods are empty.
 */
public class RobotContext
{
  /**
   * Box obstacle.
   */
  public static Obstacle box = null;
  /**
   * Channel obstacle.
   */
  public static Obstacle channel = null;

  private RobotContext()
  {}

  /**
   * Use the give image as background (playground size 501 x 501).
   * @param filename the image file to use as background.
   */
  public static void useBackground(String filename)
  {
  }

  /**
   * Sets the EV3 starting position (x-y-coordinates 0..500, origin at upper left).
   * @param x the x-coordinate of the starting position
   * @param y the y-coordinate of the starting position
   */
  public static void setStartPosition(int x, int y)
  {
  }

  /**
   * Sets the EV3 starting direction (zero to EAST).
   * @param direction the starting direction in degrees)
   */
  public static void setStartDirection(double direction)
  {
  }

 /**
  * Sets the location of the playground (pixel coordinates of the upper left vertex).
  * @param x the x-pixel-coordinate of the upper left vertex (positive to the right)
  * @param y the y-pixel-coordinate of the upper left vertex (positive to the bottom)
  */
  public static void setLocation(int x, int y)
  {
  }

  /**
   * Defines the give images as an obstacle. It will be shown at the given
   * position. More than one obstacle may be defined.
   * @param filename the image file of the obstacle
   * @param x the x-coordinate of the image center
   * @param y the y-coordinate of the image center
   */
  public static void useObstacle(String filename, int x, int y)
  {
  }

  /**
   * Uses the given obstacle at the center of the playground. Mainly used for
   * predefined obstacles.
   * @param obstacle the obstacle to use.
   */
  public static void useObstacle(Obstacle obstacle)
  {
  }

  /**
   * Shows the navigation bar of the GameGrid.
   * @param doRun if true, runs the simulation immediatetly; otherwise the start
   * button must be hit to run the simulation
   */
  public static void showNavigationBar(boolean doRun)
  {
  }

  /**
   * Shows the navigation bar of the GameGrid and runs the simulation immediately.
   */
  public static void showNavigationBar()
  {
  }
}
