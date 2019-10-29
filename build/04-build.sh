#!/usr/bin/env bash
set -ue
. 00-vars.sh
################################################################
echo "...build everything"
ant \
  -f build.xml
