package org.neofabric.core;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CopyOnWriteArrayList;

/** Shared deterministic registry contract for Fabric, Forge, and NeoForge adapters. */
public final class NeoRegistry<T> {
    private final String registryId;
    private final Map<String, T> entries = new LinkedHashMap<>();
    private final CopyOnWriteArrayList<RegistryEntryCallback<T>> callbacks = new CopyOnWriteArrayList<>();
    private boolean frozen;

    public NeoRegistry(String registryId) {
        this.registryId = Objects.requireNonNull(registryId, "registryId");
    }

    public synchronized T register(String id, T value) {
        if (frozen) throw new IllegalStateException("Registry is frozen: " + registryId);
        Objects.requireNonNull(id, "id");
        Objects.requireNonNull(value, "value");
        if (entries.containsKey(id)) throw new IllegalArgumentException("Duplicate " + registryId + " entry: " + id);
        entries.put(id, value);
        callbacks.forEach(callback -> callback.onRegistered(id, value, LoaderKind.UNKNOWN));
        return value;
    }

    public void onRegistered(RegistryEntryCallback<T> callback) {
        callbacks.add(Objects.requireNonNull(callback, "callback"));
    }

    void notifyRegistered(String id, T value, LoaderKind source) {
        callbacks.forEach(callback -> callback.onRegistered(id, value, source));
    }

    public synchronized T get(String id) {
        return entries.get(id);
    }

    public synchronized boolean contains(String id) {
        return entries.containsKey(id);
    }

    public synchronized Collection<T> values() {
        return java.util.List.copyOf(entries.values());
    }

    public synchronized Map<String, T> entries() {
        return Map.copyOf(entries);
    }

    public synchronized void freeze() {
        frozen = true;
    }

    public synchronized boolean frozen() {
        return frozen;
    }

    public String id() {
        return registryId;
    }
}
