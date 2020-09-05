// BaseTimer.java

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
 * Abstract base class for HiResTimer and LoResTimer.
 * See HiResTimer or LoResTimer for more details.
 */
abstract public class BaseTimer
{
  protected class State
  {
    public static final int ZERO = 1;
    public static final int STOPPED = 2;
    public static final int RUNNING = 3;
    public static final int ALARM = 4;
  }

  protected long _startTime;
  protected long _currentTime;
  protected int _state;
  protected boolean _doYield = true;

  protected BaseTimer()
  {
    this(false);
  }

  protected BaseTimer(boolean autostart)
  {
    _currentTime = getCurrentTime();
    _startTime = _currentTime;
    _state = State.ZERO;
    if (autostart)
      start();
  }

  protected BaseTimer(boolean autostart, boolean yield)
  {
    this(autostart);
    _doYield = yield;
  }

  /**
   * Return the current timer's value in microseconds (us).
   */
  public long getTime()
  {
    if (_doYield)
      Thread.currentThread().yield();
    long diffTime;

    if (_state == State.ZERO)
      return 0;

    if (_state == State.RUNNING)   // If _state == STOPPED, we do not modify _currentTime
      _currentTime = getCurrentTime();

    return (_currentTime - _startTime) / 1000;  // Integer division
  }

  /**
   * Reset the timer to zero and start it.
   * (If it was running or not.)
   */
  public void start()
  {
    if (_doYield)
      Thread.currentThread().yield();
    _currentTime = getCurrentTime();
    _startTime = _currentTime;
    _state = State.RUNNING;
  }

  /**
   * Stop the timer and store the current value.
   */
  public void stop()
  {
    if (_doYield)
      Thread.currentThread().yield();
    if (_state == State.RUNNING)
    {
      _currentTime = getCurrentTime();
      _state = State.STOPPED;
    }
  }

  /**
   * Restart the timer from its current value.
   */
  public void resume()
  {
    long idleTime;
    long resumeTime;

    if (_doYield)
      Thread.currentThread().yield();
    if (_state == State.STOPPED)
    {
      resumeTime = getCurrentTime();
      idleTime = resumeTime - _currentTime;
      _startTime += idleTime;
      _currentTime = resumeTime;
      _state = State.RUNNING;
    }
    else if (_state == State.ZERO)
      start();
  }

  /**
   * Stop the timer and set its value to zero.
   */
  public void reset()
  {
    if (_doYield)
      Thread.currentThread().yield();
    _currentTime = getCurrentTime();
    _startTime = _currentTime;
    _state = State.ZERO;
  }

  /**
   * Return true if the timer is running.
   */
  public boolean isRunning()
  {
    if (_doYield)
      Thread.currentThread().yield();
    if (_state == State.RUNNING)
      return true;
    return false;
  }

  protected long getCurrentTime()
  {
    return 0;
  }

  /**
   * Delay execution for the given amount of time ( in ms ).
   */
  public static void delay(int time)
  {
    try
    {
      Thread.currentThread().sleep(time);
    }
    catch (Exception e)
    {
    }
  }

}
