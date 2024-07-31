@ECHO OFF

ECHO Entering class directory...
CD out\production\Mars

ECHO Building Mars...
CALL CreateMarsJar.bat

ECHO Copying Mars.jar out...
MOVE Mars.jar ..\..\Mars.jar
ECHO Build complete
