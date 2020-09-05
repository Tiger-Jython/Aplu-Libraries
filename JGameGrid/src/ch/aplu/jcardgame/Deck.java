// Deck.java

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
import java.util.*;
import java.awt.*;
import javax.swing.JOptionPane;

/**
 * A class to bundle information about the card suits and ranks and their
 * card actors (sprite images).<br><br>
 * <b>You find a general description of the JCardGame library in the Card
 * class documentation.<br><br></b>
 *
 * A deck maintains a store of available cards and contains the information
 * about available card suits and card ranks as enumerations. Each card may have
 * an individual integer card value called card point. Suits and ranks are ordered
 * in a priority list defined by the order of enums when the deck is created.
 * Each card is linked to a JGameGrid actor that uses two sprite images,
 * one for the face and one for the cover (back). Each sprite image should have
 * the same standard dimension. The scaling and rotation of card images is
 * done dynamically when the cards are drawn in the game grid window.<br><br>
 * The sprite images are loaded from disk files in the subdirectory "sprites"
 * using the enum string values of the suits with an increasing number appended
 * starting from 0. <br>
 * For instance<br><br>
 * <code>
 * enum Suit<br>
 * {<br>
 * &nbsp;&nbsp;SPADES, HEARTS, DIAMONDS, CLUBS<br>
 * }<br>
 *<br>
 * enum Rank<br>
 * {<br>
 * &nbsp;&nbsp;ACE, KING, QUEEN, JACK, TEN, NINE, EIGHT, SEVEN, SIX<br>
 * }</code><br><br>
 * the disk files<br><br>
 * <code>
 * &nbsp;&nbsp;sprites/clubs0.gif for Clubs-Ace, sprites/clubs1.gif for Clubs-King, etc. (all lowercase)
 *</code>
 *<br><br>
 * will be used. Another set of sprites images can be loaded by providing a
 * sprites enumeration in the deck constructor. E.g. If the following
 * enumeration is passed to the constructor<br><br>
 * <code>
 * enum Sprites<br>
 * {<br>
 * &nbsp;&nbsp;A, B, C, D<br>
 * }</code><br><br>
 * the sprite images are loaded from<br><br>
 * <code>
 * &nbsp;&nbsp;sprites/a0.gif for Clubs-Ace, sprites/a1.gif for Clubs-King, etc. (all lowercase)
 *</code>
 *<br><br>
 * but the card suits and ranks names remain unchanged.
 *<br><br>
 * @see Card
 */
public class Deck
{
  /**
   * Interface for the construction of integer arrays with card values (card points,
   * card values) of each card in each suit. To setup the card values,
   * implement this interface by defining your own values() method.
   * Pass the reference to the deck constructor.
   * The values() method may return different values during program execution
   * to reflect the change of card values.
   */
  public interface CardValues
  {
    /**
     * Returns the card values (card points) for cards of all ranks in a given suit.
     * The array size should correspond to the number of cards in a suit.
     * @param suit the suit where to determine the card values
     * @return the card values (card points) of all cards in this suit in the order
     * the rank enum defined
     */
    int[] values(Enum suit);
  }
  //
  private Actor[][] actors;
  private String[] suitNames;
  private String[] rankNames;
  private String[] spriteNames;
  private int nbSuits;
  private int nbCardsInSuit;
  private int nbCards;
  private Enum[] suits;
  private Enum[] ranks;
  private Enum[] sprites;
  private CardValues cardValues;
  private String cover;
  
  /**
   * Public array of all cards in this deck in the order cards[suitId][rankId].
   */
  public Card[][] cards;

  /**
   * Creates a deck from given suit and rank enumerations with card values set to
   * zero using the values of the suits enumeration for the sprite names.
   * @param <T> the Enum type of the suit
   * @param <R> the Enum type of the rank
   * @param suits an array of suits
   * @param ranks an array of ranks
   * @param cover the partial filename of the card cover sprite image. The file
   * must reside in the sprites subdirectory and have the extension .gif
   */
  public <T extends Enum<T>, R extends Enum<R>> Deck(T[] suits, R[] ranks, String cover)
  {
    this(suits, ranks, cover, null);
  }

  /**
   * Creates a deck from given suit and rank enumerations with given card values
   * using the values of the suits enumeration for the sprite names.
   * @param <T> the Enum type of the suit
   * @param <R> the Enum type of the rank
   * @param suits an array of suits
   * @param ranks an array of ranks
   * @param cover the partial filename of the card cover sprite image. The file
   * must reside in the sprites subdirectory and have the extension .gif
   * @param cardValues the integer array of card values for each suit; if null
   * is passed, all card values are set to zero
   */
  public <T extends Enum<T>, R extends Enum<R>> Deck(T[] suits, R[] ranks,
    String cover, CardValues cardValues)
  {
    this(suits, ranks, null, cover, cardValues);
  }

  /**
   * Creates a deck from given suit and rank and sprites enumerations
   * with card values set to zero. The size of the suit and the
   * sprites enumerations must be equal.
   * @param <T> the Enum type of the suit
   * @param <R> the Enum type of the rank
   * @param <S> the Enum type of the sprites
   * @param suits an array of suits
   * @param ranks an array of ranks
   * @param sprites an array of sprites; if null is passed, the suits values are
   * used for creating the filenames
   * @param cover the partial filename of the card cover sprite image. The file
   * must reside in the sprites subdirectory and have the extension .gif
   */
  public <T extends Enum<T>, R extends Enum<R>, S extends Enum<S>> Deck(T[] suits,
    R[] ranks, S[] sprites, String cover)
  {
    this(suits, ranks, sprites, cover, null);
  }

  /**
   * Creates a deck from given suit and rank and sprites enumerations
   * with given card values. The size of the suit and the sprites enumerations
   * must be equal.
   * @param <T> the Enum type of the suit
   * @param <R> the Enum type of the rank
   * @param <S> the Enum type of the sprites
   * @param suits an array of suits
   * @param ranks an array of ranks
   * @param sprites an array of sprites; if null is passed, the suits values are
   * used for creating the filenames
   * @param cover the partial filename of the card cover sprite image. The file
   * must reside in the sprites subdirectory and have the extension .gif
   * @param cardValues the integer array of card values for each suit; if null
   * is passed, all card values are set to zero
   */
  public <T extends Enum<T>, R extends Enum<R>, S extends Enum<S>> Deck(T[] suits,
    R[] ranks, S[] sprites, String cover, CardValues cardValues)
  {
    this.suits = suits;
    this.ranks = ranks;
    this.sprites = sprites;
    this.cover = cover;
    this.cardValues = cardValues;

    if (sprites != null && suits.length != sprites.length)
      fail("Error while constructing Deck instance."
        + "\nEnumerations Suits and Sprites must have same size"
        + "\n(or parameter sprites must be null)."
        + "\nApplication will terminate.");
    int i = 0;
    nbSuits = suits.length;
    suitNames = new String[nbSuits];
    for (T value : suits)
      suitNames[i++] = value.name();
    i = 0;
    nbCardsInSuit = ranks.length;
    cards = new Card[nbSuits][nbCardsInSuit];
    rankNames = new String[nbCardsInSuit];
    for (R value : ranks)
      rankNames[i++] = value.name();

    if (sprites != null)
    {
      i = 0;
      spriteNames = new String[sprites.length];
      for (S value : sprites)
        spriteNames[i++] = value.name();
    }

    nbCards = nbSuits * nbCardsInSuit;
    actors = new Actor[nbSuits][nbCardsInSuit];
    for (i = 0; i < nbSuits; i++)
    {
      for (int k = 0; k < nbCardsInSuit; k++)
      {
        String filename;
        if (sprites == null)
          filename = "sprites/" + suitNames[i] + k + ".gif";
        else
          filename = "sprites/" + spriteNames[i] + k + ".gif";

        filename = filename.toLowerCase();
        actors[i][k] = new Actor(filename, "sprites/" + cover + ".gif");
        cards[i][k] = new Card(this, suits[i], ranks[k]);
      }
    }
  }

  /**
   * Returns the JGameGrid actor attributed to the card with the specified
   * suit and rank.
   * @param suit the suit of the card
   * @param rank the rank of the card
   * @return a reference to the Actor or null, if the card of given suit and
   * rank is not found
   */
  public <T extends Enum<T>, R extends Enum<R>> Actor getSeedActor(T suit, R rank)
  {
    if (suit == null || rank == null)
      fail("Error when calling Deck.getSeedActor."
        + "\nDeck has no suits or no ranks."
        + "\nApplication will terminate.");
    int suitId = 0;
    for (String s : suitNames)
    {
      if (s.equals(suit.toString()))
        break;
      suitId++;
    }
    int rankId = 0;
    for (String s : rankNames)
    {
      if (s.equals(rank.toString()))
        break;
      rankId++;
    }
    return actors[suitId][rankId];
  }

  /**
   * Returns an integer id of the given suit (0: for suit with highest priority)
   * @param <T> the type of the suit
   * @param suit the requested suit
   * @return a priority index starting from 0
   */
  public <T extends Enum<T>> int getSuitId(T suit)
  {
    int suitId = 0;
    for (String s : suitNames)
    {
      if (s.equals(suit.toString()))
        break;
      suitId++;
    }
    return suitId;
  }

  /**
   * Returns the suit corresponding the given suit id.
   * @param suitId the id of the suit in the order the suit enum is defined.
   * @return the suit enum value
   */
  public Enum getSuit(int suitId)
  {
    return suits[suitId];
  }

  /**
   * Returns an integer id of the given rank (0: for rank with highest priority)
   * @param <R> the type of the suit
   * @param rank the requested rank
   * @return a priority index starting from 0
   */
  public <R extends Enum<R>> int getRankId(R rank)
  {
    int rankId = 0;
    for (String s : rankNames)
    {
      if (s.equals(rank.toString()))
        break;
      rankId++;
    }
    return rankId;
  }

  /**
   * Returns the rank corresponding the given rank id.
   * @param rankId the id of the rank in the order the rank enum is defined.
   * @return the suit enum value
   */
  public Enum getRank(int rankId)
  {
    return ranks[rankId];
  }

  /**
   * Returns the suit id for the card with the given card number.
   * The cards are numbered from 0 in the suit and rank priority order given when
   * the deck is created (the following numbering is used: all card from high
   * to low rank of the cards in the highest priority suit, in the next suit, etc.)
   * @param cardNb the card number
   * @return the suit id of the card
   */
  public int getSuitId(int cardNb)
  {
    return cardNb / nbCardsInSuit;
  }

  /**
   * Returns the suit name as string for the card with the given card number.
   * The cards are numbered from 0 in the suit and rank priority order given when
   * the deck is created (the following numbering is used: all card from high
   * to low rank of the cards in the highest priority suit, in the next suit, etc.)
   * @param cardNb the card number
   * @return the suit name of the card
   */
  public String getSuitName(int cardNb)
  {
    return suitNames[getSuitId(cardNb)];
  }

  /**
   * Returns the rank id for the given card number.
   * The cards are numbered from 0 in the suit and rank priority order given when
   * the deck is created (the following numbering is used: all card from high
   * to low rank of the cards in the highest priority suit, in the next suit, etc.)
   * @param cardNb the card number
   * @return the rank id of the card
   */
  public int getRankId(int cardNb)
  {
    return cardNb % nbCardsInSuit;
  }

  /**
   * Returns the rank name as string for the given card number
   * The cards are numbered from 0 in the suit and rank priority order given when
   * the deck is created (the following numbering is used: all card from high
   * to low rank of the cards in the highest priority suit, in the next suit, etc.)
   * @param cardNb the card number
   * @return the rank name of the card
   */
  public String getRankName(int cardNb)
  {
    return rankNames[getRankId(cardNb)];
  }

  /**
   * Returs the card dimensions of the card with suit and rank id zero.
   * All card should have the same dimensions.
   * @return the dimension of the cards in this deck
   */
  public Dimension getCardDimension()
  {
    Actor actor = new Actor("sprites/" + cover + ".gif");
    Dimension dim = new Dimension(actor.getWidth(0), actor.getHeight(0));
    return dim;
  }

  /**
   * Returns the complete shuffled deck as a hand.
   * @return all cards of the shuffled deck in one hand
   */
  public Hand toHand()
  {
    return dealingOut(0, 0)[0];
  }

  /**
   * Returns the complete deck as a hand.
   * @param shuffle if true, the cards are shuffle; otherwise they are ordered
   * by rank and then by suit
   * @return all cards of the deck in one hand
   */
  public Hand toHand(boolean shuffle)
  {
    return dealingOut(0, 0, shuffle)[0];
  }

  /**
   * Same as dealingOut(nbPlayers, nbCardsPerPlayer, shuffle) with shuffle = true.
   * @param nbPlayers the number of players
   * @param nbCardsPerPlayer the number of cards for each player
   * @return an array with nbPlayers elements of hands.
   * If nbPlayers * nbCardsPerPlayer exceeds the total number of cards,
   * null is returned
   */
  public Hand[] dealingOut(int nbPlayers, int nbCardsPerPlayer)
  {
    return dealingOut(nbPlayers, nbCardsPerPlayer, true);
  }

  /**
   * Returns an array with nbPlayers + 1 hands. The number of cards for
   * hand[0] to hand[nbPlayers - 1] is equal to the given nbCardsPerPlayer.
   * The remaining cards (if any) are returned at array index nbPlayer.
   * This hand may be used as a card talon (card stock, drawing hand or dead hand).
   * When nbPlayers is zero, all cards are in the talon.
   * If shuffle = false, the the cards are ordered by the priority order given
   * by the enums when the deck is created; otherwise the cards are randomly shuffled.
   * @param nbPlayers the number of players
   * @param nbCardsPerPlayer the number of cards for each player
   * @param shuffle if true, the cards are shuffled; otherwise they are ordered
   * by rank and then by suit
   * @return an array with nbPlayers elements of hands. 
   * If nbPlayers * nbCardsPerPlayer exceeds the total number of cards,
   * null is returned
   */
  public Hand[] dealingOut(int nbPlayers, int nbCardsPerPlayer, boolean shuffle)
  {
    int n = 0;
    if (nbPlayers * nbCardsPerPlayer > nbCards)
      fail("Error in Deck.dealing out."
        + "\n" + nbCards + " cards in deck. Not enough for"
        + "\n" + nbPlayers + (nbPlayers > 1 ? " players with " : "player with ")
        + nbCardsPerPlayer + (nbCardsPerPlayer > 1 ? " cards per player." : "card per player.")
        + "\nApplication will terminate.");


    ArrayList<Card> cards = new ArrayList<Card>();
    for (Enum suit : suits)
    {
      int i = 0;
      for (Enum rank : ranks)
      {
        Card card = new Card(this, suit, rank);
        cards.add(card);
      }
    }

    if (shuffle)
      Collections.shuffle(cards);

    Hand[] hands = new Hand[nbPlayers + 1];
    for (int i = 0; i < nbPlayers; i++)
    {
      hands[i] = new Hand(this);
      for (int k = 0; k < nbCardsPerPlayer; k++)
        hands[i].insert(cards.get(i * nbCardsPerPlayer + k), false);
    }
    hands[nbPlayers] = new Hand(this);
    for (int p = nbPlayers * nbCardsPerPlayer; p < nbCards; p++)
      hands[nbPlayers].insert(cards.get(p), false);
    return hands;
  }

  /**
   * Returns an integer array of the card values in the given suit.
   * @param suit the suit from where to get the card values
   * @return an integer array of the card values in the given suit
   */
  public int[] getCardValues(Enum suit)
  {
    if (cardValues == null)
      return null;
    return cardValues.values(suit);
  }

  /**
   * Returns the enumeration values as string array for the given enumeration.
   * @param <T> the enumeration type
   * @param values the values of the given enumeration
   * @return a string array with the enumeration values
   */
  public static <T extends Enum<T>> String[] enumToStringArray(T[] values)
  {
    int i = 0;
    String[] result = new String[values.length];
    for (T value : values)
    {
      result[i++] = value.name();
    }
    return result;
  }

  /**
   * Returns the number of suits in this deck.
   * @return the number of suits
   */
  public int getNumberOfSuits()
  {
    return nbSuits;
  }

  /**
   * Returns the number of ranks in this deck.
   * @return the number of ranks
   */
  public int getNumberOfRanks()
  {
    return nbCardsInSuit;
  }

  /**
   * Returns the total number of cards in this deck.
   * @return the number of cards
   */
  public int getNumberOfCards()
  {
    return nbSuits * nbCardsInSuit;
  }

  /**
   * Returns the card number of the card with given suit and rank.
   * Cards are numbered in rank order from suit to suit in the given
   * suit priority:<br><br>
   * First suit from 0..nbCardsInSuit-1<br><br>
   * Second suit from nbCardsInSuit..2*nbCardsInSuit-1<br><br>
   * etc.
   * @param suit the suit of the card
   * @param rank the rank of the card
   * @return the card number
   */
  public <T extends Enum<T>, R extends Enum<R>> int getCardNumber(T suit, R rank)
  {
    int suitId = getSuitId(suit);
    int rankId = getRankId(rank);
    return nbCardsInSuit * suitId + rankId;
  }

  /**
   * Returns the current memory heap size.
   * @return the curren heap size
   */
  public static double getHeapSize()
  {
    return Runtime.getRuntime().freeMemory() / 1000000.0;
  }

  /**
   * Displays the current memory heap size in System.out.
   */
  public static void showHeapSize()
  {
    System.out.println("Heapsize: " + getHeapSize() + " MByte");
  }

  protected Enum[] getSuits()
  {
    return suits;
  }

  protected Enum[] getRanks()
  {
    return ranks;
  }

  protected Enum[] getSprites()
  {
    return sprites;
  }

  protected static void fail(String message)
  {
    JOptionPane.showMessageDialog(null, message + getStackTrace(),
      "JCardGame Fatal Error", JOptionPane.ERROR_MESSAGE);
    
    if (GameGrid.getClosingMode() == GameGrid.ClosingMode.TerminateOnClose
      || GameGrid.getClosingMode() == GameGrid.ClosingMode.AskOnClose)
      System.exit(0);
  }

  protected static void error(String message)
  {
    JOptionPane.showMessageDialog(null, message + getStackTrace(),
      "JCardGame Fatal Error", JOptionPane.ERROR_MESSAGE);
  }

  private static String getStackTrace()
  {
    String s = "\n\nStack Trace:\n";
    int n = 0;
    for (StackTraceElement trace : new Throwable().getStackTrace())
    {
      n++;
      if (n > 2)  // Skip first two entries
        s += trace + "\n";
    }
    return s;
  }
}
