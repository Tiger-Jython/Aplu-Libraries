// ColumnLayout.java

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
 * Class to store the column layout options for a hand.
 * Displays the cards in a column with given maximal height.
 * If stepDelay > 0, the spread out is animated and blocks until
 * the final position is reached.
 * The cards are drawn one on top others with the current
 * card list order.
 * If the column height is equal or less than the card height, the hand shows like
 * stacked, but uses more memory than a when StackLayout is used.<br><br>
 * For negative columnHeight, the vertical card distance is fixed to
 * the absolute value of this parameter. This is useful to align lines of cards.
 */
public class ColumnLayout extends HandLayout
{
  private double rotationAngle = 0;
  private int columnHeight;
  private int stepDelay = 0;
  private Hand.CardAlignment cardAlignment = Hand.CardAlignment.MIDDLE;

  private ColumnLayout(Location handLocation, int columnHeight, double scaleFactor,
    double rotationAngle, int stepDelay, Hand.CardAlignment cardAlignment)
  {
    this.handLocation = handLocation;
    this.columnHeight = columnHeight;
    this.scaleFactor = scaleFactor;
    this.rotationAngle = rotationAngle;
    this.stepDelay = stepDelay;
    this.cardAlignment = cardAlignment;
  }

  /**
   * Same as ColumnLayout(handLocation, columnHeight, rotationAngle)
   * with rotationAngle = 0.
   * Defaults:<br>
   * scaleFactor = 1<br>
   * rotationAngle = 0<br>
   * stepDelay = 0<br>
   * cardAlignment = Hand.CardAlignment.MIDDLE
   * @param handLocation the location of the first, last or middle card, depending
   * on the card alignment.
   * @param columnHeight the height of the column
   */
  public ColumnLayout(Location handLocation, int columnHeight)
  {
    this(handLocation, columnHeight, 0);
  }

  /**
   * Creates a ColumnLayout instance with given hand location,
   * maximum column height and rotation angle.
   * For negative columnHeight, the vertical card <b>distance</b> is fixed to
   * the absolute value of this parameter. This is useful to align lines of cards.<br>
   * Defaults:<br>
   * scaleFactor = 1<br>
   * stepDelay = 0<br>
   * cardAlignment = Hand.CardAlignment.MIDDLE
   * @param handLocation the location of the first, last or middle card, depending
   * on the card alignment.
   * @param columnHeight the height of the column
   * @param rotationAngle the rotion angle (in degrees, clockwise, zero to east)
   */
  public ColumnLayout(Location handLocation, int columnHeight, double rotationAngle)
  {
    this.handLocation = handLocation;
    this.columnHeight = columnHeight;
    this.rotationAngle = rotationAngle;
  }

  /**
   * Creates a new ColumnLayout instance with same options.
   * @return the new instance with same options
   */
  public ColumnLayout clone()
  {
    return new ColumnLayout(handLocation, columnHeight, scaleFactor,
      rotationAngle, stepDelay, cardAlignment);
  }

  /**
   * Returns the current column height.
   * @return the column height
   */
  public int getColumnHeight()
  {
    return columnHeight;
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
   * Sets the current column height to the given height.
   * For negative columnHeight, the vertical card <b>distance</b> is fixed to
   * the absolute value of this parameter. This is useful to align lines of cards.
   * @param columnHeight the new current column height
   */
  public void setColumnHeight(int columnHeight)
  {
    this.columnHeight = columnHeight;
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
