package com.p1nero.iss_ponder;

import com.mojang.logging.LogUtils;
import com.p1nero.iss_ponder.config.ClientConfig;
import com.p1nero.iss_ponder.network.ModNetwork;
import com.p1nero.iss_ponder.server.BuiltinSpellPreviewAdapters;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import org.slf4j.Logger;

@Mod(ISSPonderMod.MOD_ID)
public class ISSPonderMod {

    public static final String MOD_ID = "iss_ponder";
    public static final Logger LOGGER = LogUtils.getLogger();

    @SuppressWarnings("removal") // Forge 1.20.1 still exposes config registration through this loading context.
    public ISSPonderMod() {
        ModLoadingContext.get().registerConfig(ModConfig.Type.CLIENT, ClientConfig.SPEC);
        BuiltinSpellPreviewAdapters.register();
        ModNetwork.register();
    }
}
