// MouseHitXListener.java

/*
 This software is part of the JEX (Java Exemplarisch) Utility Library.
 It is Open Source Free Software, so you may
 - run the code for any purpose
 - study how the code works and adapt it to your needs
 - integrate all or parts of the code in your own programs
 - redistribute copies of the code
 - improve the code and release your improvements to the public
 However the use of the code is entirely your responsibility.
 */
package ch.aplu.turtle;

/**
 * The listener interface for receiving mouse press events. 
 * Consecutive callbacks are ignored until the former callback
 * finishes.
 */
public interface MouseHitXListener extends java.util.EventListener
{
  /**
   * Method invoked in a separate thread when the left mouse buttons is pressed.
   * Consecutive callbacks are ignored until the former callback
   * finishes.
   */
   void mouseHitX(double x, double y);
}
