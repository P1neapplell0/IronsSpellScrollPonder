package com.p1nero.iss_ponder.config;

import net.minecraftforge.common.ForgeConfigSpec;

import java.util.LinkedHashMap;
import java.util.Map;

public final class CompatConfig {
    public static final ForgeConfigSpec SPEC;
    public static final ForgeConfigSpec.BooleanValue ENABLED;
    public static final ForgeConfigSpec.BooleanValue CATACLYSM_LEGACY_PATCH;
    private static final Map<String, ForgeConfigSpec.BooleanValue> ADDONS = new LinkedHashMap<>();

    static {
        ForgeConfigSpec.Builder builder = new ForgeConfigSpec.Builder();
        builder.push("experimental");
        ENABLED = builder.comment("Enable audited, optional add-on preview adapters. Disabled by default.",
                        "Providers are not loaded when disabled. Restart after changing compatibility settings.")
                .worldRestart().define("enabled", false);
        CATACLYSM_LEGACY_PATCH = builder.comment(
                        "Forge 1.20.1 only: repair Cataclysm Spellbooks 1.2.9 references to Cataclysm 3.31.",
                        "Requires enabled=true and addons.cataclysm_spellbooks=true. Restart required.",
                        "Skipped when BielGG's Spells is installed; its own compatibility patch takes precedence.")
                .worldRestart().define("cataclysmLegacyPatch", false);
        builder.push("addons");
        for (String modId : new String[]{"legendary_spellbooks", "traveloptics", "cataclysm_spellbooks",
                "alshanex_familiars", "hazennstuff", "gametechbcs_spellbooks", "gtbcs_geomancy_plus",
                "alexs_caves_spellbooks", "ias_spellbooks"}) {
            ADDONS.put(modId, builder.worldRestart().define(modId, true));
        }
        builder.pop();
        builder.pop();
        SPEC = builder.build();
    }

    private CompatConfig() {
    }

    public static boolean enabled(String modId) {
        ForgeConfigSpec.BooleanValue addon = ADDONS.get(modId);
        return ENABLED.get() && addon != null && addon.get();
    }
}
