// Tools.java

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

import lejos.hardware.Button;
import lejos.hardware.lcd.LCD;

/**
 * Class with some useful helper methods.
 */
public class Tools
{
  private static long startTime = 0L;
  private static Thread t = null;
  private static boolean isSleeping;
  private static Object monitor = new Object();
  

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
      Thread.currentThread().sleep(duration);
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

  /**
   * Puts the current thread in a wait keyState until wakeUp() is called
 or timeout (in ms) expires. If timeout <= 0 the method blocks infinitely
 until wakeUp() is called. Only one thread may be in the wait keyState.<br>
   * Return values:<br>
   * true:  wakeUp was called before timeout expired ("no timeout occured")<br>
   * false: timeout expired before wakeUp was called ("timeout occured")<br>
   */
  public static boolean putSleep(final int timeout)
  {
    final Thread currentThread = Thread.currentThread();
    if (timeout > 0)
    {
      t = new Thread()  // Timeout thread
      {
        public void run()
        {
          boolean rc = waitTimeout(timeout);
          if (rc)
            currentThread.interrupt();
        }
      };
      t.start();
    }

    isSleeping = true;
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
   * Same as putSleep(int timeout) with timeout = 0 (timeout disalbed).
   */
  public static boolean putSleep()
  {
    return putSleep(0);
  }

  /**
   * Wakes up the waiting thread.
   */
  public static void wakeUp()
  {
    if (t != null)
      t.interrupt();  // Stop delay(), this will stop the timeout thread
    synchronized (monitor)
    {
      monitor.notify();
    }
    isSleeping = false;
  }

  private static boolean waitTimeout(int time)
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

  /**
   * Returns true, if LEFT buttons is pressed.
   */
  public static boolean isLeftPressed()
  {
    delay(10);
    int state = Button.readButtons();
    return ((state & Button.ID_LEFT) != 0);
  }

  /**
   * Waits until LEFT button is pressed.
   */
  public static void waitLeft()
  {
    Button.LEFT.waitForPress();
  }

  /**
   * Displays the prompt and waits until LEFT button is pressed.
   */
  public static void waitLeft(String prompt)
  {
    if (prompt != null)
      LCD.drawString(prompt, 0, 0);
    waitEnter();
  }

  /**
   * Returns true, if RIGHT buttons is pressed.
   */
  public static boolean isRightPressed()
  {
    delay(10);
    int state = Button.readButtons();
    return ((state & Button.ID_RIGHT) != 0);
  }

  /**
   * Waits until RIGHT button is pressed.
   */
  public static void waitRight()
  {
    Button.RIGHT.waitForPress();
  }

  /**
   * Displays the prompt and waits until RIGHT button is pressed.
   */
  public static void waitRight(String prompt)
  {
    if (prompt != null)
      LCD.drawString(prompt, 0, 0);
    waitRight();
  }

  /**
   * Returns true, if UP buttons is pressed.
   */
  public static boolean isUpPressed()
  {
    delay(10);
    int state = Button.readButtons();
    return ((state & Button.ID_UP) != 0);
  }

  /**
   * Waits until UP button is pressed.
   */
  public static void waitUp()
  {
    Button.UP.waitForPress();
  }

  /**
   * Displays the prompt and waits until UP button is pressed.
   */
  public static void waitUp(String prompt)
  {
    if (prompt != null)
      LCD.drawString(prompt, 0, 0);
    waitUp();
  }

  /**
   * Returns true, if DOWN buttons is pressed.
   */
  public static boolean isDownPressed()
  {
    delay(10);
    int state = Button.readButtons();
    return ((state & Button.ID_DOWN) != 0);
  }

  /**
   * Waits until DOWN button is pressed.
   */
  public static void waitDown()
  {
    Button.DOWN.waitForPress();
  }

  /**
   * Displays the prompt and waits until DOWN button is pressed.
   */
  public static void waitDown(String prompt)
  {
    if (prompt != null)
      LCD.drawString(prompt, 0, 0);
    waitDown();
  }

  /**
   * Returns true, if ENTER buttons is pressed.
   */
  public static boolean isEnterPressed()
  {
    delay(10);
    int state = Button.readButtons();
    return ((state & Button.ID_ENTER) != 0);
  }

  /**
   * Waits until ENTER button is pressed.
   */
  public static void waitEnter()
  {
    Button.ENTER.waitForPress();
  }

  /**
   * Displays the prompt and waits until ENTER button is pressed.
   */
  public static void waitEnter(String prompt)
  {
    if (prompt != null)
      LCD.drawString(prompt, 0, 0);
    waitEnter();
  }

  /**
   * Returns true, if ESCAPE buttons is pressed.
   */
  public static boolean isEscapePressed()
  {
    delay(10);
    int state = Button.readButtons();
    return ((state & Button.ID_ESCAPE) != 0);
  }

  /**
   * Waits until ESCAPE button is pressed.
   */
  public static void waitEscape()
  {
    Button.ESCAPE.waitForPress();
  }

  /**
   * Displays the prompt and waits until ESCAPE button is pressed.
   */
  public static void waitEscape(String prompt)
  {
    if (prompt != null)
      LCD.drawString(prompt, 0, 0);
    waitEscape();
  }

  /**
   * Returns true, if any buttons is pressed.
   */
  public static boolean isButtonPressed()
  {
    delay(10);
    int state = Button.readButtons();
    return (state != 0);
  }

  /**
   * Waits until any button is pressed.
   */
  public static void waitButton()
  {
    Button.waitForAnyPress();
  }

  /**
   * Displays the prompt and waits until any button is pressed.
   */
  public static void waitButton(String prompt)
  {
    if (prompt != null)
      LCD.drawString(prompt, 0, 0);
    waitButton();
  }
}
