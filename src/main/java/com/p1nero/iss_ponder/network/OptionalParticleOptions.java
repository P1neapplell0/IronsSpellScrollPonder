package com.p1nero.iss_ponder.network;

import com.p1nero.iss_ponder.ISSPonderMod;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public final class OptionalParticleOptions {
    private static final String SOULFIRE = "io.redspace.ironsspellbooks.particle.SoulfireRayParticleOptions";
    private static final String BUBBLE = "io.redspace.ironsspellbooks.particle.TintedBubblePopParticleOptions";
    private static final Set<Class<?>> FAILED = ConcurrentHashMap.newKeySet();

    private OptionalParticleOptions() {
    }

    @Nullable
    public static Vec3 destination(ParticleOptions particle) {
        if (!particle.getClass().getName().equals(SOULFIRE) || FAILED.contains(particle.getClass())) {
            return null;
        }
        try {
            return (Vec3) particle.getClass().getMethod("getDestination").invoke(particle);
        } catch (ReflectiveOperationException | RuntimeException | LinkageError exception) {
            failed(particle, exception);
            return null;
        }
    }

    public static ParticleOptions project(ParticleOptions particle, Vec3 origin) {
        if (FAILED.contains(particle.getClass())) {
            return particle;
        }
        try {
            if (particle.getClass().getName().equals(SOULFIRE)) {
                Vec3 destination = destination(particle);
                if (destination != null) {
                    return (ParticleOptions) particle.getClass().getConstructor(Vec3.class)
                            .newInstance(destination.subtract(origin));
                }
            } else if (particle.getClass().getName().equals(BUBBLE)) {
                BlockPos position = (BlockPos) particle.getClass().getMethod("cauldronPos").invoke(particle);
                return (ParticleOptions) particle.getClass().getConstructor(BlockPos.class)
                        .newInstance(position.subtract(BlockPos.containing(origin)));
            }
        } catch (ReflectiveOperationException | RuntimeException | LinkageError exception) {
            failed(particle, exception);
        }
        return particle;
    }

    private static void failed(ParticleOptions particle, Throwable exception) {
        if (FAILED.add(particle.getClass())) {
            ISSPonderMod.LOGGER.warn("Skipping unsupported embedded particle endpoints for {}",
                    particle.getClass().getName(), exception);
        }
    }
}
