// IRDistanceSensor.java

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
 * Class that represents a Lego EV3 Infra Red sensor in Distance Mode.
 */
public class IRDistanceSensor extends GenericIRSensor
{
  /**
   * Creates a sensor instance connected to the given port.
   * @param port the port where the sensor is plugged-in
   */
  public IRDistanceSensor(SensorPort port)
  {
    super(port, SensorMode.DISTANCE_MODE);
  }
  
  /**
   * Creates a sensor instance connected to port S1.
   */
  public IRDistanceSensor()
  {
    this(SensorPort.S1);
  }
  
   /**
   * Polls the sensor.
   * @return the current distance to the reflected target in cm (0..100)
   */
  public int getDistance()
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
      new ShowError("IRDistanceSensor (port: " + getPortLabel()
        + ") is not a part of the LegoRobot.\n"
        + "Call addPart() to assemble it.");
  }


  
 
}
