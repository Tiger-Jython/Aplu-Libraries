 // Gear.java

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
 * Combines two motors on an axis to perform a car-like movement.
 */
public class Gear extends Part
{
  enum GearState
  {
    FORWARD, BACKWARD, STOPPED, LEFT, RIGHT,
    LEFTARC, RIGHTARC, UNDEFINED
  };

  // -------------- Inner class BreakThread ------------
  private class BrakeThread extends NxtThread
  {
    private volatile boolean doStop = true;

    private BrakeThread()
    {
      if (LegoRobot.getDebugLevel() >= DEBUG_LEVEL_LOW)
        DebugConsole.show("DEBUG: BrakeThread created");
    }

    public void run()
    {
      if (LegoRobot.getDebugLevel() >= DEBUG_LEVEL_LOW)
        DebugConsole.show("DEBUG: BrakeThread started");
      try
      {
        Thread.currentThread().sleep(brakeDelay);
      }
      catch (InterruptedException ex)
      {
      }
      if (doStop)
      {
        mot1.stop();
        mot2.stop();
        state = GearState.UNDEFINED;
        if (LegoRobot.getDebugLevel() >= DEBUG_LEVEL_LOW)
          DebugConsole.show("DEBUG: BrakeThread terminated (gear stopped)");
      }
      else if (LegoRobot.getDebugLevel() >= DEBUG_LEVEL_LOW)
        DebugConsole.show("DEBUG: BrakeThread terminated (gear not stopped)");
    }
  }
  // -------------- End of inner classes -----------------
  private double axeLength;
  private GearState state = GearState.UNDEFINED;
  private double arcRadius;
  private Motor mot1;
  private Motor mot2;
  private int speed;
  private BrakeThread bt = null;
  private int brakeDelay;
  private int accelerationDelay;

  /**
   * Creates a gear instance with right motor plugged into port1, left motor plugged into port2.
   * @param port1 MotorPort.A or MotorPort.B or MotorPort.C (not both the same)
   * @param port2 MotorPort.A or MotorPort.B or MotorPort.C (not both the same)
   */
  public Gear(MotorPort port1, MotorPort port2)
  {
    if (port1 == port2)
      new ShowError("Fatal error while constructing Gear():\nPorts must be different");
    mot1 = new Motor(port1);
    mot2 = new Motor(port2);
    NxtProperties props = LegoRobot.getProperties();
    speed = props.getIntValue("GearSpeed");
    axeLength = props.getDoubleValue("AxeLength");
    brakeDelay = props.getIntValue("GearBrakeDelay");
    accelerationDelay = props.getIntValue("GearAccelerationDelay");
  }

  /**
   * Creates a gear instance with left motor plugged into port A, right motor plugged into port B.
   */
  public Gear()
  {
    this(MotorPort.A, MotorPort.B);
  }

  protected void init()
  {
    if (LegoRobot.getDebugLevel() >= DEBUG_LEVEL_MEDIUM)
      DebugConsole.show("DEBUG: Gear.init() called");

    robot.addPart(mot1);
    robot.addPart(mot2);
    mot1.setSpeed(speed);
    mot2.setSpeed(speed);
  }

  protected void cleanup()
  {
    if (LegoRobot.getDebugLevel() >= DEBUG_LEVEL_MEDIUM)
      DebugConsole.show("DEBUG: Gear.cleanup() called");
    // Two motors cleanup called anyway
  }

  /**
   * Returns the current speed (arbitrary units).
   * @return speed 0..100
   */
  public int getSpeed()
  {
    return mot1.getSpeed();
  }

  /**
   * Returns the current velocity.
   * The velocity in m/s with some inaccuracy.<br>
   * velocity = MotorSpeedFactor * speed (default: MotorSpeedFactor set in
   * NxtLib.properties).
   * @return velocity in m/s
   */
  public double getVelocity()
  {
    return mot1.getVelocity();
  }

  /**
   * Sets the speed to the given value (arbitrary units).
   * The speed will be changed to the new value at the next movement call only.
   * @param speed 0..100
   * @return the object reference to allow method chaining
   */
  public synchronized Gear setSpeed(int speed)
  {
    if (this.speed == speed)
      return this;
    this.speed = speed;
    mot1.setSpeed(speed);
    mot2.setSpeed(speed);
    state = GearState.UNDEFINED;
    return this;
  }

  /**
   * Sets the velocity to the given value.
   * The velocity will be changed to the new value at the next movement call only.<br>
   * velocity = MotorSpeedFactor * speed (default MotorSpeedFactor set in
   * NxtLib.properties).
   * @param velocity in m/s
   * @return the object reference to allow method chaining.
   */
  public synchronized Gear setVelocity(double velocity)
  {
    speed = mot1.velocityToSpeed(velocity);
    if (this.speed == speed)
      return this;
    mot1.setVelocity(velocity);
    mot2.setVelocity(velocity);
    state = GearState.UNDEFINED;
    return this;
  }

  /**
   * Stops the gear.
   * (If gear is already stopped, returns immediately.)
   * @return the object reference to allow method chaining.
   */
  public Gear stop()
  {
    if (state == GearState.STOPPED)
      return this;
    checkConnect();
    joinBrakeThread();
    synchronized (this)
    {
      mot1.stop();
      mot2.stop();
    }
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
   * @see #addMotionListener(MotionListener motionListener)
   * @see #moveTo(int count)
   * @return the object reference to allow method chaining
   */
  public synchronized Gear moveTo(int count, boolean blocking)
  {
    state = GearState.UNDEFINED;
    checkConnect();
    mot1.resetMotorCount();
    mot2.resetMotorCount();
    int speed = getSpeed();
    if (count > 0)
    {
      for (int s = 10; s < speed; s += 10)
      {
        setSpeed(s);
        mot1.forward();
        mot2.forward();
        if (accelerationDelay > 0)
          delay(accelerationDelay);
      }
      setSpeed(speed);
      mot1.forward(true);
      mot2.forward(true);
    }
    else
    {
      for (int s = 10; s < speed; s += 10)
      {
        setSpeed(s);
        mot1.backward();
        mot2.backward();
        if (accelerationDelay > 0)
          delay(accelerationDelay);
      }
      setSpeed(speed);
      mot1.backward(true);
      mot2.backward(true);
    }
    mot1.continueTo(count, false);
    mot2.continueTo(count, blocking);
    state = GearState.STOPPED;
    return this;
  }

  /**
   * Same as moveTo(int count, boolean blocking) with blocking = true.
   * @see #moveTo(int count, boolean blocking)
   */
  public Gear moveTo(int count)
  {
    return moveTo(count, true);
  }

  /**
   * Same as turnTo(int count, boolean blocking) with blocking = true.
   * @see #turnTo(int count, boolean blocking)
   */
  public Gear turnTo(int count)
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
  public synchronized Gear turnTo(int count, boolean blocking)
  {
    state = GearState.UNDEFINED;
    checkConnect();
    mot1.resetMotorCount();
    mot2.resetMotorCount();
    if (count > 0)
    {
      mot1.backward();
      mot2.forward();
    }
    else
    {
      mot1.forward();
      mot2.backward();
    }
    mot1.continueTo(-count, false);
    mot2.continueTo(count, blocking);
    state = GearState.STOPPED;
    return this;
  }

  /**
   * Register the given motion listener.
   * When calling moveTo() a motion detector thread is started that checks
   * the motion of the motors. When the motion stops because the rotation count
   * is reached or stop() is called, the MotionListener's callback
   * motionStopped() is invoked and the motion detector thread terminates.
   * @param motionListener the MotionListener to be registered
   */
  public void addMotionListener(MotionListener motionListener)
  {
    mot1.addMotionListener(motionListener);
  }

  /**
   * Starts the forward movement with preset speed.
   * Method returns, while the movement continues.
   * (If gear is already moving forward, returns immediately.)
   * @return the object reference to allow method chaining.
   */
  public Gear forward()
  {
    joinBrakeThread();
    if (state == GearState.FORWARD)
      return this;
    checkConnect();
    synchronized (this)
    {
      mot1.forward();
      mot2.forward();
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
  public Gear forward(int duration)
  {
    forward();
    delay(duration);
    startBrakeThread();
    state = GearState.STOPPED;
    return this;
  }

  /**
   * Same as forward(), but move in reverse direction.
   * @see #forward()
   */
  public synchronized Gear backward()
  {
    joinBrakeThread();
    if (state == GearState.BACKWARD)
      return this;
    checkConnect();
    synchronized (this)
    {
      mot1.backward();
      mot2.backward();
    }
    state = GearState.BACKWARD;
    return this;
  }

  /**
   * Same as forward(int duration), but move in reverse direction.
   * @see #forward(int duration)
   */
  public synchronized Gear backward(int duration)
  {
    backward();
    delay(duration);
    startBrakeThread();
    state = GearState.STOPPED;
    return this;
  }

  /**
   * Starts turning left with left motor stopped and right motor at preset speed.
   * Method returns, while the movement continues.
   * (If gear is already turning left, returns immediately.)
   * @return the object reference to allow method chaining
   */
  public Gear left()
  {
    joinBrakeThread();
    if (state == GearState.LEFT)
      return this;
    checkConnect();
    synchronized (this)
    {
      mot1.forward();
      mot2.backward();
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
  public Gear left(int duration)
  {
    left();
    delay(duration);
    startBrakeThread();
    state = GearState.STOPPED;
    return this;
  }

  /**
   * Same as left(), but turns in the opposite direction.
   * @see #left()
   */
  public Gear right()
  {
    joinBrakeThread();
    if (state == GearState.RIGHT)
      return this;
    checkConnect();
    synchronized (this)
    {
      mot1.backward();
      mot2.forward();
    }
    state = GearState.RIGHT;
    return this;
  }

  /**
   * Same as left(int duration), but turning in the opposite direction.
   * @see #left(int duration)
   */
  public Gear right(int duration)
  {
    right();
    delay(duration);
    startBrakeThread();
    state = GearState.STOPPED;
    return this;
  }

  /**
   * Starts turning to the left on an arc with given radius (in m).
   * If the radius is negative, turns left backwards.
   * The accuracy is limited and depends on the distance between the
   * two wheels (default: AxeLength set in NxtLib.properties).
   * Method returns, while the movement continues.
   * (If gear is already moving on an arc with given radius, returns immediately.)
   * @param radius the radius of the arc (in m)
   * @return the object reference to allow method chaining
   */
  public Gear leftArc(double radius)
  {
    joinBrakeThread();
    if (state == GearState.LEFTARC && arcRadius == radius)
      return this;
    if (Math.abs(radius) < axeLength)
      new ShowError("Fatal error: radius can't be smaller than axe length (" + axeLength + ")\nin Gear.leftArc()");
    checkConnect();
    int speed1 = Tools.round(speed * (Math.abs(radius) - axeLength) / (Math.abs(radius) + axeLength));
    if (LegoRobot.getDebugLevel() >= DEBUG_LEVEL_MEDIUM)
      DebugConsole.show("DEBUG: speeds in Gear.leftArc(): " + speed + " | " + speed1);
    mot1.setSpeed(speed);
    mot2.setSpeed(speed1);  // Reduce speed of left motor
    if (radius > 0)
    {
      synchronized (this)
      {
        mot1.forward();
        mot2.forward();
      }
    }
    else
    {
      synchronized (this)
      {
        mot1.backward();
        mot2.backward();
      }
    }
    mot2.setSpeed(speed);
    state = GearState.LEFTARC;
    arcRadius = radius;
    return this;
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
  public Gear leftArc(double radius, int duration)
  {
    leftArc(radius);
    delay(duration);
    startBrakeThread();
    state = GearState.STOPPED;
    return this;
  }

  /**
   * Same as leftArc(double radius), but turns in the right.
   * @see #leftArc(double radius)
   */
  public Gear rightArc(double radius)
  {
    joinBrakeThread();
    if (state == GearState.RIGHTARC && arcRadius == radius)
      return this;
    if (Math.abs(radius) < axeLength)
      new ShowError("Fatal error: radius can't be smaller than axe length (" + axeLength + ")\nin Gear.rightArc()");
    checkConnect();
    int speed1 = Tools.round(speed * (Math.abs(radius) - axeLength) / (Math.abs(radius) + axeLength));
    if (LegoRobot.getDebugLevel() >= DEBUG_LEVEL_MEDIUM)
      DebugConsole.show("DEBUG: speeds in Gear.rightArc(): " + speed + " | " + speed1);
    mot1.setSpeed(speed1);  // Reduce speed of right motor
    mot2.setSpeed(speed);
    if (radius > 0)
    {
      synchronized (this)
      {
        mot1.forward();
        mot2.forward();
      }
    }
    else
    {
      synchronized (this)
      {
        mot1.backward();
        mot2.backward();
      }
    }
    mot1.setSpeed(speed);
    state = GearState.RIGHTARC;
    arcRadius = radius;
    return this;
  }

  /**
   * Same as leftArc(double radius, int duration), but turns to the right.
   * @see #leftArc(double radius, int duration)
   */
  public Gear rightArc(double radius, int duration)
  {
    rightArc(radius);
    delay(duration);
    startBrakeThread();
    state = GearState.STOPPED;
    return this;

  }

  /**
   * Returns right motor of the gear.
   * @return the reference of the right motor
   */
  public Motor getMotRight()
  {
    return mot1;
  }

  /**
   * Returns left motor of the gear.
   * @return the reference of the left motor
   */
  public Motor getMotLeft()
  {
    return mot2;
  }

  /**
   * Checks if one or both motors are rotating.
   * @return true, if gear is moving, otherwise false
   */
  public boolean isMoving()
  {
    return (mot1.isMoving() || mot2.isMoving());
  }

  private void checkConnect()
  {
    if (robot == null)
      new ShowError("Gear is not a part of the NxtRobot.\n"
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
        bt.joinX(500);
      }
      catch (InterruptedException ex)
      {
      }
    }
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
