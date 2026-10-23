package com.p1nero.iss_ponder.compat.cataclysm;

import com.p1nero.iss_ponder.compat.CompatModule;
import com.p1nero.iss_ponder.compat.support.PreviewActions;

/**
 * Upstream: Cataclysm Spellbooks spell onCast/recast state; RewireSpell requires an IMagicSummon target.
 * Audited release JARs and exact implementation paths: docs/compatibility/releases.json and SPELL_AUDIT.md.
 */
public final class CataclysmCompat extends CompatModule {
    public CataclysmCompat() {
        // Runtime mod versions include the MC suffix (Forge resolves ${file.jarVersion} from the manifest).
        super("1.2.9-1.20.1", "1.1.14-1.21");
        rule("final_rend", "net.acetheeldritchking.cataclysm_spellbooks.spells.blood.FinalRendSpell", PreviewActions.recasts(6, 12));
        rule("quick_strike", "net.acetheeldritchking.cataclysm_spellbooks.spells.blood.QuickStrikeSpell", PreviewActions.recasts(6, 12));
        rule("abyss_fireball", "net.acetheeldritchking.cataclysm_spellbooks.spells.fire.AbyssFireballSpell", PreviewActions.recasts(6, 12));
        rule("piercing_bone", "net.acetheeldritchking.cataclysm_spellbooks.spells.fire.BonePierceSpell", PreviewActions.recasts(6, 12));
        rule("meteor_spew", "net.acetheeldritchking.cataclysm_spellbooks.spells.fire.MeteorSpewSpell", PreviewActions.recasts(6, 12));
        rule("cryopiercer", "net.acetheeldritchking.cataclysm_spellbooks.spells.ice.CryopiercerSpell", PreviewActions.recasts(6, 12));
        rule("phantasmal_blade", "net.acetheeldritchking.cataclysm_spellbooks.spells.ice.PhantasmalBladeSpell", PreviewActions.recasts(6, 12));
        rule("amethyst_puncture", "net.acetheeldritchking.cataclysm_spellbooks.spells.nature.AmethystPunctureSpell", PreviewActions.recasts(6, 12));
        rule("disabling_swipe", "net.acetheeldritchking.cataclysm_spellbooks.spells.technomancy.DisablingSwipeSpell", PreviewActions.recasts(6, 12));
        rule("laserbolt", "net.acetheeldritchking.cataclysm_spellbooks.spells.technomancy.LaserboltSpell", PreviewActions.recasts(6, 12));
        rule("missile_launch", "net.acetheeldritchking.cataclysm_spellbooks.spells.technomancy.MissileLaunchSpell", PreviewActions.recasts(6, 12));
        rule("rewire", "net.acetheeldritchking.cataclysm_spellbooks.spells.technomancy.RewireSpell", PreviewActions.friendlySummon("cataclysm_spellbooks:summoned_watcher"));
    }
}
