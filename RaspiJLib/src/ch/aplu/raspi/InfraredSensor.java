// InfraredSensor.java

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
 Class that represents an infrared sensor.
 */
public class InfraredSensor extends Part
{
  enum SensorState
  {
    PASSIVATED, ACTIVATED
  }

  private Robot robot;
  private String device;
  protected int id;
  protected SensorState sensorState = SensorState.PASSIVATED;
  protected InfraredListener infraredListener = null;

  /** Constant for id of front sensor */
  public static int IR_CENTER = 0;
  /** Constant for id of left sensor */
  public static int IR_LEFT = 1;
  /** Constant for id of right  sensor */
  public static int IR_RIGHT = 2;
  /** Constant for id of line left sensor (points to floor) */
  public static int IR_LINE_LEFT = 3;
  /** Constant for id of line right sensor (points to floor) */
  public static int IR_LINE_RIGHT = 4;

  /**
   * Creates an infrared sensor at given port.
   * For the Pi2Go the following infrared sensors are used:
   * id = 0: front center; id = 1: front left; id = 2: front right;
   * id = 3: line left; id = 4: line right. The following global constants are defined:
   * IR_CENTER = 0, IR_LEFT = 1, IR_RIGHT = 2, IR_LINE_LEFT = 3, 
   * IR_LINE_RIGHT = 4.
   * @param id the InfraredSensor identifier
   */
  public InfraredSensor(int id)
  {
    this.id = id;
    device = "irs" + id;
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
   * Checks, if infrared light ist detected.
   * @return 1, if the sensor detects infrared light; otherwise 0
   */
  public int getValue()
  {
    Tools.delay(1);
    checkRobot();
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
   * Registers the given IR listener.
   * @param infraredListener the InfraredListener to register.
   */
  public void addInfraredListener(InfraredListener infraredListener)
  {
    checkRobot();
    this.infraredListener = infraredListener;
    robot.registerSensor(this);
  }

  private void checkRobot()
  {
    if (robot == null)
      new ShowError("Fatal error while creating InfraredSensor.\nCreate Robot instance first");
  }
}
