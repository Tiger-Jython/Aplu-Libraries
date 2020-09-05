// HTCompassSensor.java
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
 * Class that represents a compass sensor (HiTechnic Compass Sensor).
 */
public class HTCompassSensor extends Sensor
{
  private boolean isCalibrating = false;
  
  /**
   * Creates a sensor instance connected to the given port.
   * @param port the port where the sensor is plugged-in
   */
  public HTCompassSensor(SensorPort port)
  {
    super(port);
    partName = "htcp" + (port.getId() + 1);
  }

  /**
   * Creates a sensor instance connected to port S1.
   */
  public HTCompassSensor()
  {
    this(SensorPort.S1);
  }

  protected void init()
  {
    if (LegoRobot.getDebugLevel() >= SharedConstants.DEBUG_LEVEL_MEDIUM)
      DebugConsole.show("DEBUG: HTBarometer.init() called (Port: "
        + getPortLabel() + ")");
  }

  protected void cleanup()
  {
    if (LegoRobot.getDebugLevel() >= SharedConstants.DEBUG_LEVEL_MEDIUM)
      DebugConsole.show("DEBUG: HTBarometer.cleanup() called (Port: "
        + getPortLabel() + ")");
  }

   /**
   * Returns the compass direction.
   * @return the compass direction (0..<360 degrees, 0 to north, 90 to west)
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
   * Starts calibration process.
   * Needs at least 2 full rotations with at least 20 s per rotation
   * before stopCalibration() is called.
   * @see #stopCalibration()
   */
  public void startCalibration()
  {
    checkConnect();
    if (isCalibrating)
      return;
    isCalibrating = true;
    robot.sendCommand(partName + ".startCalibration");
  }
  
  /**
   * Stops calibration process.
   * @see #startCalibration()
   */
  public void stopCalibration()
  {
    checkConnect();
    if (!isCalibrating)
      return;
    isCalibrating = false;
    robot.sendCommand(partName + ".stopCalibration");
  }

  private void checkConnect()
  {
    if (robot == null)
      new ShowError("HTBarometer (port: " + getPortLabel()
        + ") is not a part of the LegoRobot.\n"
        + "Call addPart() to assemble it.");
  }
 }
