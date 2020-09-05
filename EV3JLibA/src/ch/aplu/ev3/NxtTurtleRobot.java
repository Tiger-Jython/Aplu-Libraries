// NxtTurtleRobot.java

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
 * Implementation of the basic Logo turtle movements. Motors should be
 * plugged into port A and B.
 */
public class NxtTurtleRobot extends LegoRobot
{
  protected NxtGear gear;
  protected double stepFactor;
  protected double rotationFactor;
  protected int turtleSpeed;
  protected int rotationSpeed;
 
  /**
   * Creates TurtleLegoRobot instance and delays execution until the user
   * presses the ENTER button.
   * @param waitStart if true, the execution is stopped until the ENTER button is hit.
   */
  public NxtTurtleRobot(boolean waitStart)
  {
    super(waitStart);
    gear = new NxtGear();
    addPart(gear);
    EV3Properties props = LegoRobot.getProperties();
    stepFactor = props.getDoubleValue("TurtleStepFactor");
    rotationFactor = props.getDoubleValue("TurtleRotationFactor");
    turtleSpeed = props.getIntValue("TurtleSpeed");
    rotationSpeed = props.getIntValue("TurtleRotationSpeed");
    gear.setSpeed(turtleSpeed);
  }

  /**
   * Creates a TurtleLegoRobot instance.
   */
  public NxtTurtleRobot()
  {
    this(false);
  }

  /**
   * Returns the gear used as component of the turtle.
   * @return the reference of the NxtGear instance
   */
  public NxtGear getGear()
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
  public NxtTurtleRobot setTurtleSpeed(int speed)
  {
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
   * as defined in the ev3jlib.properties file.
   * The turtle speed corresponds to the "TurtleSpeed"
   * as defined in ShareConstants interface.
   * @param steps the number of steps to go.
   * @return the object reference to allow method chaining
   */
  public NxtTurtleRobot forward(int steps)
  {
    gear.moveTo(Tools.round(stepFactor * steps));
    Tools.delay(100);
    return this;
  }

  /**
   * Moves the turtle backward the given number of steps.
   * The methods blocks until the turtle is at the final position.
   * The actual distance depends on the "TurtleStepFactor"
   * as defined in the ev3jlib.properties file.
   * The turtle speed corresponds to the "TurtleSpeed"
   * as defined in the ev3jlib.properties file.
   * @param steps the number of steps to go.
   * @return the object reference to allow method chaining
   */
  public NxtTurtleRobot backward(int steps)
  {
    gear.moveTo(-Tools.round(stepFactor * steps));
    Tools.delay(100);
    return this;
  }

  /**
   * Turns the turtle to the left for the given angle.
   * The methods blocks until the turtle is at the final position.
   * Due to the construction of the EV3 the accuracy is limited.
   * It depends on the "TurtleRotationFactor" and the "TurtleRotationSpeed"
   * as defined in the ev3jlib.properties file.
   * @param angle the angle in degree to rotate.
   * @return the object reference to allow method chaining
   */
  public NxtTurtleRobot left(int angle)
  {
    int speed = gear.getSpeed();
    gear.setSpeed(rotationSpeed);
    gear.turnTo(-Tools.round(rotationFactor * angle));
    gear.setSpeed(speed);
    Tools.delay(100);
    return this;
  }

  /**
   * Turns the turtle to the right for the given angle.
   * The methods blocks until the turtle is at the final position.
   * Due to the construction of the EV3 the accuracy is limited.
   * It depends on the "TurtleRotationFactor" and the "TurtleRotationSpeed"
   * as defined in the ev3jlib.properties file.
   * @param angle the angle in degree to rotate.
   * @return the object reference to allow method chaining
   */
  public NxtTurtleRobot right(int angle)
  {
    int speed = gear.getSpeed();
    gear.setSpeed(rotationSpeed);
    gear.turnTo(Tools.round(rotationFactor * angle));
    gear.setSpeed(speed);
    Tools.delay(100);
    return this;
  }
}