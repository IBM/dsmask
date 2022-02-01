#! /bin/sh

JAVA=/opt/IBM/InformationServer/jdk/bin/java
if [ ! -f $JAVA ]; then
  JAVA=java
fi

$JAVA -Xms256m -Xmx2048m -jar lib/dsmask-jconf-* $@
