echo. Readme.txt updated????
  set now=%time%
  pause


@echo on
 date
 time

echo create new docs
  call mdoc

echo Remove old dist
  rd /q /s e:\dropbox\mynb_ev3\raspijlib\apludist

echo Make new dist
  md apludist

echo Copy jar
  xcopy e:\dropbox\mynb_ev3\raspijlib\dist\raspijlib.jar e:\dropbox\mynb_ev3\raspijlib\apludist\lib\*.* >nul
  copy /y e:\dropbox\mynb_ev3\raspijlib\dist\raspijlib.jar c:\jars >nul

echo Copy doc
  xcopy /s e:\raspijlibdoc e:\dropbox\mynb_ev3\raspijlib\apludist\doc\*.*  >nul

echo Copy readme
  copy e:\dropbox\mynb_ev3\raspijlib\readme\readme.txt e:\dropbox\mynb_ev3\raspijlib\apludist\*.*  >nul


echo Copy examples
  xcopy e:\dropbox\mynb_ev3\raspijlibdev\src e:\dropbox\mynb_ev3\raspijlib\apludist\examples\*.*  >nul


echo Copy sources
  xcopy /s e:\dropbox\mynb_ev3\raspijlib\src\ch\aplu\raspi\*.* e:\dropbox\mynb_ev3\raspijlib\apludist\src\ch\aplu\raspi\*.* >nul
  xcopy /s e:\dropbox\mynb_ev3\raspijlib\src\ch\aplu\util\*.* e:\dropbox\mynb_ev3\raspijlib\apludist\src\ch\aplu\util\*.* >nul

echo Copy RaspiTransfer
  xcopy /s e:\dropbox\mynb_ev3\raspitransfer\dist\RaspiTransfer.jar e:\dropbox\mynb_ev3\raspijlib\apludist\raspitransfer\*.* >nul
  xcopy /s e:\dropbox\mynb_ev3\raspitransfer\src\*.* e:\dropbox\mynb_ev3\raspijlib\apludist\raspitransfer\src\*.* >nul


