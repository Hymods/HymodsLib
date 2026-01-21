package io.hymods.lib.hud;

import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.server.core.entity.entities.player.hud.CustomUIHud;
import com.hypixel.hytale.server.core.ui.builder.UICommandBuilder;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import org.checkerframework.checker.nullness.compatqual.NonNullDecl;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.logging.Level;

/**
 * A custom HUD implementation that can contain and render multiple HUDs simultaneously.
 * 
 * Since Hytale does not natively support showing multiple custom HUDs at once,
 * this class wraps multiple CustomUIHud instances and renders them all by using
 * reflection to call their protected build() methods.
 */
public class MultipleCustomUIHud extends CustomUIHud {
    private static final HytaleLogger LOGGER = HytaleLogger.forEnclosingClass();
    private static Method BUILD_METHOD;

    static {
        try {
            BUILD_METHOD = CustomUIHud.class.getDeclaredMethod("build", UICommandBuilder.class);
            BUILD_METHOD.setAccessible(true);
        } catch (NoSuchMethodException e) {
            BUILD_METHOD = null;
            LOGGER.at(Level.SEVERE).log("Could not find method 'build' in CustomUIHud");
            LOGGER.at(Level.SEVERE).log(e.getMessage());
        }
    }

    private final HashMap<String, CustomUIHud> customHuds;

    public MultipleCustomUIHud(@NonNullDecl PlayerRef playerRef, HashMap<String, CustomUIHud> customHuds) {
        super(playerRef);
        this.customHuds = customHuds;
    }

    @Override
    protected void build(@NonNullDecl UICommandBuilder uiCommandBuilder) {
        for (String key : customHuds.keySet()) {
            var hud = customHuds.get(key);
            try {
                if (BUILD_METHOD != null) {
                    BUILD_METHOD.invoke(hud, uiCommandBuilder);
                }
            } catch (IllegalAccessException | InvocationTargetException e) {
                LOGGER.at(Level.SEVERE).log("Failed to build HUD '" + key + "': " + e.getMessage());
                throw new RuntimeException(e);
            }
        }
    }

    public HashMap<String, CustomUIHud> getCustomHuds() {
        return customHuds;
    }
}
