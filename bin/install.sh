#!/usr/bin/env bash

REPO_ROOT=$(git rev-parse --show-toplevel)
CWD=$(pwd)
cd "$REPO_ROOT" || exit 1

if [[ $JAVA_HOME == "" ]]; then
  echo "JAVA_HOME is not set. Please set JAVA_HOME to the path of your GraalVM Community installation."
  exit 1
fi

./bin/build.sh || exit 1

echo "Installing spring-dotfile-manager to $HOME/.local/bin/sdfm"
echo "Add $HOME/.local/bin to your PATH if it's not already there!"

mkdir -p "$HOME/.local/bin"
cp "$REPO_ROOT/target/spring-dotfile-manager" "$HOME/.local/bin/sdfm"

echo "Installed!"

cd "$CWD" || exit 1
