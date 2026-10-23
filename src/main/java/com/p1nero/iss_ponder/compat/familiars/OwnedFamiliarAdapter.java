package com.p1nero.iss_ponder.compat.familiars;

import com.p1nero.iss_ponder.api.SpellPreviewAdapter;
import com.p1nero.iss_ponder.api.SpellPreviewContext;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;

import java.util.UUID;

/** SwitcherooSpell v5 requires an AbstractSpellCastingPet whose getSummoner is the caster. */
final class OwnedFamiliarAdapter implements SpellPreviewAdapter {
    @Override
    @SuppressWarnings("unchecked")
    public EntityType<? extends LivingEntity> primaryTargetType() {
        return (EntityType<? extends LivingEntity>) BuiltInRegistries.ENTITY_TYPE
                .getOptional(ResourceLocation.parse("alshanex_familiars:mage_pet"))
                .orElseThrow(() -> new IllegalStateException("Missing audited familiar entity"));
    }

    @Override
    public void beforeCast(SpellPreviewContext context) {
        // A freshly loaded scene chunk may not expose new entities through its UUID lookup yet.
        // Prepare ownership after the normal cast delay, immediately before target validation.
        if (context.primaryTarget() == null) throw new IllegalStateException("Missing familiar target");
        try {
            // FamiliarsLib 2.0 AbstractSpellCastingPet#setOwnerUUID; do not link the optional API.
            // Its getSummoner delegates to Iron's OwnerHelper, which resolves this UUID in ServerLevel.
            context.primaryTarget().getClass().getMethod("setOwnerUUID", UUID.class)
                    .invoke(context.primaryTarget(), context.caster().getUUID());
            Object summoner = context.primaryTarget().getClass().getMethod("getSummoner")
                    .invoke(context.primaryTarget());
            if (!(summoner instanceof LivingEntity owner) || !owner.getUUID().equals(context.caster().getUUID())) {
                throw new IllegalStateException("Familiar did not resolve its preview owner");
            }
        } catch (ReflectiveOperationException exception) {
            throw new IllegalStateException("Audited familiar ownership API changed", exception);
        }
    }

    @Override
    public Action tickAfterCast(SpellPreviewContext context) {
        return context.ticksSinceLastCast() < 30 ? Action.WAIT : Action.COMPLETE;
    }
}
