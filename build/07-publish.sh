#!/usr/bin/env bash
set -ue
. build/tmp/prep.sh
################################################################
if [[ $OUR_BRANCH != "master" ]]; then
  echo "INFO: no publish: on '$OUR_BRANCH' and not on 'master' branch"
else
  if [[ $gitHubToken == "" || $gitHubToken == NO_TOKEN ]]; then
    echo "INFO: no publish: no \$gitHubToken defined"
  else
    echo "...publish to github as version '$OUR_VERSION'"
    set -x
    publishJarsOnGitHub \
        "$OUR_BRANCH" \
        "$OUR_VERSION" \
        "$gitHubToken" \
        "false" \
        "${units[@]}"
  fi
fi
