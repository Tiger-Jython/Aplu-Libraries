// ConnectPane.java

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

package ch.aplu.nxt;

import ch.aplu.nxt.platform.*;

/**
 * Class to display a text in a modeless dialog (J2SE) or a form (J2ME).
 */ 
public class ConnectPane 
{
  private ConnectPane()
  {}

  /**
   * Show the dialog (J2SE) or the form (J2ME) and display the given text.
   * @param text the text to display
   */
  public static void showText(String text)
  {
    ConnectPanel.show(text);
  }
}
