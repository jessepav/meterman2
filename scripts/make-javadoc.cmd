@ECHO OFF
SETLOCAL ENABLEEXTENSIONS ENABLEDELAYEDEXPANSION

cd %~dp0..

SET output_dir=build\javadoc

IF "%1" NEQ "" (
    SET visibility=%1
    SET output_dir="!output_dir!-!visibility!"
    IF "!visibility!" EQU "private" SET linksrc=-linksource
) ELSE (
    SET visibility=protected
)

rmdir /S /Q %output_dir%
mkdir %output_dir%

javadoc -classpath lib\*;build\production\meterman2 -%visibility% %linksrc% ^
   -sourcepath src -subpackages com.illcode.meterman2 -d "%output_dir%" ^
   -windowtitle "Meterman2 Javadocs" -doctitle "Meterman2 Javadocs" ^
   -linkoffline https://docs.oracle.com/javase/7/docs/api/ scripts\package-info\java7 ^
   -linkoffline https://jessepav.github.io/java-api-docs/bsh-2.1.9-JP-javadoc/ scripts\package-info\bsh ^
   -linkoffline http://static.javadoc.io/org.jdom/jdom2/2.0.6/ scripts\package-info\jdom2 ^
   -linkoffline http://static.javadoc.io/org.freemarker/freemarker/2.3.28/ scripts\package-info\freemarker

:END
ENDLOCAL
ECHO ON
@EXIT /B 0
