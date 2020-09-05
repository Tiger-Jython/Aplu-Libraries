// GGTextField.java

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

/**
 * Class to provide a dynamically created line of text shown in a 
 * game grid window. Gets its functionality from a TextActor instance 
 * (no subclassing of TextActor in order to shield the user from 
 * the many Actor class methods.)
 * Whenever the text, fontSize, textColor, bgColor or typeface attribute is altered,
 * the old text actor is removed from the game grid and a new actor is created
 * and added to the game grid on the next show() invocation.
 */
public class GGTextField
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
  // -------------- End of inner class ----------------------

  private GameGrid gg;
  private MyTextActor ta = null;
  private Location location;
  private String text;
  private Color textColor;
  private Color bgColor;
  private Font font;
  private Point locationOffset = new Point(0, 0);
  private boolean isModified;
  private volatile boolean isRefreshEnabled;

  /**
   * Creates a GGTextField instance with standard attributes. Does not yet show
   * the text. The internal text actor is created and added to the game grid
   * at this point, but the text actor is still hidden. The order of visiblity in 
   * respect to other actors does not depend on the moment of creation, because
   * the text actor is set on top of other actors when show() is invoked. The text
   * color is black, the background color white/transparent and the
   * font SansSerif, PLAIN, 8 pixels. Automatic refresh can cause flickering,
   * when the text actors are created rapidly. To avoid flickering, 
   * let the simulation thread perform the refreshing.
   * @param gg the GameGrid where to add the text actor
   * @param location the location where the the text will be placed 
   * (horizontally left aligned, vertically center aligned)
   * @param text the text string to show
   * @param enableRefresh if true, the automatic refresh of the game grid is turned on;
   * otherwise refresh must be called by user code or performed by the simulation thread
   */
  public GGTextField(GameGrid gg, String text, Location location, boolean enableRefresh)
  {
    this.gg = gg;
    this.location = location;
    this.text = text;
    this.font = new Font("SansSerif", Font.PLAIN, 12);
    this.textColor = Color.black;
    this.bgColor = new Color(255, 255, 255, 0);
    isModified = true;
    isRefreshEnabled = enableRefresh;
    isModified = true;
  }

  /**
   * Creates a GGTextField with standard attributes and no text.
   * @param gg the GameGrid where to add the text actor
   * @param location the location where the the text will be placed 
   * (horizontally left aligned, vertically center aligned)
   * @param enableRefresh if true, the automatic refresh of the game grid is turned on;
   * otherwise refresh must be called by user code or performed by the simulation thread
   */
  public GGTextField(GameGrid gg, Location location, boolean enableRefresh)
  {
    this(gg, "", location, enableRefresh);
  }

  /** 
   * Displays the text. If the text was never shown or attributes were modified,
   * creates the internal TextActor instance. Sets the visibility of the 
   * TextActor class on top of all other Actor's classes. This may be changed
   * by calling GameGrid.setPaintOrder(). Be aware that the paint order of all
   * other TextActors is affected. 
   * Refreshs the game grid, if enableRefresh = true was set.
   * @see ch.aplu.jgamegrid.GameGrid#setPaintOrder(Class... classes)
   */
  public synchronized void show()
  {
    if (isModified)
    {
      isModified = false;
      createTextActor();
    }
    else
    {
      gg.setPaintOrder(MyTextActor.class);
      ta.show();
    }
    if (isRefreshEnabled)
      gg.refresh();
  }

  private synchronized void createTextActor()
  {
    //   L.i("GGTextField.show() creates new actor");
    MyTextActor tmp = new MyTextActor(false, text, textColor, bgColor, font);;
    tmp.setLocationOffset(locationOffset);
    gg.addActorNoRefresh(tmp, location);
    if (ta != null)
      ta.removeSelf();
    ta = tmp;
  }

  /** 
   * Hides the text. 
   * Refreshs the game grid, if enableRefresh = true was set in
   * the constructor.   
   */
  public synchronized void hide()
  {
    ta.hide();
    if (isRefreshEnabled)
      gg.refresh();
  }

  /**
   * Returns the visibility of the text.
   * @return true, if the text is visible; otherwise false
   */
  public boolean isVisible()
  {
    return ta.isVisible();
  }

  /**
   * Sets the location attribute.
   * Refreshs the game grid, if enableRefresh = true was set in
   * the constructor.   
   * @param location the location where the the text will be placed 
   * (horizontally left aligned, vertically center aligned)
   */
  public synchronized void setLocation(Location location)
  {
    this.location = location;
    update();   // We also create a new text actor to provide the same
    // paint order like other attribute modifications
  }

  /**
   * Sets the text attribute.
   * Refreshs the game grid, if enableRefresh = true was set in
   * the constructor.   
   * @param text the text string to show
   */
  public synchronized void setText(String text)
  {
    this.text = text;
    update();
  }

  /**
   * Sets the text color attribute.
   * Refreshs the game grid, if enableRefresh = true was set in
   * the constructor.   
   * @param textColor the color of the text
   */
  public synchronized void setTextColor(Color textColor)
  {
    this.textColor = textColor;
    update();
  }

  /**
   * Sets the background color attribute.
   * Refreshs the game grid, if enableRefresh = true was set in
   * the constructor.   
   * @param bgColor the color of the text background
   */
  public synchronized void setBgColor(Color bgColor)
  {
    this.bgColor = bgColor;
    update();
  }

  /**
   * Sets the font attribute.
   * Refreshs the game grid, if enableRefresh = true was set in
   * the constructor.   
   * @param font the text font
   */
  public synchronized void setFont(Font font)
  {
    this.font = font;
    update();
  }

  /**
   * Sets a pixel offset in x- any y-direction relative to the current
   * location. 
   * Refreshs the game grid, if enableRefresh = true was set in
   * the constructor.   
   * Used to fine tune the displayed text field position in a coarse game grid.
   * @param locationOffset x,y displacement (x to the left, y downwards)
   */
  public synchronized void setLocationOffset(Point locationOffset)
  {
    this.locationOffset = new Point(locationOffset);
    update();
  }

  /**
   * Returns the current TextActor used for this text field. The actor changes
   * when the text, fontSize, textColor, bgColor or typeface attribute is altered and
   * the modified text is shown.
   * @return the text actor used for this field; null, if the actor has never
   * been shown
   */
  public TextActor getTextActor()
  {
    return ta;
  }

  /**
   * Returns the total width of the text.
   * Maybe used to align the text.
   * @return the text width in pixels
   */
  public synchronized int getTextWidth()
  {
    if (ta == null)
    {
      TextActor tmp = new TextActor(false, text, textColor, bgColor, font);
      return tmp.getTextWidth();
    }
    return ta.getTextWidth();
  }

  /**
   * Returns the height of the text.
   * Maybe used to align the text.
   * @return the text height in pixels
   */
  public synchronized int getTextHeight()
  {
    if (ta == null)
    {
      TextActor tmp = new TextActor(false, text, textColor, bgColor, font);
      return tmp.getTextHeight();
    }
    return ta.getTextHeight();
  }

  private synchronized void update()
  {
    isModified = true;
    if (ta != null && ta.isVisible())
    {
      createTextActor();
      isModified = false;
    }
  }

}
