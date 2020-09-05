// CardGame.java

/*
This software is part of the JCardGame package.
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
import java.awt.Color;

/**
 * A specialized GameGrid class used for card games.
 */
public class CardGame extends GameGrid
{
  /**
   * Constructs the game playground with 10 by 10 cells (60 pixels wide).
   * No surrounding frame window is created. Only to be used for a
   * embedded playground in a user defined frame window or an applet.
   * To initialize the playground, use the GameGrid initializer methods, e.g.<br><br>
   * <code>
   * CardGame cg = new CardGame();<br>
   * cg.setCellSize(1);<br>
   * cg.setNbHorzCells(600);<br>
   * cg.setNbVertCells(600);<br>
   * cg.setSimulationPeriod(30);<br>
   * cg.setBgColor(new Color(20, 80, 0));<br>
   * cg.doRun();
   * </code><br><br>
   * All card animation must be performed in an own worker thread that runs 
   * after the constructor has finished.
   */
  public CardGame()
  {
    super();
  }
  
  /**
   * Same as CardGame(width, height, bgColor, statusHeight, simulationPeriod)
   * with bgColor = RGB(20, 80, 0) (dark green), simulationPeriod = 30
   * and statusHeight = 0 (no status bar).
   * @param width the horizontal pixel size of the game grid window
   * @param height the vertical pixel size of the game grid window
   */
  public CardGame(int width, int height)
  {
    this(width, height, new Color(20, 80, 0), 0, 30);
  }

  /**
   * Same as CardGame(width, height, bgColor, statusHeight, simulationPeriod)
   * with bgColor = RGB(20, 80, 0) (dark green), simulationPeriod = 30.
   * @param width the horizontal pixel size of the game grid window
   * @param height the vertical pixel size of the game grid window
   * @param statusHeight the height of the status bar in pixels; if less or
   * equal zero, no status bar is displayed
   */
  public CardGame(int width, int height, int statusHeight)
  {
    this(width, height, new Color(20, 80, 0), 30, statusHeight);
  }

  /**
   * Same as CardGame(width, height, bgColor, statusHeight, simulationPeriod)
   * with simulationPeriod = 30 and statusHeight = 0 (no status bar).
   * @param width the horizontal pixel size of the game grid window
   * @param height the vertical pixel size of the game grid window
   * @param bgColor the background color of the game grid window
   */
  public CardGame(int width, int height, Color bgColor)
  {
    this(width, height, bgColor, 0, 30);
  }

  /**
   * Constructs and shows a game grid window with the given horizontal and
   * vertical pixel size (cell size = 1, no navigation bar).
   * Sets the background color, the simulation period and
   * and adds a status bar of given statusHeight (if statusHeight > 0).<br>
   * GameGrid.doRun() is called to start the simulation cycling.
   * @param width the horizontal pixel size of the game grid window
   * @param height the vertical pixel size of the game grid window
   * @param bgColor the background color of the game grid window
   * @param statusHeight the height of the status bar in pixels; if less or
   * equal zero, no status bar is displayed
   * @param simulationPeriod the simulation period used for the simulation cycling
   */
  public CardGame(int width, int height, Color bgColor, int statusHeight,
     int simulationPeriod)
  {
    super(width, height, 1, false);
    setSimulationPeriod(30);
    setBgColor(bgColor);
    if (statusHeight > 0)
      addStatusBar(statusHeight);
    doRun();
    show();
  }
}
