// GGProgressBar.java

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
import java.text.DecimalFormat;

/**
 * Class that implements a progress bar indicator. It consists of a 
 * rectangular frame with a filled color stick inside that 
 * indicates the current value. The value may be displayed as text 
 * centered in the middle of the frame. The bar may be horizonal (stick
 * advances from left to right) or vertical (stick advances from bottom to
 * top). The bar graphics is a dynamically generated GGBitmap used to
 * create an Actor instance that is added to the game grid.
 */
public class GGProgressBar
{
  // -------------- Inner dummy class for setting paint order ---------------
  private class MyTextActor extends TextActor
  {
    public MyTextActor(boolean isRotatable, String text, Color textColor, Color bgColor, Font font)
    {
      super(isRotatable, text, textColor, bgColor, font);
    }

    public MyTextActor(String text, Color textColor, Color bgColor, Font font)
    {
      super(text, textColor, bgColor, font);
    }

    public MyTextActor(String text)
    {
      super(text);
    }

  }
  
  private class BarActor extends Actor
  {
    public BarActor(BufferedImage bi)
    {
      super(bi);
    }  
  }  
    
  // -------------- End of inner class ----------------------

  private GameGrid gg;
  private BarActor barActor = null;
  private MyTextActor textActor = null;
  private Location loc;
  private int width;
  private int height;
  private Color bgColor = Color.white;
  private Color frameColor = Color.black;
  private Color stripColor = Color.lightGray;
  private Color textColor = Color.black;
  private Font font = new Font("SansSerif", Font.BOLD, 12);
  private double min = 0;
  private double max = 100;
  private String unit = "";
  private boolean isHorizontal;
  private DecimalFormat df = new DecimalFormat("#");

  /**
   * Creates a horizontal progress bar with default properties.<br>
   * Defaults:<br>
   * - frame color: black<br>
   * - background color: white<br>
   * - strip color: light gray<br>
   * - text color: black<br>
   * - minimum value: 0<br>
   * - maximum value: 100<br>
   * - unit: empty<br>
   * - decimal format pattern "#"<br>
   * - font for value/unit text: new Font("SansSerif", Font.BOLD, 12)<br>
   * @param gg the GameGrid reference where the bar is shown
   * @param loc the location of the bar's center
   * @param width the width (length) of the bar
   * @param height the height of the bar
   */
  public GGProgressBar(GameGrid gg, Location loc, int width, int height)
  {
    this(gg, loc, width, height, true);
  }

  /**
   * Creates a horizontal or vertical progress bar with default properties.<br>
   * Defaults:<br>
   * - frame color: black<br>
   * - background color: white<br>
   * - strip color: light gray<br>
   * - text color: black<br>
   * - minimum value: 0<br>
   * - maximum value: 100<br>
   * - unit: empty<br>
   * - decimal format pattern "#"<br>
   * - font for value/unit text: new Font("SansSerif", Font.BOLD, 12)<br>
   * @param gg the GameGrid reference where the bar is shown
   * @param loc the location of the bar's center
   * @param width the width (length) of the bar
   * @param height the height of the bar
   * @param isHorizontal if true, a horizontal bar is shown; 
   * otherwise a vertical bar is shown
   */
  public GGProgressBar(GameGrid gg, Location loc, int width, int height, boolean isHorizontal)
  {
    this.gg = gg;
    this.loc = loc;
    this.width = width;
    this.height = height;
    this.isHorizontal = isHorizontal;
  }

  /**
   * Sets the background color of the frame.<br>
   * Default: white
   * @param color the background color
   */
  public void setBgColor(Color color)
  {
    this.bgColor = color;
  }

  /**
   * Sets the border color of the frame.<br>
   * Default: black
   * @param color the color of the frame border
   */
  public void setFrameColor(Color color)
  {
    this.frameColor = color;
  }

  /**
   * Sets the color of the indicator strip (filled rectangle).<br>
   * Default: light gray
   * @param color the color of the strip
   */
  public void setStripColor(Color color)
  {
    this.stripColor = color;
  }

  /**
   * Sets the color of the text centered in the middle of the bar.<br>
   * Default: black
   * @param color the text color
   */
  public void setTextColor(Color color)
  {
    this.textColor = color;
  }

  /**
   * Sets the minimum value of the indicator.<br>
   * Default: 0
   * @param min the value for a strip length = 0.
   */
  public void setMin(double min)
  {
    this.min = min;
  }

  /**
   * Sets the maximum value of the indicator.<br>
   * Default: 100
   * @param max the value for a the full length strip.
   */
  public void setMax(double max)
  {
    this.max = max;
  }

  /**
   * Sets the text displayed as unit. If null, no value/unit text is shown.<br>
   * Default: empty string.
   * @param unit the unit string
   */
  public void setUnit(String unit)
  {
    if (unit == null)
      this.unit = null;
    else
      this.unit = new String(unit);
  }
  
  /**
   * Sets the font for the value/unit text.
   * Default: new Font("SansSerif", Font.BOLD, 12)
   * @param font the font for the value/unit text
   */
  public void setFont(Font font)
  {
    this.font = font;
  }

  /**
   * Sets the DecimalFormat pattern used to display the value.
   * See the class java.text.DecimalFormat for more information.<br>
   * Default: "#"
   * @param pattern the format pattern used to create the decimal format for the
   * value text
   */
  public void setPattern(String pattern)
  {
    df = new DecimalFormat(pattern);
  }
  
  /** 
   * Removes the progress bar from the game grid. Calling setValue() will
   * show it again.
   * @param doRefresh if true, the game grid is refreshed
   */
  public synchronized void remove(boolean doRefresh)
  {
    if (barActor != null)
      barActor.removeSelf();
    if (textActor != null)
      textActor.removeSelf();
    barActor = null;
    textActor = null;
    if (doRefresh)
      gg.refresh();
  }

  /**
   * Sets the current value of the progress bar. Must be a value in 
   * [min..max] (inclusive). The image of the progress bar is
   * constructed and a new actor is created and added to the game grid. (The 
   * actor classes used for the control are put on top of the paint order.) Then
   * the game grid is refreshed.
   * @param value the new value
   */
  public void setValue(double value)
  {
    setValue(value, true);
  }
  
  /**
   * Sets the current value of the progress bar. Must be a value in 
   * [min..max] (inclusive). The image of the progress bar is
   * constructed and a new actor is created and added to the game grid. (The 
   * actor classes used for the control are put on top of the paint order.) 
   * @param value the new value
   * @param doRefresh if true, the game grid is refreshed; otherwise it must be refreshed 
   * by another call (e.g. GamgeGrid.addActor(), GameGrid.refresh(), tetc.) 
   */
  public synchronized void setValue(double value, boolean doRefresh)
  {
    // Must remove old actor after creating new one, otherwise flickering will
    // happen. Unclear why (refresh may happen between removing the actor and
    // creating the new one)
    Actor oldBarActor = barActor;
    Actor oldTextActor = textActor;
    if (isHorizontal)
      barActor = new BarActor(createActor(width, height, value));
    else
      barActor = new BarActor(createActor(height, width, value));
    gg.addActorNoRefresh(barActor, loc);
    String valueStr = df.format(value);
    if (unit != null)
    {
      textActor = new MyTextActor(valueStr + " " + unit, textColor,
        new Color(255, 255, 255, 0), font);
      if (isHorizontal)
      {
        int w = textActor.getTextWidth() / 2;
        textActor.setLocationOffset(new Point(-w, 1));
        gg.addActorNoRefresh(textActor, loc);
      }
      else
      {
        Actor a = new Actor(GGBitmap.getScaledImage(textActor.getImage(), 1, 90));
        int w = textActor.getTextWidth() / 2;
        a.setLocationOffset(new Point(0, -w));
        gg.addActorNoRefresh(a, loc);
      }
    }
 
    if (oldBarActor != null)
       oldBarActor.removeSelf();
    if (oldTextActor != null)
       oldTextActor.removeSelf();
 
    gg.setPaintOrder(MyTextActor.class, BarActor.class);
    if (doRefresh)
      gg.refresh();
  }

  private BufferedImage createActor(int width, int height, double value)
  {
    int length;
    if (isHorizontal)
    {
      double a = 1.0 * (width - 1) / (max - min);
      double b = -a * min;
      length = (int)(a * value + b + 0.5);
    }
    else
    {
      double a = 1.0 * (height - 1) / (max - min);
      double b = -a * min;
      length = (int)(a * value + b);
    }
    GGBitmap bm = new GGBitmap(width + 1, height + 1);
    bm.setPaintColor(bgColor);
    bm.fillRectangle(new Point(0, 0), new Point(width, height));
    bm.setPaintColor(frameColor);
    bm.drawRectangle(new Point(0, 0), new Point(width, height));
    bm.setPaintColor(stripColor);
    if (isHorizontal)
      bm.fillRectangle(new Point(1, 1), new Point(length, height - 1));
    else
      bm.fillRectangle(new Point(1, height - length), new Point(width - 1, height - 1));
    return bm.getBufferedImage();
  }

}
