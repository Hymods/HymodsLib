package io.hymods.lib.data;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.math.vector.Vector3d;
import com.hypixel.hytale.server.core.asset.type.blocktype.config.BlockType;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;

/**
 * Result of a raycast operation containing hit information
 */
public record RaycastResult(
    Vector3d hitPosition,
    double distance,

    // Block information if a block was hit
    BlockType blockType,
    Vector3d blockPosition,

    // Entity information if an entity was hit
    Ref<EntityStore> entityRef,
    String entityName,

    HitType hitType
) {

    /**
     * Constant representing a miss (no hit)
     */
    public static final RaycastResult MISS = new RaycastResult(null, -1, null, null, null, null, HitType.NONE);

    /**
     * Constructor for a block hit
     */
    public RaycastResult(Vector3d hitPosition, double distance, BlockType blockType, Vector3d blockPosition) {
        this(hitPosition, distance, blockType, blockPosition, null, null, HitType.BLOCK);
    }

    /**
     * Constructor for an entity hit
     */
    public RaycastResult(Vector3d hitPosition, double distance, Ref<EntityStore> entityRef, String entityName) {
        this(hitPosition, distance, null, null, entityRef, entityName, HitType.ENTITY);
    }

    /**
     * Checks if the raycast resulted in a miss
     */
    public boolean isMiss() {
        return this.hitType == HitType.NONE;
    }

    /**
     * Checks if the raycast resulted in a hit (either block or entity)
     */
    public boolean isHit() {
        return this.hitType != HitType.NONE;
    }

    /**
     * Checks if the raycast hit a block
     */
    public boolean isBlockHit() {
        return this.hitType == HitType.BLOCK;
    }

    /**
     * Checks if the raycast hit an entity
     */
    public boolean isEntityHit() {
        return this.hitType == HitType.ENTITY;
    }

    /**
     * Enum representing the type of hit
     */
    public static enum HitType {
        NONE,
        BLOCK,
        ENTITY
    }

}
