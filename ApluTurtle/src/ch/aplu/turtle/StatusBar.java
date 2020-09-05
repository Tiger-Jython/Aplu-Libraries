// StatusBar.java

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

import javax.swing.*;
import java.awt.event.*;
import java.net.URL;
import java.awt.*;

class StatusBar
{
  // ---------------- Inner class SetText ----------------
  private class SetText implements Runnable
  // Used in order to call Swing methods from EDT only
  {
    private String text;

    private SetText(String text)
    {
      this.text = text;
    }

    public void run()
    {
      optionPane.setMessage(text);
    }

  }
  // ---------------- End of inner class -----------------
  //

  private JDialog dlg;
  private JOptionPane optionPane;
  private Frame owner = null;

  protected StatusBar(Frame owner, Point position, Dimension size)
  {
    this.owner = owner;
    init(position, size);
  }

  private void init(final Point position, final Dimension size)
  {
    if (EventQueue.isDispatchThread())
      doInit(position, size);
    else
    {
      try
      {
        EventQueue.invokeAndWait(new Runnable()
        {
          public void run()
          {
            doInit(position, size);
          }

        });
      }
      catch (Exception ex)
      {
      }
    }
  }

  private void doInit(Point position, Dimension size)
  {
    dlg = new JDialog(owner)
    {
      protected void processWindowEvent(WindowEvent e)
      {
        if (e.getID() == WindowEvent.WINDOW_ACTIVATED)
        {
          if (owner != null)
            owner.toFront();
        }
      }

    };

    optionPane = new JOptionPane("");
    dlg.setUndecorated(true);
    optionPane.setBorder(BorderFactory.createLineBorder(Color.black));
    optionPane.setOptionType(JOptionPane.DEFAULT_OPTION);
    optionPane.setMessageType(JOptionPane.INFORMATION_MESSAGE);
    optionPane.setIcon(new ImageIcon("...."));
    optionPane.setOptions(new Object[]
      {
      });
    optionPane.setInitialSelectionValue(null);

    dlg.setContentPane(optionPane);
    dlg.pack();
    dlg.setSize(size);
    dlg.setResizable(false);
    dlg.setLocation(position);
    dlg.setVisible(true);
  }

  protected void setText(String text)
  {
    if (SwingUtilities.isEventDispatchThread())
    {
      optionPane.setMessage(text);
    }
    else
    {
      try
      {
        SwingUtilities.invokeAndWait(new SetText(text));
      }
      catch (Exception ex)
      {
      }
    }
  }

  protected void setVisible(final boolean visible)
  {
    if (EventQueue.isDispatchThread())
      dlg.setVisible(visible);
    else
    {
      try
      {
        EventQueue.invokeAndWait(new Runnable()
        {
          public void run()
          {
            dlg.setVisible(visible);
          }

        });
      }
      catch (Exception ex)
      {
      }
    }
  }

  protected JDialog getDialog()
  {
    return dlg;
  }

}
