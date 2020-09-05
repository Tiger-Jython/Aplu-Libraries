@echo off
  rem Create zip
  time

  cd apludist
  zip -r JGameGrid.zip *.* >nul		
  copy JGameGrid.zip d:\myweb\aplu-home\download >nul
  copy lib\JGameGrid.jar c:\jars
  cd ..

  echo All done.

