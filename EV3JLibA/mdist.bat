echo. Readme.txt updated????
  set now=%time%
  pause


@echo on
 date
 time

echo create new docs
  call mdoc

echo Remove old dist
  rd /q /s e:\dropbox\mynb_ev3\ev3jliba\apludist

echo Make new dist
  md apludist

echo Copy jar
  xcopy e:\dropbox\mynb_ev3\ev3jliba\dist\EV3JLibA.jar e:\dropbox\mynb_ev3\ev3jliba\apludist\lib\*.* >nul
  copy /y e:\dropbox\mynb_ev3\ev3jliba\dist\EV3JLibA.jar c:\jars >nul

echo Copy doc
  xcopy /s e:\ev3jlibadoc e:\dropbox\mynb_ev3\ev3jliba\apludist\doc\*.*  >nul

echo Copy readme
  copy e:\dropbox\mynb_ev3\ev3jliba\readme\readme.txt e:\dropbox\mynb_ev3\ev3jliba\apludist\*.*  >nul


echo Copy examples
  xcopy e:\dropbox\mynb_ev3\ev3jlibaex\src e:\dropbox\mynb_ev3\ev3jliba\apludist\examples\*.*  >nul


echo Copy sources
  xcopy /s e:\dropbox\mynb_ev3\ev3jliba\src\ch\aplu\*.* e:\dropbox\mynb_ev3\ev3jliba\apludist\src\ch\aplu\*.* >nul

echo Copy BrickTransfer
  xcopy /s e:\dropbox\mynb_ev3\bricktransfer\src e:\dropbox\mynb_ev3\ev3jliba\apludist\bricktransfer\src\*.* >nul
  del  e:\dropbox\mynb_ev3\bricktransfer\dist\readme.txt >nul
  xcopy /s e:\dropbox\mynb_ev3\bricktransfer\dist\*.* e:\dropbox\mynb_ev3\ev3jliba\apludist\bricktransfer\dist\*.* >nul

