package com.p1nero.iss_ponder.mixin;

import com.p1nero.iss_ponder.server.PreviewSessionManager;
import io.redspace.ironsspellbooks.setup.PacketDistributor;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.network.ICustomPacket;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.simple.SimpleChannel;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Captures Iron's messages before Forge sends them into the isolated dimension's dummy connections.
 * Reference: Forge {@code PacketDistributor#playerConsumer} writes to
 * {@code player.connection.connection}, bypassing {@code ServerGamePacketListenerImpl#send(Packet)}.
 */
@Mixin(value = PacketDistributor.class, remap = false)
public abstract class IronPacketDistributorProjectionMixin {
    @Shadow
    private static SimpleChannel INSTANCE;

    @Inject(method = "sendToPlayersTrackingEntity", at = @At("HEAD"))
    private static <MSG> void issPonder$forwardTrackingVisual(Entity entity, MSG message, CallbackInfo callback) {
        PreviewSessionManager.forwardTrackingVisualPacket(entity, message);
    }

    @Inject(method = "sendToPlayer", at = @At("HEAD"))
    private static <MSG> void issPonder$forwardFakePlayerVisual(ServerPlayer player, MSG message,
                                                                CallbackInfo callback) {
        if (!(player instanceof FakePlayer) || PreviewSessionManager.isTrackingVisualPacket(message)) {
            return;
        }
        net.minecraft.network.protocol.Packet<?> encoded =
                INSTANCE.toVanillaPacket(message, NetworkDirection.PLAY_TO_CLIENT);
        if (encoded instanceof ICustomPacket<?> customPacket) {
            PreviewSessionManager.forwardCustomPacket(player, customPacket);
        }
    }
}
