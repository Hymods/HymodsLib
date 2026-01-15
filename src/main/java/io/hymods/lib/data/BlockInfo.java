package io.hymods.lib.data;

import com.hypixel.hytale.math.vector.Vector3i;
import com.hypixel.hytale.server.core.asset.type.blocktype.config.BlockType;

import io.hymods.lib.utils.WorldUtils;

/**
 * Information about a block
 */
public record BlockInfo(
    /**
     * The position of the block
     */
    Vector3i position,
    /**
     * The type of the block
     */
    BlockType type
) {

    /**
     * @return true if the block is air
     */
    public boolean isAir() {
        return WorldUtils.isAirBlock(this.type);
    }

    /**
     * @return true if the block is solid
     */
    public boolean isSolid() {
        return WorldUtils.isSolidBlock(this.type);
    }

}
