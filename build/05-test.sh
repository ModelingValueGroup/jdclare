#!/usr/bin/env bash
set -ue
. build/tmp/prep.sh
################################################################
echo "...testing"
generateAntTestFile "mvg-jdclare" > test.xml

# to keep travis-ci happy:
SECONDS=0
while sleep 60; do
  echo "=====[ still running after $SECONDS sec... ]====="
done &


if ! ant -f test.xml; then
  kill %1
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
kill %1
echo "...all tests ok!"
