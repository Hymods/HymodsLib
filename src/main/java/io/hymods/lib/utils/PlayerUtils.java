package io.hymods.lib.utils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

import com.hypixel.hytale.component.ArchetypeChunk;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.math.vector.Vector3d;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.modules.entity.component.HeadRotation;
import com.hypixel.hytale.server.core.modules.entity.component.TransformComponent;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;

/**
 * Utility class for player-related operations
 */
public class PlayerUtils {

    private PlayerUtils() {
        // Prevent instantiation
    }

    /**
     * Gets a player by their UUID
     * 
     * @param  world The world to search in
     * @param  uuid  The player's UUID
     * 
     * @return       The player reference, or null if not found
     */
    @SuppressWarnings("unchecked")
    public static Ref<EntityStore> getPlayerByUUID(World world, UUID uuid) {
        final Store<EntityStore> store = world.getEntityStore().getStore();
        final Ref<EntityStore>[] $result = new Ref[1];

        BiConsumer<ArchetypeChunk<EntityStore>, CommandBuffer<EntityStore>> checker = (archetypeChunk, _) -> {
            for (int index = 0; index < archetypeChunk.size(); index++) {
                Ref<EntityStore> ref = archetypeChunk.getReferenceTo(index);
                Player player = store.getComponent(ref, Player.getComponentType());
                @SuppressWarnings("removal")
                UUID playerUuid = player != null ? player.getUuid() : null;
                if (playerUuid != null && playerUuid.equals(uuid)) {
                    $result[0] = ref;
                    break;
                }
            }
        };

        store.forEachChunk(PlayerRef.getComponentType(), checker);
        return $result[0];
    }

    /**
     * Gets a player by their display name
     * 
     * @param  world The world to search in
     * @param  name  The player's display name
     * 
     * @return       The player reference, or null if not found
     */
    @SuppressWarnings("unchecked")
    public static Ref<EntityStore> getPlayerByName(World world, String name) {
        final Store<EntityStore> store = world.getEntityStore().getStore();
        final Ref<EntityStore>[] $result = new Ref[1];

        BiConsumer<ArchetypeChunk<EntityStore>, CommandBuffer<EntityStore>> checker = (archetypeChunk, _) -> {
            for (int index = 0; index < archetypeChunk.size(); index++) {
                Ref<EntityStore> ref = archetypeChunk.getReferenceTo(index);
                Player player = store.getComponent(ref, Player.getComponentType());
                if (player != null && player.getDisplayName().equals(name)) {
                    $result[0] = ref;
                    break;
                }
            }
        };

        store.forEachChunk(PlayerRef.getComponentType(), checker);
        return $result[0];
    }

    /**
     * Gets all online players in a world
     * 
     * @param  world The world to search in
     * 
     * @return       List of player references
     */
    public static List<Ref<EntityStore>> getAllPlayers(World world) {
        final Store<EntityStore> store = world.getEntityStore().getStore();
        final List<Ref<EntityStore>> players = new ArrayList<>();

        BiConsumer<ArchetypeChunk<EntityStore>, CommandBuffer<EntityStore>> collector = (archetypeChunk, _) -> {
            for (int index = 0; index < archetypeChunk.size(); index++) {
                Ref<EntityStore> ref = archetypeChunk.getReferenceTo(index);
                Player player = store.getComponent(ref, Player.getComponentType());
                if (player != null) {
                    players.add(ref);
                }
            }
        };

        store.forEachChunk(PlayerRef.getComponentType(), collector);
        return Collections.unmodifiableList(players);
    }

    /**
     * Gets players within a certain radius of a position
     * 
     * @param  world  The world to search in
     * @param  center The center position
     * @param  radius The search radius
     * 
     * @return        List of player references within radius
     */
    public static List<Ref<EntityStore>> getPlayersInRadius(World world, Vector3d center, double radius) {
        final Store<EntityStore> store = world.getEntityStore().getStore();
        final List<Ref<EntityStore>> result = new ArrayList<>();
        final double radiusSquared = radius * radius;

        BiConsumer<ArchetypeChunk<EntityStore>, CommandBuffer<EntityStore>> collector = (archetypeChunk, _) -> {
            for (int index = 0; index < archetypeChunk.size(); index++) {
                Ref<EntityStore> ref = archetypeChunk.getReferenceTo(index);
                TransformComponent transform = store.getComponent(ref, TransformComponent.getComponentType());

                if (transform != null) {
                    Vector3d pos = transform.getPosition();
                    double distSquared = MathUtils.distanceSquared(pos, center);

                    if (distSquared <= radiusSquared) {
                        result.add(ref);
                    }
                }
            }
        };

        store.forEachChunk(PlayerRef.getComponentType(), collector);
        return Collections.unmodifiableList(result);
    }

    /**
     * Gets the Player component from a reference
     * 
     * @param  ref   The entity reference
     * @param  store The entity store
     * 
     * @return       The Player component, or null if not a player
     */
    public static Player getPlayer(Ref<EntityStore> ref, Store<EntityStore> store) {
        if (ref == null || !ref.isValid()) {
            return null;
        }
        return store.getComponent(ref, Player.getComponentType());
    }

    /**
     * Gets a player's position
     * 
     * @param  ref   The player reference
     * @param  store The entity store
     * 
     * @return       The player's position, or null if not found
     */
    public static Vector3d getPlayerPosition(Ref<EntityStore> ref, Store<EntityStore> store) {
        if (ref == null || !ref.isValid()) {
            return null;
        }

        TransformComponent transform = store.getComponent(ref, TransformComponent.getComponentType());
        return transform != null ? transform.getPosition() : null;
    }

    /**
     * Gets a player's look direction
     * 
     * @param  ref   The player reference
     * @param  store The entity store
     * 
     * @return       The player's look direction vector, or null if not found
     */
    public static Vector3d getPlayerLookDirection(Ref<EntityStore> ref, Store<EntityStore> store) {
        if (ref == null || !ref.isValid()) {
            return null;
        }

        HeadRotation rotation = store.getComponent(ref, HeadRotation.getComponentType());
        return rotation != null ? rotation.getDirection() : null;
    }

    /**
     * Sends a message to a player
     * 
     * @param ref     The player reference
     * @param store   The entity store
     * @param message The message to send
     */
    public static void sendMessage(Ref<EntityStore> ref, Store<EntityStore> store, String message) {
        Player player = getPlayer(ref, store);
        if (player != null) {
            player.sendMessage(Message.raw(message));
        }
    }

    /**
     * Sends a formatted message to a player
     * 
     * @param ref    The player reference
     * @param store  The entity store
     * @param format The message format
     * @param args   The format arguments
     */
    public static void sendFormattedMessage(Ref<EntityStore> ref, Store<EntityStore> store, String format, Object... args) {
        sendMessage(ref, store, String.format(format, args));
    }

    /**
     * Broadcasts a message to all players
     * 
     * @param world   The world to broadcast in
     * @param message The message to broadcast
     */
    public static void broadcast(World world, String message) {
        Store<EntityStore> store = world.getEntityStore().getStore();
        List<Ref<EntityStore>> players = getAllPlayers(world);

        for (Ref<EntityStore> playerRef : players) {
            sendMessage(playerRef, store, message);
        }
    }

    /**
     * Executes an action for each online player
     * 
     * @param world  The world to iterate in
     * @param action The action to execute for each player
     */
    public static void forEachPlayer(World world, Consumer<Player> action) {
        final Store<EntityStore> store = world.getEntityStore().getStore();

        BiConsumer<ArchetypeChunk<EntityStore>, CommandBuffer<EntityStore>> executor = (archetypeChunk, _) -> {
            for (int index = 0; index < archetypeChunk.size(); index++) {
                Ref<EntityStore> ref = archetypeChunk.getReferenceTo(index);
                Player player = store.getComponent(ref, Player.getComponentType());
                if (player != null) {
                    action.accept(player);
                }
            }
        };

        store.forEachChunk(PlayerRef.getComponentType(), executor);
    }

    /**
     * Checks if a player has line of sight to a position
     * 
     * @param  ref         The player reference
     * @param  store       The entity store
     * @param  target      The target position
     * @param  maxDistance The maximum distance to check
     * 
     * @return             true if the player has line of sight
     */
    public static boolean hasLineOfSight(Ref<EntityStore> ref, Store<EntityStore> store, Vector3d target, double maxDistance) {
        Vector3d playerPos = getPlayerPosition(ref, store);
        if (playerPos == null) {
            return false;
        }

        // Add eye height
        Vector3d eyePos = new Vector3d(playerPos.getX(), playerPos.getY() + 1.6, playerPos.getZ());

        // This would need world access for proper block checking
        // For now, just check distance
        return MathUtils.distanceSquared(eyePos, target) <= maxDistance * maxDistance;
    }

    /**
     * Gets the distance between two players
     * 
     * @param  ref1  First player reference
     * @param  ref2  Second player reference
     * @param  store The entity store
     * 
     * @return       The distance between players, or -1 if either is invalid
     */
    public static double getDistanceBetweenPlayers(Ref<EntityStore> ref1, Ref<EntityStore> ref2, Store<EntityStore> store) {
        Vector3d pos1 = getPlayerPosition(ref1, store);
        Vector3d pos2 = getPlayerPosition(ref2, store);

        if (pos1 == null || pos2 == null) {
            return -1;
        }

        return MathUtils.distance(pos1, pos2);
    }

    /**
     * Gets the squared distance between two players
     * 
     * @param  ref1  First player reference
     * @param  ref2  Second player reference
     * @param  store The entity store
     * 
     * @return       The squared distance between players, or -1 if either is
     *               invalid
     */
    public static double getDistanceSquaredBetweenPlayers(Ref<EntityStore> ref1, Ref<EntityStore> ref2, Store<EntityStore> store) {
        Vector3d pos1 = getPlayerPosition(ref1, store);
        Vector3d pos2 = getPlayerPosition(ref2, store);

        if (pos1 == null || pos2 == null) {
            return -1;
        }

        return MathUtils.distanceSquared(pos1, pos2);
    }

    /**
     * Checks if a player is within a certain distance of a position
     * 
     * @param  ref      The player reference
     * @param  store    The entity store
     * @param  position The position to check
     * @param  distance The maximum distance
     * 
     * @return          true if the player is within distance
     */
    public static boolean isPlayerWithinDistance(Ref<EntityStore> ref, Store<EntityStore> store, Vector3d position, double distance) {
        Vector3d playerPos = getPlayerPosition(ref, store);
        if (playerPos == null) {
            return false;
        }

        return MathUtils.distanceSquared(playerPos, position) <= distance * distance;
    }

}
