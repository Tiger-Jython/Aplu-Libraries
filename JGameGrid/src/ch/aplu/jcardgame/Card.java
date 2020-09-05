// Card.java

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

import java.awt.*;
import ch.aplu.jgamegrid.*;
import java.awt.image.BufferedImage;

/**
 * A class to represent individual playing cards.<br><br>
 * <b>General description of the JCardGame library:</b><br>

The available suits and ranks of the game are defined using enumerations when
creating a deck (instance of class Deck). The deck stores a JGameGrid actor (the
"seed actor") for each suit and rank enumeration value that is used for displaying a
card in a game grid window. But because the card image must often be scaled and
rotated at runtime, the seed actor is never displayed directly, it only serves as store
for the card image (and the card back image, called the card cover).<br><br>

Suits and ranks are ordered. The priority is specified by the the order of the
enumeration values given when the deck is created. The deck class also provides
amethod dealingOut() that returns a user selectable number of card sets taken from
the given suits/ranks, either randomly shuffled or ordered. Cards may also have a
card value (card points) that is dynamically adaptable.<br><br>

A card (instance of class Card) belongs to one of the suits and one of the ranks
defined when a deck is created. Each card contains a link to its seed actor, but the
Card class itself is not derived from the Actor class. To display a card in a game grid
window, the seed actor's image is transformed at runtime with the required scale and
rotation parameters and the current card actor is created with the transformed image.
The card actor is then used as standard JGameGrid actor to show (and eventually
move) the card image in the game grid window. When the scale and rotation
parameters change, the old card actor is removed from the game grid and a new
card actor is created and added to the game grid. It is a good idea to make a
difference between a card as instance of the Card class that remains fixed through
the lifetime of the card and its visual representation in a game grid window using the
card actor that varies depending on the current scaling and rotation (like in the
Model View Controller paradigm). <br><br>

Normally a card is part of a set of cards called a hand and all cards of a hand are
shown together when draw() is called. The properties of the visual arrangement is
defined using 4 layout classes: StackLayout, RowLayout, ColumnLayout and
FanLayout, all derived from the abstract class HandLayout. There are also
"handless" cards that are either not yet inserted into a
hand or removed from a hand. As stated before, the current card actor associated
with the card must be dynamically calculated from its seed actor with the proper
scaling and rotation values when the hand is displayed. Thus the card actor is only
valid when the card is part of a hand and the hand was displayed. (Alternatively the
card's attributeActor() method may be used to define its card actor.) Each card may
also store an individual card value (card points). <br><br>

Hands are very useful card containers, not only for the cards in a player's hand, but
also for modeling card piles of any kind laying on the gambling table. Even when a
hand contains a maximum of one card, putting it into a hand is preferable to an
individual card because moving cards from one hand to another is simple using the
hand's transfer() method.<br><br>

A hand can be empty (and displayed as such without harm). A hand may even serve
as card store without being displayed during the whole game. It is not rare to create
between 10 to 20 hands in a 4-players game application.<br><br>

A hand contains a array list of its cards, called the card list. This list determines the
order the cards are painted in the game grid window (the "paint order"). Cards
painted later are shown on top of other cards. Whenever a hand is drawn, the current
card actor of every card is removed from the game grid, recalculated from its seed
actor and added back to the game grid. This is a somewhat time-consuming
operation and hands should only be redrawn when needed. <br><br>

The Hand class provides many card list transforming operations, like sorting with
several sort types, shuffling, shifting, reversing, etc. When the card list is modified,
the change is not visible until the hand is redisplayed by calling draw(). To simplify
redrawing, all hand modification methods have a boolean parameter that can be set
true to perform redrawing automatically. <br><br>

A card may be transferred from one hand to another using the transfer() method.
The transfer can be animated by a sequence of card positions when the card moves
from the source to the target hand. The transfer operation takes information from a
TargetArea instance that includes the target location, the card orientation and a
slide step (determines the speed). Once the target area parameters are set, all
further transfer operations use them until they are redefined.
 */
public class Card implements Comparable
{
  private Deck deck;
  private Hand myHand = null;
  private CardActor cardActor = null;
  private Enum suit;
  private Enum rank;
  private boolean isVerso;
  private double scaleFactor = 1;
  private double rotationAngle = 0;
  private static Dimension dimension;  // Same dimension for all cards
  /**
  If true; all cards are always shown with the face up (for debugging purposes).
   */
  public static boolean noVerso = false;

  /**
   * Same as Create(deck, suit, rank, isVerso) with isVerso = false.
   * @param <T> the Enum type of the suit
   * @param <R> the Enum type of the rank
   * @param deck the deck where to the the cards seed actor
   * @param suit the card suit
   * @param rank the card rank
   */
  public <T extends Enum<T>, R extends Enum<R>> Card(Deck deck, T suit, R rank)
  {
    this(deck, suit, rank, false);
  }

  /**
   * Creates a card instance from given deck using the given suit and rank.
   * Keep in mind that the current card actor is undefined (null) until the
   * card is displayed in the gamegrid using the hand's draw() method
   * or attributeActor() is called.
   * @param <T> the Enum type of the suit
   * @param <R> the Enum type of the rank
   * @param deck the deck where to the the cards seed actor
   * @param suit the card suit
   * @param rank the card rank
   * @param isVerso if true the card cover (back) will be shown; otherwise the face will be shown
   */
  public <T extends Enum<T>, R extends Enum<R>> Card(Deck deck, T suit, R rank, boolean isVerso)
  {
    this.deck = deck;
    this.suit = suit;
    this.rank = rank;
    this.isVerso = isVerso;
    Actor actor = deck.getSeedActor(suit, rank);
    dimension = new Dimension(actor.getWidth(0), actor.getHeight(0));
  }

  /**
   * Creates a card instance from given deck using the give card number
   * Keep in mind that the current card actor is undefined (null) until the
   * card is displayed in the gamegrid using the hand's draw() method
   * or attributeActor() is called.
   * @param deck the deck where to the the cards seed actor
   * @param cardNb the card number as defined in the Deck class
   * @see Deck#getSuitId(int cardNb)
   * @see Deck#getRankId(int cardNb)
   */
  public Card(Deck deck, int cardNb)
  {
    this(deck, deck.getSuit(deck.getSuitId(cardNb)),
      deck.getRank(deck.getRankId(cardNb)));
  }

  /**
   * Calculates the current card actor with given scale factor and rotation angle
   * from the seed actor taken from the card's deck.
   * @param scaleFactor the scale factor (1: no scaling) applied to the image transformation
   * @param rotationAngle the rotation angle (in degrees, clockwise) applied to the image transformation
   * @return the current card actor (also stored as attribute)
   */
  public CardActor associateActor(double scaleFactor, double rotationAngle)
  {
    this.scaleFactor = scaleFactor;
    this.rotationAngle = rotationAngle;
    Actor actor = deck.getSeedActor(suit, rank);
    BufferedImage bi0 = actor.getScaledImage(0, scaleFactor, rotationAngle);
    BufferedImage bi1 = actor.getScaledImage(1, scaleFactor, rotationAngle);
    cardActor = new CardActor(this, bi0, bi1);
    return cardActor;
  }

  private Card clone(double rotationAngle)
  {
    Card c = new Card(deck, suit, rank, isVerso);
    c.isVerso = isVerso;
    c.scaleFactor = scaleFactor;
    c.rotationAngle = rotationAngle;
    c.associateActor(scaleFactor, rotationAngle);
    return c;
  }

  /**
   * Deep copy of a card with same attributes, including the card actor,
   * but is handless (getHand() returns null).
   * @return a clone of the current card
   */
  public Card clone()
  {
    return clone(rotationAngle);
  }

  /**
   * Same as cloneAndAdd(double rotationAngle) with rotationAngle of
   * current card.
   */
  public Card cloneAndAdd()
  {
    return cloneAndAdd(rotationAngle);
  }

  /**
   * Deep copy of a card with same attributes, including the card actor,
   * but is handless (getHand() returns null). If the current card actor
   * is part of the game grid, the clone card is added to the game grid
   * at the same location. If the card has no associated card actor, the
   * card is simply cloned but not visible.
   * @param rotationAngle the modified rotation angle of the card clone
   * @return a clone of the current card
   */
  public Card cloneAndAdd(double rotationAngle)
  {
    Card card = clone(rotationAngle);
    CardActor ca = getCardActor();
    if (ca != null)
    {
      GameGrid gg = ca.gameGrid;
      if (gg != null)
      {
        Location location = getCardActor().getLocation();
        gg.addActor(card.getCardActor(), location);
      }
    }
    return card;
  }

  /**
   * Returns the card actor reference of the card. The card actor changes
   * if the card is part of a hand the hand's draw() methods
   * are called or when attributeActor() is called. 
   * @return the card actor associated with this card; null, if no actor is
   * yet associated
   */
  public CardActor getCardActor()
  {
    return cardActor;
  }

  /**
   * Returns the card number of the current card.
   * Cards are numbered in rank order from suit to suit in the given
   * suit priority:<br><br>
   * First suit from 0..nbCardsInSuit-1<br><br>
   * Second suit from nbCardsInSuit..2*nbCardsInSuit-1<br><br>
   * etc.
   * @return the card number
   */
  public int getCardNumber()
  {
    return deck.getCardNumber(suit, rank);
  }

  /**
   * Returns the current scale (zoom) factor.
   * @return the scale factor (<1: zoomed-out, >1:  zoomed-in)
   */
  public double getScaleFactor()
  {
    return scaleFactor;
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
   * Returns the current card dimension (scaling accounted).
   * @return the scaled card dimension
   */
  public Dimension getDimension()
  {
    return new Dimension((int)(scaleFactor * dimension.width),
      (int)(scaleFactor * dimension.height));
  }

  /**
   * Returns the deck reference attributed to this card.
   * @return the deck the card belongs to
   */
  public Deck getDeck()
  {
    return deck;
  }

  /**
   * Returns the card's suit.
   * @return the suit of the card
   */
  public Enum getSuit()
  {
    return suit;
  }

  /**
   * Returns the card's suit id.
   * @return the suit id of the card
   */
  public int getSuitId()
  {
    return deck.getSuitId(suit);
  }

  /**
   * Returns the card's rank
   * @return the rank of the card
   */
  public Enum getRank()
  {
    return rank;
  }

  /**
   * Returns the card's rank id.
   * @return the rank id of the card
   */
  public int getRankId()
  {
    return deck.getRankId(rank);
  }

  /**
   * Implementation of comparable interface. Compares suits and ranks in the order
   * defined in the SortType enumeration.
   */
  public int compareTo(Object other)
  {
    // return  1, if my card rank is less than other card
    // return -1, if my card rank is greater than other card
    // return  0, if same rank
    Card otherCard = (Card)other;
    int otherSuitId = deck.getSuitId(otherCard.getSuit());
    int otherRankId = deck.getRankId(otherCard.getRank());
    int mySuitId = deck.getSuitId(suit);
    int myRankId = deck.getRankId(rank);

    switch (myHand.getSortType())
    {
      case SUITPRIORITY:  // Suits order and then ranks order
        if (mySuitId > otherSuitId)
          return 1;
        if (mySuitId < otherSuitId)
          return -1;

        // Same suit
        if (myRankId > otherRankId)
          return 1;
        if (myRankId < otherRankId)
          return -1;
        return 0;  // equal

      case RANKPRIORITY:  // Rank order and then suit order
        if (myRankId > otherRankId)
          return 1;
        if (myRankId < otherRankId)
          return -1;

        // Same rank
        if (mySuitId > otherSuitId)
          return 1;
        if (mySuitId < otherSuitId)
          return -1;
        return 0;  // equal

      case POINTPRIORITY:
        if (getValue() > ((Card)other).getValue())
          return 1;
        if (getValue() < ((Card)other).getValue())
          return -1;
        return 0;
    }
    return 0;
  }

  /**
   * Returns true, if the card cover (back) will be shown.
   * @return true, if the card cover (back) is active (sprite Id = 1);
   * otherwise the card face is active (sprite Id = 0)
   */
  public boolean isVerso()
  {
    return isVerso;
  }

  /**
   * Determines if the card's face or cover (back) will be shown. If the card
   * is visible, it is immediately shown in the requested state.
   * @param isVerso the visibilty used for the card; if true, the card cover (back)
   * is active (sprite Id = 1); otherwise the card face is active (sprite Id = 0)
   */
  public void setVerso(boolean isVerso)
  {
    if (Card.noVerso)
      return;

    this.isVerso = isVerso;
    if (cardActor != null)
      cardActor.setVerso(isVerso);
  }

  /**
   * Returns a string representation in the format "suit-rank".
   * @return a string representation of the card
   */
  public String toString()
  {
    return suit + "-" + rank;
  }

  /**
   * Returns true, if the current card is part of the given hand.
   * @return true, if hand is part of hand's card list; otherwise false
   */
  public boolean isInHand(Hand hand)
  {
    if (this.myHand == null)
      return false;
    return hand.contains(this);
  }

  /**
   * Returns the value of the card.
   * @return the card value
   */
  public int getValue()
  {
    int[] values = deck.getCardValues(suit);
    if (values == null)
      return 0;
    return values[deck.getRankId(rank)];
  }

  /**
   * Removes the card from its hand. Nothing happens if the card is handless.
   * The card is removed from the hand's card list, but the
   * the modification becomes only visible in a displayed hand,
   * if redraw = true (or the hand is drawn or redrawn manually).
   * @param redraw if true, a redraw is automatically done;
   * otherwise redraw is not invoked
   */
  public void removeFromHand(boolean redraw)
  {
    if (myHand != null)
      myHand.remove(this, redraw);
  }

  /**
   * Same as slideToTarget(targetLocation, slideStep, blocking) with blocking = true.
   * @param targetLocation the location where the card should arrive
   * @param slideStep the number of steps moved in one cycle
   */
  public void slideToTarget(Location targetLocation, int slideStep)
  {
    slideToTarget(targetLocation, slideStep, true);
  }

  /**
   * If the card is added to the GameGrid, handless and visible, moves 
   * the card actor from current location to
   * the given location using the given number of steps per GameGrid's
   * simulation cycle. If the card is part of a hand, nothing happens (use
   * transfer() instead). If the card is not part of the GameGrid, an error
   * message is generated. Be aware that the paint order (which actor is
   * shown on top) is given by the time the actors are added to the game grid.
   * When a hand is drawn or redrawn, all card actors of the hand are reconstructed.
   * Thus if the handless card is added later than the cards of a hand,
   * it moves over the hand card's images. To make it move under the hand, redraw the
   * hand after the handless card is added.
   * @param targetLocation the location where the card should arrive
   * @param slideStep the number of steps moved in one cycle
   * @param blocking if true, the methods blocks until the card arrives at the
   * target; otherwise the method returns immediately
   */
  public void slideToTarget(Location targetLocation, int slideStep,
    boolean blocking)
  {
    if (cardActor.gameGrid == null)
      Deck.fail("Error when calling Card.slideToTarget()."
        + "\nCard actor is not part of the game grid."
        + "\nApplication will terminate.");

    if (myHand == null && cardActor != null && cardActor.isVisible())
      cardActor.slideToTarget(targetLocation, slideStep, blocking);
  }

 
  /**
   * Animated or non-animated transfer from current hand to new hand using the
   * currently defined target area. If no target area is defined, the target
   * hand location (that is defined, when the hand is drawn)
   * is used as target location and the card transfer is animated.<br><br>
   *
   * For more advanced transferring option, define a target area with appropriate
   * parameters.<br><br>
   * 
   * The method blocks until the card arrives at the target.
   * Arrived at the target, an atTarget() notification is triggered. <br><br>
   * If the card is handless, nothing happens. (use slideToTarget() instead).
   * See Hand.transfer() more more information.
   * @param targetHand the hand where to move the card
   * @param doDraw if true, the hand the card belongs to and the target hand are drawn
   * (stacked arrangments are always drawn)
   * @see Hand#transfer(Card card, Hand targetHand, boolean blocking, boolean redraw)
   * @see TargetArea
   */
  public void transfer(Hand targetHand, boolean doDraw)
  {
    if (myHand != null)
      myHand.transfer(this, targetHand, doDraw);
  }
  
   /**
   * Same as transfer(targetHand, doDraw), but
   * the methods returns immediately. Use the atTarget callback to get
   * a notification when the card arrived at its destination.
   * @param targetHand the hand where to transfer the card
   * @param doDraw if true, the hand the card belongs to and the target hand are drawn
   * (stacked arrangments are always drawn)
   */
  public void transferNonBlocking(Hand targetHand, boolean doDraw)
  {
    if (myHand != null)
      myHand.transferNonBlocking(this, targetHand, doDraw);
  }
  
  /**
   * Checks if the given card has the same suit and rank as the current card
   * (overrides Object.equals()).
   * @param obj the object whose suit and rank is checked
   * @return true, if the given object is of class Card and 
   * has the same suit and rank as the current card; otherwise false
   */
  public boolean equals(Object obj)
  {
    if (obj == null)
      return false;
    if (obj == this)
      return true;
    if (getClass() != obj.getClass())
      return false;
    Card card = (Card)obj;
    if (suit.equals(card.getSuit())
      && rank.equals(card.getRank()))
      return true;
    return false;
  }
  
    /**
   * Returns a hash code value for the object. 
   * In accordance with the general contract for hashCode().
   * (Code from NetBeans IDE auto generate)
   */
  public int hashCode()
  {
    int hash = 3;
    hash = 11 * hash + (this.suit != null ? this.suit.hashCode() : 0);
    hash = 11 * hash + (this.rank != null ? this.rank.hashCode() : 0);
    return hash;
  }
  
  protected void setHand(Hand hand)
  {
    myHand = hand;
  }

  /**
   * Returns the hand the card belongs to. If the card is handless, returns null.
   * @return the owner hand of the card; null, if card is handless
   */
  public Hand getHand()
  {
    return myHand;
  }

  
}
