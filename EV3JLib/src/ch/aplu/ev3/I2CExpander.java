// I2CExpander.java
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
 * Class that represents one of the standard I2C expander ICs.
 * The EV3 acts as master and the the expander as slave.
 */
public class I2CExpander extends Sensor
{
  private int deviceType = -1;  // no device
  private int inputMode = 0;  // single ended
  private int slaveAddress8bit;

  /**
   * Creates a sensor instance connected to the given port.
   * Call setup() to set the device type and I2C address.
   * @param port the port where the sensor is plugged-in
   */
  public I2CExpander(SensorPort port)
  {
    super(port);
    partName = "i2c" + (port.getId() + 1);
  }

  /**
   * Creates a sensor instance connected to sensor port 1.
   * Call setup() to set the device type and I2C address.
   */
  public I2CExpander()
  {
    this(SensorPort.S1);
  }

  /**
   * Creates a sensor instance connected to the given port and selects
   * devivce type, input mode and I2C address of attached device.
   * Supported device types: 0: PCF8574, 1: PCF8574A, 2: PCF8591
   * Supported device modes for PCF8591: 0: Four single ended inputs, 1: Three
   * differential inputs, 2: Single ended and differential mixed, 3: Two differential 
   * inputs (see PCF8591 data sheet for more information). Keep in mind to
   * set unused analog inputs to defined level.<br><br>
   * For compatibility with other leJOS I2C devices, the 8-bit I2C addressing 
   * is used, where the LSB is always 0 and the 7 standard I2C
   * address bits A0..A6 are arranged in a byte as follows:<br> 
   * A6 A5 A4, A3 | A2 A1 A0 0
   * @param port the port where the sensor is plugged-in
   * @param deviceType expander type
   * @param inputMode expander input mode (for PCF8591)
   * @param slaveAddress8bit the 8 bit I2C address of the device
   */
  public I2CExpander(SensorPort port, int deviceType, int inputMode, int slaveAddress8bit)
  {
    super(port);
    partName = "i2c" + (port.getId() + 1);
    setup(deviceType, slaveAddress8bit);
    this.inputMode = inputMode;
  }

  /**
   * Creates a sensor instance connected to the given port and selects
   * device type and I2C address of attached device. The default input mode 0
   * is used (single ended).
   * Supported device types: 0: PCF8574, 1: PCF8574A, 2: PCF8591
   * For compatibility with other leJOS I2C devices, the 8-bit I2C addressing 
   * is used, where the LSB is always 0 and the 7 standard I2C
   * address bits A0..A6 are arranged in a byte as follows:<br> 
   * A6 A5 A4, A3 | A2 A1 A0 0
   * @param port the port where the sensor is plugged-in
   * @param deviceType expander type
   * @param slaveAddress8bit the 8 bit I2C address of the device
   */
  public I2CExpander(SensorPort port, int deviceType, int slaveAddress8bit)
  {
    super(port);
    partName = "i2c" + (port.getId() + 1);
    setup(deviceType, slaveAddress8bit);
  }

  /**
   * Selects device type and I2C address of attached device.
   * For compatibility with other leJOS I2C devices, the 8-bit I2C addressing is used, where the LSB is always 0 and the 7
   * address bits A0..A6 are arranged in a byte as follows:<br> 
   * A6 A5 A4, A3 | A2 A1 A0 0
   * @param deviceType expander type; supported devices. 0: PCF8574, 1: PCF8574A, 2: PCF8591
   * @param slaveAddress8bit the 8 bit I2C address of the device
   */
  public void setup(int deviceType, int slaveAddress8bit)
  {
    boolean ok = true;
    int slaveAddress7Bit = (slaveAddress8bit >> 1) & 0x7F;
    if (deviceType == 0)
      if (slaveAddress7Bit < 0x20 || slaveAddress7Bit > 0x27)
        ok = false;
    if (deviceType == 1)
      if (slaveAddress7Bit < 0x38 || slaveAddress7Bit > 0x3F)
        ok = false;
    if (deviceType == 2)
      if (slaveAddress7Bit < 0x48 || slaveAddress7Bit > 0x4F)
        ok = false;
    if (!ok)
      new ShowError("I2C Expander: I2CSlaveAddress not in range for this device");
    this.deviceType = deviceType;
    this.slaveAddress8bit = slaveAddress8bit;
  }

  protected void init()
  {
    if (LegoRobot.getDebugLevel() >= SharedConstants.DEBUG_LEVEL_MEDIUM)
      DebugConsole.show("DEBUG: I2CExpander.init() called (Port: "
        + getPortLabel() + ")");
    robot.sendCommand(partName + ".setup." + deviceType + "." + slaveAddress8bit);
    if (inputMode != 0)
      robot.sendCommand(partName + ".setAnalogInputMode." + inputMode);
  }

  protected void cleanup()
  {
    if (LegoRobot.getDebugLevel() >= SharedConstants.DEBUG_LEVEL_MEDIUM)
      DebugConsole.show("DEBUG: I2CExpander.cleanup() called (Port: "
        + getPortLabel() + ")");
  }

  /**
   * Writes one byte data to the output and reads back the result.
   * Returns the current data. To define a port pin as input, write the bit value 1
   * to the port pin. On power on all pins are pulled to VDD with approx. 50 kOhm.<br> 
   * Digital output: If 0 is written to port pin, the max. pull-down current is 10 mA.<br>
   * Digital input: 1 must be written to port pin. The attached electronics pulls this port either to ground 
   * (GND) or VCC. Input current is approx. 100 uA for LO input (impedance approx. 
   * 50 kOhm) and infinite impedance for HI input. 
   * @param out Digital out: 0 for digital out - LO state, 1 for digital out - HI state.
   * Digital in: 1 must be written to the port pin and the external electronics pulls
   * the pin to LO or HI.
   * @return the current state of the port bits: 0 for LO, 1 for HI; -1 if the
   * device does not support digital in/out. An integer is
   * returned that represents the unsigned byte
   */
  public int writeDigital(int out)
  {
    checkConnect();
    if (deviceType < 0 || deviceType > 1)
    {
      new ShowError("I2CExpander.writeDigital() not available for this type of device");
      return -1;
    }
    int v = 0;
    try
    {
      v = Integer.parseInt(robot.sendCommand(partName + ".writeDigital." + out));
    }
    catch (NumberFormatException ex)
    {
    }
    return v;
  }

  /**
   * Sets the output voltage of the 8-bit DAC. 
   * @param out 0..255 converted to 0..VCC
   */
  public void writeAnalog(int out)
  {
    checkConnect();
    robot.sendCommand(partName + ".writeAnalog." + out);
  }

  /**
   * Sets the input mode for the PCF8591 device.
   * Supported input modes: 0: Four single ended inputs, 1: Three
   * differential inputs, 2: Single ended and differential mixed, 3: Two differential 
   * inputs (see PCF8591 data sheet for more information). Keep in mind to
   * set unused analog inputs to defined level.
   * @param inputMode the input mode (0, 1, 2, 3)
   */
  public void setAnalogInputMode(int inputMode)
  {
    this.inputMode = inputMode;
    robot.sendCommand(partName + ".setAnalogInputMode." + inputMode);
  }

  /**
   * Reads all analog inputs at same time (8 bit ADC). The returned values 
   * are in 0..255 for single ended inputs and -128..127 for differential inputs.
   * For device mode 0, channels 0..3 are returned, for mode 1 and 2, channels 0..2 
   * are returned, for mode 3, channels 0..1 are returned.
   * @return the current input voltages 0..VCC digitized to 8 bits  
   */
  public int[] readAnalog()
  {
    int v = 0;
    try
    {
      v = Integer.parseInt(robot.sendCommand(partName + ".readAnalogInt"));
    }
    catch (NumberFormatException ex)
    {
    }
    int[] data = new int[4];
    data[0] = toInt((byte)(v & 0xFF), 0);
    data[1] = toInt((byte)((v >> 8) & 0xFF), 1);
    data[2] = toInt((byte)((v >> 16) & 0xFF), 2);
    data[3] = toInt((byte)((v >> 24) & 0xFF), 3);
    return data;
  }

  /**
   * Reads all analog inputs at same time (8 bit ADC). The returned values are
   * in 0..255 for single ended inputs and -128..127 for differential inputs. 
   * For device mode 0, channels 0..3 are returned, for mode 1 and 2, channels 0..2 
   * are returned, for mode 3, channels 0..1 are returned.
   * @return the current input voltages 0..VCC digitized to 8 bits and
   * packed into a 32 bit integer with channel 0 at LSB
   */
  public int readAnalogInt()
  {
    checkConnect();
    int v = 0;
    try
    {
      v = Integer.parseInt(robot.sendCommand(partName + ".readAnalogInt"));
    }
    catch (NumberFormatException ex)
    {
    }
    return v;
  }

  /**
   * Reads one analog input channel (8 bit ADC). The returned value is
   * in 0..255 for single ended input and -128..127 for differential input.
   * @return the current input voltage 0..VCC digitized to 8 bits.
   */
  public int readAnalog(int channel)
  {
   checkConnect();
   int v = 0;
    try
    {
      v = Integer.parseInt(robot.sendCommand(partName + ".readAnalog." + channel));
    }
    catch (NumberFormatException ex)
    {
    }
    return v;
  }

  private int toInt(byte b, int channel)
  {
    switch (inputMode)
    {
      case 0:
        return b & 0xFF;  // unsigned byte
      case 2:
        if (channel == 2)
          return b;
        else
          return b & 0xFF;
      default:
        return b;  // signed byte
    }
  }

  private void checkConnect()
  {
    if (robot == null)
      new ShowError("I2CExpander (port: " + getPortLabel()
        + ") is not a part of the LegoRobot.\n"
        + "Call addPart() to assemble it.");
  }

}
