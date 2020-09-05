// GGButtonOverListener.java

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
 * Declarations of the notification method called when the move enters and exits
 * the active mouse area of a push or toggle button.
 */
public interface GGButtonOverListener extends java.util.EventListener
{
  /**
   * Event callback method when the mouse cursor enters the active area.
   * @param button the GGButton reference of the button that caused the event
   */
  public void buttonEntered(GGButton button);

  /**
   * Event callback method called when the mouse cursor exits the active area.
   * @param button the GGButton reference of the button that caused the event
   */
  public void buttonExited(GGButton button);

}