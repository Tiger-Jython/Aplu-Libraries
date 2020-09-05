// Motor.java
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
 * Class that represents one of the EV3 motors.
 * Most methods will call connect, if not yet connected.
 *
 */
public class Motor extends GenericMotor
{
  /**
   * Creates a motor instance that is plugged into given port.
   * Default speed set in ev3jlib.properties.
   * @param port the port where the motor is plugged-in (MotorPort.A, MotorPort.B, MotorPort.C)
   */
  public Motor(MotorPort port)
  {
    super(port, MotorType.EV3Large);
  }

  /**
   * Creates a motor instance that is plugged into port A.
   * Default speed set in ev3jlib.properties.
   */
  public Motor()
  {
    super(MotorType.EV3Large);
  }
}
