#!/usr/bin/env bash
set -ue
. build/tmp/prep.sh
################################################################
echo "...build everything"
sed 's/^/@@@ /' build/tmp/prep.sh
ant \
  -f build.xml
