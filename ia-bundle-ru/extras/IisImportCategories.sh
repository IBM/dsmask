#! /bin/sh

. ./IisOptions.sh

$ISTOOL glossary import -dom localhost:19443 -u "$XMETAUSER" -p "$XMETAPASS" \
  -filename `pwd`/categories.xml -mergemethod ignore
