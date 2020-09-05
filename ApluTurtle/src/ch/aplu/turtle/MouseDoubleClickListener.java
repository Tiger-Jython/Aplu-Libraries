// MouseDoubleClickListener.java

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

import java.awt.event.MouseEvent;

/**
 * The listener interface for receiving mouse double-click events.
 * See TurtleFrame.setDoubleClickDelay() to get information about 
 * how notifyClick() and notifyDoubleClick() work together. Keep in mind that 
 * there is a delay until the notifyClick is reported because the system has to 
 * check if a second click follows that is interpreted as double-click. 
 */
public interface MouseDoubleClickListener extends java.util.EventListener
{
  /**
   * Invoked when a click is detected. 
   */
  void notifyClick(MouseEvent evt);

  /**
   * Invoked when a double-click is detected. 
   */
  void notifyDoubleClick(MouseEvent evt);
}
