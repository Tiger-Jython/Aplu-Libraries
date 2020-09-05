// UltrasonicSensor.java

/*
 This software is part of the RaspiSim library.
 It is Open Source Free Software, so you may
 - run the code for any purpose
 - study how the code works and adapt it to your needs
 - integrate all or parts of the code in your own programs
 - redistribute copies of the code
 - improve the code and release your improvements to the public
 However the use of the code is entirely your responsibility.

 Author: Aegidius Pluess, www.aplu.ch
 */
package ch.aplu.raspisim;

import ch.aplu.jgamegrid.*;
import java.awt.Color;
import java.awt.Point;
import ch.aplu.util.X11Color;

/**
 * Class that represents a ultrasonic sensor. The sensor detects targets
 in a cone with the apex at the current LegoRobot location. The cone's axis 
 orientation  may be selected using the port parameter (forward, 
 left or backward  direction of the current LegoRobot direction). The beam
 opening angle (beam width) is fixed to the value ultrasonicBeamWidth defined
 in ShareConstants.java. Targets normally have a visible actor sprite image, but
 the target detection algorithms does not use it. The detection is based on 
 a mesh of triangles defined when the target is created.<br><br>
 * 
 * Targets may dynamically change (modification of their location, 
 * new targets added, existing targets removed) because the current target 
 * layout is used each time the getDistance() method is called.<br><br>
 * 
 * Only one ultrasonic sensor is supported.
 */
public class UltrasonicSensor extends Part
{
  private Robot robot;
  private final double beamWidth
    = Math.toRadians(SharedConstants.ultrasonicBeamWidth);
//  private static final Location pos = new Location(-18, -1);
  private static final Location pos = new Location(20, 0);
  private UltrasonicListener ultrasonicListener = null;
  private double triggerLevel;
  private volatile boolean isFarNotified = false;
  private volatile boolean isNearNotified = false;
  private Color sectorColor = null;
  private Color meshTriangleColor = null;
  private Color proximityCircleColor = null;

  /**
   * The port selection determines the position of the sensor and
   * the direction of the beam axis.
   * Creates a sensor instance connected to the given port.
   * Only one sensor is allowed.
   */
  public UltrasonicSensor()
  {
    super("sprites/dummy.gif", pos);
    robot = RobotInstance.getRobot();
    if (robot == null)
      // Defer addPart() to robot ctor
      RobotInstance.partsToAdd.add(this);
    else
      robot.addPart(this);
  }

  protected void cleanup()
  {
  }

  /**
   * Registers the given UltrasonicListener to detect crossing the given trigger triggerLevel.
   * @param listener the UltrasonicListener to register
   * @param triggerLevel the distance value used as trigger level
   */
  public void addUltrasonicListener(UltrasonicListener listener, int triggerLevel)
  {
    ultrasonicListener = listener;
    this.triggerLevel = triggerLevel;
  }

  /**
   * Registers the given UltrasonicListener with default trigger triggerLevel 20.
   * @param listener the UltrasonicListener to register
   */
  public void addUltrasonicListener(UltrasonicListener listener)
  {
    addUltrasonicListener(listener, 20);
  }

  /**
   * Sets a new trigger level and returns the previous one.
   * @param triggerLevel the new trigger level
   */
  public void setTriggerLevel(double triggerLevel)
  {
    this.triggerLevel = triggerLevel;
  }

  /**
   * Returns the distance to the nearest target object. The distance must
   * be adapted to the real robot's environment (the real ultrasonic sensor
   * returns distances in cm in cm (-1 corresponds to no target or error)
   * @return the distance (in pixels of the 501x501 pixel window); 
   * -1 of no target in range or robot inside a target
   */
  public double getDistance()
  {
    RobotInstance.checkRobot();
    Tools.delay(1);
    synchronized (RobotContext.targets)
    {
      int value = -1;
      Location loc = getLocation();
      double direction = getDirection();
      GGVector center = new GGVector(loc.x, loc.y);
      GGVector dir = new GGVector(1000 * Math.cos(Math.toRadians(direction)),
        1000 * Math.sin(Math.toRadians(direction)));
      dir = center.add(dir);
      ViewingCone cone = new ViewingCone(center, dir, beamWidth, true);
      if (sectorColor != null)
        cone.drawCone(gameGrid.getBg(), sectorColor);
      if (RobotContext.targets.size() == 0)  // No target
      {
        // Erase old proximityCircle when last target is removed
        if (proximityCircleColor != null)
          cone.drawProximityCircle(gameGrid.getBg(), value, proximityCircleColor);
      }
      else
      {
        for (Target target : RobotContext.targets)
        {
          // Triangles created dynamically, because target may change location
          Point[] mesh = target.getMesh();
          GGVector targetCenter = new GGVector(target.getX(), target.getY());
          int size = mesh.length;
          for (int i = 0; i < size - 1; i++)
          {
            Triangle t = new Triangle(
              targetCenter,
              targetCenter.add(new GGVector(mesh[i])),
              targetCenter.add(new GGVector(mesh[i + 1])));
            if (meshTriangleColor != null)
              t.drawTriangle(gameGrid.getBg(), meshTriangleColor);
            cone.addObstacle(t);
          }
          Triangle t = new Triangle(
            targetCenter,
            targetCenter.add(new GGVector(mesh[size - 1])),
            targetCenter.add(new GGVector(mesh[0])));
          if (meshTriangleColor != null)
            t.drawTriangle(gameGrid.getBg(), meshTriangleColor);
          cone.addObstacle(t);
          double measure = (int)cone.getDistanceToClosestObstacle();
          if (measure == 0)
            value = -1;
          else
            value = (int)(measure + 0.5);

          if (proximityCircleColor != null)
            cone.drawProximityCircle(gameGrid.getBg(), value, proximityCircleColor);
        }
      }
      return (double)value;
    }
  }

  /**
   * Sets the color of the triangle mesh lines.
   * @param color the color of the mesh; if null, the mesh is not shown (default)
   */
  public void setMeshTriangleColor(Color color)
  {
    meshTriangleColor = color;
  }

  /**
   * Sets the color of the triangle mesh lines.
   * @param color the X11 color name of the mesh; if null, the mesh is not shown (default)
   */
  public void setMeshTriangleColor(String colorStr)
  {
    meshTriangleColor = X11Color.toColor(colorStr);
  }

  /**
   * Sets the color of the beam area (two sector border lines and axis).
   * @param color the color of the beam area; if null, the beam lines are not shown (default)
   */
  public void setBeamAreaColor(Color color)
  {
    sectorColor = color;
  }

  /**
   * Sets the color of the beam area (two sector border lines and axis).
   * @param color the X11 color name of the beam area; if null, the beam lines are not shown (default)
   */
  public void setBeamAreaColor(String colorStr)
  {
    sectorColor = X11Color.toColor(colorStr);
  }

  /**
   * Erases the beam area (if it is currently shown).
   */
  public void eraseBeamArea()
  {
    ViewingCone.eraseCone(gameGrid.getBg());
  }

  /**
   * Sets the color of the circle with center at sensor location and radius
   * equals to the current distance value. If value = 0, no circle is shown.
   * @param color the color of the circle; if null, the circle is not shown (default)
   */
  public void setProximityCircleColor(Color color)
  {
    proximityCircleColor = color;
  }

  /**
   * Sets the color of the circle with center at sensor location and radius
   * equals to the current distance value. If value = 0, no circle is shown.
   * @param color the X11 color name of the circle; if null, the circle is not shown (default)
   */
  public void setProximityCircleColor(String colorStr)
  {
    proximityCircleColor = X11Color.toColor(colorStr);
  }

  protected void notifyEvent()
  {
    if (ultrasonicListener == null)
      return;
    final double value = getDistance();
    if (value == -1)
      return;

    if (value < triggerLevel)
      isFarNotified = false;
    if (value >= triggerLevel)
      isNearNotified = false;

    if (value >= triggerLevel && !isFarNotified)
    {
      isFarNotified = true;
      new Thread()
      {
        public void run()
        {
          ultrasonicListener.far(value);
        }
      }.start();
    }
    if (value < triggerLevel && !isNearNotified)
    {
      isNearNotified = true;
      new Thread()
      {
        public void run()
        {
          ultrasonicListener.near(value);
        }
      }.start();
    }
  }
}
