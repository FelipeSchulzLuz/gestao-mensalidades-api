CI/CD workflows overview

This folder contains CI/CD workflows for the repository. The goal is to keep a clear separation between fast PR checks and heavy scans or deployment flows.

Workflows

- `pr-checks.yml` (PR fast checks)
  - Purpose: quick feedback on pull requests.
  - Runs: `on: pull_request` (opened/synchronize/reopened)
  - Steps: validate PR title/size, reusable `build-and-test` (unit tests, jacoco), lightweight static checks (checkstyle, spotbugs), Sonar quality gate polling (optional)
  - Note: SARIF/uploads are guarded to be fork-safe.

- `ci.yml` (CI pipeline)
  - Purpose: build/test matrix, integration tests, produce artifacts and Docker image.
  - Runs: `on: push` (master/develop) and `pull_request`
  - Steps: build+test with services (Postgres/Redis), code-quality job (dependency check + Trivy), docker build/push.

- `security-scan.yml` (Scheduled heavy security scans)
  - Purpose: run heavy/long-running scans (Trivy image/fs, CodeQL, full dependency checks).
  - Runs: scheduled weekly and manual dispatch only (to avoid slowing PRs or pushes).
  - Steps: Trivy, CodeQL, TruffleHog, and upload SARIF artifacts.

- `sonarcloud.yml` (Sonar Cloud integration)
  - Purpose: SonarCloud analysis. Prefer SonarCloud Automatic Analysis (GitHub App). This workflow builds and indicates where to view results.
  - Runs: push/pull_request/workflow_dispatch (kept for historical reasons; consider removing if Automatic Analysis via SonarCloud App is enabled).

- `cd.yml` (Deploy)
  - Purpose: Deploy to staging and production using SSH and Docker Compose.
  - Runs: `push` to `master`, tags `v*.*.*`, or manual dispatch with environment input.

Guidelines

- Fast feedback belongs to PR workflows. Keep PR builds < 10 minutes when possible.
- Heavy scans and vulnerable network calls (NVD, third-party scanners) should be scheduled or run on push to main only.
- Reuse `build-and-test.yml` for consistent behavior across PRs and CI.
- Secrets required:
  - `SONAR_TOKEN` (optional) — SonarCloud API token for PR quality gate polling
  - `NVD_API_KEY` (optional) — NVD API key for OWASP Dependency Check
  - `DOCKER_USERNAME`, `DOCKER_PASSWORD` — for publishing images
  - `STAGING_SSH_KEY`, `PRODUCTION_SSH_KEY`, and host/user secrets for deploy

If you want, I can proceed to:
- Move other heavy checks off PRs (even more aggressive).
- Add job-level `permissions` tightening for each job that requires write access.
- Add a small GitHub Action to auto-notify maintainers when `NVD_API_KEY` or critical secrets are missing.

---
Changes applied automatically by the CI assistant were committed to `master`. If you'd like any behavior adjusted, tell me which workflow and I'll change it.