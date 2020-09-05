// FunctionPlayer.java

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
import java.io.*;

/**
 * Class to play audio clips based on mathematical functions.
 * To define your own audio function, create a class that implements Waveform
 * and load an instance using load(). Be sure that the method f() has the
 * right signature and is bounded to -1..1. See the following example:
 *<br><br><code><font color="#0000ff">public</font><font color="#000000">&nbsp;</font><font color="#0000ff">class</font><font color="#000000">&nbsp;MyWaveform&nbsp;</font><font color="#0000ff">implements</font><font color="#000000">&nbsp;Waveform</font><br>
 <font color="#000000">{</font><br>
 <font color="#000000">&nbsp;&nbsp;</font><font color="#0000ff">public</font><font color="#000000">&nbsp;</font><font color="#0000ff">double</font><font color="#000000">&nbsp;</font><font color="#000000">f</font><font color="#000000">(</font><font color="#0000ff">double</font><font color="#000000">&nbsp;t,&nbsp;</font><font color="#0000ff">double</font><font color="#000000">&nbsp;freq</font><font color="#000000">)</font><br>
 <font color="#000000">&nbsp;&nbsp;</font><font color="#000000">{</font><br>
 <font color="#000000">&nbsp;&nbsp;&nbsp;&nbsp;</font><font color="#0000ff">double</font><font color="#000000">&nbsp;amplitude&nbsp;</font><font color="#c00000">=</font><font color="#000000">&nbsp;</font><font color="#000000">0</font><font color="#000000">.</font><font color="#000000">8</font><font color="#000000">;</font><br>
 <font color="#000000">&nbsp;&nbsp;&nbsp;&nbsp;</font><font color="#0000ff">double</font><font color="#000000">&nbsp;omega&nbsp;</font><font color="#c00000">=</font><font color="#000000">&nbsp;</font><font color="#000000">2</font><font color="#000000">&nbsp;</font><font color="#c00000">*</font><font color="#000000">&nbsp;</font><font color="#00008b">Math</font><font color="#000000">.PI&nbsp;</font><font color="#c00000">*</font><font color="#000000">&nbsp;freq;</font><br>
 <font color="#000000">&nbsp;&nbsp;&nbsp;&nbsp;</font><font color="#0000ff">double</font><font color="#000000">[]</font><font color="#000000">&nbsp;overtones&nbsp;</font><font color="#c00000">=</font><br>
 <font color="#000000">&nbsp;&nbsp;&nbsp;&nbsp;</font><font color="#000000">{</font><br>
 <font color="#000000">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</font><font color="#000000">1</font><font color="#000000">.</font><font color="#000000">0</font><font color="#000000">,&nbsp;</font><font color="#000000">1</font><font color="#000000">.</font><font color="#000000">0</font><font color="#000000">&nbsp;</font><font color="#c00000">/</font><font color="#000000">&nbsp;</font><font color="#000000">2</font><font color="#000000">,&nbsp;</font><font color="#000000">1</font><font color="#000000">.</font><font color="#000000">0</font><font color="#000000">&nbsp;</font><font color="#c00000">/</font><font color="#000000">&nbsp;</font><font color="#000000">3</font><font color="#000000">,&nbsp;</font><font color="#000000">1</font><font color="#000000">.</font><font color="#000000">0</font><font color="#000000">&nbsp;</font><font color="#c00000">/</font><font color="#000000">&nbsp;</font><font color="#000000">4</font><font color="#000000">,&nbsp;</font><font color="#000000">1</font><font color="#000000">.</font><font color="#000000">0</font><font color="#000000">&nbsp;</font><font color="#c00000">/</font><font color="#000000">&nbsp;</font><font color="#000000">5</font><font color="#000000">,&nbsp;</font><font color="#000000">1</font><font color="#000000">.</font><font color="#000000">0</font><font color="#000000">&nbsp;</font><font color="#c00000">/</font><font color="#000000">&nbsp;</font><font color="#000000">6</font><font color="#000000">,&nbsp;</font><font color="#000000">1</font><font color="#000000">.</font><font color="#000000">0</font><font color="#000000">&nbsp;</font><font color="#c00000">/</font><font color="#000000">&nbsp;</font><font color="#000000">7</font><font color="#000000">,&nbsp;</font><font color="#000000">1</font><font color="#000000">.</font><font color="#000000">0</font><font color="#000000">&nbsp;</font><font color="#c00000">/</font><font color="#000000">&nbsp;</font><font color="#000000">8</font><font color="#000000">,&nbsp;</font><font color="#000000">1</font><font color="#000000">.</font><font color="#000000">0</font><font color="#000000">&nbsp;</font><font color="#c00000">/</font><font color="#000000">&nbsp;</font><font color="#000000">9</font><br>
 <font color="#000000">&nbsp;&nbsp;&nbsp;&nbsp;</font><font color="#000000">}</font><font color="#000000">;</font><br>
 <font color="#000000">&nbsp;&nbsp;&nbsp;&nbsp;</font><font color="#0000ff">double</font><font color="#000000">&nbsp;value&nbsp;</font><font color="#c00000">=</font><font color="#000000">&nbsp;</font><font color="#000000">0</font><font color="#000000">;</font><br>
 <font color="#000000">&nbsp;&nbsp;&nbsp;&nbsp;</font><font color="#0000ff">for</font><font color="#000000">&nbsp;</font><font color="#000000">(</font><font color="#0000ff">int</font><font color="#000000">&nbsp;n&nbsp;</font><font color="#c00000">=</font><font color="#000000">&nbsp;</font><font color="#000000">0</font><font color="#000000">;&nbsp;n&nbsp;</font><font color="#c00000">&lt;</font><font color="#000000">&nbsp;overtones.length;&nbsp;n</font><font color="#c00000">++</font><font color="#000000">)</font><br>
 <font color="#000000">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;value&nbsp;</font><font color="#c00000">=</font><font color="#000000">&nbsp;value&nbsp;</font><font color="#c00000">+</font><font color="#000000">&nbsp;overtones</font><font color="#000000">[</font><font color="#000000">n</font><font color="#000000">]</font><font color="#000000">&nbsp;</font><font color="#c00000">*</font><font color="#000000">&nbsp;</font><font color="#00008b">Math</font><font color="#000000">.</font><font color="#000000">sin</font><font color="#000000">(</font><font color="#000000">n&nbsp;</font><font color="#c00000">*</font><font color="#000000">&nbsp;omega&nbsp;</font><font color="#c00000">*</font><font color="#000000">&nbsp;t</font><font color="#000000">)</font><font color="#000000">;</font><br>
 <font color="#000000">&nbsp;&nbsp;&nbsp;&nbsp;</font><font color="#0000ff">return</font><font color="#000000">&nbsp;amplitude&nbsp;</font><font color="#c00000">*</font><font color="#000000">&nbsp;value;</font><br>
 <font color="#000000">&nbsp;&nbsp;</font><font color="#000000">}</font><br>
 <font color="#000000">}</font></code><br><br>
 * To check what you have done, you may display the function using Waveform.WavePlot.
 * Waveform also contains some predefined waveforms like sine, square, sawtooth,
 * triangle, etc.
 * @see ch.aplu.util.Waveform
 */
public class FunctionPlayer extends ClipPlayer
{
  private ByteArrayOutputStream data = null;

  /**
   * Creates a FunctionPlayer with given audio format. There are simple predefined
   * formats in class ch.aplu.util.AudioFormats.
   * @see ch.aplu.util.AudioFormats
   * @param audioFormat the audio format to use.
   */
  public FunctionPlayer(AudioFormat audioFormat)
  {
    super(audioFormat);
  }

  /**
   * Creates a FunctionPlayer with format Audioformats.dvd_mono.
   * @see ch.aplu.util.AudioFormats
   */
  public FunctionPlayer()
  {
    super(AudioFormats.dvd_mono);
  }

  /**
   * Loads the audio data using the function from given Waveform. The clip will
   * be played during the given duration with given frequency and amplitude. To
   * prevent audio distortion the user defined audio function should be bounded
   * to -1..1.
   * @param wf the Waveform that defines the function to play
   * @param duration the time in seconds the clip is played
   * @param frequency the frequency in Hertz
   */
  public void load(Waveform wf, double duration, double frequency)
  {
    if (clip != null)
    {
      stop();
      clip = null;
    }

    data = new ByteArrayOutputStream();
    float sampleRate = audioFormat.getSampleRate();
    int nbFrames = (int)(duration * sampleRate);
    double amplitude;
    if (audioFormat.getSampleSizeInBits() == 8)
      amplitude = 127.0;
    else
      amplitude = 32767.0;
    double t = 0;
    double dt = 1.0 / sampleRate;
    int soundData;

    for (int i = 0; i < nbFrames; i++)
    {
      soundData = (int)(amplitude * wf.f(t, frequency));
      if (!audioFormat.getEncoding().equals(AudioFormat.Encoding.PCM_SIGNED))
      {
        if (audioFormat.getSampleSizeInBits() == 8)
          soundData += 127;
        else
          soundData += 32767;
      }
      t += dt;
      byte low = (byte)(soundData & 0xFF);
      byte high = (byte)((soundData >> 8) & 0xFF);
      if (audioFormat.isBigEndian())
      {
        if (audioFormat.getSampleSizeInBits() == 16)
          data.write(high);
        data.write(low);
        if (audioFormat.getChannels() == 2)
        {
          if (audioFormat.getSampleSizeInBits() == 16)
            data.write(high);
          data.write(low);
        }
      }
      else
      {
        data.write(low);
        if (audioFormat.getSampleSizeInBits() == 16)
          data.write(high);
        if (audioFormat.getChannels() == 2)
        {
          data.write(low);
          if (audioFormat.getSampleSizeInBits() == 16)
            data.write(high);
        }
      }
    }

    InputStream is =
      new ByteArrayInputStream(data.toByteArray());
    audioInputStream =
      new AudioInputStream(is, audioFormat,
      data.size()
      / audioFormat.getFrameSize());
  }

  /**
   * Save the loaded sound data to the given WAV file using the current
   * audio format.
   * @param file the WAV file to be created. If it already exists, it is overwritten.
   * @return true, if successful; otherwise false
   */
  public boolean save(File file)
  {
    if (data == null)
      return false;

    byte[] audioData = data.toByteArray();
    InputStream byteArrayInputStream =
      new ByteArrayInputStream(audioData);
    AudioInputStream audioInputStream =
      new AudioInputStream(byteArrayInputStream, audioFormat,
      audioData.length
      / audioFormat.getFrameSize());
    try
    {
      AudioSystem.write(audioInputStream,
        AudioFileFormat.Type.WAVE, file);
    }
    catch (IOException ex)
    {
      return false;
    }
    return true;
  }

}
