// GenericMotor.java

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
 * Abstract class that represents a EV3LargeRegulatedMotors or a 
 * lejos.hardware.motor.NXTRegulatedMotor.
 */
public abstract class GenericMotor extends Part
{
  enum MotorState
  {
    FORWARD, BACKWARD, STOPPED, UNDEFINED
  };

  private MotorPort port;
  private MotorState state = MotorState.UNDEFINED;
  private Object mot;
  private boolean isNxt;
  private int portId;
  private int speed;
  private int speedMultiplier;
  private double speedFactor;
  private double velocity;

  protected GenericMotor(MotorPort port, boolean isNxt)
  {
    this.port = port;
    this.isNxt = isNxt;
    portId = port.getId();
    if (isNxt)
      mot = new lejos.hardware.motor.NXTRegulatedMotor(getMotorPort(port));
    else
      mot = new lejos.hardware.motor.EV3LargeRegulatedMotor(getMotorPort(port));
    EV3Properties props = LegoRobot.getProperties();
    speed = props.getIntValue("MotorSpeed");
    speedFactor = props.getDoubleValue("MotorSpeedFactor");
    speedMultiplier = props.getIntValue("MotorSpeedMultiplier");
    setSpeed(speed);
    velocity = speedToVelocity(speed);
  }

  protected GenericMotor(boolean isNXT)
  {
    this(MotorPort.A, isNXT);
  }

  /**
   * Returns the port number.
   * @return the port number (0..3)
   */
  public int getPortId()
  {
    return portId;
  }

  protected lejos.hardware.motor.NXTRegulatedMotor _getMot()
  {
    return ((lejos.hardware.motor.NXTRegulatedMotor)mot);
  }

  protected lejos.hardware.motor.EV3LargeRegulatedMotor getMot()
  {
    return ((lejos.hardware.motor.EV3LargeRegulatedMotor)mot);
  }

  protected lejos.hardware.motor.EV3MediumRegulatedMotor getMediumMot()
  {
    return ((lejos.hardware.motor.EV3MediumRegulatedMotor)mot);
  }

  /**
   * Returns the port label.
   * @return the port label (A, B, C, D)
   */
  public String getPortLabel()
  {
    System.out.println("lbl= " + port.getLabel());
    return port.getLabel();
  }

  protected void init()
  {
    if (LegoRobot.getDebugLevel() >= SharedConstants.DEBUG_LEVEL_MEDIUM)
      DebugConsole.show("Motor.init() (" + getPortLabel() + ")");
  }

  protected void cleanup()
  {
    if (LegoRobot.getDebugLevel() >= SharedConstants.DEBUG_LEVEL_MEDIUM)
      DebugConsole.show("Motor.clean() (" + getPortLabel() + ")");
    stop();
    if (isNxt)
      _getMot().close();
    else
      getMot().close();
  }

  /**
   * Starts the forward rotation with preset speed.
   * Method returns, while the rotation continues.
   *(If motor is already rotating with same speed, returns immediately.)
   * The rotation counter continues to be increased from its start value.
   * @return the object reference to allow method chaining
   */
  public GenericMotor forward()
  {
    if (state == MotorState.FORWARD)
      return this;
    checkConnect();
    if (isNxt)
    {
      _getMot().setSpeed(Tools.round(speedMultiplier * speed));
      _getMot().forward();
    }
    else
    {
      getMot().setSpeed(Tools.round(speedMultiplier * speed));
      getMot().forward();
    }
    state = MotorState.FORWARD;
    return this;
  }

  /**
   * Starts the backward rotation with preset speed.
   * Method returns, while the rotation continues.
   * The rotation counter continues to be decreased from its start value.
   * @return the object reference to allow method chaining
   */
  public GenericMotor backward()
  {
    if (state == MotorState.BACKWARD)
      return this;
    checkConnect();
    if (isNxt)
    {
      _getMot().setSpeed(Tools.round(speedMultiplier * speed));
      _getMot().backward();
    }
    else
    {
      getMot().setSpeed(Tools.round(speedMultiplier * speed));
      getMot().backward();
    }
    state = MotorState.BACKWARD;
    return this;
  }

  /**
   * Sets the speed to the given value (arbitrary units).
   * The speed will be changed to the new value at the next movement call only.
   * @param speed the speed 0..100
   * @return the object reference to allow method chaining
   */
  public GenericMotor setSpeed(int speed)
  {
    // Do not set the brick's speed because we want that the new value
    // is taken only when calling forward()/backward() (direct mode compatibility)
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
   * ShareContants interface).
   * @param velocity the velocity in m/s
   * @return the object reference to allow method chaining
   */
  public GenericMotor setVelocity(double velocity)
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
   * ShareContants interface).
   * @return the velocity in m/s
   */
  public double getVelocity()
  {
    return velocity;
  }

  /**
   * Stops the motor.
   * If immediately = true, the method returns immediately while the
   * motor is still spinning down.
   * The rotation counter is stopped but maintains its value.
   * (If motor is already stopped, returns immediately.)
   * @param immediately if true, the method returns immediately; otherwise
   * it blocks until the motor is at rest
   * @return the object reference to allow method chaining
   */
  public GenericMotor stop(boolean immediately)
  {
    if (state == MotorState.STOPPED)
      return this;
    checkConnect();
    if (isNxt)
      _getMot().stop(immediately);
    else
      getMot().stop(immediately);
    state = MotorState.STOPPED;
    return this;
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
    if (isNxt)
      _getMot().stop();
    else
      getMot().stop();
    state = MotorState.STOPPED;
    return this;
  }

  /**
   * Sets the motor speed factor to given value.
   * Formula: velocity = speedFactor * speed
   * where velocity is in m/s and speed is value used in setSpeed()
   * Default: Set in ShareContants interface
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
   * Resets the rotation counter to zero.
   * @see #rotateTo(int count)
   */
  public void resetMotorCount()
  {
    state = MotorState.UNDEFINED;
    checkConnect();
    if (isNxt)
      _getMot().resetTachoCount();
    else
      getMot().resetTachoCount();
  }

  /**
   * Returns current value of the rotation counter.
   * @return the value of the rotation counter
   */
  public int getMotorCount()
  {
    checkConnect();
    if (isNxt)
      return _getMot().getTachoCount();
    else
      return getMot().getTachoCount();
  }

  /**
   * Sets the rotation counter to zero and rotates the motor until the given count is reached.
   * If count is negative, the motor turns backwards.
   * This method returns when the count is reached and the motor stops.
   * @see #rotateTo(int count, boolean blocking)
   * @return the object reference to allow method chaining
   */
  public GenericMotor rotateTo(int count)
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
   * @see #rotateTo(int count)
   * @return the object reference to allow method chaining
   */
  public GenericMotor rotateTo(int count, boolean blocking)
  {
    return rotateInternal(count, blocking, true);
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
  public GenericMotor continueTo(int count)
  {
    return rotateInternal(count, true, false);
  }

  /**
   * Same as rotateTo(int count, boolean blocking), but the rotation counter
   * is not set to zero.
   * The given count is the absolute value of the rotation counter to be reached.
   * If the current count is higher than the count to reach, the motor turns backward.
   * @param count the rotation counter value to be reached
   * @see #rotateTo(int count, boolean blocking)
   */
  public GenericMotor continueTo(int count, boolean blocking)
  {
    return rotateInternal(count, blocking, false);
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
  public GenericMotor continueRelativeTo(int count)
  {
    int currentCount = getMotorCount();
    return rotateInternal(count + currentCount, true, false);
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
  public GenericMotor continueRelativeTo(int count, boolean blocking)
  {
    int currentCount = getMotorCount();
    return rotateInternal(count + currentCount, blocking, false);
  }

  private GenericMotor rotateInternal(int count, boolean blocking, boolean resetting)
  {
    state = MotorState.UNDEFINED;
    checkConnect();
    if (isNxt)
    {
      if (resetting)
        _getMot().resetTachoCount();
      _getMot().setSpeed(Tools.round(speedMultiplier * speed));
      _getMot().rotateTo(count, !blocking);
    }
    else
    {
      if (resetting)
        getMot().resetTachoCount();
      getMot().setSpeed(Tools.round(speedMultiplier * speed));
      getMot().rotateTo(count, !blocking);
    }
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
    if (isNxt)
      return _getMot().isMoving();
    else
      return getMot().isMoving();
  }

  /**
   * Sets the acceleration rate of the motor in degrees/sec/sec
   * The default value is 6000. Smaller values make speed changes smoother.
   * @param acceleration the new accelaration
   */
  public void setAcceleration(int acceleration)
  {
    checkConnect();
    if (isNxt)
      _getMot().setAcceleration(acceleration);
    else
      getMot().setAcceleration(acceleration);
  }

  protected static lejos.hardware.port.Port getMotorPort(MotorPort port)
  {
    int portId = port.getId();
    switch (portId)
    {
      case 0:
        return lejos.hardware.port.MotorPort.A;
      case 1:
        return lejos.hardware.port.MotorPort.B;
      case 2:
        return lejos.hardware.port.MotorPort.C;
      case 3:
        return lejos.hardware.port.MotorPort.D;
      default:
        return lejos.hardware.port.MotorPort.A;
    }
  }

  private void checkConnect()
  {
    if (robot == null)
      new ShowError("Motor is not a part of the LegoRobot.\n"
        + "Call addPart() to assemble it.");
  }
}
