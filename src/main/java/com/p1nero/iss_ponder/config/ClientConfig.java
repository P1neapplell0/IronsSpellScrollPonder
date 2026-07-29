package com.p1nero.iss_ponder.config;

import net.minecraftforge.common.ForgeConfigSpec;

/** Client-only presentation settings. */
public final class ClientConfig {
    public static final ForgeConfigSpec SPEC;
    public static final ForgeConfigSpec.BooleanValue SHOW_SCROLL_SPELL_DESCRIPTION;

    static {
        ForgeConfigSpec.Builder builder = new ForgeConfigSpec.Builder();
        builder.push("tooltip");
        SHOW_SCROLL_SPELL_DESCRIPTION = builder
                .comment("Add the spell's guide description to spell scroll tooltips.")
                .define("showScrollSpellDescription", true);
        builder.pop();
        SPEC = builder.build();
    }

    private ClientConfig() {
    }
}
