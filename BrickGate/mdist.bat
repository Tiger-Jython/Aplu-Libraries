@echo off

  set now=%time%


@echo off
 date
 time

echo Remove old dist
  rd /q /s e:\dropbox\mynb_ev3\brickgate\apludist

echo Make new dist
  md apludist


echo Copy jar
  xcopy e:\dropbox\mynb_ev3\brickgate\dist\BrickGate.jar e:\dropbox\mynb_ev3\brickgate\apludist\*.* >nul

