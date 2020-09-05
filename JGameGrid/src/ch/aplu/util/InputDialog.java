// InputDialog.java

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
import java.awt.*;

/**
 * Modal input dialog.<br><br>
 * If the user enters a value with wrong type, the dialog is 
 * displayed again.<br><br>
 * All methods run in the Event Dispatch Thread (EDT).
 */
public class InputDialog
{
  private static String _title = null;
  private static String _prompt = null;
  private static String _str;

  /**
   * Construct dialog with default title and prompt.
   */
  public InputDialog()
  {
    this("Input Dialog", "Enter a value please:");
  }

  /**
   * Construct dialog with given title and prompt.
   */
  public InputDialog(String title, String prompt)
  {
    _title = title;
    _prompt = prompt;
  }

  /**
   * Show the dialog to get a string value (same as readString()).
   * @return null, if the title bar's cancel button is pressed
   */
  public static String getString()
  {
    if (EventQueue.isDispatchThread())
      _str = askString();
    else
    {
      try
      {
        EventQueue.invokeAndWait(new Runnable()
        {
          public void run()
          {
            _str = askString();
          }
        });
      }
      catch (Exception ex)
      {
      }
    }
    return _str;
  }

  /**
   * Show the dialog to get a string value (same as getString()).
   * @return null, if the title bar's cancel button is pressed
   */
  public static String readString()
  {
    return getString();
  }

  private static String askString()
  {
    String valueStr;
    do
    {
      JOptionPane optionPane =
        new JOptionPane(_prompt,
        JOptionPane.QUESTION_MESSAGE,
        JOptionPane.DEFAULT_OPTION,
        null, null, null);
      optionPane.setWantsInput(true);
      JDialog dialog = optionPane.createDialog(null, _title);
      dialog.setLocation(10, 20);
      dialog.setVisible(true);
      Object value = optionPane.getInputValue();
      dialog.dispose();
      if (value == JOptionPane.UNINITIALIZED_VALUE)
        return null;
      valueStr = (String)value;

    }
    while (valueStr.trim().length() == 0);
    return valueStr;
  }

  /**
   * Show the dialog to get an integer value.
   * @return null, if the title bar's cancel button is pressed
   */
  public static Integer getInt()
  {
    Integer value = null;
    String valueStr;
    boolean ok = false;

    while (!ok)
    {
      valueStr = getString();
      if (valueStr == null)
        return null;
      try
      {
        value = new Integer(valueStr);
        ok = true;
      }
      catch (NumberFormatException e)
      {
      }
    }
    return value;
  }

  /**
   * Show the dialog to read an integer value.
   * Redraw the dialog with empty field, wrong data type or 
   * if title bar's cancel button is pressed.
   * @return the integer value
   */
  public static int readInt()
  {
    Integer value = null;
    do
    {
      value = getInt();
    }
    while (value == null);
    return value.intValue();
  }

  /**
   * Show the dialog to get a double value.
   * @return null, if the title bar's cancel button is pressed
   */
  public static Double getDouble()
  {
    Double value = null;
    String valueStr;
    boolean ok = false;

    while (!ok)
    {
      valueStr = getString();
      if (valueStr == null)
        return null;
      try
      {
        value = new Double(valueStr);
        ok = true;
      }
      catch (NumberFormatException e)
      {
      }
    }
    return value;
  }

  /**
   * Show the dialog to read a double value.
   * Redraw the dialog with empty field, wrong data type or 
   * if title bar's cancel button is pressed.
   * @return the double value
   */
  public static double readDouble()
  {
    Double value = null;
    do
    {
      value = getDouble();
    }
    while (value == null);
    return value.doubleValue();
  }

  /**
   * Show the dialog to get a long value.
   * @return null, if the title bar's cancel button is pressed
   */
  public static Long getLong()
  {
    Long value = null;
    String valueStr;
    boolean ok = false;

    while (!ok)
    {
      valueStr = getString();
      if (valueStr == null)
        return null;
      try
      {
        value = new Long(valueStr);
        ok = true;
      }
      catch (NumberFormatException e)
      {
      }
    }
    return value;
  }

  /**
   * Show the dialog to read a long value.
   * Redraw the dialog with empty field, wrong data type or 
   * if title bar's cancel button is pressed.
   * @return the long value
   */
  public static long readLong()
  {
    Long value = null;
    do
    {
      value = getLong();
    }
    while (value == null);
    return value.longValue();
  }

  /**
   * Show the dialog with a yes/no button pair to get a boolean.
   * @return Boolean.TRUE or BOOLEAN.FALSE depending the button that is hit;
   * null if the title bar's cancel button is pressed
   */
  public static Boolean getBoolean()
  {
   if (_title == null)
     _title = "Confirmation Dialog";
   if (_prompt == null)
     _prompt = "Please select:";
   int rc = JOptionPane.showConfirmDialog(null, _prompt, _title,
                                 JOptionPane.YES_NO_OPTION);
   if (rc == JOptionPane.YES_OPTION)
     return Boolean.TRUE;
   if (rc == JOptionPane.NO_OPTION)
     return Boolean.FALSE;
   return null;
  }

  /**
   * Show the dialog with a yes/no button pair to get a boolean.
   * Redraw the dialog, if title bar's cancel button is pressed.
   * @return true or false depending the button that is hit
   */
  public static boolean readBoolean()
  {
    Boolean value = null;
    do
      value = getBoolean();
    while (value == null);
    return value.booleanValue();
  }
}
