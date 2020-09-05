// SingleLed.java

/*
 This software is part of the RaspiSim library.
 It is Open Source Free Software, so you may
 - run the code for any purpose
 - study how the code works and adapt it to your needs
 - integrate all or parts of the code in your own programs
 - redistribute copies of the code
 - improve the code and release your improvements to the public
 However the use of the code is entirely your responsibility.

 Author: Aegidius Pluess, www.aplu.ch
 */
package ch.aplu.raspisim;

import ch.aplu.jgamegrid.Location;
import java.awt.image.BufferedImage;

class SingleLed extends Part
{
  public SingleLed(BufferedImage bi, Location pos)
  {
    super(bi, pos);
    Robot robot = RobotInstance.getRobot();
    robot.addPart(this);
  }

  public SingleLed(String filename, Location pos)
  {
    super(filename, pos);
    Robot robot = RobotInstance.getRobot();
    robot.addPart(this);
  }
  
  protected void cleanup()
  {
  }
  
}
