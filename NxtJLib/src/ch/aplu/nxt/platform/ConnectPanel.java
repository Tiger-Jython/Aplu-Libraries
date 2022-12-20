// ConnectPanel.java, Java SE version
// Platform (Java SE, ME) dependent code
// Should be visible in package only. Not included in Javadoc

/*
 This software is part of the NxtJLib library.
 It is Open Source Free Software, so you may
 - run the code for any purpose
 - study how the code works and adapt it to your needs
 - integrate all or parts of the code in your own programs
 - redistribute copies of the code
 - improve the code and release your improvements to the public
 However the use of the code is entirely your responsibility.
 */
package ch.aplu.nxt.platform;

import ch.aplu.nxt.*;
import ch.aplu.util.*;
import javax.swing.*;
import java.net.URL;
import java.awt.*;

/**
 * Message dialog with standard title and NXT icon.
 */
public class ConnectPanel implements SharedConstants
{
  private static URL iconUrl;
  private static String iconResourcePath = "ch/aplu/nxt/gifs/nxt.gif";
  private static ModelessOptionPane mop = null;
  private static String value;
  protected static LegoRobot.ClosingMode myClosingMode;

  private ConnectPanel()  // No instance allowed
  {
    ClassLoader loader = getClass().getClassLoader();
    iconUrl = loader.getResource(iconResourcePath);
    String emptyMsg = "                                                                                         ";
    Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();
    int xCenter = dim.width / 2;
    int yCenter = dim.height / 2;
    mop = new ModelessOptionPane(xCenter + 55, yCenter - 55, emptyMsg, iconUrl);
    mop.showTitle(TITLE);
    mop.addExitListener(
      new ExitListener()
      {
        public void notifyExit()
        {
          if (LegoRobot.isConnecting)
          {
            mop.setTitle("Connecting. Please wait...");
            return;
          }
        }
      });
  }

  public static ModelessOptionPane getMop()
  {
    return mop;
  }

  /**
   * Shows a modal dialog and asks for Bluetooth friendly name.
   * @return the trimmed string entered when the OK button is hit.
   */
  public static String askBtName(LegoRobot.ClosingMode closingMode)
  {
    NxtProperties props = LegoRobot.getProperties();
    final String btName = props.getStringValue("BluetoothName");
    myClosingMode = closingMode;
    if (SwingUtilities.isEventDispatchThread())
      value = askInternal(btName);
    else
    {
      try
      {
        EventQueue.invokeAndWait(new Runnable()
        {
          public void run()
          {
            value = askInternal(btName);
          }
        });
      }
      catch (Exception ex)
      {
      }
    }
    return value;
  }

  private static String askInternal(String btName)
  {
    Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();
    final int xCenter = dim.width / 2;
    final int yCenter = dim.height / 2;
    String prompt = "Enter Bluetooth Name";
    String str = null;
    JFrame frame = new JFrame()
    {
      public boolean isShowing()
      {
        return true;
      }

      public Rectangle getBounds()
      {
        return new Rectangle(xCenter + 150, yCenter, 0, 0);
      }
    };
    do
    {
      str = JOptionPane.showInputDialog(frame, prompt, btName);
      if (str == null)
      {
        switch (myClosingMode)
        {
          case TerminateOnClose:
            System.exit(0);
            break;
          case ReleaseOnClose:
          case DisposeOnClose:
            frame.dispose();
            return null;
        }
      }
    }
    while (str.trim().length() == 0);
    frame.dispose();
    return str;
  }

  /**
   * Creates a modeless dialog and shows the given text.
   * Returns immediately. If the dialog is already created, replaces the
   * text. If the dialog is hidden, shows it with the given text.
   * @param text the text to display
   */
  public static void show(String text)
  {
    if (mop == null)
      create();
    mop.setText(text, false);
    mop.setVisible(true);
  }
  
  /**
   * Shows the version title.
   */
  public static void showVersion()
  {
    if (mop != null)
      mop.setTitle(TITLE);
  }

  /**
   * Hides the dialog. May be redisplayed with show().
   */
  public static void hide()
  {
    if (mop != null)
      mop.setVisible(false);
  }

  /** 
   * Disposes the dialog.
   */
  public static void dispose()
  {
    if (mop != null)
    {
      mop.dispose();
      mop = null;
    }
  }

  private static void create()
  {
    if (mop == null)
      new ConnectPanel();
  }
}
