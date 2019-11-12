#!/usr/bin/env bash
set -ue
. build/tmp/prep.sh
################################################################
if [[ $release != "" && $release != SNAPHOT && $gitHubToken != "" && $gitHubToken != NO_TOKEN ]]; then
  echo "...publish to github as release '$release'"
  publishJarsOnGitHub \
      "$release" \
      "$gitHubToken" \
      "false" \
      "${units[@]}"
fi
