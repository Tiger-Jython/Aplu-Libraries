// TurtleCrawler.java

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
/**
 * Implementation of the basic Logo turtle movements for a NXT crawler-mounted vehicle.
 */
package ch.aplu.nxt;

import ch.aplu.nxt.platform.*;

public class TurtleCrawler extends TurtleRobot
{
  /**
   * Creates a turtle crawler instance using a NXT brick with given Bluetooth name.
   * A connection trial is engaged while an information box is shown.
   * If the connection fails, the calling thread will be put in a wait state and the program hangs.
   * The user can then terminate the application by closing the information pane.
   * established at this time.<br><br>
   * If showConsole = true, the turtle commands are displayed in a console window.
   * If btName = null, a input dialog is displayed where the Bluetooth name can be entered.
   * @param btName the Bluetooth friendly name, e.g. NXT
   * @param showPane if true, an information pane is showed, where the turtle commands are listed
   */
  public TurtleCrawler(String btName, boolean showPane)
  {
    super(btName, showPane);
    init();
  }

  /**
   * Same as TurtleCrawler(btName, true)
   * @param btName the Bluetooth friendly name, e.g. NXT
   */
  public TurtleCrawler(String btName)
  {
    this(btName, true);
  }

  /**
   * Same as TurtleCrawler(btName), but ask for Bluetooth name
   */
  public TurtleCrawler()
  {
    this(null, true);
  }

  /**
   * Same as TurtleCrawler(btName, showPane), but Bluetooth address given.
   * @param address the 48-bit Bluetooth address as long
   * @param showPane if true, an information pane is showed, where the turtle commands are listed
   */
  public TurtleCrawler(long address, boolean showPane)
  {
    super(address, showPane);
    init();
  }

  /**
   * Same as TurtleCrawler(address, true)
   * @param address the 48-bit Bluetooth address as long
   */
  public TurtleCrawler(long address)
  {
    this(address, true);
  }

  private void init()
  {
    NxtProperties props = LegoRobot.getProperties();
    stepFactor = props.getDoubleValue("CrawlerStepFactor");
    rotationFactor = props.getDoubleValue("CrawlerRotationFactor");
    turtleSpeed = props.getIntValue("CrawlerSpeed");
    rotationSpeed = props.getIntValue("CrawlerRotationSpeed");
  }

}
