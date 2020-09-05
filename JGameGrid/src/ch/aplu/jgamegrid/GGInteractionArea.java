// GGInteractionArea.java

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


enum InteractionType {NONE, RECTANGLE, CIRCLE, IMAGE};

class GGInteractionArea
{
  private GGRectangle rectangle = null;
  private GGCircle circle = null;
  private InteractionType interactionType;

  protected GGInteractionArea(GGRectangle rectangle, GGCircle circle,
    InteractionType interactionType)
  {
    this.rectangle = rectangle;
    this.circle = circle;
    this.interactionType = interactionType;
  }

  protected GGInteractionArea(GGInteractionArea area)
  {
    rectangle = area.rectangle;
    circle = area.circle;
    interactionType = area.interactionType;
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
   * Returns the interactionType.
   * @return the type of the interaction detection
   */
  protected InteractionType getInteractionType()
  {
    return interactionType;
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
   * Sets the type of interaction.
   * @param the interaction type
   */
  protected void setInteractionType(InteractionType interactionType)
  {
    this.interactionType = interactionType;
  }

  protected GGInteractionArea clone()
  {
    return new GGInteractionArea(this);
  }
}
