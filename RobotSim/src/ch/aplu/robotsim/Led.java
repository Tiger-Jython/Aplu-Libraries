// Led.java
// Simulation mode only

/*
 This software is part of the RobotSim library.
 It is Open Source Free Software, so you may
 - run the code for any purpose
 - study how the code works and adapt it to your needs
 - integrate all or parts of the code in your own programs
 - redistribute copies of the code
 - improve the code and release your improvements to the public
 However the use of the code is entirely your responsibility.

 Author: Aegidius Pluess, www.aplu.ch
 */
package ch.aplu.robotsim;

import ch.aplu.jgamegrid.*;

/**
 * Class that represents one of the Led.
 */
public class Led extends Part
{

  private class Flasher extends Thread
  {
    private Actor actor;

    public Flasher(Actor actor)
    {
      this.actor = actor;
    }

    public void run()
    {
      while (isRunning)
      {
        if (isDoubleFlashing)
        {
          actor.show(colorID);
          delay(200);
          actor.hide();
          delay(400);
          actor.show(colorID);
          delay(200);
          actor.hide();
          delay(600);
        }
        else if (isSingleFlashing)
        {
          actor.show(colorID);
          delay(500);
          actor.hide();
          delay(500);
        }
        else
        {
          delay(500);
        }

      }
//      System.out.println("Flasher thread terminated");
    }

    void delay(int ms)
    {
      try
      {
        Thread.currentThread().sleep(ms);
      }
      catch (InterruptedException ex)
      {
      }
    }
  }

  private static final Location pos = new Location(-17, 0);
  private int colorID = -1; // off
  private Flasher flasher = null;
  private boolean isRunning = false;
  private boolean isDoubleFlashing = false;
  private boolean isSingleFlashing = false;

  /**
   * Creates a Led instance.
   */
  public Led()
  {
    super("sprites/ev3led.gif", pos, 3);  // green, red, lightred
    hide();
  }

  protected void cleanup()
  {
    removeSelf();
    gameGrid.refresh();
    if (flasher != null)
    {
      isRunning = false;
      try
      {
        flasher.join();
      }
      catch (InterruptedException ex)
      {
      }
    }
  }

  public void setLED(int state)
  {
    stopFlashing();
    if (state < 0 || state > 9)
      return;
    switch (state)
    {
      case 0:  // off
        colorID = -1;
        hide();
        break;
      case 1:  // green
        colorID = 0;
        show(colorID);
        break;
      case 2:  // red
        colorID = 1;
        show(colorID);
        break;
      case 3:  // lightred
        colorID = 2;
        show(colorID);
        break;
      case 4:  // green flashing
        colorID = 0;
        startFlashing(false);
        break;
      case 5:  // red flashing
        colorID = 1;
        startFlashing(false);
        break;
      case 6:  // lightred flashing
        colorID = 2;
        startFlashing(false);
        break;
      case 7:  // green double flashing
        colorID = 0;
        startFlashing(true);
        break;
      case 8:  // red double flashing
        colorID = 1;
        startFlashing(true);
        break;
      case 9:  // lightred double flashing
        colorID = 2;
        startFlashing(true);
        break;
    }
  }

  private void startFlashing(boolean doubleFlashing)
  {
    if (flasher == null)
    {
      isRunning = true;
      flasher = new Flasher(this);
      flasher.setDaemon(true);
      flasher.start();
    }
    if (doubleFlashing)
      isDoubleFlashing = true;
    else
      isSingleFlashing = true;
  }

  private void stopFlashing()
  {
    isSingleFlashing = isDoubleFlashing = false;
  }

}
