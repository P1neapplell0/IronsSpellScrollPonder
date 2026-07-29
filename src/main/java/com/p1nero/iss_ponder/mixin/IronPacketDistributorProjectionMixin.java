package com.p1nero.iss_ponder.mixin;

import com.p1nero.iss_ponder.server.PreviewSessionManager;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.neoforged.neoforge.common.util.FakePlayer;
import net.neoforged.neoforge.network.PacketDistributor;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Captures Iron's typed payloads before NeoForge sends them into the isolated dimension's dummy connections.
 * Reference: Iron's Spellbooks 3.16.2 sends particle payloads through NeoForge's static
 * {@code PacketDistributor} methods, including {@code sendToPlayersTrackingEntityAndSelf} for blood siphon.
 */
@Mixin(value = PacketDistributor.class, remap = false)
public abstract class IronPacketDistributorProjectionMixin {
    @Inject(method = "sendToPlayersTrackingEntity", at = @At("HEAD"))
    private static void issPonder$forwardTrackingVisual(Entity entity, CustomPacketPayload payload,
                                                         CustomPacketPayload[] additionalPayloads,
                                                         CallbackInfo callback) {
        forwardTracking(entity, payload, additionalPayloads);
    }

    @Inject(method = "sendToPlayersTrackingEntityAndSelf", at = @At("HEAD"))
    private static void issPonder$forwardTrackingAndSelfVisual(Entity entity, CustomPacketPayload payload,
                                                                CustomPacketPayload[] additionalPayloads,
                                                                CallbackInfo callback) {
        forwardTracking(entity, payload, additionalPayloads);
    }

    @Inject(method = "sendToPlayer", at = @At("HEAD"))
    private static void issPonder$forwardFakePlayerVisual(ServerPlayer player, CustomPacketPayload payload,
                                                           CustomPacketPayload[] additionalPayloads,
                                                           CallbackInfo callback) {
        if (!(player instanceof FakePlayer)) {
            return;
        }
        PreviewSessionManager.forwardPlayerVisualPacket(player, payload);
        for (CustomPacketPayload additionalPayload : additionalPayloads) {
            PreviewSessionManager.forwardPlayerVisualPacket(player, additionalPayload);
        }
    }

    private static void forwardTracking(Entity entity, CustomPacketPayload payload,
                                        CustomPacketPayload[] additionalPayloads) {
        PreviewSessionManager.forwardTrackingVisualPacket(entity, payload);
        for (CustomPacketPayload additionalPayload : additionalPayloads) {
            PreviewSessionManager.forwardTrackingVisualPacket(entity, additionalPayload);
        }
    }
}
