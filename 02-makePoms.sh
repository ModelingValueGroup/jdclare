#!/usr/bin/env bash
set -ue
. 00-vars.sh
################################################################
set -x
echo "...make the poms with the dependencies"
makeAllPoms \
    "http://www.dclare-lang.org" \
    "https://github.com/ModelingValueGroup/jdclare.git" \
    "$release" \
    "${units[@]}"
