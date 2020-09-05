// ButtonEntry.java

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

import javax.swing.*;

/**
 * Class derived from EntryItem to create a push button in a EntryDialog.
 * Buttons are displayed horizontally and center aligned in the EntryPane.
 */
public class ButtonEntry extends EntryItem
{
  private JButton btn;
  private boolean touched = false;

  /**
   * Creates a push button with given button text.
   * @param text the button text
   */
  public ButtonEntry(String text)
  {
    btn = new JButton(text);
  }

  /**
   * Returns the underlaying JButton.
   * @return the JButton reference
   */
  public JButton getButton()
  {
    return btn;
  }

  /**
   * Returns the current state of the button. 
   * @return true, if the button is pressed; otherwise false
   */
  public boolean getValue()
  {
    return btn.getModel().isPressed();
  }

  /**
   * Enable/disable the button.
   */
  public void setEnabled(final boolean enable)
  {
    if (SwingUtilities.isEventDispatchThread())
      btn.setEnabled(enable);
    else
    {
      try
      {
        SwingUtilities.invokeAndWait(
          new Runnable()
          {
            public void run()
            {
              btn.setEnabled(enable);
            }
          });
      }
      catch (Exception ex)
      {
      }
    }
  }

  /**
   * Returns true, if the button was hit by the mouse until the last call if this method.
   * Any further call will return false, until the button is hit again.
   * Puts calling thread to sleep for 1 millisecond, so it can be used 
   * in a tight polling loop.
   * @return true, if the button was hit since the last call
   */
  public boolean isTouched()
  {
    delay(1);
    boolean b = touched;
    touched = false;
    return b;
  }

  protected void setTouched(boolean b)
  {
    touched = b;
  }

  private static void delay(int time)
  {
    try
    {
      Thread.currentThread().sleep(time);
    }
    catch (Exception e)
    {
    }
  }

}
