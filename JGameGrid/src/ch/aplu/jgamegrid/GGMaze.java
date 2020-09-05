// GGMaze.java

/*
This software is part of the JGameGrid package.
It is Open Source Free Software, so you may
- run the code for any purpose
- study how the code works and adapt it to your needs
- integrate all or parts of the code in your own programs
- redistribute copies of the code
- improve the code and release your improvements to the public
However the use of the code is entirely your responsibility.

Author: Carol Hamer from the book 'Creating Mobile Games'
        adapted by Aegidius Pluess, www.aplu.ch
*/

package ch.aplu.jgamegrid;

import java.util.Random;
import java.util.Vector;

/**
 * This class creates a random maze in a two-dimensional grid of cells.
 * The  maze has one entry and one exit cell and one pass (no cycles).<br>
 * For details about the algorithm used to create the maze, please consult the
 * book 'Creating Mobile Games' by Carol Hamer, from where the code is
 * copied with acknowledgment to the autor.
 *
 */
public class GGMaze
{
  private int width;
  private int height;

  private Random myRandom = new Random();

  /**
   * data for which squares are filled and which are blank.
   * 0 = black
   * 1 = white
   * values higher than 1 are used during the maze creation 
   * algorithm.
   * 2 = the square could possibly be appended to the maze this round.
   * 3 = the square will be white but is not close enough to be appended to the maze this round.
   */
  private int[][] mySquares;

  /**
   * Creates a new random maze with given number of horizontal an vertical cells.
   * @param width the number of horizontal cells (must be odd)
   * @param height the number of vertical cells (must be odd)
   */
  public GGMaze(int width, int height)
  {
    this.width = width;
    this.height = height;
    mySquares = new int[width][height];
    // initialize all of the squares to white except a lattice 
    // framework of black squares.
    for (int i = 1; i < width - 1; i++)
    {
      for (int j = 1; j < height - 1; j++)
      {
        if (((i & 0x1) != 0) || ((j & 0x1) != 0))
        {
          mySquares[i][j] = 1;
        }
      }
    }
    // the entrance to the maze is at (0,1).
    mySquares[0][1] = 1;
    createMaze();
    mySquares[width - 1][height - 2] = 1;  // Exit cell
  }

  private void createMaze()
  {
    // create an initial framework of black squares.
    for (int i = 1; i < mySquares.length - 1; i++)
    {
      for (int j = 1; j < mySquares[i].length - 1; j++)
      {
        if (((i + j) & 0x1) != 0)
        {
          mySquares[i][j] = 0;
        }
      }
    }
    // initialize the squares that will be white and act
    // as vertices: set the value to 3 which means the 
    // square has not been connected to the maze tree.
    for (int i = 1; i < mySquares.length - 1; i += 2)
    {
      for (int j = 1; j < mySquares[i].length - 1; j += 2)
      {
        mySquares[i][j] = 3;
      }
    }
    // Then those squares that can be selected to be open 
    // (white) paths are given the value of 2.  
    // We randomly select the square where the tree of maze 
    // paths will begin.  The maze is generated starting from 
    // this initial square and branches out from here in all 
    // directions to fill the maze grid.  
    Vector possibleSquares = new Vector(mySquares.length * mySquares[0].length);
    int[] startSquare = new int[2];
    startSquare[0] = getRandomInt(mySquares.length / 2) * 2 + 1;
    startSquare[1] = getRandomInt(mySquares[0].length / 2) * 2 + 1;
    mySquares[startSquare[0]][startSquare[1]] = 2;
    possibleSquares.addElement(startSquare);
    // Here we loop to select squares one by one to append to 
    // the maze pathway tree.
    while (possibleSquares.size() > 0)
    {
      // the next square to be joined on is selected randomly.
      int chosenIndex = getRandomInt(possibleSquares.size());
      int[] chosenSquare = (int[])possibleSquares.elementAt(chosenIndex);
      // we set the chosen square to white and then 
      // remove it from the list of possibleSquares (i.e. squares 
      // that can possibly be added to the maze), and we link 
      // the new square to the maze.
      mySquares[chosenSquare[0]][chosenSquare[1]] = 1;
      possibleSquares.removeElementAt(chosenIndex);
      link(chosenSquare, possibleSquares);
    }
    // now that the maze has been completely generated, we 
    // throw away the objects that were created during the 
    // maze creation algorithm and reclaim the memory.
    possibleSquares = null;
    System.gc();
  }

  /**
   * internal to createMaze.  Checks the four squares surrounding 
   * the chosen square.  Of those that are already connected to 
   * the maze, one is randomly selected to be joined to the 
   * current square (to attach the current square to the 
   * growing maze).  Those squares that were not previously in 
   * a position to be joined to the maze are added to the list 
   * of "possible" squares (that could be chosen to be attached 
   * to the maze in the next round).
   */
  private void link(int[] chosenSquare, Vector possibleSquares)
  {
    int linkCount = 0;
    int i = chosenSquare[0];
    int j = chosenSquare[1];
    int[] links = new int[8];
    if (i >= 3)
    {
      if (mySquares[i - 2][j] == 1)
      {
        links[2 * linkCount] = i - 1;
        links[2 * linkCount + 1] = j;
        linkCount++;
      }
      else if (mySquares[i - 2][j] == 3)
      {
        mySquares[i - 2][j] = 2;
        int[] newSquare = new int[2];
        newSquare[0] = i - 2;
        newSquare[1] = j;
        possibleSquares.addElement(newSquare);
      }
    }
    if (j + 3 <= mySquares[i].length)
    {
      if (mySquares[i][j + 2] == 3)
      {
        mySquares[i][j + 2] = 2;
        int[] newSquare = new int[2];
        newSquare[0] = i;
        newSquare[1] = j + 2;
        possibleSquares.addElement(newSquare);
      }
      else if (mySquares[i][j + 2] == 1)
      {
        links[2 * linkCount] = i;
        links[2 * linkCount + 1] = j + 1;
        linkCount++;
      }
    }
    if (j >= 3)
    {
      if (mySquares[i][j - 2] == 3)
      {
        mySquares[i][j - 2] = 2;
        int[] newSquare = new int[2];
        newSquare[0] = i;
        newSquare[1] = j - 2;
        possibleSquares.addElement(newSquare);
      }
      else if (mySquares[i][j - 2] == 1)
      {
        links[2 * linkCount] = i;
        links[2 * linkCount + 1] = j - 1;
        linkCount++;
      }
    }
    if (i + 3 <= mySquares.length)
    {
      if (mySquares[i + 2][j] == 3)
      {
        mySquares[i + 2][j] = 2;
        int[] newSquare = new int[2];
        newSquare[0] = i + 2;
        newSquare[1] = j;
        possibleSquares.addElement(newSquare);
      }
      else if (mySquares[i + 2][j] == 1)
      {
        links[2 * linkCount] = i + 1;
        links[2 * linkCount + 1] = j;
        linkCount++;
      }
    }
    if (linkCount > 0)
    {
      int linkChoice = getRandomInt(linkCount);
      int linkX = links[2 * linkChoice];
      int linkY = links[2 * linkChoice + 1];
      mySquares[linkX][linkY] = 1;
      int[] removeSquare = new int[2];
      removeSquare[0] = linkX;
      removeSquare[1] = linkY;
      possibleSquares.removeElement(removeSquare);
    }
  }

  /**
   * a randomization utility. 
   * @param upper the upper bound for the random int.
   * @return a random non-negative int less than the bound upper.
   */
  private int getRandomInt(int upper)
  {
    int retVal = myRandom.nextInt() % upper;
    if (retVal < 0)
    {
      retVal += upper;
    }
    return (retVal);
  }

  /**
   * Returns the location of the entry cell.
   * @return the location of the cell where to enter into the maze
   */
  public Location getStartLocation()
  {
    return new Location(0, 1);
  }
  
  /**
   * Returns the location of the exit cell.
   * @return the location of the cell where to leave the maze
   */
  public Location getExitLocation()
  {
    return new Location(width - 1, height - 2);
  }

  /**
   * Returns true, if the cell with given cell location is a wall
   * @param location the location of the requested cell
   * @return true, if the cell is a wall; false, if it is a gangway
   */
  public boolean isWall(Location location)
  {
     return mySquares[location.x][location.y] == 0 ? true : false;
  }

}
