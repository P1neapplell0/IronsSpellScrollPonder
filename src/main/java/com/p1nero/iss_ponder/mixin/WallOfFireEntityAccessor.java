package com.p1nero.iss_ponder.mixin;

import io.redspace.ironsspellbooks.entity.spells.wall_of_fire.WallOfFireEntity;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.List;

/** Accesses the absolute anchor list serialized by Iron's {@code WallOfFireEntity#writeSpawnData}. */
@Mixin(value = WallOfFireEntity.class, remap = false)
public interface WallOfFireEntityAccessor {
    @Accessor("anchorPoints")
    List<Vec3> issPonder$getAnchorPoints();

    @Accessor("anchorPoints")
    void issPonder$setAnchorPoints(List<Vec3> anchorPoints);
}
