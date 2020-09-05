// CardActor.java

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
import java.awt.image.BufferedImage;

/**
 * Class that represents the game grid actor that belongs to the sprite image
 * of a card.<br><br>
 * <b>You find a general description of the JCardGame library in the Card
 * class documentation.</b><br><br>
 * @see Card
 */
public class CardActor extends Actor
{
  private Card card;
  private boolean atTarget = false;
  private Location targetLocation;
  private boolean mustFinish = false;
  private Hand myHand;
  private Hand targetHand;
  private Card myCard;
  private boolean isTouchEnabled;
  private boolean doDraw;
  private double _xLoc, _yLoc;
  private double _direction;
  private int _nbSteps;
  private int _totNbStepsRounded;
  private double _slideStep;

    protected CardActor(Card card, BufferedImage bi0, BufferedImage bi1)
  {
    super(bi0, bi1);
    setActEnabled(false);
    this.card = card;
  }

  /**
   * Return the card the current card actor belongs to.
   * @return the card to which belongs the card actor.
   */
  public Card getCard()
  {
    return card;
  }

  
  protected void slideToTarget(Location targetLocation, int slideStep,
    boolean blocking)
  {
    if (slideStep <= 0)
    {
      atTarget = true;
      return;
    }

    _nbSteps = 0;
    _xLoc = super.getX();
    _yLoc = super.getY();
    double _xTarget = targetLocation.x;
    double _yTarget = targetLocation.y;
    _direction = Math.atan2((_yTarget - _yLoc), (_xTarget - _xLoc));
    _slideStep = slideStep;
    double _distance = Math.sqrt((_yTarget - _yLoc) * (_yTarget - _yLoc)
      + (_xTarget - _xLoc) * (_xTarget - _xLoc));
    double _totNbSteps = _distance / _slideStep;
    _totNbStepsRounded = (int)Math.ceil(_totNbSteps);
    _slideStep = _distance / _totNbStepsRounded; // adjust

    this.targetLocation = targetLocation;
    atTarget = false;
    setActEnabled(true);
    if (blocking)
    {
      Thread transferBlocker = new Thread()
      {
        public void run()
        {
          while (isActEnabled() && !GameGrid.isDisposed())
            GameGrid.delay(100);
        }

      };
      transferBlocker.start();
      try
      {
        transferBlocker.join();
      }
      catch (InterruptedException ex)
      {
      }
    }
  }

  /**
   * For internal use only. Overrides Actor.act().
   */
  public void act()
  {
    super.setLocation(new Location(
      (int)(_xLoc + _slideStep * _nbSteps * Math.cos(_direction)),
      (int)(_yLoc + _slideStep * _nbSteps * Math.sin(_direction))));


    if (_nbSteps > _totNbStepsRounded)
    {
      setLocation(targetLocation);
      atTarget = true;
      setActEnabled(false);
      if (mustFinish)
      {
        mustFinish = false;
        myHand.finishTransfer(targetHand, myCard, isTouchEnabled, doDraw);
      }
    }

    _nbSteps++;
  }
  
  protected void setFinishTransfer(Hand myHand, Hand targetHand, Card myCard,
    boolean isTouchEnabled,  boolean doDraw)
  {
    mustFinish = true;
    this.myHand = myHand;
    this.targetHand = targetHand;
    this.myCard = myCard;
    this.isTouchEnabled = isTouchEnabled;
    this.doDraw = doDraw;
  }

  protected void setVerso(boolean isVerso)
  {
    show(isVerso ? 1 : 0);
  }

  protected boolean isAtTarget()
  {
    return atTarget;
  }
}
