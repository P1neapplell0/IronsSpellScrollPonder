# Development fixtures and real-client preview tests

**Real-client smoke coverage is recorded below; no full visual certification is claimed.**
Compilation, dependency resolution, static review and bytecode checks are not rendering tests.
The attempted Forge Wind's client run on October 23, 2026 stopped in Early
Display/GLFW with `Failed to find a primary monitor`. No spell was tested.
Missing or unfinished results must never be interpreted as success.

A subsequent NeoForge Wind's run did enter a real world and reproduced an
`iron_slash` render crash: the server-only `dash_stop` helper has no registered
entity renderer. Both branches now temporarily exclude unrenderable helpers
from Ponder's drawing pass, preserving their projection state. After the fix,
all seven Wind's spells completed at minimum and maximum level (14 cases),
with screenshots and clean automatic exit. Selected screenshots were inspected;
complete visual/mechanical and dedicated-server certification is still pending.

Forge also subsequently entered a real world with Legendary and Legendary
Monsters: `annihilation_resonance`, `flame_eater` and `overgrown_shockwave` each
completed at minimum/maximum level (six cases). Server logs confirmed the
`GuardedAdapter` path. The initial monitor error was not a persistent inability
to run Forge. See [recorded results](test-results/) for machine-readable evidence.

On October 24, Forge's `all` profile completed maximum-level cases for all 152
enabled registered spells: **149 rendered-complete, three Illage safety
restrictions, zero remaining cast failures**. The restrictions are upstream API
incompatibilities, not working summon effects. Tests found and fixed Legendary's
entity-iterator crash, book-exclusive cast prerequisites, FakePlayer mana
clamping and Thoth's armor requirement; see [runtime findings](RUNTIME_FINDINGS.md).
Selected Thoth screenshots were inspected and showed the remnant and particles.
The isolated T.O profile subsequently completed all **68 enabled registered
spells**, using Cataclysm 3.16 and Lionfish 2.7. BielGG's patch-yield log was
observed, but its own early config read prevented startup: zero preview cases,
not a successful interoperability pass.

NeoForge **21.1.248** then exercised all **177 enabled registered spells**:
176 completed in the fresh full run; Switcheroo failed during early target
preparation. After moving ownership setup to `beforeCast`, an identical-fixture
resume reran that failure successfully, yielding 177 rendered-complete rows.
The fixture was restarted from scratch after adding Hazen's missing Prometheus
and Cobweb dependencies; no successful rows from the earlier missing-API
environment were reused. Changed target rules also receive fresh low/high
targeted retests. See the JSON execution notes and [visual notes](VISUAL_REVIEW.md).
The fresh NeoForge five-spell min/max suite completed all ten cases. Its
master-off Legendary suite completed two generic cases with zero optional
provider/support classes observed in JVM class-loading output. Forge recorded
the same master-off isolation result separately.
The final Forge four-spell min/max retest completed all eight cases. Its first
launch stopped at transient primary-monitor detection before mod loading; the
serial retry entered a world and produced the finished report. Failed launches
are never counted as completed cases.

## Isolation and profiles

Normal builds add no add-on dependencies or test source roots.
`-PcompatTestProfile=...` enables development runtime fixtures and the appropriate
loader's client runner. Every JAR task excludes `com/p1nero/iss_ponder/dev/**`,
including stale test classes. Fixtures are never bundled or required by releases.

`gradle/compat-test-lock.json` pins official CurseForge filenames, project/file
IDs and SHA-256 hashes. The preparation script supports resumable downloads to
`$GRADLE_USER_HOME/caches/iss-ponder-compat/maven` (default `~/.gradle/...`).
Configs/worlds/logs live in `run/compat/<sorted-profile-names>-<on|off>`.
It never copies an installed modpack, changes normal `run/config`, launches
Minecraft or accepts an EULA. ForgeGradle maps the Forge runtime JARs;
ModDevGradle uses `localRuntime` on NeoForge.

| Profile | Forge 1.20.1 | NeoForge 1.21.1 | Additional fixtures |
| --- | --- | --- | --- |
| `wind` / `smoke` | Wind's 1.0.3 | Wind's 1.0.5 | Normal project dependencies |
| `legendary` | Legendary 0.4.1 | Legendary 0.3.2 | Legendary Monsters 2.2.3, including Forge's undeclared code dependency |
| `cataclysm` | Spellbooks 1.2.9 | Spellbooks 1.1.14 | Cataclysm, Lionfish API, AzureLib (Forge 3.1.24); Ace's Spell Utils on NeoForge |
| `familiars` | Familiars 4.1.0 | Familiars 5.0 | FamiliarsLib; Forge GeckoLib >=4.8.4; NeoForge >=21.1.244 for FamiliarsLib 2.0 |
| `geomancy` | Geomancy 2.0.0 | Geomancy 1.1.0 | GTBC Spell Lib 1.5.0 and Mowzie's Mobs (undeclared upstream dependency) |
| `traveloptics` | T.O 6.3.0 | Excluded: deprecated | Separate Cataclysm 3.16, Lionfish API 2.7, Alex's Caves, Citadel, AttributesLib, Placebo |
| `alexscaves` | Spellbooks 1.2.0 | Spellbooks 1.2.1 | Alex's Caves (unofficial NeoForge port), Citadel |
| `hazen` | Not included | Hazen 1.4.0.14 | HazenTouvelib, Caelus, Prometheus 1.2.5, Cobweb 1.4.0 |
| `gtbc` | Not included | GTBC Spellbooks 3.0.0 | GTBC Spell Lib 1.5.0 |
| `bielgg` | BielGG 1.5-patchwork | Not included | Cataclysm profile plus BielGG, to test patch ownership yielding |
| `deeper` | Darker Magic 1.3.1 | Darker Magic 1.3.3 | Deeper and Darker |
| `illage` | Illage and Spell-age 1.1.0 | Not available | Illage and Spillage: Respillaged, GTBC Spell Lib |
| `all` | Applicable non-isolated profiles | Applicable non-isolated profiles | Excludes `bielgg`; Forge also excludes `traveloptics` (conflicting Cataclysm version) |

The NeoForge baseline is now **21.1.248**, explicitly approved for the
FamiliarsLib minimum. Forge uses GeckoLib **4.8.4**. T.O 6.3.0 fails to load
with Cataclysm 3.31 (`DungeonEyeItem` was removed); its isolated 3.16 fixture must
not be combined with the modern Cataclysm Spellbooks profile. Mixed duplicate
mod versions fail early in the preparation/Gradle configuration checks.
Lionfish 3.0 is also incompatible with Cataclysm 3.16's old models: the real
Blood Howl test reproduced an `AbstractMethodError` for `BasicEntityModel#root`.
The T.O profile uses 2.7; modern Cataclysm keeps 3.0 in its separate profile.

The locked Legendary Monsters NeoForge JAR contains unexpanded loader/version
placeholders. The actual 21.1.248 `all` client accepted it and entered a world;
the static verifier still warns because the upstream metadata is unresolved.
We do not silently rewrite the downloaded JAR or generalize that observation
to every loader version.

## Run manually

Use JDK 17 in the Forge checkout; JDK 21 in the sibling
`IronsSpellScrollPonder-1.21.1` worktree. Start with individual profiles:

Run only one game client at a time. Complete Forge testing before starting the
NeoForge client; do not run the two loader suites concurrently.

```sh
node tools/compat/prepare-test-mods.mjs --profile wind
node tools/compat/verify-test-fixtures.mjs run/compat/wind-on
./gradlew verifyCompatTestRuntime -PcompatTestProfile=wind
./gradlew runClient -PcompatTestProfile=wind
```

Create an isolated world, obtain a scroll and open its preview normally.
Adapters are enabled only in the fixture common config. No automation starts
unless requested. Prepare each exact profile selection before using it.

## Run automated previews

```sh
node tools/compat/prepare-test-mods.mjs --profile legendary --scope targeted
./gradlew runClient -PcompatTestProfile=legendary -PcompatSmoke=true -PcompatAutoWorld=true -PcompatLevels=min,max
node tools/compat/check-smoke-results.mjs run/compat/legendary-on
```

`compatAutoWorld=true` creates a fresh isolated creative flat world at the title
screen. Otherwise open/create a single-player world yourself. The runner refuses
to automate remote servers. It uses real preview requests, integrated-server
casting and Ponder rendering, disables replay between cases and records results
and screenshots. A render crash can terminate Minecraft before results are saved.

Options:

- Preparation `--scope all` (default): enumerate enabled fixture-namespace spells
  from the real registry, including inherited/helper registrations absent from
  the audit. Stale audit IDs and disabled spells are inventoried separately.
- Preparation `--scope targeted`: audited adapter cases plus Wind's baseline.
- `-PcompatSpells=legendary_spellbooks:flame_eater`: explicit comma-separated IDs.
- `-PcompatLevels=min,max` or `-PcompatLevels=1,3`: bounds or explicit levels,
  clamped to each spell's legal range. Default `max`.
- `-PcompatSmokeExit=true`: close the client after the suite; default false.
- `-PcompatResume=true`: reuse completed cases only when the entire fixture
  manifest matches; failed/incomplete cases rerun. Default false.
  Do not resume across changes to loader/Iron baselines, spell configs or
  behavior being revalidated: use a fresh suite or explicit targeted cases.
- Combine compatible profiles with commas, or select `all`; run Forge
  `traveloptics` separately to complete the isolated spell coverage.

To compare adapters disabled in a separate directory:

```sh
node tools/compat/prepare-test-mods.mjs --profile legendary --mode off
./gradlew runClient -PcompatTestProfile=legendary -PcompatTestMode=off -PcompatSmoke=true -PcompatAutoWorld=true -PcompatLevels=min,max
```

Existing fixture configs are preserved. `--reset-config` explicitly resets only
the selected fixture config. Restart after startup-patch changes. Forge's
`cataclysm` on fixture enables the version-gated legacy patch; its off fixture
may reproduce the upstream startup failure. Test `bielgg` separately to confirm
our patch yields ownership. Do not mix fixtures into installed modpacks.

## Evidence and acceptance

Under the selected `run/compat/...` directory inspect:

- `compat-fixtures.json`: exact fixtures, requested mode and scope.
- `compat-cases.json`: static seed cases, not passed tests.
- `compat-runtime-cases.json`: actual selection after registry discovery.
- `compat-audit-unavailable.json`, `compat-disabled-spells.json`: audit-only IDs
  and explicitly disabled registrations, not silently claimed as tested spells.
- `compat-test-results.json`: finished flag and individual results, if produced.
- `screenshots/compat-*.png`, `logs/latest.log`, `crash-reports/`.

`RENDERED_COMPLETE` means a scene completed with projected entities, not that
effects, damage, targets, recasts or framing are correct. Compare screenshots
and live behavior with normal casting before recording a visual pass. Repeat
low/high levels, off/on, replay/close and cleanup. Dedicated-server projection
needs a separate server and real client; this runner does not cover that topology.

Always run `check-smoke-results.mjs` after an automated suite. A failed FML
preload can still return Gradle exit code zero; the checker requires a clean
latest-game-log FINISHED marker, a matching nonempty finished report and no
failed cases or stopped experimental adapters. Screenshots include a unique run
ID so subsequent retries do not obscure the earlier evidence.

The Forge Illage fixture exposes an upstream missing `OwnerGetter` API. Three
summon cases are deliberately restricted by the enabled safety provider. The
checker rejects skips by default. After reviewing [runtime findings](RUNTIME_FINDINGS.md),
use `--acknowledge-illage-restrictions` to accept only those three exact Forge
safety skips; output still distinguishes blocked cases from effect-test passes.
Any cast failure, other skip or adapter exception continues to fail the check.

Fixture hashes/ZIPs, required-mod-ID closure, runtime resolution and test-source
compilation are tracked separately from gameplay. The current lock's `all`
selection contains 19 fixture JARs on Forge and 24 on NeoForge; these counts are
not a count of passing tests. Version ranges and undeclared APIs require actual
loader checks; recorded results identify exactly which versions ran.
Wind's NeoForge smoke coverage above is not dedicated-server or exhaustive
visual certification. The monitor failure was not worked around by changing
the user's desktop or modpack; macOS does report an online built-in main display.
