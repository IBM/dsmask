#! /bin/sh

JAVA_HOME=/Library/Java/JavaVirtualMachines/jdk1.8.0_261.jdk/Contents/Home
export JAVA_HOME

MVN="/Applications/Apache NetBeans 12.0.app/Contents/Resources/NetBeans/netbeans/java/maven/bin/mvn"

"$MVN" clean
"$MVN" package -P full-build-profile 

