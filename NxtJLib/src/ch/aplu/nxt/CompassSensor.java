// CompassSensor.java
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

/**
 * Class that represents an Compass sensor.
 * Most of the code and the documentation
 * taken from the leJOS library (lejos.sourceforge.net, with thanks to the author.
 */
public class CompassSensor extends I2CSensor
{
  private interface SensorState // Simulate enum for J2ME compatibility
  {
    int TOLEFT = 0;
    int TORIGHT = 1;
  }

// -------------- Inner class CompassSensorThread ------------
  private class CompassSensorThread extends NxtThread
  {
    private volatile boolean isRunning = false;

    private CompassSensorThread()
    {
      if (LegoRobot.getDebugLevel() >= DEBUG_LEVEL_LOW)
        DebugConsole.show("DEBUG: CompassSensorThread created (port: "
          + getPortLabel() + ")");
    }

    public void run()
    {
      if (LegoRobot.getDebugLevel() >= DEBUG_LEVEL_LOW)
        DebugConsole.show("DEBUG: CompassSensorThread "
          + Thread.currentThread().getName()
          + " started (port: " + getPortLabel() + ")");

      isRunning = true;
      while (isRunning)
      {
        if (compassListener != null)
        {
          delay(pollDelay);
          double level = getLevel();
          if (state == SensorState.TOLEFT && isRight(level, triggerLevel))
          {
            if (inCallback)
            {
              if (LegoRobot.getDebugLevel() >= DEBUG_LEVEL_LOW)
                DebugConsole.show("DEBUG: Compass event 'toLeft' (port: "
                  + getPortLabel() + ") -------- rejected: Other callback underway!");
            }
            else
            {
              inCallback = true;
              if (LegoRobot.getDebugLevel() >= DEBUG_LEVEL_LOW)
                DebugConsole.show("DEBUG: Compass event 'toRight' (port: "
                  + getPortLabel() + ")");
              compassListener.toRight(getPort(), level);
              state = SensorState.TORIGHT;
              inCallback = false;
            }
          }
          if (state == SensorState.TORIGHT && !isRight(level, triggerLevel))
          {
            if (inCallback)
            {
              if (LegoRobot.getDebugLevel() >= DEBUG_LEVEL_LOW)
                DebugConsole.show("DEBUG: Ultrasonci event 'toRight' (port: "
                  + getPortLabel() + ") -------- rejected: Other callback underway!");
            }
            else
            {
              inCallback = true;
              if (LegoRobot.getDebugLevel() >= DEBUG_LEVEL_LOW)
                DebugConsole.show("DEBUG: Compass event 'toRight' (port: "
                  + getPortLabel() + ")");
              compassListener.toLeft(getPort(), level);
              state = SensorState.TOLEFT;
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
          DebugConsole.show("DEBUG: CompassSendorThread stopping failed (port: "
            + getPortLabel() + ")");
        else
          DebugConsole.show("DEBUG: CompassSensorThread successfully stopped (port: "
            + getPortLabel() + ")");
    }

    private boolean isRight(double level, double triggerLevel)
    {
      if (triggerLevel <= 180)
      {
        if (level >= triggerLevel && level < 180 + triggerLevel)
          return true;
        return false;
      }
      else
      {
        if (level > triggerLevel - 180 && level < triggerLevel)
          return false;
        return true;
      }
    }
  }
// -------------- End of inner classes -----------------------
  private volatile static boolean inCallback = false;
  private CompassListener compassListener = null;
  private CompassSensorThread cst;
  private int state = SensorState.TOLEFT;
  private int triggerLevel;
  private int pollDelay;
  private boolean isCalibrating = false;
  private boolean isMindsensor;
  private final String MINDSENSORS_ID = "mndsnsrs";
  private boolean is50;
  // Mindsensors Compass Commands (from www.mindsensors.com):
  private final byte AUTO_TRIG_ON = 0x41; // On by default
  private final byte AUTO_TRIG_OFF = 0x53;
  private final byte BYTE_MODE = 0x42;
  private final byte INTEGER_MODE = 0x49;
  private final byte USA_MODE = 0x55; // 60 Hz
  private final byte EU_MODE = 0x45; // 50 Hz
  private final byte ADPA_ON = 0x4E;
  private final byte ADPA_OFF = 0x4F; // Off by default
  private final byte BEGIN_CALIBRATION = 0x43;
  private final byte END_CALIBRATION = 0x44;
  private final byte LOAD_USER_CALIBRATION = 0x4C;  // Mindsensors Compass Registers:
  private final byte COMMAND = 0x41;
  private final byte HEADING_LSB = 0x42;
  private final byte HEADING_MSB = 0x43;

  /**
   * Creates a sensor instance connected to the given port at location with either
   * 50 Hz or 60 Hz household current frequency.
   * @param port the port where the sensor is plugged-in
   * @param is50 true, if current frequency is 50 Hz (e.g. Europe) false, if current frequency is 60 Hz (e.g. USA)
   */
  public CompassSensor(SensorPort port, boolean is50)
  {
    super(port, LOWSPEED_9V);
    this.is50 = is50;
    cst = new CompassSensorThread();
    NxtProperties props = LegoRobot.getProperties();
    pollDelay = props.getIntValue("CompassSensorPollDelay");
  }

  /**
   * Creates a sensor instance connected to the given port at location with
   * 50 Hz household current frequency (e.g. Europe).
   * @param port the port where the sensor is plugged-in
   */
  public CompassSensor(SensorPort port)
  {
    this(port, true);
  }

  /**
   * Creates a sensor instance connected to port S1 at location with
   * 50 Hz household current frequency (e.g. Europe).
   */
  public CompassSensor()
  {
    this(SensorPort.S1);
  }

  protected void init()
  {
    super.init();
    if (LegoRobot.getDebugLevel() >= DEBUG_LEVEL_MEDIUM)
      DebugConsole.show("DEBUG: CompassSensor.init() called (Port: "
        + getPortLabel() + ")");

    isMindsensor = getProductID().equals(MINDSENSORS_ID);

    if (isMindsensor)
    {
      if (is50)
        // Set to USA or EU household current frequency
        setRegion(EU_MODE);
      else
        setRegion(USA_MODE);
    }
  }

  protected void cleanup()
  {
    if (LegoRobot.getDebugLevel() >= DEBUG_LEVEL_MEDIUM)
      DebugConsole.show("DEBUG: Compass.cleanup() called (Port: "
        + getPortLabel() + ")");
    if (cst != null)
      cst.stopThread();
  }

  /**
   * Registers the given compass listener for the given trigger level.
   * @param compassListener the CompassListener to become registered.
   * @param triggerLevel the trigger level where the callback is triggered
   */
  public void addCompassListener(CompassListener compassListener, int triggerLevel)
  {
    this.compassListener = compassListener;
    this.triggerLevel = triggerLevel;
    if (!cst.isAlive())
      startCompassThread();
  }

  /**
   * Registers the given compass listener with default trigger level 180.
   * @param compassListener the CompassListener to become registered.
   */
  public void addCompassListener(CompassListener compassListener)
  {
    addCompassListener(compassListener, 180);
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

  protected void startCompassThread()
  {
    cst.start();
  }

  protected void stopCompassThread()
  {
    if (cst.isAlive())
      cst.stopThread();
  }

  private double getDegrees(boolean check)
  {
    if (check)
      checkConnect();
    byte[] buf = getData(HEADING_LSB, 2);

    if (isMindsensor)  // Mindstorm sensor
    {
      int iHeading = (0xFF & buf[0]) | ((0xFF & buf[1]) << 8);
      float dHeading = iHeading / 10.00F;
      return dHeading;
    }
    else
    { // HiTechnic sensor
      return ((buf[0] & 0xff) << 1) + buf[1];
    }
  }

  private double getLevel()
  {
    if (robot == null || !robot.isConnected())
      return -1;
    return getDegrees(false);
  }

  /**
   * Polls the sensor.
   * @return the current compass reading (0..<360 clockwise, 0 to north)
   */
  public double getDegrees()
  {
    delay(10);
    return getDegrees(true);
  }

  /**
   * Starts calibration process.
   * Needs at least 2 full revolutions with at least 20 s per rotation
   * before stopCalibration() is called.
   * The compass sensor should be in a horizontal position with no ferromagnetica
   * in the neighbourhood (e.g. floor heating tubes).
   * @see #stopCalibration()
   */
  public void startCalibration()
  {
    checkConnect();
    if (isCalibrating)
      return;
    isCalibrating = true;
    sendData(COMMAND, BEGIN_CALIBRATION);
  }

  /**
   * Stops calibration process.
   * @see #startCalibration()
   */
  public void stopCalibration()
  {
    checkConnect();
    if (!isCalibrating)
      return;
    isCalibrating = false;
    sendData(COMMAND, END_CALIBRATION);
  }

  private void checkConnect()
  {
    if (robot == null)
      new ShowError("CompassSensor (port: " + getPortLabel()
        + ") is not a part of the NxtRobot.\n"
        + "Call addPart() to assemble it.");
  }

  private void setRegion(byte region)
  {
    sendData(COMMAND, region);
  }

  /**
   * Overrides I2CSensor.getData() because of unreliability when retrieving more than a single
   * byte at a time with some I2C sensors.
   */
  public byte[] getData(byte register, int length)
  {
    byte[] result = new byte[length];
    for (int i = 0; i < length; i++)
      result[i] = super.getData((byte)(register + i), 1)[0];
    return result;
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
