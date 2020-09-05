// ConnectionListener.java

/*
This software is part of the RaspiJLib library.
It is Open Source Free Software, so you may
- run the code for any purpose
- study how the code works and adapt it to your needs
- integrate all or parts of the code in your own programs
- redistribute copies of the code
- improve the code and release your improvements to the public
However the use of the code is entirely your responsibility.

Author: Aegidius Pluess, www.aplu.ch
*/

package ch.aplu.raspi;

/**
 * Callback declaration for IP socket connection/disconnection.
 * 
 */
public interface ConnectionListener 
{
 /**
  * Called when the RaspiRobot connects or disconnects from the Raspi brick.
  * Disconnection is only notified when data between the LegoRobot and the bricks
  * are exchanged.
  * @param isConnected if true; the brick connects; otherwise it disconnects
  */
  public void notifyConnection(boolean isConnected);
}
