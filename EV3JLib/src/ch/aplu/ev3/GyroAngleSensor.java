// GyroAngleSensor.java
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
 * Class that represents a gyro sensor in angle mode (EV3 Gyro Sensor).
 */
public class GyroAngleSensor extends Sensor
{
  /**
   * Creates a sensor instance connected to the given port.
   * @param port the port where the sensor is plugged-in
   */
  public GyroAngleSensor(SensorPort port)
  {
    super(port);
    partName = "gas" + (port.getId() + 1);
  }

  /**
   * Creates a sensor instance connected to port S1.
   */
  public GyroAngleSensor()
  {
    this(SensorPort.S1);
  }

  protected void init()
  {
    if (LegoRobot.getDebugLevel() >= SharedConstants.DEBUG_LEVEL_MEDIUM)
      DebugConsole.show("DEBUG: GyroAngleSensor.init() called (Port: "
        + getPortLabel() + ")");
  }

  protected void cleanup()
  {
    if (LegoRobot.getDebugLevel() >= SharedConstants.DEBUG_LEVEL_MEDIUM)
      DebugConsole.show("DEBUG: GyroAngleSensor.cleanup() called (Port: "
        + getPortLabel() + ")");
  }

  /**
   * Returns the orientation of the sensor in repect to 
   * its start orientation. The start orientation fixed when the
   * sensor instance is created or after reset() is called.
   * @return the current orientation in degrees (positive anti-clockwise)
   */
  public int getValue()
  {
    checkConnect();
    int v = 0;
    try
    {
      v = Integer.parseInt(robot.sendCommand(partName + ".getValue"));
    }
    catch (NumberFormatException ex)
    {
    }
    return v;
  }
  
  /**
   * Resets the start orientation to the current orientation.
   */
  public void reset()
  {
    checkConnect();
    robot.sendCommand(partName + ".reset");
  }


  private void checkConnect()
  {
    if (robot == null)
      new ShowError("GyroAngleSensor (port: " + getPortLabel()
        + ") is not a part of the LegoRobot.\n"
        + "Call addPart() to assemble it.");
  }
}
