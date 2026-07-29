package com.p1nero.iss_ponder.mixin;

import com.p1nero.iss_ponder.server.PreviewSessionManager;
import io.redspace.ironsspellbooks.api.spells.AbstractSpell;
import io.redspace.ironsspellbooks.api.spells.CastSource;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Captures every server cast, including continuous and instant casts.
 * Reference: Iron's {@code AbstractSpell#castSpell} sends {@code OnClientCastPacket} after this body.
 */
@Mixin(AbstractSpell.class)
public abstract class AbstractSpellProjectionMixin {
    @Inject(method = "castSpell", at = @At("RETURN"), remap = false)
    private void issPonder$forwardClientCast(Level level, int spellLevel, ServerPlayer caster,
                                             CastSource castSource, boolean triggerCooldown, CallbackInfo callback) {
        PreviewSessionManager.onSimulatedCast((AbstractSpell) (Object) this, level, spellLevel, caster, castSource);
    }
}
