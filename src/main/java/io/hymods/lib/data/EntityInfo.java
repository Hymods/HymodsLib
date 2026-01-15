package io.hymods.lib.data;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.math.vector.Vector3d;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;

public record EntityInfo(
    /**
     * A reference to the entity's store.
     */
    Ref<EntityStore> reference,
    /**
     * The name of the entity.
     */
    String name,
    /**
     * The type of the entity.
     */
    String type,
    /**
     * The position of the entity in the world.
     */
    Vector3d position,
    /**
     * The distance from a reference point to the entity.
     */
    double distance,
    /**
     * Whether the entity is a player.
     */
    boolean isPlayer,
    /**
     * Whether the entity is a non-player character (NPC).
     */
    boolean isNPC
) {

}
