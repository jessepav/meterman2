@ECHO OFF
SETLOCAL ENABLEEXTENSIONS ENABLEDELAYEDEXPANSION

cd %~dp0

"C:\Program Files\ViceVersa Pro\ViceVersa.exe" replicate-to-github-mirror.fsf /console /hiddenautoexec /autoclose

ENDLOCAL
ECHO ON
@EXIT /B 0
