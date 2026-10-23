package com.p1nero.iss_ponder.compat.illage;

import com.p1nero.iss_ponder.api.SpellPreviewAdapter;
import com.p1nero.iss_ponder.compat.CompatModule;

/**
 * Illage and Spell-age 1.1.0 summon registerGoals methods link to the removed Iron's OwnerGetter.
 * The real Handemic development-client test reproduced a delayed MagicManager tick crash.
 * Refuse these previews when that API is absent; this does not repair normal upstream gameplay.
 */
public final class IllageCompat extends CompatModule {
    public IllageCompat() {
        super("1.1.0-1.20.1", "unavailable");
        SpellPreviewAdapter guarded = new SpellPreviewAdapter() {
            @Override
            public boolean allowsSimulation() {
                try {
                    Class.forName("io.redspace.ironsspellbooks.entity.mobs.goals.OwnerGetter", false,
                            IllageCompat.class.getClassLoader());
                    return true;
                } catch (ClassNotFoundException | LinkageError unavailable) {
                    return false;
                }
            }
        };
        rule("handemic", "com.gametechbc.iasspellbooks.spells.evocation.HandemicSpell", guarded);
        rule("spirits_chosen", "com.gametechbc.iasspellbooks.spells.blood.SpiritsChosenSpell", guarded);
        rule("venomous_reborn", "com.gametechbc.iasspellbooks.spells.eldritch.VenomousRebornSpell", guarded);
    }
}
