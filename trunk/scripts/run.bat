@echo off
pushd %~dp0..
java -Xmx256M -cp build\production\meterman2;build\production\riverboat;lib\*;jars\* ^
    -splash:assets/meterman2/splash-screen.png ^
    com.illcode.meterman2.Meterman
popd
