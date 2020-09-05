// GenericGear.java
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
 * Abstract class that represents the combination of two motors on an axis to perform a car-like movement.
 */
public abstract class GenericGear extends Part
{
  enum GearState
  {
    FORWARD, BACKWARD, STOPPED, LEFT, RIGHT,
    LEFTARC, RIGHTARC, UNDEFINED
  };

  private GearState state = GearState.UNDEFINED;
  private int speed;
  private int acceleration;
  private double axeLength;
  private int brakeDelay;
  private double speedFactor;
  private double arcRadius = 0;

  protected GenericGear(boolean isNxt)
  {
    EV3Properties props = LegoRobot.getProperties();
    speed = props.getIntValue("GearSpeed");
    acceleration = props.getIntValue("GearAcceleration");
    speedFactor = props.getDoubleValue("MotorSpeedFactor");
    axeLength = props.getDoubleValue("AxeLength");
    brakeDelay = props.getIntValue("GearBrakeDelay");
    partName = isNxt ? "_gear" : "gear";
  }

  protected void init()
  {
    if (LegoRobot.getDebugLevel() >= SharedConstants.DEBUG_LEVEL_MEDIUM)
      DebugConsole.show("DEBUG: GenericGear.init() called");
    robot.sendCommand(partName + ".setAxeLengthMilli." + (int)(1000 * axeLength));
    robot.sendCommand(partName + ".setBrakeDelay." + brakeDelay);
    setSpeed(speed);
    setAcceleration(acceleration);
  }

  protected void cleanup()
  {
    if (LegoRobot.getDebugLevel() >= SharedConstants.DEBUG_LEVEL_MEDIUM)
      DebugConsole.show("DEBUG: GenericGear.cleanup() called");
    // Two motors cleanup called anyway
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
   * Returns the current velocity.
   * The velocity in m/s with some inaccuracy.<br>
   * velocity = MotorSpeedFactor * speed (default: MotorSpeedFactor set in
   * ev3jlib.properties).
   * @return velocity in m/s
   */
  public double getVelocity()
  {
    return speedToVelocity(speed);
  }

  /**
   * Sets the speed to the given value (arbitrary units).
   * The speed will be changed to the new value at the next movement call only.
   * @param speed 0..100
   * @return the object reference to allow method chaining
   */
  public synchronized GenericGear setSpeed(int speed)
  {
    if (this.speed == speed)
      return this;
    this.speed = speed;
    if (robot != null)
    {
      robot.sendCommand(partName + ".setSpeed." + speed);
      state = GearState.UNDEFINED;
    }
    return this;
  }

  /**
   * Sets the acceleration rate of both motors in degrees/sec/sec
   * Smaller values make speed changes smoother.
   * @param acceleration the new accelaration
   */
  public synchronized GenericGear setAcceleration(int acceleration)
  {
    if (this.acceleration == acceleration)
      return this;
    this.acceleration = acceleration;
    if (robot != null)
      robot.sendCommand(partName + ".setAcceleration." + acceleration);
    return this;
  }

  /**
   * Sets the velocity to the given value.
   * The velocity will be changed to the new value at the next movement call only.<br>
   * velocity = MotorSpeedFactor * speed (default MotorSpeedFactor set in
   * ev3jlib.properties).
   * @param velocity in m/s
   * @return the object reference to allow method chaining.
   */
  public synchronized GenericGear setVelocity(double velocity)
  {
    speed = velocityToSpeed(velocity);
    setSpeed(speed);
    return this;
  }

  /**
   * Stops the gear.
   * (If gear is already stopped, returns immediately.)
   * @return the object reference to allow method chaining.
   */
  public GenericGear stop()
  {
    if (state == GearState.STOPPED)
      return this;
    checkConnect();
    robot.sendCommand(partName + ".stop");
    state = GearState.STOPPED;
    return this;
  }

  /**
   * Sets the rotation counter to zero and rotate both motors until the given count is reached.
   * If count is negative, the motors turn backwards.
   * If blocking = false, the method returns immediately.
   * You may register a motion listener with addMotionListener() to get a callback
   * when the count is reached and the  motors stop.
   * If blocking = true, the method returns when the count is reached and
   * the motors stop.<br>
   * The motors start smoothly if the speed is not too fast.
   * @see #moveTo(int count)
   * @return the object reference to allow method chaining
   */
  public synchronized GenericGear moveTo(int count, boolean blocking)
  {
    state = GearState.UNDEFINED;
    checkConnect();
    robot.sendCommand(partName + ".moveTo." + count + (blocking ? ".b1" : ".b0"));
    return this;
  }

  /**
   * Same as moveTo(int count, boolean blocking) with blocking = true.
   * @see #moveTo(int count, boolean blocking)
   */
  public GenericGear moveTo(int count)
  {
    return moveTo(count, true);
  }

  /**
   * Same as turnTo(int count, boolean blocking) with blocking = true.
   * @see #turnTo(int count, boolean blocking)
   */
  public GenericGear turnTo(int count)
  {
    return turnTo(count, true);
  }

  /**
   * Sets the rotation counter to zero and turn with the motors running in
   * opposite direction.
   * If count is positive, turns clockwise, otherwise anti-clockwise
   * (MotorPort.A is right, MotorPort.B is left motor).
   * If blocking = false, the method returns immediately.
   * If blocking = true, the method returns when the count is reached and
   * the motors stop.<br>
   * @see #turnTo(int count)
   * @return the object reference to allow method chaining.
   */
  public synchronized GenericGear turnTo(int count, boolean blocking)
  {
    state = GearState.UNDEFINED;
    checkConnect();
    robot.sendCommand(partName + ".turnTo." + count + (blocking ? ".b1" : ".b0"));
    return this;
  }

  /**
   * Starts the forward movement with preset speed.
   * Method returns, while the movement continues.
   * (If gear is already moving forward, returns immediately.)
   * @return the object reference to allow method chaining.
   */
  public GenericGear forward()
  {
    if (state == GearState.FORWARD)
      return this;
    checkConnect();
    robot.sendCommand(partName + ".forward");
    state = GearState.FORWARD;
    return this;
  }

  /**
   * Starts the forward movement for the given duration (in ms) with preset speed.
   * Method returns at the end of the given duration, but the
   * movement continues for 200 ms. Then it stops unless another movement method
   * call (forward, backward, left, right, leftArc, rightArc) is
   * invoked within that time.
   * Calling several movement methods one after the other will result
   * a seamless movement without intermediate stops.<br>
   * To use it without problems in a callback method, it returns quickly
   * when LegoRobot.disconnect is called.
   * @param duration the duration (in ms)
   * @return the object reference to allow method chaining
   */
  public GenericGear forward(int duration)
  {
    checkConnect();
    robot.sendCommand(partName + ".forward." + duration);
    state = GearState.STOPPED;
    return this;
  }

  /**
   * Same as forward(), but move in reverse direction.
   * @see #forward()
   */
  public synchronized GenericGear backward()
  {
    if (state == GearState.BACKWARD)
      return this;
    checkConnect();
    robot.sendCommand(partName + ".backward");
    state = GearState.BACKWARD;
    return this;
  }

  /**
   * Same as forward(int duration), but move in reverse direction.
   * @see #forward(int duration)
   */
  public synchronized GenericGear backward(int duration)
  {
    checkConnect();
    robot.sendCommand(partName + ".backward." + duration);
    state = GearState.STOPPED;
    return this;
  }

  /**
   * Starts turning left with left motor stopped and right motor at preset speed.
   * Method returns, while the movement continues.
   * (If gear is already turning left, returns immediately.)
   * @return the object reference to allow method chaining
   */
  public GenericGear left()
  {
    if (state == GearState.LEFT)
      return this;
    checkConnect();
    robot.sendCommand(partName + ".left");
    state = GearState.LEFT;
    return this;
  }

  /**
   * Starts turning left for the given duration (in ms) with preset speed.
   * Method returns at the end of the given duration but the
   * movement continues for 200 ms. Then it stops unless another movement method
   * call (forward, backward, left, right, leftArc, rightArc) is
   * invoked within that time.
   * Calling several movement methods one after the other will result
   * a seamless movement without intermediate stops.
   * @param duration the duration (in ms)
   * @return the object reference to allow method chaining
   */
  public GenericGear left(int duration)
  {
    checkConnect();
    robot.sendCommand(partName + ".left." + duration);
    state = GearState.STOPPED;
    return this;
  }

  /**
   * Same as left(), but turns in the opposite direction.
   * @see #left()
   */
  public GenericGear right()
  {
    if (state == GearState.RIGHT)
      return this;
    checkConnect();
    robot.sendCommand(partName + ".right");
    state = GearState.RIGHT;
    return this;
  }

  /**
   * Same as left(int duration), but turning in the opposite direction.
   * @see #left(int duration)
   */
  public GenericGear right(int duration)
  {
    checkConnect();
    robot.sendCommand(partName + ".right." + duration);
    state = GearState.STOPPED;
    return this;
  }

  /**
   * Starts turning to the left on an arc with given radius (in m).
   * If the radius is negative, turns left backwards.
   * The accuracy is limited and depends on the distance between the
   * two wheels (default: AxeLength set in ev3jlib.properties).
   * Method returns, while the movement continues.
   * (If gear is already moving on an arc with given radius, returns immediately.)
   * @param radius the radius of the arc (in m)
   * @return the object reference to allow method chaining
   */
  public GenericGear leftArc(double radius)
  {
    if (state == GearState.LEFTARC && arcRadius == radius)
      return this;
    checkConnect();
    arcRadius = radius;
    robot.sendCommand(partName + ".leftArcMilli." + (int)(1000 * radius));
    state = GearState.LEFTARC;
    return this;
  }

  /**
   * Starts turning to the left on an arc with given radius (in m) for the given
   * duration (in ms) with preset speed.
   * If the radius is negative, turns left backwards.
   * The accuracy is limited and depends on the distance between the
   * two wheels (default: AxeLength set in ev3jlib.properties).
   * Method returns at the end of the given duration but the
   * movement continues for 200 ms. Then it stops unless another movement method
   * call (forward, backward, left, right, leftArc, rightArc) is
   * invoked within that time.
   * Calling several movement methods one after the other will result
   * a seamless movement without intermediate stops.
   * @param radius the radius of the arc (in m)
   * @param duration the duration (in ms)
   * @return the object reference to allow method chaining
   */
  public GenericGear leftArc(double radius, int duration)
  {
    checkConnect();
    arcRadius = radius;
    robot.sendCommand(partName + ".leftArcMilli." + (int)(1000 * radius) + "." + duration);
    state = GearState.STOPPED;
    return this;
  }

  /**
   * Same as leftArc(double radius), but turns in the right.
   * @see #leftArc(double radius)
   */
  public GenericGear rightArc(double radius)
  {
    if (state == GearState.RIGHTARC && arcRadius == radius)
      return this;
    checkConnect();
    arcRadius = radius;
    robot.sendCommand(partName + ".rightArcMilli." + (int)(1000 * radius));
    state = GearState.RIGHTARC;
    return this;
  }

  /**
   * Same as leftArc(double radius, int duration), but turns to the right.
   * @see #leftArc(double radius, int duration)
   */
  public GenericGear rightArc(double radius, int duration)
  {
    checkConnect();
    arcRadius = radius;
    robot.sendCommand(partName + ".rightArcMilli." + (int)(1000 * radius) + "." + duration);
    state = GearState.STOPPED;
    return this;

  }

  /**
   * Checks if one or both motors are rotating.
   * @return true, if gear is moving, otherwise false
   */
  public boolean isMoving()
  {
    checkConnect();
    Tools.delay(1);
    String rc = robot.sendCommand(partName
      + ".isMoving.n.n");
    return (rc.equals("1"));
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
   * Returns current value of the rotation counter of the left motor.
   * @return the value of the left motor's rotation counter
   */
  public int getLeftMotorCount()
  {
    checkConnect();
    int v = 0;
    try
    {
      v = Integer.parseInt(robot.sendCommand(partName + ".getLeftMotorCount"));
    }
    catch (NumberFormatException ex)
    {
    }
    return v;
  }

  /**
   * Returns current value of the rotation counter of the right motor.
   * @return the value of the right motor's rotation counter
   */
  public int getRightMotorCount()
  {
    checkConnect();
    int v = 0;
    try
    {
      v = Integer.parseInt(robot.sendCommand(partName + ".getRightMotorCount"));
    }
    catch (NumberFormatException ex)
    {
    }
    return v;
  }
  
  /**
   * Resets the rotation counter of the left motor to zero.
   * Keep in mind that this may influence other actions in progress.
   */
  public void resetLeftMotorCount()
  {
    checkConnect();
    robot.sendCommand(partName + ".resetLeftMotorCount");
  }

  /**
   * Resets the rotation counter of the right motor to zero.
   * Keep in mind that this may influence other actions in progress.
   */
  public void resetRightMotorCount()
  {
    checkConnect();
    robot.sendCommand(partName + ".resetRightMotorCount");
  }

  private void checkConnect()
  {
    if (robot == null)
      new ShowError("Gear is not a part of the EV3Robot.\n"
        + "Call addPart() to assemble it.");
  }
}
