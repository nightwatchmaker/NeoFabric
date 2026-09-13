package org.neofabric.core;

/** Version profile for the first Minecraft integration target. */
public record MinecraftTarget(String runtimeVersion, String developmentCoordinate, int javaRelease) {
    public static final MinecraftTarget MC_26_2 = new MinecraftTarget("26.2", "1.21.11", 25);
}
