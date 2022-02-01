#! /bin/sh

. ./IisOptions.sh

$ISTOOL glossary export -dom localhost:19443 -u "$XMETAUSER" -p "$XMETAPASS" \
  -filename `pwd`/categories.xml -allcategories
