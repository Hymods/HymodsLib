package io.hymods.lib.utils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Predicate;

import com.hypixel.hytale.math.util.ChunkUtil;
import com.hypixel.hytale.math.vector.Vector3d;
import com.hypixel.hytale.math.vector.Vector3i;
import com.hypixel.hytale.server.core.asset.type.blocktype.config.BlockType;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.chunk.WorldChunk;

import io.hymods.lib.data.BlockInfo;

/**
 * Utility class for world and block operations
 */
public class WorldUtils {

    private WorldUtils() {
        // Prevent instantiation
    }

    /**
     * Gets a block at a specific position
     * 
     * @param  world The world
     * @param  x     X coordinate
     * @param  y     Y coordinate
     * @param  z     Z coordinate
     * 
     * @return       The block type at the position
     */
    public static BlockType getBlock(World world, int x, int y, int z) {
        return world.getBlockType(x, y, z);
    }

    /**
     * Gets a block at a vector position
     * 
     * @param  world    The world
     * @param  position The position
     * 
     * @return          The block type at the position
     */
    public static BlockType getBlock(World world, Vector3d position) {
        return getBlock(
            world,
            (int) Math.floor(position.getX()),
            (int) Math.floor(position.getY()),
            (int) Math.floor(position.getZ())
        );
    }

    /**
     * Sets a block at a specific position Note: Block setting requires chunk access
     * via world.getChunk(index).setBlock()
     * 
     * @param  world     The world
     * @param  x         X coordinate
     * @param  y         Y coordinate
     * @param  z         Z coordinate
     * @param  blockType The block type to set
     * 
     * @return           true if the block was set successfully
     */
    public static boolean setBlock(World world, int x, int y, int z, BlockType blockType) {
        try {
            long chunkIndex = ChunkUtil.indexChunkFromBlock(x, z);
            WorldChunk chunk = world.getChunk(chunkIndex);
            if (chunk != null) {
                int blockId = BlockType.getAssetMap().getIndex(blockType.getId());
                return chunk.setBlock(x, y, z, blockId);
            }
            return false;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Gets all blocks in a cubic area
     * 
     * @param  world The world
     * @param  min   Minimum corner
     * @param  max   Maximum corner
     * 
     * @return       List of block positions and types
     */
    public static List<BlockInfo> getBlocksInArea(World world, Vector3i min, Vector3i max) {
        List<BlockInfo> blocks = new ArrayList<>();
        for (int x = min.getX(); x <= max.getX(); x++) {
            for (int y = min.getY(); y <= max.getY(); y++) {
                for (int z = min.getZ(); z <= max.getZ(); z++) {
                    BlockType block = world.getBlockType(x, y, z);
                    if (block != null) {
                        blocks.add(new BlockInfo(new Vector3i(x, y, z), block));
                    }
                }
            }
        }
        return Collections.unmodifiableList(blocks);
    }

    /**
     * Gets all blocks in a sphere
     * 
     * @param  world  The world
     * @param  center Center of the sphere
     * @param  radius Radius of the sphere
     * 
     * @return        List of block positions and types
     */
    public static List<BlockInfo> getBlocksInSphere(World world, Vector3d center, double radius) {
        List<BlockInfo> blocks = new ArrayList<>();
        double radiusSquared = radius * radius;

        int minX = (int) Math.floor(center.getX() - radius);
        int maxX = (int) Math.ceil(center.getX() + radius);
        int minY = (int) Math.floor(center.getY() - radius);
        int maxY = (int) Math.ceil(center.getY() + radius);
        int minZ = (int) Math.floor(center.getZ() - radius);
        int maxZ = (int) Math.ceil(center.getZ() + radius);

        for (int x = minX; x <= maxX; x++) {
            for (int y = minY; y <= maxY; y++) {
                for (int z = minZ; z <= maxZ; z++) {
                    double distSquared = MathUtils.distanceSquared(center, new Vector3d(x + 0.5, y + 0.5, z + 0.5));
                    if (distSquared <= radiusSquared) {
                        BlockType block = world.getBlockType(x, y, z);
                        if (block != null) {
                            blocks.add(new BlockInfo(new Vector3i(x, y, z), block));
                        }
                    }
                }
            }
        }

        return Collections.unmodifiableList(blocks);
    }

    /**
     * Finds the highest solid block at a position
     * 
     * @param  world The world
     * @param  x     X coordinate
     * @param  z     Z coordinate
     * @param  maxY  Maximum Y to check
     * 
     * @return       Y coordinate of highest solid block, or -1 if none found
     */
    public static int getHighestBlock(World world, int x, int z, int maxY) {
        for (int y = maxY; y >= 0; y--) {
            BlockType block = world.getBlockType(x, y, z);
            if (block != null && !isAirBlock(block)) {
                return y;
            }
        }
        return -1;
    }

    /**
     * Finds the first air block above a position
     * 
     * @param  world  The world
     * @param  x      X coordinate
     * @param  startY Starting Y coordinate
     * @param  z      Z coordinate
     * @param  maxY   Maximum Y to check
     * 
     * @return        Y coordinate of first air block, or -1 if none found
     */
    public static int getFirstAirBlock(World world, int x, int startY, int z, int maxY) {
        for (int y = startY; y <= maxY; y++) {
            BlockType block = world.getBlockType(x, y, z);
            if (block == null || isAirBlock(block)) {
                return y;
            }
        }
        return -1;
    }

    /**
     * Checks if a block is air or empty
     * 
     * @param  block The block type
     * 
     * @return       true if the block is air/empty
     */
    public static boolean isAirBlock(BlockType block) {
        if (block == null) {
            return true;
        }
        String id = block.getId();
        if (id == null || id.isEmpty()) {
            return true;
        }
        String lower = id.toLowerCase();
        return lower.contains("air") || lower.contains("void") || lower.contains("empty");
    }

    /**
     * Checks if a block is solid
     * 
     * @param  block The block type
     * 
     * @return       true if the block is solid
     */
    public static boolean isSolidBlock(BlockType block) {
        return block != null && !isAirBlock(block);
    }

    /**
     * Gets adjacent blocks
     * 
     * @param  world            The world
     * @param  x                X coordinate
     * @param  y                Y coordinate
     * @param  z                Z coordinate
     * @param  includeDiagonals Whether to include diagonal blocks
     * 
     * @return                  List of adjacent blocks
     */
    public static List<BlockInfo> getAdjacentBlocks(World world, int x, int y, int z, boolean includeDiagonals) {
        List<BlockInfo> adjacent = new ArrayList<>();

        // Direct neighbors (6)
        adjacent.add(new BlockInfo(new Vector3i(x + 1, y, z), world.getBlockType(x + 1, y, z)));
        adjacent.add(new BlockInfo(new Vector3i(x - 1, y, z), world.getBlockType(x - 1, y, z)));
        adjacent.add(new BlockInfo(new Vector3i(x, y + 1, z), world.getBlockType(x, y + 1, z)));
        adjacent.add(new BlockInfo(new Vector3i(x, y - 1, z), world.getBlockType(x, y - 1, z)));
        adjacent.add(new BlockInfo(new Vector3i(x, y, z + 1), world.getBlockType(x, y, z + 1)));
        adjacent.add(new BlockInfo(new Vector3i(x, y, z - 1), world.getBlockType(x, y, z - 1)));

        if (includeDiagonals) {
            // Edge neighbors (12)
            for (int dx = -1; dx <= 1; dx += 2) {
                for (int dy = -1; dy <= 1; dy += 2) {
                    adjacent.add(new BlockInfo(new Vector3i(x + dx, y + dy, z), world.getBlockType(x + dx, y + dy, z)));
                    adjacent.add(new BlockInfo(new Vector3i(x + dx, y, z + dy), world.getBlockType(x + dx, y, z + dy)));
                    adjacent.add(new BlockInfo(new Vector3i(x, y + dy, z + dy), world.getBlockType(x, y + dy, z + dy)));
                }
            }

            // Corner neighbors (8)
            for (int dx = -1; dx <= 1; dx += 2) {
                for (int dy = -1; dy <= 1; dy += 2) {
                    for (int dz = -1; dz <= 1; dz += 2) {
                        adjacent.add(
                            new BlockInfo(
                                new Vector3i(x + dx, y + dy, z + dz),
                                world.getBlockType(x + dx, y + dy, z + dz)
                            )
                        );
                    }
                }
            }
        }

        return Collections.unmodifiableList(adjacent);
    }

    /**
     * Finds blocks matching a predicate in an area
     * 
     * @param  world     The world
     * @param  center    Center position
     * @param  radius    Search radius
     * @param  predicate Block filter predicate
     * 
     * @return           List of matching blocks
     */
    public static List<BlockInfo> findBlocks(World world, Vector3d center, double radius, Predicate<BlockType> predicate) {
        List<BlockInfo> matches = new ArrayList<>();

        List<BlockInfo> allBlocks = getBlocksInSphere(world, center, radius);
        for (BlockInfo info : allBlocks) {
            if (predicate.test(info.type())) {
                matches.add(info);
            }
        }

        return Collections.unmodifiableList(matches);
    }

    /**
     * Gets the chunk index for a block position
     * 
     * @param  blockX Block X coordinate
     * @param  blockZ Block Z coordinate
     * 
     * @return        The chunk index
     */
    public static long getChunkIndexAtBlock(int blockX, int blockZ) {
        return ChunkUtil.indexChunkFromBlock(blockX, blockZ);
    }

    /**
     * Gets the chunk index from chunk coordinates
     * 
     * @param  chunkX Chunk X coordinate
     * @param  chunkZ Chunk Z coordinate
     * 
     * @return        The chunk index
     */
    public static long getChunkIndex(int chunkX, int chunkZ) {
        return ChunkUtil.indexChunk(chunkX, chunkZ);
    }

    /**
     * Checks if a chunk is loaded
     * 
     * @param  world  The world
     * @param  chunkX Chunk X coordinate
     * @param  chunkZ Chunk Z coordinate
     * 
     * @return        true if the chunk is loaded
     */
    public static boolean isChunkLoaded(World world, int chunkX, int chunkZ) {
        long chunkIndex = ChunkUtil.indexChunk(chunkX, chunkZ);
        return world.getChunk(chunkIndex) != null;
    }

}
