// Gear.java

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
import ch.aplu.util.*;
import javax.swing.JOptionPane;

/**
 * Combines two motors on an axis to perform a car-like movement.
 */
public class Gear extends Part
{
  protected enum GearState
  {
    FORWARD, BACKWARD, LEFT, RIGHT, STOPPED
  };
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
    super("sprites/gear.gif", pos);
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
    checkPart();
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
    checkPart();
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
    checkPart();
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
    checkPart();
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
    checkPart();
    state = GearState.STOPPED;
    isMoving = false;
  }

  /**
   * Starts to rotate left (center of rotation at middle of the wheel axes).
   * Method returns immediately, while the movement continues.
   */
  public void left()
  {
    checkPart();
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
    checkPart();
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
    checkPart();
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
    checkPart();
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
    checkPart();
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
    checkPart();
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
    checkPart();
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
    checkPart();
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
    delay(1);
    checkPart();
    return isMoving;
  }

  /**
   * Returns the x-coordinate of the current location.
   * Calls Thread.sleep(1) to prevent CPU overload in close polling loops.
   * @return the current x-coordinate
   */
  public int getX()
  {
    delay(1);
    checkPart();
    return super.getX();
  }

  /**
   * Returns the y-coordinate of the current location.
   * Calls Thread.sleep(1) to prevent CPU overload in close polling loops.
   * @return the current x-coordinate
   */
  public int getY()
  {
    delay(1);
    checkPart();
    return super.getY();
  }

  /**
   * Returns the current location.
   * Calls Thread.sleep(1) to prevent CPU overload in close polling loops.
   * @return the current location
   */
  public Location getLocation()
  {
    delay(1);
    checkPart();
    return super.getLocation();
  }

  private void checkPart()
  {
    if (robot == null)
    {
      JOptionPane.showMessageDialog(null,
        "Gear is not part of the LegoRobot.\n"
        + "Call addPart() to assemble it.",
        "Fatal Error", JOptionPane.ERROR_MESSAGE);
      if (GameGrid.getClosingMode() == GameGrid.ClosingMode.TerminateOnClose
        || GameGrid.getClosingMode() == GameGrid.ClosingMode.AskOnClose)
        System.exit(1);
      if (GameGrid.getClosingMode() == GameGrid.ClosingMode.DisposeOnClose)
        throw new RuntimeException("Gear is not part of the LegoRobot.\n"
          + "Call addPart() to assemble it.");
    }
  }
}
