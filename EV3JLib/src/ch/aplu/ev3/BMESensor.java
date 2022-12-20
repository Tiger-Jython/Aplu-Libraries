// BMESensor.java
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
 * Class that represents the Bosch BME280 environment sensor.
 */
public class BMESensor extends Sensor
{
  /**
   * Creates a sensor instance with default I2C address connected to the given port. 
   * @param port the port where the sensor is plugged-in
   */
  public BMESensor(SensorPort port)
  {
    super(port);
    partName = "bme" + (port.getId() + 1);
  }

  /**
   * Creates a sensor instance connected to port S1.
   */
  public BMESensor()
  {
    this(SensorPort.S1);
  }

  protected void init()
  {
    if (LegoRobot.getDebugLevel() >= SharedConstants.DEBUG_LEVEL_MEDIUM)
      DebugConsole.show("DEBUG: BMESensor.init() called (Port: "
        + getPortLabel() + ")");
  }

  protected void cleanup()
  {
    if (LegoRobot.getDebugLevel() >= SharedConstants.DEBUG_LEVEL_MEDIUM)
      DebugConsole.show("DEBUG: BMESensor.cleanup() called (Port: "
        + getPortLabel() + ")");
  }

  /**
   * Performs a measurement and returns current temperature, humididy and 
   * air pressure (rounded to two decimals).
   * @return a double array of size 3 with temperature (in degrees centigrades, 
   * humidity (in percents) and air pressure (in hPa)
   */
  public double[] getValues()
  {
    checkConnect();
    double[] values = new double[]
    {
      0, 0, 0
    };
    try
    {
      String v = robot.sendCommand(partName + ".readBME");
      String[] valuesStr = v.split(":");
      for (int i = 0; i < 3; i++)
        values[i] = Double.parseDouble(valuesStr[i]);
    }
    catch (NumberFormatException ex)
    {
    }
    return values;

  }

  private void checkConnect()
  {
    if (robot == null)
      new ShowError("BMESensor (port: " + getPortLabel()
        + ") is not a part of the LegoRobot.\n"
        + "Call addPart() to assemble it.");
  }
}
