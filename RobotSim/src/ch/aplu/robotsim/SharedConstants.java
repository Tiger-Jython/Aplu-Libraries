// SharedConstants.java

/*
This software is part of the RobotSim library.
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
 * V1.00 - Aug 2010: - First official release, all basic features implemented
 * V1.01 - Aug 2010: - Modified rotation speed when motors turn with same speed
                     - Added: automatic call of _init()
                     - Modified NxtContext.usePlayground() to useObstacle()
                     - Added: NxtRobot.getVersion()
 * V1.02 - Sep 2010: - Added: class SoundSensor, class SoundListener
                     - Modified NxtContext.usePlayground() to useObstacle()
                     - Added: NxtRobot.getVersion()
 * V1.03 - Oct 2010  - Added: Gear.isMoving(), Motor.isMoving()
 * V1.04 - Oct 2010  - Added: class SoundAdapter
 * V1.05 - Oct 2010  - LightSensor, SoundSensor event callbacks in own thread
                     - NxtRobot: critical methods synchronized
 * V1.06 - Oct 2010  - Move window to upper left screen corner
                     - NxtRobot.exit() stops all movement now
 * V1.07 - Nov 2010  - Added LightSensor.addLightListener() with default level
                     - Added LightSensor.setTriggerLevel()
                     - Added SoundSensor.addLightListener() with default level
                     - Added SoundSensor.setTriggerLevel()
 * V1.08 - Oct 2011  - Modified: Nxt.getPartLocation() is public now
 * V1.09 - Oct 2011  - Modified: turn angular speed when two motors have same speed is
 *                     speed dependent now
 *                   - Added NxtRobot.reset() to set robot at start position/direction
 * V1.10 - Oct 2011  - Modified: Turn rotation step is 1 degree now (instead of 6)
 * V1.11 - Feb 2012  - Fixed: LightSensor bright(), dark() now return port correctly
 * V1.12 - Mar 2012  - Added: Button.isDown() for compatiblity with leJOS 0.91
 *                   - Fixed: Blocking methods wait with Thread.sleep(1) now
 *                   - Modified: polling methods calls Thread.sleep(1) now
 * V1.13 - Jul 2012  - Fixed: Part instances may now be created as instance variables
 *                     (ArrayOutOfBounds error fixed)
 * V1.14 - Jan 2013  - Modified: gearTurnAngle depends on speed now
 *                   - Modified: TurtleRobot.left(), right() now turn with small speed
 *                   - Fixed: _init() now also works with TurtleRobot
 *                   - Added: Support of ultrasonic sensor
 * V1.15 - Jan 2013  - Modified documentation
 * V1.16 - Feb 2013  - Modified: NxtRobot.removeTarget() to completely remove it
 * V1.17 - Apr 2013  - Modified: Patch needed for Ultrasonic sensor (from S.Moser)
 *                   - Added: Class Main and annotation NoMain
 * V1.18 - Oct 2013  - Added: NxtRobot.isRunning()
 * V1.19 - Oct 2013  - Added: Throw RuntimeException when error in DisposeOnClose 
 * V1.20 - Nov 2013  - Modified in class gear: all timed moving commands can
 *                     now be interrupted by calling other moving commands
 * V1.21 - Nov 2013  - Added: NxtContext.useTarget() with array parameters
 * V1.22 - Jan 2014  - Fixed: added lost NxtContext.showNavigationBar()
 * V1.23 - May 2014  - NxtRobot renamed to LegoRobot, NxtContext renamed to RobotContext
 *                     Dummy classes NxtRobot, NxtContext for compatibility with
 *                     pre-EV3 programs
 * V1.24 - Jul 2014  - Added: class PackageInfo
 *                   - Refactored package ch.aplu.nxtsim to ch.aplu.robotsim
 *                   - Added: Tools.startTimer(), getTime()
 * V1.25 - Jul 2014  - Added: color cubes in ColorSensor, class ColorLabel
 * V1.26 - Jul 2014  - Added: LegoRobot.drawString() writes to status bar
 * V1.27 - Aug 2014  - Added: in class LegoRobot key events to simulate brick buttons
 * V1.28 - Aug 2014  - Added: ButtonListener to support the event model
 * V1.29 - Aug 2014  - Added: MouseListener to suppert mouse events
 * V1.30 - Sep 2014  - Added: ColorSensor.getColorStr()
 * V1.31 - Nov 2014  - Removed chaining in all classes (to avoid output in Jython console)
 * V1.32 - Dec 2014  - Fixed: fail() terminates now in Jython mode
 * V1.33 - Jan 2015  - Modified: Tools.delay() now with HiResAlarmTimer
 * V1.34 - Oct 2015  - Modified: Fixed simulation period now, Tools.delay() based (bad idea)
                       on simulation period (good idea)->erratic movement solved
 * V1.35 - Oct 2015  - UltrasonicListerner now derived from EventListener
 * V1.36 - May 2016  - RobotContext checks if called after robot creation
 * V1.37 - Jun 2016  - Fixed: RobotContext check crash in second invocation
 * V1.38 - Jul 2019  - Added: Support for LED
 * V1.39 - Aug 2019  - Added: Imgages of rotating wheels
 * V1.40 - Aug 2019  - Fixed: Simulation with one motor only
 * V1.41 - Aug 2019  - Added: RobotContext.showTrace(), RobotContext.showRotCenter()
 */

package ch.aplu.robotsim;  

interface SharedConstants
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


  String ABOUT =
    "2003-2019 Aegidius Pluess\n" +
    "OpenSource Free Software\n" +
    "http://www.aplu.ch\n" +
    "All rights reserved";
  String VERSION = "1.41 - August 2019";
}
