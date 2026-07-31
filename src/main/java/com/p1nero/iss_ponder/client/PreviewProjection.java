package com.p1nero.iss_ponder.client;

import com.mojang.authlib.GameProfile;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.blaze3d.vertex.VertexSorting;
import com.p1nero.iss_ponder.ISSPonderMod;
import dev.kosmx.playerAnim.api.TransformType;
import dev.kosmx.playerAnim.api.layered.IAnimation;
import dev.kosmx.playerAnim.api.layered.KeyframeAnimationPlayer;
import dev.kosmx.playerAnim.api.layered.ModifierLayer;
import dev.kosmx.playerAnim.api.layered.modifier.AbstractFadeModifier;
import dev.kosmx.playerAnim.core.data.KeyframeAnimation;
import dev.kosmx.playerAnim.core.util.Ease;
import dev.kosmx.playerAnim.core.util.Vec3f;
import dev.kosmx.playerAnim.minecraftApi.PlayerAnimationAccess;
import dev.kosmx.playerAnim.minecraftApi.PlayerAnimationRegistry;
import io.redspace.ironsspellbooks.api.spells.SpellAnimations;
import io.redspace.ironsspellbooks.api.util.Utils;
import io.redspace.ironsspellbooks.util.ParticleHelper;
import net.createmod.catnip.levelWrappers.WrappedClientLevel;
import net.createmod.catnip.render.DefaultSuperRenderTypeBuffer;
import net.createmod.catnip.render.SuperRenderTypeBuffer;
import net.createmod.ponder.api.level.PonderLevel;
import net.createmod.ponder.foundation.PonderScene;
import net.createmod.ponder.foundation.SelectionImpl;
import net.createmod.ponder.foundation.registration.PonderLocalization;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.resources.DefaultPlayerSkin;
import net.minecraft.client.player.RemotePlayer;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Isolated client projection backed by Ponder's virtual level.
 *
 * <p>References: Ponder {@code PonderLevel#createBackup/restore}, {@code PonderScene#tick}, and
 * {@code WrappedClientLevel#of}. Server coordinates are translated to this level's zero origin.</p>
 */
public final class PreviewProjection {
    private static final int MIN_X = -24;
    private static final int MAX_X = 24;
    private static final int MIN_Y = -8;
    private static final int MAX_Y = 24;
    private static final int MIN_Z = -24;
    private static final int MAX_Z = 24;
    private static final int FLOOR_MIN_X = -3;
    private static final int FLOOR_MAX_X = 3;
    private static final int FLOOR_MIN_Z = -5;
    private static final int FLOOR_MAX_Z = 5;
    private static final float TARGET_HEALTH = 2_048.0F;
    private static final Vec3 DIFFUSE_LIGHT_0 = new Vec3(-0.2, 1.0, 0.7).normalize();
    private static final Vec3 DIFFUSE_LIGHT_1 = new Vec3(0.2, 1.0, -0.7).normalize();
    private static final int BLOOD_SIPHON_PARTICLES_PER_PACKET = 40;
    private static final int BLOOD_SIPHON_BEAM_TICKS = 3;

    private static final Map<Integer, Entity> ENTITIES = new HashMap<>();
    private static PonderLevel level;
    private static PonderScene scene;
    private static ClientLevel projectedClientLevel;
    private static LocalPlayer contextPlayer;
    private static RemotePlayer caster;
    private static ModifierLayer<IAnimation> casterAnimationLayer;
    private static Vec3 serverOrigin;
    private static Vec3 pan = Vec3.ZERO;
    private static float yaw;
    private static float pitch;
    private static float zoom;
    private static float castProgress = -1.0F;
    private static ResourceLocation pendingAnimationDiagnostic;
    private static int animationDiagnosticTicks;
    private static Vec3 bloodSiphonSource;
    private static Vec3 bloodSiphonDestination;
    private static int bloodSiphonBeamTicks;
    private static int bloodSiphonPacketCount;
    private static int bloodSiphonParticleSubmissions;
    private static boolean active;

    private PreviewProjection() {
    }

    public static void start(double ignoredX, double ignoredY, double ignoredZ) {
        clear();
        Minecraft minecraft = Minecraft.getInstance();
        ClientLevel source = minecraft.level;
        if (source == null) {
            return;
        }

        // PonderLevel is a renderable client schematic, not a ServerLevel. Never execute server spell logic here.
        level = new PonderLevel(BlockPos.ZERO, source);
        projectedClientLevel = WrappedClientLevel.of(level);
        if (minecraft.getConnection() != null && minecraft.player != null) {
            contextPlayer = new LocalPlayer(minecraft, projectedClientLevel, minecraft.getConnection(),
                    minecraft.player.getStats(), minecraft.player.getRecipeBook(), false, false);
        }
        serverOrigin = new Vec3(ignoredX, ignoredY, ignoredZ);
        installBlocks(level);
        level.createBackup();
        PonderLocalization localization = new PonderLocalization();
        scene = new PonderScene(level, localization, ISSPonderMod.MOD_ID,
                net.minecraft.resources.ResourceLocation.fromNamespaceAndPath(ISSPonderMod.MOD_ID, "spell_preview"),
                java.util.List.of(), java.util.List.of());
        // Keep the original 7x7 camera basis; the longer custom floor must not change the default framing height.
        scene.builder().configureBasePlate(-3, -3, 7);
        scene.builder().scaleSceneView(0.82F);
        scene.builder().removeShadow();
        scene.begin();
        scene.getBaseWorldSection().set(SelectionImpl.of(new BoundingBox(MIN_X, MIN_Y, MIN_Z, MAX_X, MAX_Y, MAX_Z)));

        yaw = 145.0F;
        pitch = -50.0F;
        zoom = 0.82F;
        pan = Vec3.ZERO;
        castProgress = -1.0F;
        scene.getTransform().yRotation.startWithValue(yaw);
        scene.getTransform().xRotation.startWithValue(pitch);
        active = true;
    }

    private static void installBlocks(PonderLevel target) {
        for (int x = FLOOR_MIN_X; x <= FLOOR_MAX_X; x++) {
            for (int z = FLOOR_MIN_Z; z <= FLOOR_MAX_Z; z++) {
                BlockPos floor = new BlockPos(x, 0, z);
                boolean border = x == FLOOR_MIN_X || x == FLOOR_MAX_X
                        || z == FLOOR_MIN_Z || z == FLOOR_MAX_Z;
                target.setBlock(floor, (border ? Blocks.POLISHED_DEEPSLATE : Blocks.DEEPSLATE_TILES).defaultBlockState(), 19);
            }
        }
        for (int z = FLOOR_MIN_Z; z <= FLOOR_MAX_Z; z++) {
            target.setBlock(new BlockPos(0, 0, z), Blocks.POLISHED_BLACKSTONE.defaultBlockState(), 19);
        }
        target.setBlock(new BlockPos(0, 0, FLOOR_MAX_Z), Blocks.CHISELED_DEEPSLATE.defaultBlockState(), 19);
    }

    public static void resetForReplay(double x, double y, double z) {
        if (!active || level == null || scene == null) {
            start(x, y, z);
            return;
        }
        level.getEntityList().forEach(Entity::discard);
        level.restore();
        ENTITIES.clear();
        caster = null;
        casterAnimationLayer = null;
        castProgress = -1.0F;
        pendingAnimationDiagnostic = null;
        animationDiagnosticTicks = 0;
        clearBloodSiphonVisual();
        serverOrigin = new Vec3(x, y, z);
        scene.getBaseWorldSection().queueRedraw();
    }

    public static void spawnOrUpdate(int id, UUID uuid, String typeId, String name,
                                     double x, double y, double z, float entityYaw, float entityPitch,
                                     float health, int serverHurtTime, boolean serverSwinging, CompoundTag data,
                                     byte[] spawnData,
                                     java.util.List<net.minecraft.network.syncher.SynchedEntityData.DataValue<?>> syncedData) {
        if (!active || level == null) {
            return;
        }
        Entity entity = ENTITIES.get(id);
        EntityType<?> type = EntityType.byString(typeId).orElse(EntityType.ZOMBIE);
        if (entity == null || entity.getType() != type) {
            remove(id);
            if (type == EntityType.PLAYER) {
                ClientLevel wrapped = projectedClientLevel == null ? WrappedClientLevel.of(level) : projectedClientLevel;
                LocalPlayer skinSource = Minecraft.getInstance().player;
                GameProfile profile = new GameProfile(uuid,
                        name == null || name.isBlank() ? "Spell Preview" : name);
                if (skinSource != null) {
                    profile.getProperties().putAll(skinSource.getGameProfile().getProperties());
                }
                entity = new PreviewRemotePlayer(wrapped, profile, skinSource);
                if (entity instanceof RemotePlayer remote) {
                    caster = remote;
                    ensurePlayerAnimationLayer(remote);
                }
            } else {
                entity = type.create(level);
            }
            if (entity == null) {
                return;
            }
            entity.setId(id);
            entity.setUUID(uuid);
            if (data != null) {
                try {
                    entity.load(data.copy());
                } catch (RuntimeException exception) {
                    ISSPonderMod.LOGGER.debug("Could not load preview entity data for {}", typeId, exception);
                }
            }
            if (spawnData != null && spawnData.length > 0
                    && entity instanceof net.minecraftforge.entity.IEntityAdditionalSpawnData additionalSpawnData) {
                // Reference: NetworkHooks#getEntitySpawningPacket writes this payload after the vanilla spawn
                // fields. RayOfFrostVisualEntity stores its beam distance only here, never in entity NBT.
                net.minecraft.network.FriendlyByteBuf buffer = new net.minecraft.network.FriendlyByteBuf(
                        io.netty.buffer.Unpooled.wrappedBuffer(spawnData));
                try {
                    additionalSpawnData.readSpawnData(buffer);
                } catch (RuntimeException exception) {
                    ISSPonderMod.LOGGER.debug("Could not load preview spawn data for {}", typeId, exception);
                } finally {
                    buffer.release();
                }
            }
            level.addFreshEntity(entity);
            ENTITIES.put(id, entity);
        }

        entity.moveTo(x, y, z, entityYaw, entityPitch);
        entity.setYRot(entityYaw);
        entity.setXRot(entityPitch);
        if (syncedData != null && !syncedData.isEmpty()) {
            entity.getEntityData().assignValues(syncedData);
        }
        if (entity instanceof Mob mob) {
            mob.setNoAi(true);
        }
        if (entity instanceof LivingEntity living && Float.isFinite(health) && health >= 0) {
            living.setYHeadRot(entityYaw);
            living.yHeadRotO = entityYaw;
            living.yBodyRot = entityYaw;
            living.yBodyRotO = entityYaw;
            if (health > living.getMaxHealth() && living.getAttribute(Attributes.MAX_HEALTH) != null) {
                living.getAttribute(Attributes.MAX_HEALTH).setBaseValue(health);
            }
            living.setHealth(Math.min(health, living.getMaxHealth()));
            if (serverHurtTime > living.hurtTime) {
                // Vanilla entity event 2 drives hurt tint, knock animation, and the entity's hurt sound.
                living.handleEntityEvent((byte) 2);
                living.hurtTime = serverHurtTime;
                living.hurtDuration = Math.max(living.hurtDuration, serverHurtTime);
            }
            if (serverSwinging && living.swingTime == 0) {
                living.swing(net.minecraft.world.InteractionHand.MAIN_HAND);
            }
        }
    }

    public static void remove(int id) {
        Entity entity = ENTITIES.remove(id);
        if (entity != null && level != null) {
            level.getEntityList().remove(entity);
            entity.discard();
            if (entity == caster) {
                caster = null;
                casterAnimationLayer = null;
            }
        }
    }

    public static void handleClientEntityEvent(int id, byte eventId) {
        Entity entity = ENTITIES.get(id);
        if (entity instanceof io.redspace.ironsspellbooks.api.network.IClientEventEntity clientEventEntity) {
            clientEventEntity.handleClientEvent(eventId);
        }
    }

    public static void handleVanillaEntityEvent(int id, byte eventId) {
        Entity entity = ENTITIES.get(id);
        if (entity != null) {
            // Reference: ClientPacketListener#handleEntityEvent delegates packet events to this method.
            // ExtendedEvokerFang#tick sends event 4; EvokerFangs#handleEntityEvent starts the client attack.
            entity.handleEntityEvent(eventId);
        }
    }

    private static void ensurePlayerAnimationLayer(RemotePlayer player) {
        try {
            // Iron's normal factory adds IronsAdjustmentModifier.INSTANCE, a single global reference shared by
            // every client player. A preview owns a LocalPlayer context and a RemotePlayer caster at once, so use
            // an isolated layer and play the same registered keyframes without that cross-player modifier.
            PlayerAnimationAccess.PlayerAssociatedAnimationData data =
                    PlayerAnimationAccess.getPlayerAssociatedData(player);
            IAnimation existing = data.get(SpellAnimations.ANIMATION_RESOURCE);
            if (existing != null) {
                PlayerAnimationAccess.getPlayerAnimLayer(player).removeLayer(existing);
            }
            ModifierLayer<IAnimation> layer = new ModifierLayer<>();
            data.set(SpellAnimations.ANIMATION_RESOURCE, layer);
            PlayerAnimationAccess.getPlayerAnimLayer(player).addAnimLayer(42, layer);
            casterAnimationLayer = layer;
        } catch (RuntimeException exception) {
            ISSPonderMod.LOGGER.debug("Could not initialize the preview player animation layer", exception);
        }
    }

    public static void updateBlock(BlockPos position, int stateId) {
        if (!active || level == null) {
            return;
        }
        level.setBlock(position, Block.stateById(stateId), 19);
        scene.getBaseWorldSection().queueRedraw();
    }

    public static void addParticle(net.minecraft.core.particles.ParticleOptions particle, boolean longDistance,
                                   double x, double y, double z, int count,
                                   float xOffset, float yOffset, float zOffset, float speed) {
        if (!active || level == null || particle == null) {
            return;
        }
        net.minecraft.util.RandomSource random = net.minecraft.util.RandomSource.create();
        if (count == 0) {
            level.addParticle(particle, x, y, z, xOffset * speed, yOffset * speed, zOffset * speed);
            return;
        }
        for (int i = 0; i < count; i++) {
            double px = x + random.nextGaussian() * xOffset;
            double py = y + random.nextGaussian() * yOffset;
            double pz = z + random.nextGaussian() * zOffset;
            double vx = random.nextGaussian() * speed;
            double vy = random.nextGaussian() * speed;
            double vz = random.nextGaussian() * speed;
            level.addParticle(particle, px, py, pz, vx, vy, vz);
        }
    }

    /**
     * Replays Iron's packet 22 without relying on its global Minecraft player lookup.
     *
     * <p>References: {@code RayOfSiphoningSpell#onCast} sends target-center then caster-center, and
     * {@code ClientSpellCastHelper#handleClientboundBloodSiphonParticles} emits 40 BLOOD particles from the first
     * endpoint with {@code (destination - source) * 0.1} velocity plus the same random spread. Ponder's
     * {@code PonderLevel#addParticle} owns those particles. The short-lived ribbon is a Ponder-specific visibility
     * fallback because {@code SiphonParticle} lives for only one to five ticks and is not a geometric beam.</p>
     */
    static void emitBloodSiphon(Vec3 source, Vec3 destination) {
        if (!active || level == null || !isFinite(source) || !isFinite(destination)) {
            return;
        }
        Vec3 direction = destination.subtract(source).scale(0.1F);
        for (int i = 0; i < BLOOD_SIPHON_PARTICLES_PER_PACKET; i++) {
            Vec3 velocity = direction.scale(1.0 + Utils.getRandomScaled(0.35));
            Vec3 spread = new Vec3(Utils.getRandomScaled(0.08F), Utils.getRandomScaled(0.08F),
                    Utils.getRandomScaled(0.08F));
            level.addParticle(ParticleHelper.BLOOD, source.x, source.y, source.z,
                    velocity.x + spread.x, velocity.y + spread.y, velocity.z + spread.z);
        }
        bloodSiphonSource = source;
        bloodSiphonDestination = destination;
        bloodSiphonBeamTicks = BLOOD_SIPHON_BEAM_TICKS;
        bloodSiphonPacketCount++;
        bloodSiphonParticleSubmissions += BLOOD_SIPHON_PARTICLES_PER_PACKET;
    }

    public static void tick() {
        if (active && scene != null) {
            ModifierLayer<IAnimation> animationLayer = getAnimationLayer(false);
            IAnimation animation = animationLayer == null ? null : animationLayer.getAnimation();
            int animationTick = currentAnimationTick(animation);
            scene.tick();
            if (bloodSiphonBeamTicks > 0) {
                bloodSiphonBeamTicks--;
            }
            if (animationLayer != null && animation != null && animation.isActive()
                    && animationLayer.getAnimation() == animation
                    && currentAnimationTick(animation) == animationTick) {
                // Virtual levels do not always run the Player Animator mixin lifecycle.
                // Only advance the layer when the Ponder entity tick did not do so.
                animationLayer.tick();
            }
            traceRenderedAnimationPose();
        }
    }

    public static void render(GuiGraphics graphics, int sceneWidth, int height, float partialTick) {
        if (!active || scene == null || level == null) {
            return;
        }
        SuperRenderTypeBuffer buffer = DefaultSuperRenderTypeBuffer.getInstance();
        RenderSystemFacade.render(scene, buffer, graphics, sceneWidth, height, partialTick, pan);
        traceRenderedAnimationPose();
    }

    public static void updateCamera(float yawDelta, float pitchDelta) {
        if (!active || scene == null) {
            return;
        }
        yaw = wrapDegrees(yaw + yawDelta);
        pitch = Math.max(-82.0F, Math.min(20.0F, pitch + pitchDelta));
        scene.getTransform().yRotation.setValue(yaw);
        scene.getTransform().xRotation.setValue(pitch);
    }

    public static void zoomCamera(double amount) {
        if (!active || scene == null) {
            return;
        }
        zoom = Math.max(0.45F, Math.min(1.8F, zoom + (float) amount * 0.08F));
        scene.builder().scaleSceneView(zoom);
    }

    public static void panCamera(double forward, double right, double vertical) {
        if (!active) {
            return;
        }
        double radians = Math.toRadians(yaw);
        Vec3 forwardAxis = new Vec3(-Math.sin(radians), 0, Math.cos(radians));
        Vec3 rightAxis = new Vec3(Math.cos(radians), 0, Math.sin(radians));
        Vec3 delta = forwardAxis.scale(forward * 0.35)
                .add(rightAxis.scale(right * 0.35))
                .add(0, vertical * 0.35, 0);
        pan = pan.add(delta);
    }

    public static boolean isActive() {
        return active;
    }

    static Vec3 serverOrigin() {
        return serverOrigin;
    }

    static void withProjectionContext(Runnable action) {
        Minecraft minecraft = Minecraft.getInstance();
        LocalPlayer original = minecraft.player;
        ClientLevel originalLevel = minecraft.level;
        if (!active || level == null || projectedClientLevel == null || contextPlayer == null
                || original == null || caster == null || minecraft.getConnection() == null) {
            return;
        }
        // Reference: Iron's client packet helpers commonly read Minecraft.player and player.level directly.
        // Keep this global context substitution narrow and always restore it in finally.
        contextPlayer.moveTo(caster.position());
        contextPlayer.setYRot(caster.getYRot());
        contextPlayer.setXRot(caster.getXRot());
        minecraft.level = projectedClientLevel;
        minecraft.player = contextPlayer;
        try {
            action.run();
        } finally {
            minecraft.player = original;
            minecraft.level = originalLevel;
        }
    }

    public static float observedDamage() {
        if (level == null) {
            return 0;
        }
        return (float) ENTITIES.values().stream()
                .filter(entity -> entity instanceof net.minecraft.world.entity.monster.Zombie)
                .map(entity -> (LivingEntity) entity)
                .sorted((a, b) -> Double.compare(a.distanceToSqr(Vec3.ZERO), b.distanceToSqr(Vec3.ZERO)))
                .limit(3)
                .mapToDouble(zombie -> Math.max(0.0, TARGET_HEALTH - zombie.getHealth()))
                .sum();
    }

    public static float castProgress() {
        return castProgress;
    }

    public static void updateCastProgress(float progress) {
        castProgress = Mth.clamp(progress, 0.0F, 1.0F);
    }

    public static void castStarted(String spellId, int spellLevel) {
        if (!active || caster == null || level == null) {
            return;
        }
        io.redspace.ironsspellbooks.api.spells.AbstractSpell spell =
                io.redspace.ironsspellbooks.api.registry.SpellRegistry.getSpell(spellId);
        if (spell == null) {
            return;
        }
        if ("irons_spellbooks:ray_of_siphoning".equals(spellId)) {
            clearBloodSiphonVisual();
        }
        java.util.Optional<ResourceLocation> startAnimation = spell.getCastStartAnimation().getForPlayer();
        // Reference: ClientSpellCastHelper#handleClientBoundOnCastStarted starts Player Animator before
        // building the client pre-cast state. Do this first so a spell-data failure cannot swallow the pose.
        startAnimation.ifPresent(PreviewProjection::playPlayerAnimation);
        io.redspace.ironsspellbooks.api.magic.MagicData data = new io.redspace.ironsspellbooks.api.magic.MagicData();
        // MagicData() leaves syncedSpellData null, while initiateCast dereferences that field directly. Iron's
        // ClientSpellCastHelper passes null MagicData here; the preview supplies usable state for add-on pre-cast
        // hooks, so it must attach a caster-specific SyncedSpellData before initialization.
        data.setSyncedData(new io.redspace.ironsspellbooks.capabilities.magic.SyncedSpellData(caster));
        data.initiateCast(spell, spellLevel, spell.getEffectiveCastTime(spellLevel, caster),
                io.redspace.ironsspellbooks.api.spells.CastSource.COMMAND, "iss_ponder");
        withProjectionContext(() -> {
            try {
                spell.onClientPreCast(level, spellLevel, caster, net.minecraft.world.InteractionHand.MAIN_HAND, data);
            } catch (RuntimeException exception) {
                ISSPonderMod.LOGGER.debug("Preview client pre-cast failed for {}", spellId, exception);
            }
        });
    }

    public static void castEffect(String spellId, int spellLevel,
                                  io.redspace.ironsspellbooks.api.spells.ICastData castData) {
        if (!active || caster == null || level == null) {
            return;
        }
        io.redspace.ironsspellbooks.api.spells.AbstractSpell spell =
                io.redspace.ironsspellbooks.api.registry.SpellRegistry.getSpell(spellId);
        if (spell == null) {
            return;
        }
        withProjectionContext(() -> {
            try {
                spell.onClientCast(level, spellLevel, caster, castData);
            } catch (RuntimeException exception) {
                ISSPonderMod.LOGGER.debug("Preview client cast effect failed for {}", spellId, exception);
            }
        });
    }

    public static void castFinished(String spellId, boolean cancelled) {
        if (!active || caster == null) {
            return;
        }
        io.redspace.ironsspellbooks.api.spells.AbstractSpell spell =
                io.redspace.ironsspellbooks.api.registry.SpellRegistry.getSpell(spellId);
        if (spell == null) {
            return;
        }
        try {
            io.redspace.ironsspellbooks.api.util.AnimationHolder finishAnimation = spell.getCastFinishAnimation();
            java.util.Optional<net.minecraft.resources.ResourceLocation> animation = finishAnimation.getForPlayer();
            if (animation.isPresent() && !cancelled) {
                playPlayerAnimation(animation.get());
            } else if (finishAnimation != io.redspace.ironsspellbooks.api.util.AnimationHolder.pass() || cancelled) {
                cancelPlayerAnimation();
            }
            castProgress = -1.0F;
            if ("irons_spellbooks:ray_of_siphoning".equals(spellId)) {
                ISSPonderMod.LOGGER.debug("Projected blood siphon submitted {} particles from {} packets",
                        bloodSiphonParticleSubmissions, bloodSiphonPacketCount);
                bloodSiphonBeamTicks = 0;
            }
        } catch (RuntimeException exception) {
            ISSPonderMod.LOGGER.debug("Preview client cast finish failed for {}", spellId, exception);
        }
    }

    public static void clear() {
        if (level != null) {
            level.getEntityList().forEach(Entity::discard);
            level.getEntityList().clear();
        }
        ENTITIES.clear();
        caster = null;
        casterAnimationLayer = null;
        contextPlayer = null;
        projectedClientLevel = null;
        serverOrigin = null;
        scene = null;
        level = null;
        pan = Vec3.ZERO;
        castProgress = -1.0F;
        pendingAnimationDiagnostic = null;
        animationDiagnosticTicks = 0;
        clearBloodSiphonVisual();
        active = false;
    }

    private static void playPlayerAnimation(ResourceLocation animation) {
        if (caster == null) {
            return;
        }
        ModifierLayer<IAnimation> layer = getAnimationLayer(true);
        KeyframeAnimation keyframes = PlayerAnimationRegistry.getAnimation(animation);
        if (layer != null && keyframes != null) {
            // Unlike the real player, the projected caster has no preceding locomotion pose to blend from.
            // A fade modifier can remain at its identity pose in Ponder's isolated render stack, so install the
            // same Iron's keyframes directly. Cast finish still fades the animation out below.
            layer.setAnimation(new KeyframeAnimationPlayer(keyframes));
            pendingAnimationDiagnostic = animation;
            animationDiagnosticTicks = 0;
        } else if (keyframes == null) {
            ISSPonderMod.LOGGER.debug("Missing preview player animation {}", animation);
        } else {
            ISSPonderMod.LOGGER.debug("Missing preview player animation layer for {}", animation);
        }
    }

    private static void cancelPlayerAnimation() {
        ModifierLayer<IAnimation> layer = getAnimationLayer(false);
        if (layer != null) {
            layer.replaceAnimationWithFade(AbstractFadeModifier.standardFadeIn(4, Ease.INOUTSINE), null, false);
        }
    }

    @SuppressWarnings("unchecked")
    private static ModifierLayer<IAnimation> getAnimationLayer(boolean create) {
        if (caster == null) {
            return null;
        }
        if (casterAnimationLayer != null) {
            return casterAnimationLayer;
        }
        try {
            PlayerAnimationAccess.PlayerAssociatedAnimationData data =
                    PlayerAnimationAccess.getPlayerAssociatedData(caster);
            IAnimation existing = data.get(SpellAnimations.ANIMATION_RESOURCE);
            if (existing instanceof ModifierLayer<?> layer) {
                casterAnimationLayer = (ModifierLayer<IAnimation>) layer;
                return casterAnimationLayer;
            }
            if (!create) {
                return null;
            }
            ensurePlayerAnimationLayer(caster);
            return casterAnimationLayer;
        } catch (RuntimeException exception) {
            ISSPonderMod.LOGGER.debug("Could not access preview player animation layer", exception);
            return null;
        }
    }

    private static int currentAnimationTick(IAnimation animation) {
        return animation instanceof KeyframeAnimationPlayer player ? player.getCurrentTick() : Integer.MIN_VALUE;
    }

    private static void traceRenderedAnimationPose() {
        if (pendingAnimationDiagnostic == null || caster == null || ++animationDiagnosticTicks < 3) {
            return;
        }
        try {
            // PlayerModelMixin renders from the complete AnimationStack, so inspect that same stack rather than
            // only the keyframe player. This trace is a low-noise runtime proof that the projected pose is visible.
            Vec3f rotation = PlayerAnimationAccess.getPlayerAnimLayer(caster).get3DTransform(
                    "rightArm", TransformType.ROTATION, 0.0F, Vec3f.ZERO);
            if (!Vec3f.ZERO.equals(rotation)) {
                ISSPonderMod.LOGGER.debug("Projected player animation {} reached the render stack at tick {} "
                                + "(right-arm rotation {})",
                        pendingAnimationDiagnostic, animationDiagnosticTicks, rotation);
                pendingAnimationDiagnostic = null;
            } else if (animationDiagnosticTicks >= 20) {
                ISSPonderMod.LOGGER.debug("Projected player animation {} did not produce a render-stack pose",
                        pendingAnimationDiagnostic);
                pendingAnimationDiagnostic = null;
            }
        } catch (RuntimeException exception) {
            ISSPonderMod.LOGGER.debug("Could not inspect the projected player animation pose", exception);
            pendingAnimationDiagnostic = null;
        }
    }

    private static float wrapDegrees(float value) {
        value %= 360.0F;
        return value < -180.0F ? value + 360.0F : value >= 180.0F ? value - 360.0F : value;
    }

    private static boolean isFinite(Vec3 position) {
        return Double.isFinite(position.x) && Double.isFinite(position.y) && Double.isFinite(position.z);
    }

    private static void clearBloodSiphonVisual() {
        bloodSiphonSource = null;
        bloodSiphonDestination = null;
        bloodSiphonBeamTicks = 0;
        bloodSiphonPacketCount = 0;
        bloodSiphonParticleSubmissions = 0;
    }

    private static void renderBloodSiphon(PoseStack pose, SuperRenderTypeBuffer buffer, float partialTick) {
        if (bloodSiphonBeamTicks <= 0 || bloodSiphonSource == null || bloodSiphonDestination == null) {
            return;
        }
        Vec3 axis = bloodSiphonDestination.subtract(bloodSiphonSource);
        if (axis.lengthSqr() < 1.0E-6) {
            return;
        }

        Vec3 direction = axis.normalize();
        Vec3 reference = Math.abs(direction.y) > 0.9 ? new Vec3(1, 0, 0) : new Vec3(0, 1, 0);
        Vec3 side = direction.cross(reference).normalize();
        Vec3 vertical = direction.cross(side).normalize();
        float pulse = 0.88F + 0.12F * Mth.sin((Minecraft.getInstance().level == null
                ? partialTick : Minecraft.getInstance().level.getGameTime() + partialTick) * 1.7F);
        VertexConsumer vertices = buffer.getBuffer(RenderType.lightning());

        // Crossed ribbons stay visible from every Ponder camera angle. RenderType.lightning is untextured and
        // emissive, avoiding the custom-particle atlas path that can fail independently of packet delivery.
        renderBeamCross(pose, vertices, bloodSiphonSource, bloodSiphonDestination,
                side, vertical, 0.105F * pulse, 92, 0, 12, 150);
        renderBeamCross(pose, vertices, bloodSiphonSource, bloodSiphonDestination,
                side, vertical, 0.035F * pulse, 224, 24, 48, 230);
    }

    private static void renderBeamCross(PoseStack pose, VertexConsumer vertices, Vec3 start, Vec3 end,
                                        Vec3 side, Vec3 vertical, float radius,
                                        int red, int green, int blue, int alpha) {
        renderBeamRibbon(pose, vertices, start, end, side.scale(radius), red, green, blue, alpha);
        renderBeamRibbon(pose, vertices, start, end, vertical.scale(radius), red, green, blue, alpha);
    }

    private static void renderBeamRibbon(PoseStack pose, VertexConsumer vertices, Vec3 start, Vec3 end, Vec3 offset,
                                         int red, int green, int blue, int alpha) {
        Matrix4f matrix = pose.last().pose();
        beamVertex(vertices, matrix, start.subtract(offset), red, green, blue, alpha);
        beamVertex(vertices, matrix, start.add(offset), red, green, blue, alpha);
        beamVertex(vertices, matrix, end.add(offset), red, green, blue, alpha);
        beamVertex(vertices, matrix, end.subtract(offset), red, green, blue, alpha);
    }

    private static void beamVertex(VertexConsumer vertices, Matrix4f matrix, Vec3 position,
                                   int red, int green, int blue, int alpha) {
        vertices.vertex(matrix, (float) position.x, (float) position.y, (float) position.z)
                .color(red, green, blue, alpha)
                .endVertex();
    }

    private static final class RenderSystemFacade {
        private static void render(PonderScene scene, SuperRenderTypeBuffer buffer, GuiGraphics graphics,
                                   int width, int height, float partialTick, Vec3 pan) {
            Minecraft minecraft = Minecraft.getInstance();
            Entity originalCamera = minecraft.getCameraEntity();
            com.mojang.blaze3d.systems.RenderSystem.enableBlend();
            com.mojang.blaze3d.systems.RenderSystem.enableDepthTest();
            com.mojang.blaze3d.systems.RenderSystem.backupProjectionMatrix();
            PoseStack pose = graphics.pose();
            boolean pushed = false;
            boolean sceneRenderCompleted = false;
            try {
                com.mojang.blaze3d.systems.RenderSystem.setupLevelDiffuseLighting(
                        new org.joml.Vector3f((float) DIFFUSE_LIGHT_0.x, (float) DIFFUSE_LIGHT_0.y, (float) DIFFUSE_LIGHT_0.z),
                        new org.joml.Vector3f((float) DIFFUSE_LIGHT_1.x, (float) DIFFUSE_LIGHT_1.y, (float) DIFFUSE_LIGHT_1.z),
                        pose.last().pose());
                Matrix4f projection = new Matrix4f(com.mojang.blaze3d.systems.RenderSystem.getProjectionMatrix());
                projection.translate(0, 0, 800);
                com.mojang.blaze3d.systems.RenderSystem.setProjectionMatrix(projection, VertexSorting.DISTANCE_TO_ORIGIN);
                pose.pushPose();
                pushed = true;
                pose.translate(0, 0, -800);
                scene.getTransform().updateScreenParams(width, height, 0);
                scene.getTransform().apply(pose, partialTick);
                pose.translate(pan.x, pan.y, pan.z);
                scene.getTransform().updateSceneRVE(partialTick);
                scene.renderScene(buffer, graphics, partialTick);
                renderBloodSiphon(pose, buffer, partialTick);
                sceneRenderCompleted = true;
            } finally {
                try {
                    buffer.draw();
                } finally {
                    if (pushed) {
                        // PonderScene pushes the same PoseStack without a finally block.
                        // If its render fails, remove that leaked pose before ours.
                        if (!sceneRenderCompleted) {
                            pose.popPose();
                        }
                        pose.popPose();
                    }
                    minecraft.setCameraEntity(originalCamera);
                    com.mojang.blaze3d.systems.RenderSystem.restoreProjectionMatrix();
                }
            }
        }
    }

    /**
     * Keeps the simulated caster's server UUID while rendering the observer's resolved skin and arm model.
     *
     * <p>Reference: vanilla {@code AbstractClientPlayer#getSkinTextureLocation/getModelName} normally look up
     * {@code PlayerInfo} by entity UUID. The preview FakePlayer is intentionally absent from the real tab list, so
     * those methods would otherwise select a default skin even when its copied profile has texture properties.</p>
     */
    private static final class PreviewRemotePlayer extends RemotePlayer {
        private final ResourceLocation skinTexture;
        private final String skinModel;

        private PreviewRemotePlayer(ClientLevel level, GameProfile profile, LocalPlayer skinSource) {
            super(level, profile);
            if (skinSource != null) {
                skinTexture = skinSource.getSkinTextureLocation();
                skinModel = skinSource.getModelName();
            } else {
                skinTexture = DefaultPlayerSkin.getDefaultSkin(profile.getId());
                skinModel = DefaultPlayerSkin.getSkinModelName(profile.getId());
            }
        }

        @Override
        public boolean isSkinLoaded() {
            return true;
        }

        @Override
        public ResourceLocation getSkinTextureLocation() {
            return skinTexture;
        }

        @Override
        public String getModelName() {
            return skinModel;
        }
    }
}
