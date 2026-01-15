package io.hymods.lib.data;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Result of an entity search operation
 */
public record EntitySearchResult(
    List<EntityInfo> entities,
    SearchParameters searchParameters
) {

    /**
     * Get all entities from the search result
     * 
     * @return list of entities
     */
    public List<EntityInfo> getEntities() {
        return Collections.unmodifiableList(this.entities);
    }

    /**
     * Get the closest entity from the search result
     * 
     * @return closest entity or null if no entities found
     */
    public EntityInfo getClosest() {
        if (this.entities.isEmpty()) {
            return null;
        }

        EntityInfo closest = null;
        double minDistance = Double.MAX_VALUE;

        for (EntityInfo info : this.entities) {
            if (info.distance() < minDistance) {
                minDistance = info.distance();
                closest = info;
            }
        }

        return closest;
    }

    /**
     * Get all player entities from the search result
     * 
     * @return list of player entities
     */
    public List<EntityInfo> getPlayers() {
        List<EntityInfo> players = new ArrayList<>();
        for (EntityInfo info : this.entities) {
            if (info.isPlayer()) {
                players.add(info);
            }
        }
        return players;
    }

    /**
     * Get all NPC entities from the search result
     * 
     * @return list of NPC entities
     */
    public List<EntityInfo> getNPCs() {
        List<EntityInfo> npcs = new ArrayList<>();
        for (EntityInfo info : this.entities) {
            if (info.isNPC()) {
                npcs.add(info);
            }
        }
        return npcs;
    }

    /**
     * Get entities within a certain distance
     * 
     * @param  maxDistance maximum distance
     * 
     * @return             list of entities within the specified distance
     */
    public List<EntityInfo> getWithinDistance(double maxDistance) {
        List<EntityInfo> within = new ArrayList<>();
        for (EntityInfo info : this.entities) {
            if (info.distance() <= maxDistance) {
                within.add(info);
            }
        }
        return within;
    }

    /**
     * Get the total count of entities found
     * 
     * @return count of entities
     */
    public int getCount() {
        return this.entities.size();
    }

    /**
     * Check if no entities were found
     * 
     * @return true if no entities found, false otherwise
     */
    public boolean isEmpty() {
        return this.entities.isEmpty();
    }

    /**
     * Get the search parameters used for this search
     * 
     * @return search parameters
     */
    public SearchParameters getSearchParameters() {
        return searchParameters;
    }

}
