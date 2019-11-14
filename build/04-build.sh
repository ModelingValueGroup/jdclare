#!/usr/bin/env bash
set -ue
. build/tmp/prep.sh
################################################################
echo "...build everything"
ant \
  -f build.xml
