package com.p1nero.iss_ponder.compat;

import com.p1nero.iss_ponder.ISSPonderMod;
import com.p1nero.iss_ponder.api.SpellPreviewAdapter;
import com.p1nero.iss_ponder.config.CompatConfig;
import io.redspace.ironsspellbooks.api.registry.SpellRegistry;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.fml.ModList;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public final class ExperimentalCompat {
    private static final Map<String, String> PROVIDERS = Map.ofEntries(
            Map.entry("ias_spellbooks", "illage.IllageCompat"),
            Map.entry("legendary_spellbooks", "legendary.LegendaryCompat"),
            Map.entry("traveloptics", "traveloptics.TravelOpticsCompat"),
            Map.entry("cataclysm_spellbooks", "cataclysm.CataclysmCompat"),
            Map.entry("alshanex_familiars", "familiars.FamiliarsCompat"),
            Map.entry("hazennstuff", "hazen.HazenCompat"),
            Map.entry("gametechbcs_spellbooks", "gtbc.GtbcCompat"),
            Map.entry("gtbcs_geomancy_plus", "geomancy.GeomancyCompat"),
            Map.entry("alexs_caves_spellbooks", "alexscaves.AlexsCavesCompat"));
    private static final Map<String, CompatModule> LOADED = new HashMap<>();
    private static final Set<String> SKIPPED = new HashSet<>();

    private ExperimentalCompat() {
    }

    @Nullable
    public static SpellPreviewAdapter find(ResourceLocation spellId) {
        String modId = spellId.getNamespace();
        if (!CompatConfig.enabled(modId) || !PROVIDERS.containsKey(modId)
                || !ModList.get().isLoaded(modId) || SKIPPED.contains(modId)) {
            return null;
        }
        try {
            CompatModule module = LOADED.get(modId);
            if (module == null) {
                module = (CompatModule) Class.forName("com.p1nero.iss_ponder.compat." + PROVIDERS.get(modId))
                        .getDeclaredConstructor().newInstance();
                String version = ModList.get().getModContainerById(modId).orElseThrow()
                        .getModInfo().getVersion().toString();
                if (!module.supports(version)) {
                    SKIPPED.add(modId);
                    ISSPonderMod.LOGGER.warn("Skipping experimental {} adapters: unaudited version {}", modId, version);
                    return null;
                }
                LOADED.put(modId, module);
                ISSPonderMod.LOGGER.info("Loaded experimental preview rules for {} {}", modId, version);
            }
            return module.find(spellId, SpellRegistry.getSpell(spellId));
        } catch (ReflectiveOperationException | RuntimeException | LinkageError exception) {
            SKIPPED.add(modId);
            ISSPonderMod.LOGGER.warn("Disabled experimental {} adapters after a compatibility failure", modId, exception);
            return null;
        }
    }
}
