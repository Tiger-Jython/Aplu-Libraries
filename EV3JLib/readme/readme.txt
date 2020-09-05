EV3JLib Direct Mode Java Library for Lego EV3 - Readme
Version 1.22, Sep 24, 2015
==================================================================

See the websites http://www.aplu.ch/ev3 for most recent information.

For compilation, import EV3JLib.jar as external library. 
If you use the EV3PyCopy class, you must also import the Jsch JAR library
from  http://www.jcraft.com/jsch


You need the most recent update of BrickGate running on the EV3 brick. 
(BrickGate is a gateway server for direct mode in any languages 
and autonomous Python mode.)

BrickGate.jar is part of this distribution.
You may also download it from http://www.brickgate.ch 
or install/update it with WebStart from http://www.brickgate.ch/update

BrickGate is ported from EV3DirectServer. The source is not yet put to the public domain.
The source of EV3DirectServer is still included in the distribution, but
keep in mind that it is no longer maintained and does not support all 
features of BrickGate.

History:
-------
V1.00 - Jul 2014: - First public release
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

Installation:
------------
   1. Unpack the EV3JLib.zip in any folder

   2. Copy the library jar EV3JLib.jar in subfolder 'lib' into your 
      favorite folder for jar files, e.g. c:\jars

   3. Create a project with your favorite IDE and add EV3JLib.jar
      to the project library jars

   4. Compile/run some of the examples in the subfolder 'examples',
      e.g. EV3Demo.java. Try to understand the code


Compilation of EV3DirectServer (EDS)
------------------------------------ 
For compilation of the EV3DirectServer, you need EV3DirectServer.java and 
the sources of EV3JLib in package ch.aplu.ev3 found in this distribution.
Moreover you need ev3classes.jar found in the leJOS EV3 distribution, 
but there is no need to include ev3classes.jar in EV3DirectServer.jar, 
because it is part of the classpath of the EV3 brick.


For any help or suggestions send an e-mail to support@aplu.ch or post an article
to the forum at www.aplu.ch/forum.
