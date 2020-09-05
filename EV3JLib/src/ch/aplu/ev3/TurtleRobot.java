// TurtleRobot.java
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
 * Implementation of the basic Logo turtle movements.
 */
public class TurtleRobot extends LegoRobot
{
  private String id = "";
  private Gear gear;
  protected double stepFactor;
  protected double rotationFactor;
  protected int turtleSpeed;
  protected int rotationSpeed;
  private TurtlePane tp = null;

  /**
   * Creates a turtle robot instance using a EV3 brick with given IP address.
   * A connection trial is engaged while an information box is shown.
   If the connection fails, the calling thread will be put in a wait state and the program hangs.
   The user can then terminate the application by closing the information pane.
   established at this time.<br><br>
   * If showConsole = true, the turtle commands are displayed in a console window.
   * If ipAddress = null, a input dialog is displayed where the IP address can be entered.
   * @param ipAddress the IP address, e.g. 10.0.1.1
   * @param showPane if true, an information pane is showed, where the turtle commands are listed
   */
  public TurtleRobot(String ipAddress, boolean showPane)
  {
    super(ipAddress);
    init(showPane);
  }

  /**
   * Same as TurtleLegoRobot(ipAddress, true)
   * @param ipAddress the IP address, e.g. 10.0.1.1
   */
  public TurtleRobot(String ipAddress)
  {
    this(ipAddress, true);
  }

  /**
   * Same as TurtleLegoRobot(ipAddress), but ask for the IP address
   */
  public TurtleRobot()
  {
    this(null, true);
  }

  private void init(boolean showPane)
  {
    EV3Properties props = LegoRobot.getProperties();
    stepFactor = props.getDoubleValue("TurtleStepFactor");
    rotationFactor = props.getDoubleValue("TurtleRotationFactor");
    turtleSpeed = props.getIntValue("TurtleSpeed");
    rotationSpeed = props.getIntValue("TurtleRotationSpeed");
    gear = new Gear();
    gear.setSpeed(turtleSpeed);
    addPart(gear);
    if (showPane)
      tp = new TurtlePane(this, 10, 10);
  }

  /**
   * Returns the gear used as component of the turtle.
   * @return the reference of the Gear instance
   */
  public Gear getGear()
  {
    return gear;
  }

  /**
   * Sets the turtle speed to the given value.
   * The TurtleRotationSpeed remains unchanged.
   * The speed will be changed to the new value at the next movement call only.
   * @param speed 0..100
   * @return the object reference to allow method chaining
   */
  public TurtleRobot setTurtleSpeed(int speed)
  {
    if (isReleased)
      return this;
    if (tp != null)
      tp.println(id + "setTurtleSpeed(" + speed + ")");
    gear.setSpeed(speed);
    return this;
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
   * Moves the turtle forward the given number of steps.
   * The methods blocks until the turtle is at the final position.
   * The actual distance depends on the "TurtleStepFactor"
   * as defined in the nxtlib.properties file.
   * The turtle speed corresponds to the "TurtleSpeed"
   * as defined in the nxtlib.properties file.
   * @param steps the number of steps to go.
   * @return the object reference to allow method chaining
   */
  public TurtleRobot forward(int steps)
  {
    if (isReleased)
      return this;
    if (tp != null)
      tp.println(id + "forward(" + steps + ")");
    gear.moveTo(Tools.round(stepFactor * steps));
    return this;
  }

  /**
   * Moves the turtle backward the given number of steps.
   * The methods blocks until the turtle is at the final position.
   * The actual distance depends on the "TurtleStepFactor"
   * as defined in the nxtlib.properties file.
   * The turtle speed corresponds to the "TurtleSpeed"
   * as defined in the nxtlib.properties file.
   * @param steps the number of steps to go.
   * @return the object reference to allow method chaining
   */
  public TurtleRobot backward(int steps)
  {
    if (isReleased)
      return this;
    if (tp != null)
      tp.println(id + "backward(" + steps + ")");
    gear.moveTo(-Tools.round(stepFactor * steps));
    return this;
  }

  /**
   * Turns the turtle to the left for the given angle.
   * The methods blocks until the turtle is at the final position.
   * Due to the construction of the NXT the accuracy is limited.
   * It depends on the "TurtleRotationFactor" and the "TurtleRotationSpeed"
   * as defined in the nxtlib.properties file.
   * @param angle the angle in degree to rotate.
   * @return the object reference to allow method chaining
   */
  public TurtleRobot left(int angle)
  {
    if (isReleased)
      return this;
    if (tp != null)
      tp.println(id + "left(" + angle + ")");
    int speed = gear.getSpeed();
    gear.setSpeed(rotationSpeed);
    gear.turnTo(-Tools.round(rotationFactor * angle));
    gear.setSpeed(speed);
    return this;
  }

  /**
   * Turns the turtle to the right for the given angle.
   * The methods blocks until the turtle is at the final position.
   * Due to the construction of the NXT the accuracy is limited.
   * It depends on the "TurtleRotationFactor" and the "TurtleRotationSpeed"
   * as defined in the nxtlib.properties file.
   * @param angle the angle in degree to rotate.
   * @return the object reference to allow method chaining
   */
  public TurtleRobot right(int angle)
  {
    if (isReleased)
      return this;
    if (tp != null)
      tp.println(id + "right(" + angle + ")");
    int speed = gear.getSpeed();
    gear.setSpeed(rotationSpeed);
    gear.turnTo(Tools.round(rotationFactor * angle));
    gear.setSpeed(speed);
    return this;
  }

  /**
   * Starts moving forward and returns immediately.
   * @return the object reference to allow method chaining
   */
  public TurtleRobot forward()
  {
    if (!isReleased)
      gear.forward();
    return this;
  }

  /**
   * Starts moving backward and returns immediately.
   * @return the object reference to allow method chaining
   */
  public TurtleRobot backward()
  {
    if (!isReleased)
      gear.backward();
    return this;
  }

  /**
   * Starts turning right and returns immediately.
   * @return the object reference to allow method chaining
   */
  public TurtleRobot right()
  {
    if (!isReleased)
      gear.right();
    return this;
  }

  /**
   * Starts turning left and returns immediately.
   * @return the object reference to allow method chaining
   */
  public TurtleRobot left()
  {
    if (!isReleased)
      gear.left();
    return this;
  }

  /**
   * Stop the motors.
   * @return the object reference to allow method chaining
   */
  public TurtleRobot stop()
  {
    if (!isReleased)
      gear.stop();
    return this;
  }

  /**
   * Closes the communication and terminates the program.
   */
  public void exit()
  {
    if (tp != null)
      tp.dispose();
    super.exit();
  }
}
