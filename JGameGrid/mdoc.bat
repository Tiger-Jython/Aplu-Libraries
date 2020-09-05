@echo off
  rd /s /q e:\JGameGridDoc
  d:\jdk1.7.0\bin\javadoc -public -sourcepath e:\mynb\JGameGrid\src -classpath c:\jars\tritonus_share.jar;e:\mynb\kinectjlib\dist\KinectJLib.jar  ch.aplu.jgamegrid ch.aplu.jcardgame ch.aplu.util -d e:\jgamegriddoc 1>nul 2>t.txt

  rd /s /q d:\myweb\classdoc\jgamegrid
  xcopy /s e:\jgamegriddoc d:\myweb\classdoc\jgamegrid\*.* >nul

