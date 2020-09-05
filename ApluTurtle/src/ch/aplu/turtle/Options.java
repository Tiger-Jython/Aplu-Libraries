// Options.java

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
 * Class to select initializing options for the ch.aplu.turtle package. 
 * Option set here take precedence over values set in the 
 * turtlegraphics.properties file.
 */
public class Options
{
  private static String closingMode = null;
  private static String frameTitle = null;
  private static Dimension playgroundSize = null;
  private static Point framePosition = null;
  private static Color backgroundColor = null;
  private static Color shapeColor = null;
  private static Color traceColor = null;
  private static int turtleSpeed = -1;
  private static int angleResolution = -1;
  private static int framesPerSecond = -1;
  private static String edgeBehavior = null;
 
  private Options()
  {
  }
  
/**
 * Determines what happens when the title bar close button is hit. 
 * Library value: TerminateOnClose<br>
 * Values:<br>
 * TermintateOnClose -> Terminating and shutting down JRE by System.exit(0)<br>
 * ClearOnClose -> Remove all turtle images and traces, 
 * but turtles remain where they are<br>
 * AskOnClose -> Show confirmation dialog asking for termination<br>
 * DisposeOnClose -> Closes the graphics window, but does not shutdown JRE<br>
 * ReleaseOnClose -> Like DisposeOnClose, but throws runtime exception when 
 * turtle graphics methods are called<br>
 * NothingOnClose -> Do nothing
 */
 public static void setClosingMode(String mode)
  {
    closingMode = mode.trim().toLowerCase();
  }

 /**
  * Default title displayed in the frame title bar.
  * Library value: Java Turtle Playground.
  */
  public static void setFrameTitle(String title)
  {
    frameTitle = title;
  }

  
  /**
   * Default size of playground (width, height).
   * Library default: 400 x 400.
   * width and height should be even positive integers.
   * Determines the span of turtle coordinates 
   * (-width/2 .. width/2, -height/2 .. height/2) and
   * the number of pixels (width + 1) x (height + 1)
   */
  public static void setPlaygroundSize(int width, int height)
  {
    playgroundSize = new Dimension(width, height);
  }

  
  /**
   * Default position of frame (ulx, uly).
   * Library value: adapted to current screen size and number of playgrounds shown.
   */
  public static void setFramePosition(int ulx, int uly)
  {
    framePosition = new Point(ulx, uly);
  }

  /**
   * Default background color of playground.
   * Library value: Color.white.
   */
  public static void setBackgroundColor(Color bgColor)
  {
    backgroundColor = bgColor;
  }

  /**
   * Default color of the turtle shape.
   * Library value: Color.cyan.
   */
  public static void setTurtleColor(Color turtleColor)
  {
    shapeColor = turtleColor;
  }
  
  /**
   * Default pen color.
   * Library value: Color.blue.
   */
  public static void setPenColor(Color penColor)
  {
    traceColor = penColor;
  }

  /**
   * Default turtle speed used for animation. 
   * Library value: 200 pixels / secs.
   */
  public static void setTurtleSpeed(int speed)
  {
    turtleSpeed = speed;
  }

  /**
   * Default angle resolution used for animation.
   * Library value: 72 degrees / secs.
   */
  public static void setAngleResolution(int resolution)
  {
    angleResolution = resolution;
  }

  /**
   * Default frames per second used for animation.
   * Library value: 10 frames / secs.
   */
   public static void setFramesPerSecoonds(int nbFrames)
  {
    framesPerSecond = nbFrames;
  }

  /**
   * Default behavior when the turtle reaches the edge of the playground.
   * Library value: clip.
   * Values: clip or wrap
   */
  public static void setEdgeBehavior(String behavior)
  {
    edgeBehavior = behavior.trim().toLowerCase();
  }

  protected static int getFrameMode()
  {
    if (closingMode == null)
      return -1;  // No value
    if (closingMode.equals("terminateonclose"))
      return Turtle.STANDARDFRAME;
    if (closingMode.equals("clearonclose"))
      return Turtle.CLEAR_ON_CLOSE;
    if (closingMode.equals("askonclose"))
      return Turtle.ASK_ON_CLOSE;
    if (closingMode.equals("disposeonclose"))
      return Turtle.DISPOSE_ON_CLOSE;
    if (closingMode.equals("releaseonclose"))
      return Turtle.RELEASE_ON_CLOSE;
    if (closingMode.equals("nothingonclose"))
      return Turtle.NOTHING_ON_CLOSE;
    return Turtle.STANDARDFRAME;  // Value not valid
  }

  protected static String getFrameTitle()
  {
    return frameTitle;
  }

  protected static Dimension getPlaygroundSize()
  {
    return playgroundSize;
  }

  protected static Point getFramePosition()
  {
    return framePosition;
  }

  protected static Color getBackgroundColor()
  {
    return backgroundColor;
  }

  protected static Color getShapeColor()
  {
    return shapeColor;
  }

  protected static Color getTraceColor()
  {
    return traceColor;
  }

  protected static int getTurtleSpeed()
  {
    return turtleSpeed;
  }

  protected static int getAngleResolution()
  {
    return angleResolution;
  }

  protected static int getFramesPerSecond()
  {
    return framesPerSecond;
  }

  protected static int getEdgeBehavior()
  {
    if (edgeBehavior == null)
      return -1;
    if (edgeBehavior.equals("wrap"))
      return Turtle.WRAP;
    if (edgeBehavior.equals("clip"))
      return Turtle.CLIP;
    return -1;
  }
}
