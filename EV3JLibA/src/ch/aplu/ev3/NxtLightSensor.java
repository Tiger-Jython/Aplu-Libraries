// NxtLightSensor.java

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
 * Class that represents a light sensor (Lego NXT light sensor).
 */
public class NxtLightSensor extends Sensor
{
  private interface SensorState // Simulate enum for J2ME compatibility
  {
    int DARK = 0;
    int BRIGHT = 1;
  }

  // -------------- Inner class LightSensorThread ------------
  private class LightSensorThread extends Thread
  {
    private volatile boolean isRunning = false;

    private LightSensorThread()
    {
      if (LegoRobot.getDebugLevel() >= SharedConstants.DEBUG_LEVEL_LOW)
        DebugConsole.show("LsTh created");
    }

    public void run()
    {
      EV3Properties props = LegoRobot.getProperties();
      int lightPollDelay = props.getIntValue("LightSensorPollDelay");

      if (LegoRobot.getDebugLevel() >= SharedConstants.DEBUG_LEVEL_LOW)
        DebugConsole.show("LsTh started");

      isRunning = true;
      while (isRunning)
      {
        if (!isEvtEnabled)
          continue;

        if (lightListener != null)
        {
          Tools.delay(lightPollDelay);
          int level = getLevel();
          if (state == SensorState.DARK && level > triggerLevel)
          {
            if (inCallback)
            {
              if (LegoRobot.getDebugLevel() >= SharedConstants.DEBUG_LEVEL_LOW)
                DebugConsole.show("Evt'bright'(rej)");
            }
            else
            {
              inCallback = true;
              if (LegoRobot.getDebugLevel() >= SharedConstants.DEBUG_LEVEL_LOW)
                DebugConsole.show("Evt'bright'(" + getPortLabel() + ")");
              lightListener.bright(getPort(), level);
              state = SensorState.BRIGHT;
              inCallback = false;
            }
          }
          if (state == SensorState.BRIGHT && level <= triggerLevel)
          {
            if (inCallback)
            {
              if (LegoRobot.getDebugLevel() >= SharedConstants.DEBUG_LEVEL_LOW)
                DebugConsole.show("Evt'dark'(rej)");
            }
            else
            {
              inCallback = true;
              if (LegoRobot.getDebugLevel() >= SharedConstants.DEBUG_LEVEL_LOW)
                DebugConsole.show("Evt'dark'(" + getPortLabel() + ")");
              lightListener.dark(getPort(), level);
              state = SensorState.DARK;
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
          DebugConsole.show("LsTh stop failed");
        else
          DebugConsole.show("LsTh stop ok");
    }
  }
  // -------------- End of inner classes -----------------------

  private lejos.hardware.sensor.NXTLightSensor ls;
  private LightListener lightListener = null;
  private int state = SensorState.DARK;
  private int triggerLevel;
  private volatile static boolean inCallback = false;
  private LightSensorThread lst;
  private volatile static boolean isEvtEnabled = false;

  /**
   * Creates a sensor instance connected to the given port.
   * @param port the port where the sensor is plugged-in
   */
  public NxtLightSensor(SensorPort port)
  {
    super(port);
    lst = new LightSensorThread();
    ls = new lejos.hardware.sensor.NXTLightSensor(getLejosPort());
  }

  /**
   * Creates a sensor instance connected to port S1.
   */
  public NxtLightSensor()
  {
    this(SensorPort.S1);
  }

  protected void init()
  {
    if (LegoRobot.getDebugLevel() >= SharedConstants.DEBUG_LEVEL_MEDIUM)
      DebugConsole.show("_ls.init()");
    activate(false);
  }

  protected void cleanup()
  {
    if (LegoRobot.getDebugLevel() >= SharedConstants.DEBUG_LEVEL_MEDIUM)
      DebugConsole.show("_ls.cleanup()");
    activate(false);
    isEvtEnabled = false;  // Disable events
    if (lst != null)
      lst.stopThread();
    ls.close();
  }

  /**
   * Registers the given light listener for the given trigger level.
   * @param lightListener the LightListener to become registered.
   * @param triggerLevel the trigger level where the callback is triggered
   */
  public void addLightListener(LightListener lightListener, int triggerLevel)
  {
    this.lightListener = lightListener;
    this.triggerLevel = triggerLevel;
    if (!lst.isAlive())
    {
      lst.start();
      // Wait a moment until enabling events, otherwise crash may result
      Tools.delay(LegoRobot.getSensorEventDelay());
      isEvtEnabled = true;
    }
  }

  /**
   * Registers the given light listener with default trigger level 500.
   * @param lightListener the LightListener to become registered.
   */
  public void addLightListener(LightListener lightListener)
  {
    addLightListener(lightListener, 500);
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

  /**
   * Turns on/off the LED used for reflecting light back into the sensor.
   * Due to a bug in leJOS version 0.8-beta, the floodlight is activated
   * when the sensor is read.
   * @param enable if true, turn the LED on, otherwise turn it off
   */
  public void activate(boolean enable)
  {
    checkConnect();
    ls.setFloodlight(enable);
  }

  private int getLevel()
  {
    if (robot == null)
      return -1;
    return (int)(1023 * getSensorValues()[0]);
  }

  private float[] getSensorValues()
  {
    int size = ls.sampleSize();
    float[] values = new float[size];
    ls.fetchSample(values, 0);
    return values;
  }

  /**
   * Polls the sensor.
   * Due to a bug in leJOS version 0.8-beta, the floodlight is activated
   * when the sensor is read.
   * @return the current value the sensor reported: 0 (dark) .. 1023 (bright)
   */
  public int getValue()
  {
    checkConnect();
    return (int)(1023 * getSensorValues()[0]);
  }

  /**
   * Returns the reference of the the underlying lejos.hardware.sensor.NxtLightSensor.
   * @return the reference of the lejos.hardware.sensor.NxtLightSensor
   */
  public lejos.hardware.sensor.NXTLightSensor getLejosSensor()
  {
    return ls;
  }

  private void checkConnect()
  {
    if (robot == null)
      new ShowError("NxtLightSensor (port: " + getPortLabel()
        + ") is not a part of the LegoRobot.\n"
        + "Call addPart() to assemble it.");
  }
}
