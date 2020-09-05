// HiResAlarmTimer.java

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

/** Alarm timer with high resolution.
 * (Resolution normally better than 1 microseconds.)
 * The timer's precision is not garanteed because of
 * multitasking/multithreading of operating system and Java.
 * The timer starts at the given preset time, runs backwards and stops
 * when it reaches zero.<br><br>
 * (When using timer events by registering a TimerListener,
 * the AlarmTimer restarts automatically.)<br><br>
 * (All timer manipulation methods call yield()
 * of the current thread unless disabled by special constructor.)
 */
public class HiResAlarmTimer extends BaseAlarmTimer
{
  /**
   * Construct a alarm timer instance with better
   * than 1 microsecond resolution on most systems
   * and set its preset time in microseconds (us).
   * The timer must be started by calling start().
   * It runs backward and stops when it reaches zero.
   */
  public HiResAlarmTimer(long alarmTime)
  {
    super(new HiResTimer(), alarmTime, false);
  }

  /**
   * Same as HiResAlarmTimer(alarmTime), but the timer starts immediately if
   * autostart is true.
   */
  public HiResAlarmTimer(long alarmTime, boolean autostart)
  {
    super(new HiResTimer(), alarmTime, autostart);
  }

  /**
   * Same as HiResAlarmTimer(alarmTime, autostart), but most methods calls
   * Thread.yield() automatically, if yield is true.
   * This may give a much better time response when using
   * loops with an empty body like<br><br>while ( timer.isRunning() ) {}.
   */
  public HiResAlarmTimer(long alarmTime, boolean autostart, boolean yield)
  {
    super(new HiResTimer(), alarmTime, autostart, yield);
  }

}
