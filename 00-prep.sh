#!/usr/bin/env bash
set -ue
################################################################
export mavenReposDir="$1"; shift
export       jdkHome="$1"; shift
export   gitHubToken="$1"; shift
export      runTests="$1"; shift
export       release="$1"; shift
################################################################
export      ANT_OPTS="-Djdk.home.9.0=$jdkHome -Dpath.variable.maven_repository=$mavenReposDir"
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
    jdkHome
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
