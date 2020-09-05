// SharedConstants.java

/*
This software is part of the RaspiSim library.
It is Open Source Free Software, so you may
- run the code for any purpose
- study how the code works and adapt it to your needs
- integrate all or parts of the code in your own programs
- redistribute copies of the code
- improve the code and release your improvements to the public
However the use of the code is entirely your responsibility.

Author: Aegidius Pluess, www.aplu.ch
*/

/* History:
 * V1.00 - Oct 2015: - Ported from RobotSim
 * V1.01 - Oct 2015: - Modified: Tools.delay() redefined using simulation period
 * V1.02 - Oct 2015: - Modified: IRSensor callbacks renamed
 * V1.03 - Oct 2015: - Modified: Ultrasonic callbacks with double parameter
                     - Fixed: UltrasonicListerner now derived from EventListener
 * V1.04 - Oct 2015: - Added: Led.setColor(), setColorAll(), ultrasonic mesh colors
                       for X11 color strings
 * V1.05 - Oct 2015: - Fixed: IR line sensors exchanged
 * V1.06 - Apr 2016: - Added: Led.startBlinking(), stopBlinking()
 */

package ch.aplu.raspisim;  

public interface SharedConstants
{
  boolean debug = false;  // Show debug infos
  
  int simulationPeriod = 30;
  double nbSteps = 0.03;  // Number of pixels advances per simulation period / per speed
  // e.g. for speed 50: 1.5 pixels per period
  double motTurnAngle = 0.08;  // Angle per simulation period (in degrees) / per speed when motors have same speed 
  double gearTurnAngle = 0.1; // Angle per simulation period (in degrees) / per speed
  // e.g. for speed 5: 5 degrees per period
  double motorRotIncFactor = 0.6;  // Factor that determines motor rotation speed
  double gearRotIncFactor = 0.8;   // Factor that determines gear rotation speed
  int pixelPerMeter = 200; // Distance corresponding to 1 meter
  int defaultSpeed = 50;  // Default gear and motor speed
  int ultrasonicBeamWidth = 20; // Beam width (opening angle) of ultrasonic sensor (degrees)
  int[] irSensorCollisionRadius = {5, 5, 5};  // collision area of upper ir sensors
  int BUTTON_PRESSED = 1;
  int BUTTON_RELEASED = 2;
  int BUTTON_LONGPRESSED = 3;

  String ABOUT =
    "2003-2016 Aegidius Pluess\n" +
    "OpenSource Free Software\n" +
    "http://www.aplu.ch\n" +
    "All rights reserved";
  String VERSION = "1.06 - Apr 2015";
}
