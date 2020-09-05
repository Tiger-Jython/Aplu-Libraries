// Options.java

/*
 This software is part of the JEX (Java Exemplarisch) Utility Library.
 It is Open Source Free Software, so you may
 - run the code for any purpose
 - study how the code works and adapt it to your needs
 - integrate all or parts of the code in your own programs
 - redistribute copies of the code
 - improve the code and release your improvements to the public
 However the use of the code is entirely your responsibility.
 */
package ch.aplu.util;

import java.awt.*;

/** 
 * Class to select initializing options for the ch.aplu.util package. 
 * Option set here take precedence over values set in the 
 * aplu_util.properties file.
 */
public class GPanelOptions
{
  private static String panelTitle = null;
  private static Dimension panelSize = null;
  private static Point panelPosition = null;
  private static double[] panelRange = null;
  
  private GPanelOptions()
  {
  }

 /**
  * Default title displayed in the frame title bar.
  * Library value: GPanel.
  */
  public static void setPanelTitle(String title)
  {
    panelTitle = title;
  }
  
  /**
   * Default size of graphics area (width, height).
   * Library default: 500 x 500.
   * width and height should be even positive integers.
   * User coordinates (pixels) in range 0..width-1/0..height-1.
  */
  public static void setPanelSize(int width, int height)
  {
    panelSize = new Dimension(width, height);
  }
 
  /**
   * Default position of frame (ulx, uly).
   * Library value: Adapted to number of GPanel instances.
   */
  public static void setPanelPosition(int ulx, int uly)
  {
    panelPosition = new Point(ulx, uly);
  }
  
 /**
   * Default span of coordinates (xmin, xmax, ymin, ymax).
   * Library value: (-1, 1, -1, 1).
   */
  public static void setPanelRange(double xmin, double xmax, double ymin, double ymax)
  {
    panelRange = new double[4];
    panelRange[0] = xmin;
    panelRange[1] = xmax;
    panelRange[2] = ymin;
    panelRange[3] = ymax;
  }

  protected static String getPanelTitle()
  {
    return panelTitle;
  }

  protected static Dimension getPanelSize()
  {
    return panelSize;
  }

  protected static Point getPanelPosition()
  {
    return panelPosition;
  }

  protected static double[] getPanelRange()
  {
    return panelRange;
  }
}
