#!/usr/bin/env bash
set -ue
. 00-vars.sh
################################################################
echo "...getting dependencies from maven"
mvn \
  -f out/artifacts/ALL-SNAPSHOT.pom \
  dependency:copy-dependencies
