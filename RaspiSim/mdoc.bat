@echo off
  rd /s /q e:\RaspiSimDoc
  d:\jdk1.7.0\bin\javadoc -public -sourcepath e:\dropbox\mynb_ev3\RaspiSim\src ch.aplu.raspisim -d e:\RaspiSimDoc


  rd /s /q d:\myweb\classdoc\raspisim
  xcopy /s e:\RaspiSimDoc d:\myweb\classdoc\raspisim\*.* >nul
