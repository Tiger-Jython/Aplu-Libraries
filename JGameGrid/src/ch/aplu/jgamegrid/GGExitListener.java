// GGExitListener.java

/*
This software is part of the JGameGrid package.
It is Open Source Free Software, so you may
- run the code for any purpose
- study how the code works and adapt it to your needs
- integrate all or parts of the code in your own programs
- redistribute copies of the code
- improve the code and release your improvements to the public
However the use of the code is entirely your responsibility.

Author: Aegidius Pluess, www.aplu.ch
*/

package ch.aplu.jgamegrid;

/**
 * Declaration of a notification method called when the title bar close button is hit.
 * The following code may be used to show a confirmation dialog before exiting the
 * application:<br>
 * <code><br>
public boolean notifyExit()<br>
{<br>
&nbsp;&nbsp;int rc = JOptionPane.showConfirmDialog(null,<br>
&nbsp;&nbsp;&nbsp;&nbsp;"Are you sure?", "Exit Application", JOptionPane.OK_CANCEL_OPTION);<br>
&nbsp;&nbsp;return (rc == JOptionPane.OK_OPTION);<br>
{<br>
 * </code><br>
 *notifyExit() may also be used for cleanup operations (closing streams,
 * closing connections to external devices, etc.)
 */
public interface GGExitListener extends java.util.EventListener
{
  /**
   * Event callback method called when the title bar close button is hit.
   * @return true, if the application should terminate; false, 
   * if the termination is suspended
   */
  public boolean notifyExit();
}
