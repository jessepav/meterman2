@echo off
pushd %~dp0..
call scripts\package-source.bat
copy /Y meterman2-src.7z W:\Backup\Code\
popd
