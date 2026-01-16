package io.hymods.lib.data;

public record InspectionOptions(
    int maxNameLength,
    boolean showId,
    boolean showModName,
    boolean showBenchInfo,
    boolean showMiningInfo,
    boolean showFarmingInfo,
    boolean showBlockEntityInfo,
    boolean showEntityHealth
) {

    public static InspectionOptions defaults() {
        return new InspectionOptions(
            32,
            false,
            true,
            true,
            true,
            true,
            true,
            true
        );
    }
}
