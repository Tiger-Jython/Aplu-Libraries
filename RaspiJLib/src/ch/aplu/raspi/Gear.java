// Gear.java

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
 Class that represents the combination of two motors on an axis
 to perform a car-like movement.
 */
public class Gear extends Part
{
  // ----------   Interface GearState  -------------------
  interface GearState
  {
    int FORWARD = 0;
    int BACKWARD = 1;
    int STOPPED = 2;
    int LEFT = 3;
    int RIGHT = 4;
    int LEFTARC = 5;
    int RIGHTARC = 6;
    int UNDEFINED = 7;
  }
  // ------------------------------------------------------

  private Robot robot;
  private int state;
  private String device;
  private int speed;
  private int arcRadius;

  /**
   * Creates a gear instance.
   */
  public Gear()
  {
    RaspiProperties props = Robot.getProperties();
    speed = props.getIntValue("GearSpeed");
    state = GearState.UNDEFINED;
    device = "gear";
    arcRadius = 0;
    Robot r = RobotInstance.getRobot();
    if (r == null)  // Defer setup() until robot is created
      RobotInstance.partsToRegister.add(this);
    else
      setup(r);
  }

  protected void setup(Robot robot)
  {
    robot.sendCommand(device + ".create");
    robot.sendCommand(device + ".setSpeed." + speed);
    this.robot = robot;
  }

  public void setSpeed(int speed)
  {
    if (this.speed == speed)
      return;
    this.speed = speed;
    robot.sendCommand(device + ".setSpeed." + speed);
    state = GearState.UNDEFINED;
  }

  public void forward()
  {
    forward(0);
  }

  public void forward(int duration)
  {
    if (state == GearState.FORWARD)
      return;
    if (duration == 0)
    {
      robot.sendCommand(device + ".forward");
      state = GearState.FORWARD;
    }
    else
    {
      robot.sendCommand(device + ".forward." + duration);
      state = GearState.STOPPED;
    }
  }

  public void backward()
  {
    backward(0);
  }

  public void backward(int duration)
  {
    if (state == GearState.BACKWARD)
      return;
    if (duration == 0)
    {
      robot.sendCommand(device + ".backward");
      state = GearState.BACKWARD;
    }
    else
    {
      robot.sendCommand(device + ".backward." + duration);
      state = GearState.STOPPED;
    }
  }

  public void left()
  {
    left(0);
  }

  public void left(int duration)
  {
    if (state == GearState.LEFT)
      return;
    if (duration == 0)
    {
      robot.sendCommand(device + ".left");
      state = GearState.LEFT;
    }
    else
    {
      robot.sendCommand(device + ".left." + duration);
      state = GearState.STOPPED;
    }
  }

  public void right()
  {
    right(0);
  }

  public void right(int duration)
  {
    if (state == GearState.RIGHT)
      return;
    if (duration == 0)
    {
      robot.sendCommand(device + ".right");
      state = GearState.RIGHT;
    }
    else
    {
      robot.sendCommand(device + ".right." + duration);
      state = GearState.STOPPED;
    }
  }

  public void leftArc(double radius)
  {
    leftArc(radius, 0);
  }

  public void leftArc(double radius, int duration)
  {
    int _radius = (int)(1000 * radius);
    if (duration == 0)
    {
      if (state == GearState.LEFTARC && _radius == arcRadius)
        return;
      arcRadius = _radius;
      robot.sendCommand(device + ".leftArcMilli." + _radius);
      state = GearState.LEFTARC;
    }
    else
    {
      robot.sendCommand(device + ".leftArcMilli." + _radius + "." + duration);
      state = GearState.STOPPED;
    }
  }

  public void rightArc(double radius)
  {
    rightArc(radius, 0);
  }

  public void rightArc(double radius, int duration)
  {
    System.out.println("rightArc with r " + radius);
    int radius_ = (int)(1000 * radius);
    if (duration == 0)
    {
      if (state == GearState.RIGHTARC && radius_ == arcRadius)
        return;
      arcRadius = radius_;
      robot.sendCommand(device + ".rightArcMilli." + radius_);
      System.out.println("sendCommand _radius " + radius_);
      state = GearState.RIGHTARC;
    }
    else
    {
      robot.sendCommand(device + ".rightArcMilli." + radius_ + "." + duration);
      state = GearState.STOPPED;
    }
  }

  public void stop()
  {
    if (state == GearState.STOPPED)
      return;
    robot.sendCommand(device + ".stop");
    state = GearState.STOPPED;
  }

  private void checkRobot()
  {
    if (robot == null)
      new ShowError("Fatal error while creating Gear.\nCreate Robot instance first");
  }
}
