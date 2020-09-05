// LineSensor.java

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
import java.awt.Color;

/**
 * Class that represents a line IR sensor.
 */
class LineSensor extends Part
{
  private static final Location[] sensorPos =
  {
    new Location(26, -4),  // x-axis to right, y-axis downwards
    new Location(26, 4)
  };
  private Robot robot;
  private int id;
  private volatile boolean isActivateNotified = false;
  private volatile boolean isPassivateNotified = false;
  private InfraredListener infraredListener = null;
  private int triggerLevel = 300;

  /**
   * Creates a sensor instance with given id.
   * @param id identification number
   */
  public LineSensor(int id)
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
   * Registers the given InfraredListener to detect crossing the given trigger triggerLevel.
   * @param listener the InfraredListener to register
   * @param triggerLevel the light value used as trigger level
   */
  public void addInfraredListener(InfraredListener listener, int triggerLevel)
  {
    infraredListener = listener;
    this.triggerLevel = triggerLevel;
  }

  /**
   * Registers the given InfraredListener with default trigger triggerLevel 500.
   * @param lightListener the LightListener to register
   */
  public void addInfraredListener(InfraredListener infraredListener)
  {
    addInfraredListener(infraredListener, 300);
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
   * Returns the brightness (scaled intensity in the HSB color model)
   * detected by the light sensor at the current location.
   * Calls Thread.sleep(1) to prevent CPU overload in close polling loops.
   * @return the brightness of the background color at the current location
   * (0..1000, 0: dark, 1000: bright).
   */
  public int getValue()
  {
    RobotInstance.checkRobot();
    Tools.delay(1);
    Color c = getBackground().getColor(getLocation());
    float[] hsb = new float[3];
    Color.RGBtoHSB(c.getRed(), c.getGreen(), c.getBlue(), hsb);
    int intensity = (int)(1000 * hsb[2]);
    RobotInstance.getRobot().debug("intensity = " + intensity);
    if (intensity > triggerLevel)
      return 1;
    return 0;
  }

  /**
   * Returns the id of the sensor.
   * @return sensor id
   */
  public int getId()
  {
    return id;
  }

  protected void notifyEvent()
  {
    if (infraredListener == null)
      return;
    final int value = getValue();
    if (value < triggerLevel)
      isActivateNotified = false;
    if (value >= triggerLevel)
      isPassivateNotified = false;
    if (value >= triggerLevel && !isActivateNotified)
    {
      isActivateNotified = true;
      new Thread()
      {
        public void run()
        {
          infraredListener.activated(id);
        }
      }.start();
    }
    if (value < triggerLevel && !isPassivateNotified)
    {
      isPassivateNotified = true;
      new Thread()
      {
        public void run()
        {
          infraredListener.passivated(id);
        }
      }.start();
    }
  }
}
