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
 * Class for packing information about the output state of a specific port.
 * Copied from the leJOS distribution (jejos.sourceforge.net) with thanks to the autor.
 */
class OutputState
{
  protected byte status;
  protected int outputPort; 
  protected byte powerSetpoint;
  protected int mode; 
  protected int regulationMode;
  protected byte turnRatio;
  protected int runState;
  protected int tachoLimit;
  protected int tachoCount;
  protected int blockTachoCount;
  protected int rotationCount;

  protected OutputState(int port)
  {
    outputPort = port;
  }
}
