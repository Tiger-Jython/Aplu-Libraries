// TCPClient.java

/*
 This software is part of the TCPCom library.
 It is Open Source Free Software, so you may
 - run the code for any purpose
 - study how the code works and adapt it to your needs
 - integrate all or parts of the code in your own programs
 - redistribute copies of the code
 - improve the code and release your improvements to the public
 However the use of the code is entirely your responsibility.
 */
package ch.aplu.tcpcom;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.charset.Charset;

/**
 * Class to create a TCP client socket handled by event callbacks.
 */
public class TCPClient
{

  // ---------------- Inner class ClientHandler -----------------------
  private class ClientHandler extends Thread
  {
    public void run()
    {
      debug("Receiver handler thread started");
      while (true)
      {
        try
        {
          String[] junk = readResponse().split("\0");
          // more than 1 message may be received 
          // if transfer is fast. data: xxxx\0yyyyy\0zzz
          for (String s : junk)
          {
            try
            {
              if (listener != null)
                listener.onStateChanged(MESSAGE, s);
            }
            catch (Exception ex)
            {
              System.out.println("Caught exception in TCPClient.MESSAGE: " + ex);
            }
          }
        }
        catch (IOException ex)
        {
          debug("Exception from is.read(). Stream closed.");
          if (checkRefused)
            isRefused = true;
          break;
        }
      }
      try
      {
        if (listener != null)
          listener.onStateChanged(DISCONNECTED, "");
      }
      catch (Exception ex)
      {
        System.out.println("Caught exception in TCPClient.DISCONNECTED:" + ex);
      }
      debug("Receiver handler thread terminated");
    }

    private String readResponse() throws IOException
    {
      debug("Calling readResponse");
      int bufSize = 4096;
      byte[] buf = new byte[bufSize];
      ByteArrayOutputStream baos = new ByteArrayOutputStream();
      boolean done = false;
      int offset = 0;
      while (!done)
      {
        debug("Calling blocking is.read()");
        int len = is.read(buf);
        debug("Returned from blocking read().len: " + len);
        if (len == -1)
          throw new IOException("Stream closed");
        baos.write(buf, offset, len);
        offset += len;
        if (buf[len - 1] == 0)  // \0
          done = true;
      }
      String s = baos.toString("UTF-8");
      s = s.substring(0, s.length() - 1);  // Remove \0
      receiverResponse = s;
      return s;
    }
  }

  // ---------------- End of inner classes -----------------------------
  private static String VERSION = "1.00 - Feb. 13, 2016";
  /** State value */
  public static String CONNECTING = "CONNECTING";
  /** State value */
  public static String SERVER_OCCUPIED = "SERVER_OCCUPIED";
  /** State value */
  public static String CONNECTION_FAILED = "CONNECTION_FAILED";
  /** State value */
  public static String CONNECTED = "CONNECTED";
  /** State value */
  public static String DISCONNECTED = "DISCONNECTED";
  /** State value */
  public static String MESSAGE = "MESSAGE";

  private TCPClientListener listener = null;
  private String host;
  private int port;
  private boolean isVerbose;
  private boolean isClientConnecting;
  private boolean isClientConnected;
  private ClientHandler clientHandler;
  private Socket socket;
  private InputStream is;
  private OutputStream os;
  private String receiverResponse;
  private boolean checkRefused;
  private boolean isRefused;

  /**
   * Creates a TCP socket client prepared for a connection with a 
   * TCPServer at given address and port. 
   * State changes invoke the callback onStateChanged(). 
   * @param host the IP address of the host
   * @param port the IP port where to listen (0..65535)
   * @param isVerbose if true, debug messages are written to System.out
   */
  public TCPClient(String host, int port, boolean isVerbose)
  {
    this.host = host;
    this.port = port;
    this.isVerbose = isVerbose;
    isClientConnecting = false;
    isClientConnected = false;
  }

  /**
   * Same, but isVerbose = false
   * @param host the IP address of the host
   * @param port the IP port where to listen (0..65535)
   */
  public TCPClient(String host, int port)
  {
    this(host, port, false);
  }

  /**
   * Registers a TCPClientListener to get state change notifications.
   * @param listener the listener to register
   */
  public void addTCPClientListener(TCPClientListener listener)
  {
    this.listener = listener;
  }

  /**
   * Creates a connection to the server (blocking until timeout).
   * @param timeout the maximum time (in s) for the connection trial (0: for default timeout)
   * @return true, if the connection is established; 
   * false, if the server is not available or occupied
   */
  public boolean connect(int timeout)
  {
    isClientConnecting = true;
    debug("Calling Client.connect() with host: " + host + " port: " + port);
    try
    {
      if (listener != null)
        listener.onStateChanged(CONNECTING, host + ":" + port);
    }
    catch (Exception ex)
    {
      System.out.println("Caught exception in TCPClient.CONNECTING: " + ex);
    }

    try
    {
      if (timeout > 0)
      {
        socket = new Socket();
        socket.connect(new InetSocketAddress(host, port), 1000 * timeout);
      }
      else
        socket = new Socket(host, port);
      is = socket.getInputStream();
      os = socket.getOutputStream();
    }
    catch (IOException ex)
    {
      debug("Connection failed. Exception: " + ex);
      isClientConnecting = false;
      try
      {
        if (listener != null)
          listener.onStateChanged(CONNECTION_FAILED, host + ":" + port);
      }
      catch (Exception e)
      {
        System.out.println("Caught exception in TCPClient.CONNECTED: " + e);
      }
      return false;
    }

    isClientConnecting = false;
    isClientConnected = true;

    clientHandler = new ClientHandler();
    clientHandler.start();

    // Check if connection is refused
    checkRefused = true;
    isRefused = false;
    long startTime = System.nanoTime();
    while (System.nanoTime() - startTime < 2000000000L && !isRefused)
      delay(1);
    if (isRefused)
    {
      debug("Connection refused");
      try
      {
        if (listener != null)
          listener.onStateChanged(SERVER_OCCUPIED, host + ":" + port);
      }
      catch (Exception ex)
      {
        System.out.println("Caught exception in TCPClient.SERVER_OCCUPIED: " + ex);
      }
      return false;
    }
    
    try
    {
      if (listener != null)
        listener.onStateChanged(CONNECTED, host + ":" + port);
    }
    catch (Exception ex)
    {
      System.out.println("Caught exception in TCPClient.CONNECTED: " + ex);
    }
    debug("Successfully connected");
    return true;
  }

  /**
   * Same as connect(int timeout) with system default timeout.
   * @return true, if the connection is established; 
   * false, if the server is not available or occupied
   */
  public boolean connect()
  {
    return connect(0);
  }

  /**
   * Returns true during a connection trial.
   * @return true, while the client tries to connect
   */
  public boolean isConnecting()
  {
    return isClientConnecting;
  }

  /**
   * Returns true if client is connected to a server.
   * @return true, if the connection is established
   */
  public boolean isConnected()
  {
    return isClientConnected;
  }

  /**
   * Closes the connection with the server.
   */
  public void disconnect()
  {
    debug("Client.disconnect()");
    if (!isClientConnected)
    {
      debug("Connection already closed");
      return;
    }
    isClientConnected = false;
    debug("Closing socket");
    try
    {
      is.close();
      os.close();
    }
    catch (Exception ex)
    {
    }
    try
    {
      socket.close();
    }
    catch (Exception ex)
    {
    }
  }

  /**
   * Sends the information msg to the server (as String, the character \0 
   * (ASCII 0) serves as end of string indicator, it is transparently added
   * and removed).  For responseTime > 0 the method blocks and waits 
   * for maximum responseTime seconds for a server reply. 
   * @param msg the message to send
   * @param responseTime the maximum time to wait for a server reply (in s)
   * @return the message or null, if a timeout occured
   */
  public String sendMessage(String msg, int responseTime)
  {
    debug("Calling sendMessage() with msg: " + msg + "; responseTime: " + responseTime);
    if (!isClientConnected)
    {
      debug("Not connected");
      return null;
    }
    String reply = null;
    try
    {
      msg += "\0";  // Append \0
      byte[] ary = msg.getBytes(Charset.forName("UTF-8"));
      os.write(ary);
      os.flush();
      if (responseTime > 0)
        reply = waitForReply(1000 * responseTime); // blocking
      return reply;
    }
    catch (IOException ex)
    {
      try
      {
        if (listener != null)
          listener.onStateChanged(DISCONNECTED, host + ":" + port);
      }
      catch (Exception e)
      {
        System.out.println("Caught exception in TCPClient.CONNECTED: " + e);
      }
      debug("Exception ex: " + ex);
      disconnect();
    }
    return null;
  }

  private String waitForReply(int responseTime)
  // responseTime in ms
  {
    debug("Calling waitForReply() with responseTime: " + responseTime);
    receiverResponse = null;
    long startTime = System.nanoTime();
    while (isClientConnected && receiverResponse == null
      && System.nanoTime() - startTime < 1000000 * responseTime)
      delay(10);
    if (receiverResponse == null)
      debug("Timeout while waiting for reply");
    else
      debug("Response = " + receiverResponse + " time elapsed: "
        + ((int)(System.nanoTime() - startTime) / 1000000) + " ms");
    return receiverResponse;
  }

  /**
   * Same as sendMessage(String msg, int responseTime) with responseTime = 0.
   * @param msg the message to send
   */
  public void sendMessage(String msg)
  {
    sendMessage(msg, 0);
  }

  /**
   * Returns the library version.
   * @return the current version of the library
   */
  public static String getVersion()
  {
    return VERSION;
  }

  private void debug(String msg)
  {
    if (isVerbose)
      System.out.println("   TCPClient-> " + msg);
  }

  private void delay(int time)
  {
    try
    {
      Thread.sleep(time);
    }
    catch (InterruptedException ex)
    {
    }
  }
}
