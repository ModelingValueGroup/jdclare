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
        local     name="$(basename "$file" | sed "s/\(.*\)\.\([^.]*\)/\1.$tag.\2/")"
        echo "      attaching: $file as $name ($mimeType)"
        local  attJson="$(curl_ "$token" --header "Content-Type: $mimeType" -X POST -d @"$file" "$uploadUrl?name=$name")"
        local    state="$(jq --raw-output '.state' <<<"$attJson")"
        if [ "$state" == "uploaded" ]; then
            echo "        => ok"
        else
            echo "        => oops, not correctly attached: $state"
            local hadError=true
        fi
        echo "JSON: $attJson"
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
publish() {
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
###############################################################################
