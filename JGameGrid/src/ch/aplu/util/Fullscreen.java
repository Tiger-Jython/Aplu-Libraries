// Fullscreen.java

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
package ch.aplu.util;

import java.awt.*;

/**
 * Construct a Size object that has the current fullscreen width and height.
 */
public class Fullscreen extends Size
{
  /**
   * Construct a Fullscreen instance.
   */
  public Fullscreen()
  {
    super((int)(Toolkit.getDefaultToolkit().getScreenSize()).getWidth(),
      (int)(Toolkit.getDefaultToolkit().getScreenSize()).getHeight());
  }

}
