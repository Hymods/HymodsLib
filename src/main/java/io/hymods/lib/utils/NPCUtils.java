package io.hymods.lib.utils;

import java.util.function.BiConsumer;

import com.hypixel.hytale.component.Holder;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.math.vector.Vector3d;
import com.hypixel.hytale.math.vector.Vector3f;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.modules.entity.component.DisplayNameComponent;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.npc.INonPlayerCharacter;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.npc.NPCPlugin;
import com.hypixel.hytale.server.npc.entities.NPCEntity;
import com.hypixel.hytale.server.npc.role.Role;

import it.unimi.dsi.fastutil.Pair;

/**
 * Utility class for NPC-related operations
 */
public class NPCUtils {
    private static final HytaleLogger LOGGER = HytaleLogger.forEnclosingClass();

    private NPCUtils() {
        // Prevent instantiation
    }

    /**
     * Spawns an NPC at a location
     * 
     * @param  world    The world to spawn in
     * @param  roleName The NPC role name
     * @param  position The spawn position
     * @param  rotation The spawn rotation
     * 
     * @return          The spawned NPC reference, or null if failed
     */
    public static Ref<EntityStore> spawnNPC(World world, String roleName, Vector3d position, Vector3f rotation) {
        final Store<EntityStore> store = world.getEntityStore().getStore();
        final NPCPlugin npcPlugin = NPCPlugin.get();
        if (npcPlugin == null) {
            return null;
        }

        Pair<Ref<EntityStore>, INonPlayerCharacter> result = npcPlugin.spawnNPC(store, roleName, null, position, rotation);
        return result != null ? result.first() : null;
    }

    /**
     * Spawns an NPC with a flock type
     * 
     * @param  world     The world to spawn in
     * @param  roleName  The NPC role name
     * @param  flockType The flock type (for group behavior)
     * @param  position  The spawn position
     * @param  rotation  The spawn rotation
     * 
     * @return           The spawned NPC reference, or null if failed
     */
    public static Ref<EntityStore> spawnNPCWithFlock(World world, String roleName, String flockType, Vector3d position, Vector3f rotation) {
        final Store<EntityStore> store = world.getEntityStore().getStore();
        final NPCPlugin npcPlugin = NPCPlugin.get();
        if (npcPlugin == null) {
            return null;
        }

        Pair<Ref<EntityStore>, INonPlayerCharacter> result = npcPlugin.spawnNPC(store, roleName, flockType, position, rotation);
        return result != null ? result.first() : null;
    }

    /**
     * Creates a custom NPC with a callback for customization
     * 
     * @param  world      The world to spawn in
     * @param  roleName   The NPC role name
     * @param  position   The spawn position
     * @param  rotation   The spawn rotation
     * @param  customizer Callback to customize the NPC before spawning
     * 
     * @return            The spawned NPC reference, or null if failed
     */
    public static Ref<EntityStore> spawnCustomNPC(World world, String roleName, Vector3d position, Vector3f rotation, BiConsumer<NPCEntity, Holder<EntityStore>> customizer) {
        final Store<EntityStore> store = world.getEntityStore().getStore();
        final NPCPlugin npcPlugin = NPCPlugin.get();
        if (npcPlugin == null) {
            return null;
        }

        int roleIndex = npcPlugin.getIndex(roleName);
        if (roleIndex < 0) {
            return null;
        }

        Pair<Ref<EntityStore>, NPCEntity> result = npcPlugin.spawnEntity(
            store,
            roleIndex,
            position,
            rotation,
            null,
            (npc, holder, _) -> {
                if (customizer != null) {
                    customizer.accept(npc, holder);
                }
            },
            null
        );

        return result != null ? result.first() : null;
    }

    /**
     * Gets the NPC component from a reference
     * 
     * @param  ref   The entity reference
     * @param  store The entity store
     * 
     * @return       The NPC component, or null if not an NPC
     */
    public static NPCEntity getNPC(Ref<EntityStore> ref, Store<EntityStore> store) {
        if (ref == null || !ref.isValid()) {
            return null;
        }
        return store.getComponent(ref, NPCEntity.getComponentType());
    }

    /**
     * Gets the role of an NPC
     * 
     * @param  ref   The NPC reference
     * @param  store The entity store
     * 
     * @return       The NPC's role, or null if not found
     */
    public static Role getNPCRole(Ref<EntityStore> ref, Store<EntityStore> store) {
        NPCEntity npc = getNPC(ref, store);
        return npc != null ? npc.getRole() : null;
    }

    /**
     * Sets the state of an NPC
     * 
     * @param  ref      The NPC reference
     * @param  store    The entity store
     * @param  state    The new state
     * @param  subState Optional sub-state
     * 
     * @return          true if the state was set successfully
     */
    public static boolean setNPCState(Ref<EntityStore> ref, Store<EntityStore> store, String state, String subState) {
        Role role = getNPCRole(ref, store);
        if (role == null) {
            return false;
        }

        role.getStateSupport().setState(ref, state, subState, store);
        return true;
    }

    /**
     * Gets the current state of an NPC Note: State retrieval depends on internal
     * StateSupport implementation
     * 
     * @param  ref   The NPC reference
     * @param  store The entity store
     * 
     * @return       The current state, or null if not found
     */
    public static String getNPCState(Ref<EntityStore> ref, Store<EntityStore> store) {
        // StateSupport doesn't expose getCurrentState() directly
        // State is managed internally by the role system
        // This would need StateEvaluator component access for accurate state
        LOGGER.atInfo().log("FIXME: getNPCState is not implemented due to StateSupport limitations.");
        return null;
    }

    /**
     * Sets a marked target for an NPC
     * 
     * @param  ref        The NPC reference
     * @param  store      The entity store
     * @param  targetSlot The target slot name (e.g., "LockedTarget")
     * @param  targetRef  The target entity reference
     * 
     * @return            true if the target was set successfully
     */
    public static boolean setNPCTarget(Ref<EntityStore> ref, Store<EntityStore> store, String targetSlot, Ref<EntityStore> targetRef) {
        Role role = getNPCRole(ref, store);
        if (role == null) {
            return false;
        }

        role.setMarkedTarget(targetSlot, targetRef);
        return true;
    }

    /**
     * Gets the leash point of an NPC
     * 
     * @param  ref   The NPC reference
     * @param  store The entity store
     * 
     * @return       The leash point, or null if not found
     */
    public static Vector3d getNPCLeashPoint(Ref<EntityStore> ref, Store<EntityStore> store) {
        NPCEntity npc = getNPC(ref, store);
        return npc != null ? npc.getLeashPoint() : null;
    }

    /**
     * Sets the leash point for an NPC
     * 
     * @param  ref        The NPC reference
     * @param  store      The entity store
     * @param  leashPoint The new leash point
     * 
     * @return            true if the leash point was set successfully
     */
    public static boolean setNPCLeashPoint(Ref<EntityStore> ref, Store<EntityStore> store, Vector3d leashPoint) {
        NPCEntity npc = getNPC(ref, store);
        if (npc == null) {
            return false;
        }

        npc.setLeashPoint(leashPoint);
        return true;
    }

    /**
     * Sets the display name of an NPC
     * 
     * @param  ref         The NPC reference
     * @param  store       The entity store
     * @param  displayName The new display name
     * 
     * @return             true if the display name was set successfully
     */
    public static boolean setNPCDisplayName(Ref<EntityStore> ref, Store<EntityStore> store, String displayName) {
        DisplayNameComponent nameComp = store.getComponent(ref, DisplayNameComponent.getComponentType());
        if (nameComp == null) {
            // Add the component if it doesn't exist
            store.putComponent(
                ref, DisplayNameComponent.getComponentType(),
                new DisplayNameComponent(Message.raw(displayName))
            );
        } else {
            // Update existing component
            store.putComponent(
                ref, DisplayNameComponent.getComponentType(),
                new DisplayNameComponent(Message.raw(displayName))
            );
        }
        return true;
    }

    /**
     * Gets the role name of an NPC
     * 
     * @param  ref   The NPC reference
     * @param  store The entity store
     * 
     * @return       The role name, or null if not found
     */
    public static String getNPCRoleName(Ref<EntityStore> ref, Store<EntityStore> store) {
        NPCEntity npc = getNPC(ref, store);
        return npc != null ? npc.getRoleName() : null;
    }

    /**
     * Changes the role of an NPC
     * 
     * @param  ref         The NPC reference
     * @param  store       The entity store
     * @param  newRoleName The new role name
     * 
     * @return             true if the role was changed successfully
     */
    public static boolean changeNPCRole(Ref<EntityStore> ref, Store<EntityStore> store, String newRoleName) {
        NPCPlugin npcPlugin = NPCPlugin.get();
        if (npcPlugin == null) {
            return false;
        }

        int roleIndex = npcPlugin.getIndex(newRoleName);
        if (roleIndex < 0) {
            return false;
        }

        NPCEntity npc = getNPC(ref, store);
        if (npc == null) {
            return false;
        }

        // This would need proper role changing implementation
        // The actual implementation depends on how Hytale handles role changes
        npc.setRoleName(newRoleName);
        npc.setRoleIndex(roleIndex);

        return true;
    }

    /**
     * Removes an NPC from the world
     * 
     * @param  ref   The NPC reference
     * @param  store The entity store
     * 
     * @return       true if the NPC was removed successfully
     */
    public static boolean removeNPC(Ref<EntityStore> ref, Store<EntityStore> store) {
        NPCEntity npc = getNPC(ref, store);
        if (npc == null) {
            return false;
        }

        npc.remove();
        return true;
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
        return getNPC(ref, store) != null;
    }

    /**
     * Gets the health of an NPC
     * 
     * @param  ref   The NPC reference
     * @param  store The entity store
     * 
     * @return       The current health, or -1 if not found
     */
    public static double getNPCHealth(Ref<EntityStore> ref, Store<EntityStore> store) {
        // This would need access to the stat system
        // Implementation depends on how Hytale handles entity stats
        LOGGER.atInfo().log("FIXME: getNPCHealth is not implemented due to stat system limitations.");
        return -1;
    }

}
