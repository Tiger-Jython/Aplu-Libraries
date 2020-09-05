// ShowError.java
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

import javax.swing.JOptionPane;

class ShowError
{
  public ShowError(String msg)
  {
    JOptionPane.showMessageDialog(null, msg);
    if (LegoRobot.myClosingMode == null ||
      LegoRobot.myClosingMode == LegoRobot.ClosingMode.TerminateOnClose)
      System.exit(1);
    if (LegoRobot.myClosingMode == LegoRobot.ClosingMode.DisposeOnClose)
      throw new RuntimeException("Terminated with error");
  }
}
