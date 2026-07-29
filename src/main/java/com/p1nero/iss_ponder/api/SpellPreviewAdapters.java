package com.p1nero.iss_ponder.api;

import net.minecraft.resources.ResourceLocation;

import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

/** Public registry for add-ons that need to correct a spell's automated preview. */
public final class SpellPreviewAdapters {
    private static final SpellPreviewAdapter DEFAULT = new SpellPreviewAdapter() {
    };
    private static final Map<ResourceLocation, SpellPreviewAdapter> ADAPTERS = new ConcurrentHashMap<>();

    private SpellPreviewAdapters() {
    }

    public static void register(ResourceLocation spellId, SpellPreviewAdapter adapter) {
        ADAPTERS.put(Objects.requireNonNull(spellId), Objects.requireNonNull(adapter));
    }

    public static SpellPreviewAdapter get(ResourceLocation spellId) {
        return ADAPTERS.getOrDefault(spellId, DEFAULT);
    }
}
