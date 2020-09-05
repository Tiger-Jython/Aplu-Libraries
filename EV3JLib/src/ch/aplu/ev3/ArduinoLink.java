// ArduinoLink.java
// Direct mode
// DO NOT FORMAT THIS SOURCE TO KEEP THE LAYOUT OF THE CONNECTION DIAGRAM.

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
 * Class that represents I2C master-slave communication with the
 * Arduino microcontroller board. The arduino acts as slave with 
 * I2C slave address 0x04 as defined in the Arduino C program. <br><br>
<pre>
Connection diagram:
Use two resistors 82 kOhm to pull-up line A4 and A5 of Arduino.
The Ardunio may be powered from the EV3 by connecting >>>>>>>, otherwise
let the Arduino's 5V pin open. <br><b>(Never use the VIN pin, because it may have a voltage
higher than 5V if the Arduino is external powered.)</b><br><br>
The color on the left side corresponds to a standard 
Lego sensor cable.



    blue   6 [-----------x-------------------------] A4
                         |
 L                       |                                A
                         |
    yellow 5 [-------------------x-----------------] A5   R
 E                       |       |
                        ---     ---                       D
                       |82K|   |82k|
 G                     |   |   |   |                      U
                       |   |   |   |
                        ---     ---                       I
 O                       |       |
    green  4 [-----------x-------x---------->>>>>>>] 5V   N
                                                           
                                                          O
    red    3 [-------------------------------------] GND

  
    black  2 [


    white  1 [
</pre>

 */public class ArduinoLink extends Sensor
{
  /**
   * Creates a sensor instance connected to the given port.
   * @param port the port where the sensor is plugged-in
   */
  public ArduinoLink(SensorPort port)
  {
    super(port);
    partName = "arl" + (port.getId() + 1);
  }

  /**
   * Creates a sensor instance connected to port S1.
   */
  public ArduinoLink()
  {
    this(SensorPort.S1);
  }

  protected void init()
  {
    if (LegoRobot.getDebugLevel() >= SharedConstants.DEBUG_LEVEL_MEDIUM)
      DebugConsole.show("DEBUG: ArduinoLink.init() called (Port: "
        + getPortLabel() + ")");
  }

  protected void cleanup()
  {
    if (LegoRobot.getDebugLevel() >= SharedConstants.DEBUG_LEVEL_MEDIUM)
      DebugConsole.show("DEBUG: ArduinoLink.cleanup() called (Port: "
        + getPortLabel() + ")");
  }

  /**
   * Returns a one byte reply (int in range 0..255).
   * @return the reply from the Arduino when it gets the request
   */
  public int getReplyInt(int request)
  {
    checkConnect();
    int v = 0;
    try
    {
      v = Integer.parseInt(robot.sendCommand(partName + ".getReplyInt." + request));
    }
    catch (NumberFormatException ex)
    {
    }
    return v;
  }

  /**
   * Returns a integer array of size 16 containing the reply from the 
   * Arduino after sending the request. The request 
   * must be one byte only (unsigned in range 0..255), 
   * e.g. getReply(65) is same as getReply('A'). <br<br>
   *  
   * Integer are used to simulate unsigned bytes. A C-string-type reply is zero terminated.)
   */ 
  public synchronized void getReply(int request, int[] reply)
  {
    checkConnect();
    String value = robot.sendCommand(partName + ".getReplyArduino." + request);
    String[] data = value.split(":");
    for (int i = 0; i < 16; i++)
    {
      int v = 0;
      try
      {
        v = Integer.parseInt(data[i]);
      }
      catch (Exception ex)
      {
      }
      reply[i] = v;
    }
  }
  
   /**
   * Returns a null-terminated ASCII string reply.
   * The maximum byte reply length is 16 bytes (including null char).
   * If the null char is not found, returns 16 bytes converted to a Java string.
   * @return the reply from the Arduino when it gets the request
   */
  public String getReplyString(int request)
  {
    checkConnect();
    int[] data = new int[16];
    getReply(request, data);
    int k;
    for (k = 0; k < 16; k++)  
    {
      if (data[k] <= 0)   // Terminate when zero or illegal
          break;
    }
    String s = new String(data, 0, k);
    return s;
  }

  private void checkConnect()
  {
    if (robot == null)
      new ShowError("ArduinoLink (port: " + getPortLabel()
        + ") is not a part of the LegoRobot.\n"
        + "Call addPart() to assemble it.");
  }
}
