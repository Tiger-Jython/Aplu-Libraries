// SensorPort.java

/*
  This software is part of the NxtJLib library.
  It is Open Source Free Software, so you may
    - run the code for any purpose
    - study how the code works and adapt it to your needs
    - integrate all or parts of the code in your own programs
    - redistribute copies of the code
    - improve the code and release your improvements to the public
  However the use of the code is entirely your responsibility.
 */

package ch.aplu.nxt;

/**
 * Useful declarations for sensor port connections.
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

  private SensorPort(int portId)
  {
    this.portId = portId;
  }  

  /**
   * Return the port id (0 for S1, 1 for S2, 2 for S3, 3 for S4).
   * @return the port id
   */
  public int getId()
  {
    return portId;
  }  
  
  /**
   * Return the port label S1, S2, S3, S4.
   * @return the port label
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
}
