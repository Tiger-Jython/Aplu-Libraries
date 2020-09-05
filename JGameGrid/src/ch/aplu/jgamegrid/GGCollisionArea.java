// GGCollisionArea.java

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


enum CollisionType {NONE, RECTANGLE, CIRCLE, LINE, SPOT, IMAGE};

class GGCollisionArea
{
  private GGRectangle rectangle = null;
  private GGCircle circle = null;
  private GGLine line = null;
  private GGVector spot = null;
  private CollisionType collisionType;

  protected GGCollisionArea(GGRectangle rectangle, GGCircle circle,
    GGLine line, GGVector spot, CollisionType collisionType)
        {
    this.rectangle = rectangle;
    this.circle = circle;
    this.line = line;
    this.spot = spot;
    this.collisionType = collisionType;
  }

  protected GGCollisionArea(GGCollisionArea area)
  {
    rectangle = area.rectangle;
    circle = area.circle;
    line = area.line;
    spot = area.spot;
    collisionType = area.collisionType;
  }

  /**
   * Returns a copy of the rectangle.
   * @return a clone of the rectangle
   */
  protected GGRectangle getRectangle()
  {
    if (rectangle != null)
      return rectangle.clone();
    return null;
  }

  /**
   * Returns a copy of the circle.
   * @return a clone of the circle
   */
  protected GGCircle getCircle()
  {
    if (circle != null)
      return circle.clone();
    return null;
  }

  /**
   * Returns a copy of the line.
   * @return a clone of the line
   */
  protected GGLine getLine()
  {
    if (line != null)
      return line.clone();
    return null;
  }

  /**
   * Returns a copy of the spot.
   * @return a clone of the spot
   */
  protected GGVector getSpot()
  {
    if (spot != null)
      return spot.clone();
    return null;
  }

  /**
   * Returns the collisionType.
   * @return the type of the collision detection
   */
  protected CollisionType getCollisionType()
  {
    return collisionType;
  }

  /**
   * Sets the rectangle to the given rectangle (reference copy).
   * @param rectangle the rectangle reference to copy
   */
  protected void setRectangle(GGRectangle rectangle)
  {
    this.rectangle = rectangle;
  }

  /**
   * Sets the circle to the given circle (reference copy).
   * @param circle the circle reference to copy
   */
  protected void setCircle(GGCircle circle)
  {
    this.circle = circle;
  }

  /**
   * Sets the line to the given line (reference copy).
   * @param line the line reference to copy
   */
  protected void setLine(GGLine line)
  {
    this.line = line;
  }

  /**
   * Sets the spot to the given spot (reference copy).
   * @param spot the spot reference to copy
   */
  protected void setSpot(GGVector spot)
  {
    this.spot = spot;
  }

  /**
   * Sets the type of collision
   * @param the collision type
   */
  protected void setCollisionType(CollisionType collisionType)
  {
    this.collisionType = collisionType;
  }

  protected GGCollisionArea clone()
  {
    return new GGCollisionArea(this);
  }
}
