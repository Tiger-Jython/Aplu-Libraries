// TextActor.java
// ToDo: Laenge/Hoehe des Textfensters anpassen

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

import java.awt.font.*;
import java.awt.*;
import java.awt.image.BufferedImage;

/**
 * Class that implements an actor to display text dynamically. This may be a
 * better choice than displaying text as a sprite image, when the text changes
 * at run-time in many different ways.
 */
public class TextActor extends Actor
{
  private static class TextInfo
  {
    int textWidth;
    int textHeight;
  }
  private int textWidth;
  private int textHeight;
  private static TextInfo textInfo;

  /**
   * Constructs a possibly rotatable text actor that displays the given text with given text
   * and background colors. For transparent background use sRGB color with alpha = 0.
   * The text is horizontally left aligned, vertically center aligned.
   * @param isRotatable if true, the text changes the direction corresponding
   * to the actor's direction
   * @param text the text to display, if null or has length 0,
   * the text actor is completely transparent
   * @param textColor the color of the text
   * @param bgColor the background color of the text field
   * @param font the font used to display the text
   */
  public TextActor(boolean isRotatable, String text, Color textColor, Color bgColor, Font font)
  {
    super(isRotatable, createTextImage(text, textColor, bgColor, font));
    textWidth = textInfo.textWidth;
    textHeight = textInfo.textHeight;
  }

  /**
   * Constructs a unrotatable text actor that displays the given text with given text
   * and background colors. For transparent background use sRGB color with alpha = 0.
   * The text is horizontally left aligned, vertically center aligned.
   * @param text the text to display, if null or has length 0,
   * the text actor is completely transparent
   * @param textColor the color of the text
   * @param bgColor the background color of the text field
   * @param font the font used to display the text
   */
  public TextActor(String text, Color textColor, Color bgColor, Font font)
  {
    this(false, text, textColor, bgColor, font);
  }

  /**
   * Constructs a unrotatable text actor that displays the given text.
   * The text color is black, the background color 
   * white/transparent ARGB = (255, 255,255, 0) and the
   * font SansSerif, PLAIN, 8 pixels.
   * The text is horizontally left aligned, vertically center aligned.
   * @param text the text to display, if null or has length 0,
   * the text actor is completely transparent
   */
  public TextActor(String text)
  {
    this(false, text, Color.black, new Color(255, 255, 255, 0),
      new Font("SansSerif", Font.PLAIN, 12));
  }

  private static BufferedImage createTextImage(String text, Color textColor,
    Color bgColor, Font font)
  {
    textInfo = new TextInfo();
    BufferedImage bi = // Dummy bi to get font and line metrics
      new BufferedImage(1, 1, Transparency.TRANSLUCENT);
    Graphics2D g = bi.createGraphics();
    FontRenderContext frc = g.getFontRenderContext();
    LineMetrics lm = font.getLineMetrics(text, frc);
    FontMetrics fm = g.getFontMetrics(font);
    int textHeight = (int)Math.ceil(lm.getHeight());

    int height = textHeight + 2;
    int textWidth = 0;
    TextLayout textLayout = null;
    if (text != null && text.length() != 0)
    {
      textWidth = fm.charsWidth(text.toCharArray(), 0, text.length());
      textLayout = new TextLayout(text, font, frc);
    }
    else
      textWidth = 1;

    int width = 2 * textWidth;
    g.dispose();

    bi = new BufferedImage(width, height, Transparency.TRANSLUCENT);
    Graphics2D g2D = bi.createGraphics();

    // Transparent background of entire area
    g2D.setColor(new Color(bgColor.getRed(), bgColor.getGreen(), bgColor.getBlue(), 0));
    g2D.fillRect(0, 0, width, height);

    if (textLayout != null)
    {
      // Background of right part (user definable)
      g2D.setColor(bgColor);
      g2D.fillRect(textWidth, 0, textWidth, height);
      // Text in right part
      g2D.setColor(textColor);
      textLayout.draw(g2D, textWidth, (int)(0.8 * textHeight));
      textInfo.textWidth = textWidth;
    }
    else
      textInfo.textWidth = 0;

    textInfo.textHeight = textHeight;
    return bi;
  }

  /**
   * Returns the total width of the text.
   * Maybe used to align the text by calling setLocationOffset(),
   * @return the text width in pixels
   * @see Actor#setLocationOffset(Point pt)
   */
  public int getTextWidth()
  {
    return textWidth;
  }

  /**
   * Returns the height of the text.
   * Maybe used to align the text by calling setLocationOffset(),
   * @return the text height in pixels
   * @see Actor#setLocationOffset(Point pt)
   */
  public int getTextHeight()
  {
    return textHeight;
  }
}
