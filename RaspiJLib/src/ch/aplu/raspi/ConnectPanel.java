// ConnectPanel.java

/*
 This software is part of the RaspiJLib library.
 It is Open Source Free Software, so you may
 - run the code for any purpose
 - study how the code works and adapt it to your needs
 - integrate all or parts of the code in your own programs
 - redistribute copies of the code
 - improve the code and release your improvements to the public
 However the use of the code is entirely your responsibility.
 */
package ch.aplu.raspi;

import ch.aplu.util.*;
import javax.swing.*;
import java.net.URL;
import java.awt.*;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;

/**
 * Message dialog with standard title and Raspi icon.
 */
public class ConnectPanel
{
  private static URL iconUrl;
  private static String iconResourcePath = "ch/aplu/raspi/gifs/raspi.gif";
  private static ModelessOptionPane mop = null;
  private static String value;
  private static final String FS = System.getProperty("file.separator");
  private static final String propPath = System.getProperty("user.home") + FS
    + ".RaspiRemote.properties";
  private static final Properties prop = new Properties();
  private static File propFile = null;
  private static String savedIP = null;

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

  /**
   * Shows a modal dialog and asks for IP address.
   * @return the trimmed string entered when the OK button is hit.
   */
  public static String askIPAddress()
  {
    loadProperties();
    if (prop != null)
      savedIP = prop.getProperty("IP-Address");
    RaspiProperties props = Robot.getProperties();
    if (savedIP == null)
      savedIP = props.getStringValue("IPAddress");
    if (SwingUtilities.isEventDispatchThread())
      value = askInternal(savedIP);
    else
    {
      try
      {
        EventQueue.invokeAndWait(new Runnable()
        {
          public void run()
          {
            value = askInternal(savedIP);
          }
        });
      }
      catch (Exception ex)
      {
      }
      prop.setProperty("IP-Address", value);
      try
      {
        FileOutputStream fos = new FileOutputStream(propFile);
        prop.store(fos, null);
        fos.close();
      }
      catch (IOException ex)
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
        switch (Robot.myClosingMode)
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
    return str.trim();
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

  private static Properties loadProperties()
  {
    // Return null, if error

    propFile = new File(propPath);
    if (!propFile.exists())
    {
      try
      {
        propFile.createNewFile();
      }
      catch (IOException ex)
      {
        return null;
      }
    }
    FileInputStream fis = null;
    try
    {
      fis = new FileInputStream(propFile);
      prop.load(fis);
    }
    catch (IOException ex)
    {
      return null;
    }
    finally
    {
      try
      {
        fis.close();
      }
      catch (Exception ex)
      {
      }
    }
    return prop;
  }
}
