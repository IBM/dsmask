#! /bin/sh

. ./dcs-config.sh

"$IISROOT"/"$IAADMIN" -user "$IAUSER" -password "$IAPASS" -url "$IISURL" -undeployDataClasses "$JARNAME"
