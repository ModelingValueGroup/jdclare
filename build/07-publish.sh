#!/usr/bin/env bash
set -ue
. build/tmp/prep.sh
################################################################
if [[ $OUR_BRANCH == "master" ]]; then
  if [[ $gitHubToken != "" && $gitHubToken != NO_TOKEN ]]; then
    echo "INFO: can not publish, no \$gitHubToken defined"
  else
    echo "...publish to github as version '$OUR_VERSION'"
    publishJarsOnGitHub \
        "$release" \
        "$gitHubToken" \
        "false" \
        "${units[@]}"
  fi
fi
