package org.neofabric.core;

@FunctionalInterface
public interface RegistryEntryCallback<T> {
    void onRegistered(String id, T value, LoaderKind source);
}
