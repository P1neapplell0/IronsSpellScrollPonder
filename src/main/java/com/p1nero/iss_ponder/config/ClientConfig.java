package com.p1nero.iss_ponder.config;

import net.neoforged.neoforge.common.ModConfigSpec;

/** Client-only presentation settings. */
public final class ClientConfig {
    public static final ModConfigSpec SPEC;
    public static final ModConfigSpec.BooleanValue SHOW_SCROLL_SPELL_DESCRIPTION;

    static {
        ModConfigSpec.Builder builder = new ModConfigSpec.Builder();
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
