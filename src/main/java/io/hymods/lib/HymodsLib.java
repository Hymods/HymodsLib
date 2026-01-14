package io.hymods.lib;

import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.server.core.plugin.JavaPlugin;
import com.hypixel.hytale.server.core.plugin.JavaPluginInit;

/**
 * HymodsLib - A comprehensive utility library for Hytale mods Provides common
 * utilities for raycasting, entity management, world operations, and more.
 */
public class HymodsLib extends JavaPlugin {
    private static final HytaleLogger LOGGER = HytaleLogger.forEnclosingClass();

    public HymodsLib(JavaPluginInit init) {
        super(init);
        LOGGER.atInfo().log("HymodsLib initializing...");
    }

    @Override
    protected void setup() {
        LOGGER.atInfo().log("HymodsLib ready! Providing utilities for Hytale mod development");
    }

}
