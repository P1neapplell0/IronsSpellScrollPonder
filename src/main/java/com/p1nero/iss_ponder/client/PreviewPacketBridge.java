package com.p1nero.iss_ponder.client;

import com.p1nero.iss_ponder.ISSPonderMod;
import io.redspace.ironsspellbooks.player.ClientSpellCastHelper;
import io.redspace.ironsspellbooks.effect.guiding_bolt.GuidingBoltManager;
import io.netty.buffer.Unpooled;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.Vec3;

/**
 * Replays an allowlist of Iron's built-in client particle payloads against the virtual level.
 * The integer keys are this mod's stable visual IDs; payload layouts are tied to Iron's Spellbooks 3.16.2.
 * Visual ID 22 maps to BloodSiphonParticlesPacket; its dedicated adapter mirrors
 * ClientSpellCastHelper#handleClientboundBloodSiphonParticles directly in PonderLevel.
 */
public final class PreviewPacketBridge {
    private static final java.util.Set<Integer> VISUAL_PACKET_TYPES = java.util.Set.of(
            16, 17, 21, 22, 23, 26, 27, 31, 39, 40, 41, 42, 43);
    private static final java.util.Set<Integer> REPLAYED_PACKET_TYPES = new java.util.HashSet<>();

    private PreviewPacketBridge() {
    }

    public static void handle(int index, byte[] payload) {
        if (!PreviewProjection.isActive() || !VISUAL_PACKET_TYPES.contains(index)) {
            return;
        }
        FriendlyByteBuf buffer = new FriendlyByteBuf(Unpooled.wrappedBuffer(payload));
        try {
            if (index == 22) {
                // BloodSiphonParticlesPacket already contains both world-space endpoints. Decode it directly so
                // this continuous effect cannot be dropped when Iron's temporary LocalPlayer context is absent.
                PreviewProjection.emitBloodSiphon(readPosition(buffer), readPosition(buffer));
            } else {
                PreviewProjection.withProjectionContext(() -> decodeVisualPacket(index, buffer));
            }
            if (REPLAYED_PACKET_TYPES.add(index)) {
                ISSPonderMod.LOGGER.debug("Replayed Iron preview packet {}", index);
            }
        } catch (RuntimeException exception) {
            ISSPonderMod.LOGGER.debug("Could not replay Iron preview packet {}", index, exception);
        } finally {
            buffer.release();
        }
    }

    private static void decodeVisualPacket(int index, FriendlyByteBuf buffer) {
        // Visual IDs preserve the original bridge table, but Iron's 3.16.2 payloads are captured by concrete type.
        switch (index) {
            case 16 -> ClientSpellCastHelper.handleClientboundTeleport(readPosition(buffer), readPosition(buffer));
            case 17 -> ClientSpellCastHelper.handleClientboundFrostStep(readPosition(buffer), readPosition(buffer));
            case 21 -> ClientSpellCastHelper.handleClientsideHealParticles(readPosition(buffer));
            case 23 -> ClientSpellCastHelper.handleClientsideRegenCloudParticles(readPosition(buffer));
            case 26 -> ClientSpellCastHelper.handleClientsideAbsorptionParticles(readPosition(buffer));
            case 27 -> ClientSpellCastHelper.handleClientsideFortifyAreaParticles(readPosition(buffer));
            case 31 -> ClientSpellCastHelper.handleClientboundOakskinParticles(readPosition(buffer));
            case 39 -> ClientSpellCastHelper.handleClientboundFieryExplosion(readPosition(buffer), buffer.readFloat());
            case 40 -> PreviewProjection.handleClientEntityEvent(buffer.readInt(), buffer.readByte());
            case 41 -> {
                java.util.UUID target = buffer.readUUID();
                int count = buffer.readInt();
                java.util.List<Integer> projectiles = new java.util.ArrayList<>(count);
                for (int i = 0; i < count; i++) {
                    projectiles.add(buffer.readInt());
                }
                GuidingBoltManager.handleClientboundStartTracking(target, projectiles);
            }
            case 42 -> GuidingBoltManager.handleClientboundStopTracking(buffer.readUUID());
            case 43 -> {
                Vec3 position = readPosition(buffer);
                float radius = buffer.readFloat();
                ParticleType<?> particle = BuiltInRegistries.PARTICLE_TYPE.get(ResourceLocation.parse(buffer.readUtf()));
                if (particle != null) {
                    ClientSpellCastHelper.handleClientboundShockwaveParticle(position, radius, particle);
                }
            }
            case 22 -> throw new IllegalStateException("Blood siphon must use its Ponder-specific adapter");
            default -> throw new IllegalArgumentException("Unreviewed Iron preview packet " + index);
        }
    }

    private static Vec3 readPosition(FriendlyByteBuf buffer) {
        Vec3 serverPosition = new Vec3(buffer.readDouble(), buffer.readDouble(), buffer.readDouble());
        Vec3 origin = PreviewProjection.serverOrigin();
        return origin == null ? serverPosition : serverPosition.subtract(origin);
    }
}
