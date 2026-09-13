package org.neofabric.core;

/** Base implementation for metadata-level compatibility backends. */
public abstract class MetadataCompatibilityBackend implements CompatibilityBackend {
    private final LoaderKind kind;

    protected MetadataCompatibilityBackend(LoaderKind kind) {
        this.kind = kind;
    }

    @Override
    public final LoaderKind kind() {
        return kind;
    }

    @Override
    public CompatibilityDecision translate(ModDescriptor mod, MinecraftTarget target) {
        if (mod.loader() != kind) {
            return new CompatibilityDecision(mod, false,
                    "backend mismatch: expected " + kind + " metadata");
        }
        return new CompatibilityDecision(mod, true,
                kind + " metadata translated into NeoFabric " + target.runtimeVersion() + " backend");
    }
}
