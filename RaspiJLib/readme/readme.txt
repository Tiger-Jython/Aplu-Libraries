RaspiJLib Remote Mode Java Library for RaspiBrick - Readme
Version 1.02, Oct 19, 2015
==================================================================

See the websites http://www.raspibrick.com for most recent information.

For compilation, import RaspiJLib.jar as external library. 

You need the most recent update of the RaspiBrick firmware running 
on the brick. BrickGate must be started on the brick to run Java programs.

RaspiTransfer is a very useful utility to pack the Java classes into a
JAR archive and copy it to the RaspiBrick /home/pi/program folder via SSH. 
Optionally it is automatically started there by invoking jrun <jar-file> <main-class>.
The sources are provided to give up more information how it works.

History:
-------
V1.00 - Aug 2015: - Ported from Python remote library
V1.01 - Oct 2015: - Check if robot is created
V1.02 - Oct 2015: - Added: class ServoMotor


For any help or suggestions send an e-mail to support@aplu.ch or post an article
to the forum at www.aplu.ch/forum.
