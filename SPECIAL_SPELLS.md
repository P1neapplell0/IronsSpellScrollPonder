# Special Spell Preview Checklist

This list covers built-in Iron's Spellbooks 1.21.1-3.16.2 spells whose important
behavior is not represented by one ordinary cast. The implementation lives in
`BuiltinSpellPreviewAdapters`; add-on mods can register equivalent behavior
through `SpellPreviewAdapters.register`.

## Automated Follow-up Actions

| Spell ID | Preview action | Upstream behavior used | Test expectation |
| --- | --- | --- | --- |
| `echoing_strikes` | Performs a physical player attack after the buff is applied. | `EchoingStrikesSpell#onCast`, `EchoingStrikesEffect#createEcho` | The melee hit is followed by an `EchoingStrikeEntity` area hit. |
| `invisibility` | Performs a physical attack. | `TrueInvisibilityEffect#onDealDamage` | The projected caster becomes visible again after attacking. |
| `spider_aspect` | Poisons the target, then performs a physical attack. | `SpiderAspectEffect#increaseDamage` | The hit receives the aspect bonus because the target has an affected effect. |
| `abyssal_shroud` | Applies one normal zombie hit to the caster. | `AbyssalShroudEffect#doEffect` | The hit is evaded with smoke, sound, and a short sidestep. |
| `evasion` | Applies one normal zombie hit to the caster. | `EvasionEffect#doEffect` | The caster teleports and portal particles appear. |
| `oakskin` | Applies one normal zombie hit to the caster. | `OakskinEffect#reduceDamage` | The defensive effect participates in the hurt event. |
| `blight` | Makes the blighted primary target attack the caster. | `BlightEffect#reduceDamageOutput` | The target's outgoing hit passes through the blight modifier. |
| `gluttony` | Finishes consuming cooked beef. | `GluttonyEffect#finishEating` | The normal NeoForge item-finish event runs and grants mana. |
| `volt_strike` | Moves the caster into the target. | `VoltStrikeEffect#applyEffectTick` | Collision triggers the electric impact and area damage. |
| `heartstop` | Takes damage while protected, then removes the effect. | `ServerPlayerEvents#onBeforeDamageTaken`, `HeartstopEffect#onEffectRemoved` | Stored damage is released when the effect ends. |
| `guiding_bolt` | Fires an off-axis magic arrow after the target is marked. | `GuidingBoltManager` | The later projectile bends toward the marked target. |

## Automated Recasts

| Spell ID | Timing | Test expectation |
| --- | --- | --- |
| `eldritch_blast` | Recast every 8 ticks while Iron's reports recasts remaining. | All configured blasts appear, not only the first. |
| `flaming_barrage` | Recast every 8 ticks. | The complete projectile barrage appears. |
| `raise_hell` | Recast every 10 ticks. | All recast area effects are created. |
| `wall_of_fire` | Recast every 8 ticks with a yaw offset. | Multiple anchors are placed and the wall is finalized. |
| `thunder_step` | Recast after 24 ticks. | The orb travels before the follow-up teleport is requested. |

Summon spells (`raise_dead`, `summon_swords`, `summon_vex`, `summon_horse`, and
`summon_polar_bear`) also use `PlayerRecasts`, but their first cast already
creates the complete summon group. Their later recasts mainly dismiss or clean
up the summons, so the preview intentionally leaves them visible instead of
immediately consuming every recast.

## Prepared Targets

| Spell ID | Preparation | Test expectation |
| --- | --- | --- |
| `counterspell` | Places a stationary `MagicArrowProjectile` on the casting ray. | Counterspell recognizes an `AntiMagicSusceptible` target and removes it. |
| `wololo` | Replaces the primary zombie with a sheep. | The cast succeeds, changes the sheep's color, and emits critical particles. |
| `sacrifice` | Replaces the primary zombie with a summoned zombie owned by the caster. | The cast recognizes its own `IMagicSummon`, consumes it, and creates the blood explosion. |
| `spectral_hammer` | Places a `5 x 5` stone wall on the horizontal casting ray. | The hammer spawns and removes its mineable target blocks. |
| `touch_dig` | Uses the same harvestable stone wall. | The pre-cast block check succeeds and the targeted block breaks. |

## Restricted Simulation

| Spell ID | Reason |
| --- | --- |
| `pocket_dimension` | Cross-dimension and persistent manager behavior. |
| `recall` | Teleports the player using stored world state. |
| `portal` | Creates persistent linked portal entities and recast state. |

These spells remain information-only unless a disposable-world implementation
can guarantee cleanup outside the preview cell.

## Visual Packet Regression Tests

- `ray_of_siphoning`: the continuous blood beam was user-verified on the 1.20.1
  branch. On 1.21.1 it uses typed `BloodSiphonParticlesPacket` capture, internal
  visual ID 22, and two origin-relative `Vec3` positions. Recheck its
  `continuous_thrust` start animation after migration.
- `ray_of_frost`: verify that the complete beam reaches its target. Its visual
  entity stores beam distance in NeoForge `IEntityWithComplexSpawn`, not NBT;
  this travels in the initial projected entity snapshot.
- `fang_strike` and `fang_ward`: verify eruption timing, attack animation, and
  sound. Their `ExtendedEvokerFang` instances reconstruct as vanilla
  `EvokerFangs`, whose visible attack starts only after entity event `4`.
- Fireball, fire arrow, and immolate explosions: verify internal visual ID 39 even
  when the packet is sent only to players tracking a projectile or victim.
- Entity-specific spell effects: verify internal visual ID 40 reaches
  `IClientEventEntity#handleClientEvent` using retained server entity IDs.
- `guiding_bolt`: verify tracking start/stop visual IDs 41 and 42 and the later
  projectile's curved path.
- Heat Surge and Frostwave: verify shockwave visual ID 43 resolves its particle
  registry ID.

## Acceptance Procedure

For each entry, test the initial preview, automatic loop, play/pause control,
spell switching, and closing the screen during the follow-up action. Watch both
`latest.log` and the rendered scene. Include `ray_of_siphoning` start animation,
`ray_of_frost`, `fang_strike`, and `fang_ward` in the next focused run. A clean
log is not sufficient for animation, particles, hurt tint, or camera-facing
problems.
