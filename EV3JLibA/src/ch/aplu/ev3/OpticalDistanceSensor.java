// OpticalDistanceSensor.java

/*
 This software is part of the EV3JLib library.
 It is Open Source Free Software, so you may
 - run the code for any purpose
 - study how the code works and adapt it to your needs
 - integrate all or parts of the code in your own programs
 - redistribute copies of the code
 - improve the code and release your improvements to the public
 However the use of the code is entirely your responsibility.
 Code from NXT distribution, with thanks to the author.
 */
package ch.aplu.ev3;


/**
 * Class that represents a high precision infrared distance sensor 
 * (High Precision Short Range Infrared Distance Sensor from mindstorms.com).
 */
public class OpticalDistanceSensor extends I2CSensor
{
  private byte[] buf = new byte[2];
  private static final byte LOWSPEED_9V = 0x0B;
  private static final int I2CADDRESS = 0x02;

  //Registers
  private final static int COMMAND = 0x41;
  private final static int DIST_DATA_LSB = 0x42;
  private final static int DIST_DATA_MSB = 0x43;
  private final static int VOLT_DATA_LSB = 0x44;
  private final static int VOLT_DATA_MSB = 0x45;
  private final static int SENSOR_MOD_TYPE = 0x50;
  private final static int CURVE = 0x51;
  private final static int DIST_MIN_DATA_LSB = 0x52;
  private final static int DIST_MIN_DATA_MSB = 0x53;
  private final static int DIST_MAX_DATA_LSB = 0x54;
  private final static int DIST_MAX_DATA_MSB = 0x55;
  private final static int VOLT_DATA_POINT_LSB = 0x52;
  private final static int VOLT_DATA_POINT_MSB = 0x53;
  private final static int DIST_DATA_POINT_LSB = 0x54;
  private final static int DIST_DATA_POINT_MSB = 0x55;

  //Sensor Modules
  private final static byte GP2D12 = 0x31;

  /**
   * DIST-Nx-Short
   */
  private final static byte GP2D120 = 0x32;

  /**
   * DIST-Nx-Medium
   */
  private final static byte GP2YA21 = 0x33;

  /**
   * DIST-Nx-Long
   */
  private final static byte GP2YA02 = 0x34;

  /**
   * Custom sensor
   */
  private final static byte CUSTOM = 0x35;

  //Commands
  private final static byte DE_ENERGIZED = 0x44;
  private final static byte ENERGIZED = 0x45;
  private final static byte ARPA_ON = 0x4E;
  private final static byte ARPA_OFF = 0x4F; //(default)

  /**
   * Creates a sensor instance connected to the given port.
   * @param port the port where the sensor is plugged-in
   */
  public OpticalDistanceSensor(SensorPort port)
  {
    super(port, I2CADDRESS, LOWSPEED_9V);
  }

  /**
   * Creates a sensor instance connected to port S1.
   */
  public OpticalDistanceSensor()
  {
    this(SensorPort.S1);
  }
  
   protected void init()
  {
    if (LegoRobot.getDebugLevel() >= SharedConstants.DEBUG_LEVEL_MEDIUM)
      DebugConsole.show("DEBUG: ods.init() called (Port: " +
        getPortLabel() + ")");
    super.init();
    powerOn();
  }

  protected void cleanup()
  {
    if (LegoRobot.getDebugLevel() >= SharedConstants.DEBUG_LEVEL_MEDIUM)
      DebugConsole.show("DEBUG: ods.cleanup() called (Port: " +
        getPortLabel() + ")");
    is.close();  // Don't forget!!!
  }

  /**
   * Returns the distance from the object in millimeters.
   * @return distance in mm
   */
  public int getValue()
  {
    checkConnect();
    return getDistLSB();
  }

  // This only needs the be run if you are changing the sensor.
  private void setSensorModule(byte module)
  {
    sendData(COMMAND, module);
  }

   // Turns the sensor module on. 
  private void powerOn()
  {
    sendData(COMMAND, ENERGIZED);
  }

   // Turns the sensor module off.
  private void powerOff()
  {
    sendData(COMMAND, DE_ENERGIZED);
  }

   // Enables (ADPA) Auto Detecting Parallel Architecture. <br>
   // Once you have enabled it you dont have to enable again.
  private void setAPDAOn()
  {
    sendData(COMMAND, ARPA_ON);
  }

   // Disables (ADPA) Auto Detecting Parallel Architecture.<br>
   // Disabled by default.
  private void setAPDAOff()
  {
    sendData(COMMAND, ARPA_OFF);
  }

   // Returns the current distance in millimeters for the LSB.
  private int getDistLSB()
  {
    return readDISTNX(DIST_DATA_LSB, 2);
  }

   // Returns the current distance in millimeters for MSB.
  private int getDistMSB()
  {
    return readDISTNX(DIST_DATA_MSB, 2);
  }

   // Returns the current voltage level in millivolts for the LSB. 
  private int getVoltLSB()
  {
    return readDISTNX(VOLT_DATA_LSB, 2);
  }

   // Returns the current voltage level in millivolts for the MSB. 
  private int getVoltMSB()
  {
    return readDISTNX(VOLT_DATA_MSB, 2);
  }

   // Used to determin the sensore module that is configured. 
   // This can be helpful if the sensor is not working properly.
  private int getSensorModule()
  {
    return readDISTNX(SENSOR_MOD_TYPE, 1);
  }

  /*
   * Gets the number of points that will be in the curve.
   *  This corresponds to the set/get volt and distance methods.
   * Used for recalibrating the sensor. 
   */
  private int getCurveCount()
  {
    return readDISTNX(CURVE, 1);
  }

  /*
   * Sets the number of points that will be in the configured curve. 
   * This corresponds to the set/get volt and distance points methods.
   * Used for recalibrating the sensor. 
   */
  private void setCurveCount(int value)
  {
    sendData(CURVE, (byte)value);
  }

  /*
   * Gets the min value in millimeters for the LSB.
   * Used for recalibrating the sensor. 
   */
  private int getDistMinLSB()
  {
    return readDISTNX(DIST_MIN_DATA_LSB, 2);
  }

  /*
   * Sets the min value in millimeters for the LSB.
   * Used for recalibrating the sensor.
   */
  private void setDistMinLSB(int value)
  {
    writeDISTNX(DIST_MIN_DATA_LSB, (byte)value);
  }

  /*
   * Gets the min value in millimeters for the MSB.
   * Used for recalibrating the sensor. 
   */
  private int getDistMinMSB()
  {
    return readDISTNX(DIST_MIN_DATA_MSB, 2);
  }

  /*
   * Sets the min value in millimeters for the MSB.<br>
   * Used for recalibrating the sensor. 
   */
  private void setDistMinMSB(int value)
  {
    writeDISTNX(DIST_MIN_DATA_MSB, (byte)value);
  }

  /*
   * Gets the max value in millimeters for the LSB.
   * Used for recalibrating the sensor. 
   */
  private int getDistMaxLSB()
  {
    return readDISTNX(DIST_MAX_DATA_LSB, 2);
  }

  /*
   * Sets the max value in millimeters for LSB.
   * Used for recalibrating the sensor. 
   */
  private void setDistMaxLSB(int value)
  {
    writeDISTNX(DIST_MAX_DATA_LSB, (byte)value);
  }

  /*
   * Gets the max value in millimeters for the MSB.
   * Used for recalibrating the sensor. 
   */
  private int getDistMaxMSB()
  {
    return readDISTNX(DIST_MAX_DATA_MSB, 2);
  }

  /*
   * Sets the max value in millimeters for the MSB.
   * Used for recalibrating the sensor. 
   */
  private void setDistMaxMSB(int value)
  {
    writeDISTNX(DIST_MAX_DATA_MSB, (byte)value);
  }

  /*
   * Gets millivolts value of the specific index for the LSB.
   * These will corispond with the point methods index value.
   * Used for recalibrating the sensor.
   */
  private int getVoltPointLSB(int index)
  {
    if (index == 0)
      index = 1;
    index = VOLT_DATA_POINT_LSB + 4 * index;
    return readDISTNX(index, 2);
  }

  /*
   * Sets millivolts value of the specific index for the LSB. 
   * These will corispond with the point methods index value.
   * Used for recalibrating the sensor.
   */
  private void setVoltPointLSB(int index, int value)
  {
    if (index == 0)
      index = 1;
    index = VOLT_DATA_POINT_LSB + 4 * index;
    sendData(index, (byte)value);
  }

  /*
   * Gets millivolts value of the specific index for the MSB.
   *  These will corispond with the point methods index value.
   * Used for recalibrating the sensor.
   */
  private int getVoltPointMSB(int index)
  {
    if (index == 0)
      index = 1;
    index = VOLT_DATA_POINT_MSB + 4 * index;
    return readDISTNX(index, 2);
  }

  /*
   * Sets millivolts value of the specific index for the MSB. 
   * These will corispond with the point methods index value.
   * Used for recalibrating the sensor.
   */
  private void setVoltPointMSB(int index, int value)
  {
    if (index == 0)
      index = 1;
    index = VOLT_DATA_POINT_MSB + 4 * index;
    writeDISTNX(index, value);
  }

  /*
   * Gets millimeter value of the specific index for the LSB.
   * Used for recalibrating the sensor.
   */
  private int getDistPointLSB(int index)
  {
    if (index == 0)
      index = 1;
    index = DIST_DATA_POINT_LSB + 4 * index;
    return readDISTNX(index, 2);
  }

  /*
   * Sets millimeter value of the specific index for the LSB. 
   * Used for recalibrating the sensor.
   */
  private void setDistPointLSB(int index, int value)
  {
    if (index == 0)
      index = 1;
    index = DIST_DATA_POINT_LSB + 4 * index;
    writeDISTNX(index, value);
  }

  /*
   * Gets millimeter value of the specific index for the MSB.
   * Used for recalibrating the sensor.
   */
  private int getDistPointMSB(int index)
  {
    if (index == 0)
      index = 1;
    index = DIST_DATA_POINT_MSB + 4 * index;
    return readDISTNX(index, 2);
  }

  /*
   * Sets millimeter value of the specific index for the MSB. <br>
   * Used for recalibrating the sensor.
   */
  private void setDistPointMSB(int index, int value)
  {
    if (index == 0)
      index = 1;
    index = DIST_DATA_POINT_MSB + 4 * index;
    writeDISTNX(index, value);
  }

  /*
   * Returns an integer value from the specified register.<br>
   * @param register I2C register, e.g 0x41
   * @param bytes number of bytes to read 1 or 2
   */
  private int readDISTNX(int register, int bytes)
  {
    int buf0;
    int buf1;

    getData(register, buf, bytes);

    if (bytes == 1)
      return buf[0] & 0xFF;

    buf0 = buf[0] & 0xFF;
    buf1 = buf[1] & 0xFF;
    return buf1 * 256 + buf0;
  }

  /*
   * Writes an integer value to the register. <br>
   * This is called if two bytes are to be writen.<br>
   * All other methods just call sendData() from i2cSensor.
   * @param register I2C register, e.g 0x41
   * @param value iteger value to be writen to the register
   */
  private void writeDISTNX(int register, int value)
  {
    int buf0;
    int buf1;

    buf0 = value % 256;
    buf1 = value / 256;
    buf[0] = (byte)buf0;
    buf[1] = (byte)buf1;

    sendData(register, buf, 2);
  }
  
  private void checkConnect()
  {
    if (robot == null)
      new ShowError("PrototypeSensor (port: " + getPortLabel() +
        ") is not a part of the LegoRobot.\n" +
        "Call addPart() to assemble it.");
  }
}
