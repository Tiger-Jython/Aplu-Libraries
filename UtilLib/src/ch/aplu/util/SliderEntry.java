// SliderEntry.java

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
 * Class derived from EntryItem to create a horizontal slider.
 * IntEntry, LongEntry, DoubleEntry and StringEntry can be mixed up in the same EntryPane.
 */
public class SliderEntry extends EntryItem
{
  private JSlider slider;
  private boolean touched = false;

  /**
   * Creates a horizontal slider with given properties.
   * @param min the minimum value of the slider
   * @param max the maximum value of the slider
   * @param init the initial knob position of the slider
   * @param majorSpacing the number of values between the major tick marks
   * @param minorSpacing the number of values between the minor tick marks
   */
  public SliderEntry(int min, int max, int init,
    int majorSpacing, int minorSpacing)
  {
    slider = new JSlider(JSlider.HORIZONTAL, min, max, init);
    slider.setMajorTickSpacing(majorSpacing);
    slider.setMinorTickSpacing(minorSpacing);
    slider.setPaintTicks(true);
    slider.setPaintLabels(true);
  }

  /**
   * Returns the underlaying JSlider
   * @return the JSlider reference
   */
  public JSlider getSlider()
  {
    return slider;
  }

  /**
   * Returns the current knob position.
   * @return the current value
   */
  public int getValue()
  {
    return slider.getValue();
  }

  /**
   * Sets the current knob position programmatically.
   * @param value the new knob position
   */
  public void setValue(int value)
  {
    slider.setValue(value);
  }

  /**
   * Enable/disable the button.
   */
  public void setEnabled(final boolean enable)
  {
    if (SwingUtilities.isEventDispatchThread())
      slider.setEnabled(enable);
    else
    {
      try
      {
        SwingUtilities.invokeAndWait(
          new Runnable()
          {
            public void run()
            {
              slider.setEnabled(enable);
            }
          });
      }
      catch (Exception ex)
      {
      }
    }
  }

  /**
   * Returns true, if the slider widget was hit by the mouse until the last call if this method.
   * Any further call will return false, until the button is hit again.
   * Puts calling thread to sleep for 1 millisecond, so it can be used 
   * in a tight polling loop.
   * @return true, if the slider was hit since the last call
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
