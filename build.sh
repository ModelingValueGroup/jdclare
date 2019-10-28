#!/usr/bin/env bash
set -ue
################################################################
mkdir -p log
. 00-prep.sh            #> log/00-prep.log
. 01-getTools.sh        #> log/01-getTools.log
. 02-makePoms.sh        #> log/02-makePoms.log
. 03-getDependencies.sh #> log/03-getDependencies.log
. 04-build.sh           #> log/04-build.log
. 05-test.sh            #> log/05-test.log
. 06-javadoc.sh         #> log/06-javadoc.log
. 07-publish.sh         #> log/07-publish.log
