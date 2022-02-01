#! /bin/sh

# This scripts updates the Markdown table with the lines of code data.
# The script uses the CLOC tool (https://github.com/AlDanial/cloc),
# which should be in the PATH.
# Run the script from the top-most repository directory:
#   ./helpers/update-lines-of-code.sh

rm -rf dsmask-algo/target
rm -rf dsmask-beans/target
rm -rf dsmask-jconf/target
rm -rf dsmask-jmask/target
rm -rf dsmask-mock/target
rm -rf dsmask-uniq/target
rm -rf ia-custom-ru/target

cloc --quiet --md . > lines-of-code.md

# End Of File
