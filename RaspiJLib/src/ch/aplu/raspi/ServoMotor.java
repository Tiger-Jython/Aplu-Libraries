// ServoMotor.java

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
 Class that represents a motor.
 */
public class ServoMotor extends Part
{

  private Robot robot;
  private int id;
  private int home;
  private int inc;
  private String device;

  /**
   * Creates a servo motor instance with given id.
   * id selects header (port): 0: S12, 1: S13, 2: S14, 3: S15 -
   * @param id the id of the motor
   * @param home the PWM duty cycle for the home position
   * @param inc the increment factor (inc_duty/inc_position)
   */
  public ServoMotor(int id, int home, int inc)
  {
    this.id = id;
    this.home = home;
    this.inc = inc;
    device = "svo" + id;
    Robot r = RobotInstance.getRobot();
    if (r == null)  // Defer setup() until robot is created
      RobotInstance.partsToRegister.add(this);
    else
      setup(r);
  }

  protected void setup(Robot robot)
  {
   robot.sendCommand(device + ".create." + home + "." + inc);
   this.robot = robot;
  }

  /**
   Sets the relative position of the servo motor.
   @param position the position with respect to home and using the inc_duty/inc_position factor
   For most servo motors in range -200 .. 200
   */
  public void setPos(int position)
  {
    checkRobot();
    robot.sendCommand(device + ".setPos." + position);
  }

  /**
   Sets the absolute position of the servo motor.
   @param position the position in arbitrary units in 
   range 0..4095 (determines PWM duty cycle)
   For most servo motors in range 100..500
   */
  public void setPosAbs(int position)
  {
    checkRobot();
    robot.sendCommand(device + ".setPosAbs." + position);
  }

  private void checkRobot()
  {
    if (robot == null)
      new ShowError("Create Robot instance first");
  }
}
