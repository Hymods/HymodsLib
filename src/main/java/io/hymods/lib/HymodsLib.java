package io.hymods.lib;

import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.server.core.plugin.JavaPlugin;
import com.hypixel.hytale.server.core.plugin.JavaPluginInit;

/**
 * HymodsLib - A comprehensive utility library for Hytale mods Provides common
 * utilities for raycasting, entity management, world operations, and more.
 * 
 * @deprecated This is the main class for the HymodsLib mod, and isn't intended
 *             to be used in your code.
 */
@Deprecated
public class HymodsLib extends JavaPlugin {
    private static final HytaleLogger LOGGER = HytaleLogger.forEnclosingClass();

    @Deprecated
    public HymodsLib(JavaPluginInit init) {
        super(init);
        LOGGER.atInfo().log("HymodsLib initializing...");
    }

    @Deprecated
    @Override
    protected void setup() {
        LOGGER.atInfo().log("HymodsLib ready! Providing utilities for Hytale mod development");
    }

}
