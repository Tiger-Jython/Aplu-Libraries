/*
This software is part of the NxtJLib library.
It is Open Source Free Software, so you may
- run the code for any purpose
- study how the code works and adapt it to your needs
- integrate all or parts of the code in your own programs
- redistribute copies of the code
- improve the code and release your improvements to the protected
However the use of the code is entirely your responsibility.
*/

package ch.aplu.nxt;

import ch.aplu.nxt.platform.DebugConsole;

/** 
 * Class for packing information when making a request to the NXT brick.
 * Copied from the leJOS distribution (jejos.sourceforge.net) with thanks to the autor.
 */
class InputValues
{
  protected int inputPort;

  protected boolean valid = true;
  protected boolean isCalibrated;
  protected int sensorType;
  protected int sensorMode;
 
  protected int rawADValue;
  protected int normalizedADValue;
  protected short scaledValue;
  protected short calibratedValue;
  
  protected void printValues()
  {
    DebugConsole.show("  input port: " + inputPort);
    DebugConsole.show("  valid: " + valid);
    DebugConsole.show("  isCalibrated: " + isCalibrated);
    DebugConsole.show("  sensorType: " + sensorType);
    DebugConsole.show("  sensorMode: " + sensorMode);
    DebugConsole.show("  rawADValue: " + rawADValue);
    DebugConsole.show("  normalizedADValue: " + normalizedADValue);
    DebugConsole.show("  scaledValue: " + scaledValue);
    DebugConsole.show("  calibratedValue: " + calibratedValue);
   }  
}
