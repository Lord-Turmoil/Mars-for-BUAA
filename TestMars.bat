@ECHO OFF

ECHO Entering 'test' directory...
CD test

ECHO Running Mars...
CALL java -jar ../out/Mars.jar nc mc Default me we ae1 se2 mips.txt < input.txt > output.txt

IF "%1"=="-c" IF EXIST answer.txt (
    ECHO Comparing output.txt and answer.txt...
    FC output.txt answer.txt
) ELSE (
    ECHO No answer.txt found, skipping comparison...
)

ECHO Exiting 'test' directory...
CD ..

ECHO Test complete.
