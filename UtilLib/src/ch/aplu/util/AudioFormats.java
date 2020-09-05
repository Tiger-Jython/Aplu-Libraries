// AudioFormats.java

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

import javax.sound.sampled.*;

/**
 * Class to provide simple AudioFormats.
 */
public abstract class AudioFormats
{
  /**
   * PCM signed, 16 bit, stereo, 44100 kHz, small endian
   */
  public static AudioFormat dvd_stereo = dvd_stereo();
  /**
   * PCM signed, 16 bit, mono, 44100 kHz, small endian
   */
  public static AudioFormat dvd_mono = dvd_mono();
  /**
   * PCM unsigned, 8 bit, stereo, 22050 kHz, small endian
   */
  public static AudioFormat fm_stereo = fm_stereo();
  /**
   * PCM unsigned, 8 bit, mono, 22050 kHz, small endian
   */
  public static AudioFormat fm_mono = fm_mono();

  private AudioFormats()
  {
  } // No instance allowed

  private static AudioFormat dvd_stereo()
  {
    // 8000,11025,16000,22050,44100
    float sampleRate = 44100.0F;

    // 8,16
    int sampleSizeInBits = 16;

    // 1,2
    int channels = 2;

    // true,false
    boolean signed = true;

    // true,false
    boolean bigEndian = false;

    return new AudioFormat(sampleRate, sampleSizeInBits,
      channels, signed, bigEndian);
  }

  private static AudioFormat dvd_mono()
  {
    // 8000,11025,16000,22050,44100
    float sampleRate = 44100.0F;

    // 8,16
    int sampleSizeInBits = 16;

    // 1,2
    int channels = 1;

    // true,false
    boolean signed = true;

    // true,false
    boolean bigEndian = false;

    return new AudioFormat(sampleRate, sampleSizeInBits,
      channels, signed, bigEndian);
  }

  private static AudioFormat fm_stereo()
  {
    // 8000,11025,16000,22050,44100
    float sampleRate = 22050.0F;

    // 8,16
    int sampleSizeInBits = 8;

    // 1,2
    int channels = 2;

    // true,false
    boolean signed = false;

    // true,false
    boolean bigEndian = false;

    return new AudioFormat(sampleRate, sampleSizeInBits,
      channels, signed, bigEndian);
  }

  private static AudioFormat fm_mono()
  {
    // 8000,11025,16000,22050,44100
    float sampleRate = 22050.0F;

    // 8,16
    int sampleSizeInBits = 8;

    // 1,2
    int channels = 1;

    // true,false
    boolean signed = false;

    // true,false
    boolean bigEndian = false;

    return new AudioFormat(sampleRate, sampleSizeInBits,
      channels, signed, bigEndian);
  }

}
