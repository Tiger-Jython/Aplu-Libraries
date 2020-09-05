// Sensor.java

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
 * Abstract class as ancestor of all sensors.
 */
public abstract class Sensor extends Part
{
  private SensorPort port;
  private int portId;
  protected lejos.hardware.sensor.SensorMode sm;
  private lejos.hardware.port.Port sp;

  /**
   * Returns the reference of the the underlying lejos.nxt.SensorPort.
   * @return the reference of the lejos.hardware.port.Port
   */
  public lejos.hardware.port.Port getLejosPort()
  {
    return sp;
  }
 
  protected Sensor(SensorPort port)
  {
    this.port = port;
    portId = port.getId();
    sp = getSensorPort(port);
  }

  protected SensorPort getPort()
  {
    return port;
  }

  protected int getPortId()
  {
    return portId;
  }

  protected String getPortLabel()
  {
    return port.getLabel();
  }
  
  private lejos.hardware.port.Port getSensorPort(SensorPort port)
  { 
    switch (port.getId())
    {
      case 0:
        return lejos.hardware.port.SensorPort.S1;
      case 1:
        return lejos.hardware.port.SensorPort.S2;
      case 2:
        return lejos.hardware.port.SensorPort.S3;
      case 3:
        return lejos.hardware.port.SensorPort.S4;
      default:
        return lejos.hardware.port.SensorPort.S1;
    }
  }

}

