RELEASING THIS PROJECT
======================

This document explains how to publish a release using the repository's GitHub Actions workflow `.github/workflows/maven-publish.yml` (it is triggered by pushing tags like `v1.2.0`). It also includes steps to run `mvn deploy` locally for testing and how to configure the required secrets.

Quick summary
-------------
- The Actions workflow triggers on git tags matching `v*` (e.g. `v1.0.0`).
- You must add repository secrets for Sonatype (Maven Central) credentials and your GPG key.
- Tag and push the repo to trigger the workflow; Actions will run `mvn -B clean deploy -P release -DskipTests`.
- The workflow additionally sets the `pom.xml` version to the tag value (stripping a leading `v`) automatically before building, so you can release by tagging only.

Checklist before publishing
---------------------------
- [ ] You have a Sonatype (OSSRH) account and the project is provisioned for publishing.
- [ ] `pom.xml` `distributionManagement` contains the repository entry for Sonatype and the server `id` matches `central` (the workflow uses `server-id: central`).
- [ ] Add the required GitHub repository secrets (see below).
- [ ] Your GPG key is ready (used to sign artifacts) and you have the passphrase.
- [ ] Rotate any credentials that have been committed in the repo history (if applicable).

Required GitHub repository secrets
---------------------------------
Add the following secrets at the repository level (Settings → Secrets and variables → Actions → New repository secret):

- `MAVEN_USERNAME`  — your Sonatype username
- `MAVEN_PASSWORD`  — your Sonatype password or API token
- `GPG_PRIVATE_KEY` — your ASCII-armored GPG private key (the workflow expects this in `secrets.GPG_PRIVATE_KEY`)
- `GPG_PASSPHRASE`  — passphrase for the GPG private key

Note: The workflow uses `actions/setup-java@v4` which supports taking `gpg-private-key` and `gpg-passphrase` from secrets and configuring Maven signing automatically.

How to prepare your local environment (Windows / PowerShell)
-----------------------------------------------------------
1) Install prerequisites
- Java 8+ and Maven
- GPG (Gpg4win on Windows is common)

2) Import your GPG private key locally (so `mvn deploy` can sign artifacts)

Open PowerShell and run:

```powershell
# Import your ASCII-armored private key into GPG
gpg --import C:\path\to\your-private-key.asc

# Optionally list keys to confirm
gpg --list-secret-keys --keyid-format LONG
```

3) Create or confirm your local Maven settings (optional for local deploy test)

Create `%USERPROFILE%\.m2\settings.xml` with a server entry that matches the `server-id` (`central`) used by the workflow. Example:

```xml
<settings xmlns="http://maven.apache.org/SETTINGS/1.0.0"
          xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
          xsi:schemaLocation="http://maven.apache.org/SETTINGS/1.0.0
                              https://maven.apache.org/xsd/settings-1.0.0.xsd">
  <servers>
    <server>
      <id>central</id>
      <username>YOUR_SONATYPE_USERNAME</username>
      <password>YOUR_SONATYPE_PASSWORD</password>
    </server>
  </servers>
</settings>
```

4) Test local deploy

From project root (PowerShell):

```powershell
cd "C:\Users\firstpc83\Desktop\Zazalng (FastAPI)\backend\Java\DBFReader"
# Run the release profile; it should build, sign and attempt to deploy to the repository configured in pom.xml
mvn -P release clean deploy -DskipTests
```

If the local deploy succeeds then the CI Action should succeed once triggered (assuming secrets and POM are correct).

Release flows
-------------
You can release in two ways. The workflow we added supports both:

A) Manual, commit-first (recommended for first releases)
-----------------------------------------------------
This is explicit: you update `pom.xml` locally to the release version (no `-SNAPSHOT`), commit the change, tag that commit and push both commit and tag.

```powershell
cd "C:\Users\firstpc83\Desktop\Zazalng (FastAPI)\backend\Java\DBFReader"
# Set release version in POM (no -SNAPSHOT)
mvn versions:set -DnewVersion=1.0.0 -DgenerateBackupPoms=false

git add pom.xml
git commit -m "chore(release): set version to 1.0.0"

# Create annotated tag
git tag -a v1.0.0 -m "Release v1.0.0"

# Push commit and tag (the tag push triggers the CI)
git push origin HEAD
git push origin v1.0.0
```

After the release completes, bump to the next snapshot for development:

```powershell
mvn versions:set -DnewVersion=1.1.0-SNAPSHOT -DgenerateBackupPoms=false
git commit -am "chore: bump to 1.1.0-SNAPSHOT"
git push origin HEAD
```

B) Tag-only release (CI updates `pom.xml` automatically)
---------------------------------------------------------
If you prefer to only create a tag, the workflow will set the POM version from the pushed tag (it strips a leading `v`). Use this flow when you want tags to be the single source of truth for versions.

```powershell
cd "C:\Users\firstpc83\Desktop\Zazalng (FastAPI)\backend\Java\DBFReader"
# Create annotated tag - workflow will set the POM version before building
git tag -a v1.0.0 -m "Release v1.0.0"
# Push the tag only
git push origin v1.0.0
```

Notes on the automated CI approach
---------------------------------
- The workflow runs a `mvn versions:set` step in the workspace before running `mvn deploy`. This updates the POM in the runner's workspace but does not commit back to the repository.
- If you need the tag's commit to contain the release version inside `pom.xml` for provenance, use the manual commit-first flow.
- The workflow strips an optional leading `v` from the tag name so `v1.2.3` becomes `1.2.3`.

How to trigger the GitHub Actions workflow (use git tag)
-------------------------------------------------------
The workflow will run when you push an annotated tag matching `v*`.

Create a tag and push it (PowerShell):

```powershell
cd "C:\Users\firstpc83\Desktop\Zazalng (FastAPI)\backend\Java\DBFReader"
# Create annotated tag
git tag -a v1.0.0 -m "Release v1.0.0"
# Push only the tag (this triggers the workflow)
git push origin v1.0.0
```

Alternatively, you can create a Release on GitHub UI which creates the tag for you.

What the workflow does (high level)
----------------------------------
- `actions/checkout@v4` to fetch the code
- `actions/setup-java@v4` to configure Java and to provide Maven server credentials + GPG private key from secrets
- `mvn -B clean deploy -P release -DskipTests` to build, sign and deploy artifacts
- `softprops/action-gh-release@v2` to create a GitHub Release with autogenerated release notes
- A follow-up job generates javadoc and publishes it to GitHub Pages (via peaceiris/actions-gh-pages)

Important: the `server-id` used by the action is `central`. Your `pom.xml` should use the same id in `distributionManagement`.

Security notes (you must do this now if credentials were ever committed)
------------------------------------------------------------------------
- If any real credentials were committed, rotate them immediately. Remove them from history if necessary (careful: rewriting history affects collaborators).
- Never hard-code credentials in workflow files or source. Use GitHub Secrets.
- Limit access to repository secrets and use organization-level secrets if appropriate.

Storing your GPG key in GitHub Secrets
-------------------------------------
Store your ASCII-armored private key in `GPG_PRIVATE_KEY`. If you prefer to store it base64-encoded, you can use this PowerShell to create a base64 value to paste into the secret value field:

```powershell
# Create Base64 of your key file (single line)
[Convert]::ToBase64String([IO.File]::ReadAllBytes("C:\path\to\your-private-key.asc"))
```

If you store a base64 value you'll need to update the workflow to decode it before import; the current workflow expects the raw ASCII-armored string in `secrets.GPG_PRIVATE_KEY` and `actions/setup-java` will handle it.

Debugging and common failures
-----------------------------
- Check the Actions run logs in the GitHub UI (Actions tab → select the workflow run). Expand the `Set up Java` and `Build and deploy` steps to see output.
- Common problems:
  - Wrong server id in `pom.xml` vs workflow: ensure `distributionManagement` server id is `central`.
  - Missing or invalid GPG key / passphrase: import the key locally and ensure it can sign.
  - Sonatype account not configured for the project: ensure you have access to the staging repository and your project coordinates (groupId/artifactId) are registered.

If a build fails during `deploy` the action output will include the Maven failure and stacktrace. Save the full log and attach it when asking for help.

Rollback / revoke advice
------------------------
- If the GPG private key or a Sonatype credential was leaked, revoke/rotate them immediately.
- Rotate any tokens stored as `MAVEN_PASSWORD`.

Want me to update the workflow?
-------------------------------
I can:
- Update `.github/workflows/maven-publish.yml` to accept base64-encoded GPG key (if you prefer that flow).
- Add a small guard step verifying `pom.xml` `distributionManagement` server id matches the action `server-id` value.
- Add a `dry-run`/`verify` job that runs `mvn -P release -DskipTests=true -DperformRelease=false` or similar for a non-pushing build.

Tell me which of these (if any) you want me to implement next. If you're ready to try the git workflow now, create the repository secrets, tag the repo (as shown above) and push the tag — then paste the Actions run URL or the run logs here and I will help triage any failures.
