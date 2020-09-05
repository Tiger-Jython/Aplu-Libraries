// NxtGear.java

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
 * Class that represents the combination of two motors on an axis 
 * to perform a car-like movement.
 */
public class NxtGear extends GenericGear
{
  

  /**
   * Creates a gear instance with right motor plugged into port1, left motor plugged into port2.
   * @param port1 MotorPort.A, MotorPort.B, MotorPort.C, MotorPort.D (not both the same)
   * @param port2 MotorPort.A. MotorPort.B, MotorPort.C, MotorPort.D (not both the same)
   */
  public NxtGear(MotorPort port1, MotorPort port2)
  {
    super(port1, port2, true);
  }

  /**
   * Creates a gear instance with left motor plugged into port A, right motor plugged into port B.
   */
  public NxtGear()
  {
    this(MotorPort.A, MotorPort.B);
  }
  
    /**
   * Returns right motor of the gear.
   * @return the reference of the right motor
   */
  public NxtMotor getMotRight()
  {
    return _getMot(mot1);
  }

  /**
   * Returns left motor of the gear.
   * @return the reference of the left motor
   */
  public NxtMotor getMotLeft()
  {
    return _getMot(mot2);
  }
}
