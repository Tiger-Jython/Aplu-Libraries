// TurtleRobot.java

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

/**
 * Implementation of the basic Logo turtle movements.
 */
public class TurtleRobot extends LegoRobot
{
  private Gear gear = new Gear();
  private final int turtleSpeed = 50;

  /**
   * Creates a turtle robot instance.
   */
  public TurtleRobot()
  {
    super();
    addPart(gear);
    gear.setSpeed(turtleSpeed);
  }

  /**
   * Sets the turtle speed to the given value.
   * @param speed 0..100
   */
  public void setTurtleSpeed(int speed)
  {
    gear.setSpeed(speed);
  }

  /**
   * Returns the current turtle speed.
   * @return speed 0..100
   */
  public int getTurtleSpeed()
  {
    return gear.getSpeed();
  }

  /**
   * Starts moving forward and returns immediately.
   */
  public void forward()
  {
    gear.forward();
  }

  /**
   * Moves the turtle forward the given number of steps.
   * The methods blocks until the turtle is at the final position (or the
   * game grid window is disposed).
   * @param steps the number of steps to go.
   */
  public void forward(int steps)
  {
    Location loc = getRobot().getLocation();
    gear.forward();
    double d = 0;
    while (d < steps)
    {
      Location newLoc = getRobot().getLocation();
      d = Math.sqrt((newLoc.x - loc.x) * (newLoc.x - loc.x)
        + (newLoc.y - loc.y) * (newLoc.y - loc.y));
      Tools.delay(1);
      if (GameGrid.isDisposed())
        break;
    }
    gear.stop();
  }

  /**
   * Moves the turtle backward the given number of steps.
   * The methods blocks until the turtle is at the final position (or the
   * game grid window is disposed).
   * @param steps the number of steps to go.
   */
  public void backward(int steps)
  {
    Location loc = getRobot().getLocation();
    gear.backward();
    double d = 0;
    while (d < steps)
    {
      Location newLoc = getRobot().getLocation();
      d = Math.sqrt((newLoc.x - loc.x) * (newLoc.x - loc.x)
        + (newLoc.y - loc.y) * (newLoc.y - loc.y));
      Tools.delay(1);
      if (GameGrid.isDisposed())
        break;
    }
    gear.stop();
  }

  /**
   * Starts moving backward and returns immediately.
   */
  public void backward()
  {
    gear.backward();
  }

  /**
   * Turns the turtle to the right for the given angle.
   * The methods blocks until the turtle is at the final position (or the
   * game grid window is disposed).
   * @param angle the angle in degree to rotate.
   */
  public void right(double angle)
  {
    if (angle == 0)
      return;
    if (angle < 0)
    {
      left(-angle);
    }
    int oldSpeed = gear.getSpeed();
    gear.setSpeed(10);
    gear.right();
    double dir = getRobot().getDirection();
    double inc = 0;
    while (inc < angle)
    {
      double newDir = getRobot().getDirection();
      inc = newDir - dir;
      if (inc < 0)
        inc = 360 + inc;
      inc = inc % 360;
      Tools.delay(1);
      if (GameGrid.isDisposed())
        break;
    }
    gear.stop();
    gear.setSpeed(oldSpeed);
  }

  /**
   * Starts turning right and returns immediately.
   */
  public void right()
  {
    gear.right();
  }

  /**
   * Turns the left to the right for the given angle.
   * The methods blocks until the turtle is at the final position (or the
   * game grid window is disposed).
   * @param angle the angle in degree to rotate.
   */
  public void left(double angle)
  {
    if (angle == 0)
      return;
    if (angle < 0)
    {
      right(-angle);
      return;
    }
    int oldSpeed = gear.getSpeed();
    gear.setSpeed(10);
    gear.left();
    double dir = getRobot().getDirection();
    double inc = 0;
    while (inc < angle)
    {
      double newDir = getRobot().getDirection();
      inc = dir - newDir;
      if (inc < 0)
        inc = 360 + inc;
      inc = inc % 360;
      Tools.delay(1);
      if (GameGrid.isDisposed())
        break;
    }
    gear.setSpeed(oldSpeed);
    gear.stop();
  }

  /**
   * Starts turning left and returns immediately.
   */
  public void left()
  {
    gear.left();
  }

  /**
   * Returns the gear used for the turtle robot.
   * @return the gear reference
   */
  public Gear getGear()
  {
    return gear;
  }

}
