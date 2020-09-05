// GGWindowStateListener.java

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
package ch.aplu.jgamegrid;

/**
 * Declaration of notifications method that reports when the state of
 * the window changes.
 * Useful to snap another game grid window.
 */
public interface GGWindowStateListener extends java.util.EventListener
{
  /**
   * Event callback method called when the window is moved.
   * @param x the x-coordinate of the upper left vertex
   * @param y the y-coordinate of the upper left vertex
   */
  public void windowMoved(int x, int y);

  /**
   * Event callback method called when the window is iconified.
   */
  public void windowIconified();

  /**
   * Event callback method called when the window is deiconified.
   */
  public void windowDeiconified();

  /**
   * Event callback method called when the window is activated.
   */
  public void windowActivated();

  /**
   * Event callback method called when the window is deactivated.
   */
  public void windowDeactivated();
}
