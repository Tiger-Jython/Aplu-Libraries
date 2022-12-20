// ESM.java

/* Protocol for downloading files

 - Put target into raw mode: Ctrl+A
 - Send line per line and terminate with Ctrl-D (without <cr><lf>)
 fd = open('Ex1.py', 'wb')
 f = fd.write
 f(b'text1\r\n')  \r\n may be omited, is part of file
 f(b'text2\r\n')
 ...
 fd.close()
 - Put target into normal mode: Ctrl+B
 */
package ch.aplu.esm;

import jssc.*;
import java.io.*;
import java.nio.charset.Charset;
import java.util.ArrayList;
import org.apache.commons.cli.*;
import ch.aplu.util.ExitListener;
import ch.aplu.util.GConsole;
import ch.aplu.util.Monitor;
import ch.aplu.util.Position;
import ch.aplu.util.Size;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Toolkit;
import java.awt.event.KeyEvent;
import java.util.Properties;

public class ESM implements SerialPortEventListener
{
  private class SendThread extends Thread
  {
    private byte b;
    private byte[] bAry = null;

    public SendThread(byte b)
    {
      this.b = b;
    }

    public SendThread(byte[] bAry)
    {
      this.bAry = bAry;
    }

    public void run()
    {
      try
      {
        if (bAry != null)
        {
          for (byte b : bAry)
          {
            serialPort.writeByte(b);
            delay(100);
          }
        }

        else

          serialPort.writeByte(b);
      }
      catch (Exception ex)
      {
      }
    }
  }

  // ------------------- RunThread -------------------------
  class RunThread extends Thread
  {

    private String[] args;

    public RunThread(String[] args)
    {
      this.args = args;
    }

    public void run()
    {
      init(args);
    }
  }

  // ------------------- InputReader -------------------------
  private class InputReader extends Thread
  {

    public void run()
    {
      boolean inLine = false;
      debug("Starting InputReader Thread");
      char ch;
      while (console != null && !console.isDisposed())
      {
        try
        {
          delay(50);
          ch = console.getKey(true);
          int code = console.getKeyCode();
          if (ch == '\b')  // Backspace
            inLine = false;
          if (code == 38 && !inLine) // Cursor up
          {
            // Send last line
            String line = console.getLastLine().substring(4);
            byte[] bytes = line.getBytes();
            for (byte b : bytes)
            {
              serialPort.writeByte(b);  // Send last line
              delay(10);
            }
          }

          if (ch != KeyEvent.CHAR_UNDEFINED)
          {
            if (ch == '\n')
            {
              serialPort.writeByte((byte)('\r'));
              inLine = false;
            }
            else
            {
              int ascii = (int)ch;
              if ((ascii > 0 && ascii < 10) || (ascii >= 32 && ascii < 128))
              {
                serialPort.writeByte((byte)ch);
                inLine = true;
              }
            }
          }
        }
        catch (Exception ex)
        {
        }
      }
      debug("InputReader Thread terminated");
    }
  }

  // ------------------- ExitListener ------------------------
  private class MyExitListener implements ExitListener
  {

    public void notifyExit()
    {
      if (idle)
        terminate();
      else
        System.out.println("Process in progress. Please wait...");
    }
  }

  // ------------------- Constant declarations ------------------------
  private final String VERSION = "1.47 - June 10, 2019";
  // ------------------------------------------------------

  private final String waitAtReset = "scheduler on ";  // wait after hard reset
  private static boolean debug = false;
  private static int extractionResult = 0;
  private final int blockSize = 512; // Download block size
  private static String destDir = null;
  private static ESM instance = null;
  private static boolean idle = true;
  private final int consoleUlxDefault = 10;
  private final int consoleUlyDefault = 10;
  private final int consoleWidthDefault = 600;
  private final int consoleHeightDefault = 400;
  private final int consoleFontSizeDefault = 12;
  private CommandLine cmd;
  private Options options = new Options();
  private String portName;
  private String firmwarePath = null;
  private final int baudrate = 115200;
  private static SerialPort serialPort = null;
  private StringBuilder reply = new StringBuilder();
  private StringBuilder capBuf = new StringBuilder();
  private boolean fileCapture = false;
  private String fileContent = null;
  private int fileSize = -1;
  private static boolean dataCaptureEnabled = false;
  private static StringBuilder dataBuf = new StringBuilder();
  private static ArrayList<String> dataLineBuf = new ArrayList();
  private boolean bootCapture = false;
  private boolean printCapture = false;
  private boolean fileSizeCapture = false;
  private int bufLen;
  private int nbChars;
  private boolean fileListCapture = false;
  private String fileList = null;
  private int nbRetries = 4;
  private boolean isError = false;
  private static GConsole console = null;
  private Integer consoleUlx = null;
  private Integer consoleUly = null;
  private Integer consoleWidth = null;
  private Integer consoleHeight = null;
  private Integer consoleFontSize = null;
  private boolean oxocard = false;
  private boolean blockly = false;
  private boolean espLoboris = false;
  private boolean downloadModulesOnly = false;

  // ----------------- Messages -----------------
  private final String versionInfo
    = "ESPManager V" + VERSION + " [www.aplu.ch]";
  private final String noCommPort = "COM port not found. Possible reasons:\n"
    + "   Not connected -> Plug module into USB port\n"
    + "   Automatic COM port search failed or wrong COM port\n"
    + (Tools.isLinux() ? "\n   Linux: No permission for serial port->\n     Execute 'sudo adduser <username> dialout' and reboot!" : "");
  private final String noResponse = "Module not responding. Press the reset button or reconnect USB connection.";
  private final String startingInfo1
    = "Entering REPL/Console and starting 'main.py'\n   -->Press ^C to stop."
    + "\n   -->Press reset button to restart 'main.py'.";
  private final String startingInfo3
    = "Entering REPL/Console and starting 'appmain.py'\n   -->Press ^C to stop."
    + "\n   -->Press reset button(s) to restart 'appmain.py'.";
  private final String startingInfo2 = "Entering REPL/Console.";

  // ------------------- Constructor -----------------------
  public ESM(String[] args)
  {
    if (!idle)
    {
      System.out.println("Process in progress. Please wait...");
      return;
    }
    if (console != null)
    {
      terminate();
      delay(1000);  // Wait for ReaderThread to terminate
    }

    // Must run in separate thread because of call from TigerJython
    Thread t = new RunThread(args);
    t.start();
    instance = this;
  }

  private void init(String[] args)
  {
    idle = false;
    isError = false;
    if (debug)
    {
      System.out.println("-->Debug; args:");
      for (int i = 0; i < args.length; i++)
      {
        System.out.println("-->Debug: #" + i + ": " + args[i]);
      }
    }
    boolean rc = validateOptions(args);
    if (!rc)
    {
      idle = true;
      return;
    }

    if (cmd.hasOption("h"))
    {
      System.out.println(versionInfo);
      new HelpFormatter().printHelp("java -jar "
        + getClass().getName() + ".jar",
        "",
        options,
        "",
        true);
    }

    if (cmd.hasOption("d"))
    {
      debug = true;
    }

    if (cmd.hasOption("m"))
    {
      oxocard = true;
    }

    if (cmd.hasOption("z"))
    {
      downloadModulesOnly = true;
    }

    if (cmd.hasOption("o"))
    {
      parseProperties(cmd.getOptionValue("o").trim());
    }

    if (cmd.hasOption("p"))
    {
      portName = cmd.getOptionValue("p").trim();
    }
    else
    {
      portName = Tools.findCommPort(0); // No retries
    }
    debug("Got port name: " + portName);
    if (portName.equals(""))
    {
      System.out.println(noCommPort);
      idle = true;
      return;
    }

    if (cmd.hasOption("w"))
    {
      firmwarePath = cmd.getOptionValue("w").trim();
    }

    if (cmd.hasOption("n"))
    {
      System.out.println("Searching COM ports...");
      String[] comPorts = Tools.enumCommPorts();
      if (comPorts.equals(""))
        System.out.println("No COM ports found.");
      else
      {
        System.out.println("Found COM ports:");
        for (String comPort : comPorts)
          System.out.println(comPort);
      }
      idle = true;
      return;
    }

    if (cmd.hasOption("x") || cmd.hasOption("t"))
    {
      rc = openTerminal();
      if (!rc)
      {
        System.out.println(noResponse);
        idle = true;
        return;
      }

      if (cmd.hasOption("x"))
      {
        printCapture = true;
        if (oxocard)
          System.out.println(startingInfo3);
        else
          System.out.println(startingInfo1);
        softReset();
      }
      else
      {
        printCapture = true;
        System.out.println(startingInfo2);
        sendCommand("\r");
      }

      startUserInput();
    }
    else if (cmd.hasOption("r"))
    {
      rc = copyrun(cmd.getOptionValue("r").trim());
      if (rc)
        startUserInput();
    }
    else if (cmd.hasOption("c"))
    {
      copy(cmd.getOptionValue("c").trim());
    }
    else if (cmd.hasOption("e"))
    {
      extractFile(cmd.getOptionValue("e").trim());
    }
    else if (cmd.hasOption("b"))
    {
      rc = browseFileSystem(true);
      if (!rc)
        System.out.println(noResponse);
    }
    else if (cmd.hasOption("f"))
    {
      if (oxocard)
      {
        if (!downloadModulesOnly)
        {
          SelectionDialog dlg = new SelectionDialog(0);
          Monitor.putSleep();
          delay(2000);
          dlg.dispose();
          int choice = dlg.getChoice();
          if (choice == -1)  // no selection
          {
            System.out.println("No programming language selected.");
            idle = true;
            return;
          }
          if (choice == 0)
            blockly = false;
          else if (choice == 1)
            blockly = true;
        }
      }
      else
      {
        SelectionDialog dlg = new SelectionDialog(1);
        Monitor.putSleep();
        delay(2000);
        dlg.dispose();
        int choice = dlg.getChoice();
        if (choice == -1)  // no selection
        {
          System.out.println("No ESM board selected.");
          idle = true;
          return;
        }
        if (choice == 0)
          espLoboris = false;
        else if (choice == 1)
          espLoboris = true;
      }
      rc = flash();
      if (!rc)
        System.out.println("Flashing of ESP failed.");
      else
      {
        if (oxocard && !blockly)
        {
          System.out.println("Starting appmain.py now...");
          softReset();
          closePort();
        }
      }
    }
    idle = true;
  }

  private boolean validateOptions(String[] args)
  {
    OptionGroup actionGroup = new OptionGroup();

    options.addOption(Option.builder("o").
      longOpt("options").
      hasArgs().
      desc("Options setting. arg={key1=value1;key2=value2;...}\n"
        + "where keys are:\n"
        + "console.xpos,console.ypos (-1: screen center)\n"
        + "console.width,console.height, console.fontsize\n"
        + "e.g. -o {console.ypos=400;console.fontsize=14}").
      build());

    options.addOption(Option.builder("p").
      longOpt("port").
      hasArgs().
      desc("Serial port (COM port). If omitted, COM port search performed.").
      build());

    options.addOption(Option.builder("d").
      longOpt("debug").
      desc("Debug mode: Verbose debug messages to stdio.").
      build());

    options.addOption(Option.builder("m").
      longOpt("oxocard").
      desc("ESM for Oxocard").
      build());

    options.addOption(Option.builder("z").
      longOpt("modules").
      desc("Download modules for Oxocard").
      build());

    actionGroup.addOption(
      Option.builder("f").
        longOpt("flash").
        desc("Flash firmware. (All files are erased.)").
        build());

    // ----------- actionGroup -------------
    actionGroup.addOption(
      Option.builder("h").
        longOpt("help").
        desc("Show help message.").
        build());

    actionGroup.addOption(
      Option.builder("x").
        longOpt("execute").
        desc(oxocard ? "Open terminal/console (REPL) and execute appmain.py."
          : "Open terminal/console (REPL) and execute main.py.").
        build());

    actionGroup.addOption(
      Option.builder("t").
        longOpt("terminal").
        desc("Open terminal/console (REPL)").
        build());

    actionGroup.addOption(
      Option.builder("n").
        longOpt("enum").
        desc("Enumerate COM ports").
        build());

    actionGroup.addOption(
      Option.builder("b").
        longOpt("browse").
        desc("Browse target file system.").
        build());

    actionGroup.addOption(
      Option.builder("r").
        longOpt("run").
        hasArgs().
        desc(oxocard ? "Download file, rename to appmain.py, open terminal/console (REPL) and execute."
          : "Download file, rename to main.py, open terminal/console (REPL) and execute.").
        build());

    actionGroup.addOption(
      Option.builder("c").
        longOpt("copy").
        hasArgs().
        desc("Download file (keep name). <arg> = source path").
        build());

    actionGroup.addOption(
      Option.builder("e").
        longOpt("extract").
        hasArgs().
        desc("Extract file from target <arg> = file name").
        build());

    options.addOption(Option.builder("w").
      longOpt("flash").
      hasArgs().
      desc("Path to firmware flash folder. If omitted, subdirectory 'Lib' of working directory assumed.").
      build());

    actionGroup.setRequired(true);
    options.addOptionGroup(actionGroup);
    CommandLineParser parser = new DefaultParser();

    try
    {
      cmd = parser.parse(options, args);
    }
    catch (ParseException ex)
    {
      new HelpFormatter().printHelp("java -jar "
        + getClass().getName() + ".jar",
        "",
        options,
        "",
        true);
      return false;
    }
    return true;
  }

  private void parseProperties(String optionString)
  {
    optionString = optionString.replace(';', '\n');
    optionString = optionString.substring(1, optionString.length() - 1);
    Properties prop = parsePropertiesString(optionString);
    if (prop == null)
    {
      return;
    }
    consoleUlx = toInt(prop.getProperty("console.xpos"));
    consoleUly = toInt(prop.getProperty("console.ypos"));
    consoleWidth = toInt(prop.getProperty("console.width"));
    consoleHeight = toInt(prop.getProperty("console.height"));
    consoleFontSize = toInt(prop.getProperty("console.fontsize"));
  }

  private Integer toInt(String s)
  {
    if (s == null)
    {
      return null;
    }

    try
    {
      return Integer.parseInt(s.trim());
    }
    catch (NumberFormatException ex)
    {
      return null;
    }
  }

  public Properties parsePropertiesString(String s)
  {
    try
    {
      final Properties p = new Properties();
      p.load(new StringReader(s));
      return p;
    }
    catch (IOException ex)
    {
      return null;
    }
  }

  private void startUserInput()
  {
    if (console == null)
      return;
    InputReader ir = new InputReader();
    ir.start();
  }

  private boolean checkPort()
  {
    try
    {
      serialPort = new SerialPort(portName);
      serialPort.openPort();
    }
    catch (Exception ex)
    {
      System.out.println("Failed to access COM port '" + portName + "'");
      return false;
    }
    closePort();
    return true;
  }

  private boolean openTerminal()
  {
    if (isError)
      return false;
    System.out.println(versionInfo);
    System.out.println("Opening terminal at COM port " + portName + "...");
    console = openConsole();
    closePort();
    checkPort();
    try
    {
      openPort(portName);
      boolean rc = finish(true);
      if (!rc)
        return false;
    }
    catch (IOException ex)
    {
      return false;
    }
    return true;
  }

  private void openPort(String portName) throws IOException
  {
    try
    {
      serialPort = new SerialPort(portName);
      serialPort.openPort();
      serialPort.setParams(115200, 8, 1, 0);
      int mask = SerialPort.MASK_RXCHAR;
      serialPort.setEventsMask(mask);
      serialPort.addEventListener(this);
      delay(2000);
    }
    catch (Exception e)
    {
      throw new IOException("Failed to open COM port " + portName
        + "\nPort may be in use by another program.");
    }
  }

  private boolean copyrun(String source)
  {
    if (isError)
      return false;
    idle = false;
    System.out.println(versionInfo);

    String target;
    if (oxocard)
    {
      System.out.println("Transferring source via COM port " + portName + " to target 'appmain.py' and running it...");
      target = "appmain.py";
    }
    else
    {
      System.out.println("Transferring source via COM port " + portName + " to target 'main.py' and running it...");
      target = "main.py";
    }

    File fSourcePath = new File(source);
    if (!fSourcePath.exists())
    {
      System.out.println("Can't find file '" + source + "'");
      idle = true;
      return false;
    }
    console = openConsole();
    closePort();
    if (!checkPort())
    {
      idle = true;
      return false;
    }

    boolean rc = transfer(source, target);
    if (!rc)
    {
      idle = true;
      return false;
    }
    if (oxocard)
      System.out.println(startingInfo3);
    else
      System.out.println(startingInfo1);
    runScript();
    idle = true;
    return true;
  }

  private void hardReset()
  {
    if (isError)
      return;
    debug("Calling hardReset()");
    sendCommand("import machine\rmachine.reset()\r");
  }

  private void runScript()
  {
    bootCapture = true;
    hardReset();
    return;
  }

  private boolean transfer(String source, String targetFile)
  {
    if (isError)
      return false;
    try
    {
      openPort(portName);
    }
    catch (IOException ex)
    {
      System.out.println("File transfer failed.\nError: " + ex.getMessage());
      return false;
    }

    int nb = 0;
    while (nb < nbRetries)
    {
      try
      {
        if (nb > 0)
          System.out.println("Waiting for ESM ... ");
        boolean rc = finish(true);
        if (!rc)
          return false;
        delay(2000);
        rawOn();
        put(source, targetFile);
        rawOff();
        break;
      }
      catch (IOException ex)
      {
        System.out.println("File transfer failed. Error: " + ex.getMessage());
        nb += 1;
      }
    }
    if (nb == nbRetries)
    {
      return false;
    }
    return true;
  }

  private boolean copy(String source)
  {
    return copy(source, true);
  }

  private boolean copy(String source, boolean info)
  {
    if (isError)
      return false;
    idle = false;
    if (info)
    {
      System.out.println(versionInfo);
      System.out.println("Transferring file " + source + " via COM port " + portName + " to target...");
    }
    File fSource = new File(source);
    if (!fSource.exists())
    {
      System.out.println("Can't find file '" + source + "'");
      idle = true;
      return false;
    }
    if (!checkPort())
    {
      System.out.println("Failed to access COM port " + portName);
      idle = true;
      return false;
    }

    String target = source.replace("\\", "/");  // for windows
    int index = target.lastIndexOf('/');
    target = target.substring(index + 1);

    long startTime = System.nanoTime();
    boolean rc = transfer(source, target);
    if (!rc || isError)
    {
      closePort();
      idle = true;
      return false;
    }
    if (info)
    {
      double time = (System.nanoTime() - startTime) / 1E9;
      time = (int)(time * 100) / 100.0;
      System.out.println("Successfully transferred file to target"
        + " in " + time + " seconds.");
    }
    closePort();
    idle = true;
    return true;
  }

  protected static void debug(String msg)
  {
    if (debug)
      System.out.println("-->Debug: " + msg);
  }

  private String readFile(String file) throws IOException
  {
    if (isError)
      return "";
    BufferedReader reader = new BufferedReader(new FileReader(file));
    String line = null;
    StringBuilder stringBuilder = new StringBuilder();

    try
    {
      while ((line = reader.readLine()) != null)
      {
        stringBuilder.append(line);
        stringBuilder.append("\n");
      }
      return stringBuilder.toString();
    }
    finally
    {
      reader.close();
    }
  }

  private void put(String source, String target) throws IOException
  {
    if (isError)
      return;
    ArrayList<String> commands = new ArrayList<String>();  // Each entry one command
    commands.add("fd = open('" + target + "', 'wb')");
    commands.add("f = fd.write");

    File fFile = new File(source);
    if (!fFile.exists())
      throw new IOException("File not found");
    String content = readFile(source);

    // Split into character blocks
    int startIndex = 0;
    int endIndex = blockSize;

    while (startIndex < content.length())
    {
      String block;
      if (endIndex <= content.length())
        block = content.substring(startIndex, endIndex);
      else
        block = content.substring(startIndex);

      block = block.replace("\\", "\\\\");
      block = block.replace("\n", "\\r\\n");
      block = block.replace("'", "\\'");
      // Escape ' because used in following string
      commands.add("f(b'" + block + "')");
      startIndex += blockSize;
      endIndex += blockSize;
    }
    commands.add("fd.close()");
    execute(commands);
  }

  private void remove(String filename) throws IOException
  {
    if (isError)
      return;
    ArrayList<String> commands = new ArrayList<String>();
    commands.add("import os");
    commands.add("os.remove('" + filename + "')");
    execute(commands);
  }

  private void execute(ArrayList<String> commands) throws IOException
  {
    if (isError)
      return;
    for (String command : commands)
    {
      debug("Execute cmd: " + command);
      byte[] b = command.getBytes(Charset.forName("UTF-8"));
      try
      {
        serialPort.writeBytes(b);
        reply = new StringBuilder();
        serialPort.writeByte((byte)0x04);
      }
      catch (Exception ex)
      {
      }
      boolean rc = waitFor(">");
      if (!rc)
        return;
    }
    delay(100);
  }

  private synchronized void sendCommand(String cmd)
  {
    if (isError)
      return;
    debug("sendCommand() cmd: " + cmd.replace("\n", "<lf>").replace("\r", "<cr>"));
    byte[] bytes = cmd.getBytes();
    try
    {
      for (byte b : bytes)
      {
        serialPort.writeByte(b);
        delay(10);
      }
    }
    catch (Exception ex)
    {
    }
    delay(1000);
  }
 
  private boolean finish(boolean wait)
  {
    if (isError)
      return false;
    delay(4000);  // Wait with Loboris firmware, because it boots on connection
    debug("Sending ^C stop  command ... ");
    reply = new StringBuilder();
    // Send sequence of ^C to stop
    byte[] bAry = new byte[]
    {
      (byte)0x03  // ^C
    };
    // Second sequence needed for ESP32 with Loboris firmware
    SendThread t = new SendThread(bAry);
    delay(100);
    t = new SendThread(bAry);
    delay(100);
    t.start();
    try
    {
      t.join(5000);
    }
    catch (InterruptedException ex)
    {
    }
    if (t.isAlive())
    {
      debug("Send thread not terminating");
      System.out.println("Failed to access COM port '" + portName + "'");
      closePort();
      isError = true;
      return false;
    }
    if (wait)
       return waitFor(">>>");
    return true;
  }

  private void softReset()
  {
    if (isError)
      return;
    debug("Sending ^D reset command ... ");
    try
    {
      serialPort.writeByte((byte)0x04);  // ctrl+D->reset
    }
    catch (Exception ex)
    {
    }
  }

  private static void closePort() // Can be called even if port is not open  
  {
    if (serialPort == null)
      return;
    try
    {
      serialPort.closePort();
    }
    catch (Exception ex)
    {
    }
    serialPort = null;
  }

  /**
   * Checks if the communcation port is closed.
   *
   * @return true if the serial port is closed
   */
  public boolean isPortClosed()
  {
    return (serialPort == null);
  }

  private boolean rawOn()
  {
    if (isError)
      return false;
    debug("Calling rawOn()");
    reply = new StringBuilder();
    try
    {
      serialPort.writeByte((byte)0x01);  // ctrl+A->raw mode
    }
    catch (Exception ex)
    {
      return false;
    }
    return waitFor("exit");  // reply: "raw REPL; CTRL-B to exit"

  }

  private boolean rawOff()
  {
    if (isError)
      return false;
    debug("Setting to normal mode ... ");
    reply = new StringBuilder();
    try
    {
      serialPort.writeByte((byte)0x02);  // ctrl+B->raw off mode
    }
    catch (Exception ex)
    {
      return false;
    }
    return waitFor(">>>");
  }

  private void delay(long time)
  {
    try
    {
      Thread.sleep(time);
    }
    catch (InterruptedException ex)
    {
    }
  }

  private boolean waitFor(String tag)
  {
    if ((console != null && console.isDisposed()) || isError)
      return false;
    debug("waitFor() with tag " + tag);
    int nbMax = 100;
    int n = 0;
    while (!reply.toString().contains(tag) && n < nbMax)
    {
      debug("reply while waiting: " + reply + " len: " + reply.length());
      delay(100);
      n += 1;
    }
    debug("reply after waiting: " + reply);
    if (n == nbMax)
    {
      System.out.println(noResponse);
      idle = true;
      isError = true;
      debug("isError flag set to true");
      return false;
    }
    debug("tag '" + tag + "' found in reply");
    delay(100);
    return true;
  }

  private GConsole openConsole()
  {
    if (consoleUlx == null)
    {
      consoleUlx = consoleUlxDefault;
    }
    if (consoleUly == null)
    {
      consoleUly = consoleUlyDefault;
    }
    if (consoleWidth == null)
    {
      consoleWidth = consoleWidthDefault;
    }
    if (consoleHeight == null)
    {
      consoleHeight = consoleHeightDefault;
    }
    if (consoleFontSize == null)
    {
      consoleFontSize = consoleFontSizeDefault;
    }

    if (consoleUlx == -1 || consoleUly == -1)
    {
      Dimension dimension = Toolkit.getDefaultToolkit().getScreenSize();
      consoleUlx = (int)((dimension.getWidth() - consoleWidth) / 2);
      consoleUly = (int)((dimension.getHeight() - consoleHeight) / 2);
    }

    GConsole c = new GConsole(
      new Position(consoleUlx, consoleUly),
      new Size(consoleWidth, consoleHeight),
      new Font("Courier", Font.PLAIN, consoleFontSize));
    c.setTitle(versionInfo);
    c.addExitListener(new MyExitListener());
    return c;
  }

  private boolean browseFileSystem(boolean show)
  {
    if (isError)
      return false;
    idle = false;
    System.out.println(versionInfo);
    System.out.println("Browsing target file system via COM port " + portName + "...");
    closePort();
    checkPort();
    try
    {
      openPort(portName);
      boolean rc = finish(true);
      if (!rc)
      {
        idle = true;
        return false;
      }
      capBuf = new StringBuilder();
      fileListCapture = true;
      sendCommand("import os;print(os.listdir())\r");
      while (fileListCapture)
        delay(1);
      if (show)
      {
        if (fileList.equals(""))
          System.out.println("File list: (empty)");
        else
          System.out.println("File list: " + fileList);
      }
      closePort();
    }
    catch (IOException ex)
    {
      closePort();
      idle = true;
      return false;
    }
    debug("browseFileSystem returned fileList = " + fileList);
    idle = true;
    return true;
  }

  private void terminate()
  {
    closePort();
    if (console != null)
    {
      console.dispose();  // For Tigerjython users: Do not call System.exit(0)
      console.end();  // Terminate stdio capture
      console = null;
    }
  }

  /**
   * callback for SerialPortEventListener
   */
  public synchronized void serialEvent(SerialPortEvent event)
  {
    if (event.isRXCHAR())
    {
      try
      {
        int nb = event.getEventValue();
        byte buffer[] = serialPort.readBytes(nb);
        char ch;
        for (int c : buffer)
        {
          ch = (char)c;
          /*
           if (ch == '\r')
           System.out.println("ch: <cr>");
           else if (ch == '\n')
           System.out.println("ch: <lf>");
           else
           System.out.println("ch: " + ch);
           */

          if (printCapture)
            System.out.print(ch);

          if (bootCapture)
          {
            capBuf.append(ch);
            if (capBuf.toString().contains(waitAtReset))  // Suppress outout until line with ----
            {
              printCapture = true;
              bootCapture = false;
            }
          }

          if (fileListCapture)
          {
            capBuf.append(ch);
            if (capBuf.toString().contains(">>>"))
            {
              fileListCapture = false;
              String s = capBuf.toString();
              try
              {
                fileList = s.substring(s.lastIndexOf('[') + 1, s.lastIndexOf(']'));
              }
              catch (Exception ex)
              {
                fileList = "";
              }
            }
          }

          if (fileCapture)
          {
            // Target appends <cr< to each line (returns <cr><lf> for each line)
            capBuf.append(ch);
            if (ch != '\r')
              nbChars += 1;
            if (nbChars == bufLen)
              fileCapture = false;
          }

          if (dataCaptureEnabled)
          {
            // Target appends <cr< to each line (returns <cr><lf> for each line)
            if (ch != '\r' && ch != '\n')
            {
              dataBuf.append(ch);
            }
            if (ch == '\n')
            {
              dataLineBuf.add(dataBuf.toString());
              dataBuf = new StringBuilder();
            }
          }

          if (fileSizeCapture)
          {
            capBuf.append(ch);
//            System.out.println(ch);
            if (capBuf.toString().contains(">>>"))
            {
              fileSizeCapture = false;
              debug("fileSizeCapture terminated with capBuf = " + capBuf.toString().replace("\n", "<lf>").replace("\r", "<cr>"));
            }
          }
          reply.append(ch);
        }
      }
      catch (Exception ex)
      {
      }
    }
  }

  private boolean extractFile(String fileName) // Text file assumed, line separator <lf> (Unix type)
  {
    if (isError)
      return false;
    extractionResult = 1;
    System.out.println("Extracting '" + fileName + "' ... ");
    boolean rc = browseFileSystem(false);
    if (!rc)
    {
      return false;
    }
    if (!fileList.contains("'" + fileName + "'"))
    {
      System.out.println("File not found in target file system.");
      extractionResult = -1;
      return false;
    }
    rc = getFileSize(fileName);
    if (!rc)
    {
      debug("Failed to get file size");
      System.out.println("Extration failed.");
      extractionResult = -1;
      return false;
    }
    debug("Got file size: " + fileSize);
    String cmd = "fd=open('" + fileName + "');s = fd.read();print(s);fd.close()\r";
    int cmdLen = cmd.length() + 1; // <lf> will be appended by target
    try
    {
      openPort(portName);
      capBuf = new StringBuilder();
      fileCapture = true;
      debug("cmd: " + cmd + " -> length; " + cmd.length());
      bufLen = fileSize + cmdLen;
      debug("bufLen: " + bufLen);
      nbChars = 0;
      sendCommand(cmd);
      while (fileCapture)
      {
        delay(1);
      }
      fileContent = capBuf.toString();
      debug("capBuf content: " + fileContent);
    }
    catch (IOException ex)
    {
      closePort();
      System.out.println("Extraction failed.");
      extractionResult = -1;
      return false;
    }
    closePort();
    String destPath = fileName;
    try
    {
      if (destDir != null)
      {
        destPath = destDir + File.separator + fileName;
      }
      debug("Writing file in current directory: " + destPath);
      PrintWriter writer = new PrintWriter(destPath, "UTF-8");
      writer.print(fileContent.substring(cmdLen));  // Cut cmd part at beginning
      writer.close();
    }
    catch (Exception e)
    {
      e.printStackTrace();
      System.out.println("Can't write file '" + destPath + "'");
      extractionResult = -1;
      return false;
    }
    System.out.println("Successfully extracted file to '" + destPath + "'");
    extractionResult = 0;
    return true;
  }

  private boolean getFileSize(String fileName)
  {
    try
    {
      openPort(portName);
      capBuf = new StringBuilder();
      fileSizeCapture = true;
      sendCommand("f=open('" + fileName + "');f.seek(0,2);f.close()\r");
      while (fileSizeCapture)
      {
        delay(1);
      }

      String s = capBuf.toString();
      int start = s.indexOf("\r\n") + 2;
      int end = s.lastIndexOf("\r\n");
      String strSize = s.substring(start, end);
      fileSize = Integer.parseInt(strSize);
    }
    catch (IOException ex)
    {
      closePort();
      return false;
    }
    closePort();
    return true;
  }

  /**
   * Returns the result code for extractFile.
   *
   * @return 1, if extraction is pending, 0: if no extraction is terminated
   * successfully; -1, if extraction failed
   */
  public static int getExtractionResult()
  {
    return extractionResult;
  }

  /**
   * Sets the destination directory for extractFile().
   *
   * @param dir the destination directory when extracting files (without
   * trailing file separator)
   */
  public static void setDestinationDir(String dir)
  {
    destDir = dir;
  }

  /**
   * Enables/disables line capture from terminal window. The lines are written
   * into a ArrayList buffer and extracted with getDataLines()
   *
   * @param enable if true, line capture is enabled; otherwise disabled.
   */
  public static void enableDataCapture(boolean enable)
  {
    dataCaptureEnabled = enable;
    if (enable)
    {
      dataBuf = new StringBuilder();
      dataLineBuf.clear();
    }
  }

  /**
   * Retrieves captured lines from the line buffer and clears the buffer. If
   * there is no data, an empty ArrayList is returned
   *
   * @return the content of the line buffer
   */
  public static ArrayList<String> getDataLines()
  {
    if (instance == null)
    {
      return new ArrayList<String>();  // not initialized
    }
    synchronized (instance) // Do not allow new acquisition until finished
    {
      ArrayList<String> temp = new ArrayList();
      if (!dataLineBuf.isEmpty())
      {
        for (String line : dataLineBuf)
        {
          temp.add(line);
        }
        dataLineBuf.clear();
      }
      return temp;
    }
  }

  /**
   * Checks, if the terminal window is visible.
   *
   * @return true, if the terminal window is visible; otherwise false
   */
  public static boolean isDisposed()
  {
    return console == null;
  }

  private boolean flash()
  {
    System.out.println(versionInfo);
    long startTime = System.nanoTime();

    if (firmwarePath == null)
      firmwarePath = System.getProperty("user.dir") + "\\Lib\\";
    firmwarePath = firmwarePath.replace('\\', '/');
    if (!firmwarePath.endsWith("/"))
      firmwarePath += "/";
    if (oxocard)
    {
      if (blockly)
        System.out.println("Flashing Blockly on Oxocard now.");
      else
        System.out.println("ESP32 for Oxocard selected.");
    }
    else
    {
      if (espLoboris)
        System.out.println("ESP32 Loboris MicroPython selected.");
      else
        System.out.println("ESP32 MicroPython selected.");

    }
    System.out.println("Checking flash files in " + firmwarePath + "...");

    String flashBinary = "";
    String flashData = "";
    String flashPartition = "";
    String bootLoader = "";
    if (blockly)
    {
      flashBinary = firmwarePath + "OxocardBlockly.bin";
      flashPartition = firmwarePath + "partitions_blockly.bin";
      bootLoader = firmwarePath + "bootloader_blockly.bin";
    }
    else // MicroPython
    {
      if (!downloadModulesOnly)
      {
        if (espLoboris)
        {
          flashBinary = firmwarePath + "MicroPython_loboris.bin";
          flashData = firmwarePath + "phy_init_data_loboris.bin";
          flashPartition = firmwarePath + "partitions_loboris.bin";
          bootLoader = firmwarePath + "bootloader_loboris.bin";
        }
        else
          flashBinary = firmwarePath + "MicroPython.bin";
      }
    }
    if (!downloadModulesOnly)
    {
      if (!new File(flashBinary).exists())
      {
        System.out.println("Can't find flash binary " + flashBinary);
        return false;
      }
      if (blockly)
      {
        if (!new File(flashPartition).exists())
        {
          System.out.println("Can't find flash partition " + flashPartition);
          return false;
        }
        if (!new File(bootLoader).exists())
        {
          System.out.println("Can't find flash bootloader " + bootLoader);
          return false;
        }
      }
    }
    String flashTool;
    if (Tools.isWinOS())
      flashTool = firmwarePath + "esptool.exe";
    else if (Tools.isMacOS())
      flashTool = firmwarePath + "esptool";
    else if (Tools.isLinux())
    {
      System.out.println("Integrated ESP flash not supported on Linux");
      return false;
    }
    else
    {
      System.out.println("Current operating system not supported");
      return false;
    }

    if (!new File(flashTool).exists())
    {
      System.out.println("Can't find flash tool " + flashTool);
      return false;
    }

    // Erasing flash
    if (!downloadModulesOnly)
    {
      System.out.println("Erasing existing firmware now using COM port '" + portName + "'");
      String[] erasecmd =
      {
        flashTool, "--port", portName, "erase_flash"
      };
      if (!executeCommand(erasecmd))
      {
        System.out.println("Connection to Esp failed. Check COM port.");
        return false;
      }
      // Flashing
      System.out.println("\nFlashing new firmware now.");
      String baud = "";
      if (Tools.isWinOS())
        baud = "921600";
      if (Tools.isMacOS())
        baud = "115200";

      String[] flashPython =
      {
        flashTool, "--port", portName, "--chip", "esp32", "--baud", baud,
        "--before", "default_reset", "--after", "hard_reset", "write_flash", "-z",
        "0x1000", flashBinary
      };
      
      String[] flashBlockly =
      {
        flashTool, "--port", portName, "--chip", "esp32", "--baud", baud,
        "--before", "default_reset", "--after", "hard_reset", "write_flash", "-z",
        "--flash_mode", "keep", "--flash_freq", "keep", "--flash_size",
        "detect", "0x1000", bootLoader, "0x8000", flashPartition, "0x10000", flashBinary
      };

      String[] flashLoboris =
      {
        flashTool, "--port", portName, "--chip", "esp32", "--baud", baud,
        "--before", "default_reset", "--after", "hard_reset", "write_flash", "-z",
        "--flash_mode", "dio", "--flash_freq", "40m", "--flash_size",
        "detect", "0x1000", bootLoader, "0xf000", flashData, "0x10000", flashBinary, "0x8000", flashPartition
      };

      String[] flashCmd;
      if (blockly)
        flashCmd = flashBlockly;
      else
      {
        if (espLoboris)
          flashCmd = flashLoboris;
        else
          flashCmd = flashPython;
      }

      if (!executeCommand(flashCmd))
      {
        System.out.println("Connection to Esp failed. Check COM port.");
        return false;
      }
      delay(4000);
    }
    if (!blockly)
    {
      if (oxocard)
        copyUserModules("oxocardmodules");
      else
      {
        if (espLoboris)
          copyUserModules("loborismodules");
        else
          copyUserModules("usermodules");
      }

    }
    double time = (System.nanoTime() - startTime) / 1E9;
    time = (int)(time * 100) / 100.0;
    System.out.println("Flashing terminated in " + time + " seconds.");
    return true;
  }

  /*
   private String putInQuotes(String s)
   {
   return "\"" + s + "\"";
   } 
   */
  private void copyUserModules(String subfolder)
  {
    String userModulesDir = firmwarePath + subfolder;
    File[] modules = new File(userModulesDir).listFiles();
    if (modules != null && modules.length > 0)
    {
      System.out.println("Copying user modules...");
      {
        try
        {
          openPort(portName);
        }
        catch (Exception ex)
        {
          System.out.println("Failed");
          return;
        }
        if (downloadModulesOnly)
          finish(true);

        boolean rc = rawOn();
        if (!rc)
        {
          System.out.println("Failed");
          closePort();
          return;
        }

        for (File module : modules)
        {
          String target = module.toString().replace("\\", "/");  // for windows
          int index = target.lastIndexOf('/');
          target = target.substring(index + 1);
          if (target.charAt(0) == '.') // Do not copy hidden files on Mac
            continue;

          System.out.print(target + "...");
          try
          {
            put(module.toString(), target);
          }
          catch (Exception ex)
          {
            System.out.println("failed.");
          }
          System.out.println("OK.");
        }
        rawOff();
      }
    }
  }

  private boolean executeCommand(String[] cmd)
  {
    StringBuffer sb = new StringBuffer();
    for (String s : cmd)
      sb.append(s + " ");
    debug("executeCommand() with cmd: " + sb.toString());
    try
    {
      String line;
      Process process = Runtime.getRuntime().exec(cmd);
      BufferedReader bri = new BufferedReader(new InputStreamReader(process.getInputStream()));
//      BufferedReader bre = new BufferedReader(new InputStreamReader(process.getErrorStream()));
      while ((line = bri.readLine()) != null)
      {
        if (line.contains("A fatal error"))
        {
          debug("executeCommand() terminated with fatal error message: " + line);
          return false;
        }
        else
          System.out.println(line);
      }
      bri.close();
//      while ((line = bre.readLine()) != null)
//        System.out.println("stderr:" + line);
//      bre.close();
      process.waitFor();
      debug("executeCommand() terminated");

    }
    catch (Exception e)
    {
      System.out.println("Fatal error while executing command: " + cmd);
      return false;
    }
    return true;
  }

  public static void main(String[] args)
  {
    new ESM(args);
  }
}
