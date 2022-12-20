// BtPeerListener.java

/*
This software is part of the Aplu Bluetooth Library.
It is Open Source Free Software, so you may
- run the code for any purpose
- study how the code works and adapt it to your needs
- integrate all or parts of the code in your own programs
- redistribute copies of the code
- improve the code and release your improvements to the public
However the use of the code is entirely your responsibility.
 */

package ch.aplu.bluetooth;


/**
 * Declarations of notification methods called from the class BluetoothPeer.
 */
public interface BtPeerListener
{

 /**
   * Called when incoming data is received.
   */
   public void receiveDataBlock(int[] data);

 /**
   * Called when connection is established/lost.
   */
   public void notifyConnection(boolean connected);


}
