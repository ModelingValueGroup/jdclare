#!/usr/bin/env bash
set -ue
. publish-script.sh

publish "$1" false \
    out/artifacts/dclare-collections.jar \
    out/artifacts/dclare-collections-src.jar \
    out/artifacts/dclare-transactions.jar \
    out/artifacts/dclare-transactions-src.jar
