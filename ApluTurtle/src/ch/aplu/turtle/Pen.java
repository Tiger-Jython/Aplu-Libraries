// Pen.java

/*
 This software is part of the JEX (Java Exemplarisch) Utility Library.
 It is Open Source Free Software, so you may
 - run the code for any purpose
 - study how the code works and adapt it to your needs
 - integrate all or parts of the code in your own programs
 - redistribute copies of the code
 - improve the code and release your improvements to the public
 However the use of the code is entirely your responsibility.
 */
package ch.aplu.turtle;

import java.awt.Color;
import java.awt.BasicStroke;
import java.awt.Stroke;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.GraphicsEnvironment;
import java.awt.Transparency;
import java.awt.font.FontRenderContext;
import java.awt.font.LineMetrics;
import java.awt.image.BufferedImage;

/**   
 * The Pen class provides anything used for drawing the lines, such as line width,
 * pen color, end caps, dashed lines, etc.
 */
public class Pen
{
  /**
   Text information.
   */
  public static class TextInfo
  {
    /** Length of text in pixels */
    public int width;
    /** Height of text in pixels */
    public int height;
    /** Ascender height of text in pixels */
    public int ascent;
    /** Descender height of text in pixels */
    public int descent;
  }

  /** 
   * The default font that is used when drawing text.
   * First argument must be one of "Serif", "SansSerif", "Monotyped", "Dialog" or "DialogInput"
   * to guarantee that this font exists on all systems.
   * @see java.awt.Font for more information, e.g. on font styles.
   */
  public static Font DEFAULT_FONT = new Font("SansSerif", Font.PLAIN, 24);
  private Color color;
  private Color fillColor;
  private BasicStroke stroke;
  private Font font;

  /** 
   * Constructor with standard pen color (black) and and standard Stroke.
   * Fill colo same as pen color.
   * @see java.awt.BasicStroke
   */
  public Pen()
  {
    color = Color.black;
    setFillColor(Color.black);
    stroke = new BasicStroke();
    font = DEFAULT_FONT;
  }

  /**  
   * Constructor with given <code>color</code> and standard Stroke.
   * Fill color same as pen color,
   * @see java.awt.BasicStroke
   */
  public Pen(Color color)
  {
    this.color = color;
    setFillColor(color);
    stroke = new BasicStroke();
    font = DEFAULT_FONT;
  }

  /** 
   * Queries the <code>Pen</code>s color.
   */
  public Color getColor()
  {
    return color;
  }

  /** 
   * Sets the <code>Pen</code>s color.
   */
  public void setColor(Color color)
  {
    this.color = color;
  }

  /** 
   * Sets the <code>Pen</code>s fill color.
   */
  public void setFillColor(Color color)
  {
    this.fillColor = color;
  }

  /** 
   * Queries the <code>Pen</code>s fill color.
   */
  public Color getFillColor()
  {
    return this.fillColor;
  }

  /** 
   * Gets the <code>Pen</code>s <code>Stroke</code>
   * @see BasicStroke
   * @see Stroke
   */
  public Stroke getStroke()
  {
    return stroke;
  }

  /** 
   * Queries the <code>Pen</code>s line width
   */
  public float getLineWidth()
  {
    return stroke.getLineWidth();
  }

  /** 
   * Queries the <code>Pen</code>s end cap style.
   * @see java.awt.BasicStroke
   */
  public int getEndCap()
  {
    return stroke.getEndCap();
  }

  /** 
   * Queries the <code>Pen</code>s line join style.
   * @see java.awt.BasicStroke
   */
  public int getLineJoin()
  {
    return stroke.getLineJoin();
  }

  /** 
   * Queries the <code>Pen</code>s miter limit style.
   * @see java.awt.BasicStroke
   */
  public float getMiterLimit()
  {
    return stroke.getMiterLimit();
  }

  /** 
   * Queries the <code>Pen</code>s dash array.
   * @see java.awt.BasicStroke
   */
  public float[] getDashArray()
  {
    return stroke.getDashArray();
  }

  /** 
   * Queries the <code>Pen</code>s dash phase.
   * @see java.awt.BasicStroke
   */
  public float getDashPhase()
  {
    return stroke.getDashPhase();
  }

  /** 
   * Sets the <code>Pen</code>s line width. 
   */
  public void setLineWidth(float width)
  {
    stroke = new BasicStroke((float)width,
      stroke.getEndCap(),
      stroke.getLineJoin(),
      stroke.getMiterLimit(),
      stroke.getDashArray(),
      stroke.getDashPhase());
  }

  /** 
   * Sets the <code>Pen</code>s end cap style.
   * @see java.awt.BasicStroke
   */
  public void setEndCap(int endCap)
  {
    stroke = new BasicStroke(stroke.getLineWidth(),
      endCap,
      stroke.getLineJoin(),
      stroke.getMiterLimit(),
      stroke.getDashArray(),
      stroke.getDashPhase());
  }

  /** 
   * Sets the <code>Pen</code>s line join style.
   * @see java.awt.BasicStroke
   */
  public void setLineJoin(int join)
  {
    stroke = new BasicStroke(stroke.getLineWidth(),
      stroke.getEndCap(),
      join,
      stroke.getMiterLimit(),
      stroke.getDashArray(),
      stroke.getDashPhase());
  }

  /** 
   * Sets the <code>Pen</code>s miter limit.
   * @see java.awt.BasicStroke
   */
  public void setMiterLimit(float miterlimit)
  {
    stroke = new BasicStroke(stroke.getLineWidth(),
      stroke.getEndCap(),
      stroke.getLineJoin(),
      miterlimit,
      stroke.getDashArray(),
      stroke.getDashPhase());
  }

  /** 
   * Sets the <code>Pen</code>s dash array.
   * @see java.awt.BasicStroke
   */
  public void setDash(float[] dashArray)
  {
    stroke = new BasicStroke(stroke.getLineWidth(),
      stroke.getEndCap(),
      stroke.getLineJoin(),
      stroke.getMiterLimit(),
      dashArray,
      stroke.getDashPhase());
  }

  /** 
   * Sets the <code>Pen</code>s dash phase.
   * @see java.awt.BasicStroke
   */
  public void setDashPhase(float dashPhase)
  {
    stroke = new BasicStroke(stroke.getLineWidth(),
      stroke.getEndCap(),
      stroke.getLineJoin(),
      stroke.getMiterLimit(),
      stroke.getDashArray(),
      dashPhase);
  }

  /** 
   * Provides information about the currently available font families (e.g. "Roman").
   * Each font name is a string packed into a array of strings.
   * @see java.awt.Font for more information about font attributes etc.
   */
  public static String[] getAvailableFontFamilies()
  {
    GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
    String s[] = ge.getAvailableFontFamilyNames();
    return s;
  }

  /** 
   * Changes the font style.
   * @see java.awt.Font for possible styles.
   */
  public void setFontStyle(int style)
  {
    font = font.deriveFont(style);
  }

  /** 
   * Changes the font size (in points).
   */
  public void setFontSize(int size)
  {
    font = font.deriveFont((float)size);
  }

  /** 
   * Changes the font size (in points).
   * You will probably only need the int version <a href="#setFontSize(int)">setFontSize(int)</a>.
   */
  public void setFontSize(float size)
  {
    font = font.deriveFont(size);
  }

  /** 
   * Queries the size (in points, rounded to int) of the current font.
   */
  public int getFontSize()
  {
    return font.getSize();
  }

  /** 
   * Changes the font to the given one.
   */
  public void setFont(Font f)
  {
    font = f;
  }

  /** 
   * Queries the current font.
   */
  public Font getFont()
  {
    return font;
  }

  /**
   * Returns text information for given font and text.
   */
  public static TextInfo getTextInfo(String text, Font font)
  {
    TextInfo textInfo = new TextInfo();
    textInfo.height = 0;
    textInfo.ascent = 0;
    textInfo.descent = 0;
    textInfo.width = 0;
    BufferedImage bi = // Dummy bi to get font and line metrics
      new BufferedImage(1, 1, Transparency.TRANSLUCENT);
    Graphics2D g = bi.createGraphics();
    FontRenderContext frc = g.getFontRenderContext();
    LineMetrics lm = font.getLineMetrics(text, frc);
    FontMetrics fm = g.getFontMetrics(font);
    textInfo.height = (int)Math.ceil(lm.getHeight());
    textInfo.ascent = (int)Math.ceil(lm.getAscent());
    textInfo.descent = (int)Math.ceil(lm.getDescent());

    if (text != null && text.length() != 0)
      textInfo.width = fm.charsWidth(text.toCharArray(), 0, text.length());
    g.dispose();
    return textInfo;
  }

  /**
   * Returns width of given text with current font (in pixels).
   */
  public int getTextWidth(String text)
  {
    return getTextInfo(text, font).width;
  }

  /**
   * Returns height of text with current font (in pixels).
   */
  public int getTextHeight()
  {
    return getTextInfo("ABg", font).height;
  }

  /**
   * Returns ascender height of text with current font (in pixels).
   */
  public int getTextAscent()
  {
    return getTextInfo("ABg", font).ascent;
  }

  /**
   * Returns descender height of text with current font (in pixels).
   */
  public int getTextDescent()
  {
    return getTextInfo("ABg", font).descent;
  }

}
