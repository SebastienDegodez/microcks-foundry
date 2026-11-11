# Contributing

Changes should preserve the dependency direction described in [ARCHITECTURE.md](ARCHITECTURE.md). Open or reference an issue for non-trivial work, and include the relevant issue in the pull request.

## Requirements

- JDK 21
- Maven 3.9 or later
- Docker for a local native Quarkus build

## Validate changes

Format Java sources with the repository's Eclipse JDT profile before committing:

```shell
mvn spotless:apply
```

Check formatting without modifying files:

```shell
mvn spotless:check
```

Run the full module build and test suite:

```shell
mvn --batch-mode --no-transfer-progress verify
```

Build the native Quarkus executable locally:

```shell
mvn --batch-mode --no-transfer-progress -pl foundry-app -am package -Dnative -Dquarkus.native.container-build=true
```

The native executable targets the architecture of the container builder. CI builds Linux x86_64; other platforms are not currently covered by CI.

## Pull requests and dependencies

- Pull requests must pass the GitHub Actions build and architecture tests.
- Pin GitHub Actions to full commit SHAs and retain the upstream version in a comment for Dependabot.
- Dependabot manages Maven and GitHub Actions version updates. Keep the configured minimum action releases and JDK/Quarkus versions aligned with [AGENTS.md](AGENTS.md) and [ARCHITECTURE.md](ARCHITECTURE.md).
