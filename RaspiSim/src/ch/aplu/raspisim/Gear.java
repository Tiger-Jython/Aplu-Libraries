// Gear.java

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

/**
 * Combines two motors on an axis to perform a car-like movement.
 */
public class Gear extends Part
{
  protected enum GearState
  {
    FORWARD, BACKWARD, LEFT, RIGHT, STOPPED
  };
  private Robot robot;
  private static final Location pos = new Location(0, 0);
  private final int DEFAULT_SPEED = 50;
  private GearState state = GearState.STOPPED;
  private double radius;
  private int speed = DEFAULT_SPEED;
  private boolean isMoving = false;

  /**
   * Creates a gear instance with right motor plugged into port A, left motor plugged into port B.
   */
  public Gear()
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
    state = GearState.STOPPED;
    isMoving = false;
  }

  /**
   * Starts the forward movement.
   * Method returns immediately, while the movement continues.
   */
  public void forward()
  {
    RobotInstance.checkRobot();
    state = GearState.FORWARD;
    if (speed != 0)
      isMoving = true;
  }

  /**
   * Starts the forward movement for the given duration (in ms) and stops.
   * Method returns at the end of the given duration,
   * @param duration the duration (in ms)
   */
  public void forward(int duration)
  {
    RobotInstance.checkRobot();
    state = GearState.FORWARD;
    if (speed != 0)
      isMoving = true;
    Tools.delay(duration);
    state = GearState.STOPPED;
  }

  /**
   * Starts the backward movement.
   * Method returns immediately, while the movement continues.
   */
  public void backward()
  {
    RobotInstance.checkRobot();
    state = GearState.BACKWARD;
    if (speed != 0)
      isMoving = true;
  }

  /**
   * Starts the backward movement for the given duration (in ms) and stops.
   * Method returns at the end of the given duration,
   * @param duration the duration (in ms)
   */
  public void backward(int duration)
  {
    RobotInstance.checkRobot();
    state = GearState.BACKWARD;
    if (speed != 0)
      isMoving = true;
    Tools.delay(duration);
    state = GearState.STOPPED;
  }

  /**
   * Stops the movement.
   */
  public void stop()
  {
    RobotInstance.checkRobot();
    state = GearState.STOPPED;
    isMoving = false;
  }

  /**
   * Starts to rotate left (center of rotation at middle of the wheel axes).
   * Method returns immediately, while the movement continues.
   */
  public void left()
  {
    RobotInstance.checkRobot();
    state = GearState.LEFT;
    radius = 0;
    if (speed != 0)
      isMoving = true;
  }

  /**
   * Starts to rotate left (center of rotation at middle of the wheel axes)
   * for the given duration (in ms) and stops.
   * Method returns at the end of the given duration,
   * @param duration the duration (in ms)
   */
  public void left(int duration)
  {
    RobotInstance.checkRobot();
    left();
    Tools.delay(duration);
    state = GearState.STOPPED;
  }

  /**
   * Starts to rotate right (center of rotation at middle of the wheel axes).
   * Method returns immediately, while the movement continues.
   */
  public void right()
  {
    RobotInstance.checkRobot();
    state = GearState.RIGHT;
    radius = 0;
    if (speed != 0)
      isMoving = true;
  }

  /**
   * Starts to rotate right (center of rotation at middle of the wheel axes)
   * for the given duration (in ms) and stops.
   * Method returns at the end of the given duration,
   * @param duration the duration (in ms)
   */
  public void right(int duration)
  {
    RobotInstance.checkRobot();
    right();
    Tools.delay(duration);
    state = GearState.STOPPED;
  }

  /**
   * Starts to move to the left on an arc with given radius.
   * Method returns immediately, while the movement continues.
   * @param radius the radius of the arc; if negative, moves backward
   */
  public void leftArc(double radius)
  {
    RobotInstance.checkRobot();
    state = GearState.LEFT;
    this.radius = SharedConstants.pixelPerMeter * radius;
    if (speed != 0)
      isMoving = true;
  }

  /**
   * Starts to move left on an arc with given radius
   * for the given duration (in ms) and stops.
   * Method returns at the end of the given duration,
   * @param radius the radius of the arc; if negative, moves backward
   * @param duration the duration (in ms)
   */
  public void leftArc(double radius, int duration)
  {
    RobotInstance.checkRobot();
    leftArc(radius);
    Tools.delay(duration);
    state = GearState.STOPPED;
  }

  /**
   * Starts to move to the right on an arc with given radius.
   * Method returns immediately, while the movement continues.
   * @param radius the radius of the arc; if negative, moves backward
   */
  public void rightArc(double radius)
  {
    RobotInstance.checkRobot();
    state = GearState.RIGHT;
    this.radius = SharedConstants.pixelPerMeter * radius;
    if (speed != 0)
      isMoving = true;
  }

  /**
   * Starts to move right on an arc with given radius
   * for the given duration (in ms) and stops.
   * Method returns at the end of the given duration,
   * @param radius the radius of the arc; if negative, moves backward
   * @param duration the duration (in ms)
   */
  public void rightArc(double radius, int duration)
  {
    RobotInstance.checkRobot();
    rightArc(radius);
    Tools.delay(duration);
    state = GearState.STOPPED;
  }

  protected GearState getState()
  {
    return state;
  }

  protected double getRadius()
  {
    return radius;
  }

  /**
   * Sets the speed to the given value (arbitrary units).
   * @param speed 0..100
   */
  public void setSpeed(int speed)
  {
    this.speed = speed;
    if (speed != 0 && state != GearState.STOPPED)
      isMoving = true;
  }

  /**
   * Returns the current speed (arbitrary units).
   * @return speed 0..100
   */
  public int getSpeed()
  {
    return speed;
  }

  /**
   * Checks if one or both motors are rotating
   * Calls Thread.sleep(1) to prevent CPU overload in close polling loops.
   * @return true, if gear is moving; otherwise false
   */
  public boolean isMoving()
  {
    RobotInstance.checkRobot();
    delay(1);
    return isMoving;
  }

  /**
   * Returns the x-coordinate of the current location.
   * Calls Thread.sleep(1) to prevent CPU overload in close polling loops.
   * @return the current x-coordinate
   */
  public int getX()
  {
    RobotInstance.checkRobot();
    delay(1);
    return super.getX();
  }

  /**
   * Returns the y-coordinate of the current location.
   * Calls Thread.sleep(1) to prevent CPU overload in close polling loops.
   * @return the current x-coordinate
   */
  public int getY()
  {
    RobotInstance.checkRobot();
    delay(1);
    return super.getY();
  }

  /**
   * Returns the current location.
   * Calls Thread.sleep(1) to prevent CPU overload in close polling loops.
   * @return the current location
   */
  public Location getLocation()
  {
    RobotInstance.checkRobot();
    delay(1);
    return super.getLocation();
  }

}
