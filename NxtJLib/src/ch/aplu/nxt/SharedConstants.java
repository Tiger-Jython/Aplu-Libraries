// ShareConstants.java

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

/* History:
  V1.00 - Aug 2008:
    - First official release
  V1.01 - Aug 2008:
    - Added flag inCallback for all sensors to inhibit more than
      one callback at the same time
  V1.02 - Aug 2008:
    - Added sendFile(), startProgram(), findFirst(), findNext()
  V1.03 - Aug 2008:
    - Added parameter SensorPort in all sensor callback methods
  V1.04 - Oct 2009:
    - Source code included
    - JavaDoc revisted
    - Modified: Motor.continueTo() turns now to absolute position
    - Added: Motor.continueRelativeTo()
    - Modified: LightSensor init turns LED off
  V1.05 - Nov 2008: 
    - Added CompassSensor, CompassAdapter, CompassListener
    - Bug fixed in ch.aplu.util.AlarmTimer callback notification
  V1.06 - Jan 2009:
    - Added PrototypeSensor, PrototypeAdapter, PrototypeListener
    - All SensorAdapters now implement their listener
  V1.08 - Mar 2009:
    - Improved some classes in ch.aplu.util
  V1.09 - Feb 2010:
    - Added TurtleCrawler
    - Added Gear.isMoving()
  V1.10 - April 2010:
    - All listener interfaces are declared extends java.util.EventListener
      in order to be used as bean event properties (especially for Jython)
    - Modified to public: SensorPort.getId(), SensorPort.getLabel()
    - Added: addXXXListener with default trigger level, setTriggerLevel()
  V1.11 - Sep 2010:
    - Subsequent calls of motor and gear movement methods return immediately
      if motor or gear are already in the requested movement
    - Added dummy classes NxtContext, Obstacle with empty methods
      for source compatibility with NxtSim libary
  V1.12 - Oct 2010:
    - Connection pane will stay displayed until program terminates
  V1.13 - Oct 2010:
    - All polling methods call Thread.sleep(10) for better thread response
      in narrow loops
  V1.14 - Dec 2010: 
    - Update of package ch.aplu.util
  V1.15 - Jul 2011: 
    - Gear timed movement methods simplified using Thread.sleep() due to
      crash if many subsequent calls follow in a tight loop
  V1.16 - Oct 2011: 
    - Fixed null pointer exception when connection without showing the connect panel
    - NxtRobot.disconnect() now waits for cleanup and returns success flag
  V1.17 - Aug 2012: 
    - On error Sensor.LSRead() returns null now
    - Removed some public methods in class UltrasonicSensor
  V1.18 - Aug 2012: 
    - NxtRobot, TurtleRobot, TurtleCrawler ctors overloaded to take
      the Bluetooth address for fast connection
  V1.19 - Mar 2013:
    - Key ClosingMode in nxtjlib.properties, throws RuntimeException when
      using value ReleaseOnClose
  V1.20 - Apr 2013:
    - Removed automatic connection when part methods are called
    - Connect panel cannot be closed while connecting
  V1.21 - Apr 2013:
    - Hack to install special bluetooth driver for Mac 
      OSX 10.8, 10.9, 11.0
  V1.22 - May 2013:
    - Some minor adaptions to be used with Jython
  V1.23 - Oct 2013:
    - Added: ClosingMode: DisposeOnClose
  V1.24 - May 2014:
    - Modified: Motor.isMoving() implementation with flag and not
      sending command, because wrong report with Lego firmware
    - Renamed NxtContext to RobotContext and NxtRobot to LegoRobot with
      dummy classes for backward compatibility with pre-EV3 programs
   V1.25 - July 2014:
     - Fixed: GearState.STOPPED in Gear for all stopping methods
     - Added: class PackageInfo
   V1.26 - August 2014:
     - Added: LCD methods in class LegoRobot for compatibility with EV3JLib
   V1.27 - August 2014  
     - Added: in class LegoRobot key events to simulate brick buttons
   V1.28 - Aug 2014  
     - Added: ButtonListener to support the event model
   V1.29 - Jun 2016  
     - Added: TurtleRobot non-blocking movement methods
 */

package ch.aplu.nxt;

public interface SharedConstants
{
  int DEBUG_LEVEL_OFF = 0;    // Elementary debug info
  int DEBUG_LEVEL_LOW = 1;    // Debug info when threads start/stop
  int DEBUG_LEVEL_MEDIUM = 2; // Debug info for important method calls its paramaters
  int DEBUG_LEVEL_HIGH = 3;   // Debug info for sendData()/readData()

  String ABOUT =
    "2003-2016 Aegidius Pluess\n" +
    "OpenSource Free Software\n" +
    "http://www.aplu.ch\n" +
    "All rights reserved";
  String VERSION = "1.29 - Jun 2016";

  String TITLE = "NxtJLib V" + VERSION + "   (www.aplu.ch)";
  String TITLEMP = "NxtJLib V" + VERSION + "\n(www.aplu.ch)";

  byte DIRECT_COMMAND_REPLY = 0x00;
  byte SYSTEM_COMMAND_REPLY = 0x01;
  byte REPLY_COMMAND = 0x02;
  byte DIRECT_COMMAND_NOREPLY = (byte)0x80;
  byte SYSTEM_COMMAND_NOREPLY = (byte)0x81;
  byte OPEN_READ = (byte)0x80;
  byte OPEN_WRITE = (byte)0x81;
  byte READ = (byte)0x82;
  byte WRITE = (byte)0x83;
  byte CLOSE = (byte)0x84;
  byte DELETE = (byte)0x85;
  byte FIND_FIRST = (byte)0x86;
  byte FIND_NEXT = (byte)0x87;
  byte GET_FIRMWARE_VERSION = (byte)0x88;
  byte OPEN_WRITE_LINEAR = (byte)0x89;
  byte OPEN_READ_LINEAR = (byte)0x8A;
  byte OPEN_WRITE_DATA = (byte)0x8B;
  byte OPEN_APPEND_DATA = (byte)0x8C;
  byte BOOT = (byte)0x97;
  byte SET_BRICK_NAME = (byte)0x98;
  byte GET_DEVICE_INFO = (byte)0x9B;
  byte DELETE_USER_FLASH = (byte)0xA0;
  byte POLL_LENGTH = (byte)0xA1;
  byte POLL = (byte)0xA2;
  byte POLL_BUFFER = (byte)0x00;
  byte HIGH_SPEED_BUFFER = (byte)0x01;
  byte START_PROGRAM = 0x00;
  byte STOP_PROGRAM = 0x01;
  byte PLAY_SOUND_FILE = 0x02;
  byte PLAY_TONE = 0x03;
  byte SET_OUTPUT_STATE = 0x04;
  byte SET_INPUT_MODE = 0x05;
  byte GET_OUTPUT_STATE = 0x06;
  byte GET_INPUT_VALUES = 0x07;
  byte RESET_SCALED_INPUT_VALUE = 0x08;
  byte MESSAGE_WRITE = 0x09;
  byte RESET_MOTOR_POSITION = 0x0A;
  byte GET_BATTERY_LEVEL = 0x0B;
  byte STOP_SOUND_PLAYBACK = 0x0C;
  byte KEEP_ALIVE = 0x0D;
  byte LS_GET_STATUS = 0x0E;
  byte LS_WRITE = 0x0F;
  byte LS_READ = 0x10;
  byte GET_CURRENT_PROGRAM_NAME = 0x11;
  byte MESSAGE_READ = 0x13;
  byte MOTORON = 0x01;
  byte BRAKE = 0x02;
  byte REGULATED = 0x04;
  byte REGULATION_MODE_IDLE = 0x00;
  byte REGULATION_MODE_MOTOR_SPEED = 0x01;
  byte REGULATION_MODE_MOTOR_SYNC = 0x02;
  byte MOTOR_RUN_STATE_IDLE = 0x00;
  byte MOTOR_RUN_STATE_RAMPUP = 0x10;
  byte MOTOR_RUN_STATE_RUNNING = 0x20;
  byte MOTOR_RUN_STATE_RAMPDOWN = 0x40;
  byte NO_SENSOR = 0x00;
  byte SWITCH = 0x01;
  byte TEMPERATURE = 0x02;
  byte REFLECTION = 0x03;
  byte ANGLE = 0x04;
  byte LIGHT_ACTIVE = 0x05;
  byte LIGHT_INACTIVE = 0x06;
  byte SOUND_DB = 0x07;
  byte SOUND_DBA = 0x08;
  byte CUSTOM = 0x09;
  byte LOWSPEED = 0x0A;
  byte LOWSPEED_9V = 0x0B;
  byte NO_OF_SENSOR_TYPES = 0x0C;
  byte RAWMODE = 0x00;
  byte BOOLEANMODE = 0x20;
  byte TRANSITIONCNTMODE = 0x40;
  byte PERIODCOUNTERMODE = 0x60;
  byte PCTFULLSCALEMODE = (byte)0x80;
  byte CELSIUSMODE = (byte)0xA0;
  byte FAHRENHEITMODE = (byte)0xC0;
  byte ANGLESTEPSMODE = (byte)0xE0;
  byte SLOPEMASK = 0x1F;
  byte MODEMASK = (byte)0xE0;
	byte NXJ_FIND_FIRST = (byte)0xB6;
	byte NXJ_FIND_NEXT = (byte)0xB7;
}
