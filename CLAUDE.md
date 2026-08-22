# Spring Dotfile Manager

A Spring Boot CLI (`sdfm`) that manages dotfiles by symlinking files/directories from a dotfile repository to locations on the local filesystem. Dotfiles are declared via `.dotfiles` marker files (multi-document YAML) placed alongside the files they describe, with support for `{HOME}`/`{NAME}` format specifiers and per-platform (`linux`/`darwin`/`win32`) overrides. See `README.md` for full usage docs.

## Build & Run

Use the Maven wrapper (`./mvnw`), not a system-installed `mvn`:

```bash
./mvnw clean package
java -jar target/spring-dotfile-manager-<version>.jar ...
# or
./mvnw spring-boot:run -Dspring-boot.run.arguments='...'
```

Current version is tracked in `pom.xml` (`<version>`), currently `1.2.3`. Requires OpenJDK 26 (`java.version` in `pom.xml`).

`bin/build.sh` and `bin/install.sh` wrap the Maven build and install the built jar + the `bin/sdfm` wrapper script into `~/.local/bin`, so the `sdfm` command can be run directly once installed.

## Testing

```bash
./mvnw test
```

On Windows, `./mvnw test` must be run with Developer Mode enabled or from an elevated (Administrator) terminal — several `FileServiceUnitTests` cases create real symlinks via `Files.createSymbolicLink`, which requires `SeCreateSymbolicLinkPrivilege`. This is treated as a baseline requirement for developing/testing on Windows, not something the test suite works around, since the application itself requires the same privilege at runtime (see the Windows note in `README.md`).

JUnit 5 + Mockito (`MockitoExtension`). Test classes mirror the main package structure 1:1 under `src/test/java/...`, named `<ClassName>UnitTests.java` (e.g. `DotfileServiceImpl` -> `DotfileServiceUnitTests`). Tests favor constructor injection with mocked collaborators set up in `@BeforeEach`, and method names follow `test<Method>_<scenario>` (e.g. `testGetAllDotfileMarkerPaths_readConfigThrowsIOException`).

## Project Structure

Base package: `app.jhg.spring_dotfile_manager`.

- **`commands/`**: Picocli command classes (`InitCommand`, `RelinkCommand`, `UnlinkCommand`, `ListCommand`, `GetConfigCommand`, `SetConfigCommand`, `RootCommand`, `PicocliRunner`). One class per subcommand; `RootCommand` is the top-level entry, `PicocliRunner` wires Picocli into Spring Boot's `CommandLineRunner`.
- **`config/`**: Spring `@Configuration` and Picocli mixins — `SDFMConfiguration` (app config bean wiring), `VersionProviderConfiguration` (CLI version string), `DebugMixin`/`DotfileRepoPathMixin`/`KeyMixin` (shared Picocli options injected into multiple commands). `KeyMixin` backs the repeatable `--key` option on `RelinkCommand`/`UnlinkCommand` (`List<String> keys`, may be passed multiple times) — see "Key-scoped relink/unlink" below.
- **`service/`**: Business logic behind an interface + `Impl` pair per concern — `ConfigService`/`ConfigServiceImpl` (reads/writes the app's own YAML config, e.g. `dotfile-repo-path`, `allow-post-install-scripts`), `DotfileService`/`DotfileServiceImpl` (scans for `.dotfiles` markers, resolves symlink targets), `FileService`/`FileServiceImpl` (low-level filesystem/symlink operations, globbing), `SubprocessService`/`SubprocessServiceImpl` (runs external commands via `ProcessBuilder` with a timeout, returning a `SubprocessResult`), `PostInstallService`/`PostInstallServiceImpl` (discovers and runs post-install scripts under `post-install/` in the dotfile repo; see below).
- **`model/`**: `DotfileMarkerModel` (one entry from a `.dotfiles` YAML document, including platform overrides), `SDFMConfigModel` (the app's own config file shape), `SubprocessResult` (exit code + captured output from `SubprocessService`), and `PostInstallScriptResult` (`success`, `message`, and the `script` path — one per post-install script run).
- **`util/`**: `FormattingUtils` — shared string formatting (e.g. `{HOME}`/`{NAME}` specifier resolution).
- **`resources/application.yaml`**: Spring Boot app config. `resources/META-INF/additional-spring-configuration-metadata.json`: metadata for custom `application.yaml` properties (IDE autocomplete).
- **`bin/`**: `build.sh`, `install.sh`, `sdfm` — shell scripts for building/installing outside of Maven directly, and the thin wrapper script that invokes the installed jar.

## Post-install scripts

`relink` runs post-install scripts after symlinking all dotfiles, via `PostInstallService.runPostInstallScripts()`:

- Gated entirely by the `allow-post-install-scripts` config flag (`ConfigService.readAllowPostInstallScripts()`). If disabled, `runPostInstallScripts()` returns an empty list immediately without touching the filesystem or running anything — this is a trust-boundary check, not just a "don't execute" check.
- **Not supported on Windows.** Checked immediately after the `allow-post-install-scripts` gate: if the current platform (`PostInstallServiceImpl`'s constructor-injected `os.name`, resolved via `FormattingUtils.getResolvedOsName`) is `WIN32`, `findPostInstallScripts()`/`runPostInstallScripts()` log a warning and return an empty list without touching the filesystem — same short-circuit shape as the disabled-flag case, checked second. This exists because scripts are run via `bash`, which isn't available on Windows by default; there's currently no PATH-detection fallback (e.g. for git-bash) — it's a hard platform gate.
- Scripts are discovered recursively under `post-install/` at the root of the dotfile repo, matching `*.sh` (glob pattern is `spring-dotfile-manager.post-install-glob-pattern` in `application.yaml`, default `post-install/**/*.sh`).
- Discovered paths are sorted (`Comparator.naturalOrder()` on `Path`, applied to a mutable copy since `FileService.glob()` returns an immutable list) so execution order is deterministic — e.g. `01-foo.sh` runs before `02-bar.sh`.
- Each script is run as `bash <script>` with `cwd` set to the script's own parent directory.
- Per-script failures (non-zero exit code, or any of `IOException`/`InterruptedException`/`ExecutionException`/`TimeoutException` from `SubprocessService.executeCommand`) are captured as a failed `PostInstallScriptResult` and do **not** stop the remaining scripts from running. `InterruptedException` specifically restores the thread's interrupt flag (`Thread.currentThread().interrupt()`) before continuing, so a later blocking call on the same thread will still observe the interruption.
- Failures from `configService.readDotfileRepoPath()`/`fileService.glob()` (i.e. before any script runs) propagate as `IOException` out of `runPostInstallScripts()`, unlike per-script failures.
- `RelinkCommand` logs each result and sets its own exit code to `1` if any script failed (or if `runPostInstallScripts()` throws `IOException`), without aborting the command.

## Key-scoped relink/unlink

`relink` and `unlink` both accept a repeatable `--key` option (`KeyMixin`) to restrict the operation to specific dotfiles instead of every dotfile in the repository:

- Each `DotfileMarkerModel` has a `key`, which defaults to its `name` unless a `key` field is explicitly set in the marker YAML.
- For each `--key` value given, the command looks up matching markers via `DotfileService.getMarkersByKeyForCurrentSystem(key)` (matches on `key`, filtered to markers applicable to the current platform). If a key matches zero markers, or more than one (ambiguous), the command logs an error and exits `1` **without performing any relink/unlink** — this check runs for every requested key before any linking happens, so one bad key aborts the whole scoped operation.
- Repeated `--key` values are deduplicated (order-preserving) before lookup, so passing the same key twice does not relink/unlink it twice.
- With no `--key` given, both commands fall back to operating on `DotfileService.getAllDotfileMarkerModels()` (all dotfiles), as before.
- `RelinkCommand` only offers to run post-install scripts when `--key` was not given at all — a scoped relink is assumed to be narrower than "just finished setting up," so it skips the repo-wide post-install prompt.
- `ListCommand` warns (via `findDuplicateKeys`) when multiple platform-applicable dotfiles share a key, since a scoped relink/unlink with that key would refuse to run until resolved.

## Conventions

- Every service has an interface (`XService`) and a single implementation (`XServiceImpl`); commands and other services depend on the interface type.
- Picocli `@Command` classes live in `commands/`; shared CLI options (like `--dotfile-repo-path` or a debug flag) are factored into mixins under `config/` rather than duplicated per command.
- Platform-specific behavior is dispatched on the OS name string injected via `@Value("${os.name}")` into the constructor (see `DotfileServiceImpl`, `ConfigServiceImpl`) rather than `System.getProperty` calls scattered through the code — makes it mockable/testable.
- Lombok is used (see `pom.xml` dependency + annotation processor wiring) — prefer Lombok annotations over hand-written boilerplate (getters/setters/constructors) on model classes.
