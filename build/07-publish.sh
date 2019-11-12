#!/usr/bin/env bash
set -ue
. build/tmp/prep.sh
################################################################
if [[ $OUR_BRANCH != "master" ]]; then
  echo "INFO: not on master branch, no publish"
else
  if [[ $gitHubToken != "" && $gitHubToken != NO_TOKEN ]]; then
    echo "INFO: can not publish, no \$gitHubToken defined"
  else
    echo "...publish to github as version '$OUR_VERSION'"
    set -x
    publishJarsOnGitHub \
        "$OUR_VERSION" \
        "$gitHubToken" \
        "false" \
        "${units[@]}"
  fi
fi
