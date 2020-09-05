// GBitmap.java

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
import java.awt.Point;
import java.awt.Transparency;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.IndexColorModel;
import java.awt.image.WritableRaster;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Set;
import javax.imageio.ImageIO;

/**
 * GBitmap is a helper class derived from BufferedImage with
 * some common bitmap transformation methods.
 */
public class GBitmap extends BufferedImage
{
  /**
   * Constructs a BufferedImage with a specified ColorModel and Raster.
   */
  public GBitmap(ColorModel cm, WritableRaster raster,
    boolean isRasterPremultiplied, Hashtable properties)
  {
    super(cm, raster, isRasterPremultiplied, properties);
  }

  /**
   * Constructs a BufferedImage of one of the predefined image types.
   */
  public GBitmap(int width, int height, int imageType)
  {
    super(width, height, imageType);
  }
  
  /**
   * Constructs a BufferedImage of type BufferedImage.TYPE_INT_ARGB.
   */
  public GBitmap(int width, int height)
  {
    super(width, height, BufferedImage.TYPE_INT_ARGB);
  }

  /**
   * Constructs a BufferedImage of one of the predefined image types: TYPE_BYTE_BINARY or TYPE_BYTE_INDEXED.
   */
  public GBitmap(int width, int height, int imageType, IndexColorModel cm)
  {
    super(width, height, imageType, cm);
  }

  /**
   * Returns the color of the pixel at given x,y coordinates.
   */
  public Color getPixelColor(int x, int y)
  {
    return new Color(getRGB(x, y), true);  // Maintain alpha
  }

  /**
   * Returns the color of the pixel at given point 
   * (defined as int array for Jython compatibility).
   */
  public Color getPixelColor(int[] pt)
  {
    return new Color(getRGB(pt[0], pt[1]));
  }

  /**
   * Returns the X11 color name of the pixel at given x,y coordinates.
   */
  public String getPixelColorStr(int x, int y)
  {
    return X11Color.toColorStr(new Color(getRGB(x, y), true));  // Maintain alpha
  }

  /**
   * Returns the X11 color name of the pixel at given point 
   * (defined as int array for Jython compatibility).
   */
  public String getPixelColorStr(int[] pt)
  {
    return X11Color.toColorStr(new Color(getRGB(pt[0], pt[1]), true));
  }

  /**
   * Modifies the color of the pixel at given x, y coordinates.
   */
  public void setPixelColor(int x, int y, Color color)
  {
    setRGB(x, y, color.getRGB());
  }

  /**
   * Modifies the color of the pixel at given point
   * (defined as int array for Jython compatibility).
   */
  public void setPixelColor(int[] pt, Color color)
  {
    setRGB(pt[0], pt[1], color.getRGB());
  }

  /**
   * Modifies the color (defined as X11 color name)
   * of the pixel at given x, y coordinates.
   */
  public void setPixelColorStr(int x, int y, String colorStr)
  {
    setRGB(x, y, X11Color.toColor(colorStr).getRGB());
  }

  /**
   * Modifies the color (defined as X11 color name)
   * of the pixel at given point
   * (defined as int array for Jython compatibility).
   */
  public void setPixelColorStr(int[] pt, String colorStr)
  {
    setRGB(pt[0], pt[1], X11Color.toColor(colorStr).getRGB());
  }

  // ********************** static methods ****************************
  /**
   * Writes a image file of the given BufferedImage. Normally the following
   * image formats are supported: bmp, gif, jpg, png,
   * but call getSupportedImageFormats() to find all formats supported by 
   * the current platform.
   * @param bi the given BufferedImage to copy
   * @param filename the path to the image file
   * @param type the image file format like "bmp", "gif", "jpg", "png" (all lowercase)
   * @return true, if the format is supported and the file was successfully written; 
   * otherwise false
   */
  public static boolean save(BufferedImage bi, String filename, String type)
  {
    boolean ok = false;
    for (String s : getSupportedImageFormats())
    {
      if (s.equals(type))
      {
        ok = true;
        break;
      }
    }

    if (!ok)
      return false;

    // bi is of type ARGB, must transform it to INT_RGB
    BufferedImage dest = new BufferedImage(bi.getWidth(), bi.getHeight(),
      BufferedImage.TYPE_INT_RGB);

    Graphics2D g2D = dest.createGraphics();
    g2D.drawImage(bi, 0, 0, null);
    g2D.dispose();

    try
    {
      ImageIO.write(dest, type, new File(filename));
    }
    catch (IOException ex)
    {
      return false;
    }
    return true;
  }

  /**
   * Fills a bounded single-colored region with 
   * the given color. The given point is part of the region and used 
   * to specify it.
   * @param bi the BufferedImage containing the connected region
   * @param pt a point inside the region
   * @param oldColor the old color of the region
   * @param newColor the new color of the region
   * @return a new BufferedImage with the transformed region, the given BufferedImage
   * remains unchanged
   */
  public static GBitmap floodFill(BufferedImage bi, Point pt, Color oldColor,
    Color newColor)
  {
    // Implementation from Hardik Gajjar of algorithm 
    // at http://en.wikipedia.org/wiki/Flood_fill

    GBitmap image = new GBitmap(bi.getWidth(), bi.getHeight(),
      Transparency.TRANSLUCENT);
    Graphics2D g2D = image.createGraphics();
    g2D.drawImage(bi, 0, 0, null);

    int oldRGB = oldColor.getRGB();
    int newRGB = newColor.getRGB();

    // Perform filling operation
    Queue<Point> q = new LinkedList<Point>();
    q.add(pt);
    while (q.size() > 0)
    {
      Point n = q.poll();
      if (image.getRGB(n.x, n.y) != oldRGB)
        continue;

      Point w = n, e = new Point(n.x + 1, n.y);
      while ((w.x > 0) && (image.getRGB(w.x, w.y) == oldRGB))
      {
        image.setRGB(w.x, w.y, newRGB);
        if ((w.y > 0) && (image.getRGB(w.x, w.y - 1) == oldRGB))
          q.add(new Point(w.x, w.y - 1));
        if ((w.y < image.getHeight() - 1)
          && (image.getRGB(w.x, w.y + 1) == oldRGB))
          q.add(new Point(w.x, w.y + 1));
        w.x--;
      }
      while ((e.x < image.getWidth() - 1)
        && (image.getRGB(e.x, e.y) == oldRGB))
      {
        image.setRGB(e.x, e.y, newRGB);

        if ((e.y > 0) && (image.getRGB(e.x, e.y - 1) == oldRGB))
          q.add(new Point(e.x, e.y - 1));
        if ((e.y < image.getHeight() - 1)
          && (image.getRGB(e.x, e.y + 1) == oldRGB))
          q.add(new Point(e.x, e.y + 1));
        e.x++;
      }
    }
    return image;
  }

  /**
   * Returns a clone of the given image where each pixel has a new transparency value
   * (alpha component in the ARGB color model). The new transparency value is
   * the old value multiplied by the transparency multiplier (limited to 0..255).
   * @param bi the original image
   * @param factor the transparency multiplier 
   * @return the transformed image (the given image is unchanged)
   */
  public static GBitmap setTransparency(BufferedImage bi, double factor)
  {
    GBitmap image = new GBitmap(bi.getWidth(), bi.getHeight(),
      Transparency.TRANSLUCENT);
    Graphics2D g2D = image.createGraphics();
    g2D.drawImage(bi, 0, 0, null);
    for (int x = 0; x < bi.getWidth(); x++)
    {
      for (int y = 0; y < bi.getHeight(); y++)
      {
        int c = image.getRGB(x, y);
        int alpha = c >>> 24;
        int newAlpha = (int)(factor * alpha);
        newAlpha = Math.max(0, Math.min(255, newAlpha));
        newAlpha = newAlpha << 24;
        c = c & 0x00FFFFFF;
        c = c | newAlpha;
        image.setRGB(x, y, c);
      }
    }
    return image;
  }

  /**
   * Extracts the partial image if the given rectangular area. 
   * Extracted pixels outside the given image are set to
   * white.
   * @param bi the image where to extract the partial image
   * @param x1 the x-coordinate of one of the rectangle vertex 
   * @param y1 the y-coordinate of this vertex 
   * @param x2 the x-coordinate of the opposite vertex 
   * @param y2 the y-coordinate of this vertex
   * @return the extracted image (the given image is unchanged)
   */
  public static GBitmap crop(BufferedImage bi,
    int x1, int y1, int x2, int y2)
  {
//    System.out.println("getExtractedImage(" + x1 + "," + y1 + "), (" + x2 + "," + y2 + ")");

    if (x2 < x1)
    {
      int a = x2;
      x2 = x1;
      x1 = a;
    }
    if (y2 < y1)
    {
      int a = y2;
      y2 = y1;
      y1 = a;
    }
    int width = x2 - x1;
    int height = y2 - y1;
    int ulx = x1;
    int uly = y1;

    GBitmap image = new GBitmap(width, height, BufferedImage.TYPE_INT_ARGB);
    for (int x = 0; x < width; x++)
    {
      for (int y = 0; y < height; y++)
      {
        int xPix = ulx + x;
        int yPix = uly + y;
        if (xPix < bi.getWidth() && yPix < bi.getHeight())
          image.setRGB(x, y, bi.getRGB(xPix, yPix));
        else
          image.setRGB(x, y, Color.white.getRGB());
      }
    }
    return image;
  }

  /**
   * Returns a clone of the original image where a part is replaced by
   * another image.
   * @param original the original image
   * @param replacement the image to insert
   * @param xStart the upper left x-coordinate where the replacement starts
   * @param yStart the upper left y-coordinate where the replacement starts
   * @return the modified image (the original image is unchanged)
   */
  public static GBitmap paste(BufferedImage original,
    BufferedImage replacement, int xStart, int yStart)
  {
    int w1 = original.getWidth();
    int h1 = original.getHeight();
    int w2 = replacement.getWidth();
    int h2 = replacement.getHeight();
    //   System.out.println("(xStart, yStart) =  (" + xStart + "," + yStart + ")");
    //   System.out.println("org (width, height) =  (" + w1 + "," + h1 + ")");
    //   System.out.println("repl (width, height) =  (" + w2 + "," + h2 + ")");
    GBitmap image = new GBitmap(w1, h1, BufferedImage.TYPE_INT_ARGB);
    for (int x = 0; x < w1; x++)
    {
      for (int y = 0; y < h1; y++)
      {
        if (x >= xStart && y >= yStart && x - xStart < w2 && y - yStart < h2)
          image.setRGB(x, y, replacement.getRGB(x - xStart, y - yStart));
        else
          image.setRGB(x, y, original.getRGB(x, y));
      }
    }
    return image;
  }

  /**
   * Transforms the given buffered image by scaling by the given factor and
   * rotating by the given angle.
   * @param bi the buffered image to transform (unchanged)
   * @param factor the zoom factor (>1 zoom-in, <1 zoom-out)
   * @param angle the rotation angle (in degrees clockwise)
   * @return the transformed image
   */
  public static synchronized GBitmap scale(BufferedImage bi,
    double factor, double angle)
  {
    if (bi == null)
      return null;
    GBitmap bix;
    int w = bi.getWidth();
    int h = bi.getHeight();
    if (angle == 0)
    {
      if (factor == 1)
      {
        GBitmap dest = new GBitmap(bi.getWidth(), bi.getHeight(),
          Transparency.TRANSLUCENT);
        Graphics2D g2D = dest.createGraphics();
        g2D.drawImage(bi, 0, 0, null);
        g2D.dispose();
        return dest;
      }

      int width = (int)(bi.getWidth() * factor);
      int height = (int)(bi.getHeight() * factor);
      bix = new GBitmap(width, height, Transparency.TRANSLUCENT);
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

      bix = new GBitmap(s, s, Transparency.TRANSLUCENT);
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

  /**
   * Returns all supported image formats.
   * @return a string array containing all image 
   * format descriptions of the current platform.
   */
  public static String[] getSupportedImageFormats()
  {
    return unique(ImageIO.getWriterFormatNames());
  }

  private static String[] unique(String[] strings)
  // Converts all strings in 'strings' to lowercase
  // and returns an array containing the unique values.
  {
    Set set = new HashSet();
    for (int i = 0; i < strings.length; i++)
    {
      String name = strings[i].toLowerCase();
      set.add(name);
    }
    return (String[])set.toArray(new String[0]);
  }

  /**
   * Returns the image in a byte array. imageFormat is the informal name 
   * of the format (one of the strings returned by get SupportedImageFormats()).
   * @return the image data as byte array, null if the image format is not
   * supported
   */
  public static byte[] getByteArray(BufferedImage sourceImage, String imageFormat)
  {
    try
    {
      ByteArrayOutputStream baos = new ByteArrayOutputStream();
      ImageIO.write(sourceImage, imageFormat, baos);
      baos.flush();
      byte[] imageData = baos.toByteArray();
      baos.close();
      return imageData;
    }
    catch (IOException ex)
    {
      return null;
    }
  }
}
