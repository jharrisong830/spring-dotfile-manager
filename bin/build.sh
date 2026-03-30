#!/usr/bin/env bash

REPO_ROOT=$(git rev-parse --show-toplevel)
CWD=$(pwd)
cd "$REPO_ROOT" || exit 1

if [[ $JAVA_HOME == "" ]]; then
  echo "JAVA_HOME is not set. Please set JAVA_HOME to the path of your GraalVM Community installation."
  exit 1
fi

NATIVE_IMAGE_BIN="$JAVA_HOME/bin/native-image"

./mvnw clean -Pnative native:compile || exit 1

echo "Native image built to ${REPO_ROOT}/target/spring-dotfile-manager"

cd "$CWD" || exit 1
