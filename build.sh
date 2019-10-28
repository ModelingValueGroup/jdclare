#!/usr/bin/env bash
set -ue
################################################################
mkdir -p log
. 00-prep.sh            | tee log/00-prep.log
. 01-getTools.sh        | tee log/01-getTools.log
. 02-makePoms.sh        | tee log/02-makePoms.log
. 03-getDependencies.sh | tee log/03-getDependencies.log
. 04-build.sh           | tee log/04-build.log
. 05-test.sh            | tee log/05-test.log
. 06-javadoc.sh         | tee log/06-javadoc.log
. 07-publish.sh         | tee log/07-publish.log
