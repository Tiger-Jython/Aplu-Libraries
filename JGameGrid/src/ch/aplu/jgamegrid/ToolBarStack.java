// ToolBarStack.java

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

import java.util.*;

/**
 * Class that represents a stack of items (e.g. card symbols with the same suit (in
 * rank order). The stack is used to select a specific item by clicking
 * through the stack.
 */
public class ToolBarStack extends ToolBarItem
{
  private static ArrayList<ToolBarStack> instances = new ArrayList<ToolBarStack>();
  private int nbItems;

  /**
   * Creates a set of with given number of items taken from the given filename
   * template. A item stack is a single GameGrid actor with 2*nbItems sprite images.
   * <br><br>E.g.if card symbols are used, the first series of images with
   * id = 0..nbItems-1 corresponds to the cards
   * in rank order, the second series of images with id = nbItems..2*nbItems-1
   * to the cards images representing the selected card (circumscribed 
   * rectangle, grayed, etc.).<br><br>
   *
   * The actual filename of the sprite images is the given filename template
   * appended by _n with n from 0 to 2*nbItems-1. E.g. for nbItems = 3 and
   * filename = "sprites/spades.gif" the image files are "sprites/spades_0.gif",
   * "sprites/spades_1.gif", "sprites/spades_2.gif" for the normal images and 
   * "sprites/spades_3.gif", "sprites/spades_4.gif", "sprites/spades_5.gif"
   * for the selected images.
   * @param filename the filename template of the sprite images 
   * @param nbItems the number of ranks in this stack
   */
  public ToolBarStack(String filename, int nbItems)
  {
    super(filename, 2 * nbItems);
    this.nbItems = nbItems;
    instances.add(this);
  }

  /**
   * Returns the state of the item currently shown.
   * @return true, if the item is in selected state; otherwise false
   */
  public boolean isSelected()
  {
    return getIdVisible() >= nbItems;
  }

  /**
   * Sets/deselects the item currently shown.
   * @param b if true, the item is selected; otherwise deselected
   */
  public void setSelected(boolean b)
  {
    if (b)
      show(nbItems + getIdVisible() % nbItems);
    else
      show(getIdVisible() % nbItems);
  }

  /**
   * Returns the id of the item currently shown.
   * @return a number 0..nbItems-1 of the visible item (independent if selected or not)
   */
  public int getItemId()
  {
    return (getIdVisible() % nbItems);
  }

  /**
   * Shows the next item in the stack. The item state remains unchanged, if
   * the current item is selected/deselected the next item is selected/deselected too.
   */
  public void showNext()
  {
    if (isSelected())
      show(nbItems + (getIdVisible() + 1) % nbItems);
    else
      show((getIdVisible() + 1) % nbItems);
  }

  /**
   * Returns an array of all stacks that have the current item selected.
   * @return an array of all stacks with the visible items in selected state; it no
   * item is selected, the array has length zero (is not null)
   */
  public static ToolBarStack[] getSelectedStacks()
  {
    ArrayList<ToolBarStack> list = new ArrayList<ToolBarStack>();
    for (ToolBarStack stack : instances)
    {
      if (stack.isSelected())
        list.add(stack);
    }
    ToolBarStack[] stacks = new ToolBarStack[list.size()];
    for (int i = 0; i < list.size(); i++)
      stacks[i] = list.get(i);
    return stacks;
  }

  /**
   * Returns an array with length equals to the number of stacks. Each
   * array element is the id of the selected item currently visible or -1,
   * if the item currently visible is deselected.
   * @return an array of integer holding the ids of the selected items; -1 if
   * the visible item is not selected
   */
  public static int[] getSelectedItemIds()
  {
    int[] ids = new int[instances.size()];
    int i = 0;
    for (ToolBarStack stack : instances)
    {
      if (stack.isSelected())
        ids[i] = stack.getItemId();
      else
        ids[i] = -1;
      i++;
    }
    return ids;
  }
  
  protected static void initInstances()
  {
    instances.clear();
  }  

}
