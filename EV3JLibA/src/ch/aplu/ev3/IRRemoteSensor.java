// IRRemoteSensor.java

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

/**
 * Class that represents a Lego EV3 Infra Red sensor in Remote Control Mode.
 * A remote control box is needed.
 */
public class IRRemoteSensor extends GenericIRSensor
{
  // -------------- Inner class RemoteSensorThread ------------
  private class RemoteSensorThread extends Thread
  {
    private volatile boolean isRunning = false;
    private int channel;
    private int commandID = 0;

    private RemoteSensorThread()
    {
      if (LegoRobot.getDebugLevel() >= SharedConstants.DEBUG_LEVEL_LOW)
        DebugConsole.show("RsTh(" + channel + ") created");
    }

    public void run()
    {
      int remotePollDelay = 200;
      if (LegoRobot.getDebugLevel() >= SharedConstants.DEBUG_LEVEL_LOW)
        DebugConsole.show("RsTh started");

      isRunning = true;
      while (isRunning)
      {
        Tools.delay(remotePollDelay);
        int cmd = readCommand();
        if (commandID != cmd)
        {
          commandID = cmd;
          if (remoteListener != null)
            remoteListener.actionPerformed(getPort(), commandID);
        }
      }
    }

    private void stopThread()
    {
      if (!isRunning)
        return;
      isRunning = false;
      try
      {
        join(500);
      }
      catch (InterruptedException ex)
      {
      }
      if (LegoRobot.getDebugLevel() >= SharedConstants.DEBUG_LEVEL_LOW)
        if (isAlive())
          DebugConsole.show("RsTh stop failed");
        else
          DebugConsole.show("RsTh stop ok");
    }
  }
  // -------------- End of inner classes -----------------------

  private RemoteListener remoteListener = null;
  private RemoteSensorThread rst;
  private int channel;

  /**
   * Creates a sensor instance connected to the given port.
   * @param port the port where the sensor is plugged-in
   */
  public IRRemoteSensor(SensorPort port)
  {
    super(port, SensorMode.SEEK_MODE);
    channel = port.getId() + 1;
    rst = new RemoteSensorThread();
  }

  /**
   * Creates a sensor instance connected to port S1.
   */
  public IRRemoteSensor()
  {
    this(SensorPort.S1);
  }

  protected void cleanup()
  {
    if (LegoRobot.getDebugLevel() >= SharedConstants.DEBUG_LEVEL_MEDIUM)
      DebugConsole.show("irremote.cleanup()");
    rst.stopThread();
    super.cleanup();
  }

  /**
   * Registers the given infrared remote listener.
   * The channel corresponds to the port where the sensor is plugged-in:
   * Port S1:channel 1...Port S4:channel 4.
   * If the command listener thread is not yet started, start it now.
   * @param listener the RemoteListener to register
   */
  public void addRemoteListener(RemoteListener listener)
  {
    remoteListener = listener;
    if (!rst.isAlive())
      rst.start();
  }

  /**
   * Returns the command code corresponding to the currently 
   * pressed buttons on the the Remote IR Command.
   * The values are:<br>
   * 0: Nothing<br>
   * 1: TopLeft<br>
   * 2: BottomLeft<br>
   * 3: TopRight<br>
   * 4: BottomRight<br>
   * 5: TopLeft&TopRight<br>
   * 6: TopLeft&BottomRight<br>
   * 7: BottomLeft&TopRight<br>
   * 8: BottomLeft&BottomRight<br>
   * 9: Centre<br>
   * 10: BottomLeft&TopLeft<br>
   * 11: TopRight&BottomRight<br><br>
   * 
   * Centre is a toggle button with a led indicator. If pressed, 
   * it remains in the on or off state until pressed again or until another
   * button is pressed. If turned on, it may serve as a continuously emitting 
   * infrared beacon.
   * @return the command code as defined above
   */
  public int getCommand()
  {
    checkConnect();
    return irSensor.getRemoteCommand(channel - 1);
  }

  private int readCommand()
  {
    if (robot == null)
      return 0;
    return irSensor.getRemoteCommand(channel - 1);
  }

  /**
   * Converts integer command to string representation.
   * @param cmd the integer command
   * @return the string representation as defined in getCommand()
   */
  public static String toString(int cmd)
  {
    switch (cmd)
    {
      case 0:
        return "Nothing";
      case 1:
        return "TopLeft";
      case 2:
        return "BottomLeft";
      case 3:
        return "TopRight";
      case 4:
        return "BottomRight";
      case 5:
        return "TopLeft&TopRight";
      case 6:
        return "TopLeft&BottomRight";
      case 7:
        return "BottomLeft&TopRight";
      case 8:
        return "BottomLeft&BottomRight";
      case 9:
        return "Centre";
      case 10:
        return "BottomLeft&TopLeft";
      case 11:
        return "TopRight&BottomRight";
      default:
        return "Illegal";
    }
  }

  /**
   * Returns the string represention of the command code corresponding 
   * to the currently pressed buttons on the the Remote IR Command.
   * @return the string representation of the command
   */
  public String getCommandStr()
  {
    return toString(getCommand());
  }

  private void checkConnect()
  {
    if (robot == null)
      new ShowError("IRRemoteSensor (port: " + getPortLabel()
        + ") is not a part of the LegoRobot.\n"
        + "Call addPart() to assemble it.");
  }

}
