// GGBorderListener.java

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
 * Declarations of the notification method called when the actor is at 
 * a border cell.<br>
 * (Cannot be used with Jython's constructor callback registration)
 */
public interface GGBorderListener extends java.util.EventListener
{

  /**
   * Event callback method called when the actor enters a border cell.
   * @param actor the actor that was moved in a border cell
   */
  public void nearBorder(Actor actor, Location location);

}
