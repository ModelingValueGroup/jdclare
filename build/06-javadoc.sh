#!/usr/bin/env bash
set -ue
. build/tmp/prep.sh
################################################################
echo "...make javadoc"

mkdir -p out/artifacts
for n in "${units[@]}"; do
   cacheFile="cache/$n/javadoc.jar"
  targetFile="out/artifacts/$n-SNAPSHOT-javadoc.jar"
  if [[ -f "$cacheFile" ]]; then
    cp "$cacheFile" "$targetFile"
  else
    makeAllJavaDocJars "$n"
    cp "$targetFile" "$cacheFile"
  fi
done
