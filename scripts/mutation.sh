#!/usr/bin/env bash
set -euo pipefail

cd "$(dirname "$0")/.."

fingerprint=foundry-application/target/pit-history-inputs.sha
inputs_hash=$(
  {
    for pom in pom.xml foundry-domain/pom.xml foundry-application/pom.xml; do
      printf '%s\0' "$pom"
      git hash-object -- "$pom" || exit 1
    done
    find foundry-domain/src/test foundry-application/src/test \
      -type f -path '*/resources/*' -print0 |
      LC_ALL=C sort -z |
      while IFS= read -r -d '' resource; do
        printf '%s\0' "$resource"
        git hash-object -- "$resource" || exit 1
      done
  } | git hash-object --stdin
)

# PIT hashes Java tests, but not the resources read by their runners.
previous_hash=
if [[ -f "$fingerprint" ]]; then
  previous_hash=$(cat "$fingerprint")
fi
if [[ "$previous_hash" != "$inputs_hash" ]]; then
  rm -f foundry-application/target/pit-history.bin
fi

if mvn -B -q -pl foundry-application -am test-compile org.pitest:pitest-maven:mutationCoverage; then
  printf '%s\n' "$inputs_hash" > "$fingerprint"
else
  result=$?
  rm -f foundry-application/target/pit-history.bin foundry-application/target/pit-history-inputs.sha
  exit "$result"
fi
