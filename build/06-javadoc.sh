#!/usr/bin/env bash
set -ue
. build/tmp/prep.sh
################################################################
echo "...make javadoc"

for n in "${units[@]}"; do
   cacheFile="cache/$n/javadoc.jar"
  targetFile="out/artifacts/$n-SNAPSHOT-javadoc.jar"

  mkdir -p "$(dirname "$cacheFile")" "$(dirname "$targetFile")"

  if [[ -f "$cacheFile" ]]; then
    cp "$cacheFile" "$targetFile"
  else
    makeAllJavaDocJars "$n"
    cp "$targetFile" "$cacheFile"
  fi
done
