@echo off

C:\IBM\InformationServer\jdk\bin\java -Xms2048m -Xmx8192m -classpath "lib/*" com.ibm.dsmask.jconf.BuildDict %*
