// Ultrasonic.java

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

import java.awt.Color;

/**
 Class that represents an ultrasonic sensor.
 */
public class UltrasonicSensor extends Part
{
  enum SensorState
  {
    NEAR, FAR
  }

  private Robot robot;
  private String device;
  protected SensorState sensorState = SensorState.FAR;
  protected UltrasonicListener ultrasonicListener = null;
  protected double triggerLevel;

  /**
   * Creates an ultrasonic sensor instance.
   */
  public UltrasonicSensor()
  {
    device = "uss";
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
   * Performs a measurement and reports the result.
   * @return the distance in cm, -1 if no reflection detected
   */
  public double getValue()
  {
    String rc = robot.sendCommand(device + ".getValue");
    double v = -1;
    try
    {
      v = Double.parseDouble(rc);
    }
    catch (NumberFormatException ex)
    {
    }
    return v;
  }

  /**
   * Performs a measurement and reports the result.
   * @return the distance in cm formatted to two decimals, -1.00 if no reflection detected
   */
  public String getDistance()
  {
    return robot.sendCommand(device + ".getDistance");
  }

  /**
   * Registers the given ultrasonic listener for the given trigger level.
   * @param ultrasonicListener the UltrasonicListener to get registered
   * @param triggerLevel the trigger level where the callback is triggered
   */
  public void addUltrasonicListener(UltrasonicListener ultrasonicListener, int triggerLevel)
  {
    this.ultrasonicListener = ultrasonicListener;
    this.triggerLevel = triggerLevel;
    robot.registerSensor(this);
  }

  /**
   * Registers the given ultrasonic listener with default trigger level 20.
   * @param ultrasonicListener the LightListener to get registered.
   */
  public void addUltrasonicListener(UltrasonicListener ultrasonicListener)
  {
    addUltrasonicListener(ultrasonicListener, 20);
  }

  /**
   * Sets a new trigger level.
   * @param level the new level
   */
  public void setTriggerLevel(double level)
  {
    triggerLevel = level;
  }

  private void checkRobot()
  {
    if (robot == null)
      new ShowError("Fatal error while creating UltrasonicSensor.\nCreate Robot instance first");
  }
  
    /**
   * Sets the color of the triangle mesh lines. 
   * Empty method for compatibility with RaspiSim.
   * @param color the color of the mesh
   */
  public void setMeshTriangleColor(Color color)
  {
  }

  /**
   * Sets the color of the beam area (two sector border lines and axis).
   * Empty method for compatibility with RaspiSim.
   * @param color the color of the beam area
   */
  public void setBeamAreaColor(Color color)
  {
  }

  /**
   * Erases the beam area (if it is currently shown).
   * Empty method for compatibility with RaspiSim.
   */
  public void eraseBeamArea()
  {
  }

  /**
   * Sets the color of the circle with center at sensor location and radius
   * equals to the current distance value. If value = 0, no circle is shown.
   * Empty method for compatibility with RaspiSim.
   * @param color the color of the circle
   */
  public void setProximityCircleColor(Color color)
  {
  }
  
}
