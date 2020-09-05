// TurtleArea.java

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

import ch.aplu.turtle.*;
import java.awt.*;
import javax.swing.*;

/** 
 * This class is used for a turtle applet. It contains a Playground
 * where the turtles live.
 */
public class TurtleArea implements TurtleContainer
{
  private Playground playground;

  /** 
   * Creates an applet window with given applet container.
   */
  public TurtleArea(JApplet jApplet)
  {
    init(jApplet, new Playground());
  }

  /** 
   * Creates an applet window with given applet container and background color.
   */
  public TurtleArea(JApplet jApplet, Color bkColor)
  {
    init(jApplet, new Playground(bkColor));
  }

  /** 
   * Creates an applet window with given applet container, width and height.
   */
  public TurtleArea(JApplet jApplet, int width, int height)
  {
    Dimension size = new Dimension(width, height);
    init(jApplet, new Playground(size));
  }

  /** 
   * Creates an applet window with given applet container, width, height and background color.
   */
  public TurtleArea(JApplet jApplet, int width, int height, Color bkColor)
  {
    Dimension size = new Dimension(width, height);
    init(jApplet, new Playground(size, bkColor));
  }

  private void init(JApplet ja, Playground pg)
  {
    playground = pg;
    ja.getContentPane().add(playground);
  }

  /** 
   * Returns the playground of this AppletFrame.
   */
  public Playground getPlayground()
  {
    return playground;
  }

}
