EV3JLibA Autonomous Mode Java Library for Lego EV3 - Readme
Version 1.13, May 27, 2015
==================================================================

See the websites http://www.aplu.ch/ev3 for most recent information.


History:
-------
V1.00 - Jul 2014: - First public release
V1.01 - Jul 2014: - Tools.wait<button> now triggered by press (and not press&release)
V1.02 - Jul 2014: - Added: ColorSensor.getColorLabel() and ColorCubes
V1.03 - Jul 2014: - Modified: ColorLabel enum in package root
V1.04 - Aug 2014: - Fixed: GenericMotor.stop() now sets STOP flag
                  - Fixed: Tools.startButtonListener() now public
V1.05 - Aug 2014  - Added: ButtonListener to support the event model
V1.06 - Aug 2014  - Added: RemoteListener to support IRRemoteSensor events
V1.07 - Aug 2014  - Added: ColorSensor.getColorStr()
                  - Modified: UltrasonicSensor, NxtUltrasonicSensor now returns 255 
                    if no target is detected
V1.08 - Jan 2015  - Modified: ColorSensor color cube values from properties now
V1.09 - Feb 2015  - Removed: class RFIDSensor because not supported by leJOS EV3
                  - Added: GenericGear.getLeftMotorCount(), getRightMotorCount(),
                    resetLeftMotorCount(), resetRightMotorCount()
V1.10 - Feb 2015  - Added: class HTEopdSensor, class HTEopdShortSensor
V1.11 - May 2015  - Added: class ArduinoLink
V1.12 - May 2015  - Added: class TemperatureSensor
V1.13 - May 2015  - Added: class I2CExpander


 

Installation:
------------
   1. Unpack the EV3JLibA.zip in any folder

   2. Copy the library jar EV3JLibA.jar in subfolder 'lib' into your 
      favorite folder for jar files, e.g. c:\jars

   3. Download/install leJOS EV3 distribution from website found with search machine.
      Extract ev3classes.jar

   4. Create a project with your favorite IDE and add EV3JLib.jar and ev3classes.jar
      to the project library jars. Keep in mind that you may use all
      classes from leJOS and EV3JLibA in the same application

   5. Compile some of the examples in the subfolder 'examples'.
      Try to understand the code. 

   6. Create an application jar archive with all classes from the ch.aplu.ev3 package, but
      there is no need to include classes from ev3classes.jar (because it
      is already in the classpath of the EV3)

   7. Upload the application to the EV3 (for example with SCP)

   8. Start the application


Downloading applications to the EV3:
------------------------------------
For downloading the program jar to the EV3 you may use SCP 
(or the GUI based WinSCP). 

We wrote a small EV3 downloader that is included
in this distribution. It takes command line parameters to download the jar. 

You may include it as external program in your favorite IDE. 
The usage is as follows:

Ev3Download projectRoot projectName mainClass [ipAddress]

e.g

java -jar Ev3Download.jar e:\myprogs TestProject Test 192.168.1.102

If ipAddress parameter is omitted, an input box is displayed where 
you can enter the IP address. 

For Netbeans you can include some lines in the project's build.xml ant script 
to automatize the download when the Clean and Build option is clicked.


        <target name="-post-jar">
            <exec dir="bin" executable="java">
                <arg value="-jar"/>
                <arg value="Ev3Download.jar"/>
                <arg value="${basedir}"/>
                <arg value="${ant.project.name}"/>
                <arg value="${main.class}"/>
                <arg value="192.168.1.102"/>
            </exec>
        </target>

(EV3Download.jar must reside in the bin subdirectory of the project root.)
If you use the leJOS addon for Eclipse, you must copy the source files of the
ch.aplu.ev3 package into your project.


For any help or suggestions send an e-mail to support@aplu.ch or post an article
to the forum at www.aplu.ch/forum.
