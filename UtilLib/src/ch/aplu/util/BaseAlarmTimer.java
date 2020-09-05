// BaseAlarmTimer.java

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

import javax.swing.JOptionPane;

/**
 * Abstract base class for HiResAlarmTimer and LoResAlarmTimer.
 * See HiResAlarmTimer or LoResAlarmTimer for more details.
 */
abstract public class BaseAlarmTimer
{
  private class EventTimer extends Thread
  {
    int n = 0;

    public void run()
    {
      while (!_stopThread)
      {
        if (!isRunning() && _isArmed)
        {
          _isArmed = false;
          if (_timerListener != null)
          {
            boolean doRestart = _timerListener.timeElapsed();  // Call callback method
            if (doRestart)
              _start();
          }
        }
        Console.delay(1);  // Polling interval
      }
    }

  }

  protected BaseTimer _timer;
  private long _alarmTime;
  private boolean _isArmed = false;
  private boolean _stopThread = false;
  private boolean _doYield = true;
  private final double relError = 0.01;  // Error admitted to stop timer
  private TimerListener _timerListener = null;

  protected BaseAlarmTimer(BaseTimer timer, long alarmTime)
  {
    this(timer, alarmTime, false);
  }

  protected BaseAlarmTimer(BaseTimer timer, long alarmTime, boolean autostart)
  {
    _timer = timer;
    _alarmTime = alarmTime;
    if (autostart)
      start();
  }

  protected BaseAlarmTimer(BaseTimer timer, long alarmTime, boolean autostart, boolean yield)
  {
    this(timer, alarmTime, autostart);
    _doYield = yield;
  }

  /**
   * Return the current time in microseconds (us).
   */
  public long getTime()
  {
    if (_doYield)
      Thread.currentThread().yield();
    if (_timer._state == BaseTimer.State.ALARM)
      return 0;
    if (_timer.getTime() >= _alarmTime)
    {
      _timer._state = BaseTimer.State.ALARM;
      return 0;
    }
    return (_alarmTime - _timer.getTime());
  }

  /**
   * Set the timer to the preset alarm time and start it. The timer runs backward.
   */
  public void start()
  {
    if (_doYield)
      Thread.currentThread().yield();
    stop();
    _isArmed = true;
    _timer.start();
  }

  /**
   * Set the timer to the given preset alarm time and start it. The timer runs backward.
   */
  public void start(long alarmTime)
  {
    _alarmTime = alarmTime;
    start();
  }

  private void _start()
  // To distinguish from Thread.start()
  {
    start();
  }

  /**
   * Stop the timer and store the current time.
   */
  public void stop()
  {
    if (_doYield)
      Thread.currentThread().yield();
    if (_timer.getTime() >= _alarmTime)
      _timer._state = BaseTimer.State.ALARM;
    else
    {
      _isArmed = false;
      _timer.stop();
    }
  }

  /**
   * Restart the timer from its current time. The timer runs backward.
   */
  public void resume()
  {
    if (_doYield)
      Thread.currentThread().yield();
    if (_timer._state != BaseTimer.State.ALARM)
    {
      _timer.resume();
      _isArmed = true; // Former bug fixed: must do it AFTER resume()
    }
  }

  /**
   * Return true if the timer is running (current time > 0 and not stopped).
   */
  public boolean isRunning()
  {
    if (_doYield)
      Thread.currentThread().yield();
    if (_timer._state == BaseTimer.State.RUNNING)
      if (_timer.getTime() > (1 - relError) * _alarmTime)
      {
        _timer._state = BaseTimer.State.ALARM;
        return false;
      }
      else
        return true;
    return false;
  }

  /**
   * Register a TimerListener to fire callbacks when the time interval
   * expires (current time reaches 0 or the timer is stopped).
   * The accuracy of the event is about 1 ms, but may vary due to
   * multitasking/multithreading of the operation system and Java.
   * The preset time of the AlarmTimer must exceed 10 ms.
   * Be aware that the callback method must return before the preset time has elapsed.
   * If timerListener = null, the internal thread of a previously registered
   * timer listener is canceled.
   */
  public void addTimerListener(TimerListener timerListener)
  {
    if (timerListener == null)
    {
      if (_timerListener != null)
        _stopThread = true;
      _timerListener = null;
      return;
    }

    if (_alarmTime < 10000)
    {
      JOptionPane.showMessageDialog(null, "Preset time of AlarmTimer must exceed 10 ms");
      return;
    }
    EventTimer _eventTimer = new EventTimer();
    _timerListener = timerListener;
    _eventTimer.start();
  }

  /**
   * Force to stop the callback calls by terminating the timer's polling thread.
   */
  public void stopEvents()
  {
    _stopThread = true;
  }

}
