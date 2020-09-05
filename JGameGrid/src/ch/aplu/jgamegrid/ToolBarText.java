// ToolBarSeparator.java

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
 * A class to represent a text actor used for tool bar items.
 */
public class ToolBarText extends ToolBarItem
{

  /**
   * Creates a tool bar text item displaying the given text in black
   * centered in a white rectangle with given height.
   * The text font is SansSerif, PLAIN, 12 px.
   * @param text the text to display
   * @param height the height of the background rectangle in pixels
   */
  public ToolBarText(String text, int height)
  {
    this(text, Color.black, new Font("SansSerif", Font.PLAIN, 12),
      Color.white, height);
  }

  /**
   * Creates a tool bar text item displaying the given text with given font and color
   * centered in a rectangle with given color and height. The width of the rectangle
   * is adapted to the text font and text length.
   * @param text the text to display
   * @param textColor the color of the text
   * @param font the font of the text
   * @param bgColor the color of the background rectangle
   * @param height the height of the background rectangle in pixels
   */
  public ToolBarText(String text, Color textColor, Font font, Color bgColor, int height)
  {
    super(getTextImage(text, textColor, font, bgColor, height));
  }

  private static BufferedImage getTextImage(String text,
    Color textColor, Font font, Color bgColor, int height)
  {
    BufferedImage bi = // Dummy bi to get font and line metrics
      new BufferedImage(1, 1, Transparency.TRANSLUCENT);
    Graphics2D g = bi.createGraphics();
    FontRenderContext frc = g.getFontRenderContext();
    LineMetrics lm = font.getLineMetrics(text, frc);
    FontMetrics fm = g.getFontMetrics(font);
    int h = (int)Math.ceil(lm.getHeight());
    int size = fm.charsWidth(text.toCharArray(), 0, text.length());
    int width = size + 10;
    TextLayout textLayout = new TextLayout(text, font, frc);
    g.dispose();

    bi = new BufferedImage(width, height, Transparency.TRANSLUCENT);
    Graphics2D g2D = bi.createGraphics();
    g2D.setColor(bgColor);
    g2D.fillRect(0, 0, width, height);
    g2D.setColor(textColor);
    textLayout.draw(g2D, 4, height /2 + (int)(0.3 * h));
    return bi;
  }
}
