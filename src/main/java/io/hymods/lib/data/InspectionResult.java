package io.hymods.lib.data;

import java.util.List;

import org.checkerframework.checker.nullness.compatqual.NullableDecl;

/**
 * A normalized inspection payload suitable for rendering into a HUD.
 */
public record InspectionResult(
    InspectionTargetType targetType,
    String displayName,
    @NullableDecl String targetId,
    boolean showIcon,
    List<InspectionLine> lines
) {

    public boolean hasTarget() {
        return this.targetType != InspectionTargetType.NONE;
    }

    public static InspectionResult none() {
        return new InspectionResult(InspectionTargetType.NONE, "", null, false, List.of());
    }

}
