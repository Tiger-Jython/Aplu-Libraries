// RFIDSensor.java
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
 * Class that represents a RFID sensor from CODATEX (www.codatex.com).
 */
public class RFIDSensor extends Sensor
{
  /**
   * Creates a sensor instance connected to the given port.
   * @param port the port where the sensor is plugged-in
   */
  public RFIDSensor(SensorPort port)
  {
    super(port);
    partName = "rfid" + (port.getId() + 1);
  }

  /**
   * Creates a sensor instance connected to port S1.
   */
  public RFIDSensor()
  {
    this(SensorPort.S1);
  }

  protected void init()
  {
    if (LegoRobot.getDebugLevel() >= SharedConstants.DEBUG_LEVEL_MEDIUM)
      DebugConsole.show("DEBUG: RFIDSensor.init() called (Port: "
        + getPortLabel() + ")");
  }

  protected void cleanup()
  {
    if (LegoRobot.getDebugLevel() >= SharedConstants.DEBUG_LEVEL_MEDIUM)
      DebugConsole.show("DEBUG: RFIDSensor.cleanup() called (Port: "
        + getPortLabel() + ")");
  }

  /**
   * Returns the product identifier (if available).
   * @return the product identifier (PID)
   */
  public String getProductID()
  {
    checkConnect();
    return robot.sendCommand(partName + ".getProductID");
  }

  /**
   * Returns the sensor version number (if available).
   * @return the sensor version number
   */
  public String getVersion()
  {
    checkConnect();
    return robot.sendCommand(partName + ".getVersion");
  }

  /**
   * Selects between Single Read and Continous Read mode.
   * @param continuous if true, the sensor is put in Continous Read mode; otherwise
   * it is put in Single Read mode.
   */
  public void setContMode(boolean continuous)
  {
    checkConnect();
    robot.sendCommand(partName + ".setContMode." + (continuous ? "b1" : "b0"));
  }

  /**
   * Polls the sensor. If the sensor is in Single Read mode and in sleep
   * state, it is automatically woke up.
   * @return the current transponder id (0: no transponder detected)
   */
  public long getTransponderId()
  {
    checkConnect();
    long v = 0;
    try
    {
      v = Long.parseLong(robot.sendCommand(partName + ".getTransponderId"));
    }
    catch (NumberFormatException ex)
    {
    }
    return v;
  }

  private void checkConnect()
  {
    if (robot == null)
      new ShowError("RFIDSensor (port: " + getPortLabel()
        + ") is not a part of the LegoRobot.\n"
        + "Call addPart() to assemble it.");
  }
}
