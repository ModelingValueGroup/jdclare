#!/bin/bash

sedi() {
    if [[ "$OSTYPE" =~ darwin* ]]; then
        sed -i '' "$@"
    else
        sed -i "$@"
    fi
}

for xml in build.xml */module_*.xml; do
    echo "===== $xml"
    # unfortunately IntellJ generates absolute paths for some zipfileset:
    sedi 's|<zipfileset dir="/.*/jdclare/|<zipfileset dir="${basedir}/|' "$xml"

    # add includeantruntime="false" for all <javac> calls:
    sedi 's|<javac \([^i]\)|<javac includeantruntime="false" \1|' "$xml"
done
