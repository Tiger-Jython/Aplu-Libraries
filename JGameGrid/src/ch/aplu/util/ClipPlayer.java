// ClipPlayer.java

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

class ClipPlayer // not public
{
  protected Clip clip = null;
  protected AudioFormat audioFormat;
  protected AudioInputStream audioInputStream = null;

  public ClipPlayer(AudioFormat audioFormat)
  {
    this.audioFormat = audioFormat;
  }

  /**
   * Starts playing the sound clip and returns immediately.
   * @return true, if successfull; otherwise false (IO errors, sound card errors)
   */
  public boolean start()
  {
    return start(false, false);
  }

  /**
   * Starts playing the sound clip and blocks until finished if blocking is true.
   * @param blocking if true, the method blocks until the clip is finished
   * @return true, if successfull; otherwise false (IO errors, sound card errors)
   */
  public boolean start(boolean blocking)
  {
    return start(false, blocking);
  }

  /**
   * Starts playing the sound clip continously and returns immediately.
   * @return true, if successfull; otherwise false
   */
  public boolean loop()
  {
    return start(true, false);
  }

  private boolean start(boolean loop, boolean blocking)
  {
    if (audioInputStream == null)
      return false;
    DataLine.Info dataLineInfo = new DataLine.Info(Clip.class, audioFormat);

    if (clip == null) // First invocation
    {
      try
      {
        clip = (Clip)AudioSystem.getLine(dataLineInfo);
        clip.addLineListener(new LineListener()
        {
          public void update(LineEvent lineEvent)
          {
            if (lineEvent.getType() == LineEvent.Type.STOP)
              wakeUp();
          }

        });
        try
        {
          clip.open(audioInputStream);
        }
        catch (IOException ex)
        {
          clip = null;
          return false;
        }
      }
      catch (LineUnavailableException ex)
      {
        clip.flush();
        clip = null;
        return false;
      }
    }
    else // replay same clip, put it at start
      clip.setFramePosition(0);
    if (loop)
      clip.loop(Clip.LOOP_CONTINUOUSLY);
    else
      clip.start();
    if (blocking)
      putSleep();
    return true;
  }

  /**
   * Stops playing the sound clip and discards all remaining data.
   */
  public void stop()
  {
    if (clip == null)
      return;
    clip.stop();
    clip.flush();
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
