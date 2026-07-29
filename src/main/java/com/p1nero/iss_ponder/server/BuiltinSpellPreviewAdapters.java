package com.p1nero.iss_ponder.server;

import com.p1nero.iss_ponder.api.SpellPreviewAdapter;
import com.p1nero.iss_ponder.api.SpellPreviewAdapters;
import com.p1nero.iss_ponder.api.SpellPreviewContext;
import io.redspace.ironsspellbooks.entity.spells.magic_arrow.MagicArrowProjectile;
import io.redspace.ironsspellbooks.registries.MobEffectRegistry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.phys.Vec3;

/** Built-in corrections derived from the corresponding Iron's Spellbooks 3.15.4 spell/effect classes. */
public final class BuiltinSpellPreviewAdapters {
    private static boolean registered;

    private BuiltinSpellPreviewAdapters() {
    }

    public static void register() {
        if (registered) {
            return;
        }
        registered = true;

        SpellPreviewAdapter restricted = new SpellPreviewAdapter() {
            @Override
            public boolean allowsSimulation() {
                return false;
            }
        };
        register(restricted, "pocket_dimension", "recall", "portal");

        SpellPreviewAdapters.register(id("echoing_strikes"), followUp(10, 32,
                context -> context.performPhysicalAttack(4.0F)));
        SpellPreviewAdapters.register(id("invisibility"), followUp(18, 28,
                context -> context.performPhysicalAttack(2.0F)));
        SpellPreviewAdapters.register(id("spider_aspect"), followUp(12, 30, context -> {
            if (context.primaryTarget() != null) {
                context.primaryTarget().addEffect(new MobEffectInstance(MobEffects.POISON, 100, 0));
            }
            context.performPhysicalAttack(4.0F);
        }));
        SpellPreviewAdapters.register(id("abyssal_shroud"), followUp(12, 32,
                context -> context.hurtCasterFromPrimaryTarget(2.0F)));
        SpellPreviewAdapters.register(id("evasion"), followUp(12, 32,
                context -> context.hurtCasterFromPrimaryTarget(2.0F)));
        SpellPreviewAdapters.register(id("oakskin"), followUp(12, 28,
                context -> context.hurtCasterFromPrimaryTarget(4.0F)));
        SpellPreviewAdapters.register(id("blight"), followUp(12, 28,
                context -> context.hurtCasterFromPrimaryTarget(4.0F)));
        SpellPreviewAdapters.register(id("gluttony"), followUp(12, 24,
                context -> context.consume(new ItemStack(Items.COOKED_BEEF))));
        SpellPreviewAdapters.register(id("volt_strike"), followUp(2, 18,
                context -> context.moveCasterTowardPrimaryTarget(1.8)));
        SpellPreviewAdapters.register(id("heartstop"), new SpellPreviewAdapter() {
            @Override
            public Action tickAfterCast(SpellPreviewContext context) {
                if (context.ticksSinceLastCast() == 6) {
                    context.hurtCasterFromPrimaryTarget(8.0F);
                } else if (context.ticksSinceLastCast() == 24) {
                    context.removeEffectAllowDamage(MobEffectRegistry.HEARTSTOP.get());
                }
                return context.ticksSinceLastCast() >= 38 ? Action.COMPLETE : Action.WAIT;
            }
        });
        SpellPreviewAdapters.register(id("guiding_bolt"), followUp(10, 45, context -> {
            if (context.primaryTarget() == null) {
                return;
            }
            MagicArrowProjectile projectile = new MagicArrowProjectile(context.level(), context.caster());
            projectile.setPos(context.caster().getEyePosition());
            Vec3 direction = context.primaryTarget().getEyePosition().subtract(projectile.position())
                    .normalize().yRot(0.35F);
            projectile.shoot(direction);
            context.level().addFreshEntity(projectile);
        }));

        SpellPreviewAdapter fastRecasts = automaticRecasts(8, 12, 0.0F);
        SpellPreviewAdapters.register(id("eldritch_blast"), fastRecasts);
        SpellPreviewAdapters.register(id("flaming_barrage"), fastRecasts);
        SpellPreviewAdapters.register(id("raise_hell"), automaticRecasts(10, 12, 0.0F));
        SpellPreviewAdapters.register(id("wall_of_fire"), automaticRecasts(8, 4, 24.0F));
        SpellPreviewAdapters.register(id("thunder_step"), automaticRecasts(24, 3, 0.0F));

        SpellPreviewAdapters.register(id("counterspell"), new SpellPreviewAdapter() {
            @Override
            public void onSceneReady(SpellPreviewContext context) {
                Vec3 position = context.caster().getEyePosition().add(context.caster().getLookAngle().scale(3.0));
                MagicArrowProjectile target = new MagicArrowProjectile(context.level(), context.caster());
                target.setPos(position);
                target.setDeltaMovement(Vec3.ZERO);
                context.level().addFreshEntity(target);
            }
        });
    }

    private static SpellPreviewAdapter followUp(int actionTick, int completionTick,
                                                java.util.function.Consumer<SpellPreviewContext> action) {
        return new SpellPreviewAdapter() {
            @Override
            public Action tickAfterCast(SpellPreviewContext context) {
                if (context.ticksSinceLastCast() == actionTick) {
                    action.accept(context);
                }
                return context.ticksSinceLastCast() >= completionTick ? Action.COMPLETE : Action.WAIT;
            }
        };
    }

    private static SpellPreviewAdapter automaticRecasts(int delay, int maxCasts, float yawStep) {
        return new SpellPreviewAdapter() {
            @Override
            public Action tickAfterCast(SpellPreviewContext context) {
                if (context.ticksSinceLastCast() < delay) {
                    return Action.WAIT;
                }
                return context.completedCasts() < maxCasts && context.hasRemainingRecasts()
                        ? Action.RECAST : Action.COMPLETE;
            }

            @Override
            public void beforeRecast(SpellPreviewContext context) {
                if (yawStep != 0.0F) {
                    context.rotateCaster(yawStep * context.completedCasts());
                }
            }
        };
    }

    private static void register(SpellPreviewAdapter adapter, String... paths) {
        for (String path : paths) {
            SpellPreviewAdapters.register(id(path), adapter);
        }
    }

    private static ResourceLocation id(String path) {
        return ResourceLocation.fromNamespaceAndPath("irons_spellbooks", path);
    }
}
