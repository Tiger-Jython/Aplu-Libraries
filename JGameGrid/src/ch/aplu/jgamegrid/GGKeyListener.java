// GGKeyListener.java

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

import java.awt.event.KeyEvent;

/**
 * Declarations of the notification method called when a key is pressed or released.
 */
public interface GGKeyListener extends java.util.EventListener
{
  /**
   * Event callback method called when a key is pressed.
   * If the key is held down, there is a delay and the callback is
   * called periodically. 
   * If you do not want the initial delay, use a GGKeyRepeatListener.
   * @param evt the KeyEvent that gives information about the key
   * @return true, if the key is consumed, so following listeners
   * in the GGKeyListener sequence will not get the event
   */
  public boolean keyPressed(KeyEvent evt);

  /**
   * Event callback method called when a key is released.
   * @param evt the KeyEvent that gives information about the key
   * @return true, if the key is consumed, so following listeners
   * in the GGKeyListener sequence will not get the event
   */
  public boolean keyReleased(KeyEvent evt);

}
