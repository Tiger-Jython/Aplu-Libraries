// TurtleRobot.java

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
import ch.aplu.util.QuitPane;

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
  private TurtlePane tp;

  /**
   * Creates a turtle robot instance using a NXT brick with given Bluetooth name.
   * A connection trial is engaged while an information box is shown.
   * If the connection fails, the calling thread will be put in a wait state and the program hangs.
   * The user can then terminate the application by closing the information pane.
   * established at this time.<br><br>
   * If showConsole = true, the turtle commands are displayed in a console window.
   * If btName = null, a input dialog is displayed where the Bluetooth name can be entered.
   * @param btName the Bluetooth friendly name, e.g. NXT
   * @param showPane if true, an information pane is showed, where the turtle commands are listed
   */
  public TurtleRobot(String btName, boolean showPane)
  {
    super(btName);
    init(showPane);
  }

  /**
   * Same as TurtleRobot(btName, true)
   * @param btName the Bluetooth friendly name, e.g. NXT
   */
  public TurtleRobot(String btName)
  {
    this(btName, true);
  }

  /**
   * Same as TurtleRobot(btName), but ask for Bluetooth name
   */
  public TurtleRobot()
  {
    this(null, true);
  }

  /**
   * Same as TurtleRobot(btName, showPane), but Bluetooth address given.
   * @param address the 48-bit Bluetooth address as long
   * @param showPane if true, an information pane is showed, where the turtle commands are listed
   */
  public TurtleRobot(long address, boolean showPane)
  {
    super(address);
    init(showPane);
  }

  /**
   * Same as TurtleRobot(address, true)
   * @param address the 48-bit Bluetooth address as long
   */
  public TurtleRobot(long address)
  {
    this(address, true);
  }

  private void init(boolean showPane)
  {
    id = getBtName() + ": ";
    NxtProperties props = LegoRobot.getProperties();
    stepFactor = props.getDoubleValue("TurtleStepFactor");
    rotationFactor = props.getDoubleValue("TurtleRotationFactor");
    turtleSpeed = props.getIntValue("TurtleSpeed");
    rotationSpeed = props.getIntValue("TurtleRotationSpeed");
    gear = new Gear(MotorPort.A, MotorPort.B);
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
   * Closes the Bluetooth communication and terminates the program.
   */
  public void exit()
  {
    if (isReleased)
      return;
    System.out.println("Disconnecting now...");
    disconnect();
    switch (myClosingMode)
    {
      case TerminateOnClose:
        PlatformTools.exit();
        break;
      case ReleaseOnClose:
      case DisposeOnClose:
        tp.dispose();
        ConnectPanel.dispose();
        PlatformTools.wakeUp();
        QuitPane.dispose();
        isReleased = true;
        if (Tools.t != null)
          Tools.t.interrupt();  // taking out of Tools.delay()
        break;
    }
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

}
