package org.neofabric.core;

/** Entry point implemented by mods that register content through NeoFabric's shared catalog. */
public interface NeoFabricRegistryEntrypoint {
    void register(NeoRegistryCatalog registries, LoaderRegistryAdapter adapter);
}
