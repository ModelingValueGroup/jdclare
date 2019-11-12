#!/usr/bin/env bash
set -ue
. build/tmp/prep.sh
################################################################
echo "...make the poms with the dependencies"
makeAllPoms \
    "http://www.dclare-lang.org" \
    "https://github.com/ModelingValueGroup/jdclare.git" \
    "$OUR_VERSION" \
    "${units[@]}"
