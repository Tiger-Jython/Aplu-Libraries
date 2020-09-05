// GPrintable.java

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

/**
 * Interface for printing on an attached printer.
 * Normally an application derives from GPanel (or uses
 * a GPanel) and implements this interface.
 * draw() should contain all drawing operations into the
 * GPanel. The printing occures, when GPanel's print() is
 * called.<br><br>
 * Example:<br>
 <code>
 import ch.aplu.util.*;<br><br>

 public class PrintTest extends GPanel implements GPrintable<br>
 {<br>
 &nbsp;&nbsp;public PrintTest()<br>
 &nbsp;&nbsp;{<br>
 &nbsp;&nbsp;&nbsp;&nbsp;draw();       // Draw on screen<br>
 &nbsp;&nbsp;&nbsp;&nbsp;print(this);  // Draw on printer<br>
 &nbsp;&nbsp;}<br>
 <br>
 &nbsp;&nbsp;public void draw()<br>
 &nbsp;&nbsp;{<br>
 &nbsp;&nbsp;&nbsp;&nbsp;move(0, 0);<br>
 &nbsp;&nbsp;&nbsp;&nbsp;draw(0, 1);<br>
 &nbsp;&nbsp;&nbsp;&nbsp;draw(1, 1);<br>
 &nbsp;&nbsp;&nbsp;&nbsp;draw(1, 0);<br>
 &nbsp;&nbsp;&nbsp;&nbsp;draw(0, 0);<br>
 &nbsp;&nbsp;&nbsp;&nbsp;line(0, 0, 1, 1);<br>
 &nbsp;&nbsp;&nbsp;&nbsp;line(0, 1, 1, 0);<br>
 &nbsp;&nbsp;}<br>
 <br>
 &nbsp;&nbsp;public static void main(String[] args)<br>
 &nbsp;&nbsp;{<br>
 &nbsp;&nbsp;&nbsp;&nbsp;new PrintTest();<br>
 &nbsp;&nbsp;}<br>
 }<br>
 </code>

 */
public interface GPrintable
{
  /**
   * This method must perform all drawing operations.
   * Be aware that for some undocumented reason
   * draw() is called twice. So be sure you initialize it correctly.
   */
  public void draw();

}
