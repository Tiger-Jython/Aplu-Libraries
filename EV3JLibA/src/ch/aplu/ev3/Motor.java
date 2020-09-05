// Motor.java

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
 * Class that represents a EV3LargeRegulatedMotor motor.
 */
public class Motor extends GenericMotor
{
  /**
   * Creates a motor instance that is plugged into given port.
   * Default speed is set in ev3jlib.properties.
   * @param port the port where the motor is plugged-in (MotorPort.A, MotorPort.B, MotorPort.C)
   */
  public Motor(MotorPort port)
  {
    super(port, false);
  }

  /**
   * Creates a motor instance that is plugged into port A.
   * Default speed is set in ev3jlib.properties.
  */
  public Motor()
  {
    this(MotorPort.A);
  }
  
  /**
   * Returns the reference of the the underlying lejos.hardware.motor.EV3LargeRegulatedMotor.
   * @return the reference of the lejos.hardware.motor.EV3LargeRegulatedMotor
   */
  public lejos.hardware.motor.EV3LargeRegulatedMotor getLejosMotor()
  {
    return (lejos.hardware.motor.EV3LargeRegulatedMotor)getMot();
  }
}
