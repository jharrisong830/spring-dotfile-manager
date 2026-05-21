#!/usr/bin/env bash

REPO_ROOT=$(git rev-parse --show-toplevel)
CWD=$(pwd)
cd "$REPO_ROOT" || exit 1

./bin/build.sh || exit 1

POM_VERSION=$(./mvnw help:evaluate -Dexpression=project.version -q -DforceStdout)
echo "INSTALL spring-dotfile-manager v$POM_VERSION"

echo "Installing spring-dotfile-manager to $HOME/.local/bin/sdfm"
echo "Add $HOME/.local/bin to your PATH if it's not already there!"

mkdir -p "$HOME/.local/bin"
mkdir -p "$HOME/.local/bin/jar"
cp "$REPO_ROOT/target/spring-dotfile-manager-$POM_VERSION.jar" "$HOME/.local/bin/jar/sdfm.jar"
cp "$REPO_ROOT/bin/sdfm" "$HOME/.local/bin/sdfm"
chmod +x "$HOME/.local/bin/sdfm"

echo "Installed!"

cd "$CWD" || exit 1
