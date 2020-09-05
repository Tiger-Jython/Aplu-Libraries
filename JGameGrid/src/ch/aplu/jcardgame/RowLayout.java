// RowLayout.java

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
 * Class to store the row layout options for a hand.
 * Displays the cards in a row with given maximal width.
 * If stepDelay > 0, the display is animated and the method blocks until
 * the final position is reached.
 * The cards are drawn one on top others with the current
 * card list order.
 * If the row width is equal or less than the card width, the hand shows like
 * stacked, but uses more memory than a when StackLayout is used.<br><br>
 * For negative rowWidth, the horizontal card distance is fixed to
 * the absolute value of this parameter. This is useful to align lines of cards.
 */
public class RowLayout extends HandLayout
{
  private double rotationAngle = 0;
  private int rowWidth;
  private int stepDelay = 0;
  private Hand.CardAlignment cardAlignment = Hand.CardAlignment.MIDDLE;

  private RowLayout(Location handLocation, int rowWidth, double scaleFactor,
    double rotationAngle, int stepDelay, Hand.CardAlignment cardAlignment)
  {
    this.handLocation = handLocation;
    this.rowWidth = rowWidth;
    this.scaleFactor = scaleFactor;
    this.rotationAngle = rotationAngle;
    this.stepDelay = stepDelay;
    this.cardAlignment = cardAlignment;
  }

  /**
   * Same as RoyLayout(handLocation, rowWidth, rotationAngle) with rotationAngle = 0.
   * Defaults:<br>
   * scaleFactor = 1<br>
   * rotationAngle = 0<br>
   * stepDelay = 0<br>
   * cardAlignment = Hand.CardAlignment.MIDDLE
   * @param handLocation the location of the first, last or middle card, depending
   * on the card alignment.
   * @param rowWidth the width of the row
   */
  public RowLayout(Location handLocation, int rowWidth)
  {
    this(handLocation, rowWidth, 0);
  }

  /**
   * Creates a RowLayout instance with given hand location , maximum row width
   * and rotation angle.
   * For negative rowWidth, the horizontal card <b>distance</b> is fixed to
   * the absolute value of this parameter. This is useful to align lines of cards.<br>
   * Defaults:<br>
   * scaleFactor = 1<br>
   * stepDelay = 0<br>
   * cardAlignment = Hand.CardAlignment.MIDDLE
   * @param handLocation the location of the first, last or middle card, depending
   * on the card alignment.
   * @param rowWidth the width of the row
   * @param rotationAngle the rotion angle (in degrees, clockwise, zero to east)
   */
  public RowLayout(Location handLocation, int rowWidth, double rotationAngle)
  {
    this.handLocation = handLocation;
    this.rowWidth = rowWidth;
    this.rotationAngle = rotationAngle;
  }

  /**
   * Creates a new RowLayout instance with same options.
   * @return the new instance with same options
   */
  public RowLayout clone()
  {
    return new RowLayout(handLocation, rowWidth, scaleFactor,
      rotationAngle, stepDelay, cardAlignment);
  }

  /**
   * Returns the current row width.
   * @return the row width
   */
  public int getRowWidth()
  {
    return rowWidth;
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
   * Sets the current row width to the given width.
   * For negative rowWidth, the horizontal card <b>distance</b> is fixed to
   * the absolute value of this parameter. This is useful to align lines of cards.
   * @param rowWidth the new current row width
   */
  public void setRowWidth(int rowWidth)
  {
    this.rowWidth = rowWidth;
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
