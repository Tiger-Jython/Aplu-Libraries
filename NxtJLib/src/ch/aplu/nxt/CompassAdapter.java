// CompassAdapter.java

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
 * Class with empty callback methods for the compass sensor.
 */
public class CompassAdapter implements CompassListener
{
   /**
    * Empty method called when the compass reading points to a direction at the right
    * half-plane of the trigger direction.
    * Override it to process the event.
    * @param port the port where the sensor is plugged in
    * @param degrees the current compass direction
    */
   public void toRight(SensorPort port, double degrees)
   {}

   /**
    * Empty method called when the compass reading points to a direction at the left
    * half-plane of the trigger direction.
    * Override it to process the event.
    * @param port the port where the sensor is plugged in
    * @param degrees the current compass direction
    */
   public void toLeft(SensorPort port, double degrees)
   {}
}
