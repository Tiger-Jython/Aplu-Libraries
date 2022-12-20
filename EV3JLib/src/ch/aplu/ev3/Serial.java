// Serial.java
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
 * Class that represents a serial port (UART, RS232).
 */
public class Serial extends Sensor
{
   private int baudrate;
  
  /**
   * Creates a sensor instance connected to sensor port 1 with default baudrate = 9600 b.
   */
  public Serial()
  {
    this(SensorPort.S1, 9600);
  }

  /**
   * Creates a sensor instance connected to the given port with default baudrate = 9600 b.
   * @param port the port where the sensor is plugged-in
   */
  public Serial(SensorPort port)
  {
    this(port, 9600);
  }
  
  /**
    Creates an abstraction of the serial port (UART) at given
    sensor port with given baudrate  
    (8 data bits, 1 stop bit, no parity, no flow control)
   */
  public Serial(SensorPort port, int baudrate)
  {
    super(port);
    partName = "ser" + (port.getId() + 1);
    setup(baudrate);
  }

  private void setup(int baudrate)
  {
    this.baudrate = baudrate;
    switch (baudrate)
    {
      case 300:
        return;
      
      case 9600:
        return;
    }  
    new ShowError("Serial: baudrate not one of the legal values");
  }
    
  protected void init()
  {
    if (LegoRobot.getDebugLevel() >= SharedConstants.DEBUG_LEVEL_MEDIUM)
      DebugConsole.show("DEBUG: Serial.init() called (Port: "
        + getPortLabel() + ")");
    robot.sendCommand(partName + ".setup." + baudrate);
  }

  protected void cleanup()
  {
    if (LegoRobot.getDebugLevel() >= SharedConstants.DEBUG_LEVEL_MEDIUM)
      DebugConsole.show("DEBUG: Serial.cleanup() called (Port: "
        + getPortLabel() + ")");
  }

 /**
   Retrieves a line of string data from the serial port. 
   Blocks until a <cr> is received. Received data is captured 
   in a ring buffer of 1024 bytes and lines are returned one by one at each call.
   If the buffer overflows,  older data is overwritten.  
   */
  public String readline(int timeout)
  {
    checkConnect();
    String s = robot.sendCommand(partName + ".readline." + timeout);
    if (s.length() == 1 && s.charAt(0) == '0')
      return null;
    return s;
  }
  
  /**
    Writes given string to device.
    Returns the number of bytes successfully transmitted; 0
    if sending failed
   */
  public int write(String s)
  {
    checkConnect();
    int v = 0;
    try
    {
      v = Integer.parseInt(robot.sendCommand(partName + ".write.s" + s));
    }
    catch (NumberFormatException ex)
    {
    }
    return v;
  }
  
  private void checkConnect()
  {
    if (robot == null)
      new ShowError("Serial (port: " + getPortLabel()
        + ") is not a part of the LegoRobot.\n"
        + "Call addPart() to assemble it.");
  }

}
