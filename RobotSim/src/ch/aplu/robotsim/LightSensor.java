// LightSensor.java

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
 * Class that represents a light sensor.
 */
public class LightSensor extends Part
{
  private static final Location[][] locs = new Location[][]
  {
    {
      new Location(8, 7), new Location(8, -7), 
      new Location(8, 0), new Location(-35, 0)
    },
    {
      new Location(8, 7), new Location(8, -7), 
      new Location(-35, 7), new Location(-35, -7)
    }
  };

  private volatile boolean isBrightNotified = false;
  private volatile boolean isDarkNotified = false;
  private LightListener lightListener = null;
  private SensorPort port;
  private int triggerLevel;
  private boolean upwards = false;

  /**
   * Creates a sensor instance pointing downwards connected to the given port.
   * In simulation mode, the sensor detects the brightness of the background pixel.
   * The port selection determines the position of the sensor:
   * S1: right; S2: left, S3: middle, S4: rear-middle.
   * @param port the port where the sensor is plugged-in
   */
  public LightSensor(SensorPort port)
  {
    this(port, false);
  }

  /**
   * Creates a sensor instance connected to the given port.
   * The port selection determines the position of the sensor:<br>
   * Downward direction: S1: right; S2: left, S3: middle, S4: rear-middle<br>
   * Upward direction: S1: front right; S2: front left, S3: rear right, S4: rear left<br>
   * A sensor directed downwards detects the intensity of the background,
   * while a sensor directed upwards detects the summed intensity of all torches.
   * @param port the port where the sensor is plugged-in
   * @param upwards if true, the sensor is pointing upwards; otherwise pointing downwards
   */
  public LightSensor(SensorPort port, boolean upwards)
  {
    super("sprites/lightsensor"
      + (port == SensorPort.S1 ? ".gif"
      : (port == SensorPort.S2 ? ".gif"
      : (port == SensorPort.S3 ? (upwards ? "_rear.gif" : ".gif") 
      : "_rear.gif"))),
      port == SensorPort.S1 ? locs[upwards ? 1 : 0][0]
      : (port == SensorPort.S2 ? locs[upwards ? 1 : 0][1]
      : (port == SensorPort.S3 ? locs[upwards ? 1 : 0][2] 
      : locs[upwards ? 1 : 0][3])));
    this.port = port;
    this.upwards = upwards;
  }

  protected void cleanup()
  {
  }

  /**
   * Registers the given LightListener to detect crossing the given trigger triggerLevel.
   * @param listener the LightListener to register
   * @param triggerLevel the light value used as trigger level
   */
  public void addLightListener(LightListener listener, int triggerLevel)
  {
    lightListener = listener;
    this.triggerLevel = triggerLevel;
  }

  /**
   * Registers the given LightListener with default trigger triggerLevel 500.
   * @param lightListener the LightListener to register
   */
  public void addLightListener(LightListener lightListener)
  {
    addLightListener(lightListener, 500);
  }

  /**
   * Sets a new triggerLevel and returns the previous one.
   * @param triggerLevel the new trigger triggerLevel
   * @return the previous trigger triggerLevel
   */
  public int setTriggerLevel(int triggerLevel)
  {
    int oldLevel = this.triggerLevel;
    this.triggerLevel = triggerLevel;
    return oldLevel;
  }

  /**
   * For sensor ports 1, 2, 3, 4: returns the brightness of the background
   * at the current location.<br>
   * For sensor ports -1, -2, -3, -4: returns the sum of the intensity 
   * of all torches at the current location; 0, if the sensor is inside
   * a shadow region.
   * Calls Thread.sleep(1) to prevent CPU overload in close polling loops.
   * @return the brightness at the current location
   */
  public int getValue()
  {
    checkPart();
    delay(1);
    if (upwards)
    {
      synchronized (RobotContext.shadows)
      {
        for (Shadow shadow : RobotContext.shadows)
        {
          if (shadow.inShadow(getLocation()))
            return 0;
        }
      }
      double v = 0;
      synchronized (RobotContext.torches)
      {
        double w;
        for (Torch torch : RobotContext.torches)
        {
          w = torch.getIntensity(getLocation());
          v += w;
        }
      }
      return (int)v;
    }
    else
    {
      Color c = getBackground().getColor(getLocation());
      float[] hsb = new float[3];
      Color.RGBtoHSB(c.getRed(), c.getGreen(), c.getBlue(), hsb);
      return (int)(1000 * hsb[2]);
    }
  }

  /**
   * Turns on/off the LED used for reflecting light back into the sensor.
   * Empty method in simulation mode.
   * @param enable if true, turn the LED on, otherwise turn it off
   */
  public void activate(boolean enable)
  {
  }

  /**
   * Returns the port of the sensor.
   * @return the sensor port
   */
  public SensorPort getPort()
  {
    return port;
  }

  protected void notifyEvent()
  {
    if (lightListener == null)
      return;
    final int value = getValue();
    if (value < triggerLevel)
      isBrightNotified = false;
    if (value >= triggerLevel)
      isDarkNotified = false;
    if (value >= triggerLevel && !isBrightNotified)
    {
      isBrightNotified = true;
      new Thread()
      {
        public void run()
        {
          lightListener.bright(port, value);
        }
      }.start();
    }
    if (value < triggerLevel && !isDarkNotified)
    {
      isDarkNotified = true;
      new Thread()
      {
        public void run()
        {
          lightListener.dark(port, value);
        }
      }.start();
    }
  }

  private void checkPart()
  {
    if (robot == null)
    {
      JOptionPane.showMessageDialog(null,
        "LightSensor is not part of the LegoRobot.\n"
        + "Call addPart() to assemble it.",
        "Fatal Error", JOptionPane.ERROR_MESSAGE);
      if (GameGrid.getClosingMode() == GameGrid.ClosingMode.TerminateOnClose
        || GameGrid.getClosingMode() == GameGrid.ClosingMode.AskOnClose)
        System.exit(1);
      if (GameGrid.getClosingMode() == GameGrid.ClosingMode.DisposeOnClose)
        throw new RuntimeException("LightSensor is not part of the LegoRobot.\n"
          + "Call addPart() to assemble it.");
    }
  }
}
