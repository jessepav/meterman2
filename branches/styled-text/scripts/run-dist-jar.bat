@echo off

java -Xmx256M -cp meterman2.jar;lib\*;jars\* -splash:assets/meterman2/splash-screen.png ^
    com.illcode.meterman2.Meterman2
