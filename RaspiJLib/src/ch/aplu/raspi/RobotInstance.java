// RobotInstance.java

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

import java.util.ArrayList;

/**
 Holder of global Robot instance
 */
public class RobotInstance
{
  private static Robot myRobot = null;
  protected static ArrayList<Part> partsToRegister = new ArrayList<Part>();

  public static void setRobot(Robot robot)
  {
    myRobot = robot;
  }

  public static Robot getRobot()
  {
    return myRobot;
  }
}
