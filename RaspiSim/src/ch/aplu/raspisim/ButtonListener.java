// ButtonListener.java

/*
 This software is part of the RaspiSim library.
 It is Open Source Free Software, so you may
 - run the code for any purpose
 - study how the code works and adapt it to your needs
 - integrate all or parts of the code in your own programs
 - redistribute copies of the code
 - improve the code and release your improvements to the public
 However the use of the code is entirely your responsibility.

 Author: Aegidius Pluess, www.aplu.ch
 */
package ch.aplu.raspisim;

/**
 * Interface with declarations of a callback method to detect button events.
 */
public interface ButtonListener extends java.util.EventListener
{
  /**
   * Called when the Escape button is used (simultating the RaspiBrick
   * pushbutton).
   * @param event one of the values SharedConstants.BUTTON_PRESSED (=1),
   * SharedConstants.BUTTON_RELEASED (=2), SharedConstants.BUTTON_LONGPRESSED (=3)
   */
  public void buttonEvent(int event);

}
