package com.p1nero.iss_ponder.server;

import com.mojang.authlib.GameProfile;
import com.p1nero.iss_ponder.ISSPonderMod;
import com.p1nero.iss_ponder.api.SpellPreviewAdapter;
import com.p1nero.iss_ponder.api.SpellPreviewAdapters;
import com.p1nero.iss_ponder.api.SpellPreviewContext;
import com.p1nero.iss_ponder.network.ModNetwork;
import io.redspace.ironsspellbooks.api.magic.MagicData;
import io.redspace.ironsspellbooks.api.registry.SpellRegistry;
import io.redspace.ironsspellbooks.api.spells.AbstractSpell;
import io.redspace.ironsspellbooks.api.spells.CastSource;
import io.redspace.ironsspellbooks.api.spells.CastType;
import io.redspace.ironsspellbooks.api.util.Utils;
import io.redspace.ironsspellbooks.network.EntityEventPacket;
import io.redspace.ironsspellbooks.network.particles.AbsorptionParticlesPacket;
import io.redspace.ironsspellbooks.network.particles.BloodSiphonParticlesPacket;
import io.redspace.ironsspellbooks.network.particles.FieryExplosionParticlesPacket;
import io.redspace.ironsspellbooks.network.particles.FortifyAreaParticlesPacket;
import io.redspace.ironsspellbooks.network.particles.FrostStepParticlesPacket;
import io.redspace.ironsspellbooks.network.particles.HealParticlesPacket;
import io.redspace.ironsspellbooks.network.particles.OakskinParticlesPacket;
import io.redspace.ironsspellbooks.network.particles.RegenCloudParticlesPacket;
import io.redspace.ironsspellbooks.network.particles.ShockwaveParticlesPacket;
import io.redspace.ironsspellbooks.network.particles.TeleportParticlesPacket;
import io.redspace.ironsspellbooks.network.spells.GuidingBoltManagerStartTrackingPacket;
import io.redspace.ironsspellbooks.network.spells.GuidingBoltManagerStopTrackingPacket;
import io.netty.buffer.Unpooled;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.protocol.game.ClientboundSoundPacket;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.DoubleTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.entity.EntityTypeTest;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.common.util.FakePlayer;
import net.neoforged.neoforge.entity.IEntityWithComplexSpawn;
import net.neoforged.neoforge.event.PlayLevelSoundEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.server.ServerStoppingEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/**
 * Runs the real spell lifecycle on a server-only FakePlayer and projects its state to one client.
 *
 * <p>Upstream lifecycle references: Iron's Spellbooks
 * {@code AbstractSpell#attemptInitiateCast}, {@code AbstractSpell#castSpell},
 * {@code MagicData}, and {@code MagicManager#tick}. PonderLevel is client-only,
 * so arbitrary spell logic must remain in this real ServerLevel.</p>
 */
@EventBusSubscriber(modid = ISSPonderMod.MOD_ID)
public final class PreviewSessionManager {
    public static final ResourceKey<Level> PREVIEW_LEVEL = ResourceKey.create(
            Registries.DIMENSION, ResourceLocation.fromNamespaceAndPath(ISSPonderMod.MOD_ID, "spell_preview"));

    private static final String PREVIEW_ENTITY_TAG = "iss_ponder_preview_entity";
    private static final int FLOOR_Y = 64;
    private static final float TARGET_HEALTH = 2_048.0F;
    private static final int CAST_DELAY = 30;
    private static final int MAX_CAST_TICKS = 20 * 15;
    private static final Map<UUID, PreviewSession> SESSIONS = new HashMap<>();

    private PreviewSessionManager() {
    }

    public static void start(ServerPlayer player, ResourceLocation spellId, int requestedLevel) {
        if (SESSIONS.containsKey(player.getUUID())) {
            return;
        }

        AbstractSpell spell = SpellRegistry.getSpell(spellId);
        ServerLevel previewLevel = player.server.getLevel(PREVIEW_LEVEL);
        if (spell == null || spell == SpellRegistry.none() || previewLevel == null || !spell.isEnabled()) {
            player.displayClientMessage(net.minecraft.network.chat.Component.translatable("gui.iss_ponder.preview_unavailable"), true);
            return;
        }

        int spellLevel = Math.max(spell.getMinLevel(), Math.min(requestedLevel, spell.getMaxLevel()));
        BlockPos origin = cellOrigin(player.getUUID());
        Vec3 projectionOrigin = player.position();
        SpellPreviewAdapter adapter = SpellPreviewAdapters.get(spellId);
        PreviewSession session = new PreviewSession(player.getUUID(), spell, spellLevel, origin, projectionOrigin,
                adapter, adapter.allowsSimulation());
        SESSIONS.put(player.getUUID(), session);

        session.simulatedPlayer = createSimulatedPlayer(player, previewLevel, session);
        rebuildScene(previewLevel, session);
        resetLifecycle(session, player.server.getTickCount());

        ModNetwork.sendToPlayer(player, new ModNetwork.PreviewReady(spellId, spellLevel, session.simulationAllowed,
                origin.getX(), origin.getY(), origin.getZ()));
        session.projectionReady = true;
        ModNetwork.sendToPlayer(player, new ModNetwork.PreviewStatus(net.minecraft.network.chat.Component.translatable(
                session.simulationAllowed ? "gui.iss_ponder.preparing" : "gui.iss_ponder.restricted")));
        syncProjection(player, session, true);
    }

    public static void replay(ServerPlayer player) {
        PreviewSession session = SESSIONS.get(player.getUUID());
        if (session == null) {
            return;
        }
        int now = player.server.getTickCount();
        if (now - session.lastReplayAt < 20) {
            return;
        }
        session.lastReplayAt = now;
        ServerLevel level = player.server.getLevel(PREVIEW_LEVEL);
        if (level == null) {
            end(player);
            return;
        }

        session.projectionReady = false;
        discardScene(level, session);
        session.simulatedPlayer = createSimulatedPlayer(player, level, session);
        rebuildScene(level, session);
        resetLifecycle(session, player.server.getTickCount());
        session.projectionIds.clear();
        session.sentEntityData.clear();
        ModNetwork.sendToPlayer(player, new ModNetwork.ProjectionStart(session.origin.getX(),
                session.origin.getY(), session.origin.getZ()));
        session.projectionReady = true;
        ModNetwork.sendToPlayer(player, new ModNetwork.PreviewStatus(net.minecraft.network.chat.Component.translatable(
                session.simulationAllowed ? "gui.iss_ponder.preparing" : "gui.iss_ponder.restricted")));
        syncProjection(player, session, true);
    }

    public static void switchSpell(ServerPlayer player, ResourceLocation spellId, int requestedLevel) {
        PreviewSession session = SESSIONS.get(player.getUUID());
        AbstractSpell spell = SpellRegistry.getSpell(spellId);
        ServerLevel level = player.server.getLevel(PREVIEW_LEVEL);
        if (session == null || level == null || spell == null || spell == SpellRegistry.none() || !spell.isEnabled()) {
            player.displayClientMessage(net.minecraft.network.chat.Component.translatable(
                    "gui.iss_ponder.preview_unavailable"), true);
            return;
        }

        session.projectionReady = false;
        discardScene(level, session);
        session.spell = spell;
        session.spellLevel = Math.max(spell.getMinLevel(), Math.min(requestedLevel, spell.getMaxLevel()));
        session.adapter = SpellPreviewAdapters.get(spellId);
        session.simulationAllowed = session.adapter.allowsSimulation();
        session.simulatedPlayer = createSimulatedPlayer(player, level, session);
        rebuildScene(level, session);
        resetLifecycle(session, player.server.getTickCount());
        session.lastReplayAt = Integer.MIN_VALUE / 2;
        session.projectionIds.clear();
        session.sentEntityData.clear();

        ModNetwork.sendToPlayer(player, new ModNetwork.PreviewReady(spellId, session.spellLevel,
                session.simulationAllowed, session.origin.getX(), session.origin.getY(), session.origin.getZ()));
        session.projectionReady = true;
        ModNetwork.sendToPlayer(player, new ModNetwork.PreviewStatus(net.minecraft.network.chat.Component.translatable(
                session.simulationAllowed ? "gui.iss_ponder.preparing" : "gui.iss_ponder.restricted")));
        syncProjection(player, session, true);
    }

    public static void end(ServerPlayer player) {
        PreviewSession session = SESSIONS.remove(player.getUUID());
        if (session != null) {
            session.projectionReady = false;
            ModNetwork.sendToPlayer(player, new ModNetwork.ProjectionEnd());
            discardScene(player.server.getLevel(PREVIEW_LEVEL), session);
        }
    }

    @SubscribeEvent
    public static void onServerTick(ServerTickEvent.Post event) {
        MinecraftServer server = event.getServer();
        List<UUID> missingPlayers = new ArrayList<>();
        for (PreviewSession session : List.copyOf(SESSIONS.values())) {
            ServerPlayer player = server.getPlayerList().getPlayer(session.playerId);
            if (player == null) {
                missingPlayers.add(session.playerId);
                discardScene(server.getLevel(PREVIEW_LEVEL), session);
                continue;
            }
            tickSession(server, player, session);
        }
        missingPlayers.forEach(SESSIONS::remove);
    }

    @SubscribeEvent
    public static void onLogout(PlayerEvent.PlayerLoggedOutEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            PreviewSession session = SESSIONS.remove(player.getUUID());
            if (session != null) {
                discardScene(player.server.getLevel(PREVIEW_LEVEL), session);
            }
        }
    }

    @SubscribeEvent
    public static void onServerStopping(ServerStoppingEvent event) {
        for (PreviewSession session : List.copyOf(SESSIONS.values())) {
            discardScene(event.getServer().getLevel(PREVIEW_LEVEL), session);
        }
        SESSIONS.clear();
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onPreviewSoundAtPosition(PlayLevelSoundEvent.AtPosition event) {
        if (event.getLevel() instanceof ServerLevel level && event.getSound() != null) {
            forwardSound(level, event.getPosition(), event.getSound(), event.getSource(),
                    event.getNewVolume(), event.getNewPitch());
        }
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onPreviewSoundAtEntity(PlayLevelSoundEvent.AtEntity event) {
        if (event.getLevel() instanceof ServerLevel level && event.getSound() != null) {
            forwardSound(level, event.getEntity().position(), event.getSound(), event.getSource(),
                    event.getNewVolume(), event.getNewPitch());
        }
    }

    public static void forwardParticles(ServerLevel level, ParticleOptions particle, boolean longDistance,
                                        double x, double y, double z, int count,
                                        double xOffset, double yOffset, double zOffset, double speed) {
        forProjectionRelative(level, new Vec3(x, y, z), (player, projected) -> ModNetwork.sendToPlayer(player,
                new ModNetwork.ProjectionParticle(particle, longDistance, projected.x, projected.y, projected.z,
                        count, (float) xOffset, (float) yOffset, (float) zOffset, (float) speed)));
    }

    public static void forwardBlock(ServerLevel level, BlockPos position, BlockState state) {
        forProjectionRelative(level, Vec3.atLowerCornerOf(position), (player, projected) -> ModNetwork.sendToPlayer(player,
                new ModNetwork.ProjectionBlock(BlockPos.containing(projected).asLong(), Block.getId(state))));
    }

    public static void forwardEntityEvent(ServerLevel level, Entity entity, byte eventId) {
        if (level.dimension() != PREVIEW_LEVEL || entity == null) {
            return;
        }
        for (PreviewSession session : List.copyOf(SESSIONS.values())) {
            if (!session.projectionReady || session.simulatedPlayer == null) {
                continue;
            }
            AABB bounds = sceneBounds(session.origin);
            if (!bounds.contains(entity.position())) {
                continue;
            }
            Integer projectedId = session.projectionIds.get(entity.getUUID());
            ServerPlayer player = level.getServer().getPlayerList().getPlayer(session.playerId);
            if (projectedId != null && player != null && player.connection != null) {
                ModNetwork.sendToPlayer(player, new ModNetwork.ProjectionEntityEvent(projectedId, eventId));
            }
        }
    }

    private static void forwardSound(ServerLevel level, Vec3 position, Holder<SoundEvent> sound,
                                     SoundSource source, float volume, float pitch) {
        forProjection(level, position, (player, projected) -> player.connection.send(
                new ClientboundSoundPacket(sound, source, projected.x, projected.y, projected.z,
                        volume, pitch, level.random.nextLong())));
    }

    private static void forProjection(ServerLevel level, Vec3 serverPosition,
                                      java.util.function.BiConsumer<ServerPlayer, Vec3> action) {
        if (level.dimension() != PREVIEW_LEVEL) {
            return;
        }
        for (PreviewSession session : List.copyOf(SESSIONS.values())) {
            if (!session.projectionReady || session.simulatedPlayer == null) {
                continue;
            }
            AABB bounds = sceneBounds(session.origin);
            if (!bounds.contains(serverPosition)) {
                continue;
            }
            ServerPlayer player = level.getServer().getPlayerList().getPlayer(session.playerId);
            if (player != null && player.connection != null) {
                action.accept(player, session.projectionOrigin.add(serverPosition.subtract(
                        session.origin.getX(), session.origin.getY(), session.origin.getZ())));
            }
        }
    }

    private static void forProjectionRelative(ServerLevel level, Vec3 serverPosition,
                                               java.util.function.BiConsumer<ServerPlayer, Vec3> action) {
        if (level.dimension() != PREVIEW_LEVEL) {
            return;
        }
        for (PreviewSession session : List.copyOf(SESSIONS.values())) {
            if (!session.projectionReady || session.simulatedPlayer == null) {
                continue;
            }
            AABB bounds = sceneBounds(session.origin);
            if (!bounds.contains(serverPosition)) {
                continue;
            }
            ServerPlayer player = level.getServer().getPlayerList().getPlayer(session.playerId);
            if (player != null && player.connection != null) {
                action.accept(player, serverPosition.subtract(session.origin.getX(), session.origin.getY(), session.origin.getZ()));
            }
        }
    }

    private static void tickSession(MinecraftServer server, ServerPlayer player, PreviewSession session) {
        if (session.simulatedPlayer == null || !session.simulatedPlayer.isAlive()) {
            end(player);
            return;
        }

        LivingEntity primaryTarget = session.primaryTargetId == null ? null
                : (LivingEntity) server.getLevel(PREVIEW_LEVEL).getEntity(session.primaryTargetId);
        if (primaryTarget != null && primaryTarget.isAlive()) {
            faceTarget(session.simulatedPlayer, primaryTarget);
        }

        if (session.simulationAllowed && session.phase != CastPhase.COMPLETE
                && server.getTickCount() >= session.startAt) {
            tickSpell(session, server);
        }
        syncProjection(player, session, false);
    }

    private static void tickSpell(PreviewSession session, MinecraftServer server) {
        ServerPlayer caster = session.simulatedPlayer;
        MagicData magicData = MagicData.getPlayerMagicData(caster);
        int now = server.getTickCount();
        if (now - session.previewStartedAt >= MAX_CAST_TICKS) {
            if (magicData.isCasting()) {
                Utils.serverSideCancelCast(caster);
                ModNetwork.sendToPlayer(findRealPlayer(server, session),
                        new ModNetwork.ProjectionCastFinished(session.spell.getSpellId(), true));
            }
            completePreview(server, session);
            return;
        }

        if (session.phase == CastPhase.READY) {
            beginCast(server, session, magicData);
        } else if (session.phase == CastPhase.CASTING) {
            tickActiveCast(server, session, magicData);
        } else if (session.phase == CastPhase.FOLLOW_UP) {
            tickSimulatedPlayer(server, session, magicData);
            if (session.phase == CastPhase.COMPLETE) {
                return;
            }
            SpellPreviewContext context = context(session, now);
            try {
                SpellPreviewAdapter.Action action = session.adapter.tickAfterCast(context);
                if (action == SpellPreviewAdapter.Action.RECAST) {
                    ISSPonderMod.LOGGER.debug("Recasting spell preview {} after cast {}", session.spell.getSpellId(),
                            session.completedCasts);
                    session.adapter.beforeRecast(context);
                    session.phase = CastPhase.READY;
                    beginCast(server, session, magicData);
                } else if (action == SpellPreviewAdapter.Action.COMPLETE) {
                    completePreview(server, session);
                }
            } catch (RuntimeException exception) {
                ISSPonderMod.LOGGER.error("Spell preview adapter failed for {}", session.spell.getSpellId(), exception);
                failPreview(server, session, magicData);
            }
        }
    }

    private static void beginCast(MinecraftServer server, PreviewSession session, MagicData magicData) {
        magicData.resetCastingState();
        magicData.setMana(1_000_000);
        magicData.getSyncedData().learnSpell(session.spell, false);
        // Reference: AbstractSpell#attemptInitiateCast can call castSpell immediately for INSTANT casts.
        // Mirror ClientSpellCastHelper's start-before-cast ordering on the projected client.
        ModNetwork.sendToPlayer(findRealPlayer(server, session),
                new ModNetwork.ProjectionCastStarted(session.spell.getSpellId(), session.spellLevel));
        boolean started;
        try {
            started = session.spell.attemptInitiateCast(net.minecraft.world.item.ItemStack.EMPTY,
                    session.spellLevel, session.simulatedPlayer.level(), session.simulatedPlayer,
                    CastSource.COMMAND, false, "iss_ponder");
        } catch (RuntimeException exception) {
            ISSPonderMod.LOGGER.error("Failed to start simulated cast for {}", session.spell.getSpellId(), exception);
            started = false;
        }
        if (!started) {
            ModNetwork.sendToPlayer(findRealPlayer(server, session),
                    new ModNetwork.ProjectionCastFinished(session.spell.getSpellId(), true));
            failPreview(server, session, magicData);
            return;
        }
        session.phase = CastPhase.CASTING;
        if (session.spell.getCastType() != CastType.INSTANT) {
            ModNetwork.sendToPlayer(findRealPlayer(server, session),
                    new ModNetwork.ProjectionCastProgress(magicData.getCastCompletionPercent()));
        }
        ModNetwork.sendToPlayer(findRealPlayer(server, session), new ModNetwork.PreviewStatus(
                net.minecraft.network.chat.Component.translatable("gui.iss_ponder.casting")));
    }

    private static void tickActiveCast(MinecraftServer server, PreviewSession session, MagicData magicData) {
        tickSimulatedPlayer(server, session, magicData);
        if (session.phase == CastPhase.COMPLETE) {
            return;
        }
        if (magicData.isCasting() && session.spell.getCastType() != CastType.INSTANT) {
            ModNetwork.sendToPlayer(findRealPlayer(server, session),
                    new ModNetwork.ProjectionCastProgress(magicData.getCastCompletionPercent()));
        }
        if (!magicData.isCasting()) {
            ModNetwork.sendToPlayer(findRealPlayer(server, session),
                    new ModNetwork.ProjectionCastFinished(session.spell.getSpellId(), false));
            session.completedCasts++;
            session.lastCastCompletedAt = server.getTickCount();
            session.phase = CastPhase.FOLLOW_UP;
            try {
                session.adapter.onCastCompleted(context(session, server.getTickCount()));
            } catch (RuntimeException exception) {
                ISSPonderMod.LOGGER.error("Spell preview completion hook failed for {}", session.spell.getSpellId(), exception);
                failPreview(server, session, magicData);
                return;
            }
            ModNetwork.sendToPlayer(findRealPlayer(server, session), new ModNetwork.PreviewStatus(
                    net.minecraft.network.chat.Component.translatable("gui.iss_ponder.follow_up")));
        }
    }

    private static void tickSimulatedPlayer(MinecraftServer server, PreviewSession session, MagicData magicData) {
        try {
            // Reference: NeoForge FakePlayer#tick is intentionally empty. ServerPlayer#doTick reaches the normal
            // player lifecycle used by Iron's MagicManager/MagicData while the dummy connection absorbs packets.
            session.simulatedPlayer.doTick();
        } catch (RuntimeException exception) {
            ISSPonderMod.LOGGER.error("Failed while ticking simulated cast for {}", session.spell.getSpellId(), exception);
            failPreview(server, session, magicData);
        }
    }

    private static void failPreview(MinecraftServer server, PreviewSession session, MagicData magicData) {
        magicData.resetCastingState();
        session.phase = CastPhase.COMPLETE;
        ModNetwork.sendToPlayer(findRealPlayer(server, session), new ModNetwork.PreviewStatus(
                net.minecraft.network.chat.Component.translatable("gui.iss_ponder.cast_failed")));
    }

    private static void completePreview(MinecraftServer server, PreviewSession session) {
        session.phase = CastPhase.COMPLETE;
        ModNetwork.sendToPlayer(findRealPlayer(server, session), new ModNetwork.PreviewStatus(
                net.minecraft.network.chat.Component.translatable("gui.iss_ponder.complete")));
    }

    private static void resetLifecycle(PreviewSession session, int now) {
        session.phase = CastPhase.READY;
        session.completedCasts = 0;
        session.lastCastCompletedAt = 0;
        session.startAt = now + CAST_DELAY;
        session.previewStartedAt = session.startAt;
    }

    private static SpellPreviewContext context(PreviewSession session, int now) {
        LivingEntity target = null;
        if (session.primaryTargetId != null && session.simulatedPlayer != null) {
            Entity entity = session.simulatedPlayer.serverLevel().getEntity(session.primaryTargetId);
            if (entity instanceof LivingEntity living) {
                target = living;
            }
        }
        int sinceLastCast = session.lastCastCompletedAt == 0 ? 0 : now - session.lastCastCompletedAt;
        return new SpellPreviewContext(session.simulatedPlayer.serverLevel(), session.simulatedPlayer, target,
                session.spell, session.spellLevel, session.completedCasts, sinceLastCast);
    }

    private static ServerPlayer findRealPlayer(MinecraftServer server, PreviewSession session) {
        return server.getPlayerList().getPlayer(session.playerId);
    }

    /**
     * Called after {@code AbstractSpell#castSpell}, the same point at which Iron's sends
     * {@code OnClientCastPacket}; this preserves additional {@code ICastData} for client effects.
     */
    public static void onSimulatedCast(AbstractSpell spell, Level level, int spellLevel,
                                       ServerPlayer caster, CastSource castSource) {
        for (PreviewSession session : List.copyOf(SESSIONS.values())) {
            if (session.simulatedPlayer != caster || !session.projectionReady) {
                continue;
            }
            ServerPlayer player = caster.server.getPlayerList().getPlayer(session.playerId);
            if (player == null) {
                continue;
            }
            ModNetwork.sendToPlayer(player, new ModNetwork.ProjectionCastEffect(spell.getSpellId(), spellLevel,
                    castSource, MagicData.getPlayerMagicData(caster).getAdditionalCastData()));
        }
    }

    public static void forwardPlayerVisualPacket(ServerPlayer fakePlayer, CustomPacketPayload packet) {
        // Tracking payloads are captured by the corresponding PacketDistributor hook and must not be replayed twice.
        if (isTrackingVisualPacket(packet)) {
            return;
        }
        ModNetwork.ProjectionIronPacket projectedPacket = encodeVisualPacket(packet);
        if (projectedPacket == null) {
            return;
        }
        for (PreviewSession session : List.copyOf(SESSIONS.values())) {
            if (session.simulatedPlayer != fakePlayer || !session.projectionReady) {
                continue;
            }
            ServerPlayer player = fakePlayer.server.getPlayerList().getPlayer(session.playerId);
            if (player == null) {
                continue;
            }
            if (session.forwardedPacketTypes.add(projectedPacket.index())) {
                ISSPonderMod.LOGGER.debug("Forwarding Iron preview visual {} for {}", projectedPacket.index(),
                        session.spell.getSpellId());
            }
            ModNetwork.sendToPlayer(player, projectedPacket);
        }
    }

    public static void forwardTrackingVisualPacket(Entity trackedEntity, CustomPacketPayload message) {
        if (!(trackedEntity.level() instanceof ServerLevel level) || level.dimension() != PREVIEW_LEVEL) {
            return;
        }
        ModNetwork.ProjectionIronPacket projectedPacket = encodeVisualPacket(message);
        if (projectedPacket == null) {
            return;
        }
        forProjection(level, trackedEntity.position(), (player, ignored) ->
                ModNetwork.sendToPlayer(player, projectedPacket));
    }

    private static ModNetwork.ProjectionIronPacket encodeVisualPacket(CustomPacketPayload message) {
        int discriminator;
        java.util.function.Consumer<net.minecraft.network.FriendlyByteBuf> encoder;
        if (message instanceof TeleportParticlesPacket packet) {
            discriminator = 16;
            encoder = packet::write;
        } else if (message instanceof FrostStepParticlesPacket packet) {
            discriminator = 17;
            encoder = packet::write;
        } else if (message instanceof HealParticlesPacket packet) {
            discriminator = 21;
            encoder = packet::write;
        } else if (message instanceof BloodSiphonParticlesPacket packet) {
            discriminator = 22;
            encoder = packet::write;
        } else if (message instanceof RegenCloudParticlesPacket packet) {
            discriminator = 23;
            encoder = packet::write;
        } else if (message instanceof AbsorptionParticlesPacket packet) {
            discriminator = 26;
            encoder = packet::write;
        } else if (message instanceof FortifyAreaParticlesPacket packet) {
            discriminator = 27;
            encoder = packet::write;
        } else if (message instanceof OakskinParticlesPacket packet) {
            discriminator = 31;
            encoder = packet::write;
        } else if (message instanceof FieryExplosionParticlesPacket packet) {
            discriminator = 39;
            encoder = packet::write;
        } else if (message instanceof EntityEventPacket<?> packet) {
            discriminator = 40;
            encoder = packet::write;
        } else if (message instanceof GuidingBoltManagerStartTrackingPacket packet) {
            discriminator = 41;
            encoder = packet::write;
        } else if (message instanceof GuidingBoltManagerStopTrackingPacket packet) {
            discriminator = 42;
            encoder = packet::write;
        } else if (message instanceof ShockwaveParticlesPacket packet) {
            discriminator = 43;
            encoder = packet::write;
        } else {
            return null;
        }

        net.minecraft.network.FriendlyByteBuf buffer =
                new net.minecraft.network.FriendlyByteBuf(Unpooled.buffer());
        try {
            encoder.accept(buffer);
            byte[] payload = new byte[buffer.readableBytes()];
            buffer.readBytes(payload);
            return new ModNetwork.ProjectionIronPacket(discriminator, payload);
        } finally {
            buffer.release();
        }
    }

    /** Messages handled by the tracking hook must not also be replayed by the FakePlayer self-send hook. */
    private static boolean isTrackingVisualPacket(CustomPacketPayload message) {
        return message instanceof FieryExplosionParticlesPacket
                || message instanceof EntityEventPacket<?>
                || message instanceof GuidingBoltManagerStartTrackingPacket
                || message instanceof GuidingBoltManagerStopTrackingPacket;
    }

    private static FakePlayer createSimulatedPlayer(ServerPlayer realPlayer, ServerLevel level, PreviewSession session) {
        GameProfile profile = new GameProfile(UUID.nameUUIDFromBytes(
                ("iss_ponder:" + realPlayer.getUUID()).getBytes(java.nio.charset.StandardCharsets.UTF_8)),
                "Spell Preview");
        FakePlayer fake = new FakePlayer(level, profile);
        fake.moveTo(session.origin.getX() + 0.5, FLOOR_Y + 1.0, session.origin.getZ() - 2.0, 0, 0);
        fake.setNoGravity(true);
        fake.setInvulnerable(true);
        // ServerPlayerGameMode#setGameModeForPlayer broadcasts a player-info update. Fake players are not in the
        // real client's tab list, so that update only produces "unknown player" warnings; command casts do not
        // require creative mode, and the explicit abilities below are sufficient for the stationary simulation.
        fake.getAbilities().mayfly = true;
        fake.getAbilities().flying = true;
        level.addFreshEntity(fake);
        return fake;
    }

    private static void rebuildScene(ServerLevel level, PreviewSession session) {
        BlockPos origin = session.origin;
        // Remove the wider floor used by earlier preview layouts from persisted preview dimensions.
        for (int x = -9; x <= 9; x++) {
            for (int z = -8; z <= 11; z++) {
                if (Math.abs(x) > 3 || Math.abs(z) > 3) {
                    level.setBlockAndUpdate(origin.offset(x, 0, z), Blocks.AIR.defaultBlockState());
                }
            }
        }
        for (int x = -3; x <= 3; x++) {
            for (int z = -3; z <= 3; z++) {
                BlockPos floor = origin.offset(x, 0, z);
                boolean border = Math.abs(x) == 3 || Math.abs(z) == 3;
                level.setBlockAndUpdate(floor, (border ? Blocks.POLISHED_DEEPSLATE : Blocks.DEEPSLATE_TILES).defaultBlockState());
                for (int y = 1; y <= 8; y++) {
                    level.setBlockAndUpdate(floor.above(y), Blocks.AIR.defaultBlockState());
                }
            }
        }
        for (int z = -3; z <= 3; z++) {
            level.setBlockAndUpdate(origin.offset(0, 0, z), Blocks.POLISHED_BLACKSTONE.defaultBlockState());
        }
        level.setBlockAndUpdate(origin.offset(0, 0, 3), Blocks.CHISELED_DEEPSLATE.defaultBlockState());

        session.primaryTargetId = spawnTarget(level, origin, 0.5, 1.0, 3.0);
        spawnTarget(level, origin, -0.5, 1.0, 3.0);
        spawnTarget(level, origin, 1.5, 1.0, 3.0);
        AABB sceneBounds = sceneBounds(origin);
        for (Zombie zombie : level.getEntities(EntityTypeTest.forClass(Zombie.class), sceneBounds,
                entity -> entity.getTags().contains(PREVIEW_ENTITY_TAG))) {
            faceTowards(zombie, session.simulatedPlayer);
        }
        try {
            session.adapter.onSceneReady(context(session, session.simulatedPlayer.server.getTickCount()));
        } catch (RuntimeException exception) {
            ISSPonderMod.LOGGER.error("Spell preview scene hook failed for {}", session.spell.getSpellId(), exception);
        }
    }

    private static UUID spawnTarget(ServerLevel level, BlockPos origin,
                                    double relativeX, double relativeY, double relativeZ) {
        Zombie zombie = new Zombie(level);
        zombie.moveTo(origin.getX() + relativeX, origin.getY() + relativeY,
                origin.getZ() + relativeZ, 180, 0);
        zombie.setNoAi(true);
        zombie.setPersistenceRequired();
        zombie.addTag(PREVIEW_ENTITY_TAG);
        if (zombie.getAttribute(Attributes.MAX_HEALTH) != null) {
            zombie.getAttribute(Attributes.MAX_HEALTH).setBaseValue(TARGET_HEALTH);
        }
        zombie.setHealth(TARGET_HEALTH);
        level.addFreshEntity(zombie);
        return zombie.getUUID();
    }

    private static AABB sceneBounds(BlockPos origin) {
        return new AABB(Vec3.atLowerCornerOf(origin.offset(-24, -8, -24)),
                Vec3.atLowerCornerOf(origin.offset(25, 24, 25)));
    }

    private static void discardScene(ServerLevel level, PreviewSession session) {
        if (level == null) {
            return;
        }
        if (session.simulatedPlayer != null) {
            try {
                session.adapter.onSessionClosed(context(session, session.simulatedPlayer.server.getTickCount()));
            } catch (RuntimeException exception) {
                ISSPonderMod.LOGGER.debug("Spell preview cleanup hook failed for {}", session.spell.getSpellId(), exception);
            }
        }
        FakePlayer simulatedPlayer = session.simulatedPlayer;
        if (simulatedPlayer != null) {
            // ServerLevel player entities are registered through a specialized path and can be absent from a
            // generic entity query before their first normal tick. Always dispose the session-owned reference.
            simulatedPlayer.discard();
        }
        AABB bounds = sceneBounds(session.origin);
        for (Entity entity : level.getEntities((Entity) null, bounds, entity -> entity != null)) {
            entity.discard();
        }
        session.primaryTargetId = null;
        session.simulatedPlayer = null;
    }

    private static void faceTarget(ServerPlayer player, LivingEntity target) {
        faceTowards(player, target);
    }

    private static void faceTowards(LivingEntity entity, Entity target) {
        Vec3 delta = target.getEyePosition().subtract(entity.getEyePosition());
        double horizontal = Math.sqrt(delta.x * delta.x + delta.z * delta.z);
        float yaw = (float) (Math.toDegrees(Math.atan2(delta.z, delta.x)) - 90.0);
        float pitch = (float) -Math.toDegrees(Math.atan2(delta.y, horizontal));
        entity.setYRot(yaw);
        entity.setXRot(pitch);
        entity.setYHeadRot(yaw);
        entity.yBodyRot = yaw;
    }

    private static void syncProjection(ServerPlayer player, PreviewSession session, boolean forceSpawn) {
        if (player.connection == null || session.simulatedPlayer == null) {
            return;
        }
        ServerLevel level = session.simulatedPlayer.serverLevel();
        AABB bounds = sceneBounds(session.origin);
        List<Entity> entities = new ArrayList<>();
        // ServerLevel manages players separately from ordinary entities. A newly added FakePlayer may not be
        // returned by getEntities until doTick() runs, but cast-start packets are sent before that first tick.
        // Project the session-owned caster explicitly and first so client animation events cannot overtake it.
        if (session.simulatedPlayer.isAlive() && bounds.contains(session.simulatedPlayer.position())) {
            entities.add(session.simulatedPlayer);
        }
        for (Entity entity : level.getEntities((Entity) null, bounds,
                entity -> entity != null && entity.isAlive() && entity != session.simulatedPlayer)) {
            entities.add(entity);
        }
        Set<UUID> current = new HashSet<>();
        for (Entity entity : entities) {
            current.add(entity.getUUID());
            // Keep the server entity id. PonderLevel is isolated, and projectile NBT
            // references owners/targets by this id.
            int id = session.projectionIds.computeIfAbsent(entity.getUUID(), ignored -> entity.getId());
            Vec3 position = entity.position().subtract(session.origin.getX(), session.origin.getY(), session.origin.getZ());
            String typeId = BuiltInRegistries.ENTITY_TYPE.getKey(entity.getType()).toString();
            String name = entity instanceof ServerPlayer serverPlayer ? serverPlayer.getGameProfile().getName() : "";
            float health = entity instanceof LivingEntity living ? living.getHealth() : -1.0F;
            int hurtTime = entity instanceof LivingEntity living ? living.hurtTime : 0;
            boolean swinging = entity instanceof LivingEntity living && living.swinging;
            CompoundTag data = null;
            byte[] spawnData = new byte[0];
            if (forceSpawn || session.sentEntityData.add(entity.getUUID())) {
                try {
                    data = entity.saveWithoutId(new CompoundTag());
                    ListTag pos = new ListTag();
                    pos.add(DoubleTag.valueOf(position.x));
                    pos.add(DoubleTag.valueOf(position.y));
                    pos.add(DoubleTag.valueOf(position.z));
                    data.put("Pos", pos);
                } catch (RuntimeException exception) {
                    ISSPonderMod.LOGGER.debug("Could not serialize preview entity {}", typeId, exception);
                }
                if (entity instanceof IEntityWithComplexSpawn additionalSpawnData) {
                    // Reference: NeoForge AdvancedAddEntityPayload appends this mod-defined payload. It remains
                    // separate from NBT and is required by effects such as RayOfFrostVisualEntity#distance.
                    RegistryFriendlyByteBuf buffer = new RegistryFriendlyByteBuf(
                            Unpooled.buffer(), level.registryAccess());
                    try {
                        additionalSpawnData.writeSpawnData(buffer);
                        spawnData = new byte[buffer.readableBytes()];
                        buffer.readBytes(spawnData);
                    } catch (RuntimeException exception) {
                        ISSPonderMod.LOGGER.debug("Could not serialize preview spawn data for {}", typeId, exception);
                    } finally {
                        buffer.release();
                    }
                }
            }
            ModNetwork.sendToPlayer(player, new ModNetwork.ProjectionEntity(id, false, entity.getUUID(), typeId, name,
                    position.x, position.y, position.z, entity.getYRot(), entity.getXRot(), health,
                    hurtTime, swinging, data, spawnData,
                    java.util.Objects.requireNonNullElseGet(entity.getEntityData().getNonDefaultValues(), java.util.List::of)));
        }
        for (Map.Entry<UUID, Integer> entry : new ArrayList<>(session.projectionIds.entrySet())) {
            if (!current.contains(entry.getKey())) {
                ModNetwork.sendToPlayer(player, new ModNetwork.ProjectionEntity(entry.getValue(), true,
                        entry.getKey(), "minecraft:zombie", "", 0, 0, 0, 0, 0, 0,
                        0, false, null, new byte[0], java.util.List.of()));
                session.projectionIds.remove(entry.getKey());
                session.sentEntityData.remove(entry.getKey());
            }
        }
    }

    private static BlockPos cellOrigin(UUID uuid) {
        int xCell = (int) (Math.floorMod(uuid.getMostSignificantBits(), 300_000L) - 150_000L);
        int zCell = (int) (Math.floorMod(uuid.getLeastSignificantBits(), 300_000L) - 150_000L);
        return new BlockPos(xCell * 128, FLOOR_Y, zCell * 128);
    }

    private static final class PreviewSession {
        private final UUID playerId;
        private AbstractSpell spell;
        private int spellLevel;
        private final BlockPos origin;
        private final Vec3 projectionOrigin;
        private boolean simulationAllowed;
        private SpellPreviewAdapter adapter;
        private final Map<UUID, Integer> projectionIds = new HashMap<>();
        private final Set<UUID> sentEntityData = new HashSet<>();
        private UUID primaryTargetId;
        private FakePlayer simulatedPlayer;
        private int startAt;
        private int previewStartedAt;
        private final Set<Integer> forwardedPacketTypes = new HashSet<>();
        private int lastCastCompletedAt;
        private int completedCasts;
        private int lastReplayAt = Integer.MIN_VALUE / 2;
        private CastPhase phase = CastPhase.READY;
        private boolean projectionReady;

        private PreviewSession(UUID playerId, AbstractSpell spell, int spellLevel, BlockPos origin,
                               Vec3 projectionOrigin, SpellPreviewAdapter adapter, boolean simulationAllowed) {
            this.playerId = playerId;
            this.spell = spell;
            this.spellLevel = spellLevel;
            this.origin = origin;
            this.projectionOrigin = projectionOrigin;
            this.adapter = adapter;
            this.simulationAllowed = simulationAllowed;
        }
    }

    private enum CastPhase {
        READY,
        CASTING,
        FOLLOW_UP,
        COMPLETE
    }
}
