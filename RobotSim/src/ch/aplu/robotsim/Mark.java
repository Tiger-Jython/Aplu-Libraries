// Mark.java

/*
This software is part of the RobotSim library.
It is Open Source Free Software, so you may
- run the code for any purpose
- study how the code works and adapt it to your needs
- integrate all or parts of the code in your own programs
- redistribute copies of the code
- improve the code and release your improvements to the public
However the use of the code is entirely your responsibility.

Author: Aegidius Pluess, www.aplu.ch
*/

package ch.aplu.robotsim;

import ch.aplu.jgamegrid.*;

/**
 * Class to represent a mark to show the current rotation center (in trace mode only).
 */
public class Mark extends Actor
{
  /**
   * Creates a mark.
   */
  public Mark()
  {
    super("sprites/rotcenter.gif");
  }

}
