package io.hymods.lib.utils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.function.BiConsumer;
import java.util.function.Predicate;

import com.hypixel.hytale.component.ArchetypeChunk;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Component;
import com.hypixel.hytale.component.ComponentType;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.component.query.Query;
import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.math.vector.Vector3d;
import com.hypixel.hytale.math.vector.Vector3f;
import com.hypixel.hytale.server.core.entity.UUIDComponent;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.modules.entity.component.BoundingBox;
import com.hypixel.hytale.server.core.modules.entity.component.DisplayNameComponent;
import com.hypixel.hytale.server.core.modules.entity.component.TransformComponent;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.npc.entities.NPCEntity;

import io.hymods.lib.data.EntityInfo;
import io.hymods.lib.data.EntitySearchResult;
import io.hymods.lib.data.SearchParameters;

/**
 * Utility class for entity-related operations
 */
public class EntityUtils {
    private static final HytaleLogger LOGGER = HytaleLogger.forEnclosingClass();

    /**
     * Gets an entity by its UUID
     * 
     * @param  world The world to search in
     * @param  uuid  The entity's UUID
     * 
     * @return       The entity reference, or null if not found
     */
    @SuppressWarnings("unchecked")
    public static Ref<EntityStore> getEntityByUUID(World world, UUID uuid) {
        final Ref<EntityStore>[] $result = new Ref[1];

        BiConsumer<ArchetypeChunk<EntityStore>, CommandBuffer<EntityStore>> checker = (archetypeChunk, _) -> {
            for (int index = 0; index < archetypeChunk.size(); index++) {
                UUIDComponent uuidComp = archetypeChunk.getComponent(index, UUIDComponent.getComponentType());
                if (uuidComp != null && uuidComp.getUuid().equals(uuid)) {
                    $result[0] = archetypeChunk.getReferenceTo(index);
                    break;
                }
            }
        };

        world.getEntityStore().getStore().forEachChunk(UUIDComponent.getComponentType(), checker);
        return $result[0];
    }

    /**
     * Searches for entities within a radius
     * 
     * @param  world  The world to search in
     * @param  center The center position
     * @param  radius The search radius
     * 
     * @return        The search results
     */
    public static EntitySearchResult searchEntities(World world, Vector3d center, double radius) {
        final SearchParameters params = new SearchParameters(center, radius);
        return searchEntities(world, params);
    }

    /**
     * Searches for entities with specific parameters
     * 
     * @param  world  The world to search in
     * @param  params The search parameters
     * 
     * @return        The search results
     */
    public static EntitySearchResult searchEntities(World world, SearchParameters params) {
        final Store<EntityStore> store = world.getEntityStore().getStore();
        final EntitySearchResult result = new EntitySearchResult(params);
        final double radiusSquared = params.radius() * params.radius();

        BiConsumer<ArchetypeChunk<EntityStore>, CommandBuffer<EntityStore>> collector = (archetypeChunk, _) -> {
            for (int index = 0; index < archetypeChunk.size(); index++) {
                Ref<EntityStore> entityRef = archetypeChunk.getReferenceTo(index);
                TransformComponent transform = archetypeChunk.getComponent(index, TransformComponent.getComponentType());
                if (transform == null) {
                    continue;
                }

                Vector3d entityPos = transform.getPosition();
                double distSquared = MathUtils.distanceSquared(entityPos, params.center());

                if (distSquared <= radiusSquared) {
                    // Check entity type
                    Player player = store.getComponent(entityRef, Player.getComponentType());
                    NPCEntity npc = store.getComponent(entityRef, NPCEntity.getComponentType());

                    boolean isPlayer = player != null;
                    boolean isNPC = npc != null;
                    // Consider entities with health stats as living
                    boolean isLiving = isPlayer || isNPC;

                    // Apply filters
                    if (!params.includePlayer() && isPlayer) {
                        continue;
                    }
                    if (!params.includeNPC() && isNPC) {
                        continue;
                    }
                    if (!params.includeLiving() && !isLiving) {
                        continue;
                    }

                    String name = "";
                    String type = "entity";

                    if (isPlayer) {
                        name = player.getDisplayName();
                        type = "player";
                    } else if (isNPC) {
                        name = npc.getRoleName() != null ? npc.getRoleName() : "NPC";
                        type = npc.getRoleName() != null ? npc.getRoleName() : "npc";
                    }

                    if (params.filterType() != null && !type.equalsIgnoreCase(params.filterType())) {
                        continue;
                    }

                    double distance = Math.sqrt(distSquared);
                    EntityInfo info = new EntityInfo(
                        entityRef, name, type, entityPos, distance, isPlayer, isNPC
                    );
                    result.addEntity(info);
                }
            }
        };

        store.forEachChunk(TransformComponent.getComponentType(), collector);
        return result;
    }

    /**
     * Gets all entities of a specific component type
     * 
     * @param  world         The world to search in
     * @param  componentType The component type to filter by
     * 
     * @return               List of entity references
     */
    public static <T extends Component<EntityStore>> List<Ref<EntityStore>> getEntitiesWithComponent(World world, ComponentType<EntityStore, T> componentType) {
        final List<Ref<EntityStore>> results = new ArrayList<>();

        BiConsumer<ArchetypeChunk<EntityStore>, CommandBuffer<EntityStore>> collector = (archetypeChunk, _) -> {
            for (int index = 0; index < archetypeChunk.size(); index++) {
                if (archetypeChunk.getComponent(index, componentType) != null) {
                    results.add(archetypeChunk.getReferenceTo(index));
                }
            }
        };

        world.getEntityStore().getStore().forEachChunk(componentType, collector);
        return Collections.unmodifiableList(results);
    }

    /**
     * Gets the position of an entity
     * 
     * @param  ref   The entity reference
     * @param  store The entity store
     * 
     * @return       The entity's position, or null if not found
     */
    public static Vector3d getEntityPosition(Ref<EntityStore> ref, Store<EntityStore> store) {
        if (ref == null || !ref.isValid()) {
            return null;
        }

        TransformComponent transform = store.getComponent(ref, TransformComponent.getComponentType());
        return transform != null ? transform.getPosition() : null;
    }

    /**
     * Gets the rotation of an entity
     * 
     * @param  ref   The entity reference
     * @param  store The entity store
     * 
     * @return       The entity's rotation, or null if not found
     */
    public static Vector3f getEntityRotation(Ref<EntityStore> ref, Store<EntityStore> store) {
        if (ref == null || !ref.isValid()) {
            return null;
        }

        TransformComponent transform = store.getComponent(ref, TransformComponent.getComponentType());
        return transform != null ? transform.getRotation() : null;
    }

    /**
     * Gets the velocity of an entity Note: Velocity data may not be available for
     * all entities
     * 
     * @param  ref   The entity reference
     * @param  store The entity store
     * 
     * @return       The entity's velocity, or null if not found
     */
    public static Vector3d getEntityVelocity(Ref<EntityStore> ref, Store<EntityStore> store) {
        // Velocity component access depends on the specific entity type
        // This is a placeholder - actual implementation may vary based on Hytale API
        LOGGER.atWarning().log("FIXME: getEntityVelocity is not implemented");
        return new Vector3d(0, 0, 0);
    }

    /**
     * Gets the display name of an entity
     * 
     * @param  ref   The entity reference
     * @param  store The entity store
     * 
     * @return       The entity's display name, or "Unknown" if not found
     */
    public static String getEntityDisplayName(Ref<EntityStore> ref, Store<EntityStore> store) {
        if (ref == null || !ref.isValid()) {
            return "Unknown";
        }

        // Check if it's a player
        Player player = store.getComponent(ref, Player.getComponentType());
        if (player != null) {
            return player.getDisplayName();
        }

        // Check if it's an NPC
        NPCEntity npc = store.getComponent(ref, NPCEntity.getComponentType());
        if (npc != null && npc.getRoleName() != null) {
            return npc.getRoleName();
        }

        // Check for display name component
        DisplayNameComponent displayName = store.getComponent(ref, DisplayNameComponent.getComponentType());
        if (displayName != null && displayName.getDisplayName() != null) {
            return displayName.getDisplayName().toString();
        }

        return "Entity";
    }

    /**
     * Gets the bounding box of an entity
     * 
     * @param  ref   The entity reference
     * @param  store The entity store
     * 
     * @return       The entity's bounding box, or null if not found
     */
    public static BoundingBox getEntityBoundingBox(Ref<EntityStore> ref, Store<EntityStore> store) {
        if (ref == null || !ref.isValid()) {
            return null;
        }
        return store.getComponent(ref, BoundingBox.getComponentType());
    }

    /**
     * Checks if an entity is on the ground Note: This is a simplified
     * implementation - actual ground check may require accessing the entity's
     * transform and checking blocks below
     * 
     * @param  ref   The entity reference
     * @param  store The entity store
     * 
     * @return       true if the entity appears to be on ground based on position
     */
    public static boolean isOnGround(Ref<EntityStore> ref, Store<EntityStore> store) {
        // Movement state checking would require the specific component type
        // This is a placeholder that returns false by default
        // Actual implementation depends on available Hytale movement state APIs
        LOGGER.atWarning().log("FIXME: isOnGround is not implemented");
        return ref != null && ref.isValid();
    }

    /**
     * Checks if an entity is in water Note: This is a simplified implementation -
     * actual water check may require accessing the world and checking the block at
     * entity position
     * 
     * @param  ref   The entity reference
     * @param  store The entity store
     * 
     * @return       true if the entity appears to be in water
     */
    public static boolean isInWater(Ref<EntityStore> ref, Store<EntityStore> store) {
        // Fluid state checking would require the specific component type
        // This is a placeholder that returns false by default
        // Actual implementation depends on available Hytale movement state APIs
        LOGGER.atWarning().log("FIXME: isInWater is not implemented");
        return false;
    }

    /**
     * Gets the distance between two entities
     * 
     * @param  ref1  First entity reference
     * @param  ref2  Second entity reference
     * @param  store The entity store
     * 
     * @return       The distance between entities, or -1 if either is invalid
     */
    public static double getDistanceBetween(Ref<EntityStore> ref1, Ref<EntityStore> ref2, Store<EntityStore> store) {
        Vector3d pos1 = getEntityPosition(ref1, store);
        Vector3d pos2 = getEntityPosition(ref2, store);

        if (pos1 == null || pos2 == null) {
            return -1;
        }
        return MathUtils.distance(pos1, pos2);
    }

    /**
     * Checks if an entity is a player
     * 
     * @param  ref   The entity reference
     * @param  store The entity store
     * 
     * @return       true if the entity is a player
     */
    public static boolean isPlayer(Ref<EntityStore> ref, Store<EntityStore> store) {
        if (ref == null || !ref.isValid()) {
            return false;
        }
        return store.getComponent(ref, Player.getComponentType()) != null;
    }

    /**
     * Checks if an entity is an NPC
     * 
     * @param  ref   The entity reference
     * @param  store The entity store
     * 
     * @return       true if the entity is an NPC
     */
    public static boolean isNPC(Ref<EntityStore> ref, Store<EntityStore> store) {
        if (ref == null || !ref.isValid()) {
            return false;
        }
        return store.getComponent(ref, NPCEntity.getComponentType()) != null;
    }

    /**
     * Checks if an entity is a living entity (has health/can take damage)
     * 
     * @param  ref   The entity reference
     * @param  store The entity store
     * 
     * @return       true if the entity is living
     */
    public static boolean isLivingEntity(Ref<EntityStore> ref, Store<EntityStore> store) {
        if (ref == null || !ref.isValid()) {
            return false;
        }
        // Living entities are Players or NPCs
        return isPlayer(ref, store) || isNPC(ref, store);
    }

    /**
     * Gets the closest entity to a position
     * 
     * @param  world     The world to search in
     * @param  position  The position to search from
     * @param  maxRadius Maximum search radius
     * @param  filter    Optional filter predicate
     * 
     * @return           The closest entity reference, or null if none found
     */
    @SuppressWarnings("unchecked")
    public static Ref<EntityStore> getClosestEntity(World world, Vector3d position, double maxRadius, Predicate<Ref<EntityStore>> filter) {
        final Ref<EntityStore>[] $result = new Ref[1];
        final double[] $closestDist = {
                maxRadius * maxRadius
        };

        Query<EntityStore> query = TransformComponent.getComponentType();
        BiConsumer<ArchetypeChunk<EntityStore>, CommandBuffer<EntityStore>> finder = (archetypeChunk, _) -> {
            for (int index = 0; index < archetypeChunk.size(); index++) {
                Ref<EntityStore> entityRef = archetypeChunk.getReferenceTo(index);
                if (filter != null && !filter.test(entityRef)) {
                    continue;
                }

                TransformComponent transform = archetypeChunk.getComponent(index, TransformComponent.getComponentType());
                if (transform == null) {
                    continue;
                }

                double distSquared = MathUtils.distanceSquared(transform.getPosition(), position);
                if (distSquared < $closestDist[0]) {
                    $result[0] = entityRef;
                    $closestDist[0] = distSquared;
                }
            }
        };

        world.getEntityStore().getStore().forEachChunk(query, finder);
        return $result[0];
    }

    /**
     * Removes an entity from the world
     * 
     * @param  ref   The entity reference
     * @param  store The entity store
     * 
     * @return       true if the entity was removed
     */
    public static boolean removeEntity(Ref<EntityStore> ref, Store<EntityStore> store) {
        if (ref == null || !ref.isValid()) {
            return false;
        }

        // Try to remove as Player
        Player player = store.getComponent(ref, Player.getComponentType());
        if (player != null) {
            player.remove();
            return true;
        }

        // Try to remove as NPC
        NPCEntity npc = store.getComponent(ref, NPCEntity.getComponentType());
        if (npc != null) {
            npc.remove();
            return true;
        }

        return false;
    }

}
