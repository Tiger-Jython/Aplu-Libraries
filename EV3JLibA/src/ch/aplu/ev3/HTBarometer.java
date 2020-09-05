// HTBarometer.java

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
 * Class that represents a barometer sensor (HiTechnic Barometer).
 * (The sensor's temperature readings are not supported.)
 */
public class HTBarometer extends Sensor
{
  private lejos.hardware.sensor.HiTechnicBarometer htb;

  /**
   * Creates a sensor instance connected to the given port.
   * @param port the port where the sensor is plugged-in
   */
  public HTBarometer(SensorPort port)
  {
    super(port);
    htb = new lejos.hardware.sensor.HiTechnicBarometer(getLejosPort());
    sm = htb.getPressureMode();
  }

  /**
   * Creates a sensor instance connected to port S1.
   */
  public HTBarometer()
  {
    this(SensorPort.S1);
  }

  protected void init()
  {
    if (LegoRobot.getDebugLevel() >= SharedConstants.DEBUG_LEVEL_MEDIUM)
      DebugConsole.show("htb.init()");
  }

  protected void cleanup()
  {
    if (LegoRobot.getDebugLevel() >= SharedConstants.DEBUG_LEVEL_MEDIUM)
      DebugConsole.show("htb.cleanup()");
    htb.close();
  }

  /**
   * Returns the reference of the the underlying 
   * lejos.hardware.sensor.HiTechnicBarometer.
   * @return the reference of the lejos.hardware.sensor.HiTechnicBarometer
   */
  public lejos.hardware.sensor.HiTechnicBarometer getLejosSensor()
  {
    return htb;
  }

  /**
   * Returns the barometric pressure.
   * @return the barometric pressure in pascal (1 bar = 100'000 pa)
   */
  public int getValue()
  {
    checkConnect();
    int sampleSize = 1;
    float samples[] = new float[sampleSize];
    sm.fetchSample(samples, 0);
    return (int)samples[0];
  }

  private void checkConnect()
  {
    if (robot == null)
      new ShowError("HiTechnicBarometer (port: " + getPortLabel()
        + ") is not a part of the LegoRobot.\n"
        + "Call addPart() to assemble it.");
  }
}
