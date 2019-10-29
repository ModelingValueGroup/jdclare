#!/usr/bin/env bash
set -ue
. 00-vars.sh
################################################################
if [[ $release != "" && $release != SNAPHOT && $gitHubToken != "" && $gitHubToken != NO_TOKEN ]]; then
  echo "...publish to github as release '$release'"
  publishJarsOnGitHub \
      "$release" \
      "$gitHubToken" \
      "false" \
      "${units[@]}"
fi
