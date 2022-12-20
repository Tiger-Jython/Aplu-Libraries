// NxtRobot.java

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

/**
 * Class that represents a NXT robot brick. Parts (e.g. motors, sensors) may
 * be assembled into the robot to make it doing the desired job. <br><br>
 *
 * More than one instance may be created. They are identified by different
 * Bluetooth names (or addresses).<br><br>
 *
 * The Bluetooth communication uses the Bluecove library and is fully standalone
 * and portable. No serial port emulation (RFCOMM) and no Bluetooth Control Center (BCC)
 * is used. The Bluecove library supports only interfaces with Widcomm compatibility.
 * If more than one brick is used at the same time, the Bluetooth interface
 * must be capable to establish more than one Bluetooth link (e.g. Anycom USB adapter,
 * Part.No: CC-3036).<br><br>
 *
 * NxtJlib implements the usual Java event handling model using event listeners
 * and adapters. The sensors are polled at a regular interval from a internal
 * thread and the registered callback method is invoked, when the event condition
 * is fullfilled (e.g. sensor value crosses a given trigger level).<br><br>
 *
 * Many library options are defined in a property file 'nxtjlib.properties' that
 * may be modified as needed. The property file is searched in the following order:<br>
 * - Application directory (user.dir)<br>
 * - Home directory (user.home)<br>
 * - NxtJLib.jar (distribution)<br><br>
 *
 * As soon as the property file is found, the search is cancelled. This allows
 * to use a personalized property file without deleting or modifing the distributed
 * file in NxtJLib.jar. Consult the distributed file for more information.
 * Be careful to keep the original formatting.<br><br>
 *
 * If the property KeepAlive is set to 1, a thread is started that sends keep
 * alive commands every 500 s in order to prevent the automatic shutdown of the NXT.<br><br>
 * 
 * The library is carefully tested with the leJOS firmware. The Lego firmware
 * is almost 100% compatible, but some special operations may fail (e.g. 
 * the motor count)<br><br>
 * 
 * For backward compatibility with pre-EV3 programs.
 */
public class NxtRobot extends LegoRobot
{
  /**
   * Creates a NxtRobot instance with given Bluetooth name.
   * If connect is true, a connection trial is engaged while an information pane is shown.
   * If the connection fails, the calling thread will be put in a wait state and the program hangs.
   * The user can then terminate the application by closing the information pane.
   * established at this time.<br><br>
   * If connect = false, no connection trial is engaged at this time and connect() should be used.
   * If any method that needs the connection is invoked before connect() is called, the
   * connection is automatically established.<br><br>
   * If btName = null, a input dialog is displayed where the Bluetooth name can be entered.<br><br>
   * No connection pane is shown, if connect = false and connect(false) is called.
   * @param btName the Bluetooth friendly name of the brick, e.g. "NXT"
   * @param connect if true, a connection trial is engaged
   */
  public NxtRobot(String btName, boolean connect)
  {
    super(btName, connect);
  }

  /**
   * Creates a NxtRobot instance with given Bluetooth name.
   * A connection trial is engaged while an information pane is shown.
   * If the connection fails, the calling thread will be put in a wait state and the program hangs.
   * The user can then terminate the application by closing the information pane.<br><br>
   * If btName = null, a input dialog is displayed where the Bluetooth name can be entered.
   * @param btName the Bluetooth friendly name of the brick, e.g. "NXT"
   */
  public NxtRobot(String btName)
  {
    super(btName);
  }

  /**
   * Creates a NxtRobot instance with given Bluetooth address.
   * If connect is true, a connection trial is engaged while an information pane is shown.
   * If the connection fails, the calling thread will be put in a wait state and the program hangs.
   * The user can then terminate the application by closing the information pane.
   * If connect = false, no connection trial is engaged at this time and connect() should be used.
   * If any method that needs the connection is invoked before connect() is called, the
   * connection is automatically established.<br><br>
   * No connection pane is shown, if connect = false and connect(false) is called.
   * @param btAddress the Bluetooth address of the brick, e.g. 0x0000A762B001
   * @param connect if true, a connection trial is engaged
   */
  public NxtRobot(long btAddress, boolean connect)
  {
    super(btAddress, connect);
  }

  /**
   * Creates a NxtRobot instance with given Bluetooth address.
   * A connection trial is engaged while an information pane is shown.
   * If the connection fails, the calling thread will be put in a wait state and the program hangs.
   * The user can then terminate the application by closing the information pane.
   * @param btAddress the Bluetooth address of the brick, e.g. 0x0000A762B001
   */
  public NxtRobot(long btAddress)
  {
    super(btAddress);
  }

  /**
   * Asks for the Bluetooth name and creates a NxtRobot instance.
   * The initial value for the name is read from the nxtjlib.properties file.
   * In J2ME the modified name is stored in the RMS and reread at the next program execution.
   * A connection trial is engaged while an information pane is shown.
   * If the connection fails, the calling thread will be put in a wait state and the program hangs.
   * The user can then terminate the application by closing the information pane.
   */
  public NxtRobot()
  {
    super();
  }
}
