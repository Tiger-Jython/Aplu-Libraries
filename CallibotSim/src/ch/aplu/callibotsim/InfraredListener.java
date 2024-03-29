// InfraredListener.java

/*
This software is part of the MbRobotSim library.
It is Open Source Free Software, so you may
- run the code for any purpose
- study how the code works and adapt it to your needs
- integrate all or parts of the code in your own programs
- redistribute copies of the code
- improve the code and release your improvements to the public
However the use of the code is entirely your responsibility.

Author: Aegidius Pluess, www.aplu.ch
*/

package ch.aplu.callibotsim;

/**
 * Interface with declarations of callback methods for Infrared sensor
 */
public interface InfraredListener extends java.util.EventListener
{
  /**
   * Called when (reflected) infrared light is newly detected.
   * @param id the id of the infrared sensor
   */
   public void activated(int id);

   /**
   * Called when no (reflected) infrared light is newly detected.
   * @param id the id of the light sensor
   */
   public void passivated(int id);
}
