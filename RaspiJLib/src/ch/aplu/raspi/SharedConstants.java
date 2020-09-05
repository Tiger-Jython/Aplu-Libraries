// ShareConstants.java

/*
This software is part of the RaspiJLib library.
It is Open Source Free Software, so you may
- run the code for any purpose
- study how the code works and adapt it to your needs
- integrate all or parts of the code in your own programs
- redistribute copies of the code
- improve the code and release your improvements to the public
However the use of the code is entirely your responsibility.
*/

/* History:
  V1.00 - Aug 2015: - Ported from Python remote library
  V1.01 - Oct 2015: - Check if robot is created
  V1.02 - Oct 2015: - Fixed: Constant Led.LED_RIGHT now correct
  V1.03 - Oct 2015: - Added: Led.setColor(), setColorAll() with X11 color name
*/
package ch.aplu.raspi;

interface SharedConstants
{
  boolean debug = false;
  
  String ABOUT =
    "2003-2015 Aegidius Pluess\n" +
    "OpenSource Free Software\n" +
    "http://www.aplu.ch\n" +
    "All rights reserved";
  String VERSION = "1.03 - Oct 2015";
  String TITLE = "RaspiJLib V" + VERSION + "   (www.aplu.ch)";
}
