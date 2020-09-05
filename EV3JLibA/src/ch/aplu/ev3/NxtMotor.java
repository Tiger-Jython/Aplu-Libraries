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
 * Class that represents a lejos.hardware.motor.NXTRegulatedMotor motor.
 */
public class NxtMotor extends GenericMotor
{
  /**
   * Creates a motor instance that is plugged into given port.
   * Default speed is set in ev3jlib.properties.
   * @param port the port where the motor is plugged-in (MotorPort.A, MotorPort.B, MotorPort.C)
   */
  public NxtMotor(MotorPort port)
  {
    super(port, true);
  }

  /**
   * Creates a motor instance that is plugged into port A.
   * Default speed is set in ev3jlib.properties.
  */
  public NxtMotor()
  {
    this(MotorPort.A);
  }
  
  /**
   * Returns the reference of the the underlying lejos.hardware.motor.NXTRegulatedNxtMotor.
   * @return the reference of the lejos.hardware.motor.NXTRegulatedNxtMotor
   */
  public lejos.hardware.motor.NXTRegulatedMotor getLejosMotor()
  {
    return (lejos.hardware.motor.NXTRegulatedMotor)_getMot();
  }
}
