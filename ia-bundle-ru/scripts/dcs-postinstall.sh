#! /bin/sh

. ./dcs-config.sh

"$IISROOT"/"$ISTOOL" glossary import -dom "$IISDOM" -u "$IAUSER" -p "$IAPASS" \
  -filename `pwd`/extras/categories.xml -mergemethod ignore
