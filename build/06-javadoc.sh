#!/usr/bin/env bash
set -ue
. 00-vars.sh
################################################################
echo "...make javadoc"
makeAllJavaDocJars "${units[@]}"
