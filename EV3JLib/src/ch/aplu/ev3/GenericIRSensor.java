// GenericIRSensor.java
// Direct mode

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
 * Class that represents a generic Lego EV3 Infra Red sensor.
 */
public abstract class GenericIRSensor extends Sensor
{
  protected enum SensorMode
  {
    SEEK_MODE, DISTANCE_MODE
  };

  /** 
   * Class to combine channel, bearing and distance values.
   */
  public class IRValue
  {
    /**
     * The channel number 1..4
     */
    public int channel;
    //
    /**
     * The bearing value from about -12 to +12 with values increasing 
     * clockwise when looking from behind the sensor. 
     * A bearing of 0 indicates the beacon is directly in front of the sensor.
     * If no beacon is detected, a bearing of 0 is returned.
     */
    public int bearing;
    //
    /**
     * Distance values (0-100) are in cm. If no beacon is detected 
     * a distance of 255 is returned.
     */
    public int distance;
  }

  protected GenericIRSensor(SensorPort port, SensorMode mode)
  {
    super(port);
  }

  protected void init()
  {
    if (LegoRobot.getDebugLevel() >= SharedConstants.DEBUG_LEVEL_MEDIUM)
      DebugConsole.show("DEBUG: GenericIRSensor.init() called");
  }

  protected void cleanup()
  {
    if (LegoRobot.getDebugLevel() >= SharedConstants.DEBUG_LEVEL_MEDIUM)
      DebugConsole.show("DEBUG: GenericIRSensor.cleanup() called");
  }

  /**
   * Polls the sensor.
   * @return the current value as IRValue 
   */
  protected IRValue getValue()
  {
    return toIRValue(getIntValue());
  }

  /**
   * Polls the sensor.
   * The returned value has the bearing value in bit 0..7 (augemented by
   * 64 to get only positive values) and its distance value in bits 8..15.
   * @return the current value as integer
   */
  protected int getIntValue()
  {
    int v = 0;
    try
    {
      v = Integer.parseInt(robot.sendCommand(partName + ".getIntValue"));
    }
    catch (NumberFormatException ex)
    {
    }
    return v;
  }

  private IRValue toIRValue(int value)
  {
    IRValue irValue = new IRValue();
    irValue.channel = getPort().getId() + 1;
    irValue.bearing = (value & 0xFF) - 64;
    irValue.distance = ((value >> 8) & 0xFF);
    return irValue;
  }
}
