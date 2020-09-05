// TurtlePane.java

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

import java.awt.*;

/**
 * A bean class derived from Playground that can be used as component in
 * a GUI builder.
 */
public class TurtlePane extends Playground
  implements TurtleContainer
{
  /**
   * Property for bean support.
   */
  public Color backgroundColor = Playground.DEFAULT_BACKGROUND_COLOR;
  /**
   * Property for bean support.
   */
  public boolean enableFocus = false;

  /**
   * Parameterless bean constructor.
   */
  public TurtlePane()
  {
    super(true);
    setFocusable(enableFocus);
  }

  /**
   * Property setter.
   */
  public void setBackgroundColor(Color value)
  {
    backgroundColor = value;
    beanBkColor = value;
  }

  /**
   * Property getter.
   */
  public Color getBackgroundColor()
  {
    return backgroundColor;
  }

  /**
   * Property setter.
   */
  public void setEnableFocus(boolean value)
  {
    enableFocus = value;
    setFocusable(enableFocus);
  }

  /**
   * Property getter.
   */
  public boolean getEnableFocus()
  {
    return enableFocus;
  }

  /**
   * Returns current instance reference.
   */
  public Playground getPlayground()
  {
    return this;
  }

}
