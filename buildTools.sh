#!/usr/bin/env bash
set -ue

###############################################################################
   GITHUB_URL="https://api.github.com"
        OWNER="$(git remote -v | head -1 | sed 's|.*https://github.com/||;s|.*:||;s|\.git .*||' | sed 's|\([^/]*\)/\(.*\)|\1|')"
        REPOS="$(git remote -v | head -1 | sed 's|.*https://github.com/||;s|.*:||;s|\.git .*||' | sed 's|\([^/]*\)/\(.*\)|\2|')"
    REPOS_URL="$GITHUB_URL/repos/$OWNER/$REPOS"
###############################################################################
contains() {
    local find="$1"; shift
    local  str="$1"; shift

    if [[ "$str" =~ .*$find.* ]]; then
        echo true
    else
        echo false
    fi
}
curl_() {
    local token="$1"; shift

    #set -x
    curl \
        --location \
        --remote-header-name \
        --silent \
        --show-error \
        -o - \
        --header "Authorization: token $token" \
        "$@"
    #set +x
}
validateToken() {
    local token="$1"; shift

    curl_ "$token" "$REPOS_URL" >/dev/null
}
getRelease() {
    local token="$1"; shift
    local   tag="$1"; shift

    curl_ "$token" "$REPOS_URL/releases/tags/$tag"
}
publishTag() {
    local     tag="$1"; shift
    local   token="$1"; shift
    local isDraft="$1"; shift
    local  branch="$1"; shift
    local  assets=("$@")

    local comment="release $tag created on $(date +'%Y-%m-%d %H:%M:%S')"
    local   isPre="$(contains pre "$tag")"

    echo "release info:"
    echo "        tag     = $tag"
    echo "        relName = $tag"
    echo "        isDraft = $isDraft"
    echo "        isPre   = $isPre"
    echo "        branch  = $branch"
    echo "        comment = $comment"


    local   relJson="$(getRelease "$token" "$tag")"
    local uploadUrl="$(jq --raw-output '.upload_url' <<<"$relJson")"
    if [ "$uploadUrl" != "null" ]; then
        echo "ERROR: this release already exists, delete it first" 1>&2
        exit 99
    fi
    echo "    creating new release..."
    json="$(cat <<EOF
{
"tag_name"        : "$tag",
"target_commitish": "$branch",
"name"            : "$tag",
"body"            : "$comment",
"draft"           : $isDraft,
"prerelease"      : $isPre
}
EOF
)"
    local relJson="$(curl_ "$token" -X POST -d "$json" "$REPOS_URL/releases")"
    local uploadUrl="$(jq --raw-output '.upload_url' <<<"$relJson")"
    if [ "$uploadUrl" == "null" ]; then
        echo "ERROR: unable to create the release: $relJson" 1>&2
        exit 99
    fi
    local uploadUrl="$(sed -E 's/\{\?.*//' <<<"$uploadUrl")"
    echo "    using upload url: $uploadUrl"

    local hadError=false
    for file in "${assets[@]}"; do
        local mimeType="$(file -b --mime-type "$file")"
        local     name="$(basename "$file" | sed "s/SNAPSHOT/$tag/")"
        echo "      attaching: $file as $name ($mimeType)"
        local  attJson="$(curl_ "$token" --header "Content-Type: $mimeType" -X POST --data-binary @"$file" "$uploadUrl?name=$name")"
        echo "$attJson" >"$name.upload.json"
        local    state="$(jq --raw-output '.state' <<<"$attJson")"
        if [ "$state" == "uploaded" ]; then
            echo "        => ok"
        else
            echo "        => oops, not correctly attached: $state"
            local hadError=true
        fi
    done
    if [ "$hadError" == true ]; then
        echo "ERROR: some assets could not be attached" 1>&2
        exit 99
    fi
}
currentBranch() {
    if [ "${GIT_BRANCH:-}" != "" ]; then
        sed 's|.*/||' <<<"$GIT_BRANCH"
    else
        git branch | grep \* | cut -d ' ' -f2
    fi
}
publishJarsOnGitHub() {
    local version="$1"; shift
    local   token="$1"; shift
    local isDraft="$1"; shift
    local   names=("$@")

    local  assets=()
    for n in "${names[@]}"; do
        assets+=("$(makeJarName        $n)")
        assets+=("$(makeJarNameSources $n)")
        assets+=("$(makeJarNameJavadoc $n)")
    done
    publishOnGitHub "$version" "$token" "$isDraft" "${assets[@]}"
}
publishOnGitHub() {
    local version="$1"; shift
    local   token="$1"; shift
    local isDraft="$1"; shift
    local  assets=("$@")

    if [ "$version" == "" ]; then
        echo "ERROR: version is empty" 1>&2
        exit 60
    fi
    if [ "$(git tag -l "$version")" ]; then
        echo "ERROR: tag $version already exists" 1>&2
        exit 70
    fi
    if ! validateToken "$token"; then
        echo "ERROR: not a valid token" 1>&2
        exit 80
    fi
    local hadError=false
    for file in "${assets[@]}"; do
        if [ ! -f "$file" ]; then
            echo "ERROR: file not found: $file" 1>&2
            hadError=true
        fi
    done
    if [ "$hadError" == true ]; then
        exit 95
    fi

    local branch="$(currentBranch)"
    publishTag "$version" "$token" "$isDraft" "$branch" "${assets[@]}"
}
makeJavaDocJar() {
    local sjar="$1"; shift
    local djar="$1"; shift

    mkdir tmp-src
    (cd tmp-src; jar xf "../$sjar")
    javadoc -d tmp-doc -sourcepath tmp-src -subpackages "$OUR_DOMAIN"
    jar cf "$djar" -C tmp-doc .
    rm -rf tmp-src tmp-doc
}
makeJarName() {
    local      name="$1"; shift
    local variation="${1:-}"

    echo "$ARTIFACT_DIR/$name-SNAPSHOT$variation.jar"
}
makeJarNameSources() {
    makeJarName "$1" -sources
}
makeJarNameJavadoc() {
    makeJarName "$1" -javadoc
}
makeAllJavaDocJars() {
    rm -f "$ARTIFACT_DIR"/*-javadoc.jar
    mkdir -p "$ARTIFACT_DIR"
    for n in "$@"; do
        makeJavaDocJar "$(makeJarNameSources $n)" "$(makeJarNameJavadoc $n)"
    done
}
makeAllPoms() {
    local version="$1"; shift

    rm -f "$ARTIFACT_DIR"/*.pom
    mkdir -p "$ARTIFACT_DIR"
    for name in "$@"; do
        makePomFromGavs "$version" "$name" "$(findDescription "$name")" $(findAllGavsOf "$name")
    done
    makePomFromGavs "unused" "ALL" "unused" $(findAllGavs)
}
makePomFromGavs() {
    local     version="$1"; shift
    local        name="$1"; shift
    local description="$1"; shift
    local        gavs=("$@")

    genDependencies() {
        for gav in "$@"; do
            IFS=: read g a v <<<"$gav"
            cat <<EOF
        <dependency>
            <groupId>$g</groupId>
            <artifactId>$a</artifactId>
            <version>$v</version>
        </dependency>
EOF
        done
    }

    mkdir -p "$ARTIFACT_DIR"
    local pom="$ARTIFACT_DIR/$name-SNAPSHOT.pom"
    cat <<EOF >"$pom"
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0
                             http://maven.apache.org/maven-v4_0_0.xsd">
    <modelVersion>4.0.0</modelVersion>

    <groupId>$OUR_DOMAIN.$OUR_PRODUCT</groupId>
    <artifactId>$name</artifactId>
    <version>$([ "$version" == SNAPSHOT ] && echo "0.0.0-SNAPSHOT" || echo "$version")</version>
    <packaging>jar</packaging>

    <name>$OUR_PRODUCT $name</name>
    <description>$description</description>
    <url>http://www.dclare-lang.org</url>

    <licenses>
        <license>
            <name>GNU Lesser General Public License v3.0</name>
            <url>https://www.gnu.org/licenses/lgpl-3.0.en.html</url>
            <distribution>repo</distribution>
        </license>
    </licenses>

    <scm>
        <url>https://github.com/ModelingValueGroup/jdclare.git</url>
    </scm>

    <dependencies>
$(genDependencies "${gavs[@]}")
    </dependencies>
</project>
EOF
    echo "    generated $pom"
}
findAllGavsOf() {
    local name="$1"; shift

    for iml in "$OUR_DOMAIN".$name/*.iml "$OUR_DOMAIN".$name.*/*.iml; do
        fgrep '"Maven: ' $iml | fgrep -v 'scope="TEST"' | sed 's/.*"Maven: //;s/".*//'
    done | sort -u
}
findAllGavs() {
    for iml in */*.iml; do
        fgrep '"Maven: ' $iml | sed 's/.*"Maven: //;s/".*//'
    done | sort -u
}
findDescription() {
    local name="$1"; shift

    for i in "$OUR_DOMAIN".$name "$OUR_DOMAIN".$name.*; do
        cat "$i/description" 2>/dev/null || :
    done
}
genFileSets() {
    ls -d out/test/* \
        | while read d; do
            echo "<fileset dir=\"$d\"><include name=\"**/*Test.*\"/><include name=\"**/*Tests.*\"/></fileset>"
        done
}
generateAntTestFile() {
    cat <<EOF
<?xml version="1.0" encoding="UTF-8"?>
<project name="mvg-jdclare" default="all">

    <path id="cp">
        <path>
            <pathelement location="\${path.variable.maven_repository}/junit/junit/4.12/junit-4.12.jar"/>
            <pathelement location="\${path.variable.maven_repository}/org/hamcrest/hamcrest-core/1.3/hamcrest-core-1.3.jar"/>
        </path>
        <dirset dir="out/production">
            <include name="*"/>
        </dirset>
        <dirset dir="out/test">
            <include name="*"/>
        </dirset>
    </path>

    <target name="all">
        <junit haltonfailure="on" logfailedtests="on" fork="on" forkmode="perTest" threads="8">
            <classpath refid="cp"/>
            <batchtest todir=".">
$(genFileSets)
                <formatter type="xml"/>
            </batchtest>
        </junit>
    </target>
</project>
EOF
}
###############################################################################
