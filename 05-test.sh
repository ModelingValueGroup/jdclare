#!/usr/bin/env bash
set -ue
. 00-vars.sh
################################################################
if [ "$runTests" == true ]; then
  echo "...testing"
  generateAntTestFile "mvg-jdclare" > test.xml
  if ant -Dpath.variable.maven_repository=$mavenReposDir -f test.xml; then
    echo "...all tests ok!"
  else
    echo "======================================================================"
    echo " FAILURES DETECTED"
    echo "======================================================================"
    for f in $(\
          egrep '(errors|failures)="' TEST-* \
                | egrep -v 'errors="0" failures="0"' \
                | sed 's/:.*//'
          ); do
          [[ -f "$f" ]] && cat $f
    done
    echo "======================================================================"
    exit 99
  fi
fi
