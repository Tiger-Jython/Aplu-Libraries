// UltrasonicSensor.java
// Most of this class taken from the leJOS library (lejos.sourceforge.net),
// with thanks to the author.

/*
 This software is part of the NxtJLib library.
 It is Open Source Free Software, so you may
 - run the code for any purpose
 - study how the code works and adapt it to your needs
 - integrate all or parts of the code in your own programs
 - redistribute copies of the code
 - improve the code and release your improvements to the public
 However the use of the code is entirely your responsibility.
 */
package ch.aplu.nxt;

import ch.aplu.nxt.platform.*;
import java.awt.Color;

/**
 * Class that represents an ultrasonic sensor.
 * Most of the code and the documentation
 * taken from the leJOS library (lejos.sourceforge.net, with thanks to the author.
 */
public class UltrasonicSensor extends I2CSensor
{
  private interface SensorState // Simulate enum for J2ME compatibility
  {
    int NEAR = 0;
    int FAR = 1;
  }

// -------------- Inner class UltrasonicSensorThread ------------
  private class UltrasonicSensorThread extends NxtThread
  {
    private volatile boolean isRunning = false;

    private UltrasonicSensorThread()
    {
      if (LegoRobot.getDebugLevel() >= DEBUG_LEVEL_LOW)
        DebugConsole.show("DEBUG: UltrasonicSensorThread created (port: "
          + getPortLabel() + ")");
    }

    public void run()
    {
      if (LegoRobot.getDebugLevel() >= DEBUG_LEVEL_LOW)
        DebugConsole.show("DEBUG: UltrasonicSensorThread "
          + Thread.currentThread().getName()
          + " started (port: " + getPortLabel() + ")");

      isRunning = true;
      while (isRunning)
      {
        if (ultrasonicListener != null)
        {
          delay(pollDelay);
          int level = getLevel();
          if (state == SensorState.NEAR && level > triggerLevel)
          {
            if (inCallback)
            {
              if (LegoRobot.getDebugLevel() >= DEBUG_LEVEL_LOW)
                DebugConsole.show("DEBUG: Ultrasonic event 'far' (port: "
                  + getPortLabel() + ") -------- rejected: Other callback underway!");
            }
            else
            {
              inCallback = true;
              if (LegoRobot.getDebugLevel() >= DEBUG_LEVEL_LOW)
                DebugConsole.show("DEBUG: Ultrasonic event 'far' (port: "
                  + getPortLabel() + ")");
              ultrasonicListener.far(getPort(), level);
              state = SensorState.FAR;
              inCallback = false;
            }
          }
          if (state == SensorState.FAR && level <= triggerLevel)
          {
            if (inCallback)
            {
              if (LegoRobot.getDebugLevel() >= DEBUG_LEVEL_LOW)
                DebugConsole.show("DEBUG: Ultrasonci event 'near' (port: "
                  + getPortLabel() + ") -------- rejected: Other callback underway!");
            }
            else
            {
              inCallback = true;
              if (LegoRobot.getDebugLevel() >= DEBUG_LEVEL_LOW)
                DebugConsole.show("DEBUG: Ultrasonic event 'near' (port: "
                  + getPortLabel() + ")");
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
        joinX(500);
      }
      catch (InterruptedException ex)
      {
      }
      if (LegoRobot.getDebugLevel() >= DEBUG_LEVEL_LOW)
        if (isAlive())
          DebugConsole.show("DEBUG: UltrasonicSendorThread stopping failed (port: "
            + getPortLabel() + ")");
        else
          DebugConsole.show("DEBUG: UltrasonicSensorThread successfully stopped (port: "
            + getPortLabel() + ")");
    }
  }
// -------------- End of inner classes -----------------------
  private volatile static boolean inCallback = false;
  private UltrasonicListener ultrasonicListener = null;
  private UltrasonicSensorThread ust;
  private int state = SensorState.NEAR;
  private int triggerLevel;
  private int pollDelay;
  private static final byte COMMAND_STATE = 0x41; // Command or reply length = 1
  private static final byte BYTE0 = 0x42;
  private static byte CONTINUOUS_MEASUREMENT = 0x02;

  /**
   * Creates a sensor instance connected to the given port.
   * @param port the port where the sensor is plugged-in
   */
  public UltrasonicSensor(SensorPort port)
  {
    super(port, LOWSPEED_9V);
    ust = new UltrasonicSensorThread();
    NxtProperties props = LegoRobot.getProperties();
    pollDelay = props.getIntValue("UltrasonicSensorPollDelay");
  }

  /**
   * Creates a sensor instance connected to port S1.
   */
  public UltrasonicSensor()
  {
    this(SensorPort.S1);
  }

  private void setSensorMode(byte modeEnumeration)
  {
    checkConnect();
    sendData(COMMAND_STATE, modeEnumeration);
  }

  protected void init()
  {
    setSensorMode(CONTINUOUS_MEASUREMENT);
  }

  protected void cleanup()
  {
    if (LegoRobot.getDebugLevel() >= DEBUG_LEVEL_MEDIUM)
      DebugConsole.show("DEBUG: Ultrasonic.cleanup() called (Port: "
        + getPortLabel() + ")");
    if (ust != null)
      ust.stopThread();
  }

  private int getDistance(boolean check)
  {
    if (check)
      checkConnect();
    byte[] val = getData(BYTE0, 1);
    if (val == null)
      return 255;  // Illegal mesurement
    return 0xFF & val[0]; // Convert signed byte to unsigned (positive only)
  }

  private int getLevel()
  {
    if (robot == null || !robot.isConnected())
      return -1;
    return getDistance(false);
  }

  /**
   * Polls the sensor.
   * @return the current distance to the closest object in cm
   */
  public int getDistance()
  {
    delay(10);
    return getDistance(true);
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
   * Registers the given ultrasonic listener with default trigger level 20.
   * @param ultrasonicListener the UltrasonicListener to become registered.
   */
  public void addUltrasonicListener(UltrasonicListener ultrasonicListener)
  {
    addUltrasonicListener(ultrasonicListener, 20);
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

  private void checkConnect()
  {
    if (robot == null)
      new ShowError("UltrasonicSensor (port: " + getPortLabel()
        + ") is not a part of the NxtRobot.\n"
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
   * Empty method for compatibility with NxtSim.
   * @param color the color of the mesh
   */
  public void setMeshTriangleColor(Color color)
  {
  }

  /**
   * Sets the color of the beam area (two sector border lines and axis).
   * Empty method for compatibility with NxtSim.
   * @param color the color of the beam area
   */
  public void setBeamAreaColor(Color color)
  {
  }

  /**
   * Erases the beam area (if it is currently shown).
   * Empty method for compatibility with NxtSim.
   */
  public void eraseBeamArea()
  {
  }

  /**
   * Sets the color of the circle with center at sensor location and radius
   * equals to the current distance value. If value = 0, no circle is shown.
   * Empty method for compatibility with NxtSim.
   * @param color the color of the circle
   */
  public void setProximityCircleColor(Color color)
  {
  }
}
