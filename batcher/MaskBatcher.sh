#! /bin/sh

BASELIB=/opt/IBM/Masker/dsmask-jconf/lib
JAVA=/opt/IBM/InformationServer/jdk/bin/java

$JAVA -classpath $BASELIB/'*' groovy.ui.GroovyMain MaskBatcher.groovy

# End Of File
