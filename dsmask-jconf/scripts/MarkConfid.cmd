@echo off

set JAVA=C:\IBM\InformationServer\jdk\bin\java.exe

%JAVA% -classpath lib/"*" groovy.ui.GroovyMain MarkConfid.groovy

rem End Of File
