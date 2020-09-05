// TemperatureSensor.java
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
 * Class that represents the Lego NXT temperature sensor (9749). The device
 * is built up with the TMP275 digital sensor from Burr-Brown (Texas Instruments).
 * As specified by the manufacturer the temperature range is -55 .. 128 degrees Celsius.
 * The accuracy is +-0.5 degrees in range -20 to 100 degrees and the
 * the resolution is 0.0625 degrees (in 12 bit mode used here). 
 */
public class TemperatureSensor extends Sensor
{
  /**
   * Creates a sensor instance connected to the given port.
   * @param port the port where the sensor is plugged-in
   */
  public TemperatureSensor(SensorPort port)
  {
    super(port);
    partName = "temp" + (port.getId() + 1);
  }

  /**
   * Creates a sensor instance connected to port S1.
   */
  public TemperatureSensor()
  {
    this(SensorPort.S1);
  }

  protected void init()
  {
    if (LegoRobot.getDebugLevel() >= SharedConstants.DEBUG_LEVEL_MEDIUM)
      DebugConsole.show("DEBUG: TemperatureSensor.init() called (Port: "
        + getPortLabel() + ")");
  }

  protected void cleanup()
  {
    if (LegoRobot.getDebugLevel() >= SharedConstants.DEBUG_LEVEL_MEDIUM)
      DebugConsole.show("DEBUG: TemperatureSensore.cleanup() called (Port: "
        + getPortLabel() + ")");
  }

  /**
   * Returns the temperature in degrees Celsius rounded to two decimals.
   * @return the current temperature value 
   */
  public double getTemperature()
  {
    checkConnect();
    int v = 0;
    try
    {
      v = Integer.parseInt(robot.sendCommand(partName + ".getTemperature"));
    }
    catch (NumberFormatException ex)
    {
    }
    return v / 100.0;
  }

  private void checkConnect()
  {
    if (robot == null)
      new ShowError("NxtTemperatureSensor (port: " + getPortLabel()
        + ") is not a part of the LegoRobot.\n"
        + "Call addPart() to assemble it.");
  }
}
