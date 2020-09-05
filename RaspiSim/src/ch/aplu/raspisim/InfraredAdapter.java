// InfraredAdapter.java

/*
 This software is part of the RaspiSim library.
 It is Open Source Free Software, so you may
 - run the code for any purpose
 - study how the code works and adapt it to your needs
 - integrate all or parts of the code in your own programs
 - redistribute copies of the code
 - improve the code and release your improvements to the public
 However the use of the code is entirely your responsibility.
 */
package ch.aplu.raspisim;

/**
 * Class with empty callback methods for the infrared sensor.
 */
public class InfraredAdapter implements InfraredListener
{
  /**
   * Empty method called when (reflected) infrared light is newly detected.
   * @param id the id of the light sensor
   */
  public void activated(int id)
  {
  }

  /**
   * Empty method called when no (reflected) infrared light is newly detected.
   * @param id the id of the light sensor
   */
  public void passivated(int id)
  {
  }
}
