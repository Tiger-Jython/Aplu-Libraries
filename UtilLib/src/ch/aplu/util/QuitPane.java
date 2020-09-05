// QuitPane.java

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

import java.awt.*;
import javax.swing.*;
import java.awt.event.*;

/**
 * Modeless dialog containing a "Quit" button only.
 * Mainly used to hold the thread in a endless loop.
 * in simple realtime applications or to simulate a modeless application window
 * for testing or demonstration purposes.<br><br>
 * Only one QuitPane may be used.<br>
 * If no exist listener is registered, clicking the title bar's close button
 * calls System.exit(0), otherwise its notifyExit() method
 * is called.<br><br>
 * All Swing methods are invoked in the EDT.
 */
public class QuitPane
{
  private class ApplyBtnActionAdapter implements ActionListener
  {
    public void actionPerformed(ActionEvent evt)
    {
      if (quitNotifier != null)
        quitNotifier.clean();
      pressed = true;
      wakeUp();
    }
  }
  private static JDialog dlg;
  private static boolean pressed;
  private static boolean doExit = true;
  private JPanel contentPane;
  private JButton applyBtn;
  private static Object monitor = new Object();
  private Cleanable quitNotifier = null;
  private static ExitListener exitListener = null;

  /**
   * Same as QuitPane(true).
   */
  public QuitPane()
  {
    this(true);
  }

  /**
   * Construct a QuitPane with given visibility. Runs in Event Dispatch Thread.
   * (Only one instance is allowed)
   */
  public QuitPane(final boolean visible)
  {
    if (EventQueue.isDispatchThread())
      createQuitPane(visible);
    else
    {
      try
      {
        EventQueue.invokeAndWait(new Runnable()
        {
          public void run()
          {
            createQuitPane(visible);
          }
        });
      }
      catch (Exception ex)
      {
      }
    }
  }

  private void createQuitPane(boolean visible)
  {
    pressed = false;  // Initialized here because of static load in python
    if (dlg != null)
    {
      JOptionPane.showMessageDialog(null, "Only one instance of QuitPane allowed.");
      System.exit(0);
    }
    dlg = new JDialog()
    {
      protected void processWindowEvent(WindowEvent e)
      {
        if (e.getID() == WindowEvent.WINDOW_CLOSING)
        {
          if (quitNotifier != null)
          {
            quitNotifier.clean();
            wakeUp();
          }
          if (exitListener != null)
          {
            exitListener.notifyExit();
            wakeUp();
          }
          if (quitNotifier == null && exitListener == null)
          {
            if (doExit)
              System.exit(0);
            else
            {
              pressed = true;
              wakeUp();
            }  
          }
        }
        else
          super.processWindowEvent(e);
      }
    };
    contentPane = (JPanel)dlg.getContentPane();
    applyBtn = new JButton("Quit");
    applyBtn.addActionListener(new ApplyBtnActionAdapter());
    contentPane.add(applyBtn);
    dlg.setTitle("QuitPane");
    dlg.pack();
    Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
    int width = 100;
    int height = 70;
    dlg.setBounds(((int)screenSize.getWidth() - width) / 2,
      ((int)screenSize.getHeight() - height) / 2, width, height);

    dlg.setVisible(visible);
  }

  /**
   * Create a QuitPane dialog, if it's not yet done.
   * Register a class with a method notifyExit() that is called when the
   * title bar's close button is hit.
   */
  public static void addExitListener(ExitListener listener)
  {
    if (dlg == null)
      new QuitPane(true);
    exitListener = listener;
  }
  
  /**
   * Same as quit(), but if exit = false clicking the title bar's close
   * button acts the same as clicking the Quit button
   * (System.exit(0) will not be called anymore).
   * Useful if you need to proceed in any case. To remove the dialog, call
   * QuitPane.dispose().
   */ 
   public static boolean quit(boolean exit)
   {
    if (dlg == null)
      new QuitPane(true);
    doExit = exit;
    try
    {
      Thread.currentThread().sleep(1);
    }
    catch (InterruptedException ex)
    {
    }
    return pressed;
   }


  /**
   * Create a QuitPane dialog, if it's not yet done.
   * Return true, if the Quit button has been hit previously.<br>
   * Thread.currentThread().sleep(1) is called to improve time response
   * when quit() is called in a tight loop like<br>
   * <code>while (!QuitPane.quit()) {}</code><br><br>
   * If the title bar's close button is hit, System.exit(0) is called.
   */
  public static boolean quit()
  {
    return quit(true);
  }

  /**
   * Create a QuitPane dialog, if it's not yet done.
   * Block the current thread until the Quit button is hit.
   * If no cleanable is registered, clicking the title bar's close button
   * will call System.exit(0), if a cleanable is registered, its clean() method
   * will be called.<br><br>
   * After halt() returns, the internal state is resetted, so that quit()
   * returns false.
   */
  public static void halt()
  {
    halt(true);
  }

  /**
   * Same as halt(), but if exit = false clicking the title bar's close
   * button acts the same as clicking the Quit button
   * (System.exit(0) will not be called anymore).
   * Useful if you need to proceed in any case. To remove the dialog, call
   * QuitPane.dispose().
   */
  public static void halt(boolean exit)
  {
    if (dlg == null)
      new QuitPane(true);
    else
      setVisible(true);
    doExit = exit;
    putSleep();
    pressed = false;
  }

  /**
   * Create a QuitPane dialog, if it's not yet done with the given visibility.
   * Reset the internal state, so that quit() returns false.<br>
   */
  public static void init(boolean visible)
  {
    if (dlg == null)
      new QuitPane(visible);
    else
    {
      dlg.setVisible(visible);
      pressed = false;
    }
  }

  /**
   * Same as init(true).
   */
  public static void init()
  {
    init(true);
  }

  /**
   * Hide the dialog.
   * May be shown again by calling init().
   */
  public static void close()
  {
    dlg.setVisible(false);
  }

  /**
   * Hide the dialog and release the resources.
   */
  public static void dispose()
  {
    if (dlg == null)
      return;
    if (EventQueue.isDispatchThread())
    {
      dlg.setVisible(false);
      dlg.dispose();
      dlg = null;
      pressed = false;
    }
    else
    {
      try
      {
        EventQueue.invokeAndWait(new Runnable()
        {
          public void run()
          {
            dlg.setVisible(false);
            dlg.dispose();
            dlg = null;
            pressed = false;
          }
        });
      }
      catch (Exception ex)
      {
      }
    }
  }

  /**
   * Return the dialog.
   */
  public static JDialog getDialog()
  {
    return dlg;
  }

  /**
   * Show/hide the dialog.
   * @param visible if true, the dialog is shown, otherwise hidden.
   */
  public static void setVisible(final boolean visible)
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

  /**
   * Register  a Cleanble whose clean method will be called when
   * the Quit or Close button is hit.
   */
  public void addQuitNotifier(Cleanable notifier)
  {
    quitNotifier = notifier;
  }

  private static void putSleep()
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
  }

  private static void wakeUp()
  {
    synchronized (monitor)
    {
      monitor.notifyAll();
    }
  }
}
