// TimerListener.java

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
package ch.aplu.util;

/**
 * The listener interface for receiving timer events.
 * The class that is interested in processing timer event either implements this interface
 * (and the method timeElapesed()) or extends the abstract TimerAdapter class (and overridins timeElapsed()).
 */
public interface TimerListener extends java.util.EventListener
{
  /**
   * Invoked when the time interval is elapsed.
   * If true is returned, the timer restarts automatically, otherwise the timer stops.
   */
  boolean timeElapsed();

}
