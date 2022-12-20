// ExitListener.java

package ch.aplu.packagedoc;

/**
 * The listener interface for processing the GPanel's or Console's close button event.
 * The class that is interested in processing this event must implement this interface
 * (and the method notifyExit()) and register itself by calling addExitListener()
 */
public interface ExitListener extends java.util.EventListener
{
  /**
   * Invoked when the close button in the title bar is pressed.<br>
   * (Normally the program terminates when the close button is hit.)
   */
  public void notifyExit();

}
