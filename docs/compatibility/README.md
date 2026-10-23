# Experimental add-on previews (1.1.0)

See [Development testing](DEVELOPMENT_TESTING.md) for pinned optional runtime
fixtures, isolated profiles, real-client smoke runs and current testing limits.
The [real-client matrix](TEST_MATRIX.md) separates completed cases, blocked
upstream cases and visual-review limitations.

Research snapshot: **2026-10-23**. Minecraft 1.20.1/Forge and 1.21.1/NeoForge
are separate artifacts. Both were built against Iron's Spellbooks **3.16.3**
(CurseForge files 8680180 and 8680204 respectively). This is not a promise that
every add-on itself starts with every Iron's version: an add-on's own mandatory
dependencies and binary compatibility remain its author's responsibility.

## Selection and evidence

The following download totals are project-wide CurseForge metadata snapshots,
not counts for an individual file, and may lag the public site. The selection
threshold was 100,000 downloads. Download/release metadata came from the
project's CurseForge page/file list and CFWidget's cached public metadata.
Release JARs were retrieved from ForgeCDN; their actual mod metadata and
decompiled implementations, rather than descriptions alone, were inspected.
This is a broad selection, not an exhaustive list of all existing add-ons.

| Project (CurseForge slug/link) | Downloads | Forge 1.20.1 release examined | NeoForge 1.21.1 release examined | Decision |
| --- | ---: | --- | --- | --- |
| [Wind's](https://www.curseforge.com/minecraft/mc-mods/winds-spellbooks-irons-spells-n-spellbooks-addon) | 538,109 | 1.0.3 | 1.0.5 (tagged 1.21) | Seven ordinary spells; keep generic preview |
| [Legendary](https://www.curseforge.com/minecraft/mc-mods/legendary-spellbooks) | 329,969 | 0.4.1 | 0.3.2 | Five targeted rules, including book-only Geyser/Beam |
| [Cataclysm Spellbooks](https://www.curseforge.com/minecraft/mc-mods/cataclysm-spellbooks) | 3,968,602 | 1.2.9 | 1.1.14 | Recasts and summon target; separate legacy Forge startup patch |
| [Twilight Spellbooks](https://www.curseforge.com/minecraft/mc-mods/twilight-spellbooks-irons-spells-n-spellbooks) | 112,216 | 1.2.0 | 1.2.0 | Spellbooks/items, no new spell implementations to adapt |
| [T.O Magic 'n Extras](https://www.curseforge.com/minecraft/mc-mods/to-tweaks-irons-spells) | 8,736,000 | 6.3.0 | 4.4.0.1 (deprecated) | Forge rules only; upstream labels Neo release “DEPRECATED DONT USE” |
| [Alshanex's Familiars](https://www.curseforge.com/minecraft/mc-mods/alshanexs-familiars) | 6,042,234 | v4.1.0 | v5.0 | Forge Default Note finisher; Neo Switcheroo owned-pet target; no summon-dismiss recasts |
| [Hazen 'n Stuff](https://www.curseforge.com/minecraft/mc-mods/hazen-n-stuff) | 3,532,405 | watered-down 1.1.2 | 1.4.0.14 | Neo Dazzling Obliteration; Forge file has stale spell names but no spell classes |
| [GTBC Geomancy Plus](https://www.curseforge.com/minecraft/mc-mods/gtbcs-geomancy-plus) | 6,208,724 | 2.0.0 | 1.1.0 | Geo Conductor and Tremor Spike |
| [GTBC's Spellbooks](https://www.curseforge.com/minecraft/mc-mods/gtbcs-spellbooks) | 2,023,711 | unavailable | 3.0.0 | Three Neo rules |
| [Illage and Spell-age](https://www.curseforge.com/minecraft/mc-mods/illage-and-spell-age) | 2,181,995 | 1.1.0 | unavailable | Three defensive restrictions for upstream-incompatible summons; remaining spells use generic preview |
| [Deeper and Darker Spellbooks](https://www.curseforge.com/minecraft/mc-mods/deeper-and-darker-spellbooks) | 462,060 | 1.3.1 | 1.3.3 | Four summons; do not automatically recast their dismissal |
| [Alex's Caves Spellbooks](https://www.curseforge.com/minecraft/mc-mods/alexs-caves-spellbooks) | 440,505 | 1.2.0 | 1.2.1 | Enable target navigation for Aspect of Tremorsaurus |
| [Ice and Fire Spellbooks](https://www.curseforge.com/minecraft/mc-mods/ice-and-fire-spellbooks) | 8,486,930 | 2.3.2 | unavailable | No added spell classes in this release |
| [BielGG's Spells](https://www.curseforge.com/minecraft/mc-mods/bielggs-spells-addon) | 76,563 | 1.5-patchwork | unavailable | Below cutoff; explicit user-requested reference for startup repairs only |

The exact file IDs, hashes, and **upstream dependency ranges** of the 21
spell-bearing/candidate release artifacts are in [releases.json](releases.json).
Do not install only these add-ons without their normal dependencies: examples
include Cataclysm/Lionfish API, Ace's Spell Utils, Alex's Caves, or Mowzie's Mobs.
None is bundled or added as a mandatory dependency by this project.

[SPELL_AUDIT.md](SPELL_AUDIT.md) records 547 inventory rows across the selected
artifacts, including helper classes and unresolved/stale language entries.
**This does not mean 547 working spells.** It distinguishes targeted adapters,
generic candidates, summon lifecycle exclusions, player/world-sensitive code,
and unresolved IDs. [spell-audit.json](spell-audit.json) is the machine-readable
equivalent. A static generic candidate still needs visual testing, especially
when it sends its own packets or assumes a real client connection/inventory.

## Enable and disable

All integrations are **off by default**. The existing generic preview works
without them. In `config/iss_ponder-common.toml`:

```toml
[experimental]
enabled = true
cataclysmLegacyPatch = false

[experimental.addons]
legendary_spellbooks = true
traveloptics = true
cataclysm_spellbooks = true
alshanex_familiars = true
hazennstuff = true
gametechbcs_spellbooks = true
gtbcs_geomancy_plus = true
alexs_caves_spellbooks = true
ias_spellbooks = true # Forge: defensive restrictions for three incompatible summons
```

Restart after changes. The **server's** COMMON config controls simulation
adapters. The Forge startup patch additionally needs the config on each
physical installation that needs patching, including the client for renderer
repairs. No configuration is automatically synchronized to another machine.
To disable all optional rules, set `experimental.enabled = false` and restart.
Only the small config/dispatch gate and Forge Mixin plugin are then loaded;
no provider or Cataclysm transformer/bridge is resolved. The plugin must exist
early enough to read that switch, but it returns without modifying targets.

An adapter is used only if the add-on is installed, enabled, its runtime mod
version matches the audited profile, and the spell ID **and exact implementation
class name** match. Unknown versions warn once and retain the generic preview.
An add-on's explicit `SpellPreviewAdapters.register` rule always wins. An
exception or `LinkageError` in our rule is logged and quarantined; it does not
justify executing unreviewed packets or recovering arbitrary errors inside
the third-party spell itself. This is not a security sandbox for mod code.

## Targeted rules

The canonical targeted spell list is the 1.1.0 changelog. Forge also supplies
an equipment prerequisite for `thoths_witness` and three defensive Illage
restrictions for a removed upstream API (not newly supported summon effects).
See [runtime findings](RUNTIME_FINDINGS.md) for failures found in real clients.
Recasts are deliberately bounded to short demonstrations; this is not a full
gameplay rotation or an attempt to exhaust very high-level spells.

| Mechanism | Upstream reference to recheck | Preview action |
| --- | --- | --- |
| Repeated projectiles | Each listed spell's `onCast`, `getRecastCount`, `RecastInstance` | Wait a few ticks and request another normal cast only while recast state exists |
| Legendary Overgrown Shockwave | `OvergrownShockwaveSpell#onCast` ground samples | Origin-relative radius-11 support floor plus bounded recasts |
| Legendary Annihilation Resonance | `AnnihilationResonanceHandler#onCriticalHit`, effect and spell | Equip a sword and execute a real falling `Player#attack`; direct `Entity#hurt` does not post the critical event |
| T.O/GTBC Reversal | `ReversalSpell#onCast` and Reversal effect/event handler | Equip a sword before the cast, then deliver one mob hit while the short parry window is active |
| T.O Echo of the Abyss | `EchoOfTheAbyssSpell#checkPreCastConditions`, `onCast` | Glass-contained water around caster; update fluid state before casting; never recast to dismiss |
| Cataclysm Rewire | `RewireSpell#onCast`, `CSEntityRegistry.SUMMONED_WATCHER` | Use an owned `IMagicSummon` instead of a zombie |
| Cataclysm Thoth's Witness (Forge) | `ThothsWitnessSpell#canBeCastedBy` | Equip a registry-resolved Pharaoh helmet before cast validation |
| Illage summons (Forge) | Summon `registerGoals` / removed Iron `OwnerGetter` | Refuse the three affected previews when the missing API would crash the server |
| Alex's Aspect of Tremorsaurus | `AspectOfTremorsaurusSpell` / fear effect | Allow primary target AI/navigation, normally disabled in the preview |

Wind's `AeropicSpell#getRecastCount` alone does not establish the player recast
state. A blanket recast rule would have no useful effect. Summon-dismiss
recasts (Deeper/Darker, familiars, and several T.O/Hazen/Geomancy spells) would
actively remove the feature being demonstrated, so they are left alone.
Armor-specific, persistent progression, dimensional, and custom-packet effects
remain manual validation cases, not claimed complete integrations.

## Cataclysm Spellbooks legacy startup patch — Forge only

The exact audited pair is **Cataclysm Spellbooks `1.2.9-1.20.1`** (file 7687059)
and **L_Ender's Cataclysm `3.31`** (file 8246047). The first value is the actual
runtime mod version, including the MC suffix, not only the website label.
The patch is separate from preview rules and affects those legacy classes
wherever they run, not only in a Ponder scene. It does not replace either mod.

Opt in with both `enabled = true` and `cataclysmLegacyPatch = true`, keeping
`addons.cataclysm_spellbooks = true`. If the broken pair crashes before Forge
can generate the file, create `config/iss_ponder-common.toml` with these entries
manually before launching. Missing/malformed config fails closed. Other version
pairs are left untouched. **This cannot make an incompatible add-on start while
the experimental switch is off.**

The plugin skips our startup patch entirely when mod ID `bielgg_spells` is
installed, allowing BielGG's existing patch to own the same classes. The
examined reference was BielGG 1.5-patchwork (file 8859728). Install/update that
mod and its dependencies normally if using its patch; this project neither
loads BielGG classes nor silently applies two competing transformers.

Independent symbol/ABI comparison of the published JARs identified:

- `Ancient_Remnant_Rework_Renderer` renamed to `Ancient_Remnant_Renderer`.
- Ring/Storm/LightTrail/TrackLightning inner particle data moved to `Options`.
  Ring now uses integer RGB values and an integer behavior instead of float
  colors and an enum argument.
- Wither Missile's direction changed from three doubles to `Vec3`.
- `causeLaserDamage` now accepts a general `Entity` as its second argument.
- The removed remnant `setSleep` operation is bridged through necklace and
  attack state, preserving the modern entity's sleeping state convention.

`CataclysmLegacyMixin` targets exactly the 12 classes found by the ABI scan.
The guarded plugin loads `CataclysmLegacyTransformer` only for the enabled,
exact version pair. Rewriting is copy-first, constructor-shape checked, and
idempotent. The runtime particle factory and sleep bridge use guarded reflection
without addon imports. BielGG was an interoperability reference; its
implementation/assets are not copied or bundled.

Source references: the release JAR class paths in the audit;
[Legendary upstream](https://github.com/Higurashi34m/Legendary-Spellbooks)
(`1.20.1-Forge`, `1.21.1-NeoForge`),
[Cataclysm Spellbooks upstream](https://github.com/AceTheEldritchKing/Cataclysm_Spellbooks)
(`1.20.1`, `master`), Cataclysm's published 3.31 JAR, and the BielGG release
linked above. Use the hashes/file IDs to recover exactly the reviewed release;
moving source branches may differ.

## Verification and outstanding work

Completed in this workspace:

- Forge/Java 17 and NeoForge/Java 21 Gradle builds against Iron's 3.16.3.
- Static review inventory and exact release metadata/hash capture.
- Standalone ASM `BasicVerifier` on all 12 transformed Cataclysm classes,
  obsolete-symbol checks, and second-pass idempotence.
- Isolated Mixin 0.8.5 transformation of the broken remnant renderer confirmed
  the missing superclass is replaced by `postApply`. This used a probe plugin
  in LaunchWrapper, not a full Forge/modpack boot; missing old metadata still
  emitted warnings during introspection.
- Real Forge 47.4.4 client startup with the exact modern Cataclysm pair and
  24 targeted low/high-level cases, followed by a 152-spell registry suite:
  149 rendered-complete and three explicit Illage upstream safety restrictions.
- Real isolated Forge T.O suite: all 68 enabled registered spells completed
  with the Cataclysm 3.16 / Lionfish 2.7 dependency pair.
- BielGG patch ownership was yielded in a real startup attempt; its upstream
  early-config-read bug blocked world entry (not a successful combined boot).
- NeoForge 21.1.248: 177 registry cases completed after the Switcheroo retry,
  plus ten fresh minimum/maximum-level target/follow-up cases. Two master-off
  generic cases completed; JVM class-loading logs showed zero optional
  provider/support classes, matching the Forge master-off observation.

Still required before treating the experimental feature as gameplay-certified:

1. Dedicated server boot with the exact Cataclysm pair; check patch off/on,
   addon absent, unknown version, and finish BielGG gameplay testing once its
   early-config-read problem is fixed upstream.
2. Repeat class-loading isolation with individual addon switches and unknown
   versions. Both loaders' real master-off Legendary tests already observed
   zero optional provider/support classes and two completed generic cases each.
3. Visual test every targeted spell at low/high levels on integrated and
   dedicated servers. Check effects, damage, recasts and camera bounds.
4. Check out-of-scene side effects, two simultaneous observers, close/replay,
   dimension cleanup, and restoration after exceptions.
5. Audit unresolved language entries and inherited/helper implementations
   before claiming their compatibility; validate any addon custom packets.

Automated isolated development clients did start and cast spells; no complete
visual/mechanical pass is claimed. No existing modpack or EULA was modified.
The project has no JUnit/GameTest cases
for these integrations: Gradle's `test NO-SOURCE` is not a runtime test pass.

To repeat the bytecode smoke check from the Forge branch, compile
`tools/compat/VerifyCataclysmPatch.java` and the transformer with ASM core/tree/
commons/analysis on the classpath, then run `VerifyCataclysmPatch <1.2.9.jar>`.
`tools/compat/build-audit.mjs` regenerates the audit from downloaded metadata,
release JARs and Vineflower output; it does not download or redistribute them.
