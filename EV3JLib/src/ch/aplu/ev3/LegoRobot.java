 // LegoRobot.java
// Direct mode

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

import ch.aplu.util.*;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.*;
import java.net.Socket;
import java.nio.charset.Charset;
import java.util.Vector;

/**
 * Class that represents a EV3 robot brick. Parts (e.g. motors, sensors) may
 * be assembled into the robot to make it doing the desired job. <br><br>
 *
 * More than one instance may be created. They are identified by different
 * names (or addresses).<br><br>

 * The communication uses a IP socket connection with port 1299, but the port
 * can be modified in ev3jlib.properties.

 * EV3Jlib implements the usual Java event handling model using event listeners
 * and adapters. The sensors are polled at a regular interval from a internal
 * thread and the registered callback method is invoked, when the event condition
 * is fullfilled (e.g. sensor value crosses a given trigger level).<br><br>
 *
 * Many library options are defined in a property file 'ev3jlib.properties' that
 * may be modified as needed. The property file is searched in the following order:<br>
 * - Application directory (user.dir)<br>
 * - Home directory (user.home)<br>
 * - EV3JLib.jar (distribution)<br><br>
 *
 * As soon as the property file is found, the search is cancelled. This allows
 * to use a personalized property file without deleting or modifing the distributed
 * file in EV3JLib.jar. Consult the distributed file for more information.
 * Be careful to keep the original formatting.<br><br>
 *
 */
public class LegoRobot
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
      if (debugLevel >= SharedConstants.DEBUG_LEVEL_MEDIUM)
        System.out.println("DEBUG: Receiver thread starting");
      while (isReceiverRunning)
      {
        try
        {
          isReceiverUp = true;
          receiverResponse = readResponse();
        }
        catch (IOException ex)
        {
          if (debugLevel >= SharedConstants.DEBUG_LEVEL_MEDIUM)
            System.out.println("DEBUG: dis.readUTF throwed exception");
          isReceiverRunning = false;
          closeConnection();
        }
      }
      if (debugLevel >= SharedConstants.DEBUG_LEVEL_MEDIUM)
        System.out.println("DEBUG: Receiver thread finished");
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
    props = new EV3Properties();
  }
  private Vector<Part> parts = new Vector<Part>();
  private boolean isExiting = false;
  private int port;
  private String ipAddress = null;
  private boolean isConnected = false;
  private Socket socket = null;
  private InputStream is = null;
  private OutputStream os = null;
  private String msg;
  private static int debugLevel;
  private static EV3Properties props;  // Initialized by static part
  protected static ClosingMode myClosingMode = ClosingMode.TerminateOnClose;
  protected boolean isReleased;
  private ConnectionListener connectionListener = null;
  private Receiver receiver = null;
  private boolean isReceiverRunning;
  private boolean isReceiverUp = false;
  private String receiverResponse = null;
  private long receiverTimeout = 20000000000L;  // 20 s, some sensors need a long setup time
  private String rc = "";
  private ButtonListener buttonListener = null;
  private int buttonID;

  /**
   * Creates a EV3Robot instance with given IP address.
   * If immediateConnect is true, a immediateConnection trial is engaged while
   * an information pane is shown.
   * If immediateConnect is false, no connection trial is engaged at
   * this time and connect() should be used.
   * If ipAddress is null, a input dialog is displayed where the IP address can be entered.
   * If more than one LegoRobot instance is needed, use immediateConnect = false.
   * @param ipAddress the IP address of the brick, e.g. "10.0.1.1"
   * @param immediateConnect if true, a connection trial is engaged
   */
  public LegoRobot(String ipAddress, boolean immediateConnect)
  {
    isReleased = false;
    port = props.getIntValue("IPPort");
    debugLevel = props.getIntValue("DebugLevel");
    myClosingMode = getClosingMode(props);
    if (ipAddress == null)
    {
      this.ipAddress = ConnectPanel.askIPAddress();
      if (this.ipAddress == null)
        throw new RuntimeException("Java frame disposed");
    }
    else
      this.ipAddress = ipAddress;
    if (debugLevel > SharedConstants.DEBUG_LEVEL_OFF)
      DebugConsole.showTimed("DebugLevel: " + debugLevel, 3000);

    if (immediateConnect && !connect(true))
      Monitor.putSleep();
  }

  /**
   * Creates a EV3Robot instance with given IP address.
   * A connection trial is engaged while an information pane is shown.
   * If ipAddress = null, a input dialog is displayed where the IP address can be entered.
   * If more than one LegoRobot instance is needed, use LegoRobot(ipAddress, false).
   * @param ipAddress the IP address of the brick, e.g. "10.0.1.1"
   */
  public LegoRobot(String ipAddress)
  {
    this(ipAddress, true);
  }

  /**
   * Asks for the IP address and creates a EV3Robot instance.
   * The initial value for the address is read from the ev3jlib.properties file.
   * A connection trial is engaged while an information pane is shown.
   * If more than one LegoRobot instance is needed, use LegoRobot(ipAddress, false).
   */
  public LegoRobot()
  {
    this(null, true);
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
   * Returns the properties from the EV3JLib property file.
   * @return the reference to the EV3Properties
   */
  public static EV3Properties getProperties()
  {
    return props;
  }

  /**
   * Returns the current debug level.
   * @return the debug level set in the properties file.
   */
  public static int getDebugLevel()
  {
    return debugLevel;
  }

  /**
   * Assembles the given part into the robot.
   * @param part the part to assemble
   */
  public void addPart(Part part)
  {
    if (part instanceof Sensor)
    {
      for (int i = 0; i < parts.size(); i++)
      {
        if (parts.elementAt(i) instanceof Sensor)
        {
          if (((Sensor)parts.elementAt(i)).getPortId() == ((Sensor)part).getPortId())
            new ShowError("Port " + ((Sensor)part).getPortLabel() + " conflict");
        }
      }
    }

    if (part instanceof GenericMotor)
    {
      for (int i = 0; i < parts.size(); i++)
      {
        if (parts.elementAt(i) instanceof GenericMotor)
        {
          if (((GenericMotor)parts.elementAt(i)).getPortId() == ((GenericMotor)part).getPortId())
            new ShowError("Port " + ((GenericMotor)part).getPortLabel() + " conflict");
        }
      }
    }

    if (part instanceof NxtMotor)
    {
      for (int i = 0; i < parts.size(); i++)
      {
        if (parts.elementAt(i) instanceof NxtMotor)
        {
          if (((NxtMotor)parts.elementAt(i)).getPortId() == ((NxtMotor)part).getPortId())
            new ShowError("Port " + ((NxtMotor)part).getPortLabel() + " conflict");
        }
      }
    }

    part.setRobot(this);
    char portName = part.partName.charAt(part.partName.length() - 1);
    if (portName == 'A' || portName == 'B' || portName == 'C' || portName == 'D'
      || portName == '1' || portName == '2' || portName == '3' || portName == '4')
    {
      String classID = part.partName.substring(0, part.partName.length() - 1);
      sendCommand("robot.addPart." + classID + "." + portName);
    }
    else
      sendCommand("robot.addPart." + part.partName);
    parts.addElement(part);
    part.init();
  }

  /**
   * Connects the host to the robot via an IP socket.
   * Shows a information pane while connecting.
   * A connect/disconnect melody is played.
   * If more than one LegoRobot instance is needed, use connect(false) or
   * connect(false, false).
   * @return true, if successful or already connected; false,
   * if connection fails
   */
  public boolean connect()
  {
    return connect(true);
  }

  /**
   * Connects the host to the robot via an IP socket.
   * Initializes all parts already assembled.
   * If isConnectPane = true, an information pane is shown while connecting.
   * If more than one LegoRobot instance is used, do no show the connect pane.
   * @param isConnectPane if true an information pane is shown
   * @return true, if successful or already connected; false,
   * if connection fails
   */
  public boolean connect(boolean isConnectPane)
  {
    return connect(isConnectPane, true);
  }

  /**
   * Connects the host to the robot via an IP socket.
   * Initializes all parts already assembled.
   * If isConnectPane = true, an information pane is shown while connecting.
   * If isAnnounce = true, a melody is by the brick when the connection is established
   * and when the connection is closed.
   * If more than one LegoRobot instance is used, do no show the connect pane.
   * @param isConnectPane if true, an information pane is shown
   * @param isAnnounce if true, a connect/disconnect melody is played
   * @return true, if successful or already connected;
   * false, if connection fails
   */
  public boolean connect(boolean isConnectPane, boolean isAnnounce)
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
          + "  - Is USB, Bluetooth or WLAN active?\n"
          + "  - Is BrickGate server running?";
        ConnectPanel.show(msg + info);
        ConnectPanel.getMop().addExitListener(new MyExitListener());
      }
      if (myClosingMode == LegoRobot.ClosingMode.DisposeOnClose)
        throw new EV3Exception("Connection failed");
      return false;
    }
    if (connectionListener != null)
      connectionListener.notifyConnection(true);
    // Connection established
    isConnected = true;
    if (isAnnounce)
      rc = sendCommand("robot.create.1");
    else
      rc = sendCommand("robot.create.0");
    if (!rc.equals(Response.OK))
    {
      closeStreams();
      System.out.println("Failed");
      return false;
    }
    System.out.println("OK");
    initKeyListener();
    if (isConnectPane)
    {
      msg = "Connection to " + ipAddress + " established.\n"
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
   * the communication link and terminates the program (depending on
   * setting in ev3jlib.properties).
   */
  public void exit()
  {
    isExiting = true;
    if (isReleased)
      return;
    for (Part part : parts)
      part.cleanup();
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

  /**
   * Disconnects the TCP link.
   */
  public void disconnect()
  {
    for (Part part : parts)
      part.cleanup();
    if (isConnected)
    {
      closeStreams();
      if (connectionListener != null)
        connectionListener.notifyConnection(false);
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
   * The method returns immediately while the tone is still playing. 
   * @param frequency the frequency of the tone (in Hertz) (double rounded to int)
   * @param duration the duration of the tone (in Millisec) (double rounded to int)
   */
  public void playTone(double frequency, double duration)
  {
    sendCommand("robot.playTone." + (int)Math.round(frequency) + "."
      + (int)Math.round(duration));
  }

  /**
   * Plays a wav file. The file must reside in /home/root and use the filename
   * song<tag>.wav, where <tag> is a given integer that identifies the file.
   * The method is blocked until the sound is finished.
   * Supported wav file format: mono, 8 bit unsigned or 16 bit signed. maximum sampling rate 11025 Hz.
   * The wav file can be copied with a SCP program (like WinSCP). Port: 22, user: root, pwd: empty.
   * @param tag a number to identify the file in /home/root/music/song<tag>.wav
   * @param vol the volume (0..100)
   */
  public void playSampleWait(int tag, int volume)
  {
    sendCommand("robot.playSampleWait." + tag + "." + volume);
  }

  /**
   * Plays a wav file. The file must reside in /home/root and use the filename
   * song<tag>.wav, where <tag> is a given integer that identifies the file.
   * The method starts the playing and returns.
   * Supported wav file format: mono, 8 bit unsigned or 16 bit signed. maximum sampling rate 11025 Hz.
   * @param tag a number to identify the file in /home/root/music/song<tag>.wav
   * @param vol the volume (0..100)
   */
  public void playSample(int tag, int volume)
  {
    sendCommand("robot.playSample." + tag + "." + volume);
  }

  /**
   * Sets the sound volume.
   * Master volume, used also for connect/disconnect melody.
   * @param volume the sound volume (0..100)
   */
  public void setVolume(int volume)
  {
    sendCommand("robot.setVolume." + volume);
  }

  /**
   * Turn on/off the brick's left/right LEDs (only affected in pair).
   * Pattern mask:<br>
   * 0: off<br>
   * 1: green<br>
   * 2: red<br>
   * 3: red bright<br>
   * 4: green blinking<br>
   * 5: red blinking<br>
   * 6: red blinking bright<br>
   * 7: green double blinking<br>
   * 8: red double blinking<br>
   * 9: red double blinking bright<br>
   * @param pattern the pattern 0..9
   */
  public void setLED(int pattern)
  {
    sendCommand("robot.setLED." + pattern);
  }

  /**
   * Returns the battery level.
   * @return voltage (in Millivolt)
   */
  public int getBatteryLevel()
  {
    return Integer.parseInt(sendCommand("robot.getBatteryLevel"));
  }

  /**
   * Returns the IP address (in dotted format).
   * @return IP address
   */
  public String getIPAddress()
  {
    return ipAddress;
  }

  /**
   * Returns library version information.
   * @return library version
   */
  public static String getVersion()
  {
    return SharedConstants.VERSION;
  }

  /**
   * Returns copywrite information.
   * @return copywrite information
   */
  public static String getAbout()
  {
    return SharedConstants.ABOUT;
  }

  /**
   * Returns the version of the BrickGate server.
   * @return version of BrickGate
   */
  public String getBrickGateVersion()
  {
    return sendCommand("getBrickGateVersion");
  }

  /**
   * Returns a list with the current brick's IP addresses
   * @return the IP addresses used by the brick
   */
  public String[] getIPAddresses()
  {
    String response = sendCommand("getIPAddresses");
    return response.split(";");
  }

  /**
   * Returns true, if the client connects locally (with localhost connection).
   * @return true, if the client is running on the brick; otherwise false
   */
  public boolean isAutonomous()
  {
    return (sendCommand("isAutonomous").equals("1"));
  }

  private static ClosingMode getClosingMode(EV3Properties props)
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
   * Resets EV3 to start location/direction.
   * Empty method for compatibility with EV3Sim.
   *
   */
  public void reset()
  {
  }

  protected synchronized String sendCommand(String cmd)
  {
    if (debugLevel >= SharedConstants.DEBUG_LEVEL_MEDIUM)
      System.out.println("DEBUG: sendCommand() with cmd = " + cmd);
    if (!isConnected)
    {
      if (debugLevel >= SharedConstants.DEBUG_LEVEL_MEDIUM)
        System.out.println("DEBUG: Not connected. Returned: SEND_FAILED");
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
      if (debugLevel >= SharedConstants.DEBUG_LEVEL_MEDIUM)
        System.out.println("Got exception: " + ex);
      if (!isExiting && isConnected)
        closeConnection();
    }

    if (debugLevel >= SharedConstants.DEBUG_LEVEL_MEDIUM)
      System.out.println("DEBUG: sendCommand() returned: SEND_FAILED");
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
      if (debugLevel >= SharedConstants.DEBUG_LEVEL_MEDIUM)
        System.out.println(". DEBUG: No Response. "
          + (isConnected ? "Not connected" : "Timeout reached"));
      throw new IOException("Receiver timeout reached");
    }
    if (debugLevel >= SharedConstants.DEBUG_LEVEL_MEDIUM)
      System.out.println(". DEBUG: Response = " + receiverResponse);

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
    msg = "The link to the EV3 is broken!";
    if (debugLevel >= SharedConstants.DEBUG_LEVEL_MEDIUM)
      System.out.println("DEBUG: " + msg);
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
   * The keyboard simulates the EV3 buttons as follows:<br>
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
   * Returns true, if the UP button was the last button hit since
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
   * Returns true, if the DOWN button was the last button hit since
   * the last call of this method. On return, the button hit is cleared.
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
   * Returns true, if the LEFT button was the last button hit since
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
   * Returns true, if the RIGHT button was the last button hit since
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
   * Returns true, if the ENTER button was the last button hit since
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
   * Returns true, if the ESCAPE button was the last button hit since
   * the last call of this method. On return, the button hit is cleared.
   * Calls Thread.sleep(10) to be used in narrow loops.
   * @return true, if the up button was clicked or the connection in not established
   */
  public boolean isEscapeHit()
  {
    Tools.delay(10);
    if (!isConnected)
      return true;
    boolean pressed = (buttonID == BrickButton.ID_ESCAPE);
    if (pressed)
      buttonID = 0;
    return pressed;
  }

  /**
   * Draws the given text line starting at given position.
   * The display is 18 characters wide and 8 lines high.
   * Characters outside the visual range are hidden.
   * @param text the text to show
   * @param x the column number (0..17)
   * @param y the line number (0..7)
   */
  public void drawString(String text, int x, int y)
  {
    drawStringAt(text, 18 * y + x);
  }

  /**
   * Draws the given text line starting at given screen cell count.
   * The display is 18 characters wide and 8 lines high.
   * Characters outside the visual range are hidden.
   * @param text the text to show
   * @param count the cell count (cells are counted from zero line-per-line
   */
  public void drawStringAt(String text, int count)
  {
    text = text.replace(".", "`");
    sendCommand("robot.drawStringAt.s" + text + "." + count);
  }

  /**
   * Clears the display.
   */
  public void clearDisplay()
  {
    sendCommand("robot.clearDisplay");
  }
}
