// Led.java

/*
 This software is part of the RaspiJLib library.
 It is Open Source Free Software, so you may
 - run the code for any purpose
 - study how the code works and adapt it to your needs
 - integrate all or parts of the code in your own programs
 - redistribute copies of the code
 - improve the code and release your improvements to the public
 However the use of the code is entirely your responsibility.
 */
package ch.aplu.raspi;

import java.awt.Color;
import ch.aplu.util.X11Color;

/**
 Class that represents a LED pair.
 */
public class Led extends Part
{

  private static Robot robot;
  private String device;
  private int id;

  static
  {
    robot = RobotInstance.getRobot();
  }

  /** Constant for id of front LED pair */
  public static int LED_FRONT = 0;
  /** Constant for id of left LED pair */
  public static int LED_LEFT = 1;
  /** Constant for id of rear LED pair */
  public static int LED_REAR = 2;
  /** Constant for id of right LED pair */
  public static int LED_RIGHT = 3;

  /**
   * Creates a Led instance with given ID.
   * IDs of the double LEDs: 0: front, 1: left side , 2: rear, 3: right side.
   * The following global constants are defined:
   * LED_FRONT = 0, LED_LEFT = 1, LED_REAR = 2, RED_RIGHT = 3.
   * @param id the LED identifier
   */
  public Led(int id)
  {
    this.id = id;
    device = "led" + id;
    Robot r = RobotInstance.getRobot();
    if (r == null)  // Defer setup() until robot is created
      RobotInstance.partsToRegister.add(this);
    else
      setup(r);
  }

  protected void setup(Robot robot)
  {
    robot.sendCommand(device + ".create");
    this.robot = robot;
  }

  /**
   * Sets the RGB color value of the two LEDs with current ID.
   * @param red the red color component 0..255
   * @param green the greed color component 0..255
   * @param blue the blue color component 0..255
   */
  public void setColor(int red, int green, int blue)
  {
    robot.sendCommand(device + ".setColor." + red + "." + green + "." + blue);
  }

  /**
   * Sets the RGB color value of the two LEDs with current ID.
   * @param color the RGB color
   */
  public void setColor(Color color)
  {
    setColor(color.getRed(), color.getGreen(), color.getBlue());
  }
  
  /**
   * Sets the X11 color name of the two LEDs with current ID.
   * See<br><br>http://en.wikipedia.org/wiki/X11_color_names
   * @param colorStr the X11 color string
   */
  public void setColor(String colorStr)
  {
    Color color = X11Color.toColor(colorStr);
    setColor(color.getRed(), color.getGreen(), color.getBlue());
  }

  /**
   * Sets the RGB color value of all LEDs.
   * @param red the red color component 0..255
   * @param green the greed color component 0..255
   * @param blue the blue color component 0..255
   */
  public static void setColorAll(int red, int green, int blue)
  {
    robot.sendCommand("led.setColorAll." + red + "." + green + "." + blue);
  }

  /**
   * Sets the RGB color value of all LEDs.
   * @param color the RGB color
   */
  public static void setColorAll(Color color)
  {
    setColorAll(color.getRed(), color.getGreen(), color.getBlue());
  }

  /**
   * Sets the X11 color name of all LEDs.
   * See<br><br>http://en.wikipedia.org/wiki/X11_color_names
   * @param colorStr the X11 color string
   */
  public static void setColorAll(String colorStr)
  {
    Color color = X11Color.toColor(colorStr);
    setColorAll(color.getRed(), color.getGreen(), color.getBlue());
  }

  /**
   * Turns off all 4 LED pairs.
   */
  public static void clearAll()
  {
    robot.sendCommand("led.clearAll");
  }

  private static void checkRobot()
  {
    if (robot == null)
      new ShowError("Fatal error while creating Led.\nCreate Robot instance first");
  }
}
