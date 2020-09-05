// Robot.java

/*
 This software is part of the RaspiJLib library.
 It is Open Source Free Software, so you may
 - run the code for any purpose
 - study how the code works and adapt it to your needs
 - integrate all or parts of the code in your own programs
 - redistribute copies of the code
 - improve the code and release your improvements to the public
 However the use of the code is entirely your responsibility.
 */
package ch.aplu.raspi;

import ch.aplu.util.*;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.*;
import java.net.Socket;
import java.nio.charset.Charset;

/**
 * Class that represents a Raspi robot brick. Parts (e.g. motors, sensors) may
 * be assembled into the robot to make it doing the desired job. <br><br>
 *
 * More than one instance may be created. They are identified by different
 * names (or addresses).<br><br>

 * The communication uses a IP socket connection with port 1299, but the port
 * can be modified in raspijlib.properties.

 * RaspiJlib implements the usual Java event handling model using event listeners
 * and adapters. The sensors are polled at a regular interval from a internal
 * thread and the registered callback method is invoked, when the event condition
 * is fullfilled (e.g. sensor value crosses a given trigger level).<br><br>
 *
 * Many library options are defined in a property file 'raspijlib.properties' that
 * may be modified as needed. The property file is searched in the following order:<br>
 * - Application directory (user.dir)<br>
 * - Home directory (user.home)<br>
 * - RaspiJLib.jar (distribution)<br><br>
 *
 * As soon as the property file is found, the search is cancelled. This allows
 * to use a personalized property file without deleting or modifing the distributed
 * file in RaspiJLib.jar. Consult the distributed file for more information.
 * Be careful to keep the original formatting.<br><br>
 *
 */
public class Robot
{
  // ------------------- Inner class MyKeyListener ------------------
  private class MyKeyListener implements KeyListener
  {
    public void keyTyped(KeyEvent e)
    {
    }

    public void keyPressed(KeyEvent e)
    {
      switch (e.getKeyCode())
      {
        case 10:
          buttonID = BrickButton.ID_ENTER;
          if (buttonListener != null)
            buttonListener.buttonHit(buttonID);
          break;
        case 27:
          buttonID = BrickButton.ID_ESCAPE;
          if (buttonListener != null)
            buttonListener.buttonHit(buttonID);
          break;
        case 37:
          buttonID = BrickButton.ID_LEFT;
          if (buttonListener != null)
            buttonListener.buttonHit(buttonID);
          break;
        case 38:
          buttonID = BrickButton.ID_UP;
          if (buttonListener != null)
            buttonListener.buttonHit(buttonID);
          break;
        case 39:
          buttonID = BrickButton.ID_RIGHT;
          if (buttonListener != null)
            buttonListener.buttonHit(buttonID);
          break;
        case 40:
          buttonID = BrickButton.ID_DOWN;
          if (buttonListener != null)
            buttonListener.buttonHit(buttonID);
          break;
      }
    }

    public void keyReleased(KeyEvent e)
    {
    }
  }

  // ------------------- Inner interface Response --------------------
  private interface Response
  {
    String OK = "0";
    String SEND_FAILED = "-1";
    String ILLEGAL_METHOD = "-2";
    String ILLEGAL_INSTANCE = "-3";
    String CMD_ERROR = "-4";
    String ILLEGAL_PORT = "-5";
    String CREATION_FAILED = "-6";
  }

  // ------------------- public enum ClosingMode ---------------------
  /**
   * Modes to determine what happens when the title bar close button is hit.
   */
  public static enum ClosingMode
  {
    /**
     * Stops the motors and terminates application by calling System.exit(0) (default).
     */
    TerminateOnClose,
    /**
     * Stops the motors, hides window and disconnects the communication link.
     */
    DisposeOnClose
  }

  // ---------------- Inner class MyExitListener -----------------
  private class MyExitListener implements ExitListener
  {
    public void notifyExit()
    {
      new Thread()
      {
        public void run()
        {
          exit();
        }
      }.start();
    }
  }

  // ---------------- Inner class Receiver -----------------------
  private class Receiver extends Thread
  {
    public void run()
    {
      debug("Receiver thread starting");
      while (isReceiverRunning)
      {
        try
        {
          isReceiverUp = true;
          receiverResponse = readResponse();
        }
        catch (IOException ex)
        {
          debug("dis.readUTF throwed exception");
          isReceiverRunning = false;
          closeConnection();
        }
      }
      debug("Receiver thread finished");
    }

    private String readResponse() throws IOException
    {
      byte[] buf = new byte[4096];
      ByteArrayOutputStream baos = new ByteArrayOutputStream();
      boolean done = false;
      while (!done)
      {
        int len = is.read(buf);
        if (len == -1)
          throw new IOException("Stream closed");
        baos.write(buf, 0, len);
        if (buf[len - 1] == 10)  // \n
          done = true;
      }
      String s = baos.toString("UTF-8");
      return s.substring(0, s.length() - 1);  // Remove \n
    }
  }

  // ---------------- End of inner classes -----------------------------
  static
  {
    props = new RaspiProperties();
  }
  private boolean isExiting = false;
  private int port;
  private String ipAddress = null;
  private boolean isConnected = false;
  private Socket socket = null;
  private InputStream is = null;
  private OutputStream os = null;
  private String msg;
  private static RaspiProperties props;  // Initialized by static part
  protected static ClosingMode myClosingMode = ClosingMode.TerminateOnClose;
  protected static boolean isReleased;
  private ConnectionListener connectionListener = null;
  private Receiver receiver = null;
  private boolean isReceiverRunning;
  private boolean isReceiverUp = false;
  private String receiverResponse = null;
  private long receiverTimeout = 20000000000L;  // 20 s, some sensors need a long setup time
  private String rc = "";
  private ButtonListener buttonListener = null;
  private int buttonID;
  private boolean isAutonomous = false;
  private EventThread eventThread = null;

  /**
   * Creates a RaspiRobot instance with given IP address.
   * If immediateConnect is true, a immediateConnection trial is engaged while
   * an information pane is shown.
   * If immediateConnect is false, no connection trial is engaged at
   * this time and connect() should be used.
   * If ipAddress is null, a input dialog is displayed where the IP address can be entered.
   * If run locally on the Raspi, the ipAddress parameter is ignored and the 
   * IP address is set to "localhost" and no connection pane is shown.
   * @param ipAddress the IP address of the brick, e.g. "192.168.0.2"
   * @param immediateConnect if true, a connection trial is engaged
   */
  public Robot(String ipAddress, boolean immediateConnect)
  {
    File osFile = new File("/etc/os-release");
    try
    {
      BufferedReader br = new BufferedReader(new FileReader(osFile));
      debug("Reading Linux OS information file...");
      String line = br.readLine();
      while (line != null)
      {
        if (line.contains("Raspbian"))
        {
          isAutonomous = true;
          immediateConnect = true;
          debug("Found: Running on local Raspberry Pi");
          break;
        }
        line = br.readLine();
      }
    }
    catch (IOException ex)
    {
      debug("Can't read Linux OS information file");
    }

    isReleased = false;
    port = props.getIntValue("IPPort");
    myClosingMode = getClosingMode(props);
    RobotInstance.setRobot(this);

    if (isAutonomous)
    {
      this.ipAddress = "localhost";
      connect(false);
    }
    else
    {
      if (ipAddress == null)
      {
        this.ipAddress = ConnectPanel.askIPAddress();
        if (this.ipAddress == null)
          throw new RuntimeException("Java frame disposed");
      }
      else
        this.ipAddress = ipAddress;

      if (immediateConnect && !connect(true))
        Monitor.putSleep();
    }
  }

  /**
   * Creates a RaspiRobot instance with given IP address.
   * A connection trial is engaged while an information pane is shown.
   * If ipAddress = null, a input dialog is displayed where the IP address can be entered.
   * If run locally on the Raspi, the ipAddress parameter is ignored and the 
   * IP address is set to "localhost" and no connection pane is shown.
   * @param ipAddress the IP address of the brick, e.g. "192.168.0.2"
   */
  public Robot(String ipAddress)
  {
    this(ipAddress, true);
  }

  /**
   * Asks for the IP address and creates a RaspiRobot instance.
   * The initial value for the address is read from the raspijlib.properties file.
   * The selected entry is then stored in RaspiRemote.properties in the user home directory.
   * If run locally on the Raspi, the IP address is set to "localhost" and no input dialog 
   * and no connection pane is shown.
   * A connection trial is engaged while an information pane is shown.
   */
  public Robot()
  {
    this(null, true);
  }

  protected void registerSensor(Part part)
  {
    if (eventThread == null)
    {
      eventThread = new EventThread();
      eventThread.start();
    }
    eventThread.add(part);
  }

  /**
   * Registers a connection listener to get notifications when
   * the link is established or broken. Keep in mind that you may get
   * no "connection established" notification before the listener is
   * registered (e.g if the constructor calls connect() immediately).
   * @param listener the ConnectionListener to register
   */
  public void addConnectionListener(ConnectionListener listener)
  {
    connectionListener = listener;
  }

  /**
   * Returns the properties from the RaspiJLib property file.
   * @return the reference to the raspiProperties
   */
  public static RaspiProperties getProperties()
  {
    return props;
  }

  /**
   * Connects the host to the robot via an IP socket.
   * Shows a information pane while connecting.
   * @return true, if successful or already connected; false,
   * if connection fails
   */
  public boolean connect()
  {
    return connect(true);
  }

  /**
   * Connects the host to the robot via an IP socket.
   * If isConnectPane = true, an information pane is shown while connecting.
   * @param isConnectPane if true, an information pane is shown
   * @return true, if successful or already connected;
   * false, if connection fails
   */
  public boolean connect(boolean isConnectPane)
  {
    if (isConnected)
      return true;

    if (ConnectPanel.getMop() != null)
    {
      msg = "Please close the connection dialog\nbefore restarting the program.";
      ConnectPanel.show(msg);
      return false;
    }
    if (isConnectPane)
    {
      msg = "Connecting to " + ipAddress + ":" + port + "...";
      ConnectPanel.show(msg);
    }
    System.out.print("Trying to connect to " + ipAddress + ":" + port + "...");
    try
    {
      socket = new Socket(ipAddress, port);
      is = socket.getInputStream();
      os = socket.getOutputStream();
      startReceiver();
    }
    catch (IOException ex)
    {
      System.out.println("Failed");
      if (isConnectPane)
      {
        msg = "Connecting to " + ipAddress + ":" + port + " failed.";
        String info
          = "\n  - Is IP address correct?\n"
          + "  - Is BrickGate server running?";
        ConnectPanel.show(msg + info);
        ConnectPanel.getMop().addExitListener(new MyExitListener());
      }
      if (myClosingMode == Robot.ClosingMode.DisposeOnClose)
        throw new RaspiException("Connection failed");
      return false;
    }
    System.out.println("Connection established.");
    if (connectionListener != null)
      connectionListener.notifyConnection(true);
    // Connection established
    isConnected = true;
    initKeyListener();
    for (Part part : RobotInstance.partsToRegister)
      part.setup(this);
    if (isConnectPane)
    {
      msg = "Connection to " + ipAddress + ":" + port + " established.\n"
        + "Application running...\n"
        + "(Close the window to terminate.)";
      ConnectPanel.show(msg);
      ConnectPanel.showVersion();
      ConnectPanel.getMop().addExitListener(new MyExitListener());
    }
    return true;
  }

  /**
   * Closes the connection dialog and any open QuitPane, disconnects
   the communication link and terminates the program.
   */
  public void exit()
  {
    debug("Calling Robot.exit()");
    isExiting = true;
    if (isReleased)
      return;

    if (eventThread != null)
    {
      eventThread.terminate();
      try
      {
        eventThread.join(2000);
      }
      catch (InterruptedException ex)
      {
      }
    }

    if (isConnected)
    {
      closeStreams();
      if (connectionListener != null)
        connectionListener.notifyConnection(false);
    }
    switch (myClosingMode)
    {
      case TerminateOnClose:
        System.exit(0);
        break;
      case DisposeOnClose:
        ConnectPanel.dispose();
        Monitor.wakeUp();
        QuitPane.dispose();
        isConnected = false;
        isReleased = true;
        if (Tools.t != null)
          Tools.t.interrupt();  // taking out of Tools.delay()
        break;
    }
  }

  private void closeStreams()
  {
    try
    {
      is.close();
    }
    catch (Exception ex)
    {
    }
    try
    {
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
   * Returns true as long as exit() is not called and the close
   * button of the Connection Pane is not hit.
   * @return the running state of the robot
   */
  public boolean isRunning()
  {
    delay(1);
    return !isReleased;
  }

  /**
   * Returns the InputStream reference of the connection link.
   * @return InputStream or null, if not connected
   */
  public InputStream getInputStream()
  {
    if (!isConnected)
      return null;
    return is;
  }

  /**
   * Returns the OutputStream reference of the connection link.
   * @return OutputStream or null, if not connected
   */
  public OutputStream getDataOutputStream()
  {
    if (!isConnected)
      return null;
    return os;
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
   * Plays a tone with given frequency and duration.
   * @param frequency the frequency of the tone (in Hertz) (double rounded to int)
   * @param duration the duration of the tone (in Millisec) (double rounded to int)
   */
  public void playTone(double frequency, double duration)
  {
    sendCommand("robot.playTone." + (int)Math.round(frequency) + "."
      + (int)Math.round(duration));
  }

  /**
   * Sets the sound volume.
   * Master volume, used also for connect/disconnect melody.
   * @param volume the sound volume (0..100)
   */
  public void setSoundVolume(int volume)
  {
    sendCommand("robot.setSoundVolume." + volume);
  }

  /**
   * Returns all IP addresses (in dotted format, comma separated).
   * @return IP addresses
   */
  public String getIPAddresses()
  {
    return sendCommand("robot.getIPAddresses");
  }

  /**
   * Returns library version information.
   * @return library version
   */
  public String getVersion()
  {
    return sendCommand("robot.getVersion");
  }

  protected static void delay(long timeout)
  {
    try
    {
      Thread.currentThread().sleep(timeout);
    }
    catch (InterruptedException ex)
    {
    }
  }

  /**
   * Resets Raspi to start location/direction.
   * Empty method for compatibility with RaspiSim.
   *
   */
  public void reset()
  {
  }

  protected synchronized String sendCommand(String cmd)
  {
    debug("sendCommand() with cmd = " + cmd);
    if (!isConnected)
    {
      debug("Not connected. Returned: SEND_FAILED");
      return (Response.SEND_FAILED);
    }

    try
    {
      receiverResponse = null;
      cmd += "\n";  // Append \n
      byte[] ary = cmd.getBytes(Charset.forName("UTF-8"));
      os.write(ary);
      os.flush();
      String reply = waitForReply();  // Throws IOException if timeout
      return reply;
    }
    catch (IOException ex)
    {
      debug("Got exception: " + ex);
      if (!isExiting && isConnected)
        closeConnection();
    }

    debug("sendCommand() returned: SEND_FAILED");
    return (Response.SEND_FAILED);
  }

  private String waitForReply() throws IOException
  {
    long startTime = System.nanoTime();
    while (isConnected && receiverResponse == null
      && System.nanoTime() - startTime < receiverTimeout)
      delay(1);
    if (receiverResponse == null)
    {
      debug("No Response. "
        + (isConnected ? "Not connected" : "Timeout reached"));
      throw new IOException("Receiver timeout reached");
    }
    debug("Response = " + receiverResponse);

    return receiverResponse;
  }

  private void startReceiver()
  {
    isReceiverRunning = true;
    receiver = new Receiver();
    receiver.start();
    while (!isReceiverUp)
      delay(1);
    delay(100);
  }

  private synchronized void closeConnection()
  {
    if (!isConnected)
      return;
    msg = "The link to the Raspi is broken!";
    debug(msg);
    if (connectionListener != null)
      connectionListener.notifyConnection(false);
    ModelessOptionPane mop = ConnectPanel.getMop();
    if (mop != null)
    {
      mop.addExitListener(null);
      ConnectPanel.show(msg);
    }
    isConnected = false;
    closeStreams();
  }

  /**
   * Registers a button listener that simulates the events
   * when one of the brick buttons is hit. 
   * The keyboard simulates the Raspi buttons as follows:<br>
   * ESCAPE button->escape key<br>
   * ENTER button->enter key<br>
   * UP button->cursor up key<br>
   * DOWN button->cursor down key<br>
   * LEFT button->cursor left key<br>
   * RIGHT button->cursor right key<br>
   * @param listener the ButtonListener to register
   */
  public void addButtonListener(ButtonListener listener)
  {
    buttonListener = listener;
  }

  private void initKeyListener()
  {
    ModelessOptionPane pane = ConnectPanel.getMop();
    if (pane != null)
    {
      javax.swing.JDialog dlg = ConnectPanel.getMop().getDialog();
      dlg.addKeyListener(new MyKeyListener());
      dlg.setFocusable(true);
    }
  }

  /**
   * Returns true, if any of the buttons was hit.
   * Calls Thread.sleep(10) to be used in narrow loops.
   * A button listener must be started before by calling startButtonListener.
   * @return true, if a button was hit or the connection in not established
   */
  public boolean isButtonHit()
  {
    Tools.delay(10);
    if (!isConnected)
      return true;
    return (buttonID != 0);
  }

  /**
   * Returns the button ID of the button previously hit. The button hit
   * buffer is cleared then.
   * @return the ID of the button; if the hit buffer is empty or the button listener is
   * is not running or the connection in not established, return 0
   */
  public int getHitButtonID()
  {
    if (!isConnected)
      return 0;
    int id = buttonID;
    buttonID = 0;
    return id;
  }

  /**
   * (Only in remote mode) Returns true, if the cursor up key hit since
   * the last call of this method. On return, the button hit is cleared.
   * Calls Thread.sleep(10) to be used in narrow loops.
   * @return true, if the up button was clicked or the connection in not established
   */
  public boolean isUpHit()
  {
    Tools.delay(10);
    if (!isConnected)
      return true;
    boolean pressed = (buttonID == BrickButton.ID_UP);
    if (pressed)
      buttonID = 0;
    return pressed;
  }

  /**
   * (Only in remote mode) Returns true, if the cursor down key was hit since the
   * last call of this method. On return, the button hit is cleared.
   * Calls Thread.sleep(10) to be used in narrow loops.
   * @return true, if the up button was clicked or the connection in not established
   */
  public boolean isDownHit()
  {
    Tools.delay(10);
    if (!isConnected)
      return true;
    boolean pressed = (buttonID == BrickButton.ID_DOWN);
    if (pressed)
      buttonID = 0;
    return pressed;
  }

  /**
   * (Only in remote mode) Returns true, if the cursor left key was hit since
   * the last call of this method. On return, the button hit is cleared.
   * Calls Thread.sleep(10) to be used in narrow loops.
   * @return true, if the up button was clicked or the connection in not established
   */
  public boolean isLeftHit()
  {
    Tools.delay(10);
    if (!isConnected)
      return true;
    boolean pressed = (buttonID == BrickButton.ID_LEFT);
    if (pressed)
      buttonID = 0;
    return pressed;
  }

  /**
   * (Only in remote mode) Returns true, if the cursor right key was hit since
   * the last call of this method. On return, the button hit is cleared.
   * Calls Thread.sleep(10) to be used in narrow loops.
   * @return true, if the up button was clicked or the connection in not established
   */
  public boolean isRightHit()
  {
    Tools.delay(10);
    if (!isConnected)
      return true;
    boolean pressed = (buttonID == BrickButton.ID_RIGHT);
    if (pressed)
      buttonID = 0;
    return pressed;
  }

  /**
   * (Only in remote mode) Returns true, if the ENTER key was hit since
   * the last call of this method. On return, the button hit is cleared.
   * Calls Thread.sleep(10) to be used in narrow loops.
   * @return true, if the up button was clicked or the connection in not established
   */
  public boolean isEnterHit()
  {
    Tools.delay(10);
    if (!isConnected)
      return true;
    boolean pressed = (buttonID == BrickButton.ID_ENTER);
    if (pressed)
      buttonID = 0;
    return pressed;
  }

  /**
   * Returns true, if the ESCAPE key (in remote mode) or the push button
   * (in autonomouse mode) was hit since
   * the last call of this method. On return, the button hit is cleared.
   * Calls Thread.sleep(10) to be used in narrow loops.
   * @return true, if the up button was clicked or the connection in not established
   */
  public boolean isEscapeHit()
  {
    Tools.delay(10);
    if (isAutonomous)
    {
      debug("Calling isEscapeHit() running in autonomous mode");
      String rc = sendCommand("robot.isButtonHit");
      debug("Reporting button state " + rc);
      if (rc.equals("1"))
        return true;
      else
        return false;
    }
    debug("Calling isEscapeHit() running in remote mode");
    if (!isConnected)
      return true;
    boolean pressed = (buttonID == BrickButton.ID_ESCAPE);
    if (pressed)
      buttonID = 0;
    debug("Reporting button state: " + pressed);
    return pressed;
  }

  private static ClosingMode getClosingMode(RaspiProperties props)
  {
    String value = props.getStringValue("ClosingMode");
    if (value != null)  // Entry found
    {
      value = value.trim();
      if (value.equals("TerminateOnClose"))
        return ClosingMode.TerminateOnClose;
      if (value.equals("DisposeOnClose"))
        return ClosingMode.DisposeOnClose;
      return ClosingMode.TerminateOnClose;  // Entry not valid
    }
    return ClosingMode.TerminateOnClose;  // Entry not valid
  }

  private static void debug(String msg)
  {
    if (SharedConstants.debug)
      System.out.println("Debug: " + msg);
  }
}
