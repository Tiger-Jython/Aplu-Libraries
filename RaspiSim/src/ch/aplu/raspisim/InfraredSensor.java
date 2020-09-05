// InfraredSensor.java

/*
 This software is part of the RaspiSim library.
 It is Open Source Free Software, so you may
 - run the code for any purpose
 - study how the code works and adapt it to your needs
 - integrate all or parts of the code in your own programs
 - redistribute copies of the code
 - improve the code and release your improvements to the public
 However the use of the code is entirely your responsibility.

 Author: Aegidius Pluess, www.aplu.ch
 */
package ch.aplu.raspisim;

import ch.aplu.jgamegrid.*;
import java.awt.*;

/**
 * Class that represents a touch sensor.
 */
public class InfraredSensor
{
  //
  /** Constant for id of front sensor */
  public static int IR_CENTER = 0;
  /** Constant for id of left sensor */
  public static int IR_LEFT = 1;
  /** Constant for id of right  sensor */
  public static int IR_RIGHT = 2;
  /** Constant for id of line left sensor (points to floor) */
  public static int IR_LINE_LEFT = 3;
  /** Constant for id of line right sensor (points to floor) */
  public static int IR_LINE_RIGHT = 4;

  private int id;
  private IRSensor irSensor;
  private LineSensor lineSensor;

  /**
   * Creates a sensor with given id.
   * @param id identification number
   */
  public InfraredSensor(int id)
  {
    this.id = id;
    if (id < 3)
      irSensor = new IRSensor(id);
    else
      lineSensor = new LineSensor(id - 3);
  }

  /**
   * Register the given InfraredListener to detect bright and dark events.
   * Starts an internal sensor thread that polls the sensor level and runs the
   * sensor callbacks.
   * @param listener the InfraredListener to register; null, to terminate any running
   * sensor thread
   */
  public void addInfraredListener(InfraredListener listener)
  {
    if (id < 3)
      irSensor.addInfraredListener(listener);
    else
      lineSensor.addInfraredListener(listener);
  }

  /**
   * Checks, if infrared light ist detected.
   * @return 1, if the sensor detects infrared light; otherwise 0
   */
   public int getValue()
  {
    if (id < 3)
      return irSensor.getValue();
    return lineSensor.getValue();
  }
}
