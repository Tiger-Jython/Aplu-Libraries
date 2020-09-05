// GGBitmap.java

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
import java.awt.font.FontRenderContext;
import java.awt.font.TextLayout;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import javax.imageio.ImageIO;
import java.awt.geom.*;
import java.io.ByteArrayOutputStream;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Set;

/**
 * Class to provide drawing methods for creating a bitmap image and 
 * some static helper methods for image loading and
 * transformation.
 */
public class GGBitmap
{
  private BufferedImage bi;
  private Graphics2D g2D;
  private int width;
  private int height;
  private int lineWidth = 1;
  private Color paintColor = Color.white;
  private Color bgColor;
  private Font font = new Font("SansSerif", Font.PLAIN, 24);

  /**
   * Creates a GGBitmap with given number of horizontal and vertical pixels
   * that holds a Bitmap instance to draw graphics elements.
   * The coordinates for the drawing methods are 0..width-1, 0..height-1 with zero
   * at upper left corner.<br>
   * Defaults:<br>
   * - paint color: white<br>
   * - line width: 1 pixel<br>
   * - background color: white fully-transparent, ARGB = (255,255,255,0)<br>
   * - font: SansSerif, Font.PLAIN, 24 pixel<br><br>
   * @param width the width of the Bitmap in pixels
   * @param height the height of the Bitmap in pixels
   */
  public GGBitmap(int width, int height)
  {
    this(width, height, new Color(255, 255, 255, 0));
  }

  /**
   * Creates a GGBitmap with given number of horizontal and vertical pixels
   * that holds a Bitmap instance to draw graphics elements.
   * The coordinates for the drawing methods are 0..width-1, 0..height-1 with zero
   * at upper left corner.
   * Defaults:<br>
   * - paint color: white<br>
   * - line width: 1 pixel<br>
   * - font: SansSerif, Font.PLAIN, 24 pixel<br><br>
   * @param width the width of the Bitmap in pixels
   * @param height the height of the Bitmap in pixels
   * @param bgColor the color of the background, may be (semi-)transparent
   */
  public GGBitmap(int width, int height, Color bgColor)
  {
    this.bgColor = bgColor;
    bi = new BufferedImage(width, height, Transparency.TRANSLUCENT);
    g2D = bi.createGraphics();
    g2D.setColor(bgColor);
    g2D.fillRect(0, 0, width, height);
    g2D.setColor(paintColor);
  }

  /**
   * Retrieves the image either from the jar resource, from local drive or
   * from a internet server and transforms it by scaling it by the given
   * factor and rotating it by the given angle.
   * From the given filename the image file is searched in the following order:<br>
   * - if application is packed into a jar archive, relative to the root of the jar archive<br>
   * - relative to the directory &lt;userhome&gt;/gamegrid/<br>
   * - relative or absolute to current application directory<br>
   * If filename starts with http://, the image is loaded from the given URL
   * @param imagePath the file name or url
   * @param factor the zoom factor (>1 zoom-in, <1 zoom-out)
   * @param angle the rotation angle (in degrees clockwise)
   * @return the transformed image or null, if the image search fails 
   */
  public static synchronized BufferedImage getScaledImage(String imagePath,
    double factor, double angle)
  {
    return getScaledImage(getImage(imagePath), factor, angle);
  }

  /**
   * Transforms the given buffered image by scaling by the given factor and
   * rotating by the given angle.
   * @param bi the buffered image to transform
   * @param factor the zoom factor (>1 zoom-in, <1 zoom-out)
   * @param angle the rotation angle (in degrees clockwise)
   * @return the transformed image
   */
  public static synchronized BufferedImage getScaledImage(BufferedImage bi,
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
        return bi;
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

  /**
   * Retrieves the image either from the jar resource, from local drive or
   * from a internet server
   * From the given filename the image file is searched in the following order:<br>
   * - if application is packed into a jar archive, relative to the root of the jar archive<br>
   * - relative to the directory &lt;userhome&gt;/gamegrid/<br>
   * - relative or absolute to current application directory<br>
   * - if filename starts with http://, from the given URL<br>
   * - add prefix _ and search relative to the root of the jar archive
   * @param imagePath the file name or url
   * @return the buffered image or null, if the image search fails
   */
  public static synchronized BufferedImage getImage(String imagePath)
  {
//    System.out.println("getImage() loading " + imagePath);
    BufferedImage image = null;

//    System.out.println("loading from JAR");
    // First try to load from jar resource
    // URL url = ClassLoader.getSystemResource(imagePath);  // Does not work with Webstart
    URL url = Thread.currentThread().getContextClassLoader().
      getResource(imagePath);

    if (url != null)  // Image found in jar
    {
      try
      {
        image = ImageIO.read(url);
      }
      catch (IOException e)
      {
        // Read error
      }
    }

    // Failed, search in userhome\gamegrid
    if (image == null)
    {
//      System.out.println("loading from userhome/gamegrid");
      String userHome =
        (System.getProperty("user.home").toLowerCase().contains("%userprofile%"))
        ? ((System.getenv("USERPROFILE") == null) ? System.getProperty("java.io.tmpdir")
        : System.getenv("USERPROFILE")) : System.getProperty("user.home");
      String FS = System.getProperty("file.separator");
      String path = userHome + FS + "gamegrid" + FS + imagePath;
      try
      {
        image = ImageIO.read(new File(path));
      }
      catch (IOException e)
      {
      }

      if (image == null) // Failed, search in given path
      {
//        System.out.println("loading from given path: " + imagePath);
//        System.out.println("user.dir: " + System.getProperty("user.dir"));
        try
        {
          String abs = new File(imagePath).getAbsolutePath();
          image = ImageIO.read(new File(abs));
        }
        catch (IOException e)
        {
        }
      }
    }

    // Failed, try to load from server
    if (image == null && imagePath.indexOf("http://") != -1)
    {
//      System.out.println("loading from http:");
      try
      {
        image = ImageIO.read(new URL(imagePath));
      }
      catch (IOException e)
      {
        // Read error
      }
    }

    if (image == null)
    {
//      System.out.println("loading JAR prefixed with _");
      // Last: Try to load from _ prefixed subdirectory of jar resource
      url = Thread.currentThread().getContextClassLoader().
        getResource("_" + imagePath);
      if (url != null)  // Image found in jar
      {
        try
        {
          image = ImageIO.read(url);
        }
        catch (IOException e)
        {
          // Read error
        }
      }
    }
/*    
    if (image != null)
      System.out.println("image successfully loaded");
    else
      System.out.println("image failed to load");
      */ 
    return image;
  }

  /**
   * Draws the given image into the bitmap.
   * @param x x-coordinate of upper left corner
   * @param y y-coordinate of upper left corner
   */
  public void drawImage(BufferedImage bi, int x, int y)
  {
    if (bi == null)
      return;
    g2D.drawImage(bi, x, y, null);
  }

  /**
   * Draws the given image at position (0, 0) into the bitmap.
   */
  public void drawImage(BufferedImage bi)
  {
    drawImage(bi, 0, 0);
  }

  /**
   * Clears the bitmap by painting it with the given background color.
   * @param color the color of the background
   */
  public void clear(Color color)
  {
    g2D.setColor(color);
    g2D.fillRect(0, 0, width, height);
    g2D.setColor(paintColor);
  }

  /**
   * Clears the bitmap by painting it with the current background color.
   */
  public void clear()
  {
    clear(bgColor);
  }

  /**
   * Returns the current line width in pixels.
   * @return the line width
   */
  public int getLineWidth()
  {
    return lineWidth;
  }

  /**
   * Sets the current line width in pixels.
   * @param width the new line width
   */
  public void setLineWidth(int width)
  {
    BasicStroke stroke = new BasicStroke(width);
    g2D.setStroke(stroke);
    lineWidth = width;
  }

  /**
   * Returns the current paint color.
   * @return the paint color
   */
  public Color getPaintColor()
  {
    return paintColor;
  }

  /**
   * Sets the given new paint color (for drawing and filling).
   * @param color the new line color
   */
  public void setPaintColor(Color color)
  {
    g2D.setPaint(color);
    paintColor = color;
  }

  /**
   * Returns the current background color.
   * @return the background color
   */
  public Color getBgColor()
  {
    return bgColor;
  }

  /**
   * Draws a line from one coordinate pair to another coordinate pair.
   * The line width and color is determined by setLineWidth() and setPaintColor().
   * @param x1 the x-coordinate of the start point
   * @param y1 the y-coordinate of the start point
   * @param x2 the x-coordinate of the endpoint
   * @param y2 the y-coordinate of the endpoint
   */
  public void drawLine(int x1, int y1, int x2, int y2)
  {
    g2D.drawLine(x1, y1, x2, y2);
  }

  /**
   * Draws a line from one coordinate pair to another coordinate pair.
   * The line width and color is determined by setLineWidth() and setPaintColor().
   * @param pt1 the start point
   * @param pt2 the endpoint
   */
  public void drawLine(Point pt1, Point pt2)
  {
    drawLine(pt1.x, pt1.y, pt2.x, pt2.y);
  }

  private void drawCircle(int xCenter, int yCenter, int radius, boolean fill)
  {
    int ulx = xCenter - radius;
    int uly = yCenter - radius;

    Ellipse2D.Double ellipse = new Ellipse2D.Double(ulx, uly, 2 * radius, 2 * radius);
    if (fill)
    {
      g2D.fill(ellipse);
      g2D.draw(ellipse);
    }
    else
      g2D.draw(ellipse);
  }

  /**
   * Draws a circle with given center and given radius.
   * @param radius the radius of the circle
   */
  public void drawCircle(Point center, int radius)
  {
    drawCircle(center.x, center.y, radius, false);
  }

  /**
   * Draws a filled circle with given center and given radius.
   * @param center the center of the circle
   * @param radius the radius of the circle
   */
  public void fillCircle(Point center, int radius)
  {
    drawCircle(center.x, center.y, radius, true);
  }

  private void _drawRectangle(int x1, int y1, int x2, int y2, boolean fill)
  {
    int minX = Math.min(x1, x2);
    int maxX = Math.max(x1, x2);
    int minY = Math.min(y1, y2);
    int maxY = Math.max(y1, y2);
    Rectangle.Double rectangle =
      new Rectangle.Double(minX, minY, maxX - minX, maxY - minY);
    if (fill)
    {
      g2D.fill(rectangle);
      g2D.draw(rectangle);  // Draw outline
    }
    else
      g2D.draw(rectangle);
  }

  /**
   * Draws a rectangle with given opposite corners.
   * @param pt1 upper left vertex of the rectangle
   * @param pt2 lower right vertex of the rectangle
   */
  public void drawRectangle(Point pt1, Point pt2)
  {
    _drawRectangle(pt1.x, pt1.y, pt2.x, pt2.y, false);
  }

  /**
   * Draws a filled rectangle with given opposite corners.
   * (The filling includes the complete outline of the rectangle.)
   * @param pt1 upper left vertex of the rectangle
   * @param pt2 lower right vertex of the rectangle
   */
  public void fillRectangle(Point pt1, Point pt2)
  {
    _drawRectangle(pt1.x, pt1.y, pt2.x, pt2.y, true);
  }

  private void drawArc(int xCenter, int yCenter, int radius, double startAngle, double extendAngle,
    boolean fill)
  {
    int ulx = xCenter - radius;
    int uly = yCenter - radius;

    Arc2D.Double arc =
      new Arc2D.Double(ulx, uly, 2 * radius, 2 * radius, startAngle, extendAngle,
      Arc2D.OPEN);
    if (fill)
    {
      g2D.fill(arc);
      g2D.draw(arc);  // Draw outline
    }
    else
      g2D.draw(arc);
  }

  /**
   * Draws an arc with given center, radius, start and end angle.
   * @param pt the center of the arc
   * @param radius the radius of the arc
   * @param startAngle the start angle in degrees (zero to east, positive counterclockwise)
   * @param extendAngle the extend angle in degrees (zero to east, positive counterclockwise)
   */
  public void drawArc(Point pt, int radius, double startAngle, double extendAngle)
  {
    drawArc(pt.x, pt.y, radius, startAngle, extendAngle, false);
  }

  /**
   * Fills an arc with given center, radius, start and end angle.
   * @param pt the center of the arc
   * @param radius the radius of the arc
   * @param startAngle the start angle in degrees (zero to east, positive counterclockwise)
   * @param extendAngle the extendAngle in degrees (zero to east, positive counterclockwise)
   */
  public void fillArc(Point pt, int radius, double startAngle, double extendAngle)
  {
    drawArc(pt.x, pt.y, radius, startAngle, extendAngle, true);
  }

  private void drawPolygon(Point[] vertexes, boolean fill)
  {
    int size = vertexes.length;
    int[] x = new int[size];
    int[] y = new int[size];
    for (int i = 0; i < size; i++)
    {
      x[i] = vertexes[i].x;
      y[i] = vertexes[i].y;
    }

    Polygon polygon = new Polygon(x, y, size);
    if (fill)
    {
      g2D.fill(polygon);
      g2D.draw(polygon);  // Draw outline
    }
    else
      g2D.draw(polygon);
  }

  /**
   * Draws a polygon with given vertexes.
   * @param vertexes the vertexes of the polygon
   */
  public void drawPolygon(Point[] vertexes)
  {
    drawPolygon(vertexes, false);
  }

  /**
   * Draws a filled polygon with given vertexes.
   * @param vertexes the vertexes of the polygon
   */
  public void fillPolygon(Point[] vertexes)
  {
    drawPolygon(vertexes, true);
  }

  private void drawGeneralPath(GeneralPath gp, boolean fill)
  {
    if (fill)
    {
      g2D.fill(gp);
      g2D.draw(gp);  // Draw outline
    }
    else
      g2D.draw(gp);
  }

  /**
   * Draws a figure defined by the given GeneralPath.
   * @param gp the GeneralPath that defines the shape
   */
  public void drawGeneralPath(GeneralPath gp)
  {
    drawGeneralPath(gp, false);
  }

  /**
   * Fills a figure defined by the given GeneralPath.
   * @param gp the GeneralPath that defines the shape
   */
  public void fillGeneralPath(GeneralPath gp)
  {
    drawGeneralPath(gp, true);
  }

  /**
   * Draws a single point. If the lineWidth is greater than 1,
   * more than one pixel may be involved.
   * @param pt the point to draw
   */
  public void drawPoint(Point pt)
  {
    if (lineWidth > 1)
    {
      Line2D line = new Line2D.Double(pt.x, pt.y, pt.x, pt.y);
      g2D.draw(line);
    }
    else
    {
      Graphics g = (Graphics)g2D;
      g.setColor(paintColor);
      g.drawLine(pt.x, pt.y, pt.x, pt.y);
    }
  }

  /**
   * Returns the color of the pixel of the background at given point.
   * @param pt point, where to pick the color; zero at upper left corner,
   * x to the left, y downwards
   * @return the color at the selected point; Color.black, if pt is outside
   * the bitmap
   */
  public Color getColor(Point pt)
  {
    if (pt.x < 0 || pt.x > width - 1 || pt.y < 0 || pt.y > height - 1)
      return Color.black;
    return new Color(bi.getRGB(pt.x, pt.y));
  }

  /**
   * Sets the font for displaying text with setText()
   * @param font
   */
  public void setFont(Font font)
  {
    this.font = font;
  }

  /**
   * Displays the given text at the given position using the current font.
   * @param text the text to display
   * @param pt the start point of the text baseline
   */
  public void drawText(String text, Point pt)
  {
    FontRenderContext frc = g2D.getFontRenderContext();
    TextLayout textLayout = new TextLayout(text, font, frc);
    textLayout.draw(g2D, pt.x, pt.y);
  }

  /**
   * Returns the available font families for the current platform.
   * @return a string that describes the available font families
   */
  public static String[] getAvailableFontFamilies()
  {
    GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
    String s[] = ge.getAvailableFontFamilyNames();
    return s;
  }

  /**
   * Returns the graphics device context used for drawing operations.
   * @return the Graphics2D reference
   */
  public Graphics2D getContext()
  {
    return g2D;
  }

  /**
   * Returns the reference of the buffered image used as bitmap.
   * @return the buffered image holding the graphics
   */
  public BufferedImage getBufferedImage()
  {
    return bi;
  }

  /**
   * Sets the paint mode of the graphics context to overwrite
   * with current paint color.
   */
  public void setPaintMode()
  {
    g2D.setPaintMode();
  }

  /**
   * Sets the paint mode to alternate between the current color and the given color.
   * This specifies that logical pixel operations are performed in the XOR mode,
   * which alternates pixels between the current color and a specified XOR color.
   * When drawing operations are performed, pixels which are the current color
   * are changed to the specified color, and vice versa.
   * Pixels that are of colors other than those two colors are changed in an
   * unpredictable but reversible manner; if the same figure is drawn twice,
   * then all pixels are restored to their original values.
   */
  public void setXORMode(Color c)
  {
    g2D.setXORMode(c);
  }

  /**
   * Releases all resources of the graphics context.
   */
  public void dispose()
  {
    if (g2D != null)
      g2D.dispose();
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
  public static boolean writeImage(BufferedImage bi, String filename, String type)
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
  public static BufferedImage floodFill(BufferedImage bi, Point pt, Color oldColor,
    Color newColor)
  {
    // Implementation from Hardik Gajjar of algorithm 
    // at http://en.wikipedia.org/wiki/Flood_fill

    BufferedImage image = new BufferedImage(bi.getWidth(), bi.getHeight(),
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
   * Returns a BufferedImage where each pixel has a new transparency value
   * (alpha component in the ARGB color model). The new transparency value is
   * the old value multiplied by the transparency multiplier (limited to 0..255).
   * @param bi the original image
   * @param factor the transparency multiplier 
   * @return the transformed image
   */
  public static BufferedImage setTransparency(BufferedImage bi, double factor)
  {
    BufferedImage image = new BufferedImage(bi.getWidth(), bi.getHeight(),
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
