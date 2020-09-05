// ToolBar.java

/*
 This software is part of the JGameGrid library.
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

import java.util.*;

/**
 * Class to represent a linear bar of item. The items are standard JGameGrid
 * actors. A tool bar is used to show or modify the state of a game (e.g. to
 * select/show the trump suit). Mouse touch events are reported
 * by a registered tool bar listener. The actors may have several sprite
 * image that may be changed by calling Actor.show(spriteId). Because in
 * JGameGrid the paint order is determined per class (when the first actor of
 * a class is s added to the game grid), the tool bar items are shown on top
 * of card actors if the tool bar is shown first. The paint order may be
 * changed by calling setOnTop() or setOnBottom resp.
 */
public class ToolBar
{
  // ------------------ Inner class MyMouseTouchListener ------------
  private class MyMouseTouchListener implements GGMouseTouchListener
  {
    public void mouseTouched(Actor actor, GGMouse mouse, java.awt.Point spot)
    {
      if (toolBarListener == null)
        return;
      switch (mouse.getEvent())
      {
        case GGMouse.lPress:
          toolBarListener.leftPressed((ToolBarItem)actor);
          break;
        case GGMouse.lRelease:
          toolBarListener.leftReleased((ToolBarItem)actor);
          break;
        case GGMouse.lClick:
          toolBarListener.leftClicked((ToolBarItem)actor);
          break;
        case GGMouse.lDClick:
          toolBarListener.leftDoubleClicked((ToolBarItem)actor);
          break;
        case GGMouse.rPress:
          toolBarListener.rightPressed((ToolBarItem)actor);
          break;
        case GGMouse.rRelease:
          toolBarListener.rightReleased((ToolBarItem)actor);
          break;
        case GGMouse.rClick:
          toolBarListener.rightClicked((ToolBarItem)actor);
          break;
        case GGMouse.rDClick:
          toolBarListener.rightDoubleClicked((ToolBarItem)actor);
          break;
        case GGMouse.enter:
          toolBarListener.entered((ToolBarItem)actor);
          break;
        case GGMouse.leave:
          toolBarListener.exited((ToolBarItem)actor);
          break;
      }
    }

  }
  // ----------------- End of inner class --------------------------
  //

  private ArrayList<ToolBarItem> itemList = new ArrayList<ToolBarItem>();
  private MyMouseTouchListener mouseTouchListener = new MyMouseTouchListener();
  private ToolBarListener toolBarListener = null;
  private GameGrid gameGrid = null;
  private boolean isDrawn = false;
  private int mouseMask = GGMouse.enter | GGMouse.leave
    | GGMouse.lPress | GGMouse.lRelease | GGMouse.lClick | GGMouse.lDClick
    | GGMouse.rPress | GGMouse.rRelease | GGMouse.rClick | GGMouse.rDClick;

  public ToolBar(GameGrid gameGrid)
  {
    this.gameGrid = gameGrid;
  }

  /**
   * Adds the tool bar item actors to the card game window at the
   * given location (upper left corner of first item). The item actors are drawn 
   * on top of all other cardActors already visible in the card game window. 
   * If the tool bar has already been displayed before, just sets the tool bar
   * with current attributes to new location. If the tool bar is empty, 
   * nothing happens.<br><br>
   *
   * To hide a toolbar instead of hiding all tool bar items, just move it outside
   * the visible game grid.
   * @param barLocation the location of the upper left corner of the first item
   */
  public void show(Location barLocation)
  {
    if (itemList.isEmpty())
      return;

    if (isDrawn)
    {
      setLocation(barLocation);
      return;
    }
    isDrawn = true;
    int barHeight = itemList.get(0).getHeight(0);
    int xCurrent = barLocation.x;  // Ulx
    for (int i = 0; i < itemList.size(); i++)
    {
      ToolBarItem item = itemList.get(i);
      int itemWidth = item.getWidth(0);
      int xCenter = xCurrent + itemWidth / 2;
      xCurrent += itemWidth;
      Location location = new Location(xCenter, barLocation.y + barHeight / 2);
      if (i == itemList.size() - 1)
        gameGrid.addActor(item, location);
      else
        gameGrid.addActorNoRefresh(item, location);
      if (!(item instanceof ToolBarSeparator || item instanceof ToolBarText))
        item.addMouseTouchListener(mouseTouchListener, mouseMask, true);  // On top only
    }
  }

  /**
   * Removes all currently displayed items from the card game window
   * and empties the item list.
   */
  public void removeAllItems()
  {
    for (ToolBarItem item : itemList)
      item.removeSelf();
    itemList.clear();
    isDrawn = false;
  }

  /**
   * Add the next item(s) into the tool bar in the order they are passed.
   * The items will be displayed in the order the are added.
   * All items should have the same height. To actualize a currently displayed
   * tool bar, show() must be called.
   * @param item the item(s) or array of items to be added
   */
  public void addItem(ToolBarItem... item)
  {
    for (int i = 0; i < item.length; i++)
      itemList.add(item[i]);
  }

  /**
   * Sets the sprite id of the item at given index to given value.
   * For spriteId = -1, the sprite image is hidden.
   * @param itemIndex the index of the item in the item list
   * (in the range 0..nbItems-1, nothing happens if outside)
   * @param spriteId the new sprite id of the item actor
   */
  public void setSpriteId(int itemIndex, int spriteId)
  {
    if (itemIndex < 0 || itemIndex > itemList.size() - 1)
      return;
    itemList.get(itemIndex).show(spriteId);
  }

  /**
   * Sets the sprite id of all items to given value.
   * For spriteId = -1, the sprite image is hidden.
   * @param spriteId the new sprite id of all item actors
   */
  public void setAllSpriteIds(int spriteId)
  {
    for (int i = 0; i < itemList.size(); i++)
      setSpriteId(i, spriteId);
  }

  /**
   * Returns the tool bar item actor at the specified index of the item list.
   * @param itemIndex the index of the item in the item list
   * (in the range 0..nbItems-1)
   * @return the item at the specified itemIndex; null, if index outside range
   */
  public ToolBarItem getItem(int itemIndex)
  {
    if (itemIndex < 0 || itemIndex > itemList.size() - 1)
      return null;
    return itemList.get(itemIndex);
  }

  /**
   * Puts the ToolBarItem class prior to the given class in the paint order,
   * so that the tool bar item images are painted on top of actors from the given class.
   * Only valid, if the tool bar has been drawn.
   */
  public void setOnTop(Class clazz)
  {
    if (gameGrid == null)
      return;
    gameGrid.setPaintOrder(ToolBarItem.class, clazz);
  }

  /**
   * Puts the given class prior to the ToolBarItem class in the paint order,
   * so that actors from the given class are painted on top of tool bar item images.
   */
  public void setOnBottom(Class clazz)
  {
    if (gameGrid == null)
      return;
    gameGrid.setPaintOrder(clazz, ToolBarItem.class);
  }

  /**
   * Moves the current tool bar at the specified location. All current
   * attributes are maintained. If the tool bar is empty, nothing happens.
   * @param barLocation the new location of the tool bar
   */
  public void setLocation(Location barLocation)
  {
    if (itemList.isEmpty())
      return;
    int barHeight = itemList.get(0).getHeight(0);
    int xCurrent = barLocation.x;  // Ulx
    for (int i = 0; i < itemList.size(); i++)
    {
      ToolBarItem item = itemList.get(i);
      int itemWidth = item.getWidth(0);
      int xCenter = xCurrent + itemWidth / 2;
      xCurrent += itemWidth;
      Location location = new Location(xCenter, barLocation.y + barHeight / 2);
      item.setLocation(location);
    }
  }

  /**
   * Registers a tool bar listener to get event notifications when an item
   * is touched.
   * @param listener the ToolBarListener to register
   */
  public void addToolBarListener(ToolBarListener listener)
  {
    toolBarListener = listener;
  }

}
