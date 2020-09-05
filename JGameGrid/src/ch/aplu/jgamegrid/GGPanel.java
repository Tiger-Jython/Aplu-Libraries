// GGPanel.java

package ch.aplu.jgamegrid;

import java.awt.*;
import java.awt.geom.*;
import java.awt.image.BufferedImage;

/**
 * Class derived from GGBackground with a user-definable double 
 * coordinate system. All passed coordinates in the drawing methods
 * are user coordinates. Default coordinate system xmin = 0, xmax = 1,
 * ymin = 0, ymax = 1.
 */
public class GGPanel extends GGBackground
{
  private boolean isRefreshEnabled = true;
  private GameGrid gameGrid;
  private double xmin;
  private double xmax;
  private double ymin;
  private double ymax;
  private double mHorz;
  private double nHorz;
  private double mVert;
  private double nVert;
  private double xCurrent;
  private double yCurrent;

  protected GGPanel(GameGrid gameGrid)
  {
    super(gameGrid);
    this.gameGrid = gameGrid;
    window(0, 1, 0, 1);
  }

  /**
   * Transforms horizontal user coordinates to pixel coordinates.
   * @param x the user x-coordinate
   * @return the pixel x-coordinate
   */
  public int toPixelX(double x)
  {
    int xpix = (int)(mHorz * x + nHorz);
    return xpix;
  }

  /**
   * Transforms vertical user coordinates to pixel coordinates.
   * @param y the user y-coordinate
   * @return the pixel y-coordinate
   */
  public int toPixelY(double y)
  {
    int ypix = (int)(mVert * y + nVert);
    return ypix;
  }

  private int toPixelDx(double dx)
  {
    return (int)(mHorz * dx);
  }

  private int toPixelDy(double dy)
  {
    return (int)(mVert * dy);
  }

  /**
   * Transforms horizontal user coordinates to pixel coordinates.
   * @param pt the user x-y-coordinates
   * @return the pixel x-y-coordinate
   */
  public Point toPixelPoint(Point2D.Double pt)
  {
    return new Point(toPixelX(pt.x), toPixelY(pt.y));
  }

  /**
   * Transforms horizontal pixel coordinates to user coordinates.
   * @param xPix the pixel x-coordinate
   * @return the user x-coordinate
   */
  public double toUserX(int xPix)
  {
    return (xPix - nHorz) / mHorz;
  }

  /**
   * Transforms vertical pixel coordinates to user coordinates.
   * @param yPix the pixel x-coordinate
   * @return the user y-coordinate
   */
  public double toUserY(int yPix)
  {
    return (yPix - nVert) / mVert;
  }

  /**
   * Transforms pixel coordinates to user coordinates.
   * @param ptPix the pixel x-y-coordinates
   * @return the user x-y-coordinates
   */
  public Point2D.Double toUserPoint(Point ptPix)
  {
    return new Point2D.Double(toUserX(ptPix.x), toUserY(ptPix.y));
  }

  /**
   * Sets the user coordinate system (x from left to right, y from bottom to top).
   * @param xmin x-axis value at left
   * @param xmax x-axis value at right
   * @param ymin y-axis value at bottom
   * @param ymax y-axis value at top
   */
  public void window(double xmin, double xmax, double ymin, double ymax)
  {
    this.xmin = xmin;
    this.xmax = xmax;
    this.ymin = ymin;
    this.ymax = ymax;
    mHorz = (gameGrid.getNbHorzPix() - 1) / (xmax - xmin);
    nHorz = -mHorz * xmin;
    mVert = (gameGrid.getNbVertPix() - 1) / (ymin - ymax);
    nVert = -mVert * ymax;
  }

  /**
   * Draws a line from (x1, y1) to (x2, y2)
   * @param x1 the x-coordinate of the first vertex.
   * @param y1 the y-coordinate of the first vertex.
   * @param x2 the x-coordinate of the second vertex.
   * @param y2 the y-coordinate of the second vertex.
   */
  public void line(double x1, double y1, double x2, double y2)
  {
    drawLine(toPixelX(x1), toPixelY(y1), toPixelX(x2), toPixelY(y2));
    refreshInternal();
  }

  /**
   * Draws a line from the first vertext to the second vertex.
   * @param pt1 the first vertex
   * @param pt2 the second vertex
   */
  public void line(Point2D.Double pt1, Point2D.Double pt2)
  {
    drawLine(toPixelX(pt1.x), toPixelY(pt1.y), toPixelX(pt2.x), toPixelY(pt2.y));
    refreshInternal();
  }

  /**
   * Draws the given image into the background buffer.
   * @param x x-coordinate of upper left vertex
   * @param y y-coordinate of upper left vertex
   */
  public void image(BufferedImage bi, double x, double y)
  {
    drawImage(bi, toPixelX(x), toPixelY(y));
    refreshInternal();
  }

  /**
   * Draws the given image at position (0, 0) into the background buffer.
   */
  public void image(BufferedImage bi)
  {
    image(bi, 0, 0);
  }

  /**
   * Sets the current graph position to the given coordinates 
   * (without drawin anything).
   * @param x the x-coordinate of the graph position
   * @param y the y-coordinate of the graph position
   */
  public void move(double x, double y)
  {
    xCurrent = x;
    yCurrent = y;
  }

  /**
   * Sets the current graph position to the given point 
   * (without drawin anything).
   * @param pt the new graph position
   */
  public void move(Point2D.Double pt)
  {
    move(pt.x, pt.y);
  }

  /**
   * Draws a line from current graph position to the given coordinates 
   * and sets the graph position to these coordinates.
   * @param x the x-coordinate of the end point
   * @param y the y-coordinate of the end point
   */
  public void draw(double x, double y)
  {
    line(xCurrent, yCurrent, x, y);
    move(x, y);
    refreshInternal();
  }

  /**
   * Draws a line from current graph position to the given point 
   * and sets the graph position to this point.
   * @param pt the end point
   */
  public void draw(Point2D.Double pt)
  {
    draw(pt.x, pt.y);
  }

  /**
   * Draws a circle with given center coordinates and given radius in user x-coordinates.
   * The graph position is unchanged.
   * @param centerX the x-coordinate of the center
   * @param centerY the y-coordinate of the center
   * @param radius the radius in x-coordinate units
   * @param fill if true, the shape is filled
   */
  public void circle(double centerX, double centerY, double radius, boolean fill)
  {
    if (fill)
      fillCircle(new Point(toPixelX(centerX), toPixelY(centerY)),
        toPixelDx(radius));
    else
      drawCircle(new Point(toPixelX(centerX), toPixelY(centerY)),
        toPixelDx(radius));
    refreshInternal();
  }

  /**
   * Draws a circle with given center and given radius in user x-coordinates.
   * The graph position is unchanged.
   * @param center the center of the circle
   * @param radius the radius in x-coordinate units
   * @param fill if true, the shape is filled
   */
  public void circle(Point2D.Double center, double radius, boolean fill)
  {
    circle(center.x, center.y, radius, fill);
  }

  /**
   * Draws a circle with center at current graph position and given radius in user x-coordinates.
   * The graph position is unchanged.
   * @param radius the radius in x-coordinate units
   * @param fill if true, the shape is filled
   */
  public void circle(double radius, boolean fill)
  {
    circle(xCurrent, yCurrent, radius, fill);
  }

  /**
   * Draws/fills a rectangle with given opposite vertexes.
   * and given width and height.
   * The graph position is unchanged.
   * @param x1 x-coordinate of the first vertex
   * @param y1 y-coordinate of the first vertex
   * @param x2 x-coordinate of the opposite vertex
   * @param y2 y-coordinate of the opposite vertex
   * @param fill if true, the shape is filled
   */
  public void rectangle(double x1, double y1, double x2, double y2, boolean fill)
  {
    if (fill)
      fillRectangle(
        new Point(toPixelX(x1), toPixelY(y1)),
        new Point(toPixelX(x2), toPixelY(y2)));
    else
      drawRectangle(
        new Point(toPixelX(x1), toPixelY(y1)),
        new Point(toPixelX(x2), toPixelY(y2)));
    refreshInternal();
  }

  /**
   * Draws a rectangle with given opposite vertexes.
   * The graph position is unchanged.
   * @param pt1 the first vertex
   * @param pt2 the opposite vertex
   * @param fill if true, the shape is filled
   */
  public void rectangle(Point2D.Double pt1, Point2D.Double pt2, boolean fill)
  {
    rectangle(pt1.x, pt1.y, pt2.x, pt2.y, fill);
  }

  /**
   * Draws a rectangle with center at the current graph position
   * and given width and height.
   * The graph position is unchanged.
   * @param width the width of the rectangle
   * @param height the height of the rectangle
   * @param fill if true, the shape is filled
   */
  public void rectangle(double width, double height, boolean fill)
  {
    rectangle(xCurrent - width / 2, yCurrent - height / 2,
      xCurrent + width / 2, yCurrent + height / 2, fill);
  }

  /**
   * Draws an arc with given center coordinates, radius in user x-coordinates,
   * start angle and angle extent.
   * The graph position is unchanged.
   * @param centerX the x-coordinate of the center
   * @param centerY the y-coordinate of the center
   * @param radius the radius in x-coordinate units
   * @param startAngle the start angle in degrees
   * (zero at east, positive counter-clockwise)
   * @param extentAngle the angle extent in degrees
   * @param fill if true, the shape is filled
   */
  public void arc(double centerX, double centerY, double radius, double startAngle,
    double extentAngle, boolean fill)
  {
    if (fill)
      fillArc(new Point(toPixelX(centerX), toPixelY(centerY)), toPixelDx(radius),
        startAngle, extentAngle);
    else
      drawArc(new Point(toPixelX(centerX), toPixelY(centerY)), toPixelDx(radius),
        startAngle, extentAngle);
    refreshInternal();
  }

  /**
   * Draws an arc with given center, radius in user x-coordinates,
   * start angle and angle extent.
   * The graph position is unchanged.
   * @param center the center of the arc
   * @param radius the radius in x-coordinate units
   * @param startAngle the start angle in degrees
   * (zero at east, positive counter-clockwise)
   * @param extentAngle the angle extent of the arc in degrees
   * @param fill if true, the shape is filled
   */
  public void arc(Point2D.Double center, double radius, double startAngle,
    double extentAngle, boolean fill)
  {
    arc(center.x, center.y, radius, startAngle, extentAngle, fill);
  }

  /**
   * Draws an arc with center at current graph position, radius in user x-coordinates,
   * start angle and angle extent.
   * The graph position is unchanged.
   * @param radius the radius in x-coordinate units
   * @param startAngle the start angle in degrees
   * (zero at east, positive counter-clockwise)
   * @param extentAngle the angle extent of the arc in degrees
   * @param fill if true, the shape is filled
   */
  public void arc(double radius, double startAngle, double extentAngle, boolean fill)
  {
    arc(xCurrent, yCurrent, radius, startAngle, extentAngle, fill);
  }

  /**
   * Draws a polygon with given vertexes.
   * (Both arrays must be of equal size.)
   * The graph position is unchanged.
   * @param x an array of the x-coordinates of the vertexes
   * @param y an array of the y-coordinates of the vertexes
   * @param fill if true, the shape is filled
   */
  public void polygon(double[] x, double[] y, boolean fill)
  {
    Point[] vertexes = new Point[x.length];
    for (int i = 0; i < x.length; i++)
      vertexes[i] = new Point(toPixelX(x[i]), toPixelY(y[i]));
    if (fill)
      fillPolygon(vertexes);
    else
      drawPolygon(vertexes);
    refreshInternal();
  }

  /**
   * Draws a polygon with given vertexes.
   * The graph position is unchanged.
   * @param vertexes an array of the vertexes
   * @param fill if true, the shape is filled
   */
  public void polygon(Point2D.Double[] vertexes, boolean fill)
  {
    Point[] corners = new Point[vertexes.length];
    for (int i = 0; i < vertexes.length; i++)
      corners[i] = new Point(toPixelX(vertexes[i].x), toPixelY(vertexes[i].y));
    if (fill)
      fillPolygon(corners);
    else
      drawPolygon(corners);
    refreshInternal();
  }

  /**
   * Draws a triangle with given vertexes.
   * The graph position is unchanged.
   * @param x1 the x-coordinate of the first vertex
   * @param y1 the y-coordinate of the first vertex
   * @param x2 the x-coordinate of the second vertex
   * @param y2 the y-coordinate of the second vertex
   * @param x3 the x-coordinate of the third vertex
   * @param y3 the y-coordinate of the third vertex
   * @param fill if true, the shape is filled
   */
  public void triangle(double x1, double y1, double x2, double y2, double x3,
    double y3, boolean fill)
  {
    Point[] corner = new Point[4];
    corner[0] = toPixelPoint(new Point2D.Double(x1, y1));
    corner[1] = toPixelPoint(new Point2D.Double(x2, y2));
    corner[2] = toPixelPoint(new Point2D.Double(x3, y3));
    corner[3] = toPixelPoint(new Point2D.Double(x1, y1));
    if (fill)
      fillPolygon(corner);
    else
      drawPolygon(corner);
    refreshInternal();
  }

  /**
   * Draws a triangle with given vertexes.
   * The graph position is unchanged.
   * @param pt1 the first vertex
   * @param pt2 the second vertex
   * @param pt3 the third vertex
   * @param fill if true, the shape is filled
   */
  public void triangle(Point2D.Double pt1, Point2D.Double pt2, Point2D.Double pt3, boolean fill)
  {
    triangle(pt1.x, pt1.y, pt2.x, pt2.y, pt3.x, pt3.y, fill);
  }

  /**
   * Draws a figure defined by the given GeneralPath.
   * The graph position is unchanged.
   * @param gp the GeneralPath that defines the shape
   * @param fill if true, the shape is filled
   */
  public void generalPath(GeneralPath gp, boolean fill)
  {
    if (fill)
      fillGeneralPath(gp);
    else
      drawGeneralPath(gp);
  }

  /**
   * Draws a single point at given coordinates.
   * @param x the x-coordinate of the point
   * @param y the y-coordinate of the point
   */
  public void point(double x, double y)
  {
    drawPoint(new Point(toPixelX(x), toPixelY(y)));
    refreshInternal();
    xCurrent = x;
    yCurrent = y;
  }

  /**
   * Draws a single point at given coordinates.
   * @param pt the point where to draw
   */
  public void point(Point2D.Double pt)
  {
    point(pt.x, pt.y);
  }

  /**
   * Draws a single point at the current graph position.
   */
  public void point()
  {
    point(xCurrent, yCurrent);
  }

  /**
   * Displays the given text at the given user coordinates using the 
   * current font.
   * @param text the text to display
   * @param x the x-coordinate of the start point of the text baseline
   * @param y the y-coordinate of the start point of the text baseline
   */
  public void drawText(String text, double x, double y)
  {
    drawText(text, new Point(toPixelX(x), toPixelY(y)));
  }

  /**
   * Displays the given text at the given point using the 
   * current font.
   * @param text the text to display
   @param pt the start point of the text baseline
   */
  public void drawText(String text, Point2D.Double pt)
  {
    drawText(text, toPixelPoint(pt));
  }

  /**
   * Clears the frame buffer by painting it with the current background color.
   * Draws an available background image and the grid lines into the frame buffer.
   * Sets the current graph position to (0, 0). Renders the frame buffer to
   * the screen unless setRefreshEnabled(false) was called.
   */
  public void clean()
  {
    super.clear();
    refreshInternal();
    xCurrent = yCurrent = 0;
  }

  /**
   * Same as clear(), but remains the current graph position unchanged.
   */
  public void erase()
  {
    super.clear();
    refreshInternal();
  }

  /**
   * Returns the left value of the x-coordinate axis.
   * @return xmin
   */
  public double getXmin()
  {
    return xmin;
  }

  /**
   * Returns the right value of the x-coordinate axis.
   * @return xmax
   */
  public double getXmax()
  {
    return xmax;
  }

  /**
   * Returns the bottom value of the y-coordinate axis.
   * @return xmin
   */
  public double getYmin()
  {
    return ymin;
  }

  /**
   * Returns the top value of the y-coordinate axis.
   * @return xmin
   */
  public double getYmax()
  {
    return ymax;
  }

  /**
   * Sets the current paint color.
   * @param color the new paint color
   */
  public void color(Color color)
  {
    setPaintColor(color);
  }

  /**
   * Enables/disables automatic refresh in all graphics methods of GGPanel.
   * @param enabled if true, refresh is enabled (default); otherwise 
   * no refresh is done
   */
  public void setRefreshEnabled(boolean enabled)
  {
    isRefreshEnabled = enabled;
  }

  private void refreshInternal()
  {
    if (isRefreshEnabled)
      gameGrid.refresh();
  }
}
