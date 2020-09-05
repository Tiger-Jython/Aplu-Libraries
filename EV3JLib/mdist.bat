echo. Readme.txt updated????
  set now=%time%
  pause


@echo on
 date
 time

echo create new docs
  call mdoc

echo Remove old dist
  rd /q /s e:\dropbox\mynb_ev3\ev3jlib\apludist

echo Make new dist
  md apludist

echo Copy jar
  xcopy e:\dropbox\mynb_ev3\ev3jlib\dist\ev3jlib.jar e:\dropbox\mynb_ev3\ev3jlib\apludist\lib\*.* >nul
  copy /y e:\dropbox\mynb_ev3\ev3jlib\dist\ev3jlib.jar c:\jars >nul

echo Copy doc
  xcopy /s e:\ev3jlibdoc e:\dropbox\mynb_ev3\ev3jlib\apludist\doc\*.*  >nul

echo Copy readme
  copy e:\dropbox\mynb_ev3\ev3jlib\readme\readme.txt e:\dropbox\mynb_ev3\ev3jlib\apludist\*.*  >nul


echo Copy examples
  xcopy e:\dropbox\mynb_ev3\ev3jlibex\src e:\dropbox\mynb_ev3\ev3jlib\apludist\examples\*.*  >nul


echo Copy sources
  xcopy /s e:\dropbox\mynb_ev3\ev3jlib\src\ch\aplu\ev3\*.* e:\dropbox\mynb_ev3\ev3jlib\apludist\src\ch\aplu\ev3\*.* >nul

echo Copy EV3DirectServer source and BrickGate jar
  xcopy /s e:\dropbox\mynb_ev3\brickgate\dist\BrickGate.jar e:\dropbox\mynb_ev3\ev3jlib\apludist\BrickGate\*.* >nul
  xcopy /s e:\dropbox\mynb_ev3\ev3directserver\src\*.* e:\dropbox\mynb_ev3\ev3jlib\apludist\BrickGate\EV3DirectServer\src\*.* >nul


echo Copy EV3PyLib
  cd \Dropbox\mytigerjython\EV3PyLib
  call  make.bat
  xcopy /s EV3PyLib.zip e:\dropbox\mynb_ev3\ev3jlib\apludist\EV3PyLib\*.* >nul
  cd \Dropbox\mynb_Ev3\EV3JLib
  xcopy /s e:\dropbox\mytigerjython\EV3Ex\*.* e:\dropbox\mynb_ev3\ev3jlib\apludist\EV3PyLib\examples\*.* >nul
