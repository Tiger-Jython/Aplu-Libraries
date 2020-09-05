// BrickButton.java

/*
 This software is part of the RaspiSim library.
 It is Open Source Free Software, so you may
 - run the code for any purpose
 - study how the code works and adapt it to your needs
 - integrate all or parts of the code in your own programs
 - redistribute copies of the code
 - improve the code and release your improvements to the public
 However the use of the code is entirely your responsibility.
 */
package ch.aplu.raspisim;

/**
 * Interface to define constants for the brick buttons.
 * In autonomous mode the only brick button used as ESCAPE button.
 * In remote and simulation mode, the buttons correspond to the keyboard keys.
 */
public interface BrickButton
{
  /**
   * Constant for the UP button.
   */
  public static final int ID_UP = 0x01;
  /**
   * Constant for the DOWN button.
   */
  public static final int ID_DOWN = 0x04;
  /**
   * Constant for the LEFT button.
   */
  public static final int ID_LEFT = 0x10;
  /**
   * Constant for the RIGHT button.
   */
  public static final int ID_RIGHT = 0x08;
  /**
   * Constant for the ENTER button.
   */
  public static final int ID_ENTER = 0x02;
  /**
   * Constant for the ESCAPE button.
   */
  public static final int ID_ESCAPE = 0x20;
}
