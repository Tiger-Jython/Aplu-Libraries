// StackLayout.java

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
 * Class to store the stacked layout options for a hand.
 * Displays the cards in a stacked pile (only the top card is visible).
 */
public class StackLayout extends HandLayout
{
  private double rotationAngle = 0;

  private StackLayout(Location handLocation, double scaleFactor,
    double rotationAngle)
  {
    this.handLocation = handLocation;
    this.scaleFactor = scaleFactor;
    this.rotationAngle = rotationAngle;
  }

  /**
   * Same as StackLayout(handLocation, rotationAngle) with rotationAngle = 0.
   * Defaults:<br>
   * scaleFactor = 1<br>
   * rotationAngle = 0<br>
   * @param handLocation the location of the visible card
   */
  public StackLayout(Location handLocation)
  {
    this(handLocation, 0);
  }

  /**
   * Creates a StackLayout instance with given hand location and rotationAngle.
   * Default:<br>
   * scaleFactor = 1<br>
   * @param handLocation the location of the visible card
   * @param rotationAngle the rotion angle (in degrees, clockwise, zero to east)
   */
  public StackLayout(Location handLocation, double rotationAngle)
  {
    this.handLocation = handLocation;
    this.rotationAngle = rotationAngle;
  }

  /**
   * Returns the current rotation angle.
   * @return the rotation angle (in degrees, clockwise)
   */
  public double getRotationAngle()
  {
    return rotationAngle;
  }

  /**
   * Sets the current rotation angle to the given angle.
   * @param rotationAngle the new current rotation angle (in degrees, clockwise)
   */
  public void setRotationAngle(double rotationAngle)
  {
    this.rotationAngle = rotationAngle;
  }

  /**
   * Creates a new StackLayout instance with same options.
   * @return the new instance with same options
   */
  public StackLayout clone()
  {
    return new StackLayout(handLocation, scaleFactor, rotationAngle);
  }
}
