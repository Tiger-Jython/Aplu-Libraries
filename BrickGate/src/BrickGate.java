// BrickGate.java

/* History:
 V1.00 - Jul 2014: - Ported from EV3DirectServer
 V1.01 - Jul 2014: - Added: Escape button support
 V1.02 - Jul 2014: - Added: Kill Python support
 V1.03 - Aug 2014: - Modified: Escape only when in Menu mode
 V1.04 - Aug 2014: - Modified: Non Python mode when script dir is empty
 V1.05 - Aug 2014: - Modified: Spawn thread simplified (not waiting to terminate)
 V1.06 - Aug 2014: - Modified: New version of EV3JLibA
 V1.07 - Dec 2014: - Modified: New version of EV3JLibA
 V1.08 - Feb 2015: - Modified: New version of EV3JLibA
 V1.09 - Feb 2015: - Added: HT EOPD sensor
 V1.10 - Apr 2015: - Added: ArduinoLink
 V1.11 - May 2015: - Added: TemperatureSensor
 V1.12 - May 2015: - Added: I2CExpander
 V1.13 - May 2015: - Added: LegoRobot.playSample()
 */
import ch.aplu.ev3.*;
import java.io.*;
import java.lang.reflect.Method;
import java.net.*;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import lejos.hardware.LED;
import lejos.hardware.Brick;
import lejos.hardware.Button;
import lejos.hardware.Sound;
import lejos.hardware.ev3.LocalEV3;
import lejos.hardware.lcd.LCD;
import org.apache.commons.codec.binary.Base64;

public class BrickGate extends Thread
{
  private final static String VERSION = "1.13";

  // -------------- A filter to accept .py files -------------
  private class PyFilesFilter implements FilenameFilter
  {
    public boolean accept(File f, String name)
    {
      return (name.toLowerCase().endsWith(".py"));
    }
  }

  // -------------- Inner class Flasher ----------------------
  private class Flasher extends Thread
  {
    int stopPattern;

    public void setStopPattern(int stopPattern)
    {
      this.stopPattern = stopPattern;
    }

    public void run()
    {
      while (isFlashing)
      {
        led.setPattern(1);
        delay(50);
        led.setPattern(0);
        delay(4000);
      }
      led.setPattern(stopPattern);
    }

    void delay(long time)
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

  // ------------- Inner class DynamicUpdateClient ----
  private class DynamicUpdateClient extends Thread
  {
    private String hostUrl;
    private String ev3Url;
    private String userpass;
    private int ducInterval;
    private int ducIdleInterval;
    private boolean isFirst = true;

    public DynamicUpdateClient(String hostUrl, String ev3Url,
      String userpass, int ducInterval, int ducIdleInterval)
    {
      this.hostUrl = hostUrl;
      this.ev3Url = ev3Url;
      this.userpass = userpass;
      this.ducInterval = ducInterval;
      this.ducIdleInterval = ducIdleInterval;
    }

    public void run()
    {
      LCD.drawString("DUC started", 0, 7);
      while (isDucActive)
      {
        if (isFirst)
        {
          isFirst = false;
          updateRouterAddress(true);
        }
        else
          updateRouterAddress(!isConnected);  // Don't show if connected
        if (isDucActive)
        {
          try
          {
            if (isConnected)
              Thread.currentThread().sleep(60000 * ducInterval);
            else
              Thread.currentThread().sleep(60000 * ducIdleInterval);
          }
          catch (InterruptedException ex)
          {
          }
        }
      }
      showStatus("DUC terminated", "");
    }

    private void updateRouterAddress(boolean showStatus)
    {
      String params = "?hostname=" + ev3Url;
      try
      {
        URL dynupdate
          = new URL(hostUrl + params);
        URLConnection uc = dynupdate.openConnection();
        String basicAuth = "Basic "
          + new String(new Base64().encode(userpass.getBytes()));
        uc.setRequestProperty("Authorization", basicAuth);
        BufferedReader in = new BufferedReader(
          new InputStreamReader(
            uc.getInputStream()));
        String msg = "";
        String inputLine;
        int i = 0;
        while ((inputLine = in.readLine()) != null)
        {
          if (i == 0)
            msg = inputLine;
          i++;
        }
        in.close();
        if (showStatus)
          showStatus("DUC reply:", msg);
      }
      catch (IOException ex)
      {
        isDucActive = false;
        showStatus("DUC disabled", null);
      }
    }
  }

  // ------------- Inner interface Response -----------
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

  // ------------- End of inner classes ------------------
  //
  private final HashMap<String, String> classTags = new HashMap<String, String>()
  {
    // Relation between device name tag transmitted from client and class name

    
    {
      put("arl", "ArduinoLink");
      put("cs", "ColorSensor");
      put("_cs", "NxtColorSensor");
      put("gear", "Gear");
      put("_gear", "NxtGear");
      put("grs", "GyroRateSensor");
      put("gas", "GyroAngleSensor");
      put("htas", "HTAccelerometer");
      put("htgs", "HTGyroSensor");
      put("htbs", "HTBarometer");
      put("hteo", "HTEopdSensor");
      put("htes", "HTEopdShortSensor");
      put("htcp", "HTCompassSensor");
      put("htcs", "HTColorSensor");
      put("htis", "HTInfraredSeeker");
      put("htis$", "HTInfraredSeekerV2");
      put("i2c", "I2CExpander");
      put("ird", "IRDistanceSensor");
      put("irr", "IRRemoteSensor");
      put("irs", "IRSeekSensor");
      put("ls", "LightSensor");
      put("_ls", "NxtLightSensor");
      put("mot", "Motor");
      put("mmot", "MediumMotor");
      put("_mot", "NxtMotor");
      put("ods", "OpticalDistanceSensor");
      put("pts", "PrototypeSensor");
      put("rfid", "RFIDSensor");
      put("sps", "SuperProSensor");
      put("_ss", "NxtSoundSensor");
      put("ts", "TouchSensor");
      put("_ts", "NxtTouchSensor");
      put("us", "UltrasonicSensor");
      put("_us", "NxtUltrasonicSensor");
      put("temp", "TemperatureSensor");
    }
  };

  private String pyScriptFolder;
  private boolean isConnected = false;
  private int port;
  private ServerSocket serverSocket;
  private Socket socket;
  private InputStream is;
  private OutputStream os;
  private LegoRobot robot;
  private HashMap<String, Object> deviceTable = new HashMap<String, Object>();
  private boolean inError = false;
  private boolean isDucActive = true;
  private DynamicUpdateClient duc = null;
  private LED led;
  private boolean isFlashing = false;
  private Flasher flasher = null;
  private int curPos = 1; // range 1..7
  private int menuOffset = -1;
  private boolean isDisplayClean;
  private boolean isMenuActive;
  private boolean isAutonomous;
  private boolean isExiting = false;

  public BrickGate()
  {
    LCD.drawString("BrickGate  V" + VERSION, 1, 3);
    LCD.drawString("www.brickgate.ch", 1, 4);
    LCD.refresh();
    Brick localBrick = LocalEV3.get();
    led = localBrick.getLED();
    led = localBrick.getLED();
    EV3Properties props = new EV3Properties();
    port = props.getIntValue("IPport");
    pyScriptFolder = props.getStringValue("PyScriptFolder");

    File pyDir = new File(pyScriptFolder);

    isDucActive = props.getStringValue("DucEnabled").equals("true");
    if (isDucActive)
    {
      String hostUrl = props.getStringValue("DucHostUrl");
      String ev3Url = props.getStringValue("DucEV3Url");
      String userpass = props.getStringValue("DucUserPass");
      int ducInterval = props.getIntValue("DucUpdateInterval");
      int ducIdleInterval = props.getIntValue("DucIdleUpdateInterval");
      duc = new DynamicUpdateClient(hostUrl, ev3Url, userpass,
        ducInterval, ducIdleInterval);
      duc.start();
    }
    playServerUpMelody();
    startFlasher();
    killPython();  // eventually it is still running
    Tools.delay(3000);
    showFileMenu(0);
    start();

    while (isConnected || !Tools.isEscapePressed())
      Tools.delay(100);

    // Terminating gently BrickGate
    stopDuc();
    killPython();
    isMenuActive = false;
    isExiting = true;
    LCD.clear();
    LCD.drawString("Closing now...", 0, 1);
    LCD.refresh();
    stopFlasher(2);  // Both red
    Tools.delay(2000);
    try
    {
      serverSocket.close();  // take it out of socket.accept()
    }
    catch (Exception ex)
    {
    }
    led.setPattern(0);
  }

  public void run()
  {
    String cmd;
    try
    {
      serverSocket = new ServerSocket(port);
      List<String> localIPAddresses = getIPAddresses();
      if (localIPAddresses == null)
        throw new IOException();

      while (true)
      {
        isConnected = false;
        socket = serverSocket.accept(); // Blocking-------------------
        isMenuActive = false;  // Terminate menu thread
        init();
        stopFlasher(1);  // Both green
        isConnected = true;
        isDisplayClean = false;
        String ipAddress = socket.getInetAddress().getHostAddress();
        LCD.clear();
        boolean found = false;
        for (String adr : localIPAddresses)
        {
          if (adr.equals(ipAddress))
          {
            found = true;
            break;
          }
        }
        if (found)
        {
          isAutonomous = true;
          LCD.drawString("Autonomous CONNECT", 0, 6);
          LCD.drawString("DOWN+ENTER to quit", 0, 7);
        }
        else
        {
          isAutonomous = false;
          LCD.drawString("DOWN+ENTER to quit", 0, 7);
          if (ipAddress.length() <= 10)
            LCD.drawString(ipAddress + " CONNECT", 0, 6);
          else
            LCD.drawString(ipAddress + " CON", 0, 6);
        }
        LCD.refresh();
        try
        {
          is = new DataInputStream(socket.getInputStream());
          os = new DataOutputStream(socket.getOutputStream());
          while (true)
          {
            cmd = readCommand();
            executeCommand(cmd);
          }
        }
        catch (Exception ex)
        {
          startFlasher();
          showFileMenu(3000);
          if (isDucActive)
            duc.interrupt();  // Take out of sleep
        }
        if (robot != null)
        {
          robot.exit();
          robot = null;
        }
        cleanup();
      }
    }
    catch (IOException ex)
    {
      if (!isExiting)
      {
        stopDuc();
        LCD.clear();
        LCD.drawString("Can't create", 0, 1);
        LCD.drawString("BrickGate server", 0, 2);
        LCD.drawString("at port:", 0, 3);
        LCD.drawString("" + port, 0, 4);
        LCD.refresh();
      }
    }
  }

  private void init()
  {
    deviceTable.clear();
    robot = null;
    is = null;
    os = null;
  }

  private void stopDuc()
  {
    if (isDucActive)
    {
      isDucActive = false;
      if (duc != null)
        duc.interrupt();
    }
  }

  private void cleanup()
  {
    try
    {
      os.close();
    }
    catch (Exception ex)
    {
    }
    try
    {
      is.close();
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

  private void executeCommand(String cmd)
  {
    if (cmd.equals("getBrickGateVersion"))
    {
      sendReply(VERSION);
      return;
    }

    if (cmd.equals("getClassIdentifiers"))
    {
      String entries = "";
      for (String key : classTags.keySet())
        entries += key + "->" + classTags.get(key) + ";";
      sendReply(entries);
      return;
    }

    if (cmd.equals("getInstanceNames"))
    {
      String entries = "";
      for (String key : deviceTable.keySet())
        entries += key + "->" + classTags.get(key.substring(0, key.length() - 1))
          + ";";
      if (entries.equals(""))
        entries = "empty";
      sendReply(entries);
      return;
    }

    if (cmd.equals("getIPAddresses"))
    {
      String entries = "";
      for (String address : getIPAddresses())
        entries += address + ";";
      entries = entries.substring(0, entries.length() - 1);
      sendReply(entries);
      return;
    }

    if (cmd.equals("isAutonomous"))
    {
      if (isAutonomous)
        sendReply("1");
      else
        sendReply("0");
      return;
    }

    String[] splitter = cmd.split("\\.");  // Split on period .
    if (splitter == null || splitter.length < 2 || splitter.length > 4)
    {
      showError("COMMAND_ERROR", cmd);
      sendReply(Response.CMD_ERROR);   // Command error
      return;
    }
    String[] parts = new String[4];
    parts[0] = splitter[0];
    parts[1] = splitter[1];
    if (splitter.length == 2)
    {
      parts[2] = "n";
      parts[3] = "n";
    }
    else if (splitter.length == 3)
    {
      parts[2] = splitter[2];
      parts[3] = "n";
    }
    else
    {
      parts[2] = splitter[2];
      parts[3] = splitter[3];
    }

    String device = parts[0];
    String method = parts[1];
    String param1 = parts[2];
    String param2 = parts[3];

    if (device.substring(0, device.length() - 1).equals("robo"))
      dispatchRobot(method, param1, param2);
    else
      dispatchCommand(device, method, param1, param2);
  }

  private void dispatchRobot(String method, String param1, String param2)
  {
    String reply = "";

    if (method.equals("drawStringAt"))
    {
      param1 = param1.replace('`', '.');
      param2 = param2.replace('`', '.');
      if (!isDisplayClean)
      {
        isDisplayClean = true;
        LCD.clear();
      }
    }

    if (method.equals("create"))
    {
      if (param1.equals("0"))
        robot = new LegoRobot(false, false);
      else
        robot = new LegoRobot(false, true);
      reply = Response.OK;
    }
    else if (method.equals("addPart")) // Special method "addPart"
    {
      String deviceID = param1;
      String port = param2;
      int portID = 0;
      boolean isMotor = false;
      boolean isGear = false;
      try
      {
        if (deviceID.equals("gear") || deviceID.equals("_gear"))
          isGear = true;
        else
        {
          char portChar = port.charAt(0);
          if (portChar > 64 && portChar < 69)
          {
            portID = portChar - 65;
            isMotor = true;
          }
          else
            portID = portChar - 49;

          if (portID < 0 || portID > 3)
          {
            showError("ILLEGAL_PORT", port);
            sendReply(Response.ILLEGAL_PORT);
            return;
          }
        }

        String classNameStr = "ch.aplu.ev3." + classTags.get(deviceID);
        Class deviceClass = Class.forName(classNameStr);
        Object deviceObj;
        if (isMotor)
        {
          deviceObj = deviceClass.getConstructor(MotorPort.class).
            newInstance(new MotorPort(portID));
          deviceTable.put(deviceID + port, deviceObj);
        }
        else if (isGear)
        {
          deviceObj = deviceClass.newInstance();
          deviceTable.put(deviceID, deviceObj);
        }
        else
        {
          deviceObj = deviceClass.getConstructor(SensorPort.class).
            newInstance(new SensorPort(portID));
          deviceTable.put(deviceID + port, deviceObj);
        }
        robot.addPart((Part)deviceObj);
        reply = Response.OK;
      }
      catch (Exception ex)
      {
        showError("CREATION_FAILED", deviceID);
        sendReply(Response.CREATION_FAILED);
        return;
      }
    }
    else  // Standard robot command
    {
      try
      {
        Class c = Class.forName("ch.aplu.ev3.LegoRobot");
        Method m = c.getMethod(method, getArgTypes(param1, param2));
        Object rc = m.invoke(robot, getArgValues(param1, param2));
        if (rc instanceof Integer)
          reply = ((Integer)rc).toString();
        else if (rc instanceof String)
          reply = (String)rc;
        else if (rc instanceof Boolean)
        {
          Boolean b = (Boolean)rc;
          reply = (b ? "1" : "0");
        }
        else
          reply = Response.OK;
      }
      catch (Exception ex)
      {
//        System.out.println(ex);  // For detailed errors
        showError("ILLEGAL_METHOD", "robot." + method);
        sendReply(Response.ILLEGAL_METHOD);
        return;
      }
    }
    sendReply(reply);
    clearError();
  }

  private void dispatchCommand(String deviceName, String method, String param1, String param2)
  {
    boolean isMotor = false;
    boolean isGear = false;
    int portId = 0;
    if ((deviceName.length() >= 3 && deviceName.substring(0, 3).equals("mot"))
      || ((deviceName.length() >= 4) && deviceName.substring(1, 4).equals("mot")))
      isMotor = true;

    if (deviceName.equals("gear") || deviceName.equals("_gear"))
      isGear = true;
    else
    {
      if (isMotor)
        portId = (int)deviceName.charAt(deviceName.length() - 1) - 65;
      else
        portId = (int)deviceName.charAt(deviceName.length() - 1) - 49;

      if (portId < 0 || portId > 3)
      {
        showError("ILLEGAL_PORT", deviceName);
        sendReply(Response.ILLEGAL_PORT);
        return;
      }
    }

    String classTag;
    if (isGear)
      classTag = deviceName;
    else
      classTag = deviceName.substring(0, deviceName.length() - 1);

    // For performance optimization, 'getValue' is treated specially
    if (method.equals("getValue"))
    {
      try
      {
        Object deviceObj = deviceTable.get(deviceName);
        Class c = Class.forName("ch.aplu.ev3." + classTags.get(classTag));
        Method m = c.getMethod(method, new Class[]
        {
        });
        Object rc = m.invoke(deviceObj, new Object[]
        {
        });
        sendResponse(rc);
        return;
      }
      catch (Exception ex)
      {
        showError("ILLEGAL_METHOD", method);
        sendReply(Response.ILLEGAL_METHOD);
        return;
      }
    }
    else if (method.equals("readPrototype"))  // Prototype sensor read inputs
    {
      PrototypeSensor ps = (PrototypeSensor)deviceTable.get(deviceName);
      int[] ain = new int[5];
      int[] din = new int[6];
      ps.read(ain, din);
      String reply = "";
      for (int i = 0; i < 5; i++)
        reply += ((Integer)ain[i]).toString() + ":";
      int power = 1;
      int digit = 0;
      for (int i = 0; i < 6; i++)
      {
        digit += din[i] * power;
        power *= 2;
      }
      reply += ((Integer)digit).toString();
      clearError();
      sendReply(reply);
    }
    else if (method.equals("readSuperPro"))  // SuperPrototype sensor read inputs
    {
      SuperProSensor sps = (SuperProSensor)deviceTable.get(deviceName);
      int[] ain = new int[4];
      int[] din = new int[8];
      sps.read(ain, din);
      String reply = "";
      for (int i = 0; i < 4; i++)
        reply += ((Integer)ain[i]).toString() + ":";
      int power = 1;
      int digit = 0;
      for (int i = 0; i < 8; i++)
      {
        digit += din[i] * power;
        power *= 2;
      }
      reply += ((Integer)digit).toString();
      clearError();
      sendReply(reply);
    }
    else if (method.equals("getReplyArduino"))  // ArduinoLink
    {
      ArduinoLink arl = (ArduinoLink)deviceTable.get(deviceName);
      int request = new Integer(param1);
      int[] data = new int[16];
      arl.getReply(request, data);
      String reply = "";
      for (int i = 0; i < 16; i++)
        reply += ((Integer)data[i]).toString() + ":";
      clearError();
      sendReply(reply);
    }
    else if (method.equals("getTemperature")) 
    {
      TemperatureSensor temp = (TemperatureSensor)deviceTable.get(deviceName);
      int rc = (int)(100 * temp.getTemperature());
      String reply = ((Integer)rc).toString();
      clearError();
      sendReply(reply);
    }
    else // Standard method call
    {
      try
      {
        Object deviceObj = deviceTable.get(deviceName);
        Class c = Class.forName("ch.aplu.ev3." + classTags.get(classTag));
        Method m = c.getMethod(method, getArgTypes(param1, param2));
        Object rc = m.invoke(deviceObj, getArgValues(param1, param2));
        sendResponse(rc);
      }
      catch (Exception ex)
      {
        showError("ILLEGAL_METHOD", method);
        sendReply(Response.ILLEGAL_METHOD);
      }
    }
  }

  private Class[] getArgTypes(String param1, String param2)
  {
    Class clazz1 = null;
    if (param1.charAt(0) == 'n')
      clazz1 = null;
    else if (param1.charAt(0) == 'b')
      clazz1 = boolean.class;
    else if (param1.charAt(0) == 's')
      clazz1 = String.class;
    else
      clazz1 = int.class;

    Class clazz2 = null;
    if (param2.charAt(0) == 'n')
      clazz2 = null;
    else if (param2.charAt(0) == 'b')
      clazz2 = boolean.class;
    else if (param2.charAt(0) == 's')
      clazz2 = String.class;
    else
      clazz2 = int.class;

    // param = "n" : No no parameter -----------------
    if (param1.equals("n")) // First parameter "n" -> no parameters
      return new Class[]
      {
      };

    // Second parameter "n" -> only 1 parameter ----------------
    if (param2.equals("n"))
    {
      Class[] c = new Class[1];
      c[0] = clazz1;
      return c;
    }

    // Two parameters ------------------------
    Class[] c = new Class[2];
    c[0] = clazz1;
    c[1] = clazz2;
    return c;
  }

  private Object[] getArgValues(String param1, String param2)
  {
    Object value1 = null;
    if (param1.charAt(0) == 'n')
      value1 = null;
    else if (param1.charAt(0) == 'b')
      value1 = new Boolean(param1.charAt(1) == '1');
    else if (param1.charAt(0) == 's')
      value1 = new String(param1.substring(1));
    else
      value1 = new Integer(param1);

    Object value2 = null;
    if (param2.charAt(0) == 'n')
      value2 = null;
    else if (param2.charAt(0) == 'b')
      value2 = new Boolean(param2.charAt(1) == '1');
    else if (param2.charAt(0) == 's')
      value2 = new String(param2.substring(1));
    else
      value2 = new Integer(param2);

    Object[] argValues = null;
    if (param1.equals("n"))
    {
      argValues = new Object[]
      {
      };
    }
    else if (param2.equals("n"))
    {
      argValues = new Object[1];
      argValues[0] = value1;
    }
    else
    {
      argValues = new Object[2];
      argValues[0] = value1;
      argValues[1] = value2;
    }
    return argValues;
  }

  private void sendResponse(Object rc)
  {
    String reply;
    if (rc instanceof Integer)
      reply = ((Integer)rc).toString();
    else if (rc instanceof Long)
      reply = ((Long)rc).toString();
    else if (rc instanceof String)
      reply = (String)rc;
    else if (rc instanceof Boolean)
    {
      Boolean b = (Boolean)rc;
      reply = (b ? "1" : "0");
    }
    else if (rc instanceof Vector2D)
    {
      Vector2D v = (Vector2D)rc;
      reply = v.x + ";" + v.y;
    }
    else if (rc instanceof Vector3D)
    {
      Vector3D v = (Vector3D)rc;
      reply = v.x + ";" + v.y + ";" + v.z;
    }
    else
      reply = Response.OK;
    clearError();
    sendReply(reply);
  }

  private String readCommand() throws IOException
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

  private void sendReply(String reply)
  {
    reply = reply + "\n";
    byte[] ary = reply.getBytes(Charset.forName("UTF-8"));
    try
    {
      os.write(ary);
      os.flush();
    }
    catch (IOException ex)
    {
      showError("sendReply()", "failed");
    }
  }

  private void showError(String msg1, String msg2)
  {
    inError = true;
    LCD.clear(5);
    LCD.clear(6);
    LCD.drawString(msg1, 0, 5);
    LCD.drawString(msg2, 0, 6);
    LCD.refresh();
  }

  private void showStatus(final String msg1, final String msg2)
  {
    new Thread()
    {
      public synchronized void run()
      {
        LCD.clear(6);
        LCD.drawString(msg1, 0, 6);
        if (msg2 != null)
        {
          LCD.clear(7);
          LCD.drawString(msg2, 0, 7);
        }
        LCD.refresh();
        Tools.delay(4000);
        LCD.clear(6);
        if (msg2 != null)
          LCD.clear(7);
        LCD.refresh();
      }
    }.start();
  }

  private void showTimed(final String msg1, final String msg2, int timeout)
  {
    LCD.clear(6);
    LCD.drawString(msg1, 0, 6);
    if (msg2 != null)
    {
      LCD.clear(7);
      LCD.drawString(msg2, 0, 7);
    }
    LCD.refresh();
    Tools.delay(4000);
    LCD.clear(6);
    if (msg2 != null)
      LCD.clear(7);
    LCD.refresh();
    Tools.delay(timeout);
  }

  private void clearError()
  {
    // Clear only, if an error was displayed, because of delay
    // for displaying text  
    if (inError)
    {
      inError = false;
      LCD.clear(5);
      LCD.clear(6);
      LCD.refresh();
    }
  }

  private static void showDebug(String msg)
  {
    LCD.clear();
    LCD.refresh();
    System.out.println(msg);
    Tools.delay(6000);
  }

  private void playServerUpMelody()
  {
    Sound.playTone(2000, 50);
    Tools.delay(30);
    Sound.playTone(2000, 50);
    Tools.delay(30);
  }

  private void playDebugMelody(int nb)
  {
    int freq = 200;
    for (int i = 0; i < nb; i++)
    {
      Sound.playTone(freq, 100);
      Tools.delay(100);
    }
    Tools.delay(500);
  }

  /**
   * Get all the IP addresses for the device
   */
  public List<String> getIPAddresses()
  {
    List<String> result = new ArrayList<String>();
    Enumeration<NetworkInterface> interfaces;
    try
    {
      interfaces = NetworkInterface.getNetworkInterfaces();
      while (interfaces.hasMoreElements())
      {
        NetworkInterface current = interfaces.nextElement();
        if (!current.isUp() || current.isLoopback() || current.isVirtual())
          continue;
        Enumeration<InetAddress> addresses = current.getInetAddresses();
        while (addresses.hasMoreElements())
        {
          InetAddress current_addr = addresses.nextElement();
          if (current_addr.isLoopbackAddress())
            continue;
          result.add(current_addr.getHostAddress());
        }
      }
      return result;
    }
    catch (Exception ex)
    {
      showError("getIPAddresses", ex.toString());
      return null;
    }
  }

  private void startFlasher()
  {
    if (flasher != null)
      return;
    isFlashing = true;
    flasher = new Flasher();
    flasher.start();
  }

  private void stopFlasher(int stopPattern)
  {
    if (flasher == null)
      return;
    flasher.setStopPattern(stopPattern);
    isFlashing = false;
    flasher.interrupt();
    flasher = null;
  }

  private void spawnProg(final String progName)
  {
    String prog = "pyrun " + pyScriptFolder + "/" + progName;
    LCD.clear();
    showTimed("Executing:", progName, 5000);

    try
    {
      Runtime.getRuntime().exec(prog);
    }
    catch (IOException ex)
    {
    }
  }

  private void showFileMenu(final int startDelay)
  {
    isMenuActive = true;
    new Thread()
    {
      public void run()
      {
        Tools.delay(startDelay);
//        playDebugMelody(2);   // For debugging
        menuSelect();
//        playDebugMelody(1);
      }
    }.start();
  }

  private void menuSelect()
  // Menu thread terminates when script executes  
  {
    boolean isButtonPressed = false;
    boolean isPendingDelete = false;

    ArrayList<String> fileList = getFileList();
    displayFileList(fileList, curPos, menuOffset);
    while (isMenuActive)
    {
      ArrayList<String> newList = getFileList();
      boolean update = false;
      if (newList.size() != fileList.size())
        update = true;
      else
      {
        for (int i = 0; i < newList.size(); i++)
        {
          if (!newList.get(i).equals(fileList.get(i)))
          {
            update = true;
            break;
          }
        }
      }
      if (update)
      {
        fileList.clear();
        for (String s : newList)
          fileList.add(s);
        curPos = 1;
        menuOffset = -1;
        displayFileList(fileList, curPos, menuOffset);
      }

      int buttonState = Button.readButtons();
      if (buttonState == 0 && isButtonPressed)
        isButtonPressed = false;
      else if (buttonState != 0 && !isButtonPressed)
      {
        isButtonPressed = true;
        switch (buttonState)
        {
          case Button.ID_DOWN:
            if (isPendingDelete)
              break;
            if (fileList.isEmpty())
              break;
            if (curPos == fileList.size())
              break;
            if (curPos == 7)
            {
              if (8 + menuOffset == fileList.size())
                break;
              menuOffset += 1;
              displayFileList(fileList, curPos, menuOffset);
              break;
            }
            curPos++;
            displayFileList(fileList, curPos, menuOffset);
            break;
          case Button.ID_UP:
            if (isPendingDelete)
              break;
            if (fileList.isEmpty())
              break;
            if (curPos == 1)
            {
              if (menuOffset == -1)
                break;
              menuOffset -= 1;
              displayFileList(fileList, curPos, menuOffset);
              break;
            }
            curPos--;
            displayFileList(fileList, curPos, menuOffset);
            break;
          case Button.ID_RIGHT:
            if (isPendingDelete)
              break;
            if (fileList.isEmpty())
              break;
            LCD.clear();
            LCD.drawString("Deleting script...", 0, 4);
            LCD.drawString("ENTER to confirm", 0, 5);
            LCD.drawString("ESCAPE to cancel", 0, 6);
            LCD.refresh();
            isPendingDelete = true;
            break;
          case Button.ID_LEFT:
            if (fileList.isEmpty())
              break;
            break;
          case Button.ID_ESCAPE:
            if (isPendingDelete)
            {
              isPendingDelete = false;
              displayFileList(fileList, curPos, menuOffset);
            }
            break;
          case Button.ID_ENTER:
            if (fileList.isEmpty())
              break;
            String scriptFile = fileList.get(curPos + menuOffset);
            String scriptPath = pyScriptFolder + "/" + scriptFile;
            if (isPendingDelete)
            {
              File f = new File(scriptPath);
              boolean rc = f.canRead();
              if (!rc)
              {
                LCD.clear();
                showTimed("Can't delete", scriptFile, 3000);
                LCD.refresh();
              }
              else
              {
                LCD.clear();
                rc = new File(scriptPath).delete();
                if (!rc)
                  showTimed("Can't delete", scriptFile, 3000);
                LCD.refresh();
              }
              isPendingDelete = false;
              displayFileList(fileList, curPos, menuOffset);
              break;
            }
            boolean rc = new File(scriptPath).canRead();
            if (!rc)
            {
              LCD.clear();
              showTimed("Can't execute", scriptFile, 3000);
              displayFileList(fileList, curPos, menuOffset);
            }
            else
            {
              isMenuActive = false;
              spawnProg(scriptFile);
            }
            break;
        }
      }
      Tools.delay(10);
    }
  }

  private ArrayList<String> getFileList()
  {
    ArrayList<String> fileList = new ArrayList<String>();
    File[] files = (new File(pyScriptFolder)).listFiles(new PyFilesFilter());
    if (files == null)
    {
      LCD.clear();
      LCD.drawString("Illegal directory:", 0, 6);
      LCD.drawString(pyScriptFolder, 0, 7);
      LCD.refresh();
      return fileList;
    }
    int len = 0;
    for (int i = 0; i < files.length && files[i] != null; i++)
      len++;
    if (len == 0)
      return fileList;
    for (File f : files)
      fileList.add(f.getName());
    Collections.sort(fileList);
    return fileList;
  }

  private void displayFileList(ArrayList<String> fileList, int curPos, int offset)
  {
    LCD.clear();
    LCD.drawString("Wait connection...", 0, 0);
    if (fileList.size() < 7)
      LCD.drawString("ESCAPE to quit", 0, 7);
    if (!fileList.isEmpty())
    {
      for (int linePos = 1; linePos < 8 && linePos <= fileList.size(); linePos++)
      {
        int itemPos = linePos + offset;
        if (curPos == linePos)
          LCD.drawString("->" + fileList.get(itemPos), 0, linePos);
        else
          LCD.drawString(fileList.get(itemPos), 2, linePos);
      }
      if (fileList.size() < 6)
        LCD.drawString("ENTER to execute", 0, 6);
    }
    LCD.refresh();
  }

  private void killPython()
  {
    String cmd = "killall python";
    try
    {
      Runtime.getRuntime().exec(cmd);
    }
    catch (IOException ex)
    {
    }
  }

  public static void main(String[] args)
  {
    new BrickGate();
  }
}
