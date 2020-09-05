// RadioEntry.java

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
 * Class derived from EntryItem to create a radio button in a EntryDialog.
 * All radio buttons contained in the same EntryPane are grouped together
 * as combo box, so that only one of the buttons is selected. 
 * Radio buttons are displayed vertically and left aligned in the EntryPane.
 */
public class RadioEntry extends EntryItem
{
  private JRadioButton rb;
  private boolean touched = false;

  /**
   * Creates a radio with given label text
   * @param text the information text
   */
  public RadioEntry(String text)
  {
    rb = new JRadioButton(text);
  }

  protected JRadioButton getRadioButton()
  {
    return rb;
  }

  /**
   * Returns the current state of the radio button.
   * @return true, if the button is selected; otherwise false
   */
  public boolean getValue()
  {
    return rb.isSelected();
  }

    /**
   * Sets the current state of the radio button programmatically.
   * @param value if true, the box is checked; otherwise unchecked
  */
  public void setValue(boolean value)
  {
    rb.setSelected(value);
  }

  /**
   * Enable/disable the button.
   */
  public void setEnabled(final boolean enable)
  {
    if (SwingUtilities.isEventDispatchThread())
      rb.setEnabled(enable);
    else
    {
      try
      {
        SwingUtilities.invokeAndWait(
          new Runnable()
          {
            public void run()
            {
              rb.setEnabled(enable);
            }
          });
      }
      catch (Exception ex)
      {
      }
    }
  }
  
   /**
   * Returns true, if radio button widget was hit by the mouse until the last call if this method.
   * Any further call will return false, until the button is hit again.
   * Puts calling thread to sleep for 1 millisecond, so it can be used 
   * in a tight polling loop.
   * @return true, if the widget was hit since the last call
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
