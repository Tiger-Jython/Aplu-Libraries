// GGButtonListener.java

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
 * Declarations of the notification method called when a push button is pressed,
 * released or clicked using the left mouse button. The order of events is:
 * pressed, released, clicked. If the button is not released within a short time
 * span or the button is released with the mouse cursor outside the button,
 * the click event is not generated. The callbacks are executed in a separate
 * thread, so some synchronization may be necessary to prevent concurrency
 * problems.
 */
public interface GGButtonListener extends java.util.EventListener
{
  /**
   * Event callback method called when the button is pressed.
   * @param button the GGButton reference of the button that caused the event
   */
  public void buttonPressed(GGButton button);

  /**
   * Event callback method called when the button is released.
   * @param button the GGButton reference of the button that caused the event
   */
  public void buttonReleased(GGButton button);

  /**
   * Event callback method called when the button is clicked.
   * @param button the GGButton reference of the button that caused the event
   */
  public void buttonClicked(GGButton button);
}
