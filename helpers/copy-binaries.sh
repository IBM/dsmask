#! /bin/sh

# This script copies the binary packages to the specified targe directory.
# The binary packages need to be already built.

set +e
set +u

if [ -z "$1" ]; then
  echo "USAGE: $0 /path/to/target/dir" >&2
  exit 1
fi
DEST="$1"

if [ ! -d "$DEST" ]; then
  mkdir -vp "$DEST"
fi
if [ ! -d "$DEST" ]; then
  echo "ERROR: not a directory: $DEST" >&2
  exit 1
fi

set -e
set -u

BASE=`dirname $0`
if [ "$BASE" = "." ]; then BASE=".."; else BASE=`dirname $BASE`; fi

cp -v $BASE/dsmask-uniq/target/dsmask-uniq-*-bin.zip $DEST/
cp -v $BASE/dsmask-jconf/target/dsmask-jconf-*-bin.zip $DEST/
cp -v $BASE/dsmask-jmask/target/dsmask-jmask-*-bin.zip $DEST/
cp -v $BASE/ia-bundle-ru/target/ia-bundle-ru-*-bin.zip $DEST/

# End Of File
