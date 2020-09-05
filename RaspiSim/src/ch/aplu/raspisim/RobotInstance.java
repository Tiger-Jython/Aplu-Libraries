// RobotInstance.java

/*
 This software is part of the RaspiSim library.
 It is Open Source Free Software, so you may
 - run the code for any purpose
 - study how the code works and adapt it to your needs
 - integrate all or parts of the code in your own programs
 - redistribute copies of the code
 - improve the code and release your improvements to the public
 However the use of the code is entirely your responsibility.
 */
package ch.aplu.raspisim;

import ch.aplu.jgamegrid.GameGrid;
import java.util.ArrayList;
import javax.swing.JOptionPane;

/**
 Holder of global Robot instance
 */
public class RobotInstance
{
  private static Robot globalRobot = null;
  protected static ArrayList<Part> partsToAdd = new ArrayList<Part>();

  public static void setRobot(Robot robot)
  {
    globalRobot = robot;
  }

  public static Robot getRobot()
  {
    return globalRobot;
  }
  
  public static void checkRobot()
  {
    if (globalRobot == null)
    {
      JOptionPane.showMessageDialog(null,
        "Create Robot instance first",
        "Fatal Error", JOptionPane.ERROR_MESSAGE);
      if (GameGrid.getClosingMode() == GameGrid.ClosingMode.TerminateOnClose
        || GameGrid.getClosingMode() == GameGrid.ClosingMode.AskOnClose)
        System.exit(1);
      if (GameGrid.getClosingMode() == GameGrid.ClosingMode.DisposeOnClose)
        throw new RuntimeException("Create Robot instance first");
    }
  }
}
