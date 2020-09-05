// GameGridBeanInfo.java

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

import java.beans.*;

/**
 * A bean info class derived from SimpleBeanInfo in order to restrict the
 * visible properties of bean class GameGrid
 */
public class GameGridBeanInfo extends SimpleBeanInfo
{
  /**
   * Return the descriptor of visible properties.
   */
  public PropertyDescriptor[] getPropertyDescriptors()
  {
    PropertyDescriptor pds[] = null;
    try
    {
      pds = new PropertyDescriptor[]
      {
        new PropertyDescriptor("nbHorzCells", GameGrid.class),
        new PropertyDescriptor("nbVertCells", GameGrid.class),
        new PropertyDescriptor("cellSize", GameGrid.class),
        new PropertyDescriptor("gridColor", GameGrid.class),
        new PropertyDescriptor("bgImagePath", GameGrid.class),
        new PropertyDescriptor("bgColor", GameGrid.class),
        new PropertyDescriptor("bgImagePosX", GameGrid.class),
        new PropertyDescriptor("bgImagePosY", GameGrid.class),
        new PropertyDescriptor("simulationPeriod", GameGrid.class),
        new PropertyDescriptor("nbRotSprites", GameGrid.class),
      };
    }
    catch (IntrospectionException e)
    {
      e.printStackTrace();
    }
    return pds;
  }
}