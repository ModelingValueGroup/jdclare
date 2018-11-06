#!/usr/bin/env bash
set -ue
################################################################
mavenReposDir="$1"; shift
      jdkHome="$1"; shift
  gitHubToken="$1"; shift
     runTests="$1"; shift
      release="$1"; shift
################################################################
export     ANT_OPTS="-Djdk.home.9.0=$jdkHome -Dpath.variable.maven_repository=$mavenReposDir"
export   MAVEN_OPTS="-Dmaven.repo.local=$mavenReposDir -DoutputDirectory=out/dependency"
export  OUR_PRODUCT="dclare"
export   OUR_DOMAIN="org.modelingvalue"
export ARTIFACT_DIR="out/artifacts"
################################################################
units=(
    collections
    transactions
    jdclare
)
################################################################
. buildTools.sh
mkdir -p log

# make the poms with the dependencies:
echo "...making poms"
makeAllPoms "$release" "${units[@]}" >log/00-makePoms.log

# get our dependencies from maven:
echo "...getting dependencies from maven"
mvn -f out/artifacts/ALL-SNAPSHOT.pom dependency:copy-dependencies >log/01-getDeps.log

# build everything:
echo "...building"
ant -f build.xml >log/02-build.log

# if testing is requested: run the tests:
if [ "$runTests" == true ]; then
    echo "...testing"
    generateAntTestFile > test.xml
    ant -f test.xml >log/03-test.log
else
    echo "...skipping tests"
fi

# make the javadoc jars:
echo "...make javadoc"
makeAllJavaDocJars "${units[@]}" >log/04-genJavadoc.log

# if a release is requested: publish it on github:
if [ "$release" != "" -a "$release" != SNAPHOT ]; then
    echo "...publish to github"
    publishJarsOnGitHub \
        "$release" \
        "$gitHubToken" \
        "false" \
        "${units[@]}" \
        >log/05-publishJarsOnGitHub.log
else
    echo "...skipping release to github"
fi
