// CardCover.java

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
 * Class that represents the cover (back) of a card (card with face down).
 * This class derives from Actor and can be used as such. Be careful when overriding
 * the act() method because it is used for the slide animation.
 */
public class CardCover extends Actor
{
  private GameGrid gameGrid;
  private boolean atTarget = false;
  private double movingDirection;
  private Location targetLocation;
  private int slideStep;

  /**
   * Same as CardCover(gameGrid, location, deck, scaleFactor, rotationAngle, hide)
   * with show = true.
   * @param gameGrid the GameGrid reference
   * @param location the location where the actor is shown
   * @param deck the deck where to take the seed actor
   * @param scaleFactor the scale factor (1: no scaling) applied to seed actor
   * @param rotationAngle the rotation angle (in degrees, clockwise) applied
   * to the seed actor
   */
  public CardCover(GameGrid gameGrid, Location location, Deck deck,
    double scaleFactor, double rotationAngle)
  {
    this(gameGrid, location, deck, scaleFactor, rotationAngle, true);
  }

  /**
   * Creates a transformed card cover actor from the seed actor of the
   * first card in the given deck and adds it at the given location to
   * the given game grid.  The game grid is not refreshed automatically.
   * @param gameGrid the GameGrid reference
   * @param location the location where the actor is shown
   * @param deck the deck where to take the seed actor
   * @param scaleFactor the scale factor (1: no scaling) applied to seed actor
   * @param rotationAngle the rotation angle (in degrees, clockwise) applied
   * to the seed actor
   * @param show if true, the actor is shown; otherwise it is hidden (use Actor.show()
   * or slideToTarget() to show it)
   */
  public CardCover(GameGrid gameGrid, Location location, Deck deck,
    double scaleFactor, double rotationAngle, boolean show)
  {
    super(deck.getSeedActor((deck.getNumberOfSuits() == 0 ? null : deck.getSuits()[0]),
      (deck.getNumberOfRanks() == 0 ? null : deck.getRanks()[0])).
      getScaledImage(1, scaleFactor, rotationAngle));
    this.gameGrid = gameGrid;
    setActEnabled(false);
    if (!show)
      hide();
    gameGrid.addActorNoRefresh(this, location);
  }

  /**
   * Slides the card cover actor from the current location
   * to the given target location using the given step. The actor is not
   * removed at the target location (use removeSelf() if needed).
   * @param targetLocation the location where the movement ends
   * @param slideStep the number of moving steps in one simulation cycle;
   * if zero, the actor jumps immediately to the target location
   * @param onTop if true, the actor is shown on top of any other card actors;
   * otherwise it is shown below other card actors and card covers.
   * @param blocking if true, the methods blocks until the actor arrives at the
   * target; otherwise the method returns immediately
   */
  public void slideToTarget(Location targetLocation, int slideStep,
    boolean onTop, boolean blocking)
  {
    putOnTop(onTop);
    show();
    if (slideStep <= 0)
    {
      setLocation(targetLocation);
      return;
    }

    this.targetLocation = targetLocation;
    this.slideStep = slideStep;
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
    movingDirection = getLocation().getDirectionTo(targetLocation);
    setDirection(movingDirection);
    move(slideStep);
    if (getLocation().getDistanceTo(targetLocation) < 2 * slideStep)
    {
      setLocation(targetLocation);
      atTarget = true;
      setActEnabled(false);
    }
  }

  /**
   * Sets the card cover actor above or below any other card actors and card covers.
   * @param onTop if true, the actor is shown on top; otherwise it is
   * shown at bottom (the paint order is modified).
   */
  public void putOnTop(boolean onTop)
  {
    if (onTop)
    {
      gameGrid.setPaintOrder(CardCover.class, CardActor.class);
      gameGrid.setActorOnTop(this);
    }
    else
    {
      gameGrid.setPaintOrder(CardActor.class, CardCover.class);
      gameGrid.setActorOnBottom(this);
    }
  }

}
