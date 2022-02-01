#! /bin/sh

JAVA_HOME=/usr/lib/jvm/java-1.8.0-openjdk-amd64
export JAVA_HOME

MVN=/opt/netbeans/v12.0/netbeans/java/maven/bin/mvn

"$MVN" package -P full-build-profile 

