@echo off
  rd /s /q e:\RobotSimDoc
  d:\jdk1.7.0\bin\javadoc -public -sourcepath e:\dropbox\mynb_ev3\RobotSim\src ch.aplu.robotsim -d e:\RobotSimDoc


  rd /s /q d:\myweb\classdoc\robotsim
  xcopy /s e:\RobotSimDoc d:\myweb\classdoc\robotsim\*.* >nul
