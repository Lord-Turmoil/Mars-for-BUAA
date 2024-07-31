@ECHO OFF

ECHO Entering 'test' directory...
CD test

ECHO Running Mars...
CALL java -jar ../out/Mars.jar nc mc Default me we ae1 se2 mips.txt < input.txt > output.txt
