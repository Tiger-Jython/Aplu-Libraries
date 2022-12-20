// Tools.java

package ch.aplu.mbm;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import jssc.SerialPortList;

public class Tools
{
  private static String driveLetters = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
  private static final String osname = System.getProperty("os.name", "");
  private static final String username = System.getProperty("user.name", "");
  public static String deviceType = "NOT DETECTED";
  public static boolean isMicrobitV2 = false;

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
    String port = "";
    int nb = 0;
    while (((port = findCommPort()) == null) && nb < nbRetries)
    {
      nb += 1;
      delay(100);
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
  
  public static String findCommPortLinux()
  {
    Handler.debug("Calling findCommPortLinux()");  
    String[] portNames = SerialPortList.getPortNames();
    for (String portName : portNames)
    {
      if (portName.contains("ttyACM0"))
      {
        Handler.debug("findCommPortLinux() found: " + portName);
        return portName;
      }
    }
    Handler.debug("findCommPortLinux() port name not found");
    return "";
  }

  public static String findCommPortMac()
  { 
    Handler.debug("Calling findCommPortMac()");  
    File dir = new File("/dev");  
    File[] dirListing = dir.listFiles();
    for (File f: dirListing)
    { 
      String s = f.getName();
      if (s.contains("tty.usbmodem"))
      {    
        String portName = "/dev/" + s;  
        Handler.debug("findCommPortMac() found: " + portName);
        return portName;
      }
    } 
    Handler.debug("findCommPortMac() port name not found");
    return "";
  }

  public static String findCommPortWin()
  {
    Handler.debug("Calling findCommPortWin()");  
    ArrayList<String> reply = getInfoFromRegistry();
    String portName = "";
    String firstPortName = "";
    for (String line : reply)
    {
      int index = line.lastIndexOf("REG_SZ");
      if (index == -1)
        continue;
      Handler.debug("line from registry: " + line);
      portName = line.substring(index + 6).trim();
      if (line.contains("thcdcacm") || line.contains("USB"))
        return portName;
      else
      {
        if (firstPortName == null)
          firstPortName = portName;
      }
    }
    Handler.debug("findCommPortWin() found: " + firstPortName);
    return firstPortName;
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

  public static String findVolumeName()
  {
    isMicrobitV2 = false;
    String path = null;  
    if (isWinOS())
      path = findVolumeNameWin();
    else if (isMacOS())
      path = findVolumeNameMac();
    else if (isLinux())
      path = findVolumeNameLinux();
    if (deviceType.equals("MICROBIT"))
      detectMicrobitVersion(path);
    return path;
  }
  
  private static void detectMicrobitVersion(String path) {
    if (USBDeviceIdentifier.identify(path) == USBDevice.MicroBitV2)
      isMicrobitV2 = true;
  }

  public static String findVolumeNameWin()
  {
    Handler.debug("Calling findVolumeNameWin()");  
    for (int i = 0; i < driveLetters.length(); i++)
    {
      char ch = driveLetters.charAt(i);
      ArrayList<String> reply = getDriveInfoWin(ch);
      Handler.debug("reply from driveInfo: " + reply);  
      if (reply.size() > 0 && (reply.get(0).contains("MICROBIT")))
      {  
        deviceType = "MICROBIT";
        Handler.debug("device type detected " + deviceType);  
        return (ch + ":/");
      }  
      if (reply.size() > 0 && (reply.get(0).contains("MINI")))
      {  
        deviceType = "CALLIOPE";
        Handler.debug("device type detected " + deviceType);  
        return (ch + ":/");
      }
      if (reply.size() > 0 && (reply.get(0).contains("CIRCUITPY")))
      {  
        deviceType = "CIRCUITPY";
        Handler.debug("device type detected " + deviceType);  
        return (ch + ":/");
      }
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
    Handler.debug("Calling findVolumeNameMac()");  
    File[] files = new File("/Volumes/").listFiles();
    for (File f : files)
    {
      if (f.toString().equals("/Volumes/MINI"))
      {  
        deviceType = "CALLIOPE";
        Handler.debug("device type detected " + deviceType);  
        return "MINI";
      }
      if (f.toString().equals("/Volumes/MICROBIT"))
      {  
        deviceType = "MICROBIT";
        Handler.debug("device type detected " + deviceType);  
        return "MICROBIT";
      }
      if (f.toString().equals("/Volumes/CIRCUITPY"))
      {  
        deviceType = "CIRCUITPY";
        Handler.debug("device type detected " + deviceType);  
        return "CIRCUITPY";
      }
    }
    return "";
  }
  
  private static String findVolumeNameLinux()
  {
    Handler.debug("Calling findVolumeNameLinux()");  
    String subdir = "/media/" + username + "/";
    File[] files = new File(subdir).listFiles();
    if (files !=  null)
    {    
        for (File f : files)
        {
          if (f.toString().equals(subdir + "MINI"))
          {  
            deviceType = "CALLIOPE";
            Handler.debug("device type detected " + deviceType);  
            return subdir + "MINI/";
          }
          if (f.toString().equals(subdir + "MICROBIT"))
          {  
            deviceType = "MICROBIT";
            Handler.debug("device type detected " + deviceType);  
            return subdir + "MICROBIT/";
          }  
          if (f.toString().equals(subdir + "CICUITPY"))
          {  
            deviceType = "CIRCUITPY";
            Handler.debug("device type detected " + deviceType);  
            return subdir + "CIRCUITPY/";
          }  
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
