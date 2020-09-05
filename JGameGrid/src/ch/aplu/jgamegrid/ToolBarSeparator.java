// ToolBarSeparator.java

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

import java.awt.*;
import java.awt.image.BufferedImage;

/**
 * A class to represent actors used for tool bar items.
 * All sprites images in a tool bar should have the same height.
 */
public class ToolBarSeparator extends ToolBarItem
{
  /**
   * Creates a tool bar item whose image is a rectangle with
   * given width and height filled with given color. If color = null,
   * the rectangle is transparent.
   * @param width the width in pixel
   * @param height the height in pixel
   * @param color the fill color
   */
  public ToolBarSeparator(int width, int height, Color color)
  {
    super(getImage(width, height, color));
  }

  private static BufferedImage getImage(int width, int height, Color color)
  {
    BufferedImage bi = new BufferedImage(width, height, Transparency.TRANSLUCENT);
    Graphics2D g2D = bi.createGraphics();

    if (color == null)
      g2D.setColor(new Color(255, 255, 255, 0));
    else
      g2D.setColor(color);

    g2D.fillRect(0, 0, width, height);
    g2D.drawImage(bi, 0, 0, null);
    g2D.dispose();
    return bi;
  }
}
