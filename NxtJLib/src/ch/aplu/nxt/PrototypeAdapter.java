// PrototypeAdapter.java

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

/**
 * Class with empty callback methods for the prototype sensor.
 */
public class PrototypeAdapter implements PrototypeListener
{
  /**
   * Empty method called when at least one of the 5 analog inputs changed its value.
   * Override it to process the event.
   * @param port the port where the sensor is plugged in
   * @param values the value of the analog input channels: 0..1023 or -1 if unchanged
   */
  public void ainChanged(SensorPort port, int[] values)
  {
  }

  /**
   * Empty method called when at least one of the digital inputs changed its value.
   * Override it to process the event.
   * @param port the port where the sensor is plugged in
   * @param values the value of the 6 digital channels: 0, 1 or -1 if unchanged or not an input
   */
  public void dinChanged(SensorPort port, int[] values)
  {
  }
} 
