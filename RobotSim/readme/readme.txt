RobotSim - A simulation package for the Lego NXT/EV3 robot based on JGameGrid
Version 1.34, Oct 9, 2015
=======================================================================

RobotSim (former NxtSim) follows closely the NxtJLib/EV3JLibb and 
NxtJLibA/EV3JLibA package for the real robot. 
Code is easily ported from the simulation to the real robot and
vice-versa. See http://www.aplu.ch/home/nxt for more information

History:
-------
V1.00 - Mar 2011: - First official release, all basic features implemented
V1.01 - May 2012: - Added: Ultrasonic sensor
V1.02 - Jul 2013: - Added: Color sensor, rear touch and light sensor (port 4)
V1.03 - Oct 2010  - Added: Gear.isMoving(), Motor.isMoving()
V1.04 - Oct 2010  - Added: class SoundAdapter
V1.05 - Oct 2010  - LightSensor, SoundSensor event callbacks in own thread
                  - NxtRobot: critical methods synchronized
V1.06 - Oct 2010  - Move window to upper left screen corner
                  - NxtRobot.exit() stops all movement now
V1.07 - Nov 2010  - Added LightSensor.addLightListener() with default level
                  - Added LightSensor.setTriggerLevel()
                  - Added SoundSensor.addLightListener() with default level
                  - Added SoundSensor.setTriggerLevel()
V1.08 - Oct 2011  - Modified: Nxt.getPartLocation() is public now
V1.09 - Oct 2011  - Modified: turn angular speed when two motors have same speed is
                    speed dependent now
                  - Added NxtRobot.reset() to set robot at start position/direction
V1.10 - Oct 2011  - Modified: Turn rotation step is 1 degree now (instead of 6)
V1.11 - Feb 2012  - Fixed: LightSensor bright(), dark() now return port correctly
V1.12 - Mar 2012  - Added: Button.isDown() for compatiblity with leJOS 0.91
                  - Fixed: Blocking methods wait with Thread.sleep(1) now
                  - Modified: polling methods calls Thread.sleep(1) now
V1.13 - Jul 2012  - Fixed: Part instances may now be created as instance variables
                    (ArrayOutOfBounds error fixed)
V1.14 - Jan 2013  - Modified: gearTurnAngle depends on speed now
                  - Modified: TurtleRobot.left(), right() now turn with small speed
                  - Fixed: _init() now also works with TurtleRobot
                  - Added: Support of ultrasonic sensor
V1.15 - Jan 2013  - Modified documentation
V1.16 - Feb 2013  - Modified: NxtRobot.removeTarget() to completely remove it
V1.17 - Apr 2013  - Modified: Patch needed for Ultrasonic sensor (from S.Moser)
                  - Added: Class Main and annotation NoMain
V1.18 - Oct 2013  - Added: NxtRobot.isRunning()
V1.19 - Oct 2013  - Added: Throw RuntimeException when error in DisposeOnClose 
V1.20 - Nov 2013  - Modified in class gear: all timed moving commands can
                    now be interrupted by calling other moving commands
V1.21 - Nov 2013  - Added: NxtContext.useTarget() with array parameters
V1.22 - Jan 2014  - Fixed: added lost NxtContext.showNavigationBar()
V1.23 - May 2014  - NxtRobot renamed to LegoRobot, NxtContext renamed to RobotContext
                    Dummy classes NxtRobot, NxtContext for compatibility with
                    pre-EV3 programs
V1.24 - Jul 2014  - Added: class PackageInfo
                  - Refactored package ch.aplu.nxtsim to ch.aplu.robotsim
                  - Added: Tools.startTimer(), getTime()
V1.25 - Jul 2014  - Added: color cubes in ColorSensor, class ColorLabel
V1.26 - Jul 2014  - Added: LegoRobot.drawString() writes to status bar
V1.27 - Aug 2014  - Added: in class LegoRobot key events to simulate brick buttons
V1.28 - Aug 2014  - Added: ButtonListener to support the event model
V1.29 - Aug 2014  - Added: MouseListener to suppert mouse events
V1.30 - Sep 2014  - Added: ColorSensor.getColorStr()
V1.31 - Nov 2014  - Removed chaining in all classes (to avoid output in Jython console)
V1.32 - Dec 2014  - Fixed: fail() terminates now in Jython mode
V1.33 - Jan 2015  - Modified: Tools.delay() now with HiResAlarmTimer (bad idea)
V1.34 - Oct 2015  - Modified: Fixed simulation period now, Tools.delay() based
                       on simulation period (good idea)->erratic movement solved
 

Installation
------------

1. Download the latest version of the JGameGrid framework.
   Unpack the ZIP archive in any folder. 

2. Within your favorite IDE add JGameGrid.jar and RobotSim.jar to the 
   external libraries of your Java project. 

3. Copy the sprite images from the examples/src/sprites subdirectory to 
   a sprites subfolder of your project source.

4. Try to compile and run some of the examples.

5. Consult the JavaDoc by opening index.html in the doc subdirectory. 

For any help or suggestions send an e-mail to support@aplu.ch or post an article
to the forum at http://www.aplu.ch/forum.

Enjoy!
