// GGButtonBase.java

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
import java.awt.*;

/**
 * Abstract superclass of all button types. Buttons of all types
 * are GameGrid actors with special behaviour. They implement a
 * GGMouseListener that reports press, release and click events
 * when the mouse curser is over the button image. The button
 * images for the non-pressed, the pressed or the rollover state
 * should have the same size. hide()  may be called before the button
 * is added to the game grid to make the button invisible (and inactive)
 * when added. The parameterless show() will make it visible (in non-pressed
 * state). Like any other actor, the buttons will be painted over or under
 * other actors depending on the paint order.<br><br>
 * The button events will be reported by callback methods declared in
 * the GGButtonListener interface.
 */
public abstract class GGButtonBase extends Actor
{
  // ------------------ Inner class MyMouseTouchListener ------------
  private class MyMouseTouchListener implements GGMouseTouchListener
  {
    public void mouseTouched(Actor actor, GGMouse mouse, java.awt.Point spot)
    {
      if (!isEnabled || gameGrid == null)
        return;
      switch (mouse.getEvent())
      {
        case GGMouse.lPress:
          if (myButton instanceof GGButton)
          {
            show(1);
            if (refreshEnabled)
              gameGrid.refresh();
            if (buttonListener != null)
              buttonListener.buttonPressed((GGButton)actor);
          }
          if (myButton instanceof GGToggleButton)
          {
            GGToggleButton btn = (GGToggleButton)myButton;
            btn.setToggled(!btn.isToggled());
            if (toggleButtonListener != null)
              toggleButtonListener.buttonToggled((GGToggleButton)actor, btn.isToggled());
          }
          if (myButton instanceof GGCheckButton)
          {
            GGCheckButton btn = (GGCheckButton)myButton;
            btn.setChecked(!btn.isChecked());
            if (checkButtonListener != null)
              checkButtonListener.buttonChecked((GGCheckButton)actor, btn.isChecked());
          }
          if (myButton instanceof GGRadioButton)
          {
            GGRadioButton btn = (GGRadioButton)myButton;
            if (btn.getButtonGroup() == null)  // not part of button group
              btn.setSelected(!btn.isSelected());
            if (radioButtonListener != null)
              radioButtonListener.buttonSelected((GGRadioButton)actor, btn.isSelected());
          }
          break;
        case GGMouse.lRelease:
          if (myButton instanceof GGButton)
          {
            show(0);
            if (refreshEnabled)
              gameGrid.refresh();
            if (buttonListener != null)
              buttonListener.buttonReleased((GGButton)actor);
          }
          break;
        case GGMouse.lClick:
          if (myButton instanceof GGButton)
          {
            if (buttonListener != null)
              buttonListener.buttonClicked((GGButton)actor);
            break;
          }
        case GGMouse.enter:
          if (myButton instanceof GGButton)
          {
            show(2);
            if (refreshEnabled)
              gameGrid.refresh();
            if (buttonOverListener != null)
              buttonOverListener.buttonEntered((GGButton)actor);
          }
          if (myButton instanceof GGToggleButton)
          {
            GGToggleButton btn = (GGToggleButton)myButton;
            show(btn.isToggled() ? 3 : 2);
            if (refreshEnabled)
              gameGrid.refresh();
            if (buttonOverListener != null)
              buttonOverListener.buttonEntered((GGButton)actor);
          }
          break;
        case GGMouse.leave:
          if (myButton instanceof GGButton)
          {
            show(0);
            if (refreshEnabled)
              gameGrid.refresh();
            if (buttonOverListener != null)
              buttonOverListener.buttonExited((GGButton)actor);
          }
          if (myButton instanceof GGToggleButton)
          {
            GGToggleButton btn = (GGToggleButton)myButton;
            show(btn.isToggled() ? 1 : 0);
            if (refreshEnabled)
              gameGrid.refresh();
            if (buttonOverListener != null)
              buttonOverListener.buttonExited((GGButton)actor);
          }
          break;
      }
    }
  }
  // ------------------ End of inner class ----------------
  //
  private MyMouseTouchListener mouseTouchListener = new MyMouseTouchListener();
  private GGButtonListener buttonListener = null;
  private GGButtonOverListener buttonOverListener = null;
  private GGToggleButtonListener toggleButtonListener = null;
  private GGCheckButtonListener checkButtonListener = null;
  private GGRadioButtonListener radioButtonListener = null;
  private GGButtonBase myButton;
  private boolean isEnabled = true;
  private boolean refreshEnabled = true;

  protected GGButtonBase(BufferedImage... spriteImages)
  {
    super(spriteImages);
    myButton = this;
  }

  protected GGButtonBase(String buttonImage, String text, int nbSprites)
  {
    super(buttonImage, nbSprites);
    myButton = this;
  }

  protected void addButtonListener(GGButtonListener listener, boolean isRollover)
  {
    buttonListener = listener;
    int mouseMask = GGMouse.lPress | GGMouse.lRelease | GGMouse.lClick;
    if (isRollover)
      mouseMask = mouseMask | GGMouse.enter | GGMouse.leave;
    addMouseTouchListener(mouseTouchListener, mouseMask);
    setMouseTouchRectangle(new Point(0, 0), getWidth(0), getHeight(0));
  }

  protected void addButtonOverListener(GGButtonOverListener listener)
  {
    buttonOverListener = listener;
  }

  protected void addToggleButtonListener(GGToggleButtonListener listener,
    boolean isRollover)
  {
    toggleButtonListener = listener;
    int mouseMask = GGMouse.lPress;
    if (isRollover)
      mouseMask = mouseMask | GGMouse.enter | GGMouse.leave;
    addMouseTouchListener(mouseTouchListener, mouseMask);
    setMouseTouchRectangle(new Point(0, 0), getWidth(0), getHeight(0));
  }

  protected void addCheckButtonListener(GGCheckButtonListener listener)
  {
    checkButtonListener = listener;
    int mouseMask = GGMouse.lPress;
    addMouseTouchListener(mouseTouchListener, mouseMask);
    setMouseTouchRectangle(new Point(0, 0), getHeight(0), getHeight(0));
  }

  protected void addRadioButtonListener(GGRadioButtonListener listener)
  {
    radioButtonListener = listener;
    int mouseMask = GGMouse.lPress;
    addMouseTouchListener(mouseTouchListener, mouseMask);
    setMouseTouchCircle(new Point(0, 0), getHeight(0));
  }

  /**
   * Enable/Disable button actions. The button image is not changed.
   * @param enable if true, the button actions are enabled; otherwise disabled
   */
  public void setEnabled(boolean enable)
  {
    isEnabled = enable;
  }

  /**
   * Returns true if button is enabled.
   * @return true if button is enabled; otherwise false
   */
  public boolean isEnabled()
  {
    return isEnabled;
  }

  /**
   * Enables/disables automatic refresh of game grid on button actions.
   * If the game grid is refreshed by the simulation cycling, it is advisable
   * to disabled the automatic refresh on button actions to avoid flickering.
   * @param enable if true, the automatic refresh on button action is enable;
   * otherwise disabled
   */
  public void setRefreshEnabled(boolean enable)
  {
    refreshEnabled = enable;
  }

  protected boolean isRefreshEnabled()
  {
    return refreshEnabled;
  }

  /**
   * Selects the rectangle (in pixel units) relative to the button image as
   * active mouse area. The following coordinate system is used:<br><br>
   * x-axis to the left, y-axis downward, zero at image center<br><br>
   * (For even image pixel width or height, the center is half pixel width
   * to the left or resp. to the top).<br><br>
   * The default hotspot area is the button image bounding rectangle.
   * @param center the rectangle center (zero at image center)
   * @param width the width in pixel units of the rectangle (in x-direction)
   * @param height the height in pixel units of the rectangle (in y-direction)
   */
  public void setHotspotArea(Point center, int width, int height)
  {
    setMouseTouchRectangle(center, width, height);
  }
}
