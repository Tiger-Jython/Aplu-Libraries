@echo off
  rd /s /q e:\ev3jlibdoc
  d:\jdk1.7.0\bin\javadoc -public -sourcepath e:\dropbox\mynb_ev3\EV3JLib\src -classpath c:\jars\aplu5.jar ch.aplu.ev3 -d e:\ev3jlibdoc 1>nul 2>t.txt
rem  d:\jdk1.7.0_21\bin\javadoc -public -sourcepath e:\dropbox\mynb_ev3\EV3JLib\src -classpath c:\jars\aplu5.jar ch.aplu.ev3 -d e:\ev3jlibdoc 1>nul 2>t.txt

  rd /s /q d:\myweb\classdoc\ev3jlib
  xcopy /s e:\ev3jlibdoc d:\myweb\classdoc\ev3jlib\*.* >nul

