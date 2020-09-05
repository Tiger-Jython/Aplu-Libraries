// Motor.java

/*
 This software is part of the RaspiJLib library.
 It is Open Source Free Software, so you may
 - run the code for any purpose
 - study how the code works and adapt it to your needs
 - integrate all or parts of the code in your own programs
 - redistribute copies of the code
 - improve the code and release your improvements to the public
 However the use of the code is entirely your responsibility.
 */
package ch.aplu.raspi;

/**
 Class that represents a motor.
 */
public class Motor extends Part
{

  // ----------   Interface MotorState  -------------------
  interface MotorState
  {
    int FORWARD = 0;
    int BACKWARD = 1;
    int STOPPED = 2;
    int UNDEFINED = 3;
  }
  // ------------------------------------------------------

  private Robot robot;
  private int state;
  private int id;
  private String device;
  private int speed;
  public static int LEFT = 0;
  public static int RIGHT = 1;

  /**
   * Creates a motor instance with given id.
   * @param id 0 for left motor, 1 for right motor
   */
  public Motor(int id)
  {
    RaspiProperties props = Robot.getProperties();
    speed = props.getIntValue("MotorSpeed");
    state = MotorState.UNDEFINED;
    this.id = id;
    device = "mot" + id;
    Robot r = RobotInstance.getRobot();
    if (r == null)  // Defer setup() until robot is created
      RobotInstance.partsToRegister.add(this);
    else
      setup(r);
  }
  
  protected void setup(Robot robot)
  {
    robot.sendCommand(device + ".create");
    robot.sendCommand(device + ".setSpeed." + speed);
    this.robot = robot;
  }

  /**
   * Starts the forward rotation with preset speed.
   * The method returns immediately, while the rotation continues.
   */
  public void forward()
  {
    checkRobot();
    if (state == MotorState.FORWARD)
      return;
    robot.sendCommand(device + ".forward");
    state = MotorState.FORWARD;
  }

  /**
   * Starts the backward rotation with preset speed.
   * The method returns immediately, while the rotation continues.
   */
  public void backward()
  {
    checkRobot();
    if (state == MotorState.BACKWARD)
      return;
    robot.sendCommand(device + ".backward");
    state = MotorState.BACKWARD;
  }

  /**
   * Stops the motor.
   * (If motor is already stopped, returns immediately.)
   */
  public void stop()
  {
    checkRobot();
    if (state == MotorState.STOPPED)
      return;
    robot.sendCommand(device + ".stop");
    state = MotorState.STOPPED;
  }

  /**
   * Sets the speed to the given value (arbitrary units).
   * The speed will be changed to the new value at the next movement call only.
   * The speed is limited to 0..100.
   * @param speed the new speed 0..100
   */
  public void setSpeed(int speed)
  {
    checkRobot();
    if (this.speed == speed)
      return;
    this.speed = speed;
    robot.sendCommand(device + ".setSpeed." + speed);
    state = MotorState.UNDEFINED;
  }

  private void checkRobot()
  {
    if (robot == null)
      new ShowError("Create Robot instance first");
  }
}
