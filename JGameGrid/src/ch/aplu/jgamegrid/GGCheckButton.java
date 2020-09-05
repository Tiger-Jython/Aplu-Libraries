// GGCheckButton.java

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
 * Class that implements a check button actor with standard behavior. Events
 * are generated when the button changes its state due to a mouse click 
 * from checked to unchecked or vise versa.
 * In order to align several check buttons, the actor's image is centered
 * in the middle of the check box and extends the same size to the left and right
 * determined by the text length. The left part is always transparent.
 * The default hot spot area is the square that surrounds the cross.
 * Use Actor.setLocationOffset() to fine tune the button location.
 */
public class GGCheckButton extends GGButtonBase
{
  private boolean isChecked;
  private boolean isInit = false;

  /**
   * Creates a unchecked check button with the given text annotation, black text and
   * white background colors.
   * @param text the text annotation
   */
  public GGCheckButton(String text)
  {
    this(text, Color.black, Color.white, false);
  }

  /**
   * Creates a check button with the given text annotation, black text and
   * and white background colors.
   * @param text the text annotation
   * @param isChecked if true, the check box is initially checked
   */
  public GGCheckButton(String text, boolean isChecked)
  {
    this(text, Color.black, Color.white, isChecked);
  }

  /**
   * Creates a unchecked check button with the given text annotation and given
   * text and background colors. For transparent background use sRGB
   * color with alpha = 0.
   * @param text the text annotation
   * @param textColor the color of the annotation text
   * @param bkColor the background color
   */
  public GGCheckButton(String text, Color textColor, Color bkColor)
  {
    this(text, textColor, bkColor, false);
  }

  /**
   * Creates a check button with the given text annotation and given
   * text and background colors. For transparent background use sRGB
   * color with alpha = 0.
   * @param text the text annotation
   * @param textColor the color of the annotation text
   * @param bkColor the background color
   * @param isChecked if true, the check box is initially checked
   */
  public GGCheckButton(String text, Color textColor, Color bkColor,
    boolean isChecked)
  {
    super(createImages(text, textColor, bkColor));
    this.isChecked = isChecked;
  }

 /**
   * Overrides the actor's reset() called when the button is added to the game grid.
   */
  public void reset()
  {
    if (!isInit)
    {
      isInit = true;
      show(isChecked ? 1 : 0);
    }
  }

  /**
   * Returns the current state of the button.
   * @return true, if the button is checked
   */
  public boolean isChecked()
  {
    return isChecked;
  }

  /**
   * Sets the button in the checked/unchecked state.
   * Does not generate a notification event. If automatic refresh is enabled,
   * refreshs the game grid.
   * @param b if true, the button is checked; otherwise it is unchecked
   */
  public void setChecked(boolean b)
  {
    isChecked = b;
    show(b ? 1 : 0);
    if (isRefreshEnabled())
      gameGrid.refresh();
  }

  /**
   * Registers a GGCheckButtonListener to get notifications when the button is manipulated.
   * @param listener the GGCheckButtonListener to register
   */
  public void addCheckButtonListener(GGCheckButtonListener listener)
  {
    super.addCheckButtonListener(listener);
  }

  private static BufferedImage[] createImages(String text, Color textColor,
    Color bgColor)
  {
    Font font = new Font("SansSerif", Font.PLAIN, 11);
    BufferedImage bi = // Dummy bi to get font and line metrics
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
      // Check box in middle
      g2D.drawRect(width / 2 - h / 2, 0, h, h);
      // Checked check button: cross in check box
      if (i == 1)
      {
        g2D.drawLine(width / 2 - h / 2, 0, width / 2 + h / 2, h);
        g2D.drawLine(width / 2 - h / 2, h, width / 2 + h / 2, 0);
      }
    }
    return bis;
  }
}
