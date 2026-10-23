# Changelog

All notable changes to Iron's Spell Scroll Ponder are documented in this file.

## 1.1.0 - 2026-10-24

### Added

- Added opt-in development add-on profiles with pinned downloads and supporting
  dependencies, isolated on/off run directories, runtime-classpath checks and a
  client preview runner with runtime spell discovery, low/high level cases,
  screenshots and JSON reports. Test classes are excluded from release JARs.
  Actual runtime coverage is listed below; runner completion is not full
  visual/mechanical certification.
- Added default-off experimental add-on preview adapters in
  `config/iss_ponder-common.toml`, with a master switch and individual add-on
  switches. Providers load lazily only when enabled and installed; exact audited
  runtime versions/class names are checked, unknown versions are skipped, and
  runtime/linkage failures disable the affected optional rule.
- Added these targeted spell demonstrations (registry paths, not translated names):
  - **Legendary Spellbooks — both loaders:** `flame_eater`,
    `overgrown_shockwave`, `annihilation_resonance`: bounded recasts, ground
    support and a real critical-hit trigger.
    `annihilation_geyser` and `energy_beam` use their required spellbook cast
    source; Geyser also receives its exclusive equipped Curios book.
  - **Cataclysm Spellbooks — both loaders:** `abyss_fireball`,
    `piercing_bone`, `meteor_spew`, `phantasmal_blade`,
    `amethyst_puncture`, `disabling_swipe`, `laserbolt`,
    `missile_launch`, `rewire`: bounded recasts or an owned summon target.
    Forge additionally adapts `final_rend`, `quick_strike`, `cryopiercer`.
    Forge's `thoths_witness` now receives its required Pharaoh armor piece.
  - **T.O Magic 'n Extras — Forge only:** `blood_howl`,
    `nocturnal_swarm`, `psychic_bolt`, `coral_barrage`,
    `void_eruption`, `annihilation`, `nullflare`, `despair`,
    `reversal`, `echo_of_the_abyss`: bounded recasts, a timed parry hit and
    an underwater casting scene. The deprecated Neo release is excluded.
    Additional weapon/source prerequisites: `apex_harvest`, `skypiercer`,
    `shear_of_the_stars`, `supernova`, `extinction`, `tectonic_rift`,
    `gyro_slash`, `galena_shatter`, `call_forth_the_dead_king`.
    `jet_steam` receives a water/swimming scene; Echo's submerged prerequisite
    now accounts for the delayed eye-fluid cache.
  - **Alshanex's Familiars — Forge only:** `default_note` final-note sequence.
    **NeoForge:** `switcheroo` uses an owned familiar target instead of a zombie.
  - **Hazen 'n Stuff — NeoForge only:** `dazzling_obliteration` staged recasts.
  - **GTBC's Spellbooks — NeoForge only:** `psychic_bolt`, `nullflare`,
    `reversal` recasts/parry trigger.
  - **GTBC Geomancy Plus — both loaders:** `geo_conductor`, `tremor_spike` recasts.
  - **Alex's Caves Spellbooks — both loaders:** `aspect_of_tremorsaurus`
    enables target navigation for the fear demonstration.
- Added a separate opt-in **Forge-only** legacy ABI patch for Cataclysm
  Spellbooks `1.2.9-1.20.1` with Cataclysm `3.31`: renderer/particle renames,
  changed Ring options and Wither Missile constructors, laser damage signature,
  and remnant sleep state. It is disabled by default and skips when BielGG's
  Spells is installed to avoid duplicate repairs.
- Added `SpellPreviewAdapter#beforeCast` and `#castSource` for state/source
  required by cast predicates, preserving COMMAND for all existing adapters.
- Added the release/dependency inventory, per-spell static audit, source
  references, compatibility design notes and outstanding runtime test checklist
  under `docs/compatibility`. Generic candidates (including Wind's spells)
  receive no redundant adapters. Twilight/Ice and Fire releases without new
  spell implementations are not advertised as newly adapted spells.

### Changed

- Bumped both loader artifacts to 1.1.0 and the development Iron's Spellbooks
  baseline to 3.16.3. Optional add-ons are not bundled or mandatory dependencies.
- Updated the NeoForge development baseline to 21.1.248 and Forge GeckoLib to
  4.8.4 so the reviewed Familiars/FamiliarsLib releases can load. Dependency
  metadata remains permissive; these are development/test baselines.
- Pinned complete development fixture dependencies, including undeclared
  Legendary Monsters/Mowzie's Mobs references, AzureLib 3.1.24 on Forge, and a
  separate legacy-Cataclysm profile for T.O. Refuse mixed duplicate mod versions.
- Included Prometheus 1.2.5 and Cobweb 1.4.0 in the NeoForge Hazen development
  profile after a real Soul Flame Bolt hit exposed its undeclared runtime API.
  They remain development-only dependencies, not release requirements.
- Preserved explicit addon-author adapters ahead of experimental provider rules.
- Bounded multi-cast demonstrations rather than automatically dismissing summons.

### Fixed

- Fixed a real-client Legendary `annihilation_beam` crash caused by entity-list
  mutation inside Ponder's tick iterator. Use snapshot traversal only in this
  mod's projected level, keeping upstream ticking and live removal behavior.
- Added a default-off Illage safety provider: `handemic`, `spirits_chosen` and
  `venomous_reborn` are restricted when Iron's removed `OwnerGetter` API is
  absent. This prevents the observed preview server crash; it does not repair
  normal upstream gameplay or count as a successful summon-effect test.
- Fixed a real-client Wind's `iron_slash` preview crash caused by submitting a
  server-only `dash_stop` helper with no renderer to Ponder. Both loaders retain
  helper projection state but omit unrenderable entities from the drawing pass.
  NeoForge Wind's completed 14 minimum/maximum-level smoke cases after the fix;
  complete visual and dedicated-server validation remain pending.
- Removed hard references to SoulfireRay/TintedBubblePop particle option classes
  absent in older Iron's versions (including 3.15.4), avoiding a dedicated-server
  `NoClassDefFoundError` in preview particle projection. Unsupported optional
  signatures fail safely; existing embedded-endpoint render guards remain.
- Restored simulated caster health/invulnerability in `finally` after reactive
  hit demonstrations, including when an effect handler throws.

### Verification status

Forge's real-client registry suites recorded 149 rendered-complete cases plus
three explicit Illage restrictions in the 152-spell main profile, and 68/68
rendered-complete T.O cases in its isolated dependency profile. Two master-off
Legendary cases completed, with JVM class-loading evidence that optional
providers/support classes stayed unloaded. BielGG yielded patch ownership but
its own early config read blocked startup; it is not counted as a gameplay pass.
NeoForge 21.1.248 has a separate registry suite and reports under
`docs/compatibility/test-results/`: 177 enabled spells completed, including a
successful Switcheroo retry after fixing target-preparation timing. Fresh
final min/max reruns completed 10 NeoForge and 8 Forge cases; the
NeoForge master-off check also observed zero optional provider/support classes
and two generic completed cases. Full visual/mechanical and dedicated-server
verification remain separate: rendered completion is not effect certification.
The legacy patch also passed 12-class ASM stack/symbol/idempotence checks.
Integrations remain experimental and off by default; exact versions, evidence
and outstanding risks are documented in `docs/compatibility`.

## 1.0.3 - 2026-08-06

### Changed

- Relaxed the Minecraft, Forge, loader, Iron's Spellbooks, and Ponder
  dependency ranges to accept any available version.

## 1.0.2 - 2026-08-03

This release targets Minecraft 1.20.1 and Forge 47.

### Changed

- Documented the Forge FakePlayer networking lifecycle and its upstream code
  references in `TECHNICAL_NOTES.md`.

### Fixed

- Initialized Forge's shared FakePlayer connection with a persistent local
  Netty channel so optional network checks from add-on mods no longer crash
  the preview server tick with a null channel.
- Kept virtual packet handling local; unsupported remote channels are reported
  as absent and FakePlayer packets continue to be discarded.
- Fixed the Thunderstorm preview exhausting the render buffer by translating
  absolute endpoints embedded in Iron's lightning particle options.
- Added a client-side distance and finite-value guard for destination-bearing
  particles so malformed add-on effects cannot allocate unbounded geometry.

## 1.0.1 - 2026-07-31

This release targets Minecraft 1.20.1 and Forge 47.

### Added

- Added play/pause controls for the preview loop in every supported language.
- Added adapter support for spell-specific primary target entity types and
  origin-relative initial scene blocks.
- Added prepared targets for `wololo`, `sacrifice`, `spectral_hammer`, and
  `touch_dig`.

### Changed

- Expanded the arena to a symmetrical `7 x 11` platform, with two extra rows
  behind both the caster and the targets.
- Preserved the original `7 x 7` camera basis so the larger platform does not
  alter the default framing height.
- Replaced the Replay button with a playback toggle. Previews loop by default;
  pausing prevents the next replay without freezing entities, particles, or
  simulation time.
- Updated the add-on compatibility and special-spell documentation for the new
  adapter hooks.

### Fixed

- Fixed `wololo` failing because the preview supplied a zombie instead of a
  sheep.
- Fixed `sacrifice` failing because its target was not an owned magic summon.
- Fixed `spectral_hammer` and `touch_dig` failing because no valid block was
  present on the caster's horizontal ray.
- Ensured prepared scene blocks are projected on first load and every replay,
  then removed when the preview session closes.
