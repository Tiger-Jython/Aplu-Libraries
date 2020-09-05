// RobotContext.java

/*
 This software is part of the RobotSim library.
 It is Open Source Free Software, so you may
 - run the code for any purpose
 - study how the code works and adapt it to your needs
 - integrate all or parts of the code in your own programs
 - redistribute copies of the code
 - improve the code and release your improvements to the public
 However the use of the code is entirely your responsibility.

 Author: Aegidius Pluess, www.aplu.ch
 */
package ch.aplu.robotsim;

import ch.aplu.jgamegrid.*;
import java.awt.Point;
import java.util.*;
import java.awt.image.BufferedImage;
import javax.swing.JOptionPane;

/**
 * Class to select user defined initial conditions of the
 * playground and the Nxt or EV3 robot.
 */
public class RobotContext
{
  protected static String imageName = null;
  protected static Location startLocation = new Location(250, 250);
  protected static double startDirection = -90;
  protected static boolean isNavigationBar = false;
  protected static boolean isStatusBar = false;
  protected static int statusBarHeight;
  protected static ArrayList<Obstacle> obstacles = new ArrayList<Obstacle>();
  protected static ArrayList<Location> obstacleLocations = new ArrayList<Location>();
  protected static ArrayList<Target> targets = new ArrayList<Target>();
  protected static ArrayList<Location> targetLocations = new ArrayList<Location>();
  protected static ArrayList<Torch> torches = new ArrayList<Torch>();
  protected static ArrayList<Shadow> shadows = new ArrayList<Shadow>();
  protected static boolean isRun = true;
  protected static int xLoc = -1;
  protected static int yLoc = -1;
  protected static boolean isError = false;
  protected static GGMouseListener mouseListener = null;

  /**
   * Box obstacle.
   */
  public static Obstacle box = new Obstacle("sprites/box.gif");
  /**
   * Channel obstacle.
   */
  public static Obstacle channel = new Obstacle("sprites/channel.gif");

  /**
   * Creates a RobotContext instance.
   */
  public RobotContext()
  {
  }

  /**
   * Registers a mouse listener to get left mouse button press, 
   * release and drag events
   * @param listener the GGMouseListener to register
   */
  public void addMouseListener(GGMouseListener listener)
  {
    mouseListener = listener;
  }

  /** 
   * Initializes the static context. This may be necessary in Jython
   * environment, where static contexts are not reloaded automatically. 
   */
  public void init()
  {
    imageName = null;
    startLocation = new Location(250, 250);
    startDirection = -90;
    isNavigationBar = false;
    isStatusBar = false;

    if (obstacles == null)
      obstacles = new ArrayList<Obstacle>();
    else
      obstacles.clear();

    if (obstacleLocations == null)
      obstacleLocations = new ArrayList<Location>();
    else
      obstacleLocations.clear();

    if (targets == null)
      targets = new ArrayList<Target>();
    else
      targets.clear();

    if (targetLocations == null)
      targetLocations = new ArrayList<Location>();
    else
      targetLocations.clear();

    if (torches == null)
      torches = new ArrayList<Torch>();
    else
      torches.clear();

    isRun = true;
    xLoc = -1;
    yLoc = -1;
    
    RobotInstance.setRobot(null);
  }

  /**
   * Use the given image as background (playground size 501 x 501).
   * (Only one image can be used as background; for a complex background,
   * merge your images with an image editor)
   * @param filename the image file to use as background.
   */
  public static void useBackground(String filename)
  {
    checkRobot();
    imageName = filename;
  }

  /**
   * Sets the Nxt starting position (x-y-coordinates 0..500, origin at upper left).
   * @param x the x-coordinate of the starting position
   * @param y the y-coordinate of the starting position
   */
  public static void setStartPosition(int x, int y)
  {
    checkRobot();
    startLocation = new Location(x, y);
  }

  /**
   * Sets the Nxt starting direction (zero to EAST).
   * @param direction the starting direction in degrees)
   */
  public static void setStartDirection(double direction)
  {
    checkRobot();
    startDirection = direction;
  }

  /**
   * Sets the location of the playground (pixel coordinates of the upper left vertex).
   * @param x the x-pixel-coordinate of the upper left vertex (positive to the right)
   * @param y the y-pixel-coordinate of the upper left vertex (positive to the bottom)
   */
  public static void setLocation(int x, int y)
  {
    checkRobot();
    xLoc = x;
    yLoc = y;
  }

  /**
   * Defines the given sprite image to be used as touch obstacle. It will be shown at the given
   * position. More than one obstacle may be defined. The touch is detected by 
   * a JGameGrid collision with a non-transparent pixel of the obstacle sprite image.
   * @param filename the image file of the obstacle
   * @param x the x-coordinate of the image center
   * @param y the y-coordinate of the image center
   */
  public static void useObstacle(String filename, int x, int y)
  {
    checkRobot();
    Obstacle obstacle = new Obstacle(filename);
    obstacles.add(obstacle);
    obstacleLocations.add(new Location(x, y));
  }

  /**
   * Defines the given buffered image to be used as touch obstacle. It will be shown at the given
   * position. More than one obstacle may be defined. The touch is detected by 
   * a JGameGrid collision with a non-transparent pixel of the obstacle sprite image.
   * @param bi the buffered image of the obstacle
   * @param x the x-coordinate of the image center
   * @param y the y-coordinate of the image center
   */
  public static void useObstacle(BufferedImage bi, int x, int y)
  {
    checkRobot();
    Obstacle obstacle = new Obstacle(bi);
    obstacles.add(obstacle);
    obstacleLocations.add(new Location(x, y));
  }

  /**
   * Defines the given obstacle to be used as touch obstacle. It will be shown at the given
   * position. More than one obstacle may be defined. The touch is detected by 
   * a JGameGrid collision with a non-transparent pixel of the obstacle sprite image.
   * @param obstacle the obstacle to use
   * @param x the x-coordinate of the image center
   * @param y the y-coordinate of the image center
   */
  public static void useObstacle(Obstacle obstacle, int x, int y)
  {
    checkRobot();
    obstacles.add(obstacle);
    obstacleLocations.add(new Location(x, y));
  }

  /**
   * Defines the given obstacle to be used as touch obstacle. It will be shown
   * at the center of the simulation window. More than one obstacle may be defined. 
   * The touch is detected by a JGameGrid collision with a non-transparent pixel 
   * of the obstacle sprite image.
   * @param obstacle the obstacle to use
   */
  public static void useObstacle(Obstacle obstacle)
  {
    checkRobot();
    obstacles.add(obstacle);
    obstacleLocations.add(new Location(250, 250));
  }

  /**
   * Defines the given GGBitmap to be used as touch obstacle. It will be shown at the given
   * position. More than one obstacle may be defined. The touch is detected by 
   * a JGameGrid collision with a non-transparent pixel of the obstacle sprite image.
   * @param bm the GGBitmap to be used as obstacle
   * @param x the x-coordinate of the image center
   * @param y the y-coordinate of the image center
   */
  public static void useObstacle(GGBitmap bm, int x, int y)
  {
    checkRobot();
    useObstacle(bm.getBufferedImage(), x, y);
  }

  /**
   * Creates a target for the ultrasonic sensor using the given sprite image.
   * The LegoRobot constructor displays all targets known at this time.
   * Targets are scaned by the ultrasonic sensor using the mesh triangles.
   * The mesh triangles contain the image center and two successive mesh points.
   * The coordinate system for the mesh has its origin in the center location with
   * pixel coordinates x to the right and y to the bottom.
   * @param filename the image file of the target
   * @param mesh the integer array of mesh points (as x-y-coordinates array) (at least 2)
   * @param x the x-coordinate of the image center
   * @param y the y-coordinate of the image center
   * @return the reference of the created target
   */
  public static Target useTarget(String filename, int[][] mesh, int x, int y)
  {
    checkRobot();
    int size = mesh.length;
    Point[] points = new Point[size];

    for (int i = 0; i < size; i++)
    {
      Point pt = new Point(mesh[i][0], mesh[i][1]);
      points[i] = pt;
    }
    return useTarget(filename, points, x, y);
  }

  /**
   * Creates a target for the ultrasonic sensor using the given sprite image.
   * The LegoRobot constructor displays all targets known at this time.
   * Targets are scaned by the ultrasonic sensor using the mesh triangles.
   * The mesh triangles contain the image center and two successive mesh points.
   * The coordinate system for the mesh has its origin in the center location with
   * pixel coordinates x to the right and y to the bottom.
   * @param filename the image file of the target
   * @param mesh the mesh points (at least 2)
   * @param x the x-coordinate of the image center
   * @param y the y-coordinate of the image center
   * @return the reference of the created target
   */
  public static Target useTarget(String filename, Point[] mesh, int x, int y)
  {
    checkRobot();
    if (mesh.length < 2)
    {
      JOptionPane.showMessageDialog(null,
        "Ultrasonic target mesh must contain at least 2 points",
        "Fatal Error", JOptionPane.ERROR_MESSAGE);
      isError = true;
      return null;
    }

    Target target = new Target(filename, mesh);
    targets.add(target);
    targetLocations.add(new Location(x, y));
    return target;
  }

  /**
   * Creates a target for the ultrasonic sensor using the given buffered image.
   * The LegoRobot constructor displays all targets known at this time.
   * Targets are scaned by the ultrasonic sensor using the mesh triangles.
   * The mesh triangles contain the image center and two successive mesh points.
   * The coordinate system for the mesh has its origin in the center location with
   * pixel coordinates x to the right and y to the bottom.
   * @param bi the buffered image of the target
   * @param mesh the mesh points (at least 2)
   * @param x the x-coordinate of the image center
   * @param y the y-coordinate of the image center
   * @return the reference of the created target
   */
  public static Target useTarget(BufferedImage bi, Point[] mesh, int x, int y)
  {
    checkRobot();
    if (mesh.length < 2)
    {
      JOptionPane.showMessageDialog(null,
        "Ultrasonic target mesh must contain at least 2 points",
        "Fatal Error", JOptionPane.ERROR_MESSAGE);
      isError = true;
      return null;
    }

    Target target = new Target(bi, mesh);
    targets.add(target);
    targetLocations.add(new Location(x, y));
    return target;
  }

  /**
   * Creates a target for the ultrasonic sensor using the given GGBitmap.
   * The LegoRobot constructor displays all targets known at this time.
   * Targets are scaned by the ultrasonic sensor using the mesh triangles.
   * The mesh triangles contain the image center and two successive mesh points.
   * The coordinate system for the mesh has its origin in the center location with
   * pixel coordinates x to the right and y to the bottom.
   * @param bm the GGBitmap to be used as target
   * @param mesh the mesh points (at least 2)
   * @param x the x-coordinate of the image center
   * @param y the y-coordinate of the image center
   * @return the reference of the created target
   */
  public static Target useTarget(GGBitmap bm, Point[] mesh, int x, int y)
  {
    checkRobot();
    return useTarget(bm.getBufferedImage(), mesh, x, y);
  }

  /**
   * Defines a clone of the given target to be used as target.
   * It will be shown at the given position. More than one target may be defined. 
   * The target is detected by the ultrasonic sensor using the mesh triangles.
   * @param target the target to define the clone
   * @param x the x-coordinate of the image center
   * @param y the y-coordinate of the image center
   * @return the reference of the target clone
   */
  public static Target useTarget(Target target, int x, int y)
  {
    checkRobot();
    Target tmp = null;
    if (target.getImageName() != null)
      tmp = new Target(target.getImageName(), target.getMesh());
    if (target.getBufferedImage() != null)
      tmp = new Target(target.getBufferedImage(), target.getMesh());

    targets.add(tmp);
    targetLocations.add(new Location(x, y));
    return tmp;
  }

  /**
   * Defines a light source (torch) with given power at given position.
   * More than one torch may be defined. The torches are used to determine the
   * illumination of light sensors (light strength diminishes with 1/r^2).
   * Mouse pick and drag is enabled by default.
   * @param power the power of the light source (arbitrary units)
   * @param x the x-coordinate of the torch
   * @param y the y-coordinate of the torch
   * @param z the z-coordinate of the torch (height above simulation plane)
   */
  public static void useTorch(double power, int x, int y, int z)
  {
    checkRobot();
    Torch torch = new Torch(power, new Location(x, y), z);
    torches.add(torch);
  }

  /**
   * Defines a shadow with given size and position.
   * The shadows are used to determine the
   */
  public static void useShadow(int ulx, int uly, int lrx, int lry)
  {
    checkRobot();
    Shadow shadow = new Shadow(new Location(ulx, uly), new Location(lrx, lry));
    shadows.add(shadow);
  }

  /**
   * Shows the navigation bar of the GameGrid.
   * @param doRun if true, runs the simulation immediatetly; otherwise the start
   * button must be hit to run the simulation
   */
  public static void showNavigationBar(boolean doRun)
  {
    checkRobot();
    isNavigationBar = true;
    isRun = doRun;
  }

  /**
   * Shows the navigation bar of the GameGrid and runs the simulation immediately.
   */
  public static void showNavigationBar()
  {
    checkRobot();
    showNavigationBar(true);
  }

  /**
   * Shows a status bar with given height. System.out is redirected to the
   * status bar. All print(), println(), printf() methods are available. Each
   * invocation erases the old content.
   * @param height the height of the status window in pixels.
   */
  public static void showStatusBar(int height)
  {
    checkRobot();
    statusBarHeight = height;
    isStatusBar = true;
  }

  /**
   * Displays the given text in the status bar (if available).
   * @param text the text to display.
   */
  public static void setStatusText(String text)
  {
    if (LegoRobot.getGameGrid() != null)
      LegoRobot.getGameGrid().setStatusText(text);
  }

  public static void checkRobot()
  {
    if (RobotInstance.getRobot() != null)
    {
      if (GameGrid.getClosingMode() == GameGrid.ClosingMode.TerminateOnClose
        || GameGrid.getClosingMode() == GameGrid.ClosingMode.AskOnClose)
        System.exit(1);
      if (GameGrid.getClosingMode() == GameGrid.ClosingMode.DisposeOnClose)
        throw new RuntimeException("\nInvoke RobotContext instructions before Robot instance creation.");
    }
  }
}
