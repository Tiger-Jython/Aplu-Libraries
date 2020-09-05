// RemoteAdapter.java

/*
 This software is part of the EV3JLib library.
 It is Open Source Free Software, so you may
 - run the code for any purpose
 - study how the code works and adapt it to your needs
 - integrate all or parts of the code in your own programs
 - redistribute copies of the code
 - improve the code and release your improvements to the public
 However the use of the code is entirely your responsibility.
 */
package ch.aplu.ev3;

/**
 * Class with empty callback methods for the IRRemote sensor. The channel
 * is selected by the sensor port (Port S1->channel 1,...,Port S4->channel 4).
 */
public class RemoteAdapter implements RemoteListener
{
  /**
   * Called when a button action is detected.
   * The command IDs are:<br>
   * 0: All buttons released<br>
   * 1: TopLeft<br>
   * 2: BottomLeft<br>
   * 3: TopRight<br>
   * 4: BottomRight<br>
   * 5: TopLeft&TopRight<br>
   * 6: TopLeft&BottomRight<br>
   * 7: BottomLeft&TopRight<br>
   * 8: BottomLeft&BottomRight<br>
   * 9: Centre<br>
   * 10: BottomLeft&TopLeft<br>
   * 11: TopRight&BottomRight<br><br>
   * 
   * Centre is a toggle button with a led indicator. If pressed, 
   * it remains in the on or off state until pressed again or until another
   * button is pressed. 
   * @param port the port where the sensor is plugged in
   * @param command the command ID corresponding the buttons
   */
  public void actionPerformed(SensorPort port, int command)
  {
  }
}
