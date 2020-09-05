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
package ch.aplu.raspisim;

import ch.aplu.util.X11Color;
import ch.aplu.jgamegrid.*;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Transparency;
import java.awt.image.BufferedImage;
import java.util.ArrayList;

/**
 Class that represents a LED pair.
 */
public class Led
{
  private boolean debug = false;

  // ------------------- class BlinkerThread ----------------------
  class BlinkerThread extends Thread
  {
    private Led led;
    private String onColor;
    private String offColor;
    private int onTime;
    private int offTime;
    private int count;
    private boolean isAlive;
    private boolean isRunning;

    public BlinkerThread(Led led, String onColor, String offColor,
      int onTime, int offTime, int count)
    {
      if (debug)
        System.out.println("Creating BlinkerThread...");
      this.led = led;
      this.onColor = onColor;
      this.offColor = offColor;
      this.onTime = onTime;
      this.offTime = offTime;
      this.count = count;
      isAlive = true;
      setDaemon(true);
      start();
    }

    public void run()
    {
      int nb = 0;
      isRunning = true;
      while (isRunning)
      {
        led.setColor(onColor);
        long startTime = System.nanoTime();
        while (System.nanoTime() - startTime < onTime * 1000000
          && isRunning)
          Tools.delay(1);
        if (!isRunning)
          break;

        led.setColor(offColor);
        startTime = System.nanoTime();
        while (System.nanoTime() - startTime < offTime * 1000000
          && isRunning)
          Tools.delay(1);

        if (!isRunning)
          break;

        nb += 1;
        if (nb == count)
          isRunning = false;
      }
      led.setColor(0, 0, 0);
      led.blinkerThread = null;
      isAlive = false;
      if (debug)
        System.out.println("BlinkerThread finished");
    }

    public void terminate()
    {
      if (debug)
        System.out.println("Calling BlinkerThread.terminate()");
      isRunning = false;
      while (isAlive) // Wait until thread is finished
        Tools.delay(1);
    }
  }
// ---------- end of inner class ---------------

  /** Constant for id of front LED pair */
  public static int LED_FRONT = 0;
  /** Constant for id of left LED pair */
  public static int LED_LEFT = 1;
  /** Constant for id of rear LED pair */
  public static int LED_REAR = 2;
  /** Constant for id of right LED pair */
  public static int LED_RIGHT = 3;

  private static final Location[] sensorPos =
  {
    new Location(23, 4),
    new Location(23, -4),
    new Location(5, -8),
    new Location(16, -8),
    new Location(-4, 4),
    new Location(-4, -4),
    new Location(16, 8),
    new Location(5, 8),
  };

  private static SingleLed[] sLeds = new SingleLed[8];

  static
  {
    for (int id = 0; id < 4; id++)
    {
      sLeds[2 * id] = null;
      sLeds[2 * id + 1] = null;
    }
  }

  private int id;
  private BlinkerThread blinkerThread = null;
  private static ArrayList<Led> blinkerLeds = new ArrayList<Led>();

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
    Robot robot = RobotInstance.getRobot();
    if (sLeds[2 * id] != null)
    {
      robot.removePart(sLeds[2 * id]);
      robot.removePart(sLeds[2 * id + 1]);
    }
    if (color.equals(Color.black))
    {
      sLeds[2 * id] = null;
      sLeds[2 * id + 1] = null;
    }
    else
    {
      BufferedImage bi = createLedImage(color);
      sLeds[2 * id] = new SingleLed(bi, sensorPos[2 * id]);
      sLeds[2 * id + 1] = new SingleLed(bi, sensorPos[2 * id + 1]);
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
    Robot robot = RobotInstance.getRobot();
    BufferedImage bi = createLedImage(color);
    for (int id = 0; id < 4; id++)
    {
      if (sLeds[2 * id] != null)
      {
        robot.removePart(sLeds[2 * id]);
        robot.removePart(sLeds[2 * id + 1]);
      }
      if (color.equals(Color.black))
      {
        sLeds[2 * id] = null;
        sLeds[2 * id + 1] = null;
      }
      else
      {
        sLeds[2 * id] = new SingleLed(bi, sensorPos[2 * id]);
        sLeds[2 * id + 1] = new SingleLed(bi, sensorPos[2 * id + 1]);
      }
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
   * Turns off all 4 LED pairs.
   */
  public static void clearAll()
  {
    for (Led led : blinkerLeds)
      led.stopBlinker();
    setColorAll(Color.black);
  }

  /**
   * Starts blinking with given onColor and offColor. The blinking period
   * is offTime + onTime. May be stopped by calling stopBlinker(). If blocking is False, the
   * function returns immediately while the blinking goes on. The blinking is stopped by setColor().
   * @param onColor the color in on state. Must be X11 color string
   * @param offColor the color in off state. Must be X11 color string
   * @param onTime the time in ms in on state
   * @param offTime the time in ms in off state
   * @param count the total number of on states; 0 for endlessly
   * @param blocking if true, the method blocks until the blinker has finished; otherwise
   * it returns immediately
   */
  public void startBlinker(String onColor, String offColor, int onTime,
    int offTime, int count, boolean blocking)
  {
    if (blinkerThread != null)
      stopBlinker();
    blinkerThread = new BlinkerThread(this, onColor, offColor,
      onTime, offTime, count);
    blinkerLeds.add(this);
    if (blocking)
      while (isBlinkerAlive());
  }

  /**
   * Same as startBlinker() with blocking = false.
   * @param onColor the color in on state. Must be X11 color string
   * @param offColor the color in off state. Must be X11 color string
   * @param onTime the time in ms in on state
   * @param offTime the time in ms in off state
   * @param count the total number of on states; 0 for endlessly
   */
  public void startBlinker(String onColor, String offColor, int onTime,
    int offTime, int count)
  {
    startBlinker(onColor, offColor, onTime, offTime, count, false);
  }

  /**
   * Same as startBlinker() with count = 0 and blocking = false.
   * @param onColor the color in on state. Must be X11 color string
   * @param offColor the color in off state. Must be X11 color string
   * @param onTime the time in ms in on state
   * @param offTime the time in ms in off state
   */
  public void startBlinker(String onColor, String offColor, int onTime,
    int offTime)
  {
    startBlinker(onColor, offColor, onTime, offTime, 0, false);
  }

  /**
   Stops blinking and turns the led off.
   */
  public void stopBlinker()
  {
    if (debug)
      System.out.println("Calling Led.stopBlinker()");
    if (blinkerThread != null)
      blinkerThread.terminate();
  }

  /**
   @return: True, if the blinker is displaying; otherwise False
   */
  public boolean isBlinkerAlive()
  {
    Tools.delay(1);
    return (blinkerThread != null);
  }

  private static BufferedImage createLedImage(Color c)
  {
    int size = 3;
    BufferedImage bi = new BufferedImage(size, size, Transparency.BITMASK);
    Graphics2D g2D = bi.createGraphics();

    // Set background color
    g2D.setColor(c);
    g2D.fillRect(0, 0, size, size);

    return bi;
  }

}
