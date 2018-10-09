#!/usr/bin/env bash
set -ue

version="$1"; shift
  token="$1"; shift
isDraft="$1"; shift

. publish-script.sh

JARS=(collections transactions jdclare)

if [ "$version" != "" ]; then
    publish \
        "$version" \
        "$token" \
        "$isDraft" \
        $(for n in "${JARS[@]}"; do echo "out/artifacts/$n.jar out/artifacts/$n-src.jar";done)
fi