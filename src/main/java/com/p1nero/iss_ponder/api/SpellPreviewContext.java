package com.p1nero.iss_ponder.api;

import io.redspace.ironsspellbooks.api.magic.MagicData;
import io.redspace.ironsspellbooks.api.spells.AbstractSpell;
import net.minecraft.core.Holder;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

/** Immutable view of one server-side preview tick, with bounded helpers for common follow-up actions. */
public record SpellPreviewContext(ServerLevel level, ServerPlayer caster, @Nullable LivingEntity primaryTarget,
                                  AbstractSpell spell, int spellLevel, int completedCasts,
                                  int ticksSinceLastCast) {
    public boolean hasRemainingRecasts() {
        return MagicData.getPlayerMagicData(caster).getPlayerRecasts().hasRecastForSpell(spell.getSpellId());
    }

    /** Produces a non-spell player damage source, which is required by effects such as Echoing Strikes. */
    public boolean performPhysicalAttack(float damage) {
        LivingEntity target = primaryTarget;
        if (target == null || !target.isAlive()) {
            return false;
        }
        caster.swing(InteractionHand.MAIN_HAND, true);
        target.invulnerableTime = 0;
        return target.hurt(caster.damageSources().playerAttack(caster), Math.max(0.1F, damage));
    }

    /** Lets reactive defensive effects observe one ordinary mob hit without endangering the preview player. */
    public boolean hurtCasterFromPrimaryTarget(float damage) {
        if (!(primaryTarget instanceof Mob attacker) || !attacker.isAlive()) {
            return false;
        }
        float health = caster.getHealth();
        boolean invulnerable = caster.isInvulnerable();
        caster.setInvulnerable(false);
        caster.invulnerableTime = 0;
        boolean hurt = caster.hurt(caster.damageSources().mobAttack(attacker), Math.max(0.1F, damage));
        caster.setHealth(Math.max(health, 1.0F));
        caster.setInvulnerable(invulnerable);
        return hurt;
    }

    /** Runs the normal item finish path so NeoForge item-use events still fire. */
    public ItemStack consume(ItemStack stack) {
        return stack.finishUsingItem(level, caster);
    }

    /** Allows an effect's removal callback to deal its intended damage before restoring preview invulnerability. */
    public boolean removeEffectAllowDamage(Holder<MobEffect> effect) {
        boolean invulnerable = caster.isInvulnerable();
        caster.setInvulnerable(false);
        boolean removed = caster.removeEffect(effect);
        caster.setInvulnerable(invulnerable);
        return removed;
    }

    public void moveCasterTowardPrimaryTarget(double speed) {
        if (primaryTarget == null) {
            return;
        }
        Vec3 direction = primaryTarget.position().subtract(caster.position());
        if (direction.lengthSqr() > 1.0E-4) {
            caster.setDeltaMovement(direction.normalize().scale(speed));
            caster.hurtMarked = true;
        }
    }

    public void rotateCaster(float yawDelta) {
        caster.setYRot(caster.getYRot() + yawDelta);
        caster.setYHeadRot(caster.getYHeadRot() + yawDelta);
        caster.yBodyRot += yawDelta;
    }
}
