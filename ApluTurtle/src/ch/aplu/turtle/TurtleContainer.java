// TurtleContainer.java

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

import ch.aplu.turtle.*;

/** 
 * Implement this interface to define your own top-level container
 * which contains turtles.
 */
public interface TurtleContainer
{
  /**
   * As the <code>Turtle</code>s live in a Playground actually, 
   * you must have access to it. 
   */
  Playground getPlayground();

}
