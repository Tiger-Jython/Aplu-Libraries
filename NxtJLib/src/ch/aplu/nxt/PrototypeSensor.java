// PrototypeSensor.java

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
 * Class that represents a prototype sensor (HiTechnic NXT Prototyping Board).
 * The following register layout is assumed:<br>
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
 * Digital inputs/outputs 0/3.3V, pin B0..B5, max. 12mA per output (high or low)<br>
 */
public class PrototypeSensor extends I2CSensor
{
  // -------------- Inner class PrototypeSensorThread ------------
  private class PrototypeSensorThread extends NxtThread
  {
    private volatile boolean isRunning = false;

    private PrototypeSensorThread()
    {
      if (LegoRobot.getDebugLevel() >= DEBUG_LEVEL_LOW)
        DebugConsole.show("PrTh created");
    }

    public void run()
    {
      if (LegoRobot.getDebugLevel() >= DEBUG_LEVEL_LOW)
        DebugConsole.show("PrTh started");

      int[] ain = new int[5];
      int[] din = new int[6];
      for (int i = 0; i < 5; i++)
        ain[i] = 0;
      for (int i = 0; i < 6; i++)
        din[i] = 0;
      int[] ain_new = new int[5];
      int[] din_new = new int[6];

      isRunning = true;
      while (isRunning)
      {
        if (prototypeListener != null)
        {
          delay(pollDelay);
          boolean rc = readSensor(ain_new, din_new);
          if (!rc)  // Error
            continue;
          boolean isAnalogChanged = false;
          for (int i = 0; i < 5; i++)
          {
            if (ain_new[i] == ain[i])
              ain_new[i] = -1;
            else
            {
              ain[i] = ain_new[i];
              isAnalogChanged = true;
            }
          }
          boolean isDigitalChanged = false;
          for (int i = 0; i < 6; i++)
          {
            if (din_new[i] == din[i])
              din_new[i] = -1;
            else
            {
              din[i] = din_new[i];
              isDigitalChanged = true;
            }
          }
          if (isAnalogChanged)
          {
            if (inAnalogCallback)
            {
              if (LegoRobot.getDebugLevel() >= DEBUG_LEVEL_LOW)
                DebugConsole.show("DEBUG: Prototype event 'ainChanged' (port: "
                  + getPortLabel() + ") -------- rejected: Other callback underway!");
            }
            else
            {
              inAnalogCallback = true;
              if (LegoRobot.getDebugLevel() >= DEBUG_LEVEL_LOW)
                DebugConsole.show("DEBUG: Prototype event 'ainChanged' (port: "
                  + getPortLabel() + ")");
              prototypeListener.ainChanged(getPort(), ain_new);
              inAnalogCallback = false;
            }
          }
          if (isDigitalChanged)
          {
            if (inDigitalCallback)
            {
              if (LegoRobot.getDebugLevel() >= DEBUG_LEVEL_LOW)
                DebugConsole.show("DEBUG: Prototype event 'dinChanged' (port: "
                  + getPortLabel() + ") -------- rejected: Other callback underway!");
            }
            else
            {
              inDigitalCallback = true;
              if (LegoRobot.getDebugLevel() >= DEBUG_LEVEL_LOW)
                DebugConsole.show("DEBUG: Prototype event 'dinChanged' (port: "
                  + getPortLabel() + ")");
              prototypeListener.dinChanged(getPort(), din_new);
              inDigitalCallback = false;
            }
          }
        }
      }
    }

    private void stopThread()
    {
      isRunning = false;
      try
      {
        joinX(500);
      }
      catch (InterruptedException ex)
      {
      }
      if (LegoRobot.getDebugLevel() >= DEBUG_LEVEL_LOW)
        if (isAlive())
          DebugConsole.show("DEBUG: PrototypeSendorThread stopping failed (port: "
            + getPortLabel() + ")");
        else
          DebugConsole.show("DEBUG: PrototypeSensorThread successfully stopped (port: "
            + getPortLabel() + ")");
    }
  }
// -------------- End of inner classes -----------------------
  private final byte REGISTERBASE = 0x42;
  private final byte DIGITALIN = 0x4C;
  private final byte DIGITALOUT = 0x4D;
  private final byte IOCONTROL = 0x4E;
  private final byte SAMPLING = 0x4F;
  private int control = 0; // Bits 0..5, all inputs
  private volatile static boolean inAnalogCallback = false;
  private volatile static boolean inDigitalCallback = false;
  private PrototypeListener prototypeListener = null;
  private PrototypeSensorThread pst;
  private int pollDelay;

  /**
   * Creates a sensor instance connected to the given port.
   * @param port the port where the sensor is plugged-in
   */
  public PrototypeSensor(SensorPort port)
  {
    super(port, LOWSPEED_9V);
    pst = new PrototypeSensorThread();
    NxtProperties props = LegoRobot.getProperties();
    pollDelay = props.getIntValue("PrototypeSensorPollDelay");
  }

  /**
   * Creates a sensor instance connected to port S1.
   */
  public PrototypeSensor()
  {
    this(SensorPort.S1);
  }

  protected void init()
  {
    if (LegoRobot.getDebugLevel() >= DEBUG_LEVEL_MEDIUM)
      DebugConsole.show("DEBUG: Prototype.init() called (Port: "
        + getPortLabel() + ")");
    super.init();
    sendData(IOCONTROL, (byte)0); // All inputs
    sendData(SAMPLING, (byte)4); // minimum sampling period
  }

  protected void cleanup()
  {
    if (LegoRobot.getDebugLevel() >= DEBUG_LEVEL_MEDIUM)
      DebugConsole.show("DEBUG: Prototype.cleanup() called (Port: "
        + getPortLabel() + ")");
    if (pst != null)
      pst.stopThread();
    sendData(IOCONTROL, (byte)0); // All inputs
  }

  /**
   * Sets the direction of the 6 digital input/output channels.
   * @param ioControl the direction of each channel. 0: input, 1: output
   */
  public void setDIO(int[] ioControl)
  {
    if (ioControl.length != 6)
    {
      new ShowError("Error in PrototypeSensor.setDIO().\n"
        + "Parameter ioControl must have length 6.");
      return;
    }
    for (int i = 0; i < ioControl.length; i++)
    {
      if (ioControl[i] < 0 || ioControl[i] > 1)
      {
        new ShowError("Error in PrototypeSensor.setDIO().\n"
          + "Parameter values must be 0 or 1.");
        return;
      }
    }
    synchronized (this)
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
   * Reads the sensor. Analog input data 0..1023 are returned in the given ain.
   */
  public void readAnalog(int[] ain)
  {
    int[] din = new int[6];
    read(ain, din);
  }

  /**
   * Reads the sensor. Digital input data 0 (corresponds to low state) or 1 
   * (corresponds to high state) is returned in the given din.
   * For digital channels defined as outputs -1 is returned.
   */
  public void readDigital(int[] din)
  {
    int[] ain = new int[5];
    read(ain, din);
  }

  /**
   * Sets the sampling period of the ADC (4..100 ms, default 4 ms).
   * @param period the new sampling period
   */
  public void setSamplingPeriod(int period)
  {
    sendData(SAMPLING, (byte)period);
  }

  /**
   * Reads the sensor. Analog input data 0..1023 are returned in the given ain,
   * digital input data (0, 1) in the given din. For digital channels defined as outputs
   * -1 is returned.
   */
  public void read(int[] ain, int[] din)
  {
    read(true, ain, din);
  }

  private boolean readSensor(int[] ain, int[] din)
  {
    if (robot == null || !robot.isConnected())
      return false;
    read(false, ain, din);
    return true;
  }

  private synchronized void read(boolean check, int[] ain, int[] din)
  {
    if (check)
      checkConnect();
    if (ain.length != 5)
    {
      new ShowError("Error in PrototypeSensor.read().\n"
        + "Parameter ain must have length 5.");
      return;
    }
    if (din.length != 6)
    {
      new ShowError("Error in PrototypeSensor.read().\n"
        + "Parameter din must have length 6.");
      return;
    }
    byte[] in = getData(REGISTERBASE, 11);
    // Convert to int
    int[] data = new int[11];
    for (int i = 0; i < 11; i++)
      data[i] = in[i] & 0xFF;

    // Analog inputs: base -> upper 8 bits, base+1 -> lower 2 bits
    for (int i = 0; i < 5; i++)
      ain[i] = 4 * data[2 * i] + data[2 * i + 1];

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

    if (LegoRobot.getDebugLevel() >= DEBUG_LEVEL_MEDIUM)
      DebugConsole.show("DEBUG: read() values:\n"
        + getAnalogValues(ain) + getDigitalValues(din));
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
      new ShowError("Error in PrototypeSensor.write().\n"
        + "Parameter dout must have length 6.");
      return;
    }
    for (int i = 0; i < 6; i++)
    {
      if (dout[i] < 0 || dout[i] > 1)
      {
        new ShowError("Error in PrototypeSensor.read().\n"
          + "Parameter values must be 0 or 1.");
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
    if (LegoRobot.getDebugLevel() >= DEBUG_LEVEL_MEDIUM)
      DebugConsole.show("DEBUG: write():\n"
        + value);
  }

  /**
   * Registers the given prototype listener.
   * @param prototypeListener the PrototypeListener to become registered.
   */
  public void addPrototypeListener(PrototypeListener prototypeListener)
  {
    this.prototypeListener = prototypeListener;
    if (!pst.isAlive())
      startPrototypeThread();
  }

  protected void startPrototypeThread()
  {
    pst.start();
  }

  protected void stopPrototypeThread()
  {
    if (pst.isAlive())
      pst.stopThread();
  }

  private void checkConnect()
  {
    if (robot == null)
      new ShowError("PrototypeSensor (port: " + getPortLabel()
        + ") is not a part of the NxtRobot.\n"
        + "Call addPart() to assemble it.");
  }

  private void delay(long timeout)
  {
    try
    {
      Thread.currentThread().sleep(timeout);
    }
    catch (InterruptedException ex)
    {
    }
  }
}
