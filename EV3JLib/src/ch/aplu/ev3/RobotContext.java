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

 Author: Aegidius Pluess, www.aplu.ch
 */
package ch.aplu.ev3;

import java.awt.Point;
import java.awt.image.BufferedImage;

/**
 * Dummy class to make EV3JLib source compatible with RobotSim. All methods are empty.
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
  {
  }

  /** 
   * Initializes the static context. This may be necessary in Python
   * environment, where static contexts is not reloaded automatically. 
   * Empty method for compatibility with NxtSim.
   */
  public static void init()
  {
  }

  /**
   * Use the give image as background (playground size 501 x 501).
   * Empty method for compatibility with NxtSim.
   * @param filename the image file to use as background.
   */
  public static void useBackground(String filename)
  {
  }

  /**
   * Sets the Nxt starting position (x-y-coordinates 0..500, origin at upper left).
   * Empty method for compatibility with NxtSim.
   * @param x the x-coordinate of the starting position
   * @param y the y-coordinate of the starting position
   */
  public static void setStartPosition(int x, int y)
  {
  }

  /**
   * Sets the Nxt starting direction (zero to EAST).
   * Empty method for compatibility with NxtSim.
   * @param direction the starting direction in degrees)
   */
  public static void setStartDirection(double direction)
  {
  }

  /**
   * Sets the location of the playground (pixel coordinates of the upper left vertex).
   * Empty method for compatibility with NxtSim.
   * @param x the x-pixel-coordinate of the upper left vertex (positive to the right)
   * @param y the y-pixel-coordinate of the upper left vertex (positive to the bottom)
   */
  public static void setLocation(int x, int y)
  {
  }
  
   /**
   * Defines the given sprite image to be used as touch obstacle. It will be shown at the given
   * position. More than one obstacle may be defined. The touch is detected by 
   * a JGameGrid collision with a non-transparent pixel of the obstacle sprite image.
   * Empty method for compatibility with NxtSim.
   * @param filename the image file of the obstacle
   * @param x the x-coordinate of the image center
   * @param y the y-coordinate of the image center
   */
  public static void useObstacle(String filename, int x, int y)
  {
  }

  /**
   * Defines the given buffered image to be used as touch obstacle. It will be shown at the given
   * position. More than one obstacle may be defined. The touch is detected by 
   * a JGameGrid collision with a non-transparent pixel of the obstacle sprite image.
   * Empty method for compatibility with NxtSim.
   * @param bi the buffered image of the obstacle
   * @param x the x-coordinate of the image center
   * @param y the y-coordinate of the image center
   */
  public static void useObstacle(BufferedImage bi, int x, int y)
  {
  }

  /**
   * Defines the given obstacle to be used as touch obstacle. It will be shown at the given
   * position. More than one obstacle may be defined. The touch is detected by 
   * a JGameGrid collision with a non-transparent pixel of the obstacle sprite image.
   * Empty method for compatibility with NxtSim.
   * @param obstacle the obstacle to use
   * @param x the x-coordinate of the image center
   * @param y the y-coordinate of the image center
   */
  public static void useObstacle(Obstacle obstacle, int x, int y)
  {
  }

  /**
   * Defines the given obstacle to be used as touch obstacle. It will be shown
   * at the center of the simulation window. More than one obstacle may be defined. 
   * The touch is detected by a JGameGrid collision with a non-transparent pixel 
   * of the obstacle sprite image.
   * Empty method for compatibility with NxtSim.
   * @param obstacle the obstacle to use
   */
  public static void useObstacle(Obstacle obstacle)
  {
  }

  /**
   * Defines the given GGBitmap to be used as touch obstacle. It will be shown at the given
   * position. More than one obstacle may be defined. The touch is detected by 
   * a JGameGrid collision with a non-transparent pixel of the obstacle sprite image.
   * Empty method for compatibility with NxtSim.
   * @param bm the GGBitmap to be used as obstacle
   * @param x the x-coordinate of the image center
   * @param y the y-coordinate of the image center
   */
  public static void useObstacle(GGBitmap bm, int x, int y)
  {
  }

  /**
   * Creates a target for the ultrasonic sensor using the given sprite image.
   * The NxtRobot constructor displays all targets known at this time.
   * Targets are scaned by the ultrasonic sensor using the mesh triangles.
   * The mesh triangles contain the image center and two successive mesh points.
   * The coordinate system for the mesh has its origin in the center location with
   * pixel coordinates x to the right and y to the bottom.
   * Empty method for compatibility with NxtSim.
   * @param filename the image file of the target
   * @param mesh the integer array of mesh points (as x-y-coordinates array) (at least 2)
   * @param x the x-coordinate of the image center
   * @param y the y-coordinate of the image center
   * @return the reference of the created target
   */
  public static Target useTarget(String filename, int[][] mesh, int x, int y)
  {
    return null;
  }

  /**
   * Creates a target for the ultrasonic sensor using the given sprite image.
   * The NxtRobot constructor displays all targets known at this time.
   * Targets are scaned by the ultrasonic sensor using the mesh triangles.
   * The mesh triangles contain the image center and two successive mesh points.
   * The coordinate system for the mesh has its origin in the center location with
   * pixel coordinates x to the right and y to the bottom.
   * Empty method for compatibility with NxtSim.
   * @param filename the image file of the target
   * @param mesh the mesh points (at least 2)
   * @param x the x-coordinate of the image center
   * @param y the y-coordinate of the image center
   * @return the reference of the created target
   */
  public static Target useTarget(String filename, Point[] mesh, int x, int y)
  {
    return null;
  }

  /**
   * Creates a target for the ultrasonic sensor using the given buffered image.
   * The NxtRobot constructor displays all targets known at this time.
   * Targets are scaned by the ultrasonic sensor using the mesh triangles.
   * The mesh triangles contain the image center and two successive mesh points.
   * The coordinate system for the mesh has its origin in the center location with
   * pixel coordinates x to the right and y to the bottom.
   * Empty method for compatibility with NxtSim.
   * @param bi the buffered image of the target
   * @param mesh the mesh points (at least 2)
   * @param x the x-coordinate of the image center
   * @param y the y-coordinate of the image center
   * @return the reference of the created target
   */
  public static Target useTarget(BufferedImage bi, Point[] mesh, int x, int y)
  {
    return null;
  }

  /**
   * Creates a target for the ultrasonic sensor using the given GGBitmap.
   * The NxtRobot constructor displays all targets known at this time.
   * Targets are scaned by the ultrasonic sensor using the mesh triangles.
   * The mesh triangles contain the image center and two successive mesh points.
   * The coordinate system for the mesh has its origin in the center location with
   * pixel coordinates x to the right and y to the bottom.
   * Empty method for compatibility with NxtSim.
   * @param bm the GGBitmap to be used as target
   * @param mesh the mesh points (at least 2)
   * @param x the x-coordinate of the image center
   * @param y the y-coordinate of the image center
   * @return the reference of the created target
   */
  public static Target useTarget(GGBitmap bm, Point[] mesh, int x, int y)
  {
    return null;
  }

  /**
   * Defines a clone of the given target to be used as target.
   * It will be shown at the given position. More than one target may be defined. 
   * The target is detected by the ultrasonic sensor using the mesh triangles.
   * Empty method for compatibility with NxtSim.
   * @param target the target to define the clone
   * @param x the x-coordinate of the image center
   * @param y the y-coordinate of the image center
   * @return the reference of the target clone
   */
  public static Target useTarget(Target target, int x, int y)
  {
    return null;
  }

  /**
   * Shows a status bar with given height. System.out is redirected to the
   * status bar. All print(), println(), printf() methods are available. Each
   * invocation erases the old content.
   * Empty method for compatibility with NxtSim.
   * @param height the height of the status window in pixels.
   */
  public static void showStatusBar(int height)
  {
  }

  /**
   * Displays the given text in the status bar (if available).
   * Empty method for compatibility with NxtSim.
   * @param text the text to display.
   */
  public static void setStatusText(String text)
  {
  }
  
}

 