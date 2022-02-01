#! /bin/sh

DB_SRC="$1"

if [ -z "$DB_SRC" ]; then
    echo "USAGE: $0 DB_SRC"
    exit 1
fi

IS_HOME=/opt/IBM/InformationServer
DSJOB=$IS_HOME/Server/DSEngine/bin/dsjob
. $IS_HOME/Server/DSEngine/dsenv

# RUNNING
# QUEUED

$DSJOB -linvocations dstage1 MaskJdbc 2>/dev/null | grep MaskJdbc."$DB_SRC"'-' | while read jid; do
  $DSJOB -jobinfo dstage1 "$jid" 2>/dev/null | grep "Job Status" | while read sl; do
    case "$sl" in
	*RUNNING*)  echo "RUNNING $jid" ;;
	*QUEUED*)   echo "QUEUED  $jid" ;;
	esac
  done
done

# End Of File
