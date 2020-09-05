// LightSensor.java

/*
 This software is part of the RaspiSim library.
 It is Open Source Free Software, so you may
 - run the code for any purpose
 - study how the code works and adapt it to your needs
 - integrate all or parts of the code in your own programs
 - redistribute copies of the code
 - improve the code and release your improvements to the public
 However the use of the code is entirely your responsibility.

 Author: Aegidius Pluess, www.aplu.ch
 */
package ch.aplu.raspisim;

import ch.aplu.jgamegrid.*;

/**
 * Class that represents a light sensor.
 */
public class LightSensor extends Part
{
  /** Constant for id of front left sensor */
  public static int LS_FRONT_LEFT = 0;
  /** Constant for id of front right sensor */
  public static int LS_FRONT_RIGHT = 1;
  /** Constant for id of front left sensor */
  public static int LS_REAR_LEFT = 2;
  /** Constant for id of front right sensor */
  public static int LS_REAR_RIGHT = 3;

  private static final Location[] sensorPos =
  {
    new Location(24, -9),
    new Location(24, 9),
    new Location(-5, -9),
    new Location(-5, 9),
  };

  private Robot robot;
  private int id;
  private volatile boolean isBrightNotified = false;
  private volatile boolean isDarkNotified = false;
  private LightListener lightListener = null;
  private int triggerLevel;

  /**
   * Creates a motor instance with given id.
   * @param id 0 for front left, 1 for front right, 
   * 2 for rear left, 3 for rear right
   */
  public LightSensor(int id)
  {
    super("sprites/dummy.gif", sensorPos[id]);
    this.id = id;
    robot = RobotInstance.getRobot();
    if (robot == null)
      // Defer addPart() to robot ctor
      RobotInstance.partsToAdd.add(this);
    else
      robot.addPart(this);
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
   * Returns the brightness of all torches at the current location;
   * 0, if the sensor is inside a shadow region.
   * @return the sum of the intensity of all torches (in abritrary units)
   */
  public int getValue()
  {
    RobotInstance.checkRobot();
    delay(1);
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

  /**
   * Returns the if of the sensor.
   * @return the sensor id
   */
  public int getId()
  {
    return id;
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
          lightListener.bright(id, value);
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
          lightListener.dark(id, value);
        }
      }.start();
    }
  }
}
