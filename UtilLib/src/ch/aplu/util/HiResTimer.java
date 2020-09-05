// HiResTimer.java
// J2SE V5 up only

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

import java.util.*;

/**
 * Timer with high resolution.
 * (Resolution normally better than 1 microseconds.)
 * The timer's accuracy is not garanteed because of
 * multitasking/multithreading of operating system and Java.
 * (All methods (except constructors) call yield()
 * of the current thread unless disabled by special constructor.)
 */
public class HiResTimer extends BaseTimer
{
  /**
   * Construct a HiResTimer object and set its time to zero.
   * The timer must be started by calling start().
   */
  public HiResTimer()
  {
    super();
  }

  /**
   * Same as HiResTimer(), but the timer starts immediately if
   * autostart is true.
   */
  public HiResTimer(boolean autostart)
  {
    super(autostart);
  }

  /**
   * Same as HiResTimer(autostart), but most methods calls
   * Thread.yield() automatically, if yield is true.
   * This may give a much better time response when using
   * loops with an empty body like<br><br>while ( timer.isRunning() ) {}.
   */
  public HiResTimer(boolean autostart, boolean yield)
  {
    super(autostart, yield);
  }

  protected long getCurrentTime()
  {
    return System.nanoTime(); // Java V5 up
  }

}
