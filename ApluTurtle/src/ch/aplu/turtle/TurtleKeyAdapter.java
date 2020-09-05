// TurtleKeyAdapter

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

import java.awt.event.*;

/**
 * Class that overides KeyAdapter.keyPressed() in order to get
 * key events. You must register an instance with Turtle.addKeyListener().
 */
public class TurtleKeyAdapter extends KeyAdapter
{
  private static int keyCode;
  private static Object monitor = new Object();

  /**
   * For internal use only.
   */
  public void keyPressed(KeyEvent evt)
  {
    keyCode = evt.getKeyCode();
    synchronized (monitor)
    {
      monitor.notify();
    }
  }

  /**
   * Waits for a keystroke and returns the keycode.
   */
  public static int getKeyCodeWait()
  {
    synchronized (monitor)
    {
      try
      {
        monitor.wait();
      }
      catch (InterruptedException ex)
      {
      }
    }
    return keyCode;
  }

}