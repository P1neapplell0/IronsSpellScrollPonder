package com.p1nero.iss_ponder.compat.legendary;

import com.p1nero.iss_ponder.compat.CompatModule;
import com.p1nero.iss_ponder.compat.support.PreviewActions;

/**
 * Upstream: FlameEaterSpell/OvergrownShockwaveSpell#onCast; AnnihilationResonanceHandler#onCriticalHit.
 * Audited release JARs and exact implementation paths: docs/compatibility/releases.json and SPELL_AUDIT.md.
 */
public final class LegendaryCompat extends CompatModule {
    public LegendaryCompat() {
        super("0.4.1", "0.3.2");
        // Reference: canBeCastedBy rejects COMMAND and, for Geyser, checks the equipped Curios book.
        rule("annihilation_geyser", "dev.higurashi.legendary_spellbooks.common.spells.annihilation.AnnihilationGeyserSpell",
                PreviewActions.spellbook("legendary_spellbooks:annihilators_protocol", 65));
        rule("energy_beam", "dev.higurashi.legendary_spellbooks.common.spells.lightning.EnergyBeamSpell",
                PreviewActions.spellbook(null, 80));
        rule("flame_eater", "dev.higurashi.legendary_spellbooks.common.spells.fire.FlameEaterSpell", PreviewActions.recasts(5, 12));
        rule("overgrown_shockwave", "dev.higurashi.legendary_spellbooks.common.spells.nature.OvergrownShockwaveSpell", PreviewActions.recastsOnWideFloor(8, 6, 11));
        rule("annihilation_resonance", "dev.higurashi.legendary_spellbooks.common.spells.annihilation.AnnihilationResonanceSpell", PreviewActions.criticalAttack());
    }
}
