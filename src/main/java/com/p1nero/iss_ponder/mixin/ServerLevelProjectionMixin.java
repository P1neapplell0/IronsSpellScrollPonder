package com.p1nero.iss_ponder.mixin;

import com.p1nero.iss_ponder.server.PreviewSessionManager;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Mirrors the ServerLevel side effects that a client-only PonderLevel cannot observe by itself.
 * The overload descriptors below match Minecraft 1.21.1's Mojmap {@code ServerLevel} source.
 */
@Mixin(ServerLevel.class)
public abstract class ServerLevelProjectionMixin {
    @Inject(method = "sendParticles(Lnet/minecraft/core/particles/ParticleOptions;DDDIDDDD)I", at = @At("HEAD"))
    private <T extends ParticleOptions> void issPonder$forwardParticles(T particle, double x, double y, double z,
                                                                       int count, double xOffset, double yOffset,
                                                                       double zOffset, double speed,
                                                                       CallbackInfoReturnable<Integer> callback) {
        PreviewSessionManager.forwardParticles((ServerLevel) (Object) this, particle, false,
                x, y, z, count, xOffset, yOffset, zOffset, speed);
    }

    @Inject(method = "sendParticles(Lnet/minecraft/server/level/ServerPlayer;Lnet/minecraft/core/particles/ParticleOptions;ZDDDIDDDD)Z", at = @At("HEAD"))
    private <T extends ParticleOptions> void issPonder$forwardTargetedParticles(ServerPlayer ignored, T particle,
                                                                                boolean longDistance,
                                                                                double x, double y, double z,
                                                                                int count, double xOffset,
                                                                                double yOffset, double zOffset,
                                                                                double speed,
                                                                                CallbackInfoReturnable<Boolean> callback) {
        PreviewSessionManager.forwardParticles((ServerLevel) (Object) this, particle, longDistance,
                x, y, z, count, xOffset, yOffset, zOffset, speed);
    }

    @Inject(method = "sendBlockUpdated", at = @At("HEAD"))
    private void issPonder$forwardBlock(BlockPos position, BlockState oldState, BlockState newState, int flags,
                                        CallbackInfo callback) {
        PreviewSessionManager.forwardBlock((ServerLevel) (Object) this, position, newState);
    }

    @Inject(method = "broadcastEntityEvent", at = @At("HEAD"))
    private void issPonder$forwardEntityEvent(Entity entity, byte eventId, CallbackInfo callback) {
        // Reference: ExtendedEvokerFang#tick broadcasts event 4 when its warmup reaches zero; the projected
        // vanilla EvokerFangs#handleEntityEvent consumes it to start the client attack animation and sound.
        PreviewSessionManager.forwardEntityEvent((ServerLevel) (Object) this, entity, eventId);
    }
}
