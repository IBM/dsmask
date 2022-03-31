@echo off

set JAVA=C:\IBM\InformationServer\jdk\bin\java.exe

%JAVA% -classpath lib/"*" groovy.ui.GroovyMain MarkFields.groovy

rem End Of File
