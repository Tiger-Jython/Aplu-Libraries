// PlatformTools.java, Java SE version
// Platform (Java SE, ME) dependent code

/*
 This software is part of the NxtJLib library.
 It is Open Source Free Software, so you may
 - run the code for any purpose
 - study how the code works and adapt it to your needs
 - integrate all or parts of the code in your own programs
 - redistribute copies of the code
 - improve the code and release your improvements to the public
 However the use of the code is entirely your responsibility.
 */
package ch.aplu.nxt.platform;

import java.io.*;
import ch.aplu.util.*;
import ch.aplu.nxt.*;

/** 
 * Some platform specific helper methods.
 */
public class PlatformTools implements SharedConstants
{
  private static LegoRobot robot = null;

  private PlatformTools()
  {
  }

  /**
   * Calls System.exit(0).
   */
  public static void exit()
  {
    Thread t = new Thread()
    {
      public void run()
      {
        String msg = "Closing connection and\n terminating application now...";
        ConnectPanel.show(msg);
        delay(2000);
        System.exit(0);
      }
    };
    t.setPriority(Thread.MAX_PRIORITY);
    t.start();
  }

  /**
   * Calls Monitor.putSleep().
   */
  public static void putSleep()
  {
    Monitor.putSleep();
  }

  /**
   * Calls Monitor.wakeUp().
   */
  public static void wakeUp()
  {
    Monitor.wakeUp();
  }

  /**
   * Sends the given file to the NXT brick via Bluetooth.
   * A Bluetooth connection must be previously established.
   * (Code entirely from leJOS NXJ distribution.)
   * @param robot the LegoRobot already connected
   * @param file the file to send
   */
  public static boolean sendFile(LegoRobot robot, File file)
  {
    if (!robot.isConnected())
      return false;

    PlatformTools.robot = robot;

    byte[] data = new byte[60];
    int len, sent = 0;
    FileInputStream in = null;
    try
    {
      in = new FileInputStream(file);
    }
    catch (FileNotFoundException ex)
    {
      return false;
    }

    openWrite(file.getName(), (int)file.length());
    try
    {
      while ((len = in.read(data)) > 0)
      {
        byte[] sendData = new byte[len];
        for (int i = 0; i < len; i++)
          sendData[i] = data[i];
        sent += len;
        writeFile(sendData);
      }
    }
    catch (IOException ex)
    {
      return false;
    }
    closeFile();
    return true;
  }

  private static void openWrite(String filename, int size)
  {
    byte[] command =
    {
      SYSTEM_COMMAND_NOREPLY, OPEN_WRITE
    };
    byte[] asciiFileName = new byte[filename.length()];
    for (int i = 0; i < filename.length(); i++)
      asciiFileName[i] = (byte)filename.charAt(i);
    command = appendBytes(command, asciiFileName);
    byte[] request = new byte[22];
    System.arraycopy(command, 0, request, 0, command.length);
    byte[] fileLength =
    {
      (byte)size, (byte)(size >>> 8),
      (byte)(size >>> 16), (byte)(size >>> 24)
    };
    request = appendBytes(request, fileLength);
    robot.sendData(request);
  }

  private static void writeFile(byte[] data)
  {
    byte[] request = new byte[data.length + 3];
    byte[] command =
    {
      SYSTEM_COMMAND_NOREPLY, WRITE, (byte)0
    };  // No handle
    System.arraycopy(command, 0, request, 0, command.length);
    System.arraycopy(data, 0, request, 3, data.length);
    robot.sendData(request);
  }

  private static void closeFile()
  {
    byte[] request =
    {
      SYSTEM_COMMAND_NOREPLY, CLOSE, (byte)0
    };  // No handle
    robot.sendData(request);
  }

  private static byte[] appendBytes(byte[] array1, byte[] array2)
  {
    byte[] array = new byte[array1.length + array2.length];
    System.arraycopy(array1, 0, array, 0, array1.length);
    System.arraycopy(array2, 0, array, array1.length, array2.length);
    return array;
  }

  private static void delay(long timeout)
  {
    try
    {
      Thread.currentThread().sleep(timeout);
    }
    catch (InterruptedException ex)
    {
    }
  }
}
