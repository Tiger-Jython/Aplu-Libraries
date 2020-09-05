// HTColorSensor.java
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

import java.awt.Color;

/**
 * Class that represents a color sensor (HiTechnic Color Sensor in RGB mode).
 */
public class HTColorSensor extends Sensor
{
  /**
   * Creates a sensor instance connected to the given port.
   * @param port the port where the sensor is plugged-in
   */
  public HTColorSensor(SensorPort port)
  {
    super(port);
    partName = "htcs" + (port.getId() + 1);
  }

  /**
   * Creates a sensor instance connected to port S1.
   */
  public HTColorSensor()
  {
    this(SensorPort.S1);
  }

  protected void init()
  {
    if (LegoRobot.getDebugLevel() >= SharedConstants.DEBUG_LEVEL_MEDIUM)
      DebugConsole.show("DEBUG: HTColorSensor.init() called (Port: "
        + getPortLabel() + ")");
  }

  protected void cleanup()
  {
    if (LegoRobot.getDebugLevel() >= SharedConstants.DEBUG_LEVEL_MEDIUM)
      DebugConsole.show("DEBUG: HTColorSensor.cleanup() called (Port: "
        + getPortLabel() + ")");
  }

  private int getValue(boolean check)
  {
    if (check)
      checkConnect();
    int v = 0;
    try
    {
      v = Integer.parseInt(robot.sendCommand(partName + ".getColorInt"));
    }
    catch (NumberFormatException ex)
    {
    }
    return v;
  }

  /**
   * Polls the sensor.
   * Returns the java.awt.color of the color.
   * @return the current color
   */
  public Color getColor()
  {
    return new Color(getColorInt());
  }

  /**
   * Polls the sensor.
   * Returns the color as integer value.
   * Red in bits 0..6, Green in bits 7..15, Blue in bits 16..23, 1 in bits 24..31.
   * @return color int
   */
  public int getColorInt()
  {
    return getValue(true);
  }

  /**
   * Returns the color ID (0..17).<br>
   ,* 0: black<br>
   * 1,2,3; blue<br>
   * 4, 5: green<br>
   * 4, 5: green<br>
   * 6, 7: yellow<br>
   * 8, 9, 10: red<br>
   * 11-16: light red<br>
   * 17: white<br>
   * (see www.hitechnic.com for color chart).
   * @return color identifier
   */
  public int getColorID()
  {
    checkConnect();
    int v = 0;
    try
    {
      v = Integer.parseInt(robot.sendCommand(partName + ".getColorID"));
    }
    catch (NumberFormatException ex)
    {
    }
    return v;
  }

  /**
   * Checks if given color lies within given color cube.
   * @param color the color to check
   * @param colorCube an integer array of 6 boundary values in the order:
   * red_min, red_max, green_min, green_max, blue_min, blue_max
   * @return true, if the color is within the color cube; otherwise false
   */
  public static boolean inColorCube(Color color, int[] colorCube)
  {
    if (color.getRed() >= colorCube[0] && color.getRed() <= colorCube[1]
      && color.getGreen() >= colorCube[2] && color.getGreen() <= colorCube[3]
      && color.getBlue() >= colorCube[4] && color.getBlue() <= colorCube[5])
      return true;
    return false;
  }

  private void checkConnect()
  {
    if (robot == null)
      new ShowError("HTColorSensor (port: " + getPortLabel()
        + ") is not a part of the EV3Robot.\n"
        + "Call addPart() to assemble it.");
  }
}
