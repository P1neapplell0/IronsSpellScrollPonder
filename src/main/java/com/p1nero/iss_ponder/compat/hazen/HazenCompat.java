package com.p1nero.iss_ponder.compat.hazen;

import com.p1nero.iss_ponder.compat.CompatModule;
import com.p1nero.iss_ponder.compat.support.PreviewActions;

/**
 * Upstream: DazzlingObliterationSpell#onCast advances its staged sequence through real recast state (Neo only).
 * Audited release JARs and exact implementation paths: docs/compatibility/releases.json and SPELL_AUDIT.md.
 */
public final class HazenCompat extends CompatModule {
    public HazenCompat() {
        super("1.1.2", "1.4.0.14");
        rule("dazzling_obliteration", "net.hazen.hazennstuff.Spells.Schools.Lightning.DazzlingObliterationSpell", PreviewActions.recasts(12, 3));
    }
}
