package io.hymods.lib.tiles;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Manages a 3x3 grid of map tiles with wrapping support.
 * 
 * The grid uses 9 "slots" (indices 0-8) which map to asset paths Map0.png through Map8.png.
 * Each slot can hold a tile from any world position. When the player moves, slots are
 * reassigned to new world positions, reusing slots that would otherwise go off-screen.
 * 
 * Grid layout (slot indices):
 *   0 | 1 | 2
 *   ---------
 *   3 | 4 | 5
 *   ---------
 *   6 | 7 | 8
 * 
 * Slot 4 is always the "center" slot where the player currently is.
 */
public class TileGridManager {
    public static final int GRID_SIZE = 3;
    public static final int TOTAL_SLOTS = GRID_SIZE * GRID_SIZE;
    public static final int CENTER_SLOT = 4;
    
    // Size of each tile in chunks
    private final int tileSizeChunks;
    
    // Size of each tile in pixels (computed from generation)
    private int tilePixelSize = 0;
    
    // Maps slot index (0-8) to the world tile coordinate it currently holds
    private final TileCoord[] slotTiles = new TileCoord[TOTAL_SLOTS];
    
    // The current center tile in world coordinates (where the player is)
    private TileCoord centerTile = null;
    
    // Track which slots have been initialized with image data
    private final boolean[] slotInitialized = new boolean[TOTAL_SLOTS];
    
    public TileGridManager(int tileSizeChunks) {
        this.tileSizeChunks = tileSizeChunks;
    }
    
    /**
     * Initialize or update the grid centered on a new tile.
     * Returns information about which tiles need to be generated.
     * 
     * @param newCenter The new center tile coordinate
     * @return Result containing tiles to generate and slot assignments
     */
    public GridUpdateResult updateCenter(TileCoord newCenter) {
        if (centerTile == null) {
            // First initialization - assign all slots
            return initializeGrid(newCenter);
        }
        
        if (newCenter.equals(centerTile)) {
            // No change needed
            return new GridUpdateResult(List.of(), Map.of());
        }
        
        // Calculate which tiles we need in the new configuration
        Set<TileCoord> neededTiles = getTilesAroundCenter(newCenter);
        
        // Find which tiles we already have and can reuse
        Map<TileCoord, Integer> existingTileSlots = new HashMap<>();
        for (int slot = 0; slot < TOTAL_SLOTS; slot++) {
            if (slotTiles[slot] != null && slotInitialized[slot]) {
                existingTileSlots.put(slotTiles[slot], slot);
            }
        }
        
        // Determine which tiles to generate (needed but not existing)
        Set<TileCoord> tilesToGenerate = new HashSet<>(neededTiles);
        tilesToGenerate.removeAll(existingTileSlots.keySet());
        
        // Find available slots (those holding tiles we no longer need)
        List<Integer> availableSlots = new ArrayList<>();
        for (int slot = 0; slot < TOTAL_SLOTS; slot++) {
            TileCoord currentTile = slotTiles[slot];
            if (currentTile == null || !neededTiles.contains(currentTile)) {
                availableSlots.add(slot);
            }
        }
        
        // Assign new tiles to available slots
        Map<TileCoord, Integer> newAssignments = new HashMap<>();
        var tilesToGenerateList = new ArrayList<>(tilesToGenerate);
        for (int i = 0; i < tilesToGenerateList.size(); i++) {
            TileCoord tile = tilesToGenerateList.get(i);
            int slot = availableSlots.get(i);
            newAssignments.put(tile, slot);
            slotTiles[slot] = tile;
            slotInitialized[slot] = false; // Will be initialized after generation
        }
        
        // Update center
        this.centerTile = newCenter;
        
        return new GridUpdateResult(tilesToGenerateList, newAssignments);
    }
    
    /**
     * Initialize the grid with all 9 tiles around a center.
     */
    private GridUpdateResult initializeGrid(TileCoord center) {
        this.centerTile = center;
        
        List<TileCoord> tilesToGenerate = new ArrayList<>();
        Map<TileCoord, Integer> assignments = new HashMap<>();
        
        int slot = 0;
        for (int dz = -1; dz <= 1; dz++) {
            for (int dx = -1; dx <= 1; dx++) {
                TileCoord tile = center.add(dx, dz);
                slotTiles[slot] = tile;
                slotInitialized[slot] = false;
                tilesToGenerate.add(tile);
                assignments.put(tile, slot);
                slot++;
            }
        }
        
        return new GridUpdateResult(tilesToGenerate, assignments);
    }
    
    /**
     * Get the set of tiles needed around a center tile.
     */
    private Set<TileCoord> getTilesAroundCenter(TileCoord center) {
        Set<TileCoord> tiles = new HashSet<>();
        for (int dz = -1; dz <= 1; dz++) {
            for (int dx = -1; dx <= 1; dx++) {
                tiles.add(center.add(dx, dz));
            }
        }
        return tiles;
    }
    
    /**
     * Mark a slot as initialized (has valid image data).
     */
    public void markSlotInitialized(int slot) {
        if (slot >= 0 && slot < TOTAL_SLOTS) {
            slotInitialized[slot] = true;
        }
    }
    
    /**
     * Check if the grid has been initialized.
     */
    public boolean isInitialized() {
        return centerTile != null;
    }
    
    /**
     * Get the current center tile.
     */
    public TileCoord getCenterTile() {
        return centerTile;
    }
    
    /**
     * Get the tile coordinate for a specific slot.
     */
    public TileCoord getTileAtSlot(int slot) {
        return slotTiles[slot];
    }
    
    /**
     * Find which slot holds a specific tile coordinate.
     * 
     * @param tile The tile coordinate to find
     * @return The slot index, or -1 if not found
     */
    public int findSlotForTile(TileCoord tile) {
        for (int slot = 0; slot < TOTAL_SLOTS; slot++) {
            if (tile.equals(slotTiles[slot])) {
                return slot;
            }
        }
        return -1;
    }
    
    /**
     * Calculate the pixel offset for a slot based on player position.
     * 
     * The offset positions each tile correctly in the viewport, accounting for:
     * 1. The tile's world position relative to the center tile
     * 2. The player's position within the current tile
     * 
     * @param slot The slot index (0-8)
     * @param playerBlockX Player's X position in blocks
     * @param playerBlockZ Player's Z position in blocks
     * @param viewportSize Size of the viewport in pixels
     * @return int[2] containing {offsetX, offsetZ} in pixels
     */
    public int[] calculateSlotOffset(int slot, double playerBlockX, double playerBlockZ, int viewportSize) {
        if (centerTile == null || tilePixelSize == 0) {
            return new int[]{0, 0};
        }
        
        TileCoord slotTile = slotTiles[slot];
        if (slotTile == null) {
            return new int[]{0, 0};
        }
        
        int tileSizeBlocks = tileSizeChunks * TileCoord.BLOCKS_PER_CHUNK;
        double pixelsPerBlock = (double) tilePixelSize / tileSizeBlocks;
        
        // Calculate tile's position relative to center tile (in tiles)
        int tileOffsetX = slotTile.x() - centerTile.x();
        int tileOffsetZ = slotTile.z() - centerTile.z();
        
        // Convert to pixel offset (tile grid position)
        int baseTilePixelX = tileOffsetX * tilePixelSize;
        int baseTilePixelZ = tileOffsetZ * tilePixelSize;
        
        // Calculate player's offset within the center tile (in blocks from tile center)
        double centerTileMinX = centerTile.minBlockX(tileSizeChunks);
        double centerTileMinZ = centerTile.minBlockZ(tileSizeChunks);
        double playerOffsetInTileX = playerBlockX - centerTileMinX;
        double playerOffsetInTileZ = playerBlockZ - centerTileMinZ;
        
        // Convert player offset to pixels
        int playerPixelOffsetX = (int) (playerOffsetInTileX * pixelsPerBlock);
        int playerPixelOffsetZ = (int) (playerOffsetInTileZ * pixelsPerBlock);
        
        // Final offset: position tile, then shift everything to center player in viewport
        // The tile's top-left corner needs to be positioned such that the player appears at viewport center
        int viewportCenter = viewportSize / 2;
        int offsetX = baseTilePixelX - playerPixelOffsetX + viewportCenter;
        int offsetZ = baseTilePixelZ - playerPixelOffsetZ + viewportCenter;
        
        return new int[]{offsetX, offsetZ};
    }
    
    /**
     * Set the tile pixel size (determined after generation).
     */
    public void setTilePixelSize(int size) {
        this.tilePixelSize = size;
    }
    
    /**
     * Get the tile pixel size.
     */
    public int getTilePixelSize() {
        return tilePixelSize;
    }
    
    /**
     * Get the tile size in chunks.
     */
    public int getTileSizeChunks() {
        return tileSizeChunks;
    }
    
    /**
     * Result of a grid update operation.
     * 
     * @param tilesToGenerate List of tile coordinates that need to be generated
     * @param slotAssignments Map from tile coordinate to the slot it should be assigned to
     */
    public record GridUpdateResult(
        List<TileCoord> tilesToGenerate,
        Map<TileCoord, Integer> slotAssignments
    ) {
        public boolean needsGeneration() {
            return !tilesToGenerate.isEmpty();
        }
    }
}
