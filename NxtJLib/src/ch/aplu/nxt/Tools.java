// Tools.java

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

package ch.aplu.nxt;

import ch.aplu.nxt.platform.*;

/**
 * Some useful helper methods.
 */
public class Tools
{
  private static long startTime = 0L;
  protected static Thread t = null;

  /**
   * Starts a timer or restart it by setting its time to zero.
   */
  public static void startTimer()
  {
    startTime = System.currentTimeMillis();
  }

  /**
   * Gets the timer's time.
   * @return the current time of the timer (in ms)
   */
  public static long getTime()
  {
    if (startTime == 0)
      return 0L;
    else
      return System.currentTimeMillis() - startTime;
  }

  /**
   * Suspends execution of the current thread for the given amount of time.
   * (Other threads may continue to run.)
   * @param duration the duration (in ms)
   */
  public static void delay(long duration)
  {
    try
    {
      t = Thread.currentThread();
      t.sleep(duration);
    }
    catch (InterruptedException ex)
    {
    }
  }
  
  // For compatiblity with J2ME
  protected static int round(double x)
  {
    return (int)(Math.floor(x + 0.5));
  }  
}



