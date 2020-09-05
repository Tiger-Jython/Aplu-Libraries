// I2CSensor.java

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
 * A sensor wrapper to allow easy access to I2C sensors.
 */
public class I2CSensor extends Sensor
{
  protected lejos.hardware.sensor.I2CSensor is;

  /**
   * Creates a sensor instance of given type connected to the given port
   * using the default address (0x02) and the default sensor 
   * type (LOWSPEED_9V).
   * @param port the port where the sensor is plugged-in
   */
  public I2CSensor(SensorPort port)
  {
    super(port);
    is = new lejos.hardware.sensor.I2CSensor(getLejosPort());
  }

  /**
   * Creates a sensor instance of given type connected to the given port
   * using the default address (0x02) and the give sensor type.
   * @param port the port where the sensor is plugged-in
   * @param sensorType the type of the sensor
   */
  public I2CSensor(SensorPort port, int sensorType)
  {
    super(port);
    is = new lejos.hardware.sensor.I2CSensor(getLejosPort(), sensorType);
  }

  /**
   * Creates a sensor instance of given type connected to the given port
   * the given device address and the given sensor type.
   * @param port the port where the sensor is plugged-in
   * @param deviceAddress in standard Lego/NXT format (range 0x02-0xFE).
   * @param sensorType the type of the sensor
   */
  public I2CSensor(SensorPort port, int deviceAddress, int sensorType)
  {
    super(port);
    is = new lejos.hardware.sensor.I2CSensor(getLejosPort(), deviceAddress, sensorType);
  }
  
  protected void init()
  {
    if (LegoRobot.getDebugLevel() >= SharedConstants.DEBUG_LEVEL_MEDIUM)
      DebugConsole.show("DEBUG: I2CSensor.init() called (Port: " +
        getPortLabel() + ")");
  }

  protected void cleanup()
  {
    if (LegoRobot.getDebugLevel() >= SharedConstants.DEBUG_LEVEL_MEDIUM)
      DebugConsole.show("DEBUG: I2CSensor.cleanup() called (Port: " +
        getPortLabel() + ")");
    is.close();
  }

  /**
   * Retrieves data from the sensor and waits for the result.
   * Data is read from registers in the sensor, usually starting at 0x00.
   * Just supply the register to start reading at, and the length of
   * bytes to read (16 maximum).
   * @param register the starting register used
   * @param buf the buffer where data are returned
   * @param len the length of data to read (minimum 1, maximum 16)
   */
  public void getData(int register, byte[] buf, int len)
  {
    checkConnect();
    is.getData(register, buf, len);
  }
  
  /**
   * Retrieves data from the sensor and waits for the result.
   * Data is read from registers in the sensor, usually starting at 0x00.
   * Just supply the register to start reading at, and the length of
   * bytes to read (16 maximum).
   * @param register the starting register used
   * @param buf the buffer where data are returned
   * @param offset offset of the start of the data
   * @param len the length of data to read (minimum 1, maximum 16)
   */
  public void getData(int register, byte[] buf, int offset, int len)
  {
    checkConnect();
    is.getData(register, buf, offset, len);
  }

  /**
   * Sets a single byte in the I2C sensor.
   * @param register the data register in the I2C sensor
   * @param value the data sent to the sensor
   */
  public void sendData(int register, byte value)
  {
    checkConnect();
    is.sendData(register, value);
  }

  /**
   * Send multiple values with a I2C write transaction.
   * @param register the starting register in the I2C sensor
   * @param buf the buffer where data are supplied
   * @param len the length the buffer (minimum 1, maximum 16)
   */
  public void sendData(int register, byte[] buf, int len)
  {
    checkConnect();
    is.sendData(register, buf, len);
  }
  
  /**
   * Send multiple values with a I2C write transaction.
   * @param register the starting register in the I2C sensor
   * @param buf the buffer where data are supplied
   * @param len the length the buffer (minimum 1, maximum 16)
   * @param offset offset of the start of the data
   */
  public void sendData(int register, byte[] buf, int offset, int len)
  {
    checkConnect();
    is.sendData(register, buf, offset, len);
  }

  /**
   * Returns the product identifier (if available).
   * @return the product identifier (PID)
   */
  public String getProductID()
  {
    return is.getProductID();
  }

  /**
   * Returns the sensor version number (if available).
   * @return the sensor version number
   */
  public String getVersion()
  {
    return is.getVersion();
  }

  /**
   * Returns the reference of the the underlying lejos.hardware.sensor.I2CSensor.
   * @return the reference of the lejos.hardware.sensor.I2CSensor
   */
  public lejos.hardware.sensor.I2CSensor getLejosI2CSensor()
  {
    return is;
  }

  private void checkConnect()
  {
    if (robot == null)
      new ShowError("I2CSensor (port: " + getPortLabel() +
        ") is not a part of the LegoRobot.\n" +
        "Call addPart() to assemble it.");
  }
}
