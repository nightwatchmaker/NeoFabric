package org.neofabric.core;

import java.util.Objects;

/** Loader-bound facade used by native Fabric, Forge, and NeoForge registration shims. */
public final class LoaderRegistryAdapter {
    private final LoaderKind source;
    private final RegistryRegistrationAdapter translator = new RegistryRegistrationAdapter();

    public LoaderRegistryAdapter(LoaderKind source) {
        this.source = Objects.requireNonNull(source, "source");
    }

    public <T> T register(NeoRegistry<T> registry, String id, T value) {
        T registered = translator.register(source, registry, id, value);
        registry.notifyRegistered(id, registered, source);
        return registered;
    }

    public LoaderKind source() {
        return source;
    }
}
