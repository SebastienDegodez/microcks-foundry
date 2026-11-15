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

The repository configures `.githooks` as its local Git hook path during Maven's `validate` phase. Only `pre-push` runs the formatting check; run `mvn verify` once after checkout to activate it. To skip hook-path configuration for a Maven invocation, use `-Dgit.hooks.skip=true`; this does not disable a hook path already configured locally.

Run the full module build and test suite:

```shell
mvn --batch-mode --no-transfer-progress verify
```

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
