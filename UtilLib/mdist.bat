@echo off
 echo. Readme.txt updated????
 set now=%time%
 pause

 date
 time

echo Create new JavaDoc
  call mdoc
  cd \mynb\utillib

echo Remove old apludist
 rd /q /s apludist

echo Make new apludist
  md apludist

echo Copy Manifest
   md apludist\META-INF
   copy META-INF\*.*  apludist\META-INF\*.*  >nul

echo Create jar
  xcopy e:\mynb\utillib\build\classes\ch\aplu\util\*.class apludist\ch\aplu\util\* /s >nul
  xcopy e:\mynb\utillib\src\ch\aplu\util\properties apludist\ch\aplu\util\properties\* /s >nul
  xcopy e:\mynb\apluturtle\build\classes\ch\aplu\turtle\*.class apludist\ch\aplu\turtle\* /s >nul
  xcopy e:\mynb\apluturtle\src\ch\aplu\turtle\properties apludist\ch\aplu\turtle\properties\* /s >nul


  cd apludist
  dirdate -R -ALL time=$$:$$:$$ date=$$:$$:$$$$ *.* >nul
  zip -r aplu5.jar *.* >nul
  copy aplu5.jar c:\jars

echo Append readme
  cd ..\readme
  dirdate -R -ALL time=$$:$$:$$ date=$$:$$:$$$$ *.* >nul
  copy readme.txt ..\apludist
  cd ..

echo Create zip
  cd apludist
  zip aplujar5.zip aplu5.jar readme.txt >nul
  copy aplujar5.zip d:\myweb\aplu-home\download >nul

  zip -r aplu5.zip ch readme.txt
  copy aplu5.zip d:\myweb\aplu-home\download >nul

echo.
echo Library created

time %now%
rem echo Zeit zurueckgestellt auf 
rem time /t


:quit