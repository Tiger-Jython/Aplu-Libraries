// GGNavigationAdapter.java

/*
This software is part of the JGameGrid package.
It is Open Source Free Software, so you may
- run the code for any purpose
- study how the code works and adapt it to your needs
- integrate all or parts of the code in your own programs
- redistribute copies of the code
- improve the code and release your improvements to the public
However the use of the code is entirely your responsibility.

Author: Aegidius Pluess, www.aplu.ch
 */
package ch.aplu.jgamegrid;

/**
 * Implementation of empty methods of GGNavigationListener.
 */
public class GGNavigationAdapter implements GGNavigationListener
{
  /**
   * Event callback method called when the run button is hit.
   * @return true, if the event is consumed, so doRun() will not be called.
   */
  public boolean started()
  {
    return false;
  }

  /**
   * Event callback method called when the pause button is hit.
   * @return true, if the event is consumed, so doPause() will not be called.
   */
  public boolean paused()
  {
    return false;
  }

  /**
   * Event callback method called when the step button is hit.
   * @return true, if the event is consumed, so doStep() will not be called.
   */
  public boolean stepped()
  {
    return false;
  }

  /**
   * Event callback method called when the reset button is hit.
   * @return true, if the event is consumed, so doReset() will not be called.
   */
  public boolean resetted()
  {
    return false;
  }

  /**
   * Event callback method called when the simulation period slider is moved.
   * @param simulationPeriod the new value of the simulation period
   * @return true, if the event is consumed, so setSimulationPeriod() will not be called.
   */
  public boolean periodChanged(int simulationPeriod)
  {
    return false;
  }
}
