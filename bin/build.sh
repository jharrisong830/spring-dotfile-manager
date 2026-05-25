#!/usr/bin/env bash

REPO_ROOT=$(git rev-parse --show-toplevel)
CWD=$(pwd)
cd "$REPO_ROOT" || exit 1

POM_VERSION=$(./mvnw help:evaluate -Dexpression=project.version -q -DforceStdout)
echo "BUILD spring-dotfile-manager v$POM_VERSION"

./mvnw clean package

cd "$CWD" || exit 1
