#!/usr/bin/env bash
set -ue
################################################################
gitHubToken="${1:-NO_TOKEN}"  ; shift || :
################################################################
   ANT_OPTS="-Djdk.home.11=$JAVA_HOME -Dpath.variable.maven_repository=m2"
 MAVEN_OPTS="-Dmaven.repo.local=m2 -DoutputDirectory=out/dependency"
 OUR_DOMAIN="org.modelingvalue"
OUR_PRODUCT="dclare"
OUR_VERSION="$(head -1 releases.md | sed 's/#* *//')"
 OUR_BRANCH="$(sed 's|^refs/heads/||' <<<"$GITHUB_REF")"
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
set