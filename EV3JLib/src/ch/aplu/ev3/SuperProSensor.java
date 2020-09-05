// SuperProSensor.java
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
 * Class that represents a SuperPro prototype sensor
 * (HiTechnic NXT SuperPro Prototype Board).<br><br>
 * The following I2C register layout is assumed:<br>
 * Device address 0x10<br>
 * 0x00-0x07 Version number<br>
 * 0x08-0x0F Manufacturer<br>
 * 0x10-0x17 Sensor type<br>
 * 0x42 Analog input channel 0, pin A0, upper 8bits<br>
 * 0x43 Analog input channel 0, pin A0, lower 2bits<br>
 * 0x44 Analog input channel 1, pin A1, upper 8bits<br>
 * 0x45 Analog input channel 1, pin A1, lower 2bits<br>
 * 0x46 Analog input channel 2, pin A2, upper 8bits<br>
 * 0x47 Analog input channel 2, pin A2, lower 2bits<br>
 * 0x48 Analog input channel 3, pin A3, upper 8bits<br>
 * 0x49 Analog input channel 3, pin A3, lower 2bits<br>
 * 0x4C Digital input channels, bits 0..7<br>
 * 0x4D Digitag output channels, bits 0..7<br>
 * 0x4E Digital control, bits 0..7, low: input (default), high: output<br>
 * 0x50 Strobe output, bits 0..3<br>
 * 0x51 LED control, bits 0..3<br>
 * 0x52 Analog output channel 0 mode<br>
 * 0x53 Analog output channel 0 frequency, upper 5bits<br>
 * 0x54 Analog output channel 0 frequency, lower 8bits<br>
 * 0x55 Analog output channel 0 voltage, upper 8bits (exotic!)<br>
 * 0x56 Analog output channel 0 voltage, lower 2bits (exotic!)<br>
 * 0x57 Analog output channel 1 mode<br>
 * 0x58 Analog output channel 1 frequency, upper 5bits<br>
 * 0x59 Analog output channel 1 frequency, lower 8bits<br>
 * 0x5A Analog output channel 1 voltage, upper 8bits (exotic!)<br>
 * 0x5B Analog output channel 1 voltage, lower 2bits (exotic!)<br>
 *
 * Analog inputs in range 0..3.3V, 10 bit (0..1023)<br>
 * Digital inputs/outputs 0/3.3V, pin B0..B7, max. 12mA per output (high or low)<br>
 * The inputs must stay within the 0 - 3.3V range. If this is not observed, 
 * damage will occur.	The inputs are 3.3V CMOS compatible: low: < 1.0V high: > 2.3V.<br><br>
 * Strobe outputs 0..3 general purpose digital output<br>
 * Strobe output WR: rectangle pulse of approx. 0.5 us length (3V approx.) at every
 * write action to digital out (B0..B7 ports)<br>
 * Strobe output RD: rectangle pulse of approx. 0.5 us length (3V approx.) at every
 * read action of digital in (B0..B7 ports)<br>
 * Analog output frequency 1..8191 Hz, analog output voltage 0..3.3V (0..1023)<br>
 * Analog output modes:<br>
 * 0: DC<br>
 * 1: sine wave<br>
 * 2: square wave<br>
 * 3: positive sawtooth<br>
 * 4: negative sawtooth<br>
 * 5: triangle wave<br>
 * 6: pulse width modulation<br><br>
 *
 * For PWM mode the output voltage is fixed to 3.3 V and
 * the voltage parameter defines the duty cycle r = voltage / frequency<br><br>
 *
 * The device is powered by the EV3 connector, so no other external power supply
 * is necessary.
 */
public class SuperProSensor extends Sensor
{

  /**
   * Creates a sensor instance connected to the given port.
   * (default: all digital channels as input)
   * @param port the port where the sensor is plugged-in
   */
  public SuperProSensor(SensorPort port)
  {
    super(port);
    partName = "sps" + (port.getId() + 1);
  }

  /**
   * Creates a sensor instance connected to port S1.
   * (default: all digital channels as input)
   */
  public SuperProSensor()
  {
    this(SensorPort.S1);
  }

  protected void init()
  {
    if (LegoRobot.getDebugLevel() >= SharedConstants.DEBUG_LEVEL_MEDIUM)
      DebugConsole.show("DEBUG: SuperProSensor.init() called (Port: "
        + getPortLabel() + ")");
  }

  protected void cleanup()
  {
    if (LegoRobot.getDebugLevel() >= SharedConstants.DEBUG_LEVEL_MEDIUM)
      DebugConsole.show("DEBUG: SuperProSensor.cleanup() called (Port: "
        + getPortLabel() + ")");
  }

   /**
   * Sets the direction of the 8 digital input/output channels.
   * The bit #n corresponds to channel #n; bit value 0: input, 1: output
   * (default: all digital channels as input)
   * @param mask the bit mask 
   */
  public void setDIOMask(int mask)
  {
    checkConnect();
    robot.sendCommand(partName + ".setDIOMask." + mask);
  }

  /**
   * Reads the sensor. Analog input data 0..1023 (corresponding to 0..3.3 V)
   * are returned in the given ain.
   * @param ain an integer array of length 4 where to get the analog values
   */
  public void readAnalog(int[] ain)
  {
    int[] din = new int[8];
    read(ain, din);
  }

  /**
   * Reads the sensor. Digital input data 0 (corresponds to low state, 0 V) or 1
   * (corresponds to high state, 3.3 V) is returned in the given din.
   * For digital channels defined as outputs -1 is returned.
   * @param din an integer array of length 8 where to get the digital values
   */
  public void readDigital(int[] din)
  {
    int[] ain = new int[4];
    read(ain, din);
  }

  /**
   * Reads the sensor. Analog input data 0..1023 (corresponding to 0..3.3 V)
   * are returned in the given ain, digital input data (0, 1) in the given din.
   * For digital channels defined as outputs -1 is returned.
   * @param ain an integer array of length 4 where to get the analog values
   * @param din an integer array of length 8 where to get the digital values
   */
  public synchronized void read(int[] ain, int[] din)
  {
    checkConnect();
    String value = robot.sendCommand(partName + ".readSuperPro");
    String[] data = value.split(":");  // an0:an1:an2:an3:dig
    for (int i = 0; i < 4; i++)
    {
      int v = 0;
      try
      {
        v = Integer.parseInt(data[i]);
      }
      catch (Exception ex)
      {
      }
      ain[i] = v;
    }

    int v = 0;
    try
    {
      v = Integer.parseInt(data[4]);
    }
    catch (Exception ex)
    {
    }
    int power = 1;
    for (int i = 0; i < 8; i++)
    {
      din[i] = (v & power) == 0 ? 0 : 1;
      power *= 2;
    }
  }

  /**
   * Writes the given byte  to the digital output channels.
   * Only channels set as output will be affected. Input channel bits are ignored.
   * @param value a byte (lower 8 bits of int) that holds the bit state: 0->low, 1->high. 
   * Only bit 0 to bit 7 are used.
   */
  public synchronized void writeByte(int value)
  {
    checkConnect();
    robot.sendCommand(partName + ".writeByte." + value);
  }
  
   /**
   * Writes the lower half byte (lower 4 bits) of the given value to the strobe output channels.
   * @param value the lower 4 bits are written to the strobe output channel.
   */
  public synchronized void writeStrobeByte(int value)
  {
    checkConnect();
    robot.sendCommand(partName + ".writeStrobeByte." + value);
  }
  
  /**
   * Turn the given onboard LED on.
   * When the program terminates, the current state of the LEDs is maintained.
   * ledControl = 0->both LEDs off<br>
   * ledControl = 1->red LED on, green LED off<br>
   * ledControl = 2->red LED off, green LED on<br>
   * ledControl = 3->both LEDs on<br>
   * @param ledControl 0..3
   */
  public synchronized void setLED(int ledControl)
  {
    checkConnect();
    robot.sendCommand(partName + ".setLED." + ledControl);
  }
  
  /**
   * Enables the digital output at port O0 with mode 0: DC voltage level.
   * Once the output is enabled, it remains active even when the program
   * terminates.
   * @param frequency the frequency in Hertz (1..8191)
   * @param voltage the peak-to-peak voltage (0..1021 corresponding to 0..3.3V)
   */
  public synchronized void setDCOut0(int frequency, int voltage)
  {
    checkConnect();
    robot.sendCommand(partName + ".setDCOut0." + frequency + "." + voltage);
  }

  /**
   * Enables the digital output at port O0 with mode 1: Sine wave.
   * Once the output is enabled, it remains active even when the program
   * terminates.
   * @param frequency the frequency in Hertz (1..8191)
   * @param voltage the peak-to-peak voltage (0..1021 corresponding to 0..3.3V)
   */
  public synchronized void setSineOut0(int frequency, int voltage)
  {
    checkConnect();
    robot.sendCommand(partName + ".setSineOut0." + frequency + "." + voltage);
  }

  /**
   * Enables the digital output at port O0 with mode 2: Square wave.
   * Once the output is enabled, it remains active even when the program
   * terminates.
   * @param frequency the frequency in Hertz (1..8191)
   * @param voltage the peak-to-peak voltage (0..1021 corresponding to 0..3.3V)
   */
  public synchronized void setSquareOut0(int frequency, int voltage)
  {
    checkConnect();
    robot.sendCommand(partName + ".setSquareOut0." + frequency + "." + voltage);
  }

  /**
   * Enables the digital output at port O0 with mode 3: Positive going sawtooth
   * Once the output is enabled, it remains active even when the program
   * terminates.
   * @param frequency the frequency in Hertz (1..8191)
   * @param voltage the peak-to-peak voltage (0..1021 corresponding to 0..3.3V)
   */
  public synchronized void setPosSawtoothOut0(int frequency, int voltage)
  {
    checkConnect();
    robot.sendCommand(partName + ".setPosSawtoothOut0." + frequency + "." + voltage);
  }

  /**
   * Enables the digital output at port O0 with mode 4: Negative going sawtooth
   * Once the output is enabled, it remains active even when the program
   * terminates.
   * @param frequency the frequency in Hertz (1..8191)
   * @param voltage the peak-to-peak voltage (0..1021 corresponding to 0..3.3V)
   */
  public synchronized void setNegSawtoothOut0(int frequency, int voltage)
  {
    checkConnect();
    robot.sendCommand(partName + ".setNegSawtoothOut0." + frequency + "." + voltage);
  }

  /**
   * Enables the digital output at port O0 with mode 5: Triangle wave.
   * Once the output is enabled, it remains active even when the program
   * terminates.
   * @param frequency the frequency in Hertz (1..8191)
   * @param voltage the peak-to-peak voltage (0..1021 corresponding to 0..3.3V)
   */
  public synchronized void setTriangleOut0(int frequency, int voltage)
  {
    checkConnect();
    robot.sendCommand(partName + ".setTriangleOut0." + frequency + "." + voltage);
  }
    
  /**
   * Enables the digital output at port O0 with mode 6: PWM voltage.
   * Once the output is enabled, it remains active even when the program
   * terminates. For PWM mode the output voltage is fixed to 3.3 V.
   * @param frequency the frequency in Hertz (1..8191)
   * @param duty used to set the duty cycle r = duty / frequency
   */
  public synchronized void setPWMOut0(int frequency, int duty)
  {
    checkConnect();
    robot.sendCommand(partName + ".setPWMOut0." + frequency + "." + duty);
  }
  
  /**
   * Enables the digital output at port O1 with mode 0: DC voltage level.
   * Once the output is enabled, it remains active even when the program
   * terminates.
   * @param frequency the frequency in Hertz (1..8191)
   * @param voltage the peak-to-peak voltage (0..1021 corresponding to 0..3.3V)
   */
  public synchronized void setDCOut1(int frequency, int voltage)
  {
    checkConnect();
    robot.sendCommand(partName + ".setDCOut1." + frequency + "." + voltage);
  }

  /**
   * Enables the digital output at port O1 with mode 1: Sine wave.
   * Once the output is enabled, it remains active even when the program
   * terminates.
   * @param frequency the frequency in Hertz (1..8191)
   * @param voltage the peak-to-peak voltage (0..1021 corresponding to 0..3.3V)
   */
  public synchronized void setSineOut1(int frequency, int voltage)
  {
    checkConnect();
    robot.sendCommand(partName + ".setSineOut1." + frequency + "." + voltage);
  }

  /**
   * Enables the digital output at port O1 with mode 2: Square wave.
   * Once the output is enabled, it remains active even when the program
   * terminates.
   * @param frequency the frequency in Hertz (1..8191)
   * @param voltage the peak-to-peak voltage (0..1021 corresponding to 0..3.3V)
   */
  public synchronized void setSquareOut1(int frequency, int voltage)
  {
    checkConnect();
    robot.sendCommand(partName + ".setSquareOut1." + frequency + "." + voltage);
  }

  /**
   * Enables the digital output at port O1 with mode 3: Positive going sawtooth
   * Once the output is enabled, it remains active even when the program
   * terminates.
   * @param frequency the frequency in Hertz (1..8191)
   * @param voltage the peak-to-peak voltage (0..1021 corresponding to 0..3.3V)
   */
  public synchronized void setPosSawtoothOut1(int frequency, int voltage)
  {
    checkConnect();
    robot.sendCommand(partName + ".setPosSawtoothOut1." + frequency + "." + voltage);
  }

  /**
   * Enables the digital output at port O1 with mode 4: Negative going sawtooth
   * Once the output is enabled, it remains active even when the program
   * terminates.
   * @param frequency the frequency in Hertz (1..8191)
   * @param voltage the peak-to-peak voltage (0..1021 corresponding to 0..3.3V)
   */
  public synchronized void setNegSawtoothOut1(int frequency, int voltage)
  {
    checkConnect();
    robot.sendCommand(partName + ".setNegSawtoothOut1." + frequency + "." + voltage);
  }

  /**
   * Enables the digital output at port O1 with mode 5: Triangle wave.
   * Once the output is enabled, it remains active even when the program
   * terminates.
   * @param frequency the frequency in Hertz (1..8191)
   * @param voltage the peak-to-peak voltage (0..1021 corresponding to 0..3.3V)
   */
  public synchronized void setTriangleOut1(int frequency, int voltage)
  {
    checkConnect();
    robot.sendCommand(partName + ".setTriangleOut1." + frequency + "." + voltage);
  }
    
  /**
   * Enables the digital output at port O1 with mode 6: PWM voltage.
   * Once the output is enabled, it remains active even when the program
   * terminates. For PWM mode the output voltage is fixed to 3.3 V.
   * @param frequency the frequency in Hertz (1..8191)
   * @param duty used to set the duty cycle r = duty / frequency
   */
  public synchronized void setPWMOut1(int frequency, int duty)
  {
    checkConnect();
    robot.sendCommand(partName + ".setPWMOut1." + frequency + "." + duty);
  }

   /**
   * Returns the product identifier (if available).
   * @return the product identifier (PID)
   */
  public String getProductID()
  {
    checkConnect();
    return robot.sendCommand(partName + ".getProductID");
  }

  /**
   * Returns the sensor version number (if available).
   * @return the sensor version number
   */
  public String getVersion()
  {
    checkConnect();
    return robot.sendCommand(partName + ".getVersion");
  }

  private void checkConnect()
  {
    if (robot == null)
      new ShowError("SuperProSensor (port: " + getPortLabel()
        + ") is not a part of the NxtRobot.\n"
        + "Call addPart() to assemble it.");
  }
}
