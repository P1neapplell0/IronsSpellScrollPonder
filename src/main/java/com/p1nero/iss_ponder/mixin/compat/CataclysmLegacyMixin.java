package com.p1nero.iss_ponder.mixin.compat;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;

@Pseudo
@Mixin(targets = {
        "net.acetheeldritchking.cataclysm_spellbooks.entity.render.mobs.PhantomAncientRemnantRenderer",
        "net.acetheeldritchking.cataclysm_spellbooks.entity.render.mobs.AncientRemnantTransLayer",
        "net.acetheeldritchking.cataclysm_spellbooks.entity.spells.bullets.FrozenBulletProjectile",
        "net.acetheeldritchking.cataclysm_spellbooks.entity.spells.bullets.MoltenBulletProjectile",
        "net.acetheeldritchking.cataclysm_spellbooks.entity.spells.parting_shot.PartingShotProjectile",
        "net.acetheeldritchking.cataclysm_spellbooks.entity.spells.extended.ExtendedLaserBeamEntity",
        "net.acetheeldritchking.cataclysm_spellbooks.effects.potion.CursedFrenzyEffect",
        "net.acetheeldritchking.cataclysm_spellbooks.effects.potion.KingsWrathPotionEffect",
        "net.acetheeldritchking.cataclysm_spellbooks.items.armor.MechanicalFlightArmorItem",
        "net.acetheeldritchking.cataclysm_spellbooks.spells.ender.GravitationPullSpell",
        "net.acetheeldritchking.cataclysm_spellbooks.spells.holy.ThothsWitnessSpell",
        "net.acetheeldritchking.cataclysm_spellbooks.spells.technomancy.AerialAssaultSpell"
}, remap = false)
public abstract class CataclysmLegacyMixin {
}
