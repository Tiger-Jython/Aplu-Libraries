// CardListener.java

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
 * Declaration of callback methods called for card events.
 */
public interface CardListener
{
  /**
   * Triggered when a card is left pressed.
   */
  public void leftPressed(Card card);

  /**
   * Triggered when a card is left released.
   */
  public void leftReleased(Card card);

  /**
   * Triggered when a card is left clicked.
   */
  public void leftClicked(Card card);

  /**
   * Triggered when a card is left double-clicked.
   */
  public void leftDoubleClicked(Card card);

  /**
   * Triggered when a card is right pressed.
   */
  public void rightPressed(Card card);

  /**
   * Triggered when a card is right released.
   */
  public void rightReleased(Card card);

  /**
   * Triggered when a card is right clicked.
   */
  public void rightClicked(Card card);

  /**
   * Triggered when a card is right double-clicked.
   */
  public void rightDoubleClicked(Card card);

  /**
   * Triggered when a card arrives a the target after a card transfer.
   */
  public void atTarget(Card card, Location targetLocation);
}
