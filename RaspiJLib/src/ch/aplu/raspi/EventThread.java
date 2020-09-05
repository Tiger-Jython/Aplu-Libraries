// EventThread.java

/*
 This software is part of the RaspiJLib library.
 It is Open Source Free Software, so you may
 - run the code for any purpose
 - study how the code works and adapt it to your needs
 - integrate all or parts of the code in your own programs
 - redistribute copies of the code
 - improve the code and release your improvements to the public
 However the use of the code is entirely your responsibility.
 */
package ch.aplu.raspi;

import java.util.ArrayList;

class EventThread extends Thread
{
  private ArrayList<Part> sensors = new ArrayList<Part>();
  private boolean isSensorThreadRunning;
  private int pollDelay;
  private int slowDown;

  public EventThread()
  {
    RaspiProperties props = Robot.getProperties();
    pollDelay = props.getIntValue("EventPollDelay");
    slowDown = props.getIntValue("UltrasonicSlowDown");
    isSensorThreadRunning = true;
  }

  public void run()
  {
//    System.out.println("EventThread starting");
    int pollCount = 0;
    while (isSensorThreadRunning)
    {
      for (Part sensor : sensors)
      {
        // ------------- LightSensor -------------------
        int v;
        LightSensor ls;
        if (sensor instanceof LightSensor)
        {
          ls = (LightSensor)sensor;
          v = ls.getValue();
          if (v > ls.triggerLevel && ls.sensorState == LightSensor.SensorState.DARK)
          {
            ls.sensorState = LightSensor.SensorState.BRIGHT;
            ls.lightListener.bright(ls.id, v);
          }
          if (v <= ls.triggerLevel && ls.sensorState == LightSensor.SensorState.BRIGHT)
          {
            ls.sensorState = LightSensor.SensorState.DARK;
            ls.lightListener.dark(ls.id, v);
          }
        }
        // ------------- InfraredSensor ----------------
        InfraredSensor irs;
        if (sensor instanceof InfraredSensor)
        {
          irs = (InfraredSensor)sensor;
          v = irs.getValue();
          if (v == 1 && irs.sensorState == InfraredSensor.SensorState.PASSIVATED)
          {
            irs.sensorState = InfraredSensor.SensorState.ACTIVATED;
            irs.infraredListener.activated(irs.id);
          }
          if (v == 0 && irs.sensorState == InfraredSensor.SensorState.ACTIVATED)
          {
            irs.sensorState = InfraredSensor.SensorState.PASSIVATED;
            irs.infraredListener.passivated(irs.id);
          }
        }
        // ------------- UltrasonicSensor --------------
        double d;
        UltrasonicSensor us;
        if (sensor instanceof UltrasonicSensor && pollCount % slowDown == 0)
        {
          us = (UltrasonicSensor)sensor;
          d = us.getValue();
          if (d > us.triggerLevel && us.sensorState == UltrasonicSensor.SensorState.NEAR)
          {
            us.sensorState = UltrasonicSensor.SensorState.FAR;
            us.ultrasonicListener.far(d);
          }
          if (d <= us.triggerLevel && us.sensorState == UltrasonicSensor.SensorState.FAR)
          {
            us.sensorState = UltrasonicSensor.SensorState.NEAR;
            us.ultrasonicListener.near(d);
          }
        }
      }
      Tools.delay(pollDelay);
      pollCount++;
    }
//    System.out.println("EventThread terminated");
  }

  protected void terminate()
  {
    isSensorThreadRunning = false;
  }

  protected void add(Part part)
  {
    sensors.add(part);
  }
}
