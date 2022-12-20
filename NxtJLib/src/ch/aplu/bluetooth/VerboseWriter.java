// VerboseWriter.java

/*
This software is part of the Aplu Bluetooth Library.
It is Open Source Free Software, so you may
- run the code for any purpose
- study how the code works and adapt it to your needs
- integrate all or parts of the code in your own programs
- redistribute copies of the code
- improve the code and release your improvements to the public
However the use of the code is entirely your responsibility.
 */

package ch.aplu.bluetooth;

import java.io.*;
import java.util.Date;

/**
 * Class to output text information either to System.out or into a log file.
 * Call VerboseWriter.out.println() to write out the information.
 */
public class VerboseWriter
{
  private static File file = null;
  
  /**
   * PrintStream object reference.
   */
  public static PrintStream out = System.out;

  /**
   * Redirects output to the given log file. The content of an existing file is erased.
   */
  public static synchronized void init(String logpath)
  {
    file = new File(logpath);
    try
    {      
      file.createNewFile();
      out = new PrintStream(new FileOutputStream(file));
      out.println("Log started at: " + new Date());

    }
    catch (IOException ex)
    {
      System.out.println("Can't create log file " + logpath);
    }
  }

  /**
   * Closes the log file.
   */
  public static synchronized void close()
  {
    if (file != null)
      out.close();
  }
}
