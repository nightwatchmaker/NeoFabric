package org.neofabric.core;

/** Backend contract owned by the top-level NeoFabric loader. */
public interface CompatibilityBackend {
    LoaderKind kind();

    CompatibilityDecision translate(ModDescriptor mod, MinecraftTarget target);
}
