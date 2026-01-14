package io.hymods.lib.data;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Result of an entity search operation
 */
public class EntitySearchResult {
    private final List<EntityInfo> entities = new ArrayList<>();
    private final SearchParameters searchParameters;

    public EntitySearchResult(SearchParameters searchParameters) {
        this.searchParameters = searchParameters;
    }

    public void addEntity(EntityInfo info) {
        this.entities.add(info);
    }

    public List<EntityInfo> getEntities() {
        return Collections.unmodifiableList(this.entities);
    }

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

    public List<EntityInfo> getPlayers() {
        List<EntityInfo> players = new ArrayList<>();
        for (EntityInfo info : this.entities) {
            if (info.isPlayer()) {
                players.add(info);
            }
        }
        return players;
    }

    public List<EntityInfo> getNPCs() {
        List<EntityInfo> npcs = new ArrayList<>();
        for (EntityInfo info : this.entities) {
            if (info.isNPC()) {
                npcs.add(info);
            }
        }
        return npcs;
    }

    public List<EntityInfo> getWithinDistance(double maxDistance) {
        List<EntityInfo> within = new ArrayList<>();
        for (EntityInfo info : this.entities) {
            if (info.distance() <= maxDistance) {
                within.add(info);
            }
        }
        return within;
    }

    public int getCount() {
        return this.entities.size();
    }

    public boolean isEmpty() {
        return this.entities.isEmpty();
    }

    public SearchParameters getSearchParameters() {
        return searchParameters;
    }

}
