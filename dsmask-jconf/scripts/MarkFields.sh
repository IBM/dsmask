#! /bin/sh

JAVA=/opt/IBM/InformationServer/jdk/bin/java
if [ ! -f $JAVA ]; then
  JAVA=java
fi

$JAVA -classpath 'lib/*' groovy.ui.GroovyMain MarkFields.groovy $@

# End Of File
