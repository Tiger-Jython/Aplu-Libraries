// NxtUltrasonicSensor.java

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
 * Class that represents a NXT ultrasonic sensor.
 */
public class NxtUltrasonicSensor extends Sensor
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
      EV3Properties props = LegoRobot.getProperties();
      int ultrasonicPollDelay = props.getIntValue("UltrasoniSensorcPollDelay");
      if (LegoRobot.getDebugLevel() >= SharedConstants.DEBUG_LEVEL_LOW)
        DebugConsole.show("UlTh started");

      isRunning = true;
      while (isRunning)
      {
        if (!isEvtEnabled)
          continue;

        if (ultrasonicListener != null)
        {
          Tools.delay(ultrasonicPollDelay);
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

  private lejos.hardware.sensor.NXTUltrasonicSensor us;
  private UltrasonicListener ultrasonicListener = null;
  private int state = SensorState.NEAR;
  private int triggerLevel;
  private volatile static boolean inCallback = false;
  private UltrasonicSensorThread ust;
  private volatile static boolean isEvtEnabled = false;

  /**
   * Creates a sensor instance connected to the given port.
   * @param port the port where the sensor is plugged-in
   */
  public NxtUltrasonicSensor(SensorPort port)
  {
    super(port);
    ust = new UltrasonicSensorThread();
    us = new lejos.hardware.sensor.NXTUltrasonicSensor(getLejosPort());
    sm = us.getMode(0);  // Distance mode
  }

  /**
   * Creates a sensor instance connected to port S1.
   */
  public NxtUltrasonicSensor()
  {
    this(SensorPort.S1);
  }

  protected void init()
  {
    if (LegoRobot.getDebugLevel() >= SharedConstants.DEBUG_LEVEL_MEDIUM)
      DebugConsole.show("_ul.init()");
  }

  protected void cleanup()
  {
    if (LegoRobot.getDebugLevel() >= SharedConstants.DEBUG_LEVEL_MEDIUM)
      DebugConsole.show("_ul.cleanup()");
    isEvtEnabled = false;  // Disable events
    if (ust != null)
      ust.stopThread();
    us.close();
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
    {
      ust.start();
      // Wait a moment until enabling events, otherwise crash may result
      Tools.delay(LegoRobot.getSensorEventDelay());
      isEvtEnabled = true;
    }
  }

  /**
   * Registers the given ultrasonic listener with default trigger level 20.
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

  private int getLevel()
  {
    if (robot == null)
      return -1;
    return (int)(100 * getSensorValue());
  }

  private float getSensorValue()
  {
    int sampleSize = 1;
    float samples[] = new float[sampleSize];
    sm.fetchSample(samples, 0);
    return samples[0];
  }

  /**
   * Polls the sensor.
   * @return the current value the sensor reported (in cm), 255 if the
   * sensor does not detect a target. 
   */
  public int getDistance()
  {
    checkConnect();
    float v = getSensorValue();
    if (v == Float.POSITIVE_INFINITY)
      return 255;
    return (int)(100 * v);
  }

  /**
   * Returns the reference of the the underlying lejos.hardware.sensor.NXTUltrasonicSensor.
   * @return the reference of the lejos.hardware.sensor.NXTUltrasonicSensor
   */
  public lejos.hardware.sensor.NXTUltrasonicSensor getLejosSensor()
  {
    return us;
  }

  private void checkConnect()
  {
    if (robot == null)
      new ShowError("NxtUltrasonicSensor (port: " + getPortLabel()
        + ") is not a part of the LegoRobot.\n"
        + "Call addPart() to assemble it.");
  }
}
