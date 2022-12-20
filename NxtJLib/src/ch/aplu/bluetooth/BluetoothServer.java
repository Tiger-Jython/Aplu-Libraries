// BluetoothServer.java

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

import javax.bluetooth.*;
import java.io.*;
import javax.microedition.io.*;

/**
 * Class that creates a Bluetooth server thread.
 * Keep in mind that the server must be started before any client tries to connect.
 */
public class BluetoothServer extends Thread
{
  private volatile boolean isRunning = true;
  private BtListener listener;
  private StreamConnectionNotifier scn;
  private StreamConnection conn;
  private InputStream is;
  private OutputStream os;
  private String serviceName;
  private boolean isVerbose = false;

  /**
   * Creates a server instance with given serviceName. The given listener
   * gets callback information when a client connects. <br><br> If isVerbose = true,
   * many important debug information is sent to a VerboseWriter (System.out or
   * into a log file).<br><br>
   * @param serviceName the name of the service to install
   * @param listener the BtListener to get connnect information
   * @param isVerbose if true, log information are sent to a VerboseWriter
   */
  public BluetoothServer(String serviceName, BtListener listener, boolean isVerbose)
  {
    this.serviceName = serviceName;
    this.listener = listener;
    this.isVerbose = isVerbose;
    start();
  }

  /**
   * Same as BluetoothServer(String serviceName, BtListener listener, false)
   * (no verbose).
   *
   */
  public BluetoothServer(String serviceName, BtListener listener)
  {
    this(serviceName, listener, false);
  }

  /**
   * For internal use only.
   */
  public void run()
  {
    String connectionUrl =
      "btspp://localhost:68EE141812D211D78EED00B0D03D76EC;name=" + serviceName;
    try
    {
      if (isVerbose)
        VerboseWriter.out.println("Server: Connection URL: " + connectionUrl);
      scn = (StreamConnectionNotifier)Connector.open(connectionUrl);
      while (isRunning)
      {
        if (isVerbose)
          VerboseWriter.out.println("Server: Call acceptAndOpen()");
        conn = scn.acceptAndOpen();  // Blocking
        if (isVerbose)
          VerboseWriter.out.println("Server: Returned from acceptAndOpen()");
        RemoteDevice rd = RemoteDevice.getRemoteDevice(conn);
        is = conn.openInputStream();
        os = conn.openOutputStream();
        if (isVerbose)
          VerboseWriter.out.println("Server: Calling notifyConnection()");
        listener.notifyConnection(rd, is, os);
        is.close();
        os.close();
        conn.close();
      }
    }
    catch (IOException ex)
    {
      if (isVerbose)
        VerboseWriter.out.println("Server: Got IOException");
    }
    if (isVerbose)
      VerboseWriter.out.println("Server: Thread terminated");
  }

  /**
   * Cancel the blocking acceptAndOpen(). If no client is connected, the
   * server waits in its own thread for a new client by calling the blocking
   * acceptAndOpen(). In order to shutdown the waiting server and
   * terminate the thread, cancel() should be called.
   */
  public void cancel()
  {
    if (isVerbose)
      VerboseWriter.out.println("Server: Close StreamConnectionNotifier");
    try {scn.close();} catch (Exception ex) {}
    try
    {
      join(2000);
    }
    catch (InterruptedException ex) {}
  }

  /**
   * Closes the opened input and output stream. Closing the output stream
   * will inform any connected client staying in a blocking read method
   * with an end of stream value that the server is shutting down. Closing
   * the input stream will cause an IOException thrown by any blocking read method.
   */
  public void close()
  {
    if (isVerbose)
      VerboseWriter.out.println("Server: Close streams");
    try {is.close();} catch (Exception ex) {}
    try {os.close();} catch (Exception ex) {}
    try {conn.close();} catch (Exception ex) {}
  }

  /**
   * Sets the verbose mode on/off.
   * @param isVerbose if true, verbose mode is on, otherwise verbose mode is off
   */
  public void setVerbose(boolean isVerbose)
  {
    this.isVerbose = isVerbose;
  }

  /**
   * Returns the Bluetooth friendly name of the local device.
   */
  public String getLocalName()
  {
    String localName = "";
    try
    {
      localName = LocalDevice.getLocalDevice().getFriendlyName();
    }
    catch (BluetoothStateException ex)
    {
    }
    return localName;
  }
}

