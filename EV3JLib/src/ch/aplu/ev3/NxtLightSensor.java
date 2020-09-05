// NxtLightSensor.java
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
        DebugConsole.show("DEBUG: LightSensorThread created (port: "
          + getPortLabel() + ")");
    }

    public void run()
    {
      if (LegoRobot.getDebugLevel() >= SharedConstants.DEBUG_LEVEL_LOW)
        DebugConsole.show("DEBUG: LightSensorThread "
          + Thread.currentThread().getName()
          + " started (port: " + getPortLabel() + ")");
      isRunning = true;
      while (isRunning)
      {
        if (lightListener != null)
        {
          Tools.delay(pollDelay);
          int level = getLevel();
          if (state == SensorState.DARK && level > triggerLevel)
          {
            if (inCallback)
            {
              if (LegoRobot.getDebugLevel() >= SharedConstants.DEBUG_LEVEL_LOW)
                DebugConsole.show("DEBUG: Light event 'bright' (port: "
                  + getPortLabel() + ") -------- rejected: Other callback underway!");
            }
            else
            {
              inCallback = true;
              if (LegoRobot.getDebugLevel() >= SharedConstants.DEBUG_LEVEL_LOW)
                DebugConsole.show("DEBUG: Light event 'bright' (port: "
                  + getPortLabel() + ")");
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
                DebugConsole.show("DEBUG: Light event 'dark' (port: "
                  + getPortLabel() + ") -------- rejected: Other callback underway!");
            }
            else
            {
              inCallback = true;
              if (LegoRobot.getDebugLevel() >= SharedConstants.DEBUG_LEVEL_LOW)
                DebugConsole.show("DEBUG: Light event 'dark' (port: "
                  + getPortLabel() + ")");
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
          DebugConsole.show("DEBUG: LightSensorThread stopping failed (port: "
            + getPortLabel() + ")");
        else
          DebugConsole.show("DEBUG: LightSensorThread successfully stopped (port: "
            + getPortLabel() + ")");
    }
  }
// -------------- End of inner classes -----------------------
  private LightListener lightListener = null;
  private LightSensorThread lst;
  private int state = SensorState.DARK;
  private int triggerLevel;
  private int pollDelay;
  private volatile static boolean inCallback = false;

  /**
   * Creates a sensor instance connected to the given port.
   * @param port the port where the sensor is plugged-in
   */
  public NxtLightSensor(SensorPort port)
  {
    super(port);
    lst = new LightSensorThread();
    EV3Properties props = LegoRobot.getProperties();
    pollDelay = props.getIntValue("LightSensorPollDelay");
    partName = "_ls" + (port.getId() + 1);
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
      DebugConsole.show("DEBUG: NxtLightSensor.init() called (Port: "
        + getPortLabel() + ")");
  }

  protected void cleanup()
  {
    if (LegoRobot.getDebugLevel() >= SharedConstants.DEBUG_LEVEL_MEDIUM)
      DebugConsole.show("DEBUG: NxtLightSensor.cleanup() called (Port: "
        + getPortLabel() + ")");
    if (lst != null)
      lst.stopThread();
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
      startLightThread();
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

  protected void startLightThread()
  {
    lst.start();
  }

  protected void stopLightThread()
  {
    if (lst.isAlive())
      lst.stopThread();
  }

  private int getValue(boolean check)
  {
    if (check)
      checkConnect();
    int v = 0;
    try
    {
      v = Integer.parseInt(robot.sendCommand(partName + ".getValue"));
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
   * Due to a bug in leJOS version 0.8-beta, the floodlight is activated
   * when the sensor is read.
   * @return the current value the sensor reported: 0 (dark) .. 1023 (bright)
   */
  public int getValue()
  {
    return getValue(true);
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
    robot.sendCommand(partName + ".activate.b" + (enable ? "1" : "0"));
  }

  private void checkConnect()
  {
    if (robot == null)
      new ShowError("NxtLightSensor (port: " + getPortLabel()
        + ") is not a part of the EV3Robot.\n"
        + "Call addPart() to assemble it.");
  }
}
