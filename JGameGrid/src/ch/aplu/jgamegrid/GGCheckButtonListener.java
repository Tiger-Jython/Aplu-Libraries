// GGCheckButtonListener.java

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
 * Declarations of the notification method called when a check button changes state
 * caused by a mouse click.
 */
public interface GGCheckButtonListener extends java.util.EventListener
{
  /**
   * Event callback method called when the button changes the state.
   * @param checkButton the GGCheckButton reference of the button that caused the event
   * @param checked true, if the button changed from unchecked to checked state;
   * false, if the button changed from checked to unchecked state
   */
  public void buttonChecked(GGCheckButton checkButton, boolean checked);
}
