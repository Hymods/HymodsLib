package io.hymods.lib.data;

import org.checkerframework.checker.nullness.compatqual.NullableDecl;

/**
 * A single line of text intended for HUD display.
 */
public record InspectionLine(
    String label,
    @NullableDecl String value
) {

    public static InspectionLine of(String label, @NullableDecl String value) {
        return new InspectionLine(label, value);
    }

}
