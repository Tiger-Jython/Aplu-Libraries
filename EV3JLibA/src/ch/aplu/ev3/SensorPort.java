// SensorPort.java

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

/**
 * Useful declarations for port connections.
 */
public class SensorPort
{
  private int portId;

  /**
   * Declaration used by a sensor connected to port S1.
   */
  public final static SensorPort S1 = new SensorPort(0);

  /**
   * Declaration used by a sensor connected to port S2.
   */
  public final static SensorPort S2 = new SensorPort(1);

  /**
   * Declaration used by a sensor connected to port S3.
   */
  public final static SensorPort S3 = new SensorPort(2);

  /**
   * Declaration used by a sensor connected to port S4.
   */
  public final static SensorPort S4 = new SensorPort(3);

  /**
   * Creates a SensorPort from the port id.
   * @param portId the port id (0..3)
   */
  public SensorPort(int portId)
  {
    this.portId = portId;
  }

  /**
   * Returns the port identification as integer.
   * @return port id (number 0..3)
   */
  public int getId()
  {
    return portId;
  }

  /**
   * Returns the port identification as string.
   * @return port id (S1, S2, S3, S4)
   */
  public String getLabel()
  {
    switch (portId)
    {
      case 0: return "S1";
      case 1: return "S2";
      case 2: return "S3";
      case 3: return "S4";
    }
    return "";
  }

  /**
   * Compares the labels of the current port to the given port.
   * @return true, if both labels match; otherwise false
   */
  public boolean equals(SensorPort port)
  {
    return getLabel().equals(port.getLabel());
  }
}
