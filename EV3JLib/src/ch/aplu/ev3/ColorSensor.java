// ColorSensor.java
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
 * Class that represents a EV3 color sensor.
 */
public class ColorSensor extends Sensor
{
  /**
   * Color cubes for detecting color labels.
   * 6 user adaptable int arrays with red_min, red_max, green_min, green_max, blue_min, blue_max
   * for the colors black, blue, green, yellow, red, white with indices 0, 1, 2, 3, 5, 6.<br>
   * Example: Redefine blue cube:<br>
   *<code>
   * ColorSensor.colorCubes[1] = new int[]{10, 20, 30, 40, 50, 60};
   *</code>
   */
  public static int[][] colorCubes =
  {
    new int[]
    {
      -1, -1, -1, -1, -1, -1
    },
    new int[]
    {
      -1, -1, -1, -1, -1, -1
    },
    new int[]
    {
      -1, -1, -1, -1, -1, -1
    },
    new int[]
    {
      -1, -1, -1, -1, -1, -1
    },
    new int[]
    {
      -1, -1, -1, -1, -1, -1
    },
    new int[]
    {
      -1, -1, -1, -1, -1, -1
    }
  };

  /**
   * Creates a sensor instance connected to the given port.
   * @param port the port where the sensor is plugged-in
   */
  public ColorSensor(SensorPort port)
  {
    super(port);
    partName = "cs" + (port.getId() + 1);
    EV3Properties props = LegoRobot.getProperties();
    String[] blackCubeStr = props.getStringValue("ColorCubeBlack").split(",");
    for (int i = 0; i < 6; i++)
      if (colorCubes[0][i] == -1)
        colorCubes[0][i] = Integer.parseInt(blackCubeStr[i].trim());
    String[] blueCubeStr = props.getStringValue("ColorCubeBlue").split(",");
    for (int i = 0; i < 6; i++)
      if (colorCubes[1][i] == -1)
        colorCubes[1][i] = Integer.parseInt(blueCubeStr[i].trim());
    String[] greenCubeStr = props.getStringValue("ColorCubeGreen").split(",");
    for (int i = 0; i < 6; i++)
      if (colorCubes[2][i] == -1)
        colorCubes[2][i] = Integer.parseInt(greenCubeStr[i].trim());
    String[] yellowCubeStr = props.getStringValue("ColorCubeYellow").split(",");
    for (int i = 0; i < 6; i++)
      if (colorCubes[3][i] == -1)
        colorCubes[3][i] = Integer.parseInt(yellowCubeStr[i].trim());
    String[] redCubeStr = props.getStringValue("ColorCubeRed").split(",");
    for (int i = 0; i < 6; i++)
      if (colorCubes[4][i] == -1)
        colorCubes[4][i] = Integer.parseInt(redCubeStr[i].trim());
    String[] whiteCubeStr = props.getStringValue("ColorCubeWhite").split(",");
    for (int i = 0; i < 6; i++)
      if (colorCubes[5][i] == -1)
        colorCubes[5][i] = Integer.parseInt(whiteCubeStr[i].trim());
  }

  /**
   * Creates a sensor instance connected to port S1.
   */
  public ColorSensor()
  {
    this(SensorPort.S1);
  }

  protected void init()
  {
    if (LegoRobot.getDebugLevel() >= SharedConstants.DEBUG_LEVEL_MEDIUM)
      DebugConsole.show("DEBUG: ColorSensor.init() called (Port: "
        + getPortLabel() + ")");
  }

  protected void cleanup()
  {
    if (LegoRobot.getDebugLevel() >= SharedConstants.DEBUG_LEVEL_MEDIUM)
      DebugConsole.show("DEBUG: ColorSensor.cleanup() called");
  }

  /**
   * Initilize color cubes to default value (from properties file).
   */
  public static void initColorCubes()
  {
    EV3Properties props = LegoRobot.getProperties();
    String[] blackCubeStr = props.getStringValue("ColorCubeBlack").split(",");
    for (int i = 0; i < 6; i++)
      colorCubes[0][i] = Integer.parseInt(blackCubeStr[i].trim());
    String[] blueCubeStr = props.getStringValue("ColorCubeBlue").split(",");
    for (int i = 0; i < 6; i++)
      colorCubes[1][i] = Integer.parseInt(blueCubeStr[i].trim());
    String[] greenCubeStr = props.getStringValue("ColorCubeGreen").split(",");
    for (int i = 0; i < 6; i++)
      colorCubes[2][i] = Integer.parseInt(greenCubeStr[i].trim());
    String[] yellowCubeStr = props.getStringValue("ColorCubeYellow").split(",");
    for (int i = 0; i < 6; i++)
      colorCubes[3][i] = Integer.parseInt(yellowCubeStr[i].trim());
    String[] redCubeStr = props.getStringValue("ColorCubeRed").split(",");
    for (int i = 0; i < 6; i++)
      colorCubes[4][i] = Integer.parseInt(redCubeStr[i].trim());
    String[] whiteCubeStr = props.getStringValue("ColorCubeWhite").split(",");
    for (int i = 0; i < 6; i++)
      colorCubes[5][i] = Integer.parseInt(whiteCubeStr[i].trim());
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
   * Returns a integer number ID for the current color reading.
   * The colors are checked to be inside the public static colorCubes array. 
   * This array may be adapted to your illumination and color environment.<br>
   * ID 0: Undefined<br>
   * ID 1: Black<br>
   * ID 2: Blue<br>
   * ID 3: Green<br>
   * ID 4: Yellow<br>
   * ID 5: Red<br>
   * ID 6: White<br>
   * @return a integer code for the currently detected color (0..6)
   */
  public int getColorID()
  {
    for (int i = 0; i < 6; i++)
    {
      if (inColorCube(getColor(), colorCubes[i]))
        return i + 1;
    }
    return 0;
  }

  /**
   * Returns one of the enums of ColorLabel.<br>
   * UNDEFINED<br>
   * BLACK<br>
   * BLUE<br>
   * GREEN<br>
   * YELLOW<br>
   * RED<br>
   * WHITE<br>
   * The colors are checked to be inside the public static colorCubes array. 
   * This array may be adapted to your illumination and color environment.
   * @return an ColorLabel enum value for the currently detected color.
   */
  public ColorLabel getColorLabel()
  {
    return ColorLabel.values()[getColorID()];
  }

  /**
   * Returns the name of the enums of ColorLabel.<br>
   * UNDEFINED<br>
   * BLACK<br>
   * BLUE<br>
   * GREEN<br>
   * YELLOW<br>
   * RED<br>
   * WHITE<br>
   * The colors are checked to be inside the public static colorCubes array. 
   * This array may be adapted to your illumination and color environment.
   * @return an ColorLabel enum name for the currently detected color.
   */
  public String getColorStr()
  {
    return getColorLabel().toString();
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

  /**
   * Turns the red, green or blue LED on or all off.
   * @param colorValue 0: red, 1: green, 2: blue, -1: all off
   */
  public void setFloodlight(int colorValue)
  {
    checkConnect();
    robot.sendCommand(partName + ".setFloodlight." + colorValue);
  }

  private void checkConnect()
  {
    if (robot == null)
      new ShowError("ColorSensor (port: " + getPortLabel()
        + ") is not a part of the EV3Robot.\n"
        + "Call addPart() to assemble it.");
  }
}
