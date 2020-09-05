@echo on
 echo. Readme.txt updated????
 pause

 date
 time

echo create new docs
  call mdoc

echo Remove old dist
 rd /q /s apludist

echo Make new dist
  md apludist

echo Copy jar
  xcopy dist\JGameGrid.jar apludist\lib\*.* >nul
  copy /y dist\JGameGrid.jar c:\jars >nul

echo Copy doc
  xcopy /s e:\jgamegriddoc apludist\doc\*.*  >nul

echo Copy readme
  copy readme\readme.txt apludist\*.*  >nul

echo Copy spritepictures
  xcopy /s e:\spritepictures apludist\spritepictures\*.*  >nul

echo Copy tcptut
  xcopy /s e:\mynb\tcpjlibex\src apludist\tcp_tutorial\*.*  >nul

echo Copy tutorial
  xcopy /s e:\mynb\gamegridex\src apludist\tutorial\*.*  >nul
  xcopy /s e:\mynb\cardgameex\src apludist\cardgame_tutorial\*.*  >nul
  xcopy /s /y e:\mynb\cardgameexsecond\src apludist\cardgame_tutorial\*.*  >nul
  xcopy /s /y e:\mynb\schwarzpeterproblem1\src apludist\tutorial\schwarzpeter_problem1\*.*  >nul

echo Copy applications
  xcopy /s e:\mynb\ggtcpsixtysix\src apludist\applications\tcpsixtysix\*.* >nul
  xcopy /s e:\mynb\ggtcpsixtysix\dist\*.* apludist\applications\tcpsixtysix\dist\*.* >nul

  xcopy /s e:\mynb\ggtcpsixtysixext\src apludist\applications\tcpsixtysixext\*.* >nul
  xcopy /s e:\mynb\ggtcpsixtysixext\dist\*.* apludist\applications\tcpsixtysixext\dist\*.* >nul

  xcopy /s e:\mynb\ggtcpmaumau\src apludist\applications\tcpmaumau\*.* >nul
  xcopy /s e:\mynb\ggtcpmaumau\dist\*.* apludist\applications\tcpmaumau\dist\*.* >nul

  xcopy /s e:\mynb\cgmaumau\src apludist\applications\maumau\*.*  >nul
  xcopy /s e:\mynb\cgmaumau\dist\*.* apludist\applications\maumau\dist\*.* >nul

  xcopy /s e:\mynb\ggsnake\src apludist\applications\snake\*.*  >nul
  xcopy /s e:\mynb\ggsnake\dist\*.* apludist\applications\snake\dist\*.* >nul

  xcopy /s e:\mynb\ggtcpbattleship\src apludist\applications\tcpbattleship\*.*  >nul
  xcopy /s e:\mynb\ggtcpbattleship\dist\*.* apludist\applications\tcpbattleship\dist\*.* >nul

  xcopy /s e:\mynb\ggtcpmemory\src apludist\applications\tcpmemory\*.*  >nul
  xcopy /s e:\mynb\ggtcpmemory\dist\*.* apludist\applications\tcpmemory\dist\*.* >nul

  xcopy /s e:\mynb\ggtcpmemoryserver\src apludist\applications\tcpmemoryserver\*.*  >nul
  xcopy /s e:\mynb\ggtcpmemoryserver\dist\*.* apludist\applications\tcpmemoryserver\dist\*.* >nul

  xcopy /s e:\mynb\ggtcpnim\src apludist\applications\tcpnim\*.*  >nul
  xcopy /s e:\mynb\ggtcpnim\dist\*.* apludist\applications\tcpnim\dist\*.* >nul

  xcopy /s e:\mynb\ggtcpnimserver\src apludist\applications\tcpnimserver\*.*  >nul
  xcopy /s e:\mynb\ggtcpnimserver\dist\*.* apludist\applications\tcpnimserver\dist\*.* >nul

  xcopy /s e:\mynb\ggskiresort\src apludist\applications\skiresort\*.*  >nul
  xcopy /s e:\mynb\ggskiresort\dist\*.* apludist\applications\skiresort\dist\*.* >nul

  xcopy /s e:\mynb\gglightsout\src apludist\applications\lightsout\*.*  >nul
  xcopy /s e:\mynb\gglightsout\dist\*.* apludist\applications\lightsout\dist\*.* >nul

  xcopy /s e:\mynb\ggbattleship\src apludist\applications\battleship\*.*  >nul
  xcopy /s e:\mynb\ggbattleship\dist\*.* apludist\applications\battleship\dist\*.* >nul

  xcopy /s e:\mynb\ggbrownian\src apludist\applications\brownian\*.*  >nul
  xcopy /s e:\mynb\ggbrownian\dist\*.* apludist\applications\brownian\dist\*.* >nul

  xcopy /s e:\mynb\ggcatgame\src apludist\applications\catgame\*.*  >nul
  xcopy /s e:\mynb\ggcatgame\dist\*.* apludist\applications\catgame\dist\*.* >nul

  xcopy /s e:\mynb\ggclock\src apludist\applications\clock\*.*  >nul

  xcopy /s e:\mynb\ggdartgame\src apludist\applications\dartgame\*.*  >nul
  xcopy /s e:\mynb\ggdartgame\dist\*.* apludist\applications\dartgame\dist\*.* >nul

  xcopy /s e:\mynb\ggfifteenpuzzle\src apludist\applications\fifteenpuzzle\*.*  >nul
  xcopy /s e:\mynb\ggfifteenpuzzle\dist\*.* apludist\applications\fifteenpuzzle\dist\*.* >nul

  xcopy /s e:\mynb\ggflyingbird\src apludist\applications\flyingbird\*.*  >nul
  xcopy /s e:\mynb\ggflyingbird\dist\*.* apludist\applications\flyingbird\dist\*.* >nul

  xcopy /s e:\mynb\ggfrogger\src apludist\applications\frogger\*.*  >nul
  xcopy /s e:\mynb\ggfrogger\dist\*.* apludist\applications\frogger\dist\*.* >nul

  xcopy /s e:\mynb\gggameoflife\src apludist\applications\gameoflife\*.*  >nul
  xcopy /s e:\mynb\gggameoflife\dist\*.* apludist\applications\gameoflife\dist\*.* >nul

  xcopy /s e:\mynb\gglangton\src apludist\applications\langton\*.*  >nul
  xcopy /s e:\mynb\gglangton\dist\*.* apludist\applications\langton\dist\*.* >nul

  xcopy /s e:\mynb\gglunarlander\src apludist\applications\lunarlander\*.*  >nul
  xcopy /s e:\mynb\gglunarlander\dist\*.* apludist\applications\lunarlander\dist\*.* >nul

  xcopy /s e:\mynb\ggmaze\src apludist\applications\maze\*.*  >nul
  xcopy /s e:\mynb\ggmaze\dist\*.* apludist\applications\maze\dist\*.* >nul

  xcopy /s e:\mynb\ggpacman\src apludist\applications\pacman\*.*  >nul
  xcopy /s e:\mynb\ggpacman\dist\*.* apludist\applications\pacman\dist\*.* >nul

  xcopy /s e:\mynb\ggpiano\src apludist\applications\piano\*.*  >nul
  xcopy /s e:\mynb\ggpiano\dist\*.* apludist\applications\piano\dist\*.* >nul

  xcopy /s e:\mynb\ggpong\src apludist\applications\pong\*.*  >nul
  xcopy /s e:\mynb\ggpong\dist\*.* apludist\applications\pong\dist\*.* >nul

  xcopy /s e:\mynb\ggsokoban\src apludist\applications\sokoban\*.*  >nul
  xcopy /s e:\mynb\ggsokoban\dist\*.* apludist\applications\sokoban\dist\*.* >nul

  xcopy /s e:\mynb\ggspaceinvader\src apludist\applications\spaceinvader\*.*  >nul
  xcopy /s e:\mynb\ggspaceinvader\dist\*.* apludist\applications\spaceinvader\dist\*.* >nul

  xcopy /s e:\mynb\ggtetris\src apludist\applications\tetris\*.*  >nul
  xcopy /s e:\mynb\ggtetris\dist\*.* apludist\applications\tetris\dist\*.* >nul

  xcopy /s e:\mynb\ggthreebody\src apludist\applications\threebody\*.*  >nul
  xcopy /s e:\mynb\ggthreebody\dist\*.* apludist\applications\threebody\dist\*.* >nul

  xcopy /s e:\mynb\ggturtle\src apludist\applications\turtle\*.*  >nul
  xcopy /s e:\mynb\ggturtle\dist\*.* apludist\applications\turtle\dist\*.* >nul

  xcopy /s e:\mynb\ggmemory\src apludist\applications\memory\*.* >nul
  xcopy /s e:\mynb\ggmemory\dist\*.* apludist\applications\memory\dist\*.* >nul


  xcopy /s e:\mynb\ggkinect\src apludist\applications\ggkinect\src\*.*  >nul
  xcopy /s e:\mynb\ggkinect\dist\javadoc apludist\applications\ggkinect\javadoc\*.* >nul

  xcopy /s e:\mynb\ggframetutorial\src apludist\applications\ggframetutorial\src\*.*  >nul
  xcopy /s e:\mynb\ggframetutorial\dist\*.* apludist\applications\ggframetutorial\dist\*.* >nul

  xcopy /s e:\mynb\ggframetutorialstat\src apludist\applications\ggframetutorialstat\src\*.*  >nul
  xcopy /s e:\mynb\ggframetutorialstat\dist\*.* apludist\applications\ggframetutorialstat\dist\*.* >nul

  xcopy /s e:\mynb\btpeerex1\src apludist\applications\BtPeerEx1\src\*.*  >nul
  xcopy /s e:\mynb\btpeerex1\dist\*.* apludist\applications\BtPeerEx1\dist\*.* >nul

  xcopy /s e:\mynb\btserverse\src apludist\applications\BtServerSE\src\*.*  >nul
  xcopy /s e:\mynb\btserverse\dist\*.* apludist\applications\BtServerSE\dist\*.* >nul


rem  xcopy /s e:\mynb\btclientse\src apludist\applications\BtClientSE\src\*.*  >nul
rem  xcopy /s e:\mynb\btclientse\dist\*.* apludist\applications\BtClientSE\dist\*.* >nul

rem  xcopy /s e:\mynb\remotephotoclient\src apludist\applications\RemotePhotoClient\src\*.*  >nul
rem  xcopy /s e:\mynb\remotephotoclient\dist\*.* apludist\applications\RemotePhotoClient\dist\*.* >nul

echo Copy Python
  xcopy /s e:\dropbox\mytigerjython\CardGameEx apludist\python\CardGameEx\*.*  >nul
  xcopy /s e:\dropbox\mytigerjython\GameGridEx apludist\python\GameGridEx\*.*  >nul
  xcopy /s e:\dropbox\mytigerjython\GPanelEx apludist\python\GPanelEx\*.*  >nul
  xcopy /s e:\dropbox\mytigerjython\RobotSimEx apludist\python\RobotSimEx\*.*  >nul
  xcopy /s e:\dropbox\mytigerjython\TcpEx apludist\python\TcpEx\*.*  >nul
  xcopy /s e:\dropbox\mytigerjython\TurtleEx apludist\python\TurtleEx\*.*  >nul
  xcopy /s e:\dropbox\mytigerjython\SoundEx apludist\python\SoundEx\*.*  >nul
  xcopy /s e:\dropbox\mytigerjython\SoundEx apludist\python\ImageEx\*.*  >nul
  xcopy /s e:\dropbox\mytigerjython\MiscEx apludist\python\MiscEx\*.*  >nul

echo Copy RobotSim
  xcopy /s e:\dropbox\mynb_ev3\robotsim\readme\readme.txt apludist\robotsim\readme\*.* >nul
  xcopy /s e:\dropbox\mynb_ev3\robotsim\src apludist\robotsim\src\*.*  >nul
  xcopy /s e:\dropbox\mynb_ev3\robotsim\dist\RobotSim.jar apludist\robotsim\lib\*.* >nul
  xcopy /s e:\\dropbox\mynb_ev3\robotsimex\src apludist\robotsim\examples\*.*  >nul
  xcopy /s e:\RobotSimDoc\*.* apludist\robotsim\javadoc\*.*  >nul

echo Copy RaspiSim
  xcopy /s e:\dropbox\mynb_ev3\raspisim\readme\readme.txt apludist\raspisim\readme\*.* >nul
  xcopy /s e:\dropbox\mynb_ev3\raspisim\src apludist\raspisim\src\*.* >nul
  xcopy /s e:\dropbox\mynb_ev3\raspisim\dist\RaspiSim.jar apludist\raspisim\lib\*.* >nul
  xcopy /s e:\\dropbox\mynb_ev3\raspisimex\src apludist\raspisim\examples\*.* >nul
  xcopy /s e:\RaspiSimDoc\*.* apludist\raspisim\javadoc\*.* >nul


:quit