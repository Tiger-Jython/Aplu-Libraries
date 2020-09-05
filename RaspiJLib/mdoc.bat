@echo off
  rd /s /q e:\raspijlibdoc
  d:\jdk1.7.0\bin\javadoc -public -sourcepath e:\dropbox\mynb_ev3\RaspiJLib\src -classpath c:\jars\aplu5.jar ch.aplu.raspi -d e:\raspijlibdoc 1>nul 2>t.txt

  rd /s /q d:\myweb\classdoc\raspijlib
  xcopy /s e:\raspijlibdoc d:\myweb\classdoc\raspijlib\*.* >nul

