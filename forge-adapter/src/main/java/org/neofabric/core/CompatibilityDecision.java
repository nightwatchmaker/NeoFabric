package org.neofabric.core;

/** Result of deciding whether NeoFabric can hand a mod to a loader adapter. */
public record CompatibilityDecision(ModDescriptor mod, boolean accepted, String reason) {
}
