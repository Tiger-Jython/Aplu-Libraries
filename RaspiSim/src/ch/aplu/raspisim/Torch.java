// Torch.java

/*
This software is part of the RaspiSim library.
It is Open Source Free Software, so you may
- run the code for any purpose
- study how the code works and adapt it to your needs
- integrate all or parts of the code in your own programs
- redistribute copies of the code
- improve the code and release your improvements to the public
However the use of the code is entirely your responsibility.

Author: Aegidius Pluess, www.aplu.ch
*/

package ch.aplu.raspisim;

import ch.aplu.jgamegrid.*;

/**
 * Class to represent a torch (spot light source) detectable by the light sensors.
 * Mouse pick and drag is enabled by default.
 */
public class Torch extends Actor
{
  private double power;
  private Location loc;
  private int height;
  
  /**
   * Creates a spot light source of given intensity above the given location.
   * @param power the intensity of the source (arbitrary units)
   * @param loc the initial locaction (x-, y-coordinates) of the source
   * @param height the initial height above the robot moving surface (z-coordinate, in pixels)
   */
  public Torch(double power, Location loc, int height)
  {
    super("sprites/torch.png");
    this.power = power;
    this.loc = loc;
    this.height = height;
  }

  /**
  * Returns the light intensity at given location using the 1 / r^2
  * law.
  * @param target the location where to report the intensity 
  * @return the light intensity
  */
  public double getIntensity(Location target)
  {
    double rsquare = (getX() - target.x) * (getX() - target.x) +
      (getY() - target.y) * (getY() - target.y) + 
      height * height;
    return 1E7 * power / rsquare;
  }

  /**
  * Returns the initial location of the torch actor.
  * @return the torch initial location
  */
  public Location getInitialLoc()
  {
    return loc;
  }

  /**
  * Sets the torch to given pixel location. The height
  * remains the same.
  * @param x the new pixel x-coordinate
  * @param y the new pixel y-coordinate
  */
  public void setPixelLocation(int x, int y)
  {
    setLocation(gameGrid.toLocationInGrid(x, y));
  }
  
 /**
  * Sets the height of the torch (z-coordinate).
  * @param height the new height above the robot moving surface
  */
  public void setHeight(int height)
  {
    this.height = height;
  }

}
