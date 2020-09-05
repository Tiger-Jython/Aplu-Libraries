// Gear.java
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
 * Class that represents the combination of two motors on an axis 
 * to perform a car-like movement.
 */
public class Gear extends GenericGear
{
  /** 
   * Creates a gear instance with left motor plugged into port A, 
   * right motor plugged into port B.
   */
  public Gear()
  {
    super(false);
  }

}
