package io.hymods.lib.utils;

import java.util.ArrayList;
import java.util.List;

import org.checkerframework.checker.nullness.compatqual.NullableDecl;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.asset.type.blocktype.config.BlockType;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.modules.entitystats.EntityStatMap;
import com.hypixel.hytale.server.core.modules.entitystats.EntityStatValue;
import com.hypixel.hytale.server.core.modules.entitystats.asset.DefaultEntityStatTypes;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.npc.entities.NPCEntity;

import io.hymods.lib.data.InspectionLine;
import io.hymods.lib.data.InspectionOptions;
import io.hymods.lib.data.InspectionResult;
import io.hymods.lib.data.InspectionTargetType;
import io.hymods.lib.inspection.InspectionAddon;
import io.hymods.lib.inspection.InspectionAddonRegistry;

public class InspectionUtils {
    private static final int DEFAULT_MAX_NAME_LENGTH = 32;

    private InspectionUtils() {
        // utility
    }

    public static InspectionResult inspectRaycastResult(Store<EntityStore> store, io.hymods.lib.data.RaycastResult result, InspectionOptions options) {
        if (result == null || result.isMiss()) {
            return InspectionResult.none();
        }

        InspectionOptions effectiveOptions = options != null ? options : InspectionOptions.defaults();
        int clampedMaxNameLength = effectiveOptions.maxNameLength() <= 0 ? DEFAULT_MAX_NAME_LENGTH : effectiveOptions.maxNameLength();

        if (result.isBlockHit()) {
            return inspectBlock(result.blockType(), clampedMaxNameLength, effectiveOptions);
        }

        if (result.isEntityHit()) {
            return inspectEntity(store, result.entityRef(), result.entityName(), clampedMaxNameLength, effectiveOptions);
        }

        return InspectionResult.none();
    }

    public static InspectionResult inspectBlock(@NullableDecl BlockType blockType, int maxNameLength, InspectionOptions options) {
        if (blockType == null) {
            return InspectionResult.none();
        }

        InspectionOptions effectiveOptions = options != null ? options : InspectionOptions.defaults();

        String blockId = blockType.getId();
        String displayName = truncate(formatName(blockId), maxNameLength);

        List<InspectionLine> lines = new ArrayList<>();
        lines.add(InspectionLine.of("Block", displayName));

        if (effectiveOptions.showId()) {
            lines.add(InspectionLine.of("Id", blockId));
        }

        if (effectiveOptions.showModName()) {
            lines.add(InspectionLine.of("Mod", namespaceOf(blockId)));
        }

        if (effectiveOptions.showBenchInfo() && blockType.getBench() != null) {
            var bench = blockType.getBench();
            lines.add(InspectionLine.of("Bench", safe(bench.getDescriptiveLabel(), bench.getId())));

            int tiers = 0;
            for (int i = 0; i < 32; i++) {
                if (bench.getTierLevel(i) == null) {
                    break;
                }
                tiers++;
            }
            if (tiers > 0) {
                lines.add(InspectionLine.of("Bench Tiers", String.valueOf(tiers)));
            }
        }

        if (effectiveOptions.showMiningInfo() && blockType.getGathering() != null) {
            var gathering = blockType.getGathering();
            boolean hasToolData = gathering.getToolData() != null && !gathering.getToolData().isEmpty();
            boolean mineable = gathering.isHarvestable() || gathering.isSoft() || hasToolData;
            if (mineable) {
                lines.add(InspectionLine.of("Mining", "Mineable"));
            }
        }

        if (effectiveOptions.showFarmingInfo() && blockType.getFarming() != null) {
            var farming = blockType.getFarming();
            lines.add(InspectionLine.of("Farming", "Yes"));
            lines.add(InspectionLine.of("Start Stage", farming.getStartingStageSet()));
        }

        if (effectiveOptions.showBlockEntityInfo() && blockType.getBlockEntity() != null) {
            lines.add(InspectionLine.of("Chest Inventory", "Planned (EA)"));
            lines.add(InspectionLine.of("Processing Time", "Planned (EA)"));
        }

        for (InspectionAddon addon : InspectionAddonRegistry.getAddons()) {
            addon.augmentBlock(blockType, lines);
        }

        return new InspectionResult(InspectionTargetType.BLOCK, displayName, blockId, true, List.copyOf(lines));
    }

    public static InspectionResult inspectEntity(Store<EntityStore> store, @NullableDecl Ref<EntityStore> entityRef, @NullableDecl String fallbackName, int maxNameLength, InspectionOptions options) {
        if (entityRef == null || !entityRef.isValid()) {
            return InspectionResult.none();
        }

        InspectionOptions effectiveOptions = options != null ? options : InspectionOptions.defaults();

        String name = EntityUtils.getEntityDisplayName(entityRef, store);
        if (name == null || name.isBlank() || "Entity".equals(name)) {
            name = fallbackName != null ? fallbackName : "Entity";
        }
        name = truncate(formatName(name), maxNameLength);

        String entityType = resolveEntityType(store, entityRef);
        if ("npc".equals(entityType) && (fallbackName != null && !fallbackName.isBlank())) {
            entityType = formatName(fallbackName);
        }

        List<InspectionLine> lines = new ArrayList<>();
        lines.add(InspectionLine.of("Entity", name));
        lines.add(InspectionLine.of("Type", entityType));

        if (effectiveOptions.showEntityHealth()) {
            String healthText = "Unknown";
            EntityHealth health = getHealth(store, entityRef);
            if (health != null) {
                healthText = health.current().intValue() + "/" + health.max().intValue();
            }
            lines.add(InspectionLine.of("Health", healthText));
        }

        for (InspectionAddon addon : InspectionAddonRegistry.getAddons()) {
            addon.augmentEntity(store, entityRef, lines);
        }

        return new InspectionResult(InspectionTargetType.ENTITY, name, null, false, List.copyOf(lines));
    }

    private static String resolveEntityType(Store<EntityStore> store, Ref<EntityStore> entityRef) {
        Player player = store.getComponent(entityRef, Player.getComponentType());
        if (player != null) {
            return "player";
        }

        NPCEntity npc = store.getComponent(entityRef, NPCEntity.getComponentType());
        if (npc != null) {
            return npc.getRoleName() != null ? npc.getRoleName() : "npc";
        }

        return "entity";
    }

    private record EntityHealth(Float current, Float max) {
    }

    private static @NullableDecl EntityHealth getHealth(Store<EntityStore> store, Ref<EntityStore> entityRef) {
        try {
            EntityStatMap stats = store.getComponent(entityRef, EntityStatMap.getComponentType());
            if (stats == null) {
                return null;
            }

            int healthIndex = DefaultEntityStatTypes.getHealth();
            EntityStatValue health = stats.get(healthIndex);
            if (health == null) {
                return null;
            }

            return new EntityHealth(health.get(), health.getMax());
        } catch (Throwable ignored) {
            return null;
        }
    }

    public static String formatName(@NullableDecl String id) {
        if (id == null || id.isEmpty()) {
            return "Unknown";
        }

        String path = id.contains(":") ? id.split(":", 2)[1] : id;
        if (path.contains("@")) {
            path = path.substring(0, path.indexOf("@"));
        }

        String[] parts = path.split("_");
        StringBuilder result = new StringBuilder();

        for (String part : parts) {
            if (!part.isEmpty()) {
                if (result.length() > 0) {
                    result.append(" ");
                }
                result.append(Character.toUpperCase(part.charAt(0)));
                if (part.length() > 1) {
                    result.append(part.substring(1).toLowerCase());
                }
            }
        }

        return result.length() > 0 ? result.toString() : "Unknown";
    }

    public static String truncate(@NullableDecl String text, int maxChars) {
        if (text == null) {
            return "Unknown";
        }
        if (maxChars <= 0 || text.length() <= maxChars) {
            return text;
        }
        return text.substring(0, maxChars) + "...";
    }

    private static String namespaceOf(@NullableDecl String id) {
        if (id == null || id.isBlank()) {
            return "Unknown";
        }
        int colonIndex = id.indexOf(':');
        return colonIndex > 0 ? id.substring(0, colonIndex) : "Hytale";
    }

    private static String safe(@NullableDecl String preferred, @NullableDecl String fallback) {
        if (preferred != null && !preferred.isBlank()) {
            return preferred;
        }
        return fallback != null ? fallback : "Unknown";
    }
}
