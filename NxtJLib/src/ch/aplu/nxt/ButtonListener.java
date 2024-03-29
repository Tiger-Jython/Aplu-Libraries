// ButtonListener.java
// Direct mode

/*
 This software is part of the NxtJLib library.
 It is Open Source Free Software, so you may
 - run the code for any purpose
 - study how the code works and adapt it to your needs
 - integrate all or parts of the code in your own programs
 - redistribute copies of the code
 - improve the code and release your improvements to the public
 However the use of the code is entirely your responsibility.

 Author: Aegidius Pluess, www.aplu.ch
 */
package ch.aplu.nxt;

/**
 * Interface with declarations of a callback method to 
 * detect button press events. The buttons are mapped to keyboard keys.
 */
public interface ButtonListener extends java.util.EventListener
{
  /**
   * Called when one of the buttons is hit. The brick buttons are simulated 
   * by keyboard keys with the following mapping:<br>
   * ESCAPE button->escape key<br>
   * ENTER button->enter key<br>
   * UP button->cursor up key<br>
   * DOWN button->cursor down key<br>
   * LEFT button->cursor left key<br>
   * RIGHT button->cursor right key<br>
   * @param buttenID the ID of the button as defined in BrickButton interface
   */
  public void buttonHit(int buttonID);

}
