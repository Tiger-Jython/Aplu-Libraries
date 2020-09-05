// TemperatureSensor.java

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
 * Class that represents the Lego NXT temperature sensor (9749). The device
 * is built up with the TMP275 digital sensor from Burr-Brown (Texas Instruments).
 * As specified by the manufacturer the temperature range is -55 .. 128 degrees Celsius.
 * The accuracy is +-0.5 degrees in range -20 to 100 degrees and the
 * the resolution is 0.0625 degrees (in 12 bit mode used here). 
 */
public class TemperatureSensor extends I2CSensor
{

  private static final byte TYPE_LOWSPEED = 0x0A;
  private static final int I2CSlaveAddress = 0x98;
  private static final int REG_TEMPERATURE = 0x00;
  private static final int REG_CONFIG = 0x01;

  /**
   * Creates a sensor instance connected to the given port.
   * @param port the port where the sensor is plugged-in
   */
  public TemperatureSensor(SensorPort port)
  {
    super(port, I2CSlaveAddress, TYPE_LOWSPEED);
  }

  /**
   * Creates a sensor instance connected to port S1.
   */
  public TemperatureSensor()
  {
    this(SensorPort.S1);
  }

  protected void init()
  {
    if (LegoRobot.getDebugLevel() >= SharedConstants.DEBUG_LEVEL_MEDIUM)
      DebugConsole.show("DEBUG: temp.init() called (Port: "
        + getPortLabel() + ")");
    super.init();
    setResolution(3);  // 12 bit
  }

  protected void cleanup()
  {
    if (LegoRobot.getDebugLevel() >= SharedConstants.DEBUG_LEVEL_MEDIUM)
      DebugConsole.show("DEBUG: temp.cleanup() called (Port: "
        + getPortLabel() + ")");
    is.close();  // Don't forget!!!
  }

  /**
  * Returns the temperature in degrees Celsius rounded to two decimals.
  * @return the current temperature value 
  */
  public double getTemperature()
  {
    checkConnect();
    byte[] buf = new byte[2];
    getData(REG_TEMPERATURE, buf, 2);
    
    int hi = buf[0];
    int lo = buf[1];
    int hi1 = hi << 4;
    int lo1 = (lo & 0xFF) >> 4;
    int t12bit = hi1 + lo1;
    double resolution = 128 / 2048.0;
    double T;
    if (t12bit < 2048)
      T = t12bit * resolution;
    else
    {
      t12bit = 4096 - t12bit;
      T = -t12bit * resolution;
    }
    
    T = T * 100;
    T = (int)Math.round(T);
    return T / 100.0;
  }

  private int getResolution()
  {
    byte[] buf = new byte[1];
    getData(REG_CONFIG, buf, 1);
    return buf[0];
  }

  private void setResolution(int resolution)
  {
    sendData(REG_CONFIG, (byte)(resolution << 5));
  }

  private void checkConnect()
  {
    if (robot == null)
      new ShowError("TemperatureSensor (port: " + getPortLabel()
        + ") is not a part of the LegoRobot.\n"
        + "Call addPart() to assemble it.");
  }
}
