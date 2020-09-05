// ExitListener.java

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
 * The listener interface for processing the TurtleFrame close button event.
 * The class that is interested in processing this event must implement this interface
 * (and the method notifyExit()) and register itself by calling addExitListener()
 */
public interface ExitListener extends java.util.EventListener
{
  /**
   * Invoked when the close button in the title bar is pressed.
   * If an ExitListener is registered, other closing modes are disabled.
   */
  public void notifyExit();

}
