@ECHO OFF
SETLOCAL ENABLEEXTENSIONS ENABLEDELAYEDEXPANSION

cd %~dp0..

rem name="Installer (Dynamic JRE)" id="44"
rem name="ZIP (Bundled JRE)" id="92"
rem name="ZIP (no JRE)" id="127"

"C:\Program Files (x86)\install4j4\bin\install4jc.exe" -b 44,127 meterman2.install4j

:END
ENDLOCAL
ECHO ON
@EXIT /B 0
