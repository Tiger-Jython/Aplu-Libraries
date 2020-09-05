// TurtleHitListener.java

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
 */
public interface TurtleHitListener extends java.util.EventListener
{
  /**
   * Invoked in a separate thread when the left mouse buttons is pressed and
   * the mouse cursor is inside the turtle image.
   * @param t the reference of the turtle that is hit
   * @param x the x coordinate of the mouse cursor (inside the turtle image)
   * @param y the x coordinate of the mouse cursor (inside the turtle image)
   */
  void turtleHit(Turtle t, double x, double y);
}
