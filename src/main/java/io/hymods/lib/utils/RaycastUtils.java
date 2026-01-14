package io.hymods.lib.utils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
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
        return raycast(world, origin, direction, maxDistance, true, true);
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
        Store<EntityStore> store = world.getEntityStore().getStore();

        // Normalize direction if not already
        Vector3d normalizedDir = direction.normalize();

        // Check for entity hits if enabled
        if (checkEntities) {
            RaycastResult entityHit = checkEntityIntersection(store, origin, normalizedDir, maxDistance);
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

        return raycast(world, eyePos, direction, maxDistance);
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
     * Checks for entity intersection along a ray
     */
    private static RaycastResult checkEntityIntersection(Store<EntityStore> store, Vector3d origin, Vector3d direction, double maxDistance) {
        final RaycastResult[] $closestHit = new RaycastResult[1];
        final double[] $closestDistance = {
                maxDistance + 1
        };

        BiConsumer<ArchetypeChunk<EntityStore>, CommandBuffer<EntityStore>> checker = (archetypeChunk, _) -> {
            for (int index = 0; index < archetypeChunk.size(); index++) {
                Ref<EntityStore> entityRef = archetypeChunk.getReferenceTo(index);
                TransformComponent transform = archetypeChunk.getComponent(index, TransformComponent.getComponentType());
                if (transform == null) {
                    continue;
                }

                Vector3d entityPos = transform.getPosition();

                // Calculate closest point on ray to entity
                Vector3d toEntity = entityPos.subtract(origin);
                double projection = toEntity.dot(direction);

                if (projection < 0 || projection > maxDistance) {
                    continue;
                }

                Vector3d closestPointOnRay = new Vector3d(
                    origin.getX() + direction.getX() * projection,
                    origin.getY() + direction.getY() * projection,
                    origin.getZ() + direction.getZ() * projection
                );

                double distToEntity = MathUtils.distance(closestPointOnRay, entityPos);

                if (distToEntity <= ENTITY_HIT_RADIUS && projection < $closestDistance[0]) {
                    String entityName = getEntityName(entityRef, store);
                    $closestHit[0] = new RaycastResult(closestPointOnRay, projection, entityRef, entityName);
                    $closestDistance[0] = projection;
                }
            }
        };

        store.forEachChunk(TransformComponent.getComponentType(), checker);
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
     * @return             Array of entity references within the cone
     */
    public static List<Ref<EntityStore>> getEntitiesInCone(Store<EntityStore> store, Vector3d origin, Vector3d direction, double maxDistance, double angle) {
        List<Ref<EntityStore>> entities = new ArrayList<>();
        double angleRad = Math.toRadians(angle);
        double cosAngle = Math.cos(angleRad);

        Query<EntityStore> query = TransformComponent.getComponentType();
        BiConsumer<ArchetypeChunk<EntityStore>, CommandBuffer<EntityStore>> collector = (archetypeChunk, _) -> {
            for (int index = 0; index < archetypeChunk.size(); index++) {
                Ref<EntityStore> entityRef = archetypeChunk.getReferenceTo(index);
                TransformComponent transform = archetypeChunk.getComponent(index, TransformComponent.getComponentType());
                if (transform == null) {
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
