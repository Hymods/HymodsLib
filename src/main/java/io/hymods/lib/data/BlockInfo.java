package io.hymods.lib.data;

import com.hypixel.hytale.math.vector.Vector3i;
import com.hypixel.hytale.server.core.asset.type.blocktype.config.BlockType;

import io.hymods.lib.utils.WorldUtils;

/**
 * Information about a block
 */
public record BlockInfo(
    Vector3i position,
    BlockType type
) {

    public boolean isAir() {
        return WorldUtils.isAirBlock(type);
    }

    public boolean isSolid() {
        return WorldUtils.isSolidBlock(type);
    }

}
