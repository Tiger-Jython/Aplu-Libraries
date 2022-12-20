// SHTSensor.java
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
 * Class that represents the Sensirion SHT31 sensor.
 */
public class SHTSensor extends Sensor
{
  /**
   * Creates a sensor instance with default I2C address connected to the given port. 
   * @param port the port where the sensor is plugged-in
   */
  public SHTSensor(SensorPort port)
  {
    super(port);
    partName = "sht" + (port.getId() + 1);
  }

  /**
   * Creates a sensor instance connected to port S1.
   */
  public SHTSensor()
  {
    this(SensorPort.S1);
  }

  protected void init()
  {
    if (LegoRobot.getDebugLevel() >= SharedConstants.DEBUG_LEVEL_MEDIUM)
      DebugConsole.show("DEBUG: SHTSensor.init() called (Port: "
        + getPortLabel() + ")");
  }

  protected void cleanup()
  {
    if (LegoRobot.getDebugLevel() >= SharedConstants.DEBUG_LEVEL_MEDIUM)
      DebugConsole.show("DEBUG: SHTSensor.cleanup() called (Port: "
        + getPortLabel() + ")");
  }

  /**
   * Performs a measurement and returns current temperature and humididy
   * (rounded to two decimals).
   * @return a double array of size 2 with temperature (in degrees centigrades, 
   * humidity (in percents)
   */
  public double[] getValues()
  {
    checkConnect();
    double[] values = new double[]
    {
      0, 0
    };
    try
    {
      String v = robot.sendCommand(partName + ".readSHT");
      String[] valuesStr = v.split(":");
      for (int i = 0; i < 2; i++)
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
      new ShowError("SHTSensor (port: " + getPortLabel()
        + ") is not a part of the LegoRobot.\n"
        + "Call addPart() to assemble it.");
  }
}
