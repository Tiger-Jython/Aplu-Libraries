// ShowError.java, Java SE version
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

import javax.swing.JOptionPane;
import ch.aplu.nxt.LegoRobot;

public class ShowError
{
  public ShowError(String msg)
  {
    JOptionPane.showMessageDialog(null, msg);
    if (ConnectPanel.myClosingMode == LegoRobot.ClosingMode.TerminateOnClose)
      System.exit(1);
    if (ConnectPanel.myClosingMode == LegoRobot.ClosingMode.DisposeOnClose)
      throw new RuntimeException("Terminated with error");
  }
}
