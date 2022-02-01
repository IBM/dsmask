#! /bin/sh

(cd ../dsmask-jconf/target && rm -rf dsmask-jconf && unzip *.zip)

java -Xms2048m -Xmx8192m -classpath '../dsmask-jconf/target/dsmask-jconf/lib/*' \
  com.ibm.dsmask.jconf.BuildDict \
  prepare-dict-ru.xml

