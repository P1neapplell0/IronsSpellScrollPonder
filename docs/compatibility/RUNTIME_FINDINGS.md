# Runtime findings (1.1.0 development clients)

These are observations from isolated real Minecraft clients, not conclusions
drawn from compilation. Exact fixture identities and per-spell results are in
`test-results/`; full local screenshots/logs remain in `run/compat/`.

## Fixed projection defects

- Legendary `annihilation_geyser` and `energy_beam`: their `canBeCastedBy`
  rejects COMMAND. Geyser additionally requires the equipped Annihilator's
  Protocol Curios book. The adapter API now permits a normal SPELLBOOK cast
  source; only these exact optional rules request it. Geyser resolves and
  equips `legendary_spellbooks:annihilators_protocol` in the isolated caster's
  spellbook slot, without bypassing the original cast predicate.
  Both completed real-client retests after raising the fake caster's MAX_MANA:
  `MagicData#setMana` alone silently clamps a fresh fake player to 100 mana.
  Follow-up windows keep the caster effect ticking for the beam's lifetime and
  leave Geyser portals time to unfold. Airborne casters/large summons can still
  require manual zoom/pan; a fixed-camera screenshot is not a framing guarantee.
- Wind's `iron_slash` (NeoForge): `dash_stop` is a server logic helper without
  an entity renderer. Ponder dereferenced the absent renderer. Keep the helper
  tracked/ticking but temporarily omit it from the drawing pass. All seven
  Wind's spells subsequently completed at minimum/maximum level.
- Legendary `annihilation_beam` (Forge): entity ticks mutate Ponder's entity
  list, invalidating `PonderLevel#tick`'s iterator. A client-only Mixin substitutes
  a snapshot iterator only for this mod's projected level. Iterator removals
  affect the live list; upstream particles, interpolation and cleanup are retained.
- Cataclysm `thoths_witness` (Forge): `ThothsWitnessSpell#canBeCastedBy` requires
  at least one Pharaoh armor piece. The optional rule equips the registered
  `cataclysm_spellbooks:pharaoh_helmet` before validation. A real rerun rendered
  the remnant summon and particles; this rule is unnecessary on NeoForge.
- T.O's equipment-exclusive spells use registry-resolved weapons, and
  `call_forth_the_dead_king` requests its required SCROLL cast source. Echo of
  the Abyss needs a water volume on both sides of the caster's block boundary
  and two `Entity#baseTick` calls: `updateFluidOnEyes` stores the previous
  eye-fluid state. Jet Steam additionally requires swimming state. Ordinary
  cast predicates are kept, not globally bypassed.
- Familiars 5.0 `switcheroo` (NeoForge): `SwitcherooSpell#checkPreCastConditions`
  requires a FamiliarsLib pet whose `getSummoner` is the caster, not a zombie.
  `OwnedFamiliarAdapter` resolves `alshanex_familiars:mage_pet` and calls the
  audited `setOwnerUUID(UUID)` signature reflectively. Missing APIs fail inside
  the guarded provider; there is no direct Familiars/FamiliarsLib class import.
  The real retest exposed an initially unavailable UUID lookup in
  `onSceneReady`. Ownership now runs in `beforeCast`, after scene warm-up, and
  verifies `getSummoner()` resolves the caster before normal spell validation.
  Rewire ownership and fear-target AI preparation use the same later phase.

## Upstream incompatibilities (not claimed fixed)

- Illage and Spell-age 1.1.0 with Iron's 3.16.3: `SummonedSpiritHand#registerGoals`
  links to the removed `io.redspace.ironsspellbooks.entity.mobs.goals.OwnerGetter`.
  The delayed `MagicManager` cast of `handemic` crashed the integrated server.
  The same missing interface appears in `SummonedMobSpirit` and `SummonedRagno`.
  The default-off Illage compatibility provider refuses `handemic`,
  `spirits_chosen` and `venomous_reborn` previews only when that API is absent.
  Safety-policy skips are not successful effect tests. This does not repair
  ordinary addon gameplay, and disabling experimental rules removes this guard.
- T.O 6.3.0 references `Dungeon_Eye.DungeonEyeItem`, removed in Cataclysm 3.31.
  Its test profile therefore uses a separate Cataclysm 3.16 fixture. The two
  Cataclysm versions must not be installed together. No broad upstream ABI
  rewrite is attempted for T.O.
  Blood Howl then exposed Cataclysm 3.16's incompatible Lionfish 3.0 model ABI
  (`BasicEntityModel#root` AbstractMethodError); that profile pins Lionfish 2.7.
- Forge Familiars 4.1.0 needs GeckoLib 4.8.4; Cataclysm Spellbooks needs the
  AzureLib 3.1.24 `AzPlayBehaviors` API. The previous fixture versions could
  resolve/build but did not load. Development dependencies were corrected.
- NeoForge FamiliarsLib 2.0 requires NeoForge >=21.1.244. The user approved
  21.1.248 for the development baseline. A loader error screen followed by
  Gradle exit zero is never counted as a successful game test.
- BielGG 1.5-patchwork: our startup log confirmed patch ownership was yielded;
  BielGG's own transformers then ran. However its spell registration calls
  `LightweightConfig#unstableEnabled` before Forge loads that config, crashing
  the development client from `AbstractUnstableSpell` / `LaserEX3Spell`.
  No requested Cataclysm preview case ran in this profile. We do not disable
  Forge's development safety check, modify the downloaded JAR, or claim a
  successful combined startup. An upstream fix is required to finish this
  interoperability gameplay check; BielGG remains an explicitly requested
  below-100k reference, not a qualifying certified spell integration.
- Hazen 1.4.0.14 calls Prometheus `FireManager` in its damage event handler,
  despite allowing the client to start without that API. The actual Soul Flame
  Bolt hit crashed with `NoClassDefFoundError`; the NeoForge development profile
  now includes Prometheus 1.2.5 and its required Cobweb 1.4.0 dependency. These
  are test-environment dependencies, not mandatory dependencies of this mod.

## Evidence limits

NeoForge Legendary/Legendary Monsters also logged missing upstream icon and
particle textures. The client continued; these warnings are not erased or
treated as proof of complete visuals. Energy Beam's sampled frames showed an
airborne caster leaving the default frame, so a completed cast is not a visual
beam certification. See [selective visual observations](VISUAL_REVIEW.md).

`RENDERED_COMPLETE` means real server casting plus a real rendered Ponder screen
completed, with projected entities and no detected case failure. It does not
prove every particle, custom packet, damage value or summon AI is correct.
Low/high targeted cases supplement full registry runs at maximum configured
level. Screenshots require visual review; dedicated-server, multiplayer and
out-of-scene side-effect checks remain separate release risks.
