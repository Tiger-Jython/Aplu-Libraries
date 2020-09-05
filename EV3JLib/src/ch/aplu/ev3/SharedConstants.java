// ShareConstants.java
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

/* History:
  V1.00 - May 2014: - Ported from NxtJLib
  V1.01 - Jun 2014: - Adpated to new EV3DirectServer
  V1.02 - Jul 2014: - Added: class PackageInfo
  V1.03 - Jul 2014: - Fixed: LegoRobot.startReceiver() blocked until up and running
  V1.04 - Jul 2014: - Added: ColorSensor.getColorLabel() and ColorCubes
  V1.05 - Jul 2014: - Modified: ColorLabel enum in package root
  V1.06 - Jul 2014: - Fixed: Part.cleanup() now called when exiting
                    - Fixed: receiverResponse initialization moved 
  V1.07 - Jul 2014: - Added: EV3PyCopy.execute(String execScript)
  V1.08 - Aug 2014: - Modified: Revision of button actions (mapped to keyboard now)
  V1.09 - Aug 2014: - Fixed: Nullpointer error with stopButtonListener in exit()
  V1.10 - Aug 2014  - Added: ButtonListener to support the event model
                    - Added: LegoRobot.getIPAddresses(), isAutonomous()
                    - Modified: LegoRobot.isRunning() renamed to isConnected()
  V1.11 - Aug 2014  - Added: RemoteListener to support IRRemoteSensor events
  V1.12 - Aug 2014  - Added: ColorSensor.getColorStr()
  V1.13 - Jan 2015  - Added: ch.aplu.util and com.jcraft classes
  V1.14 - Jan 2015  - Modified: ColorSensor color cube values from properties now
  V1.15 - Feb 2015  - Added: GenericGear.getLeftMotorCount(), getRightMotorCount(),
                             resetLeftMotorCount(), resetRightMotorCount()
  V1.16 - Feb 2015  - Added: class HTEopdSensor, class HTEopdShortSensor
  V1.17 - May 2015  - Added: class ArduinoLink
                    - Fixed: Confusion with dots in string with command separator  
  V1.18 - May 2015  - Added: class TemperatureSensor
  V1.19 - May 2015  - Added: class I2CExpander
  V1.20 - Aug 2015  - Added: Support for RaspiBrick
  V1.21 - Aug 2015  - Fixed: Nullpointer when using LegoRobot ctor without connection pane
  V1.22 - Sep 2015  - Added: LegoRobot.disconnect()
  V1.23 - Nov 2015  - Modified: TargetCopy without timeout
  V1.24 - Mar 2016  - Modified: added killPython() to TargetCopy 
  V1.25 - Apr 2016  - Fixed: TargetCopy.execute now launches startApp() again
  V1.26 - Apr 2016  - Modified: TargetCopy does not suppress cleanup warning
  V1.27 - Jun 2016  - Added: TurtleRobot non-blocking movement methods
  V1.28 - Jun 2016  - Modified: TargetCopy download MyApp.py and then copied to filename
  V1.29 - Jun 2016  - Fixed: TargetCopy converting System.out to UTF-8
  V1.30 - Jun 2016  - Fixed: ColorSensor black color cube redefinition works now
  V1.31 - Mar 2017  - Added: ConnectPanel.setDefaultIPAddress()
  V1.32 - Mar 2017  - Added: LegoRobot.playSample()
*/
package ch.aplu.ev3;

interface SharedConstants
{
  int DEBUG_LEVEL_OFF = 0;    // Elementary debug info
  int DEBUG_LEVEL_LOW = 1;    // Debug info when threads start/stop
  int DEBUG_LEVEL_MEDIUM = 2; // Debug info for some method calls and their paramaters

  String ABOUT =
    "2003-2017 Aegidius Pluess\n" +
    "OpenSource Free Software\n" +
    "http://www.aplu.ch\n" +
    "All rights reserved";
  String VERSION = "1.32 - March 2017";
  String TITLE = "EV3JLib V" + VERSION + "   (www.aplu.ch)";
}
