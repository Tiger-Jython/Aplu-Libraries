// OpticalDistanceSensor.java
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
 * Class that represents a high precision infrared distance sensor 
 * (High Precision Short Range Infrared Distance Sensor from mindstorms.com).
 */
public class OpticalDistanceSensor extends Sensor
{
  /**
   * Creates a sensor instance connected to the given port.
   * @param port the port where the sensor is plugged-in
   */
  public OpticalDistanceSensor(SensorPort port)
  {
    super(port);
    partName = "ods" + (port.getId() + 1);
  }

  /**
   * Creates a sensor instance connected to port S1.
   */
  public OpticalDistanceSensor()
  {
    this(SensorPort.S1);
  }

  protected void init()
  {
    if (LegoRobot.getDebugLevel() >= SharedConstants.DEBUG_LEVEL_MEDIUM)
      DebugConsole.show("DEBUG: OpticalDistanceSensor.init() called (Port: "
        + getPortLabel() + ")");
  }

  protected void cleanup()
  {
    if (LegoRobot.getDebugLevel() >= SharedConstants.DEBUG_LEVEL_MEDIUM)
      DebugConsole.show("DEBUG: OpticalDistanceSensor.cleanup() called (Port: "
        + getPortLabel() + ")");
  }

  /**
   * Returns the distance from the object in millimeters.
   * @return distance in mm
   */
  public int getDistance()
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

  private void checkConnect()
  {
    if (robot == null)
      new ShowError("OpticalDistanceSensor (port: " + getPortLabel()
        + ") is not a part of the LegoRobot.\n"
        + "Call addPart() to assemble it.");
  }

}
