package com.p1nero.iss_ponder.compat.familiars;

import com.p1nero.iss_ponder.compat.CompatModule;
import com.p1nero.iss_ponder.compat.support.PreviewActions;

/**
 * Upstream: DefaultNoteSpell#onCast selects the BlackNote projectile on its final recast (Forge v4 only).
 * Audited release JARs and exact implementation paths: docs/compatibility/releases.json and SPELL_AUDIT.md.
 */
public final class FamiliarsCompat extends CompatModule {
    public FamiliarsCompat() {
        super("1.20.1_v4.1.0", "1.21.1_v5.0");
        rule("switcheroo", "net.alshanex.alshanex_familiars.spells.SwitcherooSpell", new OwnedFamiliarAdapter());
        rule("default_note", "net.alshanex.alshanex_familiars.spells.DefaultNoteSpell", PreviewActions.recasts(6, 12));
    }
}
