// ConnectPanel.java, Java SE version
// Direct mode

/*
This software is part of the EV3JLib library.
It is Open Source Free Software, so you may
- run the code for any purpose
- study how the code works and adapt it to your needs
- integrate all or parts of the code in your own programs
- redistribute copies of the code
- improve the code and release your improvements to the public
However the use of the code is entirely your responsibility.
*/
package ch.aplu.ev3;

import ch.aplu.util.*;
import javax.swing.*;
import java.net.URL;
import java.awt.*;

/**
 * Message dialog with standard title and EV3 icon.
 */
public class ConnectPanel 
{
  private static URL iconUrl;
  private static String iconResourcePath = "ch/aplu/ev3/gifs/ev3.gif";
  private static ModelessOptionPane mop = null;
  private static String value;
  private static String defaultIPAddress = null;

  private ConnectPanel()  // No instance allowed
  {
    ClassLoader loader = getClass().getClassLoader();
    iconUrl = loader.getResource(iconResourcePath);
    String emptyMsg = "                                                                                         ";
    mop = new ModelessOptionPane(10, 10, emptyMsg, iconUrl);
    mop.showTitle(SharedConstants.TITLE);
    mop.addExitListener(
      new ExitListener()
      {
        public void notifyExit()
        {
           mop.setTitle("Connecting. Please wait a moment...");
        }
      });

  }

  public static ModelessOptionPane getMop()
  {
    return mop;
  }
  
  public static void setDefaultIPAddress(String ipAddress)
  {
    defaultIPAddress = ipAddress;
  }

  /**
   * Shows a modal dialog and asks for IP address.
   * @return the trimmed string entered when the OK button is hit.
   */
  public static String askIPAddress()
  {
    final String btName;
    if (defaultIPAddress == null)
    {  
        EV3Properties props = LegoRobot.getProperties();
        btName = props.getStringValue("IPAddress");
    }
    else
      btName = defaultIPAddress;

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
    String prompt = "Enter IP address:";
    String str = null;
    JFrame frame = new JFrame()
    {
      public boolean isShowing()
      {
        return true;
      }

      public Rectangle getBounds()
      {
        return new Rectangle(xCenter, yCenter, 0, 0);
      }
    };
    do
    {
      str = JOptionPane.showInputDialog(frame, prompt, btName);
      if (str == null)
      {
        switch (LegoRobot.myClosingMode)
        {
          case TerminateOnClose:
            System.exit(0);
            break;
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
   * Shows the text.
   * @param text the text to display
   */
  public static void show(String text)
  {
    if (mop == null)
      new ConnectPanel();
    mop.setText(text, false);
  }
  
  /**
   * Shows the version title.
   */
  public static void showVersion()
  {
    if (mop != null)
      mop.setTitle(SharedConstants.TITLE);
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

  /**
  * Creates the panel.
  */
  public static void create()
  {
    if (mop == null)
    {  
      new ConnectPanel();
    }
      
  }
}
