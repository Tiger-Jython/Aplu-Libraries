// UltrasonicSensor.java
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
 * Class that represents a EV3 ultrasonic sensor.
 */
public class UltrasonicSensor extends Sensor
{
  private interface SensorState // Simulate enum for J2ME compatibility
  {
    int NEAR = 0;
    int FAR = 1;
  }

  // -------------- Inner class UltrasonicSensorThread ------------
  private class UltrasonicSensorThread extends Thread
  {
    private volatile boolean isRunning = false;

    private UltrasonicSensorThread()
    {
      if (LegoRobot.getDebugLevel() >= SharedConstants.DEBUG_LEVEL_LOW)
        DebugConsole.show("UlTh created");
    }

    public void run()
    {
      if (LegoRobot.getDebugLevel() >= SharedConstants.DEBUG_LEVEL_LOW)
        DebugConsole.show("UlTh started");

      isRunning = true;
      while (isRunning)
      {
        if (ultrasonicListener != null)
        {
          Tools.delay(pollDelay);
          int level = getLevel();
          if (state == SensorState.NEAR && level > triggerLevel)
          {
            if (inCallback)
            {
              if (LegoRobot.getDebugLevel() >= SharedConstants.DEBUG_LEVEL_LOW)
                DebugConsole.show("Evt'far'(rej)");
            }
            else
            {
              inCallback = true;
              if (LegoRobot.getDebugLevel() >= SharedConstants.DEBUG_LEVEL_LOW)
                DebugConsole.show("Evt'far'(" + getPortLabel() + ")");
              ultrasonicListener.far(getPort(), level);
              state = SensorState.FAR;
              inCallback = false;
            }
          }
          if (state == SensorState.FAR && level <= triggerLevel)
          {
            if (inCallback)
            {
              if (LegoRobot.getDebugLevel() >= SharedConstants.DEBUG_LEVEL_LOW)
                DebugConsole.show("Evt'near'(rej)");
            }
            else
            {
              inCallback = true;
              if (LegoRobot.getDebugLevel() >= SharedConstants.DEBUG_LEVEL_LOW)
                DebugConsole.show("Evt'near'(" + getPortLabel() + ")");
              ultrasonicListener.near(getPort(), level);
              state = SensorState.NEAR;
              inCallback = false;
            }
          }
        }
      }
    }

    private void stopThread()
    {
      isRunning = false;
      try
      {
        join(500);
      }
      catch (InterruptedException ex)
      {
      }
      if (LegoRobot.getDebugLevel() >= SharedConstants.DEBUG_LEVEL_LOW)
        if (isAlive())
          DebugConsole.show("UlTh stop failed");
        else
          DebugConsole.show("UlTh stop ok");
    }
  }
  // -------------- End of inner classes -----------------------

  private UltrasonicListener ultrasonicListener = null;
  private int state = SensorState.NEAR;
  private int triggerLevel;
  private int pollDelay;
  private volatile static boolean inCallback = false;
  private UltrasonicSensorThread ust;

  /**
   * Creates a sensor instance connected to the given port.
   * @param port the port where the sensor is plugged-in
   */
  public UltrasonicSensor(SensorPort port)
  {
    super(port);
    ust = new UltrasonicSensorThread();
    EV3Properties props = LegoRobot.getProperties();
    pollDelay = props.getIntValue("UltrasonicSensorPollDelay");
    partName = "us" + (port.getId() + 1);
  }

  /**
   * Creates a sensor instance connected to port S1.
   */
  public UltrasonicSensor()
  {
    this(SensorPort.S1);
  }

  protected void init()
  {
    if (LegoRobot.getDebugLevel() >= SharedConstants.DEBUG_LEVEL_MEDIUM)
      DebugConsole.show("DEBUG: UltrasonicSensor.init() called (Port: "
        + getPortLabel() + ")");
  }

  protected void cleanup()
  {
    if (LegoRobot.getDebugLevel() >= SharedConstants.DEBUG_LEVEL_MEDIUM)
      DebugConsole.show("DEBUG: UltrasonicSensor.cleanup() called (Port: "
        + getPortLabel() + ")");
    if (ust != null)
      ust.stopThread();
  }

  /**
   * Registers the given ultrasonic listener for the given trigger level.
   * @param ultrasonicListener the UltrasonicListener to become registered.
   * @param triggerLevel the trigger level where the callback is triggered
   */
  public void addUltrasonicListener(UltrasonicListener ultrasonicListener, int triggerLevel)
  {
    this.ultrasonicListener = ultrasonicListener;
    this.triggerLevel = triggerLevel;
    if (!ust.isAlive())
      startUltrasonicThread();
  }

  /**
   * Registers the given ultrasonic listener with default trigger level 10.
   * @param ultrasonicListener the UltrasonicListener to become registered.
   */
  public void addUltrasonicListener(UltrasonicListener ultrasonicListener)
  {
    addUltrasonicListener(ultrasonicListener, 10);
  }

  /**
   * Sets a new trigger level and returns the previous one.
   * @param triggerLevel the new trigger level
   * @return the previous trigger level
   */
  public int setTriggerLevel(int triggerLevel)
  {
    int oldLevel = this.triggerLevel;
    this.triggerLevel = triggerLevel;
    return oldLevel;
  }

  protected void startUltrasonicThread()
  {
    ust.start();
  }

  protected void stopUltrasonicThread()
  {
    if (ust.isAlive())
      ust.stopThread();
  }

  private int getValue(boolean check)
  {
    if (check)
      checkConnect();
    int v = 0;
    try
    {
      v = Integer.parseInt(robot.sendCommand(partName + ".getDistance"));
    }
    catch (NumberFormatException ex)
    {
    }
    return v;
  }

  private int getLevel()
  {
    if (robot == null || !robot.isConnected())
      return -1;
    return getValue(false);
  }

  /**
   * Polls the sensor.
   * @return the current value the sensor reported (in cm), 255 if the
   * sensor does not detect a target. 
   */
  public int getDistance()
  {
    return getValue(true);
  }

  private void checkConnect()
  {
    if (robot == null)
      new ShowError("UltrasonicSensor (port: " + getPortLabel()
        + ") is not a part of the EV3Robot.\n"
        + "Call addPart() to assemble it.");
  }

  private void delay(long timeout)
  {
    try
    {
      Thread.currentThread().sleep(timeout);
    }
    catch (InterruptedException ex)
    {
    }
  }

  /**
   * Sets the color of the triangle mesh lines. 
   * Empty method for compatibility with EV3Sim.
   * @param color the color of the mesh
   */
  public void setMeshTriangleColor(Color color)
  {
  }

  /**
   * Sets the color of the beam area (two sector border lines and axis).
   * Empty method for compatibility with EV3Sim.
   * @param color the color of the beam area
   */
  public void setBeamAreaColor(Color color)
  {
  }

  /**
   * Erases the beam area (if it is currently shown).
   * Empty method for compatibility with EV3Sim.
   */
  public void eraseBeamArea()
  {
  }

  /**
   * Sets the color of the circle with center at sensor location and radius
   * equals to the current distance value. If value = 0, no circle is shown.
   * Empty method for compatibility with EV3Sim.
   * @param color the color of the circle
   */
  public void setProximityCircleColor(Color color)
  {
  }

}
