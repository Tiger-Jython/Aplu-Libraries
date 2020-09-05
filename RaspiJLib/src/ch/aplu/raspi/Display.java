// Display.java

/*
 This software is part of the RaspiJLib library.
 It is Open Source Free Software, so you may
 - run the code for any purpose
 - study how the code works and adapt it to your needs
 - integrate all or parts of the code in your own programs
 - redistribute copies of the code
 - improve the code and release your improvements to the public
 However the use of the code is entirely your responsibility.
 */
package ch.aplu.raspi;

/**
 * Class that represents the 7-segment display sensor.
 */
public class Display
{
  private Robot robot;
  private String device;

  /**
   * Creates a display instance.
   */
  public Display()
  {
    device = "display";
    robot = RobotInstance.getRobot();
    checkRobot();
    robot.sendCommand(device + ".create");
  }

  /**
   * Shows the given character at one of the 4 7-segment digits. The character is mapped to
   * its binary value using the PATTERN dictionary define in SharedConstants.py.
   * Only one digit can be used at the same time. This method has much less overhead than calling
   * setValue(), because no internal display thread is started. 
   * The display remains active even when the program terminates.
   * @param ch the character to display
   * @param digit the display ID (0 is leftmost, 3 is rightmost)
   */
  public void setDigit(char ch, int digit)
  {
    robot.sendCommand(device + ".setDigit." + ch + "." + digit);
  }

  /**
   * Shows the pattern of the binary value 0..255.
   * @param value the byte value
   * @param digit the display ID (0 is leftmost, 3 is rightmost
   */
  public void setBinary(byte value, int digit)
  {
    robot.sendCommand(device + ".setBinary." + value + "." + digit);
  }

  /**
   * Shows one of the 3 decimal points.
   * @param id select the DP to show:<br>
   * 0: right bottom<br>
   * 1: middle bottom<br>
   * 2 middle top
   */
  public void setDecimalPoint(int id)
  {
    robot.sendCommand(device + ".setDecimalPoint." + id);
  }

  /**
   * Clears the given digit
   * @param digit the display ID (0 is right most, 3 is left most)
   */
  public void clearDigit(int digit)
  {
    robot.sendCommand(device + ".clearDigit." + digit);
  }

  /**
   * Turns all digits off.
   * Stops a running display thread.
   */
  public void clear()
  {
    robot.sendCommand(device + ".clear");
  }

  /**
   * Displays the given text right justified by multiplexing using a display thread.
   * If the text to display exceeds 4 digits only the 4 leading digits are shown.
   * Because only one digit can be used at the same time, 
   * an internal display thread is created that drives the digits repeatedly in 
   * a fast sequence. Call clear() to stop the thread when it is no longer used.
   * To display decimal points, use the dp parameter [0: hidden, 1: shown] in
   * the order [right bottom, middle bottom, middle top]
   * @param text the text to display
   * @param  dp a int array if size 3 with 1 or 0, if the decimal 
   * point is shown or not
   */
  public void setText(String text, int[] dp)
  {
    if (text.length() > 4)
      text = text.substring(0, 4);
    robot.sendCommand(device + ".setText." + text + "."
      + dp[0] + ", " + dp[1] + ", " + dp[2]);
  }
  
  /**
   * Same as setText(text, dp) with integer parameter.
   * @param value the value to display (first 4 digits)
   * @param  dp a int array if size 3 with 1 or 0, if the decimal 
   * point is shown or not
   */
  public void setText(int value, int[] dp)
  {
    String text = "" + value;
    setText(text, dp);
  }

  /**
   * Same as setText(text, dp) with dp = [0, 0, 0] (no decimal point).
   * @param text the text to display
   */
  public void setText(String text)
  {
    int[] dp =
    {
      0, 0, 0
    };
    setText(text, dp);
  }
  
    /**
   * Same as setText(text) with integer parameter.
   * @param value the value to display (first 4 digits)
   */
  public void setText(int value)
  {
    String text = "" + value;
    setText(text);
  }

  /**
   * Displays a text that can be scrolled. A text position index is used that
   * determines the character displayed at leftmost digit. Default pos = 0
   * @param text the text to display
   * @param pos the start value of the text pointer (character index positioned a leftmost digit)
   */
  public void setScrollableText(String text, int pos)
  {
    robot.sendCommand(device + ".setScrollableText." + text + "." + pos);
  }

  /**
   * Same as setScrollableText(text, pos) with pos = 0 (text starts at leftmost digit).
   * @param text the text to display
   */
  public void setScrollableText(String text)
  {
    setScrollableText(text, 0);
  }

  /**
   * Scrolls the scrollable text one step to the left.
   * @return the number of characters remaining at the right
   */
  public int scrollToLeft()
  {
    String rc = robot.sendCommand(device + ".scrollToLeft");
    return Integer.parseInt(rc);
  }

  /**
   * Scrolls the scrollable text one step to the right.
   * @return the number of characters remaining at the left
   */
  public int scrollToRight()
  {
    String rc = robot.sendCommand(device + ".scrollToRight");
    return Integer.parseInt(rc);
  }

  /**
   * Shows the scrollable text at the start position.
   */
  public void setToStart()
  {
    robot.sendCommand(device + ".setToStart");
  }

  /**
   *Shows a ticker text that scroll to left until the last 4 characters are displayed.
   * @param text the text to display, if shorter than 4 characters, scrolling is disabled
   * @param count the number of repetitions (default: 1). For count == 0, infinite duration,
   * stopped by calling stopTicker()
   * @param speed the speed number of scrolling operations per sec (default: 2)
   * @param blocking if True, the method blocks until the ticker has finished; otherwise
   * it returns immediately (default: False)
   */
  public void ticker(String text, int count, int speed, boolean blocking)
  {
    robot.sendCommand(device + ".ticker." + text + "." + count + "." + speed);
    if (blocking)
    {
      while (isTickerAlive())
         Tools.delay(100);
    }  
  }

  /**
   * Same as ticker(text, count, speed, blocking) with blocking = false.
   * @param text the text to display, if shorter than 4 characters, scrolling is disabled
   * @param count the number of repetitions (default: 1). For count == 0, infinite duration,
   * stopped by calling stopTicker()
   * @param speed the speed number of scrolling operations per sec (default: 2)
   */
  public void ticker(String text, int count, int speed)
  {
    ticker(text, count, speed, false);
  }

  /**
   * Same as ticker(text, count, speed, blocking) with speed = 2 and blocking = false.
   * @param text the text to display, if shorter than 4 characters, scrolling is disabled
   * @param count the number of repetitions (default: 1). For count == 0, infinite duration,
   * stopped by calling stopTicker()
   */
  public void ticker(String text, int count)
  {
    ticker(text, count, 2, false);
  }

  /**
   * Same as ticker(text, count, speed, blocking) with count = 1, 
   * speed = 2 and blocking = false.
   * @param text the text to display, if shorter than 4 characters, scrolling is disabled
   */
  public void ticker(String text)
  {
    ticker(text, 1, 2, false);
  }

  /**
   * Stops a running ticker.
   */
  public void stopTicker()
  {
    robot.sendCommand(device + ".stopTicker");
  }

  /**
  Checks, if the ticker is still displaying.
  @return true, if the ticker is displaying
  */
  public boolean isTickerAlive()
  {
    Tools.delay(1);
    String rc = robot.sendCommand(device + ".isTickerAlive");
    return rc.equals("1");
  }

  /**
   * Returns a string with all displayable characters.
   * @return The character set that can be displayed
   */
  public String getDisplayableChars()
  {
    return robot.sendCommand(device + ".getDisplayableChars");
  }

  private void checkRobot()
  {
    if (robot == null)
      new ShowError("Fatal error while creating Display.\nCreate Robot instance first");
  }
}
