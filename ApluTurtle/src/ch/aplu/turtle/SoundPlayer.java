// SoundPlayer.java

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
package ch.aplu.turtle;

import javax.sound.sampled.*;
import java.io.*;
import java.net.URL;
import java.util.ArrayList;

/**
 * Class for playing sound files using file streaming in a separate thread.
 * Sound format supported: WAV, ALAW, ULAW
 * For MP3 format, use class SoundPlayerExt.
 * @see SoundPlayerExt
 */
class SoundPlayer
{
  private class PlayerThread extends Thread
  {
    public void run()
    {
      int frameSize = audioFormat.getFrameSize();
      byte buf[] = new byte[1000 * frameSize];
      try
      {
        if (forwardStep > 0)  // if advanceXXX() was called before play()
        {
          skip(audioInputStream, forwardStep * frameSize);
          forwardStep = 0;
        }

        int cnt = 0;
        if (isRewind)
          isRewind = false;
        else if (soundPlayerListener != null)
          soundPlayerListener.notifySoundPlayerStateChange(0, mixerIndex);  // Start

        while (isRewindWhilePaused || (isRunning && (cnt = audioInputStream.read(buf, 0, buf.length)) != -1))
        {
          if (isRewindWhilePaused)
          {
            isPaused = true;
          }
          else
          {
            if (cnt > 0)
            {
              sourceDataLine.write(buf, 0, cnt);
              currentPos = currentPos + cnt / frameSize;
            }
          }
          if (isPaused)
          {
            if (!isRewindWhilePaused)
              if (soundPlayerListener != null)
                soundPlayerListener.notifySoundPlayerStateChange(2, mixerIndex);  // Pause

            synchronized (monitor)
            {
              try
              {
                if (isRewindWhilePaused)
                  isRewindWhilePaused = false;
                monitor.wait();
                if (soundPlayerListener != null)
                  soundPlayerListener.notifySoundPlayerStateChange(1, mixerIndex);  // Resume
              }
              catch (InterruptedException ex)
              {
              }
            }
          }
          if (forwardStep > 0)  // Advance
          {
            synchronized (monitor)
            {
              skip(audioInputStream, forwardStep * frameSize);
              forwardStep = 0;
            }
          }
        }
        //       sourceDataLine.close();  // Not a good idea when looping
        audioInputStream.close();
        if (is != null)
          is.close();
        wakeUp(); // Waiting loop thread
        currentPos = 0;
        forwardStep = 0;
        if (soundPlayerListener != null)
        {
          if (isRunning)
          {
            isRunning = false;
            if (!isRewind)
              if (soundPlayerListener != null)
                soundPlayerListener.notifySoundPlayerStateChange(4, mixerIndex);  // End of resource
          }
          else
          {
            if (!isRewind)
              if (soundPlayerListener != null)
                soundPlayerListener.notifySoundPlayerStateChange(3, mixerIndex);  // Stop
          }
        }
      }
      catch (IOException ex)
      {
        System.out.println(ex);
        System.exit(1);
      }
    }
  }
  private InputStream is = null;
  private URL url = null;
  protected File audioFile = null;
  protected AudioFormat audioFormat;
  private AudioFormat audioFormatBase;
  private AudioInputStream audioInputStream;
  private AudioInputStream myAudioInputStream = null;
  private SourceDataLine sourceDataLine;
  private FloatControl gainControl;
  private float volume;
  private PlayerThread playerThread = null;
  private Thread loopThread;
  private volatile boolean isRunning = false;
  private volatile boolean isPaused = false;
  private volatile boolean isLooping = false;
  private SoundPlayerListener soundPlayerListener = null;
  private Object monitor = new Object();
  private volatile long forwardStep = 0;
  private volatile long currentPos = 0;
  private volatile boolean isRewindWhilePaused = false;
  private volatile boolean isRewind = false;
  private int mixerIndex;
  private Object resourceObj;
  private String audioPathname;
  private String runtimeMessage = "";

  /**
   * Construct a sound player attached to given URL using the given mixer 
   * index (sound device). To find the mixer indices of all installed 
   * sound devices call getAvailableMixers().<br>
   * 
   * Normally an installed sound device has two entries, one for playing and
   * one for recording. You must use the index for the playing device.<br><br>
   * The given input stream is closed, when the playing ends, so playLoop() 
   * is not working. To load a resource from a jar file use<br>  
   * 
   * <code>URL url = Thread.currentThread().getContextClassLoader().getResource("&lt;relPath&gt;");</code><br><br>
   * 
   * or call URLfromJAR()
   * @throws RuntimeException if sound initialization fails
   */
  public SoundPlayer(URL url, int mixerIndex)
  {
    this.url = url;
    try
    {
      is = url.openStream();
    }
    catch (Exception ex)
    {
      throw new RuntimeException("Sound resource not found.\n" + ex);
    }
    this.mixerIndex = mixerIndex;
    initCtor();
  }

  /**
   * Same as SoundPlayer(url, mixerIndex) using the default sound device.
   */
  public SoundPlayer(URL url)
  {
    this(url, -1);
  }

  /**
   * Construct a sound player attached to given pathname from a JAR archive
   * using the given mixer index (sound device).
   * The class loader of the specified object is used.<br><br>
   * Deprecated:  Use SoundPlayer(url, mixerIndex) instead.
   */
  public SoundPlayer(Object resourceObj, String audioPathname, int mixerIndex)
  {
    this.resourceObj = resourceObj;
    this.audioPathname = audioPathname;
    this.mixerIndex = mixerIndex;
    initCtor();
  }

  /**
   * Same as SoundPlayer(obj, audioPathname, mixerIndex) using 
   * the default sound device. <br><br>
   * Deprecated:  Use SoundPlayer(url) instead.
   */
  public SoundPlayer(Object resourceObj, String audioPathname)
  {
    this(resourceObj, audioPathname, -1);
  }

  /**
   * Construct a sound player attached to given File instance using the given mixer index (sound device).
   * To find the mixer indices of all installed sound devices call getAvailableMixers().
   * @throws RuntimeException if sound initialization fails
   */
  public SoundPlayer(File audioFile, int mixerIndex)
  {
    this.audioFile = audioFile;
    this.mixerIndex = mixerIndex;
    initCtor();
  }

  /**
   * Same as SoundPlayer(audioFile, mixerIndex) using the default sound device.
   */
  public SoundPlayer(File audioFile)
  {
    this(audioFile, -1);
  }

  /**
   * Construct a sound player attached to given pathname (relative or fully qualified)
   * using the given mixer index (sound device).<br>
   * If the file is not found in the given audioPathname, it is searched
   * in the JAR resource. If it is not found there, an _ is appended to
   * audioPathname and the file is searched again in the JAR.
   * To find the mixer indices of all installed sound devices call getAvailableMixers().
   * @throws RuntimeException if sound initialization fails
   */
  public SoundPlayer(String audioPathname, int mixerIndex)
  {
    if (new File(audioPathname).exists())
    {
      this.audioPathname = audioPathname;
      audioFile = new File(audioPathname);
    }
    else
    {
      try
      {
        this.url = URLfromJAR(audioPathname);
        is = url.openStream();
      }
      catch (Exception ex)
      {
        audioPathname = "_" + audioPathname;
        this.url = URLfromJAR(audioPathname);
        try
        {
          is = url.openStream();
        }
        catch (Exception ex1)
        {
          throw new RuntimeException("Sound resource not found.\n" + ex);
        }
      }
    }
    this.mixerIndex = mixerIndex;
    initCtor();
  }

  /**
   * Same as SoundPlayer(audioPathname, mixerIndex) using the default sound device.
   */
  public SoundPlayer(String audioPathname)
  {
    this(audioPathname, -1);
  }

  /**
   * Construct a sound player attached to the given AudioInputStream using the given mixer index (sound device).
   * To find the mixer indices of all installed sound devices call getAvailableMixers().
   * @throws RuntimeException if sound initialization fails
   */
  public SoundPlayer(AudioInputStream audioInputStream, int mixerIndex)
  {
    myAudioInputStream = audioInputStream;
    this.mixerIndex = mixerIndex;
    initCtor();
  }

  /**
   * Same as SoundPlayer(audioInputStream, mixerIndex) using the default sound device.
   */
  public SoundPlayer(AudioInputStream audioInputStream)
  {
    this(audioInputStream, -1);
  }

  /**
   * Construct a sound player with data from the given ByteArrayOutputStream 
   * using the given mixer index (sound device).
   * To find the mixer indices of all installed sound devices call getAvailableMixers().
   * @throws RuntimeException if sound initialization fails
   */
  public SoundPlayer(ByteArrayOutputStream os, AudioFormat audioFormat, int mixerIndex)
  {
    InputStream is
      = new ByteArrayInputStream(os.toByteArray());
    myAudioInputStream
      = new AudioInputStream(is, audioFormat,
        os.size() / audioFormat.getFrameSize());
    this.mixerIndex = mixerIndex;
    initCtor();
  }

  /**
   * Same as SoundPlayer(os, audioFormat, mixerIndex) using the default sound device.
   */
  public SoundPlayer(ByteArrayOutputStream os, AudioFormat audioFormat)
  {
    this(os, audioFormat, -1);
  }

  /**
   * Construct a sound player with data from the given byte array 
   * using the given mixer index (sound device).
   * To find the mixer indices of all installed sound devices,
   * call getAvailableMixers().
   * @throws RuntimeException if sound initialization fails
   */
  public SoundPlayer(byte[] ary, AudioFormat audioFormat, int mixerIndex)
  {
    InputStream is
      = new ByteArrayInputStream(ary);
    myAudioInputStream
      = new AudioInputStream(is, audioFormat,
        ary.length / audioFormat.getFrameSize());
    this.mixerIndex = mixerIndex;
    initCtor();
  }

  /**
   * Same as SoundPlayer(byte[] ary, AudioFormat audioFormat, int mixerIndex)
   * using the default sound device.
   */
  public SoundPlayer(byte[] ary, AudioFormat audioFormat)
  {
    this(ary, audioFormat, -1);
  }

  /**
   * Construct a sound player with data from the given int array 
   * using the given mixer index (sound device).
   * To find the mixer indices of all installed sound devices,
   * call getAvailableMixers().
   * The int samples are converted to 2 bytes data depending on big or 
   * little endian format.
   * @throws RuntimeException if sound initialization fails
   */
  public SoundPlayer(int[] ary, AudioFormat audioFormat, int mixerIndex)
  {
    byte[] data = new byte[2 * ary.length];
    for (int i = 0; i < ary.length; ++i)
    {
      if (audioFormat.isBigEndian())
      {
        data[2 * i] = (byte)(ary[i] >> 8);  // High byte first
        data[2 * i + 1] = (byte)(ary[i] & 0xFF);  // Low byte after -> Big endian
      }
      else
      {
        data[2 * i] = (byte)(ary[i] & 0xFF);  // Low byte first
        data[2 * i + 1] = (byte)(ary[i] >> 8);  // High byte after -> Little endian
      }
    }
    InputStream is
      = new ByteArrayInputStream(data);
    myAudioInputStream
      = new AudioInputStream(is, audioFormat,
        2 * ary.length / audioFormat.getFrameSize());
    this.mixerIndex = mixerIndex;
    initCtor();
  }

  /**
   * Same as SoundPlayer(int[] ary, AudioFormat audioFormat, int mixerIndex)
   * using the default sound device.
   */
  public SoundPlayer(int[] ary, AudioFormat audioFormat)
  {
    this(ary, audioFormat, -1);
  }

  private void initCtor() throws RuntimeException
  {
    int rc = init();
    if (rc == 1)
      throw new RuntimeException(
        "Error while initializing sound system.\n" + runtimeMessage);
    if (rc == 2)
      if (audioFormatBase == null)
        throw new RuntimeException("Audio format not supported");
      else
        throw (new IllegalArgumentException("Audio format "
          + audioFormatBase.toString() + " not supported"));
    if (rc == -1 && audioFormat != null)
      throw new RuntimeException("Can't access sound system for audio format "
        + audioFormatBase.toString());
  }

  private AudioFormat cloneAudioFormat(AudioFormat audioFormat)
  {
    AudioFormat tmp = new AudioFormat(
      audioFormat.getEncoding(),
      audioFormat.getSampleRate(),
      audioFormat.getSampleSizeInBits(),
      audioFormat.getChannels(),
      audioFormat.getFrameSize(),
      audioFormat.getFrameRate(),
      audioFormat.isBigEndian());
    return tmp;
  }

  private int init()
  {
    try
    {
      if (resourceObj != null)
      {
        // Could be loaded in static context!!!
        // Code retained due to backward compatibility, but the following
        // code works even in Webstart
        // URL url = Thread.currentThread().getContextClassLoader().getResource(imagePath);

        is = resourceObj.getClass().getResourceAsStream(audioPathname);
        url = resourceObj.getClass().getResource(audioPathname);
      }

      if (is != null)
        audioInputStream = AudioSystem.getAudioInputStream(url);
      else
      {
        if (myAudioInputStream != null)
          audioInputStream = myAudioInputStream;
        else
          audioInputStream = AudioSystem.getAudioInputStream(audioFile);
      }
      if (audioInputStream == null)
        return 1;

      audioFormatBase = audioInputStream.getFormat();
      if (!isFormatSupported(audioFormatBase))
        return 2;  // Illegal format

      boolean isMPEG = audioFormatBase.getEncoding().toString().substring(0, 4).equals("MPEG");
      if (isMPEG)
        audioFormat = new AudioFormat(
          AudioFormat.Encoding.PCM_SIGNED,
          audioFormatBase.getSampleRate(), 16, audioFormatBase.getChannels(),
          audioFormatBase.getChannels() * 2, audioFormatBase.getSampleRate(), false);
      else
        audioFormat = cloneAudioFormat(audioFormatBase);

      // Redefine audioInputStream to decoded format
      audioInputStream = AudioSystem.getAudioInputStream(audioFormat, audioInputStream);
    }
    catch (Exception ex)
    {
      runtimeMessage = ex.toString();
      return 1;
    }

    try
    {
      DataLine.Info dataLineInfo
        = new DataLine.Info(SourceDataLine.class, audioFormat);

      Mixer.Info[] mixerInfo = AudioSystem.getMixerInfo();
      int nbMixers = mixerInfo.length;
      if (mixerIndex < -1 || mixerIndex >= nbMixers)
        mixerIndex = -1;

      if (mixerIndex != -1)  // Use specified mixer
      {
        Mixer mixer = AudioSystem.getMixer(mixerInfo[mixerIndex]);
        sourceDataLine
          = (SourceDataLine)mixer.getLine(dataLineInfo);
      }
      else // Use default mixer
        sourceDataLine
          = (SourceDataLine)AudioSystem.getLine(dataLineInfo);

      sourceDataLine.open(audioFormat);

      javax.sound.sampled.Control[] controls = sourceDataLine.getControls();
      for (int i = 0; i < controls.length; i++)
      {
        if (controls[i].getType() == FloatControl.Type.MASTER_GAIN)
        {
          gainControl = (FloatControl)sourceDataLine.getControl(FloatControl.Type.MASTER_GAIN);
          break;
        }
        if (gainControl != null)
          volume = gainControl.getValue();
      }
    }
    catch (Exception ex)
    {
      return -1;
    }
    return 0;
  }

  /**
   * Register a SoundPlayerListener to get notifications from the SoundPlayer.
   */
  public void addSoundPlayerListener(SoundPlayerListener listener)
  {
    soundPlayerListener = listener;
  }

  /**
   * Wait until a playing underway has ended. Then restart playing and return immediately.<br>
   * When the playing is over, paused or stopped, notifySoundPlayerStateChange(int reason) is invoked.
   */
  public void replay()
  {
    while (play() == 2)
    {
      try
      {
        Thread.sleep(10);
      }
      catch (InterruptedException ex)
      {
      }
    }
  }

  /**
   * Start or resume playing and return immediately.<br>
   * When the playing is over, paused or stopped, notifySoundPlayerStateChange(int reason) is invoked.
   * Return values:<br>
   *  0: Successfully started<br>
   *  1: Successfully resumed<br>
   *  2: Playing underway. Nothing happens.
   */
  public int play()
  {
    if (isPaused)  // Resume
    {
      isPaused = false;
      synchronized (monitor)
      {
        try
        {
          monitor.notify();
        }
        catch (Exception ex)
        {
        }
      }
      return 1;
    }

    if (playerThread != null && playerThread.isAlive())
      return 2;

    // Must (re)open stream because we have to position the file pointer
    // to the beginning. (No seek-method to offset zero available.)
    init();
    sourceDataLine.start();
    if (gainControl != null)
      gainControl.setValue(volume);
    playerThread = new PlayerThread();
    isRunning = true;
    playerThread.start();
    return 0;
  }

  /**
   * Start playing and block until the sound file is completely played.
   */
  public void blockingPlay()
  {
    addSoundPlayerListener(new SoundPlayerListener()
    {
      public void notifySoundPlayerStateChange(int reason, int mixerIndex)
      {
        if (reason == 4)  // end of play
        {
          addSoundPlayerListener(null);
          wakeUp();
        }
      }
    });
    play();
    putSleep();
  }

  /**
   * Stop playing. If playing or pausing, play() will restart
   * playing from the beginning. Block until all resources are released.
   */
  public void stop()
  {
    if (!isRunning)
      return;

    mute(true);
    if (isLooping)
    {
      isLooping = false;   // If looping, kill loop thread
      wakeUp();
    }

    isRunning = false;
    playerThread.interrupt();  // If paused
    try
    {
      playerThread.join();  // Wait the player thread to die
    }
    catch (InterruptedException ex)
    {
    }
    playerThread = null;
    isPaused = false;
    currentPos = 0;
  }

  /**
   * Stop playing momentarily. If not playing nothing happens.
   * play() must be called to resume playing.
   */
  public void pause()
  {
    if (isPlaying())
      isPaused = true;
  }

  /**
   * Return true, if playing (and not pausing), otherwise false.
   * (In other words, if true, you should hear something).
   */
  public boolean isPlaying()
  {
    if (playerThread == null)
      return false;
    if (isPaused)
      return false;
    return (playerThread.isAlive());
  }

  /**
   * Return the current position (in frames from beginning).
   */
  public long getCurrentPos()
  {
    return currentPos;
  }

  /**
   * Return the current time (in ms from beginning).
   */
  public double getCurrentTime()
  {
    float frameRate = audioFormat.getFrameRate();
//    currentPos = currentTime * frameRate
    return 1000.0 * currentPos / frameRate;
  }

  /**
   * Return AudioFormat of player's (decoded) resource.
   * If playing MP3 files, the  decoded format is AudioFormat.Encoding.PCM_SIGNED.
   * @see javax.sound.sampled.AudioFormat
   */
  public AudioFormat getFormat()
  {
    return audioFormat;
  }

  /**
   * Return frame size (nb of bytes per sound sample) of player's (decoded) resource.
   */
  public int getFrameSize()
  {
    return audioFormat.getFrameSize();
  }

  /**
   * Return frame rate (number of frames per seconds) of player's (decoded) resource.
   */
  public float getFrameRate()
  {
    return audioFormat.getFrameRate();
  }

  /**
   * Advance current position (number of frames). If pausing, remain pausing
   * at new position. If still stopped, advance start position.
   * If advanced past end of resource, advance to end of resource.
   * May be called before play() to start playing at given byte offset.
   */
  public void advanceFrames(long nbFrames)
  {
    if (nbFrames > 0)
    {
      synchronized (monitor)  // because player thread may change value at same time
      {
        forwardStep += nbFrames;
        currentPos += nbFrames;
      }
    }
  }

  /**
   * Advance current time (in ms). If pausing, remain pausing
   * at new time. If still stopped, advance start time.
   * If new time exceeds length of resource, advance to end of resource.
   * May be called before play() to start playing at given time offset.
   */
  public void advanceTime(double time)
  {
    if (time > 0)
      advanceFrames((long)(time / 1000.0 * audioFormat.getFrameRate()));
  }

  /**
   * Rewind current position (number of frames).
   * If pausing, remain pausing  at new position. If stopped, rewind
   * start position, if eventually advanced (never below 0).
   * If given nbBytes is greater than current position, rewind to start of resource.
   */
  public void rewindFrames(long nbFrames)
  {
    if (nbFrames > 0)
    {
      if (!isRunning)  // Still stopped, must compensate advances
      {
        forwardStep -= nbFrames;
        currentPos -= nbFrames;
        if (forwardStep < 0)
          forwardStep = 0;
        if (currentPos < 0)
          currentPos = 0;
        return;
      }
      boolean pausing = isPaused;
      long pos = currentPos;
      double currentTime = getCurrentTime() / 1000.0;
      isRewind = true;
      stop();  // Resets isPaused
      if (pausing)
        isRewindWhilePaused = true;
      forwardStep = (pos - nbFrames > 0) ? (pos - nbFrames) : 0;
      currentPos = forwardStep;
      play();
      // Must wait until thread enters wait state, otherwise in a
      // following play(), notify() may be missed
      while (isRewindWhilePaused)
      {
        try
        {
          Thread.currentThread().sleep(1);
        }
        catch (InterruptedException ex)
        {
        }
      }
    }
  }

  /**
   * Rewind current time (in ms). If stopped, nothing happens.
   * If pausing, remain pausing at new time. If stopped, rewind
   * start time, if eventually advanced (never below 0).
   * If given time is greater than current time, rewind to start of resource.
   */
  public void rewindTime(double time)
  {
    if (time > 0)
      rewindFrames((long)(time / 1000.0 * audioFormat.getFrameRate()));
  }

  /**
   * Set the volume to the give value (range 0..1000).
   * May be called in any player's state.
   * If the sound system has no support for volume control, nothing happens.
   */
  public void setVolume(int value)
  {
    if (gainControl == null)
      return;
    float gainMax = gainControl.getMaximum();
    float gainMin = gainControl.getMinimum();
    float width = gainMax - gainMin;
    int valMax = 1000;
    volume = gainMin + (float)value / valMax * width;
    if (volume > gainMax)
      volume = gainMax;
    if (volume < gainMin)
      volume = gainMin;
    gainControl.setValue(volume);
  }

  /**
   * If isMuting is true, set sound level to mimimum; otherwise reset it to the old value.
   *
   */
  public void mute(boolean isMuting)
  {
    if (isMuting)
      gainControl.setValue(gainControl.getMinimum());
    else
      gainControl.setValue(volume);
  }

  /**
   * Return the current volume (range 0..1000).
   * If the sound system has no support for volume control, return -1.
   */
  public int getVolume()
  {
    if (gainControl == null)
      return -1;
    float gainMax = gainControl.getMaximum();
    float gainMin = gainControl.getMinimum();
    float width = gainMax - gainMin;
    int valMax = 1000;
    return Math.round((volume - gainMin) * valMax / width);
  }

  /**
   * Return a list of the names of available mixers (sound devices). To select
   * one of these mixers, use its index in this list when constructing the
   * SoundPlayer instance.<br>
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
   * Return the index of the mixer (sound device). -1 if the default sound device
   * is used.
   */
  public int getMixerIndex()
  {
    return mixerIndex;
  }

  // Overridden in class SoundPlayerExt
  protected long skip(AudioInputStream in, long nbBytes)
  {
    long SKIP_INACCURACY_SIZE = 1200;
    long totalSkipped = 0;
    long skipped = 0;
    try
    {
      while (totalSkipped < (nbBytes - SKIP_INACCURACY_SIZE))
      {
        skipped = in.skip(nbBytes - totalSkipped);
        if (skipped == 0)
          break;
        totalSkipped = totalSkipped + skipped;
      }
    }
    catch (IOException ex)
    {
      ex.printStackTrace();
      System.exit(1);
    }
    return totalSkipped;
  }

  // Overridden in class SoundPlayerExt
  protected boolean isFormatSupported(AudioFormat audioFormat)
  {
    if (audioFormat.getEncoding() == AudioFormat.Encoding.PCM_SIGNED
      || audioFormat.getEncoding() == AudioFormat.Encoding.PCM_UNSIGNED
      || audioFormat.getEncoding() == AudioFormat.Encoding.ULAW
      || audioFormat.getEncoding() == AudioFormat.Encoding.ALAW)
      return true;
    return false;
  }

  /**
   * Start a loop playing mode, e.g. when the playing ends, it is restarted automatically until
   * stop() is called. If already playing in normal mode, the loop playing mode is activated;
   * if already playing in loop playing mode, remains in this mode.<br><br>
   * Does not work, if the player was created by specifing an input stream, because the stream
   * is closed when the playing ends.
   */
  public void playLoop()
  {
    if (isLooping)
      return;

    isLooping = true;
    loopThread = new Thread()
    {
      public void run()
      {
        while (isLooping)
        {
          replay();
          putSleep();
        }
      }
    };
    loopThread.start();
  }

  /**
   * Return the raw byte data of the give WAV file.
   */
  public static byte[] getWavRaw(String filename)
  {
    byte[] buffer = null;
    try
    {

      AudioInputStream inputStream
        = AudioSystem.getAudioInputStream(new File(filename));
      int numBytes = inputStream.available();
      buffer = new byte[numBytes];
      inputStream.read(buffer, 0, numBytes);
      inputStream.close();
    }
    catch (Exception ex)
    {
      return null;
    }
    return buffer;
  }

  /**
   * Return the sound data in given AudioInputStream in a integer array. 
   * The following audio formats are supported:
   * mono:<br> 
   * - 8bit signed, unsigned, 16bit signed<br>
   * stereo:<br>
   * - 8bit signed, unsigned, 16bit signed<br><br>
   * 
   * Sample data is grouped in pairs: left channel, right channel. For
   * mono WAV audio format, both channels contain the same data. <br>
   *
   * The values are in range -32768..37768 for 8 and 16 bit formats.
   */
  public static int[] getStereo(AudioInputStream ais)
  {
    byte[] buffer = null;
    int numBytes;
    AudioFormat format;
    try
    {
      format = ais.getFormat();

      numBytes = ais.available();
      buffer = new byte[numBytes];
      ais.read(buffer, 0, numBytes);
      ais.close();
    }
    catch (Exception ex)
    {
      return null;
    }

    int[] data = null;

    // -------------------- Mono ---------------------------
    if (format.getChannels() == 1)
    {
      if (format.getSampleSizeInBits() == 8)  // 8 bit
      {
        if (format.getEncoding() == AudioFormat.Encoding.PCM_SIGNED)
        {
          // Untested  
          data = new int[2 * numBytes];
          for (int i = 0; i < numBytes; i++)
            data[2 * i] = data[2 * i + 1] = 256 * buffer[i];
        }
        if (format.getEncoding() == AudioFormat.Encoding.PCM_UNSIGNED)
        {
          // Tested  
          data = new int[2 * numBytes];
          for (int i = 0; i < numBytes; i++)
            data[2 * i] = data[2 * i + 1] = 256 * ((buffer[i] & 0xFF) - 128);
        }
      }

      if (format.getSampleSizeInBits() == 16)  // 16 bit
      {
        if (format.getEncoding() == AudioFormat.Encoding.PCM_SIGNED)
        {
          if (format.isBigEndian())
          {
            // Untested  
            data = new int[numBytes];
            for (int i = 0; i < numBytes / 2; i++)
              data[2 * i] = data[2 * i + 1] = 256 * buffer[2 * i] + buffer[2 * i + 1];
          }
          else
          {
            // Tested  
            data = new int[numBytes];
            for (int i = 0; i < numBytes / 2; i++)
              data[2 * i] = data[2 * i + 1] = buffer[2 * i] + 256 * buffer[2 * i + 1];
          }
        }

        if (format.getEncoding() == AudioFormat.Encoding.PCM_UNSIGNED)
        {
          return null;
          /*
           if (format.isBigEndian())
           {
           // Untested  
           data = new int[numBytes];
           for (int i = 0; i < numBytes / 2; i++)
           {
           int high = (buffer[2 * i] & 0xFF) - 128;
           int low = (buffer[2 * i + 1] & 0xFF) - 128;
           data[2 * i] = data[2 * i + 1] = 256 * high + low;
           }
           }
           else
           {
           // Untested  
           data = new int[numBytes];
           for (int i = 0; i < numBytes / 2; i++)
           {
           int low = (buffer[2 * i] & 0xFF) - 128;
           int high = (buffer[2 * i + 1] & 0xFF) - 128;
           data[2 * i] = data[2 * i + 1] = 256 * high + low;
           }
           }
           */
        }
      }
    }

    // -------------------- Stereo ---------------------------
    if (format.getChannels() == 2) // Stereo
    {
      if (format.getSampleSizeInBits() == 8)  // 8 bit
      {
        if (format.getEncoding() == AudioFormat.Encoding.PCM_SIGNED)
        {
          // Untested  
          data = new int[numBytes];
          for (int i = 0; i < numBytes; i++)
            data[i] = 256 * buffer[i];
        }
        if (format.getEncoding() == AudioFormat.Encoding.PCM_UNSIGNED)
        {
          // Tested  
          data = new int[numBytes];
          for (int i = 0; i < numBytes; i++)
            data[i] = 256 * ((buffer[i] & 0xFF) - 128);
        }
      }
      if (format.getSampleSizeInBits() == 16)  // 16 bit
      {
        if (format.getEncoding() == AudioFormat.Encoding.PCM_SIGNED)
        {
          if (format.isBigEndian())
          {
            // Untested  
            data = new int[numBytes / 2];
            for (int i = 0; i < numBytes / 2; i++)
              data[i] = 256 * buffer[2 * i] + buffer[2 * i + 1];
          }
          else
          {
            // Tested  
            data = new int[numBytes / 2];
            for (int i = 0; i < numBytes / 2; i++)
              data[i] = buffer[2 * i] + 256 * buffer[2 * i + 1];
          }
        }

        if (format.getEncoding() == AudioFormat.Encoding.PCM_UNSIGNED)
        {
          return null;
          /*
           if (format.isBigEndian())
           {
           // Untested  
           data = new int[numBytes / 2];
           for (int i = 0; i < numBytes / 2; i++)
           {
           int high = (buffer[2 * i] & 0xFF) - 128;
           int low = (buffer[2 * i + 1] & 0xFF) - 128;
           data[i] = 256 * high + low;
           }
           }
           else
           {
           // Untested  
           data = new int[numBytes / 2];
           for (int i = 0; i < numBytes / 2; i++)
           {
           int low = (buffer[2 * i] & 0xFF) - 128;
           int high = (buffer[2 * i + 1] & 0xFF) - 128;
           data[i] = 256 * high + low;
           }
           }
           */
        }
      }
    }
    return data;
  }

  /**
   * Return the sound data of the given WAV file in a integer array. The
   * following audio formats are supported:
   * mono:<br> 
   * - 8bit signed, unsigned, 16bit signed<br>
   * stereo:<br>
   * - 8bit signed, unsigned, 16bit signed<br><br>
   * 
   * Sample data is grouped in pairs: left channel, right channel. For
   * mono WAV files, both channels contain the same data. <br>
   * 
   * The values are in range -32768..37768 for 8 and 16 bit formats.
   */
  public static int[] getWavStereo(String filename)
  {
    AudioInputStream ais;
    try
    {
      ais = AudioSystem.getAudioInputStream(new File(filename));
    }
    catch (Exception ex)
    {
      return null;
    }
    return getStereo(ais);
  }

  /**
   * Return the sound data of the given AudioInputStream in a integer array. 
   * The following audio formats are supported:
   * mono:<br> 
   * - 8bit signed, unsigned, 16bit signed<br>
   * stereo:<br>
   * - 8bit signed, unsigned, 16bit signed<br><br>
   * For stereo WAV audio format, the left and right channel values are averaged. <br>
   * 
   * The values are in range -32768..37768 for 8 and 16 bit formats.
   */
  public static int[] getMono(AudioInputStream ais)
  {
    AudioFormat format = ais.getFormat();
    ArrayList<Byte> buffer = new ArrayList<Byte>(100000);
    try
    {
      int nb = 0;
      byte[] buf = new byte[10000];
      while ((nb = ais.read(buf, 0, buf.length)) != -1)
      {
        for (int i = 0; i < nb; i++)
          buffer.add(buf[i]);
      }
    }
    catch (Exception ex)
    {
      ex.printStackTrace();
      return null;
    }

    int[] data = null;
    int numBytes = buffer.size();

    // -------------------- Mono ---------------------------
    if (format.getChannels() == 1)
    {
      if (format.getSampleSizeInBits() == 8)  // 8 bit
      {
        if (format.getEncoding() == AudioFormat.Encoding.PCM_SIGNED)
        {
          // Untested  
          data = new int[numBytes];
          for (int i = 0; i < numBytes; i++)
            data[i] = 256 * buffer.get(i);
        }
        if (format.getEncoding() == AudioFormat.Encoding.PCM_UNSIGNED)
        {
          // Tested  
          data = new int[numBytes];
          for (int i = 0; i < numBytes; i++)
            data[i] = 256 * ((buffer.get(i) & 0xFF) - 128);
        }
      }

      if (format.getSampleSizeInBits() == 16)  // 16 bit
      {
        if (format.getEncoding() == AudioFormat.Encoding.PCM_SIGNED)
        {
          if (format.isBigEndian())
          {
            // Untested  
            data = new int[numBytes / 2];
            for (int i = 0; i < numBytes / 2; i++)
              data[i] = 256 * buffer.get(2 * i) + buffer.get(2 * i + 1);
          }
          else
          {
            // Tested  
            data = new int[numBytes / 2];
            for (int i = 0; i < numBytes / 2; i++)
              data[i] = buffer.get(2 * i) + 256 * buffer.get(2 * i + 1);
          }
        }

        if (format.getEncoding() == AudioFormat.Encoding.PCM_UNSIGNED)
        {
          return null;
        }
      }
    }

    // -------------------- Stereo ---------------------------
    if (format.getChannels() == 2) // Stereo
    {
      if (format.getSampleSizeInBits() == 8)  // 8 bit
      {
        if (format.getEncoding() == AudioFormat.Encoding.PCM_SIGNED)
        {
          // Untested  
          data = new int[numBytes / 2];
          for (int i = 0; i < numBytes / 2; i++)
            data[i] = 128 * (buffer.get(2 * i) + buffer.get(2 * i + 1));
        }
        if (format.getEncoding() == AudioFormat.Encoding.PCM_UNSIGNED)
        {
          // Tested  
          data = new int[numBytes / 2];
          for (int i = 0; i < numBytes / 2; i++)
            data[i] = 128 * (((buffer.get(2 * i) & 0xFF) - 128)
              + ((buffer.get(2 * i + 1) & 0xFF) - 128));
        }
      }
      if (format.getSampleSizeInBits() == 16)  // 16 bit
      {
        if (format.getEncoding() == AudioFormat.Encoding.PCM_SIGNED)
        {
          if (format.isBigEndian())
          {
            // Untested  
            data = new int[numBytes / 4];
            for (int i = 0; i < numBytes / 4; i++)
              data[i] = ((256 * buffer.get(4 * i) + buffer.get(4 * i + 1))
                + (256 * buffer.get(4 * i) + buffer.get(4 * i + 1))) / 2;
          }
          else
          {
            // Tested  
            data = new int[numBytes / 4];
            for (int i = 0; i < numBytes / 4; i++)
              data[i] = ((buffer.get(4 * i) + 256 * buffer.get(4 * i + 1))
                + (buffer.get(4 * i) + 256 * buffer.get(4 * i + 1))) / 2;
          }
        }

        if (format.getEncoding() == AudioFormat.Encoding.PCM_UNSIGNED)
        {
          return null;
        }
      }
    }
    return data;
  }

  /**
   * Return the sound data of the given WAV file in a integer array. The
   * following audio formats are supported:
   * mono:<br> 
   * - 8bit signed, unsigned, 16bit signed<br>
   * stereo:<br>
   * - 8bit signed, unsigned, 16bit signed<br><br>
   * For stereo WAV files, the left and right channel values are averaged. <br>
   * 
   * The values are in range -32768..37768 for 8 and 16 bit formats.
   */
  public static int[] getWavMono(String filename)
  {
    AudioInputStream ais;
    try
    {
      ais = AudioSystem.getAudioInputStream(new File(filename));
    }
    catch (Exception ex)
    {
      return null;
    }
    return getMono(ais);
  }

  /**
   * Return the audio file format information.
   */
  public static String getWavInfo(String filename)
  {
    try
    {
      AudioInputStream inputStream
        = AudioSystem.getAudioInputStream(new File(filename));
      inputStream.close();
      return inputStream.getFormat().toString();
    }
    catch (Exception ex)
    {
      return "";
    }
  }

  /**
   * Return the URL of a sound resource from the JAR archive.
   * audioPath is relative to the root of the JAR.
   */
  public static URL URLfromJAR(String audioPath)
  {
    return Thread.currentThread().getContextClassLoader().getResource(audioPath);
  }

  /**
   * Suspend thread execution for the given amount of time (in ms).
   */
  public static void delay(long time)
  {
    try
    {
      Thread.sleep(time);
    }
    catch (InterruptedException ex)
    {
    }
  }

  private void putSleep()
  {
    synchronized (this)
    {
      try
      {
        wait();
      }
      catch (InterruptedException ex)
      {
      }
    }
  }

  private void wakeUp()
  {
    synchronized (this)
    {
      notify();
    }
  }
}
