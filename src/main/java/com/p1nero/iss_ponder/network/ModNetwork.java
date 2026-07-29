package com.p1nero.iss_ponder.network;

import com.p1nero.iss_ponder.ISSPonderMod;
import com.p1nero.iss_ponder.server.PreviewSessionManager;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.network.simple.SimpleChannel;

import java.util.function.Supplier;
import java.util.UUID;

public final class ModNetwork {
    // Registration order is the wire discriminator table. Bump this protocol whenever order or encoding changes.
    private static final String PROTOCOL = "7";
    private static final SimpleChannel CHANNEL = NetworkRegistry.newSimpleChannel(
            ResourceLocation.fromNamespaceAndPath(ISSPonderMod.MOD_ID, "main"),
            () -> PROTOCOL,
            PROTOCOL::equals,
            PROTOCOL::equals
    );
    private static int packetId;

    private ModNetwork() {
    }

    public static void register() {
        CHANNEL.messageBuilder(StartPreview.class, packetId++).encoder(StartPreview::encode).decoder(StartPreview::decode)
                .consumerMainThread(StartPreview::handle).add();
        CHANNEL.messageBuilder(EndPreview.class, packetId++).encoder(EndPreview::encode).decoder(EndPreview::decode)
                .consumerMainThread(EndPreview::handle).add();
        CHANNEL.messageBuilder(ReplayPreview.class, packetId++).encoder(ReplayPreview::encode).decoder(ReplayPreview::decode)
                .consumerMainThread(ReplayPreview::handle).add();
        CHANNEL.messageBuilder(SwitchPreview.class, packetId++).encoder(SwitchPreview::encode).decoder(SwitchPreview::decode)
                .consumerMainThread(SwitchPreview::handle).add();
        CHANNEL.messageBuilder(PreviewReady.class, packetId++).encoder(PreviewReady::encode).decoder(PreviewReady::decode)
                .consumerMainThread(PreviewReady::handle).add();
        CHANNEL.messageBuilder(PreviewStatus.class, packetId++).encoder(PreviewStatus::encode).decoder(PreviewStatus::decode)
                .consumerMainThread(PreviewStatus::handle).add();
        CHANNEL.messageBuilder(ProjectionStart.class, packetId++).encoder(ProjectionStart::encode).decoder(ProjectionStart::decode)
                .consumerMainThread(ProjectionStart::handle).add();
        CHANNEL.messageBuilder(ProjectionEntity.class, packetId++).encoder(ProjectionEntity::encode).decoder(ProjectionEntity::decode)
                .consumerMainThread(ProjectionEntity::handle).add();
        CHANNEL.messageBuilder(ProjectionEntityEvent.class, packetId++).encoder(ProjectionEntityEvent::encode).decoder(ProjectionEntityEvent::decode)
                .consumerMainThread(ProjectionEntityEvent::handle).add();
        CHANNEL.messageBuilder(ProjectionEnd.class, packetId++).encoder(ProjectionEnd::encode).decoder(ProjectionEnd::decode)
                .consumerMainThread(ProjectionEnd::handle).add();
        CHANNEL.messageBuilder(ProjectionBlock.class, packetId++).encoder(ProjectionBlock::encode).decoder(ProjectionBlock::decode)
                .consumerMainThread(ProjectionBlock::handle).add();
        CHANNEL.messageBuilder(ProjectionParticle.class, packetId++).encoder(ProjectionParticle::encode).decoder(ProjectionParticle::decode)
                .consumerMainThread(ProjectionParticle::handle).add();
        CHANNEL.messageBuilder(ProjectionCastStarted.class, packetId++).encoder(ProjectionCastStarted::encode).decoder(ProjectionCastStarted::decode)
                .consumerMainThread(ProjectionCastStarted::handle).add();
        CHANNEL.messageBuilder(ProjectionCastProgress.class, packetId++).encoder(ProjectionCastProgress::encode).decoder(ProjectionCastProgress::decode)
                .consumerMainThread(ProjectionCastProgress::handle).add();
        CHANNEL.messageBuilder(ProjectionCastEffect.class, packetId++).encoder(ProjectionCastEffect::encode).decoder(ProjectionCastEffect::decode)
                .consumerMainThread(ProjectionCastEffect::handle).add();
        CHANNEL.messageBuilder(ProjectionCastFinished.class, packetId++).encoder(ProjectionCastFinished::encode).decoder(ProjectionCastFinished::decode)
                .consumerMainThread(ProjectionCastFinished::handle).add();
        CHANNEL.messageBuilder(ProjectionIronPacket.class, packetId++).encoder(ProjectionIronPacket::encode).decoder(ProjectionIronPacket::decode)
                .consumerMainThread(ProjectionIronPacket::handle).add();
    }

    public static void sendToServer(Object message) {
        CHANNEL.sendToServer(message);
    }

    public static void sendToPlayer(ServerPlayer player, Object message) {
        CHANNEL.send(PacketDistributor.PLAYER.with(() -> player), message);
    }

    public record StartPreview(ResourceLocation spellId, int spellLevel) {
        private static void encode(StartPreview message, FriendlyByteBuf buffer) {
            buffer.writeResourceLocation(message.spellId);
            buffer.writeVarInt(message.spellLevel);
        }

        private static StartPreview decode(FriendlyByteBuf buffer) {
            return new StartPreview(buffer.readResourceLocation(), buffer.readVarInt());
        }

        private static void handle(StartPreview message, Supplier<NetworkEvent.Context> contextSupplier) {
            ServerPlayer player = contextSupplier.get().getSender();
            if (player != null) {
                PreviewSessionManager.start(player, message.spellId, message.spellLevel);
            }
        }
    }

    public record EndPreview() {
        private static void encode(EndPreview message, FriendlyByteBuf buffer) {
        }

        private static EndPreview decode(FriendlyByteBuf buffer) {
            return new EndPreview();
        }

        private static void handle(EndPreview message, Supplier<NetworkEvent.Context> contextSupplier) {
            ServerPlayer player = contextSupplier.get().getSender();
            if (player != null) {
                PreviewSessionManager.end(player);
            }
        }
    }

    public record ReplayPreview() {
        private static void encode(ReplayPreview message, FriendlyByteBuf buffer) {
        }

        private static ReplayPreview decode(FriendlyByteBuf buffer) {
            return new ReplayPreview();
        }

        private static void handle(ReplayPreview message, Supplier<NetworkEvent.Context> contextSupplier) {
            ServerPlayer player = contextSupplier.get().getSender();
            if (player != null) {
                PreviewSessionManager.replay(player);
            }
        }
    }

    public record SwitchPreview(ResourceLocation spellId, int spellLevel) {
        private static void encode(SwitchPreview message, FriendlyByteBuf buffer) {
            buffer.writeResourceLocation(message.spellId);
            buffer.writeVarInt(message.spellLevel);
        }

        private static SwitchPreview decode(FriendlyByteBuf buffer) {
            return new SwitchPreview(buffer.readResourceLocation(), buffer.readVarInt());
        }

        private static void handle(SwitchPreview message, Supplier<NetworkEvent.Context> contextSupplier) {
            ServerPlayer player = contextSupplier.get().getSender();
            if (player != null) {
                PreviewSessionManager.switchSpell(player, message.spellId, message.spellLevel);
            }
        }
    }

    public record PreviewReady(ResourceLocation spellId, int spellLevel, boolean simulationAllowed,
                               double projectionX, double projectionY, double projectionZ) {
        public PreviewReady(ResourceLocation spellId, int spellLevel, boolean simulationAllowed) {
            this(spellId, spellLevel, simulationAllowed, 0, 0, 0);
        }

        private static void encode(PreviewReady message, FriendlyByteBuf buffer) {
            buffer.writeResourceLocation(message.spellId);
            buffer.writeVarInt(message.spellLevel);
            buffer.writeBoolean(message.simulationAllowed);
            buffer.writeDouble(message.projectionX);
            buffer.writeDouble(message.projectionY);
            buffer.writeDouble(message.projectionZ);
        }

        private static PreviewReady decode(FriendlyByteBuf buffer) {
            return new PreviewReady(buffer.readResourceLocation(), buffer.readVarInt(), buffer.readBoolean(),
                    buffer.readDouble(), buffer.readDouble(), buffer.readDouble());
        }

        private static void handle(PreviewReady message, Supplier<NetworkEvent.Context> contextSupplier) {
            DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () ->
                    com.p1nero.iss_ponder.client.ClientPreviewController.open(
                            message.spellId, message.spellLevel, message.simulationAllowed,
                            message.projectionX, message.projectionY, message.projectionZ));
        }
    }

    public record PreviewStatus(Component status) {
        private static void encode(PreviewStatus message, FriendlyByteBuf buffer) {
            buffer.writeComponent(message.status);
        }

        private static PreviewStatus decode(FriendlyByteBuf buffer) {
            return new PreviewStatus(buffer.readComponent());
        }

        private static void handle(PreviewStatus message, Supplier<NetworkEvent.Context> contextSupplier) {
            DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () ->
                    com.p1nero.iss_ponder.client.ClientPreviewController.setStatus(message.status));
        }
    }

    public record ProjectionStart(double x, double y, double z) {
        private static void encode(ProjectionStart message, FriendlyByteBuf buffer) {
            buffer.writeDouble(message.x);
            buffer.writeDouble(message.y);
            buffer.writeDouble(message.z);
        }

        private static ProjectionStart decode(FriendlyByteBuf buffer) {
            return new ProjectionStart(buffer.readDouble(), buffer.readDouble(), buffer.readDouble());
        }

        private static void handle(ProjectionStart message, Supplier<NetworkEvent.Context> contextSupplier) {
            DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> {
                try {
                    com.p1nero.iss_ponder.client.PreviewProjection.resetForReplay(message.x, message.y, message.z);
                } catch (RuntimeException exception) {
                    ISSPonderMod.LOGGER.error("Could not recreate the virtual spell preview", exception);
                    com.p1nero.iss_ponder.client.PreviewProjection.clear();
                    net.minecraft.client.Minecraft.getInstance().setScreen(null);
                }
            });
        }
    }

    public record ProjectionEntity(int id, boolean removed, UUID uuid, String typeId, String name,
                                   double x, double y, double z, float yaw, float pitch, float health,
                                   int hurtTime, boolean swinging, CompoundTag data, byte[] spawnData,
                                   java.util.List<SynchedEntityData.DataValue<?>> syncedData) {
        private static void encode(ProjectionEntity message, FriendlyByteBuf buffer) {
            buffer.writeVarInt(message.id);
            buffer.writeBoolean(message.removed);
            buffer.writeUUID(message.uuid);
            buffer.writeResourceLocation(ResourceLocation.parse(message.typeId));
            buffer.writeUtf(message.name == null ? "" : message.name, 64);
            buffer.writeDouble(message.x);
            buffer.writeDouble(message.y);
            buffer.writeDouble(message.z);
            buffer.writeFloat(message.yaw);
            buffer.writeFloat(message.pitch);
            buffer.writeFloat(message.health);
            buffer.writeVarInt(message.hurtTime);
            buffer.writeBoolean(message.swinging);
            buffer.writeNbt(message.data);
            buffer.writeByteArray(message.spawnData);
            message.syncedData.forEach(value -> value.write(buffer));
            buffer.writeByte(255);
        }

        private static ProjectionEntity decode(FriendlyByteBuf buffer) {
            int id = buffer.readVarInt();
            boolean removed = buffer.readBoolean();
            UUID uuid = buffer.readUUID();
            String typeId = buffer.readResourceLocation().toString();
            String name = buffer.readUtf(64);
            double x = buffer.readDouble();
            double y = buffer.readDouble();
            double z = buffer.readDouble();
            float yaw = buffer.readFloat();
            float pitch = buffer.readFloat();
            float health = buffer.readFloat();
            int hurtTime = buffer.readVarInt();
            boolean swinging = buffer.readBoolean();
            CompoundTag data = buffer.readNbt();
            byte[] spawnData = buffer.readByteArray(1 << 20);
            java.util.List<SynchedEntityData.DataValue<?>> syncedData = new java.util.ArrayList<>();
            int dataId;
            while ((dataId = buffer.readUnsignedByte()) != 255) {
                syncedData.add(SynchedEntityData.DataValue.read(buffer, dataId));
            }
            return new ProjectionEntity(id, removed, uuid, typeId, name,
                    x, y, z, yaw, pitch, health, hurtTime, swinging, data, spawnData, syncedData);
        }

        private static void handle(ProjectionEntity message, Supplier<NetworkEvent.Context> contextSupplier) {
            DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> {
                if (message.removed) {
                    com.p1nero.iss_ponder.client.ClientPreviewController.removeProjectionEntity(message.id);
                } else {
                    com.p1nero.iss_ponder.client.ClientPreviewController.spawnProjectionEntity(
                            message.id, message.uuid, message.typeId, message.name,
                            message.x, message.y, message.z, message.yaw, message.pitch, message.health,
                            message.hurtTime, message.swinging, message.data, message.spawnData, message.syncedData);
                }
            });
        }
    }

    /** Mirrors vanilla {@code ClientboundEntityEventPacket} for entities in the isolated Ponder level. */
    public record ProjectionEntityEvent(int id, byte eventId) {
        private static void encode(ProjectionEntityEvent message, FriendlyByteBuf buffer) {
            buffer.writeVarInt(message.id);
            buffer.writeByte(message.eventId);
        }

        private static ProjectionEntityEvent decode(FriendlyByteBuf buffer) {
            return new ProjectionEntityEvent(buffer.readVarInt(), buffer.readByte());
        }

        private static void handle(ProjectionEntityEvent message, Supplier<NetworkEvent.Context> contextSupplier) {
            DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () ->
                    com.p1nero.iss_ponder.client.PreviewProjection.handleVanillaEntityEvent(
                            message.id, message.eventId));
        }
    }

    public record ProjectionEnd() {
        private static void encode(ProjectionEnd message, FriendlyByteBuf buffer) {
        }

        private static ProjectionEnd decode(FriendlyByteBuf buffer) {
            return new ProjectionEnd();
        }

        private static void handle(ProjectionEnd message, Supplier<NetworkEvent.Context> contextSupplier) {
            DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () ->
                    com.p1nero.iss_ponder.client.PreviewProjection.clear());
        }
    }

    public record ProjectionBlock(long position, int stateId) {
        private static void encode(ProjectionBlock message, FriendlyByteBuf buffer) {
            buffer.writeLong(message.position);
            buffer.writeVarInt(message.stateId);
        }

        private static ProjectionBlock decode(FriendlyByteBuf buffer) {
            return new ProjectionBlock(buffer.readLong(), buffer.readVarInt());
        }

        private static void handle(ProjectionBlock message, Supplier<NetworkEvent.Context> contextSupplier) {
            DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () ->
                    com.p1nero.iss_ponder.client.PreviewProjection.updateBlock(
                            net.minecraft.core.BlockPos.of(message.position), message.stateId));
        }
    }

    public record ProjectionParticle(ParticleOptions particle, boolean longDistance, double x, double y, double z,
                                     int count, float xOffset, float yOffset, float zOffset, float speed) {
        private static void encode(ProjectionParticle message, FriendlyByteBuf buffer) {
            buffer.writeVarInt(BuiltInRegistries.PARTICLE_TYPE.getId(message.particle.getType()));
            message.particle.writeToNetwork(buffer);
            buffer.writeBoolean(message.longDistance);
            buffer.writeDouble(message.x);
            buffer.writeDouble(message.y);
            buffer.writeDouble(message.z);
            buffer.writeVarInt(message.count);
            buffer.writeFloat(message.xOffset);
            buffer.writeFloat(message.yOffset);
            buffer.writeFloat(message.zOffset);
            buffer.writeFloat(message.speed);
        }

        private static ProjectionParticle decode(FriendlyByteBuf buffer) {
            ParticleType<?> type = BuiltInRegistries.PARTICLE_TYPE.byId(buffer.readVarInt());
            if (type == null) {
                throw new IllegalArgumentException("Unknown projection particle type");
            }
            ParticleOptions particle = readParticle(type, buffer);
            return new ProjectionParticle(particle, buffer.readBoolean(), buffer.readDouble(), buffer.readDouble(),
                    buffer.readDouble(), buffer.readVarInt(), buffer.readFloat(), buffer.readFloat(), buffer.readFloat(),
                    buffer.readFloat());
        }

        @SuppressWarnings({"unchecked", "rawtypes"})
        private static ParticleOptions readParticle(ParticleType type, FriendlyByteBuf buffer) {
            return (ParticleOptions) type.getDeserializer().fromNetwork(type, buffer);
        }

        private static void handle(ProjectionParticle message, Supplier<NetworkEvent.Context> contextSupplier) {
            DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () ->
                    com.p1nero.iss_ponder.client.PreviewProjection.addParticle(message.particle, message.longDistance,
                            message.x, message.y, message.z, message.count, message.xOffset, message.yOffset,
                            message.zOffset, message.speed));
        }
    }

    public record ProjectionCastStarted(String spellId, int spellLevel) {
        private static void encode(ProjectionCastStarted message, FriendlyByteBuf buffer) {
            buffer.writeUtf(message.spellId, 256);
            buffer.writeVarInt(message.spellLevel);
        }

        private static ProjectionCastStarted decode(FriendlyByteBuf buffer) {
            return new ProjectionCastStarted(buffer.readUtf(256), buffer.readVarInt());
        }

        private static void handle(ProjectionCastStarted message, Supplier<NetworkEvent.Context> contextSupplier) {
            DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () ->
                    com.p1nero.iss_ponder.client.PreviewProjection.castStarted(message.spellId, message.spellLevel));
        }
    }

    public record ProjectionCastProgress(float progress) {
        private static void encode(ProjectionCastProgress message, FriendlyByteBuf buffer) {
            buffer.writeFloat(message.progress);
        }

        private static ProjectionCastProgress decode(FriendlyByteBuf buffer) {
            return new ProjectionCastProgress(buffer.readFloat());
        }

        private static void handle(ProjectionCastProgress message, Supplier<NetworkEvent.Context> contextSupplier) {
            DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () ->
                    com.p1nero.iss_ponder.client.PreviewProjection.updateCastProgress(message.progress));
        }
    }

    public record ProjectionCastEffect(String spellId, int spellLevel,
                                       io.redspace.ironsspellbooks.api.spells.CastSource castSource,
                                       io.redspace.ironsspellbooks.api.spells.ICastData castData) {
        private static void encode(ProjectionCastEffect message, FriendlyByteBuf buffer) {
            buffer.writeUtf(message.spellId, 256);
            buffer.writeVarInt(message.spellLevel);
            buffer.writeEnum(message.castSource);
            if (message.castData instanceof io.redspace.ironsspellbooks.api.spells.ICastDataSerializable serializable) {
                buffer.writeBoolean(true);
                serializable.writeToBuffer(buffer);
            } else {
                buffer.writeBoolean(false);
            }
        }

        private static ProjectionCastEffect decode(FriendlyByteBuf buffer) {
            String spellId = buffer.readUtf(256);
            int level = buffer.readVarInt();
            io.redspace.ironsspellbooks.api.spells.CastSource source = buffer.readEnum(io.redspace.ironsspellbooks.api.spells.CastSource.class);
            io.redspace.ironsspellbooks.api.spells.ICastData castData = null;
            if (buffer.readBoolean()) {
                io.redspace.ironsspellbooks.api.spells.ICastDataSerializable serializable =
                        io.redspace.ironsspellbooks.api.registry.SpellRegistry.getSpell(spellId).getEmptyCastData();
                serializable.readFromBuffer(buffer);
                castData = serializable;
            }
            return new ProjectionCastEffect(spellId, level, source, castData);
        }

        private static void handle(ProjectionCastEffect message, Supplier<NetworkEvent.Context> contextSupplier) {
            DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () ->
                    com.p1nero.iss_ponder.client.PreviewProjection.castEffect(message.spellId, message.spellLevel, message.castData));
        }
    }

    public record ProjectionCastFinished(String spellId, boolean cancelled) {
        private static void encode(ProjectionCastFinished message, FriendlyByteBuf buffer) {
            buffer.writeUtf(message.spellId, 256);
            buffer.writeBoolean(message.cancelled);
        }

        private static ProjectionCastFinished decode(FriendlyByteBuf buffer) {
            return new ProjectionCastFinished(buffer.readUtf(256), buffer.readBoolean());
        }

        private static void handle(ProjectionCastFinished message, Supplier<NetworkEvent.Context> contextSupplier) {
            DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () ->
                    com.p1nero.iss_ponder.client.PreviewProjection.castFinished(message.spellId, message.cancelled));
        }
    }

    /** Raw Iron's Spellbooks packet payload captured from the server-only FakePlayer. */
    public record ProjectionIronPacket(int index, byte[] payload) {
        private static void encode(ProjectionIronPacket message, FriendlyByteBuf buffer) {
            buffer.writeVarInt(message.index);
            buffer.writeByteArray(message.payload);
        }

        private static ProjectionIronPacket decode(FriendlyByteBuf buffer) {
            return new ProjectionIronPacket(buffer.readVarInt(), buffer.readByteArray(1 << 20));
        }

        private static void handle(ProjectionIronPacket message, Supplier<NetworkEvent.Context> contextSupplier) {
            DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () ->
                    com.p1nero.iss_ponder.client.PreviewPacketBridge.handle(message.index, message.payload));
        }
    }
}
