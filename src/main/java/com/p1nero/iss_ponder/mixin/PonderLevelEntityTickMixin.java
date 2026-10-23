package com.p1nero.iss_ponder.mixin;

import com.p1nero.iss_ponder.client.PreviewProjection;
import com.p1nero.iss_ponder.client.ProjectionEntityIterator;
import net.createmod.ponder.api.level.PonderLevel;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.util.Iterator;
import java.util.List;

/**
 * PonderLevel#tick traverses a mutable ArrayList (Ponder 1.0.92/1.0.87).
 * Legendary Annihilation Beam removes helper entities during tick, invalidating it.
 * Keep upstream ticking/cleanup; change traversal only for our projected level.
 */
@Mixin(value = PonderLevel.class, remap = false)
public abstract class PonderLevelEntityTickMixin {
    @Redirect(method = "tick", at = @At(value = "INVOKE",
            target = "Ljava/util/List;iterator()Ljava/util/Iterator;", remap = false), require = 0)
    private Iterator<Entity> issPonder$stableProjectionTick(List<Entity> entities) {
        return PreviewProjection.isProjectionLevel((PonderLevel) (Object) this)
                ? new ProjectionEntityIterator(entities) : entities.iterator();
    }
}
