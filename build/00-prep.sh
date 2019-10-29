#!/usr/bin/env bash
set -ue
################################################################
export mavenReposDir="${1:-m2}"        ; shift || :
export   gitHubToken="${1:-NO_TOKEN}"  ; shift || :
export      runTests="${1:-true}"      ; shift || :
export       release="${1:-SNAPHOT}"   ; shift || :
################################################################
export      ANT_OPTS="-Djdk.home.9.0.4=$JAVA_HOME -Dpath.variable.maven_repository=$mavenReposDir"
export    MAVEN_OPTS="-Dmaven.repo.local=$mavenReposDir -DoutputDirectory=out/dependency"
export    OUR_DOMAIN="org.modelingvalue"
export   OUR_PRODUCT="dclare"
################################################################
export units=(
    collections
    transactions
    jdclare
)
################################################################
varNames=(
    mavenReposDir
    gitHubToken
    runTests
    release
    ANT_OPTS
    MAVEN_OPTS
    OUR_DOMAIN
    OUR_PRODUCT
    units
)
declare -p "${varNames[@]}" > 00-vars.sh
