// FanLayout.java

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
 * Class to store the fan layout options for a hand.
 * Displays the cards in a arc with given center, radius, start and
 * maximal end angle.
 * The cards are drawn one on top others with the current
 * card list order. If endDir is less or equal than startDir,
 * the hand shows like stacked, but uses more memory than a when
 * StackedLayout is used.<br><br>
 * For negative endDir, the angle distance between cards is fixed to
 * the absolute value of this parameter. This is useful to align cards.
 */
public class FanLayout extends HandLayout
{
  private Location center;
  private double radius;
  private double startDir;
  private double endDir;
  private int stepDelay = 0;
  private Hand.CardAlignment cardAlignment = Hand.CardAlignment.MIDDLE;

  private FanLayout(Location center, double radius, double startDir, double endDir,
    double scaleFactor, int stepDelay, Hand.CardAlignment cardAlignment)
  {
    this.handLocation = center;
    this.center = center;
    this.radius = radius;
    this.startDir = startDir;
    this.endDir = endDir;
    this.scaleFactor = scaleFactor;
    this.stepDelay = stepDelay;
    this.cardAlignment = cardAlignment;
  }

  /**
   * Creates a FanLayout instance with given center, radius, start direction and
   * end direction.
   * For negative endDir, the angle distance between cards is fixed to
   * the absolute value of this parameter. This is useful to align cards.<br>
   * (Because endDir >= startDir is assumed, fan to east: startDir = 360 - phi,
   * endDir 360 + phi)<br><br>
   * Defaults:<br>
   * rotationAngle = 0<br>
   * stepDelay = 0<br>
   * cardAlignment = Hand.CardAlignment.MIDDLE
   * @param center the center location of the arc
   * @param radius the radius of the arc
   * @param startDir the direction to the center of the first card
   * (in degrees clockwise, zero to east)
   * @param endDir the maximal direction of the last card
   * (in degrees clockwise, zero to east)
   */
  public FanLayout(Location center, double radius, double startDir, double endDir)
  {
    this.center = center;
    this.radius = radius;
    this.startDir = startDir;
    this.endDir = endDir;
  }

  /**
   * Creates a new FanLayout instance with same options.
   * @return the new instance with same options
   */
  public FanLayout clone()
  {
    return new FanLayout(center, radius, startDir, endDir, scaleFactor,
      stepDelay, cardAlignment);
  }

  /**
   * Returns the current center.
   * @return the center
   */
  public Location getCenter()
  {
    return center.clone();
  }

  /**
   * Returns the current radius.
   * @return the hand radius
   */
  public double getRadius()
  {
    return radius;
  }

  /**
   * Returns the current start direction.
   * @return the start direction (in degrees, clockwise, zero to east)
   */
  public double getStartDir()
  {
    return startDir;
  }

  /**
   * Returns the current end direction.
   * @return the end direction (in degrees, clockwise, zero to east)
   */
  public double getEndDir()
  {
    return endDir;
  }

  /**
   * Returns the current step delay. If zero, no animation.
   * @return the step delay (in ms)
   */
  public int getStepDelay()
  {
    return stepDelay;
  }

  /**
   * Returns the current card alignment
   * @return the card alignment
   */
  public Hand.CardAlignment getCardAlignment()
  {
    return cardAlignment;
  }

  /**
   * Sets the current center to the given center
   * @param center the new current center.
   */
  public void setCenter(Location center)
  {
    this.center = center.clone();
  }

  /**
   * Sets the current radius to the given radius
   * @param radius the new current radius.
   */
  public void setRadius(double radius)
  {
    this.radius = radius;
  }

  /**
   * Sets the current start direction to the given start direction
   * @param startDir the new current start direction (in degrees, clockwise, zero to east)
   */
  public void setStartDir(double startDir)
  {
    this.startDir = startDir;
  }

  /**
   * Sets the current end direction to the given end direction
   * For negative endDir, the angle distance between cards is fixed to
   * the absolute value of this parameter. This is useful to align cards.
   * @param endDir the new current end direction (in degrees, clockwise, zero to east)
   */
  public void setEndDir(double endDir)
  {
    this.endDir = endDir;
  }

  /**
   * Sets the current step delay to the given delay (in ms).
   * @param stepDelay the new current step delay
   */
  public void setStepDelay(int stepDelay)
  {
    this.stepDelay = stepDelay;
  }

  /**
   * Sets the current card alignment to the given alignment.
   * @param cardAlignment the new current card alignment
   */
  public void setCardAlignment(Hand.CardAlignment cardAlignment)
  {
    this.cardAlignment = cardAlignment;
  }
}
