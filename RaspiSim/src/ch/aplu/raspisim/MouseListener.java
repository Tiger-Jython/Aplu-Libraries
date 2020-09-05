// MouseListener.java

/*
 This software is part of the RaspiSim library.
 It is Open Source Free Software, so you may
 - run the code for any purpose
 - study how the code works and adapt it to your needs
 - integrate all or parts of the code in your own programs
 - redistribute copies of the code
 - improve the code and release your improvements to the public
 However the use of the code is entirely your responsibility.

 Author: Aegidius Pluess, www.aplu.ch
 */
package ch.aplu.raspisim;

import ch.aplu.jgamegrid.*;

/**
 * Interface with declarations of a callback method to detect robot-obstacle collisions.
 */
public interface MouseListener extends java.util.EventListener
{
  /**
   * Called when the left mouse button is pressed.
   * @param gg a reference to the GameGrid
   * @param x the x-coordinate of the mouse cursor
   * @param y the y-coordinate of the mouse cursor
   */
  public void mousePressed(GameGrid gg, int x, int y);

  /**
   * Called when the left mouse button is released.
   * @param gg a reference to the GameGrid
   * @param x the x-coordinate of the mouse cursor
   * @param y the y-coordinate of the mouse cursor
   */
  public void mouseReleased(GameGrid gg, int x, int y);

  /**
   * Called when the left mouse is dragged .
   * @param gg a reference to the GameGrid
   * @param x the x-coordinate of the mouse cursor
   * @param y the y-coordinate of the mouse cursor
   */
  public void mouseDragged(GameGrid gg, int x, int y);
}
