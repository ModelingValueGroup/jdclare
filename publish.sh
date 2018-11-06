#!/usr/bin/env bash
set -ue

version="$1"; shift
  token="$1"; shift
isDraft="$1"; shift

JARS=(collections transactions jdclare)

. publish-script.sh

makeAllPoms "$version" "${JARS[@]}"
makeAllJavaDocJars     "${JARS[@]}"

if [ "$version" != "" -a "$version" != SNAPHOT ]; then
    publishJarsOnGitHub \
        "$version" \
        "$token" \
        "$isDraft" \
        "${JARS[@]}"
fi
