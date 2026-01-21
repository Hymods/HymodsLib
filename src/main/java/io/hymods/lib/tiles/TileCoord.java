package io.hymods.lib.tiles;

/**
 * Represents a tile coordinate in world space.
 * Tiles are fixed regions of the world, each covering TILE_SIZE_CHUNKS chunks.
 * 
 * @param x Tile X coordinate in world space
 * @param z Tile Z coordinate in world space
 */
public record TileCoord(int x, int z) {
    
    // Hytale uses 32-block chunks, not 16 like Minecraft
    public static final int BLOCKS_PER_CHUNK = 32;
    
    /**
     * Calculate the tile coordinate that contains a given chunk coordinate.
     * 
     * @param chunkX Chunk X coordinate
     * @param chunkZ Chunk Z coordinate
     * @param tileSizeChunks Size of each tile in chunks
     * @return The tile coordinate containing this chunk
     */
    public static TileCoord fromChunk(int chunkX, int chunkZ, int tileSizeChunks) {
        // Use floor division to handle negative coordinates correctly
        int tileX = Math.floorDiv(chunkX, tileSizeChunks);
        int tileZ = Math.floorDiv(chunkZ, tileSizeChunks);
        return new TileCoord(tileX, tileZ);
    }
    
    /**
     * Calculate the tile coordinate that contains a given block coordinate.
     * 
     * @param blockX Block X coordinate
     * @param blockZ Block Z coordinate
     * @param tileSizeChunks Size of each tile in chunks
     * @return The tile coordinate containing this block
     */
    public static TileCoord fromBlock(double blockX, double blockZ, int tileSizeChunks) {
        int tileSizeBlocks = tileSizeChunks * BLOCKS_PER_CHUNK;
        int tileX = (int) Math.floor(blockX / tileSizeBlocks);
        int tileZ = (int) Math.floor(blockZ / tileSizeBlocks);
        return new TileCoord(tileX, tileZ);
    }
    
    /**
     * Get the minimum chunk X coordinate covered by this tile.
     */
    public int minChunkX(int tileSizeChunks) {
        return x * tileSizeChunks;
    }
    
    /**
     * Get the minimum chunk Z coordinate covered by this tile.
     */
    public int minChunkZ(int tileSizeChunks) {
        return z * tileSizeChunks;
    }
    
    /**
     * Get the maximum chunk X coordinate covered by this tile (inclusive).
     */
    public int maxChunkX(int tileSizeChunks) {
        return (x + 1) * tileSizeChunks - 1;
    }
    
    /**
     * Get the maximum chunk Z coordinate covered by this tile (inclusive).
     */
    public int maxChunkZ(int tileSizeChunks) {
        return (z + 1) * tileSizeChunks - 1;
    }
    
    /**
     * Get the center block X coordinate of this tile.
     */
    public double centerBlockX(int tileSizeChunks) {
        int tileSizeBlocks = tileSizeChunks * BLOCKS_PER_CHUNK;
        return (x + 0.5) * tileSizeBlocks;
    }
    
    /**
     * Get the center block Z coordinate of this tile.
     */
    public double centerBlockZ(int tileSizeChunks) {
        int tileSizeBlocks = tileSizeChunks * BLOCKS_PER_CHUNK;
        return (z + 0.5) * tileSizeBlocks;
    }
    
    /**
     * Get the minimum block X coordinate of this tile.
     */
    public double minBlockX(int tileSizeChunks) {
        return x * tileSizeChunks * BLOCKS_PER_CHUNK;
    }
    
    /**
     * Get the minimum block Z coordinate of this tile.
     */
    public double minBlockZ(int tileSizeChunks) {
        return z * tileSizeChunks * BLOCKS_PER_CHUNK;
    }
    
    /**
     * Calculate offset from another tile coordinate.
     * 
     * @param other The other tile coordinate
     * @return A new TileCoord representing the offset (this - other)
     */
    public TileCoord offset(TileCoord other) {
        return new TileCoord(this.x - other.x, this.z - other.z);
    }
    
    /**
     * Add an offset to this tile coordinate.
     * 
     * @param dx X offset
     * @param dz Z offset
     * @return A new TileCoord with the offset applied
     */
    public TileCoord add(int dx, int dz) {
        return new TileCoord(this.x + dx, this.z + dz);
    }
    
    @Override
    public String toString() {
        return "TileCoord(" + x + ", " + z + ")";
    }
}
