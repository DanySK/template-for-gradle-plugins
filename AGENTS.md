# Agent Instructions

## Project Scope

This repository is a template for Gradle plugin projects.

- Main build logic lives in `build.gradle.kts`.
- Project identity and root plugin management live in `settings.gradle.kts`.
- The implementation entry point is `src/main/kotlin/org/danilopianini/template/HelloGradle.kt`.
- Functional tests and template fixture coverage live under `src/test/`.

## Working Rules

- Prefer targeted changes that preserve the repository's role as a reusable template.
- Keep Gradle Kotlin DSL style consistent with the existing codebase.
- Do not hardcode dependency or plugin versions in new build logic when an existing version catalog entry or settings declaration is the correct source of truth.
- Follow the existing conventional-commit workflow when creating commits.

## Validation

- Validate changes with `./gradlew build`.
- If build-related changes affect generated wrapper files or dependency declarations, re-run `./gradlew build` after those updates.

## Version Sources

When updating versions, change them only in the files that already own them:

- `gradle/libs.versions.toml` for the version catalog and aliased plugins.
- `settings.gradle.kts` for settings-level plugin declarations.
- `gradle/wrapper/gradle-wrapper.properties` for the Gradle wrapper distribution.

## Template Maintenance Notes

- Preserve the placeholders and template-oriented metadata unless the task explicitly asks to specialize the repository.
- Keep README and test fixture expectations aligned with any structural build changes.
