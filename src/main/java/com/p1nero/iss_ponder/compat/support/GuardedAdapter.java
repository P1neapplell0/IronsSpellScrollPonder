package com.p1nero.iss_ponder.compat.support;

import com.p1nero.iss_ponder.ISSPonderMod;
import com.p1nero.iss_ponder.api.SpellPreviewAdapter;
import com.p1nero.iss_ponder.api.SpellPreviewContext;
import com.p1nero.iss_ponder.config.CompatConfig;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.block.state.BlockState;

import java.util.Map;
import java.util.function.Supplier;

public final class GuardedAdapter implements SpellPreviewAdapter {
    private final ResourceLocation spellId;
    private final SpellPreviewAdapter delegate;
    private boolean failed;

    public GuardedAdapter(ResourceLocation spellId, SpellPreviewAdapter delegate) {
        this.spellId = spellId;
        this.delegate = delegate;
    }

    private <T> T invoke(Supplier<T> operation, T fallback) {
        if (failed || !CompatConfig.enabled(spellId.getNamespace())) {
            return fallback;
        }
        try {
            return operation.get();
        } catch (RuntimeException | LinkageError exception) {
            failed = true;
            ISSPonderMod.LOGGER.warn("Stopped experimental adapter for {} in this preview", spellId, exception);
            return fallback;
        }
    }

    private void invoke(Runnable operation) {
        invoke(() -> {
            operation.run();
            return true;
        }, false);
    }

    @Override
    public boolean allowsSimulation() {
        return invoke(delegate::allowsSimulation, false);
    }

    @Override
    public io.redspace.ironsspellbooks.api.spells.CastSource castSource() {
        return invoke(delegate::castSource, io.redspace.ironsspellbooks.api.spells.CastSource.COMMAND);
    }

    @Override
    public EntityType<? extends LivingEntity> primaryTargetType() {
        return invoke(delegate::primaryTargetType, EntityType.ZOMBIE);
    }

    @Override
    public Map<BlockPos, BlockState> initialBlocks(SpellPreviewContext context) {
        return invoke(() -> delegate.initialBlocks(context), Map.of());
    }

    @Override
    public void onSceneReady(SpellPreviewContext context) {
        invoke(() -> delegate.onSceneReady(context));
    }

    @Override
    public void onCastCompleted(SpellPreviewContext context) {
        invoke(() -> delegate.onCastCompleted(context));
    }

    @Override
    public void beforeCast(SpellPreviewContext context) {
        invoke(() -> delegate.beforeCast(context));
        // Scene/target preparation may already have failed. Do not cast with missing prerequisites.
        // The normal session manager catches this controlled exception and ends the preview.
        if (failed) {
            throw new IllegalStateException("Experimental preparation failed for " + spellId);
        }
    }

    @Override
    public Action tickAfterCast(SpellPreviewContext context) {
        return invoke(() -> delegate.tickAfterCast(context), Action.COMPLETE);
    }

    @Override
    public void beforeRecast(SpellPreviewContext context) {
        invoke(() -> delegate.beforeRecast(context));
    }

    @Override
    public void onSessionClosed(SpellPreviewContext context) {
        invoke(() -> delegate.onSessionClosed(context));
    }
}
