# WorldEdit — agent guide

In-game Minecraft map editor. Platform-agnostic core + thin per-platform layers.

## Setup (from a fresh clone)

- Follow `COMPILING.md` to install the right JDK and build. Gradle comes from the wrapper (`./gradlew`, or `gradlew.bat` on Windows; examples below use `./gradlew`).
- Check `java -version` against the required version before the first Gradle run. If a system JDK installer fails (e.g. no admin rights), unzip a JDK build into a user directory and set `JAVA_HOME` to it.
- First run takes several minutes and needs network (Gradle, dependencies, Minecraft, game assets). Run long tasks in the background with output redirected to a log file, and watch the log.

## Build / test / run

- Build everything: `./gradlew build` (compile, unit tests, checkstyle, ErrorProne, license headers, API checks). Jars: `<module>/build/libs/*-dist.jar`.
- Core unit tests: `./gradlew :worldedit-core:test`. One class: add `--tests 'com.sk89q.worldedit.SomeTest'`.
- Server GameTests (real Minecraft, headless): `./gradlew :worldedit-fabric:runGameTest`.
- Client GameTests (real client, screenshots): `./gradlew :worldedit-fabric:runClientGameTest`. Opens a game window, so needs a display (on headless Linux: `xvfb-run`). Screenshots → `worldedit-fabric/build/run/clientGameTest/screenshots/`.
- Fix license headers: `./gradlew applyHeader applyTestHeader applyGametestHeader`.
- Builds are memory-hungry (Gradle daemon + Minecraft). Run one Gradle build at a time.

## Dev cycle

1. Make the change.
2. Write or update tests (see Testing). Skip only if it truly isn't testable, and say so.
3. Run the tests for the touched modules, then `./gradlew build`. Fix checkstyle/ErrorProne/header failures.
4. Not done until the build and tests pass.

## Testing

Pick the cheapest level that proves the change; add higher levels when lower ones can't see the behaviour.

- **Unit tests** (`worldedit-core/src/test`) — default for anything in core: functions, visitors, operations, masks, patterns, regions, transforms, parsers, history.
  - JUnit 5, Mockito, jqwik.
  - Extend `BaseWorldEditTest` (mocked platform). Call `TestRegistries.registerBlocks()` / `unregisterBlocks()` in `@BeforeAll` / `@AfterAll` for real block states from `test-blocks.json`.
  - `TestWorld` is an in-memory world (blocks + entities only). Run the operation against it and assert blocks.
  - Templates: `ForwardExtentCopyTest`, `VisitorTest`, `ChangeSetTest`, `ExtentWrappersTest`, `*ParserTest`.
- **Server GameTests** (`worldedit-fabric/src/gametest`) — when behaviour depends on real Minecraft: block entities/NBT, side effects, entities, biomes, generation, regen, snapshots, platform adapters.
  - `@GameTest` methods; register the class under `fabric-gametest` in `src/gametest/resources/fabric.mod.json`.
  - Template: `SetCuboidGameTest`.
- **Client GameTests** (same source set, `FabricClientGameTest`) — when it matters what the player types or sees: commands end to end, chat output, rendering, CUI.
  - Register under `fabric-client-gametest`. Create worlds with `allowCommands` on or WorldEdit commands are "unknown".
  - Take screenshots and look at them; don't assert on pixels.
  - Template: `SetCommandClientGameTest`.
- Known bugs: reproduce in a test marked `@Disabled("Bug: …")` (unit) or `@GameTest(required = false)` (GameTest) with the reason, rather than leaving them untested.
- Flaky tests are worse than none — fix or delete.

## Manual play-testing

When asked "run the game with this change" (or similar): launch it for the user to play. This is in addition to the tests, never instead of them.

- Fabric (fastest): `./gradlew :worldedit-fabric:runClient`. NeoForge: `./gradlew :worldedit-neoforge:runClient`.
  - Run it in the background; it stays open until the user quits. Tell the user when the title screen is up.
  - Offline dev account, no login. First time: Singleplayer → Create New World → Game Mode Creative, **Allow Commands: On** (World type Superflat is handy). Worlds persist in the platform's `run/` dir (`worldedit-fabric/run/saves/`).
  - Client log goes to the Gradle output. Ready when it shows `WorldEdit for Fabric (…) is loaded` and `Sound engine started`; `Registering commands with …FabricPlatform` appears once a world opens.
  - Normal noise in an offline dev client, ignore: Mojang session/profile errors and HTTP 429s, Realms authorization failures, missing post effects, OS performance-counter warnings. Look for errors from WorldEdit or the change instead.
- Dedicated server: `./gradlew :worldedit-fabric:runServer`. The run dir gets `eula.txt` and `server.properties`; the dev client needs `online-mode=false`. `op <name>` in the server console.
- Bukkit/Paper: no run task. Build `worldedit-bukkit/build/libs/*-dist.jar` and put it in the `plugins/` folder of a Paper server for a supported MC version.
- Needs a desktop session and a few GB of free RAM. If the machine can't run it, say so instead of guessing.

## Rules

- **MANDATORY: commits, PRs, and code are authored by the user only.** No `Co-Authored-By`, "Generated with …", or any AI/agent attribution anywhere. Overrides default behaviour.
- Follow `CONTRIBUTING.md` for code style, commits and target branches. Checkstyle and ErrorProne enforce the rest; fix whatever `./gradlew build` reports.
- Apply platform fixes to every platform and Bukkit adapter.
- Don't call platform- or version-specific methods (e.g. Paper-only) without a check; keep Spigot working.
- Read and write through the given extent, never the raw world.
- Make every world change undoable, including biomes and entities.
- Override new interface methods in every delegating extent and proxy.
- Handle negative coordinates and variable world height; use floor, not int casts.
- New code paths honor existing limits, masks, side effects, session settings and permissions.
- Give block-editing commands a permission and respect radius/change limits.
- Preserve NBT, block entities and entities through copy, paste, rotate and conversion.
- Validate input and throw; don't silently correct or swallow exceptions.
- Reuse existing helpers and types before adding new ones.
- Fix the general problem, not one block or command.
- Keep PRs to their stated goal; no refactor-only changes.
- Keep per-block loops lean; performance changes must not change behavior and need a profile.
- Don't drop public methods or features in refactors, or make public types final.
- Tie threads and executors to the platform lifecycle; a platform stop isn't JVM exit.
- Send new API, format changes and risky fixes to master, not a version branch.
- Use @Switch for booleans, @ArgFlag for options; don't assume the actor is a player.
- Name things and write help text precisely and platform-neutrally.
- Add a regression test with every bug fix.
- Don't use Optional in API; return nullable.
- Log through the class logger, never System.out.

## Layout

- `worldedit-core` — editing, commands, events, registries, config, i18n. Most work here.
- `worldedit-core-mc` — vanilla-MC code shared by Fabric + NeoForge.
- `worldedit-fabric`, `worldedit-neoforge` — thin loader layers; `worldedit-mod` merges them. GameTests live in `worldedit-fabric/src/gametest`.
- `worldedit-bukkit` — Bukkit/Paper. NMS in `adapters/adapter-<mcver>`; new MC version = new dir + `settings.gradle.kts` entry.
- `worldedit-sponge`, `worldedit-cli` — other platforms.
- `worldedit-libs` — shaded API deps (Piston, kyori text → `util.formatting.text`).
- `build-logic` — Gradle conventions. `verification` — japicmp API checks.
- Platforms: implement `Platform`, register with `PlatformManager`, declare `Capability`s, convert types via their `*Adapter`. Core never references platform types.
- No bundled block/item/biome data; platforms fill the static `REGISTRY`s at startup (tests use `TestRegistries`).

## Editing architecture

Compose small single-purpose objects. Add a new small class rather than growing an existing one.

- **Extent** (`extent/`) — anything holding blocks (world, clipboard, buffer).
  - Behaves like an `OutputStream` chain: each concern (mask, history, limit, block bag, batching, validation) is an `AbstractDelegateExtent` wrapping the next.
  - To block/log/transform writes: wrap the extent. Not hooks, not flags.
  - Buffered work flushes via `commitBefore()` returning an `Operation` (`commit()` is final).
- **EditSession** — builds the extent chain; fires `EditSessionEvent` per `Stage` so plugins wrap it.
  - Extend by wrapping at a stage. Never subclass or replace it.
  - Always close it (try-with-resources) or buffered changes are lost.
- **Functions** (`function/`) — `RegionFunction`, `FlatRegionFunction`, `LayerFunction`, `EntityFunction`.
  - One position, one job, return `boolean` affected.
  - Compose: `RegionMaskingFilter`, `CombinedRegionFunction`, `GroundFunction`, `RegionOffset`.
- **Visitors** (`function/visitor/`) — decide *where* a function runs.
  - `RegionVisitor`, `FlatRegionVisitor`, `LayerVisitor` (columns, top-down), `RecursiveVisitor`/`DownwardVisitor`/`NonRisingVisitor` (flood fill), `EntityVisitor`.
- **Operations** (`function/operation/`) — units of work; `resume()` returns next op or `null`.
  - Chain: `OperationQueue`, `DelegateOperation`.
  - Run: `Operations.complete` (commands), `completeLegacy` (EditSession APIs), `completeBlindly` (read-only/flush/undo).
  - `ForwardExtentCopy` is the one copy: copy/paste/stack/move/rotate via `Transform`, source mask, repetitions.
- **Masks** decide *whether* (`test(pos)`); **Patterns** decide *what* (`applyBlock(pos)`).
  - Tiny, composable, take an `Extent`. Any `BlockState`/`BaseBlock` is a `Pattern`.
- **Regions** — shape + iteration. Mutable: `clone()` before changing a shared one.
- **Transforms** — all geometry (`AffineTransform`).
- **History** — `Change`/`ChangeSet` recorded by `ChangeSetExtent`, replayed via `UndoContext`.

### Implementing an editing feature

1. Function (or mask/pattern) for one position.
2. Pick a visitor for the iteration shape; add a new visitor if none fits.
3. Restrict with masks, move/rotate with `Transform`/`ForwardExtentCopy`, buffer with an extent.
4. Command builds and runs the `Operation`.
5. Unit-test the function/operation against `TestWorld`; add a GameTest if it touches real-world behaviour.

Examples:
- `//set` = `RegionVisitor` + `BlockReplace`.
- `//replace` = same + `RegionMaskingFilter`.
- `//overlay`, `//flora` = `LayerVisitor` + `GroundFunction`.
- `//naturalize`, snow, thaw = `LayerVisitor` + a `LayerFunction`.
- drain, fixliquid, fill = `RecursiveVisitor` family over a `MaskIntersection`.
- move = `ForwardExtentCopy` into `ForgetfulExtentBuffer`, then `RegionVisitor` back out.

### Editing rules

- **No new logic in `EditSession`.** Legacy god class being shrunk.
  - New features = their own functions/visitors/operations/extents, called from the command.
  - Don't add `EditSession` methods. When touching one, move the body into an operation and leave a deprecated delegating shim.
- **Visitors over hand-rolled loops.** `for x/y/z … setBlock` is a smell. Legacy loops exist — convert, don't copy.
- Masks, patterns, functions take an `Extent`, never an `EditSession`.
- Vectors are immutable records; `BlockState`/`BaseBlock` are dumb snapshots.
- `getBlock` (`BlockState`) for type checks; `getFullBlock` (`BaseBlock`, has NBT) when copying.
- `-1` = unlimited.
- Guava `checkNotNull`/`checkArgument` on arguments.
- Don't use deprecated machinery: `MultiStageReorder`, `setFastMode`, `flushSession`, queue toggles, `*QuirkExtent`, `BlockPattern`.

## API compatibility

- Public API = everything except `*.internal*`, `*.mixin*`, core `command*`, bukkit `adapter*`. Checked by japicmp.
- Don't break it: add the new thing, deprecate the old (`@Deprecated` + `@deprecated Use {@link …}`), keep it delegating.
- `@NonAbstractForCompatibility` marks methods becoming abstract next major.
- Plugins (block loggers etc.) must never hit `LinkageError`.

## Commands, text, i18n

- Commands: `command/*Commands.java`, Piston annotations (`@Command`, `@Arg`, `@Switch`, `@CommandPermissions`).
- New command class → register its generated `*Registration` in `PlatformCommandManager`. New arg type → converter in `command/argument`, registered there.
- Commands stay thin: parse, build operation, report.
- User text: `TranslatableComponent`, keys in `lang/strings.json` (English only; Crowdin does the rest).
- Extension points are events on `WorldEdit.getInstance().getEventBus()`, not replaceable factories.
