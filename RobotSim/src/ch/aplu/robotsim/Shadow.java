// Shadow.java

/*
 This software is part of the RaspiSim library.
 It is Open Source Free Software, so you may
 - run the code for any purpose
 - study how the code works and adapt it to your needs
 - integrate all or parts of the code in your own programs
 - redistribute copies of the code
 - improve the code and release your improvements to the public
 However the use of the code is entirely your responsibility.

 Author: Aegidius Pluess, www.aplu.ch
 */
package ch.aplu.robotsim;

import ch.aplu.jgamegrid.*;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Transparency;
import java.awt.image.BufferedImage;

/**
 * Class to represent a rectangle shadow area where light from torches
 * is absorbed.
 */
public class Shadow extends Actor
{
  private Location center;
  private Location upperLeft;
  private Location lowerRight;

  /**
   * Creates a rectangular shadow area.
   * @param upperLeft the upper left vertex of the area
   * @param lowerRight the lower right vertex of the area
   */
  public Shadow(Location upperLeft, Location lowerRight)
  {
    super(createShadowImage(upperLeft, lowerRight));
    this.upperLeft = upperLeft;
    this.lowerRight = lowerRight;
    center = new Location((lowerRight.x + upperLeft.x) / 2 ,
     (lowerRight.y + upperLeft.y) / 2);
  }

  /**
   * Returns the center of the rectangular area
   * @return the center of the shadow
   */
  public Location getCenter()
  {
    return center;
  }

  /** 
   * Tests if the given target location is in the shadow area.
   * @param target the target location to check
   * @return true, if the target is inside the shadow area
   */
  public boolean inShadow(Location target)
  {
    return (target.x > upperLeft.x && target.x < lowerRight.x)
      && (target.y > upperLeft.y && target.y < lowerRight.y);
  }

  private static BufferedImage createShadowImage(Location upperLeft, Location lowerRight)
  {
    Color c = new Color(150, 150, 150, 100);
    int width = lowerRight.x - upperLeft.x;
    int height = lowerRight.y - upperLeft.y;
    BufferedImage bi = new BufferedImage(width, height, Transparency.BITMASK);
    Graphics2D g2D = bi.createGraphics();

    g2D.setColor(c);
    g2D.fillRect(0, 0, width, height);
    return bi;
  }

}
