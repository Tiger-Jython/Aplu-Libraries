// MessageDialog.java

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

import java.io.*;
import java.awt.event.*;
import java.awt.*;
import javax.swing.*;
import javax.swing.JOptionPane;

/** A simple message dialog (with no prompt button).
 * The dialog is modeless, e.g. it "floats" while
 * the application continues.<br><br>
 * All Swing methods are invoked in the EDT.
 */
public class MessageDialog
{
  // ------------- Inner class MyWindowAdapter ----------
  private class MyWindowAdapter extends WindowAdapter
  {
    public void windowClosing(WindowEvent evt)
    {
      if (_exitOnClose)
        System.exit(0);
    }

  }
  // ------------- End of inner class -------------

  private JDialog _msgDialog;
  private boolean _exitOnClose = false;

  /**
   * Construct a dialog with given message, which is not
   * yet shown. show() will display
   * the dialog in the center of the given component.<br>
   * If parent = null, a standalone window with a title bar containing the
   * close button only will be used. Clicking the close button will hide
   * the window, but not terminate the application.<br><br>
   * Runs in the Event Dispatch Thread (EDT).
   */
  public MessageDialog(final Component parent, final String message, final String iconPath)
  {
    if (EventQueue.isDispatchThread())
      createMessageDialog(parent, message, iconPath);
    else
    {
      try
      {
        EventQueue.invokeAndWait(new Runnable()
        {
          public void run()
          {
            createMessageDialog(parent, message, iconPath);
          }

        });
      }
      catch (Exception ex)
      {
      }
    }
  }

  private void createMessageDialog(Component parent, String message, String iconPath)
  {
    ImageIcon icon = null;
    if (iconPath != null && iconPath.length() > 0)
    {
      java.net.URL imgURL = getClass().getResource(iconPath);
      if (imgURL != null)
        icon = new ImageIcon(imgURL);
    }
    Object[] options =
    {
    };
    JOptionPane pane = new JOptionPane(message,
      JOptionPane.DEFAULT_OPTION,
      JOptionPane.INFORMATION_MESSAGE,
      icon,
      options,
      null);
    _msgDialog = pane.createDialog(parent, "Message");
    _msgDialog.addWindowListener(new MyWindowAdapter());
  }

  /**
   * Same as MessageDialog(parent, message, iconPath) with iconPath = null.
   */
  public MessageDialog(Component parent, String message)
  {
    this(parent, message, null);
  }

  /**
   * Same as MessageDialog(parent, message, iconPath) with parent = null and iconPath = null.
   */
  public MessageDialog(String message)
  {
    this(null, message);
  }

  /**
   * Show a modeless message dialog with given message.
   * Runs in the Event Dispatch Thread (EDT).
   * The dialog is modeless (the method does not block).
   * Return a reference to the current instance.
   */
  public MessageDialog show()
  {
    return show(false);
  }

  /**
   * Same as show(). If exitOnClose = true, the process is terminated
   * when the close button in the title bar is clicked.
   */
  public MessageDialog show(boolean exitOnClose)
  {
    _exitOnClose = exitOnClose;
    if (EventQueue.isDispatchThread())
      showInternal();
    else
    {
      try
      {
        EventQueue.invokeAndWait(new Runnable()
        {
          public void run()
          {
            showInternal();
          }

        });
      }
      catch (Exception ex)
      {
      }
    }
    return this;
  }

  private void showInternal()
  {
    _msgDialog.setModal(false);
    _msgDialog.setVisible(true);
  }

  /**
   * Hide the modeless message dialog previously shown.
   * May be reshown calling show().
   * Return a reference to the current instance.
   */
  public MessageDialog close()
  {
    if (EventQueue.isDispatchThread())
      _msgDialog.dispose();
    else
    {
      try
      {
        EventQueue.invokeAndWait(new Runnable()
        {
          public void run()
          {
            _msgDialog.dispose();
          }

        });
      }
      catch (Exception ex)
      {
      }
    }
    return this;
  }

  /**
   * Return the dialog.
   */
  public JDialog getDialog()
  {
    return _msgDialog;
  }

}
