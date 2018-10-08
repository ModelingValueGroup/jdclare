#!/usr/bin/env bash
set -ue

###############################################################################
        OWNER="$(git remote -v | head -1 | sed 's|.*:\([^/]*\)/\([^.]*\).*|\1|')"
        REPOS="$(git remote -v | head -1 | sed 's|.*:\([^/]*\)/\([^.]*\).*|\2|')"
   GITHUB_URL="https://api.github.com"
    REPOS_URL="$GITHUB_URL/repos/$OWNER/$REPOS"
###############################################################################
jsonSelect() {
    local field="$1"; shift

    tr ',' '\n' \
        | fgrep '"'"$field"'":' \
        | sed 's/.*"'"$field"'": *"\([^"{]*\).*/\1/'
}
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

    curl \
        --location \
        --remote-header-name \
        --silent \
        --show-error \
        -o - \
        --header "Authorization: token $token" \
        "$@"
}
validateToken() {
    local token="$1"; shift

    ! curl_ "$token" "$REPOS_URL" >/dev/null
}
releaseExists() {
    local token="$1"; shift
    local   tag="$1"; shift

    ! curl_ "$token" "$REPOS_URL/releases/tags/$tag" | fgrep -q '"message": "Not Found"'
}
publishTag() {
    local   token="$1"; shift
    local isDraft="$1"; shift
    local  branch="$1"; shift
    local     tag="$1"; shift
    local  assets=("$@")

    local comment="$(git tag -l -n1 "$tag" | sed 's/[^ ]* //')"
    local   isPre="$(contains pre "$tag")"
    local relName="$(sed 's/-.*//' <<<$tag)"

    echo "TAG found:"
    echo "      tag     = $tag"
    echo "      relName = $relName"
    echo "      isDraft = $isDraft"
    echo "      isPre   = $isPre"
    echo "      branch  = $branch"
    echo "      comment = $comment"

    if releaseExists "$token" "$tag"; then
        echo "    release for this tag already exists, no further action needed."
    else
        echo "    no release for this tag yet, going to create it..."
        json="$(cat <<EOF
{
  "tag_name"        : "$tag",
  "target_commitish": "$branch",
  "name"            : "$relName",
  "body"            : "$comment",
  "draft"           : $isDraft,
  "prerelease"      : $isPre
}
EOF
)"
        local uploadUrl="$(curl_ "$token" -X POST -d "$json" "$REPOS_URL/releases" | jsonSelect 'upload_url')"
        echo "    using upload url: $uploadUrl"
        for file in "${assets[@]}"; do
            if [ ! -f "$file" ]; then
                echo "ERROR: can not find file $file"
            else
                local mimeType="$(file -b --mime-type "$file")"
                local     name="$(basename "$file" | sed "s/\(.*\)\.\([^.]*\)/\1.$tag.\2/")"
                echo "      attaching: $file as $name ($mimeType)"
                local state="$(curl_ "$token" --header "Content-Type: $mimeType" -X POST -d @"$file" "$uploadUrl?name=$name" | jsonSelect 'state')"
                if [ "$state" == "uploaded" ]; then
                    echo "        => ok"
                else
                    echo "        => bad ($state)"
                fi
            fi
        done
    fi
}
publish() {
    local  token="$1"; shift
    local  draft="$1"; shift
    local assets=("$@")

    local branch="$(git branch | grep \* | cut -d ' ' -f2)"
    if [ "$branch" != master ]; then
        echo "INFO: not on master branch, so no publishing"
    elsif ! validateToken "$token"
        echo "ERROR: not a valid token"
    else
        git tag -l --points-at HEAD \
            | egrep '^v[0-9]' \
            | while read tag; do
                publishTag "$token" "$draft" "$branch" "$tag" "${assets[@]}"
            done
    fi
}
###############################################################################
