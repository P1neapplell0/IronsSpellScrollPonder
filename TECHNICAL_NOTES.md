# Technical Notes

This document records the architecture and upstream code references for Iron's
Spell Scroll Ponder. It is intended to be the first document opened when work
continues in a new session.

## Version Baseline

| Component | Development baseline | Where it is pinned |
| --- | --- | --- |
| Minecraft | `1.21.1` | `gradle.properties` |
| NeoForge | `21.1.234` | `gradle.properties` |
| ModDevGradle | `2.0.143` | `build.gradle` |
| Parchment | `2024.11.17` for `1.21.1` | `gradle.properties` |
| Iron's Spellbooks | CurseForge file `8364935`, mod `1.21.1-3.16.2` | `build.gradle`, `neoforge.mods.toml` |
| Iron's Lib | `1.21.1-2.1.0` | `gradle.properties` |
| Ponder | `1.0.87+mc1.21.1` | `gradle.properties`, Jar-in-Jar |
| Flywheel | `1.0.6` | `gradle.properties`, Jar-in-Jar |
| Player Animator | `2.0.4+1.21.1` | `gradle.properties` |
| Curios | `9.5.1+1.21.1` | `gradle.properties` |
| GeckoLib | `4.9.2` | `gradle.properties` |

The project uses Gradle `8.11.1` and Java `21`. Do not assume payload layouts,
private field names, Mixin descriptors, or
animation-layer behavior are stable across upgrades.

## Why the Preview Is Split Across Server and Client

Ponder's `PonderLevel` is a client-side schematic level. It can render blocks,
entities, and particles, but it is not a `ServerLevel` and cannot satisfy the
server assumptions made by many Iron's Spellbooks spells. Calling arbitrary
spells directly in a Ponder scene would fail for projectiles, entity queries,
server-only casts, custom packets, and add-on code that casts to `ServerLevel` or
`ServerPlayer`.

The current design therefore uses two worlds:

```text
scroll tooltip
    -> StartPreview C2S
    -> FakePlayer casts in iss_ponder:spell_preview (real ServerLevel)
    -> server captures entities / blocks / particles / sounds / cast events
    -> iss_ponder projection packets S2C
    -> PonderLevel reconstructs a local, origin-relative scene
    -> SpellPreviewScreen renders the scene and information panel
```

The real player is only the observer and packet recipient. The simulated caster
is a NeoForge `FakePlayer` placed in a per-player cell in the preview dimension.

## Component Map

| File | Responsibility |
| --- | --- |
| `ClientScrollPonderHandler` | Validates hovered scrolls, displays the prompt, and watches `PonderKeybinds.PONDER`. |
| `ClientPreviewController` | Owns request/pending state and opens or closes the preview screen. |
| `SpellPreviewScreen` | Renders the scene, dynamic spell information, controls, and casting progress. |
| `SpellSelectionMenu` | Searches enabled spells and groups them by school. |
| `PreviewSessionManager` | Creates server sessions, runs casts, manages targets, and synchronizes projections. |
| `ModNetwork` | Defines the complete C2S/S2C projection protocol. |
| `PreviewProjection` | Reconstructs and renders entities, blocks, particles, and client cast effects in `PonderLevel`. |
| `PreviewPacketBridge` | Replays an allowlist of Iron's custom client particle packets. |
| `AbstractSpellProjectionMixin` | Observes the post-cast point where Iron's normally sends `OnClientCastPacket`. |
| `ServerLevelProjectionMixin` | Captures server particle and block-update calls. |
| `IronPacketDistributorProjectionMixin` | Captures FakePlayer and tracking-only Iron's typed payloads at NeoForge's `PacketDistributor`. |

## Casting Lifecycle

`PreviewSessionManager.tickSpell` deliberately calls Iron's public casting path:

1. Reset the simulated player's `MagicData`, grant preview mana, and mark the
   spell learned.
2. Send `ProjectionCastStarted` before initiation. This ordering matters for
   instant spells, whose `castSpell` can run inside `attemptInitiateCast`.
3. Call `AbstractSpell.attemptInitiateCast(..., CastSource.COMMAND, ...)`.
4. Call `FakePlayer.doTick()` on later server ticks. NeoForge's `FakePlayer.tick()`
   is empty; `doTick()` reaches the vanilla player tick and Iron's
   `MagicManager`/`MagicData` lifecycle.
5. Synchronize `MagicData.getCastCompletionPercent()` for non-instant spells.
6. Cancel casts still active after 15 seconds through
   `Utils.serverSideCancelCast`.

The `AbstractSpellProjectionMixin` injects after `AbstractSpell.castSpell` and
sends `ProjectionCastEffect` with the spell's additional cast data. The client
then calls `AbstractSpell.onClientCast` in the projected context.

### FakePlayer Network Compatibility

NeoForge 21.1.234 constructs every `FakePlayer` with a shared
`FakePlayer$FakeConnection`. That `Connection` is never attached to a Netty
channel, so `Connection#channel()` remains null. This is normally harmless
because `FakePlayerNetHandler` discards outgoing packets, but optional-network
helpers commonly call `ICommonPacketListener#hasChannel` first. NeoForge's
`ChannelAttributes#getPayloadSetup` dereferences the missing channel, causing
the simulated player's normal `doTick()` to crash in unrelated mods such as
AppleSkin or Music And Melody.

`PreviewSessionManager#initializeFakeConnection` attaches the shared dummy
connection to one persistent `EmbeddedChannel` before the simulated player is
added to the level. It also installs `ConnectionType.OTHER` and an empty
`NetworkPayloadSetup`. Channel capability checks can therefore return false
normally, while the upstream no-op packet listener continues to prevent any
virtual traffic from leaving the server process.

### Upstream Iron's References

These were inspected from the resolved `1.21.1-3.16.2` dependency:

- `io.redspace.ironsspellbooks.api.spells.AbstractSpell`
  - `attemptInitiateCast`
  - `castSpell`
  - sends `network.casting.OnClientCastPacket` after server casting
- `io.redspace.ironsspellbooks.api.magic.MagicData`
  - `initiateCast`, `resetCastingState`, `getCastCompletionPercent`
  - stores `ICastData` used by target-aware spells
- `io.redspace.ironsspellbooks.capabilities.magic.MagicManager#tick`
  - drives server casting, continuous ticks, recasts, and synchronization
- `io.redspace.ironsspellbooks.api.util.Utils#serverSideCancelCast`
  - canonical server cancellation path
- `io.redspace.ironsspellbooks.player.ClientSpellCastHelper`
  - `handleClientboundOnCast`, `handleClientboundCancelCast`
  - `handleClientboundBloodSiphonParticles`
- `io.redspace.ironsspellbooks.spells.blood.RayOfSiphoningSpell#onCast`
  - sends target-center and caster-center in `BloodSiphonParticlesPacket`
- `io.redspace.ironsspellbooks.render.animation.AnimationHelper`
  - `animatePlayerStart`, `cancelPlayerAnimation`
- `io.redspace.ironsspellbooks.api.spells.SpellAnimations#ANIMATION_RESOURCE`
  - Player Animator associated-data key used by Iron's animation layer

## Projection Rules

Server entities within the preview bounds are serialized once with
`Entity#saveWithoutId`; position, rotation, health, and non-default synchronized
entity data are sent repeatedly. Server entity IDs are kept because projectile
owner and target relationships may refer to them. The client recreates players
as `RemotePlayer` and other entities through their registered `EntityType`.

All server positions must be translated relative to the session origin before
they reach the client. The Ponder scene is centered at `BlockPos.ZERO`, while the
server cells are spread far apart to reduce interference between simultaneous
sessions.

This rule includes positions nested inside particle options, not only the
particle packet's outer `x/y/z`. Iron's 3.16.2 stores absolute destinations in
`ZapParticleOption`, `SoulfireRayParticleOptions`, and `TraceParticleOptions`,
and stores an absolute cauldron position in
`TintedBubblePopParticleOptions`. Vanilla `VibrationParticleOption` can contain
a `BlockPositionSource`, while `SwirlingParticleOptions` can recursively wrap
another particle option. `PreviewSessionManager#projectParticleOptions`
translates those fields before network encoding. Entity-backed vibration
sources retain their entity ID because projected entities deliberately preserve
server IDs. Direction, color, scale, normal, motion, and size vectors must not
be translated.

Destination-bearing client particles are also rejected when their endpoint is
non-finite or more than 128 blocks from the projected spawn. This is a final
guard against add-ons passing an unprojected coordinate into an implementation
that allocates geometry or child particles in proportion to path length.

The client and server base plates are `7 x 11`, extending two blocks behind the
caster (`-Z`) and two behind the targets (`+Z`). The floor is symmetrical around
`Z = 0`, but `PonderSceneBuilder#configureBasePlate` deliberately retains the
original `(-3, -3, 7)` camera basis so the default framing height remains the
same as the original `7 x 7` scene. Projection and cleanup bounds are larger.
Do not shrink those bounds to match the visible floor: large projectiles,
summons, area effects, and particles need room outside the plate.

The screen starts in playing mode and loops completed previews. Its play/pause
control changes only whether another replay is requested; it never stops Ponder
ticks or the server simulation. The server marks only the final `PreviewStatus`
for a completed lifecycle, rather than each `ProjectionCastFinished`, because
adapters may recast several times before their follow-up is complete. Playing
mode waits 20 ticks before requesting a replay and pauses that countdown while
the spell selection menu is open.

### Upstream Ponder References

The current implementation is based on Ponder `1.0.87+mc1.21.1`:

- `net.createmod.ponder.enums.PonderKeybinds#PONDER`
  - supplies the configurable key state and translated key name
- `net.createmod.ponder.api.level.PonderLevel`
  - `createBackup`, `restore`, entity ticking, particles, and virtual rendering
- `net.createmod.ponder.foundation.PonderScene`
  - scene transform, base world section, camera, tick, and render lifecycle
- `net.createmod.catnip.levelWrappers.WrappedClientLevel#of`
  - provides the `ClientLevel` view required by client entities and effects
- `net.createmod.catnip.render.DefaultSuperRenderTypeBuffer`
  - Ponder/Catnip render buffer used by the scene

This project constructs `PonderScene` and uses foundation classes directly. They
are less stable than Ponder's public registration API and must be rechecked when
Ponder changes.

## Client Context and Player Animator

Many Iron's client handlers read `Minecraft.getInstance().player` and that
player's level rather than using only packet fields. `PreviewProjection` creates
one reusable projected `LocalPlayer` and temporarily swaps `Minecraft.level` and
`Minecraft.player` while invoking a client spell effect or custom packet. The
swap is always restored in `finally`.

The projected caster is a `RemotePlayer`. Iron's stores its Player Animator
`ModifierLayer` under `SpellAnimations.ANIMATION_RESOURCE`, but its normal
factory also updates the global `IronsAdjustmentModifier.INSTANCE`. A preview
contains the real local player, a context `LocalPlayer`, and a projected
`RemotePlayer`, so that singleton can point at the wrong player. The projection
now replaces the caster's Iron layer with an isolated priority-42 layer and
creates each registered `IPlayable` through `IPlayable#playAnimation`. It also compensates
when the virtual level does not advance Player Animator's normal tick lifecycle.

The caster keeps its simulated server UUID for entity relationships, but renders
with the observing player's skin. `PreviewRemotePlayer` copies the local
player's GameProfile properties and snapshots its resolved `PlayerSkin`, which
contains both the texture and `WIDE`/`SLIM` model. The `getSkin` override is
required because vanilla queries `PlayerInfo` by entity UUID; the isolated
FakePlayer is not in the real tab list and would otherwise use a default skin.

The missing start animation was not caused by the isolated layer. The projected
start path created `new MagicData()` and immediately called `initiateCast`, but
the no-argument constructor leaves `syncedSpellData` null and `initiateCast`
dereferences that field directly. Iron's 3.16.2
`ClientSpellCastHelper#handleClientBoundOnCastStarted` does not construct or
initialize `MagicData`; it installs the animation first and passes null to
`onClientPreCast`. The preview deliberately supplies usable `MagicData` for
add-on pre-cast hooks, so it must attach a caster-specific `SyncedSpellData`
before calling `initiateCast`. The old exception occurred after spell/animation
resolution but before animation installation, while finish animations bypassed
this path and therefore worked. The new path installs the start keyframes first,
matching the upstream ordering, so a future pre-cast-state failure cannot
swallow the visible pose.

The isolated layer remains intentionally different from Iron's global factory:
it installs a fresh animation from `IPlayable` without the singleton
`IronsAdjustmentModifier`. This avoids cross-player state between the real
player, projected context player, and projected caster. Keep that difference in
mind if a future spell proves to depend on an aiming adjustment rather than its
registered keyframes. Start-animation playback still requires focused visual
acceptance after the initialization fix.

Never leave the global Minecraft player or level swapped outside the narrow
`withProjectionContext` call.

## Custom Packet Bridge

Iron's 3.16.2 implements every visual message as a `CustomPacketPayload` and
sends it through NeoForge's static `PacketDistributor`. The projection Mixin
therefore targets `sendToPlayer`, `sendToPlayersTrackingEntity`, and
`sendToPlayersTrackingEntityAndSelf`. The last target is required because
`RayOfSiphoningSpell` uses it for `BloodSiphonParticlesPacket`.

`PreviewSessionManager#encodeVisualPacket` accepts only reviewed concrete Iron
payload classes, invokes their public `write(FriendlyByteBuf)` methods, and maps
them to this mod's internal visual IDs. These integers are no longer Iron packet
discriminators. `PreviewPacketBridge` decodes the reviewed layouts on the client,
usually inside the projected Minecraft player/level context. Inventory, HUD,
camera, capability, and other real-player mutations are intentionally excluded.

Visual ID `22` is deliberately different. `BloodSiphonParticlesPacket` already
contains both required positions, so its adapter decodes them without a global
Minecraft player swap, translates them relative to the preview origin, and
submits particles directly to `PonderLevel`. It reproduces Iron's exact 40
`ParticleHelper.BLOOD` submissions and velocity calculation. Iron's
`SiphonParticle` has a lifetime of only one to five ticks and the upstream
effect is not a geometric beam, so the adapter also keeps a three-tick crossed,
emissive ribbon between the latest endpoints. Repeated packets sustain that
ribbon for the duration of the cast. This fallback is rendered after
`PonderScene#renderScene` using the same transformed pose and Catnip buffer.

Important consequences:

- Concrete payload classes and their `write` layouts are version-specific.
  Re-audit `encodeVisualPacket` and `PreviewPacketBridge` after updating Iron's.
- Do not forward inventory, HUD, camera, capability, or real-player mutation
  packets merely to increase coverage.
- A packet with positions stored as `BlockPos`, primitive coordinates, nested
  objects, entity IDs, or directions needs a dedicated translation adapter.
- `ray_of_siphoning` uses `BloodSiphonParticlesPacket` and
  `ClientSpellCastHelper#handleClientboundBloodSiphonParticles`; its beam is a
  required regression test for visual ID `22` and projected `Vec3` translation.

## Entity Spawn Data and Vanilla Events

Entity NBT and synchronized entity data are not the complete NeoForge spawn
contract. Entities implementing `IEntityWithComplexSpawn` append data through
`AdvancedAddEntityPayload`. The server projection captures it with
`writeSpawnData(RegistryFriendlyByteBuf)`, includes it in the initial snapshot,
and applies it with `readSpawnData` immediately after client construction. Both
buffers use the corresponding level's `RegistryAccess`. The payload is sent only
with the initial snapshot, matching its spawn-only lifecycle.

This is required by
`io.redspace.ironsspellbooks.entity.spells.ray_of_frost.RayOfFrostVisualEntity`:
its beam `distance` is transferred through additional spawn data and is not
saved by `Entity#saveWithoutId`. NBT-only reconstruction therefore produced a
valid entity with a zero-length frost ray.

Additional spawn data follows the same nested-coordinate rule as particle
options. Iron's `WallOfFireEntity#writeSpawnData` writes absolute wall anchors
as floats, and `FieryDaggerEntity#writeSpawnData` writes an absolute owner
tracking point. `PreviewSessionManager#writeProjectedSpawnData` temporarily
supplies origin-relative values while calling Iron's own encoder, then restores
the server entity in a `finally` block. The wall anchor accessor is intentional:
subtracting the origin only after the absolute coordinates were encoded as
floats would permanently lose sub-block precision in distant preview cells.
Other reviewed Iron's 3.16.2 complex spawn payloads contain entity IDs,
directions, counters, radii, or beam lengths; those values remain unchanged.

Synchronized entity data is also reviewed by value semantics rather than Java
type alone. `DeadKingSoulEntity.DATA_RESPAWN_POS` is the only Iron's 3.16.2
entity data field using `EntityDataSerializers.VECTOR3`, and its client tick
moves toward that absolute respawn position. The projection translates that
specific value. A future `VECTOR3`, `BLOCK_POS`, or `OPTIONAL_BLOCK_POS` field
must be classified before forwarding because it may instead be a direction or
local offset.

Vanilla entity events are a separate transient channel. The
`ServerLevelProjectionMixin` captures `ServerLevel#broadcastEntityEvent`, maps
the server entity to its projected ID, and sends `ProjectionEntityEvent`; the
client then follows `ClientPacketListener#handleEntityEvent` by invoking
`Entity#handleEntityEvent`. Iron's `ExtendedEvokerFang` uses the vanilla
`minecraft:evoker_fangs` type, so client reconstruction correctly creates
`EvokerFangs`. `ExtendedEvokerFang#tick` broadcasts event `4`, and the projected
`EvokerFangs#handleEntityEvent` uses it to start the client attack state and
sound. Event projection is therefore necessary for both `fang_strike` and
`fang_ward`.

These additions changed the wire format, so `ModNetwork.PROTOCOL` is `7`.

## Mixin Maintenance

The Mixin descriptors in `iss_ponder.mixins.json` are version-sensitive:

- `AbstractSpellProjectionMixin` targets the non-remapped Iron's method
  `AbstractSpell#castSpell`.
- `IronPacketDistributorProjectionMixin` targets NeoForge
  `PacketDistributor#sendToPlayer`, `sendToPlayersTrackingEntity`, and
  `sendToPlayersTrackingEntityAndSelf`, including all vararg payloads.
- `ServerLevelProjectionMixin` targets both particle overloads and
  `sendBlockUpdated`, plus `broadcastEntityEvent` for vanilla transient entity
  events such as evoker-fang event `4`.

ModDevGradle 2.0 uses Mojmap names directly for this 1.21.1 build. The obsolete
MCP-only Mixin annotation processor was removed; Iron's 3.16.2 is distributed
without the refmap named in its own Mixin JSON as well. After any Minecraft,
NeoForge, or Iron's upgrade, run a client and dedicated server
with Mixin debug logging. A successful Java compile does not prove an injection
descriptor still matches at runtime.

## Special-Spell Audit

The generic pipeline is not sufficient when a spell requires gameplay after the
cast or depends on persistent/global state.

| Spell | Current treatment | Reason / upstream reference | Required follow-up |
| --- | --- | --- | --- |
| `pocket_dimension` | Information-only | Cross-dimension and persistent manager behavior | Keep restricted unless a disposable-world adapter is designed. |
| `recall` | Information-only | Teleports the caster and depends on stored state | Keep restricted. |
| `portal` | Information-only | Creates persistent world entities/links | Keep restricted. |
| `ray_of_siphoning` | Generic cast plus packet-22 Ponder adapter | `RayOfSiphoningSpell#onCast` sends `BloodSiphonParticlesPacket`; `ClientSpellCastHelper` emits 40 short-lived particles per packet | User-verified: the projected beam is visible. Recheck the start animation after the `MagicData` fix. |
| `ray_of_frost` | Generic entity projection plus NeoForge complex spawn data | `RayOfFrostVisualEntity#writeSpawnData` transfers the beam distance outside NBT | Build-verified; focused visual acceptance is still required. |
| `fang_strike`, `fang_ward` | Generic entity projection plus vanilla entity events | `ExtendedEvokerFang#tick` broadcasts event `4`; projected `EvokerFangs#handleEntityEvent` consumes it | Build-verified; focused visual acceptance is still required. |
| `echoing_strikes` | Physical follow-up attack adapter | `EchoingStrikesSpell#onCast`; `EchoingStrikesEffect#createEcho` | User-verified: the physical hit correctly triggers the later echo entity hit. |
| `wololo` | Sheep primary target | `WololoSpell#checkPreCastConditions` accepts only `Sheep` | Confirm the sheep changes color and emits critical particles. |
| `sacrifice` | Caster-owned summoned-zombie target | `SacrificeSpell#checkPreCastConditions` accepts only the caster's `IMagicSummon` | Confirm the summon is consumed by the blood explosion. |
| `spectral_hammer`, `touch_dig` | Projected `5 x 5` stone target wall | Both pre-cast checks require a block on the horizontal casting ray | Confirm the wall appears on every loop and removed blocks do not persist into the next one. |

The adapter API is implemented by `SpellPreviewAdapter`,
`SpellPreviewContext`, and `SpellPreviewAdapters`. It provides primary-target
selection, origin-relative initial blocks, scene-ready, post-cast, per-tick
follow-up, before-recast, and cleanup hooks. Initial blocks are bounds-checked,
projected after each client scene reset, and removed with the server session.
Built-in corrections live in `BuiltinSpellPreviewAdapters`; the complete test
matrix is in `SPECIAL_SPELLS.md`. Keep spell IDs centralized in that registry.

## Known Gaps and Regression Cases

The following visual results were accepted on the previous 1.20.1 branch. Treat
them as 1.21.1 regression cases until they are visually verified on this branch:

- Start animations require visual acceptance after initializing
  `MagicData.syncedSpellData` and installing the keyframes before client
  pre-cast setup. Finish-animation render-stack diagnostics already succeeded.
- Hurt time, swing state, and vanilla hurt event 2 are now projected. Live
  testing confirmed target tint; hurt animation and sound still require focused
  acceptance.
- The Ponder-specific blood-siphon beam was user-verified on 1.20.1. Its 1.21.1
  capture path now uses `sendToPlayersTrackingEntityAndSelf` and needs retesting.
- `ray_of_frost` additional spawn data and vanilla event `4` for `fang_strike`
  and `fang_ward` are build-verified but still need focused visual acceptance.
- `echoing_strikes` performs a physical follow-up attack through the adapter API
  and its two-stage hit was accepted in gameplay feedback.
- The complete built-in spell catalog and add-on spells have not been accepted
  as compatible.
- Tooltip validation currently checks `IScroll`, container presence, non-empty
  data, and a non-null spell, but should be hardened further before accepting
  malformed NBT from arbitrary add-ons.

Minimum regression categories:

1. Instant projectile spell.
2. Long/charged spell with progress.
3. Continuous ray spell.
4. Summon and area-of-effect spell.
5. Block-changing spell.
6. Spell using an Iron's custom particle packet.
7. Spell with Player Animator start and finish animations.
8. Multi-stage spell requiring a later attack or interaction.
9. Restricted cross-dimension spell.
10. Add-on spell registered through `SpellRegistry`.

## Build and Release Checklist

1. Run `./gradlew compileJava` and `./gradlew build` with Java 21.
2. Run `./gradlew runGameTestServer` and confirm the preview dimension loads.
3. Run `./gradlew runClient` and test the regression categories above.
4. Inspect `build/libs/iss_ponder-neoforge1.21.1-1.0.1.jar` and its
   `META-INF/jarjar/metadata.json`.
5. Confirm bundled Ponder/Flywheel versions and licenses.
6. Confirm `ModNetwork.PROTOCOL` was bumped for any payload wire-format change.
7. Re-audit Iron's typed visual payload allowlist and `write` layouts.
8. Recheck all Mixin target descriptors at runtime.
9. Test two simultaneous players to detect preview-cell leakage.
10. Test close, disconnect, replay, switch spell, server stop, and exceptions for
    cleanup.

## New-Session Handoff Checklist

Before editing this subsystem in a new session:

1. Read this file and `README.md`.
2. Check `git status`; this repository may contain uncommitted user work.
3. Read the exact dependency versions in `gradle.properties` and `build.gradle`.
4. Inspect the currently resolved/deobfuscated dependency JAR, not an unrelated
   online version.
5. Treat the special-spell table and known gaps as unresolved until current
   runtime evidence proves otherwise.
6. Keep server coordinates and projected coordinates separate.
7. Keep global Minecraft context swaps inside `try/finally`.
8. Preserve server validation for every spell ID and level received from the
   client.

## 1.21.1 Migration Verification

The migration switched from NeoGradle UserDev to ModDevGradle `2.0.143` and
retained the standard `runClient`, `runServer`, `runData`, and
`runGameTestServer` tasks. The following non-runtime checks pass:

- `./gradlew compileJava`
- `./gradlew build`
- JSON and `pack.mcmeta` parsing with `jq`
- `git diff --check`

The produced JAR contains Ponder `1.0.87+mc1.21.1` and Flywheel `1.0.6` under
`META-INF/jarjar`. A normal client launch and the focused visual checklist remain
manual acceptance work; build verification does not launch `runClient`.
