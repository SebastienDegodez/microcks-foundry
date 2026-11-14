# Contributing

Changes should preserve the dependency direction described in [ARCHITECTURE.md](ARCHITECTURE.md). Open or reference an issue for non-trivial work, and include the relevant issue in the pull request.

## Requirements

- JDK 21
- Maven 3.9 or later
- Docker for a local native Quarkus build

When several JDKs are installed, select JDK 21 with `JAVA_HOME` and check `mvn --version` before running the gates.

## Validate changes

Format Java sources with the repository's Eclipse JDT profile before committing:

```shell
mvn spotless:apply
```

Check formatting without modifying files:

```shell
mvn spotless:check
```

The repository configures `.githooks` as its local Git hook path during Maven's `validate` phase. Only `pre-push` runs the formatting check; run `mvn verify` once after checkout to activate it. To skip hook-path configuration for a Maven invocation, use `-Dgit.hooks.skip=true`; this does not disable a hook path already configured locally.

Run the full module build and test suite:

```shell
mvn --batch-mode --no-transfer-progress verify
```

Unit tests use `*Test` names and run with `mvn test`. Integration tests use `*IT` names and run with Failsafe during
`mvn verify`. Tests and resources stay in their modules' `src/test/java` and `src/test/resources` directories.

Gherkin scenarios run in `foundry-application`, grouped by behavior under `src/test/resources/feature/<name>/`.
Bindings use the matching group under `src/test/java/io/microcks/foundry/application/feature/<name>/`.

Run the incremental core mutation gate:

```shell
bash scripts/mutation.sh
```

The first run analyses the core fully. Subsequent runs reuse unchanged mutations and reevaluate changed Java code
and tests. PIT is pinned to `1.22.1`, which includes its built-in incremental history support, with the JUnit 5 plugin.
Dependabot does not upgrade PIT to `1.23+` until a separate history plugin is configured and verified.
The script invalidates history when test resources (including `.feature` files) or the root/domain/application POMs
change. A failed mutation run also discards history to avoid reusing partial results. `mvn clean` clears the cache.
The report is `foundry-application/target/pit-reports/mutations.xml`; history and its input fingerprint also live under
that module's ignored `target` directory.

The temporary `diagnostic` probe verifies this setup. It is not acceptance evidence for a product story.
Keep the Gherkin runner and mutation configuration when replacing that probe with real behavior.

Build the native Quarkus executable locally:

```shell
mvn --batch-mode --no-transfer-progress -pl foundry-app -am package -Dnative -Dquarkus.native.container-build=true
```

The native executable targets the architecture of the container builder. CI builds Linux x86_64; other platforms are not currently covered by CI.

## Where code belongs

Find the bounded context first in the [domain model](ARCHITECTURE.md#domain-model), then the layer. Packages follow `io.microcks.foundry.<layer>.<context>`; for example, the `Proposal` aggregate lives in `io.microcks.foundry.domain.proposal` and its commands in `io.microcks.foundry.application.proposal.command`.

| You are adding | Put it in | Rules |
| --- | --- | --- |
| Aggregate | `domain.<context>` | Only to protect a transactional invariant cited from the PRD; otherwise use a value object or a domain service. Reference other aggregates by identity (`ManagedApiId`), never by object. Keep `TeamSpaceId` on every aggregate. |
| Value object | `domain.<context>`, or `domain.shared` if every context needs it | Immutable `record`; validate in the constructor so an invalid instance cannot exist. |
| Domain service, algorithm | `domain.<context>`, or `domain.design` for catalog algorithms | Pure and deterministic: no clock, randomness, I/O, or framework. Take the seed and time as inputs. |
| Domain event | `domain.<context>` | Past tense, immutable `record`, carries identities only. |
| Command and handler | `application.<context>.command` | One handler changes one aggregate, persisted through the `UnitOfWork` port. |
| Query, handler, read model | `application.<context>.query` | Read the repository; add a projection only for cross-context or high-volume reads. |
| Port | `application.<context>.port` | Interface in domain language; no external type in its signature. |
| Contract used by another context | `application.<context>.api` | The only package other contexts may call. |
| Adapter | `infrastructure.<technology>` or `infrastructure.persistence.<context>` | Translates external models at the boundary (anticorruption layer). |
| HTTP resource, CLI command | `foundry-app` or `foundry-cli` | Translate input into a command or query; no business rule. |
| Plugin contract | `foundry-plugin-api` | Java platform only; no domain type; semantic versioning. |

## Naming

- Commands are imperative verbs: `GenerateExamples`, `ApproveProposal`.
- Prefer a business-specific name for queries; use `Find`, `Retrieve`, or `List` where appropriate, and `Get` only when no clearer domain term fits (e.g., `ListPendingDecisions`).
- Events are past-tense facts: `ProposalApproved`, `DivergenceDetected`.
- Identifiers are typed value objects named `<Aggregate>Id`.
- Algorithms keep the PRD identifiers and an explicit version, such as `boundary.values` v1. Changing an algorithm's output requires a new version.
- Code names are in English; user-facing text uses the message files for French and English (E1.21).

## Domain changes

- Cite the PRD requirement (`E1.x`…`E5.x`) or section in the pull request for every new aggregate, invariant, command, query, or event, and update the tables in [ARCHITECTURE.md](ARCHITECTURE.md#domain-model).
- Test domain rules without mocks. Test application handlers with in-memory port implementations. Simulate external systems (GitHub, Azure DevOps, LLM) with Microcks (PRD §11).
- Generation code must stay reproducible: the same `GenerationManifest` produces the same bytes. Add a reference-output test for any new algorithm.
- When adding an architecture rule, add a counter-example test proving it fails on a violation.

## Plugins

An enterprise algorithm implements an interface of `foundry-plugin-api` and declares it in `META-INF/services/<interface name>`.

- **JVM mode:** add the plugin JAR to the application classpath; Foundry discovers it at startup without recompiling.
- **Native mode:** include the plugin JAR in the native image build; the image must be rebuilt.

Two algorithms with the same identifier and version stop Foundry at startup. A plugin must be deterministic and pass the conformance tests shipped with `foundry-plugin-api`.

## Pull requests and dependencies

- Pull requests must pass the GitHub Actions build and architecture tests.
- Pin GitHub Actions to full commit SHAs and retain the upstream version in a comment for Dependabot.
- Dependabot manages Maven and GitHub Actions version updates. Keep the configured minimum action releases and JDK/Quarkus versions aligned with [AGENTS.md](AGENTS.md) and [ARCHITECTURE.md](ARCHITECTURE.md).

## Skraft

`skraft-config.json` defines the shared reactor component, its gates, executable Gherkin paths and publication consent.
Agent models use the plugin's standard tiers. The ignored `skraft-config.local.json` records local configuration
confirmation; each new working copy settles its own confirmation.

There is no frontend component or E2E gate yet. Skraft currently warns that its `mocking-microcks-java` skill is absent;
stories requiring external Microcks mocks need the applicable Java recipe before delivery.
