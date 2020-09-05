// TouchSensor.java
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
 * Class that represents a touch sensor.
 */
public class TouchSensor extends Sensor 
{
  private interface SensorState // Simulate enum for J2ME compatibility
  {
    int RELEASED = 0;
    int PRESSED = 1;
  }

  // -------------- Inner class TouchSensorThread ------------
  private class TouchSensorThread extends Thread
  {
    private volatile boolean isRunning = false;

    private TouchSensorThread()
    {
      if (LegoRobot.getDebugLevel() >= SharedConstants.DEBUG_LEVEL_LOW)
        DebugConsole.show("DEBUG: TouchSensorThread created (port: "
          + getPortLabel() + ")");
    }

    public void run()
    {
      if (LegoRobot.getDebugLevel() >= SharedConstants.DEBUG_LEVEL_LOW)
        DebugConsole.show("DEBUG: TouchSensorThread "
          + Thread.currentThread().getName()
          + " started (port: " + getPortLabel() + ")");

      isRunning = true;
      while (isRunning)
      {
        if (touchListener != null)
        {
          delay(pollDelay);
          boolean isActuated = isActuated();
          if (state == SensorState.RELEASED && isActuated)
          {
            if (inCallback)
            {
              if (LegoRobot.getDebugLevel() >= SharedConstants.DEBUG_LEVEL_LOW)
                DebugConsole.show("DEBUG: Touch event 'pressed' (port: "
                  + getPortLabel() + ") -------- rejected: Other callback underway!");
            }
            else
            {
              inCallback = true;
              if (LegoRobot.getDebugLevel() >= SharedConstants.DEBUG_LEVEL_LOW)
                DebugConsole.show("DEBUG: Touch event 'pressed' (port: "
                  + getPortLabel() + ")");
              touchListener.pressed(getPort());
              state = SensorState.PRESSED;
              inCallback = false;
            }
          }
          if (state == SensorState.PRESSED && !isActuated)
          {
            if (inCallback)
            {
              if (LegoRobot.getDebugLevel() >= SharedConstants.DEBUG_LEVEL_LOW)
                DebugConsole.show("DEBUG: Touch event 'released' (port: "
                  + getPortLabel() + ") -------- rejected: Other callback underway!");
            }
            else
            {
              inCallback = true;
              if (LegoRobot.getDebugLevel() >= SharedConstants.DEBUG_LEVEL_LOW)
                DebugConsole.show("DEBUG: Touch event 'released' (port: "
                  + getPortLabel() + ")");
              touchListener.released(getPort());
              state = SensorState.RELEASED;
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
          DebugConsole.show("DEBUG: TouchSendorThread stopping failed (port: "
            + getPortLabel() + ")");
        else
          DebugConsole.show("DEBUG: TouchSensorThread successfully stopped (port: "
            + getPortLabel() + ")");
    }
  }
// -------------- End of inner classes -----------------------
//
  private TouchListener touchListener = null;
  private TouchSensorThread tst;
  private int state = SensorState.RELEASED;
  private int pollDelay;
  private volatile static boolean inCallback = false;

  /**
   * Creates a sensor instance connected to the given port.
   * @param port the port where the sensor is plugged-in
   */
  public TouchSensor(SensorPort port)
  {
    super(port);
    tst = new TouchSensorThread();
    EV3Properties props = LegoRobot.getProperties();
    pollDelay = props.getIntValue("TouchSensorPollDelay");
    partName = "ts" + (port.getId() + 1);
  }

  /**
   * Creates a sensor instance connected to port S1.
   */
  public TouchSensor()
  {
    this(SensorPort.S1);
  }

  // Called by connect() if part is alread added.
  // Called by addPart() if already connected
  protected void init()
  {
    if (LegoRobot.getDebugLevel() >= SharedConstants.DEBUG_LEVEL_MEDIUM)
      DebugConsole.show("DEBUG: TouchSensor.init() called (Port: "
        + getPortLabel() + ")");
  }

  protected void cleanup()
  {
    if (LegoRobot.getDebugLevel() >= SharedConstants.DEBUG_LEVEL_MEDIUM)
      DebugConsole.show("DEBUG: TouchSensor.cleanup() called (Port: "
        + getPortLabel() + ")");
    if (tst != null)
      tst.stopThread();
  }

  /**
   * Registers the given touch listener.
   * If the touch thread is not yet started, start it now.
   * @param touchListener the TouchListener to become registered.
   */
  public void addTouchListener(TouchListener touchListener)
  {
    this.touchListener = touchListener;
    if (!tst.isAlive())
      startTouchThread();
  }

  protected void startTouchThread()
  {
    tst.start();
  }

  protected void stopTouchThread()
  {
    if (tst.isAlive())
      tst.stopThread();
  }

  protected boolean getValue(boolean check)
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
    return (v == 1);
  }
 
  
  protected boolean isActuated()
  {
    if (robot == null || !robot.isConnected())
      return false;
    return getValue(false);
  }

  /**
   * Polls the sensor.
   * @return true, if actuated; otherwise false
   */
  public boolean isPressed()
  {
    delay(1);
    return getValue(true);
  }

  private void checkConnect()
  {
    if (robot == null)
      new ShowError("TouchSensor (port: " + getPortLabel()
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
}
