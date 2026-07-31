package com.p1nero.iss_ponder.network;

import com.p1nero.iss_ponder.ISSPonderMod;
import com.p1nero.iss_ponder.server.PreviewSessionManager;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.codec.StreamDecoder;
import net.minecraft.network.codec.StreamMemberEncoder;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.neoforged.neoforge.network.handling.IPayloadHandler;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

import java.util.Map;
import java.util.UUID;

public final class ModNetwork {
    // Bump this version whenever an existing payload's binary encoding changes.
    private static final String PROTOCOL = "8";
    private static final Map<Class<?>, CustomPacketPayload.Type<?>> PAYLOAD_TYPES = Map.ofEntries(
            payloadType(StartPreview.class, "start_preview"),
            payloadType(EndPreview.class, "end_preview"),
            payloadType(ReplayPreview.class, "replay_preview"),
            payloadType(SwitchPreview.class, "switch_preview"),
            payloadType(PreviewReady.class, "preview_ready"),
            payloadType(PreviewStatus.class, "preview_status"),
            payloadType(ProjectionStart.class, "projection_start"),
            payloadType(ProjectionEntity.class, "projection_entity"),
            payloadType(ProjectionEntityEvent.class, "projection_entity_event"),
            payloadType(ProjectionEnd.class, "projection_end"),
            payloadType(ProjectionBlock.class, "projection_block"),
            payloadType(ProjectionParticle.class, "projection_particle"),
            payloadType(ProjectionCastStarted.class, "projection_cast_started"),
            payloadType(ProjectionCastProgress.class, "projection_cast_progress"),
            payloadType(ProjectionCastEffect.class, "projection_cast_effect"),
            payloadType(ProjectionCastFinished.class, "projection_cast_finished"),
            payloadType(ProjectionIronPacket.class, "projection_iron_packet")
    );

    private ModNetwork() {
    }

    public static void register(RegisterPayloadHandlersEvent event) {
        PayloadRegistrar registrar = event.registrar(PROTOCOL);
        playToServer(registrar, StartPreview.class, StartPreview::encode, StartPreview::decode, StartPreview::handle);
        playToServer(registrar, EndPreview.class, EndPreview::encode, EndPreview::decode, EndPreview::handle);
        playToServer(registrar, ReplayPreview.class, ReplayPreview::encode, ReplayPreview::decode, ReplayPreview::handle);
        playToServer(registrar, SwitchPreview.class, SwitchPreview::encode, SwitchPreview::decode, SwitchPreview::handle);
        playToClient(registrar, PreviewReady.class, PreviewReady::encode, PreviewReady::decode, PreviewReady::handle);
        playToClient(registrar, PreviewStatus.class, PreviewStatus::encode, PreviewStatus::decode, PreviewStatus::handle);
        playToClient(registrar, ProjectionStart.class, ProjectionStart::encode, ProjectionStart::decode, ProjectionStart::handle);
        playToClient(registrar, ProjectionEntity.class, ProjectionEntity::encode, ProjectionEntity::decode, ProjectionEntity::handle);
        playToClient(registrar, ProjectionEntityEvent.class, ProjectionEntityEvent::encode, ProjectionEntityEvent::decode, ProjectionEntityEvent::handle);
        playToClient(registrar, ProjectionEnd.class, ProjectionEnd::encode, ProjectionEnd::decode, ProjectionEnd::handle);
        playToClient(registrar, ProjectionBlock.class, ProjectionBlock::encode, ProjectionBlock::decode, ProjectionBlock::handle);
        playToClient(registrar, ProjectionParticle.class, ProjectionParticle::encode, ProjectionParticle::decode, ProjectionParticle::handle);
        playToClient(registrar, ProjectionCastStarted.class, ProjectionCastStarted::encode, ProjectionCastStarted::decode, ProjectionCastStarted::handle);
        playToClient(registrar, ProjectionCastProgress.class, ProjectionCastProgress::encode, ProjectionCastProgress::decode, ProjectionCastProgress::handle);
        playToClient(registrar, ProjectionCastEffect.class, ProjectionCastEffect::encode, ProjectionCastEffect::decode, ProjectionCastEffect::handle);
        playToClient(registrar, ProjectionCastFinished.class, ProjectionCastFinished::encode, ProjectionCastFinished::decode, ProjectionCastFinished::handle);
        playToClient(registrar, ProjectionIronPacket.class, ProjectionIronPacket::encode, ProjectionIronPacket::decode, ProjectionIronPacket::handle);
    }

    public static void sendToServer(CustomPacketPayload message) {
        PacketDistributor.sendToServer(message);
    }

    public static void sendToPlayer(ServerPlayer player, CustomPacketPayload message) {
        PacketDistributor.sendToPlayer(player, message);
    }

    private static <T extends Payload> Map.Entry<Class<?>, CustomPacketPayload.Type<?>> payloadType(
            Class<T> payloadClass, String path) {
        return Map.entry(payloadClass, new CustomPacketPayload.Type<>(
                ResourceLocation.fromNamespaceAndPath(ISSPonderMod.MOD_ID, path)));
    }

    @SuppressWarnings("unchecked")
    private static <T extends Payload> CustomPacketPayload.Type<T> typeFor(Class<T> payloadClass) {
        return (CustomPacketPayload.Type<T>) PAYLOAD_TYPES.get(payloadClass);
    }

    private static <T extends Payload> StreamCodec<RegistryFriendlyByteBuf, T> codec(
            StreamMemberEncoder<RegistryFriendlyByteBuf, T> encoder,
            StreamDecoder<RegistryFriendlyByteBuf, T> decoder) {
        return CustomPacketPayload.codec(encoder, decoder);
    }

    private static <T extends Payload> void playToServer(PayloadRegistrar registrar, Class<T> payloadClass,
                                                         StreamMemberEncoder<RegistryFriendlyByteBuf, T> encoder,
                                                         StreamDecoder<RegistryFriendlyByteBuf, T> decoder,
                                                         IPayloadHandler<T> handler) {
        registrar.playToServer(typeFor(payloadClass), codec(encoder, decoder), handler);
    }

    private static <T extends Payload> void playToClient(PayloadRegistrar registrar, Class<T> payloadClass,
                                                         StreamMemberEncoder<RegistryFriendlyByteBuf, T> encoder,
                                                         StreamDecoder<RegistryFriendlyByteBuf, T> decoder,
                                                         IPayloadHandler<T> handler) {
        registrar.playToClient(typeFor(payloadClass), codec(encoder, decoder), handler);
    }

    private interface Payload extends CustomPacketPayload {
        @Override
        @SuppressWarnings({"rawtypes", "unchecked"})
        default CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
            return typeFor((Class) getClass());
        }
    }

    public record StartPreview(ResourceLocation spellId, int spellLevel) implements Payload {
        private static void encode(StartPreview message, FriendlyByteBuf buffer) {
            buffer.writeResourceLocation(message.spellId);
            buffer.writeVarInt(message.spellLevel);
        }

        private static StartPreview decode(FriendlyByteBuf buffer) {
            return new StartPreview(buffer.readResourceLocation(), buffer.readVarInt());
        }

        private static void handle(StartPreview message, IPayloadContext context) {
            PreviewSessionManager.start((ServerPlayer) context.player(), message.spellId, message.spellLevel);
        }
    }

    public record EndPreview() implements Payload {
        private static void encode(EndPreview message, FriendlyByteBuf buffer) {
        }

        private static EndPreview decode(FriendlyByteBuf buffer) {
            return new EndPreview();
        }

        private static void handle(EndPreview message, IPayloadContext context) {
            PreviewSessionManager.end((ServerPlayer) context.player());
        }
    }

    public record ReplayPreview() implements Payload {
        private static void encode(ReplayPreview message, FriendlyByteBuf buffer) {
        }

        private static ReplayPreview decode(FriendlyByteBuf buffer) {
            return new ReplayPreview();
        }

        private static void handle(ReplayPreview message, IPayloadContext context) {
            PreviewSessionManager.replay((ServerPlayer) context.player());
        }
    }

    public record SwitchPreview(ResourceLocation spellId, int spellLevel) implements Payload {
        private static void encode(SwitchPreview message, FriendlyByteBuf buffer) {
            buffer.writeResourceLocation(message.spellId);
            buffer.writeVarInt(message.spellLevel);
        }

        private static SwitchPreview decode(FriendlyByteBuf buffer) {
            return new SwitchPreview(buffer.readResourceLocation(), buffer.readVarInt());
        }

        private static void handle(SwitchPreview message, IPayloadContext context) {
            PreviewSessionManager.switchSpell((ServerPlayer) context.player(), message.spellId, message.spellLevel);
        }
    }

    public record PreviewReady(ResourceLocation spellId, int spellLevel, boolean simulationAllowed,
                               double projectionX, double projectionY, double projectionZ) implements Payload {
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

        private static void handle(PreviewReady message, IPayloadContext context) {
            com.p1nero.iss_ponder.client.ClientPreviewController.open(
                    message.spellId, message.spellLevel, message.simulationAllowed,
                    message.projectionX, message.projectionY, message.projectionZ);
        }
    }

    public record PreviewStatus(Component status, boolean previewComplete) implements Payload {
        public PreviewStatus(Component status) {
            this(status, false);
        }

        private static void encode(PreviewStatus message, RegistryFriendlyByteBuf buffer) {
            ComponentSerialization.STREAM_CODEC.encode(buffer, message.status);
            buffer.writeBoolean(message.previewComplete);
        }

        private static PreviewStatus decode(RegistryFriendlyByteBuf buffer) {
            return new PreviewStatus(ComponentSerialization.STREAM_CODEC.decode(buffer), buffer.readBoolean());
        }

        private static void handle(PreviewStatus message, IPayloadContext context) {
            com.p1nero.iss_ponder.client.ClientPreviewController.setStatus(message.status);
            if (message.previewComplete) {
                com.p1nero.iss_ponder.client.ClientPreviewController.previewComplete();
            }
        }
    }

    public record ProjectionStart(double x, double y, double z) implements Payload {
        private static void encode(ProjectionStart message, FriendlyByteBuf buffer) {
            buffer.writeDouble(message.x);
            buffer.writeDouble(message.y);
            buffer.writeDouble(message.z);
        }

        private static ProjectionStart decode(FriendlyByteBuf buffer) {
            return new ProjectionStart(buffer.readDouble(), buffer.readDouble(), buffer.readDouble());
        }

        private static void handle(ProjectionStart message, IPayloadContext context) {
            try {
                com.p1nero.iss_ponder.client.ClientPreviewController.replayStarted();
                com.p1nero.iss_ponder.client.PreviewProjection.resetForReplay(message.x, message.y, message.z);
            } catch (RuntimeException exception) {
                ISSPonderMod.LOGGER.error("Could not recreate the virtual spell preview", exception);
                com.p1nero.iss_ponder.client.PreviewProjection.clear();
                net.minecraft.client.Minecraft.getInstance().setScreen(null);
            }
        }
    }

    public record ProjectionEntity(int id, boolean removed, UUID uuid, String typeId, String name,
                                   double x, double y, double z, float yaw, float pitch, float health,
                                   int hurtTime, boolean swinging, CompoundTag data, byte[] spawnData,
                                   java.util.List<SynchedEntityData.DataValue<?>> syncedData) implements Payload {
        private static void encode(ProjectionEntity message, RegistryFriendlyByteBuf buffer) {
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

        private static ProjectionEntity decode(RegistryFriendlyByteBuf buffer) {
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

        private static void handle(ProjectionEntity message, IPayloadContext context) {
            if (message.removed) {
                com.p1nero.iss_ponder.client.ClientPreviewController.removeProjectionEntity(message.id);
            } else {
                com.p1nero.iss_ponder.client.ClientPreviewController.spawnProjectionEntity(
                        message.id, message.uuid, message.typeId, message.name,
                        message.x, message.y, message.z, message.yaw, message.pitch, message.health,
                        message.hurtTime, message.swinging, message.data, message.spawnData, message.syncedData);
            }
        }
    }

    /** Mirrors vanilla {@code ClientboundEntityEventPacket} for entities in the isolated Ponder level. */
    public record ProjectionEntityEvent(int id, byte eventId) implements Payload {
        private static void encode(ProjectionEntityEvent message, FriendlyByteBuf buffer) {
            buffer.writeVarInt(message.id);
            buffer.writeByte(message.eventId);
        }

        private static ProjectionEntityEvent decode(FriendlyByteBuf buffer) {
            return new ProjectionEntityEvent(buffer.readVarInt(), buffer.readByte());
        }

        private static void handle(ProjectionEntityEvent message, IPayloadContext context) {
            com.p1nero.iss_ponder.client.PreviewProjection.handleVanillaEntityEvent(message.id, message.eventId);
        }
    }

    public record ProjectionEnd() implements Payload {
        private static void encode(ProjectionEnd message, FriendlyByteBuf buffer) {
        }

        private static ProjectionEnd decode(FriendlyByteBuf buffer) {
            return new ProjectionEnd();
        }

        private static void handle(ProjectionEnd message, IPayloadContext context) {
            com.p1nero.iss_ponder.client.PreviewProjection.clear();
        }
    }

    public record ProjectionBlock(long position, int stateId) implements Payload {
        private static void encode(ProjectionBlock message, FriendlyByteBuf buffer) {
            buffer.writeLong(message.position);
            buffer.writeVarInt(message.stateId);
        }

        private static ProjectionBlock decode(FriendlyByteBuf buffer) {
            return new ProjectionBlock(buffer.readLong(), buffer.readVarInt());
        }

        private static void handle(ProjectionBlock message, IPayloadContext context) {
            com.p1nero.iss_ponder.client.PreviewProjection.updateBlock(
                    net.minecraft.core.BlockPos.of(message.position), message.stateId);
        }
    }

    public record ProjectionParticle(ParticleOptions particle, boolean longDistance, double x, double y, double z,
                                     int count, float xOffset, float yOffset, float zOffset, float speed) implements Payload {
        private static void encode(ProjectionParticle message, RegistryFriendlyByteBuf buffer) {
            ParticleTypes.STREAM_CODEC.encode(buffer, message.particle);
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

        private static ProjectionParticle decode(RegistryFriendlyByteBuf buffer) {
            ParticleOptions particle = ParticleTypes.STREAM_CODEC.decode(buffer);
            return new ProjectionParticle(particle, buffer.readBoolean(), buffer.readDouble(), buffer.readDouble(),
                    buffer.readDouble(), buffer.readVarInt(), buffer.readFloat(), buffer.readFloat(), buffer.readFloat(),
                    buffer.readFloat());
        }

        private static void handle(ProjectionParticle message, IPayloadContext context) {
            com.p1nero.iss_ponder.client.PreviewProjection.addParticle(message.particle, message.longDistance,
                    message.x, message.y, message.z, message.count, message.xOffset, message.yOffset,
                    message.zOffset, message.speed);
        }
    }

    public record ProjectionCastStarted(String spellId, int spellLevel) implements Payload {
        private static void encode(ProjectionCastStarted message, FriendlyByteBuf buffer) {
            buffer.writeUtf(message.spellId, 256);
            buffer.writeVarInt(message.spellLevel);
        }

        private static ProjectionCastStarted decode(FriendlyByteBuf buffer) {
            return new ProjectionCastStarted(buffer.readUtf(256), buffer.readVarInt());
        }

        private static void handle(ProjectionCastStarted message, IPayloadContext context) {
            com.p1nero.iss_ponder.client.PreviewProjection.castStarted(message.spellId, message.spellLevel);
        }
    }

    public record ProjectionCastProgress(float progress) implements Payload {
        private static void encode(ProjectionCastProgress message, FriendlyByteBuf buffer) {
            buffer.writeFloat(message.progress);
        }

        private static ProjectionCastProgress decode(FriendlyByteBuf buffer) {
            return new ProjectionCastProgress(buffer.readFloat());
        }

        private static void handle(ProjectionCastProgress message, IPayloadContext context) {
            com.p1nero.iss_ponder.client.PreviewProjection.updateCastProgress(message.progress);
        }
    }

    public record ProjectionCastEffect(String spellId, int spellLevel,
                                       io.redspace.ironsspellbooks.api.spells.CastSource castSource,
                                       io.redspace.ironsspellbooks.api.spells.ICastData castData) implements Payload {
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

        private static void handle(ProjectionCastEffect message, IPayloadContext context) {
            com.p1nero.iss_ponder.client.PreviewProjection.castEffect(
                    message.spellId, message.spellLevel, message.castData);
        }
    }

    public record ProjectionCastFinished(String spellId, boolean cancelled) implements Payload {
        private static void encode(ProjectionCastFinished message, FriendlyByteBuf buffer) {
            buffer.writeUtf(message.spellId, 256);
            buffer.writeBoolean(message.cancelled);
        }

        private static ProjectionCastFinished decode(FriendlyByteBuf buffer) {
            return new ProjectionCastFinished(buffer.readUtf(256), buffer.readBoolean());
        }

        private static void handle(ProjectionCastFinished message, IPayloadContext context) {
            com.p1nero.iss_ponder.client.PreviewProjection.castFinished(message.spellId, message.cancelled);
        }
    }

    /** Raw Iron's Spellbooks packet payload captured from the server-only FakePlayer. */
    public record ProjectionIronPacket(int index, byte[] payload) implements Payload {
        private static void encode(ProjectionIronPacket message, FriendlyByteBuf buffer) {
            buffer.writeVarInt(message.index);
            buffer.writeByteArray(message.payload);
        }

        private static ProjectionIronPacket decode(FriendlyByteBuf buffer) {
            return new ProjectionIronPacket(buffer.readVarInt(), buffer.readByteArray(1 << 20));
        }

        private static void handle(ProjectionIronPacket message, IPayloadContext context) {
            com.p1nero.iss_ponder.client.PreviewPacketBridge.handle(message.index, message.payload);
        }
    }
}
