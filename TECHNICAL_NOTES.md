# Technical Notes

This document records the architecture and upstream code references for Iron's
Spell Scroll Ponder. It is intended to be the first document opened when work
continues in a new session.

## Version Baseline

| Component | Development baseline | Where it is pinned |
| --- | --- | --- |
| Minecraft | `1.20.1` | `gradle.properties` |
| Forge | `47.4.4` | `gradle.properties` |
| Parchment | `2023.09.03-1.20.1` | `gradle.properties` |
| Iron's Spellbooks | CurseForge file `8364933`, mod `3.16.2` | `build.gradle`, `mods.toml` |
| Ponder | `1.0.92` | `gradle.properties`, Jar-in-Jar |
| Flywheel | `1.0.0-215` | `gradle.properties`, Jar-in-Jar |
| Player Animator | CurseForge file `4587214` | `build.gradle` |

Do not assume packet discriminators, private field names, Mixin descriptors, or
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
is a Forge `FakePlayer` placed in a per-player cell in the preview dimension.

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
| `IronPacketDistributorProjectionMixin` | Captures FakePlayer and tracking-only Iron's messages before Forge writes them to dummy or untracked connections. |

## Casting Lifecycle

`PreviewSessionManager.tickSpell` deliberately calls Iron's public casting path:

1. Reset the simulated player's `MagicData`, grant preview mana, and mark the
   spell learned.
2. Send `ProjectionCastStarted` before initiation. This ordering matters for
   instant spells, whose `castSpell` can run inside `attemptInitiateCast`.
3. Call `AbstractSpell.attemptInitiateCast(..., CastSource.COMMAND, ...)`.
4. Call `FakePlayer.doTick()` on later server ticks. Forge's `FakePlayer.tick()`
   is empty; `doTick()` reaches the vanilla player tick and Iron's
   `MagicManager`/`MagicData` lifecycle.
5. Synchronize `MagicData.getCastCompletionPercent()` for non-instant spells.
6. Cancel casts still active after 15 seconds through
   `Utils.serverSideCancelCast`.

### FakePlayer Network Compatibility

Forge 47.4.4 constructs every `FakePlayer` with a shared
`FakePlayer$FakePlayerNetHandler.DUMMY_CONNECTION`. That connection is not
attached to a Netty channel, so `Connection#channel()` remains null. This is
normally harmless because the fake handler discards outgoing packets, but
Forge's `NetworkHooks#getConnectionData` and `getChannelList` dereference the
channel when `SimpleChannel#isRemotePresent` is used by optional-network
checks. A mod performing that check during the simulated player's tick can
therefore crash the preview server.

`PreviewSessionManager#initializeFakeConnection` attaches the shared dummy
connection to one persistent `EmbeddedChannel` before the simulated player is
added to the level. It also calls `NetworkHooks#registerClientLoginChannel` to
populate Forge's `FML_NETVERSION` attribute with `NONE`; this keeps
`NetworkHooks#getConnectionType` safe for mods that inspect the connection
type. Other Forge channel attributes remain empty, so remote-channel checks
return false normally, while the upstream no-op listener keeps virtual packets
local to the preview process.

The `AbstractSpellProjectionMixin` injects after `AbstractSpell.castSpell` and
sends `ProjectionCastEffect` with the spell's additional cast data. The client
then calls `AbstractSpell.onClientCast` in the projected context.

### Upstream Iron's References

These were inspected from the decompiled `3.16.2` dependency:

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

This includes positions nested inside particle options, not only the outer
particle coordinates. Iron's 3.16.2 stores absolute destinations in
`ZapParticleOption`, `SoulfireRayParticleOptions`, and `TraceParticleOptions`,
and `SwirlingParticleOptions` may recursively contain one of them.
`PreviewSessionManager#projectParticleOptions` translates those endpoints before
encoding the projection packet. It also translates tinted-cauldron block
positions and block-backed vanilla vibration destinations.

The client rejects destination-bearing particles whose endpoint is non-finite
or more than 128 blocks from the projected spawn. This guards against add-ons
passing an unprojected coordinate to effects such as `ZapParticle`, whose tube
geometry grows with path length. Without this check, a distant preview-cell
coordinate can expand Ponder's `BufferBuilder` toward 2 GiB and crash while
rendering the screen.

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

The current implementation is based on Ponder `1.0.92`:

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
plays the same `PlayerAnimationRegistry` keyframes directly. It also compensates
when the virtual level does not advance Player Animator's normal tick lifecycle.

The caster keeps its simulated server UUID for entity relationships, but renders
with the observing player's skin. `PreviewRemotePlayer` copies the local
player's GameProfile properties and snapshots its resolved skin texture plus
`default`/`slim` model. The explicit rendering overrides are required because
vanilla `AbstractClientPlayer#getSkinTextureLocation` and `getModelName` query
the connection's `PlayerInfo` by entity UUID; the isolated FakePlayer is not in
the real tab list and would otherwise fall back to a default skin.

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
it installs a raw `KeyframeAnimationPlayer` without the singleton
`IronsAdjustmentModifier`. This avoids cross-player state between the real
player, projected context player, and projected caster. Keep that difference in
mind if a future spell proves to depend on an aiming adjustment rather than its
registered keyframes. Start-animation playback still requires focused visual
acceptance after the initialization fix.

Never leave the global Minecraft player or level swapped outside the narrow
`withProjectionContext` call.

## Custom Packet Bridge

Forge custom packets sent to the simulated player are captured at Iron's
`PacketDistributor#sendToPlayer`, then encoded through Iron's own
`SimpleChannel`. This interception point matters: Forge's
`PacketDistributor#playerConsumer` writes directly to
`player.connection.connection`, bypassing both
`ServerGamePacketListenerImpl#send(Packet)` and FakePlayer's no-op listener
override. Only packets on Iron's `irons_spellbooks:messages` channel are
forwarded. Their numeric discriminator and raw payload are preserved.

`PreviewPacketBridge` intentionally replays only an allowlist of visual packet
layouts. Forge's play custom packet returns `Integer.MAX_VALUE` from
`getIndex()`; the actual SimpleChannel discriminator is the first byte of
`getInternalData()`. The server strips that byte, and the client synchronously
decodes the remaining body. Most handlers run inside the projected context.

Packet `22` is deliberately different. `BloodSiphonParticlesPacket` already
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

- Numeric packet IDs are version-specific. Re-audit all IDs after updating
  Iron's Spellbooks.
- Do not forward inventory, HUD, camera, capability, or real-player mutation
  packets merely to increase coverage.
- A packet with positions stored as `BlockPos`, primitive coordinates, nested
  objects, entity IDs, or directions needs a dedicated translation adapter.
- `ray_of_siphoning` uses `BloodSiphonParticlesPacket` and
  `ClientSpellCastHelper#handleClientboundBloodSiphonParticles`; its beam is a
  required regression test for index `22` and projected `Vec3` translation.

## Entity Spawn Data and Vanilla Events

Entity NBT and synchronized entity data are not the complete Forge spawn
contract. Entities implementing `IEntityAdditionalSpawnData` append a custom
payload in `NetworkHooks#getEntitySpawningPacket`. The server projection now
captures that payload with `writeSpawnData`, includes it in the initial entity
snapshot, and applies it with `readSpawnData` immediately after client entity
construction. The payload is intentionally sent only with the initial snapshot,
matching its spawn-only lifecycle.

This is required by
`io.redspace.ironsspellbooks.entity.spells.ray_of_frost.RayOfFrostVisualEntity`:
its beam `distance` is transferred through additional spawn data and is not
saved by `Entity#saveWithoutId`. NBT-only reconstruction therefore produced a
valid entity with a zero-length frost ray.

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

These additions changed the wire format/order, so `ModNetwork.PROTOCOL` is `8`.

## Mixin Maintenance

The Mixin descriptors in `iss_ponder.mixins.json` are version-sensitive:

- `AbstractSpellProjectionMixin` targets the non-remapped Iron's method
  `AbstractSpell#castSpell`.
- `IronPacketDistributorProjectionMixin` targets the non-remapped Iron's
  `sendToPlayer` and `sendToPlayersTrackingEntity` methods. Injecting into a
  server packet listener cannot observe `PacketDistributor.PLAYER` traffic on
  Forge 1.20.1 because that distributor writes to the underlying connection.
- `ServerLevelProjectionMixin` targets both particle overloads and
  `sendBlockUpdated`, plus `broadcastEntityEvent` for vanilla transient entity
  events such as evoker-fang event `4`.

After any Minecraft, Forge, or Iron's upgrade, run a client and dedicated server
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
| `ray_of_frost` | Generic entity projection plus Forge additional spawn data | `RayOfFrostVisualEntity#writeSpawnData` transfers the beam distance outside NBT | Build-verified; focused visual acceptance is still required. |
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

The following items came from prior runtime feedback and must not be described as
fixed until they are visually verified:

- Start animations require visual acceptance after initializing
  `MagicData.syncedSpellData` and installing the keyframes before client
  pre-cast setup. Finish-animation render-stack diagnostics already succeeded.
- Hurt time, swing state, and vanilla hurt event 2 are now projected. Live
  testing confirmed target tint; hurt animation and sound still require focused
  acceptance.
- The Ponder-specific blood-siphon beam is user-verified. Other tracking-only
  custom visual packets still need catalog-wide verification.
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

1. Run `./gradlew clean build`.
2. Run `./gradlew runGameTestServer` and confirm the preview dimension loads.
3. Run `./gradlew runClient` and test the regression categories above.
4. Inspect `build/libs/*-all.jar` and its `META-INF/jarjar/metadata.json`.
5. Confirm bundled Ponder/Flywheel versions and licenses.
6. Confirm `ModNetwork.PROTOCOL` was bumped for any wire-format or packet-order
   change.
7. Re-audit Iron's raw packet discriminator allowlist.
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
