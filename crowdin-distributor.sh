#!/usr/bin/env bash
set -e

# For snapshots, please specify the full version (with date and time)
cdist_version="0.1.0-20201002.083956-2"
cdist_path_version="$cdist_version"

if [ -n "${cdist_version#*-}" ]; then
  cdist_path_version="${cdist_version%%-*}-SNAPSHOT"
fi
url="https://maven.enginehub.org/repo/org/enginehub/crowdin/crowdin-distributor/$cdist_path_version/crowdin-distributor-$cdist_version-bundle.zip"
curl "$url" >./build/cdist.zip
(cd build && unzip -o cdist.zip)

# CROWDIN_DISTRIBUTOR_TOKEN is set by CI
export CROWDIN_DISTRIBUTOR_ON_CHANGE="true"
export CROWDIN_DISTRIBUTOR_PROJECT_ID="360697"
export CROWDIN_DISTRIBUTOR_MODULE="worldedit-lang"
# Artifactory & Build Number is set by CI
export CROWDIN_DISTRIBUTOR_OPTS="--enable-preview"
"./build/crowdin-distributor-$cdist_path_version/bin/crowdin-distributor"
