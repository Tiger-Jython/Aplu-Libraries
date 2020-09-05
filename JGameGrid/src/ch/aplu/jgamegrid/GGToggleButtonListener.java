// GGToggleButtonListener.java

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
 * Declarations of the notification method called when a toggle button changes state
 * caused by a mouse click.
 */
public interface GGToggleButtonListener extends java.util.EventListener
{
  /**
   * Event callback method called when the button changes the state.
   * @param toggleButton the GGToggleButton reference of the button that caused the event
   * @param toggled true, if the button changed from untoggled to toggled state;
   * false, if the button changed from toggled to untoggled state
   */
  public void buttonToggled(GGToggleButton toggleButton, boolean toggled);
}
