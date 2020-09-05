@echo off
  rd /s /q e:\ev3jlibadoc
  d:\jdk1.7.0\bin\javadoc -public -sourcepath e:\dropbox\mynb_ev3\EV3JLibA\src ch.aplu.ev3 -d e:\ev3jlibadoc 1>nul 2>t.txt
rem  d:\jdk1.7.0_21\bin\javadoc -public -sourcepath e:\dropbox\mynb_ev3\EV3JLibA\src ch.aplu.ev3 -d e:\ev3jlibadoc 1>nul 2>t.txt


  rd /s /q d:\myweb\classdoc\ev3jliba
  xcopy /s e:\ev3jlibadoc d:\myweb\classdoc\ev3jliba\*.* >nul

