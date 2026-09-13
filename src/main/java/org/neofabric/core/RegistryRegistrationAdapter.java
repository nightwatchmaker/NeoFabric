package org.neofabric.core;

import java.util.Objects;
import java.util.regex.Pattern;

/** Translates native-loader registration requests into a shared NeoFabric registry. */
public final class RegistryRegistrationAdapter {
    private static final Pattern ID = Pattern.compile("[a-z0-9_.-]+:[a-z0-9_/.-]+");

    public <T> T register(LoaderKind source, NeoRegistry<T> registry, String id, T value) {
        Objects.requireNonNull(source, "source");
        Objects.requireNonNull(registry, "registry");
        validateId(id);
        return registry.register(id, value);
    }

    public static void validateId(String id) {
        if (id == null || !ID.matcher(id).matches()) {
            throw new IllegalArgumentException("Registry IDs must be namespaced: " + id);
        }
    }
}
