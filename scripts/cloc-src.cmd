@ECHO OFF
SETLOCAL ENABLEEXTENSIONS ENABLEDELAYEDEXPANSION

cd %~dp0..
cloc --force-lang=java,bsh assets assets src

:END
ENDLOCAL
ECHO ON
@EXIT /B 0
