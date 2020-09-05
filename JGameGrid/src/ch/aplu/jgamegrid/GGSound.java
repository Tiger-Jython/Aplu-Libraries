// GGSound.java

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
 * Enumeration of all system sounds.
 */
public enum GGSound
{
  /** 
   * An empty sound clip that may be used to initialize the sound system.
   * When playing a sound the first time, there is some delay caused by the time
   * the sound system needs to initialize.
   */
  DUMMY("wav/_dummy.wav"),
  /**
   * The quack of a frog.
   */
  FROG("wav/_frog.wav"),
  /**
   * The sound of a click.
   */
  CLICK("wav/_click.wav"),
  /**
   * A fading out sound.
   */
  FADE("wav/_fade.wav"),
  /**
   * A notification sound.
   */
  NOTIFY("wav/_notify.wav"),
  /**
   * The sound of an explosion.
   */
  EXPLODE("wav/_explode.wav"),
  /**
   * The enjoying sound of an eating person.
   */
  MMM("wav/_mmm.wav"),
  /**
   * The sound of ping.
   */
  PING("wav/_ping.wav"),
  /**
   * The whistling of a bird.
   */
  BIRD("wav/_bird.wav"),
  /**
   * The sound of boing.
   */
  BOING("wav/_boing.wav");

  private String path;

  private GGSound(String path)
  {
    this.path = path;
  }

  /**
   * Returns the path to the sound file.
   * @return the path to the sound file
   */
  public String getPath()
  {
    return path;
  }
};


