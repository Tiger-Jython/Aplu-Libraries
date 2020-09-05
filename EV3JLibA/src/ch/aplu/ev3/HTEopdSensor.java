// HTEopdSensor.java

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
public class HTEopdSensor extends Sensor
{
  private lejos.hardware.sensor.HiTechnicEOPD pd;

  /**
   * Creates a sensor instance connected to the given port in long distance mode.
   * The long distance mode is the standard mode using high sensitivity. 
   * @param port the port where the sensor is plugged-in
   */
  public HTEopdSensor(SensorPort port)
  {
    super(port);
    pd = new lejos.hardware.sensor.HiTechnicEOPD(getLejosPort());
    sm = pd.getLongDistanceMode();
  }

  /**
   * Creates a sensor instance connected to the given port in long distance mode.
   * The long distance mode is the standard mode using high sensitivity. 
   */
  public HTEopdSensor()
  {
    this(SensorPort.S1);
  }

  protected void init()
  {
    if (LegoRobot.getDebugLevel() >= SharedConstants.DEBUG_LEVEL_MEDIUM)
      DebugConsole.show("hteo.init()");
  }

  protected void cleanup()
  {
    if (LegoRobot.getDebugLevel() >= SharedConstants.DEBUG_LEVEL_MEDIUM)
      DebugConsole.show("hteo.cleanup()");
    pd.close();
  }

  /**
   * Returns the reference of the the underlying 
   * lejos.hardware.sensor.HiTechnicEOPD.
   * @return the reference of the lejos.hardware.sensor.EV3ColorSensor
   */
  public lejos.hardware.sensor.HiTechnicEOPD getLejosSensor()
  {
    return pd;
  }

  private float getSensorValue()
  {
    int sampleSize = 1;
    float samples[] = new float[sampleSize];
    sm.fetchSample(samples, 0);
    return samples[0];
  }

  /**
   * Polls the sensor.
   * @return the current value the sensor reported in arbitrary units 
   * from 0..999 (999 if the sensor does not detect a target) 
   */
  public int getDistance()
  {
    checkConnect();
    float v = getSensorValue();
    return (int)(1000 * v);
  }

  private void checkConnect()
  {
    if (robot == null)
      new ShowError("HTEopd (port: " + getPortLabel()
        + ") is not a part of the LegoRobot.\n"
        + "Call addPart() to assemble it.");
  }
}
