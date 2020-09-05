// IRSeekSensor.java

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
 * Class that represents a Lego EV3 Infra Red sensor in Seek Mode. 
 * A remote control box is needed and the infrared beacon must be turned on with
 * the center button. The sensor reports the distance and the direction (bearing)
 * of the infrared source.
 */
public class IRSeekSensor extends GenericIRSensor
{
  private int channel;
  
  /**
   * Creates a sensor instance connected to the given port.
   * @param port the port where the sensor is plugged-in
   */
  public IRSeekSensor(SensorPort port)
  {
    super(port, SensorMode.SEEK_MODE);
    channel = port.getId() + 1;
  }

  /**
   * Creates a sensor instance connected to port S1.
   */
  public IRSeekSensor()
  {
    this(SensorPort.S1);
  }

  /**
   * Polls the sensor.
   * The channel corresponds to the port where the sensor is plugged-in:
   * Port S1:channel 1...Port S4:channel 4.
   * @return the current value as IRValue defined in GenericIRSensor
   */
  public IRValue getValue()
  {
    checkConnect();
    return super.getValue(channel);
  }

  /**
   * Polls the sensor.
   * The channel corresponds to the port where the sensor is plugged-in:
   * Port S1:channel 1...Port S4:channel 4.
   * The returned value has the bearing value in bit 0..7 (augemented by
   * 64 to get only positive values) and its distance value in bits 8..15.
   * @return the current value as integer
   */
  public int getIntValue()
  {
    checkConnect();
    return super.getIntValue(channel);
  }

  private void checkConnect()
  {
    if (robot == null)
      new ShowError("IRSeekSensor (port: " + getPortLabel()
        + ") is not a part of the LegoRobot.\n"
        + "Call addPart() to assemble it.");
  }

}
