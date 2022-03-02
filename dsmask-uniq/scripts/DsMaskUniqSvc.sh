#! /bin/sh

java -Xms2048m -Xmx8192m -classpath 'lib/*' net.dsmask.uniq.UniqService uniq-service-config.xml
