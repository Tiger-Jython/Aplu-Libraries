// Led.java

/*
 This software is part of the MbRobotJLib library.
 It is Open Source Free Software, so you may
 - run the code for any purpose
 - study how the code works and adapt it to your needs
 - integrate all or parts of the code in your own programs
 - redistribute copies of the code
 - improve the code and release your improvements to the public
 However the use of the code is entirely your responsibility.
 */
package ch.aplu.mbrobotsim;

import ch.aplu.util.X11Color;
import ch.aplu.jgamegrid.*;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Transparency;
import java.awt.image.BufferedImage;

/**
 Class that represents a LED.
 */
public class Led
{

  private static final Location[] ledPositions =
  {
    new Location(25, -9),  // Left
    new Location(25,  9)   // Right
  };

  private static SingleLed[] sLeds = new SingleLed[2];

  static
  {
    for (int id = 0; id < 2; id++)
      sLeds[id] = null;
  }

  private int id;

  /**
   * Creates a Led instance with given ID.
   * @param id the LED identifier
   */
  public Led(int id)
  {
    this.id = id;
  }

  /**
   * Sets the RGB color value of the two LEDs with current ID.
   * @param red the red color component 0..255
   * @param green the greed color component 0..255
   * @param blue the blue color component 0..255
   */
  public void setColor(int red, int green, int blue)
  {
    setColor(new Color(red, green, blue));
  }

  /**
   * Sets the RGB color value of the two LEDs with current ID.
   * @param color the RGB color
   */
  public void setColor(Color color)
  {
    RobotInstance.checkRobot();
    MbRobot robot = RobotInstance.getRobot();
    if (sLeds[id] != null)
      robot.removePart(sLeds[id]);
    if (color.equals(Color.black))
      sLeds[id] = null;
    else
    {
      BufferedImage bi = createLedImage(color);
      sLeds[id] = new SingleLed(bi, ledPositions[id]);
    }
  }

  /**
   * Sets the X11 color name of the two LEDs with current ID.
   * @param color the X11 name of the color
   */
  public void setColor(String colorStr)
  {
    setColor(X11Color.toColor(colorStr));
  }

  /**
   * Sets the RGB color value of all LEDs.
   * @param red the red color component 0..255
   * @param green the greed color component 0..255
   * @param blue the blue color component 0..255
   */
  public static void setColorAll(int red, int green, int blue)
  {
    setColorAll(new Color(red, green, blue));
  }

  /**
   * Sets the RGB color value of all LEDs.
   * @param color the RGB color
   */
  public static void setColorAll(Color color)
  {
    RobotInstance.checkRobot();
    MbRobot robot = RobotInstance.getRobot();
    BufferedImage bi = createLedImage(color);
    for (int id = 0; id < 2; id++)
    {
      if (sLeds[id] != null)
        robot.removePart(sLeds[id]);
      if (color.equals(Color.black))
        sLeds[id] = null;
      else
        sLeds[id] = new SingleLed(bi, ledPositions[id]);
    }
  }

  /**
   * Sets the X11 color name of all LEDs.
   * @param colorStr the X11 name of the color
   */
  public static void setColorAll(String colorStr)
  {
    setColorAll(X11Color.toColor(colorStr));
  }

  /**
   * Turns off all LEDs.
   */
  public static void clearAll()
  {
    setColorAll(Color.black);
  }

  private static BufferedImage createLedImage(Color c)
  {
    int size = 8;
    BufferedImage bi = new BufferedImage(size, size, Transparency.BITMASK);
    Graphics2D g2D = bi.createGraphics();

    // Set background color
    g2D.setColor(c);
    g2D.fillOval(0, 0, size, size);

    return bi;
  }

}
