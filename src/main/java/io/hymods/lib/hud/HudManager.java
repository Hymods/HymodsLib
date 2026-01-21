package io.hymods.lib.hud;

import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.entity.entities.player.hud.CustomUIHud;
import com.hypixel.hytale.server.core.universe.PlayerRef;

import javax.annotation.Nonnull;
import java.util.HashMap;

/**
 * Utility class for managing multiple custom HUDs per player.
 * 
 * Since Hytale does not natively support showing multiple custom HUDs at once,
 * this manager wraps multiple HUDs into a single MultipleCustomUIHud that renders
 * them all simultaneously.
 * 
 * Usage:
 * <pre>
 * // Set a HUD with an identifier
 * HudManager.setCustomHud(player, playerRef, "mymod:hud", myCustomHud);
 * 
 * // Hide a specific HUD
 * HudManager.hideCustomHud(player, playerRef, "mymod:hud");
 * </pre>
 */
public class HudManager {
    
    /**
     * Sets or updates a custom HUD for a player with the given identifier.
     * 
     * If the player already has a MultipleCustomUIHud, the new HUD is added to it.
     * If the player has a different CustomUIHud, it's preserved as "Unknown".
     * If the player has no HUD, a new MultipleCustomUIHud is created.
     * 
     * @param player The player to set the HUD for
     * @param playerRef The player reference
     * @param hudIdentifier A unique identifier for this HUD (e.g., "minimap", "whatisthat")
     * @param customHud The custom HUD to add or update
     */
    public static void setCustomHud(@Nonnull Player player, @Nonnull PlayerRef playerRef, 
                                   @Nonnull String hudIdentifier, @Nonnull CustomUIHud customHud) {
        var currentCustomHud = player.getHudManager().getCustomHud();
        if (currentCustomHud instanceof MultipleCustomUIHud multipleCustomUIHud) {
            // Add or update the HUD in the existing MultipleCustomUIHud
            multipleCustomUIHud.getCustomHuds().put(hudIdentifier, customHud);
            player.getHudManager().setCustomHud(playerRef, multipleCustomUIHud);
            multipleCustomUIHud.show();
        } else {
            // Create a new MultipleCustomUIHud
            var huds = new HashMap<String, CustomUIHud>();
            huds.put(hudIdentifier, customHud);
            // Preserve existing HUD if present
            if (currentCustomHud != null) {
                huds.put("Unknown", currentCustomHud);
            }
            var multipleHud = new MultipleCustomUIHud(playerRef, huds);
            player.getHudManager().setCustomHud(playerRef, multipleHud);
            multipleHud.show();
        }
    }

    /**
     * Hides (removes) a custom HUD identified by the given identifier.
     * 
     * @param player The player to hide the HUD for
     * @param playerRef The player reference
     * @param hudIdentifier The identifier of the HUD to hide
     */
    public static void hideCustomHud(@Nonnull Player player, @Nonnull PlayerRef playerRef, 
                                    @Nonnull String hudIdentifier) {
        var currentCustomHud = player.getHudManager().getCustomHud();
        if (currentCustomHud instanceof MultipleCustomUIHud multipleCustomUIHud) {
            multipleCustomUIHud.getCustomHuds().remove(hudIdentifier);
            player.getHudManager().setCustomHud(playerRef, multipleCustomUIHud);
            multipleCustomUIHud.show();
        }
    }
    
    /**
     * Gets a specific HUD by identifier from the player's current HUD.
     * 
     * @param player The player to get the HUD from
     * @param hudIdentifier The identifier of the HUD to retrieve
     * @return The HUD if found, null otherwise
     */
    public static CustomUIHud getCustomHud(@Nonnull Player player, @Nonnull String hudIdentifier) {
        var currentCustomHud = player.getHudManager().getCustomHud();
        if (currentCustomHud instanceof MultipleCustomUIHud multipleCustomUIHud) {
            return multipleCustomUIHud.getCustomHuds().get(hudIdentifier);
        }
        return null;
    }
    
    /**
     * Refreshes the combined HUD display for a player.
     * 
     * Call this instead of calling show() directly on individual HUDs when using
     * the multiple HUD system. This ensures all HUDs are rendered together.
     * 
     * @param player The player whose HUD should be refreshed
     */
    public static void refreshHud(@Nonnull Player player) {
        var currentCustomHud = player.getHudManager().getCustomHud();
        if (currentCustomHud instanceof MultipleCustomUIHud multipleCustomUIHud) {
            multipleCustomUIHud.show();
        } else if (currentCustomHud != null) {
            currentCustomHud.show();
        }
    }
}
