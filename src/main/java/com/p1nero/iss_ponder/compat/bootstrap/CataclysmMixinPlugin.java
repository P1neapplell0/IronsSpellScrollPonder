package com.p1nero.iss_ponder.compat.bootstrap;

import com.electronwill.nightconfig.core.file.CommentedFileConfig;
import net.minecraftforge.fml.loading.FMLPaths;
import net.minecraftforge.fml.loading.LoadingModList;
import org.objectweb.asm.tree.ClassNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spongepowered.asm.mixin.extensibility.IMixinConfigPlugin;
import org.spongepowered.asm.mixin.extensibility.IMixinInfo;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Set;

public final class CataclysmMixinPlugin implements IMixinConfigPlugin {
    private static final Logger LOGGER = LoggerFactory.getLogger("iss_ponder/experimental");
    private boolean configured;
    private boolean reportedYield;

    @Override
    public void onLoad(String mixinPackage) {
        // Mixins run before Forge constructs the mod/config spec. Missing or malformed files fail closed.
        try {
            Path path = FMLPaths.CONFIGDIR.get().resolve("iss_ponder-common.toml");
            if (!Files.isRegularFile(path)) {
                return;
            }
            try (CommentedFileConfig config = CommentedFileConfig.of(path)) {
                config.load();
                Object addon = config.get("experimental.addons.cataclysm_spellbooks");
                configured = Boolean.TRUE.equals(config.get("experimental.enabled"))
                        && Boolean.TRUE.equals(config.get("experimental.cataclysmLegacyPatch"))
                        && (addon == null || Boolean.TRUE.equals(addon));
            }
        } catch (RuntimeException exception) {
            LOGGER.warn("Skipping experimental startup patch: unreadable compatibility config", exception);
        }
    }

    @Override
    public boolean shouldApplyMixin(String targetClassName, String mixinClassName) {
        if (!configured) {
            return false;
        }
        LoadingModList list = LoadingModList.get();
        if (list == null) {
            return false;
        }
        if (list.getModFileById("bielgg_spells") != null) {
            if (!reportedYield) {
                reportedYield = true;
                LOGGER.info("Yielding Cataclysm legacy patch ownership to installed BielGG's Spells");
            }
            return false;
        }
        String cataclysm = "";
        String spellbooks = "";
        for (var info : list.getMods()) {
            if (info.getModId().equals("cataclysm")) {
                cataclysm = info.getVersion().toString();
            } else if (info.getModId().equals("cataclysm_spellbooks")) {
                spellbooks = info.getVersion().toString();
            }
        }
        return cataclysm.equals("3.31") && spellbooks.equals("1.2.9-1.20.1");
    }

    @Override
    public void postApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {
        try {
            // Do not resolve the transformer or addon APIs at all when the startup gate is closed.
            Class.forName("com.p1nero.iss_ponder.compat.cataclysm.legacy.CataclysmLegacyTransformer")
                    .getMethod("rewrite", ClassNode.class).invoke(null, targetClass);
            LOGGER.info("Applied experimental Cataclysm legacy patch to {}", targetClassName);
        } catch (ReflectiveOperationException | RuntimeException | LinkageError exception) {
            LOGGER.error("Skipped experimental Cataclysm legacy patch for {}", targetClassName, exception);
        }
    }

    @Override
    public String getRefMapperConfig() {
        return null;
    }

    @Override
    public void acceptTargets(Set<String> myTargets, Set<String> otherTargets) {
    }

    @Override
    public List<String> getMixins() {
        return null;
    }

    @Override
    public void preApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {
    }
}
