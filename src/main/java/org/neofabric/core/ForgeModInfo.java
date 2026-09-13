package org.neofabric.core;

import java.util.List;

/** Normalized Forge/NeoForge metadata retained for backend construction decisions. */
public record ForgeModInfo(
        List<String> modIds,
        List<String> versions,
        String modLoader,
        String loaderVersion,
        String displayName
) {
}
