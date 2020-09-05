// GyroRateSensor.java

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
 * Class that represents a gyro sensor in rate mode (EV3 Gyro Sensor).
 * The sensor is calibrated when turned on.<br>
 * So <b>do not move it until it is fully functional.</b>
 */
public class GyroRateSensor extends Sensor
{
  private lejos.hardware.sensor.EV3GyroSensor gs;

  /**
   * Creates a sensor instance connected to the given port.
   * @param port the port where the sensor is plugged-in
   */
  public GyroRateSensor(SensorPort port)
  {
    super(port);
    gs = new lejos.hardware.sensor.EV3GyroSensor(getLejosPort());
  }

  /**
   * Creates a sensor instance connected to port S1.
   */
  public GyroRateSensor()
  {
    this(SensorPort.S1);
  }

  protected void init()
  {
    if (LegoRobot.getDebugLevel() >= SharedConstants.DEBUG_LEVEL_MEDIUM)
      DebugConsole.show("gs.init()");
    gs.reset();
  }

  protected void cleanup()
  {
    if (LegoRobot.getDebugLevel() >= SharedConstants.DEBUG_LEVEL_MEDIUM)
      DebugConsole.show("gs.cleanup()");
    gs.close();
  }

  /**
   * Returns the reference of the the underlying 
   * lejos.hardware.sensor.EV3GyroSensor.
   * @return the reference of the lejos.hardware.sensor.EV3GyroSensor
   */
  public lejos.hardware.sensor.EV3GyroSensor getLejosSensor()
  {
    return gs;
  }

  /**
   * Returns the angular velocity.
   * @return the current value (in degrees per second, positive anti-clockwise)
   */
  public int getValue()
  {
    checkConnect();
    sm = gs.getMode(0);  // Sensor mode rate
 //   LCD.drawString("Mode: " + sm.getName(), 0, 6);
    int sampleSize = 1;
    float samples[] = new float[sampleSize];
    sm.fetchSample(samples, 0);
    return (int)samples[0];
  }

  private void checkConnect()
  {
    if (robot == null)
      new ShowError("GyroAngleSensor (port: " + getPortLabel()
        + ") is not a part of the LegoRobot.\n"
        + "Call addPart() to assemble it.");
  }
}
