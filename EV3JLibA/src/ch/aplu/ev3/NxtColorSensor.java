// NxtColorSensor.java

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

import java.awt.Color;

/**
 * Class that represents a color sensor (Lego NXT Color Sensor)
 */
public class NxtColorSensor extends Sensor
{
  private lejos.hardware.sensor.NXTColorSensor cs;

  /**
   * Creates a sensor instance connected to the given port.
   * @param port the port where the sensor is plugged-in
   */
  public NxtColorSensor(SensorPort port)
  {
    super(port);
    cs = new lejos.hardware.sensor.NXTColorSensor(getLejosPort());
    sm = cs.getRGBMode();
  }

  /**
   * Creates a sensor instance connected to port S1.
   */
  public NxtColorSensor()
  {
    this(SensorPort.S1);
  }

  protected void init()
  {
    if (LegoRobot.getDebugLevel() >= SharedConstants.DEBUG_LEVEL_MEDIUM)
      DebugConsole.show("_cs.init()");
  }

  protected void cleanup()
  {
    if (LegoRobot.getDebugLevel() >= SharedConstants.DEBUG_LEVEL_MEDIUM)
      DebugConsole.show("_cs.cleanup()");
    setFloodlight(-1);  // All off
    cs.close();
  }

  /**
   * Returns the reference of the the underlying 
   * lejos.hardware.sensor.NxtColorSensor.
   * @return the reference of the lejos.hardware.sensor.NXTColorSensor
   */
  public lejos.hardware.sensor.NXTColorSensor getLejosSensor()
  {
    return cs;
  }

  /**
   * Returns a java.awt.Color reference to the color reading.
   * @return the RGB color
   */
  public Color getColor()
  {
    checkConnect();
    int sampleSize = 4;
    float samples[] = new float[sampleSize];
    sm.fetchSample(samples, 0);
    return new Color((int)(255 * samples[0]),
      (int)(255 * samples[1]),
      (int)(255 * samples[2]));
  }

  /**
   * Returns the color as integer value of the color reading.
   * Red in bits 0..6, Green in bits 7..15, Blue in bits 16..23, 1 in bits 24..31.
   * @return color int
   */
  public int getColorInt()
  {
    checkConnect();
    int sampleSize = 4;
    float samples[] = new float[sampleSize];
    sm.fetchSample(samples, 0);
    return getIntFromColor(samples[0], samples[1], samples[2]);
  }

  private int getIntFromColor(float red, float green, float blue)
  {
    int r = Math.round(255 * red);
    int g = Math.round(255 * green);
    int b = Math.round(255 * blue);

    r = (r << 16) & 0x00FF0000;
    g = (g << 8) & 0x0000FF00;
    b = b & 0x000000FF;

    return 0xFF000000 | r | g | b;
  }

  /**
   * Returns the intensity of the detected light.
   * @return light intensity (0..1023)
   */
  public int getLightValue()
  {
    checkConnect();
    int sampleSize = 4;
    float samples[] = new float[sampleSize];
    sm.fetchSample(samples, 0);
    return (int)(1023 * samples[3]);
  }

  /**
   * Turns the red, green or blue LED on or all off.
   * @param colorValue 0: red, 1: green, 2: blue, -1: all off
   */
  public void setFloodlight(int colorValue)
  {
    checkConnect();
    cs.setFloodlight(colorValue);
  }

  private void checkConnect()
  {
    if (robot == null)
      new ShowError("NxtColorSensor (port: " + getPortLabel()
        + ") is not a part of the LegoRobot.\n"
        + "Call addPart() to assemble it.");
  }
}
