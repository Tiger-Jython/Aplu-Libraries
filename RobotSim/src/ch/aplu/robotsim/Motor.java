// Motor.java

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
import javax.swing.JOptionPane;

enum MotorState
{
  FORWARD, BACKWARD, STOPPED, ROTATE
};

/**
 * Class that represents one of the EV3 motors.
 */
public class Motor extends Part
{
  private static final Location pos = new Location(0, 0);
  private MotorState state = MotorState.STOPPED;
  private int speed = SharedConstants.defaultSpeed;
  private MotorPort port;
  private boolean isMoving = false;
  protected boolean isForward;
  private int rotateParam;
  private int motorCount = 0;
  private int counterIncrement = 0;

  /**
   * Creates a motor instance that is plugged into given port.
   * In simulation mode, there is no movement with one motor only.
   * MotorPort.A : left motor, MotorPort.B : right motor
   * @param port the port where the motor is plugged-in (MotorPort.A, MotorPort.B)
   */
  public Motor(MotorPort port)
  {
    super(port == MotorPort.A
      ? "sprites/leftmotor.gif"
      : (port == MotorPort.B ? "sprites/rightmotor.gif"
        : "sprites/rightmotor.gif"), pos);
    this.port = port;
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
    checkPart();
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
    checkPart();
    state = MotorState.BACKWARD;
    if (speed != 0)
      isMoving = true;
  }

  /**
   * Stops the rotation.
   */
  public void stop()
  {
    checkPart();
    state = MotorState.STOPPED;
    isMoving = false;
  }

  /**
   * Resets the rotation counter to zero.
   * @see #rotateTo(int count)
   */
  public void resetMotorCount()
  {
    checkPart();
    motorCount = 0;
  }

  /**
   * Returns current value of the rotation counter.
   * @return the value of the rotation counter
   */
  public int getMotorCount()
  {
    checkPart();
    return motorCount;
  }

  protected void setIncrement(int increment)
  {
    if (isForward)
    {
      motorCount += increment;
      if (motorCount >= rotateParam)
      {  
        state = MotorState.STOPPED;
        isMoving = false;
      }
    }
    else
    {
      motorCount -= increment;
      if (motorCount <= rotateParam)
      {  
        state = MotorState.STOPPED;
        isMoving = false;
      }
    }
  }

  protected int getRotateParam()
  {
    return rotateParam;
  }

  /**
   * Sets the rotation counter to zero and rotates the motor until the given count is reached.
   * If count is negative, the motor turns backwards.
   * This method returns when the count is reached and the motor stops.
   * @see #rotateTo(int count, boolean blocking)
   */
  public void rotateTo(int count)
  {
    rotateTo(count, true);
  }

  /**
   * Sets the rotation counter to zero and rotates the motor until the given count is reached.
   * If count is negative, the motor turns backwards.
   * If blocking = false, the method returns immediately.
   * You may register a motion listener with addMotionListener() to get a callback
   * when the count is reached and the  motor stops.
   * If blocking = true, the method returns when the count is reached and
   * the motor stops.
   * @see #rotateTo(int count)
   */
  public void rotateTo(int count, boolean blocking)
  {
    checkPart();
    motorCount = 0;
    isForward = (count > 0);
    rotateParam = count;
    state = MotorState.ROTATE;
    isMoving = true;
    if (blocking)
    {
      while (state != MotorState.STOPPED)
        Tools.delay(1);
    }
  }

  /**
   * Same as rotateTo(int count), but the rotation counter
   * is not set to zero.
   * The given count is the absolute value of the rotation counter to be reached.
   * If the current count is higher than the count to reach, the motor turns backward.
   * This method returns when the count is reached and the motor stops.
   * @param count the rotation counter value to be reached
   * @see #rotateTo(int count)
   */
  public void continueTo(int count)
  {
    continueTo(count, true);
  }

  /**
   * Same as rotateTo(int count, boolean blocking), but the rotation counter
   * is not set to zero.
   * The given count is the absolute value of the rotation counter to be reached.
   * If the current count is higher than the count to reach, the motor turns backward.
   * @param count the rotation counter value to be reached
   * @see #rotateTo(int count, boolean blocking)
   */
  public void continueTo(int count, boolean blocking)
  {
    checkPart();
    state = MotorState.ROTATE;
    isForward = (count > motorCount);
    rotateParam = count;
    isMoving = true;
    if (blocking)
    {
      while (state != MotorState.STOPPED)
        Tools.delay(1);
    }
  }

  /**
   * Same as rotateTo(int count), but the rotation counter
   * is not set to zero.
   * The given count is the relative increasement/decreasement
   * from the current value of the rotation counter.
   * For count < 0 the motor turns backward.
   * This method returns when the count is reached and the motor stops.
   * @param count the rotation counter value to be increased/decreased
   * @see #rotateTo(int count)
   */
  public void continueRelativeTo(int count)
  {
    continueRelativeTo(count, true);
  }

  /**
   * Same as rotateTo(int count, boolean blocking), but the rotation counter
   * is not set to zero.
   * The given count is the relative increasement/decreasement
   * from the current value of the rotation counter.
   * For count < 0 the motor turns backward.
   * @param count the rotation counter value to be increased/decreased
   * @param blocking if true, the method blocks until the count is reached, otherwise it returns immediately
   * @see #rotateTo(int count, boolean blocking)
   */
  public void continueRelativeTo(int count, boolean blocking)
  {
    checkPart();
    isForward = (count > 0);
    rotateParam = motorCount + count;
    state = MotorState.ROTATE;
    isMoving = true;
    if (blocking)
    {
      while (state != MotorState.STOPPED)
        Tools.delay(1);
    }
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
    checkPart();
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
    checkPart();
    return speed;
  }

  /**
   * Returns the port of the motor.
   * @return the motor port
   */
  public MotorPort getPort()
  {
    checkPart();
    return port;
  }

  /**
   * Checks if motor is rotating.
   * Calls Thread.sleep(1) to prevent CPU overload in close polling loops.
   * @return true, if motor is rotating; otherwise false
   */
  public boolean isMoving()
  {
    checkPart();
    delay(1);
    return isMoving;
  }

  private void checkPart()
  {
    if (robot == null)
    {
      JOptionPane.showMessageDialog(null,
        "Motor is not part of the LegoRobot.\n"
        + "Call addPart() to assemble it.",
        "Fatal Error", JOptionPane.ERROR_MESSAGE);
      if (GameGrid.getClosingMode() == GameGrid.ClosingMode.TerminateOnClose
        || GameGrid.getClosingMode() == GameGrid.ClosingMode.AskOnClose)
        System.exit(1);
      if (GameGrid.getClosingMode() == GameGrid.ClosingMode.DisposeOnClose)
        throw new RuntimeException("Motor is not part of the LegoRobot.\n"
          + "Call addPart() to assemble it.");
    }
  }
}
