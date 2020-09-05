// GGToggleButton.java

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

/**
 * Class that implements a toggle button (2-state button) actor with standard
 * behavior. Events are generated when the button changes its state due to
 * a mouse click. The button is centered in the middle of a cell.
 * Use Actor.setLocationOffset() to fine tune the button location.
 */
public class GGToggleButton extends GGButtonBase
{
  private boolean isToggled;
  private boolean isRollover = false;
  private boolean isInit = false;

  /**
   * Creates a button from the given sprite images. Multiple sprites are
   * assumed, using the implicite underline convention. If isRollover is true,
   * image_0.gif corresponds to the released state, image_1.gif to the
   * pressed state, image_2.gif to the rollover released state and image_3.gif
   * to the rollover pressed state. If isRollover is false,
   * only image_0.gif and image_1.gif are needed.
   * @param buttonImage the filename or URL of the 2 or 4 button sprites
   * @param isRollover if true, rollover imaged are used
   * @param isToggled if true, the button is initially toggled; otherwise it is untoggled
   */
  public GGToggleButton(String buttonImage, boolean isRollover, boolean isToggled)
  {
    super(buttonImage, null, isRollover ? 4 : 2);
    this.isRollover = isRollover;
    this.isToggled = isToggled;
  }

  /**
   * Same as GGToggleButton(String buttonImage, boolean isRollover, false) (untoggled).
   * @param buttonImage the filename or URL of the 2 or 4 button sprites
   */
  public GGToggleButton(String buttonImage, boolean isRollover)
  {
    this(buttonImage, isRollover, false);
  }

  /**
   * Same as GGToggleButton(String buttonImage, false, false) (no rollover, untoggled).
   * Only image_0.gif and image_1.gif are needed.
   * @param buttonImage the filename or URL of the 2 button sprites
   */
  public GGToggleButton(String buttonImage)
  {
    this(buttonImage, false, false);
  }

  /**
   * Overrides the actor's reset() called when the button is added to the game grid.
   */
  public void reset()
  {
    if (!isInit)
    {
      isInit = true;
      show(isToggled ? 1 : 0);
    }
  }

  /**
   * Returns the current state of the button.
   * @return true, if the button is toggled
   */
  public boolean isToggled()
  {
    return isToggled;
  }

  /**
   * Sets the button in the toggled/untoggled state.
   * Does not generate a notification event. If automatic refresh is enabled,
   * refreshs the game grid.
   * @param b if true, the button is toggled; otherwise it is untoggled
   */
  public void setToggled(boolean b)
  {
    isToggled = b;
    show(b ? 1 : 0);
    if (isRefreshEnabled())
      gameGrid.refresh();
  }

  /**
   * Registers a GGToggleButtonListener to get notifications when the button is manipulated.
   * @param listener the GGToggleButtonListener to register
   */
  public void addToggleButtonListener(GGToggleButtonListener listener)
  {
    super.addToggleButtonListener(listener, isRollover);
  }

  /**
   * Registers a GGOverButtonListener to get notifications when the mouse cursor
   * enters the active mouse area. The button must be created with type "Rollover".
   * @param listener the GGButtonOverListener to register
   */
  public void addButtonOverListener(GGButtonOverListener listener)
  {
    super.addButtonOverListener(listener);
  }

}
