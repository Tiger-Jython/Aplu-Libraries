// GGMouseListener.java

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
 * Declarations of the notification method called by mouse events.<br>
 * (Cannot be used with Jython's constructor callback registration)
 */
public interface GGMouseListener extends java.util.EventListener
{
  /**
   * Event callback method called when a mouse event occurs.
   * The parameter is used to get the type of the mouse event and
   * the current mouse cursor position.
   * @param mouse the mouse reference to get to get information about the event
   * @return true, if the event is consumed, so following listeners
   * in the GGMouseListener sequence will not get the event
   */
  boolean mouseEvent(GGMouse mouse);
}
