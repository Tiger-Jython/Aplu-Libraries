// LightSensor.java

/*
 This software is part of the RaspiJLib library.
 It is Open Source Free Software, so you may
 - run the code for any purpose
 - study how the code works and adapt it to your needs
 - integrate all or parts of the code in your own programs
 - redistribute copies of the code
 - improve the code and release your improvements to the public
 However the use of the code is entirely your responsibility.
 */
package ch.aplu.raspi;

/**
 Class that represents a light sensor.
 */
public class LightSensor extends Part
{
  enum SensorState
  {
    DARK, BRIGHT
  }

  private Robot robot;
  private String device;
  protected int id;
  protected SensorState sensorState = SensorState.DARK;
  protected LightListener lightListener = null;
  protected int triggerLevel;

  /** Constant for id of left front sensor */
  public static int LS_FRONT_LEFT = 0;
  /** Constant for id of right front sensor */
  public static int LS_FRONT_RIGHT = 1;
  /** Constant for id of left rear sensor */
  public static int LS_REAR_LEFT = 2;
  /** Constant for id of right rear sensor */
  public static int LS_REAR_RIGHT = 3;

  /**
   * Creates a light sensor instance with given id.
   * IDs: 0: front left, 1: front right, 2: rear left, 3: rear right
   * The following global constants are defined:
   * LS_FRONT_LEFT = 0, LS_FRONT_RIGHT = 1, LS_REAR_LEFT = 2, LS_REAR_RIGHT = 3.
   * @param id the LightSensor identifier
   */
  public LightSensor(int id)
  {
    this.id = id;
    device = "lss" + id;
    Robot r = RobotInstance.getRobot();
    if (r == null)  // Defer setup() until robot is created
      RobotInstance.partsToRegister.add(this);
    else
      setup(r);
  }

  protected void setup(Robot robot)
  {
    robot.sendCommand(device + ".create");
    this.robot = robot;
  }

  /**
   * Returns the current intensity value (0..255).
   * @return the measured light intensity
   */
  public int getValue()
  {
    checkRobot();
    Tools.delay(1);
    String rc = robot.sendCommand(device + ".getValue");
    int v = -1;
    try
    {
      v = Integer.parseInt(rc);
    }
    catch (NumberFormatException ex)
    {
    }
    return v;
  }

  /**
   * Registers the given light listener for the given trigger level.
   * @param lightListener the LightListener to get registered
   * @param triggerLevel the trigger level where the callback is triggered
   */
  public void addLightListener(LightListener lightListener, int triggerLevel)
  {
    this.lightListener = lightListener;
    this.triggerLevel = triggerLevel;
  }

  /**
   * Registers the given light listener with default trigger level 500.
   * @param lightListener the LightListener to get registered.
   */
  public void addLightListener(LightListener lightListener)
  {
    addLightListener(lightListener, 500);
  }

  /**
   * Sets a new trigger level.
   * @param level the new level
   */
  public void setTriggerLevel(int level)
  {
    triggerLevel = level;
  }

  private void checkRobot()
  {
    if (robot == null)
      new ShowError("Create Robot instance first");
  }
}
