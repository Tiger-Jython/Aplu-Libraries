// GenericIRSensor.java

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

  protected lejos.hardware.sensor.EV3IRSensor irSensor;

  protected GenericIRSensor(SensorPort port, SensorMode mode)
  {
    super(port);
    irSensor = new lejos.hardware.sensor.EV3IRSensor(getLejosPort());
    switch (mode)
    {
      case SEEK_MODE:
        sm = irSensor.getSeekMode();
        break;
      case DISTANCE_MODE:
        sm = irSensor.getDistanceMode();
        break;
    }
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
    irSensor.close();
  }

  /**
   * Returns the reference of the the underlying 
   * lejos.hardware.sensor.EV3IRSensor.
   * @return the reference of the lejos.hardware.sensor.EV3IRSensor
   */
  public lejos.hardware.sensor.EV3IRSensor getLejosSensor()
  {
    return irSensor;
  }

  /**
   * Polls the sensor.
   * @param channel the channel 1..4 determined by the remote control red switch.
   * @return the current value as IRValue 
   */
  protected IRValue getValue(int channel)
  {
    IRValue irValue = new IRValue();
    int sampleSize = 8;
    float samples[] = new float[sampleSize];
    sm.fetchSample(samples, 0);

    irValue.channel = channel;
    irValue.bearing = (int)(samples[2 * (channel - 1)]);
    irValue.distance = Math.min(2 * (int)(samples[2 * (channel - 1) + 1]), 255);
    return irValue;
  }

  /**
   * Polls the sensor.
   * The returned value has the bearing value in bit 0..7 (augemented by
   * 64 to get only positive values) and its distance value in bits 8..15.
   * @param channel the channel 1..4 determined by the remote control red switch.
   * @return the current value as integer
   */
  protected int getIntValue(int channel)
  {
    return toIntValue(getValue(channel));
  }

  private int toIntValue(IRValue irValue)
  {
    int bearing = irValue.bearing + 64;
    bearing = Math.min(bearing, 255);
    int value = bearing + (irValue.distance << 8);
    return value;
  }

  private IRValue toIRValue(int value)
  {
    IRValue irValue = new IRValue();
    irValue.bearing = (value & 0xFF) - 64;
    irValue.distance = ((value >> 8) & 0xFF);
    return irValue;
  }
}
