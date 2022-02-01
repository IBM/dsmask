#! /bin/sh

java -Xms2048m -Xmx8192m -classpath 'lib/*' com.ibm.dsmask.uniq.UniqService uniq-service-config.xml
