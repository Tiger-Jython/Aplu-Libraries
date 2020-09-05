// NxtSoundSensor.java

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

/**
 * Class that represents a NXT sound sensor.
 */
public class NxtSoundSensor extends Sensor
{
  private interface SensorState // Simulate enum
  {
    int QUIET = 0;
    int LOUD = 1;
  }

  // -------------- Inner class SoundSensorThread ------------
  private class SoundSensorThread extends Thread
  {
    private volatile boolean isRunning = false;

    private SoundSensorThread()
    {
      if (LegoRobot.getDebugLevel() >= SharedConstants.DEBUG_LEVEL_LOW)
        DebugConsole.show("SsTh created");
    }

    public void run()
    {
      EV3Properties props = LegoRobot.getProperties();
      int soundPollDelay = props.getIntValue("SoundSensorPollDelay");
      if (LegoRobot.getDebugLevel() >= SharedConstants.DEBUG_LEVEL_LOW)
        DebugConsole.show("SsTh started");

      isRunning = true;
      while (isRunning)
      {
        if (!isEvtEnabled)
          continue;

        if (soundListener != null)
        {
          Tools.delay(soundPollDelay);
          int level = getLevel();
          if (state == SensorState.QUIET && level > triggerLevel)
          {
            if (inCallback)
            {
              if (LegoRobot.getDebugLevel() >= SharedConstants.DEBUG_LEVEL_LOW)
                DebugConsole.show("Evt'loud'(rej)");
            }
            else
            {
              inCallback = true;
              if (LegoRobot.getDebugLevel() >= SharedConstants.DEBUG_LEVEL_LOW)
                DebugConsole.show("Evt'loud'(" + getPortLabel() + ")");
              soundListener.loud(getPort(), level);
              state = SensorState.LOUD;
              inCallback = false;
            }
          }
          if (state == SensorState.LOUD && level <= triggerLevel)
          {
            if (inCallback)
            {
              if (LegoRobot.getDebugLevel() >= SharedConstants.DEBUG_LEVEL_LOW)
                DebugConsole.show("Evt'quiet'(rej)");
            }
            else
            {
              inCallback = true;
              if (LegoRobot.getDebugLevel() >= SharedConstants.DEBUG_LEVEL_LOW)
                DebugConsole.show("Evt'quiet'(" + getPortLabel() + ")");
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
        join(500);
      }
      catch (InterruptedException ex)
      {
      }
      if (LegoRobot.getDebugLevel() >= SharedConstants.DEBUG_LEVEL_LOW)
        if (isAlive())
          DebugConsole.show("SsTh stop failed");
        else
          DebugConsole.show("SsTh stop ok");
    }
  }
  // -------------- End of inner classes -----------------------

  private lejos.hardware.sensor.NXTSoundSensor ss;
  private SoundListener soundListener = null;
  private int state = SensorState.QUIET;
  private int triggerLevel;
  private volatile static boolean inCallback = false;
  private SoundSensorThread sst;
  private volatile static boolean isEvtEnabled = false;

  /**
   * Creates a sensor instance connected to the given port.
   * @param port the port where the sensor is plugged-in
   */
  public NxtSoundSensor(SensorPort port)
  {
    super(port);
    sst = new SoundSensorThread();
    ss = new lejos.hardware.sensor.NXTSoundSensor(getLejosPort());
  }

  /**
   * Creates a sensor instance connected to port S1.
   */
  public NxtSoundSensor()
  {
    this(SensorPort.S1);
  }

  protected void init()
  {
    if (LegoRobot.getDebugLevel() >= SharedConstants.DEBUG_LEVEL_MEDIUM)
      DebugConsole.show("_ss.init()");

  }

  protected void cleanup()
  {
    if (LegoRobot.getDebugLevel() >= SharedConstants.DEBUG_LEVEL_MEDIUM)
      DebugConsole.show("_ss.cleanup()");
    isEvtEnabled = false;  // Disable events
    if (sst != null)
      sst.stopThread();
    ss.close();
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
    {
      sst.start();
      // Wait a moment until enabling events, otherwise crash may result
      Tools.delay(LegoRobot.getSensorEventDelay());
      isEvtEnabled = true;
    }
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

  private float[] getSensorValues()
  {
    int size = ss.sampleSize();
    float[] values = new float[size];
    ss.fetchSample(values, 0);
    return values;
  }

  private int getLevel()
  {
    if (robot == null)
      return -1;
    return (int)(100 * getSensorValues()[0]);
  }

  /**
   * Polls the sensor.
   * @return the current value the sensor reported: 0 (quiet) .. 100 (loud)
   */
  public int getValue()
  {
    checkConnect();
    return (int)(100 * getSensorValues()[0]);
  }

  /**
   * Returns the reference of the the underlying lejos.hardware.sensor.NXTNxtSoundSensor.
   * @return the reference of the NxtSoundSensor
   */
  public lejos.hardware.sensor.NXTSoundSensor getLejosSensor()
  {
    return ss;
  }

  private void checkConnect()
  {
    if (robot == null)
      new ShowError("NxtSoundSensor (port: " + getPortLabel()
        + ") is not a part of the LegoRobot.\n"
        + "Call addPart() to assemble it.");
  }
}
