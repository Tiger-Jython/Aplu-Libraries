@echo off
  rd /s /q e:\apludoc
  d:\jdk1.7.0\bin\javadoc -public -sourcepath e:\mynb\UtilLib\src -classpath c:\jars\tritonus_share.jar  ch.aplu.util -d e:\apludoc\util 1>nul 2>t.txt
  d:\jdk1.7.0\bin\javadoc -public -sourcepath e:\mynb\ApluTurtle\src ch.aplu.turtle -d e:\apludoc\turtle 1>nul 2>t.txt

  rd /s /q d:\myweb\classdoc\util
  xcopy /s e:\apludoc\util   d:\myweb\classdoc\util\*.* >nul

  rd /s /q d:\myweb\classdoc\turtle
  xcopy /s e:\apludoc\turtle d:\myweb\classdoc\turtle\*.* >nul

  cd \apludoc
  if exist apludoc.zip del apludoc.zip >nul
  zip -r apludoc.zip *.* >nul
  copy apludoc.zip d:\myweb\aplu-home\download >nul


