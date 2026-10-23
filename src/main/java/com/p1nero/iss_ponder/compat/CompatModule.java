package com.p1nero.iss_ponder.compat;

import com.p1nero.iss_ponder.api.SpellPreviewAdapter;
import com.p1nero.iss_ponder.compat.support.GuardedAdapter;
import io.redspace.ironsspellbooks.api.spells.AbstractSpell;
import net.minecraft.SharedConstants;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public abstract class CompatModule {
    private final Map<String, Set<String>> versions;
    private final Map<String, Rule> rules = new HashMap<>();

    protected CompatModule(String forgeVersion, String neoForgeVersion) {
        versions = Map.of("1.20.1", Set.of(forgeVersion), "1.21.1", Set.of(neoForgeVersion));
    }

    public final boolean supports(String version) {
        return versions.getOrDefault(SharedConstants.getCurrentVersion().getName(), Set.of()).contains(version);
    }

    protected final void rule(String spellId, String implementation, SpellPreviewAdapter adapter) {
        rules.put(spellId + "|" + implementation, new Rule(implementation, adapter));
    }

    @Nullable
    public final SpellPreviewAdapter find(ResourceLocation spellId, @Nullable AbstractSpell spell) {
        if (spell == null) {
            return null;
        }
        Rule rule = rules.get(spellId.getPath() + "|" + spell.getClass().getName());
        if (rule == null) {
            return null;
        }
        return new GuardedAdapter(spellId, rule.adapter);
    }

    private record Rule(String implementation, SpellPreviewAdapter adapter) {
    }
}
