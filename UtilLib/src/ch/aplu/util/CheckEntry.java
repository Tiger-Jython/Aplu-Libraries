// CheckEntry.java

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

import java.awt.EventQueue;
import javax.swing.*;

/**
 * Class derived from EntryItem to create a check box in a EntryDialog.
 */
public class CheckEntry extends EntryItem
{
  private JCheckBox cb;
  private boolean touched = false;

  /**
   * Creates a check box with given label text and default check/uncheck
   * sign. Check boxes are displayed vertically and left aligned in the EntryPane.
   * @param text the information text
   * @param init if true, the box is checked; otherwise it is unchecked
   */
  public CheckEntry(String text, boolean init)
  {
    cb = new JCheckBox(text, init);
  }

  /**
   * Returns the underlaying JCheckBox.
   * @return the JCheckBox reference
   */
  public JCheckBox getCheckBox()
  {
    return cb;
  }

  /**
   * Returns the current state of the check box. 
   * @return true, if the box is checked; otherwise false
   */
  public boolean getValue()
  {
    return cb.isSelected();
  }

  /**
   * Sets the current state of the check box programmatically.
   * @param value if true, the box is checked; otherwise unchecked
   */
  public void setValue(final boolean value)
  {
    if (EventQueue.isDispatchThread())
      cb.setSelected(value);
    else
    {
      try
      {
        EventQueue.invokeAndWait(new Runnable()
        {
          public void run()
          {
            cb.setSelected(value);
          }
        });
      }
      catch (Exception ex)
      {
      }
    }
  }

  /**
   * Enable/disable the button.
   */
  public void setEnabled(final boolean enable)
  {
    if (SwingUtilities.isEventDispatchThread())
      cb.setEnabled(enable);
    else
    {
      try
      {
        SwingUtilities.invokeAndWait(
          new Runnable()
          {
            public void run()
            {
              cb.setEnabled(enable);
            }
          });
      }
      catch (Exception ex)
      {
      }
    }
  }

  /**
   * Returns true, if check box control was hit by the mouse until the last call if this method.
   * Any further call will return false, until the button is hit again.
   * Puts calling thread to sleep for 1 millisecond, so it can be used 
   * in a tight polling loop.
   * @return true, if the control was hit since the last call
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
