package org.neofabric.core;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipFile;

/** Validates Fabric Mixin configuration resources before a transformer consumes them. */
public final class FabricMixinConfigDiscovery {
    private FabricMixinConfigDiscovery() {
    }

    public static List<String> validate(Path modJar, FabricModInfo info) throws IOException {
        List<String> valid = new ArrayList<>();
        try (ZipFile zip = new ZipFile(modJar.toFile())) {
            for (String config : info.mixins()) {
                if (config == null || config.isBlank() || config.startsWith("/")
                        || config.contains("..") || config.contains("\\")) {
                    throw new IOException("unsafe Fabric Mixin config path in " + modJar + ": " + config);
                }
                if (zip.getEntry(config) == null) {
                    throw new IOException("Fabric Mixin config not found in " + modJar + ": " + config);
                }
                valid.add(config);
            }
        }
        return List.copyOf(valid);
    }
}
