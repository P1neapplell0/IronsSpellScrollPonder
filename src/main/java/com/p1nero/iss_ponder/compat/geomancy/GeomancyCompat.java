package com.p1nero.iss_ponder.compat.geomancy;

import com.p1nero.iss_ponder.compat.CompatModule;
import com.p1nero.iss_ponder.compat.support.PreviewActions;

/**
 * Upstream: GeoConductorSpell/TremorSpikeSpell#onCast add timed RecastInstance state; do not dismiss Solar Chorus.
 * Audited release JARs and exact implementation paths: docs/compatibility/releases.json and SPELL_AUDIT.md.
 */
public final class GeomancyCompat extends CompatModule {
    public GeomancyCompat() {
        super("2.0.0-1.20.1", "1.1.0-1.21.1");
        rule("geo_conductor", "com.gametechbc.gtbcs_geomancy_plus.spells.geo.GeoConductorSpell", PreviewActions.recasts(8, 12));
        rule("tremor_spike", "com.gametechbc.gtbcs_geomancy_plus.spells.geo.TremorSpikeSpell", PreviewActions.recasts(6, 12));
    }
}
