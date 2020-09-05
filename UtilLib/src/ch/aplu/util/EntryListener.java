// EntryListener.java

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
 * The listener interface for processing entry dialog events.
 */
public interface EntryListener extends java.util.EventListener
{
  /**
   * Invoked when one of the widgets is touched by the mouse.
   * @param item the EntryItem that is touched
   */
  public void touched(EntryItem item);

  /**
   * Invoked when one of the widgets changes its state.
   * @param item the EntryItem that is modified
   */
  public void modified(EntryItem item);
}
