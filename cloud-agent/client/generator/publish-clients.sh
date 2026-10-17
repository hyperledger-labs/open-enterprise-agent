#!/bin/bash
set -euo pipefail

if [[ ! "${VERSION_TAG:-}" =~ ^[0-9]+\.[0-9]+\.[0-9]+(-[A-Za-z0-9]+([.-][A-Za-z0-9]+)*)?$ ]]; then
  echo "VERSION_TAG must be a semantic version" >&2
  exit 1
fi

echo "Publishing ${VERSION_TAG}"

PUBLISH_KOTLIN="${PUBLISH_KOTLIN:-true}"
PUBLISH_TS="${PUBLISH_TS:-true}"

if [[ ! "$PUBLISH_KOTLIN" =~ ^(true|false)$ || ! "$PUBLISH_TS" =~ ^(true|false)$ ]]; then
  echo "Client selection flags must be true or false" >&2
  exit 1
fi
if [[ "$PUBLISH_KOTLIN" == "false" && "$PUBLISH_TS" == "false" ]]; then
  echo "Select at least one client to publish" >&2
  exit 1
fi

# install generator dependencies
yarn install --frozen-lockfile

# Determine if the version is a snapshot or a release
if [[ "$VERSION_TAG" == *-* ]]; then
  echo "Publishing snapshot version"
  if [[ "$PUBLISH_KOTLIN" == "true" ]]; then
    echo "Publishing Kotlin client snapshot"
    gradle -p ../kotlin "-Pversion=${VERSION_TAG}-SNAPSHOT" publishToMavenCentral
  fi

  if [[ "$PUBLISH_TS" == "true" ]]; then
    echo "Publishing TypeScript client snapshot"
    (
      cd ../typescript
      npm install
      npm version "${VERSION_TAG}" --no-git-tag-version
      npm publish --provenance --access public --tag snapshot
    )
  fi
else
  echo "Publishing release version"

  if [[ "$PUBLISH_KOTLIN" == "true" ]]; then
    echo "Publishing Kotlin client release"
    gradle -p ../kotlin "-Pversion=${VERSION_TAG}" publishToMavenCentral
  fi

  if [[ "$PUBLISH_TS" == "true" ]]; then
    echo "Publishing TypeScript client release"
    (
      cd ../typescript
      npm install
      npm version "${VERSION_TAG}" --no-git-tag-version
      npm publish --provenance --access public --tag latest
    )
  fi
fi
