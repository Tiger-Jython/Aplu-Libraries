// SoundRecorder.java

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
 * Class for recording sound and save it to a WAV file.
 */
public class SoundRecorder
{
  private class RecorderThread extends Thread
  {
    public void run()
    {
      byte[] buf = new byte[bufSize];
      try
      {
        while (isCapturing)
        {
          int cnt = targetDataLine.read(buf, 0, buf.length);
          if (cnt > 0)
          {
            data.write(buf, 0, cnt);
            if (soundSampleListener != null)
              soundSampleListener.sampleReceived(cnt);
          }
        }
        data.close();
      }
      catch (IOException ex)
      {
        System.out.println(ex);
        System.exit(1);
      }
    }
  }
  private ByteArrayOutputStream data;
  private ByteArrayOutputStream baos;
  private AudioFormat audioFormat;
  private TargetDataLine targetDataLine;
  private RecorderThread recorderThread;
  private volatile boolean isCapturing = false;
  private int mixerIndex;
  private static final int defaultBufSize = 10000;
  private int bufSize;
  private SoundSampleListener soundSampleListener = null;

  /**
   * Same as SoundRecorder(bufSize, audioFormat, mixerIndex) with
   * default buffer size (10000 bytes) and default sound recording device.
   */
  public SoundRecorder(AudioFormat audioFormat)
  {
    this(defaultBufSize, audioFormat, -1);
  }

  /**
   * Same as SoundRecorder(bufSize, audioFormat, mixerIndex) with
   * default sound recording device.
   */
  public SoundRecorder(int bufSize, AudioFormat audioFormat)
  {
    this(bufSize, audioFormat, -1);
  }

  /**
   * Same as SoundRecorder(bufSize, audioFormat, mixerIndex) with
   * default buffer size (10000 bytes).
   */
  public SoundRecorder(AudioFormat audioFormat, int mixerIndex)
  {
    this(defaultBufSize, audioFormat, mixerIndex);
  }

  /**
   * Create a recorder instance with given audioFormat using the given mixer index (sound device).
   * To find the mixer indices of all installed sound devices call getAvailableMixers().<br>
   * Normally an installed sound device has two entries, one for playing and
   * one for recording. You must use the index for the recording device.
   */
  public SoundRecorder(int bufSize, AudioFormat audioFormat, int mixerIndex)
  {
    this.bufSize = bufSize;
    this.audioFormat = audioFormat;
    this.mixerIndex = mixerIndex;
  }
  
  /**
   * Register a SoundSampleLister to get notifications for each recorded
   * sound sample (default sample size: 10000 bytes).
   */
  public void addSoundSampleListener(SoundSampleListener listener)
  {
    this.soundSampleListener = listener;
  }
  
  /**
   * Same as capture(ByteArrayOutputStream data), but store data
   * in an internal buffer that can be read-out by getRecordedData().
   * @throws LineUnavailableException 
   */
  public void capture()
    throws LineUnavailableException
  {
    baos = new ByteArrayOutputStream();
    capture(baos);
  }
  
  /**
   * Return a reference to the internal sound recording buffer.
   */
  public byte[] getCapturedBytes()
  {
    return baos.toByteArray();
  }

  /**
   * Start capturing the sound in a separate capture thread and store 
   * data in the given stream. After the capture process is started, 
   * the method returns.
   * @throws javax.sound.sampled.LineUnavailableException if the sound 
   * card is not available
   */
  public void capture(ByteArrayOutputStream data)
    throws LineUnavailableException
  {
    this.data = data;

    DataLine.Info dataLineInfo =
      new DataLine.Info(TargetDataLine.class, audioFormat);

    Mixer.Info[] mixerInfo = AudioSystem.getMixerInfo();
    int nbMixers = mixerInfo.length;
    if (mixerIndex < -1 || mixerIndex >= nbMixers)
      mixerIndex = -1;

    if (mixerIndex != -1)  // Use specified mixer
    {
      Mixer mixer = AudioSystem.getMixer(mixerInfo[mixerIndex]);
      targetDataLine =
        (TargetDataLine)mixer.getLine(dataLineInfo);
    }
    else // Use default mixer
      targetDataLine =
        (TargetDataLine)AudioSystem.getLine(dataLineInfo);

    targetDataLine.open(audioFormat);
    targetDataLine.start();
    isCapturing = true;
    recorderThread = new RecorderThread();
    recorderThread.start();
  }

  /**
   * Stop the capturing process and return after the capture thread terminates.
   */
  public void stopCapture()
  {
    if (!isCapturing)
      return;
    isCapturing = false;
    try
    {
      recorderThread.join();
    }
    catch (InterruptedException ex)
    {
    }
    targetDataLine.stop();
    targetDataLine.close();
  }

  /**
   * Write the sound data from int array into the given file with given name in WAV format.
   * Return true if successful; otherwise false
   */
  public boolean writeWavFile(int[] data, String filename)
  {
    return writeWavFile(data, new File(filename));
  }

    /**
   * Write the sound data in the given byte array into file with given name in WAV format.
   * Return true if successful; otherwise false
   */
  public boolean writeWavFile(byte[] data, String filename)
  {
    return writeWavFile(data, new File(filename));
  }

  /**
   * Write the sound data in the given stream into file with given name in WAV format.
   */
  public boolean writeWavFile(ByteArrayOutputStream data, String filename)
  {
    return writeWavFile(data, new File(filename));
  }

  /**
   * Write the sound data the given stream into the given 
   * file in WAV format.
   * Return true if successful; otherwise false
   */
  public boolean writeWavFile(ByteArrayOutputStream data, File file)
  {
    return writeWavFile(data.toByteArray(), file);
  }

  /**
   * Write the sound data from byte array into the given file in WAV format.
   * Return true if successful; otherwise false
   */
  public boolean writeWavFile(byte[] data, File file)
  {
    InputStream byteArrayInputStream =
      new ByteArrayInputStream(data);
    AudioInputStream audioInputStream =
      new AudioInputStream(byteArrayInputStream, audioFormat,
      data.length
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
  
  /**
   * Write the sound data from int array into the given file in WAV format.
   * The int samples are converted to 2 bytes data depending on big or little endian format.
   * Return true if successful; otherwise false
   */
  public boolean writeWavFile(int[] data, File file)
  {
    byte[] values = new byte[2 * data.length];
    for (int i = 0; i < data.length; ++i)
    {
      if (audioFormat.isBigEndian())
      {
        values[2 * i] = (byte)(data[i] >> 8);  // High byte first
        values[2 * i + 1] = (byte)(data[i] & 0xFF);  // Low byte after -> Big endian
      }
      else
      {
        values[2 * i] = (byte)(data[i] & 0xFF);  // Low byte first
        values[2 * i + 1] = (byte)(data[i] >> 8);  // High byte after -> Little endian
      }
    }
    
    return writeWavFile(values, file);
  }

  /**
   * Return a list of the names of available mixers (sound devices). To select
   * one of these mixers, use its index in this list when constructing the
   * SoundRecorder instance.<br>
   * Normally an installed sound device has two entries, one for playing and
   * one for recording.
   */
  public static String[] getAvailableMixers()
  {
    Mixer.Info[] mixerInfo = AudioSystem.getMixerInfo();
    int nbMixers = mixerInfo.length;
    String[] ary = new String[nbMixers];
    for (int i = 0; i < nbMixers; i++)
      ary[i] = mixerInfo[i].getName();
    return ary;
  }

  /*
   * Return the current index of the mixer (sound device). -1 if the default sound device
   * is used.
   */
  public int getMixerIndex()
  {
    return mixerIndex;
  }
  
  /** 
   * Return the current audio format.
   */
  public AudioFormat getFormat()
  {
    return audioFormat;
  }
}
