// HTEopdShortSensor.java
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
 * Class that represents an Electro-Optical Proximity Detector  (HiTechnic EDPD).
 * The sensor measures distance uniformly colored objects in range of about 15 cm.
 * It is much less effected by ambient light than a standard color or 
 * light sensor.
 */
public class HTEopdShortSensor extends Sensor
{
  /**
   * Creates a sensor instance connected to the given port.
   * @param port the port where the sensor is plugged-in
   */
  public HTEopdShortSensor(SensorPort port)
  {
    super(port);
    partName = "htes" + (port.getId() + 1);
  }

  /**
   * Creates a sensor instance connected to port S1.
   */
  public HTEopdShortSensor()
  {
    this(SensorPort.S1);
  }

  protected void init()
  {
    if (LegoRobot.getDebugLevel() >= SharedConstants.DEBUG_LEVEL_MEDIUM)
      DebugConsole.show("DEBUG: HTEopdShortSensor.init() called (Port: "
        + getPortLabel() + ")");
  }

  protected void cleanup()
  {
    if (LegoRobot.getDebugLevel() >= SharedConstants.DEBUG_LEVEL_MEDIUM)
      DebugConsole.show("DEBUG: HTEopdShortSensor.cleanup() called (Port: "
        + getPortLabel() + ")");
  }

  /**
   * Returns the distance.
   * @return the current value the sensor reported in arbitrary units 
   * from 0..999 (999 if the sensor does not detect a target) 
   */
  public int getDistance()
  {
    checkConnect();
    int v = 0;
    try
    {
      v = Integer.parseInt(robot.sendCommand(partName + ".getDistance"));
    }
    catch (NumberFormatException ex)
    {
    }
    return v;

  }

  private void checkConnect()
  {
    if (robot == null)
      new ShowError("HTEopdShortSensor (port: " + getPortLabel()
        + ") is not a part of the LegoRobot.\n"
        + "Call addPart() to assemble it.");
  }
}
