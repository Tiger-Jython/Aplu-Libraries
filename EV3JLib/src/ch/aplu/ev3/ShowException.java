// ShowException.java
// Direct mode

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

import ch.aplu.util.*;

class ShowException implements ExitListener
{
  private LegoRobot robot;
  
  public ShowException(LegoRobot robot)
  {
    this.robot = robot;
    Console c = new Console();
    c.addExitListener(this);
  }  

  public void show(Exception ex)
  {
    ex.printStackTrace();
    Monitor.putSleep();  // Do never wake up
  }
  
  public void notifyExit()
  {
    robot.exit();
  }
}

