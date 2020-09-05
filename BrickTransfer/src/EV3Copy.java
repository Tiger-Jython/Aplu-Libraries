// EV3Copy.java

import com.jcraft.jsch.*;
import java.io.*;

public class EV3Copy
{

  public static class MyUserInfo implements UserInfo
  {
    public String getPassword()
    {
      return "";  // Password empty
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

  private String ipAddress;
  private String sourcePath;
  private String targetPath;
  private String fileName;

  // ------------------------------------------------------------
  public EV3Copy(String ipAddress, String sourcePath, String targetPath)
  {
    this.ipAddress = ipAddress;
    this.sourcePath = sourcePath;
    this.targetPath = targetPath;
    fileName = targetPath.replace("\\", "/");
    fileName = fileName.substring(fileName.lastIndexOf('/') + 1);
    System.out.println("ipAddress: " + ipAddress);
    System.out.println("sourcePath: " + sourcePath);
    System.out.println("targetPath: " + targetPath);
  }

  public boolean execute()
  {
    return sendCommand("runprog");
 //   return sendCommand("kill -9 2275");
  }

  public boolean sendCommand(String command)
  {
    try
    {
      JSch jsch = new JSch();
      String user = "root";
      String host = ipAddress;
      Session session = jsch.getSession(user, host, 22);

      UserInfo ui = new MyUserInfo();
      session.setUserInfo(ui);
      session.connect();

      Channel channel = session.openChannel("exec");
      ((ChannelExec)channel).setCommand(command);

      OutputStream out = channel.getOutputStream();
      channel.connect();

      out.write(command.getBytes());
      out.flush();
      out.close();
      channel.disconnect();
      session.disconnect();
    }
    catch (Exception ex)
    {
      return false;
    }
    return true;
  }

  public boolean copy()
  {
    FileInputStream fis = null;
    try
    {
      String user = "root";
      String host = ipAddress;
      JSch jsch = new JSch();
      Session session = jsch.getSession(user, host, 22);

      UserInfo ui = new MyUserInfo();
      session.setUserInfo(ui);
      session.connect();

      boolean ptimestamp = true;

// exec 'scp -t remoteFile' remotely
      String command = "scp " + (ptimestamp ? "-p" : "") + " -t " + targetPath;
      Channel channel = session.openChannel("exec");
      ((ChannelExec)channel).setCommand(command);

// get I/O streams for remote scp
      OutputStream out = channel.getOutputStream();
      InputStream in = channel.getInputStream();

      channel.connect();

      if (checkAck(in) != 0)
      {
        return false;
      }

      File _lfile = new File(sourcePath);

      if (ptimestamp)
      {
        command = "T " + (_lfile.lastModified() / 1000) + " 0";
// The access time should be sent here,
// but it is not accessible with JavaAPI ;-<
        command += (" " + (_lfile.lastModified() / 1000) + " 0\n");
        out.write(command.getBytes());
        out.flush();
        if (checkAck(in) != 0)
        {
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
        return false;
      }

// send content of localFile
      fis = new FileInputStream(sourcePath);
      byte[] buf = new byte[1024];
      while (true)
      {
        int len = fis.read(buf, 0, buf.length);
        if (len <= 0)
          break;
        out.write(buf, 0, len); //out.flush();
      }
      fis.close();
      fis = null;
// send '\0'
      buf[0] = 0;
      out.write(buf, 0, 1);
      out.flush();
      if (checkAck(in) != 0)
      {
        return false;
      }
      out.close();

      channel.disconnect();
      session.disconnect();
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
      return false;
    }
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
//        System.out.print(sb.toString());
      }
      if (b == 2)
      { // fatal error
//        System.out.print(sb.toString());
      }
    }
    return b;
  }
}
