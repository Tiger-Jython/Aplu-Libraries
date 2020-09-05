// LoResTimer.java

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
 * Timer with low resolution (based on Date.getTime())
 * (Resolution normally less than 10 milliseconds.)
 * (All methods (except constructors) call yield()
 * of the current thread unless disabled by special constructor.)
 */
public class LoResTimer extends BaseTimer
{
  /**
   * Construct a LoResTimer object and set its time to zero.
   * The timer must be started by calling start().
   */
  public LoResTimer()
  {
    super();
  }

  /**
   * Same as LoResTimer(), but the timer starts immediately if
   * autostart is true.
   */
  public LoResTimer(boolean autostart)
  {
    super(autostart);
  }

  /**
   * Same as LoResTimer(autostart), but most methods calls
   * Thread.yield() automatically, if yield is true.
   * This may give a much better time response when using
   * loops with an empty body like<br><br>while ( timer.isRunning() ) {}.
   */
  public LoResTimer(boolean autostart, boolean yield)
  {
    super(autostart, yield);
  }

  protected long getCurrentTime()
  {
    return 1000000L * new Date().getTime();
  }

}
