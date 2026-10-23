package com.p1nero.iss_ponder.compat.alexscaves;

import com.p1nero.iss_ponder.compat.CompatModule;
import com.p1nero.iss_ponder.compat.support.PreviewActions;

/**
 * Upstream: AspectOfTremorsaurusSpell and fear effect use target navigation, disabled on ordinary scene zombies.
 * Audited release JARs and exact implementation paths: docs/compatibility/releases.json and SPELL_AUDIT.md.
 */
public final class AlexsCavesCompat extends CompatModule {
    public AlexsCavesCompat() {
        super("1.2.0", "1.2.1");
        rule("aspect_of_tremorsaurus", "net.temporal.alexs_caves_spellbooks.core.spells.nature.AspectOfTremorsaurusSpell", PreviewActions.fleeingTarget());
    }
}
