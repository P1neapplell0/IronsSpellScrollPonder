package com.p1nero.iss_ponder;

import com.mojang.logging.LogUtils;
import com.p1nero.iss_ponder.config.ClientConfig;
import com.p1nero.iss_ponder.network.ModNetwork;
import com.p1nero.iss_ponder.server.BuiltinSpellPreviewAdapters;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import org.slf4j.Logger;

@Mod(ISSPonderMod.MOD_ID)
public class ISSPonderMod {

    public static final String MOD_ID = "iss_ponder";
    public static final Logger LOGGER = LogUtils.getLogger();

    public ISSPonderMod(IEventBus modBus, ModContainer modContainer) {
        modContainer.registerConfig(ModConfig.Type.CLIENT, ClientConfig.SPEC);
        modBus.addListener(ModNetwork::register);
        BuiltinSpellPreviewAdapters.register();
    }
}
