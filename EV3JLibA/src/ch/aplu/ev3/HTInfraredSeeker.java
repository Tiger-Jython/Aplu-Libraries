// HTInfraredSeeker.java

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
 * Class that represents a infrared seeker version 2 from HiTechnic. The device reports
 * the reflection level of 5 infrared sensors (sensor id = 1..5)
 * covering 60-degrees sectors. These levels correspond roughly to the distance
 * of a reflecting object. Moreover the device reports the approximate
 * direction of a reflecting object in nine 30-degrees sectors.<br><br>
 * The underlying leJOS IRSeeker works for DC (constant) infrared sources.<br><br>
 *
 * (not yet tested!) 
 */
public class HTInfraredSeeker extends Sensor
{
  private lejos.hardware.sensor.HiTechnicIRSeekerV2 is;

  /**
   * Creates a sensor instance connected to the given port.
   * @param port the port where the sensor is plugged-in
   */
  public HTInfraredSeeker(SensorPort port)
  {
    super(port);
    is = new lejos.hardware.sensor.HiTechnicIRSeekerV2(getLejosPort());
    sm = is.getModulatedMode();
  }

  /**
   * Creates a sensor instance connected to port S1.
   */
  public HTInfraredSeeker()
  {
    this(SensorPort.S1);
  }

  protected void init()
  {
    if (LegoRobot.getDebugLevel() >= SharedConstants.DEBUG_LEVEL_MEDIUM)
      DebugConsole.show("is.init()");
  }

  protected void cleanup()
  {
    if (LegoRobot.getDebugLevel() >= SharedConstants.DEBUG_LEVEL_MEDIUM)
      DebugConsole.show("is.cleanup()");
    is.close();
  }

  /**
   * Returns the direction (1..9) to the detected infrared source.
   * @return the approximate sector id nine 30-degrees sectors;
   * -1 of an error occured
   */
  public int getDirection()
  {
    checkConnect();
    int sampleSize = sm.sampleSize();
    float samples[] = new float[sampleSize];
    sm.fetchSample(samples, 0);
    return (int)samples[0];
  }

  /**
   * Returns the reference of the the underlying lejos.hardware.sensor.HiTechnicIRSeekerV2.
   * @return the reference of the lejos.hardware.sensor.HiTechnicIRSeekerV2
   */
  public lejos.hardware.sensor.HiTechnicIRSeekerV2 getLejosSensor()
  {
    return is;
  }

  private void checkConnect()
  {
    if (robot == null)
      new ShowError("HTInfraredSeeker is not a part of the LegoRobot.\n"
        + "Call addPart() to assemble it.");
  }

}
