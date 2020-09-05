// PrototypeSensor.java

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
 * Class that represents a prototype sensor 
 * (HiTechnic NXT Prototype Board).
 * The following register layout is assumed:<br>
 * Device address 0x02<br>
 * 0x00-0x07 Version number<br>
 * 0x08-0x0F Manufacturer<br>
 * 0x10-0x17 Sensor type<br>
 * 0x42 Analog channel 0, pin A0, upper 8bits<br>
 * 0x43 Analog channel 0, pin A0, lower 2bits<br>
 * 0x44 Analog channel 1, pin A1, upper 8bits<br>
 * 0x45 Analog channel 1, pin A1, lower 2bits<br>
 * 0x46 Analog channel 2, pin A2, upper 8bits<br>
 * 0x47 Analog channel 2, pin A2, lower 2bits<br>
 * 0x48 Analog channel 3, pin A3, upper 8bits<br>
 * 0x49 Analog channel 3, pin A3, lower 2bits<br>
 * 0x4A Analog channel 4, pin A4, upper 8bits<br>
 * 0x4B Analog channel 4, pin A4, lower 2bits<br>
 * 0x4C Digital input channels, bits 0..5<br>
 * 0x4D Digitag output channels, bits 0..5<br>
 * 0x4E Digital control, bits 0..5, low: input (default), high: output<br>
 * 0x4F Sampling time 4..100(ms)<br><br>
 *
 * Analog inputs in range 0..3.3V, 10 bit (0..1023)<br>
 * Digital inputs/outputs 0/3.3V, pins B0..B5, max. 12mA per output (high or low)<br>
 * The inputs must stay within the 0 - 3.3V range. If this is not observed, 
 * damage will occur.	The inputs are 3.3V CMOS compatible: 
 * low: < 1.0V high: > 2.3V<br><br>
 */
public class PrototypeSensor extends I2CSensor
{
  private static final byte LOWSPEED_9V = 0x0B;
  private static final int I2CADDRESS = 0x02;
  private final int REGISTERBASE = 0x42;
  private final int DIGITALIN    = 0x4C;
  private final int DIGITALOUT   = 0x4D;
  private final int IOCONTROL    = 0x4E;
  private final int SAMPLING     = 0x4F;
  private int control = 0; // Bits 0..5, all inputs

  /**
   * Creates a sensor instance connected to the given port.
   * (default: all digital channels as input)
   * @param port the port where the sensor is plugged-in
   */
  public PrototypeSensor(SensorPort port)
  {
    super(port, I2CADDRESS, LOWSPEED_9V);
  }

  /**
   * Creates a sensor instance connected to port S1.
   * (default: all digital channels as input)
   */
  public PrototypeSensor()
  {
    this(SensorPort.S1);
  }

  protected void init()
  {
    if (LegoRobot.getDebugLevel() >= SharedConstants.DEBUG_LEVEL_MEDIUM)
      DebugConsole.show("DEBUG: Proto.init() called (Port: " +
        getPortLabel() + ")");
    super.init();
    sendData(IOCONTROL, (byte)0); // All inputs
    sendData(SAMPLING, (byte)4); // minimum sampling period
  }

  protected void cleanup()
  {
    if (LegoRobot.getDebugLevel() >= SharedConstants.DEBUG_LEVEL_MEDIUM)
      DebugConsole.show("DEBUG: Proto.cleanup() called (Port: " +
        getPortLabel() + ")");
    sendData(IOCONTROL, (byte)0); // All inputs
    is.close();  // Don't forget!!!
  }

  /**
   * Sets the direction of the 6 digital input/output channels.
   * (default: all digital channels as input)-
   * @param ioControl the direction of each channel. 0: input, 1: output
   */
  public void setDIO(int[] ioControl)
  {
    checkConnect();
    if (ioControl.length != 6)
    {
      new ShowError("setDIO len not 6");
      return;
    }
    for (int i = 0; i < ioControl.length; i++)
    {
      if (ioControl[i] < 0  || ioControl[i] > 1)
      {
        new ShowError("setDIO not 0 or 1");
        return;
      }
    }
    synchronized(this)
    {
      control = 0;
      int bitValue = 1;
      for (int i = 0; i < ioControl.length; i++)
      {
        control = control + bitValue * ioControl[i];
        bitValue *= 2;
      }
      sendData(IOCONTROL, (byte)control); // Set digital IO control
    }
  }
  
  /**
   * Sets the direction of the 6 digital input/output channels.
   * The bit #n corresponds to channel #n; bit value 0: input, 1: output
   * (default: all digital channels as input)
   * @param mask the bit mask 
   */
  public void setDIOMask(int mask)
  {
    checkConnect();
    if (mask < 0 || mask > 63)
    {
      new ShowError("setDIOMask err");
      return;
    }
    sendData(IOCONTROL, (byte)mask); // Set digital IO control
  }
 
  /**
   * Reads the sensor. Analog input data 0..1023 (corresponding to 0..3.3 V)
   * are returned in the given ain.
   * @param ain an integer array of length 5 where to get the analog values
   */
  public void readAnalog(int[] ain)
  {
    int[] din = new int[6];
    read(ain, din);
  }

  /**
   * Reads the sensor. Digital input data 0 (corresponds to low state, 0 V) or 1
   * (corresponds to high state, 3.3 V) is returned in the given din.
   * For digital channels defined as outputs -1 is returned.
   * @param din an integer array of length 6 where to get the digital values
   */
  public void readDigital(int[] din)
  {
    int[] ain = new int[5];
    read(ain, din);
  }

  /**
   * Reads the sensor. Analog input data 0..1023 (corresponding to 0..3.3 V)
   * are returned in the given ain, digital input data (0, 1) in the given din.
   * For digital channels defined as outputs -1 is returned.
   * @param ain an integer array of length 5 where to get the analog values
   * @param din an integer array of length 6 where to get the digital values
   */
  public synchronized void read(int[] ain, int[] din)
  {
    checkConnect();
    if (ain.length != 5)
    {
      new ShowError("read len not 5");
      return;
    }
    if (din.length != 6)
    {
      new ShowError("read len not 6");
      return;
    }
    byte[] in = new byte[11];
    getData(REGISTERBASE, in, 11);
    // Convert to int
    int[] data = new int[11];
    for (int i = 0; i < 11; i++)
      data[i] = in[i] & 0xFF;

    // Analog inputs: base -> upper 8 bits, base+1 -> lower 2 bits
    for (int i = 0; i < 5; i++)
      ain[i] = 4 * data[2*i] + data[2*i+1];

    // Digital inputs
    int bitValue = 1;
    for (int i = 0; i < 6; i++)
    {
      if ((control & bitValue) != 0)
        din[i] = -1;
      else
        din[i] = (data[10] & bitValue) / bitValue;
      bitValue *= 2;
    }

    if (LegoRobot.getDebugLevel() >= SharedConstants.DEBUG_LEVEL_MEDIUM)
      DebugConsole.show("DEBUG: read() values:\n" +
                        getAnalogValues(ain) + getDigitalValues(din));
  }

  private String getAnalogValues(int[] ain)
  {
    StringBuffer sb = new StringBuffer();
    for (int i = 0; i < 5; i++)
      sb.append("a[" + i + "]: " + ain[i] + "\n");
    return sb.toString();
  }

  private String getDigitalValues(int[] din)
  {
    StringBuffer sb = new StringBuffer();
    for (int i = 0; i < 6; i++)
      sb.append("d[" + i + "]: " + din[i] + "\n");
    return sb.toString();
  }

  /**
   * Writes the given bit state (low/high) to the digital output channels.
   * Only channels set as output will be affected. Input channel bits are ignored.
   * @param dout an integer array of length 6 that holds the bit state: 0->low, 1->high
   */
  public synchronized void write(int[] dout)
  {
    checkConnect();
    if (dout.length != 6)
    {
      new ShowError("write len not 6");
      return;
    }
    for (int i = 0; i < 6; i++)
    {
      if (dout[i] < 0 || dout[i] > 1)
      {
        new ShowError("write not 0,1");
        return;
      }
    }
    int bitValue = 1;
    int value = 0;
    for (int i = 0; i < 6; i++)
    {
      if (dout[i] == 1)
        value = value + dout[i] * bitValue;
      bitValue *= 2;
    }
    sendData(DIGITALOUT, (byte)value);
    if (LegoRobot.getDebugLevel() >= SharedConstants.DEBUG_LEVEL_MEDIUM)
      DebugConsole.show("DEBUG: write():\n" +
                        value);
  }
  
  /**
   * Writes the given byte  to the digital output channels.
   * Only channels set as output will be affected. Input channel bits are ignored.
   * @param value a byte (lower 8 bits of int) that holds the bit state: 0->low, 1->high. 
   * Only bit 0 to bit 5 are used.
   */
  public synchronized void writeByte(int value)
  {
    checkConnect();
    sendData(DIGITALOUT, (byte)value);
    if (LegoRobot.getDebugLevel() >= SharedConstants.DEBUG_LEVEL_MEDIUM)
      DebugConsole.show("DEBUG: writeByte():\n" +
                            value);
  }

  /**
   * Sets the sampling period of the ADC (4..100 ms, default 4 ms).
   * @param period the new sampling period
   */
  public void setSamplingPeriod(int period)
  {
    sendData(SAMPLING, (byte)period);
  }

  private void checkConnect()
  {
    if (robot == null)
      new ShowError("PrototypeSensor (port: " + getPortLabel() +
        ") is not a part of the LegoRobot.\n" +
        "Call addPart() to assemble it.");
  }
}
