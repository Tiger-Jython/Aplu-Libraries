// Actor.java

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

import java.awt.image.*;
import java.util.ArrayList;
import java.awt.*;
import java.awt.geom.*;

/**
 * Class that holds sprite icons that plays the role of an actor in the
 * game's game grid. The actor's image may only be positioned in the center of cells.
 * (for even pixel unit cells/and or even pixel unit images, some rounding is necessary.)
 * For one pixel unit cells, the actor image is positioned at the pixel coordinates.
 * An actor may contain more than one sprite. This is useful if the appearence of the actor
 * should change, while remaining the same actor (walking, speaking, flying, etc.).
 * The sprites are referenced by an integer sprite id running from 0 up.
 * All sprites of an actor are positioned at the same location with the
 * same direction. At all times only one (or none) of the sprites is visible.
 * If an actor is declared rotatable, the sprite images are rotated when the actor 
 * direction changes. To speed-up execution the images are preloaded
 * into the Java heap when the actor is created. By default there are 60 different
 * rotation images giving an angle resolution of  360 / 60 = 6 degrees.
 * (This number may be changed using a special GameGrid constructor.)
 * If there are many rotatable sprites with big images, it may be necessary
 * to increase the heap space by using<br><br>
 * java -Xms<initial heap size> -Xmx<maximum heap size><br>
 * Defaults are: java -Xms32m -Xmx128m<br>
 * You can set this either in the Java Control Panel or on the command line,
 * depending on the environment you run your application.<br><br>
 * Actors are partially active objects, animated by the same thread that runs the simulation loop. In
 * every simulation cycle, the actor's act() method is called, where the user performs the
 * desired action. This concept has the advantage that act() is called in a strict order
 * (that can be modified) and by the same thread. This avoids concurrency problems that is an issue
 * with fully active object where each object has its own thread. As a drawback all actors must
 * be cooperative: act() must return quickly in order to give other actors the chance to act.
 * If actors should not act synchronously, the execution may be skipped for
 * a given number of simulation cycles by using individually setSlowDown()<br><br>
 *
 * A advanced collision detection mechanism for collisions betweeen actors or
 * between an actor an a tile of a tile map is implemented.
 * The collision may be detected using collision rectangles, collision circles,
 * collision spots and for some collision types collisions of non-transparent
 * image pixels. Collision rectangles, circles and spots may be placed
 * at any desired position (rectangles and circles with specified size) relative
 * to the sprite image. The collision area rotates when the sprites rotates.
 * Much effort is investigated for a high speed implementation
 * of the collision detection, using the SAT (Separating Axis Theorem)
 * for rotated rectangle intersection.<br>
 * To speed up the collision system, each actor defines actors or tiles that are
 * candidates for collisions. Collisions are considered to be events and are
 * notified by a registrated GGActorCollisionListener or a registrated
 * GGTileCollisionListener<br><br>
 * The collision check is performed in every simulation cycle and the collision
 * event callback invoked by the game thread after actor's act() invocation. The
 * default collision area is a rectangle that fits the actor image. If performance
 * is an issue, use circle-circle collision type.<br><br>
 *
 * Collision areas are considered to be part of the sprite image and stored as
 * such. Because actors with the same sprite image have the same collision area,
 * modifying a collision area effects all actors with the same sprite.<br><br>
 *
 * Most methods are only valid after the actor has been added to the GameGrid.
 * At this time reset() is called. So perform your own initialization code 
 * by overriding reset() or in the first act() cycle.<br><br>
 *
 * All public methods are synchronized to make them thread-safe. 
 */
public class Actor implements GGBorderListener,
  GGActorCollisionListener, GGTileCollisionListener
{
  // ----------------- Inner class MyGGMouseAdapter ---------------
  class MyGGMouseAdapter implements GGMouseListener
  {
    private Actor actor;
    private boolean isMove;
    private boolean isEnter;
    private boolean isLeave;
    private boolean wasInside = false;
    private boolean isPressed = false;
    private Point oldPt = new Point();

    MyGGMouseAdapter(Actor actor, boolean isMove, boolean isEnter,
      boolean isLeave)
    {
      this.actor = actor;
      this.isMove = isMove;
      this.isEnter = isEnter;
      this.isLeave = isLeave;
    }

    public boolean mouseEvent(GGMouse mouse)
    {
      if (gameGrid == null
        || !isMouseTouchEnabled
        || idVisible < 0)
        return false;
      Point pt = new Point(mouse.getX(), mouse.getY());

      GGVector v = new GGVector(pt);
      v = v.sub(new GGVector(locationOffset));
      Point pix = new Point();
      boolean intersects = false;

      switch (getCurrentInteractionType(idVisible))
      {
        case RECTANGLE:
          intersects = getCurrentInteractionRectangle(idVisible).
            isIntersecting(v, isRotatable, pix);
          break;
        case CIRCLE:
          intersects = getCurrentInteractionCircle(idVisible).
            isIntersecting(v, pix);
          break;
        case IMAGE:
          intersects = v.isIntersecting(actor.getCurrentImageCenter(idVisible),
            actor.getCurrentImageDirection(idVisible),
            actor.getCurrentImage(idVisible),
            actor.isRotatable(), pix);
          break;
      }

      int evt = mouse.getEvent();

      // If mouse moves outside the image after being pressed inside, a
      // mouse release must be reported anyway. But reports pix = null.
      boolean isRelease = isPressed
        && ((evt & GGMouse.lRelease) != 0 || (evt & GGMouse.rRelease) != 0);
      if (mouseTouchListener != null && isRelease)
      {
        mouseTouchListener.mouseTouched(actor,
          GGMouse.create(this, evt, pt.x, pt.y),
          intersects ? pix : new Point(-1, -1));
        isPressed = false;
        return false;
      }

      boolean isInside = false;

      if (intersects)
      {
        if (onTopOnly)
        {
          // All touched actors
          ArrayList<Actor> touchedActors = gameGrid.getTouchedActors(null);
          int nbTouchedActor = touchedActors.size();

          if (nbTouchedActor > 1)  // Check if is on top
          {
            if (gameGrid == null) // Already removed
              return false;
            // Find touched actor that is painted last (on top)
            ArrayList<Actor> list = gameGrid.getPaintOrderList();
            int index = -1;
            for (Actor touched : touchedActors)
            {
              int i = list.indexOf(touched);
              if (i > index)
                index = i;
            }
            if (list.get(index) != actor)  // we are not on top
            {
              //           System.out.println("not on top or ");
              return false;
            }
          }
        }

        oldPt.x = pt.x;
        oldPt.y = pt.y;
        isInside = true;
        if ((evt & GGMouse.move) != 0)
        {
          if (isMove && mouseTouchListener != null)
            mouseTouchListener.mouseTouched(actor,
              GGMouse.create(this, GGMouse.move, pt.x, pt.y),
              pix);
        }
        else
        {
          if (mouseTouchListener != null)
          {
            if (((evt & GGMouse.lPress) != 0 || (evt & GGMouse.rPress) != 0)
              && intersects)
              isPressed = true;
            if ((evt & GGMouse.lRelease) != 0 || (evt & GGMouse.rRelease) != 0)
              return false;  // release events are reported above
            if (((evt & GGMouse.lDrag) != 0 || (evt & GGMouse.rDrag) != 0)
              && !isPressed)
              return false;
            if ((evt & GGMouse.lPress) != 0 && !isMouseLPressRegistered)  // Press not registered 
              return false;
            if ((evt & GGMouse.rPress) != 0 && !isMouseRPressRegistered)  // Press not registered 
              return false;

            mouseTouchListener.mouseTouched(actor,
              GGMouse.create(this, evt, pt.x, pt.y),
              pix);
          }
        }
      }

      if (isInside && !wasInside)
      {
        if (isEnter && mouseTouchListener != null)
          mouseTouchListener.mouseTouched(actor,
            GGMouse.create(this, GGMouse.enter, pt.x, pt.y),
            pix);
      }

      if (!isInside && wasInside)
      {
        if (isLeave && mouseTouchListener != null)
          mouseTouchListener.mouseTouched(actor,
            GGMouse.create(this, GGMouse.leave, pt.x, pt.y),
            new Point(oldPt.x, oldPt.y));
      }

      wasInside = isInside;
      return false;
    }
  }
  // ----------------- End of inner class -------------------------
  //
  /**
   * The reference to the GameGrid instance.
   * Null if the actor is not yet added to the game grid. If it was added and
   * removed after, gameGrid is still valid.
   */
  public GameGrid gameGrid = null;
  //
  /**
   * The current number of simulation cycles since last reset.
   */
  public int nbCycles;
  //
  private int nbSprites;
  private final int gridLimit = 2500; // Pixelnumber limit from small to big window
  private Location location = new Location(0, 0);
  private Location startLocation = new Location(0, 0);
  private GGSprite[] sprites;
  private double direction = 0;
  private int rotationIndex = 0;
  private double startDirection = 0;
  private int[] imageWidths;
  private int[] imageHeights;
  private BufferedImage[][] bufferedImages;
  private GGBorderListener borderListener;
  private GGActorCollisionListener actorCollisionListener = null;
  private GGTileCollisionListener tileCollisionListener = null;
  private boolean isRotatable;
  private int stepCountStart = 1;
  private int stepCount;
  private int idVisible = 0;
  private int idVisibleStart = 0;
  private ArrayList<Actor> collisionActors = new ArrayList<Actor>();
  private ArrayList<Location> collisionTiles = new ArrayList<Location>();
  private int actorSimCount;
  private int tileSimCount;
  private boolean isHorzMirror = false;
  private boolean isVertMirror = false;
  private boolean isActEnabled = true;
  private boolean isActorCollisionEnabled = true;
  private boolean isTileCollisionEnabled = true;
  private Point locationOffset = new Point(0, 0);
  private MyGGMouseAdapter mouseAdapter;
  private GGMouseTouchListener mouseTouchListener = null;
  private int mouseMask = 0;
  private boolean isMouseTouchEnabled = true;
  private boolean onTopOnly;
  private boolean isRemoved = false;
  private double xDouble;
  private double yDouble;
  private boolean isMouseLPressRegistered;
  private boolean isMouseRPressRegistered;

  protected Actor(boolean isRotatable, String filename, int nbSprites, int nbRotSprites)
  {
    String[] imagePaths = new String[1];
    imagePaths[0] = filename;
    GameGrid.nbRotSprites = nbRotSprites;
    init(isRotatable, imagePaths, nbSprites, null);
  }

  /**
   * Constructs an unrotatable actor based on one or several sprite images
   * defined by the given buffered images.
   * The spriteId starts from 0 and corresponds to the order of the given images.
   * (Used to create an actor based on images defined on runtime.)
   * @param spriteImages references to BufferedImages that contains the sprite images
   */
  public Actor(BufferedImage... spriteImages)
  {
    this(false, spriteImages);
  }

  /**
   * Constructs an unrotatable actor based on the specified sprite image.
   * (Used to create an actor based on images defined on runtime.)
   * @param spriteImage references to BufferedImage that contains the sprite image
   */
  public Actor(BufferedImage spriteImage)
  {
    this(false, spriteImage);
  }

  /**
   * Constructs actor based on the specified buffered image.
   * (Used to create an actor based on an image defined on runtime.)
   * If isRotatable is true, the actor is rotated when the direction changes.
   * @param isRotatable if true, the actor's image may be rotated when the direction changes
   * @param spriteImage reference to a BufferedImage that contains the sprite image
   */
  public Actor(boolean isRotatable, BufferedImage spriteImage)
  {
    String[] imagePaths = new String[1];
    imagePaths[0] = "";
    BufferedImage[] spriteImages = new BufferedImage[1];
    spriteImages[0] = spriteImage;
    init(isRotatable, imagePaths, 1, spriteImages);
  }

  /**
   * Constructs an actor based on one several sprite images defined by the given
   * buffered images.
   * The spriteId starts from 0 and corresponds to the order of the given images.
   * (Used to create an actor based on images defined on runtime.)
   * If isRotatable is true, the actor is rotated when the direction changes.
   * @param isRotatable if true, the actor's image may be rotated when the direction changes
   * @param spriteImages references to BufferedImages that contains the sprite images
   */
  public Actor(boolean isRotatable, BufferedImage... spriteImages)
  {
    String[] imagePaths = new String[1];
    imagePaths[0] = "";
    init(isRotatable, imagePaths, spriteImages.length, spriteImages);
  }

  /**
   * Constructs an actor with no sprite image. Sometimes this is useful for
   * dummy actors that are not shown but act only.
   */
  public Actor()
  {
    init(false, null, 0, null);
    idVisible = -1;
  }

  /**
   * Constructs an unrotatable actor based on the specified sprite image.<br>
   * From the given filename the image file is searched in the following order:<br>
   * - if application is packed into a jar archive, relative to the root of the jar archive<br>
   * - relative to the directory &lt;userhome&gt;/gamegrid/<br>
   * - relative or absolute to current application directory<br>
   * - if filename starts with http://, from the given URL<br>
   * - imagePath prefixed with _ and searched relative to the root of the jar archive (used for sprite library distribution)<br>
   * @param filename the path or URL to the image file displayed for this actor.
   */
  public Actor(String filename)
  {
    String[] imagePaths = new String[1];
    imagePaths[0] = filename;
    init(false, imagePaths, 1, null);
  }

  /**
   * Constructs an unrotatable actor based on several sprite images.
   * The spriteId starts from 0 and corresponds to the order of the given filenames.
   * From the given filenames the image files are searched in the following order:<br>
   * - if application is packed into a jar archive, relative to the root of the jar archive<br>
   * - relative to the directory &lt;userhome&gt;/gamegrid/<br>
   * - relative or absolute to current application directory<br>
   * - if filename starts with http://, from the given URL<br>
   * - imagePath prefixed with _ and searched relative to the root of the jar archive (used for sprite library distribution)
   * @param filenames the paths or URLs (one or more) to the image files displayed for this actor.
   */
  public Actor(String... filenames)
  {
    init(false, filenames, filenames.length, null);
  }

  /**
   * Constructs an actor based on the specified sprite image.
   * If isRotatable is true, the actor is rotated when the direction changes.<br>
   * From the given filename the image file is searched in the following order:<br>
   * - if application is packed into a jar archive, relative to the root of the jar archive<br>
   * - relative to the directory &lt;userhome&gt;/gamegrid/<br>
   * - relative or absolute to current application directory<br>
   * - if filename starts with http://, from the given URL<br>
   * - imagePath prefixed with _ and searched relative to the root of the jar archive (used for sprite library distribution)
   * @param isRotatable if true, the actor's image may be rotated when the direction changes
   * @param filename the path to the image file displayed for this actor
   */
  public Actor(boolean isRotatable, String filename)
  {
    String[] imagePaths = new String[1];
    imagePaths[0] = filename;
    init(isRotatable, imagePaths, 1, null);
  }

  /**
   * Constructs an actor based on one several sprite images.
   * If isRotatable is true, the actor is rotated when the direction changes.
   * The spriteId starts from 0 and corresponds to the order of the given filenames.
   * From the given filenames the image files are searched in the following order:<br>
   * - if application is packed into a jar archive, relative to the root of the jar archive<br>
   * - relative to the directory &lt;userhome&gt;/gamegrid/<br>
   * - relative or absolute to current application directory<br>
   * - if filename starts with http://, from the given URL<br>
   * - imagePath prefixed with _ and searched relative to the root of the jar archive (used for sprite library distribution)
   * @param isRotatable if true, the actor's image may be rotated when the direction changes
   * @param filenames the paths or URLs to the image files displayed for this actor
   */
  public Actor(boolean isRotatable, String... filenames)
  {
    init(isRotatable, filenames, filenames.length, null);
  }

  /**
   * Constructs an unrotatable actor based on one or more sprite images.
   * The actor may contain more than one sprite images, if nbSprites > 1
   * the filenames of these images are automatically generated in a sequence
   * filename_0.ext, filename_1.ext, ...<br>
   * From the given filename the image file is searched in the following order:<br>
   * - if application is packed into a jar archive, relative to the root of the jar archive<br>
   * - relative to the directory &lt;userhome&gt;/gamegrid/<br>
   * - relative or absolute to current application directory<br>
   * - if filename starts with http://, from the given URL<br>
   * - imagePath prefixed with _ and searched relative to the root of the jar archive (used for sprite library distribution)
   * @param filename the fully qualified path to the image file displayed for this actor
   * @param nbSprites the number of sprite images for the same actor
   */
  public Actor(String filename, int nbSprites)
  {
    String[] imagePaths = new String[1];
    imagePaths[0] = filename;
    init(false, imagePaths, nbSprites, null);
  }

  /**
   * Constructs an actor based on one or more sprite images.
   * If isRotatable is true, the actor is rotated when the direction changes.
   * The actor may contain more than one sprite images, if nbSprites > 1
   * the filenames of these images are automatically generated in a sequence
   * filename_0.ext, filename_1.ext, ...<br>
   * From the given filename the image file is searched in the following order:<br>
   * - if application is packed into a jar archive, relative to the root of the jar archive<br>
   * - relative to the directory &lt;userhome&gt;/gamegrid/<br>
   * - relative or absolute to current application directory<br>
   * - if filename starts with http://, from the given URL<br>
   * - imagePath prefixed with _ and searched relative to the root of the jar archive (used for sprite library distribution)
   * @param isRotatable if true, the actor's image may be rotated when the direction changes
   * @param filename the path or URL to the image file displayed for this actor
   * @param nbSprites the number of sprite images for the same actor
   */
  public Actor(boolean isRotatable, String filename, int nbSprites)
  {
    String[] imagePaths = new String[1];
    imagePaths[0] = filename;
    init(isRotatable, imagePaths, nbSprites, null);
  }

  private void init(boolean isRotatable, String[] imagePaths,
    int nbSprites, BufferedImage[] spriteImages)
  {
    if (nbSprites < 0)
      nbSprites = 0;
    nbCycles = 0;

    this.nbSprites = nbSprites;
    if (nbSprites > 0)
    {
      sprites = new GGSprite[nbSprites];
      bufferedImages = new BufferedImage[nbSprites][];
      imageHeights = new int[nbSprites];
      imageWidths = new int[nbSprites];

      for (int i = 0; i < nbSprites; i++)
      {
        String path = "";
        if (nbSprites == 1)
          path = imagePaths[0];
        else
        {
          if (imagePaths.length == 1)  // automatic generation of filenames
          {
            int index = imagePaths[0].indexOf('.');
            if (index != -1)
              path = imagePaths[0].substring(0, index) + "_" + i + imagePaths[0].substring(index);
            else
              path = imagePaths[0] + "_" + i;
          }
          else
            path = imagePaths[i];
        }

        if (spriteImages == null)
          sprites[i] = GGSpriteStore.get().getSprite(path, isRotatable, null);
        else
          sprites[i] = GGSpriteStore.get().getSprite(path, isRotatable, spriteImages[i]);
        bufferedImages[i] = sprites[i].getImages();
        imageHeights[i] = sprites[i].getHeight();
        imageWidths[i] = sprites[i].getWidth();
      }

      this.isRotatable = isRotatable;
    }

    initStepCount();

    // Empty implementations of callback methods
    borderListener = this;
    actorCollisionListener = this;
    tileCollisionListener = this;
  }

  /**
   * Sets a pixel offset in x- any y-direction relative to the current
   * location. Effects the display position and the mouseTouchArea only.
   * All location values (e.g. getLocation(), collisionAreas, etc.) are unchanged.<br>
   * Used to fine tune the displayed sprite position in a coarse game grid.
   * @param locationOffset x,y displacement (x to the left, y downwards)
   */
  public synchronized void setLocationOffset(Point locationOffset)
  {
    this.locationOffset = new Point(locationOffset.x, locationOffset.y);
  }

  /**
   * Same as setLoctionOffset(Point locationOffset) with given x and
   * y displacements.
   * @param x displacement (positive x to the left)
   * @param y displacement (positive y to downwards)
   */
  public synchronized void setLocationOffset(int x, int y)
  {
    this.locationOffset = new Point(x, y);
  }

  /**
   * Returns the pixel offset in x- and y-direction relative to the current
   * location.
   * @return x, y displacment
   */
  public synchronized Point getLocationOffset()
  {
    return new Point(locationOffset.x, locationOffset.y);
  }

  protected void setGameGrid(GameGrid gameGrid)
  {
    this.gameGrid = gameGrid;
  }

  /**
   * Returns the GGBackground reference of the actor's game grid.
   * Requires that the actor has been added to the game grid.
   * @return the GGBackground reference of the actor's game grid.
   * If the actor is not yet added to the game grid,
   * displays an error and terminates application.
   */
  public synchronized GGBackground getBackground()
  {
    if (gameGrid == null)
      GameGrid.fail("Error in Actor.getBackground()."
        + "\nActor not part of of game grid."
        + "\nApplication will terminate.");
    return gameGrid.getBg();
  }

  /**
   * Assigns a new current horizontal cell coordinate.
   * The location may be outside the range of the cells inside the game grid.
   * Triggers a border event, if the location is near the border.
   * @param x the x-coordinate (cell index)
   */
  public synchronized void setX(int x)
  {
    location.x = x;
    notifyBorder();
  }

  /**
   * Assigns a new current vertical cell coordinate.
   * The location may be outside the range of the cells inside the game grid.
   * Triggers a border event, if the location is near the border.
   * @param y the y-coordinate (cell index)
   */
  public synchronized void setY(int y)
  {
    location.y = y;
    notifyBorder();
  }

  /**
   * Assigns a new current location.
   * Triggers a border event, if the location is near the border.
   * The location may be outside the range of the cells inside the game grid.
   * @param location the location (value copy)
   */
  public synchronized void setLocation(Location location)
  {
    if (location == null)
      GameGrid.fail("Error in Actor.setLocation()."
        + "\nParameter location should not be null."
        + "\nApplication will terminate.");

    this.location.x = location.x;
    this.location.y = location.y;
    notifyBorder();
  }

  /**
   * Moves the center of the actor to the given pixel coordinates.
   * Sets the actor location to the cell that contains the given point
   * (if outside the game grid, the nearest location in the grid is used)
   * The location offset is set to show the picture center at the given point.
   * Pixel coordinate system: origin at the left upper vertex of
   * the game grid window, x-axis to the left, y-axis downwards.<br><br>
   * 
   * Keep in mind that the location offset is an actor attribute that may
   * cause side effects and only effects the display position and 
   * the mouseTouchArea (collisionAreas are unchanged).
   * @param imageCenter the pixel coordinates of the image center
   */
  public void setPixelLocation(Point imageCenter)
  {
    if (gameGrid == null)
      GameGrid.fail("Error in Actor.setPixelLocation()."
        + "\nActor not part of of game grid."
        + "\nApplication will terminate.");
    Location cellLoc = gameGrid.toLocationInGrid(imageCenter);
    Point cellCenter = gameGrid.toPoint(cellLoc);
    Point offset
      = new Point(imageCenter.x - cellCenter.x, imageCenter.y - cellCenter.y);
    setLocation(cellLoc);
    setLocationOffset(offset);
  }

  private void notifyBorder()
  {
    if (gameGrid != null && gameGrid.isAtBorder(location))
      borderListener.nearBorder(this, location.clone());
  }

  /**
   * Returns the current horizontal coordinate.
   * @return the x-coordinate (cell index)
   */
  public synchronized int getX()
  {
    if (gameGrid == null)
      GameGrid.fail("Error in Actor.getX()."
        + "\nActor not part of of game grid."
        + "\nApplication will terminate.");
    return location.x;
  }

  /**
   * Returns the current vertical coordinate.
   * @return the y-coordinate (cell index)
   */
  public synchronized int getY()
  {
    if (gameGrid == null)
      GameGrid.fail("Error in Actor.getY()."
        + "\nActor not part of of game grid."
        + "\nApplication will terminate.");
    return location.y;
  }

  /**
   * Returns the current location (horizontal and vertical coordinates).
   * @return a clone of the current location (cell indices)
   */
  public synchronized Location getLocation()
  {
    if (gameGrid == null)
      GameGrid.fail("Error in Actor.getLocation()."
        + "\nActor not part of of game grid."
        + "\nApplication will terminate.");
    return location.clone();
  }

  protected void initStart()
  {
    if (isRemoved)
    {
      isRemoved = false;
      idVisible = 0;
      idVisibleStart = 0;
    }
    else
      idVisibleStart = idVisible;

    startLocation = location.clone();
    startDirection = direction;
    xDouble = gameGrid.toPoint(startLocation).x;
    yDouble = gameGrid.toPoint(startLocation).y;
    if (mouseMask < 0)
      gameGrid.addMouseListener(mouseAdapter, -mouseMask);
  }

  /**
   * Returns the start location (horizontal and vertical coordinates).
   * @return a clone of the location when the actor was added to the game grid
   */
  public synchronized Location getLocationStart()
  {
    if (gameGrid == null)
      GameGrid.fail("Error in Actor.getLocationStart()."
        + "\nActor not part of of game grid."
        + "\nApplication will terminate.");
    return startLocation.clone();
  }

  /**
   * Returns the x-coordinate of the start location.
   * @return the x-coordinate when the actor was added to the game grid
   */
  public synchronized int getXStart()
  {
    if (gameGrid == null)
      GameGrid.fail("Error in Actor.getXStart()."
        + "\nActor not part of of game grid."
        + "\nApplication will terminate.");
    return startLocation.x;
  }

  /**
   * Returns the y-coordinate of the start location.
   * @return the y-coordinate when the actor was added to the game grid
   */
  public synchronized int getYStart()
  {
    if (gameGrid == null)
      GameGrid.fail("Error in Actor.getYStart()."
        + "\nActor not part of of game grid."
        + "\nApplication will terminate.");
    return startLocation.y;
  }

  /**
   * Returns the start direction.
   * @return the start direction when the actor was added to the game grid
   */
  public synchronized double getDirectionStart()
  {
    if (gameGrid == null)
      GameGrid.fail("Error in Actor.getDirectionStart()."
        + "\nActor not part of of game grid."
        + "\nApplication will terminate.");
    return startDirection;
  }

  /**
   * Sets the moving direction.
   * @param direction the angle for the next movement (in degrees clockwise, 0 to east)
   */
  public synchronized void setDirection(double direction)
  {
    direction = direction % 360;
    if (direction < 0)
      direction = 360 + direction;
    this.direction = direction;
    if (isRotatable)
      rotationIndex
        = ((int)(GameGrid.nbRotSprites * direction / 360.0 + 0.5)) % GameGrid.nbRotSprites;
  }

  /**
   * Sets the moving direction to the given compass direction.
   * @param compassDir the compass dirction for the next movement
   */
  public synchronized void setDirection(Location.CompassDirection compassDir)
  {
    setDirection(compassDir.getDirection());
  }

  /**
   * Returns the current rotation index for rotatable actors.
   * By default the rotation index is within 0..59, because 
   * there are 60 different rotation images  giving an angle 
   * resolution of  360 / 60 = 6 degrees.
   * (This number may be changed using a special GameGrid constructor.)
   * @return the rotation index (for non-rotatable actors always 0)
   */
  public synchronized int getRotationIndex()
  {
    return rotationIndex;
  }

  /**
   * Gets the current direction.
   * @return the direction for the next movement (in degrees clockwise, 0 to east)
   */
  public synchronized double getDirection()
  {
    return direction;
  }

  /**
   * Gets the current direction rounded to the next integer.
   * @return the direction for the next movement (in degrees clockwise, 0 to east)
   */
  public synchronized int getIntDirection()
  {
    return (int)Math.round(direction);
  }

  protected void draw(Graphics2D g2D, int id)
  {
    if (SharedConstants.DEBUG == SharedConstants.DEBUG_LEVEL_HIGH)
      System.out.println("calling Actor.draw()");

    if (gameGrid == null)
      return;

    int cellSize = gameGrid.getCellSize();
    if (cellSize > 0)
    {
      int ulx = locationOffset.x + cellSize / 2 + location.x * cellSize
        - imageWidths[id] / 2 + (cellSize == 1 ? 1 : 0);
      int uly = locationOffset.y + cellSize / 2 + location.y * cellSize
        - imageHeights[id] / 2 + (cellSize == 1 ? 1 : 0);

      sprites[id].draw(g2D, ulx, uly, rotationIndex,
        isHorzMirror, isVertMirror);
    }
  }

  /**
   * Returns the target location of the next move().
   * For a small grid (total number of cells <= 2500 = 50 * 50)
   * the target location is one of 8 neighbour cells in the current direction (compass directions 45 degrees wide).
   * otherwise it is a cell location about 5 cell sizes away in the current direction.
   */
  public synchronized Location getNextMoveLocation()
  {
    if (getNbHorzCells() * getNbVertCells() <= gridLimit)   // Small grid
      return location.getNeighbourLocation(direction);
    else
      return location.getAdjacentLocation(direction);
  }

  /**
   * For a small grid (total number of cells <= 2500 = 50 * 50)
   * moves to one of 8 neighbour cells in the current direction (compass directions 45 degrees wide).
   * otherwise moves to a cell about 5 cell sizes away in the current direction.<br>
   * Keep in mind that successive calls of move() in the same direction does not 
   * necessarily move the actor on a straight line. If you want to move the
   * actor on a line, use displace() or declare global double (float) x and
   * y coordinates and increase them in each step with <br>
   * xnew = x + speedx * Math.cos(Math.toRadians(getDirection())<br>
   * ynew = y + speedy * Math.sin(Math.toRadians(getDirection())<br>
   * x = xnew<br>
   * y = ynew<br>
   */
  public synchronized void move()
  {
    if (getNbHorzCells() * getNbVertCells() <= gridLimit)   // Small grid
      setLocation(location.getNeighbourLocation(direction));
    else
      setLocation(location.getAdjacentLocation(direction));
  }

  /**
   * Moves the given distance in the current direction. For distance = 1, the
   * 8 neighbour locations are reached. To move the actor on a straight line, see
   * the remarks for method move().
   * @see #move()
   * @param distance the distance to the requested cell location in cell units.
   */
  public synchronized void move(int distance)
  {
    setLocation(location.getAdjacentLocation(direction, distance));
  }

  /**
   * Returns number of cells of actor's game grid in horizontal direction.
   * Requires that the actor has been added to the game grid.
   * @return the number of cells in x-direction
   */
  public synchronized int getNbHorzCells()
  {
    if (gameGrid == null)
      GameGrid.fail("Error in Actor.getNbHorzCells()."
        + "\nActor not part of of game grid."
        + "\nApplication will terminate.");
    return gameGrid.getNbHorzCells();
  }

  /**
   * Returns number of cells of actor's game grid in vertical direction.
   * Requires that the actor has been added to the game grid.
   * @return the number of cells in y-direction
   */
  public synchronized int getNbVertCells()
  {
    if (gameGrid == null)
      GameGrid.fail("Error in Actor.getNbVertCells()."
        + "\nActor not part of of game grid."
        + "\nApplication will terminate.");
    return gameGrid.getNbVertCells();
  }

  /**
   * Turns the moving direction by the given angle (clockwise for positive
   * angles, counterclockwise for negative angles). The new moving direction
   * remains within 0..360.
   * @param angle the angle to turn in degrees
   */
  public synchronized void turn(double angle)
  {
    direction = (direction + angle) % 360;
    if (direction < 0)
      direction = 360 + direction;
    if (isRotatable)
    {
      double inc = 360.0 / GameGrid.nbRotSprites;
      rotationIndex = (int)(direction / inc) % GameGrid.nbRotSprites;
    }
  }

  // Returns x-coordinate of vector x, y with constant magnitude, rotated counter-
  // clockwise by angle increment da
  protected static double xrot(double x, double y, double da)
  {
    return (x * Math.cos(da) - y * Math.sin(da));
  }

  // Same for y-coordinate
  protected static double yrot(double x, double y, double da)
  {
    return (y * Math.cos(da) + x * Math.sin(da));
  }

  /**
   * Empty method called in every simulation iteration. Override it to
   * implement your own notification. 
   */
  public void act()
  {
  }

  /**
   * Empty method called when the actor is added to the game grid, 
   * the reset button is clicked or doReset() is called.
   * Override to get your own notification. Perform your initialization code
   * here better than in the constructor, where many of the actor's method
   * are still invalid. This method is called before the first act() cycling.
   */
  public void reset()
  {
  }

  /**
   * Registers an GGBorderListener so that the callback method nearBorder() is called
   * when the actor's location is on a cell at the border of the game grid.
   * @param listener the GGBorderListener to register
   */
  public synchronized void addBorderListener(GGBorderListener listener)
  {
    this.borderListener = listener;
  }

  protected GGBorderListener getBorderListener()
  {
    return borderListener;
  }

  /**
   * Empty implementation of a BorderListener called when the actor is set into a border cell.
   * Override to get your own notification.
   * @param actor the current actor
   * @param location the border location
   */
  public void nearBorder(Actor actor, Location location)
  {
  }

  /**
   * Returns true, if the actor's location is inside the grid.
   * Requires that the actor has been added to the game grid.
   * @return true, if the current actor's location is inside the grid
   */
  public synchronized boolean isInGrid()
  {
    if (gameGrid == null)
      GameGrid.fail("Error in Actor.isInGrid()."
        + "\nActor not part of of game grid."
        + "\nApplication will terminate.");
    return (gameGrid.isInGrid(location));
  }

  /**
   * Returns true, if the current location is on a border row or column.
   * Be aware that in one pixel games, the near border location is only 1 pixel from the border.
   * Requires that the actor has been added to the game grid.
   * @return true, if the current location is on a border row or column
   */
  public synchronized boolean isNearBorder()
  {
    if (gameGrid == null)
      GameGrid.fail("Error in Actor.isNearBorder()."
        + "\nActor not part of of game grid."
        + "\nApplication will terminate.");
    return (gameGrid.isAtBorder(location));
  }

  /**
   * Returns true, if the next call of move() will put the actor in a cell
   * inside the game grid.
   * Requires that the actor has been added to the game grid.
   * @return true, if the actor remains inside the grid on the next move()
   */
  public synchronized boolean isMoveValid()
  {
    if (gameGrid == null)
      GameGrid.fail("Error in Actor.isMoveValid()."
        + "\nActor not part of of game grid."
        + "\nApplication will terminate.");
    if (getNbHorzCells() * getNbVertCells() <= gridLimit)   // Small grid
      return gameGrid.isInGrid(location.getNeighbourLocation(direction));
    else
      return gameGrid.isInGrid(location.getAdjacentLocation(direction));
  }

  /**
   * Removes the given actor from the scene, so that act() is not called any more.
   * The visiblity is turned off (spriteId = -1) and a registered mouse,
   * mouse touch or key listener is disabled. If the actor has not yet 
   * been added to the game grid, nothing happens.Be aware that clicking 
   * the reset button or calling reset() will not bring
   * the actor to life.<br><br>
   * 
   * The public gameGrid reference remains valid, even if the actor has 
   * been removed. You may check if an actor has been added and then 
   * removed by calling isRemoved().<br><br>
   *
   * If you want to reuse the removed actor, you may add it again
   * to the game grid (the sprite with id = 0 is shown). Any mouse, mouse touch
   * or key listener must be registered again. Instead of removing the actor,
   * call hide() to make him invisible and show() to display him again.<br><br>
   *
   * The buffered image resources will be released when the actor
   * reference runs out of scope. Because actors use a lot of heap space,
   * you should be careful to remove actors and deassign their reference
   * when they are no more used (e.g. moves out of the visible game grid).
   */
  public synchronized void removeSelf()
  {
    if (gameGrid != null)
      gameGrid.removeActor(this);
  }

  protected int getStepCount()
  {
    return stepCount;
  }

  protected void initStepCount()
  {
    stepCount = stepCountStart;
  }

  protected void decreaseStepCount()
  {
    stepCount--;
  }

  /**
   * Slows down the calling of act() by the given factor. This may be used to
   * individually adapt the speed of actors. Sets a counter to the given value.
   * Before calling act(), the counter is decremented. When it reaches zero,
   * act() is called.
   * @param factor the factor greater or equal to 1 for delaying the
   * invocation of act()
   */
  public synchronized void setSlowDown(int factor)
  {
    if (factor < 1)
      factor = 1;
    stepCountStart = factor;
    stepCount = factor;
  }

  /**
   * Returns the current slow down factor.
   * @return the slow down factor
   */
  public synchronized int getSlowDown()
  {
    return stepCountStart;
  }

  /**
   * Turns on the visibility of the sprite with id 0.
   * All other sprites are hidden.
   */
  public synchronized void show()
  {
    if (isRemoved || nbSprites == 0)
      return;
    idVisible = 0;
  }

  protected int getIdVisibleStart()
  {
    return idVisibleStart;
  }

  /**
   * Turns on the visibility of the sprite with given id.
   * All other sprites are hidden.
   * @param spriteId the sprite id that will become visible; visibility
   * remains unchanged if spriteId is less than zero or greater or equal to
   * the number of sprites
   */
  public synchronized void show(int spriteId)
  {
    if (isRemoved || nbSprites == 0 || spriteId < 0 || spriteId >= nbSprites)
      return;
    idVisible = spriteId;
  }

  /**
   * Turns off the visiblity of this actor (all sprites are hidden, spriteId = -1).
   */
  public synchronized void hide()
  {
    idVisible = -1;
  }

  /**
   * Returns the id of the visible sprite.
   * @return the id of the visible sprite in the range 0..number of
   * sprites (exclusive), -1 if no sprite is visible
   */
  public synchronized int getIdVisible()
  {
    return idVisible;
  }

  /**
   * Returns true, if the actor is visible.
   * @return true, if one of the sprites is shown; false, if the actor has
   * spriteId == -1 or is not part of the game grid
   */
  public synchronized boolean isVisible()
  {
    if (gameGrid == null)
      return false;
    return idVisible == -1 ? false : true;
  }

  /**
   * Registers a partner actor that becomes a collision candidate, e.g. that
   * is checked for collisions in every simulation cycle.
   * The collisions are reported by a collision listener that must be
   * registered with addActorCollisionListener().
   * @param partner the partner that is checked for collision
   */
  public synchronized void addCollisionActor(Actor partner)
  {
    collisionActors.add(partner);
  }

  /**
   * Empty implementation of a GGActorCollisionListener called when the two actors collides.
   * Override to get your own notification.
   * @param actor1 the first actor
   * @param actor2 the second actor
   */
  public int collide(Actor actor1, Actor actor2)
  {
    return 0;
  }

  /**
   * Empty implementation of a GGTileCollisionListener called when
   * the an actor collides with a tile.
   * Override to get your own notification.
   * @param actor the colliding actor
   * @param location the location of the colliding tile within the tile map
   */
  public int collide(Actor actor, Location location)
  {
    return 0;
  }

  /**
   * Registers all actors in a list as collision candidates.
   * The collisions are reported by a collision listener that must be
   * registered with addActorCollisionListener().
   * @param partners an array of actors that are checked for collision
   */
  public synchronized void addCollisionActors(Actor[] partners)
  {
    for (Actor a : partners)
      collisionActors.add(a);
  }

  /**
   * Registers all actors in a list as collision candidates.
   * The collisions are reported by a collision listener that must be
   * registered with addActorCollisionListener().
   * @param partners a list of actors that are checked for collision
   */
  public synchronized void addCollisionActors(ArrayList<Actor> partners)
  {
    for (Actor a : partners)
      collisionActors.add(a);
  }

  /**
   * Returns a list of partners that are collision candidates.
   * @return all actors that are checked for collisions
   */
  public synchronized ArrayList<Actor> getCollisionActors()
  {
    return collisionActors;
  }

  /**
   * Registers a tile location that becomes a collision candidate, e.g. that
   * is checked for collisions in every simulation cycle.
   * The collisions are reported by a collision listener that must be
   * registered with addTileCollisionListener().
   * @param location the location of the tile within the tile map
   */
  public synchronized void addCollisionTile(Location location)
  {
    collisionTiles.add(location);
  }

  /**
   * Registers all tile locations in a list as collision candidates.
   * The collisions are reported by a collision listener that must be
   * registered with addTileCollisionListener().
   * @param locations an array of tile locations that are checked for collision
   */
  public synchronized void addCollisionTiles(Location[] locations)
  {
    for (Location loc : locations)
      collisionTiles.add(loc);
  }

  /**
   * Registers all tile locations in a list as collision candidates.
   * The collisions are reported by a collision listener that must be
   * registered with addTileCollisionListener().
   * @param locations a list of tile locations that are checked for collision
   */
  public synchronized void addCollisionTiles(ArrayList<Location> locations)
  {
    for (Location loc : locations)
      collisionTiles.add(loc);
  }

  /**
   * Returns a list of tile locations that are collision candidates.
   * @return all tile locations that are checked for collisions
   */
  public synchronized ArrayList<Location> getCollisionTiles()
  {
    return collisionTiles;
  }

  protected void notifyActorCollision(Actor collisionPartner)
  {
    if (actorCollisionListener != null)
      actorSimCount = actorCollisionListener.collide(this, collisionPartner);
  }

  protected void notifyTileCollision(Location location)
  {
    if (tileCollisionListener != null)
      tileSimCount = tileCollisionListener.collide(this, location);
  }

  /**
   * Registers a collision listener that reports collision events when
   * actors collide. Another already registered listener is disabled. The
   * collision notification is called in every simulation cycle before
   * the actor's act().
   * @param listener an actor collision listener; null to disable events
   */
  public synchronized void addActorCollisionListener(GGActorCollisionListener listener)
  {
    this.actorCollisionListener = listener;
  }

  /**
   * Registers a tile listener that reports collision events when actors
   * and tiles collide. Another already registered listener is disabled.
   * @param listener a tile collision listener; null to disable events
   */
  public synchronized void addTileCollisionListener(GGTileCollisionListener listener)
  {
    this.tileCollisionListener = listener;
  }

  protected void decreaseActorSimCount()
  {
    if (actorSimCount > 0)
      actorSimCount--;
  }

  protected boolean isActorCollisionRearmed()
  {
    if (actorSimCount == 0)
      return true;
    return false;
  }

  protected void decreaseTileSimCount()
  {
    if (tileSimCount > 0)
      tileSimCount--;
  }

  protected boolean isTileCollisionRearmed()
  {
    if (tileSimCount == 0)
      return true;
    return false;
  }

  /**
   * If set, the sprite image shown is mirrored horizontally.
   * @param enable if true, horizontal mirroring is enabled
   */
  public synchronized void setHorzMirror(boolean enable)
  {
    isHorzMirror = enable;
  }

  /**
   * If set, the sprite image shown is mirrored vertically.
   * @param enable if true, vertical mirroring is enabled
   */
  public synchronized void setVertMirror(boolean enable)
  {
    isVertMirror = enable;
  }

  /**
   * Returns the horizontal mirroring state.
   * @return true, if the sprite is mirrored horizontally; otherwise false
   */
  public synchronized boolean isHorzMirror()
  {
    return isHorzMirror;
  }

  /**
   * Returns the vertical mirroring state.
   * @return true, if the sprite is mirrored vertically; otherwise false
   */
  public synchronized boolean isVertMirror()
  {
    return isVertMirror;
  }

  /**
   * Enable/disable the invocation of act() in every simulation cycle.
   * @param enable if true, act() is invoked; otherwise act() is not invoked
   */
  public synchronized void setActEnabled(boolean enable)
  {
    isActEnabled = enable;
  }

  /**
   * Returns true, if act() is invoked in every simulation cycle.
   * @return true (default), if act() is called; false if the invokation of act() is disabled
   */
  public synchronized boolean isActEnabled()
  {
    return isActEnabled;
  }

  /**
   * Enable/disable the detection of collisions with the actor collision candidates.
   * @param enable if true (default), collisions will be notified
   */
  public synchronized void setActorCollisionEnabled(boolean enable)
  {
    isActorCollisionEnabled = enable;
  }

  /**
   * Returns true, if collision notification between actors is enabled.
   * @return true, if collision detection is enabled
   */
  public synchronized boolean isActorCollisionEnabled()
  {
    return isActorCollisionEnabled;
  }

  /**
   * Enable/disable the detection of collisions with the tile collision candidates.
   * @param enable if true (default), collisions will be notified
   */
  public synchronized void setTileCollisionEnabled(boolean enable)
  {
    isTileCollisionEnabled = enable;
  }

  /**
   * Returns true, if collision notification between tiles is enabled.
   * @return true, if collision detection is enabled
   */
  public synchronized boolean isTileCollisionEnabled()
  {
    return isTileCollisionEnabled;
  }

  /**
   * Returns all collision candidates who belongs to a given class whose collision areas
   * of the image with given sprite id intersects the circle with specified radius. Also actors whose cell locations are outside the visible grid are considered.
   * Requires that the actor has been added to the game grid.
   * @param radius the radius of the circle around the center of the current location in (fractional) cell units
   * @param clazz the clazz the actors must belong to, if null, all actors are considered
   * @return the list of actors within the given distance
   */
  public synchronized ArrayList<Actor> getCollisionActorsInRange(double radius, Class clazz, int spriteId)
  {
    if (gameGrid == null)
      GameGrid.fail("Error in Actor.getCollisionActorsInRange()."
        + "\nActor not part of of game grid."
        + "\nApplication will terminate.");
    ArrayList<Actor> tmp = new ArrayList<Actor>();
    for (Actor a : collisionActors)
    {
      if (a.getClass() == clazz)
      {
        GGCollisionArea area = a.getCurrentCollisionArea(spriteId);
        GGRectangle rect = area.getRectangle();
        GGCircle circle = area.getCircle();
        if (rect != null)  // Collision detection by rectangle
        {
          Point pt = gameGrid.toPoint(location);
          GGCircle rangeCircle = new GGCircle(new GGVector(pt.x, pt.y), radius);
          if (rect.isIntersecting(rangeCircle))
            tmp.add(a);
        }

        if (circle != null)  // Collision detection by circle
        {
          Point pt = gameGrid.toPoint(location);
          GGCircle rangeCircle = new GGCircle(new GGVector(pt.x, pt.y), radius);
          if (rangeCircle.isIntersecting(circle))
            tmp.add(a);
        }
      }
    }
    return tmp;
  }

  /**
   * Returns all actors of specified class in a specified distance.
   * The distance defines a circle around the current cell center. All actors in cells that intersects
   * with this circle are returned. Also cells outside the visible grid are considered.
   * To restrict this list to actors inside the grid, use isInGrid().
   * To get the 8 nearest neighbours, use distance = 1, to exclude diagonal
   * locations, use distance = 0.5;
   * Actors at the current location are not considered.
   * Requires that the actor has been added to the game grid.
   * @param distance the distance in (fractional) cell units  public
   * @param clazz the class of the actors to look for; if null actors of all classes are included
   */
  public synchronized ArrayList<Actor> getNeighbours(double distance, Class clazz)
  {
    if (gameGrid == null)
      GameGrid.fail("Error in Actor.getNeighbours()."
        + "\nActor not part of of game grid."
        + "\nApplication will terminate.");
    ArrayList<Actor> a = new ArrayList<Actor>();
    for (Location loc : location.getNeighbourLocations(distance))
    {
      ArrayList<Actor> actors = gameGrid.getActorsAt(loc, clazz);
      for (Actor actor : actors)
        a.add(actor);
    }
    return a;
  }

  /**
   * Returns all actors in a specified distance.
   * The distance defines a circle around the current cell center. All actors in cells that intersects
   * with this circle are returned. Also cells outside the visible grid are considered.
   * To restrict this list to actors inside the grid, use isInGrid().
   * To get the 6 nearest neighbours, use distance = 1, to exlude diagonal
   * locations, use distance = 0.5;
   * Actors at the current location are not considered.
   * @param distance the distance in (fractional) cell units  public
   */
  public synchronized ArrayList<Actor> getNeighbours(double distance)
  {
    return getNeighbours(distance, null);
  }

  /**
   * Returns whether the actor is rotatable or not.
   * @return true, if rotatable, e.g. image rotates when direction changes
   */
  public synchronized boolean isRotatable()
  {
    return isRotatable;
  }

  protected GGCollisionArea getCurrentCollisionArea(int spriteId)
  {
    return sprites[spriteId].getCollisionAreas()[rotationIndex].clone();
  }

  protected GGRectangle getCurrentCollisionRectangle(int spriteId)
  {
    GGRectangle collisionRectangle = getCurrentCollisionArea(spriteId).getRectangle();
    if (collisionRectangle == null)
      return null;
    GGRectangle rect = collisionRectangle.clone();
    if (isHorzMirror)
    {
      GGVector[] vertexes = rect.getVertexes();
      for (int i = 0; i < 4; i++)
        vertexes[i].x = -vertexes[i].x;
      rect = new GGRectangle(vertexes);
    }

    if (isVertMirror)
    {
      GGVector[] vertexes = rect.getVertexes();
      for (int i = 0; i < 4; i++)
        vertexes[i].y = -vertexes[i].y;
      rect = new GGRectangle(vertexes);
    }
    Point pt = gameGrid.toPoint(location);
    rect.translate(new GGVector(pt.x, pt.y));
    return rect;
  }

  protected GGCircle getCurrentCollisionCircle(int spriteId)
  {
    GGCircle collisionCircle = getCurrentCollisionArea(spriteId).getCircle();
    if (collisionCircle == null)
      return null;
    GGCircle circle = collisionCircle.clone();
    if (isHorzMirror)
      circle.center.x = -circle.center.x;
    if (isVertMirror)
      circle.center.y = -circle.center.y;

    Point pt = gameGrid.toPoint(location);
    circle.translate(new GGVector(pt.x, pt.y));
    return circle;
  }

  protected GGLine getCurrentCollisionLine(int spriteId)
  {
    GGLine collisionLine = getCurrentCollisionArea(spriteId).getLine();
    if (collisionLine == null)
      return null;
    GGLine line = collisionLine.clone();
    if (isHorzMirror)
    {
      GGVector[] vertexes = line.getVertexes();
      for (int i = 0; i < 2; i++)
        vertexes[i].x = -vertexes[i].x;
      line = new GGLine(vertexes);
    }
    if (isVertMirror)
    {
      GGVector[] vertexes = line.getVertexes();
      for (int i = 0; i < 2; i++)
        vertexes[i].y = -vertexes[i].y;
      line = new GGLine(vertexes);
    }
    Point pt = gameGrid.toPoint(location);
    line.translate(new GGVector(pt.x, pt.y));
    return line;
  }

  protected GGVector getCurrentCollisionSpot(int spriteId)
  {
    GGVector collisionSpot = getCurrentCollisionArea(spriteId).getSpot();
    if (collisionSpot == null)
      return null;
    GGVector spot = collisionSpot.clone();
    if (isHorzMirror)
      spot.x = -spot.x;
    if (isVertMirror)
      spot.y = -spot.y;
    Point pt = gameGrid.toPoint(location);
    spot = spot.add(new GGVector(pt.x, pt.y));
    return spot;
  }

  protected CollisionType getCurrentCollisionType(int spriteId)
  {
    return getCurrentCollisionArea(spriteId).getCollisionType();
  }

  protected BufferedImage getCurrentImage(int spriteId)
  {
    return sprites[spriteId].getSourceImage();
  }

  protected GGVector getCurrentImageCenter(int spriteId)
  {
    Point pt = gameGrid.toPoint(location);
    return new GGVector(pt.x, pt.y);
  }

  protected double getCurrentImageDirection(int spriteId)
  {
    return direction;  // in degrees
  }

  /**
   * Selects the rectangle (in pixel units) relative to the sprite image that is used for
   * collision detection. The following coordinate system is used:<br><br>
   * x-axis to the left, y-axis downward, zero at image center<br><br>
   * (For even image pixel width or height, the center is half pixel width
   * to the left or resp. to the top).<br><br>
   * Any collision types defined earlier are removed.
   * @param spriteId the id of the sprite
   * @param center the rectangle center (zero at image center)
   * @param width the width in pixel units of the rectangle (in x-direction)
   * @param height the height in pixel units of the rectangle (in y-direction)
   */
  public synchronized void setCollisionRectangle(int spriteId, Point center, int width, int height)
  {
    sprites[spriteId].setCollisionRectangle(center, width, height, isRotatable);
  }

  /** 
   * Same as setCollisionRectangle(int spriteId, Point center, int width, int height)
   * for spriteId = 0
   * @param center the rectangle center (zero at image center)
   * @param width the width in pixel units of the rectangle (in x-direction)
   * @param height the height in pixel units of the rectangle (in y-direction)
   */
  public synchronized void setCollisionRectangle(Point center, int width, int height)
  {
    setCollisionRectangle(0, center, width, height);
  }

  /**
   * Selects the circle (in pixel units) relative to the sprite image that is used for
   * collision detection. The following coordinate system is used:<br><br>
   * x-axis to the left, y-axis downward, zero at image center<br><br>
   * (for even image pixel width or height, the center is half pixel width
   * to the left or resp. to the top).<br><br>
   * Any collision types defined earlier are removed.
   * @param spriteId the id of the sprite
   * @param center circle center (zero at image center)
   * @param radius the radius of the circle (in pixel units)
   */
  public synchronized void setCollisionCircle(int spriteId, Point center, int radius)
  {
    GGCircle circle = new GGCircle(new GGVector(center), radius);
    sprites[spriteId].setCollisionCircle(circle, isRotatable);
  }

  /**
   * Same as setCollisionCircle(int spriteId, Point center, int radius)
   * for spriteId = 0.
   * @param center the circle center (zero at image center)
   * @param radius the radius of the circle (in pixel units)
   */
  public synchronized void setCollisionCircle(Point center, int radius)
  {
    setCollisionCircle(0, center, radius);
  }

  /**
   * Selects the line segment (in pixel units) relative to the sprite image that is used for
   * collision detection. The following coordinate system is used:<br><br>
   * x-axis to the left, y-axis downward, zero at image center<br><br>
   * (for even image pixel width or height, the center is half pixel width
   * to the left or resp. to the top).<br><br>
   * Any collision types defined earlier are removed.
   * @param spriteId the id of the sprite
   * @param startPoint the start point of the line (zero at image center)
   * @param endPoint the end point of the line (zero at image center)
   */
  public synchronized void setCollisionLine(int spriteId, Point startPoint, Point endPoint)
  {
    GGLine line = new GGLine(new GGVector(startPoint), new GGVector(endPoint));
    sprites[spriteId].setCollisionLine(line, isRotatable);
  }

  /**
   * Same as setCollisionLine(int spriteId, Point startPoint, Point endPoint)
   * for spriteId = 0.
   * @param startPoint the start point of the line (zero at image center)
   * @param endPoint the end point of the line (zero at image center)
   */
  public synchronized void setCollisionLine(Point startPoint, Point endPoint)
  {
    setCollisionLine(0, startPoint, endPoint);
  }

  /**
   * Selects the hot spot (in pixel units) relative to the sprite image that is used for
   * collision detection. The following coordinate system is used:<br><br>
   * x-axis to the left, y-axis downward, zero at image center<br><br>
   * (for even image pixel width or height, the center is half pixel
   * width to the left or resp. to the top).<br><br>
   * Any collision types defined earlier are removed.
   * @param spriteId the id of the sprite
   * @param spot the hot spot (zero at image center)
   */
  public synchronized void setCollisionSpot(int spriteId, Point spot)
  {
    GGVector vSpot = new GGVector(spot);
    sprites[spriteId].setCollisionSpot(vSpot, isRotatable);
  }

  /**
   * Same as setCollisionSpot(int spriteId, Point spot)
   * for spriteId = 0.
   * @param spot the hot spot (zero at image center)
   */
  public synchronized void setCollisionSpot(Point spot)
  {
    setCollisionSpot(0, spot);
  }

  /**
   * Selects collision detection on non-transparent pixels of the image.
   * Due to performance reasons only available if the collision partner
   * detects collision using spot, circle or line detection.
   * Any collision types defined earlier are removed.
   * @param spriteId the id of the sprite
   */
  public synchronized void setCollisionImage(int spriteId)
  {
    sprites[spriteId].setCollisionImage();
  }

  /**
   * Same as setCollisionImage(int spriteId) for spriteId = 0.
   */
  public synchronized void setCollisionImage()
  {
    setCollisionImage(0);
  }

  /**
   * Sets the actor at the first place in the scene <b>of actor class</b>.
   * Consequently the actor act() method will be called first and the actor image
   * will be drawn on top of all other actor images <b>for this class</b>. The order
   * of the other actors remains unchanged. To change the act or paint order of
   * the actors class, use GameGrid.setActOrder() or GameGrid.setPaintOrder().
   * Requires that the actor has been added to the game grid.<br><br>
   * If you want to change the paint order of actors in different actor classes,
   * use GameGrid.setPaintOrder().
   * @see ch.aplu.jgamegrid.GameGrid#setPaintOrder(Class... classes)
   */
  public synchronized void setOnTop()
  {
    if (gameGrid == null)
      GameGrid.fail("Error in Actor.setOnTop()."
        + "\nActor not part of of game grid."
        + "\nApplication will terminate.");
    gameGrid.setActorOnTop(this);
  }

  /**
   * Sets the actor at the last place in the scene <b>of the actor class</b>.
   * Consequently the actor act() method will be called last and the actor image
   * will be drawn on the bottom of all other actor images <b>for this class</b>. The order
   * of the other actors remains unchanged. To change the act or paint order of
   * the actors class, use GameGrid.setActOrder() or GameGrid.setPaintOrder().
   * Requires that the actor has been added to the game grid.<br><br>
   * If you want to change the paint order of actors in different actor classes,
   * use GameGrid.setPaintOrder().
   * @see ch.aplu.jgamegrid.GameGrid#setPaintOrder(Class... classes)
  
   */
  public synchronized void setOnBottom()
  {
    if (gameGrid == null)
      GameGrid.fail("Error in Actor.setOnBottom()."
        + "\nActor not part of of game grid."
        + "\nApplication will terminate.");
    gameGrid.setActorOnBottom(this);
  }

  /**
   * Returns the number of simulation cycles since last reset.
   * @return the number of simulation cycles
   */
  public synchronized int getNbCycles()
  {
    return nbCycles;
  }

  /**
   * Delay execution for the given amount of time.
   * @param time the delay time (in ms)
   */
  public static void delay(long time)
  {
    try
    {
      Thread.sleep(time);
    }
    catch (InterruptedException ex)
    {
    }
  }

  /**
   * Increases the id of the currently visible sprite and makes this
   * sprite visible.
   * If spriteId reaches nbSprites - 1 the next sprite id will be 0.
   * If spriteId is -1 (sprite invisible) the next sprite id is 0.
   */
  public synchronized void showNextSprite()
  {
    idVisible++;
    if (idVisible == nbSprites)
      idVisible = 0;
    show(idVisible);
  }

  /**
   * Decreases the id of the currently visible sprite and makes this
   * sprite visible.
   * If spriteId reaches 0 the previous sprite id will be nbSprites - 1.
   * If spriteId is -1 (sprite invisible) the previous sprite id is still -1.
   */
  public synchronized void showPreviousSprite()
  {
    if (idVisible == -1)
      return;
    idVisible--;
    if (idVisible == -1)
      idVisible = nbSprites - 1;
    show(idVisible);
  }

  /**
   * Returns the buffered image of the currently visible sprite picture.
   * If the actor is rotatable, this is the rotated image and it's size
   * (bounding rectangle) is bigger than the originally loaded image.
   * @return the buffered image of the displayed sprite picture.
   */
  public synchronized BufferedImage getCurrentImage()
  {
    return bufferedImages[idVisible][rotationIndex];
  }

  /**
   * Returns the buffered image of the originally loaded sprite with
   * given sprite id. Unmodified when the actor is rotatable.
   * @return the buffered image of the loaded sprite.
   */
  public synchronized BufferedImage getImage(int spriteId)
  {
    return sprites[spriteId].getSourceImage();
  }

  /**
   * Returns the buffered image of the originally loaded sprite with
   * id = 0. Unmodified when the actor is rotatable.
   * @return the buffered image of the loaded sprite.
   */
  public synchronized BufferedImage getImage()
  {
    return getImage(0);
  }

  /**
   * Returns the color of the currently visible sprite image at given
   * pixel position ((0,0) at upper left vertex, x-axis to the right,
   * y-axis downwards).
   * @param pt the point where to look for the color
   * @return the image color of the given point returned by BufferedImage.getRGB()
   */
  public synchronized Color getPixelColor(Point pt)
  {
    return new Color(bufferedImages[idVisible][rotationIndex].getRGB(pt.x, pt.y));
  }

  /**
   * Returns the location of the actor center in pixel coordinates taking
   * into account the current location offset (default offset is 0).
   * @return the pixel coordinates with respect to the game grid window 
   * (origin at upper left vertex, x-axis to the left, y-axis downward);
   * null, if the actor is not yet added to the game grid
   * @see #setLocationOffset(Point locationOffset)
   */
  public synchronized Point getPixelLocation()
  {
    if (gameGrid == null)
      GameGrid.fail("Error in Actor.getPixelLocation()."
        + "\nActor not part of of game grid."
        + "\nApplication will terminate.");

    Point pt = new Point(
      gameGrid.toPoint(getLocation()).x + getLocationOffset().x,
      gameGrid.toPoint(getLocation()).y + getLocationOffset().y);
    return pt;
  }

  /**
   * Returns the width of the sprite with given id.
   * This is the width of the originally loaded sprited image. (For
   * rotatable actors the bounding rectangle is increased to give space
   * for the rotated images.)
   * @param spriteId the id of the sprite
   * @return The width in pixels of this sprite
   */
  public synchronized int getWidth(int spriteId)
  {
    return sprites[spriteId].getSourceImage().getWidth();
  }

  /**
   * Returns the height of the sprite with given id.
   * This is the height of the originally loaded sprited image. (For
   * rotatable actors the bounding rectangle is increased to give space
   * for the rotated images.)
   * @param spriteId the id of the sprite
   * @return The height in pixels of this sprite
   */
  public synchronized int getHeight(int spriteId)
  {
    return sprites[spriteId].getSourceImage().getHeight();
  }

  /**
   * Returns a new position vector of given position vector
   * rotated with given center point by given angle.
   * @param position the vector to the current position
   * @param center the rotation center point
   * @param angle the rotating angle (in degrees, positive clockwise)
   * @return the position (vector with double precision) to the rotated position
   */
  public synchronized static GGVector getRotatedPosition(GGVector position, Point center, double angle)
  {
    GGVector c = new GGVector(center);
    GGVector v = position.sub(c);
    double theta = Math.atan2(v.y, v.x);
    double rho = Math.sqrt(v.x * v.x + v.y * v.y);
    v = new GGVector(rho * Math.cos(theta + Math.toRadians(angle)),
      rho * Math.sin(theta + Math.toRadians(angle)));
    return v.add(c);
  }

  /**
   * Rotates the actor with given rotation center location by the given angle. 
   * The rotated location is approximate due to the grid resolution.
   * If a precise sequence of rotations with the same rotation center is needed,
   * use getRotatedPosition(Point center, GGVector position, double angle)
   * with a global center and position.
   * @param centerLoc the rotation center location
   * @param angle the rotation angle (in degrees, positive clockwise)
   */
  public synchronized void rotate(Location centerLoc, double angle)
  {
    rotate(gameGrid.toPoint(centerLoc), angle);
  }

  /**
   * Rotates the actor with given rotation center point by given angle.
   * The rotated location is approximate due to the grid resolution.
   * If a precise sequence of rotations with the same rotation center is needed,
   * use getRotatedPosition(Point center, GGVector position, double angle)
   * with a global center and position.
   * @param center the rotation center point
   * @param angle the rotation angle (in degrees, positive clockwise)
   */
  public synchronized void rotate(Point center, double angle)
  {
    GGVector v = getRotatedPosition(
      new GGVector(gameGrid.toPoint(getLocation())), center, angle);
    setLocation(gameGrid.toLocation((int)(v.x + 0.5), (int)(v.y + 0.5)));
    setDirection(getDirection() + angle);
  }

  /**
   * Same as addMouseTouchListener(listener, mouseEventMask, onTopOnly)
   * with onTopOnly = false.
   * @param listener the GGMouseTouchListener to register
   * @param mouseEventMask an OR-combinaton of constants defined in class GGMouse
   */
  public void addMouseTouchListener(GGMouseTouchListener listener,
    int mouseEventMask)
  {
    addMouseTouchListener(listener, mouseEventMask, false);
  }

  /**
   * Add a GGMouseTouchListener to get notifications when the mouse
   * interacts with the mouse touch area.
   * Only the events defined as OR-combination in the specified mask
   * are notified. GGMouse.enter and GGMouse.leave events are produced with
   * respect to the mouse touch area. Touch area types available: IMAGE
   * (default, events on non-transparent pixels of sprite image), 
   * RECTANGLE, CIRCLE. The non-default types can be selected for each sprite ID
   * with Actor.setMouseTouchRectangle(), Actor.setMouseTouchCircle().<br><br>
   * The mouse must be enabled and the mouse event not "consumed"
   * by other mouse event callbacks. The sequence of events from several
   * registered listeners is not previsible.
   * @param listener the GGMouseTouchListener to register
   * @param mouseEventMask an OR-combinaton of constants defined in class GGMouse
   * @param onTopOnly if true, enable mouse touch event for the actor at top of others
   * (painted last) only; otherwise touch event of all actors is enabled
   */
  public void addMouseTouchListener(GGMouseTouchListener listener,
    int mouseEventMask, boolean onTopOnly)
  {
    mouseTouchListener = listener;
    mouseMask = mouseEventMask;
    this.onTopOnly = onTopOnly;
    boolean isMove = false;
    boolean isEnter = false;
    boolean isLeave = false;
    if ((mouseMask & GGMouse.move) != 0)
      isMove = true;
    // Transform enter into move
    if ((mouseMask & GGMouse.enter) != 0)
    {
      mouseMask = mouseMask | GGMouse.move;
      mouseMask = mouseMask & ~GGMouse.enter;  // Remove enter
      isEnter = true;
    }
    // Transform leave into move
    if ((mouseMask & GGMouse.leave) != 0)
    {
      mouseMask = mouseMask | GGMouse.move;
      mouseMask = mouseMask & ~GGMouse.leave;  // Remove leave
      isLeave = true;
    }
    mouseAdapter = new MyGGMouseAdapter(this, isMove, isEnter, isLeave);

    if ((mouseMask & GGMouse.lPress) != 0)  // LPress registered
      isMouseLPressRegistered = true;
    else
      isMouseLPressRegistered = false;
    if ((mouseMask & GGMouse.rPress) != 0)  // RPress registered
      isMouseRPressRegistered = true;
    else
      isMouseRPressRegistered = false;

    mouseMask = mouseMask | GGMouse.lPress | GGMouse.rPress;  // We need the press event anyway
    if (gameGrid != null)
      gameGrid.addMouseListener(mouseAdapter, mouseMask);
    else
      mouseMask = -mouseMask;  // Postpone addMouseListener
  }

  protected GGInteractionArea getCurrentInteractionArea(int spriteId)
  {
    return sprites[spriteId].getInteractionAreas()[rotationIndex].clone();
  }

  protected InteractionType getCurrentInteractionType(int spriteId)
  {
    if (spriteId < 0)
      return InteractionType.NONE;
    return getCurrentInteractionArea(spriteId).getInteractionType();
  }

  /**
   * Selects the rectangle (in pixel units) relative to the sprite image that is used for
   * mouse touch detection. The following coordinate system is used:<br><br>
   * x-axis to the left, y-axis downward, zero at image center<br><br>
   * (For even image pixel width or height, the center is half pixel width
   * to the left or resp. to the top).<br><br>
   * Any touch type defined earlier is replaced.
   * @param spriteId the id of the sprite
   * @param center the rectangle center (zero at image center)
   * @param width the width in pixel units of the rectangle (in x-direction)
   * @param height the height in pixel units of the rectangle (in y-direction)
   */
  public synchronized void setMouseTouchRectangle(int spriteId, Point center, int width, int height)
  {
    sprites[spriteId].setInteractionRectangle(center, width, height, isRotatable);
  }

  /**
   * Same as setTouchRectangle(int spriteId, Rectangle rect)
   * for all sprites of this actor.
   * @param center the rectangle center (zero at image center)
   * @param width the width in pixel units of the rectangle (in x-direction)
   * @param height the height in pixel units of the rectangle (in y-direction)
   */
  public synchronized void setMouseTouchRectangle(Point center, int width, int height)
  {
    for (int i = 0; i < nbSprites; i++)
      setMouseTouchRectangle(i, center, width, height);
  }

  protected GGRectangle getCurrentInteractionRectangle(int spriteId)
  {
    GGRectangle interactionRectangle = getCurrentInteractionArea(spriteId).getRectangle();
    if (interactionRectangle == null)
      return null;
    GGRectangle rect = interactionRectangle.clone();
    if (isHorzMirror)
    {
      GGVector[] vertexes = rect.getVertexes();
      for (int i = 0; i < 4; i++)
        vertexes[i].x = -vertexes[i].x;
      rect = new GGRectangle(vertexes);
    }

    if (isVertMirror)
    {
      GGVector[] vertexes = rect.getVertexes();
      for (int i = 0; i < 4; i++)
        vertexes[i].y = -vertexes[i].y;
      rect = new GGRectangle(vertexes);
    }
    Point pt = gameGrid.toPoint(location);
    rect.translate(new GGVector(pt.x, pt.y));
    return rect;
  }

  /**
   * Selects the circle (in pixel units) relative to the sprite image that is used for
   * mouse touch detection. The following coordinate system is used:<br><br>
   * x-axis to the left, y-axis downward, zero at image center<br><br>
   * (for even image pixel width or height, the center is half pixel width
   * to the left or resp. to the top).<br><br>
   * Any interaction type defined earlier is replaced.
   * @param spriteId the id of the sprite
   * @param center circle center (zero at image center)
   * @param radius the radius of the circle (in pixel units)
   */
  public synchronized void setMouseTouchCircle(int spriteId, Point center, int radius)
  {
    GGCircle circle = new GGCircle(new GGVector(center), radius);
    sprites[spriteId].setInteractionCircle(circle, isRotatable);
  }

  /**
   * Same as setMouseTouchCircle(int spriteId, Point center, int radius)
   * for all sprites of this actor.
   * @param center circle center (zero at image center)
   * @param radius the radius of the circle (in pixel units)
   */
  public synchronized void setMouseTouchCircle(Point center, int radius)
  {
    for (int i = 0; i < nbSprites; i++)
      setMouseTouchCircle(i, center, radius);
  }

  protected GGCircle getCurrentInteractionCircle(int spriteId)
  {
    GGCircle interactionCircle = getCurrentInteractionArea(spriteId).getCircle();
    if (interactionCircle == null)
      return null;
    GGCircle circle = interactionCircle.clone();
    if (isHorzMirror)
      circle.center.x = -circle.center.x;
    if (isVertMirror)
      circle.center.y = -circle.center.y;

    Point pt = gameGrid.toPoint(location);
    circle.translate(new GGVector(pt.x, pt.y));
    return circle;
  }

  /**
   * Selects the non-transparent pixels of the sprite image for
   * mouse touch detection. This is the default detection
   * type when the GGMouseInteractionListener is registered.<br><br>
   * Any interaction type defined earlier is replaced.
   */
  public synchronized void setMouseTouchImage(int spriteId)
  {
    sprites[spriteId].setInteractionImage(isRotatable);
  }

  /**
   * Same as setMouseTouchImage(int spriteId) for all sprites of this actor.
   */
  public synchronized void setMouseTouchImage()
  {
    for (int i = 0; i < nbSprites; i++)
      setMouseTouchImage(i);
  }

  /**
   * Enable/disable the detection of mouse interactions.
   * @param enable if true, mouse interactions will be notified
   */
  public void setMouseTouchEnabled(boolean enable)
  {
    isMouseTouchEnabled = enable;
  }

  /**
   * Returns the sprite image scaled by the given factor and rotated to the
   * given angle.
   * The returned image can be used to create a new actor with a zoomed and
   * rotated image.<br><br>
   * If rotation angle != 0, the size of the returned buffer image is enlarged
   * to take place for the rotated image.
   * @param spriteId the sprite id of the actor's sprite image
   * @param factor the zoom factor (>1 zoom-in, <1 zoom-out)
   * @param angle the rotation angle (in degrees clockwise, 0 to east)
   * @return the transformed image
   */
  public synchronized BufferedImage getScaledImage(int spriteId, double factor, double angle)
  {
    return GGBitmap.getScaledImage(getImage(spriteId), factor, angle);
  }

  /**
   * Same as getScaledImage(int spriteId, double factor, double angle)
   * with spriteId = 0.
   * @param factor the zoom factor (>1 zoom-in, <1 zoom-out)
   * @param angle the rotation angle (in degrees clockwise, 0 to east)
   * @return the transformed image
   */
  public BufferedImage getScaledImage(double factor, double angle)
  {
    return getScaledImage(0, factor, angle);
  }

  /**
   * Transforms the given buffered image by scaling by the given factor and
   * rotating by the given angle.
   * @deprecated  Use GGBitmap.getScaledImage instead. {@link ch.aplu.jgamegrid.GGBitmap#getScaledImage(BufferedImage bi, double factor, double angle)}
   * @param bi the buffered image to transform
   * @param factor the zoom factor (>1 zoom-in, <1 zoom-out)
   * @param angle the rotation angle (in degrees clockwise)
   * @return the transformed image
   */
  @Deprecated
  public static synchronized BufferedImage getScaledImage(BufferedImage bi, double factor, double angle)
  {
    return GGBitmap.getScaledImage(bi, factor, angle);
  }

  /**
   * Returns number of sprites.
   * @return the number of sprites
   */
  public int getNbSprites()
  {
    return nbSprites;
  }

  protected GGMouseTouchListener getMouseTouchListener()
  {
    return mouseTouchListener;
  }

  protected GGMouseListener getMouseListener()
  {
    return mouseAdapter;
  }

  /**
   * Returns true, if the actor has been removed (by calling removeSelf() or
   * GameGrid.removeActor()). If the actor was never part of the game grid
   * or is added again to the game grid after being removed, returns false.
   * @return true, if the actor has been removed
   */
  public boolean isRemoved()
  {
    return isRemoved;
  }

  protected void setRemoved()
  {
    // gameGrid is not set to null. So we have still access to the GameGrid instance
    isRemoved = true;
    idVisible = -1;
  }

  /**
   * Increases the current double displace position in the 
   * current direction to the given double distance and moves the actor 
   * in the cell that contains the given coordinates. Sets the location 
   * offset accordingly.<br>
   * The displace position uses a double coordinate system corresponing to
   * the integer pixel coordinates. This method is convenient to move 
   * the actor as precise as possible on a straight line even in a coarse grid.
   * @param ds the distance to displace the actor; if negative, displace in
   * opposite direction
   */
  public void displace(double ds)
  {
    xDouble = xDouble + ds * Math.cos(Math.toRadians(getDirection()));
    yDouble = yDouble + ds * Math.sin(Math.toRadians(getDirection()));
    setToDoubleLocation(xDouble, yDouble);
  }

  /**
   * Returns the current displace position.
   * @return the current displace position
   */
  public Point2D.Double getDisplacePosition()
  {
    return new Point2D.Double(xDouble, yDouble);
  }

  /** 
   * Sets the current displace position and moves the actor in the cell that
   * contains the given coordinates. Sets the location offset accordingly.
   * @param displacePosition the double coordinates of the new position
   */
  public void setDisplacePosition(Point2D.Double displacePosition)
  {
    xDouble = displacePosition.x;
    yDouble = displacePosition.y;
    setToDoubleLocation(xDouble, yDouble);
  }

  private void setToDoubleLocation(double x, double y)
  {
    int xRound;
    if (x >= 0)
      xRound = (int)(x + 0.5);
    else
      xRound = (int)(x - 0.5);
    int yRound;
    if (yDouble >= 0)
      yRound = (int)(y + 0.5);
    else
      yRound = (int)(y - 0.5);
    Location loc = gameGrid.toLocation(xRound, yRound);
    Point center = gameGrid.toPoint(loc);
    Point offset = new Point(xRound - center.x, yRound - center.y);
    setLocation(loc);
    setLocationOffset(offset);
  }
}
