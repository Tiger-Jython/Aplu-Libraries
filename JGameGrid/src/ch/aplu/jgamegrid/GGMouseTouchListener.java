// GGMouseTouchListener.java

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

import java.awt.Point;

/**
 * Declarations of the notification method called when the mouse interacts with
 * the actor's mouse touch area.<br>
 * (Cannot be used with Jython's constructor callback registration)
 */
public interface GGMouseTouchListener extends java.util.EventListener
{
  /**
   * Event callback method to report events with the mouse touch area
   * of an actor's sprite. Mouse touch area types available: IMAGE (default,
   * events on non-transparent pixels of sprite image),
   * RECTANGLE, CIRCLE. The non-default types can be selected for each sprite ID
   * with Actor.setMouseTouchRectangle(), Actor.setMouseTouchCircle().
   * The parameter mouse is used to get the type of the mouse event and
   * the current mouse cursor position. spot contains the event coordinates relative
   * to the mouse touch area with the following fixed coordinate system:
   * x-axis to the left, y-axis downward, origin at center of mouse touch area.
   * The actor's location offset is respected.<br><br>
   * If the mouse touch area of several actors overlays, all actors get the
   * notification unless the MouseTouchListener is registered by setting the
   * onTopOnly flag to true.<br><br>
   * Move events are not reported. Drag events ar only reported if the
   * mouse was pressed inside the mouse touch area.
   * @param actor the reference of the actor that reports the touch
   * @param mouse the mouse reference to get to get information about the event
   * @param spot the pixel coordinates relative to the mouse touch area where the event occurred
   */
  void mouseTouched(Actor actor, GGMouse mouse, Point spot);
}
