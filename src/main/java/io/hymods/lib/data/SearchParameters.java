package io.hymods.lib.data;

import com.hypixel.hytale.math.vector.Vector3d;

public record SearchParameters(
    Vector3d center,
    double radius,
    boolean includePlayer,
    boolean includeNPC,
    boolean includeLiving,
    String filterType
) {

    public SearchParameters(Vector3d center, double radius) {
        this(center, radius, true, true, true, null);
    }

    public SearchParameters(Vector3d center, double radius, boolean includePlayer, boolean includeNPC, boolean includeLiving, String filterType) {
        this.center = center;
        this.radius = radius;
        this.includePlayer = includePlayer;
        this.includeNPC = includeNPC;
        this.includeLiving = includeLiving;
        this.filterType = filterType;
    }

}
