// MotorPort.java
// Direct mode

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
 * Useful declarations for motor port connections.
 */
public class MotorPort
{
  private int portId;

  /**
   * Declaration used by a motor connected to port A.
   */
  public final static MotorPort A = new MotorPort(0);

  /**
   * Declaration used by a motor connected to port B.
   */
  public final static MotorPort B = new MotorPort(1);

  /**
   * Declaration used by a motor connected to port C.
   */
  public final static MotorPort C = new MotorPort(2);

  /**
   * Declaration used by a motor connected to port C.
   */
  public final static MotorPort D = new MotorPort(3);

  /**
   * Creates a MotorPort from the port id.
   * @param portId the port id (0..3)
   */
  public MotorPort(int portId)
  {
    this.portId = portId;
  }

  protected int getId()
  {
    return portId;
  }

  protected String getLabel()
  {
    switch (portId)
    {
      case 0:
        return "A";
      case 1:
        return "B";
      case 2:
        return "C";
      case 3:
        return "D";
    }
    return "";
  }
}
