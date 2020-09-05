// TurtleFactory.java

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

package ch.aplu.turtle;

import java.awt.image.*;
import java.awt.*;

/**
 * Helper class to generate the standard turtle shape.
 */
public class TurtleFactory
{
  /** 
   * Generates the shape of the turtle with given color,
   * angle (in radian, zero to east, counterclockwise), width and height
   * in pixels.
   */
  public Image turtleImage(Color color, double angle, int width, int height)
  {
    BufferedImage bi = new BufferedImage(width, height, BufferedImage.TYPE_4BYTE_ABGR);
    Graphics2D g = (Graphics2D)bi.getGraphics();
    // Origin in center
    g.translate(width / 2, height / 2);
    // angle = 0 is direction east (as usual in mathematics)
    g.rotate(Math.PI / 2 - angle);
    g.setColor(color);

    // Body
    g.fillOval((int)(-0.35 * width), (int)(-0.35 * height),
      (int)(0.7 * width), (int)(0.7 * height));

    // Head
    g.fillOval((int)(-0.1 * width), (int)(-0.5 * height),
      (int)(0.2 * width), (int)(0.2 * height));

    // Tail
    int[] xcoords =
    {
      (int)(-0.05 * width), 0, (int)(0.05 * width)
    };
    int[] ycoords =
    {
      (int)(0.35 * height), (int)(0.45 * height), (int)(0.35 * height)
    };
    g.fillPolygon(xcoords, ycoords, 3);

    // Feet
    for (int i = 0; i < 4; i++)
    {
      g.rotate(Math.PI / 2);
      g.fillOval((int)(-0.35 * width), (int)(-0.35 * height),
        (int)(0.125 * width), (int)(0.125 * height));
    }
    return (Image)bi;
  }

}
