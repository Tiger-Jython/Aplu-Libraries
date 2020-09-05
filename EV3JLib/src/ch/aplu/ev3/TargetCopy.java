// TargetCopy.java

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

import com.jcraft.jsch.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.io.*;
/*
 import java.net.*;
 import ch.aplu.bluetooth.*;
 import javax.swing.*;
 import ch.aplu.util.*;
 import javax.bluetooth.RemoteDevice;
 */

/**
 * Class to download programs to Target via SCP.
 * Based on the Java Secure Channel (Jsch) project from
 * http://www.jcraft.com/jsch/
 * You must include the jsch library in your class path.
 */
public class TargetCopy
{
  private boolean debug = false;
  private static final String startScript = "startApp";

  // 
  //
  /**
   * Class to provide user information for SSH login.
   */
  public static class MyUserInfo implements UserInfo
  {
    private String pwd = "";

    public MyUserInfo(String pwd)
    {
      this.pwd = pwd;
    }

    public String getPassword()
    {
      return pwd;
    }

    public boolean promptYesNo(String str)
    {
      return true;
    }

    public String getPassphrase()
    {
      return null;
    }

    public boolean promptPassphrase(String message)
    {
      return true;
    }

    public boolean promptPassword(String message)
    {
      return true;
    }

    public void showMessage(String message)
    {
    }
  }

  private static String user;
  private static String pwd;
  private String targetName;
  private String sourcePath;
  private String targetPath;
  private String fileName;
  private String targetType;
  private boolean isLibrary;
  private boolean isBluetooth = false;
  /*
   private boolean isTransferSuccessful;
   private boolean isTransferDone;
   private String btServerName = "raspberrypi";
   private final String btServiceName = "SampleServer";
   */
  private final String errorMsg
    = "Check if the target brick is switched on,\n"
    + "and ready for connection over IP.";

  public TargetCopy(String targetAddress, String sourcePath, String fileName,
    String targetType, Boolean isLibrary)
  {
    targetName = targetAddress.trim();
    this.sourcePath = sourcePath;
    this.targetType = targetType;
    this.fileName = fileName;
    this.isLibrary = isLibrary;

    if (this.targetType.equals("ev3"))
    {
      this.targetPath = "/home/python/scripts/" + fileName;
      user = "root";
      pwd = "";
    }
    else if (this.targetType.equals("raspi"))
    {
      if (targetName.length() > 3
        && targetName.substring(0, 3).toLowerCase().equals("bt:"))
      {
        targetName = targetName.substring(3);
        isBluetooth = true;
        if (debug)
          System.out.println("Bluetooth friendly name: " + targetName);
      }
      if (isLibrary)
        this.targetPath = "/home/pi/scripts/" + fileName;
      else
        this.targetPath = "/home/pi/scripts/MyApp.py";
      user = "pi";
      pwd = "raspberry";
    }
    else
    {
      System.out.println("Illegal target type");
      return;
    }

    if (debug)
    {
      System.out.println("Current values:");
      System.out.println("targetType: " + this.targetType);
      System.out.println("user: " + this.user);
      System.out.println("password: " + this.pwd);
      System.out.println("targetName: " + targetName);
      System.out.println("sourcePath: " + this.sourcePath);
      System.out.println("targetPath: " + this.targetPath);
      System.out.println("fileName: " + this.fileName);
      System.out.println("isLibrary: " + this.isLibrary);
    }
  }

  public void execute(String execScript)
  {
    if (debug)
      System.out.println("Calling execute with param: "
        + execScript + " . Target is: " + targetType);

    if (targetType.equals("raspi"))
    {
      if (isBluetooth)
      {
        System.out.println("No remote execution with Bluetooth");
        return;
      }
      List<String> commands = new ArrayList<String>();
      String cmd = "startApp";
      if (debug)
        System.out.println("Executing " + cmd);
      commands.add(cmd);
      executeCommands(commands);
    }
    else if (targetType.equals("ev3"))
    {
      System.out.println("Executing now (if BrickGate server is running)...");
      sendCommand("pyrun /home/python/scripts/" + execScript);
    }
  }

  private void executeCommands(List<String> commands)
  {
    Session session = null;
    ChannelShell channel = null;

    try
    {
      session = connect(targetName, user, pwd);
      if (session == null)
      {
        System.out.println("Failed to execute remote command");
        return;
      }
      channel = (ChannelShell)session.openChannel("shell");
      if (channel == null)
      {
        System.out.println("Failed to execute remote command");
        return;
      }
      channel.connect();
      sendCommands(channel, commands);
      readChannelOutput(channel);
    }
    catch (JSchException ex)
    {
    }
    close(session, channel);
  }

  private Session connect(String hostname, String username, String password)
  {
    Session session = null;
    if (debug)
      System.out.println("Calling connect with hostname: " + hostname
        + ", username: " + username + ", password: " + password);
    JSch jSch = new JSch();
    try
    {
      session = jSch.getSession(username, hostname, 22);
      Properties config = new Properties();
      config.put("StrictHostKeyChecking", "no");
      session.setConfig(config);
      session.setPassword(password);
      session.connect();
    }
    catch (JSchException e)
    {
      System.out.println("Connection to '" + username + "@"
        + hostname + "' failed");
    }
    return session;
  }

  private void sendCommands(Channel channel, List<String> commands)
  {
    try
    {
      PrintStream out = new PrintStream(channel.getOutputStream());
      for (String command : commands)
        out.println(command);
      out.println("exit");
      out.flush();
    }
    catch (Exception e)
    {
      System.out.println("Error while sending execution commands");
    }
  }

  private static void readChannelOutput(Channel channel)
  {
    byte[] buffer = new byte[1024];
    boolean printout = false;
    try
    {
      InputStream in = channel.getInputStream();
      String line = "";
      while (true)
      {
        while (in.available() > 0)
        {
          int i = in.read(buffer, 0, 1024);
          if (i < 0)
            break;

          line = new String(buffer, 0, i);

          // Check for end of printout
          int idx;
          if (printout && (idx = line.indexOf("pi@")) != -1)
          {
            writeToConsole(line.substring(0, idx));
            printout = false;
          }

          // Standard write out
          if (printout)
            writeToConsole(line);

          // Check for start/end of printout
          int start = 0;
          if (!printout && (start = line.
            indexOf("~$ " + startScript)) != -1)
          {
            // Start found, check for end
            int end = line.indexOf("pi@", start + 1);
            if (end == -1)  // end not found
            {
              writeToConsole(line.substring(start));
              printout = true;
            }
            else  // end found, so terminate write out
            {
              printout = false;
            }
          }
        }
        // Check for start/end of printout
        int start;
        if (!printout && (start = line.
          indexOf("~$ " + startScript)) != -1)
        {
          int end = line.indexOf("pi@", start + 1);
          if (end == -1)
          {
            writeToConsole(line.substring(start));
            printout = true;
          }
          else
          {
            writeToConsole(line.substring(start, end));
            printout = false;
          }
        }

        // loop until 'logout' is found
        if (line.contains("logout"))
          break;

        if (channel.isClosed())
          break;
        // Sleep a while to economize CPU power
        try
        {
          Thread.sleep(100);
        }
        catch (Exception ee)
        {
        }
      }
    }
    catch (Exception e)
    {
      System.out.println("Failed to execute remote command");
    }
  }

  private static void writeToConsole(String line)
  {
    int n = line.indexOf("GPIO.setwarnings(False)");

    if (n == -1)
    {
      String s = "";
      try
      {
        s = new String(line.getBytes(), "UTF-8");
      }
      catch (UnsupportedEncodingException ex)
      {
      }
      System.out.print(s);
    }
    else
    {
      // Cutting two lines with the warning (2 lines)
      String lines[] = line.split("\n");
      int k;  // determine line index where the warning begins
      for (k = 0; k < lines.length; k++)
        if (lines[k].indexOf("GPIO.setwarnings(False)") != -1)
          break;
      String text = "";
      for (int i = 0; i < lines.length; i++)
      {
        if (!(i == k || i == k + 1))  // this line and the next
        {
          if (i < lines.length - 1)
            text += lines[i] + "\n";  // append again (split removed it)
          else
            text += lines[i];
        }
      }
      String s = "";
      try
      {
        s = new String(text.getBytes(), "UTF-8");
      }
      catch (UnsupportedEncodingException ex)
      {
      }
      System.out.println(s);
      /* another way to do it
       String header = line.substring(0, n); 
       String trailer = line.substring(n); 
       int newline = header.lastIndexOf('\n');  // End of last line
       header = header.substring(0, newline);
       newline = trailer.indexOf('\n');
       trailer = trailer.substring(newline + 1);
       newline = trailer.indexOf('\n');   
       trailer = trailer.substring(newline); // From two lines down
       System.out.print(header + trailer);
       */
    }
  }

  public void close(Session session, Channel channel)
  {
    try
    {
      channel.disconnect();
      session.disconnect();
    }
    catch (Exception ex)
    {
    }
  }

  public void cleanup()
  {
    System.out.println("Stopping Python on target now...");
    sendCommand("sudo pkill python");
  }

  public boolean sendCommand(String cmd)
  {
    if (debug)
      System.out.println("Calling sendCommand with param: " + cmd);
    try
    {
      JSch jsch = new JSch();
      String host = targetName;
      Session session = jsch.getSession(user, host, 22);
      UserInfo ui = new MyUserInfo(pwd);
      session.setUserInfo(ui);
      session.connect();

      ChannelExec channel = (ChannelExec)session.openChannel("exec");
      BufferedReader in = new BufferedReader(new InputStreamReader(channel.getInputStream()));

      channel.setCommand(cmd);
      channel.connect();

      StringBuilder output = new StringBuilder();

      String s = null;
      while ((s = in.readLine()) != null)
      {
        output.append(s + "\n");
      }

      System.out.println(output);
      channel.disconnect();
      session.disconnect();
    }
    catch (Exception ex)
    {
      ex.printStackTrace();
      return false;
    }
    return true;
  }

  public boolean copy()
  {
    if (isBluetooth)
      return copyByBluetooth();

    if (targetType.equals("ev3"))
      System.out.println("Downloading '" + fileName + "' to EV3 (" + targetName + ")...");
    if (targetType.equals("raspi"))
      System.out.println("Downloading '" + fileName + "' to Raspberry Pi (" + targetName + ")...");
    if (new File(sourcePath).length() == 0)
    {
      System.out.println("'" + fileName + "' empty. Nothing to download.");
      return false;
    }
    double startTime = System.currentTimeMillis();
    FileInputStream fis = null;
    try
    {
      String host = targetName;
      JSch jsch = new JSch();
      Session session = jsch.getSession(user, host, 22);

      UserInfo ui = new MyUserInfo(pwd);
      session.setUserInfo(ui);
      session.connect();

      boolean ptimestamp = false;  // Since NOOPS 1.9 set to false
      // false: set timestamp to current time
      // true: use timestamp of original file

// exec 'scp -t remoteFile' remotely
      String command = "scp " + (ptimestamp ? "-p" : "") + " -t " + targetPath;
      if (debug)
        System.out.println("Executing command: " + command);
      Channel channel = session.openChannel("exec");
      ((ChannelExec)channel).setCommand(command);

// get I/O streams for remote scp
      OutputStream out = channel.getOutputStream();
      InputStream in = channel.getInputStream();

      channel.connect();

      if (checkAck(in) != 0)
      {
        displayError(1);
        return false;
      }

      File _lfile = new File(sourcePath);

      if (ptimestamp)
      {
//        command = "T " + (_lfile.lastModified() / 1000) + " 0";
// Removed space after T to work on Raspberry Pi (from NOOBS 1.9)
// Not checked if this works on older NOOBS or EV3, 
// because ptimestamp is set to false now        
        command = "T" + (_lfile.lastModified() / 1000) + " 0";
// The access time should be sent here,
// but it is not accessible with JavaAPI ;-<
        command += (" " + (_lfile.lastModified() / 1000) + " 0\n");
        out.write(command.getBytes());
        out.flush();
        if (checkAck(in) != 0)
        {
          displayError(2);
          return false;
        }
      }

// send "C0644 filesize filename", where filename should not include '/'
      long filesize = _lfile.length();
      command = "C0644 " + filesize + " ";
      command += fileName;
      command += "\n";
      out.write(command.getBytes());
      out.flush();
      if (checkAck(in) != 0)
      {
        displayError(3);
        return false;
      }
// send content of localFile
      fis = new FileInputStream(sourcePath);
      byte[] buf = new byte[1024];
      int totalLen = 0;
      int nbPoints = 0;
      while (true)
      {
        int len = fis.read(buf, 0, buf.length);
        if (len <= 0)
          break;
        out.write(buf, 0, len);
        totalLen += len;
        if (totalLen % 1000 == 0)
        {
          System.out.print(".");
          nbPoints++;
          if (nbPoints % 50 == 0)
            System.out.println();
        }
      }
      fis.close();
      fis = null;
// send '\0'
      buf[0] = 0;
      out.write(buf, 0, 1);
      out.flush();
      if (checkAck(in) != 0)
      {
        displayError(4);
        return false;
      }
      out.close();

      channel.disconnect();
      session.disconnect();
      System.out.println("OK (" + totalLen + " bytes transferred).");
      double endTime = System.currentTimeMillis();
      System.out.println("   Time elapsed: " + (int)(endTime - startTime) + " ms");
      if (targetType.equals("raspi") && !isLibrary)
      {
        String cmd = "cp /home/pi/scripts/MyApp.py /home/pi/scripts/" + fileName;
        sendCommand(cmd);
      }
      return true;
    }
    catch (Exception e)
    {
      try
      {
        if (fis != null)
          fis.close();
      }
      catch (Exception ee)
      {
      }
      displayError(5);
      return false;
    }
  }

  private void displayError(int errNb)
  {
    System.out.println("Download to " + targetName + " failed.\n"
      + errorMsg);
    if (debug)
      System.out.println("errNb: " + errNb);
  }

  private int checkAck(InputStream in) throws IOException
  {
    int b = in.read();
// b may be 0 for success,
// 1 for error,
// 2 for fatal error,
// -1
    if (b == 0)
      return b;
    if (b == -1)
      return b;

    if (b == 1 || b == 2)
    {
      StringBuffer sb = new StringBuffer();
      int c;
      do
      {
        c = in.read();
        sb.append((char)c);
      }
      while (c != '\n');
      if (b == 1)
      { // error
        System.out.print(sb.toString());
      }
      if (b == 2)
      { // fatal error
        System.out.print(sb.toString());
      }
    }
    return b;
  }

  private boolean copyByBluetooth()
  {
    System.out.println("Downloading '" + fileName
      + "' to Bluetooth device '" + targetName + "'");
    new PushToRaspi(targetName, sourcePath);
    return true;
  }
}
