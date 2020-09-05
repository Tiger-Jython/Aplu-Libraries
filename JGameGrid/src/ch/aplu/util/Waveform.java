// Waveform.java

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

import java.awt.Color;
import javax.swing.JOptionPane;

/**
 * Interface to define the signature of the function to play.
 */
public interface Waveform
{
  /**
   * Signature of the function to play
   * @param t the time in seconds
   * @param freq the frequency in Hertz
   * @return the function return value
   */
  public double f(double t, double freq);

  /**
   * Implements a waveform (sine).
   */
  class SineWave implements Waveform
  {
    /**
     * Declares a sine wave of given frequency: f = sin(omega*t).
     * @param t the time argument
     * @param freq the frequency argument (omega = 2 * PI * freq)
     * @return the function return value
     */
    public double f(double t, double freq)
    {
      return Math.sin(2 * Math.PI * freq * t);
    }

    /**
     * Returns class information.
     * @return information
     */
    public String toString()
    {
      return new String("SineWave");
    }

  }

  /**
   * Implements a waveform (square).
   */
  class SquareWave implements Waveform
  {
    /**
     * Declares a square wave of given frequency with amplitude 1.
     * @param t the time argument
     * @param freq the frequency argument
     * @return the function return value
     */
    public double f(double t, double freq)
    {
      return square(2 * Math.PI * freq * t);
    }

    private double square(double x)
    {
      x = Math.IEEEremainder(x, 2 * Math.PI);
      return x > 0 ? 1.0 : -1.0;
    }

    /**
     * Returns class information.
     * @return information
     */
    public String toString()
    {
      return new String("SquareWave");
    }

  }

  /**
   * Implements a waveform (sawtooth).
   */
  class SawtoothWave implements Waveform
  {
    /**
     * Declares a sawtooth wave of given frequency with amplitude 1.
     * @param t the time argument
     * @param freq the frequency argument
     * @return the function return value
     */
    public double f(double t, double freq)
    {
      return sawtooth(2 * Math.PI * freq * t);
    }

    private double sawtooth(double x)
    {
      return 1 / Math.PI * Math.IEEEremainder(x, 2 * Math.PI);
    }

    /**
     * Returns class information.
     * @return information
     */
    public String toString()
    {
      return new String("SawtoothWave");
    }

  }

  /**
   * Implements a waveform (triangle).
   */
  class TriangleWave implements Waveform
  {
    /**
     * Declares a triangle wave of given frequency with amplitude 1.
     * @param t the time argument
     * @param freq the frequency argument
     * @return the function return value
     */
    public double f(double t, double freq)
    {
      return 2 * Math.abs(sawtooth(2 * Math.PI * freq * t + Math.PI / 2)) - 1.0;
    }

    private double sawtooth(double x)
    {
      return 1 / Math.PI * Math.IEEEremainder(x, 2 * Math.PI);
    }

    /**
     * Returns class information.
     * @return information
     */
    public String toString()
    {
      return new String("TriangleWave");
    }

  }

  /**
   * Implements a waveform (chirp).
   */
  class ChirpWave implements Waveform
  {
    /**
     * Declares a chirp wave of given start frequency.
     * A chirp is a sine function with ascending frequency:
     * fasc = (endFreq - startFreq) / upTime
     * @param t the time argument
     * @param freq the start frequency
     * @return the function return value
     */
    public double f(double t, double freq)
    {
      double upTime = 3;
      double endFreq = 10 * freq;
      double fasc = (endFreq - freq) / upTime * t + freq;
      return Math.sin(2 * Math.PI * fasc * t);
    }

    /**
     * Returns class information.
     * @return information
     */
    public String toString()
    {
      return new String("ChirpWave");
    }

  }

  /**
   * Class to display a simple graphics window with 5 periods of the
   * given waveform.
   * @param wf the Waveform to display
   */
  class WavePlot
  {
    public WavePlot(Waveform wf)
    {
      double tmax = 0.1;
      final GPanel p = new GPanel(-0.01, tmax + 0.01, -2, 2);
      p.title("WavePlot");
      String info = wf.toString();
      if (info.indexOf('@') != -1)
        info = "User Defined Waveform";
      p.text(0.04, 1.8, info);
      p.addExitListener(new ExitListener()
      {
        public void notifyExit()
        {
          int rc = JOptionPane.showConfirmDialog(null,
            "Are you sure?", "Exit Application", JOptionPane.OK_CANCEL_OPTION);
          if (rc == JOptionPane.OK_OPTION)
            System.exit(0);
        }

      });
      p.color(Color.black);
      p.line(-0.002, 0, tmax, 0);
      p.line(0, -1.9, 0, 1.9);
      p.text(-0.004, 0, "0");
      p.text(-0.004, 1, "1");
      p.text(-0.004, -1, "-1");

      p.color(Color.red);
      p.line(-0.002, 1, tmax, 1);
      p.line(-0.002, -1, tmax, -1);

      p.color(Color.blue);
      double t = 0;
      double freq = 50;
      double dt = 0.0001;
      double y;
      double amax = 0;
      while (t <= tmax)
      {
        y = wf.f(t, freq);
        double yabs = Math.abs(y);
        if (amax < yabs)
          amax = yabs;

        if (t == 0)
          p.move(t, y);
        else
          p.draw(t, y);

        t += dt;
      }
    }

  }

}
