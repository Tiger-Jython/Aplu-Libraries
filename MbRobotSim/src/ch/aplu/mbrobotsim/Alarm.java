// Alarm.java

package ch.aplu.mbrobotsim;


public class Alarm
{

  class AlarmThread extends Thread
  {
    private boolean isRunning;

    public AlarmThread()
    {
      isRunning = true;
      setDaemon(true);
      start();
    }

    public void stopAlarmThread()
    {
      isRunning = false;
      try
      {
        join(1000);
      }
      catch (InterruptedException ex)
      {
      }
    }

    public void run()
    {
      while (isRunning)
      {
        robot.playTone(volume, frequency, 100, true);
        delay(80);
        robot.playTone(volume, frequency, 100, true);
        if (isRunning)
          delay(300);
      }
    }

    void delay(int time)
    {
      try
      {
        Thread.sleep(time);
      }
      catch (InterruptedException ex)
      {
      }
    }
  }

  private MbRobot robot;
  private int frequency;
  private int volume;
  private AlarmThread alarmThread = null;

  public Alarm(MbRobot robot, int frequency, int volume)
  {
    this.robot = robot;
    this.frequency = frequency;
    this.volume = volume;
  }

  public void start()
  {
    if (alarmThread == null)
      alarmThread = new AlarmThread();
  }

  public void stop()
  {
    if (alarmThread != null)
    {  
      alarmThread.stopAlarmThread();
      alarmThread = null;
    }
  }
}
