// HTAccelerometer.java
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
 * Class that represents a accelerometer sensor (HiTechnic Accelerometer).
 */
public class HTAccelerometer extends Sensor
{
  /**
   * Creates a sensor instance connected to the given port.
   * @param port the port where the sensor is plugged-in
   */
  public HTAccelerometer(SensorPort port)
  {
    super(port);
    partName = "htas" + (port.getId() + 1);
  }

  /**
   * Creates a sensor instance connected to port S1.
   */
  public HTAccelerometer()
  {
    this(SensorPort.S1);
  }

  protected void init()
  {
    if (LegoRobot.getDebugLevel() >= SharedConstants.DEBUG_LEVEL_MEDIUM)
      DebugConsole.show("DEBUG: HTAccelerometer.init() called (Port: "
        + getPortLabel() + ")");
  }

  protected void cleanup()
  {
    if (LegoRobot.getDebugLevel() >= SharedConstants.DEBUG_LEVEL_MEDIUM)
      DebugConsole.show("DEBUG: HTAccelerometer.cleanup() called (Port: "
        + getPortLabel() + ")");
  }

  /**
   * Returns the ax, ay, az acceleration in a Vector3D.
   * The range is -20..20 m/s^2 with about 0.05 m/s^2 resolution.
   * @return the current acceleration (values rounded to two decimals).
   */
  public Vector3D getValue()
  {
    checkConnect();
    String data = robot.sendCommand(partName + ".getValue");
    String[] acc = data.split(";");
    double[] v = new double[3];
    for (int i = 0; i < 3; i++)
    {
      try
      {
        v[i] = Double.parseDouble(acc[i]);
      }
      catch (NumberFormatException ex)
      {
        v[i] = 0;
      }
    }
    return new Vector3D(v[0], v[1], v[2]);
  }

  private void checkConnect()
  {
    if (robot == null)
      new ShowError("HTAccelerometer (port: " + getPortLabel()
        + ") is not a part of the LegoRobot.\n"
        + "Call addPart() to assemble it.");
  }
}
