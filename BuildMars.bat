@echo off

cd out\production\Mars
call CreateMarsJar.bat
move Mars.jar ..\..\Mars.jar