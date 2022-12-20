// BtListener.java

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

import java.io.*;
import javax.bluetooth.*;

/**
 * Callback declaration when running a Bluetooth server.
 * 
 */
public interface BtListener 
{
 /**
  * Called when a client connects successfully to the server.
  * Both streams are openend and ready to use. Typically the
  * server reads the incoming data with a blocking stream read method and
  * sends data in another thread. When the connection is lost (e.g. because the
  * client quits), the blocking read method returns with an end of stream value
  * that must be handled as appropriate. After returning, the server closes the
  * connection and streams and waits for the next connection by calling 
  * StreamConnectionNotifier.acceptAndOpen().  
  * @param rd the RemoteDevice that is connected
  * @param is the InputStream to get data from the connected client
  * @param os the OutputStream to send data to the connected client
  */
  public void notifyConnection(RemoteDevice rd, InputStream is, OutputStream os);
}
