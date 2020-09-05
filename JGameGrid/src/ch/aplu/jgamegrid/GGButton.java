// GGButtonA.java

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
 * Class that implements a button actor with standard behavior. Button events
 * 'left entered', 'left exited', 'left pressed', 'left released' and 'left clicked' are
 * reported by a registered GGButtonListener.<br><br>
 * The button is centered in the middle of a cell. Use Actor.setLocationOffset() to fine tune
 * the button location.<br><br>3 sprites are used: one for the released state, one
 * for the pressed state and one for the rollover state.
 */
public class GGButton extends GGButtonBase
{
  private boolean isRollover = false;

  /**
   * Creates a button from the given sprite images. Multiple sprites are
   * assumed, using the implicite underline convention. If isRollover is true,
   * image_0.gif corresponds to the released state, image_1.gif to the
   * pressed state and image_2.gif to the rollover state. If isRollover is false,
   * only image_0.gif and image_1.gif are needed.
   * @param buttonImage the filename or URL of the 2 or 3 button sprites
   * @param isRollover if true, a rollover image is used
   */
  public GGButton(String buttonImage, boolean isRollover)
  {
    super(buttonImage, null, isRollover ? 3 : 2);
    this.isRollover = isRollover;
  }

  /**
   * Same as GGButton(String buttonImage, false) (no rollover).
   * Only image_0.gif and image_1.gif are needed.
   * @param buttonImage the filename or URL of the 2 button sprites
   */
  public GGButton(String buttonImage)
  {
    super(buttonImage, null, 2);
  }

  /**
   * Returns the current state of the ButtonA.
   * @return true, if the button is pressed; otherwise false
   */
  public boolean isPressed()
  {
    return getIdVisible() == 1 ? true : false;
  }

  /**
   * Registers a GGButtonListener to get notifications when the button 
   * is manipulated. The default hotspot area is the button image
   * bounding rectangle.
   * @param listener the GGButtonListener to register
   */
  public void addButtonListener(GGButtonListener listener)
  {
    super.addButtonListener(listener, isRollover);
  }

  /**
   * Registers a GGOverButtonListener to get notifications when the mouse cursor
   * enters the active mouse area.
   * @param listener the GGButtonOverListener to register
   */
  public void addButtonOverListener(GGButtonOverListener listener)
  {
    super.addButtonOverListener(listener);
  }

}
