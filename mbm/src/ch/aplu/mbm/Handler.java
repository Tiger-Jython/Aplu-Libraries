// Handler.java

/* Protocol for downloading files

 - Put MB into raw mode: Ctrl+A
 - Send line per line and terminate with Ctrl-D (without <cr><lf>)
 fd = open('Ex1.py', 'wb')
 f = fd.write
 f(b'text1\r\n')  \r\n may be omited, is part of file
 f(b'text2\r\n')
 ...
 fd.close()
 - Put MB into normal mode: Ctrl+B
 */
package ch.aplu.mbm;

import jssc.*;
import java.io.*;
import java.nio.charset.Charset;
import java.util.ArrayList;
import org.apache.commons.cli.*;
import ch.aplu.util.ExitListener;
import ch.aplu.util.GConsole;
import ch.aplu.util.Position;
import ch.aplu.util.Size;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Toolkit;
import java.awt.event.KeyEvent;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.UIManager;

public class Handler extends Thread implements SerialPortEventListener
{

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
          delay(1);
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
        closeConsole();
    }
  }

  // ------------------- Constant declarations ------------------------
  private static final String VERSION = "3.02 - Sep 25, 2019";
  private static final String versionInfo
    = "MicroBitManager (MBM) V" + VERSION + " [www.aplu.ch]";
  // ------------------------------------------------------

  private final String[] calliopeModules =
  {
    "cpglow.py","callibot.py","callibotmot.py","cbalarm.py",
    "cpmike.py", "cprover.py", "cputils.py",
    "linkup_mini.py", "sgp_mini.py", "sht_mini.py"
  };
  private final String[] microbitModules =
  {
    "mbglow.py", "mbrobot.py", "mbrobotmot.py",
    "linkup.py", "sgp.py", "sht.py", "mbalarm.py"
  };

  // Statics
  private static boolean debug = false;
  private static Handler instance = null;
  private static boolean idle = true;
  // End of statics

  private SerialPort serialPort = null;
  private String[] params;
  private boolean isError;
  private GConsole console = null;
  private int extractionResult = 0;
  private String destDir = null;
  private final int consoleUlxDefault = 10;
  private final int consoleUlyDefault = 10;
  private final int consoleWidthDefault = 600;
  private final int consoleHeightDefault = 400;
  private final int consoleFontSizeDefault = 12;
  private Options options;
  private CommandLine cmd;
  private String firmwarePath = null;
  private String modulePath = null;
  private String volumeName = "";
  private String portName = "";
  private String mainTarget = "main.py";
  private final int baudrate = 115200;
  private StringBuilder reply = new StringBuilder();
  private StringBuilder capBuf = new StringBuilder();
  private boolean fileCapture = false;
  private String fileContent = null;
  private int fileSize = -1;
  private boolean dataCaptureEnabled;
  private StringBuilder dataBuf;
  private ArrayList<String> dataLineBuf;
  private boolean startPrintCapture;
  private boolean printCapture;
  private boolean fileSizeCapture;
  private int bufLen;
  private int nbChars;
  private boolean fileListCapture = false;
  private String fileList = null;
  private int nbRetries = 4;
  private Integer consoleUlx = null;
  private Integer consoleUly = null;
  private Integer consoleWidth = null;
  private Integer consoleHeight = null;
  private Integer consoleFontSize = null;
  private boolean isTigerJython;
  private boolean isBinaryCopy;
  private boolean isRunning;

  // ----------------- Messages -----------------
  private final String noConnection
    = "   Not connected -> Plug target into USB port\n"
    + "   Connected to another client -> Close other connection\n"
    + "   Firmware corrupted -> Flash target\n"
    + "   Blocked by running application -> Flash target\n"
    + "   Initializing failed -> Remove and reinsert USB cable";
  private final String noCommPort = "Serial port no found. Possible reasons:\n"
    + noConnection
    + (Tools.isLinux() ? "\n   Linux: No permission for serial port->\n     Execute 'sudo adduser <username> dialout' and reboot!" : "")
    + (Tools.isWinOS() ? "\n   Windows 7/8: mbed driver not installed -> Download from http://www.aplu.ch/mbed" : "");
  private final String failOpen = "Failed to open comm port " + portName
    + "\nPort may be in use by another program.";
  private final String noDriveLetter = "Target drive no found. Possible reasons:\n"
    + noConnection;
  private final String noResponse = "Target not responding. Possible reasons:\n"
    + noConnection + (Tools.isWinOS() ? "\n   Serial driver for mbed not installed -> Download from http://www.aplu.ch/mbed" : "");
  private final String startingInfo1
    = "Entering REPL/Console and start 'main.py' (if exists).\n   -->Press ^C to stop (may fail)."
    + "\n   -->Press ^D to restart 'main.py'.";
  private final String startingInfo2
    = "Entering REPL/Console. \n   -->Press ^D to start 'main.py' (if exists)."
    + "\n   -->Press ^C to stop.";

  // ------------------- Constructor -----------------------
  public Handler(String[] args)
  {
    try
    {
      UIManager.setLookAndFeel(
        UIManager.getSystemLookAndFeelClassName());
    }
    catch (Exception ex)
    {
    }

    copyArgs(args);
    isRunning = true;
    start();
    instance = this;
  }

  public void run()
  {
    debug("Starting handler thread");
    while (isRunning)
    {
      if (idle)
      {
        delay(100);
        continue;
      }
      debug("Handling new request");
      doIt();
      debug("New request: Done");
      idle = true;
      isRunning = isTigerJython;
    }
    if (console == null)
      closeComPort();

    debug("Handler thread terminated");
    instance = null;
  }

  protected static void create(String[] args)
  {
    if (!idle)
    {
      System.out.println("Process in progress. Please wait...");
      return;
    }
    if (instance == null)
    {
      System.out.println(versionInfo);
      System.out.println("Creating new transfer handler.");
      instance = new Handler(args);
      idle = false;
    }
    else
    {
      instance.closeConsole();
      System.out.println(versionInfo);
      System.out.println("Using existing transfer handler.");
      instance.copyArgs(args);
      idle = false;
    }
  }

  private void copyArgs(String[] args)
  {
    if (debug)
    {
      System.out.println("-->Debug; params:");
      for (int i = 0; i < args.length; i++)
        System.out.println("-->Debug: #" + i + ": " + args[i]);
    }

    params = new String[args.length];
    for (int i = 0; i < args.length; i++)
      params[i] = args[i];
  }

  private void doIt()
  {
    dataCaptureEnabled = false;
    dataBuf = new StringBuilder();
    dataLineBuf = new ArrayList();
    startPrintCapture = false;
    printCapture = false;
    fileSizeCapture = false;
    isError = false;
    isTigerJython = false;
    isBinaryCopy = false;

    options = new Options();
    boolean rc = validateOptions();
    if (!rc)
      return;
    if (cmd.hasOption("h"))
    {
      new HelpFormatter().printHelp("java -jar "
        + getClass().getName() + ".jar",
        "",
        options,
        "",
        true);
    }

    // -------------- option "d" ------------
    if (cmd.hasOption("d"))
      debug = true;

    // -------------- option "o" ------------
    if (cmd.hasOption("o"))
      parseProperties(cmd.getOptionValue("o").trim());

    String portName;
    // -------------- option "p" ------------
    if (cmd.hasOption("p"))
      portName = cmd.getOptionValue("p").trim();
    else
      portName = Tools.findCommPort(2); // Two retries
    debug("Got port name: " + portName);
    if (portName.equals(""))
    {
      System.out.println(noCommPort);
      return;
    }

    // ------------- open serial port if necessary ---------
    if (serialPort != null && !this.portName.equals(portName))
    {
      debug("Closing serial port because name changed");
      closeComPort();
    }
    if (serialPort == null)  // only open if not yet opened
    {
      rc = openComPort(portName);
      if (!rc)
      {
        System.out.println(noCommPort);
        return;
      }
      this.portName = portName;
    }

    if (cmd.hasOption("v"))
      volumeName = cmd.getOptionValue("v").trim();
    else
    {
      volumeName = Tools.findVolumeName();
      if (volumeName == null)
      {
        System.out.println(noDriveLetter);
        return;
      }
      debug("Got volume name: " + volumeName);
    }

    // -------------- option "w" ------------
    if (cmd.hasOption("w"))
      firmwarePath = cmd.getOptionValue("w").trim();

    // -------------- option "m" ------------
    if (cmd.hasOption("m"))
      modulePath = cmd.getOptionValue("m").trim();

    // -------------- option "x" or "t"  ------------
    if (cmd.hasOption("x") || cmd.hasOption("t"))
    {
      rc = openTerminal();
      if (!rc)
      {
        System.out.println(noResponse);
        return;
      }

      // -------------- option "x" ------------
      if (cmd.hasOption("x"))
      {
        startPrintCapture = true;
        softReset();
        System.out.println(startingInfo1);
      }
      else // option "t"
      {
        isTigerJython = true;
        printCapture = true;
        System.out.println(startingInfo2);
        sendCommand("\r");
      }

      startUserInput();
    }
    // -------------- option "r" ------------
    else if (cmd.hasOption("r"))
    {
      isTigerJython = true;
      console = openConsole();
      System.out.println(startingInfo1);
      rc = copyrun(cmd.getOptionValue("r").trim());
      if (!rc)
        return;
      startUserInput();
    }
    // -------------- option "c" ------------
    else if (cmd.hasOption("c"))
    {
      isTigerJython = true;
      copy(cmd.getOptionValue("c").trim());
    }
    // -------------- option "y" ------------
    else if (cmd.hasOption("y"))
    {
      isBinaryCopy = true;
      copy(cmd.getOptionValue("y").trim());
    }
    // -------------- option "e" ------------
    else if (cmd.hasOption("e"))
      extractFile(cmd.getOptionValue("e").trim());
    // -------------- option "b" ------------
    else if (cmd.hasOption("b"))
    {
      rc = browseFileSystem(true);
      if (!rc)
        System.out.println(noResponse);
    }
    // -------------- option "f" ------------
    else if (cmd.hasOption("f"))
    {
      long startTime = System.nanoTime();
      rc = flash();
      if (!rc)
      {
        System.out.println("Flashing firmware failed.");
        return;
      }
      System.out.println("Firmware successfully installed.");
      openComPort(portName);
      copyModules();
      closeComPort();
      double time = (System.nanoTime() - startTime) / 1E9;
      time = (int)(time * 100) / 100.0;
      System.out.println("Successfully flashed"
        + " in " + time + " seconds.");
    }
  }

  private boolean validateOptions()
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

    options.addOption(Option.builder("v").
      longOpt("volume").
      hasArgs().
      desc("Volume name. If omitted, volume name search performed.").
      build());

    options.addOption(Option.builder("d").
      longOpt("debug").
      desc("Debug mode: Verbose debug messages to stdio.").
      build());

    options.addOption(Option.builder("w").
      longOpt("firmware").
      hasArgs().
      desc("Path to firmware hex file. If omitted, hex in JAR file assumed.").
      build());

    options.addOption(Option.builder("m").
      longOpt("module").
      hasArgs().
      desc("Path to module file. If file is not found, extracted from JAR file.").
      build());

    // ----------- actionGroup -------------
    actionGroup.addOption(
      Option.builder("f").
        longOpt("flash").
        desc("Perform firmware flash.").
        build());

    actionGroup.addOption(
      Option.builder("h").
        longOpt("help").
        desc("Show help message.").
        build());

    actionGroup.addOption(
      Option.builder("x").
        longOpt("execute").
        desc("Open terminal/console (REPL) and execute main.py.").
        build());

    actionGroup.addOption(
      Option.builder("t").
        longOpt("terminal").
        desc("Open terminal/console (REPL)").
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
        desc("Download file, rename to main.py, open terminal/console (REPL) and execute.").
        build());

    actionGroup.addOption(
      Option.builder("c").
        longOpt("copy").
        hasArgs().
        desc("Download file from TigerJython. <arg> = source path").
        build());

    actionGroup.addOption(
      Option.builder("y").
        longOpt("copy").
        hasArgs().
        desc("Download text or binary file (keep name). <arg> = source path").
        build());

    actionGroup.addOption(
      Option.builder("e").
        longOpt("extract").
        hasArgs().
        desc("Extract file from target <arg> = file name").
        build());

    actionGroup.addOption(
      Option.builder("f").
        longOpt("flash").
        desc("Flash firmware. (All files are erased.)").
        build());

    actionGroup.setRequired(true);
    options.addOptionGroup(actionGroup);
    CommandLineParser parser = new DefaultParser();

    try
    {
      cmd = parser.parse(options, params);  // Set global cmd
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

  private boolean flash()
  {
    closeComPort();
    if (volumeName == null || isError)
      return false;
    InputStream istreamJar = null;
    if (firmwarePath == null)
    {
      System.out.println("Device type detected: " + Tools.deviceType);
      System.out.println("Flashing firmware resource to " + volumeName + " ... ");
      URL url = null;
      if ((Tools.deviceType.equals("MICROBIT") || Tools.deviceType.equals("CIRCUITPY")) && (!Tools.isMicrobitV2))
        url = Thread.currentThread().getContextClassLoader().
          getResource("ch/aplu/mbm/micropython.hex");
      else if (Tools.deviceType.equals("MICROBIT") && (Tools.isMicrobitV2))
        url = Thread.currentThread().getContextClassLoader().
          getResource("ch/aplu/mbm/micropythonV2.hex");      
      else if (Tools.deviceType.equals("CALLIOPE"))
        url = Thread.currentThread().getContextClassLoader().
          getResource("ch/aplu/mbm/calliope-micropython.hex");         
      else
      {
        System.out.println("Illegal device type. Use -w option to set path to firmware file.");
        return false;
      }

      try
      {
        istreamJar = url.openStream();  // url may be null
      }
      catch (Exception ex)
      {
      }
      if (istreamJar == null)
      {
        System.out.println("Can't read firmware hex file from JAR.");
        return false;
      }
    }
    else
      System.out.println("Flashing firmware " + firmwarePath + " to " + volumeName + " ... ");

    BufferedReader in = null;
    StringBuilder sb = new StringBuilder();
    int ch;
    try
    {
      if (firmwarePath == null)
        in = new BufferedReader(new InputStreamReader(istreamJar));
      else
        in = new BufferedReader(new FileReader(firmwarePath));
      while ((ch = in.read()) != -1)
        sb.append((char)ch);
      in.close();
    }
    catch (IOException ex)
    {
      System.out.println("Failed.\nFirmware hex file not found in " + firmwarePath);
      return false;
    }
    finally
    {
      try
      {
        in.close();
      }
      catch (Exception ex)
      {
      };
    }

    String text = sb.toString();
    FileOutputStream out = null;   // Did not work as text file
    String fileName;
    if (Tools.isMacOS())
      fileName = "/Volumes/" + volumeName + "/microbit.hex";
    else
      fileName = volumeName + "microbit.hex";

    debug("Destination: " + fileName);
    byte[] buf = text.getBytes();
    try
    {
      out = new FileOutputStream(fileName);
      out.write(buf);
    }
    catch (Exception ex)
    {
      System.out.println("Failed.\nCan't copy firmware to  " + fileName);
      return false;
    }
    finally
    {
      try
      {
        out.close();
      }
      catch (Exception ex)
      {
      };
    }
    // Unmounting device to disable warning dialog at end of flash action.
    if (Tools.isMacOS())
    {
      debug("Unmounting " + volumeName);
      String cmd = "diskutil umount " + volumeName;
      try
      {
        Runtime.getRuntime().exec(cmd);
      }
      catch (IOException ex)
      {
      }
      while (isMacVolumeMounted())
      {
        debug("waiting to be unmounted");
        delay(1000);
      }
      delay(1000);
      while (!isMacVolumeMounted())
      {
        debug("waiting to be mounted");
        delay(1000);
      }
    }
    if (Tools.isLinux())
    {
      while (isLinuxVolumeMounted())
      {
        debug("waiting to be unmounted");
        delay(1000);
      }
      delay(1000);
      while (!isLinuxVolumeMounted())
      {
        debug("waiting to be mounted");
        delay(1000);
      }
    }
    return true;
  }

  private boolean isMacVolumeMounted()
  {
    File[] files = new File("/Volumes/" + volumeName).listFiles();
    if (files == null)
      return false;
    return true;
  }

  private boolean isLinuxVolumeMounted()
  {
    File[] files = new File(volumeName).listFiles();
    if (files == null)
      return false;
    return true;
  }

  private void copyModules()
  {
    if (isError)
      return;
    if (modulePath != null)  // External module given
    {
      File fModulePath = new File(modulePath);
      boolean rc;
      if (fModulePath.exists()) // Since file in file system exists->take it
      {
        System.out.print("Transferring " + modulePath + " ... ");
        rc = copy(modulePath);
        if (rc)
          System.out.println("OK,");
        if (rc)
          System.out.println("Failed,");
      }
    }

    System.out.println("Transferring basic add-on modules. Please wait...");
    if (Tools.deviceType.equals("CALLIOPE"))
    {
      for (String module : calliopeModules)
        transferModule(module);
      System.out.println("All done.");
    }
    else
    {
      for (String module : microbitModules)
        transferModule(module);
      System.out.println("All done.");
    }
  }

  private void transferModule(String moduleName)
  {
    System.out.print("Copying " + moduleName + " ... ");
    if (copyFromJar("ch/aplu/mbm/" + moduleName, moduleName))
      System.out.println("OK.");
    else
      System.out.println("Failed.");
  }

  private void startUserInput()
  {
    if (console == null)
      return;
    InputReader ir = new InputReader();
    ir.start();
  }

  private boolean finishProgram()
  {
    if (isError)
      return false;
    try
    {
      boolean rc = finish();
      if (!rc)
        return false;
      rawOn();
      remove("main.py");
      System.out.print("Removing main.py ... ");
      rawOff();
      delay(2000);
      softReset();
      System.out.println("Done.");
    }
    catch (IOException ex)
    {
      System.out.println("Failed to finish program.\nError: " + ex.getMessage());
      return false;
    }
    return true;
  }

  private boolean openTerminal()
  {
    if (isError)
      return false;
    System.out.println("Opening terminal at COM port " + portName + "...");
    console = openConsole();
    idle = true;
    return finish();
  }

  private boolean openComPort(String portName)
  {
    try
    {
      debug("Creating new SerialPort");
      serialPort = new SerialPort(portName);
      serialPort.openPort();
      serialPort.setParams(115200, 8, 1, 0);
      int mask = SerialPort.MASK_RXCHAR;
      serialPort.setEventsMask(mask);
      serialPort.addEventListener(this);
      return true;
    }
    catch (Exception e)
    {
      return false;
    }
  }

  private void closeComPort()
  {
    debug("Closing SerialPort");
    try
    {
      if (serialPort != null)
      {
        serialPort.closePort();
        serialPort = null;
      }
    }
    catch (Exception e)
    {
    }
  }

  private boolean copyrun(String source)
  {
    if (isError)
      return false;
    File fSourcePath = new File(source);
    if (!fSourcePath.exists())
    {
      System.out.println("Can't find file '" + source + "'");
      return false;
    }
    if (volumeName == null)
    {
      System.out.println("Can't find drive letter automatically");
      return false;
    }

    boolean rc = transfer(source, mainTarget, false);
    if (!rc)
      return false;
    runScript();
    return true;
  }

  private void softReset()
  {
    if (isError)
      return;
    if (Tools.deviceType.equals("CIRCUITPY"))
      hardReset();
    else
    {
      if (Tools.deviceType.equals("CALLIOPE"))
        sendCommand("from calliope_mini import *\rreset()\r");
      else
        sendCommand("from microbit import *\rreset()\r");
    }
  }

  private void runScript()
  {
    startPrintCapture = true;
    softReset();
  }

  private boolean transfer(String source, String targetFile, boolean isBinary)
  {
    if (isError)
      return false;

    int nb = 0;
    while (nb < nbRetries)
    {
      try
      {
        if (nb > 0)
          System.out.println("Waiting for target device ... ");
        boolean rc = finish();
        if (!rc)
          return false;
        rawOn();
        if (isBinary)
          putBinary(source, targetFile);
        else
          putText(source, targetFile);
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
      return false;
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
    File fSource = new File(source);
    if (!fSource.exists())
    {
      System.out.println("Can't find file '" + source + "'");
      return false;
    }

    boolean rc = false;
    long startTime = 0;

    source = source.replace("\\", "/");  // for windows
    int index = source.lastIndexOf('/');
    String folder = source.substring(0, index);
    String target = source.substring(index + 1);

    if (isTigerJython)
    {
      Object[] choices =
      {
        "Editor", "File"
      };
      Object defaultChoice = choices[0];
      int dialogResult = JOptionPane.showOptionDialog(null,
        "Copy from TJ editor or PC file system?",
        "Module Source",
        JOptionPane.YES_NO_OPTION,
        JOptionPane.QUESTION_MESSAGE,
        null,
        choices,
        defaultChoice);
      if (dialogResult == JOptionPane.YES_OPTION)  // taken from TJ
      {
        if (info)
        {
          System.out.println("Transferring file " + source + " via COM port " + portName + " to target...");
        }
        startTime = System.nanoTime();
        rc = transfer(source, target, false);
      }
      else  // taken from from file system
      {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setCurrentDirectory(new File(folder));
        int result = fileChooser.showOpenDialog(null);
        if (result == JFileChooser.APPROVE_OPTION)
        {
          source = fileChooser.getSelectedFile().toString();
          if (source.trim().equals(""))
          {
            return true;
          }
          source = source.replace("\\", "/");  // for windows
          index = source.lastIndexOf('/');
          target = source.substring(index + 1);
          startTime = System.nanoTime();
          if (info)
          {
            System.out.println("Transferring file " + source + " via COM port " + portName + " to target... (binary mode)");
          }
          rc = transfer(source, target, true);
        }
        else
          return true;
      }
    }
    else
    {
      startTime = System.nanoTime();
      rc = transfer(source, target, isBinaryCopy);
    }
    if (!rc || isError)
      return false;
    double time = (System.nanoTime() - startTime) / 1E9;
    time = (int)(time * 100) / 100.0;
    if (info)
      System.out.println("Successfully transferred file"
        + " in " + time + " seconds.");
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

  private void putText(String source, String target) throws IOException
  {
    if (isError)
      return;
    ArrayList<String> commands = new ArrayList<String>();  // Each entry one command
    commands.add("import os");
    commands.add("if '" + target + "' in os.listdir(): os.remove('" + target + "')");
    commands.add("fd = open('" + target + "', 'wb')");
    commands.add("f = fd.write");

    File fFile = new File(source);
    if (!fFile.exists())
    {
      throw new IOException("File not found");
    }
    StringBuffer sb = new StringBuffer();
    BufferedReader br = new BufferedReader(new FileReader(source));
    String line;
    sb.append("import gc\n");
    sb.append("gc.collect()\n");
    while ((line = br.readLine()) != null)
      sb.append(line + "\n");
    String content = sb.toString();

    // Split into 64 character blocks
    int startIndex = 0;
    int endIndex = 64;

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
      startIndex += 64;
      endIndex += 64;
    }
    commands.add("fd.close()");
    execute(commands);
  }

  private void putBinary(String source, String target) throws IOException
  {
    if (isError)
      return;
    ArrayList<String> commands = new ArrayList<String>();  // Each entry one command
    commands.add("fd = open('" + target + "', 'wb')");
    commands.add("f = fd.write");

    File fFile = new File(source);
    if (!fFile.exists())
    {
      throw new IOException("File not found");
    }

    byte[] bin = Files.readAllBytes(new File(source).toPath());
    StringBuilder sb = new StringBuilder();
    for (byte b : bin)
      sb.append("\\x" + String.format("%02x", b));

    String content = sb.toString();

    // Split into 64 character blocks
    int startIndex = 0;
    int endIndex = 64;

    while (startIndex < content.length())
    {
      String block;
      if (endIndex <= content.length())
        block = content.substring(startIndex, endIndex);
      else
        block = content.substring(startIndex);

      commands.add("f(b'" + block + "')");
      startIndex += 64;
      endIndex += 64;
    }
    commands.add("fd.close()");
    execute(commands);
  }

  private boolean copyFromJar(String source, String dest)
  {
    if (isError)
      return false;
    String tempDir = System.getProperty("java.io.tmpdir");
    String out;
    if (Tools.isLinux())
      out = tempDir + "/" + dest;
    else
      out = tempDir + dest;
    debug("copyFromJar() with source: " + source + " dest: " + out);
    try
    {
      URL url = Thread.currentThread().getContextClassLoader().getResource(source);
      InputStream is = url.openStream();
      BufferedOutputStream writer = new BufferedOutputStream(new FileOutputStream(out));
      byte[] buffer = new byte[1024];
      boolean done = false;
      while (!done)
      {
        int len = is.read(buffer);
        if (len == -1)
        {
          done = true;
          continue;
        }
        writer.write(buffer, 0, len);
      }
      writer.close();
    }
    catch (Exception ex)
    {
      return false;
    }
    return copy(out, false);
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
        delay(10);
        serialPort.writeByte((byte)0x04);
      }
      catch (Exception ex)
      {
      }
      boolean rc = waitFor(">", true);
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

  private boolean finish()
  {
    if (isError)
      return false;
    debug("Sending ^C stop  command ... ");
    try
    {
      serialPort.writeByte((byte)0x03);  // ctrl+C->stop
      serialPort.writeByte((byte)0x03);  // ctrl+C->stop
    }
    catch (Exception ex)
    {
    }
    boolean rc = waitFor(">>>", false);
    if (!rc)
    {
      System.out.println("Reinititalizing serial port");
      closeComPort();
      delay(1000);
      openComPort(portName);
      delay(1000);
      isError = false;
      try
      {
        debug("Sending ^C stop  command ... ");
        serialPort.writeByte((byte)0x03);  // ctrl+C->stop
        serialPort.writeByte((byte)0x03);  // ctrl+C->stop
      }
      catch (Exception ex)
      {
      }
      return waitFor(">>>", true);
    }
    return true;
  }

  private void hardReset()
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
//    waitFor("soft reboot");
  }

  private void rawOn() throws IOException
  {
    if (isError)
      return;
    debug("Setting to raw mode ... ");
    try
    {
      serialPort.writeByte((byte)0x01);  // ctrl+A->raw mode
    }
    catch (Exception ex)
    {
    }
    waitFor("raw REPL", true);

  }

  private void rawOff() throws IOException
  {
    if (isError)
      return;
    debug("Setting to normal mode ... ");
    try
    {
      serialPort.writeByte((byte)0x02);  // ctrl+B->raw off mode
    }
    catch (Exception ex)
    {
    }
    waitFor(">>>", true);
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

  private boolean waitFor(String tag, boolean reportError)
  {
    if ((console != null && console.isDisposed()) || isError)
      return false;
    debug("waitFor() with tag " + tag);
    int nbMax = 50;
    reply = new StringBuilder();
    int n = 0;
    while (!reply.toString().contains(tag) && n < nbMax)
    {
      delay(100);
      n += 1;
    }
    if (n == nbMax)
    {
      if (reportError)
      {
        System.out.println(noResponse);
        closeComPort();
        isError = true;
        debug("Timeout in waitFor(). Error reported.");
        return false;
      }
      else
      {
        debug("Timeout in waitFor(). Error not reported.");
        return false;
      }

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
    System.out.println("Browsing target file system via COM port " + portName + "...");
    boolean rc = finish();
    if (!rc)
      return false;
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
    debug("browseFileSystem returned fileList = " + fileList);
    return true;
  }

  private void closeConsole()
  {
    debug("calling closeConsole()");
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
//          display(ch);

          if (printCapture)
            System.out.print(ch);

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
            // Target appends <cr> to each line (returns <cr><lf> for each line)
//            display(ch);
            capBuf.append(ch);
            nbChars += 1;
//            System.out.println(nbChars);
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
            if (capBuf.toString().contains(">>>"))
            {
              fileSizeCapture = false;
              debug("fileSizeCapture terminated with capBuf = " + capBuf.toString().replace("\n", "<lf>").replace("\r", "<cr>"));
            }
          }

          if (startPrintCapture)
          {
            capBuf.append(ch);
            if (capBuf.toString().contains("reset()"))
            {
              debug("Start stdio capturing");
              startPrintCapture = false;
              capBuf = new StringBuilder();
              printCapture = true;
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

  private void display(char ch)
  {
    if (ch == '\r')
      System.out.println("ch: <cr>");
    else if (ch == '\n')
      System.out.println("ch: <lf>");
    else
      System.out.println("ch: " + ch);
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
    getFileSize(fileName);
    debug("Got file size: " + fileSize);
    String cmd = "fd=open('" + fileName + "');s = fd.read();print(s);fd.close()\r";
    int cmdLen = cmd.length() + 1; // <lf> will be appended by target
    capBuf = new StringBuilder();
    fileCapture = true;
    debug("cmd: " + cmd + " -> length; " + cmd.length());
    bufLen = fileSize + cmdLen;
    debug("bufLen: " + bufLen);
    nbChars = 0;
    sendCommand(cmd);
    while (fileCapture)
      delay(1);
    fileContent = capBuf.toString();
    debug("capBuf content: " + fileContent);
    String destPath = fileName;
    try
    {
      if (destDir != null)
        destPath = destDir + File.separator + fileName;
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

  private void getFileSize(String fileName)
  {
    capBuf = new StringBuilder();
    fileSizeCapture = true;
    sendCommand("import os;os.size('" + fileName + "')\r");
    while (fileSizeCapture)
      delay(1);

    String s = capBuf.toString();
    int start = s.indexOf("\r\n") + 2;
    int end = s.lastIndexOf("\r\n");
    String strSize = s.substring(start, end);
    fileSize = Integer.parseInt(strSize);
  }

  /**
   * Returns the result code for extractFile.
   *
   * @return 1, if extraction is pending, 0: if no extraction is terminated
   * successfully; -1, if extraction failed
   */
  public int getExtractionResult()
  {
    return extractionResult;
  }

  /**
   * Sets the destination directory for extractFile().
   *
   * @param dir the destination directory when extracting files (without
   * trailing file separator)
   */
  public void setDestinationDir(String dir)
  {
    destDir = dir;
  }

  /**
   * Enables/disables line capture from terminal window. The lines are written
   * into a ArrayList buffer and extracted with getDataLines()
   *
   * @param enable if true, line capture is enabled; otherwise disabled.
   */
  public void enableDataCapture(boolean enable)
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
  public ArrayList<String> getDataLines()
  {
    if (instance == null)
      return new ArrayList<String>();  // not initialized
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
  public boolean isDisposed()
  {
    return console == null;
  }

}
