@echo off

C:\IBM\InformationServer\jdk\bin\java -Xms2048m -Xmx8192m -classpath "lib/*" org.h2.tools.RunScript %*
