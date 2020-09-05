// SoundConverter.java

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
 * Interface for easy integration of a sound converter to SoundPlayer and
 * SoundPlayerExt classes. Typically a converter may apply some sort of
 * 'filtering', e.g.  change the pitch with same tempo or
 * change the tempo with same pitch.
 *
 * The sound is converted by data junks. putSamples() feeds the converter with
 * a new data junk. receiveSamples() returns the converted samples. Because there
 * may be same convertion delay, receiveSamples() may return none, a part or
 * all converted samples of data previously sent to the converter.
 * If the number of available bytes exceeds 'maxBytes', receiveSamples() is
 * called several times until all data is retrieved.
 *
 * The input and output format to the converter must have PCM_SIGNED mono or
 * stereo format (16 bit little endian).
 */
public interface SoundConverter
{
   /**
   * Notification from the SoundPlayer that the given number of
   * samples is available in inBuffer to be converted by the
   * SoundConverter. 
   * If mono, numBytes is a multiple of 2 (2 bytes per sample)
   * If stereo, numBytes is be multiple of 4 (4 bytes per sample)
   * The value depends on the frame size.
   */
  void putSamples(byte[] inBuffer, int nbBytes);

  /**
   * Notification from the SoundPlayer to get back the converted data in
   * outBuffer. The SoundPlayer then plays the passed nbBytes samples 
   * and calls receiveSamples() again, until receiveSamples() returns 0.
   * The data format must be maintained:
   *   If mono, nbBytes is a multiple of 2 (2 bytes per sample)
   *   If stereo, nbBytes is be multiple of 4 (4 bytes per sample)
   */
  int receiveSamples(byte[] outBuffer, int nbBytes);

  /**
   * Callback method called each time before the new samples from the inBuffer
   * are fed into the converter.
   * May be used to change converter parameters in realtime.
   */
  void update();

  /**
   * Callback method called when no more data is available before closing
   * SoundPlayer's output to the sound device.
   * May be used to flush the sound converter's buffer. Remaining sound
   * data is sent to the sound device and the device is closed.
   */
  void cleanup();

}
