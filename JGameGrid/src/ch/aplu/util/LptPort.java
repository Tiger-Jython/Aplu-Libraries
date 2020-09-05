// LptPort.java

/*
 This software is part of the JEX (Java Exemplarisch) Utility Library.
 It is Open Source Free Software, so you may
 - run the code for any purpose
 - study how the code works and adapt it to your needs
 - integrate all or parts of the code in your own programs
 - redistribute copies of the code
 - improve the code and release your improvements to the public
 However the use of the code is entirely your responsibility.
 */
package ch.aplu.util;

/**
 * Support to write to and read from the LPT port registers on a Windows operating system.
 * Uses the Java Native Interface (JNI) and INPOUT32.DLL by Jan Axelsons.
 * Circumvents Windows protection of I/O-ports on W2K, XP, W2003 OS
 * by installing a driver on the fly (when opening the port).
 * INPOUT32.DLL and JLPTPORT.DLL needed in file system path (distributed
 * with Examples, Chap. 34).
 */
public class LptPort
{
  /**
   * Bit value of bit 0 in data register of LPT port (at base address).
   * Data may be written (to the external device) or read (from the external device).
   */
  final public static int DATA0 = 1;
  /**
   * Bit value of bit 1 in data register of LPT port (at base address).
   * Data may be written (to the external device) or read (from the external device).
   */
  final public static int DATA1 = 2;
  /**
   * Bit value of bit 2 in data register of LPT port (at base address).
   * Data may be written (to the external device) or read (from the external device).
   */
  final public static int DATA2 = 4;
  /**
   * Bit value of bit 3 in data register of LPT port (at base address).
   * Data may be written (to the external device) or read (from the external device).
   */
  final public static int DATA3 = 8;
  /**
   * Bit value of bit 4 in data register of LPT port (at base address).
   * Data may be written (to the external device) or read (from the external device).
   */
  final public static int DATA4 = 16;
  /**
   * Bit value of bit 5 in data register of LPT port (at base address).
   * Data may be written (to the external device) or read (from the external device).
   */
  final public static int DATA5 = 32;
  /**
   * Bit value of bit 6 in data register of LPT port (at base address).
   * Data may be written (to the external device) or read (from the external device).
   */
  final public static int DATA6 = 64;
  /**
   * Bit value of bit 7 in data register of LPT port (at base address).
   * Data may be written (to the external device) or read (from the external device).
   */
  final public static int DATA7 = 128;
  /**
   * Bit value of Error line in status register of LPT port (at base address + 1).
   * The external device informs the program about special conditions.
   */
  final public static int ERROR = 8;
  /**
   * Bit value of Select line in status register of LPT port (at base address + 1).
   * The external device informs the program about special conditions.
   */
  final public static int SELECT = 16;
  /**
   * Bit value of Paper End line in status register of LPT port (at base address + 1).
   * The external device informs the program about special conditions.
   */
  final public static int PAPEREND = 32;
  /**
   * Bit value of Acknowledge line in status register of LPT port (at base address + 1).
   * The external device informs the program about special conditions.
   */
  final public static int ACKNOWLEDGE = 64;
  /**
   * Bit value of Busy line in status register of LPT port (at base address + 1).
   * The external device informs the program about special conditions.
   */
  final public static int BUSY = 128;
  /**
   * Bit value of Strobe line in control register of LPT port (at base address + 2).
   * The program sends some special commands to the external device.
   */
  final public static int STROBE = 1;
  /**
   * Bit value of Auto Linefeed line in control register of LPT port (at base address + 2).
   * The program sends some special commands to the external device.
   */
  final public static int AUTOLINEFEED = 2;
  /**
   * Bit value of Init/Reset line in control register of LPT port (at base address + 2).
   * The program sends some special commands to the external device.
   */
  final public static int INITRESET = 4;
  /**
   * Bit value of Select In line in control register of LPT port (at base address + 2).
   * The program sends some special commands to the external device.
   */
  final public static int SELECTIN = 8;

  /**
   * Open LPT port at specified base address, e.g. 0x378.
   * Return 0 if successful, -1 otherwise.
   */
  public native int open(int address);

  /**
   * Return true if port is open, false otherwise.
   */
  public native boolean isOpen();

  /**
   * Write lower byte of data into data port (at base address).
   * Upper byte is ignored.
   * Return -1 if port is not open, 0 otherwise
   */
  public native int writeData(int data);

  /**
   * Write lower byte of data into control port (at base address + 2).
   * Upper byte is ignored.
   * Return -1 if port is not open, 0 otherwise
   */
  public native int writeControl(int data);

  /**
   * Return content of data port (at base address) in lower byte.
   * Upper byte is 0.
   * Return -1 if port is not open.
   */
  public native int readData();

  /**
   * Return content of status port (at base address + 1) in lower byte.
   * Upper byte is 0.
   * Return -1 if port is not open.
   */
  public native int readStatus();

  /**
   * Close port
   * Do nothing, if port is not open.
   */
  public native void close();

}
