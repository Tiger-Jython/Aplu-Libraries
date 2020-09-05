// GenericGear.java

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

  // -------------- Inner class BreakThread ------------
  private class BrakeThread extends Thread
  {
    private volatile boolean doStop = true;

    private BrakeThread()
    {
      if (LegoRobot.getDebugLevel() >= SharedConstants.DEBUG_LEVEL_LOW)
        DebugConsole.show("BrTd created");
    }

    public void run()
    {
      if (LegoRobot.getDebugLevel() >= SharedConstants.DEBUG_LEVEL_LOW)
        DebugConsole.show("BrTd started");
      try
      {
        Thread.currentThread().sleep(brakeDelay);
      }
      catch (InterruptedException ex)
      {
      }
      if (doStop)
      {
        stopMotors();
        state = GearState.UNDEFINED;
        if (LegoRobot.getDebugLevel() >= SharedConstants.DEBUG_LEVEL_LOW)
          DebugConsole.show("BrTd term");
      }
      else
      {
        if (LegoRobot.getDebugLevel() >= SharedConstants.DEBUG_LEVEL_LOW)
          DebugConsole.show("BrTd term(n)");
      }
    }
  }
  // -------------- End of inner classes -----------------

  private boolean isNxt;
  private GearState state = GearState.UNDEFINED;
  private double arcRadius;
  protected Object mot1;
  protected Object mot2;
  private double axeLength;
  private int speed;
  private int acceleration;
  private int brakeDelay;
  private BrakeThread bt = null;

  protected GenericGear(MotorPort port1, MotorPort port2, boolean isNxt)
  {
    if (port1 == port2)
      new ShowError("Fatal error while constructing Gear():\nPorts must be different");
    this.isNxt = isNxt;
    if (isNxt)
    {
      mot1 = new NxtMotor(port1);
      mot2 = new NxtMotor(port2);
    }
    else
    {
      mot2 = new Motor(port1);  // Motors inversed on EV3
      mot1 = new Motor(port2);
    }
    EV3Properties props = new EV3Properties();
    speed = props.getIntValue("GearSpeed");
    axeLength = props.getDoubleValue("AxeLength");
    brakeDelay = props.getIntValue("GearBrakeDelay");
    acceleration = props.getIntValue("GearAcceleration");
  }

  protected GenericGear(boolean isNxt)
  {
    this(MotorPort.A, MotorPort.B, isNxt);
  }

  protected Motor getMot(Object mot)
  {
    return (Motor)mot;
  }

  protected NxtMotor _getMot(Object mot)
  {
    return (NxtMotor)mot;
  }

  protected void init()
  {
    if (LegoRobot.getDebugLevel() >= SharedConstants.DEBUG_LEVEL_MEDIUM)
      DebugConsole.show("Gear.init()");
    if (isNxt)
    {
      robot.addPart(_getMot(mot1));
      robot.addPart(_getMot(mot2));
      _getMot(mot1).setSpeed(speed);
      _getMot(mot2).setSpeed(speed);
      _getMot(mot1).setAcceleration(acceleration);
      _getMot(mot2).setAcceleration(acceleration);
    }
    else
    {
      robot.addPart(getMot(mot1));
      robot.addPart(getMot(mot2));
      getMot(mot1).setSpeed(speed);
      getMot(mot2).setSpeed(speed);
      getMot(mot1).setAcceleration(acceleration);
      getMot(mot2).setAcceleration(acceleration);
    }

  }

  protected void cleanup()
  {
    if (LegoRobot.getDebugLevel() >= SharedConstants.DEBUG_LEVEL_MEDIUM)
      DebugConsole.show("Gear.cleanup()");
    joinBrakeThread();
    stop();
    // Must stop gear 
    // Even if the motors are stopped by their cleanup, because both motors
    // must stop immediately
  }

  /**
   * Returns the current speed (arbitrary units).
   * @return speed 0..100
   */
  public int getSpeed()
  {
    if (isNxt)
      return _getMot(mot1).getSpeed();
    else
      return getMot(mot1).getSpeed();
  }

  /**
   * Returns the current velocity.
   * The velocity in m/s with some inaccuracy.<br>
   * velocity = MotorSpeedFactor * speed (default: MotorSpeedFactor set in
   * evjlib.properties).
   * @return velocity in m/s
   */
  public double getVelocity()
  {
    if (isNxt)
      return _getMot(mot1).getVelocity();
    else
      return getMot(mot1).getVelocity();
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
    if (isNxt)
    {
      _getMot(mot1).setSpeed(speed);
      _getMot(mot2).setSpeed(speed);
    }
    else
    {
      getMot(mot1).setSpeed(speed);
      getMot(mot2).setSpeed(speed);
    }
    state = GearState.UNDEFINED;
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
    if (this.speed == speed)
      return this;
    if (isNxt)
    {
      speed = _getMot(mot1).velocityToSpeed(velocity);
      _getMot(mot1).setVelocity(velocity);
      _getMot(mot2).setVelocity(velocity);
    }
    else
    {
      speed = getMot(mot1).velocityToSpeed(velocity);
      getMot(mot1).setVelocity(velocity);
      getMot(mot2).setVelocity(velocity);
    }
    state = GearState.UNDEFINED;
    return this;
  }

  /**
   * Stops the gear.
   * (If motor is already stopped, returns immediately.)
   * @return the object reference to allow method chaining.
   */
  public synchronized GenericGear stop()
  {
    if (state == GearState.STOPPED)
      return this;
    checkConnect();
    joinBrakeThread();
    stopMotors();
    state = GearState.STOPPED;
    return this;
  }

  private synchronized void stopMotors()
  {
    if (isNxt)
    {
      _getMot(mot2).stop(true);  // return immediately
      _getMot(mot1).stop(true);  // return immediately
      // Wait until complete stop
      while (_getMot(mot1).isMoving() || _getMot(mot2).isMoving())
      {
      }
    }
    else
    {
      getMot(mot2).stop(true);  // return immediately
      getMot(mot1).stop(true);  // return immediately
      // Wait until complete stop
      while (getMot(mot1).isMoving() || getMot(mot2).isMoving())
      {
      }
    }
  }

  /**
   * Sets the rotation counter to zero and rotate both motors until the given count is reached.
   * If count is negative, the motors turn backwards.
   * If blocking = false, the method returns immediately.
   * You may register a motion listener with addMotionListener() to get a callback
   * when the count is reached and the motors stop.
   * If blocking = true, the method returns when the count is reached and
   * the motors stop.<br>
   * @see #moveTo(int count)
   * @return the object reference to allow method chaining
   */
  public synchronized GenericGear moveTo(int count, boolean blocking)
  {
    state = GearState.UNDEFINED;
    checkConnect();
    if (isNxt)
    {
      _getMot(mot1).rotateTo(count, false);  // return immediately
      _getMot(mot2).rotateTo(count, blocking);
      while (_getMot(mot1).isMoving()) // Must wait until mot1 is spinned down
      {
      }
    }
    else
    {
      getMot(mot1).rotateTo(count, false); // return immediately
      getMot(mot2).rotateTo(count, blocking);
      while (getMot(mot1).isMoving()) // Must wait until mot1 is spinned down
      {
      }
    }
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
    if (isNxt)
    {
      _getMot(mot1).rotateTo(-count, false); // return immediately
      _getMot(mot2).rotateTo(count, blocking);
      while (_getMot(mot1).isMoving())  // Must wait until mot1 is spinned down
      {
      }
    }
    else
    {
      getMot(mot1).rotateTo(-count, false); // return immediately
      getMot(mot2).rotateTo(count, blocking);
      while (getMot(mot1).isMoving())  // Must wait until mot1 is spinned down
      {
      }
    }
    return this;
  }

  /**
   * Returns current value of the rotation counter of the left motor.
   * @return the value of the left motor's rotation counter
   */
  public int getLeftMotorCount()
  {
    if (isNxt)
      return _getMot(mot1).getMotorCount();
    else
      return getMot(mot1).getMotorCount();
  }

  /**
   * Returns current value of the rotation counter of the right motor.
   * @return the value of the right motor's rotation counter
   */
  public int getRightMotorCount()
  {
    if (isNxt)
      return _getMot(mot2).getMotorCount();
    else
      return getMot(mot2).getMotorCount();
  }

  /**
   * Resets the rotation counter of the left motor to zero.
   * Keep in mind that this may influence other actions in progress.
   */
  public void resetLeftMotorCount()
  {
    if (isNxt)
      _getMot(mot1).resetMotorCount();
    else
      getMot(mot1).resetMotorCount();
  }

  /**
   * Resets the rotation counter of the left motor to zero.
   * Keep in mind that this may influence other actions in progress.
   */
  public void resetRightMotorCount()
  {
    if (isNxt)
      _getMot(mot2).resetMotorCount();
    else
      getMot(mot2).resetMotorCount();
  }

  /**
   * Sets the acceleration rate of both motors in degrees/sec/sec
   * Smaller values make speed changes smoother.
   * @param acceleration the new accelaration
   */
  public synchronized void setAcceleration(int acceleration)
  {
    checkConnect();
    if (isNxt)
    {
      _getMot(mot1).setAcceleration(acceleration);
      _getMot(mot2).setAcceleration(acceleration);
    }
    else
    {
      getMot(mot1).setAcceleration(acceleration);
      getMot(mot2).setAcceleration(acceleration);
    }
  }

  /**
   * Starts the forward movement with preset speed.
   * Method returns, while the movement continues.
   * (If motor is already rotating with same speed, returns immediately.)
   * @return the object reference to allow method chaining.
   */
  public synchronized GenericGear forward()
  {
    joinBrakeThread();
    if (state == GearState.FORWARD)
      return this;
    checkConnect();
    if (isNxt)
    {
      _getMot(mot1).forward();
      _getMot(mot2).forward();
    }
    else
    {
      getMot(mot1).forward();
      getMot(mot2).forward();
    }

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
   To use it without problems in a callback method, it returns quickly
   when LegoRobot.disconnect is called.
   * @param duration the duration (in ms)
   * @return the object reference to allow method chaining
   */
  public GenericGear forward(int duration)
  {
    forward();
    Tools.delay(duration);
    startBrakeThread();
    return this;
  }

  /**
   * Same as forward(), but move in reverse direction.
   * @see #forward()
   */
  public synchronized GenericGear backward()
  {
    joinBrakeThread();
    if (state == GearState.BACKWARD)
      return this;
    checkConnect();
    if (isNxt)
    {
      _getMot(mot1).backward();
      _getMot(mot2).backward();
    }
    else
    {
      getMot(mot1).backward();
      getMot(mot2).backward();
    }
    state = GearState.BACKWARD;
    return this;
  }

  /**
   * Same as forward(int duration), but move in reverse direction.
   * @see #forward(int duration)
   * @return the object reference to allow method chaining
   */
  public GenericGear backward(int duration)
  {
    backward();
    Tools.delay(duration);
    startBrakeThread();
    return this;
  }

  /**
   * Starts turning left with left motor rotating forward and
   * right motor rotating backward at preset speed.
   * Method returns, while the movement continues.
   * (If gear is already turning left, returns immediately.)
   * @return the object reference to allow method chaining
   */
  public synchronized GenericGear left()
  {
    joinBrakeThread();
    if (state == GearState.LEFT)
      return this;
    checkConnect();
    joinBrakeThread();
    if (isNxt)
    {
      _getMot(mot1).forward();
      _getMot(mot2).backward();
    }
    else
    {
      getMot(mot1).forward();
      getMot(mot2).backward();
    }
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
    left();
    Tools.delay(duration);
    startBrakeThread();
    return this;
  }

  /**
   * Same as left(), but turns in the opposite direction.
   * @see #left()
   * @return the object reference to allow method chaining
   */
  public synchronized GenericGear right()
  {
    joinBrakeThread();
    if (state == GearState.RIGHT)
      return this;
    checkConnect();
    joinBrakeThread();
    if (isNxt)
    {
      _getMot(mot1).backward();
      _getMot(mot2).forward();
    }
    else
    {
      getMot(mot1).backward();
      getMot(mot2).forward();
    }
    state = GearState.RIGHT;
    return this;
  }

  /**
   * Same as left(int duration), but turns in the opposite direction.
   * @see #left(int duration)
   * @return the object reference to allow method chaining
   */
  public GenericGear right(int duration)
  {
    right();
    Tools.delay(duration);
    startBrakeThread();
    return this;
  }

  /**
   * Starts turning to the left on an arc with given radius (in m).
   * If the radius is negative, turns left backwards.
   * The accuracy is limited and depends on the distance between the
   * two wheels (default: AxeLength set in EV3JLib.properties).
   * Method returns, while the movement continues.
   * (If gear is already moving on an arc with given radius, returns immediately.)
   * @param radius the radius of the arc (in m)
   * @return the object reference to allow method chaining
   */
  public synchronized GenericGear leftArc(double radius)
  {
    joinBrakeThread();
    if (state == GearState.LEFTARC && arcRadius == radius)
      return this;
    if (Math.abs(radius) < axeLength)
      new ShowError("Fatal error: radius can't be smaller than axe length (" + axeLength + ")\nin Gear.leftArc()");
    checkConnect();
    joinBrakeThread();
    int speed1 = Tools.round(speed * (Math.abs(radius) - axeLength) / (Math.abs(radius) + axeLength));
    if (LegoRobot.getDebugLevel() >= SharedConstants.DEBUG_LEVEL_MEDIUM)
      DebugConsole.show("Speeds: " + speed + " | " + speed1);
    if (isNxt)
    {
      _getMot(mot1).setSpeed(speed);
      _getMot(mot2).setSpeed(speed1);  // Reduce speed of left motor
      if (radius > 0)
      {
        _getMot(mot1).forward();
        _getMot(mot2).forward();
      }
      else
      {
        _getMot(mot1).backward();
        _getMot(mot2).backward();
      }
      _getMot(mot2).setSpeed(speed);
    }
    else
    {
      getMot(mot1).setSpeed(speed);
      getMot(mot2).setSpeed(speed1);  // Reduce speed of left motor
      if (radius > 0)
      {
        getMot(mot1).forward();
        getMot(mot2).forward();
      }
      else
      {
        getMot(mot1).backward();
        getMot(mot2).backward();
      }
      getMot(mot2).setSpeed(speed);
    }
    state = GearState.LEFTARC;
    arcRadius = radius;
    return this;
  }

  /**
   * Same as leftArc(radius) but radius in mm.
   */
  public synchronized GenericGear leftArcMilli(int radius)
  {
    return leftArc(radius / 1000.0);
  }

  /**
   * Starts turning to the left on an arc with given radius (in m) for the given
   * duration (in ms) with preset speed.
   * If the radius is negative, turns left backwards.
   * The accuracy is limited and depends on the distance between the
   * two wheels (default: AxeLength set in NxtLib.properties).
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
  public synchronized GenericGear leftArc(double radius, int duration)
  {
    leftArc(radius);
    Tools.delay(duration);
    startBrakeThread();
    return this;
  }

  /**
   * Same as leftArc(radius, duration) but radius in mm.
   */
  public synchronized GenericGear leftArcMilli(int radius, int duration)
  {
    return leftArc(radius / 1000.0, duration);
  }

  /**
   * Same as leftArc(double radius), but turns to the right.
   * @see #leftArc(double radius)
   * @return the object reference to allow method chaining
   */
  public synchronized GenericGear rightArc(double radius)
  {
    joinBrakeThread();
    if (state == GearState.RIGHTARC && arcRadius == radius)
      return this;
    if (Math.abs(radius) < axeLength)
      new ShowError("Fatal error: radius can't be smaller than axe length (" + axeLength + ")\nin Gear.rightArc()");
    checkConnect();
    joinBrakeThread();
    int speed1 = Tools.round(speed * (Math.abs(radius) - axeLength) / (Math.abs(radius) + axeLength));
    if (LegoRobot.getDebugLevel() >= SharedConstants.DEBUG_LEVEL_MEDIUM)
      DebugConsole.show("Speeds: " + speed + " | " + speed1);
    if (isNxt)
    {
      _getMot(mot1).setSpeed(speed1);  // Reduce speed of right motor
      _getMot(mot2).setSpeed(speed);
      if (radius > 0)
      {
        _getMot(mot1).forward();
        _getMot(mot2).forward();
      }
      else
      {
        _getMot(mot1).backward();
        _getMot(mot2).backward();
      }
      _getMot(mot1).setSpeed(speed);
    }
    else
    {
      getMot(mot1).setSpeed(speed1);  // Reduce speed of right motor
      getMot(mot2).setSpeed(speed);
      if (radius > 0)
      {
        getMot(mot1).forward();
        getMot(mot2).forward();
      }
      else
      {
        getMot(mot1).backward();
        getMot(mot2).backward();
      }
      getMot(mot1).setSpeed(speed);
    }
    state = GearState.RIGHTARC;
    return this;
  }

  /**
   * Same as rightArc(radius) but radius in mm.
   */
  public synchronized GenericGear rightArcMilli(int radius)
  {
    return rightArc(radius / 1000.0);
  }

  /**
   * Same as leftArc(double radius, int duration), but turns to the right.
   * @see #leftArc(double radius, int duration)
   * @return the object reference to allow method chaining
   */
  public GenericGear rightArc(double radius, int duration)
  {
    rightArc(radius);
    Tools.delay(duration);
    startBrakeThread();
    return this;
  }

  /**
   * Same as rightArc(radius, duration) but radius in mm.
   */
  public synchronized GenericGear rightArcMilli(int radius, int duration)
  {
    return rightArc(radius / 1000.0, duration);
  }

  /**
   * Sets the axe length (distance between while centers).
   * @param axeLength the axe length in mm
   * @return the object reference to allow method chaining
   */
  public GenericGear setAxeLengthMilli(int axeLength)
  {
    this.axeLength = axeLength / 1000.0;
    return this;
  }

  /**
   * Sets the break delay (time to wait until stopping motors).
   *  @param brakeDelay delay in ms
   * @return the object reference to allow method chaining
   */
  public GenericGear setBrakeDelay(int brakeDelay)
  {
    this.brakeDelay = brakeDelay;
    return this;
  }

  /**
   * Checks if one or both motors are rotating.
   * @return true, if gear is moving, otherwise false
   */
  public synchronized boolean isMoving()
  {
    if (isNxt)
      return (_getMot(mot1).isMoving() || _getMot(mot2).isMoving());
    else
      return (getMot(mot1).isMoving() || getMot(mot2).isMoving());
  }

  private void checkConnect()
  {
    if (robot == null)
      new ShowError("Gear is not a part of the LegoRobot.\n"
        + "Call addPart() to assemble it.");
  }

  private void startBrakeThread()
  {
    bt = new BrakeThread();
    bt.start();
  }

  private void joinBrakeThread()
  {
    if (bt != null && bt.isAlive())
    {
      bt.doStop = false;
      bt.interrupt();
      try
      {
        bt.join(500);
      }
      catch (InterruptedException ex)
      {
      }
    }
  }
}
