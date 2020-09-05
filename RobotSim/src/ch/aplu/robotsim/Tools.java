// Tools.java

/*
 This software is part of the RobotSim library.
 It is Open Source Free Software, so you may
 - run the code for any purpose
 - study how the code works and adapt it to your needs
 - integrate all or parts of the code in your own programs
 - redistribute copies of the code
 - improve the code and release your improvements to the public
 However the use of the code is entirely your responsibility.

 Author: Aegidius Pluess, www.aplu.ch
 */
package ch.aplu.robotsim;

import ch.aplu.jgamegrid.*;

/**
 * Some useful helper methods.
 */
public class Tools
{

  private static long startTime = 0L;

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
   * Suspends execution of the current thread for the given amount of time (unless
   * the game grid window is disposed).
   * @param duration the duration (in ms)
   */
  public static void delay(int duration)
  {
    if (GameGrid.isDisposed())
      return;
    LegoRobot r = RobotInstance.getRobot();
    int simulationPeriod = (int)r.getRobot().gameGrid.simulationPeriod;
    if (duration < simulationPeriod)
    {
      _delay(duration);
      return;
    }
    int start = r.getRobot().nbCycles;
    int nbCycles = (int)(1.0 * duration / simulationPeriod + 0.5);
    while (r.getRobot().nbCycles - start < nbCycles && !GameGrid.isDisposed())
      _delay(1);
  }

  private static void _delay(long time)
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
