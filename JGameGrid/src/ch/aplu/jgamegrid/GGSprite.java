// GGSprite.java

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
import java.awt.image.*;
import java.awt.geom.*;

/**
 * A sprite to be displayed on the screen. Note that a sprite
 * contains no state information, i.e. its just the image and 
 * not the location. This allows us to use a single sprite in
 * lots of different places without having to store multiple 
 * copies of the image.
 */
class GGSprite
{
  // The origial image
  private BufferedImage sourceImage;
  // The images to be drawn for this sprite
  private BufferedImage[] images = new BufferedImage[GameGrid.nbRotSprites];
  // The standard collision areas (when direction is 0)
  private GGCollisionArea[] collisionAreas = new GGCollisionArea[GameGrid.nbRotSprites];
  private GGInteractionArea[] interactionAreas = new GGInteractionArea[GameGrid.nbRotSprites];
  private boolean isRotatable;

  protected GGSprite(BufferedImage sourceImage, BufferedImage[] images,
    GGCollisionArea[] collisionAreas, GGInteractionArea[] interactionAreas)
  {
    this.sourceImage = sourceImage;
    for (int i = 0; i < GameGrid.nbRotSprites; i++)
    {
      this.images[i] = images[i];
      this.collisionAreas[i] = collisionAreas[i];
      this.interactionAreas[i] = interactionAreas[i];
    }
    if (images[1] == null)
      isRotatable = false;
    else
      isRotatable = true;
  }

  protected boolean isRotatable()
  {
    return isRotatable;
  }

  protected BufferedImage getSourceImage()
  {
    return sourceImage;
  }

  protected GGCollisionArea[] getCollisionAreas()
  {
    return collisionAreas;
  }

  protected GGInteractionArea[] getInteractionAreas()
  {
    return interactionAreas;
  }

  protected void setInteractionRectangle(Point center, int width, int height, boolean isRotatable)
  {
    if (isRotatable)
    {
      for (int i = 0; i < GameGrid.nbRotSprites; i++)
      {
        GGRectangle r = new GGRectangle(new GGVector(center), 0, width, height);
        r.rotate(Math.toRadians(360.0 / GameGrid.nbRotSprites * i));
        interactionAreas[i].setRectangle(r);
        interactionAreas[i].setCircle(null);
        interactionAreas[i].setInteractionType(InteractionType.RECTANGLE);
      }
    }
    else
    {
      GGRectangle r = new GGRectangle(new GGVector(center), 0, width, height);
      interactionAreas[0].setRectangle(r);
      interactionAreas[0].setCircle(null);
      interactionAreas[0].setInteractionType(InteractionType.RECTANGLE);
      for (int i = 1; i < GameGrid.nbRotSprites; i++)
      {
        interactionAreas[i].setRectangle(null);
        interactionAreas[i].setCircle(null);
        interactionAreas[i].setInteractionType(InteractionType.NONE);
      }
    }
  }

  protected void setInteractionCircle(GGCircle circle, boolean isRotatable)
  {
    if (isRotatable)
    {
      for (int i = 0; i < GameGrid.nbRotSprites; i++)
      {
        GGCircle c = circle.clone();
        c.center.rotate(Math.toRadians(360.0 / GameGrid.nbRotSprites * i));
        interactionAreas[i].setRectangle(null);
        interactionAreas[i].setCircle(c);
        interactionAreas[i].setInteractionType(InteractionType.CIRCLE);
      }
    }
    else
    {
      GGCircle c = circle.clone();
      interactionAreas[0].setRectangle(null);
      interactionAreas[0].setCircle(c);
      interactionAreas[0].setInteractionType(InteractionType.CIRCLE);

      for (int i = 1; i < GameGrid.nbRotSprites; i++)
      {
        interactionAreas[i].setRectangle(null);
        interactionAreas[i].setCircle(null);
        interactionAreas[i].setInteractionType(InteractionType.NONE);
      }
    }
  }

  protected void setInteractionImage(boolean isRotatable)
  {
    if (isRotatable)
    {
      for (int i = 0; i < GameGrid.nbRotSprites; i++)
      {
        interactionAreas[i].setRectangle(null);
        interactionAreas[i].setCircle(null);
        interactionAreas[i].setInteractionType(InteractionType.IMAGE);
      }
    }
    else
    {
      interactionAreas[0].setRectangle(null);
      interactionAreas[0].setCircle(null);
      interactionAreas[0].setInteractionType(InteractionType.IMAGE);

      for (int i = 1; i < GameGrid.nbRotSprites; i++)
      {
        interactionAreas[i].setRectangle(null);
        interactionAreas[i].setCircle(null);
        interactionAreas[i].setInteractionType(InteractionType.NONE);
      }
    }
  }

  protected void setCollisionRectangle(Point center, int width, int height, boolean isRotatable)
  {
    if (isRotatable)
    {
      for (int i = 0; i < GameGrid.nbRotSprites; i++)
      {
        GGRectangle r = new GGRectangle(new GGVector(center), 0, width, height);
        r.rotate(Math.toRadians(360.0 / GameGrid.nbRotSprites * i));
        collisionAreas[i].setRectangle(r);
        collisionAreas[i].setCircle(null);
        collisionAreas[i].setLine(null);
        collisionAreas[i].setSpot(null);
        collisionAreas[i].setCollisionType(CollisionType.RECTANGLE);
      }
    }
    else
    {
      GGRectangle r = new GGRectangle(new GGVector(center), 0, width, height);
      collisionAreas[0].setRectangle(r);
      collisionAreas[0].setCircle(null);
      collisionAreas[0].setLine(null);
      collisionAreas[0].setSpot(null);
      collisionAreas[0].setCollisionType(CollisionType.RECTANGLE);
      for (int i = 1; i < GameGrid.nbRotSprites; i++)
      {
        collisionAreas[i].setRectangle(null);
        collisionAreas[i].setCircle(null);
        collisionAreas[i].setLine(null);
        collisionAreas[i].setSpot(null);
        collisionAreas[i].setCollisionType(CollisionType.NONE);
      }
    }
  }

  protected void setCollisionCircle(GGCircle circle, boolean isRotatable)
  {
    if (isRotatable)
    {
      for (int i = 0; i < GameGrid.nbRotSprites; i++)
      {
        GGCircle c = circle.clone();
        c.center.rotate(Math.toRadians(360.0 / GameGrid.nbRotSprites * i));
        collisionAreas[i].setRectangle(null);
        collisionAreas[i].setCircle(c);
        collisionAreas[i].setLine(null);
        collisionAreas[i].setSpot(null);
        collisionAreas[i].setCollisionType(CollisionType.CIRCLE);
      }
    }
    else
    {
      GGCircle c = circle.clone();
      collisionAreas[0].setRectangle(null);
      collisionAreas[0].setCircle(c);
      collisionAreas[0].setLine(null);
      collisionAreas[0].setSpot(null);
      collisionAreas[0].setCollisionType(CollisionType.CIRCLE);

      for (int i = 1; i < GameGrid.nbRotSprites; i++)
      {
        collisionAreas[i].setRectangle(null);
        collisionAreas[i].setCircle(null);
        collisionAreas[i].setLine(null);
        collisionAreas[i].setSpot(null);
        collisionAreas[i].setCollisionType(CollisionType.NONE);
      }
    }
  }

  protected void setCollisionLine(GGLine line, boolean isRotatable)
  {
    if (isRotatable)
    {
      for (int i = 0; i < GameGrid.nbRotSprites; i++)
      {
        GGVector startVector = line.getStartVector();
        GGVector endVector = line.getEndVector();
        startVector.rotate(Math.toRadians(360.0 / GameGrid.nbRotSprites * i));
        endVector.rotate(Math.toRadians(360.0 / GameGrid.nbRotSprites * i));
        collisionAreas[i].setRectangle(null);
        collisionAreas[i].setCircle(null);
        collisionAreas[i].setLine(new GGLine(startVector, endVector));
        collisionAreas[i].setSpot(null);
        collisionAreas[i].setCollisionType(CollisionType.LINE);
      }
    }
    else
    {
      GGLine aLine = line.clone();
      collisionAreas[0].setRectangle(null);
      collisionAreas[0].setCircle(null);
      collisionAreas[0].setLine(aLine);
      collisionAreas[0].setSpot(null);
      collisionAreas[0].setCollisionType(CollisionType.LINE);

      for (int i = 1; i < GameGrid.nbRotSprites; i++)
      {
        collisionAreas[i].setRectangle(null);
        collisionAreas[i].setCircle(null);
        collisionAreas[i].setLine(null);
        collisionAreas[i].setSpot(null);
        collisionAreas[i].setCollisionType(CollisionType.NONE);
      }
    }
  }

  protected void setCollisionSpot(GGVector vSpot, boolean isRotatable)
  {
    if (isRotatable)
    {
      for (int i = 0; i < GameGrid.nbRotSprites; i++)
      {
        GGVector v = vSpot.clone();
        v.rotate(Math.toRadians(360.0 / GameGrid.nbRotSprites * i));
        collisionAreas[i].setRectangle(null);
        collisionAreas[i].setCircle(null);
        collisionAreas[i].setLine(null);
        collisionAreas[i].setSpot(v);
        collisionAreas[i].setCollisionType(CollisionType.SPOT);
      }
    }
    else
    {
      GGVector v = vSpot.clone();
      collisionAreas[0].setRectangle(null);
      collisionAreas[0].setCircle(null);
      collisionAreas[0].setLine(null);
      collisionAreas[0].setSpot(v);
      collisionAreas[0].setCollisionType(CollisionType.SPOT);

      for (int i = 1; i < GameGrid.nbRotSprites; i++)
      {
        collisionAreas[i].setRectangle(null);
        collisionAreas[i].setCircle(null);
        collisionAreas[i].setLine(null);
        collisionAreas[i].setSpot(null);
        collisionAreas[i].setCollisionType(CollisionType.NONE);
      }
    }
  }

  protected void setCollisionImage()
  {
    for (int i = 0; i < GameGrid.nbRotSprites; i++)
    {
      collisionAreas[i].setRectangle(null);
      collisionAreas[i].setCircle(null);
      collisionAreas[i].setLine(null);
      collisionAreas[i].setSpot(null);
      collisionAreas[i].setCollisionType(CollisionType.IMAGE);
    }
  }

  /**
   * Returns the width of the sprite with given id.
   * @param spriteId the id of the sprite
   * @return The width in pixels of this sprite
   */
  public int getWidth(int spriteId)
  {
    return images[spriteId].getWidth(null);
  }

  /**
   * Returns the width of the sprite with id = 0.
   * @return The width in pixels of this sprite
   */
  public int getWidth()
  {
    return getWidth(0);
  }

  /**
   * Returns the height of the sprite with given id.
   * @param spriteId the id of the sprite
   * @return The height in pixels of this sprite
   */
  public int getHeight(int spriteId)
  {
    return images[spriteId].getHeight(null);
  }

  /**
   * Returns the height of the sprite with id = 0.
   * @return The height in pixels of this sprite
   */
  public int getHeight()
  {
    return getHeight(0);
  }

  public BufferedImage[] getImages()
  {
    return images;
  }

  /**
   * Draws the sprite onto the graphics context provided.
   * @param g2D The graphics context on which to draw the sprite
   * @param x The x location in pixel coordinates at which to draw the sprite
   * @param y The y location in pixel coordinates at which to draw the sprite
   * @param rotationIndex the index of the rotated image
   */
  public void draw(Graphics2D g2D, int x, int y, int rotationIndex, boolean isHorzMirror, boolean isVertMirror)
  {
    // Somewhat optimized for speed and not for pretty code
    if (!isHorzMirror && !isVertMirror)
    {
      g2D.drawImage(images[rotationIndex], x, y, null);
      return;
    }

    AffineTransform at = new AffineTransform();
    if (isHorzMirror && !isVertMirror)
    {
      at.scale(-1, 1);
      at.translate(-x - getWidth(), y);
    }
    if (!isHorzMirror && isVertMirror)
    {
      at.scale(1, -1);
      at.translate(x, -y - getHeight());
    }
    if (isHorzMirror && isVertMirror)
    {
      at.scale(-1, -1);
      at.translate(-x - getWidth(), -y - getHeight());
    }
    g2D.drawImage(images[rotationIndex], at, null);
  }

}
