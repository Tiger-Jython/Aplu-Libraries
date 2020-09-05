// SoundSampleListener.java

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
 * Declaration of a SoundRecorder callback method.
 */
public interface SoundSampleListener
{
  /**
   * Event callback method called by the sound recording thread each
   * time a new sound sample is copied into the sound sample buffer.
   * @param count the number of bytes copied.
   */
  public void sampleReceived(int count);

}
