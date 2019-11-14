#!/usr/bin/env bash
set -ue
################################################################
export gitHubToken="${1:-NO_TOKEN}"  ; shift || :
################################################################
export    ANT_OPTS="-Djdk.home.11=$JAVA_HOME -Dpath.variable.maven_repository=m2"
export  MAVEN_OPTS="-Dmaven.repo.local=m2 -DoutputDirectory=out/dependency"
export  OUR_DOMAIN="org.modelingvalue"
export OUR_PRODUCT="dclare"
export OUR_VERSION="$(head -1 releases.md | sed 's/#* *//')"
export  OUR_BRANCH="$(sed 's|^refs/heads/||' <<<"$GITHUB_REF")"
################################################################
export units=(
    collections
    transactions
    jdclare
)
################################################################
varNames=(
    gitHubToken
    ANT_OPTS
    MAVEN_OPTS
    OUR_DOMAIN
    OUR_PRODUCT
    OUR_VERSION
    OUR_BRANCH
    units
)
mkdir -p build/tmp
declare -p "${varNames[@]}" > build/tmp/prep.sh
