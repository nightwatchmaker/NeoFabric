package org.neofabric.core;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/** Loader-neutral runtime foundation; Minecraft bootstrap adapters will plug into this. */
public final class NeoFabricRuntime {
    private final EventBus eventBus = new EventBus();
    private final List<ModDescriptor> mods = new ArrayList<>();

    public EventBus events() {
        return eventBus;
    }

    public void addMod(ModDescriptor descriptor) {
        mods.add(descriptor);
    }

    public List<ModDescriptor> mods() {
        return mods.stream().sorted(Comparator.comparing(ModDescriptor::id)).toList();
    }

    public String compatibilityReport() {
        long fabric = mods.stream().filter(mod -> mod.loader() == LoaderKind.FABRIC).count();
        long neoforge = mods.stream().filter(mod -> mod.loader() == LoaderKind.NEOFORGE).count();
        long forge = mods.stream().filter(mod -> mod.loader() == LoaderKind.FORGE).count();
        long unknown = mods.stream().filter(mod -> mod.loader() == LoaderKind.UNKNOWN).count();
        return "NeoFabric compatibility report: Fabric=" + fabric
                + ", NeoForge=" + neoforge + ", Forge=" + forge + ", Unknown=" + unknown;
    }
}
