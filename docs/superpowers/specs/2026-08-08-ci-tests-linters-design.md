# CI: Tests + Linter — Design

## Goal

Add a GitHub Actions workflow that runs the test suite and a linter/formatter check on every
pull request and every push to `main`, so regressions and style drift are caught before merge.

## Scope

- One new workflow file: `.github/workflows/ci.yml`.
- Add a formatter to the project — none exists yet. `com.diffplug.spotless` (Gradle plugin),
  configured with `googleJavaFormat()`, applied to `src/main/java` and `src/test/java`.
- No changes to application code, migrations, or Docker setup.

## Triggers

```yaml
on:
  pull_request:
  push:
    branches: [main]
```

## Jobs

Two jobs, run in parallel, both on `ubuntu-latest`:

### `lint`

1. `actions/checkout`
2. `actions/setup-java` — distribution `liberica` (matches the Dockerfile's
   `bellsoft/liberica-openjdk` base image), Java version `25`.
3. `gradle/actions/setup-gradle` — provides Gradle dependency/build caching automatically.
4. `./gradlew spotlessCheck`

### `test`

1. `actions/checkout`
2. `actions/setup-java` — same as above (`liberica`, `25`).
3. `gradle/actions/setup-gradle`
4. `./gradlew test`

`ubuntu-latest` ships Docker preinstalled, so Testcontainers (Postgres) works with no extra
setup — this matches how tests already run locally per `CLAUDE.md` (`./gradlew test` requires
Docker).

## build.gradle changes

Add the Spotless plugin:

```groovy
plugins {
    id 'java'
    id 'org.springframework.boot' version '4.1.0'
    id 'io.spring.dependency-management' version '1.1.7'
    id 'com.diffplug.spotless' version '<pinned-version>'
}

spotless {
    java {
        target 'src/main/java/**/*.java', 'src/test/java/**/*.java'
        googleJavaFormat()
    }
}
```

The exact plugin version will be resolved and security-checked (OSV vulnerability check,
publication date, red-flag review) per the project's dependency-security rules before it is
added, as a separate step requiring explicit user confirmation. This design doc intentionally
does not pin a version yet.

## Out of scope

- Auto-formatting on push (`spotlessApply` is a local/manual dev command, not run by CI).
- Deployment, Docker image publishing, or release workflows.
- Matrix builds across multiple Java versions — the project targets Java 25 only (per
  `build.gradle` toolchain and Dockerfile).
- Checkstyle/PMD or other static analysis tools — Spotless (formatting) was chosen as
  sufficient for now; can be added later as a separate change.

## Testing / verification

- Push the branch and confirm both `lint` and `test` jobs run and pass on the PR.
- Deliberately introduce a formatting violation locally and confirm `spotlessCheck` fails
  (verified locally with `./gradlew spotlessCheck`, not committed).
