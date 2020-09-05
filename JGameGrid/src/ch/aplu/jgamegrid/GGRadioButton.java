// GGRadioButton.java

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

import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.font.*;

/**
 * Class that implements a radio button actor with standard behavior. Events
 * are generated when the button changes its state due to a mouse click
 * from selected to deselected or vise versa.
 * In order to align several radio buttons, the actor's image is centered
 * in the middle of the radio box and extends the same size to the left and right
 * determined by the text length. The left part is always transparent.
 * The default hot spot area is the bounding square of the circle.
 * Use Actor.setLocationOffset() to fine tune the button location.
 */
public class GGRadioButton extends GGButtonBase
{
  private boolean isSelected;
  private GGRadioButtonGroup buttonGroup = null;
  private boolean isInit = false;

  /**
   * Creates a deselected radio button with the given text annotation, black text and
   * white background colors.
   * @param text the text annotation
   */
  public GGRadioButton(String text)
  {
    this(text, Color.black, Color.white, false);
  }

  /**
   * Creates a radio button with the given text annotation, black text and
   * and white background colors.
   * @param text the text annotation
   * @param isSelected if true, the radio button is initially selected
   */
  public GGRadioButton(String text, boolean isSelected)
  {
    this(text, Color.black, Color.white, isSelected);
  }

  /**
   * Creates a deselected radio button with the given text annotation and given
   * text and background colors.
   * @param text the text annotation
   * @param textColor the color of the annotation text
   * @param bkColor the background color
   */
  public GGRadioButton(String text, Color textColor, Color bkColor)
  {
    this(text, textColor, bkColor, false);
  }

  /**
   * Creates a radio button with the given text annotation and given
   * text and background colors.
   * @param text the text annotation
   * @param textColor the color of the annotation text
   * @param bkColor the background color
   * @param isSelected if true, the radio button is initially selected
   */
  public GGRadioButton(String text, Color textColor, Color bkColor,
    boolean isSelected)
  {
    super(createImages(text, textColor, bkColor));
    this.isSelected = isSelected;
  }

  /**
   * Returns the current state of the button.
   * @return true, if the button is selected
   */
  public boolean isSelected()
  {
    return isSelected;
  }

  /**
   * Sets the button in the selected/deselected state.
   * If the button is selected and it is part of a radio button group,
   * any other selected buttons in the group are deselected.
   * Does not generate a notification event. If automatic refresh is enabled,
   * refreshs the game grid.
   * @param selected if true, the button is selected; otherwise it is deselected
   */
  public void setSelected(boolean selected)
  {
    isSelected = selected;
    show(selected ? 1 : 0);
    if (isRefreshEnabled())
      gameGrid.refresh();

    if (buttonGroup != null)
    {
      GGRadioButton selectedButton = buttonGroup.getSelectedButton();
      if (selected)
      {
        // Deselect already selected button
        if (selectedButton != null && selectedButton != this)
          selectedButton.setSelected(false);
        // set selected button
        buttonGroup.setSelectedButton(this);
      }
      else // deselect selected button
      {
        if (selectedButton == this)
          buttonGroup.setSelectedButton(null);  // No selected button
      }
    }
  }

  /**
   * Overrides the actor's reset() called when the button is added to the game grid.
   */
  public void reset()
  {
    if (!isInit)
    {
      isInit = true;
      show(isSelected ? 1 : 0);
    }
  }

  /**
   * Registers a GGRadioListener to get notifications when the button is manipulated.
   * @param listener the GGRadioButtonListener to register
   */
  public void addRadioButtonListener(GGRadioButtonListener listener)
  {
    super.addRadioButtonListener(listener);
  }

  protected void setButtonGroup(GGRadioButtonGroup group)
  {
    if (buttonGroup != null && buttonGroup != group)
      GameGrid.fail("Error while adding a GGRadioButton into a button group." 
        + "\nA radio button cannot be contained in more than one group."
        + "\nApplication will terminate.");
    buttonGroup = group;
  }

  protected GGRadioButtonGroup getButtonGroup()
  {
    return buttonGroup;
  }

  private static BufferedImage[] createImages(String text, Color textColor,
    Color bgColor)
  {
    Font font = new Font("SansSerif", Font.PLAIN, 11);
    BufferedImage bi = // Dummy bi to get the line metrics
      new BufferedImage(1, 1, Transparency.TRANSLUCENT);
    Graphics2D g = bi.createGraphics();
    FontRenderContext frc = g.getFontRenderContext();
    LineMetrics lm = font.getLineMetrics(text, frc);
    FontMetrics fm = g.getFontMetrics(font);
    int h = (int)Math.ceil(lm.getHeight());
    int height = h + 2;
    int size = fm.stringWidth(text);
    int width = 2 * height + 2 * size;
    TextLayout textLayout = new TextLayout(text, font, frc);
    g.dispose();

    BufferedImage[] bis = new BufferedImage[2];

    for (int i = 0; i < 2; i++)
    {
      bis[i] = new BufferedImage(width, height, Transparency.TRANSLUCENT);
      Graphics2D g2D = bis[i].createGraphics();
      // Transparent background of entire area
      g2D.setColor(new Color(bgColor.getRed(), bgColor.getGreen(), bgColor.getBlue(), 0));
      g2D.fillRect(0, 0, width, height);
      // Background of right part (user definable)
      g2D.setColor(bgColor);
      g2D.fillRect(width / 2 - h / 2, 0, width / 2 + h / 2, height);
      // Text in right part
      g2D.setColor(textColor);
      textLayout.draw(g2D, width / 2 + h, h - 2);
      // Selected radio button: center spot
      g2D.drawArc(width / 2 - h / 2, 0, h, h, 0, 360);
      // Point in radio button
      if (i == 1)
        g2D.fillArc(width / 2 - 3, 4, 6, 6, 0, 360);
    }
    return bis;
  }
}
