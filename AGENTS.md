# Agent instructions

Before changing the project, review:

- @ARCHITECTURE.md — module responsibilities, dependency direction, and supported build targets.
- @CONTRIBUTING.md — local setup, validation commands, and pull request expectations.

## Toolchain

- Use JDK 21. Keep the GitHub Actions Java setup and Maven compiler release aligned with `maven.compiler.release` in the root `pom.xml`.
- Keep the Quarkus BOM and Maven plugin aligned through the root `quarkus.platform.version` property. Its current value is `3.20.3`.
- Use Maven 3.9 or later.
- Format Java with Spotless using the repository's Eclipse JDT profile: 3-space indentation and a 120-character line limit.
- Run `mvn spotless:apply` before committing Java changes. `mvn spotless:check` and `mvn verify` must pass.
- The repository-local pre-push hook runs `mvn spotless:check`; do not bypass it to push unformatted Java.

## Architecture

- Preserve the dependency direction documented in `ARCHITECTURE.md` and enforced by the ArchUnit tests.
- Keep `foundry-domain` and `foundry-application` free of Quarkus and adapter dependencies.
- Keep adapters in `foundry-infrastructure`; compose them in `foundry-app` or `foundry-cli`.
- The current GitHub Actions jobs target Linux x86_64. The native executable is built for the runner's architecture; other operating systems and CPU architectures are not currently validated in CI.

## GitHub Actions

- Pin every action to a full commit SHA. Keep the upstream release tag in a trailing comment so Dependabot can track it.
- Do not use action releases older than `actions/checkout` v7, `actions/setup-java` v6, or `actions/upload-artifact` v7; these baselines use Node.js 24.
- Keep Dependabot enabled for Maven and GitHub Actions updates.
