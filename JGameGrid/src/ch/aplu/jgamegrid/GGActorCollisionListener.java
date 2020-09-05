// GGActorCollisionListener.java

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
 * Declarations of the notification method called when two actors collide.<br>
 * (Cannot be used with Jython's constructor callback registration)
 */
public interface GGActorCollisionListener extends java.util.EventListener
{
  /**
   * Event callback method called when actors are colliding.
   * The collision is checked in every simulation cycle and the notification
   * is called before the actor's act().
   * @param actor1 the active actor the checks his collision candidates for possible collisions
   * @param actor2 the passive actor that sustain the collision
   * @return nb of simulation cycles to wait until collision is rearmed
   */
  public int collide(Actor actor1, Actor actor2);

}
