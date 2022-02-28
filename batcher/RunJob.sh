#! /bin/sh

BATCH_ID="$1"
DB_SRC="$2"
DB_DST="$3"
TABNAME="$4"
TABPROF="$5"

if [ -z "$DB_SRC" ] || [ -z "$DB_DST" ] || [ -z "$TABNAME" ] || [ -z "$TABPROF" ]; then
    echo "USAGE: $0 BATCH_ID DB_SRC DB_DST SCHEMA.TABLE datastore.schema.table"
    exit 1
fi

IS_HOME=/opt/IBM/InformationServer
DSJOB=$IS_HOME/Server/DSEngine/bin/dsjob
. $IS_HOME/Server/DSEngine/dsenv

INSTID=`echo "$DB_SRC"."$TABNAME" | tr '.' '-'`

resetJob() {
	$DSJOB -run -mode RESET -wait dstage1 MaskJdbc."$INSTID"
}

runJob() {
	$DSJOB -run -param Globals=default \
	  -param BatchId="$BATCH_ID" \
	  -param DbParams="$DB_SRC" -param DbOutParams="$DB_DST" \
	  -param InputTable="$TABNAME" -param OutputTable="$TABNAME" \
	  -param MaskingProfile="$TABPROF" \
	  dstage1 MaskJdbc."$INSTID"
}

runJob
statusCode=$?

if [ $statusCode -ne 0 ]; then
    resetJob
    runJob
fi

# End Of File
