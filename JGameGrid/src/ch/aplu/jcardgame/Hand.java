// Hand.java

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

import java.util.*;
import java.awt.*;
import ch.aplu.jgamegrid.*;

/**
 * Representation of a card set.<br><br>
 *
 * <b>You find a general description of the CardGame library in the Card
 * class documentation.</b><br><br>
 *
 * Hands are very useful containers for cards, not only for the cards in a
 * player's hand, but also for storing card piles of any kind on the gambling table.
 * Even when a hand contains a maximum of one card, it is preferable to
 * individual cards, because the transfer from one hand to another is very simple.
 * A hand can be empty (and displayed as such without harm). A hand may also
 * serve as card store without being displayed during the whole game. It is not
 * rare to use between 10 to 20 hands in a 4-player game application.<br><br>
 *
 * A hand stores the cards in an card list and most hand modifications will
 * modify this list. A hand may be displayed in the card game (game grid)
 * window in several layouts: stacked (a card pile, only the top card is seen), in a row,
 * in a column and as fan.
 * All hand operation methods will no shown the modifications until draw() is
 * called. (All of them have a boolean doDraw parameter that can be
 * set true to call draw() automatically.) <br><br>
 * By default, the mouse touches are not enabled. To enable them,
 * use setTouchEnabled(true). For RowLayout, ColumnLayout and FanLayout
 * the card is automatically pulled on top of other cards when you left click
 * on a visible part of the card (unless disabled by putOnTopEnabled(false)).
 *
 * @see Card
 */
public class Hand implements GGMouseTouchListener, GGActListener
{
  private class RankSet
  {
    boolean[] a = new boolean[4];

    RankSet()
    {
      for (int i = 0; i < 4; i++)
        a[i] = false;
    }

    boolean isPresent()
    {
      boolean isPresent = false;
      for (int i = 0; i < 4; i++)
      {
        if (a[i])
        {
          isPresent = true;
          break;
        }
      }
      return isPresent;
    }

    public String toString()
    {
      String s = "RankSet: [";
      for (int i = 0; i < 4; i++)
      {
        s += (a[i] ? "x" : "-");
        if (i < 3)
          s += ",";
      }
      s += "]";
      return s;
    }

  }

  private enum LayoutType
  {
    NONE, ROW, COLUMN, FAN, STACK
  }

  /**
   * Card alignment used when hand is drawn using drawRow(), drawColumn()
   */
  public enum CardAlignment
  {
    /**
     * Align hand to the first card in the card list.
     */
    FIRST,
    /**
     * Align hand to the middle card in the card list.
     */
    MIDDLE,
    /**
     * Align hand to the last card in the card list.
     */
    LAST
  }

  /**
   * Compare type used for the compareTo() implementation.
   */
  public static enum SortType
  {
    /**
     * Compare rank first, if same compare suit.
     */
    RANKPRIORITY,
    /**
     * Compare suit first, if same compare rank.  
     */
    SUITPRIORITY,
    /**
     * Compare points.
     */
    POINTPRIORITY
  }

  private class AnimateParams
  {
    boolean isRowAnimated = false;
    boolean isColumnAnimated = false;
    boolean isFanAnimated = false;
    Location handLocation;
    Location center;
    double radius;
    double startDir;
    double endDir;
    int rowWidth;
    int columnHeight;
    double endArc;
    double scaleFactor;
    double rotationAngle;
    CardAlignment cardAlignment;
    int delay;
    int currentRowWidth;
    int currentColumnHeight;
    double currentEndDir;
    int cardWidth;
    int cardHeight;
  }
  //

  private int a = 0;
  private HandLayout handLayout = null;
  private AnimateParams params = new AnimateParams();
  private LayoutType layoutType = LayoutType.NONE;
  private Deck deck;
  private ArrayList<Card> cardList = new ArrayList<Card>();
  private CardGame cardGame = null;
  private CardListener cardListener = null;
  private int horzCardDistance = -1;
  private int vertCardDistance = -1;
  private boolean isTouchEnabled = false;
  private Location handLocation;
  private int cardWidth;
  private int cardHeight;
  private TargetArea targetArea = null;
  private SortType sortType = SortType.SUITPRIORITY;
  private boolean isPutOnTopEnabled = true;
  private boolean isTargetAreaDefined = false;

  /**
   * Creates a hand instance with game cards from the given deck.
   * The layout is undefined.
   * @param deck the deck where suit, rank and points are defined.
   */
  public Hand(Deck deck)
  {
    this.deck = deck;
    this.handLayout = null;
  }

  /**
   * Selects the card game instance where to display the hand's card.
   * @param cardGame the reference to the card game
   */
  public void setCardGame(CardGame cardGame)
  {
    this.cardGame = cardGame;
  }

  private void drawStack(Location handLocation, double scaleFactor,
    double rotationAngle)
  {
    cardGame.setActEnabled(false);
    this.handLocation = handLocation;
    ArrayList<CardActor> list = new ArrayList<CardActor>();
    ArrayList<Card> tmp = new ArrayList<Card>(cardList);  // Tmp to avoid concurrency
    Iterator<Card> itr = tmp.iterator();
    while (itr.hasNext())
    {
      Card card = itr.next();
      list.add(card.getCardActor());  // Save old card actor
      CardActor cardActor = card.associateActor(scaleFactor, rotationAngle);
      cardActor.show(card.isVerso() ? 1 : 0);
      cardGame.addActorNoRefresh(cardActor,
        new Location(handLocation.x, handLocation.y));
      cardActor.addMouseTouchListener(this,
        GGMouse.lPress
        | GGMouse.lRelease
        | GGMouse.lDClick
        | GGMouse.lClick
        | GGMouse.rClick
        | GGMouse.rPress
        | GGMouse.rRelease
        | GGMouse.rClick
        | GGMouse.rDClick, true); // on top only
    }
    removeCardActors(list);
    cardGame.setActEnabled(true);
  }

  private void drawRow(Location handLocation, int rowWidth,
    double scaleFactor, double rotationAngle, int delay,
    CardAlignment cardAlignment)
  {
    if (delay > 0 && !GameGrid.isDisposed())  // animate
    {
      params.handLocation = handLocation;
      params.rowWidth = rowWidth;
      params.scaleFactor = scaleFactor;
      params.rotationAngle = rotationAngle;
      params.delay = delay;
      params.cardAlignment = cardAlignment;
      params.cardWidth = (int)(deck.getCardDimension().width * scaleFactor);
      params.currentRowWidth = cardWidth;
      params.isRowAnimated = true;
      ch.aplu.util.Monitor.putSleep();
    }
    else
      drawMyRow(handLocation, rowWidth, scaleFactor, rotationAngle,
        cardAlignment);
  }

  private void drawMyRow(Location handLocation, int rowWidth, double scaleFactor,
    double rotationAngle, CardAlignment cardAlignment)
  {
    cardGame.setActEnabled(false);
    cardWidth = (int)(deck.getCardDimension().width * scaleFactor);
    this.handLocation = handLocation;
    int nbCard = cardList.size();
    setHorzCardDistance(rowWidth);
    ArrayList<CardActor> list = new ArrayList<CardActor>();
    ArrayList<Card> tmp = new ArrayList<Card>(cardList);  // Tmp to avoid concurrency
    Iterator<Card> itr = tmp.iterator();
    int i = 0;
    while (itr.hasNext())
    {
      Card card = itr.next();
      list.add(card.getCardActor());  // Save old card actor
      CardActor cardActor = card.associateActor(scaleFactor, rotationAngle);
      cardActor.show(card.isVerso() ? 1 : 0);
      int centerIndex = nbCard / 2;
      Location loc = handLocation.clone();
      int dr = 0;
      int dx;
      int dy;
      switch (cardAlignment)
      {
        case MIDDLE:
          if (nbCard % 2 == 1)  // odd
            dr = (i - centerIndex) * horzCardDistance;
          else  // even
            dr = (i - centerIndex) * horzCardDistance + horzCardDistance / 2;
          break;
        case FIRST:
          dr = i * horzCardDistance;
          break;
        case LAST:
          dr = -(nbCard - 1 - i) * horzCardDistance;
          break;
      }
      if (rowWidth >= 0 && rowWidth <= cardWidth)
        dr = 0;
      if (rotationAngle == 0)
      {
        dx = dr;
        dy = 0;
      }
      else
      {
        dx = (int)(dr * Math.cos(Math.toRadians(rotationAngle)));
        dy = (int)(dr * Math.sin(Math.toRadians(rotationAngle)));
      }
      cardGame.addActorNoRefresh(cardActor, new Location(loc.x + dx, loc.y + dy));
      cardActor.addMouseTouchListener(this,
        GGMouse.lPress
        | GGMouse.lRelease
        | GGMouse.lDClick
        | GGMouse.lClick
        | GGMouse.rClick
        | GGMouse.rPress
        | GGMouse.rRelease
        | GGMouse.rClick
        | GGMouse.rDClick, true); // on top only
      i++;
    }
    removeCardActors(list);
    cardGame.setActEnabled(true);
  }

  private void animateRow()
  {
    if (params.currentRowWidth < params.rowWidth && !GameGrid.isDisposed())
    {
      drawMyRow(params.handLocation, params.currentRowWidth,
        params.scaleFactor, params.rotationAngle, params.cardAlignment);
      if (params.currentRowWidth == params.cardWidth)
        GameGrid.delay(200);
      else
        GameGrid.delay(params.delay);
      params.currentRowWidth += 10;
    }
    else
    {
      drawMyRow(params.handLocation, params.rowWidth,
        params.scaleFactor, params.rotationAngle, params.cardAlignment);
      params.isRowAnimated = false;
      ch.aplu.util.Monitor.wakeUp();
    }
  }

  private void drawColumn(Location handLocation, int columnHeight,
    double scaleFactor, double rotationAngle, int delay,
    CardAlignment cardAlignment)
  {
    if (delay > 0 && !GameGrid.isDisposed())  // animate
    {
      params.handLocation = handLocation;
      params.columnHeight = columnHeight;
      params.scaleFactor = scaleFactor;
      params.rotationAngle = rotationAngle;
      params.delay = delay;
      params.cardAlignment = cardAlignment;
      params.cardHeight = (int)(deck.getCardDimension().height * scaleFactor);
      params.currentColumnHeight = cardHeight;
      params.isColumnAnimated = true;
      ch.aplu.util.Monitor.putSleep();
    }
    else
      drawMyColumn(handLocation, columnHeight, scaleFactor, rotationAngle,
        cardAlignment);
  }

  private void drawMyColumn(Location handLocation,
    int columnHeight, double scaleFactor, double rotationAngle,
    CardAlignment cardAlignment)
  {
    cardGame.setActEnabled(false);
    this.handLocation = handLocation;
    cardHeight = (int)(deck.getCardDimension().height * scaleFactor);
    this.handLocation = handLocation;
    int nbCard = cardList.size();
    setVertCardDistance(columnHeight);
    ArrayList<CardActor> list = new ArrayList<CardActor>();
    ArrayList<Card> tmp = new ArrayList<Card>(cardList);  // Tmp to avoid concurrency
    Iterator<Card> itr = tmp.iterator();
    int i = 0;
    while (itr.hasNext())
    {
      Card card = itr.next();
      list.add(card.getCardActor());  // Save old card actor
      CardActor cardActor = card.associateActor(scaleFactor, rotationAngle);
      cardActor.show(card.isVerso() ? 1 : 0);
      int centerIndex = nbCard / 2;
      Location loc = handLocation.clone();
      int dr = 0;
      int dx;
      int dy;
      switch (cardAlignment)
      {
        case MIDDLE:
          if (nbCard % 2 == 1)  // odd
            dr = (i - centerIndex) * vertCardDistance;
          else  // even
            dr = (i - centerIndex) * vertCardDistance + vertCardDistance / 2;
          break;
        case FIRST:
          dr = i * vertCardDistance;
          break;
        case LAST:
          dr = -(nbCard - 1 - i) * vertCardDistance;
          break;
      }
      if (columnHeight >= 0 && columnHeight <= cardHeight)
        dr = 0;
      if (rotationAngle == 0)
      {
        dx = 0;
        dy = dr;
      }
      else
      {
        dx = -(int)(dr * Math.sin(Math.toRadians(rotationAngle)));
        dy = (int)(dr * Math.cos(Math.toRadians(rotationAngle)));
      }
      cardGame.addActorNoRefresh(cardActor, new Location(loc.x + dx, loc.y + dy));
      cardActor.addMouseTouchListener(this,
        GGMouse.lPress
        | GGMouse.lRelease
        | GGMouse.lDClick
        | GGMouse.lClick
        | GGMouse.rClick
        | GGMouse.rPress
        | GGMouse.rRelease
        | GGMouse.rClick
        | GGMouse.rDClick, true); // on top only
      i++;
    }
    removeCardActors(list);
    cardGame.setActEnabled(true);
  }

  private void animateColumn()
  {
    if (params.currentColumnHeight < params.columnHeight && !GameGrid.isDisposed())
    {
      drawMyColumn(params.handLocation, params.currentColumnHeight,
        params.scaleFactor, params.rotationAngle, params.cardAlignment);
      if (params.currentColumnHeight == params.cardHeight)
        GameGrid.delay(200);
      else
        GameGrid.delay(params.delay);
      params.currentColumnHeight += 10;
    }
    else
    {
      drawMyColumn(params.handLocation, params.columnHeight,
        params.scaleFactor, params.rotationAngle, params.cardAlignment);
      params.isColumnAnimated = false;
      ch.aplu.util.Monitor.wakeUp();
    }
  }

  private void drawFan(Location center, double radius, double startDir,
    double endDir, double scaleFactor, int delay)
  {
    if (delay > 0 && !GameGrid.isDisposed())  // animate
    {
      params.center = center;
      params.radius = radius;
      params.startDir = startDir;
      params.endDir = endDir;
      params.scaleFactor = scaleFactor;
      params.delay = delay;
      params.currentEndDir = startDir;
      params.isFanAnimated = true;
      ch.aplu.util.Monitor.putSleep();
    }
    else
      drawMyFan(center, radius, startDir, endDir, scaleFactor);
  }

  private void drawMyFan(Location center,
    double radius, double startDir, double endDir, double scaleFactor)
  {
    if (cardGame == null)
      return;
    if (radius < 10)
      return;
    this.handLocation = center;
    cardGame.setActEnabled(false);
    if (endDir >= 0)
    {
      if (endDir < startDir)
        endDir = startDir;
    }
    startDir = Math.toRadians(startDir);
    endDir = Math.toRadians(endDir);

    int nbCard = cardList.size();
    if (nbCard == 0)
      return;

    double angleDistance;
    if (nbCard > 1)
    {
      if (endDir >= 0)
        angleDistance = (endDir - startDir) / (nbCard - 1);
      else
        angleDistance = -endDir;
    }
    else
      angleDistance = 0;
    ArrayList<CardActor> list = new ArrayList<CardActor>();
    ArrayList<Card> tmp = new ArrayList<Card>(cardList);  // Tmp to avoid concurrency
    Iterator<Card> itr = tmp.iterator();
    int i = 0;
    GGVector vCenter = new GGVector(new Point(center.x, center.y));

    while (itr.hasNext())
    {
      Card card = itr.next();
      list.add(card.getCardActor());  // Save old card actor
      GGVector vRadius = new GGVector(radius, 0);
      double rotAngle = startDir + i * angleDistance;
      vRadius.rotate(rotAngle);
      GGVector vCard = vCenter.add(vRadius);
      CardActor cardActor = card.associateActor(scaleFactor,
        90 + Math.toDegrees(rotAngle));
      cardActor.show(card.isVerso() ? 1 : 0);
      cardGame.addActorNoRefresh(cardActor, new Location((int)vCard.x, (int)vCard.y));
      cardActor.addMouseTouchListener(this,
        GGMouse.lPress
        | GGMouse.lRelease
        | GGMouse.lDClick
        | GGMouse.lClick
        | GGMouse.rClick
        | GGMouse.rPress
        | GGMouse.rRelease
        | GGMouse.rClick
        | GGMouse.rDClick, true); // on top only
      i++;
    }
    removeCardActors(list);
    cardGame.setActEnabled(true);
  }

  private void animateFan()
  {
    if (params.currentEndDir < params.endDir && !GameGrid.isDisposed())
    {
      drawMyFan(params.center, params.radius,
        params.startDir, params.currentEndDir, params.scaleFactor);
      if (params.currentEndDir == params.startDir)
        GameGrid.delay(200);
      else
        GameGrid.delay(params.delay);
      params.currentEndDir += 1;
    }
    else
    {
      drawMyFan(params.center, params.radius,
        params.startDir, params.endDir, params.scaleFactor);
      params.isFanAnimated = false;
      ch.aplu.util.Monitor.wakeUp();
    }
  }

  private void setHorzCardDistance(int rowWidth)
  {
    int nbCard = cardList.size();
    if (nbCard == 0)
      horzCardDistance = -1;
    else if (nbCard == 1)
      horzCardDistance = 0;
    else if (rowWidth < 0)
      horzCardDistance = -rowWidth;
    else if (nbCard * cardWidth <= rowWidth)
      horzCardDistance = cardWidth;
    else
      horzCardDistance = (rowWidth - cardWidth) / (nbCard - 1);
  }

  private void setVertCardDistance(int columnHeight)
  {
    int nbCard = cardList.size();
    if (nbCard == 0)
      vertCardDistance = -1;
    else if (nbCard == 1)
      vertCardDistance = 0;
    else if (columnHeight < 0)
      vertCardDistance = -columnHeight;
    else if (nbCard * cardHeight <= columnHeight)
      vertCardDistance = cardHeight;
    else
      vertCardDistance = (columnHeight - cardHeight) / (nbCard - 1);
  }

  private void removeCardActors(ArrayList<CardActor> list)
  {
    for (CardActor cardActor : list)
      if (cardActor != null)
        cardActor.removeSelf();
  }

  /**
   * Implementation of GGMouseTouchListener. For internal use only.
   */
  public void mouseTouched(final Actor actor, final GGMouse mouse, Point spot)
  {
    if (!isTouchEnabled)
      return;
    final Card card = ((CardActor)actor).getCard();
    new Thread()
    {
      public void run()
      {
        switch (mouse.getEvent())
        {
          case GGMouse.lPress:
            if (cardListener != null)
              cardListener.leftPressed(card);
            break;

          case GGMouse.lRelease:
            if (cardListener != null)
              cardListener.leftReleased(card);
            break;

          case GGMouse.lClick:
            synchronized (CardGame.monitor) // Avoid concurrent access of scene list
            {
              if (isPutOnTopEnabled)
                putOnTop(card);
            }
            if (cardListener != null)
              cardListener.leftClicked(card);
            break;

          case GGMouse.lDClick:
            if (cardListener != null)
              cardListener.leftDoubleClicked(card);
            break;

          case GGMouse.rPress:
            if (cardListener != null)
              cardListener.rightPressed(card);
            break;

          case GGMouse.rRelease:
            if (cardListener != null)
              cardListener.rightReleased(card);
            break;

          case GGMouse.rClick:
            if (cardListener != null)
              cardListener.rightClicked(card);
            break;

          case GGMouse.rDClick:
            if (cardListener != null)
              cardListener.rightDoubleClicked(card);
            break;
        }
      }

    }.start();
  }

  /**
   * Enable/disables automatic putOnTop when the card is left-clicked.
   * Default: enabled, (setTouchEnabled(true) must be called to enable
   * automatic putOnTop.)
   * @param enable if true, the card is automatically put on top when clicked;
   * otherwise the card must be manually put on top using one of the mouse
   * events
   */
  public void putOnTopEnabled(boolean enable)
  {
    isPutOnTopEnabled = enable;
  }

  /**
   * Draws the given card above each other card (on top) in this hand and
   * rearrages the hand appearence so that all card are shown. For a stacked
   * layout, nothing happens.
   * @param topCard the card to put on top
   */
  public void putOnTop(Card topCard)
  {
    if (cardGame == null)
      return;
    if (layoutType == LayoutType.STACK)
      return;
    synchronized (cardList)
    {
      ArrayList<Actor> list = new ArrayList<Actor>();
      for (int i = 0; i < cardList.size(); i++)
      {
        Card card = cardList.get(i);
        if (card != topCard)
          list.add(card.getCardActor());
        else
          break;
      }
      for (int i = cardList.size() - 1; i >= 0; i--)
      {
        Card card = cardList.get(i);
        if (card != topCard)
          list.add(card.getCardActor());
        else
          break;
      }
      list.add(topCard.getCardActor());
      cardGame.setSceneOrder(list);
    }
  }

  protected LayoutType getLayoutType()
  {
    return layoutType;
  }

  /**
   * Registers a card listener to get event notifications when a card
   * is touched or arrives at its target.
   * @param listener the CardListener to register
   */
  public void addCardListener(CardListener listener)
  {
    this.cardListener = listener;
  }

  /**
   * Returns the sum of the card values (card points) of all card in the hand.
   * @return the total amount of points
   */
  public int getScore()
  {
    int sum = 0;
    synchronized (cardList)
    {
      for (Card card : cardList)
        sum += card.getValue();
      return sum;
    }
  }

  /**
   * Returns a string representation of all cards in the hand.
   * @return a string representation of the hand
   */
  public String toString()
  {
    int n = 1;
    String s = new String();
    synchronized (cardList)
    {

      int nbCards = cardList.size();
      for (Card card : cardList)
      {
        s += n + ": " + card.toString();
        if (n < nbCards)
          s += "\n";
        n++;
      }
      if (s.equals(""))
        s = "Emtpy";
      return s;
    }
  }

  /**
   * Inserts all cards of the given hand in the current hand (reference copy)
   * and sets the hand of the cards to the current hand.
   * If a card is already part of the hand, the card in not reinserted.
   * @param hand the hand from where to import the cards
   * @param doDraw if true, the current hand is drawn
   * @return true, if insertion is successful; false, if one of the cards could not
   * be inserted, but all non-dublicated cards are inserted
   */
  public boolean insert(Hand hand, boolean doDraw)
  {
    boolean b = true;
    synchronized (cardList)
    {

      for (Card card : hand.getCardList())
      {
        if (!insert(card, doDraw))
          b = false;
      }
      return b;
    }
  }

  /**
   * Inserts the given card in the current hand (reference copy) and
   * sets the hand of the given card to the current hand.
   * If a card is already part of the hand, the card in not reinserted.
   * @param card the card to insert
   * @param doDraw if true, the current hand is drawn (showing the inserted card)
   * @return true, if insertion is successful; otherwise false
   */
  public boolean insert(Card card, boolean doDraw)
  {
    // Check for duplication
    synchronized (cardList)
    {

      for (Card c : cardList)
      {
        if (c.equals(card))
          return false;
      }
      cardList.add(card);
      card.setHand(this);
      if (doDraw)
        draw();
      return true;
    }
  }

  /**
   * Creates a new card with given card number and inserts it in the current hand.
   * If a card with same suit/rank is already part of the hand, the card in not inserted.
   * @param cardNumber the card number of the card to insert
   * @param doDraw if true, the current hand is drawn (showing the inserted card)
   * @return true, if insertion is successful; otherwise false
   */
  public boolean insert(int cardNumber, boolean doDraw)
  {
    // Check for duplication
    synchronized (cardList)
    {

      for (Card c : cardList)
      {
        if (c.getCardNumber() == cardNumber)
          return false;
      }
      Card card = new Card(deck, cardNumber);
      cardList.add(card);
      card.setHand(this);
      if (doDraw)
        draw();
      return true;
    }
  }

  /**
   * Creates a new card with given suit and rank and inserts it in the current hand.
   * If a card with same suit/rank is already part of the hand, the card in not inserted.
   * @param <T> the Enum type of the suit
   * @param <R> the Enum type of the rank
   * @param suit the card suit
   * @param rank the card rank
   * @param doDraw if true, the current hand is drawn (showing the inserted card)
   * @return true, if insertion is successful; otherwise false
   */
  public <T extends Enum<T>, R extends Enum<R>> boolean insert(T suit, R rank, boolean doDraw)
  {
    Card card = new Card(deck, suit, rank);
    return insert(card, doDraw);
  }

  /**
   * Removes first card from card list.
   * @param doDraw if true, the current hand is drawn
   * (not showing the card anymore)
   * @see #remove(Card card, boolean doDraw)
   */
  public void removeFirst(boolean doDraw)
  {
    synchronized (cardList)
    {
      remove(cardList.get(0), doDraw);
    }
  }

  /**
   * Removes last card from card list.
   * @param doDraw if true, the current hand is drawn
   * (not showing the card anymore)
   * @see #remove(Card card, boolean doDraw)
   */
  public void removeLast(boolean doDraw)
  {
    synchronized (cardList)
    {
      remove(cardList.get(cardList.size() - 1), doDraw);
    }
  }

  /**
   * Removes the card at given index from card list. If i less than zero or greater
   * than maximum index, nothing happens.
   * @param index the index of the card to remove
   * @param doDraw if true, the current hand is drawn
   * (not showing the card anymore)
   * @return true, if the card was removed from the hand; false, if index is
   * is outside [0..size of card list - 1]
   * @see #remove(Card card, boolean doDraw)
   */
  public boolean remove(int index, boolean doDraw)
  {
    synchronized (cardList)
    {
      if (index < 0 || index > cardList.size() - 1)
        return false;
      remove(cardList.get(index), doDraw);
      return true;
    }
  }

  /**
   * Removes card from the current hand. If the card is part of
   * the game grid, removes it also from the game grid, but
   * the card reference is still valid.
   * To show it again, the card actor must be added again to the game grid.
   * (This is automatically done, when the card is reinserted into a hand
   * and draw() is called).
   * @param card the card to remove; if the card is not in the hand, nothing happens
   * @param doDraw if true, the current hand is drawn
   * (not showing the card anymore)
   * @return true, if the card was removed from the hand; false, if it is not part of the hand
   */
  public boolean remove(Card card, boolean doDraw)
  {
    synchronized (cardList)
    {
      for (Card c : cardList)
      {
        if (c.equals(card))
        {
          cardList.remove(c);
          // If the card is still in this hand, makes it handless
          if (card.getHand() == this)
            card.setHand(null);
          CardActor cardActor = card.getCardActor();
          if (cardActor != null)
            cardActor.removeSelf();
          if (doDraw && !isEmpty())
            draw();
          return true;
        }
      }
      return false;
    }
  }

  /**
   * Removes every card in the current hand and from the game grid. The
   * card list will be empty. 
   * @param doRefresh if true, the game grid is refreshed, so that the
   * hand becomes immediately invisible
   */
  public void removeAll(boolean doRefresh)
  {
    // Does not set myHand = null, because the cards may already be
    // inserted into a new hand
    synchronized (cardList)
    {
      if (!cardList.isEmpty())
      {
        for (Card card : cardList)
          card.getCardActor().removeSelf();
        if (cardGame != null && doRefresh)
          cardGame.refresh();
        cardList.clear();
      }
    }
  }

  /**
   * Returns true, if the a card with same suit and rank is found in the
   * card list of current hand.
   * @param card the card to check
   * @return true, if a card with same suit and rank is part of the
   * current hand; otherwise false
   */
  public boolean contains(Card card)
  {
    synchronized (cardList)
    {
      for (Card c : cardList)
      {
        if (c.equals(card))
          return true;
      }
      return false;
    }
  }

  /**
   * Returns true, if the card list of the current hand is empty.
   * @return true, if the hand contains no cards; otherwise false
   */
  public boolean isEmpty()
  {
    return cardList.isEmpty();
  }

  /**
   * Returns the card list of the current hand.
   * @return the array list of all cards in this hand
   */
  public ArrayList<Card> getCardList()
  {
    return cardList;
  }

  /**
   * Returns the number of cards in the current hand.
   * @return the size of the card list
   */
  public int getNumberOfCards()
  {
    return cardList.size();
  }

  /**
   * Returns the number of cards of given suit in the current hand.
   * @param suit the suit of the cards
   * @return the number of cards with specified suit
   */
  public <T extends Enum<T>> int getNumberOfCardsWithSuit(T suit)
  {
    int nb = 0;
    synchronized (cardList)
    {
      for (Card card : cardList)
      {
        if (card.getSuit() == suit)
          nb++;
      }
      return nb;
    }
  }

  /**
   * Returns the number of cards of given rank in the current hand.
   * @param rank the rank of the cards
   * @return the number of cards with specified rank
   */
  public <R extends Enum<R>> int getNumberOfCardsWithRank(R rank)
  {
    int nb = 0;
    synchronized (cardList)
    {
      for (Card card : cardList)
      {
        if (card.getRank() == rank)
          nb++;
      }
      return nb;
    }
  }

  /**
   * Returns card reference of the card at index i of the card list or null if the hand is empty.
   * if i is outside the valid array range, returns null.
   * @return the card reference (not a clone) at index i of the card list or null, if not found
   */
  public Card get(int i)
  {
    synchronized (cardList)
    {
      if (i < 0 || i > cardList.size() - 1)
        return null;
      return cardList.get(i);
    }
  }

  /**
   * Returns card reference of the first card or null if the hand is empty.
   * @return the card reference (not a clone) at the first position of the card list (show on bottom
   * of all other cards)
   */
  public Card getFirst()
  {
    synchronized (cardList)
    {
      if (cardList.isEmpty())
        return null;
      return cardList.get(0);
    }
  }

  /**
   * Returns card reference of the last card or null if the hand is empty.
   * @return the card reference (not a clone) at the last position of the card list (shown on top
   * of all other cards)
   */
  public Card getLast()
  {
    synchronized (cardList)
    {
      if (cardList.isEmpty())
        return null;
      return cardList.get(cardList.size() - 1);
    }
  }

  /**
   * Sets the isVerso attribute to all cards in the card list. If the cards
   * are visible, they are immediately shown in the requested state.
   * @param isVerso if true, all cards are shown face down; otherwise they are
   * shown face up
   */
  public void setVerso(boolean isVerso)
  {
    synchronized (cardList)
    {
      for (Card card : cardList)
        card.setVerso(isVerso);
    }
  }

  /**
   * Sets the transferring attributes for all cards in the current hand. The
   * cards are moved to the given target location (may be anywhere, not necessarily
   * at the target hand location).
   * @param targetArea the TargetArea used for the transfer
   */
  public void setTargetArea(TargetArea targetArea)
  {
    if (targetArea == null)
      isTargetAreaDefined = false;
    else
    {
      this.targetArea = targetArea.clone();
      isTargetAreaDefined = true;
    }
  }

  /**
   * Returns a reference to the current target area.
   * @return the current target area
   */
  public TargetArea getTargetArea()
  {
    return targetArea;
  }

  /**
   * For internal use only. Implements GGActListener to get act() events used
   * for animation.
   */
  public void act()
  {
    if (params.isRowAnimated)
      animateRow();
    if (params.isColumnAnimated)
      animateColumn();
    if (params.isFanAnimated)
      animateFan();
  }

  /**
   * Sorts the card list with given sort type.
   * @param doDraw if true, the hand is drawn
   * @return card at last position of the card list (shown on top of other
   * cards); null if hand is empty
   */
  public Card sort(SortType sortType, boolean doDraw)
  {
    synchronized (cardList)
    {
      if (cardList.isEmpty())
        return null;

      this.sortType = sortType;
      // Copy to TreeSet using Card's compareTo()
      TreeSet<Card> cardSet = new TreeSet<Card>();
      for (Card card : cardList)
        cardSet.add(card);
      cardList.clear();
      // Copy to temporary ArrayList and rearrage cardList
      for (Card card : cardSet)
      {
        cardList.add(card);
      }
      if (doDraw)
        draw();
      return cardList.get(cardList.size() - 1);
    }
  }

  /**
   * Sorts the card list with given sort type and reverses the list.
   * @param sortType the card compare type used for sorting
   * @param doDraw if true, the hand is drawn
   * @return card at last position of the card list (shown on top of other
   * cards); null if hand is empty
   */
  public Card reverseSort(SortType sortType, boolean doDraw)
  {
    sort(sortType, false);
    return reverse(doDraw);
  }

  /**
   * Shifts (rolls) the card list forward or backward.  When shifting
   * forward, the last element is reinserted at the beginning,
   * when shifting backward, the first elementis reinserted
   * at the end. Keep in mind that the order in the card list determines
   * the paint order of the card images (actors at later list positions
   * are drawn on top of others)
   * @param forward if true, shifts to the right (forward); otherwise
   * shifts to the left (backward)
   * @param doDraw if true, the hand is drawn
   * @return card at last position of the card list (shown on top of other
   * cards); null if hand is empty
   */
  public Card shift(boolean forward, boolean doDraw)
  {
    synchronized (cardList)
    {
      if (cardList.isEmpty())
        return null;
      int size = cardList.size();
      ArrayList<Card> list = new ArrayList<Card>(cardList);
      cardList.clear();
      if (forward)
      {
        cardList.add(list.get(size - 1));
        for (int i = 0; i < size - 1; i++)
          cardList.add(list.get(i));
      }
      else
      {
        for (int i = 1; i < size; i++)
          cardList.add(list.get(i));
        cardList.add(list.get(0));
      }

      if (doDraw)
        draw();
      return cardList.get(size - 1);
    }
  }

  /**
   * Reverses the order of the cards in the card list.
   * @param doDraw if true, the hand is drawn
   * @return card at last position of the card list (shown on top of other
   * cards); null if hand is empty
   */
  public Card reverse(boolean doDraw)
  {
    synchronized (cardList)
    {
      if (cardList.isEmpty())
        return null;
      int size = cardList.size();
      ArrayList<Card> list = new ArrayList<Card>(cardList);
      cardList.clear();
      for (int i = size - 1; i >= 0; i--)
        cardList.add(list.get(i));
      if (doDraw)
        draw();
      return (cardList.get(cardList.size() - 1));
    }
  }

  /**
   * Shuffles the card list (random permutation of cards).
   * @return card at last position of the card list (shown on top of other;
   * null if hand is empty
   * @param doDraw if true, the hand is drawn
   */
  public Card shuffle(boolean doDraw)
  {
    synchronized (cardList)
    {
      if (cardList.isEmpty())
        return null;
      Collections.shuffle(cardList);
      if (doDraw)
        draw();
      return cardList.get(cardList.size() - 1);
    }
  }

  /**
   * Enables or disables mouse touch events. If the mouse events are generally
   * disabled by calling GameGrid.setMouseEnabled(false), no mouse touch events
   * are disabled too.
   * @param enable if true, the mouse touches are enabled; otherwise disabled
   */
  public void setTouchEnabled(boolean enable)
  {
    isTouchEnabled = enable;
  }

  /**
   * Returns the current state of mouse touches.
   * @return true, if the mouse touches are enabled; otherwise false
   */
  public boolean isTouchEnabled()
  {
    return isTouchEnabled;
  }

  /**
   * Sets the compare type of cards used for sorting to one of the enumeration
   * values defined in the SortType enumeration.
   * @param sortType the compare type used for sorting
   */
  public void setSortType(SortType sortType)
  {
    this.sortType = sortType;
  }

  /**
   * Returns the compare type for cards used for sorting.
   * @return one of the compare types defined in the SortType enumeration
   */
  public SortType getSortType()
  {
    return sortType;
  }

  /**
   * Compares the cards in the card list using the given compare type. Returns
   * the list position of the card with the maximum value. It is assumed that
   * there is exactly one card with maximum value.
   * @param sortType the compare type to apply
   * @return the index of the card with the maximum value, -1 if the card
   * list is empty
   */
  public int getMaxPosition(Hand.SortType sortType)
  {
    synchronized (cardList)
    {
      if (cardList.isEmpty())
        return -1;
      Hand tmp = new Hand(deck);
      for (Card card : cardList)
        tmp.insert(card.clone(), false);
      tmp.sort(sortType, false);
      Card winningCard = tmp.getFirst();
      int i;
      for (i = 0; i < cardList.size(); i++)
      {
        if (winningCard.equals(cardList.get(i)))
          break;
      }
      return i;
    }
  }

  protected CardListener getCardListener()
  {
    return cardListener;
  }

  /**
   * Preparing to display the current hand in a game grid window using the given
   * given layout. Any layout passed earlier is replaced.
   * @param cardGame the reference of the CardGame where to display the hand
   * @param handLayout the reference to a HandLayout instance; may be null, if
   * the hand is never drawn, but the CardGame reference should be set
   */
  public void setView(CardGame cardGame, HandLayout handLayout)
  {
    this.cardGame = cardGame;
    this.handLayout = handLayout;
    if (handLayout instanceof StackLayout)
      layoutType = LayoutType.STACK;
    if (handLayout instanceof RowLayout)
      layoutType = LayoutType.ROW;
    if (handLayout instanceof ColumnLayout)
      layoutType = LayoutType.COLUMN;
    if (handLayout instanceof FanLayout)
      layoutType = LayoutType.FAN;
    cardGame.addActListener(this);
  }

  /**
   * Displays the hand using the current layout. Whenever a hand is drawn,
   * the current card actor (if defined) of every card is removed from the game grid,
   * recalculated from its seed actor and added back to the game grid.
   * This is a somewhat time-consuming operation and hands should only
   * be redrawn when needed. If setView() was never called, nothing happens.
   * It may be necessary to draw an empty hand to set the hand location.
   */
  public void draw()
  {
    if (cardGame == null)
      return;
    switch (layoutType)
    {
      case STACK:
        StackLayout stackLayout = (StackLayout)handLayout;
        drawStack(stackLayout.getHandLocation(), stackLayout.getScaleFactor(),
          stackLayout.getRotationAngle());
        break;
      case ROW:
        RowLayout rowLayout = (RowLayout)handLayout;
        drawRow(rowLayout.getHandLocation(), rowLayout.getRowWidth(),
          rowLayout.getScaleFactor(), rowLayout.getRotationAngle(),
          rowLayout.getStepDelay(), rowLayout.getCardAlignment());
        break;
      case COLUMN:
        ColumnLayout columnLayout = (ColumnLayout)handLayout;
        drawColumn(columnLayout.getHandLocation(), columnLayout.getColumnHeight(),
          columnLayout.getScaleFactor(), columnLayout.getRotationAngle(),
          columnLayout.getStepDelay(), columnLayout.getCardAlignment());
        break;
      case FAN:
        FanLayout fanLayout = (FanLayout)handLayout;
        drawFan(fanLayout.getCenter(), fanLayout.getRadius(),
          fanLayout.getStartDir(), fanLayout.getEndDir(),
          fanLayout.getScaleFactor(), fanLayout.getStepDelay());
        break;
    }
  }

  /**
   * Returns a hand containing card clones from card of the current hand
   * that have the given suit. The cards are not removed from the current hand,
   * so care must be taken because of card duplication.
   * The cards are returned in rank order.
   * @param <T> the type of the suit
   * @param suit the requested suit
   * @return a hand of cloned cards with given suit
   */
  public <T extends Enum<T>> Hand extractCardsWithSuit(T suit)
  {
    ArrayList<Card> cards = getCardsWithSuit(suit);
    Hand hand = new Hand(deck);
    int i = 0;
    for (Card card : cards)
      hand.insert(card.clone(), false);
    return hand;
  }

  /**
   * Returns a list containing the card references of all cards with given suit.
   * The cards are returned in rank order.
   * @param <T> the type of the suit
   * @param suit the requested suit
   * @return a list of cloned cards with given suit
   */
  public <T extends Enum<T>> ArrayList<Card> getCardsWithSuit(T suit)
  {
    synchronized (cardList)
    {
      Hand hand = new Hand(deck);
      for (Card card : cardList)
      {
        if (card.getSuit() == suit)
          hand.cardList.add(card);
      }
      hand.sort(SortType.RANKPRIORITY, false);
      ArrayList<Card> list = new ArrayList<Card>();
      for (Card card : hand.cardList)
        list.add(card);
      return list;
    }
  }

  /**
   * Returns a hand containing card clones from card of the current hand
   * that have the given rank. The cards are not removed from the current hand,
   * so care must be taken because of card duplication.
   * The cards are returned in suit order.
   * @param <R> the type of the rank
   * @param rank the requested rank
   * @return a hand of cloned cards with given rank
   */
  public <R extends Enum<R>> Hand extractCardsWithRank(R rank)
  {
    ArrayList<Card> cards = getCardsWithRank(rank);
    Hand hand = new Hand(deck);
    int i = 0;
    for (Card card : cards)
      hand.insert(card.clone(), false);
    return hand;
  }

  /**
   * Returns a list containing the card references of all cards with given rank.
   * The cards are returned in suit order.
   * @param <R> the type of the rank
   * @param rank the requested rank
   * @return a list of cards with given rank
   */
  public <R extends Enum<R>> ArrayList<Card> getCardsWithRank(R rank)
  {
    synchronized (cardList)
    {
      Hand hand = new Hand(deck);
      for (Card card : cardList)
      {
        if (card.getRank() == rank)
          hand.cardList.add(card);
      }
      hand.sort(SortType.SUITPRIORITY, false);
      ArrayList<Card> list = new ArrayList<Card>();
      for (Card card : hand.cardList)
      {
        list.add(card);
      }
      return list;
    }
  }

  /**
   * Returns all sequences found in the current hand with mixed suits
   * with given length (>2). The returned card arrays contains references
   * to the cards in the current hand. The cards in the hands returned
   * are ordered with SortType.RANKPRIORITY.<br><br>
   * Cards belonging to a sequence longer than specified are not considered
   * to be part of the shorter sequence.
   * @param length the length of the requested sequence
   * @return a array of hands with all sequences found;
   * if no sequence is found or length < 3, the array has length = 0 (but is not null)
   */
  public ArrayList<Card[]> getSequences(int length)
  {
    ArrayList<Card[]> list = new ArrayList<Card[]>();
    Hand[] hands = extractSequences(length);
    for (Hand hand : hands)
    {
      Card[] cards = new Card[length];
      int i = 0;
      for (Card card : hand.getCardList())
        cards[i++] = getCard(card.getSuit(), card.getRank());
      list.add(cards);
    }
    return list;
  }

  /**
   * Returns all sequences found in the current hand with mixed suits
   * with given length (>2). The returned hands contains cloned cards.
   * The cards are not removed from the current hand, so care must be
   * taken because of card duplication. The cards in the hands returned
   * are ordered with SortType.RANKPRIORITY.<br><br>
   * Cards belonging to a sequence longer than specified are not considered
   * to be part of the shorter sequence.
   * @param length the length of the requested sequence
   * @return an array of hands with all sequences found;
   * if no sequence is found or length < 3, the array has length = 0 (but is not null)
   */
  public Hand[] extractSequences(int length)
  {
    if (length < 3)
      return new Hand[0];

    int nbRanks = deck.getNumberOfRanks();
    RankSet[] rankSets = new RankSet[nbRanks];

    for (int rankId = 0; rankId < nbRanks; rankId++)
    {
      RankSet rankSet = new RankSet();
      ArrayList<Card> rankList = getCardsWithRank(deck.getRank(rankId));
      for (Card card : rankList)
      {
        int suitId = deck.getSuitId(card.getSuit());
        rankSet.a[suitId] = true;
      }
      rankSets[rankId] = rankSet;
    }

    if (SharedConstants.DEBUG == SharedConstants.DEBUG_LEVEL_HIGH)
    {
      for (int rankId = 0; rankId < nbRanks; rankId++)
        System.out.println(rankId + ": " + rankSets[rankId]);
    }

    ArrayList<Integer> presentIds = new ArrayList<Integer>();
    for (int rankId = 0; rankId < nbRanks; rankId++)
    {
      if (rankSets[rankId].isPresent())
        presentIds.add(rankId);
    }

    if (SharedConstants.DEBUG == SharedConstants.DEBUG_LEVEL_HIGH)
      System.out.println("Present: " + presentIds);

    ArrayList<ArrayList<Integer>> allSequences = getAllSequences(presentIds);
    if (SharedConstants.DEBUG == SharedConstants.DEBUG_LEVEL_HIGH)
      System.out.println("All sequences: " + allSequences);

    ArrayList<ArrayList<Integer>> idSequences = new ArrayList<ArrayList<Integer>>();

    for (ArrayList<Integer> aSequence : allSequences)
    {

      ArrayList<Integer> idSequence = new ArrayList<Integer>();
      if (aSequence.size() == length)
      {
        for (int i = 0; i < aSequence.size(); i++)
          idSequence.add(aSequence.get(i));
        idSequences.add(idSequence);
      }
    }

    if (SharedConstants.DEBUG == SharedConstants.DEBUG_LEVEL_HIGH)
      System.out.println("Sequences of length " + length + ": " + idSequences);
    ArrayList<Hand> handList = new ArrayList<Hand>();
    ArrayList<Hand> handSequence = null;
    for (ArrayList<Integer> sequence : idSequences)
    {
      if (SharedConstants.DEBUG == SharedConstants.DEBUG_LEVEL_HIGH)
      {
        System.out.println("Sequence : " + sequence);
        for (Integer rankId : sequence)
        {
          System.out.println("rank id: " + rankId
            + "; " + rankSets[rankId]);
        }
      }
      handSequence = extractSequences(rankSets, sequence);
      if (SharedConstants.DEBUG == SharedConstants.DEBUG_LEVEL_HIGH)
        System.out.println("Hands: " + handSequence);
      for (Hand hand : handSequence)
        handList.add(hand);
    }
    if (handList == null)
      return new Hand[0];
    Hand[] hands = new Hand[handList.size()];
    for (int i = 0; i < handList.size(); i++)
      hands[i] = handList.get(i);
    return hands;
  }

  private ArrayList<Hand> extractSequences(RankSet[] rankSets, ArrayList<Integer> sequence)
  {
    int n = sequence.size();
    int startId = sequence.get(0);
    RankSet[] sets = new RankSet[n];
    for (int i = 0; i < n; i++)
    {
      int rankId = sequence.get(i);
      sets[i] = rankSets[rankId];
    }

    int nbSuits = deck.getNumberOfSuits();
    ArrayList<ArrayList<Integer>> trips = generate(n, nbSuits);
    ArrayList<Hand> hands = new ArrayList<Hand>();
    for (ArrayList<Integer> tupel : trips)
    {
      boolean isSequence = true;
      int i = 0;
      for (Integer suitId : tupel)
      {
        if (!sets[i].a[suitId])
          isSequence = false;
        i++;
      }
      Hand hand = new Hand(deck);
      if (isSequence)
      {
        for (int k = 0; k < n; k++)
        {
          int suitId = tupel.get(k);
          int rankId = startId + k;
          if (SharedConstants.DEBUG == SharedConstants.DEBUG_LEVEL_HIGH)
            System.out.println("got sequence:-- "
              + "suitId: " + suitId
              + "; rankId: " + rankId);
          Card card = getCard(deck.getSuit(suitId), deck.getRank(rankId)).clone();
          hand.insert(card, false);
          if (SharedConstants.DEBUG == SharedConstants.DEBUG_LEVEL_HIGH)
            System.out.println("Card: " + card);
        }
        hands.add(hand);
        if (SharedConstants.DEBUG == SharedConstants.DEBUG_LEVEL_HIGH)
          System.out.println("Hand: " + hand);
      }
    }
    return hands;
  }

  private ArrayList<ArrayList<Integer>> generate(int n, int z)
  // Generates a list of n-tupels with all possible numbers from 0 to z - 1
  {
    ArrayList<ArrayList<Integer>> list = new ArrayList<ArrayList<Integer>>();

    if (n == 1)
    {
      for (int i = 0; i < z; i++)
      {
        ArrayList<Integer> tupel = new ArrayList<Integer>();
        tupel.add(i);
        list.add(tupel);
      }
      return list;
    }

    // Recursive call
    ArrayList<ArrayList<Integer>> tmp = generate(n - 1, z);
    for (ArrayList<Integer> tupel : tmp)
    {
      for (int i = 0; i < z; i++)
      {
        ArrayList<Integer> newTupel = new ArrayList<Integer>(tupel);
        newTupel.add(i);
        list.add(newTupel);
      }
    }
    return list;
  }

  /**
   * Returns all sequences found in the current hand that have given suit
   * with given length (>2). The returned card arrays contains references
   * to the cards in the current hand. The cards in the hands returned
   * are ordered with SortType.RANKPRIORITY.<br><br>
   * Cards belonging to a sequence longer than specified are not considered
   * to be part of the shorter sequence.
   * @param <T> the type of the suit
   * @param suit the requested suit
   * @param length the length of the requested sequence
   * @return an array list of card arrays with all sequences found;
   * if no sequence is found or length < 3, the array has length = 0
   * (but is not null)
   */
  public <T extends Enum<T>> ArrayList<Card[]> getSequences(T suit, int length)
  {
    ArrayList<Card[]> list = new ArrayList<Card[]>();
    Hand[] hands = extractSequences(suit, length);
    for (Hand hand : hands)
    {
      Card[] cards = new Card[length];
      int i = 0;
      for (Card card : hand.getCardList())
        cards[i++] = getCard(card.getSuit(), card.getRank());
      list.add(cards);
    }
    return list;
  }

  /**
   * Returns all sequences found in the current hand that have given suit
   * with given length (>2). The returned hands contains cloned cards.
   * The original cards are not removed from the current hand, so care must be
   * taken because of card duplication. The cards in the hands returned
   * are ordered with SortType.RANKPRIORITY.<br><br>
   * Cards belonging to a sequence longer than specified are not considered
   * to be part of the shorter sequence.
   * @param <T> the type of the suit
   * @param suit the requested suit
   * @param length the length of the requested sequence
   * @return an array of hands with all sequences found;
   * if no sequence is found or length < 3, the array has length = 0
   * (but is not null)
   */
  public <T extends Enum<T>> Hand[] extractSequences(T suit, int length)
  {
    if (length < 3)
      return new Hand[0];

    ArrayList<Hand> handList = new ArrayList<Hand>();
    ArrayList<Card> sameSuit = getCardsWithSuit(suit);
    ArrayList<Integer> intList = new ArrayList<Integer>();
    for (Card card : sameSuit)
      intList.add(deck.getRankId(card.getRank()));

    ArrayList<ArrayList<Integer>> allSequences =
      getAllSequences(intList);

    for (ArrayList<Integer> aSequence : allSequences)
    {
      if (aSequence.size() == length)
      {
        Hand hand = new Hand(deck);
        for (int i = 0; i < aSequence.size(); i++)
        {
          Card card = new Card(deck, suit, deck.getRank(aSequence.get(i)));
          hand.insert(card, false);
        }
        handList.add(hand);
      }
    }

    Hand[] hands = new Hand[handList.size()];
    for (int i = 0; i < handList.size(); i++)
    {
      hands[i] = handList.get(i);
    }
    return hands;
  }

  private ArrayList<ArrayList<Integer>> getAllSequences(ArrayList<Integer> list)
  {
    ArrayList<Integer> tmp = new ArrayList<Integer>(list);  // Clone list
    ArrayList<ArrayList<Integer>> allSequences = new ArrayList<ArrayList<Integer>>();
    while (!tmp.isEmpty())
    {
      ArrayList<Integer> next = getNextSequence(tmp);
      allSequences.add(next);
    }
    return allSequences;
  }

  private ArrayList<Integer> getNextSequence(ArrayList<Integer> list)
  // Takes the given list and begins searching for a sequence of any length
  // until sequence is interrupted. Returns the sequence and
  // strips the original list with the sequence found.
  // If the given list is empty, returns an empty list.
  // Assumption: the given list is ordered
  {
    ArrayList<Integer> tmp = new ArrayList<Integer>();
    if (list.isEmpty())
      return tmp;

    // Add first element
    int index = 0;
    int number = list.get(index);
    tmp.add(number);

    // Check for sequence
    index++;
    while (index < list.size())
    {
      int nextNumber = list.get(index);
      if (nextNumber == number + 1)  // Sequence continues
      {
        tmp.add(nextNumber);
        number = nextNumber;
        index++;
      }
      else  // Sequence interrupted
        break;
    }

    // Strip original list
    for (int i = 0; i < tmp.size(); i++)
      list.remove(0);

    return tmp;
  }

  /**
   * Returns all pairs (two cards with same rank) found in the current hand. 
   * The returned card arrays have size 2 and  contains the card references
   * of the pairs. If the cards are part of a trip or quad they are
   * not returned as part of a pair. The array list elements are in rank order,
   * the cards in each pair are in suit order.
   * @return an array list of card pairs;
   * if no pair is found, the array list has size = 0 (but is not null)
   */
  public ArrayList<Card[]> getPairs()
  {
    return getSameRank(2);
  }

  /**
   * Returns all trips (three cards with same rank) found in the current hand. 
   * The returned card arrays have size 3 and  contains the card references
   * of the trips. If the cards are part of a quad they are not returned as
   * part of a trip. The array list elements are in rank order, the cards
   * in each trip are in suit order.
   * @return an array list of card trips;
   * if no trip is found, the array list has size = 0 (but is not null)
   */
  public ArrayList<Card[]> getTrips()
  {
    return getSameRank(3);
  }

  /**
   * Returns all quads (four cards with same rank) found in the current hand. 
   * The returned card arrays have size 4 and  contains the card references 
   * of the quads. The array list elements are in rank order, the cards in
   * each quad are in suit order.
   * @return an array list of card quads;
   * if no quad is found, the array list has size = 0 (but is not null)
   */
  public ArrayList<Card[]> getQuads()
  {
    return getSameRank(4);
  }

  /**
   * Returns all hands with pairs (two cards with same rank) found in the
   * current hand. The returned hands contains cloned cards. The original cards are not
   * removed from the current hand, so care must be taken because of card
   * duplication.
   * The array list elements are in rank order, the cards in each quad are in suit order.
   * @return an array of hand of card pairs;
   * if no pair is found, the array has size = 0 (but is not null)
   */
  public Hand[] extractPairs()
  {
    return extractSameRank(2);
  }

  /**
   * Returns all hands with trips (three cards with same rank) found in the
   * current hand. The returned hands contains cloned cards. The original cards
   * are not removed from the current hand, so care must be taken because
   * of card duplication.
   * The array list elements are in rank order, the cards in each quad are in suit order.
   * @return an array of hand of card trips;
   * if no trip is found, the array has size = 0 (but is not null)
   */
  public Hand[] extractTrips()
  {
    return extractSameRank(3);
  }

  /**
   * Returns all hands with quads (four cards with same rank) found in the
   * current hand. The returned hands contains cloned cards.
   * The original cards are not removed from the current hand, so care must be taken
   * because of card duplication.
   * The array list elements are in rank order, the cards in each quad are in suit order.
   * @return an array of hand of card quads;
   * if no quad is found, the array has size = 0 (but is not null)
   */
  public Hand[] extractQuads()
  {
    return extractSameRank(4);
  }

  private Hand[] extractSameRank(int nbCards)
  {
    ArrayList<Card[]> same = getSameRank(nbCards);
    Hand[] hands = new Hand[same.size()];
    int i = 0;
    for (Card[] cards : same)
    {
      Hand hand = new Hand(deck);
      for (Card card : cards)
        hand.insert(card.clone(), false);
      hands[i++] = hand;
    }
    return hands;
  }

  private ArrayList<Card[]> getSameRank(int nbCards)
  {
    ArrayList<Card[]> list = new ArrayList<Card[]>();
    for (Enum rank : deck.getRanks())
    {
      ArrayList<Card> same = getCardsWithRank(rank);
      if (same.size() == nbCards)
      {
        Card[] cards = new Card[nbCards];
        for (int i = 0; i < nbCards; i++)
          cards[i] = same.get(i);
        list.add(cards);
      }
    }
    return list;
  }

  /**
   * Returns the reference of the current HandLayout.
   * @return the current HandLayout; null, if the layout is not set.
   */
  public HandLayout getLayout()
  {
    return handLayout;
  }

  /**
   * Returns a clone of the hand location.
   * @return the hand location; null, if the hand location is not yet set (draw() not
   * yet called)
   */
  public Location getHandLocation()
  {
    if (handLocation == null)
      return null;
    return handLocation.clone();
  }

  /**
   * Divides the current hand (as a deck of cards) into two batches and reassembles
   * the batches in reverse order.
   * @param nb the number of cards in the first part. If less than one or greater
   * than the number of cards nothing happens
   * @param doDraw if true, the hand is drawn
   */
  public void cut(int nb, boolean doDraw)
  {
    synchronized (cardList)
    {
      if (nb < 1 || nb > cardList.size() - 1)
        return;
      ArrayList<Card> topList = new ArrayList<Card>();
      for (int i = 0; i < nb; i++)
      {
        topList.add(cardList.get(0));
        cardList.remove(0);
      }

      for (int i = 0; i < nb; i++)
        cardList.add(topList.get(i));
    }
    if (doDraw)
      draw();
  }

  /**
   * Moves a randomly selected batch of cards from the source hand to the
   * target hand.
   * The cards are one-by-one randomly selected in the source hand, removed
   * from the hand and inserted it in the target hand.
   * If nb is greater or equal to the total number of cards in the source hand,
   * all cards are transferred. The target hand is randomly shuffled. If doDraw = false
   * neither the source hand nor the target hand are redrawn, but the removed cards
   * disappears from a visible source hand.
   * @param nb the number of cards to take out. If nb less or equals zero,
   * an empty hand is returned; if nb is greater or equal to the number of
   * cards in the given hand, a hand with all cards is returned
   * @param source the hand where to take the cards
   * @param target the hand where to insert the cards
   * @param doDraw if true, the source hand and the target hand are redrawn
   */
  public static void randomBatchTransfer(int nb, Hand source, Hand target, boolean doDraw)
  {
    if (nb <= 0)
      return;

    int nbCards = source.getNumberOfCards();
    if (nb > nbCards)
      nb = nbCards;

    for (int i = 0; i < nb; i++)
    {
      int index = (int)(source.getNumberOfCards() * Math.random());
      Card card = source.get(index);
      source.remove(card, false);
      target.insert(card, false);
    }
    if (doDraw)
    {
      source.draw();
      target.draw();
    }
  }

  /**
   * Transfers the card from the current hand to the given target hand using
   * the currently defined target area. If no target area is defined, the target
   * hand location (that is defined, when the hand is drawn)
   * is used as target location and the card transfer is animated.<br><br>
   *
   * For more advanced transferring option, define a target area with appropriate
   * parameters.<br><br>
   *
   * The card is removed from from the current hand in inserted into
   * the target hand. If the card is not part of this hand, nothing happens.<br><br>
   *
   * The method blocks until the card arrives at the target.
   * Once arrived at the target an atTarget() notification is triggered. <br><br>
   *
   * The card is visible during the transfer and remains visible at the target
   * location, even if no draw() of the target hand is invoked.
   * @param card the card to transfer
   * @param targetHand the hand where to transfer the card
   * @param doDraw if true, the current hand and the target hand are drawn
   * @see TargetArea
   */
  public void transfer(Card card, Hand targetHand, boolean doDraw)
  {
    transfer(card, targetHand, true, doDraw);
  }

  /**
   * Same as transfer(card, targetHand, doDraw), but
   * the methods returs immediately. Use the atTarget callback to get
   * a notification when the card arrived at its destination.
   * @param card the card to transfer
   * @param targetHand the hand where to transfer the card
   * @param doDraw if true, the current hand and the target hand are drawn
   */
  public void transferNonBlocking(final Card card, final Hand targetHand, boolean doDraw)
  {
    transfer(card, targetHand, false, doDraw);
  }

  private void transfer(final Card card, final Hand targetHand, final boolean blocking,
    final boolean doDraw)
  {
    if (cardGame == null)
    {
      Deck.fail("Error when calling Hand.transfer()."
        + "\nCardGame reference null,"
        + "\nprobably because setView() was never called."
        + "\nApplication will terminate.");
    }
    if (!contains(card))
      return;
    if (!isTargetAreaDefined) // Target area not yet set:
    // we try to jump to target hand location
    {
      if (targetHand.handLocation == null)
        Deck.fail("Error when calling Hand.transfer()."
          + "\nTarget area not defined and target hand location unknown."
          + "\nApplication will terminate.");

      targetArea = new TargetArea(targetHand.handLocation);
    }
    final boolean touchEnabled = isTouchEnabled;
    isTouchEnabled = false;  // Disable momentarily touching the hand

    CardActor cardActor = card.getCardActor();
    int idVisible = cardActor.getIdVisible();
    Location loc = cardActor.getLocation();
    cardActor.removeSelf();  // Remove from game grid
    int rotAngle = 0;
    switch (targetArea.getCardOrientation())
    {
      case EAST:
        rotAngle = 90;
        break;
      case SOUTH:
        rotAngle = 180;
        break;
      case WEST:
        rotAngle = 270;
        break;
      case NORTH:
        rotAngle = 0;
        break;
    }
    card.associateActor(card.getScaleFactor(), rotAngle);
    cardActor = card.getCardActor();
    cardActor.show(idVisible);  // Restore old visibility
    if (cardGame != null)
      cardGame.addActorNoRefresh(cardActor, loc);  // Shows new single card to be moved
    if (!targetArea.isOnTop())
      targetHand.draw();
    cardList.remove(card);  // Remove card from source hand
    if (targetArea.getSlideStep() > 0 && !GameGrid.isDisposed())  // Animate transfer
    {
      if (blocking)
      {
        card.getCardActor().slideToTarget(
          targetArea.getTargetLocation(), targetArea.getSlideStep(), true);
        finishTransfer(targetHand, card, touchEnabled, doDraw);
      }
      else  // non-blocking
      {
        // Transfer information to card actor in order to call finishTransfer()
        // when the card arrives at the target
        card.getCardActor().
          setFinishTransfer(this, targetHand, card, touchEnabled, doDraw);
        card.getCardActor().
          slideToTarget(targetArea.getTargetLocation(),
          targetArea.getSlideStep(), false);  // non-blocking
      }
    }
    else  // slideStep = 0 -> jump
    {
      card.getCardActor().setLocation(targetArea.getTargetLocation());
      finishTransfer(targetHand, card, touchEnabled, doDraw);
    }


  }

  protected void finishTransfer(Hand targetHand, Card card,
    boolean touchEnabled, boolean doDraw)
  {
    targetHand.insert(card, false);  // Insert into new hand
    if (doDraw)
    {
      draw();
      targetHand.draw();
    }
    
    isTouchEnabled = touchEnabled; // Reenable touch if necessary

    if (cardListener != null)
    {
      if (targetArea.getTargetLocation() == null)
        cardListener.atTarget(card, handLocation.clone());
      else
        cardListener.atTarget(card, targetArea.getTargetLocation());
    }
  }

  /**
   * Returns card reference of card in this hand with given card number. The card
   * face up or down, depending if the card is shown face-up or face-down
   * in the hand.
   * @param cardNumber the card number of the requested card
   * @return the card with given card number; null, if card is not part of the hand
   */
  public Card getCard(int cardNumber)
  {
    synchronized (cardList)
    {
      Card card = new Card(deck, cardNumber);
      for (Card c : cardList)
      {
        if (c.equals(card))
          return c;
      }
      return null;
    }
  }

  /**
   * Returns card reference of the card in this hand with given suit and rank.
   * The card is face up or down, depending if the card is shown face-up or face-down
   * in the hand.
   * @param <T> the Enum type of the suit
   * @param <R> the Enum type of the rank
   * @param suit the card suit
   * @param rank the card rank
   * @return the card with given suit and rank;
   * null, if card is not part of the hand
   */
  public <T extends Enum<T>, R extends Enum<R>> Card getCard(T suit, R rank)
  {
    synchronized (cardList)
    {
      Card card = new Card(deck, suit, rank);
      for (Card c : cardList)
      {
        if (c.equals(card))
          return c;
      }
      return null;
    }
  }

}
