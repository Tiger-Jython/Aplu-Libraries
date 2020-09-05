// GyroAngleSensor.java

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
 * Class that represents a gyro sensor in angle mode (EV3 Gyro Sensor).
 * The sensor is calibrated when turned on.<br>
 * So <b>do not move it until it is fully functional.</b>
 */
public class GyroAngleSensor extends Sensor
{
  private lejos.hardware.sensor.EV3GyroSensor gs;

  /**
   * Creates a sensor instance connected to the given port.
   * @param port the port where the sensor is plugged-in
   */
  public GyroAngleSensor(SensorPort port)
  {
    super(port);
    gs = new lejos.hardware.sensor.EV3GyroSensor(getLejosPort());
//    sp = gs.getAngleMode();
  }

  /**
   * Creates a sensor instance connected to port S1.
   */
  public GyroAngleSensor()
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
   * Returns the orientation of the sensor in repect to 
   * its start orientation. The start orientation fixed when the
   * sensor instance is created or after reset() is called.
   * @return the current orientation in degrees (positive anti-clockwise)
   */
  public int getValue()
  {
    checkConnect();
    sm = gs.getMode(1);  // Sensor mode angle
 //   LCD.drawString("Mode: " + sm.getName(), 0, 6);
    int sampleSize = 1;
    float samples[] = new float[sampleSize];
    sm.fetchSample(samples, 0);
    return (int)samples[0];
  }

  /**
   * Resets the start orientation to the current orientation.
   */
  public void reset()
  {
    checkConnect();
    gs.reset();
  }

  private void checkConnect()
  {
    if (robot == null)
      new ShowError("GyroAngleSensor (port: " + getPortLabel()
        + ") is not a part of the LegoRobot.\n"
        + "Call addPart() to assemble it.");
  }
}
