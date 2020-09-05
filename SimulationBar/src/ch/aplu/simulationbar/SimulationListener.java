// SimulationListener.java

package ch.aplu.simulationbar;


/**
 * Declarations of the notification method called when the simulation bar is used.
 */
public interface SimulationListener extends java.util.EventListener
{

  /**
   * Event callback method called when the run button is hit.
   */
  public void start();

  /**
   * Event callback method called when the pause button is hit.
   */
  public void pause();

  /**
   * Event callback method called when the step button is hit.
   */
  public void step();

  /**
   * Event callback method called when the reset button is hit.
   */
  public void reset();

  /**
   * Event callback method called when the title bar close button is hit.
   */
  public void exit();

  /**
   * Event callback method called when the simulation period slider is moved.
   * @param simulationPeriod the new value of the simulation period
   */
  public void change(int simulationPeriod);

  /**
   * Event callback method called in every simulation cycle.
   */
  public void loop();
}
