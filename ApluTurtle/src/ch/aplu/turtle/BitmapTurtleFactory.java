// BitmapTurtleFactory.java

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


import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Transparency;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;

/**
 * Helper class to generate a custom turtle shape from a bitmap.
 */
public class BitmapTurtleFactory extends TurtleFactory
{
  private BufferedImage turtleImg;
  
  public BitmapTurtleFactory(BufferedImage turtleImg)
  {
     this.turtleImg = turtleImg;
  }
 
  /** 
   * Generates the shape of the turtle with given color,
   * angle (in radian, zero to north, counterclockwise), width and height in pixels.
   * @return the turtle image or null, if loading fails
   */
  public Image turtleImage(Color color, double angle, int w, int h)
  {
    if (turtleImg == null)
      return null;
    BufferedImage bix = scale(turtleImg, 1, 90 - Math.toDegrees(angle));
    Graphics2D g = (Graphics2D)bix.getGraphics();
    g.drawImage(bix, 0, 0, null);
    return (Image)bix;
  }
  
  /**
   * Transforms the given buffered image by scaling by the given factor and
   * rotating by the given angle.
   * @param bi the buffered image to transform (unchanged)
   * @param factor the zoom factor (>1 zoom-in, <1 zoom-out)
   * @param angle the rotation angle (in degrees clockwise)
   * @return the transformed image
   */
  private static synchronized BufferedImage scale(BufferedImage bi,
    double factor, double angle)
  {
    if (bi == null)
      return null;
    BufferedImage bix;
    int w = bi.getWidth();
    int h = bi.getHeight();
    if (angle == 0)
    {
      if (factor == 1)
      {
        BufferedImage dest = new BufferedImage(bi.getWidth(), bi.getHeight(),
          Transparency.TRANSLUCENT);
        Graphics2D g2D = dest.createGraphics();
        g2D.drawImage(bi, 0, 0, null);
        g2D.dispose();
        return dest;
      }

      int width = (int)(bi.getWidth() * factor);
      int height = (int)(bi.getHeight() * factor);
      bix = new BufferedImage(width, height, Transparency.TRANSLUCENT);
      Graphics2D g2D = bix.createGraphics();

      AffineTransform at = new AffineTransform();
      at.scale(factor, factor);
      g2D.drawImage(bi, at, null);
      g2D.dispose();
    }
    else
    {
      int s;
      if (factor > 1)
        s = (int)(factor * Math.ceil(Math.sqrt(w * w + h * h)));
      else
        s = (int)Math.ceil(Math.sqrt(w * w + h * h));
      BufferedImage biTmp = new BufferedImage(s, s, Transparency.TRANSLUCENT);
      Graphics2D gTmp = biTmp.createGraphics();
      gTmp.drawImage(bi, (s - w) / 2, (s - h) / 2, null);

      bix = new BufferedImage(s, s, Transparency.TRANSLUCENT);
      Graphics2D g2D = bix.createGraphics();

      g2D.translate(s / 2, s / 2); // Translate the coordinate system (zero a image's center)
      g2D.rotate(Math.toRadians(angle));
      if (factor != 1)
        g2D.scale(factor, factor);
      g2D.translate(-s / 2, -s / 2);
      g2D.drawImage(biTmp, 0, 0, null);
      gTmp.dispose();
      g2D.dispose();
      biTmp.flush();
    }
    return bix;
  }
}
