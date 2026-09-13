package org.neofabric.core;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

/** Owns named NeoFabric registries and provides one lookup point to all loader backends. */
public final class NeoRegistryCatalog {
    private final Map<String, NeoRegistry<?>> registries = new LinkedHashMap<>();

    public synchronized <T> NeoRegistry<T> create(String id) {
        Objects.requireNonNull(id, "id");
        if (registries.containsKey(id)) throw new IllegalArgumentException("Duplicate registry: " + id);
        NeoRegistry<T> registry = new NeoRegistry<>(id);
        registries.put(id, registry);
        return registry;
    }

    public synchronized NeoRegistry<?> get(String id) {
        return registries.get(id);
    }

    public synchronized Map<String, NeoRegistry<?>> registries() {
        return Map.copyOf(registries);
    }

    public synchronized void freezeAll() {
        registries.values().forEach(NeoRegistry::freeze);
    }
}
