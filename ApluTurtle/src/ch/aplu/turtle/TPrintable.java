// TPrintable.java

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
 * Interface for printing on an attached printer.
 * Normally an application uses a Turtle and implements this interface.
 * draw() should contain all drawing operations into the
 * Turtle's Playground. The printing occures, when Turtle's print() is
 * called.<br><br>
 */
public interface TPrintable
{
  /**
   * This method must perform all drawing operations.
   * Be aware that for some undocumented reason
   * draw() is called twice. So be sure you initialize it correctly.
   */
  public void draw();

}