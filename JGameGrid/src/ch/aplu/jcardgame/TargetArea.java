// TargetArea.java

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
 * Class to represent the target area and moving options for card transfers.
 */
public class TargetArea
{
  private Location targetLocation = null;
  private CardOrientation cardOrientation;
  private int slideStep;
  private boolean onTop;

  /**
   * Sets the attributes for transferring cards. The
   * cards are moved to the given target location (may be anywhere, not necessarily
   * at the target hand location). During the transfer, the given card orientation
   * is used.
   * Defaults:<br>
   * cardOrientation = CardOrientation.NORTH<br>
   * slideStep = 10 (animated transfer)<br>
   * onTop = true (move on top of other actors)
   * @param targetLocation the location where the cards are transferred
   */
  public TargetArea(Location targetLocation)
  {
    this(targetLocation, CardOrientation.NORTH, 10, true);
  }

  /**
   * Sets the attributes for transferring cards. The
   * cards are moved to the given target location (may be anywhere, not necessarily
   * at the target hand location). During the transfer, the given card orientation
   * is used.
   * @param targetLocation the location where the cards are transferred
   * @param cardOrientation the card orientation used for the transfer
   * @param slideStep the distance the card moves at every game grid simulation cycle (>= 1)
   * @param onTop if true, the card moves over all other actors; otherwise it moves under
   * other actors
   */
  public TargetArea(Location targetLocation, CardOrientation cardOrientation,
    int slideStep, boolean onTop)
  {
    this.targetLocation = targetLocation.clone();
    this.cardOrientation = cardOrientation;
    this.slideStep = slideStep;
    this.onTop = onTop;
  }

  /**
   * Performs a deep copy of the given TargetArea.
   * @return a new TargetArea reference with the same attributes
   */
  public TargetArea clone()
  {
    return new TargetArea(targetLocation.clone(), cardOrientation, slideStep, onTop);
  }

  /**
   * Returns a clone of the current target location.
   * @return the target location
   */
  public Location getTargetLocation()
  {
    return targetLocation.clone();
  }

  /**
   * Returns the current card orientation.
   * @return the card orientation
   */
  public CardOrientation getCardOrientation()
  {
    return cardOrientation;
  }

  /**
   * Returns the curren slide step (steps per simulation cycle to move the card in an
   * animated transfer).
   * @return the slide step
   */
  public int getSlideStep()
  {
    return slideStep;
  }

  /**
   * Returns the current state of set onTop flag.
   * @return the onTop flag
   */
  public boolean isOnTop()
  {
    return onTop;
  }

  /**
   * Sets the target location to the given location.
   * @param targetLocation the new target location
   */
  public void setTargetLocation(Location targetLocation)
  {
    this.targetLocation = targetLocation.clone();
  }

  /**
   * Sets the card orientation to the given orientation.
   * @param cardOrientation the new card orientation
   */
  public void setCardOrientation(CardOrientation cardOrientation)
  {
    this.cardOrientation = cardOrientation;
  }

  /**
   * Sets the slide step to the given step
   * @param slideStep the new slide step (steps per simulation cycle to
   * move the card in an animated transfer).
   */
  public void setSlideStep(int slideStep)
  {
    this.slideStep = slideStep;
  }

  /**
   * Sets/resets the onTop flag.
   * @param enable if true, the card moves over all other actors in
   * an animated transfer; otherwise it moves below other actors
   */
  public void setOnTop(boolean enable)
  {
    onTop = enable;
  }
}
