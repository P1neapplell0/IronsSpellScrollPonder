package com.p1nero.iss_ponder.compat.support;

import com.p1nero.iss_ponder.api.SpellPreviewAdapter;
import com.p1nero.iss_ponder.api.SpellPreviewContext;
import io.redspace.ironsspellbooks.capabilities.magic.SummonManager;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

import java.util.LinkedHashMap;
import java.util.Map;

public final class PreviewActions {
    private PreviewActions() {
    }

    public static SpellPreviewAdapter castSource(io.redspace.ironsspellbooks.api.spells.CastSource source) {
        return new SpellPreviewAdapter() {
            @Override
            public io.redspace.ironsspellbooks.api.spells.CastSource castSource() {
                return source;
            }
        };
    }

    /** Normal book-only validation, optionally with the required Curios spellbook equipped. */
    public static SpellPreviewAdapter spellbook(String requiredBook) {
        return spellbook(requiredBook, 0);
    }

    public static SpellPreviewAdapter spellbook(String requiredBook, int followUpTicks) {
        return new SpellPreviewAdapter() {
            @Override
            public io.redspace.ironsspellbooks.api.spells.CastSource castSource() {
                return io.redspace.ironsspellbooks.api.spells.CastSource.SPELLBOOK;
            }

            @Override
            public void beforeCast(SpellPreviewContext context) {
                if (requiredBook == null) return;
                var item = BuiltInRegistries.ITEM.getOptional(ResourceLocation.parse(requiredBook))
                        .orElseThrow(() -> new IllegalStateException("Missing preview book " + requiredBook));
                var inventory = top.theillusivec4.curios.api.CuriosApi.getCuriosInventory(context.caster())
                        .orElseThrow(() -> new IllegalStateException("No preview Curios inventory"));
                var slot = inventory.getStacksHandler("spellbook")
                        .orElseThrow(() -> new IllegalStateException("No preview spellbook slot"));
                slot.getStacks().setStackInSlot(0, new ItemStack(item));
            }

            @Override
            public Action tickAfterCast(SpellPreviewContext context) {
                // Energy Beam's caster effect must keep ticking; Geyser's portals need time to unfold.
                return context.ticksSinceLastCast() < followUpTicks ? Action.WAIT : Action.COMPLETE;
            }
        };
    }

    /** Resolve prerequisites by registry ID, without linking to an optional addon's item classes. */
    public static SpellPreviewAdapter equipment(String itemId, EquipmentSlot slot) {
        return new SpellPreviewAdapter() {
            @Override
            public void beforeCast(SpellPreviewContext context) {
                var item = BuiltInRegistries.ITEM.getOptional(ResourceLocation.parse(itemId))
                        .orElseThrow(() -> new IllegalStateException("Missing preview equipment " + itemId));
                context.caster().setItemSlot(slot, new ItemStack(item));
            }
        };
    }

    public static SpellPreviewAdapter recasts(int delay, int maximumCasts) {
        return recastsOnWideFloor(delay, maximumCasts, 0);
    }

    public static SpellPreviewAdapter recastsOnWideFloor(int delay, int maximumCasts, int radius) {
        return new SpellPreviewAdapter() {
            @Override
            public Map<BlockPos, BlockState> initialBlocks(SpellPreviewContext context) {
                Map<BlockPos, BlockState> blocks = new LinkedHashMap<>();
                for (int x = -radius; radius > 0 && x <= radius; x++) {
                    for (int z = -radius; z <= radius; z++) {
                        blocks.put(new BlockPos(x, 0, z - 2), Blocks.STONE.defaultBlockState());
                    }
                }
                return blocks;
            }

            @Override
            public Action tickAfterCast(SpellPreviewContext context) {
                if (context.ticksSinceLastCast() < delay) {
                    return Action.WAIT;
                }
                return context.completedCasts() < maximumCasts && context.hasRemainingRecasts()
                        ? Action.RECAST : Action.COMPLETE;
            }
        };
    }

    public static SpellPreviewAdapter criticalAttack() {
        return new SpellPreviewAdapter() {
            @Override
            public void onSceneReady(SpellPreviewContext context) {
                context.caster().setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(Items.IRON_SWORD));
            }

            @Override
            public Action tickAfterCast(SpellPreviewContext context) {
                if (context.ticksSinceLastCast() == 22 && context.primaryTarget() != null) {
                    boolean onGround = context.caster().onGround();
                    float fallDistance = context.caster().fallDistance;
                    context.caster().setOnGround(false);
                    context.caster().fallDistance = 1.0F;
                    try {
                        context.caster().attack(context.primaryTarget());
                    } finally {
                        context.caster().setOnGround(onGround);
                        context.caster().fallDistance = fallDistance;
                    }
                }
                return context.ticksSinceLastCast() >= 42 ? Action.COMPLETE : Action.WAIT;
            }
        };
    }

    public static SpellPreviewAdapter reversal() {
        return new SpellPreviewAdapter() {
            @Override
            public void beforeCast(SpellPreviewContext context) {
                context.caster().setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(Items.IRON_SWORD));
            }

            @Override
            public void onCastCompleted(SpellPreviewContext context) {
                context.hurtCasterFromPrimaryTarget(2.0F);
            }

            @Override
            public Action tickAfterCast(SpellPreviewContext context) {
                return context.ticksSinceLastCast() >= 24 ? Action.COMPLETE : Action.WAIT;
            }
        };
    }

    public static SpellPreviewAdapter submerged() {
        return submerged(false);
    }

    public static SpellPreviewAdapter submerged(boolean swimming) {
        return new SpellPreviewAdapter() {
            @Override
            public Map<BlockPos, BlockState> initialBlocks(SpellPreviewContext context) {
                Map<BlockPos, BlockState> blocks = new LinkedHashMap<>();
                for (int x = -1; x <= 1; x++) {
                    // The caster's Z is exactly -2.0, on a block boundary: contain both adjacent cells.
                    for (int z = -4; z <= -1; z++) {
                        for (int y = 1; y <= 3; y++) {
                            boolean wall = x != 0 || z == -4 || z == -1;
                            blocks.put(new BlockPos(x, y, z), wall ? Blocks.GLASS.defaultBlockState()
                                    : Blocks.WATER.defaultBlockState());
                        }
                    }
                }
                return blocks;
            }

            @Override
            public void beforeCast(SpellPreviewContext context) {
                // Entity#updateFluidOnEyes copies the PREVIOUS eye-fluid state into wasEyeInWater.
                // isUnderWater therefore needs two base ticks, not a single FakePlayer#doTick.
                context.caster().baseTick();
                context.caster().baseTick();
                if (swimming) {
                    // Player#isSwimming explicitly returns false while the flying ability is active.
                    context.caster().getAbilities().flying = false;
                    context.caster().setSprinting(true);
                    context.caster().setSwimming(true);
                }
            }

            @Override
            public Action tickAfterCast(SpellPreviewContext context) {
                return swimming && context.ticksSinceLastCast() < 24 ? Action.WAIT : Action.COMPLETE;
            }
        };
    }

    public static SpellPreviewAdapter friendlySummon(String entityId) {
        return new SpellPreviewAdapter() {
            @Override
            @SuppressWarnings("unchecked")
            public EntityType<? extends LivingEntity> primaryTargetType() {
                ResourceLocation id = ResourceLocation.parse(entityId);
                return (EntityType<? extends LivingEntity>) BuiltInRegistries.ENTITY_TYPE.getOptional(id)
                        .orElse(EntityType.ZOMBIE);
            }

            @Override
            public void beforeCast(SpellPreviewContext context) {
                // UUID lookup is ready after the normal scene warm-up, not necessarily at scene creation.
                if (context.primaryTarget() == null) throw new IllegalStateException("Missing summon target");
                SummonManager.setOwner(context.primaryTarget(), context.caster());
            }
        };
    }

    public static SpellPreviewAdapter fleeingTarget() {
        return new SpellPreviewAdapter() {
            @Override
            public void beforeCast(SpellPreviewContext context) {
                if (!(context.primaryTarget() instanceof Mob mob)) {
                    throw new IllegalStateException("Missing fleeing target");
                }
                mob.setNoAi(false);
            }

            @Override
            public Action tickAfterCast(SpellPreviewContext context) {
                return context.ticksSinceLastCast() >= 45 ? Action.COMPLETE : Action.WAIT;
            }
        };
    }
}
