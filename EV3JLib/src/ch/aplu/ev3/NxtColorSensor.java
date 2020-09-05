// NxtColorSensor.java
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
 * Class that represents a color sensor (Lego NXT color sensor).
 */
public class NxtColorSensor extends Sensor
{
  /**
   * Creates a sensor instance connected to the given port.
   * @param port the port where the sensor is plugged-in
   */
  public NxtColorSensor(SensorPort port)
  {
    super(port);
    partName = "_cs" + (port.getId() + 1);
  }

  /**
   * Creates a sensor instance connected to port S1.
   */
  public NxtColorSensor()
  {
    this(SensorPort.S1);
  }

  protected void init()
  {
    if (LegoRobot.getDebugLevel() >= SharedConstants.DEBUG_LEVEL_MEDIUM)
      DebugConsole.show("DEBUG: NxtColorSensor.init() called (Port: "
        + getPortLabel() + ")");
  }

  protected void cleanup()
  {
    if (LegoRobot.getDebugLevel() >= SharedConstants.DEBUG_LEVEL_MEDIUM)
      DebugConsole.show("DEBUG: NxtColorSensor.cleanup() called (Port: "
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
   * Returns the intensity of the detected light.
   * @return light intensity (0..1023)
   */
  public int getLightValue()
  {
    checkConnect();
    int v = 0;
    try
    {
      v = Integer.parseInt(robot.sendCommand(partName + ".getLightValue"));
    }
    catch (NumberFormatException ex)
    {
    }
    return v;
  }

  /**
   * Turns the red, green or blue LED on or all off.
   * @param colorValue 0: red, 1: green, 2: blue, -1: all off
   */
  public void setFloodlight(int colorValue)
  {
    checkConnect();
    robot.sendCommand(partName + ".setFloodlight." + colorValue);
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
      new ShowError("NxtColorSensor (port: " + getPortLabel()
        + ") is not a part of the EV3Robot.\n"
        + "Call addPart() to assemble it.");
  }

}
