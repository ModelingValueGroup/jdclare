#!/usr/bin/env bash
set -ue
. build/tmp/prep.sh
################################################################
echo "...make javadoc"
makeAllJavaDocJars "${units[@]}"
