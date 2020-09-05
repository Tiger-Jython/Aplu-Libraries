// TimerAdapter.java

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
package ch.aplu.util;

/**
 * An abstract adapter class for receiving timer events.
 * Extend this class to create a Timer listener and override
 * the method timeElapsed().<br><br>
 * Create a listener object by extending this class and
 * register it with the HiResAlarmTimer/'s or LoResAlarmTimer's addTimerListener() method.
 * When the time interval of the AlarmTimer expires
 * the timeElapsed() method in the listener object is invoked.
 */
abstract public class TimerAdapter implements TimerListener
{
  /**
   * Invoked when the time interval expires.
   */
  public boolean timeElapsed()
  {
    return false;
  }

}
