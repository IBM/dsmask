#! /bin/sh

. ./dcs-config.sh

cp -f "$JARSRC" "$IISROOT"/"$IISJAR1"/"$JARDST"
cp -f "$JARSRC" "$IISROOT"/"$IISJAR2"/"$JARDST"

"$IISROOT"/"$IAADMIN" -user "$IAUSER" -password "$IAPASS" -url "$IISURL" -updateDataClasses "$JARNAME"

"$IISROOT"/"$IAADMIN" -installClassifiers "$JARNAME"
