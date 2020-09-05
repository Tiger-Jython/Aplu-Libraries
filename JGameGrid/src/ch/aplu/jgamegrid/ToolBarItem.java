// ToolBarItem.java

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

import java.awt.image.BufferedImage;

/**
 * A class to represent actors used for tool bar items.
 * All sprites images in a tool bar should have the same height.
 */
public class ToolBarItem extends Actor
{
  private String filename = null;
  private int nbSprites = 1;

 /**
   * Constructs a tool bar item based on one or several sprite images
   * defined by the given buffered images.
   * @see ch.aplu.jgamegrid.Actor#Actor(BufferedImage... bis)
   */
  public ToolBarItem(BufferedImage... spriteImages)
  {
    super(spriteImages);
  }

  /**
   * Constructs a tool bar item with one sprite image.
   * @param filename the fully qualified path to the image file displayed
   * for this actor
   * @see ch.aplu.jgamegrid.Actor#Actor(String filename)
   */
  public ToolBarItem(String filename)
  {
    this(filename, 1);
  }

  /**
   * Constructs a tool bar item using nbSprites sprite images.
   * @param filename the fully qualified path to the image file displayed
   * for this actor
   * @param nbSprites the number of sprite images for the same actor
   * @see ch.aplu.jgamegrid.Actor#Actor(String filename, int nbSprites)
   */
  public ToolBarItem(String filename, int nbSprites)
  {
    super(filename, nbSprites);
    this.filename = filename;
    this.nbSprites = nbSprites;
  }

  /**
   * Returns the filename of the sprite image.
   * @return the path to the image file
   */
  public String getFilename()
  {
    return filename;
  }

  /**
   * Returns the number of sprite images specified for this actor.
   * @return the number of sprite images
   */
  public int getNumberOfSprites()
  {
    return nbSprites;
  }
}
