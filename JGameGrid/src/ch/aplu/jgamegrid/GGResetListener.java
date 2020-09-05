// GGResetListener.java

/*
This software is part of the JGameGrid package.
It is Open Source Free Software, so you may
- run the code for any purpose
- study how the code works and adapt it to your needs
- integrate all or parts of the code in your own programs
- redistribute copies of the code
- improve the code and release your improvements to the public
However the use of the code is entirely your responsibility.

Author: Aegidius Pluess, www.aplu.ch
 */
package ch.aplu.jgamegrid;

/**
 * Declarations of the notification method called when the reset button is hit.
 */
public interface GGResetListener extends java.util.EventListener
{
  /**
   * Event callback method called when the reset button is hit.
   * @return true, if the event is consumed, so doReset() will not be called
   * and any following registered GGResetListener callback will not be fired
   */
  public boolean resetted();
}
