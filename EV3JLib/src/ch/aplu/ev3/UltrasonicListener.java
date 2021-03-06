// UltrasonicListener.java
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
 * Class with declarations of callback methods for the ultrasonic sensor.
 */
public interface UltrasonicListener extends java.util.EventListener
{
   /**
    * Called when the distance exceeds the trigger level.
    * @param port the port where the sensor is plugged in
    * @param level the current distance level
    */
   public void far(SensorPort port, int level);

   /**
    * Called when the distance falls below the trigger level.
    * @param port the port where the sensor is plugged in
    * @param level the current distance level
    */
   public void near(SensorPort port, int level);
}
