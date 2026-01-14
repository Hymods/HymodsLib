package io.hymods.lib.data;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.math.vector.Vector3d;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;

public record EntityInfo(
    Ref<EntityStore> reference,
    String name,
    String type,
    Vector3d position,
    double distance,
    boolean isPlayer,
    boolean isNPC
) {

}
