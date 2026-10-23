package com.p1nero.iss_ponder.compat.gtbc;

import com.p1nero.iss_ponder.compat.CompatModule;
import com.p1nero.iss_ponder.compat.support.PreviewActions;

/**
 * Upstream: GTBC spell onCast/recast state and Reversal effect/event handler (Neo only).
 * Audited release JARs and exact implementation paths: docs/compatibility/releases.json and SPELL_AUDIT.md.
 */
public final class GtbcCompat extends CompatModule {
    public GtbcCompat() {
        super("unavailable", "3.0.0-1.21.1");
        rule("psychic_bolt", "com.gametechbc.gametechbcsspellbooks.spells.eldritch.PsychicBoltSpell", PreviewActions.recasts(6, 12));
        rule("nullflare", "com.gametechbc.gametechbcsspellbooks.spells.holy.NullflareSpell", PreviewActions.recasts(6, 12));
        rule("reversal", "com.gametechbc.gametechbcsspellbooks.spells.eldritch.ReversalSpell", PreviewActions.reversal());
    }
}
