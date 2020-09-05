// GGButtonEvent.java

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

class GGButtonEvent extends Actor
{
  public enum EventType
  {
    PRESS, RELEASE, TOGGLE, UNTOGGLE, CLICK, CHECK, UNCHECK,
    SELECT, DESELECT, ENTER, EXIT
  };

  public EventType eventType;
  public GGButtonBase button;

  public GGButtonEvent(EventType eventType, GGButtonBase button)
  {
    this.eventType = eventType;
    this.button = button;
  }
}
