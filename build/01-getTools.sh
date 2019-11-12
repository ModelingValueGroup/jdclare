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

git remote -v | sed 's/^/@A@ /'
git remote -v | head -1  | sed 's/^/@B@ /'
git remote -v | head -1 | sed 's|.*https://github.com/||;s|.*:||;s|\.git .*||'  | sed 's/^/@C@ /'
git remote -v | head -1 | sed 's|.*https://github.com/||;s|.*:||;s|\.git .*||' | sed 's|\([^/]*\)/\(.*\)|\2|' | sed 's/^/@D@ /'
