// HandLayout.java

/*
This software is part of the JCardGame library.
It is Open Source Free Software, so you may
- run the code for any purpose
- study how the code works and adapt it to your needs
- integrate all or parts of the code in your own programs
- redistribute copies of the code
- improve the code and release your improvements to the public
However the use of the code is entirely your responsibility.

Author: Aegidius Pluess, www.aplu.ch
 */
package ch.aplu.jcardgame;

import ch.aplu.jgamegrid.*;

/**
 * Base class of hand layouts.
 */
public abstract class HandLayout
{
  protected Location handLocation;
  protected double scaleFactor = 1;

  /**
   * Returns the current hand location.
   * @return the hand location
   */
  public Location getHandLocation()
  {
    return handLocation.clone();
  }

  /**
   * Returns the current scale factor.
   * @return the scale factor
   */
  public double getScaleFactor()
  {
    return scaleFactor;
  }

  /**
   * Sets the current hand location to the given location.
   * @param handLocation the new current hand location
   */
  public void setHandLocation(Location handLocation)
  {
    this.handLocation = handLocation.clone();
  }

  /**
   * Sets the current scale factor to the given factor.
   * @param scaleFactor the new current scale factor
   */
  public void setScaleFactor(double scaleFactor)
  {
    this.scaleFactor = scaleFactor;
  }
}
