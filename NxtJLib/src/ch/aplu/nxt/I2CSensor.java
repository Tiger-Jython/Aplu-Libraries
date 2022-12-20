// I2CSensor.java
// Most of this class taken from the leJOS library (lejos.sourceforge.net),
// with thanks to the author.

/*
 This software is part of the NxtJLib library.
 It is Open Source Free Software, so you may
 - run the code for any purpose
 - study how the code works and adapt it to your needs
 - integrate all or parts of the code in your own programs
 - redistribute copies of the code
 - improve the code and release your improvements to the public
 However the use of the code is entirely your responsibility.
 */
package ch.aplu.nxt;

import ch.aplu.nxt.platform.*;

/**
 * A sensor wrapper to allow easy access to I2C sensors, like the ultrasonic sensor.
 * Currently uses the default I2C address of 0x02, but some sensors can
 * be connected to same port lines and use different addresses using the
 * Auto Detecting Parallel Architecture (ADPA). Currently unsure if there are
 * commercial port expanders yet to use this function, or whether the
 * Lego UltrasonicSensor sensor is ADPA compatible.<br>
 * Most of the code and the documentation
 * taken from the leJOS library (lejos.sourceforge.net, with thanks to the author.
 */
public class I2CSensor extends Sensor implements SharedConstants
{
  private final byte DEFAULT_ADDRESS = 0x02;
  /**
   * Returns the version number of the sensor. e.g. "V1.0" Reply length = 8.
   */
  protected static byte VERSION = 0x00;
  /**
   * Returns the product ID of the sensor.  e.g. "LEGO" Reply length = 8.
   */
  protected static byte PRODUCT_ID = 0x08;
  /**
   * Returns the sensor type. e.g. "Sonar" Reply length = 8.
   */
  protected static byte SENSOR_TYPE = 0x10;
  private byte portId;
  private byte sensorType;

  /**
   * Creates a sensor instance of given type connected to the given port.
   * @param port the port where the sensor is plugged-in
   * @param sensorType the type of the sensor
   */
  public I2CSensor(SensorPort port, byte sensorType)
  {
    super(port);
    this.sensorType = sensorType;
    portId = (byte)port.getId();
  }

  protected void init()
  {
    if (LegoRobot.getDebugLevel() >= DEBUG_LEVEL_MEDIUM)
      DebugConsole.show("DEBUG: I2CSensor.init() called (Port: "
        + getPortLabel() + ")");

    setTypeAndMode(sensorType, RAWMODE);
  }

  protected void cleanup()
  {
    if (LegoRobot.getDebugLevel() >= DEBUG_LEVEL_MEDIUM)
      DebugConsole.show("DEBUG: I2CSensor.cleanup() called (Port: "
        + getPortLabel() + ")");
  }

  /**
   * Retrieves data from the sensor.
   * Data is read from registers in the sensor, usually starting at 0x00
   * and ending around 0x49.
   * Just supply the register to start reading at, and the length of
   * bytes to read (16 maximum).
   * NOTE: The NXT supplies an unsigned byte but Java converts them into signed bytes.
   * @param register the register used, e.g. FACTORY_SCALE_DIVISOR, BYTE0, etc....
   * @param length the length of data to read (minimum 1, maximum 16)
   * @return the data from the sensor
   */
  public byte[] getData(byte register, int length)
  {
    checkConnect();
    byte[] txData =
    {
      DEFAULT_ADDRESS, register
    };
    LSWrite(portId, txData, (byte)length);
    byte[] result = LSRead(portId);
    return result;
  }

  /**
   * Sets a single byte in the I2C sensor.
   * (Fails to work with some I2C sensors.)
   * @param register the data register in the I2C sensor
   * @param value the data sent to the sensor
   */
  public void sendData(byte register, byte value)
  {
    checkConnect();
    byte[] txData =
    {
      DEFAULT_ADDRESS, register, value
    };
    LSWrite(portId, txData, (byte)0);
  }

  /**
   * Sets two consecutive bytes in the I2C sensor.
   * (Fails to work with some I2C sensors.)
   * @param register the first data register in the I2C sensor
   * @param value1 the first data value sent to the sensor
   * @param value2 the second data value sent to the sensor
   */
  public void sendData(byte register, byte value1, byte value2)
  {
    checkConnect();
    //   System.out.println("reg: " + register);
    byte[] txData =
    {
      DEFAULT_ADDRESS, register, value1, value2
    };
    LSWrite(portId, txData, (byte)0);
  }

  /**
   * Returns the version number of the sensor hardware.
   * @return The version number. e.g. "V1.0"
   */
  public String getVersion()
  {
    return fetchString(VERSION, 8);
  }

  /**
   * Returns the Product ID as a string.
   * @return The product ID, e.g. "LEGO"
   */
  public String getProductID()
  {
    return fetchString(PRODUCT_ID, 8);
  }

  /**
   * Returns the type of sensor as a string.
   * @return The sensor type. e.g. "Sonar"
   */
  public String getSensorType()
  {
    return fetchString(SENSOR_TYPE, 8);
  }

  protected String fetchString(byte constantEnumeration, int rxLength)
  {
    byte[] stringBytes = getData(constantEnumeration, rxLength);
    int zeroPos = 0;
    for (zeroPos = 0; zeroPos < stringBytes.length; zeroPos++)
    {
      if (stringBytes[zeroPos] == 0)
        break;
    }
    String s = new String(stringBytes).substring(0, zeroPos);
    return s;
  }

  private void checkConnect()
  {
    if (robot == null)
      new ShowError("I2CSensor (port: " + getPortLabel()
        + ") is not a part of the NxtRobot.\n"
        + "Call addPart() to assemble it.");
 }

}
