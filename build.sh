#!/usr/bin/env bash
set -ue
################################################################
mavenReposDir="$1"; shift
      jdkHome="$1"; shift
  gitHubToken="$1"; shift
     runTests="$1"; shift
      release="$1"; shift
################################################################
rm -rf tools
git clone 'https://github.com/ModelingValueGroup/tools.git'
. tools/tools.sh
################################################################
export     ANT_OPTS="-Djdk.home.9.0=$jdkHome -Dpath.variable.maven_repository=$mavenReposDir"
export   MAVEN_OPTS="-Dmaven.repo.local=$mavenReposDir -DoutputDirectory=out/dependency"
export   OUR_DOMAIN="org.modelingvalue"
export  OUR_PRODUCT="dclare"
################################################################
units=(
    collections
    transactions
    jdclare
)
################################################################
mkdir -p log

# make the poms with the dependencies:
echo "...making poms"
makeAllPoms \
    "http://www.dclare-lang.org" \
    "https://github.com/ModelingValueGroup/jdclare.git" \
    "$release" \
    "${units[@]}"

# get our dependencies from maven:
echo "...getting dependencies from maven"
mvn -f out/artifacts/ALL-SNAPSHOT.pom dependency:copy-dependencies

# build everything:
echo "...building"
ant -f build.xml

# if testing is requested: run the tests:
if [ "$runTests" == true ]; then
    echo "...testing"
    generateAntTestFile "mvg-jdclare" > test.xml
    if ! ant -debug -Dpath.variable.maven_repository=$mavenReposDir -f test.xml; then
      echo "======================================================================"
      echo " FAILURES DETECTED"
      echo "======================================================================"
      cat TEST-*
      echo "======================================================================"
      for f in $(\
            egrep '(errors|failures)="' TEST-* \
                  | egrep -v 'errors="0" failures="0"' \
                  | sed 's/:.*//'
            ); do
            [[ -f "$f" ]] && cat $f
      done
      echo "======================================================================"
      exit 99
    else
      echo "...all tests ok!"
    fi
else
    echo "...skipping tests"
fi

# make the javadoc jars:
echo "...make javadoc"
makeAllJavaDocJars "${units[@]}"

# if a release is requested: publish it on github:
if [ "$release" != "" -a "$release" != SNAPHOT ]; then
    echo "...publish to github"
    publishJarsOnGitHub \
        "$release" \
        "$gitHubToken" \
        "false" \
        "${units[@]}"
else
    echo "...skipping release to github"
fi
