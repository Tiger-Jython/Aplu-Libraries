// SoundReader.java

/*
 This software is part of the JEX (Java Exemplarisch) Utility Library.
 It is Open Source Free Software, so you may
 - run the code for any purpose
 - study how the code works and adapt it to your needs
 - integrate all or parts of the code in your own programs
 - redistribute copies of the code
 - improve the code and release your improvements to the public
 However the use of the code is entirely your responsibility.
 
 Part of code copied from http://www.archive.org with thanks to the authors.
 */
package ch.aplu.util;

import javax.sound.sampled.*;
import java.io.*;
import java.net.URL;

/**
 * Class for reading sound files and extract sound data.
 * Sound format supported: WAV, ALAW, ULAW
 */
public class SoundReader
{
  private AudioInputStream audioInputStream;
  private AudioFormat audioFormat;

  /**
   * Construct a sound reader attached to given sound file.
   */
  public SoundReader(File audioFile)
  {
    try
    {
      audioInputStream = AudioSystem.getAudioInputStream(audioFile);
      audioFormat = audioInputStream.getFormat();
    }
    catch (Exception ex)
    {
    }
  }

  /**
   * Construct a sound reader attached to given sound file path.
   */
  public SoundReader(String audioPathname)
  {
    this(new File(audioPathname));
  }

  /**
   * Return the number of samples of all channels
   */
  public long getTotalNbSamples()
  {
    long total = (audioInputStream.getFrameLength()
      * audioFormat.getFrameSize() * 8) / audioFormat.getSampleSizeInBits();
    return total / audioFormat.getChannels();
  }

  // Get the sound samples for all channels. 
  public void getSamples(double[] samples) 
    throws IOException, IllegalArgumentException
  {
    // nbBytes = nbSamples * sampleSizeinByte * nbChannels
    long nbBytes = samples.length * (audioFormat.getSampleSizeInBits() / 8)
      * audioFormat.getChannels();
    if (nbBytes > Integer.MAX_VALUE)
      throw new IllegalArgumentException("too many samples");
    byte[] inBuffer = new byte[(int)nbBytes];
    audioInputStream.read(inBuffer, 0, inBuffer.length);
    decodeBytes(inBuffer, samples);
  }

  // Extract samples of a particular channel from interleavedSamples and
  // copy them into channelSamples
  /*
  public void getChannelSamples(int channel,
    double[] interleavedSamples, double[] channelSamples)
  {
    int nbChannels = audioFormat.getChannels();
    for (int i = 0; i < channelSamples.length; i++)
    {
      channelSamples[i] = interleavedSamples[nbChannels * i + channel];
    }
  }
  */ 

  // Convenience method. Extract left and right channels for common stereo
  // files. leftSamples and rightSamples must be of size getSampleCount()
  public void getStereoSamples(double[] leftSamples, double[] rightSamples)
    throws IOException
  {
    if (leftSamples.length != rightSamples.length)
      throw new IllegalArgumentException("");
    long sampleCount = leftSamples.length;
    double[] interleavedSamples = new double[(int)sampleCount * 2];
    getSamples(interleavedSamples);
    for (int i = 0; i < leftSamples.length; i++)
    {
      leftSamples[i] = interleavedSamples[2 * i];
      rightSamples[i] = interleavedSamples[2 * i + 1];
    }
  }

  // Private. Decode bytes of audioBytes into audioSamples
  private void decodeBytes(byte[] audioBytes, double[] audioSamples)
  {
    int sampleSizeInBytes = audioFormat.getSampleSizeInBits() / 8;
    int[] sampleBytes = new int[sampleSizeInBytes];
    int k = 0; // index in audioBytes
    for (int i = 0; i < audioSamples.length; i++)
    {
      // collect sample byte in big-endian order
      if (audioFormat.isBigEndian())
      {
        // bytes start with MSB
        for (int j = 0; j < sampleSizeInBytes; j++)
        {
          sampleBytes[j] = audioBytes[k++];
        }
      }
      else
      {
        // bytes start with LSB
        for (int j = sampleSizeInBytes - 1; j >= 0; j--)
        {
          sampleBytes[j] = audioBytes[k++];
          if (sampleBytes[j] != 0)
            j = j + 0;
        }
      }
      // get integer value from bytes
      int ival = 0;
      for (int j = 0; j < sampleSizeInBytes; j++)
      {
        ival += sampleBytes[j];
        if (j < sampleSizeInBytes - 1)
          ival <<= 8;
      }
      // decode value
      double ratio = Math.pow(2., audioFormat.getSampleSizeInBits() - 1);
      double val = ((double)ival) / ratio;
      audioSamples[i] = val;
    }
  }
}