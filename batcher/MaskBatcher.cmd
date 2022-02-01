@echo off

set BASELIB=C:\IBM\Masker\dsmask-jconf\lib
set JAVA=C:\IBM\InformationServer\jdk\bin\java.exe

%JAVA% -classpath %BASELIB%/"*" groovy.ui.GroovyMain MaskBatcher.groovy

rem End Of File
