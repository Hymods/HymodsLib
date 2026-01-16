package io.hymods.lib.inspection;

import java.util.List;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.asset.type.blocktype.config.BlockType;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;

import io.hymods.lib.data.InspectionLine;

/**
 * Optional extension point for adding custom lines to inspection results.
 *
 * Other mods can call {@link InspectionAddonRegistry#register(InspectionAddon)}
 * in their setup method.
 */
public interface InspectionAddon {

    String id();

    default void augmentBlock(BlockType blockType, List<InspectionLine> lines) {
        // optional
    }

    default void augmentEntity(Store<EntityStore> store, Ref<EntityStore> entityRef, List<InspectionLine> lines) {
        // optional
    }
}
