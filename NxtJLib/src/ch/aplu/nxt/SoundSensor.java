// SoundSensor.java

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

/**
 * Class that represents a sound sensor.
 */
public class SoundSensor extends Sensor implements SharedConstants
{
  private interface SensorState // Simulate enum for J2ME compatibility
  {
    int QUIET = 0;
    int LOUD = 1;
  }

// -------------- Inner class SoundSensorThread ------------
  private class SoundSensorThread extends NxtThread
  {
    private volatile boolean isRunning = false;

    private SoundSensorThread()
    {
      if (LegoRobot.getDebugLevel() >= DEBUG_LEVEL_LOW)
        DebugConsole.show("DEBUG: SoundSensorThread created (port: "
          + getPortLabel() + ")");
    }

    public void run()
    {
      if (LegoRobot.getDebugLevel() >= DEBUG_LEVEL_LOW)
        DebugConsole.show("DEBUG: SoundSensorThread "
          + Thread.currentThread().getName()
          + " started (port: " + getPortLabel() + ")");
      isRunning = true;
      while (isRunning)
      {
        if (soundListener != null)
        {
          delay(pollDelay);
          int level = getLevel();
          if (state == SensorState.QUIET && level > triggerLevel)
          {
            if (inCallback)
            {
              if (LegoRobot.getDebugLevel() >= DEBUG_LEVEL_LOW)
                DebugConsole.show("DEBUG: Sound event 'loud' (port: "
                  + getPortLabel() + ") -------- rejected: Other callback underway!");
            }
            else
            {
              inCallback = true;
              if (LegoRobot.getDebugLevel() >= DEBUG_LEVEL_LOW)
                DebugConsole.show("DEBUG: Sound event 'loud' (port: "
                  + getPortLabel() + ")");
              soundListener.loud(getPort(), level);
              state = SensorState.LOUD;
              inCallback = false;
            }
          }
          if (state == SensorState.LOUD && level <= triggerLevel)
          {
            if (inCallback)
            {
              if (LegoRobot.getDebugLevel() >= DEBUG_LEVEL_LOW)
                DebugConsole.show("DEBUG: Sound event 'quiet' (port: "
                  + getPortLabel() + ") -------- rejected: Other callback underway!");
            }
            else
            {
              inCallback = true;
              if (LegoRobot.getDebugLevel() >= DEBUG_LEVEL_LOW)
                DebugConsole.show("DEBUG: Sound event 'quiet' (port: "
                  + getPortLabel() + ")");
              soundListener.quiet(getPort(), level);
              state = SensorState.QUIET;
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
          DebugConsole.show("DEBUG: SoundSendorThread stopping failed (port: "
            + getPortLabel() + ")");
        else
          DebugConsole.show("DEBUG: SoundSensorThread successfully stopped (port: "
            + getPortLabel() + ")");
    }
  }
// -------------- End of inner classes -----------------------
  private SoundListener soundListener = null;
  private SoundSensorThread sst;
  private int state = SensorState.QUIET;
  private int triggerLevel;
  private final int pollDelay;
  private volatile static boolean inCallback = false;

  /**
   * Creates a sensor instance connected to the given port.
   * @param port the port where the sensor is plugged-in
   */
  public SoundSensor(SensorPort port)
  {
    super(port);
    sst = new SoundSensorThread();
    NxtProperties props = LegoRobot.getProperties();
    pollDelay = props.getIntValue("SoundSensorPollDelay");
  }

  /**
   * Creates a sensor instance connected to port S1.
   */
  public SoundSensor()
  {
    this(SensorPort.S1);
  }

  // Called by connect() if part is alread added.
  // Called by addPart() if already connected
  protected void init()
  {
    if (LegoRobot.getDebugLevel() >= DEBUG_LEVEL_MEDIUM)
      DebugConsole.show("DEBUG: SoundSensor.init() called (Port: "
        + getPortLabel() + ")");

    setTypeAndMode(SOUND_DB, PCTFULLSCALEMODE);
  }

  protected void cleanup()
  {
    if (LegoRobot.getDebugLevel() >= DEBUG_LEVEL_MEDIUM)
      DebugConsole.show("DEBUG: SoundSensor.cleanup() called (Port: "
        + getPortLabel() + ")");
    if (sst != null)
      sst.stopThread();
  }

  /**
   * Registers the given sound listener for the given trigger level.
   * @param soundListener the SoundListener to become registered.
   * @param triggerLevel the trigger level where the callback is triggered
   */
  public void addSoundListener(SoundListener soundListener, int triggerLevel)
  {
    this.soundListener = soundListener;
    this.triggerLevel = triggerLevel;
    if (!sst.isAlive())
      startSoundThread();
  }

  /**
   * Registers the given sound listener with default trigger level 50.
   * @param soundListener the SoundListener to become registered.
   */
  public void addSoundListener(SoundListener soundListener)
  {
    addSoundListener(soundListener, 50);
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

  protected void startSoundThread()
  {
    sst.start();
  }

  protected void stopSoundThread()
  {
    if (sst.isAlive())
      sst.stopThread();
  }

  private int getValue(boolean check)
  {
    if (check)
      checkConnect();
    return readScaledValue();
  }

  private int getLevel()
  {
    if (robot == null || !robot.isConnected())
      return -1;
    return getValue(false);
  }

  /**
   * Polls the sensor.
   * @return the current value the sensor reported: 0 (quiet) .. 150 (loud)
   */
  public int getValue()
  {
    delay(10);
    return getValue(true);
  }

  private void checkConnect()
  {
    if (robot == null)
      new ShowError("SoundSensor (port: " + getPortLabel()
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
}
