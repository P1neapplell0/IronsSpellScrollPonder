package com.p1nero.iss_ponder.compat.cataclysm.legacy;

import com.p1nero.iss_ponder.ISSPonderMod;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.phys.Vec3;

import java.lang.reflect.Constructor;

public final class CataclysmLegacyBridge {
    private static Constructor<?> ringConstructor;
    private static boolean ringUnavailable;

    private CataclysmLegacyBridge() {
    }

    public static Vec3 vector(double x, double y, double z) {
        return new Vec3(x, y, z);
    }

    public static ParticleOptions ring(float yaw, float pitch, int duration, float red, float green, float blue,
                                       float alpha, float scale, boolean facesCamera, Object behavior) {
        if (ringUnavailable) {
            return ParticleTypes.END_ROD;
        }
        try {
            if (ringConstructor == null) {
                ringConstructor = Class.forName("com.github.L_Ender.cataclysm.client.particle.Options.RingParticleOptions")
                        .getConstructor(float.class, float.class, int.class, int.class, int.class, int.class,
                                float.class, float.class, boolean.class, int.class);
            }
            int ordinal = behavior instanceof Enum<?> value ? value.ordinal() : 1;
            return (ParticleOptions) ringConstructor.newInstance(yaw, pitch, duration, color(red), color(green),
                    color(blue), alpha, scale, facesCamera, ordinal);
        } catch (ReflectiveOperationException | RuntimeException | LinkageError exception) {
            ringUnavailable = true;
            ISSPonderMod.LOGGER.warn("Disabled experimental Cataclysm legacy ring factory", exception);
            return ParticleTypes.END_ROD;
        }
    }

    public static void sleep(Object remnant, boolean sleeping) {
        try {
            remnant.getClass().getMethod("setNecklace", boolean.class).invoke(remnant, !sleeping);
            remnant.getClass().getMethod("setAttackState", int.class).invoke(remnant, sleeping ? 1 : 0);
        } catch (ReflectiveOperationException | RuntimeException | LinkageError exception) {
            ISSPonderMod.LOGGER.warn("Could not update experimental Cataclysm remnant sleep state", exception);
        }
    }

    private static int color(float value) {
        return Math.max(0, Math.min(255, Math.round(value * 255.0F)));
    }
}
