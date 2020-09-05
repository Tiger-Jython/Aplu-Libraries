// GGTileMap.java

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
import java.util.HashMap;
import java.awt.image.BufferedImage;
import java.net.URL;
import javax.imageio.ImageIO;
import java.io.*;

/**
 * Class that implements a tile map, e.g. an arragement of rectangular
 * images of the same size like tiles on a floor. A single tile is
 * addressed by its horizontal and vertical indices, ranging from
 * 0..nbHorzTiles-1 and 0..nbVertTiles-1 packed into a Location object.
 * Same tile images are loaded only once to save memory space. A tile image set to null is like an invisible
 * tile.<br><br>
 *
 * The tile map may be positioned relative to the playground by setting the
 * position of the upper left vertex in pixel units. The origin of the
 * coordinate system corresponds to the upper left vertex of the playground,
 * x-coordinate to the right, y-coordinate downwards (negative
 * coordinates allowed).<br><br>
 *
 * The tile map may be moved by setting the tile position. In every
 * simulation loop or when refresh() is called the images are drawn
 * in the following order:<br><br>
 *
 * - background (background image, grid, background drawing using Graphics2D)<br><br>
 * - tile map<br><br>
 * - actors<br><br><br>
 *
 * Be aware that the size of the tiles are given in pixel units, but
 * an image using a x b pixels has the size (a-1) x (b-1) in pixel units.
 */
public class GGTileMap
{
  private GameGrid pane;
  private int nbHorzTiles;
  private int nbVertTiles;
  private int tileWidth;
  private int tileHeight;
  private int ulx = 0;
  private int uly = 0;
  private int width;
  private int height;
  private HashMap<String, BufferedImage> tileStore =
    new HashMap<String, BufferedImage>();    // Matrix: images[horz][vert]
  private BufferedImage[][] images;
  private GGCollisionArea[][] collisionArea;
  private boolean[][] isTileCollisionEnabled;

  protected GGTileMap(GameGrid pane)
  {
    this.pane = pane;
  }

  protected void init(int nbHorzTiles, int nbVertTiles, int tileWidth, int tileHeight)
  {
    this.nbHorzTiles = nbHorzTiles;
    this.nbVertTiles = nbVertTiles;
    this.tileWidth = tileWidth;
    this.tileHeight = tileHeight;
    width = nbHorzTiles * tileWidth;
    height = nbVertTiles * tileHeight;
    images = new BufferedImage[nbHorzTiles][nbVertTiles];
    collisionArea = new GGCollisionArea[nbHorzTiles][nbVertTiles];
    isTileCollisionEnabled = new boolean[nbHorzTiles][nbVertTiles];
    for (int i = 0; i < nbHorzTiles; i++)
    {
      for (int k = 0; k < nbVertTiles; k++)
      {
        images[i][k] = null;
        collisionArea[i][k] = null;
        isTileCollisionEnabled[i][k] = true;
      }
    }
  }

  /**
   * Sets the tile image of the tile with given indices. In order the image
   * fits exactly on the tile, its size in number of pixels should be
   * tileHeight x tileWidth given when constructing the tile map.
   * @param imagePath the path to the image file; null if the tile is invisible
   * @param horz the horizontal tile index in the range 0..nbHorzTiles-1
   * @param vert the vertical tile index in the range 0..nbVertTiles-1
   */
  public void setImage(String imagePath, int horz, int vert)
  {
    setImage(imagePath, new Location(horz, vert));
  }

  /**
   * Sets the tile image of the tile with given map location.In order the image
   * fits exactly on the tile, its size in number of pixels should be
   * tileHeight x tileWidth given when constructing the tile map.
   * @param imagePath the path to the image file; null if the tile is invisible
   * @param location the location of the tile within the map (0..nbHorzTiles-1, 0..nbVertTiles1)
   */
  public void setImage(String imagePath, Location location)
  {
    if (imagePath == null || imagePath.equals(""))
    {
      images[location.x][location.y] = null;
      collisionArea[location.x][location.y] = null;
    }
    else
    {
      BufferedImage bi = getImage(imagePath);
      if (bi != null)
      {
        images[location.x][location.y] = bi;
        // Sets default collision area
        // Image of w x h pixels is (w-1) x (h-1) pixels units wide
        GGRectangle rect =
          new GGRectangle(new GGVector(0, 0), 0, bi.getWidth() - 1, bi.getHeight() - 1);
        collisionArea[location.x][location.y] =
          new GGCollisionArea(rect, null, null, null, CollisionType.RECTANGLE);
      }
      else
      {
        images[location.x][location.y] = null;
        collisionArea[location.x][location.y] = null;
      }
    }
  }

  /**
   * Returns the number of horizontal tiles.
   * @return the number of horizontal tiles
   */
  public int getNbHorzTiles()
  {
    return nbHorzTiles;
  }

  /**
   * Returns the number of vertical tiles.
   * @return the number of vertical tiles
   */
  public int getNbVertTiles()
  {
    return nbVertTiles;
  }

  /**
   * Returns total width (in pixels units) of the tile map.
   * @return the width of the tile map
   */
  public int getWidth()
  {
    return width;
  }

  /**
   * Returns total height (in pixels units) of the tile map.
   * @return the height of the tile map
   */
  public int getHeigth()
  {
    return height;
  }

  /**
   * Returns current position of upper left vertex with respect to the
   * playground coordinate system (origin at upper left vertex, x-coordinate to the left,
   * y-coordinate downwards).
   * @return the current (ulx, uly)
   */
  public Point getPosition()
  {
    return new Point(ulx, uly);
  }

  /**
   * Sets the current position of the upper left vertex with respect to the
   * playground coordinate system (origin at upper left vertex, x-coordinate to the left,
   * y-coordinate downwards).
   * @param point the new (ulx, uly)
   */
  public void setPosition(Point point)
  {
    ulx = point.x;
    uly = point.y;
  }

  /**
   * Returns the current coordinates of the upper left vertex of tile at given
   * map location with respect to the playground coordinate system (origin
   * at upper left vertex, x-coordinate to the left, y-coordinate downwards).
   * @param loc the tile location within the map (0..nbHorzTiles-1, 0..nbVertTiles-1)
   * @return the upper left vertex of the tile
   */
  public Point getUpperLeftVertex(Location loc)
  {
    return new Point(ulx + loc.x * tileWidth, uly + loc.y * tileHeight);
  }

  /**
   * Returns the current center of tile at given map location with respect
   * to the playground coordinate system (origin  at upper left vertex,
   * x-coordinate to the left, y-coordinate downwards). The center is defined as
   * half the distance (in pixel units) to the neighbour tile (rounded to an integer).
   * @param loc the tile location within the map (0..nbHorzTiles-1, 0..nbVertTiles-1)
   * @return the center of the tile
   */
  public Point getCenter(Location loc)
  {
    return new Point(getUpperLeftVertex(loc).x + tileWidth / 2,
      getUpperLeftVertex(loc).y + tileHeight / 2);
  }

  protected BufferedImage getImage(String imagePath)
  {
    if (imagePath == null || imagePath.equals(""))
      return null;
    if (tileStore.get(imagePath) != null)
    {
      return tileStore.get(imagePath);
    }

    BufferedImage sourceImage = GGBitmap.getImage(imagePath);
    if (sourceImage == null)
    {
      GameGrid.fail("Error while loading tile map image\n" + imagePath
        + "\nApplication will terminate.");
    }
    int w = sourceImage.getWidth();
    int h = sourceImage.getHeight();

    // create an accelerated image of the right size to store our sprite in
    GraphicsConfiguration gc =
      GraphicsEnvironment.getLocalGraphicsEnvironment().
      getDefaultScreenDevice().getDefaultConfiguration();

    BufferedImage bi = gc.createCompatibleImage(
      sourceImage.getWidth(),
      sourceImage.getHeight(),
      Transparency.TRANSLUCENT);

    Graphics2D g2D = bi.createGraphics();
    g2D.drawImage(sourceImage, 0, 0, null);

    // create a sprite, add it to the cache then return it
    tileStore.put(imagePath, bi);

    return bi;
  }

  protected GGCollisionArea getCurrentCollisionArea(Location location)
  {
    return collisionArea[location.x][location.y];
  }

  protected GGRectangle getCurrentCollisionRectangle(Location location)
  {
    if (getCurrentCollisionArea(location) == null
      || !isTileCollisionEnabled(location))
      return null;
    GGRectangle collisionRectangle = getCurrentCollisionArea(location).getRectangle();
    if (collisionRectangle == null)
      return null;
    GGRectangle rect = collisionRectangle.clone();
    Point pt = getCenter(location);
    rect.translate(new GGVector(pt.x, pt.y));
    return rect;
  }

  protected GGCircle getCurrentCollisionCircle(Location location)
  {
    if (getCurrentCollisionArea(location) == null
      || !isTileCollisionEnabled(location))
      return null;
    GGCircle collisionCircle = getCurrentCollisionArea(location).getCircle();
    if (collisionCircle == null)
      return null;
    GGCircle circle = collisionCircle.clone();
    Point pt = getCenter(location);
    circle.translate(new GGVector(pt.x, pt.y));
    return circle;
  }

  protected GGLine getCurrentCollisionLine(Location location)
  {
    if (getCurrentCollisionArea(location) == null
      || !isTileCollisionEnabled(location))
      return null;
    GGLine collisionLine = getCurrentCollisionArea(location).getLine();
    if (collisionLine == null)
      return null;
    GGLine line = collisionLine.clone();
    Point pt = getCenter(location);
    line.translate(new GGVector(pt.x, pt.y));
    return line;
  }

  protected GGVector getCurrentCollisionSpot(Location location)
  {
    if (getCurrentCollisionArea(location) == null
      || !isTileCollisionEnabled(location))
      return null;
    GGVector collisionSpot = getCurrentCollisionArea(location).getSpot();
    if (collisionSpot == null)
      return null;
    GGVector spot = collisionSpot.clone();
    Point pt = getCenter(location);
    spot = spot.add(new GGVector(pt.x, pt.y));
    return spot;
  }

  protected CollisionType getCurrentCollisionType(Location location)
  {
    if (getCurrentCollisionArea(location) == null
      || !isTileCollisionEnabled(location))
      return CollisionType.NONE;
    return getCurrentCollisionArea(location).getCollisionType();
  }

  /**
   * Enable/disable the detection of collisions with the given tile.
   * @param location the tile location within the tile map
   * @param enable if true, collisions will be notified
   */
  public void setTileCollisionEnabled(Location location, boolean enable)
  {
    isTileCollisionEnabled[location.x][location.y] = enable;
  }

  /**
   * Returns true, if collision notification with the given tiles is enabled.
   * @param location the tile location within the tile map
   * @return true, if collision detection is enabled
   */
  public boolean isTileCollisionEnabled(Location location)
  {
    return isTileCollisionEnabled[location.x][location.y];
  }

  /**
   * Selects the rectangle (in pixel units) relative to the tile that is used for
   * collision detection. The following coordinate system is used:<br><br>
   * x-axis to the left, y-axis downward, zero at tile center<br><br>
   * Any collision types defined earlier are removed.
   * @param location to location of the tile within the tile map
   * @param center the rectangle center (zero at tile center)
   * @param width the width in pixel units of the rectangle (in x-direction)
   * @param height the height in pixel units of the rectangle (in y-direction)
   */
  public void setCollisionRectangle(Location location, Point center, int width, int height)
  {
    GGRectangle rect =
      new GGRectangle(new GGVector(center), 0, width, height);
    collisionArea[location.x][location.y] =
      new GGCollisionArea(rect, null, null, null, CollisionType.RECTANGLE);
  }

  /**
   * Selects the circle (in pixel units) relative to the tile center that is used for
   * collision detection. The following coordinate system is used:<br><br>
   * x-axis to the left, y-axis downward, zero at tile center.<br><br>
   * Any collision types defined earlier are removed.
   * @param location to location of the tile within the tile map
   * @param center circle center (zero at tile center)
   * @param radius the radius of the circle (in pixel units)
   */
  public void setCollisionCircle(Location location, Point center, int radius)
  {
    GGCircle circle = new GGCircle(new GGVector(center), radius);
    collisionArea[location.x][location.y] =
      new GGCollisionArea(null, circle, null, null, CollisionType.CIRCLE);
  }

  /**
   * Selects the line segment (in pixel units) relative to the tile that is used for
   * collision detection. The following coordinate system is used:<br><br>
   * x-axis to the left, y-axis downward, zero at image center<br><br>
   * Any collision types defined earlier are removed.
   * @param location to location of the tile within the tile map
   * @param startPoint the start point of the line (zero at image center)
   * @param endPoint the end point of the line (zero at image center)
   */
  public void setCollisionLine(Location location, Point startPoint, Point endPoint)
  {
    GGLine line = new GGLine(new GGVector(startPoint), new GGVector(endPoint));
    collisionArea[location.x][location.y] =
      new GGCollisionArea(null, null, line, null, CollisionType.LINE);
  }

  /**
   * Selects the hot spot relative to the sprite image that is used for
   * collision detection. The following coordinate system is used:<br><br>
   * x-axis to the left, y-axis downward, zero at image center<br><br>
   * Any collision types defined earlier are removed.
   * @param location to location of the tile within the tile map
   * @param spot the hot spot (zero at image center)
   */
  public void setCollisionSpot(Location location, Point spot)
  {
    GGVector vSpot = new GGVector(spot);
    collisionArea[location.x][location.y] =
      new GGCollisionArea(null, null, null, vSpot, CollisionType.SPOT);

  }

  protected void draw(Graphics2D g2D)
  {
    if (SharedConstants.DEBUG == SharedConstants.DEBUG_LEVEL_MEDIUM)
      System.out.println("GGTileMap.draw(): (ulx, uly) = (" + ulx + ", " + uly + ")");
    BufferedImage bi;
    // Select tiles that are in the visible part of the playground
    int minx = Math.max(0, -ulx / tileWidth);
    int maxx = Math.min(nbHorzTiles, (-ulx + pane.getPgWidth()) / tileWidth + 1);
    int miny = Math.max(0, -uly / tileHeight);
    int maxy = Math.min(nbVertTiles, (-uly + pane.getPgHeight()) / tileHeight + 1);
    for (int i = minx; i < maxx; i++)
    {
      for (int k = miny; k < maxy; k++)
      {
        if (SharedConstants.DEBUG == SharedConstants.DEBUG_LEVEL_MEDIUM)
          System.out.println("tile indices (i, k) = (" + i + ", " + k + ")");
        bi = images[i][k];
        if (bi != null)
        {
          g2D.drawImage(bi, ulx + i * tileWidth, uly + k * tileHeight, null);
        }
      }
    }
  }
}
