package org.neofabric.core;

import java.util.Objects;

public record ModDescriptor(
        String id,
        String version,
        LoaderKind loader,
        String source
) {
    public ModDescriptor {
        Objects.requireNonNull(id, "id");
        Objects.requireNonNull(version, "version");
        Objects.requireNonNull(loader, "loader");
        Objects.requireNonNull(source, "source");
    }

    @Override
    public String toString() {
        return id + " " + version + " [" + loader + "] from " + source;
    }
}
