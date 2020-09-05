// Location.java

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

import java.util.ArrayList;


/**
 * Class to represent the position of a cell (in cell coordinates).
 * Directions are measured in degrees clockwise from the positive x-axis.
 */
public class Location
{
  /**
   * The public horizontal coordinate (cell index) of the location.
   */
  public int x;
  /**
   * The public vertical coordinate (cell index) of the location.
   */
  public int y;
  /**
   * The compass direction for east.
   */
  public static final CompassDirection EAST = CompassDirection.EAST;
  /**
   * The compass direction for southeast.
   */
  public static final CompassDirection SOUTHEAST = CompassDirection.SOUTHEAST;
  /**
   * The compass direction for south.
   */
  public static final CompassDirection SOUTH = CompassDirection.SOUTH;
  /**
   * The compass direction for southwest.
   */
  public static final CompassDirection SOUTHWEST = CompassDirection.SOUTHWEST;
  /**
   * The compass direction for west.
   */
  public static final CompassDirection WEST = CompassDirection.WEST;
  /**
   * The compass direction for northwest.
   */
  public static final CompassDirection NORTHWEST = CompassDirection.NORTHWEST;
  /**
   * The compass direction for north.
   */
  public static final CompassDirection NORTH = CompassDirection.NORTH;
  /**
   * The compass direction for northeast.
   */
  public static final CompassDirection NORTHEAST = CompassDirection.NORTHEAST;

  /**
   * Class to represent the 8 compass directions.
   */
  public enum CompassDirection
  {
    /**
     * The compass direction for east.
     */
    EAST(0),
    /**
     * The compass direction for southeast.
     */
    SOUTHEAST(45),
    /**
     * The compass direction for south.
     */
    SOUTH(90),
    /**
     * The compass direction for southwest.
     */
    SOUTHWEST(135),
    /**
     * The compass direction for west.
     */
    WEST(180),
    /**
     * The compass direction for northwest.
     */
    NORTHWEST(225),
    /**
     * The compass direction for north.
     */
    NORTH(270),
    /**
     * The compass direction for northeast.
     */
    NORTHEAST(315);
    private final int direction;

    private CompassDirection(int direction)
    {
      this.direction = direction;
    }

    /**
     * Returns the direction in degrees (0 to east, clockwise).
     * @return the direction in degrees, rounded to an integer
     */
    public int getDirection()
    {
      return direction;
    }
  };

  /**
   * Constructs a location at (0, 0).
   */
  public Location()
  {
    this.x = 0;
    this.y = 0;
  }

  /**
   * Constructs a location with given horizontal and vertical cell coordinates.
   * @param x the horizontal cell coordinate
   * @param y the vertical cell coordinate
   */
  public Location(int x, int y)
  {
    this.x = x;
    this.y = y;
  }

  /**
   * Constructs a location with the coordinates of the given location.
   * @param location the location where to take the horizontal and vertical cell coordinates
   */
  public Location(Location location)
  {
    x = location.x;
    y = location.y;
  }

  /**
   * Gets the horizontal cell coordinate (index).
   * @return the horizontal coordinate
   */
  public int getX()
  {
    return x;
  }

  /**
   * Gets the vertical cell coordinate (index).
   * @return the vertical coordinate
   */
  public int getY()
  {
    return y;
  }

  /**
   * Gets the adjacent location of a cell where a displacement arrow from the
   * current center of the current cell with given direction and
   * length = (distance + epsilon) * cellSize ends up. For distance == 1,
   * epsilon is 0.3. This will give the neighbour cells in the 8 compass
   * directions 45 degrees wide. For distance > 1, epsilon is -0.2.
   * @param direction the direction in which to find a adjacent location
   * @param distance the distance to the requested cell location in cell units.
   * @return the adjacent location in the direction where the displacement arrow ends up
   */
  public Location getAdjacentLocation(double direction, int distance)
  {
    if (distance == 1)
      return getAdjacentLocation(direction, distance, 0.3);
    else
      return getAdjacentLocation(direction, distance, -0.2);
  }

  /**
   * Same as getAdjacentLocation(double direction, int distance) with
   * given compass direction.
   * @param compassDir the compass direction in which to find a adjacent location
   * @param distance the distance to the requested cell location
   * @return the adjacent location in the direction where the displacement arrow ends up
   */
  public Location getAdjacentLocation(CompassDirection compassDir, int distance)
  {
    return getAdjacentLocation(compassDir.getDirection(), distance);
  }

  /**
   * Same as getAdjacentLocation(double direction, 5).
   * @param direction the direction in which to find a adjacent location
   * @return the adjacent location in the direction where the displacement arrow ends up
   */
  public Location getAdjacentLocation(double direction)
  {
    return getAdjacentLocation(direction, 5, -0.2);
  }

  /**
   * Same as getAdjacentLocation(double direction, 5) with given
   * compass direction.
   * @param compassDir the compass direction in which to find a adjacent location
   * @return the adjacent location in the direction where the displacement arrow ends up
   */
  public Location getAdjacentLocation(CompassDirection compassDir)
  {
    return getAdjacentLocation(compassDir.getDirection());
  }

  private Location getAdjacentLocation(double direction, int distance, 
    double epsilon)
  {
    if (distance < 0)
    {
      direction = 180 + direction;
      distance = -distance;
    }
    direction = direction % 360;
    if (direction < 0)
      direction = 360 + direction;
    int xNew = (int)Math.floor(x + 0.5 +
      (distance + epsilon) * Math.cos(direction / 180 * Math.PI));
    int yNew = (int)Math.floor(y + 0.5 +
      (distance + epsilon) * Math.sin(direction / 180 * Math.PI));
    return (new Location(xNew, yNew));
  }

  /**
   * Gets one of the 8 surrounding cells in given direction 45 degrees wide.
   * @param direction the direction in which to find a neighbour location
   * @return one of the 8 neighbour cell locations
   */
  public Location getNeighbourLocation(double direction)
  {
    return getAdjacentLocation(direction, 1, 0.3);
  }

  /**
   * Gets one of the 8 surrounding cells in the given compass directions.
   * @param compassDir the compass direction in which to find a neighbour location
   * @return one of the 8 neighbour cell locations
   */
  public Location getNeighbourLocation(CompassDirection compassDir)
  {
    return getNeighbourLocation(compassDir.getDirection());
  }

  /**
   * Returns the direction from the current location to the given location.
   * If the location is the same as the current location, returns zero.
   * @param location the target location
   * @return the direction that points from the current location to the
   * given location (0..360 degrees, clockwise, zero to east)
   */
  public double getDirectionTo(Location location)
  {
    if (equals(location) && location == null)
      return 0;

    int dx = location.x - x;
    int dy = location.y - y;

    double dir = Math.atan2(dy, dx);
    dir = Math.toDegrees(dir);
    if (dir < 0)
      dir = 360 + dir;

    return dir;
  }

  /**
   * Returns the distance from the current location to the given location
   * (in cellsize units, rounded to integer).
   * @param location the remote location
   * @return the distance to the remote location (in cellsize units)
   */
  public int getDistanceTo(Location location)
  {
    if (location == null)
      return 0;
    int dx = location.x - x;
    int dy = location.y - y;
    double d = Math.sqrt(dx * dx + dy * dy);
    return (int)(d + 0.5);
  }

  /**
   * Returns the compass direction restricted to 4 sectors from the current location the given location.
   * If the location is the same as the current location, returns CompassDirection.WEST.
   * @param location the target location
   * @return the direction that points from the current location to the
   * given location (0..360 degrees, clockwise, zero to east)
   */
  public CompassDirection get4CompassDirectionTo(Location location)
  {
    if (location == null)
      return null;
    double dir = getDirectionTo(location);
    if ((dir > 315 && dir < 360) || (dir >= 0 && dir <= 45))
    {
      return CompassDirection.EAST;
    }
    if (dir > 45 && dir <= 135)
    {
      return CompassDirection.SOUTH;
    }
    if (dir > 135 && dir <= 225)
    {
      return CompassDirection.WEST;
    }
    if (dir > 225 && dir <= 315)
    {
      return CompassDirection.NORTH;
    }
    return null; 
  }

  /**
   * Returns the compass direction restricted to 8 sectors from the current location the given location.
   * If the location is the same as the current location, returns CompassDirection.WEST.
   * @param location the target location
   * @return the direction that points from the current location to the
   * given location (0..360 degrees, clockwise, zero to east)
   */
  public CompassDirection getCompassDirectionTo(Location location)
  {
    if (location == null)
      return null;
    double dir = getDirectionTo(location);
    if ((dir > 337.5 && dir < 360) || (dir >= 0 && dir <= 22.5))
    {
      return CompassDirection.EAST;
    }
    if (dir > 22.5 && dir <= 67.5)
    {
      return CompassDirection.SOUTHEAST;
    }
    if (dir > 67.5 && dir <= 112.5)
    {
      return CompassDirection.SOUTH;
    }
    if (dir > 112.5 && dir <= 157.5)
    {
      return CompassDirection.SOUTHWEST;
    }
    if (dir > 157.5 && dir <= 202.5)
    {
      return CompassDirection.WEST;
    }
    if (dir > 202.5 && dir <= 247.5)
    {
      return CompassDirection.NORTHWEST;
    }
    if (dir > 247.5 && dir <= 292.5)
    {
      return CompassDirection.NORTH;
    }
    if (dir > 292.5 && dir <= 337.5)
    {
      return CompassDirection.NORTHEAST;
    }
    return null;  // never happens
  }

 
  /**
   * Returns a hash code value for the object. 
   * In accordance with the general contract for hashCode().
   * (Code from JDK's Point class)
   */
  public int hashCode()
  {
    long bits = Double.doubleToLongBits(x);
    bits ^= Double.doubleToLongBits(y) * 31;
    return (((int) bits) ^ ((int) (bits >> 32)));
  }
 
  /**
   * Checks whether the x-y-coordinates of the given location
   * are equal to the x-y-coordinates of the current location
   * (overrides Object.equals()).
   * @param obj the object whose location is checked
   * @return true if the given object is of class Location and  has the same (x, y)
   * as the current location; otherwise false
   */
  public boolean equals(Object obj)
  {
    if (obj == null)
      return false;
    if (obj == this)
      return true;
    if (obj.getClass() != Location.class)
      return false;
    Location location = (Location)obj;
    return x == location.x && y == location.y;
  }

  /**
   * Returns all locations in a specified distance.
   * The distance defines a circle around the current cell center. All cells that intersects
   * with this circle are returned. Also cells outside the visible grid are considered.
   * To restrict this list to cells inside the grid, use GameGrid.isInGrid().
   * To get the 6 nearest neighbours, use distance = 1, to exclude diagonal
   * locations, use distance = 0.5;
   * The current location is not included.
   *
   * @param distance the distance in (fractional) cell units
   */
  public ArrayList<Location> getNeighbourLocations(double distance)
  {
    ArrayList<Location> a = new ArrayList<Location>();
    int ymax = (int)(distance + 0.5);
    for (int dy = 1; dy <= ymax; dy += 1)
    {
      double y1 = dy - 0.5;
      double x1 = Math.sqrt(distance * distance - y1 * y1);
      int xmax = (int)(x1 + 0.5);
      for (int dx = 0; dx <= xmax; dx++)
      {
        a.add(new Location(x + dx, y + dy));
        a.add(new Location(x + dy, y - dx));
        a.add(new Location(x - dy, y + dx));
        a.add(new Location(x - dx, y - dy));
      }
    }
    return a;
  }

  /**
   * Returns a string that represents this location.
   * @return a string with horizontal and vertical coordinates of this location, in the format
   * (x, y)
   */
  public String toString()
  {
    return "(" + getX() + ", " + getY() + ")";
  }

  /**
   * Returns a new location with duplicated coordinates.
   * @return a clone of the current location
   */
  public Location clone()
  {
    return new Location(this);
  }
}
