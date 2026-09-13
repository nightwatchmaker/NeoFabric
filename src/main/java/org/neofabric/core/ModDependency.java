package org.neofabric.core;

import java.util.Objects;

/** A required mod dependency used by the loader-neutral dependency resolver. */
public record ModDependency(String id, String requiredVersion) {
    public ModDependency {
        Objects.requireNonNull(id, "id");
        Objects.requireNonNull(requiredVersion, "requiredVersion");
    }
}
