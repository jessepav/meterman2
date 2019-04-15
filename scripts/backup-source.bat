@echo off
pushd %~dp0..
call scripts\package-source.bat
copy /Y meterman2-src.7z ^
  "C:\Users\JP\AppData\Local\Box\Box Edit\Documents\Z2Zp7egf2kCRVi0VPvyRXQ==\meterman2-src.7z"
popd
