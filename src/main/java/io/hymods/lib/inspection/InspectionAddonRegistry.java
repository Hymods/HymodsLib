package io.hymods.lib.inspection;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public final class InspectionAddonRegistry {
    private static final CopyOnWriteArrayList<InspectionAddon> ADDONS = new CopyOnWriteArrayList<>();

    private InspectionAddonRegistry() {
        // registry
    }

    public static void register(InspectionAddon addon) {
        if (addon == null) {
            return;
        }
        ADDONS.addIfAbsent(addon);
    }

    public static List<InspectionAddon> getAddons() {
        return List.copyOf(ADDONS);
    }
}
