// GGRadioButtonListener.java

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
 * Declarations of the notification method called when a radio button changes state
 * caused by a mouse click. When registered in a GGRadioButtonGroup, only changes
 * from deselected to selected state are reported.
 */
public interface GGRadioButtonListener extends java.util.EventListener
{

  /**
   * Event callback method called when the button changes the state.
   * @param radioButton the GGRadioButton reference of the button that caused the event
   * @param selected true, if the button changed from deselected to selected state;
   * false, if the button changed from selected to deselected state. (If registered in
   * a GGRadioButtonGroup, only changes from deselected to selected state are reported, so
   * selected is always true.)
   */
  public void buttonSelected(GGRadioButton radioButton, boolean selected);
}
