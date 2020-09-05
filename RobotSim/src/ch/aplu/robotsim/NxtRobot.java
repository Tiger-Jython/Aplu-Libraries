// NxtRobot.java

/*
 This software is part of the RobotSim library.
 It is Open Source Free Software, so you may
 - run the code for any purpose
 - study how the code works and adapt it to your needs
 - integrate all or parts of the code in your own programs
 - redistribute copies of the code
 - improve the code and release your improvements to the public
 However the use of the code is entirely your responsibility.

 Author: Aegidius Pluess, www.aplu.ch
 */
package ch.aplu.robotsim;

/**
 * Class that represents a simulated NXT robot brick. Parts (e.g. motors, sensors) may
 be assembled into the robot to make it doing the desired job. Each instance
 creates its own square playground (501 x 501 pixels). Some initial conditions may be modified by
 calling static methods of the class NxtContext in a static block. A typical example
 is:<br><br>
 * <code>
 <font color="#0000ff">import</font><font color="#000000">&nbsp;ch.aplu.nxtsim.</font><font color="#c00000">*</font><font color="#000000">;</font><br>
 <font color="#000000"></font><br>
 <font color="#0000ff">public</font><font color="#000000">&nbsp;</font><font color="#0000ff">class</font><font color="#000000">&nbsp;Example</font><br>
 <font color="#000000">{</font><br>
 <font color="#000000">&nbsp;&nbsp;</font><font color="#0000ff">static</font><br>
 <font color="#000000">&nbsp;&nbsp;</font><font color="#000000">{</font><br>
 <font color="#000000">&nbsp;&nbsp;&nbsp;&nbsp;NxtContext.</font><font color="#000000">setStartPosition</font><font color="#000000">(</font><font color="#000000">100</font><font color="#000000">,&nbsp;</font><font color="#000000">100</font><font color="#000000">)</font><font color="#000000">;</font><br>
 <font color="#000000">&nbsp;&nbsp;&nbsp;&nbsp;NxtContext.</font><font color="#000000">setStartDirection</font><font color="#000000">(</font><font color="#000000">45</font><font color="#000000">)</font><font color="#000000">;</font><br>
 <font color="#000000">&nbsp;&nbsp;</font><font color="#000000">}</font><br>
 <font color="#000000"></font><br>
 <font color="#000000">&nbsp;&nbsp;</font><font color="#0000ff">public</font><font color="#000000">&nbsp;</font><font color="#000000">Example</font><font color="#000000">()</font><br>
 <font color="#000000">&nbsp;&nbsp;</font><font color="#000000">{</font><br>
 <font color="#000000">&nbsp;&nbsp;&nbsp;&nbsp;LegoRobot&nbsp;robot&nbsp;</font><font color="#c00000">=</font><font color="#000000">&nbsp;</font><font color="#0000ff">new</font><font color="#000000">&nbsp;</font><font color="#000000">LegoRobot</font><font color="#000000">()</font><font color="#000000">;</font><br>
 <font color="#000000">&nbsp;&nbsp;&nbsp;&nbsp;Gear&nbsp;gear&nbsp;</font><font color="#c00000">=</font><font color="#000000">&nbsp;</font><font color="#0000ff">new</font><font color="#000000">&nbsp;</font><font color="#000000">Gear</font><font color="#000000">()</font><font color="#000000">;</font><br>
 <font color="#000000">&nbsp;&nbsp;&nbsp;&nbsp;robot.</font><font color="#000000">addPart</font><font color="#000000">(</font><font color="#000000">gear</font><font color="#000000">)</font><font color="#000000">;</font><br>
 <font color="#000000">&nbsp;&nbsp;&nbsp;&nbsp;gear.</font><font color="#000000">forward</font><font color="#000000">(</font><font color="#000000">5000</font><font color="#000000">)</font><font color="#000000">;</font><br>
 <font color="#000000">&nbsp;&nbsp;&nbsp;&nbsp;robot.</font><font color="#000000">exit</font><font color="#000000">()</font><font color="#000000">;</font><br>
 <font color="#000000">&nbsp;&nbsp;</font><font color="#000000">}</font><br>
 <font color="#000000"></font><br>
 <font color="#000000">&nbsp;&nbsp;</font><font color="#0000ff">public</font><font color="#000000">&nbsp;</font><font color="#0000ff">static</font><font color="#000000">&nbsp;</font><font color="#0000ff">void</font><font color="#000000">&nbsp;</font><font color="#000000">main</font><font color="#000000">(</font><font color="#00008b">String</font><font color="#000000">[]</font><font color="#000000">&nbsp;args</font><font color="#000000">)</font><br>
 <font color="#000000">&nbsp;&nbsp;</font><font color="#000000">{</font><br>
 <font color="#000000">&nbsp;&nbsp;&nbsp;&nbsp;</font><font color="#0000ff">new</font><font color="#000000">&nbsp;</font><font color="#000000">Example</font><font color="#000000">()</font><font color="#000000">;</font><br>
 <font color="#000000">&nbsp;&nbsp;</font><font color="#000000">}</font><br>
 <font color="#000000">}</font><br>
 * </code><br><br>
 * In principle you may remove the static header and use the program unmodified
 * for the real NXT robot using the NxtJLib or NxtJLibA library (see www.aplu.ch/nxt).<br><br>
 * Because LegoRobot extends Actor all public methods of Actor are exposed. Some
 * of them are overridden. All sprite images are loaded from subdirectory "sprites"
 * of the application folder or, if not found there, from _sprites of the distribution JAR.<br><br>
 *
 * For backward compatibility with pre-EV3 programs.
 */
public class NxtRobot extends LegoRobot
{
}
