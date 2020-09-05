// Monitor.java

package ch.aplu.util;

/*
 This software is part of the JEX (Java Exemplarisch) Utility Library.
 It is Open Source Free Software, so you may
 - run the code for any purpose
 - study how the code works and adapt it to your needs
 - integrate all or parts of the code in your own programs
 - redistribute copies of the code
 - improve the code and release your improvements to the public
 However the use of the code is entirely your responsibility.
 */
/**
 * Helper class for wait-notify mechanism.
 */
public class Monitor
{
  private static Object myMonitor = new Object();
  private static Thread t = null;

  private Monitor()
  {
  }

  /**
   * Same as putSleep(Object monitor, int timeout), but uses internal
   * static monitor object.
   */
  public static boolean putSleep(int timeout)
  {
    return putSleep(myMonitor, timeout);
  }

  /**
   * Put the current thread in a wait state until wakeUp() is called
   * or timeout ( in ms ) expires. If timeout <= 0 the method blocks infinitely
   * until wakeUp() is called. More than one thread may be in the wait state,
   * calling wakeUp() will release one by one.<br>
   * Return values:<br>
   * true:  wakeUp was called before timeout expired ("no timeout occured")<br>
   * false: timeout expired before wakeUp was called ("timeout occured")<br>
   */
  public static boolean putSleep(Object monitor, final int timeout)
  {
    final Thread currentThread = Thread.currentThread();
    if (timeout > 0)
    {
      t = new Thread()  // Timeout thread
      {
        public void run()
        {
          boolean rc = delay(timeout);
          if (rc)
            currentThread.interrupt();
        }

      };
      t.start();
    }

    synchronized (monitor)
    {
      try
      {
        monitor.wait();
      }
      catch (InterruptedException ex)
      {
        return false;
      }
    }
    return true;
  }

  /**
   * Same as putSleep(Object monitor, int timeout) with timeout = 0 (timeout disabled).
   */
  public static boolean putSleep(Object monitor)
  {
    return putSleep(monitor, 0);
  }

  /**
   * Same as putSleep(int timeout) with timeout = 0 (timeout disabled).
   */
  public static boolean putSleep()
  {
    return putSleep(myMonitor, 0);
  }

  /**
   * Wake up all threads waiting for the given object's lock.
   */
  public static void wakeUp(Object monitor)
  {
    if (t != null)
      t.interrupt();  // Stop delay(), this will stop the timeout thread
    synchronized (monitor)
    {
      monitor.notifyAll();
    }
  }

  /**
   * Wake up all threads waiting for the internal static monitor lock.
   */
  public static void wakeUp()
  {
    wakeUp(myMonitor);
  }

  private static boolean delay(int time)
  {
    try
    {
      t.sleep(time);
    }
    catch (InterruptedException ex)
    {
      return false;
    }
    return true;
  }

}
