// NxtTouchSensor.java

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
public class NxtTouchSensor extends Sensor
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
        DebugConsole.show("TsTh created");
    }

    public void run()
    {
      EV3Properties props = LegoRobot.getProperties();
      int touchPollDelay = props.getIntValue("TouchSensorPollDelay");
      if (LegoRobot.getDebugLevel() >= SharedConstants.DEBUG_LEVEL_LOW)
        DebugConsole.show("TsThread started");

      isRunning = true;
      while (isRunning)
      {
        if (!isEvtEnabled)
          continue;

        if (touchListener != null)
        {
          Tools.delay(touchPollDelay);
          boolean isActuated = isActuated();
          if (state == SensorState.RELEASED && isActuated)
          {
            if (inCallback)
            {
              if (LegoRobot.getDebugLevel() >= SharedConstants.DEBUG_LEVEL_LOW)
                DebugConsole.show("Evt'pressed'(rej)");
            }
            else
            {
              inCallback = true;
              if (LegoRobot.getDebugLevel() >= SharedConstants.DEBUG_LEVEL_LOW)
                DebugConsole.show("Evt'pressed'(" + getPortLabel() + ")");
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
                DebugConsole.show("Evt'rleasd'(rej)");
            }
            else
            {
              inCallback = true;
              if (LegoRobot.getDebugLevel() >= SharedConstants.DEBUG_LEVEL_LOW)
                DebugConsole.show("Evt'rleasd'(" + getPortLabel() + ")");
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
          DebugConsole.show("TsTh stop failed");
        else
          DebugConsole.show("TsTh stop ok");
    }
  }
// -------------- End of inner classes -----------------------

  private lejos.hardware.sensor.NXTTouchSensor ts;
  private TouchListener touchListener = null;
  private int state = SensorState.RELEASED;
  private volatile static boolean inCallback = false;
  private TouchSensorThread tst;
  private volatile static boolean isEvtEnabled = false;

  /**
   * Creates a sensor instance connected to the given port.
   * @param port the port where the sensor is plugged-in
   */
  public NxtTouchSensor(SensorPort port)
  {
    super(port);
    tst = new TouchSensorThread();
    ts = new lejos.hardware.sensor.NXTTouchSensor(getLejosPort());
  }

  /**
   * Creates a sensor instance connected to port S1.
   */
  public NxtTouchSensor()
  {
    this(SensorPort.S1);
  }

  protected void init()
  {
    if (LegoRobot.getDebugLevel() >= SharedConstants.DEBUG_LEVEL_MEDIUM)
      DebugConsole.show("_ts.init()");
  }

  protected void cleanup()
  {
    if (LegoRobot.getDebugLevel() >= SharedConstants.DEBUG_LEVEL_MEDIUM)
      DebugConsole.show("_ts.cleanup()");
    isEvtEnabled = false;  // Disable events
    if (tst != null)
      tst.stopThread();
    ts.close();
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
    {
      tst.start();
      // Wait a moment until enabling events, otherwise crash may result
      Tools.delay(LegoRobot.getSensorEventDelay());
      isEvtEnabled = true;
    }
  }

  protected boolean isActuated()
  {
    if (robot == null)
      return false;
    return !((int)getSensorValues()[0] == 0);
  }

  private float[] getSensorValues()
  {
    int size = ts.sampleSize();
    float[] values = new float[size];
    ts.fetchSample(values, 0);
    return values;
  }

  /**
   * Polls the sensor.
   * @return 1, if actuated; otherwise 0
   */
  public int getValue()
  {
    checkConnect();
    return (int)getSensorValues()[0];
  }

  /**
   * Polls the sensor.
   * @return true, if actuated; otherwise false
   */
  public boolean isPressed()
  {
    checkConnect();
    return !((int)getSensorValues()[0] == 0);
  }

  /**
   * Returns the reference of the the underlying lejos.hardware.sensor.NXTTouchSensor.
   * @return the reference of the lejos.hardware.sensor.NXTTouchSensor
   */
  public lejos.hardware.sensor.NXTTouchSensor getLejosSensor()
  {
    return ts;
  }

  private void checkConnect()
  {
    if (robot == null)
      new ShowError("NxtTouchSensor (port: " + getPortLabel()
        + ") is not a part of the LegoRobot.\n"
        + "Call addPart() to assemble it.");
  }
}
