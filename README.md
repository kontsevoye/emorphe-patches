# emorphe-patches

Custom patch source for use with Morphe. The repository is intended to host
small targeted patches that are not part of the upstream source.

## About

Current patches:

- `YouTube Music proxy`

`YouTube Music proxy` injects process-wide HTTP or SOCKS proxy setup into
YouTube Music startup and disables Cronet QUIC so HTTP/3 does not bypass the
proxy path.

The patch is intended for YouTube Music `8.47.56`.

## Patches List

<!-- PATCHES_START EXPANDED -->

The generated patch list will be inserted here by the release workflow.

<!-- PATCHES_END -->

## Usage

After a GitHub release is published, add this repository to Morphe as a patch
source:

```text
https://morphe.software/add-source?github=kontsevoye/emorphe-patches
```

Or add the GitHub repository URL manually:

```text
https://github.com/kontsevoye/emorphe-patches
```

## Patch Options

`YouTube Music proxy`:

- `proxyType`: `SOCKS` or `HTTP`
- `proxyHost`: default `127.0.0.1`
- `proxyPort`: default `1080`
- `proxyUsername`: optional
- `proxyPassword`: optional

This version stores proxy values as patch options. Changing proxy settings
requires repatching the app.

## Development Flow

Development happens in feature branches based on `dev`. Feature branches are
merged into `dev`, and `dev` is merged into `main` when ready for release.
Pushing to `dev` or `main` runs the release workflow. Semantic commit messages
are used for versioning.

## Development Environment

This project uses a Nix devshell for local development. Please use it for
builds and checks so Java, Node.js, Android SDK, and GitHub Packages access are
consistent with CI.

```bash
nix develop
```

The devshell provides JDK 17, Node.js, GitHub CLI, `jq`, and `curl`. It also
exports `GITHUB_ACTOR` and `GITHUB_TOKEN` from the active `gh` login so Gradle
can download the Morphe Gradle plugin from GitHub Packages.

If you use `direnv`, allow the committed `.envrc` once:

```bash
direnv allow
```

After that, entering the repository automatically loads the same Nix devshell.

Before the first build, make sure the GitHub CLI token has package access:

```bash
gh auth refresh -h github.com -s read:packages
```

## Build

Run all local checks with:

```bash
make check
```

The release workflow generates `patches-bundle.json`, `patches-list.json`, and
the `.mpp` bundle that Morphe downloads.

## Contributing

See [CONTRIBUTING.md](CONTRIBUTING.md).

## License

Licensed under the [GNU General Public License v3.0](LICENSE). See
[NOTICE](NOTICE) for additional terms inherited from the Morphe template.
