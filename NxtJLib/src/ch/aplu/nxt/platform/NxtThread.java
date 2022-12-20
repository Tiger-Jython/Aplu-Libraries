// NxtThread.java, Java SE version
// Platform (Java SE, ME) dependent code
// Should be visible in package only. Not included in Javadoc
// Dummy class in J2SE. Simulates join(int millis) in J2ME.

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

package ch.aplu.nxt.platform;

public class NxtThread extends Thread
{
 public void joinX(long millis) throws InterruptedException
 {
   try
   {
     join(millis);
   }
   catch (InterruptedException ex)
   {
     throw ex;
   }
 }
}
   