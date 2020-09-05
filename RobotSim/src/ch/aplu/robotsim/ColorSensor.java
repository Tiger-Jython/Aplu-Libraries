// ColorSensor.java

/*
 This software is part of the RobotSim library.
 It is Open Source Free Software, so you may
 - run the code for any purpose
 - study how the code works and adapt it to your needs
 - integrate all or parts of the code in your own programs
 - redistribute copies of the code
 - improve the code and release your improvements to the public
 However the use of the code is entirely your responsibility.

 Author: Aegidius Pluess, www.aplu.ch
 */
package ch.aplu.robotsim;

import ch.aplu.jgamegrid.*;
import java.awt.Color;
import javax.swing.JOptionPane;

/**
 * Class that represents a color sensor.
 */
public class ColorSensor extends Part
{
  /**
   * Color cubes for detecting color labels.
   * 6 user adaptable int arrays with red_min, red_max, green_min, green_max, blue_min, blue_max
   * for the colors Black, Blue, Green, Yellow, Red, White.<br>
   * Example: Redefine blue cube:<br>
   *<code>
   * ColorSensor.colorCubes[1] = new int[]{0, 2, 0, 2, 0, 2};
   *</code>
   */
  public static int[][] colorCubes =
  {
    new int[]
    {
      0, 50, 0, 50, 0, 50
    }, // BLACK
    new int[]
    {
      0, 50, 0, 50, 205, 255
    }, // BLUE
    new int[]
    {
      0, 50, 205, 255, 0, 50
    }, // GREEN
    new int[]
    {
      205, 255, 205, 255, 0, 50
    }, // YELLOW
    new int[]
    {
      205, 255, 0, 50, 0, 50
    }, // RED
    new int[]
    {
      205, 255, 205, 255, 205, 255
    }  // WHITE
  };

  private static final Location pos1 = new Location(8, 7);
  private static final Location pos2 = new Location(8, -7);
  private static final Location pos3 = new Location(8, 0);
  private static final Location pos4 = new Location(-35, 0);
  private SensorPort port;

  /**
   * Creates a sensor instance connected to the given port.
   * The port selection determines the position of the sensor:
   * S1: right; S2: left, S3: middle, S4: rear-middle.
   * @param port the port where the sensor is plugged-in
   */
  public ColorSensor(SensorPort port)
  {
    super("sprites/colorsensor"
      + (port == SensorPort.S1 ? ".gif"
      : (port == SensorPort.S2 ? ".gif"
      : (port == SensorPort.S3 ? ".gif" : "_rear.gif"))),
      port == SensorPort.S1 ? pos1
      : (port == SensorPort.S2 ? pos2
      : (port == SensorPort.S3 ? pos3 : pos4)));
    this.port = port;
  }

  /**
   * Creates a sensor instance connected to port S1.
   */
  public ColorSensor()
  {
    this(SensorPort.S1);
  }

  protected void cleanup()
  {
  }

  /**
   * Returns the brightness (scaled intensity in the HSB color model)
   * detected by the light sensor at the current location.
   * Calls Thread.sleep(1) to prevent CPU overload in close polling loops.
   * @return the brightness of the background color at the current location
   * (0..1000, 0: dark, 1000: bright).
   */
  public int getLightValue()
  {
    checkPart();
    Tools.delay(1);
    Color c = getBackground().getColor(getLocation());
    float[] hsb = new float[3];
    Color.RGBtoHSB(c.getRed(), c.getGreen(), c.getBlue(), hsb);
    return (int)(1000 * hsb[2]);
  }

  /**
   * Returns the color detected by the color sensor at the current location.
   * Calls Thread.sleep(1) to prevent CPU overload in close polling loops.
   * @return the background color at the current location
   */
  public Color getColor()
  {
    checkPart();
    Tools.delay(1);
    return getBackground().getColor(getLocation());
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
   * Returns the port of the sensor.
   * @return the sensor port
   */
  public SensorPort getPort()
  {
    return port;
  }

  private void checkPart()
  {
    if (robot == null)
    {
      JOptionPane.showMessageDialog(null,
        "ColorSensor is not part of the LegoRobot.\n"
        + "Call addPart() to assemble it.",
        "Fatal Error", JOptionPane.ERROR_MESSAGE);
      if (GameGrid.getClosingMode() == GameGrid.ClosingMode.TerminateOnClose
        || GameGrid.getClosingMode() == GameGrid.ClosingMode.AskOnClose)
        System.exit(1);
      if (GameGrid.getClosingMode() == GameGrid.ClosingMode.DisposeOnClose)
        throw new RuntimeException("ColorSensor is not part of the LegoRobot.\n"
          + "Call addPart() to assemble it.");
    }
  }
}
