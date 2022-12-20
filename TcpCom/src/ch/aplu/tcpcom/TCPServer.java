// TCPServer.java

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
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.Charset;

/**
 * Class to create a TCP server socket handled by event callbacks.
 */
public class TCPServer extends Thread
{
  // ---------------------- Inner class ServerHandler ------------------------
  class ServerHandler extends Thread
  {
    public void run()
    {
      debug("ServerHandler started");
      int bufSize = 4096;
      try
      {
        byte[] buf = new byte[bufSize];
        while (true)
        {
          ByteArrayOutputStream baos = new ByteArrayOutputStream();
          boolean done = false;
          int offset = 0;
          while (!done)
          {
            debug("Calling blocking read()");
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
          if (listener != null)
            listener.onStateChanged(MESSAGE, s);
        }
      }
      catch (IOException ex)
      {

      }
      disconnect();
      debug("ServerHandler terminated");
    }
  }
  // ---------------------- End of inner class ServerHandler ----------------

  private static String VERSION = "1.00 - Feb. 13, 2016";
  /** State value */
  public static String PORT_IN_USE = "PORT_IN_USE";
  /** State value */
  public static String CONNECTED = "CONNECTED";
  /** State value */
  public static String LISTENING = "LISTENING";
  /** State value */
  public static String TERMINATED = "TERMINATED";
  /** State value */
  public static String MESSAGE = "MESSAGE";

  private TCPServerListener listener = null;
  private int port;
  private static boolean isVerbose;
  private boolean isClientConnected;
  private boolean terminateServer;
  private ServerSocket serverSocket;
  private InputStream is;
  private OutputStream os;

  /**
   * Creates a TCP socket server that listens on TCP port 
   * for a connecting client. The server runs in its own thread, so the
   * constructor returns immediately. State changes invoke the callback
   * onStateChanged().
   * @param port the IP port where to listen (0..65535)
   * @param isVerbose if true, debug messages are written to System.out
   */
  public TCPServer(int port, boolean isVerbose)
  {
    this.port = port;
    this.isVerbose = isVerbose;
    isClientConnected = false;
    terminateServer = false;
    start();
  }

  /**
   * Same with isVerbose = false.
   * @param port the IP port where to listen (0..65535)
   */
  public TCPServer(int port)
  {
    this(port, false);
  }

  /** For internal use only */
  public void run()
  {
    try
    {
      debug("TCPServer thread started");
      serverSocket = new ServerSocket(port);
      try
      {
        if (listener != null)
          listener.onStateChanged(LISTENING, "" + port);
      }
      catch (Exception ex)
      {
        System.out.println("Caught exception in TCPServer.LISTENING: " + ex);
      }
      while (true)
      {
        // wait to accept a connection - blocking call
        debug("Calling blocking accept()...");
        Socket socket = serverSocket.accept(); // Blocking-------------------
        if (isClientConnected)
        {
          debug("Returning form blocking accept(). Client refused");
          InputStream is1 = new DataInputStream(socket.getInputStream());
          OutputStream os1 = new DataOutputStream(socket.getOutputStream());
          try
          {
            is1.close();
            os1.close();
          }
          catch (Exception ex)
          {
          }
          continue;
        }

        isClientConnected = true;
        debug("Returning form blocking accept()");
        is = new DataInputStream(socket.getInputStream());
        os = new DataOutputStream(socket.getOutputStream());
        ServerHandler serverHandler = new ServerHandler();
        serverHandler.setDaemon(true);
        serverHandler.start();
        String clientIP = socket.getInetAddress().toString();
        if (clientIP.equals("/0:0:0:0:0:0:0:1"))
          clientIP = "localhost";
        try
        {
          if (listener != null)
            listener.onStateChanged(CONNECTED, clientIP);
        }
        catch (Exception ex)
        {
          System.out.println("Caught exception in TCPServer.CONNECTED: " + ex);
        }
      }
    }
    catch (java.net.BindException ex)
    {
      try
      {
        if (listener != null)
          listener.onStateChanged(PORT_IN_USE, "");
      }
      catch (Exception e)
      {
        System.out.println("Caught exception in TCPServer.PORT_IN_USE: " + e);
      }
    }
    catch (IOException ex)
    {
      debug("Exception in blocking accept(). " + ex);
      try
      {
        if (listener != null)
          listener.onStateChanged(TERMINATED, "");
      }
      catch (Exception e)
      {
        System.out.println("Caught exception in TCPServer.TERMINATED: " + e);
      }
    }
    debug("Main server thread terminated");
  }

  /**
   * Registers a TCPServerListener to get state change notifications.
   * @param listener the listener to register
   */
  public void addTCPServerListener(TCPServerListener listener)
  {
    this.listener = listener;
  }

  /**
   * Returns  true, if a client is connected to the server.
   * @return true, if the communication link is establed
   */
  public boolean isConnected()
  {
    return isClientConnected;
  }

  /**
   * Closes the connection with the client and enters 
   * the LISTENING state.
   */
  public void disconnect()
  {
    debug("Calling Server.disconnect()");
    if (isClientConnected)
    {
      isClientConnected = false;
      try
      {
        if (listener != null)
          listener.onStateChanged(LISTENING, new Integer(port).toString());
      }
      catch (Exception ex)
      {
        System.out.println("Caught exception in TCPServer.LISTENING: " + ex);
      }
      debug("Closing streams");
      try
      {
        is.close();
        os.close();
      }
      catch (Exception ex)
      {
      }
    }
  }

  /**
   * Closes the connection and terminates the server thread. 
   * Releases the IP port.
   */
  public void terminate()
  {
    debug("Calling Server.terminate()");
    terminateServer = true;
    try
    {
      os.close();
      is.close();
    }
    catch (Exception ex)
    {
      // maybe os, is = null
    }
    try
    {
      serverSocket.close();  // take it out of socket.accept()
    }
    catch (Exception ex)
    {
    }
  }

  /**
   * Returns true, if the server is in TERMINATED state.
   * @return true, if the server thread is terminated
   */
  public boolean isTerminated()
  {
    return terminateServer;
  }

  /**
   * Sends the information msg to the client (as String, the character \0 (ASCII 0) serves as end of 
   * string indicator, it is transparently added and removed)
   * @param msg the message to send
   */
  public void sendMessage(String msg)
  {
    debug("sendMessage() with msg: " + msg);
    if (!isClientConnected)
    {
      debug("Not connected");
      return;
    }
    msg = msg + "\0";
    byte[] ary = msg.getBytes(Charset.forName("UTF-8"));
    try
    {
      os.write(ary);
      os.flush();
    }
    catch (Exception ex)  // Also catch null pointer, if os = null
                          // because connection is not yet fully valid
    {
      debug("Exception in sendMessage(). ex: " + ex);
    }
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
      System.out.println("   TCPServer-> " + msg);
  }
}
