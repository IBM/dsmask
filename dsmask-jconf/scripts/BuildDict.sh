#! /bin/sh

JAVA=/opt/IBM/InformationServer/jdk/bin/java
if [ ! -f $JAVA ]; then
  JAVA=java
fi

$JAVA -Xms256m -Xmx8192m -classpath 'lib/*' com.ibm.dsmask.jconf.BuildDict $@
