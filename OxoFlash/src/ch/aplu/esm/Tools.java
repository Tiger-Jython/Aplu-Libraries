// Tools.java

package ch.aplu.esm;

import ch.aplu.util.Monitor;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import jssc.SerialPortList;
import java.awt.EventQueue;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.Collections;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;

// --------------- class SelectionDialog -----------------------
class SelectionDialog extends JDialog
{
  private int mode = 0;

  private JRadioButton[] radioBtn =
  {
    new JRadioButton("Python"), new JRadioButton("Blockly")
  };
  private JRadioButton[] radioBtn1 =
  {
    new JRadioButton("ESP32 Original"), new JRadioButton("ESP32 Loboris")
  };
  private int btnSelected = -1;

  private class RadioBtnActionAdapter
    implements ActionListener
  {
    private int btnNb;

    private RadioBtnActionAdapter(int btnNb)
    {
      this.btnNb = btnNb;
    }

    public void actionPerformed(ActionEvent evt)
    {
      btnSelected = -1;
      for (int nb = 0; nb < radioBtn.length; nb++)
        if (radioBtn[nb].isSelected())
          btnSelected = nb;
      Monitor.wakeUp();
    }
  }

  protected SelectionDialog(int mode)
  {
    this.mode = mode;
    try
    {
      UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
    }
    catch (Exception e)
    {
    }
    init();
  }

  public void init()
  {
    if (EventQueue.isDispatchThread())
      initDialog();
    else
    {
      try
      {
        EventQueue.invokeAndWait(new Runnable()
        {

          public void run()
          {
            initDialog();
          }
        });
      }
      catch (Exception ex)
      {
      }
    }
  }

  private void initDialog()
  {
    JPanel contentPane = (JPanel)getContentPane();
    JPanel p = new JPanel();
    p.setBorder(new EmptyBorder(10, 10, 10, 10));
    TitledBorder border = null;
    if (mode == 0)
      border = BorderFactory.createTitledBorder(BorderFactory.createRaisedBevelBorder(), "Programming Language");
    else if (mode == 1)
      border = BorderFactory.createTitledBorder(BorderFactory.createRaisedBevelBorder(), "Firmware Option");
    border.setTitleJustification(TitledBorder.LEADING);
    contentPane.setBorder(border);
    contentPane.add(p);

    JLabel label = new JLabel("Make your choice!");
    p.add(label);

    if (mode == 1)
      radioBtn = radioBtn1;

    ButtonGroup buttonGroup = new ButtonGroup();
    for (int nb = 0; nb < radioBtn.length; nb++)
    {
      buttonGroup.add(radioBtn[nb]);
      p.add(radioBtn[nb]);
      radioBtn[nb].addActionListener(
        new RadioBtnActionAdapter(nb));
    }
    pack();
    setLocationRelativeTo(null);
    setResizable(false);
    setVisible(true);

    addWindowListener(new WindowAdapter()
    {
      public void windowClosing(WindowEvent e)
      {
        btnSelected = -1;
        Monitor.wakeUp();
      }
    });
  }

  int getChoice()
  {
    return btnSelected;
  }
}

// --------------- class Tools -----------------------
public class Tools
{
  private static String driveLetters = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
  private static final String osname = System.getProperty("os.name", "");
  private static final String username = System.getProperty("user.name", "");

  public static boolean addLibraryPath(String pathToAdd)
  {
    try
    {
      Field usrPathsField = ClassLoader.class.getDeclaredField("usr_paths");
      usrPathsField.setAccessible(true);

      String[] paths = (String[])usrPathsField.get(null);

      for (String path : paths)
        if (path.equals(pathToAdd))
          return true;  // Already present

      String[] newPaths = Arrays.copyOf(paths, paths.length + 1);
      newPaths[newPaths.length - 1] = pathToAdd;
      usrPathsField.set(null, newPaths);
    }
    catch (Exception ex)
    {
      return false;
    }
    return true;
  }

  public static String findCommPort(int nbRetries)
  {
    if (nbRetries < 0)
      nbRetries = 0;
    String port = null;
    int nb = 0;
    while (((port = findCommPort()).equals("")) && nb < nbRetries)
    {
      nb += 1;
      delay(1000);
    }
    return port;
  }

  public static String findCommPort()
  {
    if (isWinOS())
      return findCommPortWin();
    else if (isMacOS())
      return findCommPortMac();
    else if (isLinux())
      return findCommPortLinux();
    return "";
  }

  public static String[] enumCommPorts()
  {
    if (isWinOS())
      return enumCommPortsWin();
    else if (isMacOS())
      return enumCommPortsMac();
    else if (isLinux())
      return enumCommPortsLinux();
    return new String[0];
  }

  public static String[] enumCommPortsWin()
  {
    // Take first device independant of name 
    ESM.debug("Calling enumCommPortWin(). Request registry:");
    ArrayList<String> reply = getInfoFromRegistry();
    ArrayList<String> comPorts = new ArrayList();
    String portName = "";
    for (String line : reply)
    {
      ESM.debug("line: " + line);
      int index = line.lastIndexOf("REG_SZ");
      if (index == -1)
        continue;
      portName = line.substring(index + 6).trim();
      comPorts.add(portName);
    }
    ESM.debug("enumCommPortPortWin() found: " + Arrays.toString(comPorts.toArray()));
    return comPorts.toArray(new String[0]);
  }

  public static String[] enumCommPortsMac()
  {
    ESM.debug("Calling enumCommPortMac()");
    ArrayList<String> devices = getMacDevices();
    ArrayList<String> comPorts = new ArrayList();
    for (String device : devices)
    {
      if (device.contains("tty.") && device.toLowerCase().contains("usb"))
      {
        ESM.debug("enumCommPortsMac() found device: " + device);
        comPorts.add("/dev/" + device);
      }
    }
    ESM.debug("enumCommPortsMac() returned: " + Arrays.toString(comPorts.toArray()));
    return comPorts.toArray(new String[0]);
  }

  public static String[] enumCommPortsLinux()
  {
    ESM.debug("Calling enumCommPortsLinux()");
    String[] portNames = SerialPortList.getPortNames();
    ArrayList<String> comPorts = new ArrayList();
    for (String portName : portNames)
    {
      ESM.debug(portName);
      if (portName.contains("ttyUSB") || portName.contains("ttyACM"))
      {
        ESM.debug("enumCommPortLinux() found: " + portName);
        comPorts.add(portName);
      }
    }
    ESM.debug("enumCommPortsLinux() returned: " + Arrays.toString(comPorts.toArray()));
    return comPorts.toArray(new String[0]);
  }

  public static String findCommPortWin()
  {
    ESM.debug("Calling findCommPortWin(). Request registry:");
    String[] comPorts = enumCommPortsWin();
    if (comPorts.length == 0)
      return "";
    String comPort = comPorts[comPorts.length - 1];
    ESM.debug("findCommPortWin() found: " + comPort);
    return comPort;
  }

  public static String findCommPortMac()
  {
    ESM.debug("Calling findCommPortMac()");
    String[] comPorts = enumCommPortsMac();
    String comPort = "";
    if (comPorts.length == 0)
      comPort = "";
    else if (comPorts.length == 1)
      comPort = comPorts[0];
    else
    {  
      for (String port : comPorts)
      { 
        comPort = port;
        if (port.contains("wchusbserial"))
          break;
      }
    }
    ESM.debug("findCommPortMac() found: " + comPort);
    return comPort;
  }

  public static String findCommPortLinux()
  {
    ESM.debug("Calling findCommPortLinux()");
    String[] comPorts = enumCommPortsLinux();
    if (comPorts.length == 0)
      return "";
    String comPort = comPorts[comPorts.length - 1];
    ESM.debug("findCommPortLinux() found: " + comPort);
    return comPort;
  }

  private static ArrayList<String> getInfoFromRegistry()
  {
    final ArrayList<String> reply = new ArrayList();
    try
    {
      String cmd = "reg query HKLM\\HARDWARE\\DEVICEMAP\\SERIALCOMM";
      final Process p = Runtime.getRuntime().exec(cmd);

      new Thread(new Runnable()
      {
        public void run()
        {
          BufferedReader input
            = new BufferedReader(new InputStreamReader(p.getInputStream()));
          String line = null;
          try
          {
            while ((line = input.readLine()) != null)
              reply.add(line);
          }
          catch (IOException e)
          {
            e.printStackTrace();
          }
        }
      }).start();

      p.waitFor();
    }
    catch (Exception ex)
    {
      ex.printStackTrace();
    }
    /*
     System.out.println("Reply:");
     for (String s: reply)
     System.out.println(s);
     */
    return reply;
  }

  private static ArrayList<String> getMacDevices()
  {
    final ArrayList<String> reply = new ArrayList();
    try
    {
      String cmd = "ls /dev";
      final Process p = Runtime.getRuntime().exec(cmd);

      BufferedReader input
        = new BufferedReader(new InputStreamReader(p.getInputStream()));
      String line = null;
      try
      {
        while ((line = input.readLine()) != null)
          reply.add(line);
      }
      catch (IOException e)
      {
        e.printStackTrace();
      }

      p.waitFor();
    }
    catch (Exception ex)
    {
      ex.printStackTrace();
    }
    return reply;
  }

  public static String findVolumeName()
  {
    if (isWinOS())
      return findVolumeNameWin();
    else if (isMacOS())
      return findVolumeNameMac();
    else if (isLinux())
      return findVolumeNameLinux();
    return null;

  }

  public static String findVolumeNameWin()
  {
    ESM.debug("Calling findVolumeNameWin()");
    for (int i = 0; i < driveLetters.length(); i++)
    {
      char ch = driveLetters.charAt(i);
      ArrayList<String> reply = getDriveInfoWin(ch);
      if (reply.size() > 0 && (reply.get(0).contains("MICROBIT") || reply.get(0).contains("MINI")))
        return (ch + ":/");
    }
    return null;
  }

  private static ArrayList<String> getDriveInfoWin(char letter)
  {
    final ArrayList<String> reply = new ArrayList();
    try
    {
      String cmd = "cmd /c vol " + letter + ":";
      final Process p = Runtime.getRuntime().exec(cmd);

      new Thread(new Runnable()
      {
        public void run()
        {
          BufferedReader input
            = new BufferedReader(new InputStreamReader(p.getInputStream()));
          String line = null;
          try
          {
            while ((line = input.readLine()) != null)
              reply.add(line);
          }
          catch (IOException e)
          {
            e.printStackTrace();
          }
        }
      }).start();

      p.waitFor();
    }
    catch (Exception ex)
    {
      ex.printStackTrace();
    }
    return reply;
  }

  private static String findVolumeNameMac()
  {
    ESM.debug("Calling findVolumeNameMac()");
    File[] files = new File("/Volumes/").listFiles();
    for (File f : files)
    {
      if (f.toString().equals("/Volumes/MINI"))
        return "MINI";
      if (f.toString().equals("/Volumes/MICROBIT"))
        return "MICROBIT";
    }
    return "";
  }

  private static String findVolumeNameLinux()
  {
    ESM.debug("Calling findVolumeNameLinux()");
    String subdir = "/media/" + username + "/";
    File[] files = new File(subdir).listFiles();
    if (files != null)
    {
      for (File f : files)
      {
        if (f.toString().equals(subdir + "MINI"))
          return subdir + "MINI/";
        if (f.toString().equals(subdir + "MICROBIT"))
          return subdir + "MICROBIT/";
      }
    }
    return "";
  }

  private static boolean is64bit()
  {
    return System.getProperty("sun.arch.data.model").equals("64");
  }

  private static String getOSName()
  {
    return System.getProperty("os.name");
  }

  private static void delay(long time)
  {
    try
    {
      Thread.sleep(time);
    }
    catch (InterruptedException ex)
    {
    }
  }

  protected static boolean isMacOS()
  {
    return osname.startsWith("Mac");
  }

  protected static boolean isMacOSLeopard()
  {
    return osname.startsWith("Mac")
      && System.getProperty("os.version").compareTo("10.5") >= 0;
  }

  protected static boolean isMacOSSnowLeopard()
  {
    return osname.startsWith("Mac")
      && System.getProperty("os.version").compareTo("10.6") >= 0;
  }

  protected static boolean isWinOS()
  {
    return osname.startsWith("Windows");
  }

  protected static boolean isModernWinOS()
  {
    return isWinOS()
      && System.getProperty("os.version").compareTo("6.0") >= 0;
  }

  protected static boolean isLinux()
  {
    return osname.startsWith("Linux");
  }

  protected static boolean isSolaris()
  {
    return osname.startsWith("Solaris");
  }
}
