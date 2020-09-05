// HTCompassSensor.java

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
  private lejos.hardware.sensor.HiTechnicCompass htcp;
  private boolean isCalibrating = false;

  /**
   * Creates a sensor instance connected to the given port.
   * @param port the port where the sensor is plugged-in
   */
  public HTCompassSensor(SensorPort port)
  {
    super(port);
    htcp = new lejos.hardware.sensor.HiTechnicCompass(getLejosPort());
    sm = htcp.getCompassMode();
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
      DebugConsole.show("htcp.init()");
  }

  protected void cleanup()
  {
    if (LegoRobot.getDebugLevel() >= SharedConstants.DEBUG_LEVEL_MEDIUM)
      DebugConsole.show("htcp.cleanup()");
    htcp.close();
  }

  /**
   * Returns the reference of the the underlying 
   * lejos.hardware.sensor.HiTechnicCompass.
   * @return the reference of the lejos.hardware.sensor.HiTechnicCompass
   */
  public lejos.hardware.sensor.HiTechnicCompass getLejosSensor()
  {
    return htcp;
  }

  /**
   * Returns the compass direction.
   * @return the compass direction (0..<360 degrees, 0 to north, 90 to west)
   */
  public int getValue()
  {
    checkConnect();
    int sampleSize = 1;
    float samples[] = new float[sampleSize];
    sm.fetchSample(samples, 0);
    return (int)samples[0];
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
    htcp.startCalibration();
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
    htcp.stopCalibration();
  }


  private void checkConnect()
  {
    if (robot == null)
      new ShowError("HiTechnicCompass (port: " + getPortLabel()
        + ") is not a part of the LegoRobot.\n"
        + "Call addPart() to assemble it.");
  }
}
