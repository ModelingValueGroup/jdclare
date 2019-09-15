#!/usr/bin/env bash
set -ue
. 00-vars.sh
################################################################
if [ "$release" != "" -a "$release" != SNAPHOT ]; then
  echo "...publish to github"
  publishJarsOnGitHub \
      "$release" \
      "$gitHubToken" \
      "false" \
      "${units[@]}"
fi
