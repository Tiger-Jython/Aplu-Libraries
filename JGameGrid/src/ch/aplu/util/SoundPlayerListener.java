// SoundPlayerListener.java

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
 * The listener interface for receiving StreamingPlayer notification.
 * The class that is interested in getting the notification either implements this interface
 * and registers it with addPlayerListener().
 */
public interface SoundPlayerListener extends java.util.EventListener
{
  /**
   * Invoked when the player stops playing.
   * The reason may be:<br>
   * 0: playing from start<br>
   * 1: resume playing after pause<br>
   * 2: pausing<br>
   * 3: stopping<br>
   * 4: end of sound resource<br><br>
   * mixerIndex is the index of the mixer (sound device). If only the default
   * sound device is used, it may be ignored.
   */
  void notifySoundPlayerStateChange(int reason, int mixerIndex);

}
