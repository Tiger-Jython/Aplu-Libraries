// Motor.java

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

enum MotorState
{
  FORWARD, BACKWARD, STOPPED
};

/**
 * Class that represents one of the motors.
 */
public class Motor extends Part
{
  /**
  * Motor id for left motor.
  */
  public static int LEFT = 0;
  /**
  * Motor id for right motor.
  */
  public static int RIGHT = 1;
  //
  private Robot robot;
  private static final Location pos = new Location(0, 0);
  private MotorState state = MotorState.STOPPED;
  private int speed = SharedConstants.defaultSpeed;
  private boolean isMoving = false;
  private int id;

  /**
   * Creates a motor instance with given id.
   * In simulation mode, there is no movement with one motor only.
   * @param id 0 for left motor, 1 for right motor
   */
  public Motor(int id)
  {
    super("sprites/dummy.gif", pos);
    this.id = id;
    robot = RobotInstance.getRobot();
    if (robot == null)
      // Defer addPart() to robot ctor
      RobotInstance.partsToAdd.add(this);
    else
      robot.addPart(this);
   }

  protected void cleanup()
  {
    state = MotorState.STOPPED;
    isMoving = false;
  }

  /**
   * Starts the forward rotation.
   * Method returns immediately, while the rotation continues.
   */
   public void forward()
  {
    RobotInstance.checkRobot();
    state = MotorState.FORWARD;
    if (speed != 0)
      isMoving = true;
  }

  /**
   * Starts the backward rotation.
   * Method returns immediately, while the rotation continues.
   */
   public void backward()
  {
    RobotInstance.checkRobot();
    state = MotorState.BACKWARD;
    if (speed != 0)
      isMoving = true;
  }

  /**
   * Stops the rotation.
   */
   public void stop()
  {
    RobotInstance.checkRobot();
    state = MotorState.STOPPED;
    isMoving = false;
  }

  protected MotorState getState()
  {
    return state;
  }

  /**
   * Sets the speed to the given value (arbitrary units).
   * @param speed the speed 0..100
   */
   public void setSpeed(int speed)
  {
    this.speed = speed;
    if (speed != 0 && state != MotorState.STOPPED)
      isMoving = true;
  }

  /**
   * Returns the current speed (arbitrary units).
   * @return the speed 0..100
   */
   public int getSpeed()
  {
    return speed;
  }

  /**
   * Returns the port of the motor.
   * @return the motor port
   */
   public int getId()
  {
    return id;
  }

  /**
   * Checks if motor is rotating.
   * Calls Thread.sleep(1) to prevent CPU overload in close polling loops.
   * @return true, if motor is rotating; otherwise false
   */
   public boolean isMoving()
  {
    RobotInstance.checkRobot();
    delay(1);
    return isMoving;
  }
}
