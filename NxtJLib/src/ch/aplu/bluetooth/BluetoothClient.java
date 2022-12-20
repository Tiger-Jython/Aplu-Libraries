// BluetoothClient.java

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
import javax.microedition.io.*;
import java.io.*;
import java.util.*;

/**
 * Class to create a client that connects to a server via Bluetooth.
 * Keep in mind that a server must be up and running before any client can connect.
 */
public class BluetoothClient
{
  // ---------------- Inner class MyBluetoothResponder -----------------
  private class MyBluetoothResponder implements BluetoothResponder
  {
    public void notifyBluetoothDeviceSearch(Vector deviceTable)
    {
      if (deviceTable.size() == 0)
      {
        if (isVerbose)
          VerboseWriter.out.println("Client: Server with Bluetooth name " + serverName + " not found");
        wakeUp();
        return;
      }

      di = (BtDeviceInfo)deviceTable.elementAt(0);
      dev = (RemoteDevice)(di.getRemoteDevice());
      serverAddressStr = dev.getBluetoothAddress();
      serverAddressLong = Long.parseLong(serverAddressStr, 16);
      if (isVerbose)
      {
        VerboseWriter.out.println("Client: Server " + serverName + " found");
        VerboseWriter.out.println("Client: Server reported address: " + serverAddressStr);
      }
      if (channel != -1) // No service search
        doConnect();
    }

    public void notifyBluetoothServiceSearch(Vector serviceTable)
    {
      if (isVerbose)
        VerboseWriter.out.println("Client: Service report:");
      if (serviceTable.size() == 0)
      {
        if (isVerbose)
          VerboseWriter.out.println("Client: No services found");
         wakeUp();
         return;
      }

      for (int i = 0; i < serviceTable.size(); i++)
      {
        BtServiceInfo si = (BtServiceInfo)serviceTable.elementAt(i);
        RemoteDevice dev = si.getRemoteDevice();
        ServiceRecord sr = si.getServiceRecord();

        String reportedServiceName;
        DataElement de = sr.getAttributeValue(0x0100);
        if (de != null)
        {
          reportedServiceName = (String)de.getValue();
          if (isVerbose)
            VerboseWriter.out.println("Client: Found service name: " + reportedServiceName);

          if (reportedServiceName.trim().equals(serviceName.trim()))
          {
            // Get connection URL
            String serviceUrl = sr.getConnectionURL(0, false);
            if (isVerbose)
              VerboseWriter.out.println("Client: Connection URL: " + serviceUrl);

            String s = serviceUrl.substring(21);
            int semicolonIndex = s.indexOf(';');
            String channelStr = s.substring(0, semicolonIndex);
            if (isVerbose)
              VerboseWriter.out.println("Client: Got channel #: " + channelStr);
            try
            {
              channel = Integer.parseInt(channelStr);
              detectedServerChannel = channel;
            }
            catch (NumberFormatException ex)
            {
              if (isVerbose)
                VerboseWriter.out.println("Client: Channel not an integer");
              wakeUp();
              return;
            }
            // All ok
            doConnect();
            return;
          }
        }
      }
      if (isVerbose)
        VerboseWriter.out.println("Client: Service " + serviceName + 
          " not found");
      wakeUp();
    }
  }
  
  // ---------------- End of inner classes -----------------------------

  private BluetoothFinder btf;
  private BtDeviceInfo di;
  private RemoteDevice dev;
  private RemoteDevice remoteDevice;
  private String serverName = null;
  private long serverAddressLong = 0;
  private String serverAddressStr = "";
  private boolean isConnected = false;
  private InputStream is = null;
  private OutputStream os = null;
  private int channel;
  private int detectedServerChannel = -1;
  private String serviceName = null;
  private StreamConnection conn = null;
  private boolean isVerbose = false;
  private volatile boolean isConnectionSuccessful;

  /**
   * Creates a BluetoothClient instance for the given server's Bluetooth name
   * that will connect to Bluetooth channel 1.<br><br>
   * No connection is established before connect() is called.
   */
  public BluetoothClient(String serverName)
  {
    this(serverName, 1);
  }

  /**
   * Creates a BluetoothClient instance for the given server's Bluetooth name
   * that will connect using the given given channel.
   * The Bluetooth channel is dynamically attributed when the server starts.
   * If you do not know the channel number, either use the server service name
   * or spy the reachable Bluetooth servers with a Bluetooth sniffer, like
   * RFCommBrowser from www.aplu.ch/download/RFCommBrowser.zip<br><br>
   * No connection is established before connect() is called.
   */
  public BluetoothClient(String serverName, int channel)
  {
    this.serverName = serverName;
    this.channel = channel;
  }

  /**
   * Creates a BluetoothClient instance for the given server's Bluetooth name
   * that will connect using the given service name.
   * connect() may take some time, because the channel number has
   * to be retrieved from the server by a Bluetooth service search.<br><br>
   * No connection is established before connect() is called.
   */
  public BluetoothClient(String serverName, String serviceName)
  {
    this.serverName = serverName;
    this.serviceName = serviceName;
    channel = -1;
  }

  /**
   * Creates a BluetoothClient instance with given Bluetooth address
   * that will connect to channel 1.<br><br>
   * No connection is established before connect() is called.
   */
  public BluetoothClient(long serverAddress)
  {
    this(serverAddress, 1);
  }

  /**
   * Creates a BluetoothClient instance with given Bluetooth address
   * that will connect using the given given channel.
   * The Bluetooth channel is dynamically attributed when the server starts.
   * If you do not know the channel number, either use the server service name
   * or spy the reachable Bluetooth servers with a Bluetooth sniffer, like
   * RFCommBrowser from www.aplu.ch/download/RFCommBrowser.zip<br><br>
   * No connection is established before connect() is called.
   */
  public BluetoothClient(long serverAddress, int channel)
  {
    serverAddressLong = serverAddress;
    this.channel = channel;
    serverAddressStr = Long.toString(serverAddress, 16).toUpperCase();
  }

  /**
   * Creates a BluetoothClient instance for the given remote device
   * that will connect using the given service name.
   * connect() may take some time, because the channel number has
   * to be retrieved from the server by a Bluetooth service search.<br><br>
   * No connection is established a this time.
   */
  public BluetoothClient(RemoteDevice remoteDevice, String serviceName)
  {
    this.remoteDevice = remoteDevice;
    serverAddressStr = remoteDevice.getBluetoothAddress();
    serverAddressLong = Long.valueOf(serverAddressStr, 16);
    this.serviceName = serviceName;
    channel = -1;
  }

  /**
   * Attemps to connect the client to the host with given timeout.
   * @return true, if successful; false, if connection cannot be established
   * within the given timemout or already connected
   * @param timeout in seconds
   */
  public boolean connect(int timeout)
  {
    final Thread currentThread = Thread.currentThread();
    Thread t = new Thread()
    {
      public void run()
      {
        isConnectionSuccessful = connect();
        currentThread.interrupt();
      }
    };
    t.start();
    try
    {
      Thread.currentThread().sleep(1000 * timeout);
    }
    catch (InterruptedException ex)
    {
       return isConnectionSuccessful;  // connect() returned with return code
    }
    // connect() hangs
    disconnect();  // Hopefully connect() is deblocked
    return false;
  }
  
  /**
   * Attemps to connect the client to the host.
   * @return true, if successful; false, if connection cannot be established or already connected
   */
  public boolean connect()
  {
    if (isConnected)
      return false;
    
    if (serverName != null) // Name given
    {
      if (isVerbose)
        VerboseWriter.out.println("Client: Searching server with Bluetooth name " + serverName);

      int uuid_RFCOMM = 0x0003;
      int[] uuids =
      {
        uuid_RFCOMM
      };
      if (channel != -1)
        uuids = null; // No service search

      btf = new BluetoothFinder(serverName, uuids, isVerbose, new MyBluetoothResponder());
      putSleep();  // Wait for notification from callback

      if (!isConnected)
      {
        if (isVerbose)
          VerboseWriter.out.println("Client: Connection failed");
        return false;
      }
    }
    else // Remote device given
    {
      if (isVerbose)
      {
        VerboseWriter.out.println("Client: connect() with remoteDevice: " + remoteDevice.getBluetoothAddress());
        VerboseWriter.out.println("Client: Must get the channel by a service search...");
      }
      int uuid_RFCOMM = 0x0003;
      int[] uuids =
      {
        uuid_RFCOMM
      };
      btf = new BluetoothFinder(remoteDevice, uuids, isVerbose, new MyBluetoothResponder()); 
      putSleep();  // Wait for notification from callback
      if (!isConnected)
      {
        if (isVerbose)
          VerboseWriter.out.println("Client: Connection failed");
        return false;
      }
    }
    if (isVerbose)
      VerboseWriter.out.println("Client: Connection established");

    return true;
  }

  /**
   * Closes the opened input and output stream and releases the Bluetooth connection.
   * Any blocking read method of a connected server will return with an end of stream value that
   * informs the server that the client connection is lost.
   * Closing the input stream will cause an IOException throwed by any blocking read method.
   */
  public void disconnect()
  // Some of the references may be null  
  {
    if (!isConnected)
      return;

    if (isVerbose)
      VerboseWriter.out.println("Client: disconnect() closes streams");

    isConnected = false;
    try
    {
      is.close();
      os.close();
      conn.close();
    }
    catch (Exception ex)
    {
    }
  }

  /**
   * Returns the StreamConnection reference of the Bluetooth connection.
   * @return StreamConnection or null, if not connected
   */
  public StreamConnection getStreamConnection()
  {
    if (!isConnected)
      return null;
    return conn;
  }

  /**
   * Returns the InputStream reference of the Bluetooth connection.
   * @return InputStream or null, if not connected
   */
  public InputStream getInputStream()
  {
    if (!isConnected)
      return null;
    return is;
  }

  /**
   * Returns the OutputStream reference of the Bluetooth connection.
   * @return OutputStream or null, if not connected
   */
  public OutputStream getOutputStream()
  {
    if (!isConnected)
      return null;
    return os;
  }

  private void doConnect()
  {
    String connectionUrl = "btspp://" + serverAddressStr + ":" + channel;
    if (isVerbose)
      VerboseWriter.out.println("Client: Trying to connect with URL: " + connectionUrl);
    try
    {
      conn = (StreamConnection)Connector.open(connectionUrl);
      is = conn.openInputStream();
      os = conn.openOutputStream();
    }
    catch (IOException ex)
    {
      if (isVerbose)
        VerboseWriter.out.println("Client: Exception in Connector.open(). " + ex.getMessage());

      wakeUp();
      return;
    }
    // Connection established
    isConnected = true;
    wakeUp();
  }

  /**
   * Returns the connection state.
   * @return true, if connected, otherwise false
   */
  public boolean isConnected()
  {
    return isConnected;
  }

  /**
   * Returns the Bluetooth address (hex).
   * @return Bluetooth address as string in hex format or null, if not connected
   */
  public String getBtAddress()
  {
    if (!isConnected)
      return null;

    return serverAddressStr;
  }

  /**
   * Returns the server's Bluetooth friendly name.
   * Either it is given when constructing the client or it is requested
   * when the connection is established.
   * @return Bluetooth friendly name or null if not connected and not given when
   * in the contructor.
   */
  public String getServerName()
  {
    if (serverName != null)
      return serverName;

    if (!isConnected)
      return null;

    return serverName;
  }

  /**
   * Returns the Bluetooth address (long).
   * @return Bluetooth address as long or -1 if not connected
   */
  public long getBtAddressLong()
  {
    if (!isConnected)
      return -1;

    return serverAddressLong;
  }
  
  /**
   * Returns the channel number returned by the server after a service search.
   * @return Detected server channel number, -1 if no service search was performed
  */
  public int getDetectedServerChannel()
  {
    return detectedServerChannel;
  }

  /**
   * Set the verbose mode on/off.
   * Log information are sent via a VerbosWriter. (By default to System.out, but
   * may be redirected to a file.)
   * @param isVerbose if true, the verbose mode is on, otherwise off
   * @see VerboseWriter
   */
  public void setVerbose(boolean isVerbose)
  {
    this.isVerbose = isVerbose;
  }

  private void putSleep()
  {
    synchronized (this)
    {
      try
      {
        wait();
      }
      catch (InterruptedException ex)
      {
      }
    }
  }

  private void wakeUp()
  {
    synchronized (this)
    {
      notify();
    }
  }
}


