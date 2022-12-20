// BluetoothPeer.java

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

/**
 * Class that implements a Bluetooth peer-to-peer communication based on
 * the client-server model. When started, an existing server is searched
 * using a specified Bluetooth service. If found, the connection is establish
 * in a client mode. Otherwise a server mode with the given service name is started.<br><br>
 *
 * Data is exchanged as integer blocks of arbitrary length.
 */
public class BluetoothPeer
{
  // --------------- Inner class MyBtListener --------------
  private class MyBtListener implements BtListener
  {
    // I am a server and a client connected successfully
    public void notifyConnection(RemoteDevice rd, InputStream is, OutputStream os)
    {
      if (isVerbose)
      {
        try
        {
          VerboseWriter.out.println("Peer: Connection established.");
          VerboseWriter.out.println("Peer: Client's name: " + rd.getFriendlyName(false));
          VerboseWriter.out.println("Peer: Client's address " + rd.getBluetoothAddress());
        }
        catch (IOException ex)
        {
        }
      }
      isConnected = true;
      dis = new DataInputStream(is);
      dos = new DataOutputStream(os);
      if (listener != null)
        listener.notifyConnection(true);

      int value = 0;
      int size = 0;
      int[] data = null;
      int index = -1;
      try
      {
        while (isConnected)
        {
          if (isVerbose)
            VerboseWriter.out.println("Peer: Calling readInt()");

          // Blocking, throws IOException
          // when client closes connection or BluetoothServer.cancel() is called
          value = dis.readInt();

          if (isVerbose)
            VerboseWriter.out.println("Peer: Returning from readInt(). Got: " + value);


          if (index == -1)
          {
            size = value;
            data = new int[size];
            index = 0;
          }
          else
          {
            data[index++] = value;
            if (index == size)
            {
              index = -1;
              if (listener != null)
                listener.receiveDataBlock(data);
            }
          }
        }
      }
      catch (IOException ex)
      {
        if (isVerbose)
          VerboseWriter.out.println("Peer: Connection lost");
        isConnected = false;
        if (listener != null)
          listener.notifyConnection(false);
        bs.cancel();
      }
    }
  }

  // I am a client and handle the connection
  private class ExecuteThread extends Thread
  {
    public void run()
    {
      dis = new DataInputStream(bc.getInputStream());
      dos = new DataOutputStream(bc.getOutputStream());

      isRunning = true;
      boolean isError = false;

      int value = 0;
      int size = 0;
      int[] data = null;
      int index = -1;
      while (isRunning)
      {
        try
        {
          if (isVerbose)
            VerboseWriter.out.println("Peer: Call blocking readline()");
          value = dis.readInt(); // Blocking
        }
        catch (EOFException ex)
        {
          if (isVerbose)
            VerboseWriter.out.println("Peer: End of stream occured");
          isError = true;
        }
        catch (IOException ex)
        {
          if (isVerbose)
            VerboseWriter.out.println("Peer: Got IOException");
          isError = true;
        }
        if (isError)
        {
          bc.disconnect();
          isRunning = false;
        }
        else
        {
          if (isVerbose)
            VerboseWriter.out.println("Peer: Returned from blocking readline(). Got: " + value);
          if (index == -1)
          {
            size = value;
            data = new int[size];
            index = 0;
          }
          else
          {
            data[index++] = value;
            if (index == size)
            {
              index = -1;
              if (listener != null)
                listener.receiveDataBlock(data);
            }
          }
        }
      }
      if (listener != null)
        listener.notifyConnection(false);
    }
  }
  // --------------- End of inner classes ------------------
  private BtPeerListener listener = null;
  private String nodeName;
  private String serviceName;
  private BluetoothServer bs = null;
  private BluetoothClient bc = null;
  private DataInputStream dis;
  private DataOutputStream dos;
  private boolean isVerbose = false;
  private boolean isConnected = false;
  private volatile boolean isRunning = false;

  /**
   * Creates a BluetoothPeer instance that tries to connect to the given node using
   * the given service name. After returning the connection is established or
   * we are waiting for an incoming connection. isConnected() can be called to check
   * the case.<br><br>
   * The given listener receives notifications about
   * connection status and incoming data blocks.
   * @param nodeName the Bluetooth name of the partner node. If null or empty
   * start immediately as Bluetooth server
   * @param serviceName the Bluetooth service name used for the communication channel
   * @param listener the BtPeerListener that receives notifications
   * @param isVerbose if true, debug information is sent using a VerboseWriter
   */
  public BluetoothPeer(String nodeName, String serviceName, BtPeerListener listener, boolean isVerbose)
  {
    this.nodeName = nodeName;
    this.serviceName = serviceName;
    this.listener = listener;
    this.isVerbose = isVerbose;
    if (nodeName == null || nodeName.equals(""))  // Start as server
    {
      if (isVerbose)
        VerboseWriter.out.println("Peer: Starting as server");
      bs = new BluetoothServer(serviceName, new MyBtListener(), isVerbose);
    }
    else
    {

      if (isVerbose)
        VerboseWriter.out.println("Peer: Trying to connect as client to server " + nodeName +
          " using service " + serviceName);
      bc = new BluetoothClient(nodeName, serviceName);
      bc.setVerbose(isVerbose);
      if (bc.connect())
      {
        if (isVerbose)
          VerboseWriter.out.println("Peer: Connection as client successful");
        isConnected = true;
        listener.notifyConnection(true);
        new ExecuteThread().start();
        while (!isRunning)
        {
        }  // Wait until thread is up and running
      }
      else
      {
        if (isVerbose)
          VerboseWriter.out.println("Peer: Connection as client failed. Starting as server");
        bc = null;
        bs = new BluetoothServer(serviceName, new MyBtListener(), isVerbose);
      }
    }
  }

  /**
   * Send a block of data to the connected node. Returns immediately of not connected.
   * @param data the data block to send
   */
  public void sendDataBlock(int[] data)
  {
    if (isConnected)
    {
      try
      {
        int size = data.length;
        if (size == 0)
          return;
        dos.writeInt(size);
        dos.flush();
        if (isVerbose)
          VerboseWriter.out.print("Peer: Sending block\n" + size + ": ");
        for (int i = 0; i < size; i++)
        {
          dos.writeInt(data[i]);
          if (isVerbose)
            VerboseWriter.out.print(data[i] + ((i < size - 1) ? "|" : "\n"));
          dos.flush();
        }
      }
      catch (IOException ex)
      {
        if (isVerbose)
          VerboseWriter.out.println("Peer: IOException in writeInt()");
      }
    }
  }

  /**
   * Returns the connection status.
   * @return true, if connected, otherwise false
   */
  public boolean isConnected()
  {
    return isConnected;
  }

  /**
   * Releases a connection and informs the connected node's listener by calling
   * notifyConnection(false). If no connection is established, nothing happens.
   * You should call this cleanup method to be sure that the Bluetooth service
   * is shutdown.<br><br>
   * Closes any opened VerboseWriter file stream.
   */
  public void releaseConnection()
  {
    if (isConnected)
    {
      if (bc != null)
        bc.disconnect();
      if (bs != null)
        bs.close();
    }
    else
    {
      if (bs != null)
        bs.cancel();  // Shutdown waiting server
    }

    VerboseWriter.close();
  }

  /**
   * Returns true, if connected as client.
   * @return true, if connected as client; if not connected or
   * connected as server, returns false
   */
  public boolean isClient()
  {
    if (bc != null)
      return true;
    return false;
  }

  /**
   * Returns true, if connected as server.
   * @return true, if connected as server; if not connected or
   * connected as client, returns false
   */
  public boolean isServer()
  {
    if (bs != null)
      return true;
    return false;
  }
}
