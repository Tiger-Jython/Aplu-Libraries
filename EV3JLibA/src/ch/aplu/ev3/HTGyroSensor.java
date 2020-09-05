// HTGyroSensor.java

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
 * Class that represents a gyro sensor from HiTechnic. The sensor reports
 * the angular velocity. To get the current rotation angle (heading)
 * you must use a separate thread to poll the sensor in a tight loop (of about 1 ms)
 * and integrate the reported values.
 * See also net.mosen.nxt.GyroSensor (author Kirk P. Thomson).
 */
public class HTGyroSensor extends Sensor
{
  private lejos.hardware.sensor.HiTechnicGyro gs;

  /**
   * Creates a sensor instance connected to the given port.
   * @param port the port where the sensor is plugged-in
   */
  public HTGyroSensor(SensorPort port)
  {
    super(port);
    gs = new lejos.hardware.sensor.HiTechnicGyro(getLejosPort());
  }

  /**
   * Creates a sensor instance connected to port S1.
   */
  public HTGyroSensor()
  {
    this(SensorPort.S1);
  }

  protected void init()
  {
    if (LegoRobot.getDebugLevel() >= SharedConstants.DEBUG_LEVEL_MEDIUM)
      DebugConsole.show("gs.init()");
  }

  protected void cleanup()
  {
    if (LegoRobot.getDebugLevel() >= SharedConstants.DEBUG_LEVEL_MEDIUM)
      DebugConsole.show("gs.cleanup()");
    gs.close();
 }

  /**
   * Returns the angular velocity.
   * @return the current value (in degrees per second, positive anti-clockwise)
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
   * Returns the reference of the the underlying lejos.hardware.sensor.HiTechnicGyro.
   * @return the reference of the lejos.hardware.sensor.HiTechnicGyro
   */
  public lejos.hardware.sensor.HiTechnicGyro getLejosSensor()
  {
    return gs;
  }

  private void checkConnect()
  {
    if (robot == null)
     new ShowError("HTGyroSensor is not a part of the LegoRobot.\n" +
        "Call addPart() to assemble it.");
  }
}
