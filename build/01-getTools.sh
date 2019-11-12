#!/usr/bin/env bash
set -ue
. build/tmp/prep.sh
################################################################
echo "...get our tools"
rm -rf tools
git clone 'https://github.com/ModelingValueGroup/tools.git'
( echo ". tools/tools.sh"
  cat build/tmp/prep.sh
) >> build/tmp/prep.sh-tmp
cp build/tmp/prep.sh-tmp build/tmp/prep.sh

set -x
. build/tmp/prep.sh