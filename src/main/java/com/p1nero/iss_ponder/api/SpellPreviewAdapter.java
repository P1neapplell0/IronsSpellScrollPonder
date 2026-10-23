package com.p1nero.iss_ponder.api;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.block.state.BlockState;

import java.util.Map;

/**
 * Extends the generic preview lifecycle for spells that require gameplay after their initial cast.
 * Implementations are server-side and must leave all persistent state inside the preview session.
 */
public interface SpellPreviewAdapter {
    /** Most previews use COMMAND; exclusive spellbook spells can request their normal validation path. */
    default io.redspace.ironsspellbooks.api.spells.CastSource castSource() {
        return io.redspace.ironsspellbooks.api.spells.CastSource.COMMAND;
    }

    enum Action {
        WAIT,
        RECAST,
        COMPLETE
    }

    default boolean allowsSimulation() {
        return true;
    }

    /** Selects the main entity placed directly in the caster's line of sight. */
    default EntityType<? extends LivingEntity> primaryTargetType() {
        return EntityType.ZOMBIE;
    }

    /**
     * Supplies origin-relative blocks needed before casting. The preview owns, projects, and removes these blocks.
     */
    default Map<BlockPos, BlockState> initialBlocks(SpellPreviewContext context) {
        return Map.of();
    }

    default void onSceneReady(SpellPreviewContext context) {
    }

    /** Runs before normal cast validation/initiation, including recasts; prepare predicate inputs here. */
    default void beforeCast(SpellPreviewContext context) {
    }

    default void onCastCompleted(SpellPreviewContext context) {
    }

    default Action tickAfterCast(SpellPreviewContext context) {
        return Action.COMPLETE;
    }

    default void beforeRecast(SpellPreviewContext context) {
    }

    default void onSessionClosed(SpellPreviewContext context) {
    }
}
