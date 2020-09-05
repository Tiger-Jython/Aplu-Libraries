// LegoRobot.java

/*
 This software is part of the EV3JLib library.
 It is Open Source Free Software, so you may
 - run the code for any purpose
 - study how the code works and adapt it to your needs
 - integrate all or parts of the code in your own programs
 - redistribute copies of the code
 - improve the code and release your improvements to the public
 However the use of the code is entirely your responsibility.
 */
package ch.aplu.ev3;

import java.io.File;
import java.util.*;
import lejos.hardware.lcd.LCD;
import lejos.hardware.Sound;
import lejos.hardware.Battery;
import lejos.hardware.Brick;
import lejos.hardware.Button;
import lejos.hardware.LED;
import lejos.hardware.ev3.LocalEV3;

/**
 * Class that represents a EV3 robot brick. Parts (e.g. motors, sensors) may
 * be assembled into the robot to make it doing the desired job.
 */
public class LegoRobot
{

  private class Player extends Thread
  {
    private String filename;
    private int volume;
    private boolean isRunning = true;

    public Player(String filename, int volume)
    {
      this.filename = filename;
      this.volume = volume;
    }

    public void startPlaying(boolean blocking)
    {
      if (!new File(filename).exists())
      {
        LCD.clear(0);
        LCD.clear(1);
        LCD.drawString(filename.substring(filename.lastIndexOf("/") + 1), 0, 0);
        LCD.drawString("not found", 0, 1);
        Sound.beep();
        Tools.delay(100);
        Sound.beep();
        return;
      }
      Tools.delay(300);
      start();
      while (blocking && isRunning)
        Tools.delay(200);
      Tools.delay(200);
    }

    public void run()
    {
      Sound.playSample(new File(filename), volume);
      isRunning = false;
    }
  }

  private class MyKeyListener extends Thread
  {
    private int pollPeriod = 200; // ms

    public void run()
    {
      boolean isKeyDown = false;
      while (isKeyListenerRunning)
      {
        int keyState = Button.readButtons();
        if (keyState == 0 && isKeyDown)
          isKeyDown = false;
        else if (keyState != 0 && !isKeyDown)
        {
          isKeyDown = true;
          if ((keyState & Button.ID_DOWN) != 0)
          {
            buttonID = Button.ID_DOWN;
            if (buttonListener != null)
              buttonListener.buttonHit(buttonID);
          }
          else if ((keyState & Button.ID_UP) != 0)
          {
            buttonID = Button.ID_UP;
            if (buttonListener != null)
              buttonListener.buttonHit(buttonID);
          }
          else if ((keyState & Button.ID_LEFT) != 0)
          {
            buttonID = Button.ID_LEFT;
            if (buttonListener != null)
              buttonListener.buttonHit(buttonID);
          }
          else if ((keyState & Button.ID_RIGHT) != 0)
          {
            buttonID = Button.ID_RIGHT;
            if (buttonListener != null)
              buttonListener.buttonHit(buttonID);
          }
          else if ((keyState & Button.ID_ENTER) != 0)
          {
            buttonID = Button.ID_ENTER;
            if (buttonListener != null)
              buttonListener.buttonHit(buttonID);
          }
          else if ((keyState & Button.ID_ESCAPE) != 0)
          {
            buttonID = Button.ID_ESCAPE;
            if (buttonListener != null)
              buttonListener.buttonHit(buttonID);
          }
          isButtonHit = true;
        }
        try
        {
          Thread.currentThread().sleep(pollPeriod);
        }
        catch (InterruptedException ex)
        {
        }
      }
    }
  }

  private Vector<Part> parts = new Vector<Part>();
  private static EV3Properties props;
  private static int debugLevel;
  private static int sensorEventDelay;
  private boolean isMelodyEnabled = true;
  private boolean isActive;
  private LED led;
  private int buttonID;
  private boolean isButtonHit = false;
  private ButtonListener buttonListener = null;
  private MyKeyListener keyListener = null;
  private boolean isKeyListenerRunning;

  /**
   * Creates an instance of LegoRobot and and delays execution until the user presses the
   * ENTER button. A ButtonListener thread is started to get button hit events.
   * @param waitStart if true, the execution is stopped until the ENTER button is hit.
   * @param isMelodyEnabled if true, a connect and disconnect melody is played
   */
  public LegoRobot(boolean waitStart, boolean isMelodyEnabled)
  {
    isActive = true;
    this.isMelodyEnabled = isMelodyEnabled;
    Brick localBrick = LocalEV3.get();
    led = localBrick.getLED();
    led.setPattern(1);
    if (isMelodyEnabled)
      playConnectMelody();
    if (waitStart)
    {
      LCD.clear();
      LCD.drawString("Press ENTER", 0, 0);
      Tools.waitEnter();
      LCD.drawString("Program started", 0, 0);
    }
    initKeyListener();
    props = new EV3Properties();
    debugLevel = props.getIntValue("DebugLevel");
    sensorEventDelay = props.getIntValue("SensorEventDelay");
    if (debugLevel > SharedConstants.DEBUG_LEVEL_OFF)
      DebugConsole.show("DebugLevel: " + debugLevel);
  }

  /**
   * Creates an instance of LegoRobot and and delays execution until the user presses the
   * ENTER button. A ButtonListener thread is started to get button hit events.
   * A connect and disconnect melody is played.
   * @param waitStart if true, the execution is stopped until the ENTER button is hit.
   */
  public LegoRobot(boolean waitStart)
  {
    this(waitStart, true);
  }

  /**
   * Creates an instance of LegoRobot and starts execution normally.
   * A connect and disconnect melody is played. 
   * A ButtonListener thread is started to get button hit events.
   */
  public LegoRobot()
  {
    this(false, true);
  }

  protected static EV3Properties getProperties()
  {
    return props;
  }

  protected static int getDebugLevel()
  {
    return debugLevel;
  }

  protected static int getSensorEventDelay()
  {
    return sensorEventDelay;
  }

  /**
   * Assembles the given part into the robot.
   * Initializes the part.
   * @param part the part to assemble
   */
  public void addPart(Part part)
  {
    if (!isActive)
      return;
    if (part instanceof Sensor)
    {
      for (int i = 0; i < parts.size(); i++)
      {
        if (parts.elementAt(i) instanceof Sensor)
        {
          if (((Sensor)parts.elementAt(i)).getPortId() == ((Sensor)part).getPortId())
            new ShowError("Port " + ((Sensor)part).getPortLabel() + " conflict");
        }
      }
    }

    if (part instanceof GenericMotor)
    {
      for (int i = 0; i < parts.size(); i++)
      {
        if (parts.elementAt(i) instanceof GenericMotor)
        {
          if (((GenericMotor)parts.elementAt(i)).getPortId() == ((GenericMotor)part).getPortId())
            new ShowError("Port " + ((GenericMotor)part).getPortLabel() + " conflict");
        }
      }
    }

    if (part instanceof NxtMotor)
    {
      for (int i = 0; i < parts.size(); i++)
      {
        if (parts.elementAt(i) instanceof NxtMotor)
        {
          if (((NxtMotor)parts.elementAt(i)).getPortId() == ((NxtMotor)part).getPortId())
            new ShowError("Port " + ((NxtMotor)part).getPortLabel() + " conflict");
        }
      }
    }

    part.setRobot(this);
    parts.addElement(part);
    part.init();
  }

  /**
   * Returns the battery level.
   * @return voltage (in Millivolt)
   */
  public int getBatteryLevel()
  {
    if (!isActive)
      return -1;
    return (int)(1000 * Battery.getVoltage());
  }

  /**
   * Returns library version information.
   * @return library version
   */
  public static String getVersion()
  {
    return SharedConstants.VERSION;
  }

  /**
   * Calls cleanup() of all sensors, stops any running motor and terminates the running program.
   */
  public void exit()
  {
    stopKeyListener();
    if (!isActive)
      return;
    isActive = false;
    for (Part part : parts)
      part.cleanup();
    Tools.delay(1000);
    if (isMelodyEnabled)
      playDisconnectMelody();
    led.setPattern(0);
    if (debugLevel > SharedConstants.DEBUG_LEVEL_OFF)
    {
      DebugConsole.show("DEBUG wait...");
      Tools.delay(5000);
    }
  }

  /**
   * Plays a tone of given frequency (in Hz) for the given duration (in ms).
   * The method returns immediately while the tone is still playing. 
   * In order to play a melody, the program
   * should be delayed (using Tools.delay()) for an appropriate duration.
   * @param frequency the frequency in Hertz (must be int)
   * @param duration the duration in milliseconds (must be int)
   */
  public void playTone(int frequency, int duration)
  {
    Sound.playTone(frequency, duration);
  }

  /**
   * Plays a wav file. The file must reside in /home/root and use the filename
   * song<tag>.wav, where <tag> is a given integer that identifies the file.
   * The method is blocked until the sound is finished.
   * Supported wav file format: mono, 8 bit unsigned or 16 bit signed. maximum sampling rate 11025 Hz.
   * @param tag a number to identify the file in /home/root/music/song<tag>.wav
   * @param vol the volume (0..100)
   */
  public void playSampleWait(int tag, int volume)
  {
    String path = "/home/root/music/song" + tag + ".wav";
    Player p = new Player(path, volume);
    p.startPlaying(true);
  }

  /**
   * Plays a wav file. The file must reside in /home/root and use the filename
   * song<tag>.wav, where <tag> is a given integer that identifies the file.
   * The method starts the playing and returns.
   * Supported wav file format: mono, 8 bit unsigned or 16 bit signed. maximum sampling rate 11025 Hz.
   * @param tag a number to identify the file in /home/root/music/song<tag>.wav
   * @param vol the volume (0..100)
   */
  public void playSample(int tag, int volume)
  {
    String path = "/home/root/music/song" + tag + ".wav";
    Player p = new Player(path, volume);
    p.startPlaying(false);
  }

  /**
   * Sets the sound volume.
   * Master volume, used also for connect/disconnect melody.
   * @param volume the sound volume (0..100)
   */
  public void setVolume(int volume)
  {
    Sound.setVolume(volume);
  }

  /**
   * Turn on/off the brick's left/right LEDs (only affected in pair). 
   * Pattern mask:<br>
   * 0: off<br>
   * 1: green<br>
   * 2: red<br>
   * 3: red bright<br>
   * 4: green blinking<br>
   * 5: red blinking<br>
   * 6: red blinking bright<br>
   * 7: green double blinking<br>
   * 8: red double blinking<br>
   * 9: red double blinking bright<br>
   * @param pattern the pattern 0..9
   */
  public void setLED(int pattern)
  {
    Brick localBrick = LocalEV3.get();
    localBrick.getLED().setPattern(pattern);
  }

  protected synchronized void playConnectMelody()
  {
    Tools.delay(1000);
    Sound.playTone(600, 100);
    Tools.delay(30);
    Sound.playTone(900, 100);
    Tools.delay(30);
    Sound.playTone(750, 200);
    Tools.delay(200);
  }

  protected synchronized void playDisconnectMelody()
  {
    Tools.delay(1000);
    Sound.playTone(900, 100);
    Tools.delay(30);
    Sound.playTone(750, 100);
    Tools.delay(30);
    Sound.playTone(600, 200);
    Tools.delay(200);
  }

  protected static void showDebug(String msg)
  {
    LCD.clear();
    LCD.refresh();
    System.out.println(msg);
    Tools.delay(6000);
  }

  public String getRobotProperties()
  {
    String s = ""
      + "\nDebugLevel = " + props.getIntValue("DebugLevel")
      + "\nMotorSpeed = " + props.getIntValue("MotorSpeed")
      + "\nMotorSpeedFactor = " + props.getDoubleValue("MotorSpeedFactor")
      + "\nMotorSpeedMultiplier = " + props.getDoubleValue("MotorSpeedMultiplier")
      + "\nGearSpeed = " + props.getIntValue("GearSpeed")
      + "\nGearAcceleration = " + props.getIntValue("GearAcceleration")
      + "\nGearBrakeDelay = " + props.getIntValue("GearBrakeDelay")
      + "\nAxeLength = " + props.getDoubleValue("AxeLength")
      + "\nTurtleSpeed = " + props.getIntValue("TurtleSpeed")
      + "\nTurtleStepFactor = " + props.getDoubleValue("TurtleStepFactor")
      + "\nTurtleRotationFactor = " + props.getDoubleValue("TurtleRotationSpeed")
      + "\nSoundSensorPollDelay = " + props.getIntValue("SoundSensorPollDelay")
      + "\nLightSensorPollDelay = " + props.getIntValue("LightSensorPollDelay")
      + "\nTouchSensorPollDelay = " + props.getIntValue("TouchSensorPollDelay")
      + "\nUltrasonicSensorPollDelay = " + props.getIntValue("UltrasonicSensorPollDelay")
      + "\nCompassSensorPollDelay = " + props.getIntValue("CompassSensorPollDelay")
      + "\nPrototypeSensorPollDelay = " + props.getIntValue("PrototypeSensorPollDelay")
      + "\nMotionDetectorPollDelay = " + props.getIntValue("MotionDetectorPollDelay")
      + "\nPrototypeSensorPollDelay = " + props.getIntValue("PrototypeSensorPollDelay")
      + "\nInfraredSeekerPollDelay = " + props.getIntValue("InfraredSeekerPollDelay")
      + "\nRFIDSensorPollDelay = " + props.getIntValue("RFIDSensorPollDelay")
      + "\nHTGyroSensorPollDelay = " + props.getIntValue("HTGyroSensorPollDelay")
      + "\nSensorEventDelay = " + props.getIntValue("SensorEventDelay")
      + "\nIPport = " + props.getIntValue("IPport")
      + "\nDucEnabled = " + props.getStringValue("DucEnabled")
      + "\nDucHostUrl = " + props.getStringValue("DucHostUrl")
      + "\nDucEV3Url = " + props.getStringValue("DucEV3Url")
      + "\nDucUserPass = " + props.getStringValue("DucUserPass")
      + "\nDucUpdateInterval = " + props.getIntValue("DucUpdateInterval")
      + "\nDucIdleUpdateInterval = " + props.getIntValue("DucIdleUpdateInterval");
    return s;
  }

  /**
   * Registers a button listener to report events
   * when one of the brick buttons is hit. 
   * @param listener the ButtonListener to register
   */
  public void addButtonListener(ButtonListener listener)
  {
    buttonListener = listener;
  }

  private void initKeyListener()
  {
    isButtonHit = false;
    isKeyListenerRunning = true;
    keyListener = new MyKeyListener();
    keyListener.start();
  }

  private void stopKeyListener()
  {
    isButtonHit = false;
    isKeyListenerRunning = false;
  }

  /**
   * Returns true, if any of the buttons was hit. 
   * Calls Thread.sleep(10) to be used in tide loops.
   * A button listener must be started before by calling startButtonListener.
   * @return true, if a button was hit
   */
  public boolean isButtonHit()
  {
    Tools.delay(10);
    return isButtonHit;
  }

  /**
   * Returns the button ID of the button previously hit. The button hit
   * buffer is cleared then.
   * @return the ID of the button; if the hit buffer is empty or the button listener is
   * is not running, return 0
   */
  public int getHitButtonID()
  {
    if (!isButtonHit)
      return 0;
    isButtonHit = false;
    return buttonID;
  }

  /**
   * Returns true, if the UP button was the last button hit since 
   * the last call of this method. On return, the button hit is cleared.
   * Calls Thread.sleep(10) to be used in tide loops.
   * @return true, if the up button was clicked
   */
  public boolean isUpHit()
  {
    Tools.delay(10);
    boolean pressed = isButtonHit && buttonID == Button.ID_UP;
    if (pressed)
      isButtonHit = false;
    return pressed;
  }

  /**
   * Returns true, if the DOWN button was the last button hit since 
   * the last call of this method. On return, the button hit is cleared.
   * Calls Thread.sleep(10) to be used in tide loops.
   * @return true, if the up button was clicked
   */
  public boolean isDownHit()
  {
    Tools.delay(10);
    boolean pressed = isButtonHit && buttonID == Button.ID_DOWN;
    if (pressed)
      isButtonHit = false;
    return pressed;
  }

  /**
   * Returns true, if the LEFT button was the last button hit since 
   * the last call of this method. On return, the button hit is cleared.
   * Calls Thread.sleep(10) to be used in tide loops.
   * @return true, if the up button was clicked
   */
  public boolean isLeftHit()
  {
    Tools.delay(10);
    boolean pressed = isButtonHit && buttonID == Button.ID_LEFT;
    if (pressed)
      isButtonHit = false;
    return pressed;
  }

  /**
   * Returns true, if the RIGHT button was the last button hit since 
   * the last call of this method. On return, the button hit is cleared.
   * Calls Thread.sleep(10) to be used in tide loops.
   * @return true, if the up button was clicked
   */
  public boolean isRightHit()
  {
    Tools.delay(10);
    boolean pressed = isButtonHit && buttonID == Button.ID_RIGHT;
    if (pressed)
      isButtonHit = false;
    return pressed;
  }

  /**
   * Returns true, if the ENTER button was the last button hit since 
   * the last call of this method. On return, the button hit is cleared.
   * Calls Thread.sleep(10) to be used in tide loops.
   * @return true, if the up button was clicked
   */
  public boolean isEnterHit()
  {
    Tools.delay(10);
    boolean pressed = isButtonHit && buttonID == Button.ID_ENTER;
    if (pressed)
      isButtonHit = false;
    return pressed;
  }

  /**
   * Returns true, if the ESCAPE button was the last button hit since 
   * the last call of this method. On return, the button hit is cleared.
   * Calls Thread.sleep(10) to be used in tide loops.
   * @return true, if the up button was clicked
   */
  public boolean isEscapeHit()
  {
    Tools.delay(10);
    boolean pressed = isButtonHit && buttonID == Button.ID_ESCAPE;
    if (pressed)
      isButtonHit = false;
    return pressed;
  }

  /**
   * Draws the given text line starting at given position. 
   * The display is 18 characters wide and 8 lines high.
   * Characters outside the visual range are hidden.
   * @param text the text to show
   * @param x the column number (0..17)
   * @param y the line number (0..7)
   */
  public void drawString(String text, int x, int y)
  {
    LCD.clear(y);
    LCD.drawString(text, x, y);
    LCD.refresh();
  }

  /**
   * Draws the given text line starting at given screen cell count. 
   * The display is 18 characters wide and 8 lines high.
   * Characters outside the visual range are hidden.
   * @param text the text to show
   * @param count the cell count (cells are counted from zero line-per-line
   */
  public void drawStringAt(String text, int count)
  {
    int x = count % 18;
    int y = count / 18;
    drawString(text, x, y);
  }

  /**
   * Clears the display. 
   */
  public void clearDisplay()
  {
    LCD.clear();
    LCD.refresh();
  }
}
