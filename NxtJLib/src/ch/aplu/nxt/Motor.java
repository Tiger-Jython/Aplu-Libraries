// Motor.java

/*
 This software is part of the NxtJLib library.
 It is Open Source Free Software, so you may
 - run the code for any purpose
 - study how the code works and adapt it to your needs
 - integrate all or parts of the code in your own programs
 - redistribute copies of the code
 - improve the code and release your improvements to the public
 However the use of the code is entirely your responsibility.
 */
package ch.aplu.nxt;

import ch.aplu.nxt.platform.*;

/**
 * Class that represents one of the NXT motors.
 * Most methods will call connect, if not yet connected.
 *
 */
public class Motor extends Part
{
  enum MotorState
  {
    FORWARD, BACKWARD, STOPPED, UNDEFINED
  };

  // -------------- Inner class MotionDetector ---------------
  private class MotionDetector extends NxtThread
  {
    private volatile boolean isRunning = false;

    private MotionDetector()
    {
      if (LegoRobot.getDebugLevel() >= DEBUG_LEVEL_LOW)
        DebugConsole.show("DEBUG: MotionDetector thread created (port: "
          + getPortLabel() + ")");
    }

    public void run()
    {
      if (LegoRobot.getDebugLevel() >= DEBUG_LEVEL_LOW)
        DebugConsole.show("DEBUG: MotionDetector thread started (port: "
          + getPortLabel() + ")");
      isRunning = true;
      while (isRunning)
      {
        OutputState o = robot.getOutputState(portId);
        if (o.runState == MOTOR_RUN_STATE_IDLE)
        {
          if (motionListener != null)
            motionListener.motionStopped();
          isRunning = false;
        }
        else
          delay(pollDelay);
      }
      if (LegoRobot.getDebugLevel() >= DEBUG_LEVEL_LOW)
        DebugConsole.show("DEBUG: MotionDetector thread terminated (port: "
          + getPortLabel() + ")");
    }

    private void stopThread()
    {
      isRunning = false;
      try
      {
        joinX(500);
      }
      catch (InterruptedException ex)
      {
      }
    }
  }

  // -------------- Inner class MyMotionListener -------------
  private class MyMotionListener implements MotionListener
  {
    public void motionStopped()
    {
      if (LegoRobot.getDebugLevel() >= DEBUG_LEVEL_MEDIUM)
        DebugConsole.show("MyMotionListener will wake-up Motor.rotateTo()");
      PlatformTools.wakeUp();
    }
  }
  // -------------- End of inner classes ---------------------
  private volatile boolean isRunning = false;
  private MotorPort port;
  private MotorState state = MotorState.UNDEFINED;
  private boolean isMotorMoving = false;
  private int portId;
  private int speed;
  private int mode;
  private int regulationMode;
  private byte turnRatio;
  private int runState;
  private double speedFactor;
  private int pollDelay;
  private double velocity;
  private MotionDetector md = null;
  private MotionListener motionListener = null;

  /**
   * Creates a motor instance that is plugged into given port.
   * Default speed set in nxtlib.properties.
   * @param port the port where the motor is plugged-in (MotorPort.A, MotorPort.B, MotorPort.C)
   */
  public Motor(MotorPort port)
  {
    NxtProperties props = LegoRobot.getProperties();
    speedFactor = props.getDoubleValue("MotorSpeedFactor");
    speed = props.getIntValue("MotorSpeed");
    pollDelay = props.getIntValue("MotionDetectorPollDelay");
    this.port = port;
    portId = port.getId();
    velocity = speedToVelocity(speed);
    mode = BRAKE + REGULATED;
    regulationMode = REGULATION_MODE_MOTOR_SPEED;
    turnRatio = 0;
    runState = MOTOR_RUN_STATE_IDLE;
  }

  /**
   * Returns the port number.
   * @return the port number (0..3)
   */
  public int getPortId()
  {
    return portId;
  }

  /**
   * Returns the port label.
   * @return the port label (A, B, C)
   */
  public String getPortLabel()
  {
    return port.getLabel();
  }

  protected void init()
  {
    if (LegoRobot.getDebugLevel() >= DEBUG_LEVEL_MEDIUM)
      DebugConsole.show("DEBUG: Motor.init() called (Port: "
        + getPortLabel() + ")");
  }

  protected void cleanup()
  {
    if (LegoRobot.getDebugLevel() >= DEBUG_LEVEL_MEDIUM)
      DebugConsole.show("DEBUG: Motor.cleanup() called (Port: "
        + getPortLabel() + ")");
    stopMotionDetector();
  }

  /**
   * Registers the given motion listener.
   * When calling rotateTo(), a motion detector thread is started that checks
   * the motion of the motor. When the motion stops because the rotation count
   * is reached or stop() is called, the MotionListener's callback
   * motionStopped() is invoked and the motion detector thread terminates.
   * @param motionListener a reference to a MotionListener
   * @see MotionListener
   * @see #rotateTo(int count, boolean blocking)
   */
  public void addMotionListener(MotionListener motionListener)
  {
    this.motionListener = motionListener;
    md = new MotionDetector();
  }

  protected void startMotionDetector()
  {
    if (md != null && !md.isAlive())
      md.start();
  }

  protected void stopMotionDetector()
  {
    if (md != null && md.isAlive())
      md.stopThread();
  }

  /**
   * Starts the forward rotation with preset speed.
   * Method returns, while the rotation continues. (If motor is already
   * rotating with same speed, returns immediately.)
   * The rotation counter continues to be increased from its start value.
   * @return the object reference to allow method chaining
   */
  public Motor forward()
  {
    if (state == MotorState.FORWARD)
      return this;
    checkConnect();
    isMotorMoving = true;
    forward(false);
    state = MotorState.FORWARD;
    return this;
  }

  protected Motor forward(boolean rampup)
  {
    if (rampup)
      runState = MOTOR_RUN_STATE_RAMPUP;
    else
      runState = MOTOR_RUN_STATE_RUNNING;
    robot.setOutputState(portId, (byte)speed, mode + MOTORON,
      regulationMode, turnRatio, runState, 0);
    return this;
  }

  /**
   * Starts the backward rotation with preset speed.
   * Method returns, while the rotation continues. (If motor is already
   * rotating with same speed, returns immediately.)
   * The rotation counter continues to be decreased from its start value.
   * @return the object reference to allow method chaining
   */
  public Motor backward()
  {
    if (state == MotorState.BACKWARD)
      return this;
    checkConnect();
    isMotorMoving = true;
    backward(false);
    state = MotorState.BACKWARD;
    return this;
  }

  protected Motor backward(boolean rampup)
  {
    if (rampup)
      runState = MOTOR_RUN_STATE_RAMPUP;
    else
      runState = MOTOR_RUN_STATE_RUNNING;
    robot.setOutputState(portId, (byte)-speed, mode + MOTORON,
      regulationMode, turnRatio, runState, 0);
    return this;
  }

  /**
   * Sets the speed to the given value (arbitrary units).
   * The speed will be changed to the new value at the next movement call only.
   * @param speed the speed 0..100
   * @return the object reference to allow method chaining
   */
  public Motor setSpeed(int speed)
  {
    if (this.speed == speed)
      return this;
    this.speed = speed;
    velocity = speedToVelocity(speed);
    state = MotorState.UNDEFINED;
    return this;
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
   * Sets the velocity to the given value.
   * The velocity will be changed to the new value at the next movement call only.<br>
   * velocity = MotorSpeedFactor * speed (default MotorSpeedFactor set in
   * nxtlib.properties).
   * @param velocity the velocity in m/s
   * @return the object reference to allow method chaining
   */
  public Motor setVelocity(double velocity)
  {
    if (this.velocity == velocity)
      return this;
    this.velocity = velocity;
    speed = velocityToSpeed(velocity);
    state = MotorState.UNDEFINED;
    return this;
  }

  /**
   * Returns the current velocity.
   * The velocity in m/s with some inaccuracy.<br>
   * velocity = MotorSpeedFactor * speed (default MotorSpeedFactor set in
   * nxtlib.properties).
   * @return the velocity in m/s
   */
  public double getVelocity()
  {
    return velocity;
  }

  /**
   * Stops the motor.
   * The rotation counter is stopped but maintains its value.
   * (If motor is already stopped, returns immediately.)
   * @return the object reference to allow method chaining
   */
  public Motor stop()
  {
    if (state == MotorState.STOPPED)
      return this;
    checkConnect();
    isMotorMoving = false;
    runState = MOTOR_RUN_STATE_RUNNING;
    robot.setOutputState(portId, (byte)0, BRAKE + MOTORON + REGULATED,
      regulationMode, turnRatio, runState, 0);
    state = MotorState.STOPPED;
    return this;
  }

  private void checkConnect()
  {
    if (robot == null)
      new ShowError("Motor (port: " + getPortLabel()
        + ") is not a part of the NxtRobot.\n"
        + "Call addPart() to assemble it.");
  }

  /**
   * Sets the motor speed factor to given value.
   * Formula: velocity = speedFactor * speed
   * where velocity is in m/s and speed is value used in setSpeed()
   * Default: Set in nxtlib.properties
   * @param value the velocity per speed unit
   */
  public void setSpeedFactor(double value)
  {
    speedFactor = value;
  }

  /**
   * Conversion from speed to velocity.
   * @param speed the speed as set in setSpeed()
   * @return the velocity in m/s
   * @see #setSpeedFactor(double value)
   */
  public double speedToVelocity(int speed)
  {
    return speedFactor * speed;
  }

  /**
   * Conversion from velocity to speed.
   * @param velocity the velocity in m/s
   * @return the speed as set in setSpeed()
   * @see #setSpeedFactor(double value)
   */
  public int velocityToSpeed(double velocity)
  {
    return Tools.round(velocity / speedFactor);
  }

  /**
   * Returns current value of the rotation counter.
   * @return the value of the rotation counter
   */
  public int getMotorCount()
  {
    OutputState o = robot.getOutputState(portId);
    return o.rotationCount;
  }

  /**
   * Resets the rotation counter to zero.
   * The rotation counter's value is maintained from one program
   * invocation to the other. 
   * @see #rotateTo(int count)
   */
  public void resetMotorCount()
  {
    state = MotorState.UNDEFINED;
    checkConnect();
    byte[] request =
    {
      DIRECT_COMMAND_NOREPLY, RESET_MOTOR_POSITION, (byte)portId, (byte)0
    };
    robot.sendData(request);
  }

  private Motor rotateInternal(int count, boolean blocking, boolean resetting)
  {
    state = MotorState.UNDEFINED;
    checkConnect();
    if (resetting)
      resetMotorCount();
    if (count == 0)  // special case will cause rotation for ever
      return this;

    if (blocking)
      addMotionListener(new MyMotionListener());
    this.runState = MOTOR_RUN_STATE_RUNNING;
    if (count > 0)
      robot.setOutputState(portId, (byte)speed, mode + MOTORON, regulationMode,
        turnRatio, runState, count);
    else
      robot.setOutputState(portId, (byte)-speed, mode + MOTORON, regulationMode,
        turnRatio, runState, -count);

    if (motionListener != null)
      startMotionDetector();

    if (blocking)
    {
      if (LegoRobot.getDebugLevel() >= DEBUG_LEVEL_MEDIUM)
        DebugConsole.show("Motor.rotateTo() going to sleep");
      PlatformTools.putSleep();
    }
    return this;
  }

  /**
   * Sets the rotation counter to zero and rotates the motor until the given count is reached.
   * If count is negative, the motor turns backwards.
   * This method returns when the count is reached and the motor stops.
   * @see #addMotionListener(MotionListener motionListener)
   * @see #rotateTo(int count, boolean blocking)
   * @param count the rotation counter value to be reached
   * @return the object reference to allow method chaining
   */
  public Motor rotateTo(int count)
  {
    return rotateInternal(count, true, true);
  }

  /**
   * Sets the rotation counter to zero and rotates the motor until the given count is reached.
   * If count is negative, the motor turns backwards.
   * If blocking = false, the method returns immediately.
   * You may register a motion listener with addMotionListener() to get a callback
   * when the count is reached and the  motor stops.
   * If blocking = true, the method returns when the count is reached and
   * the motor stops.
   * @see #addMotionListener(MotionListener motionListener)
   * @see #rotateTo(int count)
   * @param count the rotation counter value to be reached
   * @param blocking if true, the method blocks until the count is reached, otherwise it returns immediately
   * @return the object reference to allow method chaining
   */
  public Motor rotateTo(int count, boolean blocking)
  {
    return rotateInternal(count, blocking, true);
  }

  /**
   * Same as rotateTo(int count), but the rotation counter
   * is not set to zero. 
   * The given count is the absolute value of the rotation counter to be reached.
   * If the current count is higher than the count to reach, the motor turns backward.
   * @param count the rotation counter value to be reached
   * @see #rotateTo(int count)
   */
  public Motor continueTo(int count)
  {
    int currentCount = getMotorCount();
    return rotateInternal(count - currentCount, true, false);
  }

  /**
   * Same as rotateTo(int count, boolean blocking), but the rotation counter
   * is not set to zero.
   * The given count is the absolute value of the rotation counter to be reached.
   * If the current count is higher than the count to reach, the motor turns backward.
   * @param count the rotation counter value to be reached
   * @param blocking if true, the method blocks until the count is reached, otherwise it returns immediately
   * @see #rotateTo(int count, boolean blocking)
   */
  public Motor continueTo(int count, boolean blocking)
  {
    int currentCount = getMotorCount();
    return rotateInternal(count - currentCount, blocking, false);
  }

  /**
   * Same as rotateTo(int count), but the rotation counter
   * is not set to zero. 
   * The given count is the relative increasement/decreasement 
   * from the current value of the rotation counter.
   * For count < 0 the motor turns backward.
   * @param count the rotation counter value to be increased/decreased
   * @see #rotateTo(int count)
   */
  public Motor continueRelativeTo(int count)
  {
    return rotateInternal(count, true, false);
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
  public Motor continueRelativeTo(int count, boolean blocking)
  {
    return rotateInternal(count, blocking, false);
  }

  /**
   * Checks if the motor is rotating.
   * @return true, if rotating, otherwise false
   */
  public boolean isMoving()
  {
    checkConnect();
    return isMotorMoving;
  }

  private void delay(long timeout)
  {
    try
    {
      Thread.currentThread().sleep(timeout);
    }
    catch (InterruptedException ex)
    {
    }
  }
}
