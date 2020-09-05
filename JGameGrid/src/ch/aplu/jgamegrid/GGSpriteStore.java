// GGSpriteStore.java

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

/**
 * A resource manager for sprites in the game. The sprite is identified via
 * its image path. When it is requested the first time, the image is loaded and
 * the image path/instance reference pair store in a Hash map. When request again,
 * the instance reference is retrieved from the Hash map (idea from Kevin Glass).
 */
class GGSpriteStore
{

  /* The single instance of this class */
  private static GGSpriteStore single = new GGSpriteStore();

  /* The cached sprite map, from reference to sprite instance */
  private HashMap<String, GGSprite> sprites = new HashMap<String, GGSprite>();

  /**
   * Returns the single instance of this class.
   * @return The single instance of this class
   */
  protected static GGSpriteStore get()
  {
    return single;
  }

  /**
   * Retrieves a rotatable sprite from the store.
   * If the sprite is not yet in the store, it is loaded from the given disk file.
   * @param imagePath the fully qualified path to the image to use for the sprite
   * @return the sprite instance reference containing the accelerated image
   */
  protected GGSprite getSprite(String imagePath)
  {
    return getSprite(imagePath, false, null);
  }

  /**
   * Retrieves a sprite from the store.
   * If isRotatable is false, only the default sprite image is stored.
   * This saves a lot of memory compared to a rotatable sprite.
   * If the sprite is not yet in the store, it is loaded from the given disk file.
   * @param imagePath the fully qualified path to the image to use for the sprite
   * @param isRotatable if true, a rotated actor's image is stored for every 360 / 60 = 6 degrees
   * @return the sprite instance reference containing the accelerated image
   */
  protected synchronized GGSprite getSprite(String imagePath, boolean isRotatable,
    BufferedImage spriteImage)
  {
    // if we've already got the sprite in the cache
    // then just return the existing version
    // Sprites created from BufferedImages are not stored in the buffer
    // because the passed buffer should be released when the actor runs
    // out of scope
    if (spriteImage == null)  // not from BufferedImage
    {
      GGSprite s = sprites.get(imagePath);
      if (s != null && // found in store
        ((!s.isRotatable() && !isRotatable) || // old sprite not rotatable and 
        // new one not rotatable -> ok
        s.isRotatable()))  // Old sprite is rotatable
        // new on rotatable or not -> ok 
        return s;
    }

    BufferedImage sourceImage = null;
    if (spriteImage != null)
      sourceImage = spriteImage;
    else
    {
      sourceImage = GGBitmap.getImage(imagePath);
      if (sourceImage == null)
        GameGrid.fail("Error while loading sprite image\n" + imagePath
          + "\nApplication will terminate.");
    }

    int w = sourceImage.getWidth();
    int h = sourceImage.getHeight();

    // create an accelerated image of the right size to store our sprite in
    GraphicsConfiguration gc =
      GraphicsEnvironment.getLocalGraphicsEnvironment().
      getDefaultScreenDevice().getDefaultConfiguration();

    BufferedImage[] bi = new BufferedImage[GameGrid.nbRotSprites];
    GGCollisionArea[] collisionAreas = new GGCollisionArea[GameGrid.nbRotSprites];
    GGInteractionArea[] interactionAreas = new GGInteractionArea[GameGrid.nbRotSprites];

    if (isRotatable)
    {
      int s = (int)Math.ceil(Math.sqrt(w * w + h * h));
      BufferedImage biTmp = gc.createCompatibleImage(s, s, Transparency.TRANSLUCENT);
      Graphics2D gTmp = biTmp.createGraphics();
      gTmp.drawImage(sourceImage, (s - w) / 2, (s - h) / 2, null);

      for (int i = 0; i < GameGrid.nbRotSprites; i++)
      {
        bi[i] = gc.createCompatibleImage(s, s, Transparency.TRANSLUCENT);
        Graphics2D g2D = bi[i].createGraphics();

        g2D.translate(s / 2, s / 2); // Translate the coordinate system (zero a image's center)
        g2D.rotate(Math.toRadians(360.0 / GameGrid.nbRotSprites * i));  // Rotate the image
        g2D.translate(-s / 2, -s / 2); // Translate the coordinate system (zero a image's center)
        g2D.drawImage(biTmp, 0, 0, null);
        g2D.dispose();

        // Create default collision areas for all sprite images
        // image with w x h pixels is inly (w-1) x (h-1) wide
        GGRectangle rect = new GGRectangle(new GGVector(0, 0),
          Math.toRadians(360.0 / GameGrid.nbRotSprites * i), w - 1, h - 1);
        collisionAreas[i] = new GGCollisionArea(rect, null, null, null, CollisionType.RECTANGLE);
        interactionAreas[i] = new GGInteractionArea(null, null, InteractionType.IMAGE);
      }
      gTmp.dispose();
    }
    else
    {
      bi[0] = gc.createCompatibleImage(
        sourceImage.getWidth(),
        sourceImage.getHeight(),
        Transparency.TRANSLUCENT);

      Graphics2D g2D = bi[0].createGraphics();
      g2D.drawImage(sourceImage, 0, 0, null);
      g2D.dispose();

      // Create default collision area
      // image with w x h pixels is inly (w-1) x (h-1) wide
      GGRectangle rect = new GGRectangle(new GGVector(0, 0), 0, w - 1, h - 1);
      collisionAreas[0] = new GGCollisionArea(rect, null, null, null, CollisionType.RECTANGLE);
      interactionAreas[0] = new GGInteractionArea(null, null, InteractionType.IMAGE);
      for (int i = 1; i < GameGrid.nbRotSprites; i++)
      {
        bi[i] = null;
        collisionAreas[i] = new GGCollisionArea(null, null, null, null, CollisionType.NONE);
        interactionAreas[i] = new GGInteractionArea(null, null, InteractionType.NONE);
      }

    }

    // create a sprite, add it to the cache then return it
    GGSprite sprite = new GGSprite(sourceImage, bi, collisionAreas, interactionAreas);

    if (spriteImage == null)
      sprites.put(imagePath, sprite);
    return sprite;
  }

  protected synchronized void removeFromStore(String key)
  {
    sprites.remove(key);
  }
}
 
