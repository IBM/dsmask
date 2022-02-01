#! /bin/sh

set -e
set +u

if [ -z "$JAVA_HOME" ]; then
  JAVA_HOME=/usr/lib/jvm/java-8-openjdk-amd64
  export JAVA_HOME
fi

for p in ia-custom-ru ia-bundle-ru dsmask-log4j dsmask-algo dsmask-beans dsmask-uniq dsmask-mock dsmask-jconf dsmask-jmask; do
  (cd $p && mvn clean)
  (cd $p && mvn install)
done

# End Of File
