// EV3Exception.java
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


class EV3Exception extends RuntimeException
{
  public EV3Exception(String s)
  {
    super(s);
  }
}
