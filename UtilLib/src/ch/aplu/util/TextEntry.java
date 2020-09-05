// TextEntry.java

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
 * Base class of StringEntry, DoubleEntry, IntegerEntry.
 * Text fields are displayed vertically in the EntryPane.
 */
public class TextEntry extends EntryItem
{
  private JLabel lbl = new JLabel();
  private JTextField tf = new JTextField(10);
  private boolean touched = false;
  private boolean isEditable = true;

  protected TextEntry(String prompt, String init)
  {
    lbl.setText(" " + prompt + " ");
    if (init != null)
      tf.setText(init);
  }

  protected String getTextValue()
  {
    return tf.getText().trim();
  }

  protected void setEditable(final boolean b)
  {
    if (EventQueue.isDispatchThread())
      tf.setEditable(b);
    else
    {
      try
      {
        EventQueue.invokeAndWait(new Runnable()
        {
          public void run()
          {
            tf.setEditable(b);
          }
        });
      }
      catch (Exception ex)
      {
      }
    }
  }

  protected void setTextValue(final String value)
  {
    if (EventQueue.isDispatchThread())
      tf.setText(value);
    else
    {
      try
      {
        EventQueue.invokeAndWait(new Runnable()
        {
          public void run()
          {
            tf.setText(value);
          }
        });
      }
      catch (Exception ex)
      {
      }
    }
  }

  /**
   * Returns the underlaying JTextField.
   * @return the JTextField reference
   */
  public JTextField getTextField()
  {
    return tf;
  }

  /**
   * Returns the underlaying JLabel.
   * @return the JLabel reference
   */
  public JLabel getLabel()
  {
    return lbl;
  }

  /**
   * Enable/disable the text field.
   */
  public void setEnabled(final boolean enable)
  {
    if (SwingUtilities.isEventDispatchThread())
    {
      tf.setEnabled(enable);
      lbl.setEnabled(enable);
    }
    else
    {
      try
      {
        SwingUtilities.invokeAndWait(
          new Runnable()
          {
            public void run()
            {
              tf.setEnabled(enable);
              lbl.setEnabled(enable);
            }
          });
      }
      catch (Exception ex)
      {
      }
    }
  }
  
   /**
   * Returns true, if text field widget was hit by the mouse until the last call if this method.
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
