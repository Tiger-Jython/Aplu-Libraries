// GGBackground.java

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
import java.awt.geom.*;
import java.awt.image.BufferedImage;
import java.awt.font.*;

/**
 * Class for drawing into the GameGrid background using Java Graphics2D methods.
 * The size of the drawing area is determined by the number and size of the cells
 * when constructing the GameGrid: In pixel units, e.g. distance between adjacent pixel,
 * the width of the background is nbHorzCells * cellSize horizontally and
 * the height is nbVertCells * cellSize vertically. Thus the background contains
 * width + 1 pixels horizontally and height + 1 pixels vertically,
 * and the pixel coordinates are 0 <= i <= width (inclusive), 0 <= k <= height
 * (inclusive) respectively.<br>
 * Example: Constructing new GameGrid(600, 400, 1) will give a background with 601x401
 * pixels. x-pixel coordinates are in the range 0 <= x <= 600, y-pixel coordinates
 * in the range 0 <=y <= 400. The center is exactly at coordinate (300, 200).<br><br>
 *
 * Defaults:<br>
 * - paint color: white<br>
 * - line width: 1 pixel<br>
 * - background color: black<br>
 * - font: SansSerif, Font.PLAIN, 24 pixel<br><br>
 *
 * GGBackground uses an won offscreen buffer that may contain a background image,
 * the grid lines and any background graphics. The current content of the buffer
 * may be saved and restored in an extra buffer using save() and restore().<br><br>
 * All drawing methods draw into the buffer that is automatically rendered
 * to the screen in every simulation cycle when the actor's act() methods are called.
 * If act() is not active, an explicit call to GameGrid.refresh() may be necessary to
 * render the buffer. Because actor sprite images are drawn in the foreground,
 * use text as sprite image to display it in the foreground.
 */
public class GGBackground
{
  private GameGrid gameGrid;
  private BufferedImage bi;
  private Graphics2D g2D;
  private BufferedImage bgImage;
  private BufferedImage saveBuffer;
  private Graphics2D saveG2D;
  private int lineWidth = 1;
  private Color paintColor = Color.white;
  private Font font = new Font("SansSerif", Font.PLAIN, 24);

  protected GGBackground(GameGrid gameGrid)
  {
    this.gameGrid = gameGrid;

    // Create background buffer
    bi = new BufferedImage(gameGrid.getNbHorzPix(), gameGrid.getNbVertPix(),
      BufferedImage.TYPE_INT_ARGB);
    g2D = bi.createGraphics();
    g2D.setColor(gameGrid.getBgColor());
    g2D.fillRect(0, 0, gameGrid.getNbHorzPix(), gameGrid.getNbVertPix());
    if (gameGrid.getBgImagePath() != null && !gameGrid.getBgImagePath().equals(""))
    {
      setBackgroundImage(gameGrid.getBgImagePath());
      g2D.drawImage(bgImage,
        gameGrid.getBgImagePosX(), gameGrid.getBgImagePosY(), null);
    }
    if (gameGrid.getGridColor() != null)
      drawGridLines(gameGrid.getGridColor());
    g2D.setColor(paintColor);
  }

  /**
   * Returns the BufferedImage of the current background.
   * The size is nbHorzPix x nbVertPix.
   * @return the background image
   */
  public BufferedImage getBackgroundImage()
  {
    return bi;
  }

  /**
   * Saves the current background to an extra buffer.
   */
  public void save()
  {
    if (saveBuffer == null)
    {
      saveBuffer =
        new BufferedImage(gameGrid.getNbHorzPix(), gameGrid.getNbVertPix(),
        BufferedImage.TYPE_INT_ARGB);
      saveG2D = saveBuffer.createGraphics();
    }
    saveG2D.drawImage(bi, 0, 0, null);
  }

  /**
   * Restores a previously saved background. Returns immediately if no save operation
   * was done yet.
   */
  public void restore()
  {
    if (saveBuffer == null)
      return;
    g2D.drawImage(saveBuffer, 0, 0, null);
  }

  /**
   * Retrieves the image either from the jar resource, from local drive or
   * from a internet server and draws it into the background buffer.
   * From the given filename the image file is searched in the following order:<br>
   * - if application is packed into a jar archive, relative to the root of the jar archive<br>
   * - relative to the directory &lt;userhome&gt;/gamegrid/<br>
   * - relative or absolute to current application directory<br>
   * - if filename starts with http://, from the given URL<br>
   * - add prefix _ and search relative to the root of the jar archive<br><br>
   * 
   * If the image cannot be loaded, nothing happens.
   * @param imagePath the file name or url
   * @param x x-coordinate of upper left corner
   * @param y y-coordinate of upper left corner
   */
  public void drawImage(String imagePath, int x, int y)
  {
    BufferedImage bi = GGBitmap.getImage(imagePath);
    if (bi == null)
      return;
    g2D.drawImage(bi, x, y, null);
  }
  
  /**
   * Draws the given image into the background buffer.
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
   * Draws the given image at position (0, 0) into the background buffer.
   */
  public void drawImage(BufferedImage bi)
  {
    drawImage(bi, 0, 0);
  }

  /**
   * Clears the background buffer by painting
   * it with the given background color. If necessary, draws an available
   * background image and the grid lines into the background buffer.
   * The background color is set to the given color, but
   * the paint color remains unchanged.
   * @param color the color of the background, alpha channel ignored
   */
  public void clear(Color color)
  {
    gameGrid.
      setBgColor(new Color(color.getRed(), color.getGreen(), color.getBlue()));
  }

  /**
   * Clears the background buffer by painting it with the current background color.
   * If necessary, draw an available background image and the grid lines into
   * the background buffer. The paint color remains unchanged.
   */
  public void clear()
  {
    BasicStroke stroke = new BasicStroke(1);
    g2D.setStroke(stroke);
    g2D.setColor(gameGrid.getBgColor());
    g2D.fillRect(0, 0, gameGrid.getNbHorzPix(), gameGrid.getNbVertPix());
    if (bgImage != null)
      g2D.drawImage(bgImage,
        gameGrid.getBgImagePosX(), gameGrid.getBgImagePosY(), null);
    if (gameGrid.getGridColor() != null)
      drawGridLines(gameGrid.getGridColor());
    g2D.setColor(paintColor);
    setLineWidth(lineWidth);
  }

  /**
   * Draws the grid lines using the given color. The current paint color is unchanged.
   * @param color the color of the grid lines
   */
  public void drawGridLines(Color color)
  {
    int cellSize = gameGrid.getCellSize();
    Color oldColor = g2D.getColor();
    g2D.setColor(color);
    for (int i = 0; i <= gameGrid.getNbHorzCells(); i++)
      g2D.drawLine(i * cellSize, 0, i * cellSize, gameGrid.getPgHeight());
    for (int k = 0; k <= gameGrid.getNbVertCells(); k++)
      g2D.drawLine(0, k * cellSize, gameGrid.getPgWidth(), k * cellSize);
    g2D.setColor(oldColor);
  }

  private void setBackgroundImage(String imagePath)
  {
    BufferedImage sourceImage = GGBitmap.getImage(imagePath);
    if (sourceImage == null)
    {
      GameGrid.fail("Failed to load background image from path\n" + imagePath
        + "\nApplication will terminate.");
    }
    GraphicsConfiguration gc =
      GraphicsEnvironment.getLocalGraphicsEnvironment().
      getDefaultScreenDevice().getDefaultConfiguration();
    bgImage =
      gc.createCompatibleImage(sourceImage.getWidth(),
      sourceImage.getHeight(),
      Transparency.TRANSLUCENT);

    bgImage.getGraphics().drawImage(sourceImage, 0, 0, null);
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
    return gameGrid.getBgColor();
  }

  /**
   * Sets the given new background color.
   * The background is redrawn by calling clear().
   * @param color the new background color
   */
  public void setBgColor(Color color)
  {
    gameGrid.setBgColor(color);
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

  private void drawArc(int xCenter, int yCenter, int radius, double startAngle, 
    double extentAngle, boolean fill)
  {
    int ulx = xCenter - radius;
    int uly = yCenter - radius;

    Arc2D.Double arc =
      new Arc2D.Double(ulx, uly, 2 * radius, 2 * radius, startAngle, extentAngle,
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
   * Draws an arc with given center, radius, start angle and angle extent.
   * @param pt the center of the arc
   * @param radius the radius of the arc
   * @param startAngle the start angle in degrees (zero to east, positive counterclockwise)
   * @param extentAngle the angle extent of the arc in degrees
   */
  public void drawArc(Point pt, int radius, double startAngle, double extentAngle)
  {
    drawArc(pt.x, pt.y, radius, startAngle, extentAngle, false);
  }

  /**
   * Fills an arc with given center, radius, start angle and angle extent.
   * @param pt the center of the arc
   * @param radius the radius of the arc
   * @param startAngle the start angle in degrees (zero to east, positive counterclockwise)
   * @param extentAngle the angle extent of the arc in degrees
   */
  public void fillArc(Point pt, int radius, double startAngle, double extentAngle)
  {
    drawArc(pt.x, pt.y, radius, startAngle, extentAngle, true);
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
   * Fills a cell  with given color. The boundary lines are considered to be
   * part of the cell.
   * The paint color remains unchanged.
   * @param location the cell's location (cell indices).
   * @param fillColor the filling color of the cell
   */
  public void fillCell(Location location, Color fillColor)
  {
    fillCell(location, fillColor, true);
  }

  /**
   * Fills a cell with given color.
   * The paint color remains unchanged.
   * @param location the cell's location (cell indices).
   * @param fillColor the filling color of the cell
   * @param boundary if true the boundary lines are considered to be part of the cell;
  otherwise the boundary lines are left intact
   */
  public void fillCell(Location location, Color fillColor, boolean boundary)
  {
    int cellSize = gameGrid.getCellSize();
    int ulx = location.x * cellSize;
    int uly = location.y * cellSize;
    g2D.setPaint(fillColor);
    Rectangle.Double rectangle;
    if (boundary)
      rectangle = new Rectangle.Double(ulx, uly, cellSize + 1, cellSize + 1);
    else
      rectangle = new Rectangle.Double(ulx + 1, uly + 1, cellSize - 1, cellSize - 1);
    g2D.fill(rectangle);
    g2D.setPaint(paintColor);
  }

  /**
   * Returns the color of the pixel of the background at given point.
   * (Be aware that sprite images are not part of the background.)
   * @param pt point, where to pick the color; if pt is outside
   * the grid, returns Color.black.
   * @return the color at the selected point
   */
  public Color getColor(Point pt)
  {
    if (!gameGrid.isInGrid(gameGrid.toLocation(pt)))
      return Color.black;
    return new Color(bi.getRGB(pt.x, pt.y));
  }

  /**
   * Returns the color of the pixel of the background at given cell's center.
   * (Be aware that sprite images are not part of the background.)
   * @param location cell's location where to pick the color; if location is outside
   * the grid, returns Color.black.
   * @return the color at the selected cell's center
   */
  public Color getColor(Location location)
  {
    if (!gameGrid.isInGrid(location))
      return Color.black;
    Point pt = gameGrid.toPoint(location);
    return getColor(pt);
  }

  /**
   * Sets the current font for displaying text with drawText()
   * @param font the font to be used
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
   * Returns the graphics device context of the background.
   * @return the Graphics2D of the background
   */
  public Graphics2D getContext()
  {
    return g2D;
  }

  /**
   * Sets the paint mode of the graphics context to overwrite
   * with current color.
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

  protected void dispose()
  {
    if (g2D != null)
      g2D.dispose();
    if (saveG2D != null)
      saveG2D.dispose();
  }
}
