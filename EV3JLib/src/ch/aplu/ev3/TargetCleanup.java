// TargetCleanup.java
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

import com.jcraft.jsch.*;
import java.io.*;

/**
 * Class execute a remote shutdown of all Python processes.
 * Based on the Java Secure Channel (Jsch) project from
 * http://www.jcraft.com/jsch/
 * You must include the jsch library in your class path.
 */
public class TargetCleanup
{
  private boolean debug = false;

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

  private final String VERSION = "1.00";
  private static String user;
  private static String pwd;
  private String ipAddress;
  private String targetType;
  private boolean isCleaningUp = false;
  private boolean isShuttingDown = false;

  public TargetCleanup(String ipAddress, String targetType)
  {
    this.ipAddress = ipAddress;
    this.targetType = targetType;

    if (this.targetType.equals("ev3"))
    {
      user = "root";
      pwd = "";
    }
    else if (this.targetType.equals("raspi"))
    {
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
      System.out.println("ipAddress: " + this.ipAddress);
    }
  }

  public void cleanup()
  {
    if (isCleaningUp)
    {
      System.out.println("Be patient, please!");
      return;
    }
    isCleaningUp = true;
    System.out.println("Stopping Python on target "
      + ipAddress + " now...");
    if (targetType.equals("raspi"))
      sendCommand("rkill");
    else
      sendCommand("sudo pkill python");
    isCleaningUp = false;
  }

  public void shutdown(boolean restart)
  {
    if (isShuttingDown)
    {  
      System.out.println("Be patient, please!");
      return;
    }
    isShuttingDown = true;
    if (restart)
    {
      System.out.println("Shutting down/restarting target "
        + ipAddress + " now...");
      boolean rc = sendCommand("sudo shutdown -r now");
      if (rc)
        System.out.println("Done");

    }
    else
    {
      System.out.println("Shutting down target "
        + ipAddress + " now...");
      boolean rc = sendCommand("sudo halt");
      if (rc)
        System.out.println("Done");
    }
    isShuttingDown = false;
  }

  public boolean sendCommand(String cmd)
  {
    if (debug)
      System.out.println("Calling sendCommand with param: " + cmd);
    try
    {
      JSch jsch = new JSch();
      String host = ipAddress;
      Session session = jsch.getSession(user, host, 22);
      UserInfo ui = new MyUserInfo(pwd);
      session.setUserInfo(ui);
      session.connect();

      ChannelExec channel = (ChannelExec)session.openChannel("exec");
      BufferedReader in = new BufferedReader(new InputStreamReader(channel.getInputStream()));

      channel.setCommand(cmd);
      channel.connect();

      StringBuilder output = new StringBuilder();

      String s;
      while ((s = in.readLine()) != null)
        output.append(s + "\n");
      System.out.println(output);
      channel.disconnect();
      session.disconnect();
    }
    catch (Exception ex)
    {
      System.out.println("Remote command execution failed");
      return false;
    }
    return true;
  }
}
