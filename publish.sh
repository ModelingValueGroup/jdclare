#!/usr/bin/env bash
set -ue

version="$1"; shift
  token="$1"; shift
isDraft="$1"; shift

. publish-script.sh

if [ "$version" != "" ]; then
    publish \
        "$version" \
        "$token" \
        "$isDraft" \
        out/artifacts/dclare-collections.jar \
        out/artifacts/dclare-collections-src.jar \
        out/artifacts/dclare-transactions.jar \
        out/artifacts/dclare-transactions-src.jar
fi