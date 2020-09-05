// SharedConstants.java

/*
This software is part of the JGameGrid package.
It is Open Source Free Software, so you may
- run the code for any purpose
- study how the code works and adapt it to your needs
- integrate all or parts of the code in your own programs
- redistribute copies of the code
- improve the code and release your improvements to the public
However the use of the code is entirely your responsibility.

Author: Aegidius Pluess, www.aplu.ch
*/

/* History:
 * V1.00 - Mar 2011: - First official distribution
 * V1.01 - Mar 2011: - JavaDoc revisted
 *                   - Modified: all indentifiers containing ..pips.. to ..values..
 * V1.02 - Mar 2011  - Modified: StackLayout now uses all cards in stack (not only
 *                     top two)
 * V1.03 - Mar 2011  - Added: Deck.getCardNumber(), Card.getCardNumber()
 *                   - Added: ctor Card(deck, cardNb)
 * V1.04 - Apr 2011  - Modified/added: Hand.transferNonBlocking()
 *                   - Added: Errors now show a stack trace
 * V1.05 - Apr 2011  - Fixed: sporadic null-pointer exception when animate layouts
 *                   - Added: Hand.getCard()
 * V1.06 - Apr 2011  - Modified: CardGame ctor now blocks until the window is visible
 *                   - Fixed: side effect in Hand.getMaxPosition()
 *                   - Fixed: side effect in Card.clone()
 * V1.07 - Apr 2011  - Added Deck.toHand()
 *                   - Hand.contains() and Hand.remove(Card card, boolean doDraw)
 *                     now check for same suit/rank and not same reference
 * V1.08 - Apr 2011  - Modified Hand.getPairs() returns references now, added Hand.extractPairs()
 *                   - Modified Hand.getTrips() returns references now, added Hand.extractTrips()
 *                   - Modified Hand.getQuads() returns references now, added Hand.extractQuads()
 *                   - Modified Hand.getSequences() returns references now, added Hand.extractSequences()
 *                   - Added Hand.getTargetArea()
 *                   - Modified: CardGame ctor does no more block until visible
 * V1.09 - May 2011  - Modified: Card.equals() now overrides Object.equals()
 * V1.10 - May 2011  - Added: Card.cloneAndAdd()
 *                   - Modified: Card.slideToTarget() now generates an error message
 *                     if card actor is not part of the game grid
 *                   - Fixed: FanLayout to east now supported
 *                   - Modified: Hand.remove(Card, doDraw) and Hand.removeAll()
 *                     now refresh the game grid (because actors are removed)
 *                   - Fixed: Hand.draw() returns immediately for an empty hand
 * V1.11 - Jun 2011  - Modified: Hand click to put card on top synchronized now
 * V1.12 - Jun 2011  - Fixed: Hand.handLocation now set also when hand is empty 
 * V1.13 - Apr 2012  - Added: default (parameterless) ctor of class CardGame
 * V1.14 - Jul 2012  - Methods in class Hand synchronized(cardList)
 * V1.15 - Aug 2012  - Fixed: Card.transferNonBlocking() now works
 * V1.16 - Aug 2012  - Removed: ctor Hand(CardGame cardGame, HandLayout layout)
 *                   - Modified: Card.transferNonBlocking() takes boolean doDraw now
 *                   - Modified: Hand.transferNonBlocking() takes boolean doDraw now
 * V1.17 - Sep 2012  - Modified: Hand.transfer() animation with new implementation
 * V1.18 - Feb 2013  - Added: Card.hashCode() in accordance with equals()
 * V1.19 - Apr 2013  - Modified: Monitor.putSleep() in class Hand now uses 
 *                     static Monitor object
 *                   - Modified: blocking methods now check for disposed game grid
 * V1.20 - Jul 2014  - Added: class PackageInfo
*/

package ch.aplu.jcardgame;

interface SharedConstants
{
  int DEBUG_LEVEL_OFF = 0;
  int DEBUG_LEVEL_LOW = 1;
  int DEBUG_LEVEL_MEDIUM = 2;
  int DEBUG_LEVEL_HIGH = 3;

  int DEBUG = DEBUG_LEVEL_OFF;

  String ABOUT =
    "2003-2014 Aegidius Pluess\n" +
    "OpenSource Free Software\n" +
    "http://www.aplu.ch\n" +
    "All rights reserved";
  String VERSION = "1.20 - July 2014";
}
