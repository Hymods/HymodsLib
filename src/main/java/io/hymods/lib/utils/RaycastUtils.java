package io.hymods.lib.utils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.function.BiConsumer;

import com.hypixel.hytale.component.ArchetypeChunk;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.component.query.Query;
import com.hypixel.hytale.math.vector.Vector3d;
import com.hypixel.hytale.server.core.asset.type.blocktype.config.BlockType;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.modules.entity.component.TransformComponent;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.npc.entities.NPCEntity;

import io.hymods.lib.data.RaycastResult;

/**
 * Utility class for raycasting and line-of-sight operations
 */
public class RaycastUtils {
    private static final double DEFAULT_STEP = 0.1;
    private static final double ENTITY_HIT_RADIUS = 1.5;

    /**
     * Performs a raycast from an origin point in a direction
     * 
     * @param  world       The world to raycast in
     * @param  origin      The starting position
     * @param  direction   The normalized direction vector
     * @param  maxDistance Maximum distance to check
     * 
     * @return             The raycast result
     */
    public static RaycastResult raycast(World world, Vector3d origin, Vector3d direction, double maxDistance) {
        return raycast(world, origin, direction, maxDistance, true, true, null);
    }

    /**
     * Performs a raycast with options for what to check
     * 
     * @param  world         The world to raycast in
     * @param  origin        The starting position
     * @param  direction     The normalized direction vector
     * @param  maxDistance   Maximum distance to check
     * @param  checkBlocks   Whether to check for block collisions
     * @param  checkEntities Whether to check for entity collisions
     * 
     * @return               The raycast result
     */
    public static RaycastResult raycast(World world, Vector3d origin, Vector3d direction, double maxDistance, boolean checkBlocks, boolean checkEntities) {
        return raycast(world, origin, direction, maxDistance, checkBlocks, checkEntities, null);
    }

    /**
     * Performs a raycast with options for what to check and entity exclusion
     * 
     * @param  world         The world to raycast in
     * @param  origin        The starting position
     * @param  direction     The normalized direction vector
     * @param  maxDistance   Maximum distance to check
     * @param  checkBlocks   Whether to check for block collisions
     * @param  checkEntities Whether to check for entity collisions
     * @param  excludeEntity Entity to exclude from intersection checks (e.g., the source entity)
     * 
     * @return               The raycast result
     */
    public static RaycastResult raycast(World world, Vector3d origin, Vector3d direction, double maxDistance, boolean checkBlocks, boolean checkEntities, Ref<EntityStore> excludeEntity) {
        Store<EntityStore> store = world.getEntityStore().getStore();

        // Normalize direction if not already
        Vector3d normalizedDir = direction.normalize();

        // Check for entity hits if enabled
        if (checkEntities) {
            RaycastResult entityHit = checkEntityIntersection(store, origin, normalizedDir, maxDistance, excludeEntity);
            if (entityHit != null && entityHit.isHit()) {
                // If we hit an entity and aren't checking blocks, return immediately
                if (!checkBlocks) {
                    return entityHit;
                }

                // Otherwise, we need to check if a block is closer
                RaycastResult blockHit = checkBlockIntersection(world, origin, normalizedDir, entityHit.distance());
                if (blockHit != null && blockHit.isHit()) {
                    return blockHit;
                }

                return entityHit;
            }
        }

        // Check for block hits if enabled
        if (checkBlocks) {
            return checkBlockIntersection(world, origin, normalizedDir, maxDistance);
        }

        return RaycastResult.MISS;
    }

    /**
     * Performs a raycast from a player's eye position
     * 
     * @param  playerRef   The player reference
     * @param  store       The entity store
     * @param  world       The world
     * @param  maxDistance Maximum distance to check
     * 
     * @return             The raycast result
     */
    public static RaycastResult raycastFromPlayer(Ref<EntityStore> playerRef, Store<EntityStore> store, World world, double maxDistance) {
        TransformComponent transform = store.getComponent(playerRef, TransformComponent.getComponentType());
        if (transform == null) {
            return RaycastResult.MISS;
        }

        Vector3d position = transform.getPosition();
        Vector3d eyePos = new Vector3d(position.getX(), position.getY() + 1.6, position.getZ());

        Vector3d direction = PlayerUtils.getPlayerLookDirection(playerRef, store);
        if (direction == null) {
            return RaycastResult.MISS;
        }

        // Exclude the player themselves from entity intersection checks
        return raycast(world, eyePos, direction, maxDistance, true, true, playerRef);
    }

    /**
     * Checks for block intersection along a ray
     */
    private static RaycastResult checkBlockIntersection(World world, Vector3d origin, Vector3d direction, double maxDistance) {
        double stepSize = DEFAULT_STEP;
        int steps = (int) (maxDistance / stepSize);

        for (int i = 1; i <= steps; i++) {
            double dist = i * stepSize;
            Vector3d checkPos = new Vector3d(
                origin.getX() + direction.getX() * dist,
                origin.getY() + direction.getY() * dist,
                origin.getZ() + direction.getZ() * dist
            );

            int blockX = (int) Math.floor(checkPos.getX());
            int blockY = (int) Math.floor(checkPos.getY());
            int blockZ = (int) Math.floor(checkPos.getZ());

            BlockType block = world.getBlockType(blockX, blockY, blockZ);
            if (block != null && !isPassableBlock(block)) {
                Vector3d blockPos = new Vector3d(blockX, blockY, blockZ);
                return new RaycastResult(checkPos, dist, block, blockPos);
            }
        }

        return RaycastResult.MISS;
    }

    /**
     * Approximate entity center height offset
     */
    private static final double ENTITY_CENTER_HEIGHT = 1.0;

    /**
     * Checks for entity intersection along a ray.
     * Mirrors the working pattern from the original CheckBlock implementation.
     * 
     * @param  store         The entity store
     * @param  origin        The ray origin (typically eye position)
     * @param  direction     The ray direction (normalized)
     * @param  maxDistance   Maximum distance to check
     * @param  excludeEntity Entity to exclude from checks (can be null)
     * 
     * @return               The raycast result, or MISS if nothing hit
     */
    @SuppressWarnings("removal")
    private static RaycastResult checkEntityIntersection(Store<EntityStore> store, Vector3d origin, Vector3d direction, double maxDistance, Ref<EntityStore> excludeEntity) {
        // Get the UUID of the entity to exclude BEFORE iteration
        // This matches the working pattern - get player UUID upfront
        final UUID excludeUUID;
        if (excludeEntity != null) {
            Player excludePlayer = store.getComponent(excludeEntity, Player.getComponentType());
            excludeUUID = excludePlayer != null ? excludePlayer.getUuid() : null;
        } else {
            excludeUUID = null;
        }

        // Result holder matching the working code pattern
        final RaycastResult[] $closestHit = new RaycastResult[1];
        final double[] $closestDistance = { maxDistance + 1 };

        final double eyeX = origin.getX();
        final double eyeY = origin.getY();
        final double eyeZ = origin.getZ();

        final double dirX = direction.getX();
        final double dirY = direction.getY();
        final double dirZ = direction.getZ();

        Query<EntityStore> query = TransformComponent.getComponentType();
        BiConsumer<ArchetypeChunk<EntityStore>, CommandBuffer<EntityStore>> checker = (archetypeChunk, commandBuffer) -> {
            for (int index = 0; index < archetypeChunk.size(); index++) {
                // Get entity ref and transform - matches working code order
                Ref<EntityStore> entityRef = archetypeChunk.getReferenceTo(index);
                TransformComponent entityTransform = archetypeChunk.getComponent(index, TransformComponent.getComponentType());
                if (entityTransform == null) {
                    continue;
                }

                // Skip if it's the player themselves - EXACTLY like working code
                Player entityPlayer = store.getComponent(entityRef, Player.getComponentType());
                if (entityPlayer != null && excludeUUID != null && entityPlayer.getUuid().equals(excludeUUID)) {
                    continue;
                }

                Vector3d entityPos = entityTransform.getPosition();
                double entityCenterY = entityPos.getY() + ENTITY_CENTER_HEIGHT; // Approximate entity center height

                // Calculate distance from eye to entity
                double dx = entityPos.getX() - eyeX;
                double dy = entityCenterY - eyeY;
                double dz = entityPos.getZ() - eyeZ;
                double distToEntity = Math.sqrt(dx * dx + dy * dy + dz * dz);

                if (distToEntity > maxDistance) {
                    continue;
                }

                // Check if entity is roughly in the direction we're looking
                double dot = dx * dirX + dy * dirY + dz * dirZ;
                if (dot < 0) {
                    continue; // Entity is behind us
                }

                // Point on ray closest to entity
                double projX = eyeX + dirX * dot;
                double projY = eyeY + dirY * dot;
                double projZ = eyeZ + dirZ * dot;

                // Perpendicular distance from entity to ray
                double perpDist = Math.sqrt(
                    Math.pow(entityPos.getX() - projX, 2) +
                    Math.pow(entityCenterY - projY, 2) +
                    Math.pow(entityPos.getZ() - projZ, 2));

                if (perpDist <= ENTITY_HIT_RADIUS && dot < $closestDistance[0]) {
                    // Get display name based on entity type - reuse entityPlayer we already fetched
                    String displayName;
                    if (entityPlayer != null) {
                        displayName = entityPlayer.getDisplayName();
                    } else {
                        NPCEntity npc = store.getComponent(entityRef, NPCEntity.getComponentType());
                        if (npc != null && npc.getRoleName() != null) {
                            displayName = npc.getRoleName();
                        } else {
                            displayName = "Entity";
                        }
                    }

                    Vector3d hitPos = new Vector3d(projX, projY, projZ);
                    $closestHit[0] = new RaycastResult(hitPos, dot, entityRef, displayName);
                    $closestDistance[0] = dot;
                }
            }
        };

        store.forEachChunk(query, checker);
        return $closestHit[0] != null ? $closestHit[0] : RaycastResult.MISS;
    }

    /**
     * Gets the name of an entity
     */
    private static String getEntityName(Ref<EntityStore> entityRef, Store<EntityStore> store) {
        Player player = store.getComponent(entityRef, Player.getComponentType());
        if (player != null) {
            return player.getDisplayName();
        }

        NPCEntity npc = store.getComponent(entityRef, NPCEntity.getComponentType());
        if (npc != null && npc.getRoleName() != null) {
            return npc.getRoleName();
        }

        return "Entity";
    }

    /**
     * Checks if a block is passable (air, water, etc)
     */
    private static boolean isPassableBlock(BlockType block) {
        if (block == null) {
            return true;
        }

        String blockId = block.getId();
        if (blockId == null || blockId.isEmpty()) {
            return true;
        }

        String lowerBlockId = blockId.toLowerCase();
        return lowerBlockId.contains("air") ||
            lowerBlockId.contains("void") ||
            lowerBlockId.contains("empty");
    }

    /**
     * Checks if there's line of sight between two positions
     * 
     * @param  world The world to check in
     * @param  from  Starting position
     * @param  to    Target position
     * 
     * @return       true if there's clear line of sight
     */
    public static boolean hasLineOfSight(World world, Vector3d from, Vector3d to) {
        Vector3d direction = to.subtract(from).normalize();
        double distance = direction.length();

        RaycastResult result = raycast(world, from, direction, distance, true, false);
        return !result.isHit() || result.distance() >= distance;
    }

    /**
     * Checks if there's line of sight between two entities
     * 
     * @param  entity1 First entity reference
     * @param  entity2 Second entity reference
     * @param  store   The entity store
     * @param  world   The world
     * 
     * @return         true if there's clear line of sight
     */
    public static boolean hasLineOfSightBetweenEntities(Ref<EntityStore> entity1, Ref<EntityStore> entity2, Store<EntityStore> store, World world) {
        TransformComponent transform1 = store.getComponent(entity1, TransformComponent.getComponentType());
        TransformComponent transform2 = store.getComponent(entity2, TransformComponent.getComponentType());

        if (transform1 == null || transform2 == null) {
            return false;
        }

        // Use eye height for better accuracy
        Vector3d pos1 = transform1.getPosition().add(0, 1.6, 0);
        Vector3d pos2 = transform2.getPosition().add(0, 1.6, 0);

        return hasLineOfSight(world, pos1, pos2);
    }

    /**
     * Finds the first solid block along a ray
     * 
     * @param  world       The world to check in
     * @param  origin      Starting position
     * @param  direction   Direction to check
     * @param  maxDistance Maximum distance
     * 
     * @return             The position of the first solid block, or null if none
     *                     found
     */
    public static Vector3d findFirstSolidBlock(World world, Vector3d origin, Vector3d direction, double maxDistance) {
        RaycastResult result = raycast(world, origin, direction, maxDistance, true, false);
        return result.isBlockHit() ? result.blockPosition() : null;
    }

    /**
     * Performs a cone-shaped check for entities
     * 
     * @param  store       The entity store
     * @param  origin      Starting position
     * @param  direction   Direction of the cone
     * @param  maxDistance Maximum distance
     * @param  angle       Cone angle in degrees
     * 
     * @return             List of entity references within the cone
     */
    public static List<Ref<EntityStore>> getEntitiesInCone(Store<EntityStore> store, Vector3d origin, Vector3d direction, double maxDistance, double angle) {
        return getEntitiesInCone(store, origin, direction, maxDistance, angle, null);
    }

    /**
     * Performs a cone-shaped check for entities with exclusion support
     * 
     * @param  store         The entity store
     * @param  origin        Starting position
     * @param  direction     Direction of the cone
     * @param  maxDistance   Maximum distance
     * @param  angle         Cone angle in degrees
     * @param  excludeEntity Entity to exclude from checks (e.g., the source entity)
     * 
     * @return               List of entity references within the cone
     */
    @SuppressWarnings("removal")
    public static List<Ref<EntityStore>> getEntitiesInCone(Store<EntityStore> store, Vector3d origin, Vector3d direction, double maxDistance, double angle, Ref<EntityStore> excludeEntity) {
        List<Ref<EntityStore>> entities = new ArrayList<>();
        double angleRad = Math.toRadians(angle);
        double cosAngle = Math.cos(angleRad);

        // Get the UUID of the entity to exclude - same pattern as working code
        final UUID excludeUUID;
        if (excludeEntity != null) {
            Player excludePlayer = store.getComponent(excludeEntity, Player.getComponentType());
            excludeUUID = excludePlayer != null ? excludePlayer.getUuid() : null;
        } else {
            excludeUUID = null;
        }

        Query<EntityStore> query = TransformComponent.getComponentType();
        BiConsumer<ArchetypeChunk<EntityStore>, CommandBuffer<EntityStore>> collector = (archetypeChunk, commandBuffer) -> {
            for (int index = 0; index < archetypeChunk.size(); index++) {
                Ref<EntityStore> entityRef = archetypeChunk.getReferenceTo(index);
                
                TransformComponent transform = archetypeChunk.getComponent(index, TransformComponent.getComponentType());
                if (transform == null) {
                    continue;
                }

                // Skip if it's the player themselves - exactly like working code
                Player entityPlayer = store.getComponent(entityRef, Player.getComponentType());
                if (entityPlayer != null && excludeUUID != null && entityPlayer.getUuid().equals(excludeUUID)) {
                    continue;
                }

                Vector3d entityPos = transform.getPosition();
                Vector3d toEntity = entityPos.subtract(origin);
                double distance = toEntity.length();

                if (distance > maxDistance) {
                    continue;
                }

                Vector3d normalizedToEntity = toEntity.normalize();
                double dot = normalizedToEntity.dot(direction);

                if (dot >= cosAngle) {
                    entities.add(entityRef);
                }
            }
        };

        store.forEachChunk(query, collector);
        return Collections.unmodifiableList(entities);
    }

}
