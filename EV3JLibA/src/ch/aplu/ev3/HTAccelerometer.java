// HTAccelerometer.java

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
  private lejos.hardware.sensor.HiTechnicAccelerometer am;

  /**
   * Creates a sensor instance connected to the given port.
   * @param port the port where the sensor is plugged-in
   */
  public HTAccelerometer(SensorPort port)
  {
    super(port);
    am = new lejos.hardware.sensor.HiTechnicAccelerometer(getLejosPort());
    sm = am.getAccelerationMode();
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
      DebugConsole.show("cs.init()");
  }

  protected void cleanup()
  {
    if (LegoRobot.getDebugLevel() >= SharedConstants.DEBUG_LEVEL_MEDIUM)
      DebugConsole.show("cs.cleanup()");
    am.close();
  }

  /**
   * Returns the reference of the the underlying 
   * lejos.hardware.sensor.HiTechnicAccelerometer.
   * @return the reference of the lejos.hardware.sensor.HiTechnicAccelerometerr
   */
  public lejos.hardware.sensor.HiTechnicAccelerometer getLejosSensor()
  {
    return am;
  }

  /**
   * Returns the ax, ay, az acceleration in a Vector3D.
   * The range is -20..20 m/s^2 with about 0.05 m/s^2 resolution.
   * @return the current acceleration (values rounded to two decimals).
   */
  public Vector3D getValue()
  {
    checkConnect();
    int sampleSize = 3;
    float samples[] = new float[sampleSize];
    sm.fetchSample(samples, 0);
    double x = Math.round(samples[0] * 100) / 100.0;
    double y = Math.round(samples[1] * 100) / 100.0;
    double z = Math.round(samples[2] * 100) / 100.0;
    return new Vector3D(x, y, z);
  }
 
  private void checkConnect()
  {
    if (robot == null)
      new ShowError("ColorSensor (port: " + getPortLabel()
        + ") is not a part of the LegoRobot.\n"
        + "Call addPart() to assemble it.");
  }
}
