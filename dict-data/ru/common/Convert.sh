#! /bin/sh

sed 's/.*"\(.*\)".*/\1/' <"$1" | (LANG=C sort -u) >"$1".tmp
mv "$1".tmp "$1"
