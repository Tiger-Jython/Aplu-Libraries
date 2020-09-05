// GenericMotor.java
// Direct mode

/*
This software is part of the EV3JLib library.
It is Open Source Free Software, so you may
- run the code for any purpose
- study how the code works and adapt it to your needs
- integrate all or parts of the code in your own programs
- redistribute copies of the code
- improve the code and release your improvements to the public
However the use of the code is entirely your responsibility.
*/
package ch.aplu.ev3;

/**
 * Abstract class that represents one of the motors.
 */
public abstract class GenericMotor extends Part
{
  enum MotorState
  {
    FORWARD, BACKWARD, STOPPED, UNDEFINED
  };
  
  enum MotorType
  {
    EV3Large, EV3Medium, NXT
  }

  private MotorPort port;
  private MotorState state = MotorState.UNDEFINED;
  private int portId;
  private int speed;
  private double speedFactor;
  private double velocity;

  protected GenericMotor(MotorPort port, MotorType type)
  {
    EV3Properties props = LegoRobot.getProperties();
    speedFactor = props.getDoubleValue("MotorSpeedFactor");
    speed = props.getIntValue("MotorSpeed");
    this.port = port;
    portId = port.getId();
    velocity = speedToVelocity(speed);
    String classID = "";
    switch (type)
    {
      case EV3Large:
        classID = "mot";
        break;
      case EV3Medium:
        classID = "mmot";
        break;
      case NXT:
        classID = "_mot";
        break;
    }        
    partName = classID + port.getLabel();
  }
  
  protected GenericMotor(MotorType type)
  {
    this(MotorPort.A, type);
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
   * @return the port label (A, B, C, D)
   */
  public String getPortLabel()
  {
    return port.getLabel();
  }

  protected void init()
  {
    if (LegoRobot.getDebugLevel() >= SharedConstants.DEBUG_LEVEL_MEDIUM)
      DebugConsole.show("DEBUG: GenericMotor.init() called (Port: "
        + getPortLabel() + ")");
    robot.sendCommand(partName + ".setMotorSpeed." + speed);
  }

  protected void cleanup()
  {
    if (LegoRobot.getDebugLevel() >= SharedConstants.DEBUG_LEVEL_MEDIUM)
      DebugConsole.show("DEBUG: GenericMotor.cleanup() called (Port: "
        + getPortLabel() + ")");
  }

  /**
   * Starts the forward rotation with preset speed.
   * Method returns, while the rotation continues. (If motor is already
   * rotating with same speed, returns immediately.)
   * The rotation counter continues to be increased from its start value.
   * @return the object reference to allow method chaining
   */
  public GenericMotor forward()
  {
    if (state == MotorState.FORWARD)
      return this;
    checkConnect();
    robot.sendCommand(partName + ".forward");
    state = MotorState.FORWARD;
    return this;
  }

  /**
   * Starts the backward rotation with preset speed.
   * Method returns, while the rotation continues. (If motor is already
   * rotating with same speed, returns immediately.)
   * The rotation counter continues to be decreased from its start value.
   * @return the object reference to allow method chaining
   */
  public GenericMotor backward()
  {
    if (state == MotorState.BACKWARD)
      return this;
    checkConnect();
    robot.sendCommand(partName + ".backward");
    state = MotorState.BACKWARD;
    return this;
  }

  /**
   * Sets the speed to the given value (arbitrary units).
   * @param speed the speed 0..100
   * @return the object reference to allow method chaining
   */
  public GenericMotor setSpeed(int speed)
  {
    if (this.speed == speed)
      return this;
    this.speed = speed;
    velocity = speedToVelocity(speed);
    if (robot != null)  // already connected
    {  
      robot.sendCommand(partName + ".setSpeed." + speed);
      state = MotorState.UNDEFINED;
    }
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
   * velocity = MotorSpeedFactor * speed (default MotorSpeedFactor set in
   * ev3lib.properties).
   * @param velocity the velocity in m/s
   * @return the object reference to allow method chaining
   */
  public GenericMotor setVelocity(double velocity)
  {
    if (this.velocity == velocity)
      return this;
    this.velocity = velocity;
    speed = velocityToSpeed(velocity);
    setSpeed(speed);
    return this;
  }

  /**
   * Returns the current velocity.
   * The velocity in m/s with some inaccuracy.<br>
   * velocity = MotorSpeedFactor * speed (default MotorSpeedFactor set in
   * ev3jlib.properties).
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
  public GenericMotor stop()
  {
    if (state == MotorState.STOPPED)
      return this;
    checkConnect();
    robot.sendCommand(partName + ".stop");
    state = MotorState.STOPPED;
    return this;
  }

   /**
   * Sets the motor speed factor to given value.
   * Formula: velocity = speedFactor * speed
   * where velocity is in m/s and speed is value used in setSpeed()
   * Default: Set in ev3lib.properties
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
   */
  public double speedToVelocity(int speed)
  {
    return speedFactor * speed;
  }

  /**
   * Conversion from velocity to speed.
   * @param velocity the velocity in m/s
   * @return the speed as set in setSpeed()
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
    return Integer.parseInt(robot.sendCommand(partName + ".getMotorCount"));
  }

  /**
   * Resets the rotation counter to zero.
   * The rotation counter's value is maintained from one program
   * invocation to the other. 
   * @return the object reference to allow method chaining
   */
  public GenericMotor resetMotorCount()
  {
    state = MotorState.UNDEFINED;
    checkConnect();
    robot.sendCommand(partName + ".resetMotorCount");
    return this;
  }

  /**
   * Sets the rotation counter to zero and rotates the motor until the given count is reached.
   * If count is negative, the motor turns backwards.
   * This method returns when the count is reached and the motor stops.
   * @param count the rotation counter value to be reached
   * @return the object reference to allow method chaining
   */
  public GenericMotor rotateTo(int count)
  {
    state = MotorState.UNDEFINED;
    checkConnect();
    robot.sendCommand(partName + ".rotateTo." + count);
    return this;
  }

  /**
   * Sets the rotation counter to zero and rotates the motor until the given count is reached.
   * If count is negative, the motor turns backwards.
   * If blocking = false, the method returns immediately.
   * You may register a motion listener with addMotionListener() to get a callback
   * when the count is reached and the  motor stops.
   * If blocking = true, the method returns when the count is reached and
   * the motor stops.
   * @param count the rotation counter value to be reached
   * @param blocking if true, the method blocks until the count is reached, 
   * otherwise it returns immediately
   * @return the object reference to allow method chaining
   */
  public GenericMotor rotateTo(int count, boolean blocking)
  {
    state = MotorState.UNDEFINED;
    checkConnect();
    robot.sendCommand(partName + ".rotateTo." + count + "." + (blocking ? "b1" : "b0"));
    return this;
  }

  /**
   * Same as rotateTo(int count), but the rotation counter
   * is not set to zero. 
   * The given count is the absolute value of the rotation counter to be reached.
   * If the current count is higher than the count to reach, the motor turns backward.
   * This method returns when the count is reached and the motor stops.
   * @param count the rotation counter value to be reached
   * @return the object reference to allow method chaining
   */
  public GenericMotor continueTo(int count)
  {
    state = MotorState.UNDEFINED;
    checkConnect();
    robot.sendCommand(partName + ".continueTo." + count);
    return this;
  }

  /**
   * Same as rotateTo(int count, boolean blocking), but the rotation counter
   * is not set to zero.
   * The given count is the absolute value of the rotation counter to be reached.
   * If the current count is higher than the count to reach, the motor turns backward.
   * @param count the rotation counter value to be reached
   * @param blocking if true, the method blocks until the count is reached, otherwise it returns immediately
   * @return the object reference to allow method chaining
   */
  public GenericMotor continueTo(int count, boolean blocking)
  {
    state = MotorState.UNDEFINED;
    checkConnect();
    robot.sendCommand(partName + ".continueTo." + count + "." + (blocking ? "b1" : "b0"));
    return this;
  }

  /**
   * Same as rotateTo(int count), but the rotation counter
   * is not set to zero. 
   * The given count is the relative increasement/decreasement 
   * from the current value of the rotation counter.
   * For count < 0 the motor turns backward.
   * This method returns when the count is reached and the motor stops.
   * @param count the rotation counter value to be increased/decreased
   * @return the object reference to allow method chaining
   */
  public GenericMotor continueRelativeTo(int count)
  {
    state = MotorState.UNDEFINED;
    checkConnect();
    robot.sendCommand(partName + ".continueRelativeTo." + count);
    return this;
  }

  /**
   * Same as rotateTo(int count, boolean blocking), but the rotation counter
   * is not set to zero.
   * The given count is the relative increasement/decreasement 
   * from the current value of the rotation counter.
   * For count < 0 the motor turns backward.
   * @param count the rotation counter value to be increased/decreased
   * @param blocking if true, the method blocks until the count is reached, otherwise it returns immediately
   * @return the object reference to allow method chaining
   */
  public GenericMotor continueRelativeTo(int count, boolean blocking)
  {
    state = MotorState.UNDEFINED;
    checkConnect();
    robot.sendCommand(partName + ".continueRelativeTo." + count + "." + (blocking ? "b1" : "b0"));
    return this;
  }

  /**
   * Checks if the motor is rotating.
   * When the motor is stopped by calling stop(), it may last a while until
   * isMoving returns false.
   * @return true, if rotating, otherwise false
   */
  public boolean isMoving()
  {
    checkConnect();
    Tools.delay(1);
    String rc = robot.sendCommand(partName
      + ".isMoving");
    return (rc.equals("1"));
  }

  /**
   * Sets the acceleration rate of the motor in degrees/sec/sec
   * The default value is 6000. Smaller values make speed changes smoother.
   * @param acceleration the new accelaration
   * @return the object reference to allow method chaining
   */
  public GenericMotor setAcceleration(int acceleration)
  {
    checkConnect();
    robot.sendCommand(partName
      + ".setAcceleration." + acceleration);
    return this;
  }
  
  private void checkConnect()
  {
    if (robot == null)
      new ShowError("Motor (port: " + getPortLabel()
        + ") is not a part of the EV3Robot.\n"
        + "Call addPart() to assemble it.");
  }
}
