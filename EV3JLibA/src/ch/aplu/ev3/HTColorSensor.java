// HTColorSensor.java

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
 * Class that represents a color sensor (HiTechnic Color Sensor in RGB mode)
 */
public class HTColorSensor extends Sensor
{
  private lejos.hardware.sensor.HiTechnicColorSensor hcs;

  /**
   * Creates a sensor instance connected to the given port.
   * @param port the port where the sensor is plugged-in
   */
  public HTColorSensor(SensorPort port)
  {
    super(port);
    hcs = new lejos.hardware.sensor.HiTechnicColorSensor(getLejosPort());
    sm = hcs.getRGBMode();
  }

  /**
   * Creates a sensor instance connected to port S1.
   */
  public HTColorSensor()
  {
    this(SensorPort.S1);
  }

  protected void init()
  {
    if (LegoRobot.getDebugLevel() >= SharedConstants.DEBUG_LEVEL_MEDIUM)
      DebugConsole.show("hcs.init()");
  }

  protected void cleanup()
  {
    if (LegoRobot.getDebugLevel() >= SharedConstants.DEBUG_LEVEL_MEDIUM)
      DebugConsole.show("hcs.cleanup()");
    hcs.close();
  }

  /**
   * Returns the reference of the the underlying 
   * lejos.hardware.sensor.HiTechnicColorSensor.
   * @return the reference of the lejos.hardware.sensor.EV3ColorSensor
   */
  public lejos.hardware.sensor.HiTechnicColorSensor getLejosSensor()
  {
    return hcs;
  }

  /**
   * Returns a java.awt.Color reference to the color reading.
   * @return the RGB color
   */
  public Color getColor()
  {
    checkConnect();
    int sampleSize = 3;
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
    int sampleSize = 3;
    float samples[] = new float[sampleSize];
    sm.fetchSample(samples, 0);
    return getIntFromColor(samples[0], samples[1], samples[2]);
  }
  
  /**
   * Returns the color ID (0..17).<br>
  ,* 0: black<br>
   * 1,2,3; blue<br>
   * 4, 5: green<br>
   * 4, 5: green<br>
   * 6, 7: yellow<br>
   * 8, 9, 10: red<br>
   * 11-16: light red<br>
   * 17: white<br>
   * (see www.hitechni.com for color chart).
   * @return color identifier
   */
  public int getColorID()
  {
    checkConnect();
    return hcs.getColorID();
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

  private void checkConnect()
  {
    if (robot == null)
      new ShowError("HTColorSensor (port: " + getPortLabel()
        + ") is not a part of the LegoRobot.\n"
        + "Call addPart() to assemble it.");
  }
}
