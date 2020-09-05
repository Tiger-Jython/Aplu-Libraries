// IRSensor.java

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
import java.awt.*;

/**
 * Class that represents a ceil IR sensor.
 */
class IRSensor extends Part
{
  // -------Inner class SensorThread ---------------------  
  private class SensorThread extends Thread
  {
    public void run()
    {
      while (isRunning)
      {
        synchronized (monitor)
        {
          try
          {
            monitor.wait();
          }
          catch (InterruptedException ex)
          {
          }
        }
        if (!isBrightNotified)
        {
          isBrightNotified = true;
          infraredListener.activated(id);
        }
        if (!isDarkNotified)
        {
          isDarkNotified = true;
          infraredListener.passivated(id);
        }
      }
      infraredListener = null;
    }
  }
  // -------------- End of inner class ------------------

  private static final Location[] sensorPos =
  {
    new Location(30, 0), 
    new Location(24, -10), // x-axis to right, y-axis downwards
    new Location(24, 10)
  };
  private Robot robot;
  private int nbObstacles = 0;
  private InfraredListener infraredListener = null;
  private int id;
  private Actor collisionActor = null;
  private final SensorThread st = new SensorThread();
  private volatile boolean isRunning = false;
  private volatile boolean isBrightNotified = true;
  private volatile boolean isDarkNotified = true;
  private final Object monitor = new Object();

  /**
   * Creates a sensor with given id.
   * @param id identification number
   */
  public IRSensor(int id)
  {
    super("sprites/dummy.gif", sensorPos[id]);
    this.id = id;
    setCollisionCircle(new Point(0, 0), SharedConstants.irSensorCollisionRadius[id]);
    robot = RobotInstance.getRobot();
    if (robot == null)
      // Defer addPart() to robot ctor
      RobotInstance.partsToAdd.add(this);
    else
      robot.addPart(this);
  }

  protected void cleanup()
  {
    isRunning = false;
  }

  /**
   * Register the given InfraredListener to detect bright and dark events.
   * Starts an internal sensor thread that polls the sensor level and runs the
   * sensor callbacks.
   * @param listener the InfraredListener to register; null, to terminate any running
   * sensor thread
   */
  public void addInfraredListener(InfraredListener listener)
  {
    if (listener != null)
    {
      if (infraredListener == null)
      {
        isRunning = true;
        st.start();
      }
      infraredListener = listener;
    }
    else
      isRunning = false;
  }

  /**
   * For internal use only (overrides Actor.act()).
   */
  public void act()
  {
    // Add new obstacles as collision actor
    int nb = RobotContext.obstacles.size();
    if (nb > nbObstacles)
    {
      for (int i = nb - 1; i >= nbObstacles; i--)
        addCollisionActor(RobotContext.obstacles.get(i));
      nbObstacles = nb;
    }

    if (infraredListener != null)
    {
      if (collisionActor == null && getValue() == 1)
      {
        isBrightNotified = false;
        synchronized (monitor)
        {
          monitor.notify();
        }
      }
      if (collisionActor != null && getValue() == 0)
      {
        isDarkNotified = false;
        synchronized (monitor)
        {
          monitor.notify();
        }
      }
    }
  }

  protected Actor getCollisionActor()
  {
    return collisionActor;
  }

  /**
   * Checks, if infrared light ist detected.
   * @return 1, if the sensor detects infrared light; otherwise 0
   */
  public int getValue()
  {
    RobotInstance.checkRobot();
    Tools.delay(1);
    for (Actor a : RobotContext.obstacles)
    {
      if (gameGrid.isActorColliding(a, this))
      {
        collisionActor = a;
        return 1;
      }
    }
    collisionActor = null;
    return 0;
  }
}
