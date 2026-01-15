package io.hymods.lib.data;

import com.hypixel.hytale.math.vector.Vector3d;

public record SearchParameters(
    /**
     * The center point of the search.
     */
    Vector3d center,
    /**
     * The radius of the search.
     */
    double radius,
    /**
     * Whether to include player entities in the search.
     */
    boolean includePlayer,
    /**
     * Whether to include NPC entities in the search.
     */
    boolean includeNPC,
    /**
     * Whether to include living entities in the search.
     */
    boolean includeLiving,
    /**
     * The type of filter to apply (can be null).
     */
    String filterType
) {

    /**
     * Default constructor including all entity types and no filter.
     * 
     * @param center the center point of the search
     * @param radius the radius of the search
     */
    public SearchParameters(Vector3d center, double radius) {
        this(center, radius, true, true, true, null);
    }

    /**
     * Full constructor allowing customization of entity types and filter.
     * 
     * @param center        the center point of the search
     * @param radius        the radius of the search
     * @param includePlayer whether to include player entities
     * @param includeNPC    whether to include NPC entities
     * @param includeLiving whether to include living entities
     * @param filterType    the type of filter to apply (can be null)
     */
    public SearchParameters(Vector3d center, double radius, boolean includePlayer, boolean includeNPC, boolean includeLiving, String filterType) {
        this.center = center;
        this.radius = radius;
        this.includePlayer = includePlayer;
        this.includeNPC = includeNPC;
        this.includeLiving = includeLiving;
        this.filterType = filterType;
    }

}
