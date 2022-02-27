#! /bin/sh

set -e
set +u

if [ -z "$1" ] || [ ! -d "$1"  ]; then
  echo "USAGE: $0 SRCDIR"
  exit 1
fi

SRCDIR="$1"

if [ -z "$JAVA_HOME" ]; then
  JAVA_HOME=/usr/lib/jvm/java-8-openjdk-amd64
  export JAVA_HOME
fi

mvn install:install-file -Dfile="$SRCDIR"/dataquality.jar \
 -DgroupId=com.ibm.iis -DartifactId=dataquality -Dversion=11.7.1.1 -Dpackaging=jar
mvn install:install-file -Dfile="$SRCDIR"/dataqualityjavadoc.zip \
 -DgroupId=com.ibm.iis -DartifactId=dataquality -Dversion=11.7.1.1 -Dpackaging=jar -Dclassifier=javadoc
