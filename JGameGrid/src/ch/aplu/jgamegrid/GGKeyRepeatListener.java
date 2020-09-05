// GGKeyRepeater.java

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
 * Declarations of a notification method called when a key is pressed 
 * and held down. 
 */
public interface GGKeyRepeatListener extends java.util.EventListener
{
  /**
   * Event callback when the key is continously pressed. As compared to
   * the GGKeyListener.keyPressed() event, there is no delay after the key is 
   * pressed until the next event happens.
   * @param keyCode the key code of the key
   */
  public void keyRepeated(int keyCode);
}
