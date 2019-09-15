#!/usr/bin/env bash
set -ue
. 00-vars.sh
################################################################
echo "...get our tools"
rm -rf tools
git clone 'https://github.com/ModelingValueGroup/tools.git'
. tools/tools.sh
