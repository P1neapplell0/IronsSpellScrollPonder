# Spell-by-spell static audit

Release 1.1.0 research snapshot: 2026-10-23. This is a source/bytecode inventory, **not a gameplay certification**.

Each row identifies the implementation in the exact release JAR listed in `releases.json`. Review `onCast`,
`checkPreCastConditions`, recast lifecycle and related effect/event handlers at that class before changing a rule.
Names from language files without a matching implementation remain explicitly unresolved (they may be stale).
Generic candidates intentionally receive no compatibility code. Dependency declarations do not prove compatibility
with every newer Iron's release; only our two base builds have been verified against 3.16.3.

Flags are conservative source-search hints, not proof that a special condition blocks the preview.
Summon recasts often dismiss the entity; never blindly replay every spell with `getRecastCount`.

## Wind's Spellbooks : Iron's Spells 'n Spellbooks Addon — 1.20.1

CurseForge file: 8486282; 7 inventory rows.

| Spell ID | Implementation (within the JAR) | Conditions / action |
| --- | --- | --- |
| `wind_spellbooks:aeropic` | `net.raptorzizi.wind_spellbooks.spells.wind.AeropicSpell` | generic candidate; visual test pending; recast |
| `wind_spellbooks:almighty_push` | `net.raptorzizi.wind_spellbooks.spells.wind.AlmightyPushSpell` | generic candidate; visual test pending; no extra source-search flags |
| `wind_spellbooks:iron_slash` | `net.raptorzizi.wind_spellbooks.spells.wind.IronSlashSpell` | generic candidate; visual test pending; no extra source-search flags |
| `wind_spellbooks:tailwind` | `net.raptorzizi.wind_spellbooks.spells.wind.TailwindSpell` | generic candidate; visual test pending; no extra source-search flags |
| `wind_spellbooks:tornado` | `net.raptorzizi.wind_spellbooks.spells.wind.TornadoSpell` | generic candidate; visual test pending; no extra source-search flags |
| `wind_spellbooks:wind_blade` | `net.raptorzizi.wind_spellbooks.spells.wind.WindBladeSpell` | generic candidate; visual test pending; no extra source-search flags |
| `wind_spellbooks:wind_jump` | `net.raptorzizi.wind_spellbooks.spells.wind.WindJumpSpell` | generic candidate; visual test pending; no extra source-search flags |

## Wind's Spellbooks : Iron's Spells 'n Spellbooks Addon — 1.21.1

CurseForge file: 8485822; 7 inventory rows.

| Spell ID | Implementation (within the JAR) | Conditions / action |
| --- | --- | --- |
| `wind_spellbooks:aeropic` | `net.raptorzizi.wind_spellbooks.spells.wind.AeropicSpell` | manual validation: player/inventory/world conditions; no adapter; recast, player/world-sensitive |
| `wind_spellbooks:almighty_push` | `net.raptorzizi.wind_spellbooks.spells.wind.AlmightyPushSpell` | generic candidate; visual test pending; no extra source-search flags |
| `wind_spellbooks:iron_slash` | `net.raptorzizi.wind_spellbooks.spells.wind.IronSlashSpell` | generic candidate; visual test pending; no extra source-search flags |
| `wind_spellbooks:tailwind` | `net.raptorzizi.wind_spellbooks.spells.wind.TailwindSpell` | generic candidate; visual test pending; no extra source-search flags |
| `wind_spellbooks:tornado` | `net.raptorzizi.wind_spellbooks.spells.wind.TornadoSpell` | generic candidate; visual test pending; no extra source-search flags |
| `wind_spellbooks:wind_blade` | `net.raptorzizi.wind_spellbooks.spells.wind.WindBladeSpell` | generic candidate; visual test pending; no extra source-search flags |
| `wind_spellbooks:wind_jump` | `net.raptorzizi.wind_spellbooks.spells.wind.WindJumpSpell` | generic candidate; visual test pending; no extra source-search flags |

## Legendary Spellbooks — 1.20.1

CurseForge file: 8673872; 35 inventory rows.

| Spell ID | Implementation (within the JAR) | Conditions / action |
| --- | --- | --- |
| (unresolved) | `dev.higurashi.legendary_spellbooks.api.spells.BaseDoubleSchoolSpell` | unresolved ID / helper class; not counted as compatible; no extra source-search flags |
| (unresolved) | `dev.higurashi.legendary_spellbooks.api.spells.BaseOverheadMeleeSwingSpell` | unresolved ID / helper class; not counted as compatible; no extra source-search flags |
| (unresolved) | `dev.higurashi.legendary_spellbooks.api.spells.BaseSpell` | unresolved ID / helper class; not counted as compatible; no extra source-search flags |
| (unresolved) | `dev.higurashi.legendary_spellbooks.api.spells.BaseSummonSpell` | unresolved ID / helper class; not counted as compatible; recast, summon lifecycle, player/world-sensitive |
| `legendary_spellbooks:ambush_thorns` | `dev.higurashi.legendary_spellbooks.common.spells.nature.AmbushThornsSpell` | generic candidate; visual test pending; no extra source-search flags |
| `legendary_spellbooks:annihilation_arrow` | `dev.higurashi.legendary_spellbooks.common.spells.annihilation.AnnihilationArrowSpell` | generic candidate; visual test pending; no extra source-search flags |
| `legendary_spellbooks:annihilation_beam` | `dev.higurashi.legendary_spellbooks.common.spells.annihilation.AnnihilationBeamSpell` | generic candidate; visual test pending; no extra source-search flags |
| `legendary_spellbooks:annihilation_bomb` | `dev.higurashi.legendary_spellbooks.common.spells.annihilation.AnnihilationBombSpell` | generic candidate; visual test pending; no extra source-search flags |
| `legendary_spellbooks:annihilation_geyser` | `dev.higurashi.legendary_spellbooks.common.spells.annihilation.AnnihilationGeyserSpell` | adapter: SPELLBOOK source and equipped registry-resolved Annihilators Protocol Curios book; no extra source-search flags |
| `legendary_spellbooks:annihilation_resonance` | `dev.higurashi.legendary_spellbooks.common.spells.annihilation.AnnihilationResonanceSpell` | adapter: criticalAttack(); no extra source-search flags |
| `legendary_spellbooks:annihilation_shockwave` | `dev.higurashi.legendary_spellbooks.common.spells.annihilation.AnnihilationShockwaveSpell` | generic candidate; visual test pending; no extra source-search flags |
| `legendary_spellbooks:cloud_rail` | `dev.higurashi.legendary_spellbooks.common.spells.lightning.CloudRailSpell` | generic candidate; visual test pending; no extra source-search flags |
| `legendary_spellbooks:cloud_ring` | `dev.higurashi.legendary_spellbooks.common.spells.lightning.CloudRingSpell` | generic candidate; visual test pending; no extra source-search flags |
| `legendary_spellbooks:collapsed_kingdoms_legion` | `dev.higurashi.legendary_spellbooks.common.spells.evocation.CollapsedKingdomsLegionSpell` | generic candidate; visual test pending; no extra source-search flags |
| `legendary_spellbooks:cumulo_charge` | `dev.higurashi.legendary_spellbooks.common.spells.lightning.CumuloChargeSpell` | generic candidate; visual test pending; cast predicate |
| `legendary_spellbooks:energy_beam` | `dev.higurashi.legendary_spellbooks.common.spells.lightning.EnergyBeamSpell` | adapter: SPELLBOOK source required by canBeCastedBy; no extra source-search flags |
| `legendary_spellbooks:flame_eater` | `dev.higurashi.legendary_spellbooks.common.spells.fire.FlameEaterSpell` | adapter: recasts(5, 12); recast, cast predicate, player/world-sensitive |
| `legendary_spellbooks:flame_sector` | `dev.higurashi.legendary_spellbooks.common.spells.fire.FlameSectorSpell` | generic candidate; visual test pending; no extra source-search flags |
| `legendary_spellbooks:flameborn_drift` | `dev.higurashi.legendary_spellbooks.common.spells.annihilation.FlamebornDriftSpell` | generic candidate; visual test pending; no extra source-search flags |
| `legendary_spellbooks:fossilized_fury` | `dev.higurashi.legendary_spellbooks.common.spells.nature.FossilizedFurySpell` | generic candidate; visual test pending; no extra source-search flags |
| `legendary_spellbooks:glacier_eruption` | `dev.higurashi.legendary_spellbooks.common.spells.ice.GlacierEruptionSpell` | generic candidate; visual test pending; no extra source-search flags |
| `legendary_spellbooks:glacier_ringburst` | `dev.higurashi.legendary_spellbooks.common.spells.ice.GlacierRingburstSpell` | generic candidate; visual test pending; no extra source-search flags |
| `legendary_spellbooks:hematite_trishula` | `dev.higurashi.legendary_spellbooks.common.spells.blood.HematiteTrishulaSpell` | generic candidate; visual test pending; no extra source-search flags |
| `legendary_spellbooks:nimbus_array` | `dev.higurashi.legendary_spellbooks.common.spells.lightning.NimbusArraySpell` | generic candidate; visual test pending; no extra source-search flags |
| `legendary_spellbooks:overgrown_shockwave` | `dev.higurashi.legendary_spellbooks.common.spells.nature.OvergrownShockwaveSpell` | adapter: recastsOnWideFloor(8, 6, 11); recast |
| `legendary_spellbooks:possessed_falling_soul_blade` | `dev.higurashi.legendary_spellbooks.common.spells.blood.PossessedFallingSoulBladeSpell` | generic candidate; visual test pending; no extra source-search flags |
| `legendary_spellbooks:possessed_soul_blade` | `dev.higurashi.legendary_spellbooks.common.spells.blood.PossessedSoulBladeSpell` | generic candidate; visual test pending; no extra source-search flags |
| `legendary_spellbooks:possessed_wing` | `dev.higurashi.legendary_spellbooks.common.spells.blood.PossessedWingSpell` | generic candidate; visual test pending; no extra source-search flags |
| `legendary_spellbooks:quad_tornado` | `dev.higurashi.legendary_spellbooks.common.spells.lightning.QuadTornadoSpell` | generic candidate; visual test pending; no extra source-search flags |
| `legendary_spellbooks:release_riftwalker_predator` | `dev.higurashi.legendary_spellbooks.common.spells.annihilation.ReleaseRiftwalkerPredatorSpell` | generic candidate; visual test pending; no extra source-search flags |
| `legendary_spellbooks:sentinel_saturation` | `dev.higurashi.legendary_spellbooks.common.spells.fire.SentinelSaturationSpell` | generic candidate; visual test pending; no extra source-search flags |
| `legendary_spellbooks:summon_flameborn_knights` | `dev.higurashi.legendary_spellbooks.common.spells.annihilation.SummonFlamebornKnightsSpell` | generic candidate; visual test pending; recast |
| `legendary_spellbooks:thunder_fanburst` | `dev.higurashi.legendary_spellbooks.common.spells.lightning.ThunderFanburstSpell` | generic candidate; visual test pending; no extra source-search flags |
| `legendary_spellbooks:tornado` | `dev.higurashi.legendary_spellbooks.common.spells.lightning.TornadoSpell` | generic candidate; visual test pending; cast predicate |
| `legendary_spellbooks:triple_nimbus_array` | `dev.higurashi.legendary_spellbooks.common.spells.lightning.TripleNimbusArraySpell` | generic candidate; visual test pending; no extra source-search flags |

## Legendary Spellbooks — 1.21.1

CurseForge file: 8255715; 35 inventory rows.

| Spell ID | Implementation (within the JAR) | Conditions / action |
| --- | --- | --- |
| (unresolved) | `dev.higurashi.legendary_spellbooks.api.spells.BaseDoubleSchoolSpell` | unresolved ID / helper class; not counted as compatible; no extra source-search flags |
| (unresolved) | `dev.higurashi.legendary_spellbooks.api.spells.BaseOverheadMeleeSwingSpell` | unresolved ID / helper class; not counted as compatible; no extra source-search flags |
| (unresolved) | `dev.higurashi.legendary_spellbooks.api.spells.BaseSpell` | unresolved ID / helper class; not counted as compatible; no extra source-search flags |
| (unresolved) | `dev.higurashi.legendary_spellbooks.api.spells.BaseSummonSpell` | unresolved ID / helper class; not counted as compatible; recast, summon lifecycle, player/world-sensitive |
| `legendary_spellbooks:ambush_thorns` | `dev.higurashi.legendary_spellbooks.common.spells.nature.AmbushThornsSpell` | generic candidate; visual test pending; no extra source-search flags |
| `legendary_spellbooks:annihilation_arrow` | `dev.higurashi.legendary_spellbooks.common.spells.annihilation.AnnihilationArrowSpell` | generic candidate; visual test pending; no extra source-search flags |
| `legendary_spellbooks:annihilation_beam` | `dev.higurashi.legendary_spellbooks.common.spells.annihilation.AnnihilationBeamSpell` | generic candidate; visual test pending; no extra source-search flags |
| `legendary_spellbooks:annihilation_bomb` | `dev.higurashi.legendary_spellbooks.common.spells.annihilation.AnnihilationBombSpell` | generic candidate; visual test pending; no extra source-search flags |
| `legendary_spellbooks:annihilation_geyser` | `dev.higurashi.legendary_spellbooks.common.spells.annihilation.AnnihilationGeyserSpell` | adapter: SPELLBOOK source and equipped registry-resolved Annihilators Protocol Curios book; no extra source-search flags |
| `legendary_spellbooks:annihilation_resonance` | `dev.higurashi.legendary_spellbooks.common.spells.annihilation.AnnihilationResonanceSpell` | adapter: criticalAttack(); no extra source-search flags |
| `legendary_spellbooks:annihilation_shockwave` | `dev.higurashi.legendary_spellbooks.common.spells.annihilation.AnnihilationShockwaveSpell` | generic candidate; visual test pending; no extra source-search flags |
| `legendary_spellbooks:cloud_rail` | `dev.higurashi.legendary_spellbooks.common.spells.lightning.CloudRailSpell` | generic candidate; visual test pending; no extra source-search flags |
| `legendary_spellbooks:cloud_ring` | `dev.higurashi.legendary_spellbooks.common.spells.lightning.CloudRingSpell` | generic candidate; visual test pending; no extra source-search flags |
| `legendary_spellbooks:collapsed_kingdoms_legion` | `dev.higurashi.legendary_spellbooks.common.spells.evocation.CollapsedKingdomsLegionSpell` | generic candidate; visual test pending; no extra source-search flags |
| `legendary_spellbooks:cumulo_charge` | `dev.higurashi.legendary_spellbooks.common.spells.lightning.CumuloChargeSpell` | generic candidate; visual test pending; cast predicate |
| `legendary_spellbooks:energy_beam` | `dev.higurashi.legendary_spellbooks.common.spells.lightning.EnergyBeamSpell` | adapter: SPELLBOOK source required by canBeCastedBy; no extra source-search flags |
| `legendary_spellbooks:flame_eater` | `dev.higurashi.legendary_spellbooks.common.spells.fire.FlameEaterSpell` | adapter: recasts(5, 12); recast, cast predicate, player/world-sensitive |
| `legendary_spellbooks:flame_sector` | `dev.higurashi.legendary_spellbooks.common.spells.fire.FlameSectorSpell` | generic candidate; visual test pending; no extra source-search flags |
| `legendary_spellbooks:flameborn_drift` | `dev.higurashi.legendary_spellbooks.common.spells.annihilation.FlamebornDriftSpell` | generic candidate; visual test pending; no extra source-search flags |
| `legendary_spellbooks:fossilized_fury` | `dev.higurashi.legendary_spellbooks.common.spells.nature.FossilizedFurySpell` | generic candidate; visual test pending; no extra source-search flags |
| `legendary_spellbooks:glacier_eruption` | `dev.higurashi.legendary_spellbooks.common.spells.ice.GlacierEruptionSpell` | generic candidate; visual test pending; no extra source-search flags |
| `legendary_spellbooks:glacier_ringburst` | `dev.higurashi.legendary_spellbooks.common.spells.ice.GlacierRingburstSpell` | generic candidate; visual test pending; no extra source-search flags |
| `legendary_spellbooks:hematite_trishula` | `dev.higurashi.legendary_spellbooks.common.spells.blood.HematiteTrishulaSpell` | generic candidate; visual test pending; no extra source-search flags |
| `legendary_spellbooks:nimbus_array` | `dev.higurashi.legendary_spellbooks.common.spells.lightning.NimbusArraySpell` | generic candidate; visual test pending; no extra source-search flags |
| `legendary_spellbooks:overgrown_shockwave` | `dev.higurashi.legendary_spellbooks.common.spells.nature.OvergrownShockwaveSpell` | adapter: recastsOnWideFloor(8, 6, 11); recast |
| `legendary_spellbooks:possessed_falling_soul_blade` | `dev.higurashi.legendary_spellbooks.common.spells.blood.PossessedFallingSoulBladeSpell` | generic candidate; visual test pending; no extra source-search flags |
| `legendary_spellbooks:possessed_soul_blade` | `dev.higurashi.legendary_spellbooks.common.spells.blood.PossessedSoulBladeSpell` | generic candidate; visual test pending; no extra source-search flags |
| `legendary_spellbooks:possessed_wing` | `dev.higurashi.legendary_spellbooks.common.spells.blood.PossessedWingSpell` | generic candidate; visual test pending; no extra source-search flags |
| `legendary_spellbooks:quad_tornado` | `dev.higurashi.legendary_spellbooks.common.spells.lightning.QuadTornadoSpell` | generic candidate; visual test pending; no extra source-search flags |
| `legendary_spellbooks:release_riftwalker_predator` | `dev.higurashi.legendary_spellbooks.common.spells.annihilation.ReleaseRiftwalkerPredatorSpell` | generic candidate; visual test pending; no extra source-search flags |
| `legendary_spellbooks:sentinel_saturation` | `dev.higurashi.legendary_spellbooks.common.spells.fire.SentinelSaturationSpell` | generic candidate; visual test pending; no extra source-search flags |
| `legendary_spellbooks:summon_flameborn_knights` | `dev.higurashi.legendary_spellbooks.common.spells.annihilation.SummonFlamebornKnightsSpell` | generic candidate; visual test pending; recast |
| `legendary_spellbooks:thunder_fanburst` | `dev.higurashi.legendary_spellbooks.common.spells.lightning.ThunderFanburstSpell` | generic candidate; visual test pending; no extra source-search flags |
| `legendary_spellbooks:tornado` | `dev.higurashi.legendary_spellbooks.common.spells.lightning.TornadoSpell` | generic candidate; visual test pending; cast predicate |
| `legendary_spellbooks:triple_nimbus_array` | `dev.higurashi.legendary_spellbooks.common.spells.lightning.TripleNimbusArraySpell` | generic candidate; visual test pending; no extra source-search flags |

## Cataclysm: Spellbooks — 1.20.1

CurseForge file: 7687059; 77 inventory rows.

| Spell ID | Implementation (within the JAR) | Conditions / action |
| --- | --- | --- |
| `cataclysm_spellbooks:abyss_fireball` | `net.acetheeldritchking.cataclysm_spellbooks.spells.fire.AbyssFireballSpell` | adapter: recasts(6, 12); recast, player/world-sensitive |
| `cataclysm_spellbooks:abyssal_blast` | `net.acetheeldritchking.cataclysm_spellbooks.spells.abyssal.AbyssalBlastSpell` | generic candidate; visual test pending; no extra source-search flags |
| `cataclysm_spellbooks:abyssal_predator` | `net.acetheeldritchking.cataclysm_spellbooks.spells.abyssal.AbyssalPredatorSpell` | generic candidate; visual test pending; no extra source-search flags |
| `cataclysm_spellbooks:abyssal_slash` | `net.acetheeldritchking.cataclysm_spellbooks.spells.abyssal.AbyssalSlashSpell` | generic candidate; visual test pending; no extra source-search flags |
| `cataclysm_spellbooks:aerial_assault` | `net.acetheeldritchking.cataclysm_spellbooks.spells.technomancy.AerialAssaultSpell` | generic candidate; visual test pending; no extra source-search flags |
| `cataclysm_spellbooks:amethyst_puncture` | `net.acetheeldritchking.cataclysm_spellbooks.spells.nature.AmethystPunctureSpell` | adapter: recasts(6, 12); recast, equipment/inventory |
| `cataclysm_spellbooks:ashen_breath` | `net.acetheeldritchking.cataclysm_spellbooks.spells.fire.AshenBreathSpell` | generic candidate; visual test pending; no extra source-search flags |
| `cataclysm_spellbooks:atomic_laser` | `net.acetheeldritchking.cataclysm_spellbooks.spells.technomancy.AtomicLaserSpell` | generic candidate; visual test pending; no extra source-search flags |
| `cataclysm_spellbooks:bone_storm` | `net.acetheeldritchking.cataclysm_spellbooks.spells.fire.BoneStormSpell` | generic candidate; visual test pending; no extra source-search flags |
| `cataclysm_spellbooks:comet_shower` | `net.acetheeldritchking.cataclysm_spellbooks.spells.fire.CometShowerSpell` | generic candidate; visual test pending; no extra source-search flags |
| `cataclysm_spellbooks:conjure_abyssal_gnawer` | language only | language entry only: stale/removed/unresolved; not counted as compatible; no extra source-search flags |
| `cataclysm_spellbooks:conjure_amethyst_crab` | `net.acetheeldritchking.cataclysm_spellbooks.spells.nature.ConjureAmethystCrabSpell` | generic summon; do not auto-dismiss with a recast; summon lifecycle |
| `cataclysm_spellbooks:conjure_clawdian` | `net.acetheeldritchking.cataclysm_spellbooks.spells.abyssal.ConjureClawdianSpell` | generic summon; do not auto-dismiss with a recast; summon lifecycle |
| `cataclysm_spellbooks:conjure_coral_golem` | `net.acetheeldritchking.cataclysm_spellbooks.spells.abyssal.ConjureCoralGolemSpell` | generic summon; do not auto-dismiss with a recast; summon lifecycle |
| `cataclysm_spellbooks:conjure_coralssus` | `net.acetheeldritchking.cataclysm_spellbooks.spells.abyssal.ConjureCoralssusSpell` | generic summon; do not auto-dismiss with a recast; summon lifecycle |
| `cataclysm_spellbooks:conjure_ignited_reinforcement` | `net.acetheeldritchking.cataclysm_spellbooks.spells.fire.ConjureIgnitedReinforcement` | generic summon; do not auto-dismiss with a recast; summon lifecycle |
| `cataclysm_spellbooks:conjure_koboldiator` | `net.acetheeldritchking.cataclysm_spellbooks.spells.holy.ConjureKoboldiatorSpell` | generic summon; do not auto-dismiss with a recast; summon lifecycle |
| `cataclysm_spellbooks:conjure_thralls` | `net.acetheeldritchking.cataclysm_spellbooks.spells.ice.ConjureThrallsSpell` | generic summon; do not auto-dismiss with a recast; summon lifecycle |
| `cataclysm_spellbooks:construct_prowler` | `net.acetheeldritchking.cataclysm_spellbooks.spells.technomancy.ConstructProwlerSpell` | generic summon; do not auto-dismiss with a recast; summon lifecycle |
| `cataclysm_spellbooks:construct_watchers` | `net.acetheeldritchking.cataclysm_spellbooks.spells.technomancy.ConstructWatchersSpell` | generic summon; do not auto-dismiss with a recast; summon lifecycle |
| `cataclysm_spellbooks:cryopiercer` | `net.acetheeldritchking.cataclysm_spellbooks.spells.ice.CryopiercerSpell` | adapter: recasts(6, 12); recast |
| `cataclysm_spellbooks:cursed_rush` | `net.acetheeldritchking.cataclysm_spellbooks.spells.ice.CursedRushSpell` | generic candidate; visual test pending; no extra source-search flags |
| `cataclysm_spellbooks:ddos` | `net.acetheeldritchking.cataclysm_spellbooks.spells.technomancy.DDoSSpell` | manual validation: player/inventory/world conditions; no adapter; player/world-sensitive |
| `cataclysm_spellbooks:depth_charge` | `net.acetheeldritchking.cataclysm_spellbooks.spells.abyssal.DepthChargeSpell` | generic candidate; visual test pending; no extra source-search flags |
| `cataclysm_spellbooks:desert_winds` | `net.acetheeldritchking.cataclysm_spellbooks.spells.nature.DesertWindsSpell` | generic candidate; visual test pending; no extra source-search flags |
| `cataclysm_spellbooks:dimensional_rift` | `net.acetheeldritchking.cataclysm_spellbooks.spells.abyssal.DimensionalRiftSpell` | generic candidate; visual test pending; no extra source-search flags |
| `cataclysm_spellbooks:disabling_swipe` | `net.acetheeldritchking.cataclysm_spellbooks.spells.technomancy.DisablingSwipeSpell` | adapter: recasts(6, 12); recast, player/world-sensitive |
| `cataclysm_spellbooks:dos_swarm` | `net.acetheeldritchking.cataclysm_spellbooks.spells.technomancy.DoSSwarmSpell` | generic summon; do not auto-dismiss with a recast; summon lifecycle |
| `cataclysm_spellbooks:emp_blast` | `net.acetheeldritchking.cataclysm_spellbooks.spells.technomancy.EMPSpell` | generic candidate; visual test pending; no extra source-search flags |
| `cataclysm_spellbooks:final_rend` | `net.acetheeldritchking.cataclysm_spellbooks.spells.blood.FinalRendSpell` | adapter: recasts(6, 12); recast |
| `cataclysm_spellbooks:flash_bang` | `net.acetheeldritchking.cataclysm_spellbooks.spells.technomancy.FlashBangSpell` | generic candidate; visual test pending; no extra source-search flags |
| `cataclysm_spellbooks:forgone_rage` | `net.acetheeldritchking.cataclysm_spellbooks.spells.ice.ForgoneRageSpell` | generic candidate; visual test pending; no extra source-search flags |
| `cataclysm_spellbooks:gear_shift` | `net.acetheeldritchking.cataclysm_spellbooks.spells.technomancy.GearShiftSpell` | generic candidate; visual test pending; recast |
| `cataclysm_spellbooks:gravitation_pull` | `net.acetheeldritchking.cataclysm_spellbooks.spells.ender.GravitationPullSpell` | generic candidate; visual test pending; no extra source-search flags |
| `cataclysm_spellbooks:gravity_storm` | `net.acetheeldritchking.cataclysm_spellbooks.spells.ender.GravityStormSpell` | generic candidate; visual test pending; no extra source-search flags |
| `cataclysm_spellbooks:hardware_update` | `net.acetheeldritchking.cataclysm_spellbooks.spells.technomancy.HardwareUpdateSpell` | generic candidate; visual test pending; no extra source-search flags |
| `cataclysm_spellbooks:hellish_blade` | `net.acetheeldritchking.cataclysm_spellbooks.spells.fire.HellishBladeSpell` | generic candidate; visual test pending; cast predicate |
| `cataclysm_spellbooks:hemorrhaging_impact` | `net.acetheeldritchking.cataclysm_spellbooks.spells.blood.HemorrhagingImpactSpell` | generic candidate; visual test pending; no extra source-search flags |
| `cataclysm_spellbooks:incineration` | `net.acetheeldritchking.cataclysm_spellbooks.spells.fire.IncinerationSpell` | generic candidate; visual test pending; no extra source-search flags |
| `cataclysm_spellbooks:infernal_strike` | `net.acetheeldritchking.cataclysm_spellbooks.spells.fire.InfernalStrikeSpell` | generic candidate; visual test pending; no extra source-search flags |
| `cataclysm_spellbooks:intrusion_prevention_system` | `net.acetheeldritchking.cataclysm_spellbooks.spells.technomancy.IntrusionPreventionSystemSpell` | manual validation: player/inventory/world conditions; no adapter; cast predicate, player/world-sensitive |
| `cataclysm_spellbooks:laserbolt` | `net.acetheeldritchking.cataclysm_spellbooks.spells.technomancy.LaserboltSpell` | adapter: recasts(6, 12); recast |
| `cataclysm_spellbooks:lock_on` | `net.acetheeldritchking.cataclysm_spellbooks.spells.technomancy.LockOnSpell` | generic candidate; visual test pending; cast predicate |
| `cataclysm_spellbooks:malevolent_battlefield` | `net.acetheeldritchking.cataclysm_spellbooks.spells.ice.MalevolentBattlefieldSpell` | generic candidate; visual test pending; no extra source-search flags |
| `cataclysm_spellbooks:meteor_spew` | `net.acetheeldritchking.cataclysm_spellbooks.spells.fire.MeteorSpewSpell` | adapter: recasts(6, 12); recast |
| `cataclysm_spellbooks:missile_launch` | `net.acetheeldritchking.cataclysm_spellbooks.spells.technomancy.MissileLaunchSpell` | adapter: recasts(6, 12); recast, cast predicate, player/world-sensitive |
| `cataclysm_spellbooks:monolith_crash` | `net.acetheeldritchking.cataclysm_spellbooks.spells.nature.MonolithCrashSpell` | generic candidate; visual test pending; no extra source-search flags |
| `cataclysm_spellbooks:overcharged` | `net.acetheeldritchking.cataclysm_spellbooks.spells.technomancy.OverchargedSpell` | manual validation: player/inventory/world conditions; no adapter; recast, equipment/inventory, player/world-sensitive |
| `cataclysm_spellbooks:parting_shot` | `net.acetheeldritchking.cataclysm_spellbooks.spells.technomancy.PartingShotSpell` | generic candidate; visual test pending; no extra source-search flags |
| `cataclysm_spellbooks:phantasmal_blade` | `net.acetheeldritchking.cataclysm_spellbooks.spells.ice.PhantasmalBladeSpell` | adapter: recasts(6, 12); recast |
| `cataclysm_spellbooks:pharaohs_wrath` | `net.acetheeldritchking.cataclysm_spellbooks.spells.nature.PharaohsWrathSpell` | generic candidate; visual test pending; no extra source-search flags |
| `cataclysm_spellbooks:piercing_bone` | `net.acetheeldritchking.cataclysm_spellbooks.spells.fire.BonePierceSpell` | adapter: recasts(6, 12); recast, player/world-sensitive |
| `cataclysm_spellbooks:pilfer` | `net.acetheeldritchking.cataclysm_spellbooks.spells.evocation.PilferSpell` | manual validation: player/inventory/world conditions; no adapter; cast predicate, equipment/inventory |
| `cataclysm_spellbooks:quick_strike` | `net.acetheeldritchking.cataclysm_spellbooks.spells.blood.QuickStrikeSpell` | adapter: recasts(6, 12); recast |
| `cataclysm_spellbooks:reboot` | `net.acetheeldritchking.cataclysm_spellbooks.spells.technomancy.RebootSpell` | generic candidate; visual test pending; no extra source-search flags |
| `cataclysm_spellbooks:rewire` | `net.acetheeldritchking.cataclysm_spellbooks.spells.technomancy.RewireSpell` | adapter: friendlySummon("cataclysm_spellbooks:summoned_watcher"); recast, cast predicate |
| `cataclysm_spellbooks:sandstorm` | `net.acetheeldritchking.cataclysm_spellbooks.spells.nature.SandstormSpell` | generic candidate; visual test pending; no extra source-search flags |
| `cataclysm_spellbooks:scorched_earth` | `net.acetheeldritchking.cataclysm_spellbooks.spells.fire.ScorchedEarthSpell` | generic candidate; visual test pending; recast |
| `cataclysm_spellbooks:shutdown` | `net.acetheeldritchking.cataclysm_spellbooks.spells.technomancy.ShutdownSpell` | generic candidate; visual test pending; cast predicate |
| `cataclysm_spellbooks:software_update` | `net.acetheeldritchking.cataclysm_spellbooks.spells.technomancy.SoftwareUpdateSpell` | generic candidate; visual test pending; no extra source-search flags |
| `cataclysm_spellbooks:summon_koboleton` | `net.acetheeldritchking.cataclysm_spellbooks.spells.holy.ConjureKoboletonSpell` | generic summon; do not auto-dismiss with a recast; summon lifecycle |
| `cataclysm_spellbooks:surveillance_drone` | `net.acetheeldritchking.cataclysm_spellbooks.spells.technomancy.SurveillanceDroneSpell` | generic candidate; visual test pending; cast predicate |
| `cataclysm_spellbooks:tectonic_tremble` | `net.acetheeldritchking.cataclysm_spellbooks.spells.fire.TectonicTrembleSpell` | generic candidate; visual test pending; no extra source-search flags |
| `cataclysm_spellbooks:thoths_witness` | `net.acetheeldritchking.cataclysm_spellbooks.spells.holy.ThothsWitnessSpell` | adapter: registered Pharaoh helmet before cast validation; real-client summon observed; summon lifecycle, equipment/inventory |
| `cataclysm_spellbooks:tidal_grab` | `net.acetheeldritchking.cataclysm_spellbooks.spells.abyssal.TidalGrabSpell` | generic candidate; visual test pending; cast predicate |
| `cataclysm_spellbooks:void_beam` | `net.acetheeldritchking.cataclysm_spellbooks.spells.abyssal.VoidBeamSpell` | generic candidate; visual test pending; recast, cast predicate |
| `cataclysm_spellbooks:void_bulwark` | `net.acetheeldritchking.cataclysm_spellbooks.spells.ender.VoidRuneBulwarkSpell` | generic candidate; visual test pending; no extra source-search flags |
| `cataclysm_spellbooks:void_rune` | `net.acetheeldritchking.cataclysm_spellbooks.spells.ender.VoidRuneSpell` | generic candidate; visual test pending; cast predicate |
| (unresolved) | `net.acetheeldritchking.cataclysm_spellbooks.spells.AbstractSummonSpell` | unresolved ID / helper class; not counted as compatible; recast, summon lifecycle, player/world-sensitive |
| (unresolved) | `net.acetheeldritchking.cataclysm_spellbooks.spells.abyssal.AbstractAbyssalSpell` | unresolved ID / helper class; not counted as compatible; no extra source-search flags |
| (unresolved) | `net.acetheeldritchking.cataclysm_spellbooks.spells.abyssal.AbstractAbyssalSummonSpell` | unresolved ID / helper class; not counted as compatible; summon lifecycle |
| (unresolved) | `net.acetheeldritchking.cataclysm_spellbooks.spells.fire.AbstractIgnisSpell` | unresolved ID / helper class; not counted as compatible; no extra source-search flags |
| (unresolved) | `net.acetheeldritchking.cataclysm_spellbooks.spells.fire.AbstractIgnisSummonSpell` | unresolved ID / helper class; not counted as compatible; summon lifecycle |
| (unresolved) | `net.acetheeldritchking.cataclysm_spellbooks.spells.ice.AbstractMaledictusSpell` | unresolved ID / helper class; not counted as compatible; no extra source-search flags |
| (unresolved) | `net.acetheeldritchking.cataclysm_spellbooks.spells.ice.AbstractMaledictusSummonSpell` | unresolved ID / helper class; not counted as compatible; summon lifecycle |
| (unresolved) | `net.acetheeldritchking.cataclysm_spellbooks.spells.technomancy.AbstractHarbingerSpell` | unresolved ID / helper class; not counted as compatible; no extra source-search flags |
| (unresolved) | `net.acetheeldritchking.cataclysm_spellbooks.spells.technomancy.AbstractHarbingerSummonSpell` | unresolved ID / helper class; not counted as compatible; summon lifecycle |

## Cataclysm: Spellbooks — 1.21.1

CurseForge file: 8847070; 69 inventory rows.

| Spell ID | Implementation (within the JAR) | Conditions / action |
| --- | --- | --- |
| `cataclysm_spellbooks:abyss_fireball` | `net.acetheeldritchking.cataclysm_spellbooks.spells.fire.AbyssFireballSpell` | adapter: recasts(6, 12); recast, player/world-sensitive |
| `cataclysm_spellbooks:abyssal_blast` | `net.acetheeldritchking.cataclysm_spellbooks.spells.abyssal.AbyssalBlastSpell` | generic candidate; visual test pending; no extra source-search flags |
| `cataclysm_spellbooks:abyssal_predator` | `net.acetheeldritchking.cataclysm_spellbooks.spells.abyssal.AbyssalPredatorSpell` | generic candidate; visual test pending; no extra source-search flags |
| `cataclysm_spellbooks:abyssal_slash` | `net.acetheeldritchking.cataclysm_spellbooks.spells.abyssal.AbyssalSlashSpell` | generic candidate; visual test pending; no extra source-search flags |
| `cataclysm_spellbooks:aerial_assault` | `net.acetheeldritchking.cataclysm_spellbooks.spells.technomancy.AerialAssaultSpell` | generic candidate; visual test pending; no extra source-search flags |
| `cataclysm_spellbooks:amethyst_puncture` | `net.acetheeldritchking.cataclysm_spellbooks.spells.nature.AmethystPunctureSpell` | adapter: recasts(6, 12); recast, equipment/inventory |
| `cataclysm_spellbooks:ashen_breath` | `net.acetheeldritchking.cataclysm_spellbooks.spells.fire.AshenBreathSpell` | generic candidate; visual test pending; no extra source-search flags |
| `cataclysm_spellbooks:atomic_laser` | `net.acetheeldritchking.cataclysm_spellbooks.spells.technomancy.AtomicLaserSpell` | generic candidate; visual test pending; no extra source-search flags |
| `cataclysm_spellbooks:bone_storm` | `net.acetheeldritchking.cataclysm_spellbooks.spells.fire.BoneStormSpell` | generic candidate; visual test pending; no extra source-search flags |
| `cataclysm_spellbooks:comet_shower` | `net.acetheeldritchking.cataclysm_spellbooks.spells.fire.CometShowerSpell` | generic candidate; visual test pending; no extra source-search flags |
| `cataclysm_spellbooks:conjure_abyssal_gnawer` | language only | language entry only: stale/removed/unresolved; not counted as compatible; no extra source-search flags |
| `cataclysm_spellbooks:conjure_amethyst_crab` | `net.acetheeldritchking.cataclysm_spellbooks.spells.nature.ConjureAmethystCrabSpell` | generic summon; do not auto-dismiss with a recast; summon lifecycle |
| `cataclysm_spellbooks:conjure_clawdian` | language only | language entry only: stale/removed/unresolved; not counted as compatible; no extra source-search flags |
| `cataclysm_spellbooks:conjure_coral_golem` | language only | language entry only: stale/removed/unresolved; not counted as compatible; no extra source-search flags |
| `cataclysm_spellbooks:conjure_coralssus` | language only | language entry only: stale/removed/unresolved; not counted as compatible; no extra source-search flags |
| `cataclysm_spellbooks:conjure_ignited_reinforcement` | `net.acetheeldritchking.cataclysm_spellbooks.spells.fire.ConjureIgnitedReinforcement` | generic summon; do not auto-dismiss with a recast; summon lifecycle |
| `cataclysm_spellbooks:conjure_koboldiator` | `net.acetheeldritchking.cataclysm_spellbooks.spells.holy.ConjureKobolediatorSpell` | manual validation: player/inventory/world conditions; no adapter; recast, summon lifecycle, player/world-sensitive |
| `cataclysm_spellbooks:conjure_thralls` | `net.acetheeldritchking.cataclysm_spellbooks.spells.ice.ConjureThrallsSpell` | manual validation: player/inventory/world conditions; no adapter; recast, summon lifecycle, player/world-sensitive |
| `cataclysm_spellbooks:construct_prowler` | `net.acetheeldritchking.cataclysm_spellbooks.spells.technomancy.ConstructProwlerSpell` | generic summon; do not auto-dismiss with a recast; summon lifecycle |
| `cataclysm_spellbooks:construct_watchers` | `net.acetheeldritchking.cataclysm_spellbooks.spells.technomancy.ConstructWatchersSpell` | generic summon; do not auto-dismiss with a recast; summon lifecycle |
| `cataclysm_spellbooks:cryopiercer` | language only | language entry only: stale/removed/unresolved; not counted as compatible; no extra source-search flags |
| `cataclysm_spellbooks:cursed_rush` | `net.acetheeldritchking.cataclysm_spellbooks.spells.ice.CursedRushSpell` | generic candidate; visual test pending; no extra source-search flags |
| `cataclysm_spellbooks:ddos` | `net.acetheeldritchking.cataclysm_spellbooks.spells.technomancy.DDoSSpell` | manual validation: player/inventory/world conditions; no adapter; player/world-sensitive |
| `cataclysm_spellbooks:depth_charge` | `net.acetheeldritchking.cataclysm_spellbooks.spells.abyssal.DepthChargeSpell` | generic candidate; visual test pending; no extra source-search flags |
| `cataclysm_spellbooks:desert_winds` | `net.acetheeldritchking.cataclysm_spellbooks.spells.nature.DesertWindsSpell` | generic candidate; visual test pending; no extra source-search flags |
| `cataclysm_spellbooks:dimensional_rift` | `net.acetheeldritchking.cataclysm_spellbooks.spells.abyssal.DimensionalRiftSpell` | generic candidate; visual test pending; no extra source-search flags |
| `cataclysm_spellbooks:disabling_swipe` | `net.acetheeldritchking.cataclysm_spellbooks.spells.technomancy.DisablingSwipeSpell` | adapter: recasts(6, 12); recast, equipment/inventory |
| `cataclysm_spellbooks:dos_swarm` | `net.acetheeldritchking.cataclysm_spellbooks.spells.technomancy.DoSSwarmSpell` | manual validation: player/inventory/world conditions; no adapter; summon lifecycle, cast predicate, player/world-sensitive |
| `cataclysm_spellbooks:emp_blast` | `net.acetheeldritchking.cataclysm_spellbooks.spells.technomancy.EMPSpell` | generic candidate; visual test pending; no extra source-search flags |
| `cataclysm_spellbooks:final_rend` | language only | language entry only: stale/removed/unresolved; not counted as compatible; no extra source-search flags |
| `cataclysm_spellbooks:flash_bang` | `net.acetheeldritchking.cataclysm_spellbooks.spells.technomancy.FlashBangSpell` | generic candidate; visual test pending; no extra source-search flags |
| `cataclysm_spellbooks:forgone_rage` | `net.acetheeldritchking.cataclysm_spellbooks.spells.ice.ForgoneRageSpell` | generic candidate; visual test pending; no extra source-search flags |
| `cataclysm_spellbooks:gear_shift` | `net.acetheeldritchking.cataclysm_spellbooks.spells.technomancy.GearShiftSpell` | generic candidate; visual test pending; recast |
| `cataclysm_spellbooks:gravitation_pull` | `net.acetheeldritchking.cataclysm_spellbooks.spells.ender.GravitationPullSpell` | generic candidate; visual test pending; no extra source-search flags |
| `cataclysm_spellbooks:gravity_storm` | `net.acetheeldritchking.cataclysm_spellbooks.spells.ender.GravityStormSpell` | generic candidate; visual test pending; no extra source-search flags |
| `cataclysm_spellbooks:hardware_update` | `net.acetheeldritchking.cataclysm_spellbooks.spells.technomancy.HardwareUpdateSpell` | generic candidate; visual test pending; no extra source-search flags |
| `cataclysm_spellbooks:hellish_blade` | `net.acetheeldritchking.cataclysm_spellbooks.spells.fire.HellishBladeSpell` | generic candidate; visual test pending; cast predicate |
| `cataclysm_spellbooks:hemorrhaging_impact` | language only | language entry only: stale/removed/unresolved; not counted as compatible; no extra source-search flags |
| `cataclysm_spellbooks:hijack` | `net.acetheeldritchking.cataclysm_spellbooks.spells.technomancy.HijackSpell` | generic candidate; visual test pending; cast predicate |
| `cataclysm_spellbooks:incineration` | `net.acetheeldritchking.cataclysm_spellbooks.spells.fire.IncinerationSpell` | generic candidate; visual test pending; no extra source-search flags |
| `cataclysm_spellbooks:infernal_strike` | `net.acetheeldritchking.cataclysm_spellbooks.spells.fire.InfernalStrikeSpell` | generic candidate; visual test pending; no extra source-search flags |
| `cataclysm_spellbooks:intrusion_prevention_system` | `net.acetheeldritchking.cataclysm_spellbooks.spells.technomancy.IntrusionPreventionSystemSpell` | manual validation: player/inventory/world conditions; no adapter; cast predicate, player/world-sensitive |
| `cataclysm_spellbooks:laserbolt` | `net.acetheeldritchking.cataclysm_spellbooks.spells.technomancy.LaserboltSpell` | adapter: recasts(6, 12); recast |
| `cataclysm_spellbooks:lock_on` | `net.acetheeldritchking.cataclysm_spellbooks.spells.technomancy.LockOnSpell` | generic candidate; visual test pending; cast predicate |
| `cataclysm_spellbooks:malevolent_battlefield` | `net.acetheeldritchking.cataclysm_spellbooks.spells.ice.MalevolentBattlefieldSpell` | generic candidate; visual test pending; no extra source-search flags |
| `cataclysm_spellbooks:meteor_spew` | `net.acetheeldritchking.cataclysm_spellbooks.spells.fire.MeteorSpewSpell` | adapter: recasts(6, 12); recast |
| `cataclysm_spellbooks:missile_launch` | `net.acetheeldritchking.cataclysm_spellbooks.spells.technomancy.MissileLaunchSpell` | adapter: recasts(6, 12); recast, cast predicate, player/world-sensitive |
| `cataclysm_spellbooks:monolith_crash` | `net.acetheeldritchking.cataclysm_spellbooks.spells.holy.MonolithCrashSpell` | generic candidate; visual test pending; no extra source-search flags |
| `cataclysm_spellbooks:overcharged` | `net.acetheeldritchking.cataclysm_spellbooks.spells.technomancy.OverchargedSpell` | manual validation: player/inventory/world conditions; no adapter; recast, equipment/inventory, player/world-sensitive |
| `cataclysm_spellbooks:parting_shot` | language only | language entry only: stale/removed/unresolved; not counted as compatible; no extra source-search flags |
| `cataclysm_spellbooks:phantasmal_blade` | `net.acetheeldritchking.cataclysm_spellbooks.spells.ice.PhantasmalBladeSpell` | adapter: recasts(6, 12); recast |
| `cataclysm_spellbooks:pharaohs_wrath` | `net.acetheeldritchking.cataclysm_spellbooks.spells.holy.PharaohsWrathSpell` | generic candidate; visual test pending; no extra source-search flags |
| `cataclysm_spellbooks:piercing_bone` | `net.acetheeldritchking.cataclysm_spellbooks.spells.fire.BonePierceSpell` | adapter: recasts(6, 12); recast, player/world-sensitive |
| `cataclysm_spellbooks:pilfer` | `net.acetheeldritchking.cataclysm_spellbooks.spells.evocation.PilferSpell` | manual validation: player/inventory/world conditions; no adapter; cast predicate, equipment/inventory |
| `cataclysm_spellbooks:quick_strike` | language only | language entry only: stale/removed/unresolved; not counted as compatible; no extra source-search flags |
| `cataclysm_spellbooks:reboot` | `net.acetheeldritchking.cataclysm_spellbooks.spells.technomancy.RebootSpell` | generic candidate; visual test pending; no extra source-search flags |
| `cataclysm_spellbooks:rewire` | `net.acetheeldritchking.cataclysm_spellbooks.spells.technomancy.RewireSpell` | adapter: friendlySummon("cataclysm_spellbooks:summoned_watcher"); recast, cast predicate |
| `cataclysm_spellbooks:sandstorm` | `net.acetheeldritchking.cataclysm_spellbooks.spells.nature.SandstormSpell` | generic candidate; visual test pending; no extra source-search flags |
| `cataclysm_spellbooks:scorched_earth` | language only | language entry only: stale/removed/unresolved; not counted as compatible; no extra source-search flags |
| `cataclysm_spellbooks:shutdown` | `net.acetheeldritchking.cataclysm_spellbooks.spells.technomancy.ShutdownSpell` | generic candidate; visual test pending; cast predicate |
| `cataclysm_spellbooks:software_update` | `net.acetheeldritchking.cataclysm_spellbooks.spells.technomancy.SoftwareUpdateSpell` | generic candidate; visual test pending; no extra source-search flags |
| `cataclysm_spellbooks:summon_koboleton` | `net.acetheeldritchking.cataclysm_spellbooks.spells.holy.ConjureKoboletonSpell` | generic summon; do not auto-dismiss with a recast; summon lifecycle |
| `cataclysm_spellbooks:surveillance_drone` | `net.acetheeldritchking.cataclysm_spellbooks.spells.technomancy.SurveillanceDroneSpell` | generic summon; do not auto-dismiss with a recast; summon lifecycle |
| `cataclysm_spellbooks:tectonic_tremble` | `net.acetheeldritchking.cataclysm_spellbooks.spells.fire.TectonicTrembleSpell` | manual validation: player/inventory/world conditions; no adapter; equipment/inventory |
| `cataclysm_spellbooks:thoths_witness` | `net.acetheeldritchking.cataclysm_spellbooks.spells.holy.ThothsWitnessSpell` | generic summon; do not auto-dismiss with a recast; summon lifecycle |
| `cataclysm_spellbooks:tidal_grab` | `net.acetheeldritchking.cataclysm_spellbooks.spells.abyssal.TidalGrabSpell` | generic candidate; visual test pending; cast predicate |
| `cataclysm_spellbooks:void_beam` | `net.acetheeldritchking.cataclysm_spellbooks.spells.abyssal.VoidBeamSpell` | generic candidate; visual test pending; recast, cast predicate |
| `cataclysm_spellbooks:void_bulwark` | `net.acetheeldritchking.cataclysm_spellbooks.spells.ender.VoidRuneBulwarkSpell` | generic candidate; visual test pending; no extra source-search flags |
| `cataclysm_spellbooks:void_rune` | `net.acetheeldritchking.cataclysm_spellbooks.spells.ender.VoidRuneSpell` | generic candidate; visual test pending; cast predicate |

## T.O Magic 'n Extras - Iron's Spells Addon — 1.20.1

CurseForge file: 7424522; 85 inventory rows.

| Spell ID | Implementation (within the JAR) | Conditions / action |
| --- | --- | --- |
| (unresolved) | `com.gametechbc.traveloptics.api.spells.AbstractUniqueSpell` | unresolved ID / helper class; not counted as compatible; no extra source-search flags |
| (unresolved) | `com.gametechbc.traveloptics.api.spells.AbstractWeaponSpell` | unresolved ID / helper class; not counted as compatible; no extra source-search flags |
| (unresolved) | `com.gametechbc.traveloptics.api.spells.AdvancedSpell` | unresolved ID / helper class; not counted as compatible; no extra source-search flags |
| `traveloptics:abyssal_blast` | `com.gametechbc.traveloptics.spells.eldritch.AbyssalBlastSpell` | generic candidate; visual test pending; no extra source-search flags |
| `traveloptics:aerial_collapse` | `com.gametechbc.traveloptics.spells.nature.AerialCollapseSpell` | generic candidate; visual test pending; cast predicate |
| `traveloptics:annihilation` | `com.gametechbc.traveloptics.spells.fire.AnnihilationSpell` | adapter: recasts(6, 12); recast |
| `traveloptics:apex_harvest` | `com.gametechbc.traveloptics.spells.eldritch.ApexHarvestSpell` | adapter: registry-resolved exclusive weapon before original checkPreCastConditions; cast predicate, equipment/inventory |
| `traveloptics:aqua_missiles` | `com.gametechbc.traveloptics.spells.aqua.AquaMissilesSpell` | generic candidate; visual test pending; no extra source-search flags |
| `traveloptics:arcane_artillery` | `com.gametechbc.traveloptics.spells.lightning.ArcaneArtillerySpell` | generic candidate; visual test pending; no extra source-search flags |
| `traveloptics:ashen_breath` | `com.gametechbc.traveloptics.spells.evocation.AshenBreathSpell` | generic candidate; visual test pending; no extra source-search flags |
| `traveloptics:astral_sense` | `com.gametechbc.traveloptics.spells.ender.AstralSenseSpell` | generic candidate; visual test pending; no extra source-search flags |
| `traveloptics:axe_of_the_doomed` | `com.gametechbc.traveloptics.spells.ice.AxeOfTheDoomedSpell` | manual validation: player/inventory/world conditions; no adapter; recast, summon lifecycle, cast predicate, player/world-sensitive |
| `traveloptics:banish` | `com.gametechbc.traveloptics.spells.holy.BanishSpell` | generic candidate; visual test pending; no extra source-search flags |
| `traveloptics:berserker` | language only | language entry only: stale/removed/unresolved; not counted as compatible; no extra source-search flags |
| `traveloptics:blackout` | `com.gametechbc.traveloptics.spells.eldritch.BlackoutSpell` | generic candidate; visual test pending; no extra source-search flags |
| `traveloptics:blink` | `com.gametechbc.traveloptics.spells.ender.BlinkSpell` | manual validation: player/inventory/world conditions; no adapter; player/world-sensitive |
| `traveloptics:blood_howl` | `com.gametechbc.traveloptics.spells.blood.BloodHowlSpell` | adapter: recasts(6, 12); recast |
| `traveloptics:bubble_spray` | `com.gametechbc.traveloptics.spells.aqua.BubbleSpraySpell` | generic candidate; visual test pending; no extra source-search flags |
| `traveloptics:burning_judgment` | `com.gametechbc.traveloptics.spells.fire.BurningJudgmentSpell` | generic candidate; visual test pending; no extra source-search flags |
| `traveloptics:call_forth_the_dead_king` | `com.gametechbc.traveloptics.spells.blood.CallForthTheDeadKingSpell` | adapter: SCROLL source required by canBeCastedBy; no extra source-search flags |
| `traveloptics:conjure_void_tomes` | `com.gametechbc.traveloptics.spells.ender.ConjureVoidTomesSpell` | manual validation: player/inventory/world conditions; no adapter; recast, summon lifecycle, player/world-sensitive |
| `traveloptics:coral_barrage` | `com.gametechbc.traveloptics.spells.aqua.CoralBarrageSpell` | adapter: recasts(6, 12); recast, cast predicate, player/world-sensitive |
| `traveloptics:cursed_blast` | language only | language entry only: stale/removed/unresolved; not counted as compatible; no extra source-search flags |
| `traveloptics:cursed_minefield` | `com.gametechbc.traveloptics.spells.ender.CursedMinefieldSpell` | generic candidate; visual test pending; no extra source-search flags |
| `traveloptics:cursed_revenants` | `com.gametechbc.traveloptics.spells.ice.CursedRevenantsSpell` | manual validation: player/inventory/world conditions; no adapter; recast, summon lifecycle, player/world-sensitive |
| `traveloptics:dawns_favor` | language only | language entry only: stale/removed/unresolved; not counted as compatible; no extra source-search flags |
| `traveloptics:death_laser` | `com.gametechbc.traveloptics.spells.lightning.DeathLaserSpell` | generic candidate; visual test pending; no extra source-search flags |
| `traveloptics:despair` | `com.gametechbc.traveloptics.spells.ice.DespairSpell` | adapter: recasts(6, 12); recast |
| `traveloptics:earthshatter` | language only | language entry only: stale/removed/unresolved; not counted as compatible; no extra source-search flags |
| `traveloptics:echo_of_the_abyss` | `com.gametechbc.traveloptics.spells.aqua.EchoOfTheAbyssSpell` | adapter: submerged(); recast, summon lifecycle, cast predicate, player/world-sensitive |
| `traveloptics:eek` | `com.gametechbc.traveloptics.spells.blood.EekSpell` | generic candidate; visual test pending; no extra source-search flags |
| `traveloptics:em_pulse` | `com.gametechbc.traveloptics.spells.lightning.EmPulse` | generic candidate; visual test pending; no extra source-search flags |
| `traveloptics:eternal_sentinel` | `com.gametechbc.traveloptics.spells.ender.EternalSentinelSpell` | manual validation: player/inventory/world conditions; no adapter; recast, summon lifecycle, cast predicate, player/world-sensitive |
| `traveloptics:extinction` | `com.gametechbc.traveloptics.spells.fire.ExtinctionSpell` | adapter: registry-resolved exclusive weapon before original checkPreCastConditions; cast predicate |
| `traveloptics:flames_reborn` | `com.gametechbc.traveloptics.spells.fire.FlamesRebornSpell` | generic candidate; visual test pending; no extra source-search flags |
| `traveloptics:flood_slash` | `com.gametechbc.traveloptics.spells.aqua.FloodSlashSpell` | generic candidate; visual test pending; no extra source-search flags |
| `traveloptics:floodgate` | `com.gametechbc.traveloptics.spells.aqua.FloodgateSpell` | generic candidate; visual test pending; no extra source-search flags |
| `traveloptics:galena_shatter` | `com.gametechbc.traveloptics.spells.lightning.GalenaShatterSpell` | adapter: registry-resolved exclusive weapon before original checkPreCastConditions; cast predicate, equipment/inventory |
| `traveloptics:gyro_slash` | `com.gametechbc.traveloptics.spells.fire.GyroSlashSpell` | adapter: registry-resolved exclusive weapon before original checkPreCastConditions; cast predicate |
| `traveloptics:halberd_horizon` | `com.gametechbc.traveloptics.spells.ice.HalberdHorizonSpell` | generic candidate; visual test pending; no extra source-search flags |
| `traveloptics:herald_of_acropolis` | `com.gametechbc.traveloptics.spells.aqua.HeraldOfAcropolisSpell` | manual validation: player/inventory/world conditions; no adapter; recast, summon lifecycle, cast predicate, player/world-sensitive |
| `traveloptics:hydroshot` | `com.gametechbc.traveloptics.spells.aqua.HydroshotSpell` | generic candidate; visual test pending; no extra source-search flags |
| `traveloptics:ignited_onslaught` | `com.gametechbc.traveloptics.spells.fire.IgnitedOnslaughtSpell` | manual validation: player/inventory/world conditions; no adapter; recast, summon lifecycle, cast predicate, player/world-sensitive |
| `traveloptics:jet_steam` | `com.gametechbc.traveloptics.spells.aqua.JetSteamSpell` | adapter: submerged swimming scene, flying ability off, brief follow-up; recast, cast predicate |
| `traveloptics:lava_bomb` | `com.gametechbc.traveloptics.spells.fire.LavaBombSpell` | generic candidate; visual test pending; no extra source-search flags |
| `traveloptics:lingering_strain` | `com.gametechbc.traveloptics.spells.evocation.LingeringStrainSpell` | generic candidate; visual test pending; no extra source-search flags |
| `traveloptics:magnetron_deployment` | `com.gametechbc.traveloptics.spells.lightning.MagnetronDeploymentSpell` | manual validation: player/inventory/world conditions; no adapter; recast, summon lifecycle, cast predicate, player/world-sensitive |
| `traveloptics:mechanized_predator` | `com.gametechbc.traveloptics.spells.lightning.MechanizedPredatorSpell` | manual validation: player/inventory/world conditions; no adapter; recast, summon lifecycle, cast predicate, player/world-sensitive |
| `traveloptics:meteor_storm` | `com.gametechbc.traveloptics.spells.fire.MeteorStormSpell` | generic candidate; visual test pending; no extra source-search flags |
| `traveloptics:nocturnal_swarm` | `com.gametechbc.traveloptics.spells.blood.NocturnalSwarmSpell` | adapter: recasts(6, 12); recast, summon lifecycle, player/world-sensitive |
| `traveloptics:nullflare` | `com.gametechbc.traveloptics.spells.holy.NullflareSpell` | adapter: recasts(6, 12); recast, custom packet path |
| `traveloptics:orbital_void` | `com.gametechbc.traveloptics.spells.ender.OrbitalVoidSpell` | generic candidate; visual test pending; no extra source-search flags |
| `traveloptics:overflow` | `com.gametechbc.traveloptics.spells.aqua.OverflowSpell` | generic candidate; visual test pending; no extra source-search flags |
| `traveloptics:primal_pack` | `com.gametechbc.traveloptics.spells.nature.PrimalPackSpell` | manual validation: player/inventory/world conditions; no adapter; recast, summon lifecycle, cast predicate, player/world-sensitive |
| `traveloptics:primordial_steed` | `com.gametechbc.traveloptics.spells.nature.PrimordialSteedSpell` | manual validation: player/inventory/world conditions; no adapter; recast, summon lifecycle, cast predicate, player/world-sensitive |
| `traveloptics:psychic_bolt` | `com.gametechbc.traveloptics.spells.eldritch.PsychicBoltSpell` | adapter: recasts(6, 12); recast, cast predicate, player/world-sensitive |
| `traveloptics:rainfall` | `com.gametechbc.traveloptics.spells.aqua.RainfallSpell` | manual validation: player/inventory/world conditions; no adapter; cast predicate, player/world-sensitive |
| `traveloptics:rapid_laser` | `com.gametechbc.traveloptics.spells.lightning.RapidLaserSpell` | generic candidate; visual test pending; no extra source-search flags |
| `traveloptics:reversal` | `com.gametechbc.traveloptics.spells.eldritch.ReversalSpell` | adapter: reversal(); cast predicate |
| `traveloptics:rune_swirl` | `com.gametechbc.traveloptics.spells.ender.RunicRadarSpell` | generic candidate; visual test pending; no extra source-search flags |
| `traveloptics:serpentide` | `com.gametechbc.traveloptics.spells.aqua.SerpentideSpell` | generic candidate; visual test pending; no extra source-search flags |
| `traveloptics:shadowed_miasma` | `com.gametechbc.traveloptics.spells.eldritch.ShadowedMiasmaSpell` | generic candidate; visual test pending; no extra source-search flags |
| `traveloptics:shear_of_the_stars` | `com.gametechbc.traveloptics.spells.ender.ShearOfTheStarsSpell` | adapter: registry-resolved exclusive weapon before original checkPreCastConditions; cast predicate |
| `traveloptics:skypiercer` | `com.gametechbc.traveloptics.spells.aqua.SkypiercerSpell` | adapter: registry-resolved exclusive weapon before original checkPreCastConditions; cast predicate |
| `traveloptics:solar_flare` | language only | language entry only: stale/removed/unresolved; not counted as compatible; no extra source-search flags |
| `traveloptics:spectral_blink` | `com.gametechbc.traveloptics.spells.eldritch.SpectralBlinkSpell` | manual validation: player/inventory/world conditions; no adapter; custom packet path, player/world-sensitive |
| `traveloptics:stele_cascade` | `com.gametechbc.traveloptics.spells.nature.SteleCascadeSpell` | generic candidate; visual test pending; no extra source-search flags |
| `traveloptics:sticky_steed_summon` | `com.gametechbc.traveloptics.spells.ice.StickySteedSummonSpell` | manual validation: player/inventory/world conditions; no adapter; recast, summon lifecycle, cast predicate, player/world-sensitive |
| `traveloptics:summon_desert_dwellers` | `com.gametechbc.traveloptics.spells.holy.SummonDesertDwellers` | manual validation: player/inventory/world conditions; no adapter; recast, summon lifecycle, cast predicate, equipment/inventory, player/world-sensitive |
| `traveloptics:sunbeam` | language only | language entry only: stale/removed/unresolved; not counted as compatible; no extra source-search flags |
| `traveloptics:supernova` | `com.gametechbc.traveloptics.spells.ender.SupernovaSpell` | adapter: registry-resolved exclusive weapon before original checkPreCastConditions; cast predicate |
| `traveloptics:swirl` | `com.gametechbc.traveloptics.spells.nature.SwirlSpell` | generic candidate; visual test pending; no extra source-search flags |
| `traveloptics:sword_of_the_ancients` | `com.gametechbc.traveloptics.spells.holy.SwordOfTheAncientsSpell` | manual validation: player/inventory/world conditions; no adapter; recast, summon lifecycle, cast predicate, player/world-sensitive |
| `traveloptics:tectonic_rift` | `com.gametechbc.traveloptics.spells.fire.TectonicRiftSpell` | adapter: registry-resolved exclusive weapon before original checkPreCastConditions; cast predicate |
| `traveloptics:the_forgotten_beast` | `com.gametechbc.traveloptics.spells.eldritch.TheForgottenBeastSpell` | manual validation: player/inventory/world conditions; no adapter; recast, summon lifecycle, cast predicate, player/world-sensitive |
| `traveloptics:the_howling_tempest` | `com.gametechbc.traveloptics.spells.aqua.TheHowlingTempestSpell` | generic candidate; visual test pending; no extra source-search flags |
| `traveloptics:tidal_grasp` | `com.gametechbc.traveloptics.spells.aqua.TidalGraspSpell` | generic candidate; visual test pending; cast predicate |
| `traveloptics:tsunami` | `com.gametechbc.traveloptics.spells.aqua.TsunamiSpell` | generic candidate; visual test pending; no extra source-search flags |
| `traveloptics:venomous_rift` | `com.gametechbc.traveloptics.spells.nature.VenomousRiftSpell` | generic candidate; visual test pending; no extra source-search flags |
| `traveloptics:vigor_siphon` | `com.gametechbc.traveloptics.spells.blood.VigorSiphonSpell` | generic candidate; visual test pending; no extra source-search flags |
| `traveloptics:violent_skreech` | `com.gametechbc.traveloptics.compat.spells.eldritch.ViolentSkreechSpell` | generic candidate; visual test pending; no extra source-search flags |
| `traveloptics:void_devourer` | `com.gametechbc.traveloptics.spells.eldritch.VoidDevourerSpell` | generic candidate; visual test pending; no extra source-search flags |
| `traveloptics:void_eruption` | `com.gametechbc.traveloptics.spells.ender.VoidEruptionSpell` | adapter: recasts(6, 12); recast |
| `traveloptics:vortex_of_the_deep` | `com.gametechbc.traveloptics.spells.aqua.VortexOfTheDeepSpell` | generic candidate; visual test pending; cast predicate |
| `traveloptics:vortex_punch` | `com.gametechbc.traveloptics.spells.ender.VortexPunchSpell` | generic candidate; visual test pending; cast predicate |

## T.O Magic 'n Extras - Iron's Spells Addon — 1.21.1

CurseForge file: 6342780; 67 inventory rows.

| Spell ID | Implementation (within the JAR) | Conditions / action |
| --- | --- | --- |
| (unresolved) | `com.gametechbc.traveloptics.api.spells.AbstractUniqueSpell` | excluded: upstream deprecated release; no extra source-search flags |
| (unresolved) | `com.gametechbc.traveloptics.api.spells.AbstractWeaponSpell` | excluded: upstream deprecated release; no extra source-search flags |
| `traveloptics:abyssal_blast` | `com.gametechbc.traveloptics.spells.eldritch.AbyssalBlastSpell` | excluded: upstream deprecated release; no extra source-search flags |
| `traveloptics:aerial_collapse` | `com.gametechbc.traveloptics.spells.nature.AerialCollapseSpell` | excluded: upstream deprecated release; cast predicate |
| `traveloptics:annihilation` | language only | language entry only: stale/removed/unresolved; not counted as compatible; no extra source-search flags |
| `traveloptics:aqua_missiles` | language only | language entry only: stale/removed/unresolved; not counted as compatible; no extra source-search flags |
| `traveloptics:ashen_breath` | `com.gametechbc.traveloptics.spells.evocation.AshenBreathSpell` | excluded: upstream deprecated release; no extra source-search flags |
| `traveloptics:astral_sense` | `com.gametechbc.traveloptics.spells.ender.AstralSenseSpell` | excluded: upstream deprecated release; no extra source-search flags |
| `traveloptics:axe_of_the_doomed` | `com.gametechbc.traveloptics.spells.ice.AxeOfTheDoomedSpell` | excluded: upstream deprecated release; cast predicate |
| `traveloptics:berserker` | language only | language entry only: stale/removed/unresolved; not counted as compatible; no extra source-search flags |
| `traveloptics:blackout` | `com.gametechbc.traveloptics.spells.eldritch.BlackoutSpell` | excluded: upstream deprecated release; no extra source-search flags |
| `traveloptics:blood_howl` | `com.gametechbc.traveloptics.spells.blood.BloodHowlSpell` | excluded: upstream deprecated release; recast |
| `traveloptics:bubble_spray` | language only | language entry only: stale/removed/unresolved; not counted as compatible; no extra source-search flags |
| `traveloptics:burning_judgment` | `com.gametechbc.traveloptics.spells.fire.BurningJudgmentSpell` | excluded: upstream deprecated release; no extra source-search flags |
| `traveloptics:call_forth_the_dead_king` | language only | language entry only: stale/removed/unresolved; not counted as compatible; no extra source-search flags |
| `traveloptics:coral_barrage` | language only | language entry only: stale/removed/unresolved; not counted as compatible; no extra source-search flags |
| `traveloptics:cursed_blast` | `com.gametechbc.traveloptics.spells.ice.CursedBlastSpell` | excluded: upstream deprecated release; cast predicate |
| `traveloptics:cursed_minefield` | `com.gametechbc.traveloptics.spells.ender.CursedMinefieldSpell` | excluded: upstream deprecated release; no extra source-search flags |
| `traveloptics:cursed_revenants` | `com.gametechbc.traveloptics.spells.ice.CursedRevenantsSpell` | excluded: upstream deprecated release; no extra source-search flags |
| `traveloptics:dawns_favor` | language only | language entry only: stale/removed/unresolved; not counted as compatible; no extra source-search flags |
| `traveloptics:death_laser` | `com.gametechbc.traveloptics.spells.lightning.DeathLaserSpell` | excluded: upstream deprecated release; no extra source-search flags |
| `traveloptics:despair` | `com.gametechbc.traveloptics.spells.ice.DespairSpell` | excluded: upstream deprecated release; recast |
| `traveloptics:earthshatter` | language only | language entry only: stale/removed/unresolved; not counted as compatible; no extra source-search flags |
| `traveloptics:echo_of_the_abyss` | language only | language entry only: stale/removed/unresolved; not counted as compatible; no extra source-search flags |
| `traveloptics:eek` | language only | language entry only: stale/removed/unresolved; not counted as compatible; no extra source-search flags |
| `traveloptics:em_pulse` | `com.gametechbc.traveloptics.spells.lightning.EmPulse` | excluded: upstream deprecated release; no extra source-search flags |
| `traveloptics:eternal_sentinel` | `com.gametechbc.traveloptics.spells.ender.EternalSentinelSpell` | excluded: upstream deprecated release; cast predicate |
| `traveloptics:extinction` | language only | language entry only: stale/removed/unresolved; not counted as compatible; no extra source-search flags |
| `traveloptics:flood_slash` | language only | language entry only: stale/removed/unresolved; not counted as compatible; no extra source-search flags |
| `traveloptics:floodgate` | language only | language entry only: stale/removed/unresolved; not counted as compatible; no extra source-search flags |
| `traveloptics:gyro_slash` | `com.gametechbc.traveloptics.spells.fire.GyroSlashSpell` | excluded: upstream deprecated release; cast predicate |
| `traveloptics:halberd_horizon` | `com.gametechbc.traveloptics.spells.ice.HalberdHorizonSpell` | excluded: upstream deprecated release; no extra source-search flags |
| `traveloptics:hydroshot` | language only | language entry only: stale/removed/unresolved; not counted as compatible; no extra source-search flags |
| `traveloptics:ignited_onslaught` | `com.gametechbc.traveloptics.spells.fire.IgnitedOnslaughtSpell` | excluded: upstream deprecated release; cast predicate |
| `traveloptics:jet_steam` | language only | language entry only: stale/removed/unresolved; not counted as compatible; no extra source-search flags |
| `traveloptics:lava_bomb` | `com.gametechbc.traveloptics.spells.fire.LavaBombSpell` | excluded: upstream deprecated release; no extra source-search flags |
| `traveloptics:lingering_strain` | `com.gametechbc.traveloptics.spells.evocation.LingeringStrainSpell` | excluded: upstream deprecated release; no extra source-search flags |
| `traveloptics:magnetron_deployment` | language only | language entry only: stale/removed/unresolved; not counted as compatible; no extra source-search flags |
| `traveloptics:mechanized_predator` | `com.gametechbc.traveloptics.spells.lightning.MechanizedPredatorSpell` | excluded: upstream deprecated release; cast predicate |
| `traveloptics:meteor_storm` | `com.gametechbc.traveloptics.spells.fire.MeteorStormSpell` | excluded: upstream deprecated release; no extra source-search flags |
| `traveloptics:nocturnal_swarm` | language only | language entry only: stale/removed/unresolved; not counted as compatible; no extra source-search flags |
| `traveloptics:nullflare` | `com.gametechbc.traveloptics.spells.holy.NullflareSpell` | excluded: upstream deprecated release; recast |
| `traveloptics:orbital_void` | `com.gametechbc.traveloptics.spells.ender.OrbitalVoidSpell` | excluded: upstream deprecated release; no extra source-search flags |
| `traveloptics:primal_pack` | language only | language entry only: stale/removed/unresolved; not counted as compatible; no extra source-search flags |
| `traveloptics:primordial_steed` | language only | language entry only: stale/removed/unresolved; not counted as compatible; no extra source-search flags |
| `traveloptics:psychic_bolt` | `com.gametechbc.traveloptics.spells.eldritch.PsychicBoltSpell` | excluded: upstream deprecated release; recast, cast predicate, player/world-sensitive |
| `traveloptics:rainfall` | language only | language entry only: stale/removed/unresolved; not counted as compatible; no extra source-search flags |
| `traveloptics:rapid_laser` | `com.gametechbc.traveloptics.spells.lightning.RapidLaserSpell` | excluded: upstream deprecated release; no extra source-search flags |
| `traveloptics:reversal` | `com.gametechbc.traveloptics.spells.eldritch.ReversalSpell` | excluded: upstream deprecated release; cast predicate |
| `traveloptics:shadowed_miasma` | language only | language entry only: stale/removed/unresolved; not counted as compatible; no extra source-search flags |
| `traveloptics:solar_flare` | language only | language entry only: stale/removed/unresolved; not counted as compatible; no extra source-search flags |
| `traveloptics:spectral_blink` | `com.gametechbc.traveloptics.spells.eldritch.SpectralBlinkSpell` | excluded: upstream deprecated release; no extra source-search flags |
| `traveloptics:stele_cascade` | `com.gametechbc.traveloptics.spells.nature.SteleCascadeSpell` | excluded: upstream deprecated release; no extra source-search flags |
| `traveloptics:sticky_steed_summon` | language only | language entry only: stale/removed/unresolved; not counted as compatible; no extra source-search flags |
| `traveloptics:summon_desert_dwellers` | `com.gametechbc.traveloptics.spells.holy.SummonDesertDwellers` | excluded: upstream deprecated release; cast predicate, equipment/inventory |
| `traveloptics:sunbeam` | language only | language entry only: stale/removed/unresolved; not counted as compatible; no extra source-search flags |
| `traveloptics:sword_of_the_ancients` | `com.gametechbc.traveloptics.spells.holy.SwordOfTheAncientsSpell` | excluded: upstream deprecated release; cast predicate |
| `traveloptics:tectonic_rift` | language only | language entry only: stale/removed/unresolved; not counted as compatible; no extra source-search flags |
| `traveloptics:the_forgotten_beast` | language only | language entry only: stale/removed/unresolved; not counted as compatible; no extra source-search flags |
| `traveloptics:tidal_grasp` | language only | language entry only: stale/removed/unresolved; not counted as compatible; no extra source-search flags |
| `traveloptics:tsunami` | language only | language entry only: stale/removed/unresolved; not counted as compatible; no extra source-search flags |
| `traveloptics:vigor_siphon` | language only | language entry only: stale/removed/unresolved; not counted as compatible; no extra source-search flags |
| `traveloptics:violent_skreech` | language only | language entry only: stale/removed/unresolved; not counted as compatible; no extra source-search flags |
| `traveloptics:void_devourer` | language only | language entry only: stale/removed/unresolved; not counted as compatible; no extra source-search flags |
| `traveloptics:void_eruption` | `com.gametechbc.traveloptics.spells.ender.VoidEruptionSpell` | excluded: upstream deprecated release; recast |
| `traveloptics:vortex_of_the_deep` | language only | language entry only: stale/removed/unresolved; not counted as compatible; no extra source-search flags |
| `traveloptics:vortex_punch` | `com.gametechbc.traveloptics.spells.ender.VortexPunchSpell` | excluded: upstream deprecated release; cast predicate |

## Alshanex's Familiars — 1.20.1

CurseForge file: 9052384; 15 inventory rows.

| Spell ID | Implementation (within the JAR) | Conditions / action |
| --- | --- | --- |
| `alshanex_familiars:birds_spell` | `net.alshanex.alshanex_familiars.spells.BirdsSpell` | manual validation: player/inventory/world conditions; no adapter; recast, summon lifecycle, player/world-sensitive |
| `alshanex_familiars:default_note` | `net.alshanex.alshanex_familiars.spells.DefaultNoteSpell` | adapter: recasts(6, 12); recast |
| `alshanex_familiars:end_mayhem` | `net.alshanex.alshanex_familiars.spells.EndMayhemSpell` | generic candidate; visual test pending; no extra source-search flags |
| `alshanex_familiars:explosion_melody` | `net.alshanex.alshanex_familiars.spells.ExplosionMelodySpell` | generic candidate; visual test pending; cast predicate |
| `alshanex_familiars:fire_fist` | `net.alshanex.alshanex_familiars.spells.HikenSpell` | generic candidate; visual test pending; no extra source-search flags |
| `alshanex_familiars:guardian_angel` | `net.alshanex.alshanex_familiars.spells.AngelSpell` | generic candidate; visual test pending; cast predicate |
| `alshanex_familiars:harp_symphony` | `net.alshanex.alshanex_familiars.spells.VibrationSpell` | generic candidate; visual test pending; no extra source-search flags |
| `alshanex_familiars:ice_age` | `net.alshanex.alshanex_familiars.spells.IceAgeSpell` | generic candidate; visual test pending; custom packet path |
| `alshanex_familiars:ice_chamber` | `net.alshanex.alshanex_familiars.spells.IceChamberSpell` | generic candidate; visual test pending; no extra source-search flags |
| `alshanex_familiars:lullaby` | `net.alshanex.alshanex_familiars.spells.LullabySpell` | manual validation: player/inventory/world conditions; no adapter; player/world-sensitive |
| `alshanex_familiars:megido` | `net.alshanex.alshanex_familiars.spells.MegidoSpell` | generic candidate; visual test pending; cast predicate |
| `alshanex_familiars:music_bolt` | `net.alshanex.alshanex_familiars.spells.MusicBoltSpell` | generic candidate; visual test pending; no extra source-search flags |
| `alshanex_familiars:sonata` | `net.alshanex.alshanex_familiars.spells.SonataSpell` | manual validation: player/inventory/world conditions; no adapter; player/world-sensitive |
| `alshanex_familiars:summon_shadows` | `net.alshanex.alshanex_familiars.spells.ShadowSummonSpell` | manual validation: player/inventory/world conditions; no adapter; recast, summon lifecycle, player/world-sensitive |
| (unresolved) | `net.alshanex.alshanex_familiars.mixin.CleanseSpellMixin` | unresolved ID / helper class; not counted as compatible; no extra source-search flags |

## Alshanex's Familiars — 1.21.1

CurseForge file: 9276628; 8 inventory rows.

| Spell ID | Implementation (within the JAR) | Conditions / action |
| --- | --- | --- |
| `alshanex_familiars:end_mayhem` | `net.alshanex.alshanex_familiars.spells.EndMayhemSpell` | generic candidate; visual test pending; no extra source-search flags |
| `alshanex_familiars:fire_fist` | `net.alshanex.alshanex_familiars.spells.HikenSpell` | generic candidate; visual test pending; no extra source-search flags |
| `alshanex_familiars:ice_age` | `net.alshanex.alshanex_familiars.spells.IceAgeSpell` | generic candidate; visual test pending; custom packet path |
| `alshanex_familiars:ice_chamber` | `net.alshanex.alshanex_familiars.spells.IceChamberSpell` | generic candidate; visual test pending; no extra source-search flags |
| `alshanex_familiars:megido` | `net.alshanex.alshanex_familiars.spells.MegidoSpell` | generic candidate; visual test pending; cast predicate |
| `alshanex_familiars:summon_shadows` | `net.alshanex.alshanex_familiars.spells.ShadowSummonSpell` | manual validation: player/inventory/world conditions; no adapter; recast, summon lifecycle, player/world-sensitive |
| `alshanex_familiars:switcheroo` | `net.alshanex.alshanex_familiars.spells.SwitcherooSpell` | adapter: registry-resolved mage pet target with reflected owner UUID prerequisite; cast predicate, custom packet path, player/world-sensitive |
| (unresolved) | `net.alshanex.alshanex_familiars.mixin.CleanseSpellMixin` | unresolved ID / helper class; not counted as compatible; no extra source-search flags |

## Hazen 'N Stuff — 1.20.1

CurseForge file: 7501581; 16 inventory rows.

| Spell ID | Implementation (within the JAR) | Conditions / action |
| --- | --- | --- |
| `hazennstuff:brimstone_hellblast` | language only | language entry only: stale/removed/unresolved; not counted as compatible; no extra source-search flags |
| `hazennstuff:chaotic_teleport` | language only | language entry only: stale/removed/unresolved; not counted as compatible; no extra source-search flags |
| `hazennstuff:cinderous_step` | language only | language entry only: stale/removed/unresolved; not counted as compatible; no extra source-search flags |
| `hazennstuff:counterspell_spider_lily` | language only | language entry only: stale/removed/unresolved; not counted as compatible; no extra source-search flags |
| `hazennstuff:crystal_volley` | language only | language entry only: stale/removed/unresolved; not counted as compatible; no extra source-search flags |
| `hazennstuff:death_sentence` | language only | language entry only: stale/removed/unresolved; not counted as compatible; no extra source-search flags |
| `hazennstuff:energy_burst` | language only | language entry only: stale/removed/unresolved; not counted as compatible; no extra source-search flags |
| `hazennstuff:golden_shower` | language only | language entry only: stale/removed/unresolved; not counted as compatible; no extra source-search flags |
| `hazennstuff:ice_arrow` | language only | language entry only: stale/removed/unresolved; not counted as compatible; no extra source-search flags |
| `hazennstuff:nights_edge_strike` | language only | language entry only: stale/removed/unresolved; not counted as compatible; no extra source-search flags |
| `hazennstuff:scorching_slash` | language only | language entry only: stale/removed/unresolved; not counted as compatible; no extra source-search flags |
| `hazennstuff:shard_sword` | language only | language entry only: stale/removed/unresolved; not counted as compatible; no extra source-search flags |
| `hazennstuff:soul_seekers` | language only | language entry only: stale/removed/unresolved; not counted as compatible; no extra source-search flags |
| `hazennstuff:spectral_axe` | language only | language entry only: stale/removed/unresolved; not counted as compatible; no extra source-search flags |
| `hazennstuff:syringe_barrage` | language only | language entry only: stale/removed/unresolved; not counted as compatible; no extra source-search flags |
| `hazennstuff:thorn_chakram` | language only | language entry only: stale/removed/unresolved; not counted as compatible; no extra source-search flags |

## Hazen 'N Stuff — 1.21.1

CurseForge file: 8277644; 46 inventory rows.

| Spell ID | Implementation (within the JAR) | Conditions / action |
| --- | --- | --- |
| `hazennstuff:arcane_cards` | `net.hazen.hazennstuff.Spells.Schools.Shadow.ArcaneCardsSpell` | generic candidate; visual test pending; no extra source-search flags |
| `hazennstuff:bone_bolt` | `net.hazen.hazennstuff.Spells.Schools.Blood.BoneBoltSpell` | generic candidate; visual test pending; no extra source-search flags |
| `hazennstuff:brimstone_hellblast` | language only | language entry only: stale/removed/unresolved; not counted as compatible; no extra source-search flags |
| `hazennstuff:call_forth_terraprisma` | `net.hazen.hazennstuff.Spells.Schools.Radiance.CallForthTerraprismaSpell` | manual validation: player/inventory/world conditions; no adapter; recast, summon lifecycle, player/world-sensitive |
| `hazennstuff:cinderous_step` | `net.hazen.hazennstuff.Spells.Schools.Fire.CinderousStepSpell` | generic candidate; visual test pending; no extra source-search flags |
| `hazennstuff:cosmic_bolt` | `net.hazen.hazennstuff.Spells.Schools.Cosmic.CosmicBoltSpell` | generic candidate; visual test pending; no extra source-search flags |
| `hazennstuff:counterspell_spider_lily` | `net.hazen.hazennstuff.Spells.Schools.Nature.CounterspellSpiderLilySpell` | generic candidate; visual test pending; no extra source-search flags |
| `hazennstuff:dazzling_obliteration` | `net.hazen.hazennstuff.Spells.Schools.Lightning.DazzlingObliterationSpell` | adapter: recasts(12, 3); recast, cast predicate |
| `hazennstuff:death_sentence` | `net.hazen.hazennstuff.Spells.Schools.Nature.DeathSentenceSpell` | generic candidate; visual test pending; no extra source-search flags |
| `hazennstuff:endraconic_meteor` | `net.hazen.hazennstuff.Spells.Schools.Ender.EndraconicMeteorSpell` | generic candidate; visual test pending; no extra source-search flags |
| `hazennstuff:energy_burst` | `net.hazen.hazennstuff.Spells.Schools.Lightning.EnergyBurstSpell` | generic candidate; visual test pending; no extra source-search flags |
| `hazennstuff:evercomet_barrage` | `net.hazen.hazennstuff.Spells.Schools.Cosmic.EvercometBarrageSpell` | generic candidate; visual test pending; no extra source-search flags |
| `hazennstuff:fiery_dagger` | language only | language entry only: stale/removed/unresolved; not counted as compatible; no extra source-search flags |
| `hazennstuff:golden_shower` | `net.hazen.hazennstuff.Spells.AbstractSpells.GoldenShowerSpell` | generic candidate; visual test pending; no extra source-search flags |
| `hazennstuff:hailstorm` | `net.hazen.hazennstuff.Spells.Schools.Ice.HailstormSpell` | generic candidate; visual test pending; no extra source-search flags |
| `hazennstuff:horn_shell` | language only | language entry only: stale/removed/unresolved; not counted as compatible; no extra source-search flags |
| `hazennstuff:hydrobullet` | language only | language entry only: stale/removed/unresolved; not counted as compatible; no extra source-search flags |
| `hazennstuff:ice_arrow` | `net.hazen.hazennstuff.Spells.Schools.Ice.IceArrowSpell` | generic candidate; visual test pending; no extra source-search flags |
| `hazennstuff:ionic_slash` | `net.hazen.hazennstuff.Spells.Schools.Lightning.IonicSlashSpell` | manual validation: player/inventory/world conditions; no adapter; equipment/inventory, player/world-sensitive |
| `hazennstuff:moonkissed` | `net.hazen.hazennstuff.Spells.Schools.Cosmic.MoonkissedSpell` | generic candidate; visual test pending; no extra source-search flags |
| `hazennstuff:nights_edge_strike` | `net.hazen.hazennstuff.Spells.Schools.Shadow.NightsEdgeStrikeSpell` | manual validation: player/inventory/world conditions; no adapter; equipment/inventory |
| `hazennstuff:parry` | `net.hazen.hazennstuff.Spells.Schools.Evocation.ParrySpell` | generic candidate; visual test pending; no extra source-search flags |
| `hazennstuff:prismatic_shift` | `net.hazen.hazennstuff.Spells.Schools.Radiance.PrismaticShiftSpell` | manual validation: player/inventory/world conditions; no adapter; player/world-sensitive |
| `hazennstuff:razorblade_typhoon` | language only | language entry only: stale/removed/unresolved; not counted as compatible; no extra source-search flags |
| `hazennstuff:reign_of_tyros` | `net.hazen.hazennstuff.Spells.Schools.Fire.ReignOfTyrosSpell` | manual validation: player/inventory/world conditions; no adapter; recast, summon lifecycle, equipment/inventory, player/world-sensitive |
| `hazennstuff:scorching_slash` | `net.hazen.hazennstuff.Spells.Schools.Fire.ScorchingSlashSpell` | manual validation: player/inventory/world conditions; no adapter; equipment/inventory |
| `hazennstuff:shadow_reaver` | `net.hazen.hazennstuff.Spells.Schools.Shadow.ShadowReaverSpell` | generic candidate; visual test pending; no extra source-search flags |
| `hazennstuff:shard_sword` | `net.hazen.hazennstuff.Spells.Schools.Nature.ShardSwordSpell` | manual validation: player/inventory/world conditions; no adapter; equipment/inventory |
| `hazennstuff:shooting_star` | `net.hazen.hazennstuff.Spells.Schools.Cosmic.ShootingStarSpell` | generic candidate; visual test pending; cast predicate |
| `hazennstuff:soul_flame_bolt` | `net.hazen.hazennstuff.Spells.Schools.Fire.SoulflameBoltSpell` | generic candidate; visual test pending; no extra source-search flags |
| `hazennstuff:soul_flaming_strike` | `net.hazen.hazennstuff.Spells.Schools.Fire.SoulFlamingStrikeSpell` | manual validation: player/inventory/world conditions; no adapter; equipment/inventory |
| `hazennstuff:soul_seekers` | `net.hazen.hazennstuff.Spells.Schools.Eldritch.SoulSeekersSpell` | generic candidate; visual test pending; no extra source-search flags |
| `hazennstuff:spectral_axe` | `net.hazen.hazennstuff.Spells.Schools.Evocation.SpectralAxeSpell` | generic candidate; visual test pending; no extra source-search flags |
| `hazennstuff:supernova` | language only | language entry only: stale/removed/unresolved; not counted as compatible; no extra source-search flags |
| `hazennstuff:syringe_barrage` | `net.hazen.hazennstuff.Spells.Schools.Radiance.SyringeBarrageSpell` | generic candidate; visual test pending; no extra source-search flags |
| `hazennstuff:terraprismic_barrage` | `net.hazen.hazennstuff.Spells.Schools.Radiance.TerraprismicBarrageSpell` | generic candidate; visual test pending; no extra source-search flags |
| `hazennstuff:thorn_chakram` | `net.hazen.hazennstuff.Spells.Schools.Nature.ThornChakramSpell` | generic candidate; visual test pending; no extra source-search flags |
| `hazennstuff:trident_jetstream` | `net.hazen.hazennstuff.Spells.Schools.Hydro.TridentJetstreamSpell` | generic candidate; visual test pending; no extra source-search flags |
| `hazennstuff:umbrashift_barrage` | `net.hazen.hazennstuff.Spells.Schools.Shadow.UmbrashiftBarrageSpell` | generic candidate; visual test pending; no extra source-search flags |
| `hazennstuff:violent_regurgitation` | `net.hazen.hazennstuff.Spells.Schools.Blood.ViolentRegurgitationSpell` | generic candidate; visual test pending; no extra source-search flags |
| `hazennstuff:water_bolt` | `net.hazen.hazennstuff.Spells.Schools.Hydro.WaterBoltSpell` | generic candidate; visual test pending; no extra source-search flags |
| (unresolved) | `net.hazen.hazennstuff.Spells.AbstractSpells.HydroSpells` | unresolved ID / helper class; not counted as compatible; no extra source-search flags |
| (unresolved) | `net.hazen.hazennstuff.Spells.AbstractSpells.NightsEdgeSpell` | unresolved ID / helper class; not counted as compatible; no extra source-search flags |
| (unresolved) | `net.hazen.hazennstuff.Spells.AbstractSpells.RavensBaneSpell` | unresolved ID / helper class; not counted as compatible; no extra source-search flags |
| (unresolved) | `net.hazen.hazennstuff.Spells.AbstractSpells.RodOfDiscordSpell` | unresolved ID / helper class; not counted as compatible; no extra source-search flags |
| (unresolved) | `net.hazen.hazennstuff.Spells.AbstractSpells.TyrosSpells` | unresolved ID / helper class; not counted as compatible; no extra source-search flags |

## GTBC's Geomancy Plus - Iron's Spells Addon — 1.20.1

CurseForge file: 7580390; 21 inventory rows.

| Spell ID | Implementation (within the JAR) | Conditions / action |
| --- | --- | --- |
| (unresolved) | `com.gametechbc.gtbcs_geomancy_plus.mixin.EarthquakeMixin` | unresolved ID / helper class; not counted as compatible; no extra source-search flags |
| (unresolved) | `com.gametechbc.gtbcs_geomancy_plus.mixin.StormSpellMixin` | unresolved ID / helper class; not counted as compatible; no extra source-search flags |
| (unresolved) | `com.gametechbc.gtbcs_geomancy_plus.mixin.TouchDigMixin` | unresolved ID / helper class; not counted as compatible; no extra source-search flags |
| `gtbcs_geomancy_plus:chunker` | `com.gametechbc.gtbcs_geomancy_plus.spells.geo.ChunkerSpell` | generic candidate; visual test pending; no extra source-search flags |
| `gtbcs_geomancy_plus:crystalline_conduit` | `com.gametechbc.gtbcs_geomancy_plus.spells.geo.CrystallineConduitSpell` | generic candidate; visual test pending; no extra source-search flags |
| `gtbcs_geomancy_plus:dripstone_bolt` | `com.gametechbc.gtbcs_geomancy_plus.spells.geo.DripstoneBoltSpell` | generic candidate; visual test pending; no extra source-search flags |
| `gtbcs_geomancy_plus:earthshatter` | `com.gametechbc.gtbcs_geomancy_plus.spells.geo.EarthshatterSpell` | generic candidate; visual test pending; no extra source-search flags |
| `gtbcs_geomancy_plus:earthshatter` | `com.gametechbc.gtbcs_geomancy_plus.spells.nature.EarthshatterSpell` | generic candidate; visual test pending; no extra source-search flags |
| `gtbcs_geomancy_plus:eroding_boulder` | `com.gametechbc.gtbcs_geomancy_plus.spells.geo.ErodingBoulderSpell` | generic candidate; visual test pending; no extra source-search flags |
| `gtbcs_geomancy_plus:fissure` | `com.gametechbc.gtbcs_geomancy_plus.spells.geo.FissureSpell` | generic candidate; visual test pending; no extra source-search flags |
| `gtbcs_geomancy_plus:geo_conductor` | `com.gametechbc.gtbcs_geomancy_plus.spells.geo.GeoConductorSpell` | adapter: recasts(8, 12); recast |
| `gtbcs_geomancy_plus:petrivise` | `com.gametechbc.gtbcs_geomancy_plus.spells.geo.PetriviseSpell` | generic candidate; visual test pending; no extra source-search flags |
| `gtbcs_geomancy_plus:pillar_of_the_resounding_earth` | `com.gametechbc.gtbcs_geomancy_plus.spells.geo.PillarOfTheResoundingEarthSpell` | generic candidate; visual test pending; no extra source-search flags |
| `gtbcs_geomancy_plus:seismic_surf` | `com.gametechbc.gtbcs_geomancy_plus.spells.geo.SeismicSurfSpell` | generic candidate; visual test pending; no extra source-search flags |
| `gtbcs_geomancy_plus:solar_beam` | `com.gametechbc.gtbcs_geomancy_plus.spells.holy.SolarBeamSpell` | generic candidate; visual test pending; no extra source-search flags |
| `gtbcs_geomancy_plus:solar_chorus` | `com.gametechbc.gtbcs_geomancy_plus.spells.holy.SolarChorusSpell` | manual validation: player/inventory/world conditions; no adapter; recast, summon lifecycle, player/world-sensitive |
| `gtbcs_geomancy_plus:solar_storm` | `com.gametechbc.gtbcs_geomancy_plus.spells.holy.SolarStormSpell` | generic candidate; visual test pending; no extra source-search flags |
| `gtbcs_geomancy_plus:tremor_spike` | `com.gametechbc.gtbcs_geomancy_plus.spells.geo.TremorSpikeSpell` | adapter: recasts(6, 12); recast |
| `gtbcs_geomancy_plus:tremor_step` | `com.gametechbc.gtbcs_geomancy_plus.spells.geo.TremorStepSpell` | generic candidate; visual test pending; no extra source-search flags |
| `gtbcs_geomancy_plus:weeping_cluster` | `com.gametechbc.gtbcs_geomancy_plus.spells.geo.WeepingClusterSpell` | generic candidate; visual test pending; no extra source-search flags |
| `gtbcs_geomancy_plus:weeping_wall` | `com.gametechbc.gtbcs_geomancy_plus.spells.geo.WeepingWallSpell` | generic candidate; visual test pending; no extra source-search flags |

## GTBC's Spellbooks - Iron's Spells Addon — 1.21.1

CurseForge file: 6312018; 18 inventory rows.

| Spell ID | Implementation (within the JAR) | Conditions / action |
| --- | --- | --- |
| `gametechbcs_spellbooks:acid_rain` | `com.gametechbc.gametechbcsspellbooks.spells.nature.AcidRainSpell` | manual validation: player/inventory/world conditions; no adapter; cast predicate, player/world-sensitive |
| `gametechbcs_spellbooks:aerial_collapse` | `com.gametechbc.gametechbcsspellbooks.spells.nature.AerialCollapseSpell` | generic candidate; visual test pending; no extra source-search flags |
| `gametechbcs_spellbooks:ashen_breath` | `com.gametechbc.gametechbcsspellbooks.spells.evocation.AshenBreathSpell` | generic candidate; visual test pending; no extra source-search flags |
| `gametechbcs_spellbooks:astral_sense` | `com.gametechbc.gametechbcsspellbooks.spells.ender.AstralSenseSpell` | generic candidate; visual test pending; no extra source-search flags |
| `gametechbcs_spellbooks:banish` | `com.gametechbc.gametechbcsspellbooks.spells.holy.BanishSpell` | manual validation: player/inventory/world conditions; no adapter; player/world-sensitive |
| `gametechbcs_spellbooks:blackout` | `com.gametechbc.gametechbcsspellbooks.spells.eldritch.BlackoutSpell` | generic candidate; visual test pending; no extra source-search flags |
| `gametechbcs_spellbooks:call_forth_the_dead_king` | `com.gametechbc.gametechbcsspellbooks.spells.blood.CallForthTheDeadKingSpell` | generic candidate; visual test pending; no extra source-search flags |
| `gametechbcs_spellbooks:crimson_downpour` | `com.gametechbc.gametechbcsspellbooks.spells.blood.CrimsonDownpourSpell` | manual validation: player/inventory/world conditions; no adapter; cast predicate, player/world-sensitive |
| `gametechbcs_spellbooks:displacement` | `com.gametechbc.gametechbcsspellbooks.spells.ender.DisplacementSpell` | manual validation: player/inventory/world conditions; no adapter; player/world-sensitive |
| `gametechbcs_spellbooks:ensnare` | `com.gametechbc.gametechbcsspellbooks.spells.nature.EnsnareSpell` | generic candidate; visual test pending; no extra source-search flags |
| `gametechbcs_spellbooks:flames_reborn` | `com.gametechbc.gametechbcsspellbooks.spells.fire.FlamesRebornSpell` | generic candidate; visual test pending; no extra source-search flags |
| `gametechbcs_spellbooks:lingering_strain` | `com.gametechbc.gametechbcsspellbooks.spells.evocation.LingeringStrainSpell` | generic candidate; visual test pending; no extra source-search flags |
| `gametechbcs_spellbooks:meteor_storm` | `com.gametechbc.gametechbcsspellbooks.spells.fire.MeteorStormSpell` | generic candidate; visual test pending; no extra source-search flags |
| `gametechbcs_spellbooks:nullflare` | `com.gametechbc.gametechbcsspellbooks.spells.holy.NullflareSpell` | adapter: recasts(6, 12); recast |
| `gametechbcs_spellbooks:psychic_bolt` | `com.gametechbc.gametechbcsspellbooks.spells.eldritch.PsychicBoltSpell` | adapter: recasts(6, 12); recast, cast predicate, player/world-sensitive |
| `gametechbcs_spellbooks:reversal` | `com.gametechbc.gametechbcsspellbooks.spells.eldritch.ReversalSpell` | adapter: reversal(); cast predicate |
| `gametechbcs_spellbooks:shatterpoint` | `com.gametechbc.gametechbcsspellbooks.spells.ice.ShatterpointSpell` | generic candidate; visual test pending; no extra source-search flags |
| `gametechbcs_spellbooks:spectral_blink` | `com.gametechbc.gametechbcsspellbooks.spells.eldritch.SpectralBlinkSpell` | generic candidate; visual test pending; no extra source-search flags |

## Deeper and Darker: Spellbooks — 1.20.1

CurseForge file: 7709065; 4 inventory rows.

| Spell ID | Implementation (within the JAR) | Conditions / action |
| --- | --- | --- |
| `darkermagic:summoned_sculk_centipede` | `net.rev.darkermagic.spells.eldritch.SummonSculkCentipedeSpell` | manual validation: player/inventory/world conditions; no adapter; recast, summon lifecycle, player/world-sensitive |
| `darkermagic:summoned_sculk_snapper` | `net.rev.darkermagic.spells.eldritch.SummonSculkSnapperSpell` | manual validation: player/inventory/world conditions; no adapter; recast, summon lifecycle, player/world-sensitive |
| `darkermagic:summoned_shattered` | `net.rev.darkermagic.spells.eldritch.SummonShatteredSpell` | manual validation: player/inventory/world conditions; no adapter; recast, summon lifecycle, player/world-sensitive |
| `darkermagic:summoned_warden` | `net.rev.darkermagic.spells.eldritch.SummonWardenSpell` | manual validation: player/inventory/world conditions; no adapter; recast, summon lifecycle, player/world-sensitive |

## Ice and Fire: Spellbooks — 1.20.1

CurseForge file: 7413433; 0 inventory rows.

| Spell ID | Implementation (within the JAR) | Conditions / action |
| --- | --- | --- |

## Illage and Spell-age: Iron's Spells Addon — 1.20.1

CurseForge file: 7003776; 11 inventory rows.

| Spell ID | Implementation (within the JAR) | Conditions / action |
| --- | --- | --- |
| `ias_spellbooks:blazing_imprint` | `com.gametechbc.iasspellbooks.spells.fire.BlazingImprintSpell` | generic candidate; visual test pending; no extra source-search flags |
| `ias_spellbooks:dragline` | `com.gametechbc.iasspellbooks.spells.nature.DraglineSpell` | generic candidate; visual test pending; no extra source-search flags |
| `ias_spellbooks:handemic` | `com.gametechbc.iasspellbooks.spells.evocation.HandemicSpell` | restricted when removed Iron OwnerGetter API is absent; upstream gameplay incompatible, not an effect-test pass; no extra source-search flags |
| `ias_spellbooks:hellish_horde` | `com.gametechbc.iasspellbooks.spells.evocation.HellishHordeSpell` | generic candidate; visual test pending; cast predicate |
| `ias_spellbooks:pumpkin_spice` | `com.gametechbc.iasspellbooks.spells.nature.PumpkinSpiceSpell` | generic candidate; visual test pending; cast predicate |
| `ias_spellbooks:restorative_rain` | `com.gametechbc.iasspellbooks.spells.evocation.RestorativeRainSpell` | manual validation: player/inventory/world conditions; no adapter; equipment/inventory |
| `ias_spellbooks:silken_breath` | `com.gametechbc.iasspellbooks.spells.evocation.SilkenBreathSpell` | generic candidate; visual test pending; no extra source-search flags |
| `ias_spellbooks:snack_attack` | `com.gametechbc.iasspellbooks.spells.ice.SnackAttackSpell` | manual validation: player/inventory/world conditions; no adapter; equipment/inventory |
| `ias_spellbooks:spirits_chosen` | `com.gametechbc.iasspellbooks.spells.blood.SpiritsChosenSpell` | restricted when removed Iron OwnerGetter API is absent; upstream gameplay incompatible, not an effect-test pass; no extra source-search flags |
| `ias_spellbooks:venomous_reborn` | `com.gametechbc.iasspellbooks.spells.eldritch.VenomousRebornSpell` | restricted when removed Iron OwnerGetter API is absent; upstream gameplay incompatible, not an effect-test pass; no extra source-search flags |
| `ias_spellbooks:zoom_n_boom` | `com.gametechbc.iasspellbooks.spells.evocation.ZoomNBoomSpell` | generic candidate; visual test pending; cast predicate |

## Deeper and Darker: Spellbooks — 1.21.1

CurseForge file: 7897469; 4 inventory rows.

| Spell ID | Implementation (within the JAR) | Conditions / action |
| --- | --- | --- |
| `darkermagic:summoned_sculk_centipede` | `net.rev.darkermagic.spells.eldritch.SummonSculkCentipedeSpell` | manual validation: player/inventory/world conditions; no adapter; recast, summon lifecycle, player/world-sensitive |
| `darkermagic:summoned_sculk_snapper` | `net.rev.darkermagic.spells.eldritch.SummonSculkSnapperSpell` | manual validation: player/inventory/world conditions; no adapter; recast, summon lifecycle, player/world-sensitive |
| `darkermagic:summoned_shattered` | `net.rev.darkermagic.spells.eldritch.SummonShatteredSpell` | manual validation: player/inventory/world conditions; no adapter; recast, summon lifecycle, player/world-sensitive |
| `darkermagic:summoned_warden` | `net.rev.darkermagic.spells.eldritch.SummonWardenSpell` | manual validation: player/inventory/world conditions; no adapter; recast, summon lifecycle, player/world-sensitive |

## GTBC's Geomancy Plus - Iron's Spells Addon — 1.21.1

CurseForge file: 7041615; 15 inventory rows.

| Spell ID | Implementation (within the JAR) | Conditions / action |
| --- | --- | --- |
| (unresolved) | `com.gametechbc.gtbcs_geomancy_plus.mixin.EarthquakeMixin` | unresolved ID / helper class; not counted as compatible; no extra source-search flags |
| (unresolved) | `com.gametechbc.gtbcs_geomancy_plus.mixin.StormSpellMixin` | unresolved ID / helper class; not counted as compatible; no extra source-search flags |
| `gtbcs_geomancy_plus:chunker` | `com.gametechbc.gtbcs_geomancy_plus.spells.geo.ChunkerSpell` | generic candidate; visual test pending; no extra source-search flags |
| `gtbcs_geomancy_plus:dripstone_bolt` | `com.gametechbc.gtbcs_geomancy_plus.spells.geo.DripstoneBoltSpell` | generic candidate; visual test pending; no extra source-search flags |
| `gtbcs_geomancy_plus:earthshatter` | `com.gametechbc.gtbcs_geomancy_plus.spells.geo.EarthshatterSpell` | generic candidate; visual test pending; no extra source-search flags |
| `gtbcs_geomancy_plus:eroding_boulder` | `com.gametechbc.gtbcs_geomancy_plus.spells.geo.ErodingBoulderSpell` | generic candidate; visual test pending; no extra source-search flags |
| `gtbcs_geomancy_plus:fissure` | `com.gametechbc.gtbcs_geomancy_plus.spells.geo.FissureSpell` | generic candidate; visual test pending; no extra source-search flags |
| `gtbcs_geomancy_plus:geo_conductor` | `com.gametechbc.gtbcs_geomancy_plus.spells.geo.GeoConductorSpell` | adapter: recasts(8, 12); recast |
| `gtbcs_geomancy_plus:petrivise` | `com.gametechbc.gtbcs_geomancy_plus.spells.geo.PetriviseSpell` | generic candidate; visual test pending; no extra source-search flags |
| `gtbcs_geomancy_plus:pillar_of_the_resounding_earth` | `com.gametechbc.gtbcs_geomancy_plus.spells.geo.PillarOfTheResoundingEarthSpell` | generic candidate; visual test pending; no extra source-search flags |
| `gtbcs_geomancy_plus:seismic_surf` | `com.gametechbc.gtbcs_geomancy_plus.spells.geo.SeismicSurfSpell` | generic candidate; visual test pending; no extra source-search flags |
| `gtbcs_geomancy_plus:solar_beam` | `com.gametechbc.gtbcs_geomancy_plus.spells.holy.SolarBeamSpell` | generic candidate; visual test pending; no extra source-search flags |
| `gtbcs_geomancy_plus:solar_storm` | `com.gametechbc.gtbcs_geomancy_plus.spells.holy.SolarStormSpell` | generic candidate; visual test pending; no extra source-search flags |
| `gtbcs_geomancy_plus:tremor_spike` | `com.gametechbc.gtbcs_geomancy_plus.spells.geo.TremorSpikeSpell` | adapter: recasts(6, 12); recast |
| `gtbcs_geomancy_plus:tremor_step` | `com.gametechbc.gtbcs_geomancy_plus.spells.geo.TremorStepSpell` | generic candidate; visual test pending; no extra source-search flags |

## Alex's Caves: Spellbooks — 1.20.1

CurseForge file: 9013630; 4 inventory rows.

| Spell ID | Implementation (within the JAR) | Conditions / action |
| --- | --- | --- |
| `alexs_caves_spellbooks:aspect_of_tremorsaurus` | `net.temporal.alexs_caves_spellbooks.core.spells.nature.AspectOfTremorsaurusSpell` | adapter: fleeingTarget(); no extra source-search flags |
| `alexs_caves_spellbooks:summon_grottoceratops` | `net.temporal.alexs_caves_spellbooks.core.spells.nature.SummonGrottoceratopsSpell` | manual validation: player/inventory/world conditions; no adapter; recast, summon lifecycle, player/world-sensitive |
| `alexs_caves_spellbooks:summon_vesper` | `net.temporal.alexs_caves_spellbooks.core.spells.blood.SummonVesperSpell` | manual validation: player/inventory/world conditions; no adapter; recast, summon lifecycle, player/world-sensitive |
| (unresolved) | `net.temporal.alexs_caves_spellbooks.core.spells.AbstractSummonSpell` | unresolved ID / helper class; not counted as compatible; recast, summon lifecycle, player/world-sensitive |

## Alex's Caves: Spellbooks — 1.21.1

CurseForge file: 9239413; 3 inventory rows.

| Spell ID | Implementation (within the JAR) | Conditions / action |
| --- | --- | --- |
| `alexs_caves_spellbooks:aspect_of_tremorsaurus` | `net.temporal.alexs_caves_spellbooks.core.spells.nature.AspectOfTremorsaurusSpell` | adapter: fleeingTarget(); no extra source-search flags |
| `alexs_caves_spellbooks:summon_grottoceratops` | `net.temporal.alexs_caves_spellbooks.core.spells.nature.SummonGrottoceratopsSpell` | manual validation: player/inventory/world conditions; no adapter; recast, summon lifecycle, player/world-sensitive |
| `alexs_caves_spellbooks:summon_vesper` | `net.temporal.alexs_caves_spellbooks.core.spells.blood.SummonVesperSpell` | manual validation: player/inventory/world conditions; no adapter; recast, summon lifecycle, player/world-sensitive |
